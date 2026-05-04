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

package com.jaspersoft.jasperserver.jaxrs.common;

import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptorBuilder;
import com.jaspersoft.jasperserver.dto.resources.ClientProperty;
import org.springframework.stereotype.Component;
import org.teiid.core.TeiidException;

import jakarta.annotation.Resource;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * <p>
 *
 * @author ivan.chan
 * @version $Id: SqlExceptionMapper.java 64993 2016-10-26 13:40:48Z ykovalch $
 * @see
 */
@Provider
@Component
public class VirtualDataSourceExceptionMapper implements ExceptionMapper<TeiidException> {

    private VirtualDataSourceErrorDescriptorBuilder errorDescriptorBuilder = new VirtualDataSourceErrorDescriptorBuilder();

    @Override
    public Response toResponse(TeiidException exception) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(errorDescriptorBuilder.build(exception))
                .build();
    }

    public class VirtualDataSourceErrorDescriptorBuilder implements ErrorDescriptorBuilder<TeiidException> {
        @Override
        public ErrorDescriptor build(TeiidException e) {

            ErrorDescriptor errorDescriptor = new ErrorDescriptor();
            errorDescriptor.setErrorCode("virtual.data.source.exception");
            StringBuilder stringBuilder = new StringBuilder("Virtual data source error.");
            List<ClientProperty> parameters = new ArrayList<ClientProperty>(3);
            String message = e.getMessage();
            if (message != null && !message.isEmpty()) {
                stringBuilder.append(" Reason: " + message + ".");
                parameters.add(new ClientProperty("reason", message));
            }
            String code = e.getCode();
            if (code != null && !code.isEmpty()) {
                stringBuilder.append(" Code: " + code + ".");
                parameters.add(new ClientProperty("code", code));
            }
            String originalType = e.getOriginalType();
            if (originalType != null && !originalType.isEmpty()) {
                stringBuilder.append(" Original Type: " + originalType + ".");
                parameters.add(new ClientProperty("originalType", originalType));
            }

            errorDescriptor.setMessage(stringBuilder.toString());
            errorDescriptor.setProperties(parameters);

            return errorDescriptor;
        }
    }
}

