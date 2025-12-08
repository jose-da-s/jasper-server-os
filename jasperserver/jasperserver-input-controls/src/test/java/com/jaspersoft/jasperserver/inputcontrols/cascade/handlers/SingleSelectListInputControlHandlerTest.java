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

package com.jaspersoft.jasperserver.inputcontrols.cascade.handlers;

import com.jaspersoft.jasperserver.api.common.util.diagnostic.DiagnosticSnapshotPropertyHelper;
import com.jaspersoft.jasperserver.api.engine.common.service.ReportInputControlInformation;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.JasperReportInputControlInformation;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.ReportInputControlValuesInformationImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.DataType;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ListOfValuesItem;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.ListOfValuesItemImpl;
import com.jaspersoft.jasperserver.dto.reports.inputcontrols.InputControlOption;
import com.jaspersoft.jasperserver.dto.reports.inputcontrols.InputControlState;
import com.jaspersoft.jasperserver.inputcontrols.cascade.CascadeResourceNotFoundException;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlValidationException;
import com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.converters.DataConverterService;
import org.apache.commons.collections.set.ListOrderedSet;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.jaspersoft.jasperserver.dto.common.validations.MandatoryValidationRule.ERROR_KEY;
import static com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.InputControlHandler.NOTHING_SUBSTITUTION_LABEL;
import static com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.InputControlHandler.NOTHING_SUBSTITUTION_VALUE;
import static com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.ParametersHelper.entry;
import static com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.ParametersHelper.listOfOptions;
import static com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.ParametersHelper.listOfValues;
import static com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.ParametersHelper.map;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


