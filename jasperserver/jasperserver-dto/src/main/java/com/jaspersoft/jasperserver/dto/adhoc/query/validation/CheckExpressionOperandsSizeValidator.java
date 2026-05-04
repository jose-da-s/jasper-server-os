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

import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientExpression;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.common.ValidationErrorDescriptorBuilder;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorCode.QUERY_EXPRESSION_OPERANDS_SIZE_OF_BOUND;

/**
 * <p></p>
 *
 * @author Volodya Sabadosh
 * @version $Id$
 */
public class CheckExpressionOperandsSizeValidator implements ConstraintValidator<CheckExpressionOperandsSize,
        List<ClientExpression>>, ValidationErrorDescriptorBuilder {

    private Integer annotationMin;
    private Integer annotationMax;

    @Override
    public void initialize(CheckExpressionOperandsSize constraintAnnotation) {
        this.annotationMin = constraintAnnotation.min();
        this.annotationMax = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(List<ClientExpression> value, ConstraintValidatorContext context) {
        return value == null || value.size() >= annotationMin && value.size() <= annotationMax;
    }

    @Override
    public ErrorDescriptor build(ConstraintViolation violation) {
        return QUERY_EXPRESSION_OPERANDS_SIZE_OF_BOUND.createDescriptor(violation.getPropertyPath().toString(),
                annotationMin.toString(), annotationMax.toString());
    }
}
