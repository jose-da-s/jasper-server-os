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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import javax.validation.ConstraintValidatorContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * @author Olexandr Dahno <odahno@tibco.com>
 */

class ParameterMapValidatorTest {

    private ParameterMapValidator objectUnderTests = new ParameterMapValidator();
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
        ParameterMap container = mock(ParameterMap.class);

        objectUnderTests.initialize(container);
        verifyNoInteractions(container);
    }

    /*
     * isValid
     */

    @Test
    public void isValid_nullMap_true() {
        ConstraintValidatorContext stub = mock(ConstraintValidatorContext.class);

        boolean isValid = objectUnderTests.isValid(null, stub);

        assertTrue(isValid);
        verifyNoInteractions(stub);
    }

    @Test
    public void isValid_emptyMap_true() {
        ConstraintValidatorContext stub = mock(ConstraintValidatorContext.class);

        Map<String, ClientExpressionContainer> map = new HashMap<String, ClientExpressionContainer>();

        boolean isValid = objectUnderTests.isValid(map, stub);

        assertTrue(isValid);
        verifyNoInteractions(stub);
    }

    @Test
    public void isValid_mapWithKeyIsNull_false() {
        ConstraintValidatorContext stub = mock(ConstraintValidatorContext.class);

        Map<String, ClientExpressionContainer> map = new HashMap<String, ClientExpressionContainer>();
        map.put(null, null);

        boolean isValid = objectUnderTests.isValid(map, stub);

        assertFalse(isValid);
        verifyNoInteractions(stub);
    }

    @Test
    public void isValid_mapWithKeyContainsDot_false() {
        ConstraintValidatorContext stub = mock(ConstraintValidatorContext.class);

        Map<String, ClientExpressionContainer> map = new HashMap<String, ClientExpressionContainer>();
        map.put(".", null);

        boolean isValid = objectUnderTests.isValid(map, stub);

        assertFalse(isValid);
        verifyNoInteractions(stub);
    }

    @Test
    public void isValid_paramNameStartWithUnderscore_false() {
        assertTrue(objectUnderTests.isValid(Collections.singletonMap("_test", null), null));
    }

    @Test
    public void isValid_validParameterName_true() {
        assertTrue(objectUnderTests.isValid(Collections.singletonMap("test", null), null));
    }

    @Test
    public void isValid_parameterNameContainsUnderline_true() {
        assertTrue(objectUnderTests.isValid(Collections.singletonMap("test_test", null), null));
    }

    @Test
    public void isValid_paramNameStartWithNumber_false() {
        assertFalse(objectUnderTests.isValid(Collections.singletonMap("1test", null), null));
    }

    @Test
    public void isValid_parameterNameContainsSpecialSymbol_false() {
        assertFalse(objectUnderTests.isValid(Collections.singletonMap("test+test", null), null));
    }

    @Test
    public void isValid_parameterNameContainsSpace_false() {
        assertFalse(objectUnderTests.isValid(Collections.singletonMap("test test", null), null));
    }

    @Test
    public void isValid_nameStartWithSpace_false() {
        assertFalse(objectUnderTests.isValid(Collections.singletonMap("test ", null), null));
    }

    @Test
    public void isValid_nameEndsWithSpace_false() {
        assertFalse(objectUnderTests.isValid(Collections.singletonMap(" test", null), null));
    }
}