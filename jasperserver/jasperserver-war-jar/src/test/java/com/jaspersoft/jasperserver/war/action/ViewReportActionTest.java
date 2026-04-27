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

import com.jaspersoft.jasperserver.api.JSShowOnlyErrorMessage;
import com.jaspersoft.jasperserver.api.common.domain.ValidationErrors;
import com.jaspersoft.jasperserver.api.common.domain.impl.ValidationErrorsImpl;
import com.jaspersoft.jasperserver.api.engine.common.service.EngineService;
import com.jaspersoft.jasperserver.api.engine.common.service.SecurityContextProvider;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.ReportUnitResult;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.ReportInputControlsInformationImpl;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.ReportLoadingService;
import com.jaspersoft.jasperserver.api.logging.audit.context.AuditContext;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.RepositoryConfiguration;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.dto.reports.inputcontrols.InputControlState;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlValidationError;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlsLogicService;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlsValidationException;
import com.jaspersoft.jasperserver.war.util.SessionObjectSerieAccessor;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.web.servlets.JasperPrintAccessor;
import net.sf.jasperreports.web.servlets.ReportPageStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.FlowExecutionContext;
import org.springframework.webflow.execution.RequestContext;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Vladimir Tsukur
 * @author Anton Fomin
 * @version $Id$
 */
@RunWith(MockitoJUnitRunner.class)
public class ViewReportActionTest {

    @InjectMocks
    private ViewReportAction viewReportAction = new ViewReportAction();

    @Mock
    private AuditContext auditContext;

    @Mock
    private MessageSource messages;

    @Mock
    private EngineService engine;

    @Mock
    private ReportLoadingService reportLoadingService;

    @Mock
    private InputControlsLogicService inputControlsLogicService;

    @Mock
    private SessionObjectSerieAccessor jasperPrintAccessor;

    @Mock
    private WebflowReportContextAccessor reportContextAccessor;

    @Mock
    private SecurityContextProvider securityContextProvider;

    @Mock
    private RepositoryConfiguration configuration;

    @Mock
    private UIExceptionRouter uiExceptionRouter;

    @Mock
    private RepositoryService repositoryService;

    private RequestContext requestContext = mock(RequestContext.class);

    @Mock
    private JasperReportsContext jasperReportsContext;

    @Before
    public void setUp() {
        viewReportAction.setAttributeInputControlsInformation("controlsInfo");
        viewReportAction.setInputControlsAttrName("controlsAttr");
        viewReportAction.setReportUnitAttrName("reportUnitAttrName");

        mockMessageSource();

        WebflowReportContext reportContextMock = mock(WebflowReportContext.class);
		when(reportContextAccessor.getContext(any(RequestContext.class))).thenReturn(reportContextMock);
    }

    @Test
    public void ensureProperMessageSetForEmptyRyportIfValidationErrorsOccursDuringGettingTypedParams() throws Exception {
        final Map<String, Object> flowScopeParams = map(
                entry("controlsInfo", new ReportInputControlsInformationImpl()),
                entry("reportUnitAttrName", "/uri"),
                entry("controlsAttr", new ArrayList<InputControl>()));
        final Map<String, Object> requestParams = map();
        mockRequestContext(requestParams, flowScopeParams, map());

        when(inputControlsLogicService.getTypedParameters(any(), any())).thenAnswer(invocation -> {
            final ValidationErrorsImpl errors = new ValidationErrorsImpl();
            errors.add(new InputControlValidationError("code", null, "message", "/OrderId", null));
            errors.add(new InputControlValidationError("code", null, "message", "/Country", null));
            throw new InputControlsValidationException(errors);
        });

        viewReportAction.runReport(requestContext);

        assertEquals("jasper.report.view.controls.validation.failed: OrderId, Country", requestParams.get(ViewReportAction.ATTRIBUTE_EMPTY_REPORT_MESSAGE));
    }

