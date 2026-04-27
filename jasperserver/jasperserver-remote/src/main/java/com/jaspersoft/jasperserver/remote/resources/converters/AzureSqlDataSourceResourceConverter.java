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
package com.jaspersoft.jasperserver.remote.resources.converters;

import jakarta.annotation.Resource;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import org.springframework.stereotype.Service;

import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConversionOptions;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.AzureSqlReportDataSource;
import com.jaspersoft.jasperserver.dto.resources.ClientAzureSqlDataSource;
import com.jaspersoft.jasperserver.dto.resources.ClientReference;
import com.jaspersoft.jasperserver.dto.resources.ClientReferenceableFile;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;

import java.util.List;

@Service
public class AzureSqlDataSourceResourceConverter extends GenericJdbcDataSourceResourceConverter<AzureSqlReportDataSource, ClientAzureSqlDataSource> {
    @Resource
    protected ResourceReferenceConverterProvider resourceReferenceConverterProvider;

    @Override
    protected AzureSqlReportDataSource resourceSpecificFieldsToServer(ExecutionContext ctx,ClientAzureSqlDataSource clientObject, AzureSqlReportDataSource resultToUpdate, List<Exception> exceptions, ToServerConversionOptions options) throws IllegalParameterValueException {
        final AzureSqlReportDataSource azureSqlReportDataSource = super.resourceSpecificFieldsToServer(ctx, clientObject, resultToUpdate, exceptions, options);
        azureSqlReportDataSource.setSubscriptionId(clientObject.getSubscriptionId());
        azureSqlReportDataSource.setKeyStoreResource(convertResourceToServer(ctx, clientObject.getKeyStoreUri(), options));
        // modify password only if use explicitly modifies it (clientObject.getKeyStorePassword()!=null) or if subscription id is erased.
        if (clientObject.getKeyStorePassword() != null || clientObject.getSubscriptionId() == null) {
            azureSqlReportDataSource.setKeyStorePassword(clientObject.getKeyStorePassword());
        }
        azureSqlReportDataSource.setServerName(clientObject.getServerName());
        azureSqlReportDataSource.setDbName(clientObject.getDbName());
        return azureSqlReportDataSource;
    }

    @Override
    protected ClientAzureSqlDataSource resourceSpecificFieldsToClient(ClientAzureSqlDataSource client, AzureSqlReportDataSource serverObject, ToClientConversionOptions options) {
        final ClientAzureSqlDataSource clientAzureSqlDataSource = super.resourceSpecificFieldsToClient(client, serverObject, options);
        clientAzureSqlDataSource.setSubscriptionId(serverObject.getSubscriptionId());
        clientAzureSqlDataSource.setKeyStoreUri(convertResourceToClient(serverObject.getKeyStoreResource(), options));
        clientAzureSqlDataSource.setKeyStorePassword(serverObject.getKeyStorePassword());
        clientAzureSqlDataSource.setServerName(serverObject.getServerName());
        clientAzureSqlDataSource.setDbName(serverObject.getDbName());
        return clientAzureSqlDataSource;
    }

    @Override
    protected void resourceSecureFieldsToClient(ClientAzureSqlDataSource client, AzureSqlReportDataSource serverObject, ToClientConversionOptions options) {
        super.resourceSecureFieldsToClient(client, serverObject, options);
        client.setKeyStorePassword(serverObject.getKeyStorePassword());
    }

    private ResourceReference convertResourceToServer(ExecutionContext ctx, String uri, ToServerConversionOptions options) {
        ResourceReferenceConverter<ClientReferenceableFile> referenceConverter = resourceReferenceConverterProvider
                .getConverterForType(ClientReferenceableFile.class);
        return referenceConverter.toServer(ctx, new ClientReference(uri), options);
    }
    
    private String convertResourceToClient(ResourceReference resource, ToClientConversionOptions options) {
        if (resource == null) {
            return null;
        }
        ResourceReferenceConverter<ClientReferenceableFile> referenceConverter = resourceReferenceConverterProvider
                .getConverterForType(ClientReferenceableFile.class);
        return referenceConverter.toClient(resource, options).getUri();
    }

}

