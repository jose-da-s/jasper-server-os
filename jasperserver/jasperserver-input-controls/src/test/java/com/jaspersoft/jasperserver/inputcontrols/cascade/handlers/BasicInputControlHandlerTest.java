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
package com.jaspersoft.jasperserver.inputcontrols.cascade.handlers;

import com.jaspersoft.jasperserver.api.engine.common.service.ReportInputControlInformation;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.CalendarFormatProvider;
import com.jaspersoft.jasperserver.api.metadata.common.domain.DataType;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.DataTypeImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.InputControlImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConversionOptions;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConverter;
import com.jaspersoft.jasperserver.dto.common.validations.DateTimeFormatValidationRule;
import com.jaspersoft.jasperserver.dto.common.validations.MandatoryValidationRule;
import com.jaspersoft.jasperserver.dto.common.validations.ValidationRule;
import com.jaspersoft.jasperserver.dto.reports.inputcontrols.InputControlOption;
import com.jaspersoft.jasperserver.dto.reports.inputcontrols.InputControlState;
import com.jaspersoft.jasperserver.dto.reports.inputcontrols.ReportInputControl;
import com.jaspersoft.jasperserver.dto.resources.ClientDataType;
import com.jaspersoft.jasperserver.inputcontrols.cascade.CachedRepositoryService;
import com.jaspersoft.jasperserver.inputcontrols.cascade.CascadeResourceNotFoundException;
import com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.converters.DataConverterService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;


import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@RunWith(MockitoJUnitRunner.class)
public class BasicInputControlHandlerTest extends InputControlHandlerBaseTest {
    @InjectMocks
    private BasicInputControlHandler handler = spy(new BasicInputControlHandler());

    @Mock
    private CachedRepositoryService cachedRepositoryService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private CalendarFormatProvider calendarFormatProvider;

    @Mock
    private ToClientConverter dataTypeResourceConverter;

    @Before
    public void init() throws Exception {
        doAnswer(invocationOnMock -> invocationOnMock.getArguments()[0])
                .when(messageSource).getMessage(anyString(), any(), any(Locale.class));
        reset(cachedRepositoryService, dataTypeResourceConverter);
    }

    @Test
    public void buildReportInputControl_localDataType() throws CascadeResourceNotFoundException {
        final InputControlImpl inputControl = new InputControlImpl();
        final DataTypeImpl dataType = new DataTypeImpl();
        inputControl.setDataType(dataType);
        final ClientDataType clientDataType = new ClientDataType().setMaxValue("100").setMinValue("10")
                .setStrictMax(true).setStrictMin(true);
        doReturn(clientDataType).when(dataTypeResourceConverter).toClient(eq(dataType), eq(ToClientConversionOptions.getDefault()));
        final ReportInputControl result = handler.buildReportInputControl(inputControl, null, null);
        assertNotNull(result);
        assertEquals(clientDataType, result.getDataType());
    }

    @Test
    public void buildReportInputControl_referenceDataType() throws CascadeResourceNotFoundException {
        final InputControlImpl inputControl = new InputControlImpl();
        final DataTypeImpl dataType = new DataTypeImpl();
        final String referenceUri = "/test/resource/reference";
        inputControl.setDataTypeReference(referenceUri);
        final ClientDataType clientDataType = new ClientDataType().setMaxValue("100").setMinValue("10")
                .setStrictMax(true).setStrictMin(true);
        doReturn(clientDataType).when(dataTypeResourceConverter).toClient(eq(dataType), eq(ToClientConversionOptions.getDefault()));
        doReturn(dataType).when(cachedRepositoryService).getResource(eq(DataType.class), eq(referenceUri));
        final ReportInputControl result = handler.buildReportInputControl(inputControl, null, null);
        assertNotNull(result);
        assertEquals(clientDataType, result.getDataType());
    }

    @Test
    public void getValidationRules_mandatory_nonmandatory() throws Exception{
        InputControl inputControl = new InputControlImpl();
        inputControl.setMandatory(true);
        List<ValidationRule> validationRules = handler.getValidationRules(inputControl);
        assertNotNull(validationRules);
        Assert.assertFalse(validationRules.isEmpty());
        MandatoryValidationRule mandatoryValidationRule = getValidationRule(validationRules, MandatoryValidationRule.class);
        assertNotNull(mandatoryValidationRule);
        // mocked message source returns error code as result message
        Assert.assertEquals(MandatoryValidationRule.ERROR_KEY, mandatoryValidationRule.getErrorMessage());
        // check case for nonmandatory
        inputControl.setMandatory(false);
        validationRules = handler.getValidationRules(inputControl);
        mandatoryValidationRule = getValidationRule(validationRules, MandatoryValidationRule.class);
        Assert.assertNull(mandatoryValidationRule);
    }

    private <T extends ValidationRule> T getValidationRule(List<ValidationRule> rules, Class<T> concreteRuleType){
        T result = null;
        if(rules != null && !rules.isEmpty()){
            for(ValidationRule currentRule : rules){
                if(concreteRuleType.isAssignableFrom(currentRule.getClass())){
                    result = (T) currentRule;
                    break;
                }
            }
        }
        return result;
    }

