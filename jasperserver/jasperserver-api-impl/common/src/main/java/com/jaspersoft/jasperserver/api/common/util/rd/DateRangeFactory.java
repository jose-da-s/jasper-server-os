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
package com.jaspersoft.jasperserver.api.common.util.rd;

import com.jaspersoft.jasperserver.api.common.timezone.ClientTimezoneFormattingRulesResolver;
import com.jaspersoft.jasperserver.api.common.util.TimeZoneContextHolder;
import net.sf.jasperreports.types.date.DateRange;
import net.sf.jasperreports.types.date.DateRangeBuilder;
import net.sf.jasperreports.types.date.DateRangeExpression;
import net.sf.jasperreports.types.date.InvalidDateRangeExpressionException;
import org.apache.commons.lang3.ObjectUtils;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import java.util.regex.Pattern;

import static com.jaspersoft.jasperserver.api.common.util.spring.StaticApplicationContext.getApplicationContext;
import static com.jaspersoft.jasperserver.dto.adhoc.CommonAdhocDateFormats.DOMEL_DATE_TIME_PATTERN_WITHOUT_TIMEZONE;
import static com.jaspersoft.jasperserver.dto.adhoc.CommonAdhocDateFormats.DOMEL_TIMESTAMP_WITHOUT_MILLISECONDS_WITHOUT_TIMEZONE_PATTERN;
import static com.jaspersoft.jasperserver.dto.adhoc.CommonAdhocDateFormats.TIME_WITHOUT_MILLISECONDS_PATTERN;
import static com.jaspersoft.jasperserver.dto.adhoc.CommonAdhocDateFormats.TIME_WITHOUT_TIMEZONE_PATTERN;

/**
 * <p>Factory which returns proper instance of {@link DateRange}</p>
 *
 * @author Sergey Prilukin
 * @version $Id: DateRangeFactory.java 25242 2012-10-15 14:11:08Z sergey.prilukin $
 */
public class DateRangeFactory {

    public static Pattern timePattern = Pattern.compile(".*\\d+:\\d+:\\d+\\.\\d+.*");

    public static DateRange getInstance(final String expression) throws InvalidDateRangeExpressionException {
        return getInstance(expression, null);
    }

    public static DateRange getInstance(final Date date) {
        return new DateRangeBuilder(date).toDateRange();
    }

    public static DateRange getInstance(final Date date, final Class<? extends Date> valueClass) {
        return new DateRangeBuilder(date).set(valueClass).toDateRange();
    }

    public static DateRange getInstance(final Date date, TimeZone timeZone, final Class<? extends Date> valueClass) {
        return new DateRangeBuilder(date).set(valueClass).set(timeZone).toDateRange();
    }

    public static DateRange getInstance(final String expression, final Class<? extends Date> valueClass)
            throws InvalidDateRangeExpressionException {
        TimeZone tz = null;
        if (ObjectUtils.equals(valueClass, Timestamp.class)) {
            tz = getTimeZoneByValueType(Timestamp.class);
        }
        return getInstance(expression, tz, valueClass);
    }

    public static DateRange getInstance(final String expression, final Class<? extends Date> valueClass, String datePattern)
            throws InvalidDateRangeExpressionException {
        DateRangeBuilder builder = new DateRangeBuilder(expression).set(valueClass).set(datePattern);
        if (ObjectUtils.equals(valueClass, Timestamp.class)) {
            builder.set(getTimeZoneByValueType(Timestamp.class));
        }
        return builder.toDateRange();
    }

    public static DateRange getInstance(final String expression, TimeZone timeZone, final Class<? extends Date> valueClass)
            throws InvalidDateRangeExpressionException {

        String pattern = getDataPattern(expression, valueClass);
        return new DateRangeBuilder(expression).set(valueClass).set(timeZone).set(pattern).toDateRange();
    }

    private static String getDataPattern(String expression, Class<? extends Date> valueClass) {
        if (expression == null || valueClass == null) return null;
        boolean withMillisecond = timePattern.matcher(expression).matches();

        if (valueClass.equals(Timestamp.class)) {
            return withMillisecond ? DOMEL_DATE_TIME_PATTERN_WITHOUT_TIMEZONE : DOMEL_TIMESTAMP_WITHOUT_MILLISECONDS_WITHOUT_TIMEZONE_PATTERN;
        } else if (valueClass.equals(Time.class)) {
            return withMillisecond ? TIME_WITHOUT_TIMEZONE_PATTERN : TIME_WITHOUT_MILLISECONDS_PATTERN;
        }
        return null;
    }

    public static DateRange getInstanceForExpression(final String expression, final Class<? extends Date> valueClass)
            throws InvalidDateRangeExpressionException {
        if (expression == null || "null".equals(expression)) {
            return null;
        }

        DateRange testDateRange = getInstance(expression, valueClass);
        TimeZone tz = TimeZone.getDefault();

        if (testDateRange instanceof DateRangeExpression && ObjectUtils.equals(valueClass, Timestamp.class)) {
            tz = getTimeZoneByValueType(Timestamp.class);
        }

        return getInstance(expression, tz, valueClass);
    }

    private static TimeZone getTimeZoneByValueType(Class valueClass) {
    	if (Optional.ofNullable(getApplicationContext())
    			.map(ctx -> ctx.getBean(ClientTimezoneFormattingRulesResolver.class))
    			.map(bean -> bean.isApplyClientTimezone(valueClass) ? true : null)
    			.isPresent()) {
            return TimeZoneContextHolder.getTimeZone();
        }
        return null;
    }
}
