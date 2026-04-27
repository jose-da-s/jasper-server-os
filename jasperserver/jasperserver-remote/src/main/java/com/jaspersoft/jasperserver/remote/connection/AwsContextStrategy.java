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
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.BaseJdbcDataSource;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.AwsReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceServiceFactory;
import com.jaspersoft.jasperserver.dto.resources.ClientAwsDataSource;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.resources.converters.AwsDataSourceResourceConverter;
import com.jaspersoft.jasperserver.remote.resources.converters.ToServerConversionOptions;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

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
public class AwsContextStrategy implements ContextManagementStrategy<ClientAwsDataSource, ClientAwsDataSource> {
    @Resource
    private MessageSource messageSource;
    @Resource(name = "concreteRepository")
    private RepositoryService repository;
    @Resource
    private AwsDataSourceResourceConverter awsDataSourceResourceConverter;
    @Resource(name = "awsDataSourceServiceFactory")
    private ReportDataSourceServiceFactory awsDataSourceFactory;
    @Resource
    private SecureExceptionHandler secureExceptionHandler;

    @Override
    public ClientAwsDataSource createContext(ExecutionContext ctx, ClientAwsDataSource contextDescription, Map<String, Object> data) throws IllegalParameterValueException {
        boolean passed = false;
        Exception exception = null;

        AwsReportDataSource awsReportDataSource = awsDataSourceResourceConverter.toServer(ctx,
                contextDescription, ToServerConversionOptions.getDefault().setSuppressValidation(true));

        try {
            String passwordSubstitution = messageSource.getMessage("input.password.substitution", null,
                    LocaleContextHolder.getLocale());

            // On edit datasource we set the passwordSubstitution to the passwords form fields
            // If we get the substitution from UI then set the password from original datasource (if it exists)
            AwsReportDataSource existingDs = null;
            final String password = contextDescription.getPassword();
            final String secretKey = contextDescription.getSecretKey();
            if (password == null || password.equals(passwordSubstitution) || secretKey == null || secretKey.equals(passwordSubstitution)) {
                existingDs = (AwsReportDataSource) repository.getResource(null, contextDescription.getUri());
            }
            if ((password == null || password.equals(passwordSubstitution)) && existingDs != null) {
                awsReportDataSource.setPassword(existingDs.getPassword());
            }
            if ((secretKey == null || secretKey.equals(passwordSubstitution)) && existingDs != null) {
                awsReportDataSource.setAWSSecretKey(existingDs.getAWSSecretKey());
            }

            passed = ((BaseJdbcDataSource)awsDataSourceFactory.createService(awsReportDataSource)).testConnection();
        } catch (Exception e) {
            exception = e;
        }
        if (!passed) {
            throw new ContextCreationFailedException(contextDescription, exception, secureExceptionHandler);
        }
        return contextDescription;
    }

    @Override
    public void deleteContext(ClientAwsDataSource contextDescription, Map<String, Object> data) {
        // nothing to clean, do nothing
    }

    @Override
    public ClientAwsDataSource getContextForClient(ClientAwsDataSource contextDescription, Map<String, Object> data, Map<String, String[]> additionalProperties) {
        return new ClientAwsDataSource(contextDescription).setPassword(null).setSecretKey(null);
    }
}

