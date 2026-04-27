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
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.arithmetic.ClientAdd;
import com.jaspersoft.jasperserver.dto.adhoc.query.field.ClientQueryAggregatedField;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;

import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorCode.Codes.QUERY_AGGREGATE_DEFINITION_ERROR;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorCode.createDescriptorFrom;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Volodya Sabadosh
 * @version $Id$
 */
@RunWith(MockitoJUnitRunner.class)
public class CheckAggregateDefinitionValidatorTest {
    CheckAggregateDefinitionValidator validator = new CheckAggregateDefinitionValidator();
    ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
    private static final ClientExpressionContainer TEST_EXPRESSION_CONTAINER = new ClientExpressionContainer().setObject(new ClientAdd());
    private static final String TEST_AGGREGATE_FUNCTION = "TEST_AGGREGATE_FUNCTION";

    @Test
    public void isValidAggregate_true_without_AggregateFunction() {
        ClientQueryAggregatedField instance = new ClientQueryAggregatedField();

        assertTrue(validator.isValid(instance, context));
    }

    @Test
    public void isValidAggregate_true_with_ExpressionContainer_and_without_AggregateFunction() {
        ClientQueryAggregatedField instance = new ClientQueryAggregatedField()
                .setExpressionContainer(TEST_EXPRESSION_CONTAINER);

        assertTrue(validator.isValid(instance, context));
    }

    @Test
    public void isValidAggregate_true_without_ExpressionContainer_and_with_AggregateFunction() {
        ClientQueryAggregatedField instance = new ClientQueryAggregatedField()
                .setAggregateFunction(TEST_AGGREGATE_FUNCTION);

        assertTrue(validator.isValid(instance, context));
    }

    @Test
    public void isValidAggregate_false_with_ExpressionContainer_and_with_AggregateFunction() {
        ClientQueryAggregatedField instance = new ClientQueryAggregatedField()
                .setAggregateFunction(TEST_AGGREGATE_FUNCTION)
                .setExpressionContainer(TEST_EXPRESSION_CONTAINER);

        assertFalse(validator.isValid(instance, context));
    }

    @Test
    public void isValidAggregate_true_with_empty_in_ExpressionContainer_and_without_AggregateExpression() {
        ClientQueryAggregatedField instance = new ClientQueryAggregatedField()
                .setExpressionContainer(new ClientExpressionContainer(""));

        assertTrue(validator.isValid(instance, context));
    }

    @Test
    public void isValidAggregate_true_with_nonEmpty_in_ExpressionContainer_and_without_AggregateExpression() {
        ClientQueryAggregatedField instance = new ClientQueryAggregatedField()
                .setExpressionContainer(new ClientExpressionContainer("TEST_STRING"));

        assertTrue(validator.isValid(instance, context));
    }

    @Test
    public void isValidAggregate_true_with_null_in_ExpressionContainer_and_without_AggregateExpression() {
        ClientQueryAggregatedField instance = new ClientQueryAggregatedField()
                .setExpressionContainer(new ClientExpressionContainer().setString(null));

        assertTrue(validator.isValid(instance, context));
    }

    @Test
    public void isValidAggregate_true_without_ExpressionContainer_and_with_nonEmpty_AggregateFunction() {
        ClientQueryAggregatedField instance = new ClientQueryAggregatedField()
                .setAggregateFunction(TEST_AGGREGATE_FUNCTION);

        assertTrue(validator.isValid(instance, context));
    }

    @Test
    public void isValidAggregate_false_without_ExpressionContainer_and_with_Empty_AggregateFunction() {
        ClientQueryAggregatedField instance = new ClientQueryAggregatedField()
                .setAggregateFunction("");

        assertFalse(validator.isValid(instance, context));
    }

    @Test
    public void build_success() {
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("path1");
        ConstraintViolation violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessageTemplate()).thenReturn(QUERY_AGGREGATE_DEFINITION_ERROR);

        ErrorDescriptor errorDescriptor = validator.build(violation);

        assertEquals(createDescriptorFrom(violation.getMessageTemplate(), violation.getPropertyPath().toString()), errorDescriptor);
    }


}
