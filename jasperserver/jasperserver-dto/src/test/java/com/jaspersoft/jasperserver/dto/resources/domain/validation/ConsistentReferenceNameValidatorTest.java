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
import com.jaspersoft.jasperserver.dto.common.MessageAgnosticErrorDescriptor;
import com.jaspersoft.jasperserver.dto.resources.ClientProperty;
import com.jaspersoft.jasperserver.dto.resources.domain.ClientDomain;
import com.jaspersoft.jasperserver.dto.resources.domain.JoinResourceGroupElement;
import com.jaspersoft.jasperserver.dto.resources.domain.ReferenceElement;
import com.jaspersoft.jasperserver.dto.resources.domain.ResourceGroupElement;
import com.jaspersoft.jasperserver.dto.resources.domain.Schema;
import org.junit.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 * @since
 */
public class ConsistentReferenceNameValidatorTest {
    private ConsistentReferenceNameValidator validator = new ConsistentReferenceNameValidator();

    @Test
    public void getReferencedResourceName(){
        assertEquals("name3", validator.getReferencedResourceName("name1.name2.name3"));
        assertEquals("name1", validator.getReferencedResourceName("name1"));
    }

    @Test
    public void isValid_true(){
        assertTrue(validator
                .isValid(new ReferenceElement().setName("name3").setReferencePath("name1.name2.name3"), null));
    }

    @Test
    public void isValid_false(){
        assertFalse(validator
                .isValid(new ReferenceElement().setName("name2").setReferencePath("name1.name2.name3"), null));
    }
    
    @Test
    public void build_withValidationContext_withResourcePathProperty(){
        final ConstraintViolation constraintViolation = mock(ConstraintViolation.class);
        final Path path = mock(Path.class);
        when(constraintViolation.getPropertyPath()).thenReturn(path);
        final String expectedJsonPath = "schema.resources[1].elements[2]";
        when(path.toString()).thenReturn(expectedJsonPath);

        final ClientDomain clientDomain = new ClientDomain().setSchema(
                new Schema().setResources((List) Arrays.asList(
                        new ResourceGroupElement(),
                        new JoinResourceGroupElement().setName("joinGroupName")
                        .setElements((List) Arrays.asList(
                                new ReferenceElement(),
                                new ReferenceElement(),
                                new ReferenceElement().setName("referenceElementName")
                        ))))
        );

        when(constraintViolation.getRootBean()).thenReturn(clientDomain);
        when(constraintViolation.getInvalidValue())
                .thenReturn(new ReferenceElement().setName("someName").setReferencePath("name1.name2"));
        final ErrorDescriptor result = validator.build(constraintViolation);
        final ErrorDescriptor expectedErrorDescriptor = new ErrorDescriptor()
                .setErrorCode("domain.schema.resources.join.reference.name.inconsistent")
                .addProperties(
                        new ClientProperty("expectedName", "name2"),
                        new ClientProperty("currentName", "someName")
                );
        assertTrue(new MessageAgnosticErrorDescriptor(expectedErrorDescriptor)
                .equals(new MessageAgnosticErrorDescriptor(result)));
    }
}
