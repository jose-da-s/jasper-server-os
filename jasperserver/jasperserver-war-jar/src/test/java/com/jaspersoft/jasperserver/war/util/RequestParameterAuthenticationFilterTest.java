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
/*
 * debasish
 */

package com.jaspersoft.jasperserver.war.util;

import static org.junit.Assert.assertEquals;
import com.jaspersoft.jasperserver.api.security.SecurityConfiguration;

import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.security.core.context.SecurityContextHolder;

import com.jaspersoft.jasperserver.api.common.util.spring.UsernamePasswordAuthenticationParameterConfiguration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.mock.web.MockHttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;

@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("com.jaspersoft.jasperserver.api.security.SecurityConfiguration")
@PrepareForTest({ SecurityConfiguration.class, SecurityContextHolder.class})
@PowerMockIgnore( {"javax.management.*", "org.w3c.dom.*", "org.apache.log4j.*", "org.xml.sax.*",
  "javax.xml.*", "javax.script.*", "javax.security.*", "java.lang.Object", "java.lang.reflect.*"})
public class RequestParameterAuthenticationFilterTest {
  @InjectMocks
  private RequestParameterAuthenticationFilter requestParameterAuthenticationFilter;
  @Mock
  private UsernamePasswordAuthenticationParameterConfiguration parameterConfiguration;
  private MockHttpServletRequest requestMock;

  private List<Map<String, String>> authParameters = new ArrayList<Map<String, String>>(){{
    add(new HashMap<String, String>() {{
      put("usernameParameter","j_username");put("passwordParameter","j_password");
    }});
    add(new HashMap<String, String>() {{
      put("usernameParameter","username");put("passwordParameter","password");
    }});
  }};
  private Authentication authentication = Mockito.mock(Authentication.class);
  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(parameterConfiguration.getAuthParameters()).thenReturn(authParameters);
    PowerMockito.mockStatic(SecurityConfiguration.class);
    SecurityContextHolder securityContextHolder = PowerMockito.mock(SecurityContextHolder.class);
    securityContextHolder.getContext().setAuthentication(authentication);
  }

  @Test
  public void requiresAuthenticationTest(){
    requestMock = new MockHttpServletRequest();
    requestMock.setParameter("username", "superuser");
    requestMock.setParameter("password", "superuser");
    assertEquals(requestParameterAuthenticationFilter.requiresAuthentication(requestMock),true);
  }

  @Test
  public void requiresAuthenticationForEmptyCredentialTest(){
    requestMock = new MockHttpServletRequest();
    assertEquals(requestParameterAuthenticationFilter.requiresAuthentication(requestMock),false);
  }

  @Test
  public void requiresAuthenticationForAuthenticatedTest(){
    requestMock = new MockHttpServletRequest();
    requestMock.setParameter("username", "superuser");
    requestMock.setParameter("password", "superuser");
    Mockito.when(authentication.isAuthenticated()).thenReturn(true);
    Mockito.when(authentication.getName()).thenReturn("superuser");
    assertEquals(requestParameterAuthenticationFilter.requiresAuthentication(requestMock),false);
  }
}
