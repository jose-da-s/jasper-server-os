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

import com.jaspersoft.jasperserver.remote.exception.builders.LocalizedErrorDescriptorBuilder;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import org.glassfish.jersey.internal.inject.ExtractorException;
import org.glassfish.jersey.server.ParamException;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static com.jaspersoft.jasperserver.dto.common.CommonErrorCode.EXCEPTION_REMOTE_ILLEGAL_PARAMETER_VALUE_ERROR;

/**
 * <p></p>
 *
 * @author Zakhar.Tomchenco
 * @version $Id$
 */
@Provider
@Component
public class GenericParamExceptionMapper implements ExceptionMapper<ParamException> {

    @Resource(name= "localizedErrorDescriptorBuilder")
    private LocalizedErrorDescriptorBuilder localizedErrorDescriptorBuilder;

    @Override
    public Response toResponse(ParamException exception) {
        Throwable cause = exception.getCause();
        if(cause instanceof ExtractorException && cause.getCause() != null){
            // fix of JRS-13813
            cause = cause.getCause();
        }
        ErrorDescriptor base = EXCEPTION_REMOTE_ILLEGAL_PARAMETER_VALUE_ERROR.createDescriptor(
                exception.getParameterName(), "", cause);
        ErrorDescriptor localizedDescriptor = localizedErrorDescriptorBuilder.localizeDescriptor(base, false);

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(localizedDescriptor)
                .build();

    }
}

