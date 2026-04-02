/*
 * Copyright (C) 2025-2026 the Jasper Server OS Authors
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
package com.jaspersoft.jasperserver.jaxrs.common;

import com.jaspersoft.jasperserver.api.JSValidationException;
import com.jaspersoft.jasperserver.api.common.domain.impl.ValidationErrorImpl;
import com.jaspersoft.jasperserver.api.common.domain.impl.ValidationErrorsImpl;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import org.testng.annotations.Test;

import jakarta.ws.rs.core.Response;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
public class JSValidationErrorCodeMapperTest {
    private JSValidationExceptionMapper mapper = new JSValidationExceptionMapper();

    @Test
    public void toResponse(){
        final ValidationErrorsImpl errors = new ValidationErrorsImpl();
        final ValidationErrorImpl error = new ValidationErrorImpl();
        final Object[] arguments1 = new Object[]{"test1", "test2"};
        final String testField1 = "testField1";
        final String testErrorCode1 = "testErrorCode1";
        final String defaultMessage1 = "Test Message 1";
        error.setField(testField1);
        error.setDefaultMessage(defaultMessage1);
        error.setErrorArguments(arguments1);
        error.setErrorCode(testErrorCode1);
        errors.add(error);
        final Object[] arguments2 = new Object[]{"test3", "test4"};
        final String testField2 = "testField2";
        final String testErrorCode2 = "testErrorCode2";
        final String defaultMessage2 = "Test Message 2";
        error.setField(testField2);
        error.setDefaultMessage(defaultMessage2);
        error.setErrorArguments(arguments2);
        error.setErrorCode(testErrorCode2);
        errors.add(error);
        final Response response = mapper.toResponse(new JSValidationException(errors));
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        final Object entity = response.getEntity();
        assertTrue(entity instanceof List);
        List<ErrorDescriptor> descriptors = (List<ErrorDescriptor>) entity;
        assertEquals(descriptors.size(), 2);
        for(ErrorDescriptor descriptor : descriptors){
            final String[] parameters = descriptor.getParameters();
            assertNotNull(parameters);
            assertEquals(parameters.length, 3);
            if(defaultMessage1.equals(descriptor.getMessage())){
                assertEquals(parameters[0], testField1);
                assertEquals(parameters[1], arguments1[0]);
                assertEquals(parameters[2], arguments1[1]);
                assertEquals(descriptor.getErrorCode(), testErrorCode1);
            } else {
                assertEquals(descriptor.getMessage(), defaultMessage2);
                assertEquals(parameters[0], testField2);
                assertEquals(parameters[1], arguments2[0]);
                assertEquals(parameters[2], arguments2[1]);
                assertEquals(descriptor.getErrorCode(), testErrorCode2);
            }
        }

    }
}
