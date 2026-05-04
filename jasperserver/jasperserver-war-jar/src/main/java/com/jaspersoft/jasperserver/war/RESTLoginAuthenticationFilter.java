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

package com.jaspersoft.jasperserver.war;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaspersoft.jasperserver.api.security.ResponseHeaderUpdater;
import com.jaspersoft.jasperserver.api.security.encryption.EncryptionRequestUtils;
import com.jaspersoft.jasperserver.api.security.internalAuth.InternalDaoAuthenticationProvider;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Locale;

import static org.springframework.web.bind.annotation.RequestMethod.OPTIONS;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author dlitvak
 * @version $Id$
 */
public class RESTLoginAuthenticationFilter implements Filter {

    private static final Log log = LogFactory.getLog(RESTLoginAuthenticationFilter.class);
    static final String LOGIN_PATH_INFO = "/login";

    private AuthenticationManager authenticationManager;
    private String userNameParam = "j_username";
    private String userPwdParam = "j_password";

    private boolean postOnly = true;
    private ResponseHeaderUpdater responseHeadersUpdater;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Autowired
    private MessageSource messageSource;

    private static Locale locale;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        removeSessionAttributesForLoginLockout(httpRequest);
        if (responseHeadersUpdater != null) {
            responseHeadersUpdater.changeHeaders(httpResponse, httpRequest);
        }

        String pathInfo = httpRequest.getPathInfo();
        if (pathInfo != null && pathInfo.equalsIgnoreCase(LOGIN_PATH_INFO)) {
            if (!POST.name().equalsIgnoreCase(httpRequest.getMethod()) && this.postOnly) {
                sendUnauthorizedResponse(httpResponse, HttpServletResponse.SC_BAD_REQUEST, "Unauthorized. Only POST is accepted per configuration.");
                return;
            } else if (OPTIONS.name().equalsIgnoreCase(httpRequest.getMethod())) {
                httpResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }

            String username = EncryptionRequestUtils.getValue(request, userNameParam);
            String password = EncryptionRequestUtils.getValue(request, userPwdParam);

            if (!StringUtils.isEmpty(username)) {
                // decoding since | is not http safe
                username = URLDecoder.decode(username, CharEncoding.UTF_8);

                UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
                authRequest.setDetails(new WebAuthenticationDetails(httpRequest));

                Authentication authResult;
                try {

                    authResult = authenticationManager.authenticate(authRequest);

                    if (log.isDebugEnabled()) {
                        log.debug("User " + username + " authenticated: " + authResult);
                    }

                    SecurityContextHolder.getContext().setAuthentication(authResult);
                } catch (AuthenticationException e) {
                    if (log.isWarnEnabled()) {
                        log.warn("User " + username + " failed to authenticate: " + e.toString() + " " + e, e.getCause());
                    }

                    SecurityContextHolder.getContext().setAuthentication(null);
                    sendUnauthorizedResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED,"");
                }
            } else {
                if (log.isWarnEnabled()) {
                    log.warn("Failed to authenticate: Bad request. Username and password must be specified.");
                }

                sendUnauthorizedResponse(httpResponse, HttpServletResponse.SC_BAD_REQUEST,"Bad request."
                        + (StringUtils.isEmpty(username) ? " Parameter " + userNameParam + " not found." : "")
                        + (StringUtils.isEmpty(password) ? " Parameter " + userPwdParam + " not found." : ""));

            }

            return;
        }

        chain.doFilter(request, response);
    }

    private void removeSessionAttributesForLoginLockout(HttpServletRequest httpServletRequest) {
        HttpSession httpSession = httpServletRequest.getSession();
        Assert.notNull(httpSession,"Unexpected error. Session is null");
        httpSession.removeAttribute(InternalDaoAuthenticationProvider.NUMBER_OF_LOGIN_ATTEMPTS_REMAINING_SESSIONATTR);
        httpSession.removeAttribute(InternalDaoAuthenticationProvider.USER_PRINCIPAL_SESSIONATTR);
        httpSession.removeAttribute(InternalDaoAuthenticationProvider.IS_USER_LOCKED_SESSIONATTR);
        httpSession.removeAttribute(InternalDaoAuthenticationProvider.EMPTY_CREDENTIALS_SESSIONATTR);
    }

    private void sendUnauthorizedResponse(HttpServletResponse httpResponse, int responseCode, String msg)throws IOException {
        httpResponse.setStatus(responseCode);
        locale = LocaleContextHolder.getLocale();
        ErrorDescriptor errorDescriptor = new ErrorDescriptor();
        errorDescriptor.setErrorCode(String.valueOf(HttpServletResponse.SC_UNAUTHORIZED));
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
            HttpSession httpSession = request.getSession();
            if(null != httpSession.getAttribute(InternalDaoAuthenticationProvider.EMPTY_CREDENTIALS_SESSIONATTR)){
                String emptyCredentialsMessage = (String) httpSession.getAttribute(InternalDaoAuthenticationProvider.EMPTY_CREDENTIALS_SESSIONATTR);
                errorDescriptor.setMessage(emptyCredentialsMessage.isEmpty() ? msg : emptyCredentialsMessage);
            }
            else if(null != httpSession.getAttribute(InternalDaoAuthenticationProvider.IS_USER_LOCKED_SESSIONATTR) &&
                    (boolean)httpSession.getAttribute(InternalDaoAuthenticationProvider.IS_USER_LOCKED_SESSIONATTR)){
                errorDescriptor.setMessage(messageSource.getMessage("jsp.loginError.userLockedMessage",null,locale));
            }
            else if (null != httpSession.getAttribute(
                    InternalDaoAuthenticationProvider.NUMBER_OF_LOGIN_ATTEMPTS_REMAINING_SESSIONATTR)){
                int numberOfLoginAttemptsRemaining = (int) httpSession.getAttribute(
                        InternalDaoAuthenticationProvider.NUMBER_OF_LOGIN_ATTEMPTS_REMAINING_SESSIONATTR);
                String badCredentialsMessage = messageSource.getMessage("jsp.loginError.invalidCredentials1",null,locale);
                String nofaMessage = messageSource.getMessage("jsp.loginError.remainingLoginAttempts",null,locale);
                errorDescriptor.setMessage(badCredentialsMessage+" "+nofaMessage+":"+numberOfLoginAttemptsRemaining);
            }
            else{
                errorDescriptor.setMessage(msg);
                errorDescriptor.setErrorCode(""+responseCode);
            }
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_NULL,
                    JsonInclude.Include.ALWAYS));
            httpResponse.setContentType("application/json");
            objectMapper.writeValue(httpResponse.getWriter(), errorDescriptor);
        }
        else{
            PrintWriter pw = httpResponse.getWriter();
            pw.print(msg);
        }
    }

    @Override
    public void destroy() {
    }

    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setResponseHeadersUpdater(ResponseHeaderUpdater responseHeadersUpdater) {
        this.responseHeadersUpdater = responseHeadersUpdater;
    }

    public String getUserNameParam() {
        return userNameParam;
    }

    public void setUserNameParam(String userNameParam) {
        this.userNameParam = userNameParam;
    }

    public String getUserPwdParam() {
        return userPwdParam;
    }

    public void setUserPwdParam(String userPwdParam) {
        this.userPwdParam = userPwdParam;
    }

    public boolean isPostOnly() {
        return postOnly;
    }

    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
}

