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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.jaspersoft.jasperserver.api.JSMissingDataSourceFieldsException;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Query;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.QueryImpl;
import com.jaspersoft.jasperserver.inputcontrols.cascade.CachedEngineService;
import com.jaspersoft.jasperserver.inputcontrols.cascade.CachedRepositoryService;
import com.jaspersoft.jasperserver.inputcontrols.cascade.CascadeResourceNotFoundException;
import com.jaspersoft.jasperserver.inputcontrols.cascade.token.FilterResolver;
import com.jaspersoft.jasperserver.inputcontrols.cascade.token.ParameterTypeLookup;
import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.map.LinkedMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Stubber;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.EhcacheEngineService.DIAGNOSTIC_REPORT_URI;
import static com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.EhcacheEngineService.DIAGNOSTIC_STATE;
import static com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.EhcacheEngineService.IC_REFRESH_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * <p></p>
 *
 * @author Vlad Zavadskyi
 */
@RunWith(MockitoJUnitRunner.class)
public class QueryValuesLoaderStrategyTest {
    @Spy
    @InjectMocks
    private QueryValuesLoaderStrategy strategy;
    @Mock
    private CachedEngineService cachedEngineService;
    @Mock
    private InputControlDataSourceResolver inputControlDataSourceResolver;
    @Mock
    private CachedRepositoryService cachedRepositoryService;
    @Mock
    private FilterResolver filterResolver;
    @Mock
    private ParameterTypeLookup parameterTypeCompositeLookup;

    private ResourceReference dataSourceReference = mock(ResourceReference.class);

    private ResourceReference resolvedDataSourceReference = mock(ResourceReference.class);

    private ResourceReference queryReference = mock(ResourceReference.class);

    private InputControl inputControl = mock(InputControl.class);

    private Query query = mock(Query.class);

    private OrderedMap orderedMap = new LinkedMap();

    private Map<String, Object> parameters = new HashMap<>();

    private Map<String, Class<?>> parameterTypes = new HashMap<>();

    private Map<String, Object> domainSchemaParameters = new HashMap<>();

    private final String criteria = "criteria";

    @Captor
    private ArgumentCaptor<Map<String, Object>> executionParametersCaptor;
    @Captor
    private ArgumentCaptor<Map<String, Class<?>>> executionParameterTypesCaptor;
    @Captor
    private ArgumentCaptor<Set<String>> parametersNameCaptor;

    @Before
    public void setUp() throws Exception {
        doReturn(queryReference).when(inputControl).getQuery();
        doReturn(resolvedDataSourceReference).when(inputControlDataSourceResolver).resolveDatasource(eq(inputControl), eq(dataSourceReference));
        doReturn(query).when(cachedRepositoryService).getResource(eq(Query.class), eq(queryReference));
        doReturn(domainSchemaParameters).when(inputControlDataSourceResolver).prepareDomainDataSourceParameters(eq(resolvedDataSourceReference));
    }

    @Test
    public void resultsOrderedMap_filtersParametersAndReturnsOrderMap() throws Exception {
        Map<String, Object> executionParameters = new HashMap<String, Object>() {{
            put("exec", "execValue");
        }};
        Map<String, Class<?>> executionParameterTypes = new HashMap<String, Class<?>>() {{
            put("e1type", String.class);
        }};
        Map<String, Class<?>> missingParameterTypes = new HashMap<String, Class<?>>() {{
            put("m1type", String.class);
            put("m2type", String.class);
        }};

        doReturn(executionParameters).when(strategy).filterAndFillMissingQueryParameters(eq(query), eq(parameters), eq(domainSchemaParameters));
        doReturn(executionParameterTypes).when(strategy).filterParameterTypes(anySet(), eq(parameterTypes));
        doReturn(missingParameterTypes).when(strategy).findMissingParameterTypes(eq(dataSourceReference), eq(executionParameters), eq(executionParameterTypes));
        doReturn(orderedMap).when(cachedEngineService).executeQuery(
                any(ExecutionContext.class), nullable(ResourceReference.class), nullable(String.class), nullable(String[].class),
                nullable(ResourceReference.class), executionParametersCaptor.capture(), executionParameterTypesCaptor.capture(), nullable(String.class)
        );

        ResultsOrderedMap result = strategy.resultsOrderedMap(inputControl, dataSourceReference, criteria, parameters, parameterTypes);

        verify(strategy).filterAndFillMissingQueryParameters(eq(query), eq(parameters), eq(domainSchemaParameters));
        verify(strategy).filterParameterTypes(anySet(), eq(parameterTypes));
        verify(strategy).findMissingParameterTypes(eq(dataSourceReference), eq(executionParameters), eq(executionParameterTypes));

        assertNotNull(result);
        assertEquals(orderedMap, result.getOrderedMap());
        assertEquals("execValue", executionParametersCaptor.getValue().get("exec"));
        assertEquals(String.class, executionParameterTypesCaptor.getValue().get("m1type"));
        assertEquals(String.class, executionParameterTypesCaptor.getValue().get("m2type"));
        assertEquals(String.class, executionParameterTypesCaptor.getValue().get("e1type"));
    }

