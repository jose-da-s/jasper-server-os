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

package com.jaspersoft.jasperserver.api.metadata.user.service.impl;

import com.jaspersoft.jasperserver.api.metadata.user.service.impl.CreateExecutionApplicationEvent.ExecutionType;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@RunWith(MockitoJUnitRunner.class)
public class ExecutionsPublishingEventServiceImplTest {
    private static final String ID = UUID.randomUUID().toString();

    @InjectMocks
    private ExecutionsPublishingEventServiceImpl publishingEventService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @After
    public void tearDown() {
        RequestContextHolder.setRequestAttributes(null);
    }

    @Captor
    private ArgumentCaptor<CreateExecutionApplicationEvent> eventCaptor;

    @Test
    public void publishReportExecutions_noAttributes_ignorePublishing() {
        publishingEventService.publishReportExecutions(ID);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    public void publishReportExecutions_noSession_ignorePublishing() {
        mockReturnRequest();
        publishingEventService.publishReportExecutions(ID);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    public void publishReportExecutions_containsSession_publishedEvent() {
        mockReturnSession();
        publishingEventService.publishReportExecutions(ID);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        CreateExecutionApplicationEvent event = eventCaptor.getValue();
        assertEquals(ExecutionType.REPORT, event.getType());
        assertEquals(ID, event.getExecutionID());
    }

    @Test
    public void publishDashboardExecutions_noAttributes_ignorePublishing() {
        publishingEventService.publishDashboardExecutions(ID);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    public void publishDashboardExecutions_noSession_ignorePublishing() {
        mockReturnRequest();
        publishingEventService.publishDashboardExecutions(ID);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    public void publishDashboardExecutions_containsSession_publishedEvent() {
        mockReturnSession();
        publishingEventService.publishDashboardExecutions(ID);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        CreateExecutionApplicationEvent event = eventCaptor.getValue();
        assertEquals(ExecutionType.DASHBOARD, event.getType());
        assertEquals(ID, event.getExecutionID());
    }

    private HttpServletRequest mockReturnRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        return request;
    }

    private HttpSession mockReturnSession() {
        HttpSession session = mock(HttpSession.class);
        HttpServletRequest request = mockReturnRequest();
        doReturn(session).when(request).getSession(false);
        return session;
    }

}