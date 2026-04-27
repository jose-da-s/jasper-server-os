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

import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.common.JavaAliasConverter;
import com.jaspersoft.jasperserver.dto.common.ValidationErrorDescriptorBuilder;
import com.jaspersoft.jasperserver.dto.resources.ClientProperty;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;

/**
 * <p>
 * <p>
 *
 * @author tetiana.iefimenko
 * @version $Id$
 * @see
 */
public class ValidDomElTypeValidator implements ConstraintValidator<ValidDomElType, String>, ValidationErrorDescriptorBuilder {
    String errorCode;
    String message;

    @Override
    public void initialize(ValidDomElType constraintAnnotation) {
        errorCode = constraintAnnotation.errorCode();
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value != null) {
            return JavaAliasConverter.map.containsKey(value);
        }
        return Boolean.TRUE; //skip validation for null value
    }

    @Override
    public ErrorDescriptor build(ConstraintViolation violation) {
        final ErrorDescriptor errorDescriptor = new ErrorDescriptor().setErrorCode(errorCode)
                .setMessage(message);
        errorDescriptor.addProperties(new ClientProperty("elementPath",violation.getPropertyPath().toString()),
                new ClientProperty("invalidValue", violation.getInvalidValue().toString()));
        return errorDescriptor;
    }
}