    @Test
    public void resultsOrderedMap_executionParametersContainsEhcacheProperties() throws Exception {
        parameters.put(IC_REFRESH_KEY, Boolean.TRUE.toString());
        parameters.put(DIAGNOSTIC_REPORT_URI, "/uri");
        parameters.put(DIAGNOSTIC_STATE, Boolean.TRUE.toString());

        Map<String, Object> executionParameters = new HashMap<>();

        mockExecuteQuery(() -> doReturn(orderedMap));
        doReturn(executionParameters).when(strategy).filterAndFillMissingQueryParameters(eq(query), eq(parameters), eq(domainSchemaParameters));
        ResultsOrderedMap result = strategy.resultsOrderedMap(inputControl, dataSourceReference, criteria, parameters, parameterTypes);

        assertNotNull(result);
        assertEquals(orderedMap, result.getOrderedMap());
        assertEquals(Boolean.TRUE.toString(), executionParameters.get(IC_REFRESH_KEY));
        assertEquals("/uri", executionParameters.get(DIAGNOSTIC_REPORT_URI));
        assertEquals(Boolean.TRUE.toString(), executionParameters.get(IC_REFRESH_KEY));
    }

    @Test
    public void resultsOrderedMap_dataSourceException_null() throws Exception {
        mockExecuteQuery(() -> doThrow(JSMissingDataSourceFieldsException.class));
        ResultsOrderedMap result = strategy.resultsOrderedMap(inputControl, dataSourceReference, criteria, parameters, parameterTypes);

        assertNotNull(result);
        assertNull(result.getOrderedMap());
    }

    @Test
    public void resultsOrderedMap_illegalArgumentException_null() throws Exception {
        mockExecuteQuery(() -> doThrow(JSMissingDataSourceFieldsException.class));
        ResultsOrderedMap result = strategy.resultsOrderedMap(inputControl, dataSourceReference, criteria, parameters, parameterTypes);

        assertNotNull(result);
        assertNull(result.getOrderedMap());
    }

    @Test
    public void filterParameterTypes_emptyParametersAndTypes_emptyTypes() {
        Map<String, Class<?>> result = strategy.filterParameterTypes(Collections.emptySet(), parameterTypes);
        assertTrue(result.isEmpty());
    }

    @Test
    public void filterParameterTypes_someParametersAndTypes_filteredTypes() {
        parameterTypes.put("param1", String.class);
        parameterTypes.put("param2", Collection.class);
        parameterTypes.put("param3", BigDecimal.class);
        Map<String, Class<?>> result = strategy.filterParameterTypes(ImmutableSet.of("param1", "param2"), parameterTypes);
        assertEquals(2, result.size());
        assertEquals(String.class, result.get("param1"));
        assertEquals(Collection.class, result.get("param2"));
        assertFalse(result.containsKey("param3"));
    }

    @Test
    public void findMissingParameterTypes_emptyParametersAndTypes_emptyMissingTypes() throws CascadeResourceNotFoundException {
        Map<String, Class<?>> result = strategy.findMissingParameterTypes(dataSourceReference, parameters, parameterTypes);
        assertTrue(result.isEmpty());
    }

    @Test
    public void findMissingParameterTypes_missingOneParameter_resolvedParametertype() throws CascadeResourceNotFoundException {
        parameters.put("missing1param", "missing1obj");
        parameters.put("presentParam", "presentObject");
        parameterTypes.put("presentParam", String.class);
        Map<String, Class<?>> missingParameters = ImmutableMap.of("missing1param", String.class, "anotherMissingParam", Collection.class);

        doReturn(missingParameters).when(parameterTypeCompositeLookup)
                .getParameterTypes(any(ExecutionContext.class), eq(dataSourceReference), parametersNameCaptor.capture());

        Map<String, Class<?>> result = strategy.findMissingParameterTypes(dataSourceReference, parameters, parameterTypes);

        assertEquals(ImmutableSet.of("missing1param"), parametersNameCaptor.getValue());
        assertEquals(missingParameters, result);
    }

    @Test
    public void createNewQueryImplWithNewSQL() {
        QueryImpl queryImpl = new QueryImpl();
        queryImpl.setLanguage("SQL");
        queryImpl.setName("name");
        queryImpl.setLabel("label");
        queryImpl.setSql("old sql");
        queryImpl.setParentFolder("folder");
        QueryImpl newQueryImpl = strategy.createNewQueryImplWithNewSQL(queryImpl, "new sql");

        assertEquals(newQueryImpl.getLanguage(), queryImpl.getLanguage());
        assertEquals(newQueryImpl.getName(), queryImpl.getName());
        assertEquals(newQueryImpl.getParentFolder(), queryImpl.getParentFolder());
        assertEquals(newQueryImpl.getLabel(), queryImpl.getLabel());
        assertEquals(newQueryImpl.getSql(), "new sql");
    }

    private void mockExecuteQuery(Supplier<Stubber> mockSupplier) throws CascadeResourceNotFoundException {
        mockSupplier.get().when(cachedEngineService).executeQuery(
                any(ExecutionContext.class), nullable(ResourceReference.class), nullable(String.class), nullable(String[].class),
                nullable(ResourceReference.class), nullable(Map.class), nullable(Map.class), nullable(String.class)
        );
    }
}