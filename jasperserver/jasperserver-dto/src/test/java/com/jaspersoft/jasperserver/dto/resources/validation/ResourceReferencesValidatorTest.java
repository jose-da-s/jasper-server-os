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

package com.jaspersoft.jasperserver.dto.resources.validation;

import com.google.common.collect.ImmutableMap;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.resources.ClientFile;
import com.jaspersoft.jasperserver.dto.resources.ClientReference;
import com.jaspersoft.jasperserver.dto.resources.ClientReferenceableFile;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Alexei Skorodumov askorodu@tibco.com
 */
public class ResourceReferencesValidatorTest {
    private static final String DATA_FILE_FIELD = "dataFile";
    private static final String PROPERTY_PATH = "resources";

    private ResourceReferencesValidator validator = new ResourceReferencesValidator();

    @Mock
    private ClientFile clientFile = mock(ClientFile.class);
    @Mock
    private ClientReference clientReference = mock(ClientReference .class);
    @Mock
    private ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
    @Mock
    private ConstraintViolation violation = mock(ConstraintViolation.class);
    @Mock
    private Path path = mock(Path.class);

    private Map<String, ClientReferenceableFile> invalidValue =
            ImmutableMap.<String, ClientReferenceableFile>of(DATA_FILE_FIELD, clientFile);
    private Map<String, ClientReferenceableFile> correctValue =
            ImmutableMap.<String, ClientReferenceableFile>of(DATA_FILE_FIELD, clientReference);

    @Before
    public void setUp() throws Exception {
        when(violation.getInvalidValue()).thenReturn(invalidValue);
        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn(PROPERTY_PATH);
    }

    @Test
    public void isValid_null_valid() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    public void isValid_mapWithFileReference_valid() {
        assertTrue(validator.isValid(correctValue, context));
    }

    @Test
    public void isValid_mapWithFileResource_invalid() {
        assertFalse(validator.isValid(invalidValue, context));
    }

    @Test
    public void build_returnsCorrectErrorDescriptor() {
        ErrorDescriptor errorDescriptor = validator.build(violation);

        assertEquals(ValidResourceReferences.ERROR_CODE, errorDescriptor.getErrorCode());
        assertNotNull(errorDescriptor.getProperties());
        assertEquals(1, errorDescriptor.getProperties().size());

        assertEquals(ValidResourceReferences.PROPERTY_INVALID_REFERENCE, errorDescriptor.getProperties().get(0).getKey());
        assertEquals("resources.dataFile.fileReference.uri", errorDescriptor.getProperties().get(0).getValue());
    }
}
