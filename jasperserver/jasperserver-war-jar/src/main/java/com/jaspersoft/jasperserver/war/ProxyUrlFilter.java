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

package com.jaspersoft.jasperserver.war;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author dlitvak
 * @version $Id$
 */
public class ProxyUrlFilter implements Filter {
    private final static Logger logger = LogManager.getLogger(ProxyUrlFilter.class);

    private String forwardedForProtoHeaderName;
    private String forwardedForPortHeaderName;

    private URL proxyUrl;

    public void setForwardedForProtoHeaderName(String forwardedForProto) {
        this.forwardedForProtoHeaderName = forwardedForProto;
    }

    public void setForwardedForPortHeaderName(String forwardedForPortHeaderName) {
        this.forwardedForPortHeaderName = forwardedForPortHeaderName;
    }

    public void setProxyUrl(String proxyUrl) throws MalformedURLException {
        try {
            if (proxyUrl == null || proxyUrl.trim().isEmpty())
                return;

            this.proxyUrl = new URL(proxyUrl);
        }
        catch (MalformedURLException mue) {
            logger.warn("proxyUrl property cannot be parsed.  Proxy headers will be used instead.");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // NOOP
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ProxyRequestWrapper lbRequest = new ProxyRequestWrapper((HttpServletRequest) request);
        chain.doFilter(lbRequest, response);
    }

    @Override
    public void destroy() {
        // NOOP
    }

    public class ProxyRequestWrapper extends HttpServletRequestWrapper {
        private HttpServletRequest request;
        /**
         * Constructs a request object wrapping the given request.
         *
         * @param request
         * @throws IllegalArgumentException if the request is null
         */
        private ProxyRequestWrapper(HttpServletRequest request) {
            super(request);
            this.request = request;
        }

        @Override
        public String getScheme() {
            if (proxyUrl != null) {
                return proxyUrl.getProtocol();
            }
            else if (forwardedForProtoHeaderName != null) {
                String lbProto = request.getHeader(forwardedForProtoHeaderName);
                if (lbProto != null && !lbProto.isEmpty())
                    return lbProto;
            }

            return request.getScheme();
        }

        @Override
        public String getServerName() {
            if (proxyUrl != null) {
                return proxyUrl.getHost();
            }

            return request.getServerName();
        }

        @Override
        public int getServerPort() {
            if (proxyUrl != null) {
                return proxyUrl.getPort();
            }
            else if (forwardedForPortHeaderName != null) {
                String lbPortStr = request.getHeader(forwardedForPortHeaderName);
                if (lbPortStr != null && !lbPortStr.isEmpty())
                    return Integer.parseInt(lbPortStr);
            }

            return request.getServerPort();
        }
    }
}

