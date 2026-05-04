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
import com.jaspersoft.jasperserver.dto.common.ValidationErrorDescriptorBuilder;
import com.jaspersoft.jasperserver.dto.resources.ClientProperty;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Service;

/**
 * <p>
 * <p>
 *
 * @author tetiana.iefimenko
 * @version $Id$
 * @see
 */
@Service
public class SizeConstraintValidationErrorDescriptorBuilder implements ValidationErrorDescriptorBuilder<Size> {
    @Override
    public ErrorDescriptor build(ConstraintViolation<Size> violation) {
        final Size annotation = (Size) violation.getConstraintDescriptor().getAnnotation();
        final String propertyPath = violation.getPropertyPath().toString();
        final String invalidValue = violation.getInvalidValue().toString();

        final String proprtyName = propertyPath.substring(propertyPath.lastIndexOf(".") + 1);
        final ErrorDescriptor errorDescriptor = new ErrorDescriptor().
                setErrorCode(annotation.message()).
                setMessage("Length of field \"" + proprtyName + "\" breaks length limit.");
        errorDescriptor.addProperties(new ClientProperty("invalidValue", invalidValue),
                new ClientProperty("actualLength", String.valueOf(invalidValue.length())),
                new ClientProperty("maxLimit", String.valueOf(annotation.max())),
                new ClientProperty("minLimit", String.valueOf(annotation.min())),
                new ClientProperty("propertyPath", propertyPath));

        return errorDescriptor;
    }
}
