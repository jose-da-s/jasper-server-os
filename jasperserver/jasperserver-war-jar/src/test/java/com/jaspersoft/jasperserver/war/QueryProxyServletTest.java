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

import com.jaspersoft.jasperserver.api.metadata.user.domain.client.UserImpl;
import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.client.MetadataUserDetails;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.message.BasicHttpRequest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.InputStream;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static com.jaspersoft.jasperserver.war.SessionXssNonceSetterFilter.XSS_NONCE_ATTRIB_NAME;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

/**
 * @author schubar
 * @version $Id $
 */
//@RunWith(MockitoJUnitRunner.class)
public class QueryProxyServletTest {

    private QueryProxyServlet proxyServlet = new QueryProxyServlet();

    private HttpSession sessionMock = new MockHttpSession();

    @Mock
    private HttpServletRequest requestMock;
    @Mock
    private HttpServletResponse responseMock;

    private UserImpl user = new UserImpl() {{
        setUsername("joeuser");
        setPassword("joeuserpwd");
    }};

    private TestingAuthenticationToken auth = new TestingAuthenticationToken(new MetadataUserDetails(user), null);


    //@Before
    public void setupTest() {
//        when(requestMock.getSession(anyBoolean())).thenReturn(sessionMock);
        when(requestMock.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
//        when(requestMock.getPathInfo()).thenReturn(NOT_IN_WHITELIST_URI);
    }

    //@Test
    public void shouldGetCredentials() throws Exception {

        String credentials = proxyServlet.getCredentials(auth);

        assertNotNull(credentials);
        assertEquals("joeuser:joeuserpwd", credentials);
    }

    //@Test
    public void shouldCreateAuthHeader() throws Exception {
        Authentication prevAuthentication = SecurityContextHolder.getContext().getAuthentication();

        try {
            SecurityContextHolder.getContext().setAuthentication(auth);
            Optional<Header> header = proxyServlet.basicAuth();

            assertTrue(header.isPresent());
            assertEquals("Authorization", header.get().getName());
            assertEquals("Basic am9ldXNlcjpqb2V1c2VycHdk", header.get().getValue());
        } finally {
            SecurityContextHolder.getContext().setAuthentication(prevAuthentication);

        }
    }

    //@Test
    public void shouldCopyRequestHeaders() throws Exception {
        Authentication prevAuthentication = SecurityContextHolder.getContext().getAuthentication();
        BasicHttpRequest proxyRequest = new BasicHttpRequest("POST", "http://localhost:8080/jasperserver-pro/rest_v2/queryExecutions");

        try {
            SecurityContextHolder.getContext().setAuthentication(auth);
//            proxyServlet.copyRequestHeaders(requestMock, proxyRequest);

            assertEquals(2, proxyRequest.getAllHeaders().length);
            assertEquals("Basic am9ldXNlcjpqb2V1c2VycHdk", proxyRequest.getLastHeader("Authorization").getValue());
            assertTrue(proxyRequest.getLastHeader("X-Execution-ID").getValue().length() == 36);
        } finally {
            SecurityContextHolder.getContext().setAuthentication(prevAuthentication);

        }
    }

    //@Test
    public void shouldCopyRequestHeadersForData() throws Exception {
        Authentication prevAuthentication = SecurityContextHolder.getContext().getAuthentication();
        BasicHttpRequest proxyRequest = new BasicHttpRequest(
                "POST", "http://localhost:8080/jasperserver-pro/rest_v2/queryExecutions/2c8608bc-93b6-402b-824e-99ca1da43c4f/data");

        try {
            SecurityContextHolder.getContext().setAuthentication(auth);
//            proxyServlet.copyRequestHeaders(requestMock, proxyRequest);

            assertEquals(2, proxyRequest.getAllHeaders().length);
            assertEquals("Basic am9ldXNlcjpqb2V1c2VycHdk", proxyRequest.getLastHeader("Authorization").getValue());
            assertEquals("2c8608bc-93b6-402b-824e-99ca1da43c4f", proxyRequest.getLastHeader("X-Execution-ID").getValue());
        } finally {
            SecurityContextHolder.getContext().setAuthentication(prevAuthentication);

        }
    }

}
