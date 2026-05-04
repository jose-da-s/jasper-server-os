/*
 * Copyright (C) 2025-2026 the Jasper Server OS Authors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * Copyright (C) 2005-2023. Cloud Software Group, Inc. All Rights Reserved.
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.jaspersoft.jasperserver.war.common;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContext;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.jaspersoft.jasperserver.api.JasperServerAPI;

/**
 * Implements "subsessions" under the HttpSession so multiple client apps can be running simultaneously.
 * To use:
 * - call createClientKey() to get a new clientKey
 * - put the clientKey in all your HTTP requests
 * - if using WebFlow, put it into the flowScope
 * - replace calls to the HttpSession with calls to SessionAttributeManager
 * 
 * The SAM is smart enough to dig the clientKey out of the param map or the flow scope.
 * If no clientKey is found, -1 will be used.
 * Instead of storing objects directly on the session, the SAM stores a map from clientKey to actual values.
 */

@JasperServerAPI
public final class SessionAttributeManager {
    protected static final Log log = LogFactory.getLog(SessionAttributeManager.class);

    private static final SessionAttributeManager _singelTon = new SessionAttributeManager();
    public static final String CLIENT_KEY = "clientKey";
    public static final String MANAGED_ATTR_SUFFIX = "_managedAttribute";
    public static final String ATTRIBUTE_MANAGER_CACHE_SIZE_PARAM = "attributeManagerCacheSize";
    public static final String ATTRIBUTE_MANAGER_EXPIRE_AFTER_PARAM = "attributeManagerExpireAfter";
    //use this if no key provided (assuming value can be shared accross clients)
    public static final long DEFAULT_KEY = -1;
    // to revert to single-instance session attribute mgmt set supportMultipleInstances to false
    private boolean supportMultipleInstances = true;
    public static final int CACHE_SIZE = 500;
    public static final int EXPIRE_AFTER = 60 * 24;
    private ObjectMapper mapper = new ObjectMapper();

    // used to be private, but I don't see the point of that, because there is no per-instance data.
    // All the calls are synchronized, which serializes session access, but synching on a singleton is the wrong granularity.
    // It could possibly cause unnecessary blocking, but the calls don't do much except set/get session values
    public SessionAttributeManager(){
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }


    /**
     * Get singleton attribute manager instance
     * TODO do we really need a singleton?
     * @return instance of manager
     */
    public synchronized static SessionAttributeManager getInstance(){
        return SessionAttributeManager._singelTon;
    }

    /**
     * Convenience method to access session map key from an requestObject
     * @param requestObject - can be HttpServletRequest or webflow RequestContext
     * @return
     */
    public synchronized String getClientKeyAsString(Object requestObject) {
    	if (requestObject instanceof HttpServletRequest) {
    		String clientKey = ((HttpServletRequest)requestObject).getParameter(CLIENT_KEY);
            if (clientKey == null) {
                clientKey = (String)((HttpServletRequest)requestObject).getAttribute(CLIENT_KEY);
            }
            return clientKey;
    	}
		if (requestObject instanceof RequestContext) {
			Object value = ((RequestContext)requestObject).getFlowScope().get(CLIENT_KEY);
			return value != null ? value.toString() : null;
		}
		return null;
    }

    /**
     * Convenience method to access session from an requestObject
     * @param requestObject - can be HttpServletRequest or webflow RequestContext
     * @return
     */
    public synchronized HttpSession getSession(Object requestObject) {
    	if (requestObject instanceof HttpServletRequest) {
    		return ((HttpServletRequest)requestObject).getSession();
    	}
		if (requestObject instanceof RequestContext) {
			ServletExternalContext extContext = (ServletExternalContext) ((RequestContext)requestObject).getExternalContext();
			return ((HttpServletRequest) extContext.getNativeRequest()).getSession();
		}
		return null;
    }

