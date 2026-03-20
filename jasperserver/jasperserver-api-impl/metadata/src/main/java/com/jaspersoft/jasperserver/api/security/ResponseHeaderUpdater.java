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

package com.jaspersoft.jasperserver.api.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ResponseHeaderUpdater {

    private Map<String, List> cookieHeaders;

    public void setCookieHeaders(Map cookieHeaders) {
        this.cookieHeaders = cookieHeaders;
    }

    public void changeHeaders(HttpServletResponse response, HttpServletRequest request) {
        if((request.getHeader("X-Forwarded-Proto")==null && request.isSecure()) || "https".equals(request.getHeader("X-Forwarded-Proto")))
            modifyCookieHeaders(response);
    }

    private boolean updateCookieHeaders(HttpServletRequest request) {
        return true;
    }

    private void modifyCookieHeaders(HttpServletResponse response) {
        Collection<String> responseHeaderValues;
        for (String cookieHeaderName : cookieHeaders.keySet()) {
            if(cookieHeaders.get(cookieHeaderName)!=null && cookieHeaders.get(cookieHeaderName).size()>0 ) {
                String cookieHeaderValueSuffix = stringJoin(";", cookieHeaders.get(cookieHeaderName));

                boolean firstHeader = true;
                responseHeaderValues = response.getHeaders(cookieHeaderName);
                for (String headerValue : responseHeaderValues) {
                    String updatedHeaderValue = headerValue;
                    if (headerValue != null && !headerValue.contains(cookieHeaderValueSuffix))
                        updatedHeaderValue = String.join(";", headerValue, cookieHeaderValueSuffix);
                    if (firstHeader) {
                        response.setHeader(cookieHeaderName, updatedHeaderValue);
                        firstHeader = false;
                        continue;
                    }
                    response.addHeader(cookieHeaderName, updatedHeaderValue);
                }
            }
        }

    }

    private String stringJoin(String delimiter, String... strings) {
        StringBuilder sb = new StringBuilder();
        int k = 0;
        for (String str : strings) {
            sb.append(str);
            if (++k < strings.length) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    private String stringJoin(String delimiter, List<String> strings) {
        StringBuilder sb = new StringBuilder();
        int k = 0;
        for (String str : strings) {
            sb.append(str);
            if (++k < strings.size()) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

}

