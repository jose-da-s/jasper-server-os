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
package com.jaspersoft.jasperserver.api.security.externalAuth.preauth;

import org.junit.Test;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BasePreAuthenticatedProcessingFilterTest {

    BasePreAuthenticatedProcessingFilter filter = new BasePreAuthenticatedProcessingFilter();

    @Test
    public void isJsonResponseRequested_checkContextPath() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        filter.setWelcomePage("/index.htm");
        when(request.getHeader(anyString())).thenReturn("application/json");
        when(request.getRequestURI()).thenReturn("/jasperserver-pro/index.htm");
        when(request.getContextPath()).thenReturn("/jasperserver-pro");
        assertTrue(filter.isJsonResponseRequested(request));
    }
}

