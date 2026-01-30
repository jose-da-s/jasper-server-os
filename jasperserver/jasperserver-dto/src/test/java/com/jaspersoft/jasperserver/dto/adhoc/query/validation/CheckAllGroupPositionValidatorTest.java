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

import com.jaspersoft.jasperserver.dto.adhoc.query.field.ClientQueryGroup;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.MockitoAnnotations;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import java.util.Arrays;
import java.util.Collections;

import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorCode.QUERY_GROUPBY_ALLGROUP_NOT_FIRST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Olexandr Dahno <odahno@tibco.com>
 */

class CheckAllGroupPositionValidatorTest {

    private static final String TEST_PATH = "TEST_PATH";

    private CheckAllGroupPositionValidator objectUnderTests = new CheckAllGroupPositionValidator();
    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
    }

    @Test
    public void initialize() {
        // without interactions
        CheckAllGroupPosition mock = mock(CheckAllGroupPosition.class);
        objectUnderTests.initialize(mock);
        verifyNoInteractions(mock);
    }

    /*
     * isValid
     */

    @Test
    public void isValid_nullGroups_throwsException() {
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
    public void isValid_emptyGroups_true() {
        ConstraintValidatorContext mock = mock(ConstraintValidatorContext.class);
        boolean isValid = objectUnderTests.isValid(
                Collections.<ClientQueryGroup>emptyList(),
                mock
        );
        verifyNoInteractions(mock);
        assertTrue(isValid);
    }

    @Test
    public void isValid_oneElementInGroups_true() {
        ConstraintValidatorContext mock = mock(ConstraintValidatorContext.class);
        boolean isValid = objectUnderTests.isValid(
                Collections.singletonList(
                        new ClientQueryGroup()
                ),
                mock
        );
        verifyNoInteractions(mock);
        assertTrue(isValid);
    }

    @Test
    public void isValid_FirstElement_AllGroups_true() {
        ConstraintValidatorContext mock = mock(ConstraintValidatorContext.class);
        boolean isValid = objectUnderTests.isValid(
                Arrays.asList(
                        new ClientQueryGroup.ClientAllGroup(),
                        new ClientQueryGroup()
                ),
                mock
        );
        verifyNoInteractions(mock);
        assertTrue(isValid);
    }

    @Test
    public void isValid_SecondElement_AllGroups_false() {
        ConstraintValidatorContext mock = mock(ConstraintValidatorContext.class);
        boolean isValid = objectUnderTests.isValid(
                Arrays.asList(
                        new ClientQueryGroup(),
                        new ClientQueryGroup.ClientAllGroup()
                ),
                mock
        );
        verifyNoInteractions(mock);
        assertFalse(isValid);
    }

    @Test
    public void isValid_without_AllGroups_true() {
        ConstraintValidatorContext mock = mock(ConstraintValidatorContext.class);
        boolean isValid = objectUnderTests.isValid(
                Arrays.asList(
                        new ClientQueryGroup(),
                        new ClientQueryGroup()
                ),
                mock
        );
        verifyNoInteractions(mock);
        assertTrue(isValid);
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

        ErrorDescriptor expected = (QUERY_GROUPBY_ALLGROUP_NOT_FIRST.createDescriptor(TEST_PATH));

        ErrorDescriptor actual = objectUnderTests.build(violation);
        assertEquals(expected, actual);
    }

}