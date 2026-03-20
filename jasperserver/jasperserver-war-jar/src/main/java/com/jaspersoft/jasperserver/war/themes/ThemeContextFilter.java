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

package com.jaspersoft.jasperserver.war.themes;

import org.springframework.ui.context.ThemeSource;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ThemeResolver;

import jakarta.servlet.*;
import java.io.IOException;

/**
 * This class extends Spring WebMVC framework to support themes on error pages.
 * Spring implements themes in org.springframework.web.servlet.DispatcherServlet, which works fine for normal flow.
 * However, if exception happens in one of the servlet filter (for example,
 * Spring Security exception that denied access to a certain URL), spring:theme tag
 * is not available because theme objects are not set in a request.
 *
 * The code below replicates these settings as they are in DispatcherServlet (v.2.5.6.SEC01) with minor simplification.
 * and sets all objects earlier in the execution. Note that DispatcherServlet will set them again.
 *
 * Please, place this filter to be the first in the chain
 *
 * @author asokolnikov
 */
public class ThemeContextFilter implements Filter {

    private FilterConfig filterConfig;
    private ThemeResolver themeResolver;
    private ThemeSource themeSource;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        request.setAttribute(DispatcherServlet.THEME_RESOLVER_ATTRIBUTE, this.themeResolver);
        request.setAttribute(DispatcherServlet.THEME_SOURCE_ATTRIBUTE, this.themeSource);

        chain.doFilter(request, response);
    }

    public void destroy() {
    }

    public ThemeResolver getThemeResolver() {
        return themeResolver;
    }

    public void setThemeResolver(ThemeResolver themeResolver) {
        this.themeResolver = themeResolver;
    }

    public ThemeSource getThemeSource() {
        return themeSource;
    }

    public void setThemeSource(ThemeSource themeSource) {
        this.themeSource = themeSource;
    }
}

