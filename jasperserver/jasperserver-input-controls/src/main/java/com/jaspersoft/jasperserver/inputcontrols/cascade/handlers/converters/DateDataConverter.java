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
package com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.converters;

import com.jaspersoft.jasperserver.api.engine.jasperreports.util.CalendarFormatProvider;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.text.ParseException;
import java.util.Date;

/**
 * @author Yaroslav.Kovalchyk
 */
@Service
public class DateDataConverter extends BaseChronoDataConverter implements DataConverter<Date>{

    @Override
    public Date stringToValue(String rawData) throws ParseException {
        try {
            return StringUtils.isNotEmpty(rawData) ? getDateFormat(rawData).parse(rawData) : null;
        } catch (ParseException initial){
            try {
                return new Date(Long.parseLong(rawData));
            } catch (Exception e){
                throw initial;
            }
        }
    }

    @Override
    public String valueToString(Date value) {
        return value != null ? getDateFormat().format(value) : "";
    }
}

