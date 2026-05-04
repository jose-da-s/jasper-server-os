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

package com.jaspersoft.jasperserver.jaxrs.common.validation;

import com.jaspersoft.jasperserver.dto.adhoc.query.validation.NotEmpty;
import org.junit.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.metadata.ConstraintDescriptor;

import java.lang.annotation.Annotation;

import static com.jaspersoft.jasperserver.dto.common.CommonErrorCode.Codes.MANDATORY_PARAMETER_ERROR;
import static com.jaspersoft.jasperserver.jaxrs.common.validation.DefaultConstraintAnnotation.NOT_EMPTY;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.jaspersoft.jasperserver.jaxrs.common.validation.DefaultConstraintAnnotation.NOT_NULL;
import static com.jaspersoft.jasperserver.jaxrs.common.validation.DefaultConstraintAnnotation.getErrorCode;

public class DefaultConstraintAnnotationTest {

    @Test
    public void getErrorCode_violationForNotNullHasDefaultMsgTemplate_returnMandatoryErrorCode() {
        ConstraintViolation violation = mockViolation(NotNull.class, NOT_NULL.defaultErrorCode);

        assertEquals(MANDATORY_PARAMETER_ERROR, getErrorCode(violation));
    }

    @Test
    public void getErrorCode_violationForNotNullHasNonDefaultMsgTemplate_returnNonDefaultMsg() {
        ConstraintViolation violation = mockViolation(NotNull.class, "some.error.code");

        assertEquals("some.error.code", getErrorCode(violation));
    }

    @Test
    public void getErrorCode_violationForNotEmptyHasDefaultMsgTemplate_returnMandatoryErrorCode() {
        ConstraintViolation violation = mockViolation(NotEmpty.class, NOT_EMPTY.defaultErrorCode);

        assertEquals(MANDATORY_PARAMETER_ERROR, getErrorCode(violation));
    }

    @Test
    public void getErrorCode_violationForNotEmptyHasNonDefaultMsgTemplate_returnNonDefaultMsg() {
        ConstraintViolation violation = mockViolation(NotEmpty.class, "some.error.code");

        assertEquals("some.error.code", getErrorCode(violation));
    }

    private ConstraintViolation mockViolation(Class clazz, String messageTemplate)  {
        ConstraintViolation violation = mock(ConstraintViolation.class);
        ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
        Annotation annotation = mock(Annotation.class);

        when(annotation.annotationType()).thenReturn(clazz);
        when(descriptor.getAnnotation()).thenReturn(annotation);
        when(violation.getConstraintDescriptor()).thenReturn(descriptor);
        when(violation.getMessageTemplate()).thenReturn(messageTemplate);

        return violation;
    }
}
