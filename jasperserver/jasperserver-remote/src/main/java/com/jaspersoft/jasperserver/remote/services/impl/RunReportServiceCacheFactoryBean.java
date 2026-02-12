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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static java.lang.String.format;


/**
 * Configuring Run Report cache after cache manager is ready.
 *
 * @author esytnik, schubar
 */
@Component
public class RunReportServiceCacheFactoryBean implements FactoryBean<Cache>, InitializingBean {
    private static final Log log = LogFactory.getLog(RunReportServiceCacheFactoryBean.class);

    private static final String DEFAULT_CACHE_NAME = "RRSCache";

    @Resource(name = "springCacheManager")
    private CacheManager cacheManager;

    private String cacheName = DEFAULT_CACHE_NAME;

    private Cache cache;

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    @Override
    public Cache getObject() {
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

        // Register cache event listener if using EhCache
        if (cacheManager instanceof EhCacheCacheManager) {
            net.sf.ehcache.Ehcache ehcache = (net.sf.ehcache.Ehcache) cache.getNativeCache();

            if (ehcache.getCacheEventNotificationService() == null) {
                throw new IllegalStateException(format("%s Cache is not initialized.", FAIL_MSG));
            }

            final net.sf.ehcache.event.RegisteredEventListeners cacheEventNotificationService =
                    ehcache.getCacheEventNotificationService();

            cacheEventNotificationService.getCacheEventListeners().clear();
            cacheEventNotificationService.registerListener(new RunReportCacheEventListener());
        }
    }

    static class RunReportCacheEventListener implements net.sf.ehcache.event.CacheEventListener {
        // freaking Oracle edge case
        public Object clone() {
            return this.clone();
        }

        @Override
        public void notifyRemoveAll(net.sf.ehcache.Ehcache arg0) {
        }

        @Override
        public void notifyElementUpdated(net.sf.ehcache.Ehcache arg0, net.sf.ehcache.Element arg1) {
        }

        @Override
        public void notifyElementRemoved(net.sf.ehcache.Ehcache arg0, net.sf.ehcache.Element element) {
            if (log.isDebugEnabled()) {
                log.debug("33816 DEBUG: remove element: " + element.getObjectKey());
            }
        }

        @Override
        public void notifyElementPut(net.sf.ehcache.Ehcache arg0, net.sf.ehcache.Element element) {
            if (log.isDebugEnabled()) {
                log.debug("33816 DEBUG: put element: " + element.getObjectKey());
            }
        }

        @Override
        public void notifyElementExpired(net.sf.ehcache.Ehcache arg0, net.sf.ehcache.Element arg1) {
        }

        @Override
        public void notifyElementEvicted(net.sf.ehcache.Ehcache arg0, net.sf.ehcache.Element element) {
            String requestId = (String) element.getObjectKey();
            Pair<String, ReportExecution> pair = (Pair<String, ReportExecution>) element.getObjectValue();
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
            if (log.isDebugEnabled()) {
                log.debug("33816 DEBUG: evicted element: " + requestId);
            }
        }

        @Override
        public void dispose() {
        }

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
}
