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

import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientExpression;
import com.jaspersoft.jasperserver.dto.common.ClientTypeUtility;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.common.ValidationErrorDescriptorBuilder;
import com.jaspersoft.jasperserver.dto.resources.ClientProperty;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author serhii.blazhyievskyi
 * @version $Id$
 */
public class InstanceOfValidator
        implements ConstraintValidator<InstanceOf, ClientExpression>, ValidationErrorDescriptorBuilder {
    protected Set<Class> acceptedClasses = new HashSet<Class>();

    @Override
    public void initialize(InstanceOf constraintAnnotation) {
        acceptedClasses = new HashSet<Class>(Arrays.asList(constraintAnnotation.value()));
    }

    @Override
    public boolean isValid(ClientExpression value, ConstraintValidatorContext context) {
        if (value == null) return true;
        if (acceptedClasses != null && !acceptedClasses.isEmpty()) {
            for (Class clazz : acceptedClasses) {
                if (clazz.isInstance(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ErrorDescriptor build(ConstraintViolation violation) {
        InstanceOf annotation = (InstanceOf) violation.getConstraintDescriptor().getAnnotation();
        final ErrorDescriptor errorDescriptor = new ErrorDescriptor()
                .setErrorCode(annotation.errorCode())
                .setMessage(annotation.message());
        errorDescriptor.addProperties(new ClientProperty("invalidType",
                ClientTypeUtility.extractClientType(violation.getInvalidValue().getClass()))
        );
        return errorDescriptor;
    }
}
