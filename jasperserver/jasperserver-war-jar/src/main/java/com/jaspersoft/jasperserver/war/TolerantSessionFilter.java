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

import com.jaspersoft.jasperserver.api.SessionAttribMissingException;
import com.jaspersoft.jasperserver.core.util.TolerantRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;


/**
 *
 * Pushes the Tolerant request down the filter chain *
 *
 * Created by nthapa on 7/3/13.
 */
public class TolerantSessionFilter implements Filter {
    protected final static Log log = LogFactory.getLog(TolerantSessionFilter.class);
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        /* Left blank */
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        TolerantRequest req=new TolerantRequest((HttpServletRequest)request);
        HttpSession session=((HttpServletRequest) req).getSession();

        chain.doFilter(req, response);

    }

    @Override
    public void destroy() {
        /* Left blank */
    }
}

