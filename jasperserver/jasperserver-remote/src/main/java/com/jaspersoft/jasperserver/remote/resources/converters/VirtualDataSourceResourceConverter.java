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

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConversionOptions;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.VirtualReportDataSource;
import com.jaspersoft.jasperserver.dto.resources.ClientSubDataSourceReference;
import com.jaspersoft.jasperserver.dto.resources.ClientVirtualDataSource;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@Service
public class VirtualDataSourceResourceConverter extends ResourceConverterImpl<VirtualReportDataSource, ClientVirtualDataSource> {
    @jakarta.annotation.Resource(name = "concreteRepository")
    protected RepositoryService repositoryService;

    @Override
    protected VirtualReportDataSource resourceSpecificFieldsToServer(ExecutionContext ctx, ClientVirtualDataSource clientObject, VirtualReportDataSource resultToUpdate, List<Exception> exceptions, ToServerConversionOptions options) throws IllegalParameterValueException {
        final List<ClientSubDataSourceReference> subDataSources = clientObject.getSubDataSources();
        Map<String,ResourceReference> dataSourceUriMap = Collections.EMPTY_MAP;
        if(subDataSources != null && !subDataSources.isEmpty()){
            dataSourceUriMap = new HashMap<String, ResourceReference>(subDataSources.size());
            for(ClientSubDataSourceReference currentReference : subDataSources){
                dataSourceUriMap.put(currentReference.getId(), new ResourceReference(currentReference.getUri()));
            }
        }

        resultToUpdate.setDataSourceUriMap(dataSourceUriMap);
        return resultToUpdate;
    }

    @Override
    protected ClientVirtualDataSource resourceSpecificFieldsToClient(ClientVirtualDataSource client, VirtualReportDataSource serverObject, ToClientConversionOptions options) {
        final Map<String,ResourceReference> dataSourceUriMap = serverObject.getDataSourceUriMap();
        List<ClientSubDataSourceReference> subDataSources = null;
        if(dataSourceUriMap != null && !dataSourceUriMap.isEmpty()){
            subDataSources = new ArrayList<ClientSubDataSourceReference>(dataSourceUriMap.size());
            for(String currentId : dataSourceUriMap.keySet()){
                final ClientSubDataSourceReference currentReference = new ClientSubDataSourceReference();
                currentReference.setId(currentId);
                currentReference.setUri(dataSourceUriMap.get(currentId).getReferenceURI());
                subDataSources.add(currentReference);
            }
        }
        client.setSubDataSources(subDataSources);
        return client;
    }
}

