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
package com.jaspersoft.jasperserver.inputcontrols.cascade.cache;

import com.jaspersoft.jasperserver.api.metadata.user.service.impl.UserManagerServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import javax.cache.Cache;
import javax.cache.CacheManager;

import java.util.concurrent.TimeUnit;


/**
 * ControlLogicCacheManagerImpl
 * @author jwhang
 */

public class ControlLogicCacheManagerImpl implements ControlLogicCacheManager, InitializingBean {

    private Cache<Object, SessionCache> inputControlCache;
    private CacheManager cacheManager;
    private static final Logger log = LogManager.getLogger(ControlLogicCacheManagerImpl.class);
    private long cacheCleanTriggerTime = TimeUnit.MINUTES.toMillis(30);

    public ControlLogicCacheManagerImpl(){
    }

    public void afterPropertiesSet() throws Exception {
        if (cacheManager != null) {
            inputControlCache = cacheManager.getCache("inputControlCache");
            // Spring Cache automatically handles cache expiration through ehcache.xml configuration (timeToIdleSeconds, timeToLiveSeconds).
            // Manual timer-based cleanup via EhCacheCleanerRunner is no longer needed.
        }
    }

    public void clearCache(){
        if (inputControlCache != null) {
            inputControlCache.clear();
        }
    }

    public SessionCache getSessionCache() {
        String key = getSessionCacheKey();
        SessionCache cache = getItem(key);
        if (cache == null) {
            //create a new cache.
            cache = new SessionCacheImpl();
            addItem(key, cache);
        }
        return cache;
    }

    public void addItem(Object key, SessionCache value) {
        logEvent("PUT", key, value);
        if (inputControlCache != null) {
            inputControlCache.put(key, value);
        }
    }

    public SessionCache getItem(Object key) {
        if (inputControlCache != null) {
        	SessionCache value = inputControlCache.get(key);
            if (value != null) {
                log.debug("element found for key: {}", key);
                logEvent("GET", key, value);
                return value;
            }
        }
        return null;
    }

    public void shutdown() {
        log.warn("ControlLogicCacheManagerImpl shutdown called. This normal shutdown operation.");
        if (inputControlCache != null) {
            inputControlCache.clear();
        }
    }

    public String getSessionCacheKey() {
        String key;
        try {
            key = UserManagerServiceImpl.getCurrentUserQualifiedName();
            log.debug("Current Session key = {} time = {}", key, System.currentTimeMillis());
        } catch (IllegalStateException e) {
            // For case when code is invoked on some internal event without real HTTP request.
            key = Long.toString(Thread.currentThread().getId());
        }
        if (key == null) key = Long.toString(Thread.currentThread().getId());
    	return key;
    }

    private void logEvent(String event, Object key, SessionCache value) {
        if (log.isDebugEnabled()) {
            log.debug("{} key = {}, value = {}, time = {}",
                    event, key, (value != null ? value.toString() : null), System.currentTimeMillis());
        }
    }

    public void setInputControlCache(Cache<Object, SessionCache> inputControlCache) {
        this.inputControlCache = inputControlCache;
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void setCacheCleanTriggerTime(long cacheCleanTriggerTime) {
        // Convert seconds into millisecond equivalent
        this.cacheCleanTriggerTime = TimeUnit.SECONDS.toMillis(cacheCleanTriggerTime);
    }
}




















