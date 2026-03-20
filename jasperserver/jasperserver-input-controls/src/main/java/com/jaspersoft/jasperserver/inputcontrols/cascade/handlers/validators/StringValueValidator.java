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

import com.jaspersoft.jasperserver.api.metadata.common.domain.DataType;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlValidationException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@Service
public class StringValueValidator extends ComparableValidator<String> {

    @Resource
    protected MessageSource messageSource;

    @Resource
    protected Boolean applyRegexpToEmptyString;

    @Override
    public void validateSingleValue(String value, DataType dataType) throws InputControlValidationException {
        if (dataType != null && value != null) {
            final Locale locale = LocaleContextHolder.getLocale();
            if (dataType.getMaxLength() != null && value.length() > dataType.getMaxLength()) {
                throw new InputControlValidationException("fillParameters.error.fieldTooLong", null, messageSource.getMessage("fillParameters.error.fieldTooLong", null, locale), null);
            } else if (dataType.getRegularExpr() != null && dataType.getRegularExpr().trim().length() > 0
                    && ((applyRegexpToEmptyString || value.length() > 0) && !Pattern.matches(dataType.getRegularExpr(), value))) {
                throw new InputControlValidationException("fillParameters.error.invalidPattern", null, messageSource.getMessage("fillParameters.error.invalidPattern", null, locale), null);
            }
            super.validateSingleValue(value, dataType);
        }
    }
}

