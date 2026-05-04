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

import com.jaspersoft.jasperserver.api.common.util.AuthFilterConstants;
import com.jaspersoft.jasperserver.api.security.encryption.EncryptionRequestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * Overwrites obtainUsername and obtainPassword so that decrypted values can be used.
 * EncryptionFilter must come before this so that this can pull out the decrypted values
 * for username and password.
 *
 * @author norm
 * @see com.jaspersoft.jasperserver.api.security.encryption.EncryptionFilter
 */
public class EncryptionAuthenticationProcessingFilter extends UsernamePasswordAuthenticationFilterWarningWrapper {

    private Logger log = LogManager.getLogger(this.getClass());


    /**
     * When the password is encrypted in the EncryptionFilter, the encrypted value is passed into the
     * PARAM_PWD attribute.  With encryption off, the password is in the original request parameter.
     *
     * @param request A HttpServletRequest.
     * @return String or null.
     */
    @Override
    protected String obtainPassword(HttpServletRequest request) {
        return EncryptionRequestUtils.getValueWithLegacySupport(request, getPasswordParameter());
    }
	@Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response, FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
        if (request.getHeader(AuthFilterConstants.X_REMOTE_DOMAIN)!=null) {
            request.setAttribute(AuthFilterConstants.AUTH_FLOW_CONST, "true");
            chain.doFilter(request, response);
        }
        super.successfulAuthentication(request, response, chain, authResult);

    }

    @Override
    public AuthenticationManager getAuthenticationManager() {
        return super.getAuthenticationManager();
    }}

