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
package com.jaspersoft.jasperserver.remote.connection;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.error.handling.SecureExceptionHandler;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.BeanReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceServiceFactory;
import com.jaspersoft.jasperserver.dto.resources.ClientBeanDataSource;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.resources.converters.BeanDataSourceResourceConverter;
import com.jaspersoft.jasperserver.remote.resources.converters.ToServerConversionOptions;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Map;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@Service
public class BeanContextStrategy implements ContextManagementStrategy<ClientBeanDataSource, ClientBeanDataSource> {
    @Resource(name = "beanDataSourceServiceFactory")
    private ReportDataSourceServiceFactory dataSourceFactory;
    @Resource
    private BeanDataSourceResourceConverter beanDataSourceResourceConverter;
    @Resource
    private SecureExceptionHandler secureExceptionHandler;

    @Override
    public ClientBeanDataSource createContext(ExecutionContext ctx, ClientBeanDataSource contextDescription, Map<String, Object> data) throws IllegalParameterValueException {
        // functionality of this method is copied from com.jaspersoft.jasperserver.war.action.DataSourceAction.testBeanDataSource()
        Exception exception = null;
        boolean passed = false;
        BeanReportDataSource beanReportDataSource = beanDataSourceResourceConverter.
                toServer(ctx, contextDescription, ToServerConversionOptions.getDefault().setSuppressValidation(true));

        try {
            ReportDataSourceService beanReportDataSourceService = dataSourceFactory.createService(beanReportDataSource);
            if (beanReportDataSourceService != null) {
                passed = true;
            }
        } catch(Exception e) {
            exception = e;
        }

        if(!passed){
            throw new ContextCreationFailedException(contextDescription, exception, secureExceptionHandler);
        }


        return contextDescription;
    }

    @Override
    public void deleteContext(ClientBeanDataSource contextDescription, Map<String, Object> data) {
    }

    @Override
    public ClientBeanDataSource getContextForClient(ClientBeanDataSource contextDescription, Map<String, Object> data, Map<String, String[]> additionalProperties) {
        // no hidden attributes
        return contextDescription;
    }
}

