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

import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.jaspersoft.jasperserver.api.common.error.handling.ExceptionOutputManager;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.remote.exception.builders.LocalizedErrorDescriptorBuilder;
import static com.jaspersoft.jasperserver.api.common.error.handling.ExceptionOutputManager.GENERIC_ERROR_MESSAGE_CODE;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author: Zakhar.Tomchenco
 * @version $Id$
 */

@Provider
@Component
public class GenericRemoteExceptionMapper implements ExceptionMapper<ErrorDescriptorException> {
    private static final Log log = LogFactory.getLog(GenericRemoteExceptionMapper.class);

    @Resource(name= "localizedErrorDescriptorBuilder")
    private LocalizedErrorDescriptorBuilder localizedErrorDescriptorBuilder;
    @Resource
    private JSExceptionWrapperMapper exceptionWrapperMapper;
    @Resource
    private MessageSource messageSource;
    @Resource
    private ExceptionOutputManager exceptionOutputManager;
    public Response toResponse(ErrorDescriptorException exception) {
        Response.Status status = Response.Status.BAD_REQUEST;
        final ErrorDescriptor rootErrorDescriptor = localizedErrorDescriptorBuilder
                .localizeDescriptor(exception.getErrorDescriptor());
        List<ErrorDescriptor> details = new ArrayList<ErrorDescriptor>();
        final Throwable cause = exception.getCause();
        if (exception.isUnexpected()){
            status = Response.Status.INTERNAL_SERVER_ERROR;
            log.error("Unexpected error occurs", exception);
        } else if(cause != null) {
            // do try to build cause descriptor if not unexpected only
            final Object entity = exceptionWrapperMapper
                    .toResponse(new JSExceptionWrapper((Exception) cause)).getEntity();
            if(entity instanceof ErrorDescriptor){
                if(!exceptionOutputManager.isExceptionMessageAllowed() && (exception.getErrorDescriptor().getMessage().contains(messageSource.getMessage(GENERIC_ERROR_MESSAGE_CODE, null, LocaleContextHolder.getLocale())))){
                    ((ErrorDescriptor) entity).setMessage(exception.getErrorDescriptor().getMessage());
                    ((ErrorDescriptor) entity).setProperties(null);
                }
                details.add((ErrorDescriptor) entity);
            } else if(entity instanceof GenericEntity){
                final Object genericEntity = ((GenericEntity) entity).getEntity();
                if(genericEntity instanceof Collection
                        && ((Collection)genericEntity).iterator().next() instanceof ErrorDescriptor) {
                    if(!exceptionOutputManager.isExceptionMessageAllowed() && (exception.getErrorDescriptor().getMessage().contains(messageSource.getMessage(GENERIC_ERROR_MESSAGE_CODE, null, LocaleContextHolder.getLocale())))){
                        ((ErrorDescriptor) entity).setMessage(exception.getErrorDescriptor().getMessage());
                        ((ErrorDescriptor) entity).setProperties(null);
                    }
                    details.addAll((Collection) genericEntity);
                }
            }
        }
        if(!details.isEmpty()){
            rootErrorDescriptor.setDetails(details);
        }
        return Response.status(status).entity(rootErrorDescriptor).build();
    }
}

