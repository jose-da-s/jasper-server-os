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
package com.jaspersoft.jasperserver.inputcontrols.cascade;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.engine.common.service.EngineService;
import com.jaspersoft.jasperserver.api.engine.common.service.ReportInputControlsInformation;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.EhcacheEngineService;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.ReportLoadingService;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControlsContainer;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Query;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceService;
import com.jaspersoft.jasperserver.inputcontrols.cascade.cache.SessionCache;
import com.jaspersoft.jasperserver.inputcontrols.cascade.token.FilterResolver;
import com.jaspersoft.jasperserver.inputcontrols.cascade.cache.ControlLogicCacheManager;
import org.apache.commons.collections.OrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides cached access to some methods of EngineService.
 * @author Anton Fomin
 * @version $Id$
 */
@Service
public class CachedEngineService {

    private static final Log log = LogFactory.getLog(CachedEngineService.class);

    @Resource
    private ControlLogicCacheManager controlLogicCacheManager;

    @Resource
    private CachedRepositoryService cachedRepositoryService;

    @Resource(name = "${bean.engineService}")
    protected EngineService engineService;

    @Resource
    protected EngineService ehcacheEngineService;

    @Resource
    private FilterResolver filterResolver;

    @jakarta.annotation.Resource(name = "${bean.reportLoadingService}")
    private ReportLoadingService reportLoadingService;

    @Resource(name = "inputControlsCachingEnabled")
    private boolean doCache;

    public ReportInputControlsInformation getReportInputControlsInformation(ExecutionContext exContext, InputControlsContainer container, Map<String, Object> initialParameters) {
        final SessionCache sessionCache = controlLogicCacheManager.getSessionCache();
        ReportInputControlsInformation infos = sessionCache.getCacheInfo(ReportInputControlsInformation.class, container.getURI());
        if (!doCache || infos == null) {
            infos = engineService.getReportInputControlsInformation(exContext, container, initialParameters);
            sessionCache.setCacheInfo(ReportInputControlsInformation.class, container.getURI(), infos);
        }
        return infos;
    }

    public List<InputControl> getInputControls(InputControlsContainer container) {
        final SessionCache sessionCache = controlLogicCacheManager.getSessionCache();
        // Cache Input Controls as List class
        List<InputControl> inputControls = sessionCache.getCacheInfo(List.class, container.getURI());
        if (!doCache || inputControls == null) {
            inputControls = reportLoadingService.getInputControls(ExecutionContextImpl.getRuntimeExecutionContext(), container);
            sessionCache.setCacheInfo(List.class, container.getURI(), inputControls);
        }
        //make own copy of list for each thread
        List<InputControl> copy = new ArrayList<InputControl>(inputControls.size());
        copy.addAll(inputControls);
        return copy;
    }

    public Map getSLParameters(ReportDataSource dataSource) {
        final SessionCache sessionCache = controlLogicCacheManager.getSessionCache();
        // Cache SL parameters as Map class
        Map parameters = sessionCache.getCacheInfo(Map.class, dataSource.getURIString());
        if (!doCache || parameters == null) {
            parameters = new HashMap();
            ReportDataSourceService dataSourceService = engineService.createDataSourceService(dataSource);
            dataSourceService.setReportParameterValues(parameters);
            sessionCache.setCacheInfo(Map.class, dataSource.getURIString(), parameters);
        }
        //make own copy of list for each thread
        Map copy = new HashMap(parameters.size());
        copy.putAll(parameters);
        return copy;
    }

    public ReportInputControlsInformation getReportInputControlsInformation(ReportUnit reportUnit) {
        return getReportInputControlsInformation(ExecutionContextImpl.getRuntimeExecutionContext(), reportUnit, null);
    }

    public OrderedMap executeQuery(ExecutionContext context, ResourceReference queryReference, String keyColumn, String[] resultColumns,
			ResourceReference defaultDataSourceReference, Map<String, Object> parameterValues, Map<String, Class<?>> parameterTypes, /* Temporary for test */String controlName)
            throws CascadeResourceNotFoundException {

        final Query query = cachedRepositoryService.getResource(Query.class, queryReference);
        final String cacheKey = String.valueOf(filterResolver.getCacheKey(query.getSql(), parameterValues, keyColumn, resultColumns));
        final SessionCache sessionCache = controlLogicCacheManager.getSessionCache();
        OrderedMap results = sessionCache.getCacheInfo(OrderedMap.class, cacheKey);

        boolean refresh = parameterValues != null && parameterValues.containsKey(EhcacheEngineService.IC_REFRESH_KEY);
        if (!doCache || results == null || refresh) {
            results = ehcacheEngineService.executeQuery(context != null ? context : ExecutionContextImpl.getRuntimeExecutionContext(),
                    queryReference, keyColumn, resultColumns, defaultDataSourceReference, parameterValues, parameterTypes, false);
            sessionCache.setCacheInfo(OrderedMap.class, cacheKey, results);
            log.debug("Database query \"" + cacheKey + "\"");
        } else {
            log.debug("Cached query \"" + cacheKey + "\"");
        }
        return results;
    }

    public OrderedMap executeQuery(ExecutionContext context, ResourceReference queryReference, String keyColumn, String[] resultColumns,
			ResourceReference defaultDataSourceReference, Map<String, Object> parameterValues, /* Temporary for test */String controlName)
            throws CascadeResourceNotFoundException {
        return executeQuery(context, queryReference, keyColumn, resultColumns, defaultDataSourceReference, parameterValues, null, controlName);
    }


    public ControlLogicCacheManager getControlLogicCacheManager() {
        return controlLogicCacheManager;
    }

    public void setControlLogicCacheManager(ControlLogicCacheManager controlLogicCacheManager) {
        this.controlLogicCacheManager = controlLogicCacheManager;
    }

    public EngineService getEngineService() {
        return engineService;
    }

    public void setEngineService(EngineService engineService) {
        this.engineService = engineService;
    }

    public boolean isDoCache() {
        return doCache;
    }

    public void setDoCache(boolean doCache) {
        this.doCache = doCache;
    }

    public void setCachedRepositoryService(CachedRepositoryService cachedRepositoryService) {
        this.cachedRepositoryService = cachedRepositoryService;
    }
}

