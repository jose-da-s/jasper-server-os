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

package com.jaspersoft.jasperserver.war.httpheaders;

import com.google.common.collect.Lists;
import com.jaspersoft.jasperserver.war.httpheaders.ResourceHTTPHeadersFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;

/**
 * Created by schubar on 11/18/15.
 */
@RunWith(MockitoJUnitRunner.class)
abstract public class BaseGetHttpHeaderFilterTest {

    @Mock
    protected FilterConfig config;

    @Mock
    protected HttpServletRequest request;

    @Spy
    protected HttpServletResponse response = new MockHttpServletResponse();

    @Spy
    protected ResourceHTTPHeadersFilter filter = new ResourceHTTPHeadersFilter();

    private MockFilterChain chainTrigger;
    private TestServlet testServlet;

    @Before
    public void setUp() {
        this.request = mock(HttpServletRequest.class);
        this.testServlet = new TestServlet();
        this.chainTrigger = new MockFilterChain(testServlet, this.filter);
    }

    protected HeadersRule rule(HttpMethod m, String urlPattern, Header ... headers) {
        return new HeadersRule(m.toString(), Pattern.compile(urlPattern), asList(headers));
    }

    protected Header header(String k, String v) {
        return new BasicHeader(k, v);
    }

    protected Header jrsExpires(long v) {
        JRSExpiresHeader header = spy(new JRSExpiresHeader(v));

        when(header.getCurrentDate()).thenReturn(new Date(1448073680831L));

        return header;
    }

    protected void givenConfig(HeadersRule... rules) throws ServletException {
        filter.setHeadersRules(asList(rules));
        filter.init(config);
    }

    protected void whenFilter(HttpMethod method, String servletPath, String pathInfo) throws IOException, ServletException {
        when(this.request.getMethod()).thenReturn(method.toString());
        when(this.request.getServletPath()).thenReturn(servletPath);
        when(this.request.getPathInfo()).thenReturn(pathInfo);

        chainTrigger.doFilter(request, response);
    }

    protected void thenHeader(String name, String value) {
        verify(this.response).setHeader(name, value);
    }

    protected void thenNoHeader(String name, String value) {
        verify(this.response, times(0)).setHeader(name, value);
    }

    class TestServlet extends HttpServlet {
        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.flushBuffer();
        }
    }
}

