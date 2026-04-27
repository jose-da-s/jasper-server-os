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

import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.error.handling.SecureExceptionHandler;
import com.jaspersoft.jasperserver.api.engine.common.service.EngineService;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.CustomReportDataSourceServiceFactory;
import com.jaspersoft.jasperserver.api.metadata.common.service.JsonMarshaller;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ConnectionTestingDataSourceService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataAdapterService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceService;
import com.jaspersoft.jasperserver.dto.connection.metadata.PartialMetadataOptions;
import com.jaspersoft.jasperserver.dto.resources.ClientCustomDataSource;
import com.jaspersoft.jasperserver.dto.resources.ClientProperty;
import com.jaspersoft.jasperserver.remote.connection.jdbc.JdbcDataSourceMetadataBuilder;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.exception.NotAcceptableException;
import com.jaspersoft.jasperserver.remote.resources.converters.CustomDataSourceResourceConverter;
import com.jaspersoft.jasperserver.remote.resources.converters.ToServerConversionOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@Service
public class CustomDataSourceContextStrategy implements
        ContextManagementStrategy<ClientCustomDataSource, ClientCustomDataSource>,
        GenericTypeMetadataBuilder<ClientCustomDataSource>,
        ContextParametrizedMetadataBuilder<ClientCustomDataSource, PartialMetadataOptions> {
    private final static Log log = LogFactory.getLog(CustomDataSourceContextStrategy.class);
    @Resource(name = "cdsPropertiesToIgnore")
    private Set<String> propertiesToIgnore;
    @Resource(name = "customDataSourceServiceFactory")
    private CustomReportDataSourceServiceFactory customDataSourceFactory;
    @Resource
    private CustomDataSourceResourceConverter customDataSourceResourceConverter;
    @Resource(name = "concreteRepository")
    private RepositoryService repository;
    @Resource(name = "engineService")
    private EngineService engine;
    @Resource
    private SecureExceptionHandler secureExceptionHandler;
    @Resource
    private JsonMarshaller jsonMarshaller;
    @Resource
    private JdbcDataSourceMetadataBuilder jdbcDataSourceMetadataBuilder;

    @Override
    public ClientCustomDataSource createContext(ExecutionContext ctx, ClientCustomDataSource contextDescription, Map<String, Object> data) throws IllegalParameterValueException {
        boolean passed = false;
        Exception exception = null;

        try {
            final CustomReportDataSource reportDataSource = toServer(ctx, contextDescription);
            ReportDataSourceService service = customDataSourceFactory.createService(reportDataSource);

            if (service instanceof ConnectionTestingDataSourceService) {
                passed = ((ConnectionTestingDataSourceService) service).testConnection();
            } else {
                passed = true;
            }

            if (service instanceof ReportDataAdapterService) {
                // try to generate metadata to validate if file is accessible and valid
                engine.getMetaDataFromConnector(reportDataSource);
            }
        } catch (Exception e) {
            exception = e;
        }
        if (!passed || exception != null) {
            if (exception instanceof ErrorDescriptorException) {
                throw (ErrorDescriptorException) exception;
            } else {
                throw new ContextCreationFailedException(contextDescription, null, "Connection failed", exception, secureExceptionHandler);
            }
        }
        return contextDescription;
    }

    protected CustomReportDataSource toServer(ExecutionContext ctx,ClientCustomDataSource clientCustomDataSource) {
        CustomReportDataSource ds = customDataSourceResourceConverter.
                toServer(ctx, clientCustomDataSource, ToServerConversionOptions.getDefault().setSuppressValidation(true));
        // On edit data source we set the null as value for the password if not changed
        // If we get the null from client then set the password from original data source (if it exists)
        if (ds.getPropertyMap() != null && ds.getPropertyMap().get("password") == null && clientCustomDataSource.getUri() != null) {
            CustomReportDataSource existingDs = (CustomReportDataSource) repository.getResource(null, clientCustomDataSource.getUri());
            if (existingDs != null) {
                ds.getPropertyMap().put("password", existingDs.getPropertyMap().get("password"));
            }
        }
        return ds;
    }

    @Override
    public void deleteContext(ClientCustomDataSource contextDescription, Map<String, Object> data) {
        // nothing to clean. Do nothing
    }

    @Override
    public ClientCustomDataSource getContextForClient(ClientCustomDataSource contextDescription, Map<String, Object> data, Map<String, String[]> additionalProperties) {
        ClientCustomDataSource copy = new ClientCustomDataSource(contextDescription);
        final List<ClientProperty> properties = copy.getProperties();
        if (properties != null) {
            Iterator<ClientProperty> iterator = properties.iterator();
            for (; iterator.hasNext(); ) {
                ClientProperty property = iterator.next();
                if (propertiesToIgnore.contains(property.getKey())) iterator.remove();
            }
        }
        return copy;
    }

    public boolean isMetadataSupported(ExecutionContext ctx, ClientCustomDataSource clientCustomDataSource, String metadataClientType) {
        // disabling metadata building for now. Let file data source project put
        // specific logic for enabling of metadata for file custom data source only
        return false;
    }

    @Override
    public Object build(ClientCustomDataSource context, Map<String, String[]> options, Map<String, Object> contextData) {
        Object result;
        try {
            // custom data source metadata is supported for those custom data sources, that are implemented in a
            // JDBC like way (via TEIID). So, just delegating and handle exceptions in specific way
            result = jdbcDataSourceMetadataBuilder.build(context, options, contextData);
        } catch (UnsupportedDataSourceException e) {
            throw e;
        } catch (Exception e) {
            throw new ErrorDescriptorException(e, secureExceptionHandler);
        }
        return result;
    }

    @Override
    public String getMetadataClientResourceType(ClientCustomDataSource connectionDescription) {
        return "repository.cds." + connectionDescription.getDataSourceName() + ".metadata";
    }

    @Override
    public Object build(ClientCustomDataSource context, PartialMetadataOptions options, Map<String, Object> contextData) {
        Object result;
        try {
            // custom data source metadata is supported for those custom data sources, that are implemented in a
            // JDBC like way (via TEIID). So, just delegating and handle exceptions in specific way
            result = jdbcDataSourceMetadataBuilder.build(context, options, contextData);
        } catch (UnsupportedDataSourceException e) {
            throw new NotAcceptableException();
        } catch (Exception e) {
            throw new ErrorDescriptorException(e, secureExceptionHandler);
        }
        return result;
    }

}

