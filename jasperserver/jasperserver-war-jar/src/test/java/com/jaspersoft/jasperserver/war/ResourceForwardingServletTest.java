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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.jaspersoft.jasperserver.war.ResourceForwardingServlet.FORWARD_WHITELIST_PARAM;
import static org.mockito.Mockito.*;

/**
 * Test for {@link ResourceForwardingServletTest}
 *
 * @author dlitvak
 */

@RunWith(MockitoJUnitRunner.class)
public class ResourceForwardingServletTest {

    private static final String CONFIG_WHITELIST_STR = "/scripts,/themes";
    private static final String[] CONFIG_WHITELIST_ARR = CONFIG_WHITELIST_STR.split(",");
    private static final String RUNTIME_URI_PART = "/runtime/123";
    private static final String NOT_IN_WHITELIST_URI = "/not_in_whitelist";

    private ResourceForwardingServlet resourceForwardingServlet = new ResourceForwardingServlet();

    @Mock
    private HttpServletRequest requestMock;
    @Mock
    private HttpServletResponse responseMock;

    @Mock
    private ServletConfig config;

    @Mock
    private RequestDispatcher requestDispatcher;

    @Before
    public void setup() throws Exception {
        when(config.getInitParameter(FORWARD_WHITELIST_PARAM)).thenReturn(CONFIG_WHITELIST_STR);
        when(requestMock.getServletPath()).thenReturn(RUNTIME_URI_PART);

        resourceForwardingServlet.init(config);
    }

    @Test
    public void testForwardingHappens() throws Exception {
        when(requestMock.getPathInfo()).thenReturn(CONFIG_WHITELIST_ARR[0]);
        when(requestMock.getRequestDispatcher(CONFIG_WHITELIST_ARR[0])).thenReturn(requestDispatcher);
        resourceForwardingServlet.doGet(requestMock, responseMock);

        verify(responseMock, times(0)).sendError(403);
        verify(requestDispatcher, times(1)).forward(requestMock, responseMock);
    }

    @Test
    public void testForwardingDoesNotHappen_403() throws Exception {
        when(requestMock.getPathInfo()).thenReturn(NOT_IN_WHITELIST_URI);
        resourceForwardingServlet.doGet(requestMock, responseMock);

        verify(requestDispatcher, times(0)).forward(requestMock, responseMock);
        verify(responseMock, times(1)).sendError(403);
    }


}

