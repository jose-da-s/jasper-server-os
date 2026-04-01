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

import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientExpressionContainer;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.literal.ClientNumber;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.logical.ClientOr;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import jakarta.validation.ConstraintViolation;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorCode.Codes.QUERY_WHERE_PARAMETERS_EXPRESSION_NOT_VALID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author vspachyn
 */
@RunWith(MockitoJUnitRunner.class)
public class CheckParametersExpressionContainerValidatorTest {

    @InjectMocks
    private CheckParametersExpressionContainerValidator checkParametersExpressionContainerValidator;
    @Mock
    private Set<Class> acceptedExpressions;

    @Before
    public void setUp() throws Exception {
        Mockito.when(acceptedExpressions.contains(eq(ClientOr.class))).thenReturn(false);
        Mockito.when(acceptedExpressions.contains(eq(ClientNumber.class))).thenReturn(true);
    }

    @Test
    public void initialize_success() {
        CheckParametersExpressionContainer checkParametersExpressionContainer = mock(CheckParametersExpressionContainer.class);
        when(checkParametersExpressionContainer.value()).thenReturn(new Class[]{});

        checkParametersExpressionContainerValidator.initialize(checkParametersExpressionContainer);

        verify(checkParametersExpressionContainer, atLeastOnce()).value();
    }

    @Test
    public void isValid_expressionMapIsNull_returnTrue() {
        assertTrue(checkParametersExpressionContainerValidator.isValid(null, null));
    }

    @Test
    public void isValid_expressionMapIsEmpty_returnTrue() {
        assertTrue(checkParametersExpressionContainerValidator.isValid(new HashMap<String, ClientExpressionContainer>(), null));
    }

    @Test
    public void isValid_expressionValueIsNull_returnFalse() {
        HashMap<String, ClientExpressionContainer> expressionMap = new HashMap<String, ClientExpressionContainer>() {{
            put("test", null);
        }};
        assertFalse(checkParametersExpressionContainerValidator.isValid(expressionMap, null));
    }

    @Test
    public void isValid_emptyClientExpressionContainer_returnFalse() {
        HashMap<String, ClientExpressionContainer> expressionMap = new HashMap<String, ClientExpressionContainer>() {{
            put("test", new ClientExpressionContainer());
        }};
        assertFalse(checkParametersExpressionContainerValidator.isValid(expressionMap, null));
    }

    @Test
    public void isValid_ClientExpressionContainerWithString_returnTrue() {
        HashMap<String, ClientExpressionContainer> expressionMap = new HashMap<String, ClientExpressionContainer>() {{
            put("test", new ClientExpressionContainer().setString("test expression"));
        }};
        assertTrue(checkParametersExpressionContainerValidator.isValid(expressionMap, null));
    }

    @Test
    public void isValid_ClientExpressionContainerWithInteger_returnTrue() {
        HashMap<String, ClientExpressionContainer> expressionMap = new HashMap<String, ClientExpressionContainer>() {{
            put("test", new ClientExpressionContainer().setObject(new ClientNumber()));
        }};
        assertTrue(checkParametersExpressionContainerValidator.isValid(expressionMap, null));
    }

    @Test
    public void isValid_ClientExpressionContainerWithNonValidFunction_returnFalse() {
        HashMap<String, ClientExpressionContainer> expressionMap = new HashMap<String, ClientExpressionContainer>() {{
            put("test", new ClientExpressionContainer().setObject(new ClientOr()));
        }};
        assertFalse(checkParametersExpressionContainerValidator.isValid(expressionMap, null));
    }

    @Test
    public void isValid_ClientExpressionContainerMultipleExpressionsWithOneInvalid_returnFalse() {
        HashMap<String, ClientExpressionContainer> expressionMap = new LinkedHashMap<String, ClientExpressionContainer>();
        expressionMap.put("firstValid", new ClientExpressionContainer().setObject(new ClientNumber(10)));
        expressionMap.put("secondValid", new ClientExpressionContainer().setString("integer(10)"));
        expressionMap.put("thirdInvalid", new ClientExpressionContainer());
        assertFalse(checkParametersExpressionContainerValidator.isValid(expressionMap, null));
    }

    @Test
    public void build_success() {
        ConstraintViolation violation = mock(ConstraintViolation.class);
        ErrorDescriptor errorDescriptor = checkParametersExpressionContainerValidator.build(violation);
        assertEquals(new ErrorDescriptor().setErrorCode(QUERY_WHERE_PARAMETERS_EXPRESSION_NOT_VALID), errorDescriptor);
    }
}