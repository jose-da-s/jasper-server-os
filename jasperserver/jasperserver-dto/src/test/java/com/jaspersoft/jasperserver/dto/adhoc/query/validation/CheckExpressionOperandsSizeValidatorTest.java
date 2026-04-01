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
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import java.util.ArrayList;
import java.util.Arrays;

import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorCode.QUERY_EXPRESSION_OPERANDS_SIZE_OF_BOUND;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * <p></p>
 *
 * @author Volodya Sabadosh
 * @version $Id$
 */
@RunWith(MockitoJUnitRunner.class)
public class CheckExpressionOperandsSizeValidatorTest {
    CheckExpressionOperandsSizeValidator validator = new CheckExpressionOperandsSizeValidator();
    ConstraintValidatorContext validatorContext = mock(ConstraintValidatorContext.class);
    CheckExpressionOperandsSize annotation = mock(CheckExpressionOperandsSize.class);

    @Before
    public void initialize() {
        when(annotation.min()).thenReturn(1);
        when(annotation.max()).thenReturn(2);

        validator.initialize(annotation);
    }

    @Test
    public void isValid_operandsIsNull_returnTrue() {
        boolean valid = validator.isValid(null, validatorContext);
        assertTrue(valid);
    }

    @Test
    public void isValid_operandsSizeIs1_returnTrue() {
        boolean valid = validator.isValid(Arrays.asList(mock(ClientExpression.class)), validatorContext);
        assertTrue(valid);
    }

    @Test
    public void isValid_operandsSizeIs2_returnTrue() {
        boolean valid = validator.isValid(Arrays.asList(mock(ClientExpression.class), mock(ClientExpression.class)),
                validatorContext);
        assertTrue(valid);
    }

    @Test
    public void isValid_operandsSizeIs0_returnFalse() {
        boolean valid = validator.isValid(new ArrayList<ClientExpression>(), validatorContext);
        assertFalse(valid);
    }

    @Test
    public void isValid_operandsSizeIs3_returnFalse() {
        boolean valid = validator.isValid(Arrays.asList(mock(ClientExpression.class), mock(ClientExpression.class),
                mock(ClientExpression.class)), validatorContext);
        assertFalse(valid);
    }

    @Test
    public void build_success() {
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("path1");
        ConstraintViolation violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(path);

        ErrorDescriptor errorDescriptor = validator.build(violation);

        assertEquals(QUERY_EXPRESSION_OPERANDS_SIZE_OF_BOUND.createDescriptor("path1", "1", "2"), errorDescriptor);
    }

}
