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

package com.jaspersoft.jasperserver.war.action;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.SharedAttributeMap;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class contains common ad hoc mocking code which sets up the HttpSession so it will work correctly with the SessionAttributeManager.
 * If you want to put mocks into the session for Ad Hoc to use, use SessionAttributeManager.getInstance() and call its methods.
 * There was a bunch of code that reached into the session directly and broke after some SAM refactoring--this has been removed.
 * @author bob
 * @version $Id$
 *
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class BaseHttpSessionAwareTest  {

    @Mock
    private HttpSession httpSessionMock;
    @Mock
    private ServletExternalContext servletExternalContextMock;
    @Mock
    private SharedAttributeMap<Object> sharedAttributeMapMock;
    // preserve session map across invocations
    private final Map<String, Object> sessionMap = new HashMap<>();

    // set up a mock HttpSession on an HttpServletRequest which wraps a map
    // NOTE: to support the SessionAttributeManager correctly, call this method, then go through the SessionAttributeManager to set session attributes
    // DON'T go directly into the session itself
    protected void prepareHttpSession(HttpServletRequest requestMock) {
        // use map to hold session stuff
    	// we can call this multiple times if we want to change other request behavior, but we will keep the same sessionMap
        when(httpSessionMock.getAttribute(any())).thenAnswer(invocation -> {
            String name = invocation.getArgument(0);
            return sessionMap.get(name);
        });

        doAnswer(invocation -> {
            sessionMap.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(httpSessionMock).setAttribute(any(), any());

        lenient().doAnswer(invocation -> {
            String name = invocation.getArgument(0);
            sessionMap.remove(name);
            return null;
        }).when(httpSessionMock).removeAttribute(any());
        when(httpSessionMock.getServletContext()).thenReturn(mock(ServletContext.class));
        when(requestMock.getSession()).thenReturn(httpSessionMock);
    }
    
    protected void prepareHttpSession(HttpServletRequest requestMock, RequestContext reqContextMock) {
        prepareHttpSession(requestMock);
        lenient().when(servletExternalContextMock.getNativeRequest()).thenReturn(requestMock);
        lenient().when(servletExternalContextMock.getRequestMap()).thenReturn(new LocalAttributeMap<>());
        lenient().when(servletExternalContextMock.getSessionMap()).thenReturn(sharedAttributeMapMock);
        when(reqContextMock.getExternalContext()).thenReturn(servletExternalContextMock);
    }

    protected Map<String, Object> getSessionMap() {
    	return sessionMap;
    }
}