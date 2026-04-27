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

import com.jaspersoft.jasperserver.api.common.timezone.ClientTimezoneFormattingRulesResolver;
import com.jaspersoft.jasperserver.api.common.util.TimeZoneContextHolder;
import net.sf.jasperreports.types.date.DateRangeExpression;
import com.jaspersoft.jasperserver.api.common.util.rd.DateRangeFactory;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.CalendarFormatProvider;
import net.sf.jasperreports.types.date.TimestampRange;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.sql.Timestamp;
import java.text.DateFormat;

/**
 * @author Sergey Prilukin
 * @version $Id$
 */
@Service
public class TimestampRangeDataConverter extends BaseChronoDataConverter implements DataConverter<TimestampRange> {

    @Resource
    private ClientTimezoneFormattingRulesResolver clientTimezoneFormattingRulesResolver;

    @Override
    public TimestampRange stringToValue(String rawData) throws Exception {
        if (StringUtils.isEmpty(rawData)) {
            return null;
        }

        DateFormat datetimeFormat = getDatetimeFormat(rawData);
        return (TimestampRange) DateRangeFactory.getInstance(rawData, Timestamp.class,
                DateRangeDataConverter.getStringDatePattern(datetimeFormat));
    }

    @Override
    public String valueToString(TimestampRange value) {
        if (value == null) {
            return "";
        } else if (value instanceof DateRangeExpression) {
            return ((DateRangeExpression) value).getExpression();
        } else {
            DateFormat datetimeFormat = getDatetimeFormat();
            if (clientTimezoneFormattingRulesResolver.isApplyClientTimezone(value.getStart())) {
                datetimeFormat.setTimeZone(TimeZoneContextHolder.getTimeZone());

                return datetimeFormat.format(value.getStart());
            }
            return datetimeFormat.format(value.getStart());
        }
    }
}