    @Test
    public void ensureProperMessageSetForEmptyRyportIfValidationErrorsOccursDuringGettingControlStates() throws Exception {
        final Map<String, Object> flowScopeParams = map(
                entry("controlsInfo", new ReportInputControlsInformationImpl()),
                entry("reportUnitAttrName", "/uri"),
                entry("controlsAttr", new ArrayList<InputControl>()));
        final Map<String, Object> requestParams = map();
        mockRequestContext(requestParams, flowScopeParams, map());

        when(inputControlsLogicService.getValuesForInputControls(any(), any(), any(), eq(false))).thenAnswer(invocation -> {
            InputControlState inputControlState = new InputControlState();
            inputControlState.setError("Error");
            inputControlState.setUri("/OrderId");

            InputControlState inputControlStateCountry = new InputControlState();
            inputControlStateCountry.setError("Error");
            inputControlStateCountry.setUri("/Country");

            return Arrays.asList(inputControlState, inputControlStateCountry);
        });

        viewReportAction.runReport(requestContext);

        assertEquals("jasper.report.view.controls.validation.failed: OrderId, Country", requestParams.get(ViewReportAction.ATTRIBUTE_EMPTY_REPORT_MESSAGE));
    }

    @Test
    public void ensureProperMessageSetForEmptyRyportIfNoValidation() throws Exception {
        final Map<String, Object> flowScopeParams = map(
                entry("controlsInfo", new ReportInputControlsInformationImpl()),
                entry("reportUnitAttrName", "/uri"),
                entry("controlsAttr", new ArrayList<InputControl>()));
        final Map<String, Object> requestParams = map();

        mockRequestContext(requestParams, flowScopeParams, map());

        when(inputControlsLogicService.getTypedParameters(any(), any())).thenReturn(new HashMap<>());

        ReportUnitResult reportUnitResultMock = mock(ReportUnitResult.class);
        JasperPrintAccessor jasperPrintAccessorMock = mock(JasperPrintAccessor.class);
        ReportPageStatus reportPageStatusMock = mock(ReportPageStatus.class);
        when(engine.execute(any(), any())).thenReturn(reportUnitResultMock);
        when(reportUnitResultMock.getJasperPrintAccessor()).thenReturn(jasperPrintAccessorMock);
        when(jasperPrintAccessorMock.pageStatus(eq(-1), any())).thenReturn(reportPageStatusMock);

        viewReportAction.runReport(requestContext);

        assertEquals("jasper.report.view.empty", requestParams.get(ViewReportAction.ATTRIBUTE_EMPTY_REPORT_MESSAGE));
    }

    @Test(expected = JSShowOnlyErrorMessage.class)
    public void ensureUIExceptionRouterIsUsedForRegisteredExceptionTypes() throws Exception {
        final Map<String, Object> flowScopeParams = map(
                entry("controlsInfo", new ReportInputControlsInformationImpl()),
                entry("reportUnitAttrName", "/uri"),
                entry("controlsAttr", new ArrayList<InputControl>()));

        mockRequestContext(map(), flowScopeParams, map());

        when(inputControlsLogicService.getTypedParameters(any(), any())).thenReturn(new HashMap<>());
        when(engine.execute(any(), any())).thenThrow(new RuntimeException("testException"));

        JSShowOnlyErrorMessage uiErrorMessage = new JSShowOnlyErrorMessage("testMessage");
        when(uiExceptionRouter.getUIException(any())).thenReturn(uiErrorMessage);

        viewReportAction.runReport(requestContext);
    }

    @Test
    public void testGetRequestParametersAsJSON() throws IOException {
        RequestContext requestContextMock = mock(RequestContext.class);
        Map<String, String[]> nativeParameterMap = new LinkedHashMap<>();
        nativeParameterMap.put("p1", new String[]{"v1"});
        nativeParameterMap.put("p2", new String[]{"v1", "v2"});

        ServletRequest servletRequestMock = mock(ServletRequest.class);
        when(servletRequestMock.getParameterMap()).thenReturn(nativeParameterMap);
        ExternalContext externalContextMock = mock(ExternalContext.class);
        when(externalContextMock.getNativeRequest()).thenReturn(servletRequestMock);
        when(requestContextMock.getExternalContext()).thenReturn(externalContextMock);

        String actualObject = viewReportAction.getRequestParametersAsJSON(requestContextMock);

        assertEquals("{\"p1\":[\"v1\"],\"p2\":[\"v1\",\"v2\"]}", actualObject);
    }

