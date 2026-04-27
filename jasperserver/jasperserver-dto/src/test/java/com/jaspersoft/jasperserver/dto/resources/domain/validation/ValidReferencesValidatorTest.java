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

package com.jaspersoft.jasperserver.dto.resources.domain.validation;

import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.resources.ClientProperty;
import com.jaspersoft.jasperserver.dto.resources.domain.PresentationElement;
import com.jaspersoft.jasperserver.dto.resources.domain.PresentationGroupElement;
import com.jaspersoft.jasperserver.dto.resources.domain.PresentationSingleElement;
import com.jaspersoft.jasperserver.dto.resources.domain.ResourceElement;
import com.jaspersoft.jasperserver.dto.resources.domain.ResourceSingleElement;
import com.jaspersoft.jasperserver.dto.resources.domain.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.validation.ConstraintViolation;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

/**
 * @author Andriy Tivodar <ativodar@tibco>
 */

public class ValidReferencesValidatorTest {
    private static final String ERROR_CODE = "domain.schema.invalid.reference";
    private static final String ERROR_MESSAGE = "Schema contains invalid references: [valid]";
    private static final String VALID_PATH = "valid";
    private static final String INVALID_PATH = "invalid";
    private static final String INVALID_REFERENCE_EXCEPTION_VALUE = "invalidReference";

    private static final ResourceElement RESOURCE_ELEMENT_WITH_INVALID_PATH_NAME = new ResourceSingleElement().setName(INVALID_PATH);
    private static final ResourceElement RESOURCE_ELEMENT_WITH_VALID_PATH_NAME = new ResourceSingleElement().setName(VALID_PATH);

    private static final PresentationSingleElement SINGLE_ELEMENT_WITH_VALID_RESOURCE_PATH =
            new PresentationSingleElement().setResourcePath(VALID_PATH);
    private static final PresentationGroupElement GROUP_ELEMENT_WITH_SINGLE_ELEMENT =
            new PresentationGroupElement().setElements(Collections.<PresentationElement>singletonList(SINGLE_ELEMENT_WITH_VALID_RESOURCE_PATH));

    private static final ClientProperty CLIENT_PROPERTY_WITH_VALID_PATH_VALUE = new ClientProperty().setKey(INVALID_REFERENCE_EXCEPTION_VALUE).setValue(VALID_PATH);
    private static final ErrorDescriptor ERROR_DESCRIPTOR = new ErrorDescriptor()
            .setErrorCode(ERROR_CODE)
            .setMessage(ERROR_MESSAGE)
            .setProperties(Collections.singletonList(CLIENT_PROPERTY_WITH_VALID_PATH_VALUE));

    private ConstraintViolation constraintViolation;
    private ValidReferencesValidator validReferencesValidator;
    private Schema schema;

    @BeforeEach
    public void beforeEach() {
        init();
        setup();
    }

    private void init() {
        constraintViolation = Mockito.mock(ConstraintViolation.class);
        validReferencesValidator = new ValidReferencesValidator();
        schema = new Schema();
    }

    private void setup() {
        schema.setPresentation(Collections.singletonList(GROUP_ELEMENT_WITH_SINGLE_ELEMENT));
        schema.setResources(Collections.singletonList(RESOURCE_ELEMENT_WITH_VALID_PATH_NAME));

        doReturn(schema).when(constraintViolation).getInvalidValue();
    }

    @Test
    public void isValid_invalidReference_false() {
        schema.setResources(Collections.singletonList(RESOURCE_ELEMENT_WITH_INVALID_PATH_NAME));

        boolean result = validReferencesValidator.isValid(schema, null);
        assertFalse(result);
    }

    @Test
    public void isValid_validReference_true() {
        boolean result = validReferencesValidator.isValid(schema, null);
        assertTrue(result);
    }

    @Test
    public void build_invalidReference_errorDescriptor() {
        schema.setResources(null);

        ErrorDescriptor result = validReferencesValidator.build(constraintViolation);
        assertEquals(ERROR_DESCRIPTOR, result);
    }
}
