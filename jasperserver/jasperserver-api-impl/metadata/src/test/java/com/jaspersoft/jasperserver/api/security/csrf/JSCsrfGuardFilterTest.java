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

package com.jaspersoft.jasperserver.api.security.csrf;

import org.apache.http.protocol.HTTP;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.owasp.csrfguard.CsrfGuard;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Properties;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Anton Fomin
 *
 * jrs.csrfguard.properties:
 * * only POST,PUT,DELETE requests are scanned for CSRF
 */
@RunWith(MockitoJUnitRunner.class)
public class JSCsrfGuardFilterTest {
    /*
    public static final String CSRF_TOKEN_VALUE = "12345_Token";
    public static final String AJAX_HEADER = "X-Requested-With";
    private static JSCsrfGuardFilter filter = new JSCsrfGuardFilter();

    @Mock
    HttpSession sessionMock;
    @Mock
    HttpServletRequest requestMock;
    @Mock
    HttpServletResponse responseMock;
    @Mock
    FilterChain filterChainMock;

    @BeforeClass
    public static void setup() throws Exception {
        //load CsrfGuard properties
        InputStream is = JSCsrfGuardFilterTest.class.getClassLoader().getResourceAsStream("jrs.csrfguard.test.properties");
        Properties properties = new Properties();
        properties.load(is);
        CsrfGuard.load(properties);

        filter.setProtectedUserAgentRegexs(Arrays.asList("Mozilla/.*","Opera/.*"));
    }

    @Before
    public void setupTest() {
        reset(requestMock);
        when(sessionMock.getAttribute(CsrfGuard.getInstance().getSessionKey())).thenReturn(CSRF_TOKEN_VALUE);
//        when(requestMock.getSession()).thenReturn(sessionMock);
        when(requestMock.getSession(false)).thenReturn(sessionMock);
        when(requestMock.getSession(true)).thenReturn(sessionMock);
        when(requestMock.getRequestURL()).thenReturn(new StringBuffer("testCSRF.html"));
        when(requestMock.getRequestURI()).thenReturn("testCSRF.html");
    }

    @Test
    public void testRequestWithoutUserAgentPasses() throws Exception {
//        when(requestMock.getMethod()).thenReturn("POST");

        Method doFilter = JSCsrfGuardFilter.class.getDeclaredMethod("doFilter", ServletRequest.class, ServletResponse.class, FilterChain.class);
        doFilter.invoke(filter, requestMock, responseMock, filterChainMock);

        verify(filterChainMock).doFilter(requestMock, responseMock);
    }

    @Test
    public void testRequestWithUnknownUserAgentPasses() throws Exception {
        when(requestMock.getHeader(HTTP.USER_AGENT)).thenReturn("Unknown/1.2.3");
//        when(requestMock.getMethod()).thenReturn("POST");

        Method doFilter = JSCsrfGuardFilter.class.getDeclaredMethod("doFilter", ServletRequest.class, ServletResponse.class, FilterChain.class);
        doFilter.invoke(filter, requestMock, responseMock, filterChainMock);

        verify(filterChainMock).doFilter(requestMock, responseMock);
    }

    @Test
    public void testGetRequestWithoutUserAgentPasses() throws Exception {
        when(requestMock.getHeader(HTTP.USER_AGENT)).thenReturn("Mozilla/1.2.3");
        when(requestMock.getMethod()).thenReturn("GET");

        Method doFilter = JSCsrfGuardFilter.class.getDeclaredMethod("doFilter", ServletRequest.class, ServletResponse.class, FilterChain.class);
        doFilter.invoke(filter, requestMock, responseMock, filterChainMock);

        // 2nd argument is null for 'any' object invokation.  This is because org.owasp.csrfguard.CsrfGuardFilter.doFilter() wraps response in
        // org.owasp.csrfguard.http.InterceptRedirectResponse
        verify(filterChainMock).doFilter(eq(requestMock), any());
    }

    @Test
    public void testNonAjaxRequestWithoutTokenFails() throws Exception {
        when(requestMock.getHeader(HTTP.USER_AGENT)).thenReturn("Mozilla/1.2.3");
        when(requestMock.getMethod()).thenReturn("POST");

        Method doFilter = JSCsrfGuardFilter.class.getDeclaredMethod("doFilter", ServletRequest.class, ServletResponse.class, FilterChain.class);
        doFilter.invoke(filter, requestMock, responseMock, filterChainMock);

        verify(filterChainMock, never()).doFilter(requestMock, responseMock);
    }

    @Test
    public void testNonAjaxRequestWithTokenPasses() throws Exception {
        when(requestMock.getHeader(HTTP.USER_AGENT)).thenReturn("Mozilla/1.2.3");
        when(requestMock.getMethod()).thenReturn("POST");
        when(requestMock.getParameter(CsrfGuard.getInstance().getTokenName())).thenReturn(CSRF_TOKEN_VALUE);

        Method doFilter = JSCsrfGuardFilter.class.getDeclaredMethod("doFilter", ServletRequest.class, ServletResponse.class, FilterChain.class);
        doFilter.invoke(filter, requestMock, responseMock, filterChainMock);

        // 2nd argument is null for 'any' object invokation.  This is because org.owasp.csrfguard.CsrfGuardFilter.doFilter() wraps response in
        // org.owasp.csrfguard.http.InterceptRedirectResponse
        verify(filterChainMock).doFilter(eq(requestMock), any());
    }

    @Test
    public void testAjaxRequestWithoutTokenFails() throws Exception {
        when(requestMock.getHeader(HTTP.USER_AGENT)).thenReturn("Mozilla/1.2.3");
        when(requestMock.getMethod()).thenReturn("POST");
        when(requestMock.getHeader(AJAX_HEADER)).thenReturn("XmlHttpRequest");

        Method doFilter = JSCsrfGuardFilter.class.getDeclaredMethod("doFilter", ServletRequest.class, ServletResponse.class, FilterChain.class);
        doFilter.invoke(filter, requestMock, responseMock, filterChainMock);

        verify(filterChainMock, never()).doFilter(requestMock, responseMock);
    }

    @Test
    public void testAjaxRequestWithTokenPasses() throws Exception {
        when(requestMock.getHeader(HTTP.USER_AGENT)).thenReturn("Mozilla/1.2.3");
        when(requestMock.getMethod()).thenReturn("POST");
        when(requestMock.getHeader(AJAX_HEADER)).thenReturn("XmlHttpRequest");
        when(requestMock.getHeader(CsrfGuard.getInstance().getTokenName())).thenReturn(CSRF_TOKEN_VALUE);

        Method doFilter = JSCsrfGuardFilter.class.getDeclaredMethod("doFilter", ServletRequest.class, ServletResponse.class, FilterChain.class);
        doFilter.invoke(filter, requestMock, responseMock, filterChainMock);

        // 2nd argument is null for 'any' object invokation.  This is because org.owasp.csrfguard.CsrfGuardFilter.doFilter() wraps response in
        // org.owasp.csrfguard.http.InterceptRedirectResponse
        verify(filterChainMock).doFilter(eq(requestMock), any());
    }
*/
}
