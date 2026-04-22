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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.multipart.MultipartResolver;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * It seems that Tomcat 10's implementation of HttpServletRequest will consume the bodies of multipart requests
 * to parse parameters
 * so Jersey's MultipartFeature cannot read the request body again. Using {@link MultipartResolverFilter}
 * seems like a good idea, but it also consumes the body. It adds special parameter- and part-oriented
 * methods. but Jersey uses the raw request input stream and does not accessing those so that doesn't help
 * either. In Tomcat 9, it wasn't a problem because getParameter would return null if
 * there was no explicit multipart config. In 10.1, there is a default empty-config that does not prevent
 * the body from being parsed and consumed. So here is a workaround to copy old behavior.
 */
public class MultipartNoParametersRequestWrapperFilter implements Filter {

    protected final Log log = LogFactory.getLog(this.getClass());

    private MultipartResolver multipartResolver;

    public void setMultipartResolver(MultipartResolver multipartResolver) {
        this.multipartResolver = multipartResolver;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (multipartResolver.isMultipart((HttpServletRequest) request) && request.getContentLength() > 0) {
			request = new MultipartNoParametersRequestWrapper((HttpServletRequest) request);
		}
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
