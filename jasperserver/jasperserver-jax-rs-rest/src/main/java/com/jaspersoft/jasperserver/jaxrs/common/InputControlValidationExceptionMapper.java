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
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlValidationException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@Component
public class InputControlValidationExceptionMapper implements ExceptionMapper<InputControlValidationException>, ErrorDescriptorBuilder<InputControlValidationException> {
    @Resource
    MessageSource messageSource;

    @Override
    public Response toResponse(InputControlValidationException exception) {
        return Response.status(Response.Status.BAD_REQUEST).
                entity(build(exception)).build();
    }

    @Override
    public ErrorDescriptor build(InputControlValidationException e) {
        final Object[] args = e.getValidationError().getArgs();
        String errorCode = e.getValidationError().getErrorCode();
        final ErrorDescriptor errorDescriptor = new ErrorDescriptor().setMessage(messageSource.getMessage(errorCode, args, LocaleContextHolder.getLocale()));
        errorDescriptor.setErrorCode(errorCode);
        return errorDescriptor;
    }
}


