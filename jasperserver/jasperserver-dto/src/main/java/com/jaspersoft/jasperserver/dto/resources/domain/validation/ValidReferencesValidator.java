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
import com.jaspersoft.jasperserver.dto.common.ValidationErrorDescriptorBuilder;
import com.jaspersoft.jasperserver.dto.resources.ClientProperty;
import com.jaspersoft.jasperserver.dto.resources.domain.PresentationElement;
import com.jaspersoft.jasperserver.dto.resources.domain.PresentationGroupElement;
import com.jaspersoft.jasperserver.dto.resources.domain.PresentationSingleElement;
import com.jaspersoft.jasperserver.dto.resources.domain.ResourceElement;
import com.jaspersoft.jasperserver.dto.resources.domain.Schema;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.resources.domain.DomainSchemaHelper.findResourceElement;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
public class ValidReferencesValidator implements ConstraintValidator<ValidReferences, Schema>, ValidationErrorDescriptorBuilder {

    @Override
    public void initialize(ValidReferences constraintAnnotation) {
    }

    @Override
    public boolean isValid(Schema value, ConstraintValidatorContext context) {
        return findInvalidReferences(value).isEmpty();
    }

    private List<String> findInvalidReferences(Schema schema){
        final List<String> result = new ArrayList<String>();
        final List<PresentationGroupElement> presentation = schema.getPresentation();
        final List<ResourceElement> resources = schema.getResources();
        fillInvalidReferences((List)presentation, resources, result);
        return result;
    }

    private void fillInvalidReferences(List<PresentationElement> presentationElements, List<ResourceElement> resources,
            List<String> invalidReferences){
        if(presentationElements != null){
            for (PresentationElement presentationElement : presentationElements) {
                if(presentationElement instanceof PresentationSingleElement){
                    final PresentationSingleElement presentationSingleElement = (PresentationSingleElement) presentationElement;
                    final String resourcePath = presentationSingleElement.getResourcePath();
                    if(resourcePath != null && findResourceElement(resourcePath, resources) == null) {
                        invalidReferences.add(resourcePath);
                    }
                } else if(presentationElement instanceof PresentationGroupElement){
                    fillInvalidReferences(((PresentationGroupElement) presentationElement).getElements(), resources, invalidReferences);
                }
            }
        }

    }

    @Override
    public ErrorDescriptor build(ConstraintViolation violation) {
        final List<String> invalidReferences = findInvalidReferences((Schema) violation.getInvalidValue());
        final ErrorDescriptor errorDescriptor = new ErrorDescriptor().setErrorCode(ValidReferences.errorCode)
                .setMessage("Schema contains invalid references: " + invalidReferences.toString());
        for (String invalidReference : invalidReferences) {
            errorDescriptor.addProperties(new ClientProperty().setKey(ValidReferences.PROPERTY_INVALID_REFERENCE)
                    .setValue(invalidReference));
        }
        return errorDescriptor;
    }
}
