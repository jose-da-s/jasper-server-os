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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author
 */
public class IPadSupportFilter implements Filter, InitializingBean {

    public static final String IS_IPAD = "isIPad";

    private String redirectUrl;
    private List<String> notSupportedUrls;

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public void setNotSupportedUrls(List<String> notSupportedUrls) {
        this.notSupportedUrls = notSupportedUrls;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.hasLength(redirectUrl, "redirectUrl must be specified");
        Assert.notEmpty(notSupportedUrls, "notSupportedUrls must be specified");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpResponse = (HttpServletResponse)response;

        if(isIPad(httpRequest)) {
            httpRequest.setAttribute(IS_IPAD, true);
        }

        // check for iPad support
        if (isSupported(httpRequest)) {
            chain.doFilter(request, response);
        } else {
            httpResponse.sendRedirect(createRedirectUrl(httpRequest, httpResponse));
        }
    }

    private boolean isSupported(HttpServletRequest request) {
        return !isIPad(request) || isRequestUrlSupported(request);
    }

    private boolean isIPad(HttpServletRequest request) {
        String userAgent = request.getHeader("user-agent");
        return userAgent != null && userAgent.indexOf("iPad") > -1;
    }

    private boolean isRequestUrlSupported(HttpServletRequest request) {
        String requestUrl = request.getRequestURL().append("?").append(request.getQueryString()).toString();

        for(String url : notSupportedUrls) {
            if (requestUrl.indexOf(url) > -1) {
                return false;
            }
        }

        return true;
    }

    private String createRedirectUrl(HttpServletRequest request, HttpServletResponse response) {
        return response.encodeRedirectURL(request.getContextPath() + redirectUrl);
    }

    public void init(FilterConfig filterConfig) throws ServletException {

    }

    public void destroy() {

    }
}

