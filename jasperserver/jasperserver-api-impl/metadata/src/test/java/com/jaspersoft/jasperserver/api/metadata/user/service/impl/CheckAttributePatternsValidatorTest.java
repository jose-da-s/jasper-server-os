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
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.jaspersoft.jasperserver.api.metadata.common.util.ConstraintValidatorContextDecorator.ARGUMENTS;
import static com.jaspersoft.jasperserver.dto.common.AttributeErrorCode.ATTRIBUTE_PATTERNS_INCLUDES_INVALID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import static org.junit.Assert.assertEquals;
import static com.jaspersoft.jasperserver.api.metadata.user.service.impl.ProfileAttributesResolverImpl.attributeNameGroup;
import static com.jaspersoft.jasperserver.api.metadata.user.service.impl.ProfileAttributesResolverImpl.categoryGroup;


/**
 * <p></p>
 *
 * @author Volodya Sabadosh
 * @version $Id: $
 */
@RunWith(MockitoJUnitRunner.class)
public class CheckAttributePatternsValidatorTest {
    @Spy
    private CheckAttributePatternsValidator validator;

    private String validAttrPattern1 = "\\s*attribute\\s*\\(\\s*'(?<name>[^\\\\/']+)'\\s*(,\\s*'(?<category>[^\\\\/']+)'\\s*)?\\)\\s*";
    private String validAttrPattern2 = "\\{\\s*attribute\\s*\\(\\s*''(?<name>[^\\\\/']+)''\\s*(,\\s*''(?<category>[^\\\\/']+)''\\s*)?\\)\\s*\\}";

    private String invalidAttrPattern1 = "{\"\\s*attribute\\s*\\(\\s*'([^\\\\/']+)'\\s*(,\\s*'([^\\\\/']+)'\\s*)?\\)\\s*\"}";
    private String invalidAttrPattern2 = "{\\s*attribute\\s*\\(\\s*''([^\\\\/']+)''\\s*(,\\s*''(?<category>[^\\\\/']+)''\\s*)?\\)\\s*}";


    @Test
    public void isValid_attrPatternsIsValid_success() {
        ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContextImpl.class);

        assertEquals(validator.isValid(Collections.singletonList(validAttrPattern1), context), true);
        assertEquals(validator.isValid(Collections.singletonList(validAttrPattern2), context), true);

        verify(validator, times(0)).wrapContext(context);
    }

    @Test
    public void isValid_attrPatternNotHaveAttrNameAndCategoryGroups_success() {
        ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContextImpl.class);
        ConstraintValidatorContext.ConstraintViolationBuilder contextBuilder = Mockito.mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        ConstraintValidatorContextDecorator wrappedContext = Mockito.mock(ConstraintValidatorContextDecorator.class);

        when(validator.wrapContext(context)).thenReturn(wrappedContext);

        when(wrappedContext.buildConstraintViolationWithTemplate(ATTRIBUTE_PATTERNS_INCLUDES_INVALID.toString())).thenReturn(contextBuilder);
                assertEquals(validator.isValid(Collections.singletonList(invalidAttrPattern1), context), false);

        verify(wrappedContext, times(1)).setArguments(invalidAttrPattern1, Arrays.asList(attributeNameGroup, categoryGroup).toString());
        verify(contextBuilder, times(1)).addConstraintViolation();
        verify(validator, times(1)).wrapContext(context);
    }

    @Test
    public void isValid_attrPatternNotHaveAttrNameGroup_success() {
        ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContextImpl.class);
        ConstraintValidatorContext.ConstraintViolationBuilder contextBuilder = Mockito.mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        ConstraintValidatorContextDecorator wrappedContext = Mockito.mock(ConstraintValidatorContextDecorator.class);

        when(validator.wrapContext(context)).thenReturn(wrappedContext);

        when(wrappedContext.buildConstraintViolationWithTemplate(ATTRIBUTE_PATTERNS_INCLUDES_INVALID.toString())).thenReturn(contextBuilder);
        assertEquals(validator.isValid(Collections.singletonList(invalidAttrPattern2), context), false);

        verify(wrappedContext, times(1)).setArguments(invalidAttrPattern2, Arrays.asList(attributeNameGroup).toString());
        verify(contextBuilder, times(1)).addConstraintViolation();
        verify(validator, times(1)).wrapContext(context);
    }

    @Test
    public void build_someViolationWithErrorArgs_returnErrorDescriptor() {
        String value1 = "value1";
        String value2 = "value2";

        ConstraintViolation violation = mockViolationThatContainsExpressionVariables(value1, value2);

        assertEquals(ATTRIBUTE_PATTERNS_INCLUDES_INVALID.createDescriptor(value1, value2),
                validator.build(violation));
    }

    private ConstraintViolation mockViolationThatContainsExpressionVariables(Object... args) {
        ConstraintViolationImpl violation = mock(ConstraintViolationImpl.class);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(ARGUMENTS, Arrays.asList(args));
        when(violation.getExpressionVariables()).thenReturn(map);
        return violation;
    }

}
