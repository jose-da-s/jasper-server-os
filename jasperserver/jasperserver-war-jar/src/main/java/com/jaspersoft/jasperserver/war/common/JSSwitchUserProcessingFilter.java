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

package com.jaspersoft.jasperserver.war.common;

import com.jaspersoft.jasperserver.api.logging.audit.context.AuditContext;
import com.jaspersoft.jasperserver.api.logging.audit.domain.AuditEvent;
import com.jaspersoft.jasperserver.api.metadata.user.domain.TenantQualified;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ThemeResolver;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.jaspersoft.jasperserver.api.logging.audit.domain.AuditEventType.EXIT_SWITCHED_USER;
import static com.jaspersoft.jasperserver.api.logging.audit.domain.AuditEventType.SWITCH_USER;

/**
 * @author Yuriy Plakosh
 */
public class JSSwitchUserProcessingFilter extends SwitchUserFilter {

    private String jsTargetUrl;
    private String exitTargetUrl;
    private AuditContext auditContext;
    private ThemeResolver themeResolver;

    public void setAuditContext(AuditContext auditContext) {
        this.auditContext = auditContext;
    }

    public void afterPropertiesSet() {
        super.afterPropertiesSet();

        Assert.hasLength(jsTargetUrl, "jsTargetUrl must be specified");
        Assert.hasLength(exitTargetUrl, "exitTargetUrl must be specified");
    }

    private void createAuditEvent(final String eventTypeName) {
        auditContext.doInAuditContext(new AuditContext.AuditContextCallback() {
            public void execute() {
                auditContext.createAuditEvent(eventTypeName);
            }
        });
    }

    private String getTenantId(Authentication authentication) {
        String tenantId = "";
        Object principal = authentication.getPrincipal();
        if (principal instanceof TenantQualified) {
            tenantId = ((TenantQualified) principal).getTenantId();
        }

        return tenantId;
    }

    private void addParamsToSwitchUserAuditEvent(final Authentication authentication) {
        auditContext.doInAuditContext(SWITCH_USER.toString(), new AuditContext.AuditContextCallbackWithEvent() {
            public void execute(AuditEvent auditEvent) {
                auditContext.addPropertyToAuditEvent("username", authentication.getName(), auditEvent);
                auditContext.addPropertyToAuditEvent("organization", getTenantId(authentication), auditEvent);
            }
        });
    }

    private void addParamsToExitUserAuditEvent(final Authentication authentication) {
        auditContext.doInAuditContext(EXIT_SWITCHED_USER.toString(), new AuditContext.AuditContextCallbackWithEvent() {
            public void execute(AuditEvent auditEvent) {
                String loggedInUserName = auditEvent.getUsername();
                String loggedInTenantId = auditEvent.getTenantId();
                auditEvent.setUsername(authentication.getName());
                auditEvent.setTenantId(getTenantId(authentication));
                auditContext.addPropertyToAuditEvent("username", loggedInUserName, auditEvent);
                auditContext.addPropertyToAuditEvent("organization", loggedInTenantId, auditEvent);
            }
        });
    }

    private void closeAuditEvent(String eventTypeName) {
        auditContext.doInAuditContext(eventTypeName, new AuditContext.AuditContextCallbackWithEvent() {
            public void execute(AuditEvent auditEvent) {
                auditContext.closeAuditEvent(auditEvent);
            }
        });
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) req;
        HttpServletResponse httpResponse = (HttpServletResponse) res;
        // check for switch or exit request
        if (requiresSwitchUser(httpRequest)) {
            createAuditEvent(SWITCH_USER.toString());

            // if set, attempt switch and store original
            Authentication targetUser = attemptSwitchUser(httpRequest);

            addParamsToSwitchUserAuditEvent(targetUser);
            
            // update the current context to the new target user
            SecurityContextHolder.getContext().setAuthentication(targetUser);

            // redirect to target url
            httpResponse.sendRedirect(httpResponse.encodeRedirectURL(httpRequest.getContextPath() + jsTargetUrl));

            closeAuditEvent(SWITCH_USER.toString());

            // reset the current theme to initiate resolving again
            themeResolver.setThemeName(httpRequest, httpResponse, null);
            
            return;
        } else if (requiresExitUser(httpRequest)) {
            createAuditEvent(EXIT_SWITCHED_USER.toString());

            // get the original authentication object (if exists)
            Authentication originalUser = attemptExitUser(httpRequest);

            addParamsToExitUserAuditEvent(originalUser);

            // update the current context back to the original user
            SecurityContextHolder.getContext().setAuthentication(originalUser);

            // redirect to target url
            httpResponse.sendRedirect(httpResponse.encodeRedirectURL(httpRequest.getContextPath() + exitTargetUrl));

            closeAuditEvent(EXIT_SWITCHED_USER.toString());

            // reset the current theme to initiate resolving again
            themeResolver.setThemeName(httpRequest, httpResponse, null);

            return;
        }

        chain.doFilter(httpRequest, httpResponse);
    }

    public void setTargetUrl(String targetUrl) {
        super.setTargetUrl(targetUrl);

        jsTargetUrl = targetUrl;
    }

    public void setExitTargetUrl(String exitTargetUrl) {
        this.exitTargetUrl = exitTargetUrl;
    }

    public ThemeResolver getThemeResolver() {
        return themeResolver;
    }

    public void setThemeResolver(ThemeResolver themeResolver) {
        this.themeResolver = themeResolver;
    }
}

