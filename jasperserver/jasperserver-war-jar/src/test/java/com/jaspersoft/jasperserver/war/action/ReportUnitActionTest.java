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

import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.war.common.JasperServerConstImpl;
import com.jaspersoft.jasperserver.war.dto.ReportUnitWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Sergey Prilukin
 */
@RunWith(MockitoJUnitRunner.class)
public class ReportUnitActionTest {

    @InjectMocks
    private ReportUnitAction reportUnitAction;

    private RequestContext requestContextMock = mock(RequestContext.class);
    private ParameterMap requestParametersMock = mock(ParameterMap.class);
    private MutableAttributeMap flowScopeMock = mock(MutableAttributeMap.class);
    private ReportUnitWrapper reportUnitWrapperMock = mock(ReportUnitWrapper.class);
    private ReportUnit reportUnitMock = mock(ReportUnit.class);
    private ResourceReference resourceReferenceMock = mock(ResourceReference.class);
    private FileResource localFileResource = mock(FileResource.class);

    private FileResource fileResource = mock(FileResource.class);

    @Mock
    private RepositoryService repository;

    public static final String JRXML_FILE_NAME = "simpleReport.jrxml";

    /*
     * All checkJrxmlResourceUploadedFlag* tests for:
     * Bug 23217 - uploaded jrxml file disappers if user navigate away from Set Up Report page
     */

    @Test
    public void checkJrxmlResourceUploadedFlagWhenDataIsUploadedFromFileSystemAndRUResourceIsLocal() throws Exception {
        setUpCommonBehaviourForUploadJrxmlTests();
        setUpSourceAndLocalness(JasperServerConstImpl.getFieldChoiceFile(), true);
        setUpJrxmlDataAndResourceReferenceURI(new byte[10], null);

        Event event = reportUnitAction.uploadJRXML(requestContextMock);

        assertEquals(event.getId(), "success");
        verify(flowScopeMock, times(1)).put(ReportUnitAction.JRXML_FILE_RESOURCE_ALREADY_UPLOADED, JRXML_FILE_NAME);
        verify(reportUnitWrapperMock, times(1)).setJrxmlChanged(eq(true));
    }

    @Test
    public void checkJrxmlResourceUploadedFlagWhenDataIsUploadedFromFileSystemAndRUResourceIsNotLocal()
            throws Exception {
        setUpCommonBehaviourForUploadJrxmlTests();
        setUpSourceAndLocalness(JasperServerConstImpl.getFieldChoiceFile(), false);
        setUpJrxmlDataAndResourceReferenceURI(new byte[10], null);

        Event event = reportUnitAction.uploadJRXML(requestContextMock);

        assertEquals(event.getId(), "success");
        verify(flowScopeMock, times(1)).put(ReportUnitAction.JRXML_FILE_RESOURCE_ALREADY_UPLOADED, JRXML_FILE_NAME);
        verify(reportUnitWrapperMock, times(1)).setJrxmlChanged(eq(true));
    }

    @Test
    public void checkJrxmlResourceUploadedFlagWhenDataIsUploadedFromFileSystemAndRUResourceIsLocalNoJrxmlData()
            throws Exception {
        setUpCommonBehaviourForUploadJrxmlTests();
        setUpSourceAndLocalness(JasperServerConstImpl.getFieldChoiceFile(), true);
        setUpJrxmlDataAndResourceReferenceURI(null, null);

        Event event = reportUnitAction.uploadJRXML(requestContextMock);

        assertEquals(event.getId(), "success");
        verify(flowScopeMock, never()).put(eq(ReportUnitAction.JRXML_FILE_RESOURCE_ALREADY_UPLOADED), any());
        verify(reportUnitWrapperMock, never()).setJrxmlChanged(true);
    }

    @Test
    public void checkJrxmlResourceUploadedFlagWhenDataIsUploadedFromRepositoryAndRUResourceIsLocal()
            throws Exception {
        setUpCommonBehaviourForUploadJrxmlTests();
        setUpSourceAndLocalness("ggg", true);
        setUpJrxmlDataAndResourceReferenceURI(new byte[10], null);

        Event event = reportUnitAction.uploadJRXML(requestContextMock);

        assertEquals(event.getId(), "success");
        //TODO: check this functionality after our changes
        verify(flowScopeMock, times(1)).put(ReportUnitAction.JRXML_FILE_RESOURCE_ALREADY_UPLOADED, "");
        verify(reportUnitWrapperMock, times(1)).setJrxmlChanged(eq(true));
    }

    @Test
    public void checkJrxmlResourceUploadedFlagWhenDataIsUploadedFromRepositoryAndRUResourceIsNotLocal()
            throws Exception {
        setUpCommonBehaviourForUploadJrxmlTests();
        setUpSourceAndLocalness("ggg", false);
        setUpJrxmlDataAndResourceReferenceURI(new byte[10], "/other_uri");

        Event event = reportUnitAction.uploadJRXML(requestContextMock);

        assertEquals(event.getId(), "success");
        verify(flowScopeMock, times(1)).put(ReportUnitAction.JRXML_FILE_RESOURCE_ALREADY_UPLOADED, "");
        verify(reportUnitWrapperMock, times(1)).setJrxmlChanged(eq(true));
    }

    @Test
    public void checkJrxmlResourceUploadedFlagWhenDataIsUploadedFromRepositoryAndRUResourceIsNotLocalButSameUri()
            throws Exception {
        setUpCommonBehaviourForUploadJrxmlTests();
        setUpSourceAndLocalness("ggg", false);
        setUpJrxmlDataAndResourceReferenceURI(new byte[10], "/uri");

        Event event = reportUnitAction.uploadJRXML(requestContextMock);

        assertEquals(event.getId(), "success");
        verify(flowScopeMock, never()).put(eq(ReportUnitAction.JRXML_FILE_RESOURCE_ALREADY_UPLOADED), any());
        verify(reportUnitWrapperMock, never()).setJrxmlChanged(eq(true));
    }

    private void setUpSourceAndLocalness(String source, boolean isLocal) {
        when(reportUnitWrapperMock.getSource()).thenReturn(source);
        when(resourceReferenceMock.isLocal()).thenReturn(isLocal);
    }

    private void setUpJrxmlDataAndResourceReferenceURI(byte[] jrxmlData, String resourceReferenceUri) {
        when(reportUnitWrapperMock.getJrxmlData()).thenReturn(jrxmlData);
        when(resourceReferenceMock.getReferenceURI()).thenReturn(resourceReferenceUri);
    }

    private void setUpCommonBehaviourForUploadJrxmlTests() {
        when(reportUnitWrapperMock.getReportUnit()).thenReturn(reportUnitMock);
        when(reportUnitWrapperMock.getJrxmlUri()).thenReturn("/uri");

        // we do not ever want this to return true in this test
        // because this test not intended to cover this branch of uploadJRXML logic
        when(reportUnitWrapperMock.isJrxmlChanged()).thenReturn(false);
        when(flowScopeMock.get("wrapper")).thenReturn(reportUnitWrapperMock);
        when(requestContextMock.getFlowScope()).thenReturn(flowScopeMock);

        when(requestParametersMock.get("fileName")).thenReturn(JRXML_FILE_NAME);
        when(requestContextMock.getRequestParameters()).thenReturn(requestParametersMock);

        when(reportUnitMock.getMainReport()).thenReturn(resourceReferenceMock);
        when(reportUnitMock.getName()).thenReturn("report");
        when(resourceReferenceMock.getLocalResource()).thenReturn(localFileResource);
        when(repository.newResource(null, FileResource.class)).thenReturn(fileResource);
    }
}
