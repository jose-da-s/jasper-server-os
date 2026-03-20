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

import com.jaspersoft.jasperserver.api.security.EncryptionAuthenticationProcessingFilter;
import com.jaspersoft.jasperserver.api.security.externalAuth.BaseAuthenticationProcessingFilter;
import com.jaspersoft.jasperserver.api.security.externalAuth.ExternalAuthProperties;
import org.apache.commons.lang.CharEncoding;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * A filter that intercepts the redirected SSO server request with the SSO token.
 * The filter starts SSO token validation process.
 *
 * @author Chaim Arbiv
 */
public abstract class AbstractSsoAuthenticationProcessingFilter extends EncryptionAuthenticationProcessingFilter {
	private final static Logger logger = LogManager.getLogger(AbstractSsoAuthenticationProcessingFilter.class);

	private ExternalAuthProperties externalAuthProperties;

    //~ Methods ========================================================================================================

    /**
     * Attempt validation of SSO token.
     * @param request sso server request
     * @return {@link Authentication} with some of the principal information
     * @throws AuthenticationException if authentication fails
     */
	@Override
    public final Authentication attemptAuthentication(final HttpServletRequest request, final HttpServletResponse response) throws AuthenticationException {
        try{

            logger.debug("Attempt authentication with SSO token ...");
            Object ticket = obtainTicket(request);

            logger.debug("SSO Token: " + ticket);
            String userName = obtainUsername(request);
            userName = userName!=null ? URLDecoder.decode(userName, CharEncoding.UTF_8): null;
            String password = obtainPassword(request);

            final SsoAuthenticationToken authToken = new SsoAuthenticationToken(ticket, userName, password);
            setDetails(request, authToken);
            return this.getAuthenticationManager().authenticate(authToken);
        }
        catch (UnsupportedEncodingException e) {
            logger.error( "could not decode user name "+ obtainUsername(request));
            throw new IllegalStateException(e);
        }
    }

    /**
     * Retrieve an SSO token from the request
     * @param request  redirected SSO server request
     * @return SSO ticket/token object
     */
    protected abstract Object obtainTicket(HttpServletRequest request);

	public ExternalAuthProperties getExternalAuthProperties() {
		return externalAuthProperties;
	}

	public void setExternalAuthProperties(ExternalAuthProperties externalAuthProperties) {
		this.externalAuthProperties = externalAuthProperties;
	}
}

