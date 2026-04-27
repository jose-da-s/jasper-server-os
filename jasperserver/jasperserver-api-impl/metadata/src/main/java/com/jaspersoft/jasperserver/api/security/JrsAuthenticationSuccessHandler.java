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

import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.client.MetadataUserDetails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * <p>Wrapper class for Spring Security org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler</p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
public class JrsAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
	private static final Logger logger = LogManager.getLogger(JrsAuthenticationSuccessHandler.class);

	private RequestCache requestCache = new HttpSessionRequestCache();
    private ResponseHeaderUpdater responseHeadersUpdater;
	private SessionRegistry sessionRegistry;
    private String jsonRedirectUrl;

    public void setJsonRedirectUrl(String jsonRedirectUrl) {
        this.jsonRedirectUrl = jsonRedirectUrl;
    }

    public void setSessionRegistry(SessionRegistry sessionRegistry) {
		this.sessionRegistry = sessionRegistry;
	}

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        if (responseHeadersUpdater != null) {
            responseHeadersUpdater.changeHeaders(response, request);
        }
        final SecurityContext securityContext = SecurityContextHolder.getContext();
		//[bug 40360] - Fix Spring Security multi-threading bug.
		new HttpSessionSecurityContextRepository().saveContext(securityContext, request, response);
		if (logger.isDebugEnabled()) {
			logger.debug("Security context saved to a session. Context: " + securityContext);
		}
		final SavedRequest cachedRequest = requestCache.getRequest(request, response);
        final String accept = request.getHeader("Accept");
        if (cachedRequest != null &&
                ("true".equalsIgnoreCase(request.getParameter("forceDefaultRedirect"))
                        || cachedRequest.getRedirectUrl().endsWith("flow.html")
                        || (accept != null && accept.toLowerCase().contains("application/json")))) {
            // 1) forceDefaultRedirect=true means that client doesn't want to follow redirect to any stored URL
            // 2) cachedRequest.getRedirectUrl().endsWith("flow.html") -  cannot restore web flow state
            // 3) json is requested. No stored request should be used in this case. See also determineTargetUrl() below
            // redirection to default page in both cases
            requestCache.removeRequest(request, response);
        }
		super.onAuthenticationSuccess(request, response, authentication);

		HttpSession session = request.getSession(false);
		Object principal = securityContext.getAuthentication().getPrincipal();
        String userName = "";
        if (principal instanceof UserDetails) {
            userName =  ((UserDetails)principal).getUsername();
        }
        if (principal instanceof MetadataUserDetails && ((MetadataUserDetails)principal).getTenantId() != null &&
                ((MetadataUserDetails)principal).getTenantId().length() > 0) {
            userName = userName + "|" + ((MetadataUserDetails)principal).getTenantId();
        }
        if (session != null) {
            sessionRegistry.registerNewSession(session.getId(), userName);
        }
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        final String accept = request.getHeader("Accept");
        return accept != null && accept.toLowerCase().contains("application/json") ?
                jsonRedirectUrl : super.determineTargetUrl(request, response);
    }

    public void setResponseHeadersUpdater(ResponseHeaderUpdater responseHeadersUpdater) {
        this.responseHeadersUpdater = responseHeadersUpdater;
    }
}

