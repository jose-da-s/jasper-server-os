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
import java.sql.Time;
import java.text.SimpleDateFormat;

import static com.jaspersoft.jasperserver.dto.adhoc.query.el.adapters.DomELCommonSimpleDateFormats.TIME_FORMAT_STRING;
import static com.jaspersoft.jasperserver.dto.adhoc.query.el.adapters.DomELCommonSimpleDateFormats.TIME_FORMAT_STRING_WITHOUT_MILLISECONDS;


/**
 * @author Grant Bacon <gbacon@tibco.com>
 * @author Stas Chubar <schubar@tibco.com>
 * @version $Id $
 */
public class ELTimeAdapter extends XmlAdapter<String, Time> {

    private ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return DomELCommonSimpleDateFormats.timeFormat();
        }
    };

    @Override
    public String marshal(Time v) throws Exception {
        return formatter.get().format(v);
    }

    @Override
    public Time unmarshal(String v) throws Exception {
        return new Time(DateUtils.parseDate(v, TIME_FORMAT_STRING_WITHOUT_MILLISECONDS, TIME_FORMAT_STRING).getTime());
    }
}