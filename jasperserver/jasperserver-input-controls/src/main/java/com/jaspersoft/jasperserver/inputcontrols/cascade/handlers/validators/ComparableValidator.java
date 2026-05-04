/*
 * Copyright (C) 2025-2026 the Jasper Server OS Authors
 * SPDX-License-Identifier: AGPL-3.0-or-later
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
package com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.validators;

import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.metadata.common.domain.DataType;
import com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.converters.DataConverterService;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlValidationException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Locale;

import static com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.NumberUtils.toBigDecimal;

/**
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@Service
public class ComparableValidator<T extends Comparable<?>> implements InputControlValueValidator<T> {

    @Resource
    private DataConverterService dataConverterService;
    @Resource
    protected MessageSource messageSource;

    @Override
    public void validateSingleValue(T value, DataType dataType) throws InputControlValidationException {
        if (value != null && dataType != null) {
            final Locale locale = LocaleContextHolder.getLocale();
            if (dataType.getMinValue() != null) {
                @SuppressWarnings("unchecked") // concrete type of Number implements Comparable<ConcreteNumberType>. Therefore cast is safe for all standard Number types
                        Comparable min = convertRestrictionValueToParameterType(value.getClass(), dataType.getMinValue(), dataType);
                int result = compare(value, min);
                if (dataType.isStrictMin()) {
                    if (result >= 0) {
                        throw new InputControlValidationException("fillParameters.error.smallerOrEqual", null, messageSource.getMessage("fillParameters.error.smallerOrEqual", null, locale), null);
                    }
                } else if (result > 0) {
                    throw new InputControlValidationException("fillParameters.error.smallerThan", null, messageSource.getMessage("fillParameters.error.smallerThan", null, locale), null);
                }
            }

            if (dataType.getMaxValue() != null) {
                @SuppressWarnings("unchecked") // concrete type of Number implements Comparable<ConcreteNumberType>. Therefore cast is safe for all standard Number types
                        Comparable max = convertRestrictionValueToParameterType(value.getClass(), dataType.getMaxValue(), dataType);
                int result = compare(value, max);
                if (dataType.isStrictMax()) {
                    if (result <= 0) {
                        throw new InputControlValidationException("fillParameters.error.greaterOrEqual", null, messageSource.getMessage("fillParameters.error.greaterOrEqual", null, locale), null);
                    }
                } else if (result < 0) {
                    throw new InputControlValidationException("fillParameters.error.greaterThan", null, messageSource.getMessage("fillParameters.error.greaterThan", null, locale), null);
                }
            }
        }
    }

    /**
     * Restriction could be of different type than value.
     * But we still can try to compare these two values.
     * @param value value to compare
     * @param restriction restriction
     * @return same result as restriction.compareTo(value)
     */
    protected int compare(T value, Comparable restriction) {
        if (restriction.getClass().isAssignableFrom(value.getClass())) {
            return restriction.compareTo(value);
        } else if (Number.class.isAssignableFrom(restriction.getClass())
                && Number.class.isAssignableFrom(value.getClass())) {
            return toBigDecimal((Number)restriction).compareTo(toBigDecimal((Number)value));
        } else
            throw new JSException("Restriction type " + restriction.getClass().getName() + " incompatible with value type " + value.getClass().getName());
    }

    @SuppressWarnings("unchecked")// cast safety guarantied by dataConverterService
    protected Comparable convertRestrictionValueToParameterType(Class<?> type, Object restrictionValue, DataType dataType) {
        Comparable result;
        final Class<?> restrictionValueClass = restrictionValue.getClass();
        if (restrictionValueClass.isAssignableFrom(type)) {
            result = (Comparable) restrictionValue;
        } else if (restrictionValue instanceof String) {
            try {
                result = (Comparable) dataConverterService.convertSingleValue((String) restrictionValue, dataType, type);
            } catch (Exception e) {
                throw new IllegalStateException("Value '" + restrictionValue + "' couldn't be used as restriction for input control of type " + type.getName());
            }
        } else if (Number.class.isAssignableFrom(type) && Number.class.isAssignableFrom(restrictionValueClass)) {
            result = (Comparable) restrictionValue;
        } else
            throw new JSException("Restriction type " + restrictionValueClass.getName() + " incompatible with value type " + type.getName());
        return result;
    }


    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
}

