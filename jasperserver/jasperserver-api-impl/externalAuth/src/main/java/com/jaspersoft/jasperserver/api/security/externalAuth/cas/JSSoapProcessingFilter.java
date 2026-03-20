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
package com.jaspersoft.jasperserver.api.security.externalAuth.cas;

import org.apache.commons.codec.binary.Base64;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.util.Assert;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Chaim Arbiv
 * @version $id$
 */
public class JSSoapProcessingFilter extends JSCasProcessingFilter{
	//TODO: Do we need to expose these properties in config???  Should we make them constants?
	private String authorizationHeaderKeyName = "Authorization";
	private String encryptionKeyName = "Basic";
	private String credentialsDelimiter = ":";

    private String userName;
    private String password;
    private String ticket;
    private FilterChain chain;

    public void afterPropertiesSet() {
        Assert.notNull(getAuthenticationManager(), "authenticationManager must be specified");

        if (getRememberMeServices() == null) {
            setRememberMeServices(new NullRememberMeServices());
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        this.chain = chain;
        super.doFilter(req, res, chain);
    }

	protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
		String uri = request.getRequestURI();
		int pathParamIndex = uri.indexOf(';');

		if (pathParamIndex > 0) {
			// strip everything after the first semi-colon
			uri = uri.substring(0, pathParamIndex);
		}

// TODO: ogavavka SpringUpgrade 22.08.2016 - most probably we will drop SOAP support, so just making this method to compile
//		if ("".equals(request.getContextPath())) {
//			return uri.endsWith(getFilterProcessesUrl());
//		}
//
//		return uri.startsWith(request.getContextPath() + getFilterProcessesUrl());
        return true;
	}

    // since we need to deal with the usaul superuser:superuser auth and ticket:<ticket> auth
    protected void onPreAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {
        String header = request.getHeader("Authorization");

		if (header != null && logger.isDebugEnabled()) {
			logger.debug("Authorization header: " + header);
		}

		if ((header != null) && header.startsWith(encryptionKeyName)) {
			String base64Token = header.substring(6);
            String token = new String(Base64.decodeBase64(base64Token.getBytes()));

            int delim = token.indexOf(":");
            String tempHeader; // to check if we got a normal user pass token or a it is a sso ticket
            if (delim != -1) {
                tempHeader = token.substring(0, delim);
                if (tempHeader.equalsIgnoreCase(encryptionKeyName))
                    ticket = token.substring(delim + 1);
                else {
                    userName = tempHeader;
                    password = token.substring(delim + 1);
                }
            }
        }
	}

    protected void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException{
        try {
            chain.doFilter(request, response);
        }

        catch (ServletException e) {
            logger.error( "ServletException: "+ e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    public String getAuthorizationHeaderKeyName() {
        return authorizationHeaderKeyName;
    }

    public void setAuthorizationHeaderKeyName(String authorizationHeaderKeyName) {
        this.authorizationHeaderKeyName = authorizationHeaderKeyName;
    }

    public String getEncryptionKeyName() {
        return encryptionKeyName;
    }

    protected String obtainTicket(HttpServletRequest request) {
        return ticket;
    }

    public void setEncryptionKeyName(String encryptionKeyName) {
        this.encryptionKeyName = encryptionKeyName;
    }

    public String getCredentialsDelimiter() {
        return credentialsDelimiter;
    }

    public void setCredentialsDelimiter(String credentialsDelimiter) {
        this.credentialsDelimiter = credentialsDelimiter;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public FilterChain getChain() {
        return chain;
    }

    public void setChain(FilterChain chain) {
        this.chain = chain;
    }
}

