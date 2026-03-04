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

import com.jaspersoft.jasperserver.api.JSException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.execution.RequestContext;

import java.util.HashSet;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Sergey Prilukin
 * @version $Id:$
 */
@RunWith(MockitoJUnitRunner.class)
public class ReportExporterActionTest {

    @InjectMocks
    private ReportExporterAction reportExporterAction;

    private RequestContext requestContextMock = mock(RequestContext.class);
    private ParameterMap parameterMapMock = mock(ParameterMap.class);

    @Mock
    private Map configuredExporters;

    @Mock
    private MessageSource messageSource;

    /**
     * If exported type not supported ensure that
     * JSException is thrown
     *
     * @throws Exception
     */
    @Test(expected = JSException.class)
    public void ensureExceptionThrownWhenExportTypeNotSupported() throws Exception{

        when(messageSource.getMessage(
                eq(ReportExporterAction.EXPORT_TYPE_NOT_SUPPORTED_MESSAGE_KEY), any(), any())).thenReturn("Exception message");
        when(configuredExporters.containsKey("xml")).thenReturn(false);
        when(configuredExporters.keySet()).thenReturn(new HashSet());

        when(parameterMapMock.get(ReportExporterAction.OUTPUT)).thenReturn("xml");
        when(requestContextMock.getRequestParameters()).thenReturn(parameterMapMock);
        reportExporterAction.exportOptions(requestContextMock);
    }
}
