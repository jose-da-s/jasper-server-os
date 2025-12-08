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

import com.jaspersoft.jasperserver.dto.adhoc.query.order.ClientGenericOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.MockitoAnnotations;

import javax.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * @author Olexandr Dahno <odahno@tibco.com>
 */

class GenericOrderFieldReferenceValidatorTest {

    private static final String TEST_REFERENCE = "TEST_REFERENCE";
    private GenericOrderFieldReferenceValidator objectUnderTests = new GenericOrderFieldReferenceValidator();
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
        CheckGenericOrderFieldReference container = mock(CheckGenericOrderFieldReference.class);

        objectUnderTests.initialize(container);
        verifyNoInteractions(container);
    }

    /*
     * isValid
     */

    @Test
    public void isValid_nullOrder_throwsException() {
        final ConstraintValidatorContext stub = mock(ConstraintValidatorContext.class);

        assertThrows(
                NullPointerException.class,
                new Executable() {
                    @Override
                    public void execute() {
                        objectUnderTests.isValid(
                                null,
                                stub
                        );
                    }
                }
        );
    }

    @Test
    public void isValid_orderWithNullFieldReferenceAndAggregationLevelNull_throwsException() {
        final ConstraintValidatorContext stub = mock(ConstraintValidatorContext.class);
        final ClientGenericOrder order = new ClientGenericOrder()
                .setFieldReference(null)
                .setAggregation(null);

        assertThrows(
                NullPointerException.class,
                new Executable() {
                    @Override
                    public void execute() {
                        objectUnderTests.isValid(
                                order,
                                stub
                        );
                    }
                }
        );
    }

    @Test
    public void isValid_orderWithNullFieldReferenceAndAggregationLevelNotSet_false() {
        final ConstraintValidatorContext stub = mock(ConstraintValidatorContext.class);
        final ClientGenericOrder order = new ClientGenericOrder()
                .setFieldReference(null);

        boolean isValid = objectUnderTests.isValid(order, stub);
        assertFalse(isValid);
        verifyNoInteractions(stub);
    }

    @Test
    public void isValid_orderWithNullFieldReferenceAndAggregationLevelSet_true() {
        final ConstraintValidatorContext stub = mock(ConstraintValidatorContext.class);
        final ClientGenericOrder order = new ClientGenericOrder()
                .setFieldReference(null)
                .setAggregation(true);

        boolean isValid = objectUnderTests.isValid(order, stub);
        assertTrue(isValid);
        verifyNoInteractions(stub);
    }

    @Test
    public void isValid_orderWithSomeFieldReferenceAndAggregationLevelNotSet_true() {
        final ConstraintValidatorContext stub = mock(ConstraintValidatorContext.class);
        final ClientGenericOrder order = new ClientGenericOrder()
                .setFieldReference(TEST_REFERENCE);

        boolean isValid = objectUnderTests.isValid(order, stub);
        assertTrue(isValid);
        verifyNoInteractions(stub);
    }

    @Test
    public void isValid_orderWithSomeFieldReferenceAndAggregationLevelSet_false() {
        final ConstraintValidatorContext stub = mock(ConstraintValidatorContext.class);
        final ClientGenericOrder order = new ClientGenericOrder()
                .setFieldReference(TEST_REFERENCE)
                .setAggregation(true);

        boolean isValid = objectUnderTests.isValid(order, stub);
        assertFalse(isValid);
        verifyNoInteractions(stub);
    }

}