    /**
     * create a new client key if you are starting up an ad hoc session
     * @return long value suitable for using as a clientKey
     */
    public long createClientKey() {
    	return (long) Math.floor(Math.random() * 9999999999999d);
    }

    /**
     * Sets a session attribute to the supplied value when a client key has already been set on the request or flow scope.
     * @param name The name of attribute we are setting
     * @param obj the value to set
     * @param requestObject - can be HttpServletRequest or webflow RequestContext
     */
    public synchronized void setSessionAttribute(String name, Object obj, Object requestObject){
    	setSessionAttribute(name, obj, null, requestObject);
    }

    /**
     * Sets a session attribute to the supplied value.
     * This version is used when you have a new session, so client key isn't in the params or flow scope yet.
     * @param attrName The name of attribute we are setting
     * @param obj the value to set
     * @param clientKey client key to use (or null to get it from req object)
     * @param requestObject - can be HttpServletRequest or webflow RequestContext
     */
    @SuppressWarnings("unchecked")
    public synchronized void setSessionAttribute(final String attrName, Object obj, Long clientKey, Object requestObject){

        final HttpSession session = getSession(requestObject);

        if (!supportMultipleInstances) {
        	//old fashioned way
        	session.setAttribute(attrName, obj);
        	return;
        }

        String cacheSizeValue = session.getServletContext().getInitParameter(ATTRIBUTE_MANAGER_CACHE_SIZE_PARAM);
        int cacheSize = StringUtils.isNumeric(cacheSizeValue) ? Integer.parseInt(cacheSizeValue) : CACHE_SIZE;

        String expireAfterValue = session.getServletContext().getInitParameter(ATTRIBUTE_MANAGER_EXPIRE_AFTER_PARAM);
        int expireAfter = StringUtils.isNumeric(expireAfterValue) ? Integer.parseInt(expireAfterValue) : EXPIRE_AFTER;

        final Cache<Long, Object> attributeMap;

        // if clientKey not supplied, look in request
        if (clientKey == null) {
        	clientKey = getClientKey(requestObject);
        }
        
        final String name = attrName + MANAGED_ATTR_SUFFIX; //tag this as a managed attribute
        
        if(session.getAttribute(name) == null || !(session.getAttribute(name) instanceof Cache)){
            //create map, add object to map then add to session object
            attributeMap = CacheBuilder.newBuilder()
                    .maximumSize(cacheSize)
                    .expireAfterAccess(expireAfter, TimeUnit.MINUTES)
                    .removalListener(new RemovalListener<Object, Object>() {
                        @Override
                        public void onRemoval(RemovalNotification<Object, Object> notification) {
                            log(CacheEventType.CACHE_ITEM_REMOVED, name,
                                    notification.getValue(), notification.getKey(), session,
                                    notification.getCause().toString());
                        }
                    })
                    .build();
            session.setAttribute(name, attributeMap);
            log(CacheEventType.CACHE_CREATED, name, obj, clientKey, session, "");
            if (obj != null) {
            attributeMap.put(clientKey, obj);
            }
            log(CacheEventType.CACHE_ITEM_ADDED, name, obj, clientKey, session, "");
        } else {
            attributeMap = (Cache<Long, Object>) session.getAttribute(name);
            log(CacheEventType.CACHE_REUSED, name, obj, clientKey, session, "");
            if (obj != null) {
        attributeMap.put(clientKey, obj);
        }
            log(CacheEventType.CACHE_ITEM_ADDED, name, obj, clientKey, session, "");
    }
    }

    public synchronized Object getSessionAttribute(String name, Object requestObject){
    	return getSessionAttribute(name, null, requestObject);
    }
    /**
     * Gets a session attribute value 
     * @param name - the name of attribute we are getting
     * @param requestObject - can be HttpServletRequest or webflow RequestContext
     * @return previously stored attribute value
     */
    @SuppressWarnings("unchecked")
    public synchronized Object getSessionAttribute(String name, Long clientKey, Object requestObject){
        HttpSession session = getSession(requestObject);
        
        if (!supportMultipleInstances) {
        	//old fashioned way
        	return session.getAttribute(name);
        }        
        
        // if clientKey not supplied, look in request
        if (clientKey == null) {
        	clientKey = getClientKey(requestObject);
        }
        name = name + MANAGED_ATTR_SUFFIX; //tag this as a managed attribute
        Object obj = null;
        Cache<Long, Object> values = (Cache<Long, Object>)session.getAttribute(name);
	        if(values != null){
            obj = values.getIfPresent(clientKey);
	        }
        return obj;
        }

