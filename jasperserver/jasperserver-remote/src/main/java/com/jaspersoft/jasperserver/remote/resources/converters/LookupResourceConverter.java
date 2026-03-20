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
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceLookup;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConversionOptions;
import com.jaspersoft.jasperserver.dto.resources.ClientResourceLookup;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.search.common.ResourceDetails;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.text.DateFormat;
import java.util.List;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@Service
public class LookupResourceConverter extends ResourceConverterImpl<ResourceLookup, ClientResourceLookup> {

    @Resource
    private ResourceConverterProvider resourceConverterProvider;

    @Override
    public ResourceDetails toServer(ExecutionContext ctx, ClientResourceLookup clientObject, ResourceLookup resultToUpdate, ToServerConversionOptions options) {
        throw new IllegalStateException("ResourceLookup is read only object. ToServer conversion isn't supported");
    }

    @Override
    protected ResourceLookup resourceSpecificFieldsToServer(ExecutionContext ctx, ClientResourceLookup clientObject, ResourceLookup resultToUpdate, List<Exception> exceptions, ToServerConversionOptions options) throws IllegalParameterValueException {
        throw new IllegalStateException("ResourceLookup is read only object. ToServer conversion isn't supported");
    }

    @Override
    public ResourceLookup toServer(ExecutionContext ctx, ClientResourceLookup clientObject, ToServerConversionOptions options) {
        throw new IllegalStateException("ResourceLookup is read only object. ToServer conversion isn't supported");
    }

    @Override
    protected ClientResourceLookup resourceSpecificFieldsToClient(ClientResourceLookup client, ResourceLookup serverObject, ToClientConversionOptions options) {
        final DateFormat dateTimeFormatter = getDateTimeFormat();
        return client.setResourceType(toClientResourceType(serverObject.getResourceType())).setLastAccessTime(serverObject.getLastAccessTime() != null ?
        dateTimeFormatter.format(serverObject.getLastAccessTime()) : null);
    }

    protected String toClientResourceType(String serverResourceType){
        String clientType;
        try {
            clientType = resourceConverterProvider.getToClientConverter(serverResourceType).getClientResourceType();
        } catch (IllegalParameterValueException e){
            // no converter for this serverResourceType
            clientType = "unknown";
        }
        return clientType;
    }
}

