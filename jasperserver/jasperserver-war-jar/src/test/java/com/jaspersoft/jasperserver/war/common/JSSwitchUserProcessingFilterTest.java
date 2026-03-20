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

package com.jaspersoft.jasperserver.war.common;

import com.jaspersoft.jasperserver.api.logging.audit.context.AuditContext;
import com.jaspersoft.jasperserver.api.logging.audit.domain.AuditEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.switchuser.SwitchUserGrantedAuthority;
import org.springframework.web.servlet.ThemeResolver;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * @author Andriy Tivodar <ativodar@tibco>
 * @version $Id$
 */
public class JSSwitchUserProcessingFilterTest {
    private static final String USERNAME = "username";
    private static final String TENANT_ID = "tenant_id";
    private static final String USERNAME_PARAM = "usernameParameter";
    private static final String TARGET_URL = "/targetUrl";
    private static final String TARGET_EXIT_URL = "/exitTargetUrl";
    private static final String REQUEST_URL = "/requestUrl";
    private static final String SWITCH_URL = "/switchUrl";
    private static final String EXIT_USER_URL = "/exitUserUrl";
    private static final String CONTEXT_PATH = "path";

    @InjectMocks
    private JSSwitchUserProcessingFilter objectUnderTest;
    @Mock
    private AuditContext auditContext;
    @Mock
    private ThemeResolver themeResolver;

    private HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
    private HttpServletResponse servletResponse = Mockito.mock(HttpServletResponse.class);
    private FilterChain filterChain = Mockito.mock(FilterChain.class);
    private UserDetailsService userDetailsService = Mockito.mock(UserDetailsService.class);
    private UserDetails userDetails = Mockito.mock(UserDetails.class);
    private UserDetailsChecker userDetailsChecker = Mockito.mock(UserDetailsChecker.class);
    private AuditEvent auditEvent = Mockito.mock(AuditEvent.class);
    private Authentication authentication = Mockito.mock(Authentication.class);
    private Authentication previousAuthentication;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        objectUnderTest.setTargetUrl(TARGET_URL);
        objectUnderTest.setExitTargetUrl(TARGET_EXIT_URL);
        objectUnderTest.setUserDetailsService(userDetailsService);
        objectUnderTest.setUsernameParameter(USERNAME_PARAM);
        objectUnderTest.setSwitchUserUrl(SWITCH_URL);
        objectUnderTest.setExitUserUrl(EXIT_USER_URL);
        objectUnderTest.setUserDetailsChecker(userDetailsChecker);

