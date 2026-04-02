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

import com.jaspersoft.jasperserver.api.ExceptionListWrapper;
import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.common.MessageAgnosticErrorDescriptor;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import jakarta.annotation.Resource;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.springframework.stereotype.Component;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@Component
@Provider
public class ExceptionListExceptionMapper implements ExceptionMapper<ExceptionListWrapper> {
    @Resource
    private JSExceptionWrapperMapper exceptionWrapperMapper;
    @Override
    public Response toResponse(ExceptionListWrapper exceptionListWrapper) {
        // to avoid duplication of errors in response  the error descriptors are put to Set
        // as massage of error descriptor can be different for identical error codes and properties
        // use MessageAgnosticErrorDescriptor instead of ErrorDescriptor (equals and hashcode are overridden to  ignore massage differences)
        Set<MessageAgnosticErrorDescriptor> messageAgnosticErrorDescriptors = new LinkedHashSet<MessageAgnosticErrorDescriptor>();
        for (Exception exception : exceptionListWrapper.getExceptions()) {
            Object entity = exceptionWrapperMapper.toResponse(new JSExceptionWrapper(exception))
                    .getEntity();
            if (entity instanceof List) {
                final List<ErrorDescriptor> errorDescriptors = (List<ErrorDescriptor>) entity;
                for (ErrorDescriptor errorDescriptor : errorDescriptors) {
                    messageAgnosticErrorDescriptors.add(new MessageAgnosticErrorDescriptor(errorDescriptor));
                }
            }
            if (entity instanceof GenericEntity) {
                final Collection<? extends ErrorDescriptor> errorDescriptors = (Collection<? extends ErrorDescriptor>) ((GenericEntity) entity).getEntity();
                for (ErrorDescriptor descriptor : errorDescriptors) {
                    messageAgnosticErrorDescriptors.add(new MessageAgnosticErrorDescriptor(descriptor));
                }
            }
            if(entity instanceof ErrorDescriptor){
                messageAgnosticErrorDescriptors.add(new MessageAgnosticErrorDescriptor((ErrorDescriptor) entity));
            }

        }
        Response.ResponseBuilder responseBuilder = Response.status(Response.Status.BAD_REQUEST);
        return responseBuilder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).
                entity(messageAgnosticErrorDescriptors.size() == 1 ? messageAgnosticErrorDescriptors.iterator().next() :
                        new GenericEntity<Set<MessageAgnosticErrorDescriptor>>(messageAgnosticErrorDescriptors){}).build();
    }

    protected Object getEntity(Exception exception) {
        return exceptionWrapperMapper
                .toResponse(new JSExceptionWrapper(exception))
                .getEntity();
    }
}

