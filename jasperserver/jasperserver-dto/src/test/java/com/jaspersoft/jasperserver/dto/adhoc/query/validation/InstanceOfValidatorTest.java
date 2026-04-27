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

import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientVariable;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.literal.ClientString;
import com.jaspersoft.jasperserver.dto.common.ClientTypeUtility;
import com.jaspersoft.jasperserver.dto.resources.ClientProperty;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.metadata.ConstraintDescriptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

/**
 * @author serhii.blazhyievskyi
 * @version $Id$
 */
public class InstanceOfValidatorTest {

    private InstanceOfValidator validator = new InstanceOfValidator();
    @Mock
    private ConstraintValidatorContext constraintValidatorContextMock;
    @Mock
    private ConstraintViolation constraintViolationMock;
    @Mock
    private ConstraintDescriptor constraintDescriptorMock;
    @Mock
    private InstanceOf instanceOfMock;
    @Mock
    private Object invalidValueMock;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void isValid_nullInput_expectedTrue() {
        assertTrue(validator.isValid(null, constraintValidatorContextMock));
    }

    @Test
    public void isValid_expectedTrue() {
        validator.acceptedClasses.add(ClientVariable.class);

        assertTrue(validator.isValid(new ClientVariable("testVariable"), constraintValidatorContextMock));
    }

    @Test
    public void isValid_expectedFalse() {
        validator.acceptedClasses.add(ClientVariable.class);

        assertFalse(validator.isValid(new ClientString("testString"), constraintValidatorContextMock));
    }

    @Test
    public void buildErrorDescriptor() {
        String invalidType = ClientTypeUtility.extractClientType(invalidValueMock.getClass());

        doReturn(invalidValueMock).when(constraintViolationMock).getInvalidValue();
        doReturn(constraintDescriptorMock).when(constraintViolationMock).getConstraintDescriptor();
        doReturn(instanceOfMock).when(constraintDescriptorMock).getAnnotation();
        doReturn("message").when(instanceOfMock).message();
        doReturn("errorCode").when(instanceOfMock).errorCode();

        assertNotNull(validator.build(constraintViolationMock));
        assertEquals(validator.build(constraintViolationMock).getMessage(), "message");
        assertEquals(validator.build(constraintViolationMock).getErrorCode(), "errorCode");
        assertNotNull(validator.build(constraintViolationMock).getProperties());
        ClientProperty clientProperty = validator.build(constraintViolationMock).getProperties().get(0);
        assertEquals(clientProperty.getKey(), "invalidType");
        assertEquals(clientProperty.getValue(), invalidType);
    }

    @After
    public void after() {
        Mockito.reset(constraintValidatorContextMock, constraintViolationMock,
                constraintDescriptorMock, instanceOfMock, invalidValueMock);
    }
}