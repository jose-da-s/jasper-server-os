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
package com.jaspersoft.jasperserver.war;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

import static com.jaspersoft.jasperserver.war.RESTLoginAuthenticationFilter.LOGIN_PATH_INFO;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.web.bind.annotation.RequestMethod.OPTIONS;

/**
 * <p></p>
 *
 * @author Vlad Zavadskyi
 */
public class RESTLoginAuthenticationFilterTest {
    private final RESTLoginAuthenticationFilter filter = new RESTLoginAuthenticationFilter();

    private final HttpServletRequest request = mock(HttpServletRequest.class);

    private final HttpServletResponse response = mock(HttpServletResponse.class);

    private final PrintWriter writer = mock(PrintWriter.class);

    private final FilterChain filterChain = mock(FilterChain.class);

    private HttpSession httpSession = mock(HttpSession.class);

    @Before
    public void setUp() throws IOException {
        doReturn(writer).when(response).getWriter();
        doReturn(LOGIN_PATH_INFO).when(request).getPathInfo();
        doReturn(httpSession).when(request).getSession();
    }

    @Test
    public void doFilter_nonLoginPath_invokeNextFilter() throws Exception {
        doReturn(null).when(request).getPathInfo();

        filter.doFilter(request, response, filterChain);

        verify(request).getPathInfo();
        //verifyNoMoreInteractions(request);
        verifyNoInteractions(response);
        verify(filterChain).doFilter(eq(request), eq(response));
    }

    @Test
    public void doFilter_optionsMethod_noContentResponse() throws Exception {
        doReturn(OPTIONS.name()).when(request).getMethod();
        filter.setPostOnly(false);

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    public void doFilter_nonPostMethodWithPostOnly_unauthorizedResponse() throws Exception {
        doReturn(OPTIONS.name()).when(request).getMethod();
        filter.setPostOnly(true);

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

}