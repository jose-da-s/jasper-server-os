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

import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.common.ValidationErrorDescriptorBuilder;
import com.jaspersoft.jasperserver.dto.resources.ClientFile;
import com.jaspersoft.jasperserver.dto.resources.ClientProperty;
import com.jaspersoft.jasperserver.dto.resources.ClientReferenceableFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Alexei Skorodumov <askorodu@tibco.com>
 */
public class ResourceReferencesValidator implements ConstraintValidator<ValidResourceReferences, Map<String, ClientReferenceableFile>>, ValidationErrorDescriptorBuilder {
    private static final String MANDATORY_PARAMETER = "fileReference.uri";
    @Override
    public void initialize(ValidResourceReferences constraintAnnotation) {
    }

    @Override
    public boolean isValid(Map<String, ClientReferenceableFile> value, ConstraintValidatorContext context) {
        return findInvalidReferences(value).isEmpty();
    }

    @Override
    public ErrorDescriptor build(ConstraintViolation violation) {
        @SuppressWarnings("unchecked")
        List<String> invalidReferences = findInvalidReferences((Map) violation.getInvalidValue());
        ErrorDescriptor errorDescriptor = new ErrorDescriptor().setErrorCode(ValidResourceReferences.ERROR_CODE)
                .setMessage("The mandatory parameter \"" + MANDATORY_PARAMETER + "\" is missing.");

        for (String invalidReference : invalidReferences) {
            errorDescriptor.addProperties(new ClientProperty().setKey(ValidResourceReferences.PROPERTY_INVALID_REFERENCE)
                    .setValue("" + violation.getPropertyPath() + "." + invalidReference));
        }
        return errorDescriptor;
    }

    private List<String> findInvalidReferences(Map<String, ClientReferenceableFile> value) {
        List<String> invalidResources = new ArrayList<String>();

        if (value != null) {
            for (Map.Entry<String, ClientReferenceableFile> entry : value.entrySet()) {
                if (entry.getValue() instanceof ClientFile) {
                    invalidResources.add("" + entry.getKey() + "." + MANDATORY_PARAMETER);
                }
            }
        }

        return invalidResources;
    }
}
