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

package com.jaspersoft.jasperserver.api.security.externalAuth.sso;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.util.Assert;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Chaim Arbiv
 * @version $id$
 *          handles SOAP SSO authentication requests.
 */
public class SsoAuthenticationSoapProcessingFilter extends SsoAuthenticationProcessingFilter {
	//TODO: Do we need to expose these properties in config???  Should we make them constants?
	private String authorizationHeaderKeyName = "Authorization";
	private String encryptionKeyName = "Basic";
	private String credentialsDelimiter = ":";

    private String userName;
    private String password;
    private String ticket;

    private String service;

    private final static Logger logger = LogManager.getLogger(SsoAuthenticationSoapProcessingFilter.class);

    public void afterPropertiesSet() {
        Assert.notNull(getAuthenticationManager(), "authenticationManager must be specified");
        Assert.notNull(getExternalAuthProperties(), "externalAuthProperties cannot be null");

        Assert.notNull(getAuthorizationHeaderKeyName(), "authorizationHeaderKeyName cannot be null");
        Assert.notNull(getEncryptionKeyName(), "encryptionKeyName cannot be null");
        Assert.notNull(getCredentialsDelimiter(), "credentialsDelimiter cannot be null");

        if (getRememberMeServices() == null) {
            setRememberMeServices(new NullRememberMeServices());
        }
    }

    public void doFilterHttp(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        if (requiresAuthentication(request, response)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Request is to process authentication");
            }

            Authentication authResult;

            try {
                onPreAuthentication(request, response);
                authResult = attemptAuthentication(request, response);
            }
            catch (AuthenticationException failed) {
                // Authentication failed
                unsuccessfulAuthentication(request, response, failed);

                return;
            }

            // Authentication success
            successfulAuthentication(request, response,chain, authResult);
            chain.doFilter(request, response);
        }
    }

    @Override
    protected boolean requiresAuthentication(final HttpServletRequest request, final HttpServletResponse response) {
        String attToken = request.getHeader(authorizationHeaderKeyName);
        return ( attToken!=null && attToken.startsWith(encryptionKeyName) || super.requiresAuthentication(request, response) );
    }

    // gets the token. the spec says encryption name + space and then key:value
    protected Object obtainTicket(HttpServletRequest request) {
        return ticket;
    }

    // gets the token. the spec says encryption name + space and then key:value
    protected String obtainUsername(HttpServletRequest request) {
        return userName;
    }

    // gets the token. the spec says encryption name + space and then key:value
    protected String obtainPassword(HttpServletRequest request) {
        return password;
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

    protected void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url){

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

    public void setEncryptionKeyName(String encryptionKeyName) {
        this.encryptionKeyName = encryptionKeyName;
    }

    public String getCredentialsDelimiter() {
        return credentialsDelimiter;
    }

    public void setCredentialsDelimiter(String credentialsDelimiter) {
        this.credentialsDelimiter = credentialsDelimiter;
    }
}