    @Test
    public void testFormatInputControlsValidationErrorMessage() {
        ValidationErrors validationErrors = mock(ValidationErrors.class);
        InputControlValidationError error_1 = new InputControlValidationError("fillParameters.error.invalidValueForType",
                new Object[]{"DateRange"}, "Specify a valid value for type DateRange.", "/Relative_Date_Equals_files/Date", "DAYY");

        InputControlValidationError error_2 = new InputControlValidationError("fillParameters.error.invalidValueForType",
                new Object[]{"DateRange"}, "Specify a valid value for type DateRange.", "/Relative_Date_Equals_files/DateTime", "MONT");

        List<InputControlValidationError> list = new ArrayList<>();
        list.add(error_1);
        list.add(error_2);

        doReturn(list).when(validationErrors).getErrors();
        String actual = viewReportAction.formatInputControlsValidationErrorMessage(validationErrors);
        assertEquals("jasper.report.view.controls.validation.failed: Date, DateTime",actual);
    }

    @Test
    public void testChooseExportMode_withReportOutput() {
        final Map<String, Object> flowScopeParams = map(
                entry("reportOutput", "html"));

        mockRequestContext(map(), flowScopeParams, map());
        Event event = viewReportAction.chooseExportMode(requestContext);
        assertNotNull(event);
        assertEquals("viewReport", event.getId());
    }

    @Test
    public void testChooseExportMode_withShowInputControlsByExport() {
        final Map<String, Object> flowScopeParams = map(
                entry("reportOutput", "value"),
                entry("hasInputControls", Boolean.valueOf(true)),
                entry("reportForceControls", Boolean.valueOf(true)));

        mockRequestContext(map(), flowScopeParams, map());
        Event event = viewReportAction.chooseExportMode(requestContext);
        assertEquals("showInputControlsByExport", event.getId());
    }

    @Test
    public void testChooseExportMode_withExportReport() {
        final Map<String, Object> flowScopeParams = map(
                entry("reportOutput", "value"),
                entry("hasInputControls", Boolean.valueOf(false)),
                entry("reportForceControls", Boolean.valueOf(false)));

        mockRequestContext(map(), flowScopeParams, map());
        Event event = viewReportAction.chooseExportMode(requestContext);
        assertEquals("exportReport", event.getId());
    }

    @Test
    public void ensureExportersListIsPresentOnFlowScopeParams() throws Exception {
        final Map<String, Object> flowScopeParams = map();
        mockRequestContextForPrepareReportView(map(), flowScopeParams, map());

        Map<String, ExporterConfigurationBean> configuredExporters = new LinkedHashMap<>();

        ExporterConfigurationBean pdfConfigurationBean = new ExporterConfigurationBean();
        pdfConfigurationBean.setDescriptionKey("jasper.report.view.hint.export.pdf");
        configuredExporters.put("pdf", pdfConfigurationBean);

        ExporterConfigurationBean xlsConfigurationBean = new ExporterConfigurationBean();
        xlsConfigurationBean.setDescriptionKey("jasper.report.view.hint.export.excel");
        configuredExporters.put("xls", xlsConfigurationBean);

        viewReportAction.setConfiguredExporters(configuredExporters);

        User userMock = mock(User.class);
        when(securityContextProvider.getContextUser()).thenReturn(userMock);
        when(userMock.getTenantId()).thenReturn("tenantId");
        when(configuration.getPublicFolderUri()).thenReturn("/public");
        when(configuration.getTempFolderUri()).thenReturn("/temp");

        viewReportAction.prepareReportView(requestContext);

        String serializeExporters = (String) flowScopeParams.get("exportersList");
        String expected = "[{\"type\": \"simpleAction\"," +
                "\"text\": \"jasper.report.view.hint.export.pdf\"," +
                "\"action\": \"Report.exportReport\"," +
                "\"actionArgs\": [\"pdf\"]}," +
                "{\"type\": \"simpleAction\"," +
                "\"text\": \"jasper.report.view.hint.export.excel\"," +
                "\"action\": \"Report.exportReport\"," +
                "\"actionArgs\": [\"xls\"]}]";

        assertEquals(expected, serializeExporters);
    }

