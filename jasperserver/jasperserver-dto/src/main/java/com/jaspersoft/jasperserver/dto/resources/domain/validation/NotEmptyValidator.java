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

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import java.util.Collection;
import java.util.Map;

/**
 * <p></p>
 *
 * @author Tetiana Iefimenko
 * @version $Id$$
 */
public class NotEmptyValidator implements ConstraintValidator<NotEmpty, Object>, ValidationErrorDescriptorBuilder {

    private String errorCode;
    private String message;

    @Override
    public void initialize(NotEmpty constraintAnnotation) {
       errorCode = constraintAnnotation.errorCode();
       message = constraintAnnotation.message();

    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return true;
        if (value instanceof String) return !((String) value).isEmpty();
        if (value instanceof Collection) return !(((Collection) value).isEmpty());
        if (value instanceof Map) return !(((Map) value).isEmpty());
        return false;
    }

    @Override
    public ErrorDescriptor build(ConstraintViolation violation) {
        final ErrorDescriptor errorDescriptor = new ErrorDescriptor().setErrorCode(errorCode)
                .setMessage(message);
            errorDescriptor.addProperties(new ClientProperty().setKey("propertyPath")
                    .setValue(violation.getPropertyPath().toString()));
        return errorDescriptor;
    }
}
