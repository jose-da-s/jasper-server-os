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
package com.jaspersoft.jasperserver.dto.common;

import javax.xml.XMLConstants;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;

/**
 * @author Paul Lysak
 */
@XmlType(name = "time", namespace = XMLConstants.W3C_XML_SCHEMA_NS_URI)
public class TimeString implements DeepCloneable<TimeString> {
    private String time;

    public TimeString() {
    }

    public TimeString(String time) {
        this.time = time;
    }

    public TimeString(TimeString timeString) {
        checkNotNull(timeString);

        this.time = timeString.getTime();
    }

    @Override
    public TimeString deepClone() {
        return new TimeString(this);
    }

    @XmlValue
    public String getTime() {
        return time;
    }

    public TimeString setTime(String time) {
        this.time = time;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeString that = (TimeString) o;

        return time != null ? time.equals(that.time) : that.time == null;
    }

    @Override
    public int hashCode() {
        return time != null ? time.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "TimeString{" +
                "time='" + time + '\'' +
                '}';
    }
}
