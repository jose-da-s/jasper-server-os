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

import static org.mockito.Mockito.times;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.jaspersoft.jasperserver.api.common.util.AuthFilterConstants;

public class CorsAuthRedirectionFilterTest {

    @InjectMocks
    CorsAuthRedirectionFilter  filter;

    @Mock
    ServletRequest  request;

    @Mock
    ServletResponse  response;

    @Mock
    FilterChain chain;


    private final String AuthFlow = "authFlow";


    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void testRequestWhenAuthFlowConstIsNull() throws Exception {
        filter.doFilter(request, response, chain);
        Mockito.verify(chain, times(1)).doFilter(request, response);

    }

    @Test
    public void testRequestWhenAuthFlowConstNotNull() throws Exception {
        Mockito.when(request.getAttribute(AuthFilterConstants.AUTH_FLOW_CONST)).thenReturn(AuthFlow);
        filter.doFilter(request, response, chain);
        Mockito.verify(chain, times(0)).doFilter(request, response);

    }

}

