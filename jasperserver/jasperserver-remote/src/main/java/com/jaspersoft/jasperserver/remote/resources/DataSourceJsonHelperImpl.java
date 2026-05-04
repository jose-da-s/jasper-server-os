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
package com.jaspersoft.jasperserver.remote.resources;

import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.helper.DataSourceJsonHelper;
import com.jaspersoft.jasperserver.dto.resources.ClientResource;
import com.jaspersoft.jasperserver.remote.helpers.JacksonMapperProvider;
import com.jaspersoft.jasperserver.remote.resources.converters.ResourceConverterProvider;
import com.jaspersoft.jasperserver.remote.resources.converters.ToServerConversionOptions;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@Service
public class DataSourceJsonHelperImpl implements DataSourceJsonHelper {
    @Resource
    private JacksonMapperProvider jacksonMapperProvider;
    @Resource
    private ResourceConverterProvider resourceConverterProvider;

    @Override
    public ReportDataSource parse(String dataSourceJson, String clientType) {
        try {
            final Class<? extends ClientResource> clientTypeClass = resourceConverterProvider
                    .getClientTypeClass(clientType);
            final ClientResource clientResource = jacksonMapperProvider.getObjectMapper()
                    .reader(clientTypeClass).readValue(dataSourceJson);
            return (ReportDataSource) resourceConverterProvider.getToServerConverter(clientResource)
                    .toServer(ExecutionContextImpl.getRuntimeExecutionContext(), clientResource, ToServerConversionOptions.getDefault());
        } catch (Exception e) {
            throw new JSExceptionWrapper(e);
        }
    }
}

