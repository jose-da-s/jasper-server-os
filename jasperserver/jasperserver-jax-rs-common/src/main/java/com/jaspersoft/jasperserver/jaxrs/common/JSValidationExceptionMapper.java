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

import com.jaspersoft.jasperserver.api.JSValidationException;
import com.jaspersoft.jasperserver.api.common.domain.ValidationError;
import com.jaspersoft.jasperserver.api.common.domain.impl.ValidationErrorImpl;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@Provider
@Component
public class JSValidationExceptionMapper implements ExceptionMapper<JSValidationException> {
    @Resource(name = "messageSource")
    private MessageSource messageSource;

    @Override
    public Response toResponse(JSValidationException exception) {
        // raw type is used by core class. Cast is safe
        @SuppressWarnings("unchecked")
        List<ValidationError> errors = exception.getErrors().getErrors();
        List<ErrorDescriptor> errorDescriptors = new ArrayList<ErrorDescriptor>();
        for (ValidationError error : errors) {
            final ErrorDescriptor errorDescriptor = new ErrorDescriptor().setErrorCode(error.getErrorCode());
            final List<Object> errorArgumentsList = new ArrayList<Object>();
            if (error.getErrorArguments() != null && error.getErrorArguments().length > 0) {
                errorArgumentsList.addAll(Arrays.asList(error.getErrorArguments()));
            }
            if (error.getField() != null) {
                errorArgumentsList.add(0, error.getField());
            }
            String errorMessage = error.getDefaultMessage();
            if (errorMessage == null) {
                errorMessage = messageSource.getMessage(error.getErrorCode(), error.getErrorArguments(),
                        Locale.ENGLISH);
            }
            if(error instanceof ValidationErrorImpl) {
                errorDescriptor.setProperties(((ValidationErrorImpl) error).getProperties());
            }
            errorDescriptors.add(errorDescriptor
                    .setMessage(errorMessage)
                    .addParameters(errorArgumentsList.toArray()));
        }
        return Response.status(Response.Status.BAD_REQUEST).
                entity(errorDescriptors.size() == 1 ? errorDescriptors.get(0) :
                        new GenericEntity<List<ErrorDescriptor>>(errorDescriptors){}).build();
    }
}

