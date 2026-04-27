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

package com.jaspersoft.jasperserver.api.security.externalAuth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaspersoft.jasperserver.api.common.configuration.LoginLockoutConfig;
import com.jaspersoft.jasperserver.api.security.internalAuth.InternalAuthenticationToken;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

/**
 * @author dlitvak
 */
public class RestAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    protected final Log logger = LogFactory.getLog(getClass());
    private ExternalDataSynchronizer externalDataSynchronizer;

    @Autowired
    MessageSource messageSource;

    private static Locale locale;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            if (!(authentication instanceof InternalAuthenticationToken))
                externalDataSynchronizer.synchronize();
            logger.debug("Successful authentication");

            //empty-body response
        } catch(DisabledException disabledException){
            logger.debug("User account is disabled");
            int numberOfAllowedLoginAttempts = Integer.parseInt(LoginLockoutConfig.getNumberOfFailedLoginAttempts());
            if(numberOfAllowedLoginAttempts > 0 ){ //indicates that Login-Lockout is enabled.
                locale = LocaleContextHolder.getLocale();
                ErrorDescriptor errorDescriptor = new ErrorDescriptor();
                errorDescriptor.setErrorCode(String.valueOf(HttpServletResponse.SC_UNAUTHORIZED));
                errorDescriptor.setMessage(messageSource.getMessage("jsp.loginError.userLockedMessage",null,locale));
                final ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_NULL,
                        JsonInclude.Include.ALWAYS));
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                objectMapper.writeValue(response.getWriter(), errorDescriptor);
            }
            else{
                throw disabledException;
            }
        }
        catch (RuntimeException e) {
            SecurityContextHolder.getContext().setAuthentication(null);
            throw e;
        }
    }

    /**
     *
     * @param externalDataSynchronizer
     */
    public void setExternalDataSynchronizer(ExternalDataSynchronizer externalDataSynchronizer) {
        this.externalDataSynchronizer = externalDataSynchronizer;
    }

    protected ExternalDataSynchronizer getExternalDataSynchronizer() {
        return externalDataSynchronizer;
    }
}