    private void mockRequestContextForPrepareReportView(
            Map<String, Object> requestParams, Map<String, Object> flowScopeParams, final Map<String, Object> sessionParams) {

        ServletExternalContext externalContext = mock(ServletExternalContext.class);
        HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);
        MutableAttributeMap mutableAttributeMap = mock(MutableAttributeMap.class);

        doReturn(setupMutableAttributeMap(flowScopeParams)).when(requestContext).getFlowScope();
        doReturn(setupMutableAttributeMap(requestParams)).when(requestContext).getRequestScope();
        doReturn(externalContext).when(requestContext).getExternalContext();
        doReturn(mutableAttributeMap).when(externalContext).getRequestMap();
        doReturn(false).when(mutableAttributeMap).get(any());
        doReturn(setupParameterMap(requestParams)).when(requestContext).getRequestParameters();

        doReturn(httpServletRequestMock).when(externalContext).getNativeRequest();
        doReturn(setupParameterMap(requestParams)).when(externalContext).getRequestParameterMap();
    }

    private void mockRequestContext(
            Map<String, Object> requestParams, Map<String, Object> flowScopeParams, final Map<String, Object> sessionParams) {

        ServletExternalContext externalContext = mock(ServletExternalContext.class);
        HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);
        FlowExecutionContext flowExecutionContextMock = mock(FlowExecutionContext.class);

        doReturn(setupMutableAttributeMap(flowScopeParams)).when(requestContext).getFlowScope();
        doReturn(setupMutableAttributeMap(requestParams)).when(requestContext).getRequestScope();
        doReturn(setupMutableAttributeMap(requestParams)).when(requestContext).getFlashScope();
        doReturn(externalContext).when(requestContext).getExternalContext();
        doReturn(setupParameterMap(requestParams)).when(requestContext).getRequestParameters();
        doReturn(flowExecutionContextMock).when(requestContext).getFlowExecutionContext();

        doReturn(httpServletRequestMock).when(externalContext).getNativeRequest();
    }

    private void mockMessageSource() {
        when(messages.getMessage(any(), any(), any())).thenAnswer(invocation -> {
            StringBuilder sb = new StringBuilder(invocation.getArgument(0));
            Object[] arg1 = (Object[]) invocation.getArguments()[1];
            if (arg1 != null) {
                sb.append(": ");
                for (Object param: arg1) {
                    sb.append((String)param);
                }
            }

            return sb.toString();
        });
    }

    private ParameterMap setupParameterMap(final Map<String, Object> properties) {
        ParameterMap parameterMapMock = mock(ParameterMap.class);
        when(parameterMapMock.get(any())).thenAnswer(invocation -> properties.get(invocation.getArgument(0)));

        return parameterMapMock;
    }

    private MutableAttributeMap setupMutableAttributeMap(final Map<String, Object> properties) {
        MutableAttributeMap<Object> attributeMapMock = mock(MutableAttributeMap.class);
        when(attributeMapMock.get(any())).thenAnswer(invocation -> properties.get(invocation.getArgument(0)));
        doAnswer(invocation -> {
            final String key = invocation.getArgument(0);
            final Object value = invocation.getArgument(1);
            properties.put(key, value);
            return null;
        }).when(attributeMapMock).put(any(), any());

        return attributeMapMock;
    }

    private Map<String, Object> map(Map.Entry<String, Object>... entries) {
        Map<String, Object> entryMap = new HashMap<>();
        for (Map.Entry<String, Object> entry: entries) {
            entryMap.put(entry.getKey(), entry.getValue());
        }

        return entryMap;
    }

    private Map.Entry<String, Object> entry(String key, Object value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }
}
