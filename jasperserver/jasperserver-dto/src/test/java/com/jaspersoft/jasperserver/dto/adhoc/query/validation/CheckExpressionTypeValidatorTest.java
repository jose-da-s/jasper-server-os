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
import org.mockito.junit.MockitoJUnitRunner;

import jakarta.validation.ConstraintViolation;
import java.util.Set;

import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorCode.QUERY_WHERE_EXPRESSION_TYPE_NOT_SUPPORTED;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author vspachyn
 */
@RunWith(MockitoJUnitRunner.class)
public class CheckExpressionTypeValidatorTest {

    @InjectMocks
    private CheckExpressionTypeValidator checkExpressionTypeValidator;

    private Set<Class> acceptedExpressions = mock(Set.class);

    @Before
    public void setUp() throws Exception {
        when(acceptedExpressions.contains(eq(ClientOr.class))).thenReturn(true);
    }

    @Test
    public void initialize_value_success() {
        CheckExpressionType checkExpressionContainer = mock(CheckExpressionType.class);
        when(checkExpressionContainer.value()).thenReturn(new Class[]{});

        checkExpressionTypeValidator.initialize(checkExpressionContainer);

        verify(checkExpressionContainer, atLeastOnce()).value();
    }

    @Test
    public void isValid_ClientOr_success() {
        boolean isValid = checkExpressionTypeValidator.isValid(new ClientExpressionContainer().setObject(new ClientOr()), null);

        assertThat(isValid, is(true));
    }

    @Test
    public void isValid_ClientNumber_NotValid() {
        boolean isValid = checkExpressionTypeValidator.isValid(new ClientExpressionContainer().setObject(new ClientNumber()), null);

        assertThat(isValid, is(false));
    }

    @Test
    public void isValid_whenClientExpressionContainerIsNull_returnTrue() {
        boolean isValid = checkExpressionTypeValidator.isValid(null, null);

        assertThat(isValid, is(true));
    }

    @Test
    public void isValid_whenObjectAndExpressionIsNull_returnTrue() {
        boolean isValid = checkExpressionTypeValidator.isValid(new ClientExpressionContainer()
                .setObject(null), null);

        assertThat(isValid, is(true));
    }

    @Test
    public void build_violation_success() {
        ConstraintViolation violation = mock(ConstraintViolation.class);
        when(violation.getInvalidValue()).thenReturn(new ClientExpressionContainer().setObject(new ClientNumber()));
        when(violation.getMessageTemplate()).thenReturn(QUERY_WHERE_EXPRESSION_TYPE_NOT_SUPPORTED.toString());

        ErrorDescriptor errorDescriptor = checkExpressionTypeValidator.build(violation);

        assertEquals(QUERY_WHERE_EXPRESSION_TYPE_NOT_SUPPORTED.createDescriptor("ClientNumber"), errorDescriptor);
    }
}