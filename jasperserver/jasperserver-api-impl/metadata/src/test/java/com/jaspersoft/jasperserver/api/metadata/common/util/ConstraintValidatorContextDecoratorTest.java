/*
 * Copyright (C) 2025 the Jasper Server OS Authors
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

package com.jaspersoft.jasperserver.api.metadata.common.util;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jaspersoft.jasperserver.api.metadata.common.util.ConstraintValidatorContextDecorator.ARGUMENTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * <p></p>
 *
 * @author Vlad Zavadskyi
 */
@RunWith(MockitoJUnitRunner.class)
public class ConstraintValidatorContextDecoratorTest {
    private static final List<Object> LIST_ARGS = Arrays.<Object>asList("a", "b", "c");

    private ConstraintValidatorContextDecorator decorator;

    private HibernateConstraintValidatorContext context = mock(HibernateConstraintValidatorContext.class);

    @Before
    public void setUp() {
        decorator = new ConstraintValidatorContextDecorator(context);
    }

    @Test
    public void constructor_rightContextClass_success() {
        try {
            new ConstraintValidatorContextDecorator(mock(HibernateConstraintValidatorContext.class));
        } catch (IllegalArgumentException e) {
            fail("Should accept " + HibernateConstraintValidatorContext.class.getName() + " context");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_wrongContextClass_error() {
        new ConstraintValidatorContextDecorator(mock(ConstraintValidatorContext.class));
    }

    @Test
    public void getArguments_violationIsNull_success() {
        assertNull(ConstraintValidatorContextDecorator.getArguments(null));
    }

    @Test
    public void getArguments_violationHasWrongImplementation_success() {
        assertNull(ConstraintValidatorContextDecorator.getArguments(mock(ConstraintViolation.class)));
    }

    @Test
    public void getArguments_violationHasNullVariables_success() {
        assertNull(ConstraintValidatorContextDecorator.getArguments(createViolation(null)));
    }

    @Test
    public void getArguments_violationHasEmptyVariables_success() {
        assertNull(ConstraintValidatorContextDecorator.getArguments(createViolation(
                Collections.<String, Object>emptyMap()
        )));
    }

    @Test
    public void getArguments_violationHasSomeVariables_success() {
        Map<String, Object> variables = new HashMap<String, Object>() {{
            put(ARGUMENTS, LIST_ARGS);
        }};

        assertEquals(
                ConstraintValidatorContextDecorator.getArguments(createViolation(variables)),
                LIST_ARGS
        );
    }

    @Test
    public void setArguments_argumentsAsListWereSetToContext_success() {
        decorator.setArguments(LIST_ARGS);

        verify(context).addExpressionVariable(ARGUMENTS, LIST_ARGS);
    }

    @Test
    public void setArguments_argumentsAsArrayWereSetToContext_success() {
        decorator.setArguments(LIST_ARGS.toArray());

        verify(context).addExpressionVariable(ARGUMENTS, LIST_ARGS);
    }

    @Test
    public void disableDefaultConstraintViolation_wasExecutedOnContext_success() {
        decorator.disableDefaultConstraintViolation();

        verify(context).disableDefaultConstraintViolation();
    }

    @Test
    public void getDefaultConstraintMessageTemplate_wasExecutedOnContext_success() {
        decorator.getDefaultConstraintMessageTemplate();

        verify(context).getDefaultConstraintMessageTemplate();
    }

    @Test
    public void buildConstraintViolationWithTemplate_wasExecutedOnContext_success() {
        final String message = "message";

        decorator.buildConstraintViolationWithTemplate(message);

        verify(context).buildConstraintViolationWithTemplate(message);
    }

    @Test
    public void unwrap_wereExecutedOnContext_success() {
        decorator.unwrap(String.class);

        verify(context).unwrap(String.class);
    }

    private ConstraintViolationImpl createViolation(Map<String, Object> expressionVariables) {
        return (ConstraintViolationImpl) ConstraintViolationImpl.forBeanValidation(
                "", // messageTemplate
                expressionVariables, // expressionVariables
                Collections.emptyMap(), // messageParameters
                "", // interpolatedMessage
                Object.class, // rootBeanClass
                new Object(), // rootBean
                new Object(), // leafBeanInstance
                new Object(), // value
                null, // propertyPath
                null, // constraintDescriptor
                null // dynamicPayload
        );
    }
}
