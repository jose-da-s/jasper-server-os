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
package com.jaspersoft.jasperserver.api.common.timezone;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author Vasyl Spachynskyi
 * @version $Id: clientTimezoneFormattingRulesResolver.java 68639 2018-03-05 14:20:59Z vspachyn $
 * @since 14.03.2018
 */
@Component
public class ClientTimezoneFormattingRulesResolver {

    @Resource
    private Map<String, Boolean> applyClientTimezoneFormatting;

    /**
     * Return true if timezone applying is accepted for specific value.
     * 
     * @param value
     * @return true if timezone applying allowed
     */
    public boolean isApplyClientTimezone(Object value) {
        if (value == null || !(value instanceof Date)) return false;

        Boolean isApply = applyClientTimezoneFormatting.get(value.getClass().getCanonicalName());
        return isApply != null && isApply;
    }

    /**
     * Return true if timezone applying is accepted for specific value type.
     *
     * @param valueClass the value java type, like <code>java.sql.Time</code> or <code>java.util.Date</code>
     * @return true if timezone applying allowed
     */
    public boolean isApplyClientTimezone(Class valueClass) {
        if (valueClass == null) return false;

        if (!Date.class.isAssignableFrom(valueClass)) {
            return false;
        }

        Boolean isApply = applyClientTimezoneFormatting.get(valueClass.getCanonicalName());
        return isApply != null && isApply;
    }

    /**
     * Return true if timezone applying is accepted for specific data type.
     *
     * @param dataType the name of the type, like: time, timestamp, date and ect.
     * @return true if timezone applying allowed
     */
    public boolean isApplyClientTimezone(String dataType) {
        if (dataType == null) return false;

        if (isSameType(dataType, Timestamp.class)) {
            return isApplyClientTimezone(Timestamp.class);
        } else if (isSameType(dataType, Time.class)) {
            return isApplyClientTimezone(Time.class);
        } else if (isSameType(dataType, Date.class) || isSameType(dataType, java.sql.Date.class)) {
            return isApplyClientTimezone(Date.class);
        }
        return false;
    }

    private boolean isSameType(String type, Class classType) {
        return  type.equalsIgnoreCase(classType.getSimpleName()) || type.equals(classType.getName());
    }
}

