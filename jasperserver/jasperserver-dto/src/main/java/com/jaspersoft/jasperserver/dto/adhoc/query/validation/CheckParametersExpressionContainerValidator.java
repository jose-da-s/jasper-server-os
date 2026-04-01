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
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientExpressionContainer;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.common.ValidationErrorDescriptorBuilder;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorCode.QUERY_WHERE_PARAMETERS_EXPRESSION_NOT_VALID;

/**
 * @author Vasyl Spachynskyi
 * @since 27.01.2017
 */
public class CheckParametersExpressionContainerValidator
        implements ConstraintValidator<CheckParametersExpressionContainer, Map<String, ClientExpressionContainer>>, ValidationErrorDescriptorBuilder {

    private Set<Class> acceptedExpressions;

    @Override
    public void initialize(CheckParametersExpressionContainer constraintAnnotation) {
        acceptedExpressions = new HashSet<Class>(Arrays.asList(constraintAnnotation.value()));
    }

    @Override
    public boolean isValid(Map<String, ClientExpressionContainer> expressionMap, ConstraintValidatorContext context) {
        if (expressionMap == null) return true;

        for (Map.Entry<String, ClientExpressionContainer> entry : expressionMap.entrySet()) {
            ClientExpressionContainer expressionContainer = entry.getValue();

            if (expressionContainer == null) {
                return false;
            } else if (expressionContainer.getString() != null) {
                continue;
            }

            ClientExpression expression = expressionContainer.getObject();
            if (expression == null || !acceptedExpressions.contains(expression.getClass())){
                return false;
            }
        }

        return true;
    }

    @Override
    public ErrorDescriptor build(jakarta.validation.ConstraintViolation violation) {
        return QUERY_WHERE_PARAMETERS_EXPRESSION_NOT_VALID.createDescriptor();
    }
}