        doReturn(USERNAME).when(auditEvent).getUsername();
        doReturn(TENANT_ID).when(auditEvent).getTenantId();
        doReturn(SWITCH_URL).when(servletResponse).encodeRedirectURL(CONTEXT_PATH + TARGET_URL);
        doReturn(EXIT_USER_URL).when(servletResponse).encodeRedirectURL(CONTEXT_PATH + TARGET_EXIT_URL);
        doReturn(userDetails).when(userDetailsService).loadUserByUsername(USERNAME);
        doReturn(REQUEST_URL).when(servletRequest).getRequestURI();
        doReturn(USERNAME).when(servletRequest).getParameter(USERNAME_PARAM);
        doReturn(CONTEXT_PATH).when(servletRequest).getContextPath();
        doReturn(singleton(new SwitchUserGrantedAuthority("role", authentication))).when(authentication).getAuthorities();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Exception {
                ((AuditContext.AuditContextCallback) invocationOnMock.getArguments()[0]).execute();
                return null;
            }
        }).when(auditContext).doInAuditContext(any(AuditContext.AuditContextCallback.class));
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Exception {
                ((AuditContext.AuditContextCallbackWithEvent) invocationOnMock.getArguments()[1]).execute(auditEvent);
                return null;
            }
        }).when(auditContext).doInAuditContext(org.mockito.AdditionalMatchers.or(eq("switchUser"), eq("exitSwitchedUser")), any(AuditContext.AuditContextCallbackWithEvent.class));

        previousAuthentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void release() {
        SecurityContextHolder.getContext().setAuthentication(previousAuthentication);
    }

    @Test
    public void getAndSet_instanceWithDefaultValues() {
        JSSwitchUserProcessingFilter instance = new JSSwitchUserProcessingFilter();

        assertNull(instance.getThemeResolver());
    }

    @Test
    void getAndSet_fullyConfiguredInstance() {
        ThemeResolver themeResolver = Mockito.mock(ThemeResolver.class);

        JSSwitchUserProcessingFilter instance = new JSSwitchUserProcessingFilter();
        instance.setThemeResolver(themeResolver);

        assertSame(themeResolver, instance.getThemeResolver());
    }

    @Test
    public void afterPropertiesSet_jsTargetUrlIsNull_illegalArgumentException() {
        objectUnderTest.setTargetUrl(null);

        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                objectUnderTest.afterPropertiesSet();
            }
        });
    }

    @Test
    public void afterPropertiesSet_exitTargetUrlIsNull_illegalArgumentException() {
        objectUnderTest.setExitTargetUrl(null);

        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                objectUnderTest.afterPropertiesSet();
            }
        });
    }

    @Test
    public void afterPropertiesSet_userDetailsServiceIsNull_illegalArgumentException() {
        objectUnderTest.setUserDetailsService(null);

        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                objectUnderTest.afterPropertiesSet();
            }
        });
    }

    @Test
    public void doFilter_userExitOrSwitchIsNotRequired_nextFilterIsInvokedWithRequest() throws IOException, ServletException {
        objectUnderTest.doFilter(servletRequest, servletResponse, filterChain);
        verify(filterChain).doFilter(servletRequest, servletResponse);
    }

    @Test
    public void doFilter_userSwitchIsRequired_userIsSwitched() throws IOException, ServletException {
        doReturn(CONTEXT_PATH + SWITCH_URL).when(servletRequest).getRequestURI();

        objectUnderTest.doFilter(servletRequest, servletResponse, filterChain);

        verify(auditContext).createAuditEvent("switchUser");
        verify(auditContext).addPropertyToAuditEvent("username", null, auditEvent);
        verify(auditContext).addPropertyToAuditEvent("organization", "", auditEvent);
        verify(servletResponse).sendRedirect(SWITCH_URL);
        verify(auditContext).closeAuditEvent(auditEvent);
        verify(themeResolver).setThemeName(servletRequest, servletResponse, null);
    }

    @Test
    public void doFilter_userExitIsRequired_userIsExited() throws IOException, ServletException {
        doReturn(CONTEXT_PATH + EXIT_USER_URL).when(servletRequest).getRequestURI();

        objectUnderTest.doFilter(servletRequest, servletResponse, filterChain);

        verify(auditContext).createAuditEvent("exitSwitchedUser");
        verify(auditContext).addPropertyToAuditEvent("username", USERNAME, auditEvent);
        verify(auditContext).addPropertyToAuditEvent("organization", TENANT_ID, auditEvent);
        verify(servletResponse).sendRedirect(EXIT_USER_URL);
        verify(auditContext).closeAuditEvent(auditEvent);
        verify(themeResolver).setThemeName(servletRequest, servletResponse, null);
    }

    @Test
    public void doFilter_sendRedirectThrowsIOException_IOException() throws IOException {
        doReturn(CONTEXT_PATH + SWITCH_URL).when(servletRequest).getRequestURI();
        doThrow(new IOException()).when(servletResponse).sendRedirect(SWITCH_URL);

        assertThrows(IOException.class, new Executable() {
            @Override
            public void execute() throws IOException, ServletException {
                objectUnderTest.doFilter(servletRequest, servletResponse, filterChain);
            }
        });
    }

    @Test
    public void doFilter_chainDoFilterThrowsServletException_servletException() throws IOException, ServletException {
        doThrow(new ServletException()).when(filterChain).doFilter(servletRequest, servletResponse);

        assertThrows(ServletException.class, new Executable() {
            @Override
            public void execute() throws IOException, ServletException {
                objectUnderTest.doFilter(servletRequest, servletResponse, filterChain);
            }
        });
    }
}

