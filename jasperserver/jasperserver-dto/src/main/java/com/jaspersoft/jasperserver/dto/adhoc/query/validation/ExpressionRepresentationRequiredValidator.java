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

import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorCode.createDescriptorFrom;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
public class ExpressionRepresentationRequiredValidator implements ConstraintValidator<ExpressionRepresentationRequired, ClientExpressionContainer>, ValidationErrorDescriptorBuilder {
    @Override
    public ErrorDescriptor build(ConstraintViolation violation) {
        return createDescriptorFrom(violation.getMessageTemplate(), violation.getPropertyPath().toString());
    }

    @Override
    public void initialize(ExpressionRepresentationRequired constraintAnnotation) {
        // nothing to initialize
    }

    @Override
    public boolean isValid(ClientExpressionContainer value, ConstraintValidatorContext context) {
        return value == null || value.getObject() != null || value.getString() != null;
    }
}
