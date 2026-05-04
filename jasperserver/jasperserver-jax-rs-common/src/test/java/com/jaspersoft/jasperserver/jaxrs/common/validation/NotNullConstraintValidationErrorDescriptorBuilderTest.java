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

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;

import java.lang.annotation.Annotation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Volodya Sabadosh
 */
public class NotNullConstraintValidationErrorDescriptorBuilderTest {
    private NotNullConstraintValidationErrorDescriptorBuilder builder = new NotNullConstraintValidationErrorDescriptorBuilder();
    private ConstraintViolation violation;
    private String someErrorCode = "some.error.code";
    private String somePropertyPath = "some.property.path";
    private String someInvalidValue = "some.invalid.value";

    @Before
    public void before() {
        violation = mock(ConstraintViolation.class);
        ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
        Path path = mock(Path.class);
        Annotation someAnnotation = mock(Annotation.class);
        when(descriptor.getAnnotation()).thenReturn(someAnnotation);
        when(violation.getConstraintDescriptor()).thenReturn(descriptor);
        when(path.toString()).thenReturn(somePropertyPath);
        when(violation.getMessageTemplate()).thenReturn(someErrorCode);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getInvalidValue()).thenReturn(someInvalidValue);
    }

    @Test
    public void build_someViolation_returnDescriptor() {
        ErrorDescriptor expected = new ErrorDescriptor().setErrorCode(someErrorCode)
                .setParameters(somePropertyPath).addProperties(new ClientProperty("propertyPath",
                        violation.getPropertyPath().toString()));

        assertEquals(expected, builder.build(violation));
    }

}
