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

package com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.provider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testng.annotations.BeforeClass;
import org.mockito.InjectMocks;
import static org.testng.Assert.assertEquals;


import jakarta.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RequestInfoProviderTest {

    @InjectMocks
    private RequestInfoProvider requestInfoProvider;

    @Mock
    private HttpServletRequest request;

    @BeforeClass
    public void setUp() throws Exception{
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void checkBaseURLWithContextRoot() throws Exception {
        String contextPath = "/jasperserver-pro";
        when(request.getContextPath()).thenReturn(contextPath);
        System.out.println(requestInfoProvider.getBaseUrl());
        assertEquals(requestInfoProvider.getBaseUrl(),contextPath+"/");
    }

    @Test
    public void checkBaseURLWithOutContextRoot() throws Exception {
        when(request.getContextPath()).thenReturn("");
        System.out.println(requestInfoProvider.getBaseUrl());
        assertEquals(requestInfoProvider.getBaseUrl(),"/");
    }
}

