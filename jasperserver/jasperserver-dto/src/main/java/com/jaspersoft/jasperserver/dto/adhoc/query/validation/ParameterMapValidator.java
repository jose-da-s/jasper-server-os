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

/**
 * @author fpang
 * @since May 6, 2016
 * @version $Id$
 *
 */

package com.jaspersoft.jasperserver.dto.adhoc.query.validation;

import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientExpressionContainer;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.common.ValidationErrorDescriptorBuilder;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import java.util.Map;
import java.util.regex.Pattern;

import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorCode.PARAMETER_NAME_IN_NOT_VALID;

/**
 * @author fpang, schubar
 *
 */
public class ParameterMapValidator implements ConstraintValidator<ParameterMap, Map<String, ClientExpressionContainer>>,
        ValidationErrorDescriptorBuilder {
    public static Pattern NOT_W_PATTERN = Pattern.compile("[^\\p{L}\\p{N}_]");
    public static Pattern NOT_A_TO_Z_OR_UNDERSCORE_PATTERN = Pattern.compile("[^\\p{L}_]");

    @Override
    public void initialize(ParameterMap constraintAnnotation) {
    }

    @Override
    public boolean isValid(Map<String, ClientExpressionContainer> targetParameterMap, ConstraintValidatorContext context) {
        if (targetParameterMap != null) {
            for (String paramEl : targetParameterMap.keySet()) {
                if (paramEl == null
                    || paramEl.contains(".") // This check is explicitly defined because <dot> has special meaning in Data Engine and impl of ExpressionEvaluator
                    || NOT_A_TO_Z_OR_UNDERSCORE_PATTERN.matcher(paramEl.substring(0, 1)).matches()
                    || NOT_W_PATTERN.matcher(paramEl).find()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ErrorDescriptor build(ConstraintViolation violation) {
        return PARAMETER_NAME_IN_NOT_VALID.createDescriptor();
    }
}