    /**
     * Not thread safe
     */
    public void invalidateAttribute(String name, HttpSession session) {
        Object cache = session.getAttribute(name + MANAGED_ATTR_SUFFIX);
        if(cache instanceof Cache) {
            ((Cache) cache).invalidateAll();
            log(CacheEventType.CACHE_INVALIDATED, name + MANAGED_ATTR_SUFFIX, null, null, session, "");
        }
    }

    /**
     * Not thread safe
     */
    public void removeAttribute(String name, HttpSession session) {
        session.removeAttribute(name + MANAGED_ATTR_SUFFIX);
    }

    /*
     * look for clientKey on request
     */
    Long getClientKey(Object requestObject) {
        String clientKeyString = getClientKeyAsString(requestObject);

        if(clientKeyString != null && clientKeyString.length() > 0) {
            try {
            	return Long.parseLong(clientKeyString);
            } catch (NumberFormatException e) {
                return DEFAULT_KEY;
                //does this need to be bubbled up to the request? may need to create some sort of setParameter()
            }
	    } else {
            //no param value sent - assume attribute is ok to be shared between clients - so use default key
	    	return DEFAULT_KEY;
        }
    }

    public void log(CacheEventType type, String name, Object obj, Object clientKey, HttpSession session, String details) {
        if (log.isDebugEnabled()) {
            Cache<Long, Object> cache = (Cache<Long, Object>) session.getAttribute(name);
            CacheEvent event;
            if (type == CacheEventType.CACHE_CREATED) {
                event = new CacheEvent(name, cache, session, details);
            } else {
                event = new CacheItemEvent(name, cache, session, details, clientKey, obj);
            }
            event.type = type;
            try {
                log.debug(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(event));
            } catch (IOException e) {
                log.debug("Failed attempt to log cache event");
            }
        }
    }

    class CacheEvent {
        public CacheEventType type;
        public String cache;
        public long cacheSize;
        public String cacheStats;
        public String session;
        public String cacheItems;
        public long threadId;
        public String threadName;
        public String stack;
        public String details;

        CacheEvent(String name, Cache<Long, Object> cache, HttpSession session, String details) {
            this.session = session.getId() + "@" + ObjectUtils.identityToString(session);
            this.cache = name + "@" + ObjectUtils.identityToString(cache);
            this.cacheSize = cache != null ? cache.size() : -1;
            this.cacheStats = cache != null ? ToStringBuilder.reflectionToString(cache.stats()) : "";
            this.cacheItems = cache != null ? cache.asMap().toString() : "";
            this.threadId = Thread.currentThread().getId();
            this.threadName = Thread.currentThread().getName();
            this.stack = Throwables.getStackTraceAsString(new Throwable());
            this.details = details;
        }
    }
    class CacheItemEvent extends CacheEvent {
        public String clientKey;
        public String valueRef;
        public String value;

        CacheItemEvent(String name, Cache<Long, Object> cache, HttpSession session, String details, Object clientKey, Object obj) {
            super(name, cache, session, details);
            this.clientKey = ObjectUtils.toString(clientKey);
            this.valueRef = ObjectUtils.identityToString(obj);
            this.value = ToStringBuilder.reflectionToString(obj);
        }
    }
    public enum CacheEventType { CACHE_CREATED, CACHE_REUSED, CACHE_ITEM_ADDED, CACHE_ITEM_REMOVED, CACHE_INVALIDATED }
}

