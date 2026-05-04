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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.owasp.csrfguard.CsrfGuard;
import org.springframework.mock.web.MockHttpSession;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.InputStream;
import java.util.Properties;

import static com.jaspersoft.jasperserver.war.SessionXssNonceSetterFilter.XSS_NONCE_ATTRIB_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

/**
 * @author dlitvak
 * @version $Id$
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionXssNonceSetterFilterTest {

    private SessionXssNonceSetterFilter filter = new SessionXssNonceSetterFilter();

    private HttpSession sessionMock = new MockHttpSession();

    @Mock
    private HttpServletRequest requestMock;
    @Mock
    private HttpServletResponse responseMock;
    @Mock
    private FilterChain filterChainMock;

    @BeforeClass
    public static void setup() throws Exception {
        //load CsrfGuard properties
        InputStream is = SessionXssNonceSetterFilterTest.class.getClassLoader().getResourceAsStream("jrs.csrfguard.sessionxssnonce.test.properties");
        Properties properties = new Properties();
        properties.load(is);
        CsrfGuard.load(properties);
    }

    @Before
    public void setupTest() {
        when(requestMock.getSession(anyBoolean())).thenReturn(sessionMock);
    }

    @Test
    public void testRandomTokenIsGeneratedOncePerSession() throws Exception {
        filter.doFilter(requestMock, responseMock, filterChainMock);
        Object token1 = sessionMock.getAttribute(XSS_NONCE_ATTRIB_NAME);
        filter.doFilter(requestMock, responseMock, filterChainMock);
        Object token2 = sessionMock.getAttribute(XSS_NONCE_ATTRIB_NAME);

        assertNotNull("XSS Nonce is null", token1 );
        assertNotNull("XSS Nonces is null", token2 );
        assertTrue("XSS Nonces must be strings", token1 instanceof String && token2 instanceof String);
        assertEquals("XSS Nonce was generated more than once per session.", token1, token2);
    }

    @Test
    public void testRandomTokensAreUniquePerSession() throws Exception {
        filter.doFilter(requestMock, responseMock, filterChainMock);
        Object token1 = sessionMock.getAttribute(XSS_NONCE_ATTRIB_NAME);
        sessionMock.removeAttribute(XSS_NONCE_ATTRIB_NAME);
        filter.doFilter(requestMock, responseMock, filterChainMock);
        Object token2 = sessionMock.getAttribute(XSS_NONCE_ATTRIB_NAME);

        assertNotNull("XSS Nonce is null", token1 );
        assertNotNull("XSS Nonces is null", token2 );
        assertTrue("XSS Nonces must be strings", token1 instanceof String && token2 instanceof String);
        assertTrue("XSS Nonces are not random", !token1.equals(token2));
    }
}

