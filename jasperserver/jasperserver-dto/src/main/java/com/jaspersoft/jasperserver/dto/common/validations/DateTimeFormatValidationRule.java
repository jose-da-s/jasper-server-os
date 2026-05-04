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
package com.jaspersoft.jasperserver.dto.common.validations;

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@XmlRootElement
public class DateTimeFormatValidationRule extends ValidationRule<DateTimeFormatValidationRule> {
    public static final String INVALID_DATE = "fillParameters.error.invalidDate";
    public static final String INVALID_DATE_TIME = "fillParameters.error.invalidDateTime";
    public static final String INVALID_TIME = "fillParameters.error.invalidTime";

    private String format;

    public DateTimeFormatValidationRule() {
    }

    public DateTimeFormatValidationRule(DateTimeFormatValidationRule other) {
        super(other);
        this.format = other.getFormat();
    }

    @Override
    public DateTimeFormatValidationRule deepClone() {
        return new DateTimeFormatValidationRule(this);
    }

    public String getFormat() {
        return format;
    }

    public DateTimeFormatValidationRule setFormat(String format) {
        this.format = format;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DateTimeFormatValidationRule)) return false;
        if (!super.equals(o)) return false;

        DateTimeFormatValidationRule that = (DateTimeFormatValidationRule) o;

        return format != null ? format.equals(that.format) : that.format == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (format != null ? format.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DateTimeFormatValidationRule{" +
                "format='" + format + '\'' +
                "} " + super.toString();
    }
}
