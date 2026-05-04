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

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import java.util.List;

/**
 * <p>
 * <p>
 *
 * @author tetiana.iefimenko
 * @version $Id$
 * @see
 */
public class NoNullElementsValidator implements ConstraintValidator<NoNullElements, List>, ValidationErrorDescriptorBuilder {
    private String errorCode;
    private String message;

    @Override
    public void initialize(NoNullElements constraintAnnotation) {
        errorCode = constraintAnnotation.errorCode();
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(List value, ConstraintValidatorContext context) {
        if (value != null) {
            for (Object element : value) {
                if(element == null) return false;
            }
        }
        return true;
    }

    @Override
    public ErrorDescriptor build(ConstraintViolation violation) {
        return new ErrorDescriptor().setErrorCode(errorCode).setMessage(message);
    }
}
