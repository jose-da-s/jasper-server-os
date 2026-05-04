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
package com.jaspersoft.jasperserver.war.util;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.web.firewall.RequestRejectedException;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * <p>Puts sessionDecorator parameter into the session if it was found in the request parameters list.</p>
 *
 * @author Yuriy Plakosh
 * @version $Id$
 * @since 5.0.1
 */
public class SessionDecoratorFilter implements Filter {
    private static String SESSION_DECORATOR = "sessionDecorator";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing.
    }

    @Override
    public void destroy() {
        // Nothing.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        String sessionDecorator = request.getParameter(SESSION_DECORATOR);

        if (!StringUtils.isEmpty(sessionDecorator)) {
            if (request instanceof HttpServletRequest) {
                HttpSession session = ((HttpServletRequest) request).getSession();

                session.setAttribute(SESSION_DECORATOR, sessionDecorator);
            }
        }
        try {
            chain.doFilter(request, response);
        } catch (RequestRejectedException exception) {
            ((HttpServletResponse)response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter pw = ((HttpServletResponse)response).getWriter();
            pw.print(exception.getMessage());
        }
    }
}

