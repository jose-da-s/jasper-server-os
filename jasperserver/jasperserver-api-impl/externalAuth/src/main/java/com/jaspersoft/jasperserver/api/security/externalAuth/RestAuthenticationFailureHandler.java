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
import com.jaspersoft.jasperserver.api.common.error.handling.JSEmptyCredentialsException;
import com.jaspersoft.jasperserver.api.security.internalAuth.InternalDaoAuthenticationProvider;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;

/**
 * @author dlitvak
 */
public class RestAuthenticationFailureHandler implements AuthenticationFailureHandler {
    protected final Log logger = LogFactory.getLog(getClass());
    @Autowired
    private MessageSource messageSource;

    private static Locale locale;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        logger.debug("Authentication Failure");
        //added for Login-Lockout JS-64098
        locale = LocaleContextHolder.getLocale();
        ErrorDescriptor errorDescriptor = new ErrorDescriptor();
        errorDescriptor.setErrorCode(String.valueOf(HttpServletResponse.SC_UNAUTHORIZED));
        HttpSession httpSession = request.getSession();

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_NULL,
                JsonInclude.Include.ALWAYS));
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        if(exception instanceof AuthenticationServiceException){
            // can be when GET is used instead of POST for API endpoint /rest_v2/login
            errorDescriptor.setMessage(exception.getMessage());
            objectMapper.writeValue(response.getWriter(), errorDescriptor);
        }
        else if(exception instanceof JSEmptyCredentialsException){
            Object emptyCredentialsMessage = httpSession.getAttribute(InternalDaoAuthenticationProvider.EMPTY_CREDENTIALS_SESSIONATTR);
            if(null != emptyCredentialsMessage ){
                errorDescriptor.setMessage(String.valueOf(emptyCredentialsMessage));
            }
            else{
                errorDescriptor.setMessage(messageSource.getMessage("jsp.loginError.invalidCredentials1",null,locale));
            }
            objectMapper.writeValue(response.getWriter(), errorDescriptor);
        }else if(exception instanceof LockedException || exception instanceof DisabledException ||
        exception instanceof BadCredentialsException){
            int allowedNumberOfLoginAttempts = Integer.parseInt(LoginLockoutConfig.getNumberOfFailedLoginAttempts());
            if(allowedNumberOfLoginAttempts > 0) {   //if Login-Lockout is enabled
                if (null != httpSession.getAttribute(InternalDaoAuthenticationProvider.IS_USER_LOCKED_SESSIONATTR) &&
                        (boolean) httpSession.getAttribute(InternalDaoAuthenticationProvider.IS_USER_LOCKED_SESSIONATTR)) {
                    errorDescriptor.setMessage(messageSource.getMessage("jsp.loginError.userLockedMessage", null, locale));
                } else {
                    int numberOfLoginAttemptsRemaining = (int) httpSession.getAttribute(
                            InternalDaoAuthenticationProvider.NUMBER_OF_LOGIN_ATTEMPTS_REMAINING_SESSIONATTR);
                    String badCredentialsMessage = messageSource.getMessage("jsp.loginError.invalidCredentials1", null, locale);
                    String nofaMessage = messageSource.getMessage("jsp.loginError.remainingLoginAttempts", null, locale);
                    errorDescriptor.setMessage(badCredentialsMessage + " " + nofaMessage + ":" + numberOfLoginAttemptsRemaining);
                }
                objectMapper.writeValue(response.getWriter(), errorDescriptor);
            }
            else{
                errorDescriptor.setMessage(exception.getMessage());
                objectMapper.writeValue(response.getWriter(), errorDescriptor);
            }
        }
        else{
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            //empty-body response
        }
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
}

