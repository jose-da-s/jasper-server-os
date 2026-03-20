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

import com.jaspersoft.jasperserver.api.security.EncryptionAuthenticationProcessingFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Chaim Arbiv
 * @version $id$
 * set the correct responses for the REST login.
 */
public class DefaultAuthenticationRestProcessingFilter extends EncryptionAuthenticationProcessingFilter {

    @Override
    public void afterPropertiesSet() {
// TODO: ogavavka SpringUpgrade 22.08.2016 - check if it`s ok to disable these checks
//        Assert.hasLength(getFilterProcessesUrl(), "filterProcessesUrl must be specified");
//        Assert.isTrue(UrlUtils.isValidRedirectUrl(getFilterProcessesUrl()), getFilterProcessesUrl()+ " isn't a valid redirect URL");

        Assert.notNull(getAuthenticationManager(), "authenticationManager must be specified");
    }

}