    private <T extends ValidationRule> List<T> getValidationRules(List<ValidationRule> rules, Class<T> concreteRuleType){
        List<T> result = new ArrayList<T>();
        if(rules != null && !rules.isEmpty()){
            for(ValidationRule currentRule : rules){
                if(concreteRuleType.isAssignableFrom(currentRule.getClass())){
                    result.add((T) currentRule);
                }
            }
        }
        return result;
    }

    @Test
    public void getValidationRules_date_datetime() throws Exception{
        InputControl inputControl = new InputControlImpl();
        inputControl.setDataType(new ResourceReference((String)null));
        DataType dataType = new DataTypeImpl();
        doReturn(dataType).when(cachedRepositoryService).getResource(any(), any(ResourceReference.class));
        final String expectedDatePattern = "expectedDatePattern";
        final String expectedDateTimePattern = "expectedDateTimePattern";
        final String expectedTimePattern = "expectedTimePattern";
        doReturn(expectedDatePattern).when(calendarFormatProvider).getDatePattern();
        doReturn(expectedDateTimePattern).when(calendarFormatProvider).getDatetimePattern();
        doReturn(expectedTimePattern).when(calendarFormatProvider).getTimePattern();
        // case with type date
        dataType.setDataTypeType(DataType.TYPE_DATE);
        List<ValidationRule> validationRules = handler.getValidationRules(inputControl);
        List<DateTimeFormatValidationRule> rules = getValidationRules(validationRules, DateTimeFormatValidationRule.class);
        assertNotNull(rules);
        Assert.assertTrue(rules.size() == 1);
        DateTimeFormatValidationRule rule = rules.get(0);
        assertNotNull(rule);
        Assert.assertEquals(DateTimeFormatValidationRule.INVALID_DATE, rule.getErrorMessage());
        Assert.assertEquals(expectedDatePattern, rule.getFormat());
        // case with type datetime
        dataType.setDataTypeType(DataType.TYPE_DATE_TIME);
        validationRules = handler.getValidationRules(inputControl);
        rules = getValidationRules(validationRules, DateTimeFormatValidationRule.class);
        assertNotNull(rules);
        Assert.assertTrue(rules.size() == 1);
        rule = rules.get(0);
        assertNotNull(rule);
        Assert.assertEquals(DateTimeFormatValidationRule.INVALID_DATE_TIME, rule.getErrorMessage());
        Assert.assertEquals(expectedDateTimePattern, rule.getFormat());
        // case with type time
        dataType.setDataTypeType(DataType.TYPE_TIME);
        validationRules = handler.getValidationRules(inputControl);
        rules = getValidationRules(validationRules, DateTimeFormatValidationRule.class);
        assertNotNull(rules);
        Assert.assertTrue(rules.size() == 1);
        rule = rules.get(0);
        assertNotNull(rule);
        Assert.assertEquals(DateTimeFormatValidationRule.INVALID_TIME, rule.getErrorMessage());
        Assert.assertEquals(expectedTimePattern, rule.getFormat());
    }

    @Test
    public void buildReportInputControl_getValidationRules_getType_invoked() throws Exception{
        final String expectedType = "expectedType";
        final InputControl inputControl = new InputControlImpl();
        inputControl.setURIString("/test/uri/string");
        final String uiType = "testUiType";
        final List<ValidationRule> validationRules = new ArrayList<ValidationRule>();
        validationRules.add(new MandatoryValidationRule());
        doReturn(expectedType).when(handler).getType(eq(inputControl), eq(uiType));
        doReturn(validationRules).when(handler).getValidationRules(eq(inputControl));
        final ReportInputControl reportInputControl = handler.buildReportInputControl(inputControl, uiType, null);
        assertNotNull(reportInputControl);
        Assert.assertSame(validationRules, reportInputControl.getValidationRules());
        Assert.assertEquals(expectedType, reportInputControl.getType());
        Assert.assertEquals(inputControl.getURI(), reportInputControl.getUri());
    }

    @Test
    public void convertParameterValueFromRawDataNullAndDefaultNull() throws Exception {
        setApplicationContextForTestCase("classpath:/com/jaspersoft/jasperserver/inputcontrols/cascade/handlers/basicInputControlHandler/" +
                "verifyNullSubstitutionWhenNoInputParamsAndDefaultIsNull.xml");

        String testCaseName = "verifyNullSubstitutionWhenNoInputParamsAndDefaultIsNull";

        InputControl control = getControl("String", testCaseName);
        ReportInputControlInformation info = getControlInfo("String", testCaseName);
        InputControlHandler inputControlHandler = getHandler(testCaseName, InputControl.TYPE_SINGLE_VALUE);
        String[] rawData = getRawDataArray(testCaseName);

        Object typedValues = inputControlHandler.convertParameterValueFromRawData(rawData, control, info);

        assertTypedValue("verifyNullSubstitutionWhenNoInputParamsAndDefaultIsNull", typedValues);
    }

