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
package com.jaspersoft.jasperserver.dto.adhoc.query.adapter;

import com.jaspersoft.jasperserver.dto.adhoc.query.el.adapters.DomELCommonSimpleDateFormats;
import org.apache.commons.lang3.time.DateUtils;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static com.jaspersoft.jasperserver.dto.adhoc.CommonAdhocDateFormats.DOMEL_DATE_TIME_PATTERN;
import static com.jaspersoft.jasperserver.dto.adhoc.CommonAdhocDateFormats.DOMEL_TIMESTAMP_WITHOUT_MILLISECONDS_WITHOUT_TIMEZONE_PATTERN;
import java.util.Arrays;
import java.util.Date;

/**
 * @author Grant Bacon <gbacon@tibco.com>
 * @author Stas Chubar <schubar@tibco.com>
 * @version $Id $
 */
public class ELTimestampAdapter extends XmlAdapter<String, Timestamp> {

    private ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return DomELCommonSimpleDateFormats.timestampFormat();
        }
    };

    private ThreadLocal<SimpleDateFormat> isoFormatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return DomELCommonSimpleDateFormats.isoTimestampFormat();
        }
    };

    @Override
    public String marshal(Timestamp v) throws Exception {
        return isoFormatter.get().format(v);
    }

    @Override
    public Timestamp unmarshal(String v) throws Exception {

        Date date;
        try {
            date = DateUtils.parseDate(v,
                    DomELCommonSimpleDateFormats.ISO_TIMESTAMP_FORMAT_STRING,
                    DomELCommonSimpleDateFormats.ISO_TIMESTAMP_FORMAT_STRING_NO_MILLISECONDS,
                    DomELCommonSimpleDateFormats.TIMESTAMP_FORMAT_STRING,
                    DomELCommonSimpleDateFormats.TIMESTAMP_FORMAT_STRING_NO_MILLISECONDS);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Unable to parse given value for timestamp: " + v, e);
        }
        return new Timestamp(date.getTime());
    }

    public String isoMarshal(Timestamp v) throws Exception {
        return isoFormatter.get().format(v);
    }

}