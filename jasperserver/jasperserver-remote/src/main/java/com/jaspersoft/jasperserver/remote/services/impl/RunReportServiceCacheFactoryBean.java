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
package com.jaspersoft.jasperserver.remote.services.impl;

import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.ReportUnitResult;
import com.jaspersoft.jasperserver.dto.executions.ExecutionStatus;
import com.jaspersoft.jasperserver.remote.services.ReportExecution;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JRVirtualizer;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.ReportContext;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import javax.cache.Cache;
import javax.cache.CacheManager;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryRemovedListener;

import static java.lang.String.format;


/**
 * Configuring Run Report cache after cache manager is ready.
 *
 * @author esytnik, schubar
 */
@Component
public class RunReportServiceCacheFactoryBean implements FactoryBean<Cache<String, Pair<String, ReportExecution>>>, InitializingBean {
    private static final Log log = LogFactory.getLog(RunReportServiceCacheFactoryBean.class);

    private static final String DEFAULT_CACHE_NAME = "RRSCache";

    @Resource(name = "cacheManager")
    private CacheManager cacheManager;

    private String cacheName = DEFAULT_CACHE_NAME;

    private Cache<String, Pair<String, ReportExecution>> cache;

    private CacheEntryListenerConfiguration<String, Pair<String, ReportExecution>> runReportCacheCreateEventListenerConfiguration;
    private CacheEntryListenerConfiguration<String, Pair<String, ReportExecution>> runReportCacheRemoveEventListenerConfiguration;
    
    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    @Override
    public Cache<String, Pair<String, ReportExecution>> getObject() {
        if (cache == null) {
            throw new IllegalStateException(format("Cache '%s' is not configured.", getCacheName()));
        }

        return cache;
    }

    @Override
    public Class<?> getObjectType() {
        return this.cache != null ? this.cache.getClass() : Cache.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() {
        final String FAIL_MSG = format("Failed to configure %s cache.", getCacheName());

        if (cacheManager == null) {
            throw new IllegalStateException(format("%s Missing 'cacheManager' cache manager.", FAIL_MSG));
        }

        if (log.isDebugEnabled()) {
            log.debug(format("************ RRS *********: manager: %s",
                    cacheManager.getClass().getSimpleName()));
        }

        cache = cacheManager.getCache(getCacheName());

        if (cache == null) {
            throw new IllegalStateException(format("%s Cache '%s' doesn't exist.", FAIL_MSG, getCacheName()));
        }

        if (log.isDebugEnabled()) {
            log.debug(format("************ RRS *********: Cache: %s", cache.getName()));
        }

    	// all of this just to register an event listener... i am dying inside
    	runReportCacheCreateEventListenerConfiguration = new MutableCacheEntryListenerConfiguration<String, Pair<String, ReportExecution>>(
    			new FactoryBuilder.SingletonFactory<RunReportCacheCreateEventListener>(new RunReportCacheCreateEventListener()),
    			new FactoryBuilder.SingletonFactory<CacheEntryEventFilter<String, Pair<String, ReportExecution>>>(new TrueCacheEntryEventFilter()),
    			false,
    			false
    			);
    	runReportCacheRemoveEventListenerConfiguration = new MutableCacheEntryListenerConfiguration<String, Pair<String, ReportExecution>>(
    			new FactoryBuilder.SingletonFactory<RunReportCacheRemoveEventListener>(new RunReportCacheRemoveEventListener()),
    			new FactoryBuilder.SingletonFactory<CacheEntryEventFilter<String, Pair<String, ReportExecution>>>(new TrueCacheEntryEventFilter()),
    			false,
    			false
    			);

        	cache.deregisterCacheEntryListener(runReportCacheCreateEventListenerConfiguration);
        	cache.deregisterCacheEntryListener(runReportCacheRemoveEventListenerConfiguration);
        	cache.registerCacheEntryListener(runReportCacheCreateEventListenerConfiguration);
        	cache.registerCacheEntryListener(runReportCacheRemoveEventListenerConfiguration);
    }

    static class RunReportCacheCreateEventListener implements CacheEntryCreatedListener<String, Pair<String, ReportExecution>> {
    	public RunReportCacheCreateEventListener() {}
        // freaking Oracle edge case (wat? comment and clone() method carried over during ehcache 2->3 migration)
        public Object clone() {
            return this.clone();
        }

        @Override
        public void onCreated(Iterable<CacheEntryEvent<? extends String, ? extends Pair<String, ReportExecution>>> events) {
            if (log.isDebugEnabled()) {
            	for (CacheEntryEvent<? extends String, ? extends Pair<String, ReportExecution>> event : events)
            		log.debug("33816 DEBUG: put element: " + event.getKey());
            }
        }
    }

    static class RunReportCacheRemoveEventListener implements CacheEntryRemovedListener<String, Pair<String, ReportExecution>> {
    	public RunReportCacheRemoveEventListener() {}
        // freaking Oracle edge case (wat? comment and clone() method carried over during ehcache 2->3 migration)
        public Object clone() {
            return this.clone();
        }

        @Override
        public void onRemoved(Iterable<CacheEntryEvent<? extends String, ? extends Pair<String, ReportExecution>>> events) {
        	for (CacheEntryEvent<? extends String, ? extends Pair<String, ReportExecution>> event : events) {
        		if (log.isDebugEnabled()) {
            		log.debug("33816 DEBUG: remove element: " + event.getKey());
        		}
                String requestId = (String) event.getKey();
                Pair<String, ReportExecution> pair = event.getValue();
                ReportExecution execution = pair.getRight();
                try {
                    // cancelReportExecution((String)requestId);
                    if (execution.getStatus() == ExecutionStatus.ready
                            || execution.getStatus() == ExecutionStatus.cancelled) {
                        cleanupRUR(execution.getFinalReportUnitResult());
                    }
                } catch (RuntimeException ex) {
                    log.warn("Report execution cleanup failed: ", ex);
                }
        	}}

        private void cleanupRUR(ReportUnitResult rur) {
            if (rur == null) {
                return;
            }
            // and virtualizer
            JRVirtualizer v = rur.getVirtualizer();
            if (v != null) {
                v.cleanup();
            }
            // clear context
            ReportContext ctx = rur.getReportContext();
            if (ctx != null) {
                ctx.clearParameterValues();
            }
            rur.setReportContext(null);
            // go through pages and clear circular references too
            JasperPrint jp = rur.getJasperPrint();
            if (jp != null) {
                if (jp.getPages() != null) {
                    for (JRPrintPage page : jp.getPages()) {
                        if (page.getElements() != null) {
                            page.getElements().clear();
                        }
                    }
                    jp.getPages().clear();
                }
            }
            // cleanup printer
            rur.setJasperPrintAccessor(null);
        }
    }

    static class TrueCacheEntryEventFilter implements CacheEntryEventFilter<String, Pair<String, ReportExecution>> {
    	public TrueCacheEntryEventFilter(){}
    	@Override
    	public boolean evaluate(CacheEntryEvent<? extends String, ? extends Pair<String, ReportExecution>> e) { return true; };
    }
}
