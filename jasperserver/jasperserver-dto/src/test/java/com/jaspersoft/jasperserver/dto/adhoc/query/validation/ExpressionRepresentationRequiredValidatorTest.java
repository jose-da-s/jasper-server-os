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

package com.jaspersoft.jasperserver.dto.adhoc.query.validation;

import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientExpressionContainer;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.arithmetic.ClientAdd;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Path;

import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorCode.EXPRESSION_REPRESENTATION_REQUIRED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Olexandr Dahno <odahno@tibco.com>
 */

class ExpressionRepresentationRequiredValidatorTest {

    private static final String TEST_STRING = "TEST_STRING";
    private static final String TEST_PATH = "TEST_PATH";
    private ExpressionRepresentationRequiredValidator objectUnderTests = new ExpressionRepresentationRequiredValidator();
    private AutoCloseable mockitoCloseable;

    @BeforeEach
    public void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
    }

    @Test
    public void initialize() {
        ExpressionRepresentationRequired container = mock(ExpressionRepresentationRequired.class);

        objectUnderTests.initialize(container);
        verifyNoInteractions(container);
    }

    /*
     * isValid
     */

    @Test
    public void isValid_nullContainer_true() {
        final ConstraintValidatorContext stub = mock(ConstraintValidatorContext.class);

        boolean isValid = objectUnderTests.isValid(
                null,
                stub
        );
        assertTrue(isValid);
    }

    @Test
    public void isValid_containerWithNullStringAndNullObject_false() {
        final ConstraintValidatorContext stub = mock(ConstraintValidatorContext.class);

        ClientExpressionContainer container = new ClientExpressionContainer()
                .setObject(null)
                .setString(null);

        boolean isValid = objectUnderTests.isValid(container, stub);

        assertFalse(isValid);
        verifyNoInteractions(stub);
    }

    @Test
    public void isValid_containerWithNonNullString_true() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        ClientExpressionContainer container = new ClientExpressionContainer()
                .setString(TEST_STRING)
                .setObject(null);

        boolean isValid = objectUnderTests.isValid(container, context);
        assertTrue(isValid);
        verifyNoInteractions(context);
    }

    @Test
    public void isValid_containerWithNonNullObject_true() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        ClientExpressionContainer container = new ClientExpressionContainer()
                .setObject(new ClientAdd())
                .setString(null);

        boolean isValid = objectUnderTests.isValid(container, context);
        assertTrue(isValid);
        verifyNoInteractions(context);
    }

    /*
     * build
     */

    @Test
    public void build_someViolationObject_ErrorDescription() {
        ConstraintViolation violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn(TEST_PATH);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessageTemplate()).thenReturn(EXPRESSION_REPRESENTATION_REQUIRED.toString());

        ErrorDescriptor expected = EXPRESSION_REPRESENTATION_REQUIRED.createDescriptor(TEST_PATH);

        ErrorDescriptor actual = objectUnderTests.build(violation);
        assertEquals(expected, actual);
    }

}