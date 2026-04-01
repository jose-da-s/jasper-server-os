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

import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.resources.ClientProperty;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.constraints.Size;
import jakarta.validation.metadata.ConstraintDescriptor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Volodya Sabadosh
 */
public class SizeConstraintValidationErrorDescriptorBuilderTest {
    private SizeConstraintValidationErrorDescriptorBuilder builder = new SizeConstraintValidationErrorDescriptorBuilder();
    @Mock
    private ConstraintViolation violation;
    private int minValue = 3;
    private int maxValue = 10;
    private String errorCode = "some.error.code";
    private String propertyPath = "path.specPropertyName";
    private String invalidValue = "invalid value";

    @Before
    public void before() {
        violation = mock(ConstraintViolation.class);
        ConstraintDescriptor mockDescriptor = mock(ConstraintDescriptor.class);
        Path mockPath = mock(Path.class);
        Object mockInvalidValue = mock(Object.class);

        Size mockSize = mock(Size.class);
        when(mockSize.min()).thenReturn(minValue);
        when(mockSize.max()).thenReturn(maxValue);
        when(mockSize.message()).thenReturn(errorCode);

        when(mockDescriptor.getAnnotation()).thenReturn(mockSize);

        when(mockPath.toString()).thenReturn(propertyPath);
        when(mockInvalidValue.toString()).thenReturn(invalidValue);

        when(violation.getConstraintDescriptor()).thenReturn(mockDescriptor);
        when(violation.getPropertyPath()).thenReturn(mockPath);
        when(violation.getInvalidValue()).thenReturn(mockInvalidValue);
    }

    @Test
    public void build_someViolation_returnDescriptor() {
        ErrorDescriptor expectedErrorDescriptor = new ErrorDescriptor().setErrorCode(errorCode).
                addProperties(
                    new ClientProperty("invalidValue",  String.valueOf(invalidValue)),
                    new ClientProperty("actualLength",  String.valueOf(invalidValue.length())),
                    new ClientProperty("maxLimit",  String.valueOf(maxValue)),
                    new ClientProperty("minLimit",  String.valueOf(minValue)),
                    new ClientProperty("propertyPath",  String.valueOf(propertyPath))
                )
                .setMessage("Length of field \"specPropertyName\" breaks length limit.");

        assertEquals(expectedErrorDescriptor, builder.build(violation));
    }

}
