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
package com.jaspersoft.jasperserver.remote.connection.jdbc;

import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.common.error.handling.SecureExceptionHandler;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.BaseJdbcDataSource;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.DataSourceServiceDefinition;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.DataSourceServiceFactoryImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.JdbcReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.JndiJdbcReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.client.JdbcReportDataSourceImpl;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceServiceFactory;
import com.jaspersoft.jasperserver.remote.connection.ContextCreationFailedException;
import com.jaspersoft.jasperserver.remote.connection.UnsupportedDataSourceException;
import com.jaspersoft.jasperserver.remote.connection.datadiscovery.Connector;
import net.sf.jasperreports.engine.JRParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@Service
public class JdbcConnector<ConnectionDescriptionType extends Resource> implements Connector<Connection, ConnectionDescriptionType> {
    private static final String CHECK_AWS_ACCESS_KEY = "The security token included in the request is invalid";
    private static final String CHECK_AWS_SECRET_KEY =  "Check your AWS Secret Access Key and signing method";
    private static final String CHECK_ARN_ROLE_ASSIGNED = "'roleArn' failed to satisfy constraint: Member must have length greater than or equal to 20";
    private static final String CHECK_ARN_ASSUME_ROLE = "not authorized to perform: sts:AssumeRole";
    private static final String AWS_ATHENA_SIMBA_DRIVER_NAME = "com.simba.athena.jdbc42.Driver";

    private Map<Connection, BaseJdbcDataSource> openConnections = new HashMap<Connection, BaseJdbcDataSource>();
    @jakarta.annotation.Resource(name = "dataSourceServiceFactories")
    private DataSourceServiceFactoryImpl dataSourceServiceFactory;
    @Autowired
    private ApplicationContext applicationContext;
    @jakarta.annotation.Resource
    private SecureExceptionHandler secureExceptionHandler;
    @jakarta.annotation.Resource
    private MessageSource messageSource;
    @jakarta.annotation.Resource(name = "concreteRepository")
    private RepositoryService repository;

    @Override
    public Connection openConnection(ConnectionDescriptionType connectionDescriptor) {
        final HashMap parameterValues = new HashMap();
        BaseJdbcDataSource dataSourceService = getDataSourceService(connectionDescriptor);
        dataSourceService.setReportParameterValues(parameterValues);
        Connection connection = (Connection) parameterValues.get(JRParameter.REPORT_CONNECTION);
        openConnections.put(connection, dataSourceService);
        return connection;
    }

    @Override
    public void closeConnection(Connection connection) {
        openConnections.remove(connection).closeConnection();
    }

    public void testConnection(ConnectionDescriptionType connectionDescriptor) {
        boolean passed = false;
        Throwable exception = null;

        try {
            passed = getDataSourceService(connectionDescriptor).testConnection();
        } catch (SQLException vex) {
            if (vex.getMessage().indexOf("[JI_CONNECTION_VALID]") >= 0) passed = true;
            exception = vex;
        } catch (Throwable e) {
            exception = e;
        }
        if (!passed) {
            if (connectionDescriptor instanceof JndiJdbcReportDataSource) {
                final String jndiName = ((JndiJdbcReportDataSource) connectionDescriptor).getJndiName();
                throw new ContextCreationFailedException(jndiName, "jndiName", "Invalid JNDI name: " + jndiName,
                        exception, secureExceptionHandler);
            } else if(connectionDescriptor instanceof JdbcReportDataSourceImpl) {
                if (((JdbcReportDataSource) connectionDescriptor).getDriverClass()
                    .equals(AWS_ATHENA_SIMBA_DRIVER_NAME)) {
                    errorMessageForAthenaDriverConnection(connectionDescriptor, exception);
                } else{
                    throw new ContextCreationFailedException(connectionDescriptor, exception,
                        secureExceptionHandler);
                }
            } else{
                throw new ContextCreationFailedException(connectionDescriptor, exception,
                    secureExceptionHandler);
            }
        }
    }

    protected BaseJdbcDataSource getDataSourceService(ConnectionDescriptionType connectionDescription) {
        if (connectionDescription instanceof JdbcReportDataSource) {
            ensureJdbcDataSourcePassword((JdbcReportDataSource) connectionDescription);
        }
        final DataSourceServiceDefinition dataSourceServiceDefinition = dataSourceServiceFactory
                .getServiceDefinitionMap().get(connectionDescription.getResourceType());
        if (dataSourceServiceDefinition == null) {
            throw new IllegalStateException("Unsupported connection description type. Check configuration");
        }
        final ReportDataSourceServiceFactory reportDataSourceServiceFactory = applicationContext
                .getBean(dataSourceServiceDefinition.getServiceBeanName(), ReportDataSourceServiceFactory.class);
        final ReportDataSourceService service = reportDataSourceServiceFactory.createService((ReportDataSource) connectionDescription);
        if(!(service instanceof BaseJdbcDataSource)){
            throw new UnsupportedDataSourceException("Impossible to obtain JDBC connection from " +
                    connectionDescription.getResourceType());
        }
        return (BaseJdbcDataSource) service;
    }

    protected void ensureJdbcDataSourcePassword(JdbcReportDataSource jdbcDataSource) {
        final String password = jdbcDataSource.getPassword();
        final String passwordSubstitution = messageSource.getMessage("input.password.substitution", null, LocaleContextHolder.getLocale());
        if ((password == null || password.equals(passwordSubstitution)) && jdbcDataSource.getURIString() != null) {
            JdbcReportDataSource existingDs = (JdbcReportDataSource) repository
                    .getResource(ExecutionContextImpl.getRuntimeExecutionContext(), jdbcDataSource.getURIString());
            if (existingDs != null) {
                jdbcDataSource.setPassword(existingDs.getPassword());
            }
        }
    }

    private void errorMessageForAthenaDriverConnection(ConnectionDescriptionType connectionDescriptor, Throwable exception){
        if(exception.getMessage().contains(CHECK_AWS_ACCESS_KEY)){
            throw new ContextCreationFailedException(connectionDescriptor,
                messageSource.getMessage("resource.dataSource.jdbc.accessKey", null, LocaleContextHolder.getLocale()),
                messageSource.getMessage("error.aws.key.is.invalid", null, LocaleContextHolder.getLocale()),
                exception, secureExceptionHandler);
        } else if(exception.getMessage().contains(CHECK_AWS_SECRET_KEY)){
            throw new ContextCreationFailedException(connectionDescriptor,
                messageSource.getMessage("resource.dataSource.jdbc.secretKey", null, LocaleContextHolder.getLocale()),
                messageSource.getMessage("error.aws.secret.key.is.invalid", null, LocaleContextHolder.getLocale()),
                exception, secureExceptionHandler);
        } else if(exception.getMessage().contains(CHECK_ARN_ASSUME_ROLE) || exception.getMessage().contains(CHECK_ARN_ROLE_ASSIGNED)){
            throw new ContextCreationFailedException(connectionDescriptor,
                messageSource.getMessage("resource.dataSource.jdbc.arn", null, LocaleContextHolder.getLocale()),
                messageSource.getMessage("error.aws.arn.is.invalid", null, LocaleContextHolder.getLocale()),
                exception, secureExceptionHandler);
        } else{
            throw new ContextCreationFailedException(connectionDescriptor, exception,
                secureExceptionHandler);
        }
    }
}

