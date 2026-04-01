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
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptorTemplate;
import com.jaspersoft.jasperserver.dto.common.ValidationErrorDescriptorBuilder;
import com.jaspersoft.jasperserver.dto.resources.ClientProperty;

import jakarta.validation.ConstraintViolation;

import static com.jaspersoft.jasperserver.dto.common.validations.ArrayUtils.objectToString;
import static com.jaspersoft.jasperserver.dto.common.ErrorDescriptorTemplateRegistry.fromCode;

/**
 * @author Volodya Sabadosh
 */
public class DefaultConstraintValidationErrorDescriptorBuilder implements ValidationErrorDescriptorBuilder {
    @Override
    public ErrorDescriptor build(ConstraintViolation violation) {
        String errorCode = DefaultConstraintAnnotation.getErrorCode(violation);
        String propPath = violation.getPropertyPath().toString();
        String invalidValue = objectToString(violation.getInvalidValue());

        ErrorDescriptorTemplate errorTemplate = fromCode(errorCode);
        if (errorTemplate != null) {
            return errorTemplate.createDescriptor(propPath, invalidValue);
        }
        //As we don't have all codes registered in ErrorDescriptorTemplateRegistry, then lets leave this code for now.
        // In future we should remove this line.
        return new ErrorDescriptor().setErrorCode(errorCode).addProperties(
                        new ClientProperty("propertyPath", propPath),
                        new ClientProperty("invalidValue", invalidValue)
        );
    }
}
