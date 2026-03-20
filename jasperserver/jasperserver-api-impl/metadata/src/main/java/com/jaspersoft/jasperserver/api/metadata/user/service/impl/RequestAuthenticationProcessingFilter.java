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
package com.jaspersoft.jasperserver.api.metadata.user.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.PortResolver;
import org.springframework.security.web.PortResolverImpl;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.util.Assert;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class RequestAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {
	
	private static final Log log = LogFactory.getLog(RequestAuthenticationProcessingFilter.class);
	
	public static final String REQUEST_AUTHENTICATION_ID = "REQUEST_AUTHENTICATION_ID"; 

	private PortResolver portResolver = new PortResolverImpl();

    /**
     * @param defaultFilterProcessesUrl the default value for <tt>filterProcessesUrl</tt>.
     */
    protected RequestAuthenticationProcessingFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
    }


    public void afterPropertiesSet() {
        Assert.notNull(getAuthenticationManager(),
            "authenticationManager must be specified");
        Assert.notNull(getRememberMeServices());
    }

	/* (non-Javadoc)
	 * @see org.springframework.security.ui.AbstractProcessingFilter#attemptAuthentication(jakarta.servlet.http.HttpServletRequest)
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        Map requestParameters = obtainRequestParameters(request);
	    String targetUrl = new HttpSessionRequestCache().getRequest(request, null).getRedirectUrl();

        if (log.isDebugEnabled()) {
		    log.debug("Authenticating with values: '" + requestParameters + "'");
		    log.debug("from URL: " + targetUrl);
        }
        
        Authentication authRequest = new RequestAuthenticationToken(requestParameters);

        // This call to getSession().setAttribute needs to happen, otherwise you get into an
        // infinite loop. Maybe just a getAttribute will work?
        request.getSession().setAttribute(REQUEST_AUTHENTICATION_ID, targetUrl);

        return this.getAuthenticationManager().authenticate(authRequest);
	}

    protected boolean requiresAuthentication(HttpServletRequest request,
            HttpServletResponse response) {
    	return SecurityContextHolder.getContext().getAuthentication() == null;
    }


    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult)
            throws IOException, ServletException {
        if (logger.isDebugEnabled()) {
            logger.debug("Authentication success: " + authResult.toString());
        }

        String targetUrl = (new DefaultSavedRequest(request, portResolver)).getRequestURL();

        if (logger.isDebugEnabled()) {
            logger.debug("Redirecting to target URL from HTTP Session (or default): " + targetUrl);
        }

        super.successfulAuthentication(request, response, chain, authResult);

        response.sendRedirect(response.encodeRedirectURL(targetUrl));
    }

    
    protected Map obtainRequestParameters(HttpServletRequest request) {
		
		Map result = new HashMap();
		result.putAll(request.getParameterMap());
		
		Enumeration attrs = request.getAttributeNames();
		while (attrs.hasMoreElements()) {
			String attrName = (String) attrs.nextElement();
			result.put(attrName, request.getAttribute(attrName));
		}
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.security.ui.AbstractProcessingFilter#getDefaultFilterProcessesUrl()
	 */
	public String getDefaultFilterProcessesUrl() {
        return "/requestAuthentication";
	}
}

