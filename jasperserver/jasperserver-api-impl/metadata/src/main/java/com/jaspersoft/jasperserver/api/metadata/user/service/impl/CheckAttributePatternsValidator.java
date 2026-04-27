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
package com.jaspersoft.jasperserver.api.metadata.user.service.impl;

import com.jaspersoft.jasperserver.api.metadata.common.util.ConstraintValidatorContextDecorator;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.common.ValidationErrorDescriptorBuilder;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.jaspersoft.jasperserver.api.metadata.user.service.impl.ProfileAttributesResolverImpl.attributeNameGroup;
import static com.jaspersoft.jasperserver.api.metadata.user.service.impl.ProfileAttributesResolverImpl.categoryGroup;
import static com.jaspersoft.jasperserver.api.metadata.user.service.impl.ProfileAttributesResolverImpl.groupExpression;
import static com.jaspersoft.jasperserver.dto.common.AttributeErrorCode.ATTRIBUTE_PATTERNS_INCLUDES_INVALID;

/**
 * <p></p>
 *
 * @author Volodya Sabadosh
 * @version $Id$
 */
public class CheckAttributePatternsValidator implements ConstraintValidator<CheckAttributePatterns, List<String>>,
        ValidationErrorDescriptorBuilder {
    
    @Override
    public void initialize(CheckAttributePatterns constraintAnnotation) {

    }

    @Override
    public boolean isValid(List<String> value, ConstraintValidatorContext context) {
        return validateAttributePatterns(value, context);
    }

    private boolean validateAttributePatterns(List<String> patterns, ConstraintValidatorContext context) {
        for (String pattern : patterns) {
            List<String> missingGroups = new ArrayList<String>();
            for (String group : Arrays.asList(attributeNameGroup, categoryGroup)) {
                if (pattern.contains(String.format(groupExpression, group))) continue;
                missingGroups.add(group);
            }

            if (!missingGroups.isEmpty()) {
                ConstraintValidatorContextDecorator decorator = wrapContext(context);

                decorator.setArguments(pattern, missingGroups.toString());
                decorator.buildConstraintViolationWithTemplate(ATTRIBUTE_PATTERNS_INCLUDES_INVALID.toString())
                        .addConstraintViolation();

                return false;
            }
        }

        return true;
    }

    protected ConstraintValidatorContextDecorator wrapContext(ConstraintValidatorContext context) {
        ConstraintValidatorContextDecorator decorator = new ConstraintValidatorContextDecorator(context);
        decorator.disableDefaultConstraintViolation();

        return decorator;
    }

   

    
    @Override
    public ErrorDescriptor build(jakarta.validation.ConstraintViolation violation) {
        return ATTRIBUTE_PATTERNS_INCLUDES_INVALID.
                createDescriptor(ConstraintValidatorContextDecorator.getArgumentsArray(violation));
    }
    
}
