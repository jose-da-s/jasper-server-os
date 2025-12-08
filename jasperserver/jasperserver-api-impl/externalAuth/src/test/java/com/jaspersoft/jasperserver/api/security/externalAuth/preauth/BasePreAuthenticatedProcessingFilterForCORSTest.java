/*
 * Copyright (C) 2025 the Jasper Server OS Authors
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

package com.jaspersoft.jasperserver.api.security.externalAuth.preauth;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import com.jaspersoft.jasperserver.api.common.crypto.DevelopmentPlainTextNonCipher;
import com.jaspersoft.jasperserver.api.common.util.AuthFilterConstants;
import com.jaspersoft.jasperserver.api.security.externalAuth.ExternalDataSynchronizer;
import com.jaspersoft.jasperserver.api.security.externalAuth.wrappers.spring.JSProviderManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ SecurityContextHolder.class })
@PowerMockIgnore( {"javax.management.*", "org.w3c.dom.*", "org.apache.log4j.*", "org.xml.sax.*",   "javax.xml.*",  "javax.script.*",  "javax.security.*"})
public class BasePreAuthenticatedProcessingFilterForCORSTest {

    @InjectMocks
    BasePreAuthenticatedProcessingFilter currentFilter;

    @Mock
    SecurityContextHolder contextHolder;

    @Mock
    SecurityContext securityContext;

    @Mock
    Authentication authentication;

    @Mock
    JSProviderManager authManager;

    @Mock
    PreAuthenticatedAuthenticationToken authToken;

    @Mock
    ExternalDataSynchronizer extDataSynchronizer;

    @Mock
    FilterChain chain;

    private MockHttpServletRequest request;
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = (filterRequest, filterResponse) -> {
    };


    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(SecurityContextHolder.class);
        setupRequest();
        setFilterAttributes();
        setProviderManager();
    }

    private void setupRequest() {
        request = new MockHttpServletRequest(HttpMethod.POST.name(), "https://jaspersoft.com/test.html");
        request.addHeader("Accept", "application/json");
        request.setContextPath("/jasperserver-pro");
        request.setRequestURI("/jasperserver-pro");
        request.setParameter("pp", "u=test1|o=organization_3|r=test");
    }

    private void setFilterAttributes() {
        currentFilter.setCheckForPrincipalChanges(true);
        currentFilter.setTokenInRequestParam(true);
        DevelopmentPlainTextNonCipher tokenDecryptor = new DevelopmentPlainTextNonCipher();
        currentFilter.setTokenDecryptor(tokenDecryptor);
        currentFilter.setAuthenticationManager(authManager);
        currentFilter.setExternalDataSynchronizer(extDataSynchronizer);
    }

    private void setProviderManager() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        PowerMockito.when(SecurityContextHolder.getContext()).thenReturn(securityContext);
        PowerMockito.when(SecurityContextHolder.createEmptyContext()).thenReturn(securityContext);

        // Fix for NullPointerException - ensure getName() never returns null
        Mockito.when(authentication.getName()).thenReturn("test");
        Mockito.when(authentication.getPrincipal()).thenReturn("test");

        // Setup authToken mock as well to prevent NPE
        Mockito.when(authToken.getName()).thenReturn("test1");
        Mockito.when(authToken.getPrincipal()).thenReturn("test1");

        Mockito.when(authManager.authenticate(Mockito.any())).thenReturn(authToken);
        Mockito.doNothing().when(extDataSynchronizer).synchronize();
    }

    @Test
    public void shouldContinueRedirectforNonXRemoteRequests() throws Exception {
        currentFilter.doFilter(request, response, filterChain);
        assertEquals(response.getStatus(), HttpServletResponse.SC_FOUND);
    }

    @Test
    public void shouldContinueFilterforXRemoteRequests() throws Exception {
        request.addHeader(AuthFilterConstants.X_REMOTE_DOMAIN, "test");
        currentFilter.doFilter(request, response, filterChain);
        assertEquals(request.getAttribute(AuthFilterConstants.AUTH_FLOW_CONST), "true");
    }

}
