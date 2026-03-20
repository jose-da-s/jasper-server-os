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

package com.jaspersoft.jasperserver.api.security.csrf;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.DefaultCorsProcessor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSCorsProcessor extends DefaultCorsProcessor {

    private Map<String, List<String>> headerUrlPatterns;
    private Map<String, List<String>> headerValues;


    @Override
    public boolean processRequest(CorsConfiguration config, HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean processRequest = super.processRequest(config, request, response);
        addJrsHeaders(response, request);
        return processRequest;
    }

    public void addJrsHeaders(HttpServletResponse response, HttpServletRequest request) {
        for (String header : headerUrlPatterns.keySet()) {
            String resourceUrl = UrlUtils.buildRequestUrl(request);

            for (String urlPattern : headerUrlPatterns.get(header)) {
                Matcher matcher = Pattern.compile(urlPattern).matcher(resourceUrl);

                if (matcher.matches()) {
                    String valuesToAdd = String.join(", ", headerValues.get(header));
                    String oldValues = response.getHeader(header);
                    String newValues = StringUtils.isNotBlank(oldValues)
                            ? String.format("%s, %s", oldValues, valuesToAdd)
                            : valuesToAdd;
                    response.setHeader(header, newValues);
                }
            }
        }
    }


    public void setHeaderUrlPatterns(Map<String, List<String>> urlPatterns) {
        this.headerUrlPatterns = urlPatterns;
    }

    public void setHeaderValues(Map<String, List<String>> headerValues) { this.headerValues = headerValues; }
}

