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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * This deprecated class is introduced for supporting legacy systems which didn't update to new username and password parameters and old entry url,
 * usage of old parameters will be throwing warnings but will ty to use them.
 * This wrapper will be deprecated in some future and UsernamePasswordAuthenticationFilter will be used directly.
 */
@Deprecated
public class UsernamePasswordAuthenticationFilterWarningWrapper extends UsernamePasswordAuthenticationFilter {
    private Logger log = LogManager.getLogger(this.getClass());
    public RequestMatcher matcher;

    public static final String LEGACY_SPRING_SECURITY_FORM_USERNAME_KEY = "j_username";
    public static final String LEGACY_SPRING_SECURITY_FORM_PASSWORD_KEY = "j_password";
    public static final String LEGACY_SPRING_SECURITY_FORM_ENDPOINT = "/j_spring_security_check";


    public UsernamePasswordAuthenticationFilterWarningWrapper() {
        super();
    }

    public UsernamePasswordAuthenticationFilterWarningWrapper(RequestMatcher matcher) {
        super();
        setMatcher(matcher);

    }


    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        if (matcher==null) {
            List<RequestMatcher> matchers = new ArrayList<RequestMatcher>();

            matchers.add(new AntPathRequestMatcher("/login", "POST"));
            /*
            * By default from Spring Security 4  UsernamePasswordAuthenticationFilter is cheking /login endpoint,
            * adding legacy endpoint to support legacy systems */
            matchers.add(new AntPathRequestMatcher(LEGACY_SPRING_SECURITY_FORM_ENDPOINT, "POST"));
            matchers.add(new AntPathRequestMatcher(LEGACY_SPRING_SECURITY_FORM_ENDPOINT, "GET"));
            matcher=new OrRequestMatcher(matchers);
        }
        setRequiresAuthenticationRequestMatcher(matcher);
    }

    @Override
    protected String obtainUsername(HttpServletRequest request) {
        String username = super.obtainUsername(request);
        if (username==null) {
            /*
            * By default from Spring Security 4 username is stored under 'username' parameter name,
            * this code will try to fallback to deprecated parameter name, this functionality will be removed in some future*/
            username = request.getParameter(LEGACY_SPRING_SECURITY_FORM_USERNAME_KEY);
            log.warn("Deprecated username parameter used to obtain it from request! switch to new parameter name.");
        }
        return username;
    }

    @Override
    protected String obtainPassword(HttpServletRequest request) {
        String password = super.obtainPassword(request);
        if (password==null) {
            /*
            * By default from Spring Security 4 password is stored under 'password' parameter name,
            * this code will try to fallback to deprecated parameter name, this functionality will be removed in some future*/
            password = request.getParameter(LEGACY_SPRING_SECURITY_FORM_PASSWORD_KEY);
            log.warn("Deprecated password parameter used to obtain it from request! switch to new parameter name.");
        }
        return password;
    }

    public static String obtainUsernameWithLegacySupport(HttpServletRequest request) {
        String username = request.getParameter(SPRING_SECURITY_FORM_USERNAME_KEY);
        if (username==null) {
            username = request.getParameter(LEGACY_SPRING_SECURITY_FORM_USERNAME_KEY);
        }
        return username;

    }

    public static String obtainPasswordWithLegacySupport(HttpServletRequest request) {
        String password = request.getParameter(SPRING_SECURITY_FORM_PASSWORD_KEY);
        if (password==null) {
            password = request.getParameter(LEGACY_SPRING_SECURITY_FORM_PASSWORD_KEY);
        }
        return password;

    }

    public void setMatcher(RequestMatcher matcher) {
        this.matcher = matcher;
    }

}

