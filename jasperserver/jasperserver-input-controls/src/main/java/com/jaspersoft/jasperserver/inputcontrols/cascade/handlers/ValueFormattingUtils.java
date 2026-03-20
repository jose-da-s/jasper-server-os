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
package com.jaspersoft.jasperserver.inputcontrols.cascade.handlers;

import com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.converters.DataConverterService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class ValueFormattingUtils {

    private final static Log log = LogFactory.getLog(ValueFormattingUtils.class);

    @jakarta.annotation.Resource
    protected DataConverterService dataConverterService;

    public String formatSingleValue(Object value) {
        try {
            // Use registered type converter
            return dataConverterService.formatSingleValue(value);
        } catch (IllegalStateException ex) {
            log.error(value.getClass(), ex);
            // No converter found, call toString()
            return value.toString();
        }
    }

    public Map<String, String[]> formatTypedParameters(Map<String, Object> typedParameters) {
        Map<String, String[]> formattedValues = new HashMap<>(typedParameters.size());
        for (Map.Entry<String, Object> entity : typedParameters.entrySet()) {
            Object value = entity.getValue();
            if (value.getClass().isArray()) {
                if (value instanceof String[]) {
                    // add as is
                    formattedValues.put(entity.getKey(), (String[]) entity.getValue());
                } else {
                    // Convert array elements to strings
                    formattedValues.put(entity.getKey(), Arrays.stream((Object[]) value).map(x -> formatSingleValue(x)).toArray(String[]::new));
                }
            } else if (value instanceof Collection) {
                // Convert a collection of objects into an array of strings
                formattedValues.put(entity.getKey(), (String[]) ((Collection) value).stream().map(x -> formatSingleValue(x)).toArray(String[]::new));
            } else {
                // Convert an object into an array with a single string
                formattedValues.put(entity.getKey(), new String[] { formatSingleValue(entity.getValue()) });
            }
        }
        return formattedValues;
    }
}

