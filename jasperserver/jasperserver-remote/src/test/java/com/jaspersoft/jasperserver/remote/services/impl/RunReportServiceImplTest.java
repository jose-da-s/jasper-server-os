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

package com.jaspersoft.jasperserver.remote.services.impl;

import com.jaspersoft.jasperserver.api.common.error.handling.SecureExceptionHandler;
import com.jaspersoft.jasperserver.api.engine.common.service.EngineService;
import com.jaspersoft.jasperserver.api.engine.common.service.ReportExecutionStatusInformation;
import com.jaspersoft.jasperserver.api.engine.common.service.VirtualizerFactory;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.user.domain.TenantQualified;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.ExecutionsPublishingEventService;
import com.jaspersoft.jasperserver.dto.executions.ExecutionStatus;
import com.jaspersoft.jasperserver.dto.reports.inputcontrols.ReportInputControl;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlUITypeMapper;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlsLogicService;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlsValidationException;
import com.jaspersoft.jasperserver.remote.exception.ErrorDescriptorBuildingService;
import com.jaspersoft.jasperserver.remote.exception.ResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.reports.HtmlExportStrategy;
import com.jaspersoft.jasperserver.remote.services.ReportExecution;
import com.jaspersoft.jasperserver.remote.services.ReportExecutionOptions;
import com.jaspersoft.jasperserver.remote.services.ReportExecutor;
import com.jaspersoft.jasperserver.remote.utils.AuditHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.Cache;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static com.jaspersoft.jasperserver.api.metadata.common.domain.InputControl.TYPE_MULTI_SELECT_LIST_OF_VALUES;
import static com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.InputControlHandler.NOTHING_SUBSTITUTION_VALUE;
import static com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.InputControlHandler.NULL_SUBSTITUTION_VALUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(locations = {
        "classpath:applicationContext*.xml",
})
@ActiveProfiles("test")
public class RunReportServiceImplTest extends AbstractTestNGSpringContextTests {

    private static final String REQUEST_ID = "a6dd37aa-57e0-4410-9329-0df6770ce948";

    @InjectMocks
    private RunReportServiceImpl service;
    @Mock
    private AuditHelper auditHelper;
    @Mock
    private EngineService engine;
    @Mock
    private EngineService unsecuredEngine;
    @Mock
    private Executor asyncExecutor;
    @Mock
    private InputControlsLogicService inputControlsLogicService;
    @Mock
    private ReportExecutor reportExecutor;
    @Mock
    private RepositoryService repositoryService;
    @Mock
    private ErrorDescriptorBuildingService errorDescriptorBuildingService;
    @Mock
    private Map<String, HtmlExportStrategy> htmlExportStrategies;
    @Mock
    private HtmlExportStrategy defaultHtmlExportStrategy;
    @Mock
    private VirtualizerFactory virtualizerFactory;
    @Mock
    private SecureExceptionHandler secureExceptionHandler;
    @Mock
    private RunReportServiceCacheFactoryBean cacheFactoryBean;
    @Mock
    private ExecutionsPublishingEventService publishingEventService;

    private final Cache cache = mock(Cache.class);

    private List<ReportInputControl> controlsCascade = new ArrayList<>();

