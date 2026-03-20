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

import com.jaspersoft.jasperserver.core.util.type.GenericTypeProcessorRegistry;
import com.jaspersoft.jasperserver.dto.common.ValidationErrorDescriptorBuilder;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintViolation;
import javax.validation.metadata.ConstraintDescriptor;
import java.util.List;

/**
 * @author Volodya Sabadosh
 */
@Service
public class ErrorDescriptorBuilderFinder {
    @Resource
    private GenericTypeProcessorRegistry genericTypeProcessorRegistry;

    /**
     * Finds appropriate instance of  <tt>ValidationErrorDescriptorBuilder</tt>, that matches passed constrains
     * violation. This algorithm consists of tree steps:
     *  1) Checks if constraint validator classes includes <tt>ValidationErrorDescriptorBuilder</tt>. Example of such
     *  validator is <tt>CheckAggregateDefinitionValidator</tt>;
     *  2) Tries to check if there are registered validator for default validation annotations. Example of such validator
     *  is  <tt>NotEmptyConstraintValidationErrorDescriptorBuilder</tt>.
     *  3) Otherwise creates instance of  <tt>DefaultConstraintValidationErrorDescriptorBuilder</tt>.
     *
     * @param violation constraint violation
     * @return instance of appropriate <tt>ValidationErrorDescriptorBuilder</tt>.
     */
    @SuppressWarnings("unchecked")
    public ValidationErrorDescriptorBuilder find(final ConstraintViolation violation) {
        ValidationErrorDescriptorBuilder validationErrorDescriptorBuilder = null;
        //Tries to check whether constraint validator class is a builder itself
        final ConstraintDescriptor constraintDescriptor = violation.getConstraintDescriptor();
        final List<Class<ConstraintValidator>> constraintValidatorClasses =
                constraintDescriptor.getConstraintValidatorClasses();
        if (constraintValidatorClasses != null) {
            for (Class<ConstraintValidator> constraintValidatorClass : constraintValidatorClasses) {
                if (ValidationErrorDescriptorBuilder.class.isAssignableFrom(constraintValidatorClass)) {
                    try {
                        final ConstraintValidator validator = constraintValidatorClass.newInstance();
                        validator.initialize(constraintDescriptor.getAnnotation());
                        validationErrorDescriptorBuilder = (ValidationErrorDescriptorBuilder) validator;
                        break;
                    } catch (Exception e) {
                        throw new IllegalStateException("Failed to instantiate validator", e);
                    }
                }
            }
        }
        //Tries to check if there are registered validator for default validation annotations.
        if (validationErrorDescriptorBuilder == null) {
            validationErrorDescriptorBuilder = genericTypeProcessorRegistry.
                    getTypeProcessor(constraintDescriptor.getAnnotation().getClass().getInterfaces()[0],
                            ValidationErrorDescriptorBuilder.class, false);
        }

        //Return default validator by default.
        if (validationErrorDescriptorBuilder == null) {
            validationErrorDescriptorBuilder = new DefaultConstraintValidationErrorDescriptorBuilder();
        }

        return validationErrorDescriptorBuilder;
    }
}

