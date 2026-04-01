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

import com.jaspersoft.jasperserver.core.util.type.GenericTypeProcessorRegistry;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.common.ValidationErrorDescriptorBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.metadata.ConstraintDescriptor;
import java.lang.annotation.Annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@RunWith(MockitoJUnitRunner.class)
public class ErrorDescriptorBuilderFinderTest {
    @Mock
    private GenericTypeProcessorRegistry genericTypeProcessorRegistry;
    @InjectMocks
    private ErrorDescriptorBuilderFinder errorDescriptorBuilderFinder;


    @Test
    public void find_violationContainsBuilder_returnInitializedBuilder() {
        ConstraintViolation violation = mockViolation(true, ConstraintValidatorMock.class);
        mockTypeProcessor(true);

        ValidationErrorDescriptorBuilder errorDescriptorBuilder = errorDescriptorBuilderFinder.find(violation);

        assertSame(errorDescriptorBuilder.getClass(), ConstraintValidatorMock.class);
    }

    @Test(expected = IllegalStateException.class)
    public void find_violationContainsBuilderButInitIsFail_throwException() {
        ConstraintViolation violation = mockViolation(true,
                ConstraintValidatorMockWithoutDefaultConstructor.class);
        mockTypeProcessor(true);

        ValidationErrorDescriptorBuilder errorDescriptorBuilder = errorDescriptorBuilderFinder.find(violation);

        assertSame(errorDescriptorBuilder.getClass(), ConstraintValidatorMock.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void find_violationNotContainsBuilderButTypeProcessorCanFind_returnInitializedBuilder() {
        ConstraintViolation violation = mockViolation(false, ConstraintValidatorMock.class);
        ValidationErrorDescriptorBuilder builder = mockTypeProcessor(true);

        ValidationErrorDescriptorBuilder errorDescriptorBuilder = errorDescriptorBuilderFinder.find(violation);

        assertEquals(builder, errorDescriptorBuilder);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void find_violationAndTypeProcessorCannotFindBuilder_returnInitializedBuilder() {
        ConstraintViolation violation = mockViolation(false, ConstraintValidatorMock.class);
        mockTypeProcessor(false);

        ValidationErrorDescriptorBuilder errorDescriptorBuilder = errorDescriptorBuilderFinder.find(violation);

        assertSame(DefaultConstraintValidationErrorDescriptorBuilder.class, errorDescriptorBuilder.getClass());
    }

    private ValidationErrorDescriptorBuilder mockTypeProcessor(boolean canFindValidationBuilder) {
        reset(genericTypeProcessorRegistry);
        ValidationErrorDescriptorBuilder builder = null;
        if (canFindValidationBuilder) {
            builder = mock(ValidationErrorDescriptorBuilder.class);
        }
        when(genericTypeProcessorRegistry.getTypeProcessor(any(Class.class), any(Class.class), anyBoolean()))
                .thenReturn(builder);
        return builder;
    }

    private ConstraintViolation mockViolation(boolean containsValidatorBuilder,
                                              Class<? extends ConstraintValidator> validatorClass) {
        ConstraintViolation violation = mock(ConstraintViolation.class);
        ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
        Annotation annotation = mock(Annotation.class);
        when(descriptor.getAnnotation()).thenReturn(annotation);
        if (containsValidatorBuilder) {
            when(descriptor.getConstraintValidatorClasses()).thenReturn(singletonList(validatorClass));
        } else {
            when(descriptor.getConstraintValidatorClasses()).thenReturn(emptyList());
        }

        when(violation.getConstraintDescriptor()).thenReturn(descriptor);

        return violation;
    }

    private static class ConstraintValidatorMock implements ConstraintValidator, ValidationErrorDescriptorBuilder {

        public ConstraintValidatorMock() {
            super();
        }

        @Override
        public void initialize(Annotation constraintAnnotation) {
            //Do Nothing;
        }

        @Override
        public ErrorDescriptor build(ConstraintViolation violation) {
            return null;
        }

        @Override
        public boolean isValid(Object value, ConstraintValidatorContext context) {
            return false;
        }
    }

    private static class ConstraintValidatorMockWithoutDefaultConstructor extends ConstraintValidatorMock {}

}
