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

import com.jaspersoft.jasperserver.api.metadata.user.domain.Tenant;
import com.jaspersoft.jasperserver.api.metadata.user.domain.TenantQualified;
import com.jaspersoft.jasperserver.api.metadata.user.service.TenantService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.theme.AbstractThemeResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Application theme resolver for multi tenant environment.
 * The class detects a tenant for the current user, and finds out
 * the active theme that takes presence.
 *
 * @author asokolnikov
 */
public class MTThemeResolver extends AbstractThemeResolver {

    private TenantService tenantService;

    public String resolveThemeName(HttpServletRequest request) {

        if (isLoginPage(request)) {
            // for login page, use root active theme
            return getRootActive();
        }

        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext != null) {
            Authentication auth = securityContext.getAuthentication();
            if (auth != null && auth.getPrincipal() != null) {
                if (auth.getPrincipal() instanceof TenantQualified) {
                    TenantQualified tenantQualified = (TenantQualified) auth.getPrincipal();
                    String tenantId = tenantQualified.getTenantId() != null ? tenantQualified.getTenantId() : TenantService.ORGANIZATIONS;
                    Tenant tenant = tenantService.getTenant(null, tenantId);
                    if (tenant != null) {
                        String theme = tenant.getTheme();
                        if (theme != null && theme.length() > 0) {
                            return theme;
                        }
                    }
                }
            }
        }

        return getRootActive();
    }

    protected String getRootActive() {
        Tenant root = tenantService.getTenant(null, TenantService.ORGANIZATIONS);
        if (root.getTheme() != null && root.getTheme().length() > 0) {
            return root.getTheme();
        }
        // fallback to the configured default (should not happen)
        return getDefaultThemeName();
    }

    protected boolean isLoginPage(HttpServletRequest request) {
        if (request.getRequestURI().contains("login.jsp")) {
            return true;
        }
        return false;
    }

    public void setThemeName(HttpServletRequest request, HttpServletResponse response, String themeName) {
        throw new UnsupportedOperationException("MTThemeResolver does not support using setThemeName. Set the active theme for the organization instead.");
    }

    public TenantService getTenantService() {
        return tenantService;
    }

    public void setTenantService(TenantService tenantService) {
        this.tenantService = tenantService;
    }
}

