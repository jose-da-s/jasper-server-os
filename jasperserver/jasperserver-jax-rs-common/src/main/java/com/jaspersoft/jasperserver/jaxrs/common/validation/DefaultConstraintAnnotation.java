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

import jakarta.validation.ConstraintViolation;

import static com.jaspersoft.jasperserver.dto.common.CommonErrorCode.Codes.MANDATORY_PARAMETER_ERROR;
import static java.lang.String.format;

/**
 * @author Volodya Sabadosh
 */
public enum DefaultConstraintAnnotation {
    NOT_NULL(jakarta.validation.constraints.NotNull.class, MANDATORY_PARAMETER_ERROR),
    NOT_EMPTY(com.jaspersoft.jasperserver.dto.adhoc.query.validation.NotEmpty.class, MANDATORY_PARAMETER_ERROR);

    Class clazz;
    String overrideErrorCode;
    String defaultErrorCode;

    DefaultConstraintAnnotation(Class clazz, String overrideErrorCode) {
        this.clazz = clazz;
        this.overrideErrorCode = overrideErrorCode;
        this.defaultErrorCode = getDefaultAnnotationMessage(clazz, METHOD_MESSAGE);
    }

    public static final String METHOD_MESSAGE = "message";


    public static String getErrorCode(final ConstraintViolation violation) {
        for (DefaultConstraintAnnotation annotationEnum : values()) {
            if (violation.getConstraintDescriptor().getAnnotation().annotationType() == annotationEnum.clazz &&
                    violation.getMessageTemplate().equals(annotationEnum.defaultErrorCode)) {
                return annotationEnum.overrideErrorCode;
            }
        }

        return violation.getMessageTemplate();
    }

    private static <T> T getDefaultAnnotationMessage(Class<?> type, String method) {
        try {
            return (T) type.getMethod(method).getDefaultValue();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    format("The method \"%s\" for class \"%s\" is not exists", method, type), e);
        }
    }

}