/**
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
public class SingleSelectListInputControlHandlerTest extends BaseInputControlHandlerTest {

    @Spy
    @InjectMocks
    private SingleSelectListInputControlHandler handler;

    /**
     * Incoming value is of type Integer.
     * List of values for input control contains values ot type BigDecimal.
     * Integer value has corresponding BigDecimal equivalent in list of values and corresponding option become selected.
     * @throws Exception
     */
    @Test
    public void getStateOfInputControlNumberWithListOfBigDecimalAndIntegerValue() throws Exception {
        final String parameterName = "testName";
        // incoming value is of type Integer
        final Integer integerValue = Integer.valueOf(2);
        // BigDecimal equivalent of incoming value
        final BigDecimal bigDecimalValue = BigDecimal.valueOf(2);

        doReturn(parameterName).when(inputControl).getName();
        final HashMap<String, Class<?>> parameterTypes = new HashMap<String, Class<?>>();
        Map<String, Object> inputParameters = map(entry(parameterName, integerValue));

        // building list of values from BigDecimals
        final List<ListOfValuesItem> listOfValues = listOfValues(BigDecimal.valueOf(0), BigDecimal.valueOf(1), bigDecimalValue, BigDecimal.valueOf(3), BigDecimal.valueOf(4));
        doReturn(listOfValues).when(loader).loadValues(eq(inputControl), nullable(ResourceReference.class), eq(inputParameters), eq(parameterTypes), eq(reportInputControlInformation), anyBoolean());
        // dummy data conversion
        doReturn(bigDecimalValue.toString()).when(dataConverterService).formatSingleValue(eq(bigDecimalValue), eq(inputControl), eq(reportInputControlInformation));

        ////////////tested method call/////////////////
        final List<InputControlOption> options = handler.getState(inputControl, null, inputParameters, parameterTypes, reportInputControlInformation).getOptions();
        // state should have options
        assertNotNull(options);
        // options number is one item more then values because of nothing selected item.
        assertEquals(listOfValues.size(), options.size());
        List<InputControlOption> selectedOptions = new ArrayList<InputControlOption>();
        for (InputControlOption currentOption : options) {
            if (currentOption.isSelected() != null && currentOption.isSelected()) selectedOptions.add(currentOption);
        }
        // only one item is selected
        assertEquals(selectedOptions.size(), 1);
        // selected item is string representation of BigDecimal, which is equivalent to incoming Integer value
        assertEquals(selectedOptions.get(0).getValue(), bigDecimalValue.toString());
    }

    @Test
    public void getStateOfInputControlNumberWith_FirstValueSelected() throws Exception {
        final String parameterName = "testName";
        // incoming value is of type Integer
        final Integer integerValue = Integer.valueOf(3);
        // BigDecimal equivalent of incoming value
        final BigDecimal bigDecimalValue = BigDecimal.valueOf(2);

        doReturn(parameterName).when(inputControl).getName();
        doReturn(true).when(inputControl).isMandatory();
        final HashMap<String, Class<?>> parameterTypes = new HashMap<String, Class<?>>();
        Map<String, Object> inputParameters = map(entry(parameterName, integerValue));

        // building list of values from BigDecimals
        final List<ListOfValuesItem> listOfValues = listOfValues(BigDecimal.valueOf(0));
        doReturn(listOfValues).when(loader).loadValues(eq(inputControl), nullable(ResourceReference.class), eq(inputParameters), eq(parameterTypes), eq(reportInputControlInformation), anyBoolean());
        ////////////tested method call/////////////////
        final List<InputControlOption> options = handler.getState(inputControl, null, inputParameters, parameterTypes, reportInputControlInformation).getOptions();
        // state should have options
        assertNotNull(options);
        // options number is one item more then values because of nothing selected item.
        assertEquals(listOfValues.size(), options.size());
        assertEquals(options.get(0).isSelected(), true);
    }

    @Test
    public void getStateOfInputControlNumberWith_NoValueSelected() throws Exception {
        final String errorMessage = "mandatoryErrorMessage";
        final String parameterName = "testName";
        final String parameterName_offset = "testName_offset";
        // incoming value is of type Integer
        final Integer integerValue = 5;

        doReturn(errorMessage).when(messageSource).getMessage(eq(ERROR_KEY), any(), any());
        doReturn(parameterName).when(inputControl).getName();
        doReturn(true).when(inputControl).isMandatory();
        final HashMap<String, Class<?>> parameterTypes = new HashMap<>();
        Map<String, Object> inputParameters = map(entry(parameterName, integerValue), entry(parameterName_offset, "2"));

        // building list of values from BigDecimals
        Object[] values = IntStream.range(0, 4).boxed().map(BigDecimal::new).toArray();
        final List<ListOfValuesItem> listOfValues = listOfValues(values);
        doReturn(listOfValues).when(loader).loadValues(eq(inputControl), nullable(ResourceReference.class), eq(inputParameters), eq(parameterTypes), eq(reportInputControlInformation), anyBoolean());

        InputControlState result = handler.getState(inputControl, null, inputParameters, parameterTypes, reportInputControlInformation);

        assertEquals(errorMessage, result.getError());
    }

    /**
     * Incoming value is of type BigDecimal.
     * List of values for input control contains values ot type Integer.
     * BigDecimal value has corresponding Integer equivalent in list of values and corresponding option become selected.
     *
     * @throws Exception
     */
    @Test
    public void getStateOfInputControlNumberWithListOfIntegerAndBigDecimalValue() throws Exception {
        final String parameterName = "testName";
        // incoming value is of type BigDecimal
        final BigDecimal bigDecimalValue = BigDecimal.valueOf(3);
        // Integer equivalent of incoming value
        final Integer integerValue = 3;

        doReturn(parameterName).when(inputControl).getName();
        final HashMap<String, Class<?>> parameterTypes = new HashMap<String, Class<?>>();
        Map<String, Object> inputParameters = map(entry(parameterName, bigDecimalValue));
        // building list of values from Integers
        final List<ListOfValuesItem> listOfValues = listOfValues(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2), integerValue, Integer.valueOf(4));
        doReturn(listOfValues).when(loader).loadValues(eq(inputControl), nullable(ResourceReference.class), eq(inputParameters), eq(parameterTypes), eq(reportInputControlInformation), anyBoolean());
        // dummy data conversion
        doReturn(integerValue.toString()).when(dataConverterService).formatSingleValue(eq(integerValue), eq(inputControl), eq(reportInputControlInformation));
        ////////////tested method call/////////////////
        final List<InputControlOption> options = handler.getState(inputControl, null, inputParameters, parameterTypes, reportInputControlInformation).getOptions();
        // state should have options
        assertNotNull(options);
        // options number is one item more then values because of nothing selected item.
        assertEquals(listOfValues.size(), options.size());
        List<InputControlOption> selectedOptions = new ArrayList<InputControlOption>();
        for (InputControlOption currentOption : options) {
            if (currentOption.isSelected() != null && currentOption.isSelected()) selectedOptions.add(currentOption);
        }
        // only one item is selected
        assertEquals(selectedOptions.size(), 1);
        // selected item is string representation of Integer, which is equivalent to incoming BigDecimal value
        assertEquals(selectedOptions.get(0).getValue(), integerValue.toString());
    }

    /**
     * Input control options ot type Long.
     * Checks if Numbers matcher works well for Long type. Covers the bugzilla #33225 case.
     *
     * @throws Exception
     */
    @Test
    public void getStateOfInputControlNumberWithListOfLong() throws Exception {
        final String parameterName = "testName";
        // Selected value is of type Long
        final Long selectedValue = Long.valueOf("49539595901085460");

        doReturn(parameterName).when(inputControl).getName();
        final HashMap<String, Class<?>> parameterTypes = new HashMap<String, Class<?>>();
        Map<String, Object> inputParameters = map(entry(parameterName, selectedValue));
        // building list of values from Longs
        final List<ListOfValuesItem> listOfValues = listOfValues(Long.valueOf("0"), Long.valueOf("49539595901085457"),
                Long.valueOf("49539595901085458"), Long.valueOf("49539595901085459"), selectedValue, Long.valueOf("49539595901085461"));

        doReturn(listOfValues).when(loader).loadValues(eq(inputControl), nullable(ResourceReference.class), eq(inputParameters), eq(parameterTypes), eq(reportInputControlInformation), anyBoolean());

        doAnswer(invocationOnMock ->
                invocationOnMock.getArguments()[0].toString()
        ).when(dataConverterService).formatSingleValue(nullable(Object.class), any(InputControl.class), eq(reportInputControlInformation));

        ////////////tested method call/////////////////
        final List<InputControlOption> options = handler.getState(inputControl, null, inputParameters, parameterTypes, reportInputControlInformation).getOptions();
        // state should have options
        assertNotNull(options);
        // options number is one item more then values because of nothing selected item.
        assertEquals(listOfValues.size(), options.size());
        List<InputControlOption> selectedOptions = new ArrayList<InputControlOption>();
        for (InputControlOption currentOption : options) {
            if (currentOption.isSelected() != null && currentOption.isSelected()) selectedOptions.add(currentOption);
        }
        // only one item is selected
        assertEquals(selectedOptions.size(), 1);
        // selected item is string representation of Integer, which is equivalent to incoming BigDecimal value
        assertEquals(selectedOptions.get(0).getValue(), selectedValue.toString());
    }

    @Test
    public void fillStateOfInputControlNumberWithListOfLong() throws Exception {
        final String parameterName = "testName";
        // Selected value is of type Long
        final Long selectedValue = Long.valueOf("49539595901085460");

        doReturn(parameterName).when(inputControl).getName();
        final HashMap<String, Class<?>> parameterTypes = new HashMap<String, Class<?>>();
        Map<String, Object> inputParameters = map(entry(parameterName, selectedValue));
        // building list of values from Longs
        final List<ListOfValuesItem> listOfValues = listOfValues(Long.valueOf("0"), Long.valueOf("49539595901085457"),
                Long.valueOf("49539595901085458"), Long.valueOf("49539595901085459"), selectedValue, Long.valueOf("49539595901085461"));

        doReturn(listOfValues).when(loader).loadValues(eq(inputControl), nullable(ResourceReference.class), eq(inputParameters), eq(parameterTypes), eq(reportInputControlInformation), anyBoolean());

        doAnswer(invocationOnMock ->
                invocationOnMock.getArguments()[0].toString()
        ).when(dataConverterService).formatSingleValue(nullable(Object.class), any(InputControl.class), eq(reportInputControlInformation));

        ////////////tested method call/////////////////


        handler.fillStateValue(state, inputControl, dataSource, inputParameters, reportInputControlInformation, Collections.emptyMap());


        List<InputControlOption>  options = state.getOptions();
        // state should have options
        assertNotNull(options);
        // options number is one item more then values because of nothing selected item.
        assertEquals(listOfValues.size(), options.size());
        List<InputControlOption> selectedOptions = new ArrayList<InputControlOption>();
        for (InputControlOption currentOption : options) {
            if (currentOption.isSelected() != null && currentOption.isSelected()) selectedOptions.add(currentOption);
        }
        // only one item is selected
        assertEquals(selectedOptions.size(), 1);
        // selected item is string representation of Integer, which is equivalent to incoming BigDecimal value
        assertEquals(selectedOptions.get(0).getValue(), selectedValue.toString());
        assertEquals(inputParameters.get(parameterName), selectedValue);
    }
    
    @Test
    public void populateSelectedValuesList_checkDefaultValue() throws CascadeResourceNotFoundException {
        SingleSelectListInputControlHandler selectListInputControlHandler = spy(new MultiSelectListInputControlHandler());
        DataConverterService dataConverterService = mock(DataConverterService.class);

        InputControl inputControl = null;
        ReportInputControlInformation info = null;
        boolean isNothingSelected = false;
        List<String> defaultValue = new ArrayList<>();
        defaultValue.add("USA");
        defaultValue.add("Canada");

        List<InputControlOption> selectedValues = new ArrayList<>();
        selectListInputControlHandler.setDataConverterService(dataConverterService);

        when(selectListInputControlHandler.getCurrentItemValue(null, null, values.get(0))).thenReturn("USA");
        when(selectListInputControlHandler.getCurrentItemValue(null, null, values.get(1))).thenReturn("Canada");
        when(selectListInputControlHandler.getCurrentItemValue(null, null, values.get(2))).thenReturn("Mexico");

        selectListInputControlHandler.populateSelectedValuesList(inputControl, info, selectedValues, new ArrayList<>(), null, values, defaultValue, isNothingSelected);

        assertThat(selectedValues.get(0).getLabel(), is(defaultValue.get(0)));
    }


    @Test
    public void populateSelectedValuesList_validateNothingValue() throws CascadeResourceNotFoundException {
        SingleSelectListInputControlHandler selectListInputControlHandler = spy(new SingleSelectListInputControlHandler());
        DataConverterService dataConverterService = mock(DataConverterService.class);
        InputControl inputControl = mock(InputControl.class);
        when(inputControl.isMandatory()).thenReturn(false);
        ReportInputControlInformation info = null;
        boolean isNothingSelected = true;
        List<String> defaultValue = new ArrayList<>();
        defaultValue.add("---");

        List<InputControlOption> selectedValues = new ArrayList<>();
        selectListInputControlHandler.setDataConverterService(dataConverterService);
        when(selectListInputControlHandler.getCurrentItemValue(inputControl, info, values.get(0))).thenReturn("USA");
        when(selectListInputControlHandler.getCurrentItemValue(inputControl, info, values.get(1))).thenReturn("Canada");
        when(selectListInputControlHandler.getCurrentItemValue(inputControl, info, values.get(2))).thenReturn("Mexico");

        selectListInputControlHandler.populateSelectedValuesList(inputControl, info, selectedValues, new ArrayList<>(), null, values, null, isNothingSelected);

        assertThat(selectedValues.get(0).getLabel(), is(defaultValue.get(0)));
    }

    @Test
    public void populateSelectedValuesList_checkDefaultFirstValue() throws CascadeResourceNotFoundException {
        SingleSelectListInputControlHandler selectListInputControlHandler = spy(new MultiSelectListInputControlHandler());
        DataConverterService dataConverterService = mock(DataConverterService.class);
        InputControl inputControl = mock(InputControl.class);
        when(inputControl.isMandatory()).thenReturn(true);
        ReportInputControlInformation info = null;
        boolean isNothingSelected = false;
        List<String> defaultValue = new ArrayList<>();
        defaultValue.add("USA");

        List<InputControlOption> selectedValues = new ArrayList<>();
        selectListInputControlHandler.setDataConverterService(dataConverterService);
        when(selectListInputControlHandler.getCurrentItemValue(inputControl, info, values.get(0))).thenReturn("USA");
        when(selectListInputControlHandler.getCurrentItemValue(inputControl, info, values.get(1))).thenReturn("Canada");
        when(selectListInputControlHandler.getCurrentItemValue(inputControl, info, values.get(2))).thenReturn("Mexico");

        selectListInputControlHandler.populateSelectedValuesList(inputControl, info, selectedValues, new ArrayList<>(), null, values, null, isNothingSelected);

        assertThat(selectedValues.get(0).getLabel(), is(defaultValue.get(0)));
    }

    @Test
    public void populateSelectedValuesWithNoLabel_withValueList() throws CascadeResourceNotFoundException {
        SingleSelectListInputControlHandler selectListInputControlHandler = spy(new MultiSelectListInputControlHandler());
        DataConverterService dataConverterService = mock(DataConverterService.class);
        InputControl inputControl = mock(InputControl.class);
        ReportInputControlInformation info = null;
        List<String> defaultValue = new ArrayList<>();
        defaultValue.add("USA");

        List<InputControlOption> selectedValues = new ArrayList<>();
        selectListInputControlHandler.setDataConverterService(dataConverterService);
        doReturn("USA").when(dataConverterService).formatSingleValue(any(), nullable(InputControl.class), nullable(ReportInputControlInformation.class));

        selectListInputControlHandler.populateSelectedValuesWithNoLabel(inputControl, info, selectedValues, new ArrayList<>(), defaultValue);
        assertThat(selectedValues.get(0).getValue(), is(defaultValue.get(0)));
        assertNull(selectedValues.get(0).getLabel());

    }

    @Test
    public void populateSelectedValuesWithNoLabel_withSingleValue() throws CascadeResourceNotFoundException {
        SingleSelectListInputControlHandler selectListInputControlHandler = spy(new SingleSelectListInputControlHandler());
        DataConverterService dataConverterService = mock(DataConverterService.class);
        InputControl inputControl = mock(InputControl.class);
        ReportInputControlInformation info = null;
        String defaultValue = "USA";

        List<InputControlOption> selectedValues = new ArrayList<>();
        selectListInputControlHandler.setDataConverterService(dataConverterService);
        doReturn("USA").when(dataConverterService).formatSingleValue(any(), nullable(InputControl.class), nullable(ReportInputControlInformation.class));

        selectListInputControlHandler.populateSelectedValuesWithNoLabel(inputControl, info, selectedValues, new ArrayList<>(), defaultValue);
        assertThat(selectedValues.get(0).getValue(), is(defaultValue));
        assertNull(selectedValues.get(0).getLabel());

    }

    @Test
    public void getMandatoryValues_withMandatoryIC() throws CascadeResourceNotFoundException {
        SingleSelectListInputControlHandler selectListInputControlHandler = spy(new SingleSelectListInputControlHandler());
        DataConverterService dataConverterService = mock(DataConverterService.class);
        InputControl inputControl = mock(InputControl.class);
        ReportInputControlInformation info = null;

        boolean  isNothingSelected = false;
        List<InputControlOption> selectedValues = new ArrayList<>();
        List<Object> selectedValuesList = new ArrayList<>();

        selectListInputControlHandler.setDataConverterService(dataConverterService);

        doReturn(true).when(inputControl).isMandatory();
        doReturn("USA_Converted").when(dataConverterService).convertSingleValue(eq("USA"), eq(inputControl), eq(info));
        doReturn("USA_Formatted").when(dataConverterService).formatSingleValue(any(), nullable(InputControl.class), nullable(ReportInputControlInformation.class));

        selectListInputControlHandler.setDataConverterService(dataConverterService);

        selectListInputControlHandler.getMandatoryValues(inputControl, info, selectedValues, selectedValuesList, values.get(0), isNothingSelected);
        assertEquals("USA_Converted", selectedValuesList.get(0));
        assertEquals("USA", selectedValues.get(0).getLabel());
        assertEquals("USA_Formatted", selectedValues.get(0).getValue());
    }

    @Test
    public void fillSelectedValue_withValidSelectedValues() throws CascadeResourceNotFoundException {
        Map<String, List<InputControlOption>> selectedValuesMap;
        SingleSelectListInputControlHandler selectListInputControlHandler = spy(new MultiSelectListInputControlHandler());
        ValuesLoader loader = mock(ValuesLoader.class);
        ReportInputControlInformation info = mock(ReportInputControlInformation.class);
        InputControl inputControl = mock(InputControl.class);

        Map<String, List<String>> result = new HashMap<>();
        result.put("Country", Arrays.asList("USA"));
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("Country", "USA");
        when(info.getDefaultValue()).thenReturn("USA");
        when(inputControl.getName()).thenReturn("Country");


        List<InputControlOption> selectedValues = new ArrayList<>();
        selectedValues.add(new InputControlOption().setValue("USA").setLabel("USA"));


        selectListInputControlHandler.setLoader(loader);

        when(selectListInputControlHandler.isNothingSelected(inputControl.getName(), parameters)).thenReturn(true);
        when(selectListInputControlHandler.populateSelectedValuesList(nullable(InputControl.class), nullable(ReportInputControlInformation.class), nullable(List.class), nullable(List.class), nullable(DataType.class), nullable(List.class), nullable(Object.class), anyBoolean())).thenReturn(selectedValues);
        selectedValuesMap = selectListInputControlHandler.fillSelectedValue(inputControl, null, parameters, info, null);
        assertEquals(selectedValues, selectedValuesMap.get("Country"));
    }


    /**
     * diagnostic datasnapshot contains IC default values
     * chosen by user when run report under diagnostic
     *
     * @throws Exception
     */
    @Test
    public void getStateOfInputControlFromSingleDefaultValueOfDiagnosticDatasnapshot() throws Exception {
        final String parameterName = "testName";
        final String valueForDefault = "Diagnostic";

        final List<InputControlOption> defaultItems = new ArrayList<InputControlOption>() {{
            add(new InputControlOption(valueForDefault, valueForDefault, true));
        }};

        doReturn(parameterName).when(inputControl).getName();
        final HashMap<String, Class<?>> parameterTypes = new HashMap<String, Class<?>>();
        Map<String, Object> inputParameters = map(
                entry(DiagnosticSnapshotPropertyHelper.ATTRIBUTE_IS_DIAG_SNAPSHOT, Boolean.TRUE),
                entry(parameterName, valueForDefault),
                entry(parameterName + "_offset", "1"));

        doReturn(valueForDefault).when(reportInputControlInformation).getDefaultValue();

        final List<ListOfValuesItem> listOfValues = listOfValues(
                Integer.valueOf(-2), Integer.valueOf(-1),
                Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2));

        ListOfValuesItem listOfValuesItem = new ListOfValuesItemImpl();
        listOfValuesItem.setLabel("Diagnostic");
        listOfValuesItem.setValue("Diagnostic");
        final List<ListOfValuesItem> expectedResult = new ArrayList<>();
        expectedResult.add(listOfValuesItem);

        doReturn(2).when(inputControlPagination).getLimit(nullable(InputControl.class), nullable(Map.class), nullable(Map.class));
        doReturn(0).when(inputControlPagination).getOffset(nullable(InputControl.class), nullable(Map.class), anyInt(), anyMap());
        doReturn(expectedResult).when(loader).checkCriteriaAndAddItem(nullable(String.class), anyList(), any(ListOfValuesItem.class));


        doReturn(valueForDefault).when(dataConverterService).formatSingleValue(eq(valueForDefault), eq(inputControl), eq(reportInputControlInformation));
        doReturn(valueForDefault).when(dataConverterService).convertSingleValue(eq(valueForDefault), eq(inputControl), eq(reportInputControlInformation));
        ////////////tested method call/////////////////
        final List<InputControlOption> options = handler.getState(inputControl, null, inputParameters, parameterTypes, reportInputControlInformation).getOptions();
        // state should have options
        assertNotNull(options);
        assertFalse("options not same list from db", options.size() == listOfValues.size() + 1);
        assertTrue("option same list of defaults", options.contains(defaultItems.get(0)));

        // options number is one item more then values because of nothing selected item.
        assertEquals(defaultItems.size(), options.size());
        List<InputControlOption> selectedOptions = new ArrayList<InputControlOption>();
        for (InputControlOption currentOption : options) {
            if (currentOption.hasSelected()) {
                selectedOptions.add(currentOption);
            }
        }
        // only one item is selected
        assertEquals(selectedOptions.size(), 1);

    }

    @Test
    public void getStateOfInputControlFromMultiDefaultValueOfDiagnosticDatasnapshot() throws Exception {
        final String parameterName = "testName";

        final List<String> valuesForDefault = new ArrayList<String>() {{
            add("USA");
            add("Canada");
            add("Mexico");
        }};

        final ListOrderedSet defaultICValues = new ListOrderedSet() {{
            addAll(valuesForDefault);
        }};

        final List<InputControlOption> defaultItems = listOfOptions(valuesForDefault, true);

        doReturn(parameterName).when(inputControl).getName();
        final HashMap<String, Class<?>> parameterTypes = new HashMap<>();
        Map<String, Object> inputParameters = map(
                entry(DiagnosticSnapshotPropertyHelper.ATTRIBUTE_IS_DIAG_SNAPSHOT, Boolean.TRUE),
                entry(parameterName, valuesForDefault.get(1)));

        doReturn(defaultICValues).when(reportInputControlInformation).getDefaultValue();

        final List<ListOfValuesItem> listOfValues = listOfValues(
                Integer.valueOf(-2), Integer.valueOf(-1),
                Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2));
        lenient().doReturn(listOfValues).when(loader).loadValues(eq(inputControl), nullable(ResourceReference.class), eq(inputParameters), eq(parameterTypes), eq(reportInputControlInformation), anyBoolean());


        final List<ListOfValuesItem> expectedResult = values;

        doReturn(expectedResult).when(loader).checkCriteriaAndAddItem(nullable(String.class), anyList(), any(ListOfValuesItem.class));
        doReturn(4).when(inputControlPagination).getLimit(nullable(InputControl.class), nullable(Map.class), nullable(Map.class));
        doReturn(1).when(inputControlPagination).getOffset(nullable(InputControl.class), nullable(Map.class), anyInt(), anyMap());


        // dummy data conversion
        doReturn(valuesForDefault.get(0)).when(dataConverterService).formatSingleValue(eq(valuesForDefault.get(0)), eq(inputControl), eq(reportInputControlInformation));
        doReturn(valuesForDefault.get(1)).when(dataConverterService).formatSingleValue(eq(valuesForDefault.get(1)), eq(inputControl), eq(reportInputControlInformation));
        doReturn(valuesForDefault.get(0)).when(dataConverterService).convertSingleValue(eq(valuesForDefault.get(0)), eq(inputControl), eq(reportInputControlInformation));
        doReturn(valuesForDefault.get(1)).when(dataConverterService).convertSingleValue(eq(valuesForDefault.get(1)), eq(inputControl), eq(reportInputControlInformation));
        ////////////tested method call/////////////////
        final List<InputControlOption> options = handler.getState(inputControl, null, inputParameters, parameterTypes, reportInputControlInformation).getOptions();
        // state should have options
        assertNotNull(options);
        assertFalse("options not same list from db", options.size() == listOfValues.size() + 1);
        assertTrue("option same list of defaults", options.contains(defaultItems.get(1)));

        // options number is size minus offset
        assertEquals(2, options.size());
        List<InputControlOption> selectedOptions = new ArrayList<>();
        for (InputControlOption currentOption : options) {
            if (currentOption.hasSelected()) {
                selectedOptions.add(currentOption);
            }
        }
        // only one item is selected
        assertEquals(selectedOptions.size(), 1);
    }

    @Test
    public void setStateTotalCount_withValidCount() {
        InputControlState inputControlState = new InputControlState();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("totalCount", "10");

        handler.setStateTotalCount(inputControlState, parameters);
        assertEquals("10", inputControlState.getTotalCount());
    }

    @Test
    public void setStateTotalCount_withNullCount() {
        InputControlState inputControlState = new InputControlState();

        Map<String, Object> parameters = new HashMap<>();

        handler.setStateTotalCount(inputControlState, parameters);
        assertEquals(null, inputControlState.getTotalCount());
    }

    @Test
    public void setIncomingValue_withValidSelectedValue() {
        ReportInputControlInformation info = mock(ReportInputControlInformation.class);
        when(info.getDefaultValue()).thenReturn("USA");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("Country_select", "selectedValues");
        parameters.put("Country", "Canada");
        assertEquals("USA", handler.setIncomingValue("Country", parameters, info));
    }

    @Test
    public void setIncomingValue_withNoSelectedValue() {
        ReportInputControlInformation info = mock(ReportInputControlInformation.class);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("Country", "Canada");

        assertEquals("Canada", handler.setIncomingValue("Country", parameters, info));
    }

    @Test
    public void getDefaultValuesList_withCollectionValue() {
        ReportInputControlInformation info = mock(ReportInputControlInformation.class);
        List<Object> list = new ArrayList<>();
        list.add("USA");

        ListOrderedSet orderedSet = new ListOrderedSet();
        orderedSet.add("USA");
        when(info.getDefaultValue()).thenReturn(orderedSet);

        assertEquals(list, handler.getDefaultValuesList(info));
    }

    @Test
    public void getDefaultValuesList_withSingleValue() {
        ReportInputControlInformation info = mock(ReportInputControlInformation.class);
        List<Object> list = new ArrayList<>();
        list.add("USA");

        when(info.getDefaultValue()).thenReturn("USA");
        assertEquals(list, handler.getDefaultValuesList(info));
    }

    @Test
    public void generateValuesFromDefaultValues_withValidOffsetLimit() {
        ListOrderedSet orderedSet = new ListOrderedSet();
        orderedSet.add("USA");
        orderedSet.add("Canada");
        orderedSet.add("Mexico");

        ReportInputControlInformation info = new JasperReportInputControlInformation();
        info.setDefaultValue(orderedSet);

        QueryValuesLoader valuesLoader = new QueryValuesLoader();
        handler.setLoader(valuesLoader);

        List<ListOfValuesItem> result = handler.generateValuesFromDefaultValues(info, "");
        assertEquals("USA", result.get(0).getValue());
    }

    @Test
    public void SelectedValuesDict_withCollection() {

        List<Object> defaultValue = Arrays.asList(new Object[]{10.20, "String"});
        SingleSelectListInputControlHandler.SelectedValuesDict selectedValuesDict = handler.createSelectedValuesDict(defaultValue);
        assertTrue(selectedValuesDict.checkMatch(10.20));
        assertTrue(selectedValuesDict.checkMatch("String"));
    }

    @Test
    public void SelectedValuesDict_withSingleIncomingValue() {

        Double defaultValue = 10.0;
        SingleSelectListInputControlHandler.SelectedValuesDict selectedValuesDict = handler.createSelectedValuesDict(defaultValue);
        assertTrue(selectedValuesDict.checkMatch(10));

        doReturn("10").when(dataConverterService).formatSingleValue("10", null, (Class) null);
        assertFalse(selectedValuesDict.checkMatch("10"));

        doThrow(new IllegalStateException()).when(dataConverterService).formatSingleValue("invalid", null, (Class) null);
        assertFalse(selectedValuesDict.checkMatch("invalid"));
    }

    @Test
    public void SelectedValuesDict_withSingle_CaseSensitive() {
        assertTrue(handler.matches("abc", "abc"));
    }

    @Test
    public void SelectedValuesDict_withSingle_CaseInSensitive() {
        handler.setCaseSensitive(false);
        doAnswer(invocation -> invocation.getArgument(0)).when(dataConverterService)
                .formatSingleValue(anyString(), nullable(DataType.class), nullable(Class.class));
        assertTrue(handler.matches("abc", "aBc"));
    }

    @Test
    public void SelectedValuesDict_withCollection_CaseInSensitive() {
        handler.setCaseSensitive(false);
        List<Object> defaultValue = Arrays.asList(new String[]{"abc2", "efg2"});
        doAnswer(invocation -> invocation.getArgument(0)).when(dataConverterService)
                    .formatSingleValue(anyString(), nullable(DataType.class), nullable(Class.class));
        SingleSelectListInputControlHandler.SelectedValuesDict selectedValuesDict = handler.createSelectedValuesDict(defaultValue);
        assertTrue(selectedValuesDict.checkMatch("aBc2"));
        assertTrue(selectedValuesDict.checkMatch("EFG2"));
    }

    @Test
    public void SelectedValuesDict_withCollection_CaseSensitive() {
        List<Object> defaultValue = Arrays.asList(new Object[]{"abc", "efg"});
        SingleSelectListInputControlHandler.SelectedValuesDict selectedValuesDict = handler.createSelectedValuesDict(defaultValue);
        assertTrue(selectedValuesDict.checkMatch("abc"));
        assertFalse(selectedValuesDict.checkMatch("EFG"));
    }

    @Test
    public void SelectedValuesDict_withNullValue() {
        Object defaultValue = null;
        SingleSelectListInputControlHandler.SelectedValuesDict selectedValuesDict = handler.createSelectedValuesDict(defaultValue);
        assertTrue(selectedValuesDict.checkMatch(null));

        defaultValue = "string";
        selectedValuesDict = handler.createSelectedValuesDict(defaultValue);
        assertFalse(selectedValuesDict.checkMatch(null));

    }

    @Test
    public void offsetAndLimit_fillStateValue_paginatedOptions() throws CascadeResourceNotFoundException {
        Map<String, Object> parameters = new HashMap<>();
        setLimit(parameters, 2);
        setOffset(parameters, 2);

        mockLoadValues(values);

        handler.fillStateValue(state, inputControl, dataSource, parameters, reportInputControlInformation, Collections.emptyMap());

        assertEquals(1, state.getOptions().size());
        assertEquals("Mexico", state.getOptions().get(0).getLabel());
    }

    @Test
    public void onlyLimit_fillStateValue_paginatedOptions() throws CascadeResourceNotFoundException {
        Map<String, Object> parameters = new HashMap<>();
        setLimit(parameters, 1);

        mockLoadValues(values);

        handler.fillStateValue(state, inputControl, dataSource, parameters, reportInputControlInformation, Collections.emptyMap());

        assertEquals(1, state.getOptions().size());
        assertEquals("USA", state.getOptions().get(0).getLabel());
    }

    @Test(expected = InputControlValidationException.class)
    public void offsetBiggerThenLimit_fillStateValue_exception() throws CascadeResourceNotFoundException {
        Map<String, Object> parameters = new HashMap<>();
        setLimit(parameters, 3);
        setOffset(parameters, 3);

        mockLoadValues(values);

        handler.fillStateValue(state, inputControl, dataSource, parameters, reportInputControlInformation, Collections.emptyMap());
    }

    @Test
    public void selectedOnly_fillStateValue_selectedOptions() throws CascadeResourceNotFoundException {
        Map<String, Object> parameters = new HashMap<>();
        setIncomingValue(parameters, "Canada", inputControl);
        setSelectedOnly(parameters, true);

        mockLoadValues(values);

        handler.fillStateValue(state, inputControl, dataSource, parameters, reportInputControlInformation, Collections.emptyMap());

        assertEquals(1, state.getOptions().size());
        assertEquals("Canada", state.getOptions().get(0).getValue());
    }

    @Test
    public void selectedAllInParameters_fillStateValue_selectedOptions() throws CascadeResourceNotFoundException {
        Map<String, Object> parameters = new HashMap<>();
        setSelectedOnly(parameters, true);
        parameters.put(inputControl.getName() + SingleSelectListInputControlHandler.SELECT, InputControlHandler.ALL_VALUES);

        mockLoadValues(values);

        handler.fillStateValue(state, inputControl, dataSource, parameters, reportInputControlInformation, Collections.emptyMap());

        assertEquals(3, state.getOptions().size());
        assertEquals("USA", state.getOptions().get(0).getLabel());
        assertThat(Collections.nCopies(3, true), equalTo(state.getOptions().stream()
                .map(InputControlOption::isSelected).collect(Collectors.toList())));
    }

    @Test
    public void selectedAllInInputControl_fillStateValue_selectedOptions() throws CascadeResourceNotFoundException {
        Map<String, Object> parameters = new HashMap<>();
        setSelectedOnly(parameters, true);

        ReportInputControlValuesInformationImpl valuesInformation = new ReportInputControlValuesInformationImpl();
        valuesInformation.setAnyValue(true);
        doReturn(valuesInformation).when(reportInputControlInformation).getReportInputControlValuesInformation();

        mockLoadValues(values);

        handler.fillStateValue(state, inputControl, dataSource, parameters, reportInputControlInformation, Collections.emptyMap());

        assertEquals(3, state.getOptions().size());
        assertEquals("USA", state.getOptions().get(0).getLabel());
        assertThat(Collections.nCopies(3, true), equalTo(state.getOptions().stream()
                .map(InputControlOption::isSelected).collect(Collectors.toList())));
    }

    @Test
    public void selectedOnlyWhenIsAnyValueTrue_fillStateValue_selectedOptions() throws CascadeResourceNotFoundException {
        Map<String, Object> parameters = new HashMap<>();
        setIncomingValue(parameters, "Canada", inputControl);
        setSelectedOnly(parameters, true);

        ReportInputControlValuesInformationImpl valuesInformation = new ReportInputControlValuesInformationImpl();
        valuesInformation.setAnyValue(true);
        doReturn(valuesInformation).when(reportInputControlInformation).getReportInputControlValuesInformation();

        mockLoadValues(values);

        handler.fillStateValue(state, inputControl, dataSource, parameters, reportInputControlInformation, Collections.emptyMap());

        assertEquals(1, state.getOptions().size());
        assertEquals("Canada", state.getOptions().get(0).getLabel());
    }

    @Test
    public void selectedOnlyWithSelectedNothing_fillStateValue_selectedOptions() throws CascadeResourceNotFoundException {
        Map<String, Object> parameters = new HashMap<>();
        setSelectedOnly(parameters, true);

        ListOfValuesItem nothingItem = new ListOfValuesItemImpl();
        nothingItem.setLabel(NOTHING_SUBSTITUTION_LABEL);
        nothingItem.setValue(NOTHING_SUBSTITUTION_VALUE);
        values.add(nothingItem);

        mockLoadValues(values);

        handler.fillStateValue(state, inputControl, dataSource, parameters, reportInputControlInformation, Collections.emptyMap());

        assertEquals(1, state.getOptions().size());
        assertEquals(NOTHING_SUBSTITUTION_LABEL, state.getOptions().get(0).getLabel());
        assertEquals(NOTHING_SUBSTITUTION_VALUE, state.getOptions().get(0).getValue());
    }

    @Test
    public void selectedOnlyWithoutIncomingValueForNonMandatoryIC_fillStateValue_empty() throws CascadeResourceNotFoundException {
        Map<String, Object> parameters = new HashMap<>();
        setSelectedOnly(parameters, true);

        mockLoadValues(values);

        handler.fillStateValue(state, inputControl, dataSource, parameters, reportInputControlInformation, Collections.emptyMap());

        assertTrue(state.getOptions().isEmpty());
    }

    @Test
    public void selectedOnlyWithEmptyIncomingValueForMandatoryIC_fillStateValue_firstSelectedOption() throws CascadeResourceNotFoundException {
        Map<String, Object> parameters = new HashMap<>();
        setSelectedOnly(parameters, true);

        doReturn(true).when(inputControl).isMandatory();
        setIncomingValue(parameters, "", inputControl);

        mockLoadValues(values);

        handler.fillStateValue(state, inputControl, dataSource, parameters, reportInputControlInformation, Collections.emptyMap());

        assertEquals(1, state.getOptions().size());
        assertEquals("USA", state.getOptions().get(0).getValue());
    }

    @Test
    public void selectedOnlyWithDefaultValues_fillStateValue_defaultSelectedOption() throws CascadeResourceNotFoundException {
        Map<String, Object> parameters = new HashMap<>();
        setSelectedOnly(parameters, true);

        doReturn("Mexico").when(reportInputControlInformation).getDefaultValue();
        setIncomingValue(parameters, "", inputControl);

        mockLoadValues(values);

        handler.fillStateValue(state, inputControl, dataSource, parameters, reportInputControlInformation, Collections.emptyMap());

        assertEquals(1, state.getOptions().size());
        assertEquals("Mexico", state.getOptions().get(0).getValue());
    }

    @Test
    public void selectedOnlyWithoutAnyValuesAndMandatoryIC_fillStateValue_mandatoryICErrorMessage() throws CascadeResourceNotFoundException {
        final String errorMessage = "mandatoryICErrorMessage";
        Map<String, Object> parameters = new HashMap<>();
        setSelectedOnly(parameters, true);

        doReturn(errorMessage).when(messageSource).getMessage(anyString(), any(), any());
        doReturn(true).when(inputControl).isMandatory();
        mockLoadValues(values);

        handler.fillStateValue(state, inputControl, dataSource, parameters, reportInputControlInformation, Collections.emptyMap());

        assertEquals(errorMessage, state.getError());
    }

    @Test(expected = InputControlValidationException.class)
    public void offsetBiggerThenLimitAndTwoInvalidOptions_fillStateValue_exception() throws CascadeResourceNotFoundException {
        Map<String, Object> parameters = new HashMap<>();
        setLimit(parameters, 3);
        setOffset(parameters, 1);

        doThrow(InputControlValidationException.class).
                doThrow(InputControlValidationException.class).
                doNothing().when(handler).validateSingleValue(any(), any());

        mockLoadValues(values);

        handler.fillStateValue(state, inputControl, dataSource, parameters, reportInputControlInformation, Collections.emptyMap());
    }

    @Test
    public void valuesWithNothingOption_fillStateValue_nothingIsSelected() throws CascadeResourceNotFoundException {
        Map<String, Object> parameters = new HashMap<>();

        ListOfValuesItem nothingItem = listOfValuesItem(NOTHING_SUBSTITUTION_LABEL, NOTHING_SUBSTITUTION_VALUE);
        values.add(nothingItem);
        mockLoadValues(values);

        handler.fillStateValue(state, inputControl, dataSource, parameters, reportInputControlInformation, Collections.emptyMap());

        assertEquals(NOTHING_SUBSTITUTION_LABEL, state.getOptions().get(3).getLabel());
        assertTrue(state.getOptions().get(3).isSelected());
    }

    @Test
    public void valuesWithNothingOptionSelectedOnly_fillStateValue_nothingIsSelected() throws CascadeResourceNotFoundException {
        Map<String, Object> parameters = new HashMap<>();
        setSelectedOnly(parameters, true);

        ListOfValuesItem nothingItem = listOfValuesItem(NOTHING_SUBSTITUTION_LABEL, NOTHING_SUBSTITUTION_VALUE);
        values.add(nothingItem);
        mockLoadValues(values);

        handler.fillStateValue(state, inputControl, dataSource, parameters, reportInputControlInformation, Collections.emptyMap());

        assertEquals(NOTHING_SUBSTITUTION_LABEL, state.getOptions().get(0).getLabel());
        assertTrue(state.getOptions().get(0).isSelected());
    }


    @Test
    public void valuesWithNothingOptionAndUnknownValue_fillStateValue_nothingIsSelected() throws CascadeResourceNotFoundException {
        Map<String, Object> parameters = new HashMap<>();

        setIncomingValue(parameters, "NotFound", inputControl);
        doReturn(false).when(inputControl).isMandatory();

        ListOfValuesItem nothingItem = listOfValuesItem(NOTHING_SUBSTITUTION_LABEL, NOTHING_SUBSTITUTION_VALUE);
        values.add(nothingItem);
        mockLoadValues(values);

        handler.fillStateValue(state, inputControl, dataSource, parameters, reportInputControlInformation, Collections.emptyMap());

        assertEquals(NOTHING_SUBSTITUTION_LABEL, state.getOptions().get(3).getLabel());
        assertTrue(state.getOptions().get(3).isSelected());
    }

    @Test
    public void valuesWithNothingOptionAndUnknownValueSelectedOnly_fillStateValue_nothingIsSelected() throws CascadeResourceNotFoundException {
        Map<String, Object> parameters = new HashMap<>();

        setIncomingValue(parameters, "NotFound", inputControl);
        setSelectedOnly(parameters, true);
        doReturn(false).when(inputControl).isMandatory();

        ListOfValuesItem nothingItem = listOfValuesItem(NOTHING_SUBSTITUTION_LABEL, NOTHING_SUBSTITUTION_VALUE);
        values.add(nothingItem);
        mockLoadValues(values);

        handler.fillStateValue(state, inputControl, dataSource, parameters, reportInputControlInformation, Collections.emptyMap());

        assertEquals(NOTHING_SUBSTITUTION_LABEL, state.getOptions().get(0).getLabel());
        assertTrue(state.getOptions().get(0).isSelected());
    }

    @Test
    public void nullValues_fillStateValue_empty() throws CascadeResourceNotFoundException {
        mockLoadValues(null);

        handler.fillStateValue(state, inputControl, dataSource, new HashMap<>(), reportInputControlInformation, Collections.emptyMap());

        assertTrue(state.getOptions().isEmpty());
    }

}