    @BeforeClass
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeMethod
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);

        controlsCascade = new ArrayList<>();

        controlsCascade.add(new ReportInputControl().setId("a"));
        controlsCascade.add(new ReportInputControl().setId("b").setMasterDependencies(Arrays.asList("a")));

        doReturn(cache).when(cacheFactoryBean).getObject();
    }

    @AfterMethod
    public void afterEach() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(groups = {"verifyCorrectParameterValuesForNonCascadingControls"})
    public void verifyCorrectParameterValuesForNonCascadingControls_SingleNothing() throws Exception {
        Map<String, String[]> initial = new HashMap<String, String[]>(), processed = new HashMap<String, String[]>();

        initial.put("a", new String[]{"a 1"});

        processed.put("a", new String[]{"a 1"});

        service.verifyCorrectParameterValuesForNonCascadingControls(controlsCascade, initial, processed);
    }

    @Test(groups = {"verifyCorrectParameterValuesForNonCascadingControls"}, expectedExceptions = {InputControlsValidationException.class})
    public void verifyCorrectParameterValuesForNonCascadingControls_SingleChanged_Exception() throws Exception {
        Map<String, String[]> initial = new HashMap<String, String[]>(), processed = new HashMap<String, String[]>();

        initial.put("a", new String[]{"a 1"});

        processed.put("a", new String[]{"a 2"});

        service.verifyCorrectParameterValuesForNonCascadingControls(controlsCascade, initial, processed);
    }

    @Test(groups = {"verifyCorrectParameterValuesForNonCascadingControls"}, expectedExceptions = {InputControlsValidationException.class})
    public void verifyCorrectParameterValuesForNonCascadingControls_SingleChangedChild_Exception() throws Exception {
        Map<String, String[]> initial = new HashMap<String, String[]>(), processed = new HashMap<String, String[]>();

        initial.put("b", new String[]{"b 1"});

        processed.put("b", new String[]{"b 2"});

        service.verifyCorrectParameterValuesForNonCascadingControls(controlsCascade, initial, processed);
    }

    @Test(groups = {"verifyCorrectParameterValuesForNonCascadingControls"}, expectedExceptions = {InputControlsValidationException.class})
    public void verifyCorrectParameterValuesForNonCascadingControls_SingleChangedByAdding_Exception() throws Exception {
        Map<String, String[]> initial = new HashMap<String, String[]>(), processed = new HashMap<String, String[]>();

        initial.put("a", new String[]{"a 1"});

        processed.put("a", new String[]{"a 1", "a 2"});

        service.verifyCorrectParameterValuesForNonCascadingControls(controlsCascade, initial, processed);
    }

    @Test(groups = {"verifyCorrectParameterValuesForNonCascadingControls"}, expectedExceptions = {InputControlsValidationException.class})
    public void verifyCorrectParameterValuesForNonCascadingControls_SingleChangedByRemoving_Exception() throws Exception {
        Map<String, String[]> initial = new HashMap<String, String[]>(), processed = new HashMap<String, String[]>();

        initial.put("a", new String[]{"a 1"});

        processed.put("a", new String[]{});

        service.verifyCorrectParameterValuesForNonCascadingControls(controlsCascade, initial, processed);
    }

    @Test(groups = {"verifyCorrectParameterValuesForNonCascadingControls"})
    public void verifyCorrectParameterValuesForNonCascadingControls_SingleUnknown() throws Exception {
        Map<String, String[]> initial = new HashMap<String, String[]>(), processed = new HashMap<String, String[]>();

        initial.put("c", new String[]{"c 1"});

        processed.put("c", new String[]{"c 1"});

        service.verifyCorrectParameterValuesForNonCascadingControls(controlsCascade, initial, processed);
    }

    @Test(groups = {"verifyCorrectParameterValuesForNonCascadingControls"})
    public void verifyCorrectParameterValuesForNonCascadingControls_CascadeNothing() throws Exception {
        Map<String, String[]> initial = new HashMap<String, String[]>(), processed = new HashMap<String, String[]>();

        initial.put("a", new String[]{"a 1"});
        initial.put("b", new String[]{"b 1"});

        processed.put("a", new String[]{"a 1"});
        processed.put("b", new String[]{"b 1"});

        service.verifyCorrectParameterValuesForNonCascadingControls(controlsCascade, initial, processed);
    }

    @Test(groups = {"verifyCorrectParameterValuesForNonCascadingControls"}, expectedExceptions = {InputControlsValidationException.class})
    public void verifyCorrectParameterValuesForNonCascadingControls_CascadeChangedParent_Exception() throws Exception {
        Map<String, String[]> initial = new HashMap<String, String[]>(), processed = new HashMap<String, String[]>();

        initial.put("a", new String[]{"a 1"});
        initial.put("b", new String[]{"b 1"});

        processed.put("a", new String[]{"a 2"});
        processed.put("b", new String[]{"b 1"});

        service.verifyCorrectParameterValuesForNonCascadingControls(controlsCascade, initial, processed);
    }

    @Test(groups = {"verifyCorrectParameterValuesForNonCascadingControls"})
    public void verifyCorrectParameterValuesForNonCascadingControls_CascadeChangedChild() throws Exception {
        Map<String, String[]> initial = new HashMap<String, String[]>(), processed = new HashMap<String, String[]>();

        initial.put("a", new String[]{"a 1"});
        initial.put("b", new String[]{"b 1"});

        processed.put("a", new String[]{"a 1"});
        processed.put("b", new String[]{"b 2"});

        service.verifyCorrectParameterValuesForNonCascadingControls(controlsCascade, initial, processed);
    }

    @Test(groups = {"verifyCorrectParameterValuesForNonCascadingControls"}, expectedExceptions = {InputControlsValidationException.class})
    public void verifyCorrectParameterValuesForNonCascadingControls_CascadeChangedParentAndChild_Exception() throws Exception {
        Map<String, String[]> initial = new HashMap<String, String[]>(), processed = new HashMap<String, String[]>();

        initial.put("a", new String[]{"a 1"});
        initial.put("b", new String[]{"b 1"});

        processed.put("a", new String[]{"a 2"});
        processed.put("b", new String[]{"b 2"});

        service.verifyCorrectParameterValuesForNonCascadingControls(controlsCascade, initial, processed);
    }

    @Test(groups = {"verifyCorrectParameterValuesForNonCascadingControls"})
    public void verifyCorrectParameterValuesForNonCascadingControls_Nothing() throws Exception {
        Map<String, String[]> initial = new HashMap<String, String[]>(), processed = new HashMap<String, String[]>();

        initial.put("a", new String[]{NOTHING_SUBSTITUTION_VALUE});

        processed.put("a", new String[]{});

        service.verifyCorrectParameterValuesForNonCascadingControls(controlsCascade, initial, processed);
    }

    @Test(groups = {"verifyCorrectParameterValuesForNonCascadingControls"})
    public void verifyCorrectParameterValuesForNonCascadingControls_Null() throws Exception {
        Map<String, String[]> initial = new HashMap<String, String[]>(), processed = new HashMap<String, String[]>();

        initial.put("a", new String[]{NULL_SUBSTITUTION_VALUE});

        processed.put("a", new String[]{NOTHING_SUBSTITUTION_VALUE});

        service.verifyCorrectParameterValuesForNonCascadingControls(controlsCascade, initial, processed);
    }

    @Test(groups = {"verifyCorrectParameterValuesForNonCascadingControls"})
    public void verifyCorrectParameterValuesForNonCascadingControls_SingleRandomized() throws Exception {
        Map<String, String[]> initial = new HashMap<String, String[]>(), processed = new HashMap<String, String[]>();

        initial.put("c", new String[]{"c 1", "c 2"});

        processed.put("c", new String[]{"c 2", "c 1"});

        service.verifyCorrectParameterValuesForNonCascadingControls(controlsCascade, initial, processed);
    }

    @Test(groups = {"verifyCorrectParameterValuesForNonCascadingControls"}, expectedExceptions = InputControlsValidationException.class)
    public void verifyCorrectParameterValuesForNonCascadingControls_emptyProcessed_Exception() throws Exception {
        Map<String, String[]> initial = new HashMap<>(), processed = new HashMap<>();

        initial.put("a", new String[]{"a 1", "a 2"});

        service.verifyCorrectParameterValuesForNonCascadingControls(controlsCascade, initial, processed);
    }

    @Test(groups = {"verifyCorrectParameterValuesForNonCascadingControls"})
    public void verifyCorrectParameterValuesForNonCascadingControls_nothingSubstitutionAndEmptyProcessed() throws Exception {
        Map<String, String[]> initial = new HashMap<>(), processed = new HashMap<>();

        initial.put("a", new String[]{NOTHING_SUBSTITUTION_VALUE});
        initial.put("b", new String[]{NOTHING_SUBSTITUTION_VALUE});

        service.verifyCorrectParameterValuesForNonCascadingControls(controlsCascade, initial, processed);
    }

    @Test(groups = {"verifyCorrectParameterValuesForNonCascadingControls"})
    public void verifyCorrectParameterValuesForNonCascadingControls_singleWithNothingSubstitution() throws Exception {
        Map<String, String[]> initial = new HashMap<>(), processed = new HashMap<>();

        initial.put("a", new String[]{NOTHING_SUBSTITUTION_VALUE});
        initial.put("b", new String[]{"b 1"});

        processed.put("b", new String[]{"b 1"});

        service.verifyCorrectParameterValuesForNonCascadingControls(controlsCascade, initial, processed);
    }

    @Test(groups = {"verifyCorrectParameterValuesForNonCascadingControls"}, expectedExceptions = InputControlsValidationException.class)
    public void verifyCorrectParameterValuesForNonCascadingControls_singleWithNothingSubstitutionAndValue_Exception() throws Exception {
        Map<String, String[]> initial = new HashMap<>(), processed = new HashMap<>();

        initial.put("a", new String[]{NOTHING_SUBSTITUTION_VALUE, "a 1"});

        service.verifyCorrectParameterValuesForNonCascadingControls(controlsCascade, initial, processed);
    }

    @Test(groups = {"verifyCorrectParameterValuesForNonCascadingControls"})
    public void verifyCorrectParameterValuesForNonCascadingControls_duplicatedInitialValues_Nothing() throws Exception {
        Map<String, String[]> initial = new HashMap<>();
        Map<String, String[]> processed = new HashMap<>();

        initial.put("a", new String[]{"a1", "a1"});
        processed.put("a", new String[]{"a1"});

        service.verifyCorrectParameterValuesForNonCascadingControls(controlsCascade, initial, processed);
    }

    @Test(groups = {"verifyCorrectParameterValuesForNonCascadingControls"})
    public void verifyCorrectParameterValuesForNonCascadingControls_duplicatedFormattedValues_Nothing() throws Exception {
        Map<String, String[]> initial = new HashMap<>();
        Map<String, String[]> processed = new HashMap<>();

        initial.put("a", new String[]{"a1"});
        processed.put("a", new String[]{"a1", "a1"});

        service.verifyCorrectParameterValuesForNonCascadingControls(controlsCascade, initial, processed);
    }

    @Test
    public void verifyRequestAttributesArePropagatedToReportExecutionRunnable() {
        // Arrange
        final RequestAttributes attributes1 = mock(RequestAttributes.class);
        final RequestAttributes attributes2 = mock(RequestAttributes.class);
        // Set 1-st version of attributes
        RequestContextHolder.setRequestAttributes(attributes1);

        // Act
        service.startReportExecution(new ReportExecution());
        ArgumentCaptor<Runnable> argumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(asyncExecutor).execute(argumentCaptor.capture());
        // Override original attributes with 2-nd version
        RequestContextHolder.setRequestAttributes(attributes2);
        Runnable runnable = argumentCaptor.getValue();
        runnable.run();

        // Assert
        // Ensure that runnable is using 1-st version of attributes
        assertEquals(attributes1, RequestContextHolder.getRequestAttributes());
    }

    @Test
    public void verifyCorrectParameterValuesForNonCascadingControls() throws InputControlsValidationException {
        ReportInputControl reportInputControl = mock(ReportInputControl.class);
        when(reportInputControl.getId()).thenReturn("city");
        when(reportInputControl.getType()).thenReturn(InputControlUITypeMapper.getUiType(TYPE_MULTI_SELECT_LIST_OF_VALUES));
        List<ReportInputControl> inputControlList = Arrays.asList(reportInputControl);
        Map<String, String[]> rawInputParameters = new HashMap<>();
        rawInputParameters.put("city", new String[]{NOTHING_SUBSTITUTION_VALUE});

        Map<String, String[]> inputControlFormattedValues = new HashMap<>();
        inputControlFormattedValues.put("city", new String[]{"ABC", "EFG"});
        service.verifyCorrectParameterValuesForNonCascadingControls(inputControlList, rawInputParameters, inputControlFormattedValues);
    }

    @Test(expectedExceptions = ResourceNotFoundException.class)
    public void getReportExecution_emptyCache_exception() {
        service.getReportExecution(REQUEST_ID);
    }

    @Test
    public void getReportExecution_hasExecution_success() {
        authenticateWithUser("jasperadmin");

        ReportExecution expected = mockReturnExecution(REQUEST_ID, "jasperadmin");
        ReportExecution result = service.getReportExecution(REQUEST_ID);
        assertEquals(result.getRequestId(), expected.getRequestId());
    }

    @Test
    public void getReportExecution_hasExecutionForUserAndTenant_success() {
        authenticateWithUserAndTenant("jasperadmin", "org_1");

        ReportExecution expected = mockReturnExecution(REQUEST_ID, "jasperadmin|org_1");
        ReportExecution result = service.getReportExecution(REQUEST_ID);
        assertEquals(result.getRequestId(), expected.getRequestId());
    }

    @Test(expectedExceptions = ResourceNotFoundException.class)
    public void getReportExecution_missesUsername_exception() {
        authenticateWithUserAndTenant("jasperadmin", "org_1");

        ReportExecution expected = mockReturnExecution(REQUEST_ID, null);
        ReportExecution result = service.getReportExecution(REQUEST_ID);
        assertEquals(result.getRequestId(), expected.getRequestId());
    }

    @Test(expectedExceptions = ResourceNotFoundException.class)
    public void getReportExecution_differentUser_exception() {
        authenticateWithUser("jasperadmin");

        mockReturnExecution(REQUEST_ID, "jasperadmin|org_1");
        service.getReportExecution(REQUEST_ID);
    }

    @Test
    public void createReportExecution_withParameters_createdReportExecution() {
        authenticateWithUser("jasperadmin|org_1");

        ReportExecutionOptions options = new ReportExecutionOptions();
        Map<String, String[]> rawParameters = Collections.singletonMap("parameter", new String[]{"value1", "value2"});
        ReportExecution reportExecution = service.createReportExecution("uri", rawParameters, options);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);
        verify(cache).put(keyCaptor.capture(), valueCaptor.capture());

        Object cacheValue = valueCaptor.getValue();
        assertNotNull(cacheValue);
        Pair<String, ReportExecution> pair = (Pair<String, ReportExecution>) cacheValue;
        ReportExecution actualReportExecution = pair.getValue();
        assertEquals("jasperadmin|org_1", pair.getKey());
        assertEquals(reportExecution, actualReportExecution);
        assertEquals(options, actualReportExecution.getOptions());
        assertEquals(rawParameters, actualReportExecution.getRawParameters());
        assertEquals("uri", actualReportExecution.getReportURI());
    }

    @Test
    public void cancelReportExecution_noExecution_false() {
        doReturn(false).when(engine).cancelExecution(REQUEST_ID);

        boolean result = service.cancelReportExecution(REQUEST_ID);
        assertFalse(result);
    }

    @Test
    public void cancelReportExecution_missingReportExecution_true() {
        doReturn(true).when(engine).cancelExecution(REQUEST_ID);
        boolean result = service.cancelReportExecution(REQUEST_ID);
        assertTrue(result);
    }

    @Test
    public void cancelReportExecution_hasReportExecution_true() {
        doReturn(true).when(engine).cancelExecution(REQUEST_ID);
        ReportExecution reportExecution = mockReturnExecution(REQUEST_ID, "jasperadmin|org_1");
        boolean result = service.cancelReportExecution(REQUEST_ID);
        assertTrue(result);
        assertEquals(reportExecution.getStatus(), ExecutionStatus.cancelled);
    }

    @Test
    public void actualizeExecutionStatus_NoReportStatus_NotInEng() {
        ReportExecution reportExecution = new ReportExecution();
        reportExecution.setRequestId("ABC");
        reportExecution.setStatus(ExecutionStatus.execution);
        List<ReportExecutionStatusInformation> reportExecutionStatusInformationList = new ArrayList<>();
        doReturn(reportExecutionStatusInformationList).when(engine).getReportExecutionStatusList();
        service.actualizeExecutionStatus(reportExecution);
        assertEquals(ExecutionStatus.queued, reportExecution.getStatus());
    }

    @Test
    public void actualizeExecutionStatus_NoReportStatus_InEng() {
        ReportExecution reportExecution = new ReportExecution();
        reportExecution.setRequestId("ABC");
        reportExecution.setStatus(ExecutionStatus.execution);
        List<ReportExecutionStatusInformation> reportExecutionStatusInformationList = new ArrayList<>();
        ReportExecutionStatusInformation reportExecutionStatus = mock(ReportExecutionStatusInformation.class);
        doReturn("ABC").when(reportExecutionStatus).getRequestId();
        reportExecutionStatusInformationList.add(reportExecutionStatus);
        doReturn(reportExecutionStatusInformationList).when(engine).getReportExecutionStatusList();
        service.actualizeExecutionStatus(reportExecution);
        assertEquals(ExecutionStatus.execution, reportExecution.getStatus());
    }

    private ReportExecution mockReturnExecution(String requestId, String username) {
        ReportExecution reportExecution = new ReportExecution();
        reportExecution.setRequestId(requestId);

        Pair<String, ReportExecution> pair = Pair.of(username, reportExecution);
        Cache.ValueWrapper wrapper = () -> pair;
        doReturn(wrapper).when(cache).get(eq(requestId));

        return reportExecution;
    }

    private Authentication authenticateWithUser(String username) {
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        doReturn(username).when(authentication).getName();
        return authentication;
    }

    private void authenticateWithUserAndTenant(String username, String tenantId) {
        Authentication authentication = authenticateWithUser(username);
        TenantQualified tenantQualified = mock(TenantQualified.class);
        doReturn(tenantQualified).when(authentication).getPrincipal();
        doReturn(tenantId).when(tenantQualified).getTenantId();
    }

}
