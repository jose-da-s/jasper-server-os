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

import com.jaspersoft.jasperserver.api.common.util.rd.DateRangeFactory;
import net.sf.jasperreports.types.date.DateRange;
import net.sf.jasperreports.types.date.DateRangeExpression;
import net.sf.jasperreports.types.date.InvalidDateRangeExpressionException;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @author Anton Fomin
 */
@Service
public class DateRangeDataConverter extends BaseChronoDataConverter implements DataConverter<DateRange> {

    @Override
    public DateRange stringToValue(String rawData) throws Exception {
        if (StringUtils.isEmpty(rawData)) {
            return null;
        }

        try {
        	return DateRangeFactory.getInstance(rawData, Timestamp.class,
        			getStringDatePattern(getDatetimeFormat(rawData)));
        } catch (InvalidDateRangeExpressionException e1) {
        	// maybe it is a date without timestamp, try to add a midnight timestamp
        	try {
            	return DateRangeFactory.getInstance(rawData + " 00:00:00", Timestamp.class,
            			getStringDatePattern(getDatetimeFormat(rawData)));
        	} catch (InvalidDateRangeExpressionException e2) {
        		throw e1;
        	}
        }
    }

    @Override
    public String valueToString(DateRange value) {
        if (value == null) {
            return "";
        } else if (value instanceof DateRangeExpression) {
            return ((DateRangeExpression) value).getExpression();
        } else {
            return getDatetimeFormat().format(value.getStart());
        }
    }

    public static String getStringDatePattern(DateFormat dateFormat) {
        return ((SimpleDateFormat) dateFormat).toPattern();
    }

}
