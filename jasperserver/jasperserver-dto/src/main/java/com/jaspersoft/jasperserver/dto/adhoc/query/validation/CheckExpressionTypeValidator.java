/*
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
package com.jaspersoft.jasperserver.dto.adhoc.query.validation;

import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientExpressionContainer;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.common.ValidationErrorDescriptorBuilder;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorCode.createDescriptorFrom;

/**
 * @author Vasyl Spachynskyi
 * @version $Id$
 * @since 24.01.2017
 */
public class CheckExpressionTypeValidator implements ConstraintValidator<CheckExpressionType, ClientExpressionContainer>, ValidationErrorDescriptorBuilder {
    private Set<Class> acceptedExpressions;

    @Override
    public void initialize(CheckExpressionType constraintAnnotation) {
        acceptedExpressions = new HashSet<Class>(Arrays.asList(constraintAnnotation.value()));
    }

    @Override
    public boolean isValid(ClientExpressionContainer value, ConstraintValidatorContext context) {
        if (value == null || value.getObject() == null) {
            return true;
        }

        return acceptedExpressions.contains(value.getObject().getClass());
    }


    @Override
    public ErrorDescriptor build(ConstraintViolation violation) {
        String type = ((ClientExpressionContainer) violation.getInvalidValue()).getObject().getClass().getSimpleName();

        return createDescriptorFrom(violation.getMessageTemplate(), type);
    }
}
