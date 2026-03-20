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

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.ui.context.ThemeSource;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ThemeResolver;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * This class extends AccessDeniedHandlerImpl to make it aware of some JRS specific attributes.
 * Normal servlet dispatching is done by DispatchServlet which takes care of ThemeResolver, LocaleResolver, etc.
 * However in case of flow errors (like, access to a view is denied), it happens before DispatchServlet.
 * To make error page be able to use themes and proper locale, this class injects the required attributes info into request
 *
 * User: asokolnikov
 */
public class JRSAccessDeniedHandlerImpl extends AccessDeniedHandlerImpl {

    private ThemeResolver themeResolver;
    private ThemeSource themeSource;
    private LocaleResolver localeResolver;
    private Map jsOptimizationProperties;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        request.setAttribute(DispatcherServlet.THEME_RESOLVER_ATTRIBUTE, themeResolver);
        request.setAttribute(DispatcherServlet.THEME_SOURCE_ATTRIBUTE, themeSource);
        request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, localeResolver);
        request.setAttribute("jsOptimizationProperties", jsOptimizationProperties);
        super.handle(request, response, accessDeniedException);
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

    public LocaleResolver getLocaleResolver() {
        return localeResolver;
    }

    public void setLocaleResolver(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    public Map getJsOptimizationProperties() {
        return jsOptimizationProperties;
    }

    public void setJsOptimizationProperties(Map jsOptimizationProperties) {
        this.jsOptimizationProperties = jsOptimizationProperties;
    }
}

