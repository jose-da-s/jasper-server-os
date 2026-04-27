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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.theme.SessionThemeResolver;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This is an implementation of ThemeResolver for JasperServer.
 * It extends <code>SessionThemeResolver</code> and keeps the theme in the HTTP session.
 * It checks the request for a special parameter to possibly overwrite the current theme.
 * If no current theme, it calls the delegate to find out the theme name.
 * If still not resolved, it uses <code>defaultThemeName</code>.
 * @author asokolnikov
 */
public class JSThemeResolver extends SessionThemeResolver {

    public static final String THEME_PARAMETER = "theme";

    private ThemeResolver delegate;

    public String resolveThemeName(HttpServletRequest request) {

        // Check the parameter
        String requestTheme = request.getParameter(THEME_PARAMETER);
        if (requestTheme != null && requestTheme.length() > 0) {
            if (!isLoginPage(request)) {
                setThemeName(request, null, requestTheme);
            }
            return requestTheme;
        }

        // Check if theme is set already
        //String currentTheme = super.resolveThemeName(request);
        String currentTheme = null;
        currentTheme = (String) WebUtils.getSessionAttribute(request, THEME_SESSION_ATTRIBUTE_NAME);
        if (currentTheme != null && currentTheme.length() > 0) {
            return currentTheme;
        }

        // Call theme delegate for a theme name
        if (delegate != null) {
            currentTheme = delegate.resolveThemeName(request);
        }

        // Finally, use default
        if (currentTheme == null || currentTheme.length() == 0) {
            currentTheme = getDefaultThemeName();
        }

        // do not store the theme in the session for login page or not authenticated user
        if (!isLoginPage(request) && isAuthenticatedUser()) {
            setThemeName(request, null, currentTheme);
        }
        return currentTheme;
    }

    protected boolean isLoginPage(HttpServletRequest request) {
        if (request.getRequestURI().contains("login.jsp")) {
            return true;
        }
        return false;
    }

    protected boolean isAuthenticatedUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext != null) {
            Authentication auth = securityContext.getAuthentication();
            if (!(auth == null || auth instanceof AnonymousAuthenticationToken)) {
                return true;
            }
        }
        return false;
    }

    public ThemeResolver getDelegate() {
        return delegate;
    }

    public void setDelegate(ThemeResolver delegate) {
        this.delegate = delegate;
    }
}

