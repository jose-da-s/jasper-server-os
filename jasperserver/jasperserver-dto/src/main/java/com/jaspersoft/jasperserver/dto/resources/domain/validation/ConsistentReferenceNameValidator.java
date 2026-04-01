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
import com.jaspersoft.jasperserver.dto.resources.domain.ReferenceElement;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
public class ConsistentReferenceNameValidator implements ConstraintValidator<ConsistentReferenceName, ReferenceElement>,
        ValidationErrorDescriptorBuilder {



    @Override
    public ErrorDescriptor build(ConstraintViolation violation) {
        final ReferenceElement invalidValue = (ReferenceElement) violation.getInvalidValue();
        String expectedName = getReferencedResourceName(invalidValue.getReferencePath());
        return new ErrorDescriptor().setErrorCode("domain.schema.resources.join.reference.name.inconsistent")
                .setMessage("Reference name should be the same as name of referenced resource. I.e. \"" + expectedName +
                        "\"").addProperties(
                        new ClientProperty("expectedName", expectedName),
                        new ClientProperty("currentName", invalidValue.getName())
                );
    }

    @Override
    public void initialize(ConsistentReferenceName constraintAnnotation) {
        // nothing for now
    }

    @Override
    public boolean isValid(ReferenceElement value, ConstraintValidatorContext context) {
        boolean result = true;
        final String referencePath = value.getReferencePath();
        if(referencePath != null && value.getName() != null){
            result = value.getName().equals(getReferencedResourceName(referencePath));
        }
        return result;
    }

    protected String getReferencedResourceName(String referencePath){
        return referencePath.substring(referencePath.lastIndexOf(".") + 1);
    }
}
