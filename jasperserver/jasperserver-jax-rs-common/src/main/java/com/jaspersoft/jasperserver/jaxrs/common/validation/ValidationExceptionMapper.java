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

package com.jaspersoft.jasperserver.jaxrs.common.validation;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.jaspersoft.jasperserver.core.util.type.GenericTypeProcessorRegistry;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.remote.exception.builders.DefaultMessageApplier;
import com.jaspersoft.jasperserver.remote.validation.ValidationErrorPostProcessor;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.springframework.stereotype.Component;

/**
 * {@link ExceptionMapper} for {@link ValidationException}.
 * <p/>
 * instances is sent in {@link Response} as well (in addition to HTTP 400/500 status code). Supported media types are:
 * {@code application/json}/{@code application/xml} (in appropriate provider is registered on server) or
 *
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
@javax.ws.rs.ext.Provider
@Component
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
    @Resource
    private GenericTypeProcessorRegistry genericTypeProcessorRegistry;
    @Resource
    private ErrorDescriptorBuilderFinder errorDescriptorBuilderFinder;
    @Resource
    private DefaultMessageApplier defaultMessageApplier;
    private static final Logger LOGGER = Logger.getLogger(ValidationExceptionMapper.class.getName());

    @Override
    public Response toResponse(final ValidationException exception) {
        if (exception instanceof ConstraintViolationException) {
            LOGGER.log(Level.FINER, "constraint.violations.encountered", exception);

            final ConstraintViolationException cve = (ConstraintViolationException) exception;
            final Response.ResponseBuilder response = Response.status(Response.Status.BAD_REQUEST);

            List<ErrorDescriptor> errorDescriptors = toErrorDescriptors(cve);
            response.entity(errorDescriptors.size() == 1 ? errorDescriptors.get(0) :
                    new GenericEntity<List<ErrorDescriptor>>(errorDescriptors){});

            return response.build();
        } else {
            LOGGER.log(Level.WARNING, "validation.exception.raised", exception);

            return Response.serverError().entity(exception.getMessage()).build();
        }
    }

    /**
     * Extract {@link ConstraintViolation constraint violations} from given exception and transform them into a list of
     * {@link com.jaspersoft.jasperserver.dto.common.ErrorDescriptor validation errors}.
     *
     * @param violation exception containing constraint violations.
     * @return list of validation errors (not {@code null}).
     */
    @SuppressWarnings("unchecked")
    private List<ErrorDescriptor> toErrorDescriptors(final ConstraintViolationException violation) {
        return Lists.transform(Lists.newArrayList(violation.getConstraintViolations()),
                new Function<ConstraintViolation, ErrorDescriptor>() {
                    @Override
                    public ErrorDescriptor apply(final ConstraintViolation violation) {
                        ErrorDescriptor errorDescriptor = errorDescriptorBuilderFinder.find(violation).build(violation);
                        ErrorDescriptor processedErrorDescriptor = postProcessErrorDescriptor(violation, errorDescriptor);
                        return defaultMessageApplier.applyDefaultMessageIfNotSet(processedErrorDescriptor, true);
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private ErrorDescriptor postProcessErrorDescriptor(final ConstraintViolation violation, ErrorDescriptor descriptor) {
        final Object rootBean = violation.getRootBean();
        if (rootBean != null) {
            final ValidationErrorPostProcessor errorPostProcessor = genericTypeProcessorRegistry.
                    getTypeProcessor(rootBean.getClass(), ValidationErrorPostProcessor.class, false);
            if (errorPostProcessor != null) {
                return errorPostProcessor.process(descriptor, violation);
            }
        }
        return descriptor;
    }

}