    @Test
    public void getValueNullSubstitutionTypedParamsNullDefaultNull() throws Exception  {
        setApplicationContextForTestCase("classpath:/com/jaspersoft/jasperserver/inputcontrols/cascade/handlers/basicInputControlHandler/" +
                "getValueWhenTypedParamNullDefaultNull.xml");

        String testCaseName = "getValueWhenTypedParamNullDefaultNull";

        InputControl control = getControl("String", testCaseName);
        ReportInputControlInformation info = getControlInfo("String", testCaseName);
        InputControlHandler inputControlHandler = getHandler(testCaseName, InputControl.TYPE_SINGLE_VALUE);
        Map<String,Object> typedParamsMap = getTypedParamsMap(testCaseName);

        //Actuall call of test method
        InputControlState envelope =
                inputControlHandler.getState(control, null, typedParamsMap, getParameterTypes(testCaseName), info);

        assertControlState(testCaseName, envelope);
    }

    @Test
    public void setTotalCount_withValidCount() {
        InputControlState inputControlState = new InputControlState();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeTotalCount", "true");

        handler.setTotalCount(inputControlState, parameters);
        assertEquals("1", inputControlState.getTotalCount());
    }

    @Test
    public void setTotalCount_withNullCount() {
        InputControlState inputControlState = new InputControlState();

        Map<String, Object> parameters = new HashMap<>();

        handler.setTotalCount(inputControlState, parameters);
        assertEquals(null, inputControlState.getTotalCount());
    }


    @Test
    public void fillStateValue_withValidParameterValue() throws CascadeResourceNotFoundException {
        InputControlState state = new InputControlState();
        InputControl inputControl = mock(InputControl.class);
        DataConverterService dataConverterService = mock(DataConverterService.class);
        when(inputControl.getName()).thenReturn("Country");
        ResourceReference resourceReference = mock(ResourceReference.class);
        handler.setDataConverterService(dataConverterService);

        when(dataConverterService.formatSingleValue(any(Object.class), any(InputControl.class), nullable(ReportInputControlInformation.class))).thenReturn("USA");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("Country", "USA");
        handler.fillStateValue(state, inputControl, null, parameters, null, null);
        assertEquals("USA", state.getValue());
    }

    @Test
    public void fillStateValue_withNoParameterValue() throws CascadeResourceNotFoundException {
        InputControlState state = new InputControlState();
        InputControl inputControl = mock(InputControl.class);
        DataConverterService dataConverterService = mock(DataConverterService.class);
        when(inputControl.getName()).thenReturn("Country");
        ResourceReference resourceReference = mock(ResourceReference.class);
        handler.setDataConverterService(dataConverterService);

        Map<String, Object> parameters = new HashMap<>();

        handler.fillStateValue(state, inputControl, null, parameters, null, null);
        assertEquals(0, parameters.size());
    }

    @Test
    public void fillSelectedValue_defaultSingleValue() throws CascadeResourceNotFoundException {
        BasicInputControlHandler inputControlHandler = spy(new BasicInputControlHandler());
        DataConverterService dataConverterService = mock(DataConverterService.class);
        ReportInputControlInformation info = mock(ReportInputControlInformation.class);
        InputControl inputControl = mock(InputControl.class);

        Map<String, List<InputControlOption>> result = new HashMap<>();
        result.put("Country", Arrays.asList(new InputControlOption[]{new InputControlOption().setValue("USA").setLabel("USA").setSelected(null)}));
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("Country", "USA");

        inputControlHandler.setDataConverterService(dataConverterService);
        when(inputControl.getName()).thenReturn("Country");
        when(dataConverterService.formatSingleValue(parameters.get(inputControl.getName()), inputControl, info)).thenReturn("USA");
        assertThat(result, is(inputControlHandler.filterSelectedValues(inputControl, null, parameters, null, info)));
    }

    @Test
    public void fillSelectedValue_defaultNullValue() throws CascadeResourceNotFoundException {
        BasicInputControlHandler inputControlHandler = spy(new BasicInputControlHandler());
        DataConverterService dataConverterService = mock(DataConverterService.class);
        ReportInputControlInformation info = mock(ReportInputControlInformation.class);
        InputControl inputControl = mock(InputControl.class);

        Map<String, List<InputControlOption>> result = new HashMap<>();
        result.put("Country", Arrays.asList(new InputControlOption[]{new InputControlOption().setValue("USA").setLabel("USA").setSelected(null)}));
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("Country", null);

        inputControlHandler.setDataConverterService(dataConverterService);
        when(inputControl.getName()).thenReturn("Country");
        when(dataConverterService.formatSingleValue(parameters.get(inputControl.getName()), inputControl, info)).thenReturn(null);
        when(dataConverterService.convertSingleValue(null, inputControl, info)).thenReturn(InputControlHandler.NULL_SUBSTITUTION_VALUE);
        inputControlHandler.filterSelectedValues(inputControl, null, parameters, null, info);
        assertEquals(InputControlHandler.NULL_SUBSTITUTION_VALUE, parameters.get("Country"));
    }
}

