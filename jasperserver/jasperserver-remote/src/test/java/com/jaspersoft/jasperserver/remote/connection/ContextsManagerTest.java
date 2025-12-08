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

package com.jaspersoft.jasperserver.remote.connection;

import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributesResolver;
import com.jaspersoft.jasperserver.core.util.type.GenericTypeProcessorRegistry;
import com.jaspersoft.jasperserver.remote.common.JrsBeanValidator;
import com.jaspersoft.jasperserver.remote.connection.storage.ContextDataStorage;
import com.jaspersoft.jasperserver.remote.connection.storage.ContextDataPair;
import com.jaspersoft.jasperserver.remote.exception.MandatoryParameterNotFoundException;
import com.jaspersoft.jasperserver.remote.exception.OperationCancelledException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testng.annotations.BeforeMethod;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@RunWith(MockitoJUnitRunner.class)
public class ContextsManagerTest {
    @InjectMocks
    private ContextsManager contextsManager = new ContextsManager();
    private ContextsManager spy;
    @Mock
    private ContextExecutorService contextExecutorService;
    @Mock
    private ContextDataStorage contextDataStorage;
    @Mock
    private GenericTypeProcessorRegistry genericTypeProcessorRegistry;
    @Mock
    private ProfileAttributesResolver profileAttributesResolver;
    @Mock
    private JrsBeanValidator jrsBeanValidator;

    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);
        spy = Mockito.spy(contextsManager);
    }

    @BeforeMethod
    public void refresh(){
        reset(contextExecutorService, spy, contextDataStorage, genericTypeProcessorRegistry,
                profileAttributesResolver, jrsBeanValidator);
    }

    @Test
    public void removeConnection_contextIsNull_noStrategyCallButWithStorageDeleteAndExecutorCancel(){
        final UUID uuid = UUID.randomUUID();
        // line below declares behaviour, that is default behaviour of mock, but let's keep it for better understanding of given
        doReturn(null).when(contextDataStorage).get(uuid, false);

        spy.removeContext(uuid);

        verify(spy, never()).getStrategy(any(ContextDataPair.class));
        verify(contextDataStorage).delete(uuid);
        verify(contextExecutorService).cancelContext(uuid);
    }

    @Test
    public void removeConnection_contextExists_removeActionsAreDone(){
        final UUID uuid = UUID.randomUUID();
        final Object context = new Object();
        final HashMap<String, Object> data = new HashMap<String, Object>();
        final ContextDataPair pair = new ContextDataPair(context, data);
        doReturn(pair).when(contextDataStorage).get(uuid, false);
        final ContextManagementStrategy strategy = mock(ContextManagementStrategy.class);
        doReturn(strategy).when(spy).getStrategy(pair);

        spy.removeContext(uuid);

        verify(strategy).deleteContext(context, data);
        verify(contextDataStorage).delete(uuid);
        verify(contextExecutorService).cancelContext(uuid);
    }

    @Test
    public void removeConnection_contextNonExists_removeActionsAreDone(){
        final UUID uuid = UUID.randomUUID();
        spy.removeContext(uuid);
        verify(contextDataStorage).get(uuid, false);
        verify(contextDataStorage).delete(uuid);
    }

    @Test
    public void removeConnection_strategyNonExists_removeActionsAreDone(){
        final UUID uuid = UUID.randomUUID();
        final Object context = new Object();
        final HashMap<String, Object> data = new HashMap<String, Object>();
        final ContextDataPair pair = new ContextDataPair(context, data);
        doReturn(pair).when(contextDataStorage).get(uuid, false);

        spy.removeContext(uuid);

        verify(spy).getStrategy(pair);
        verify(contextDataStorage).delete(uuid);
        verify(contextExecutorService).cancelContext(uuid);
    }

    @Test
    public void getConnectionMetadata_withParamsMap() throws Exception {
        final UUID uuid = UUID.randomUUID();
        final HashMap<String, String[]> options = new HashMap<String, String[]>();
        final Object context = new Object();
        final HashMap<String, Object> data = new HashMap<String, Object>();
        when(contextDataStorage.get(uuid)).thenReturn(new ContextDataPair(context, data));
        final ContextMetadataBuilder contextMetadataBuilder = mock(ContextMetadataBuilder.class);
        when(genericTypeProcessorRegistry.getTypeProcessor(context.getClass(), ContextMetadataBuilder.class, false))
                .thenReturn(contextMetadataBuilder);
        final Object mergedContext = new Object();
        when(profileAttributesResolver.mergeObject(same(context), eq(uuid.toString()))).thenReturn(mergedContext);
        final ArgumentCaptor<Callable> callableCaptor = ArgumentCaptor.forClass(Callable.class);
        final Object expectedResult = new Object();
        doReturn(expectedResult).when(spy).callAndGet(same(uuid), callableCaptor.capture());
        final Object expectedCallableResult = new Object();
        doReturn(expectedCallableResult).when(contextMetadataBuilder).build(mergedContext, options, data);

        final Object result = spy.getContextMetadata(uuid, options);

        verify(spy).resolveAttributes(options);
        assertSame(result, expectedResult);
        verify(contextMetadataBuilder, never()).build(any(Object.class), anyMap(), anyMap());
        final Callable callable = callableCaptor.getValue();
        assertNotNull(callable);
        assertSame(callable.call(), expectedCallableResult);
    }

    @Test
    public void getConnectionMetadata_withParamsObject() throws Exception {
        final UUID uuid = UUID.randomUUID();
        final String uuidString = uuid.toString();
        final Object metadataParams = new Object();
        final Object context = new Object();
        final HashMap<String, Object> data = new HashMap<String, Object>();
        when(contextDataStorage.get(uuid)).thenReturn(new ContextDataPair(context, data));
        final ContextParametrizedMetadataBuilder contextMetadataBuilder = mock(ContextParametrizedMetadataBuilder.class);
        when(genericTypeProcessorRegistry.getTypeProcessor(context.getClass(), ContextParametrizedMetadataBuilder.class, false))
                .thenReturn(contextMetadataBuilder);
        final Object mergedContext = new Object();
        when(profileAttributesResolver.mergeObject(same(context), eq(uuidString))).thenReturn(mergedContext);
        final Object mergedMetadataParams = new Object();
        when(profileAttributesResolver.mergeObject(same(metadataParams), eq(uuidString))).thenReturn(mergedMetadataParams);
        final ArgumentCaptor<Callable> callableCaptor = ArgumentCaptor.forClass(Callable.class);
        final Object expectedResult = new Object();
        doReturn(expectedResult).when(spy).callAndGet(same(uuid), callableCaptor.capture());
        final Object expectedCallableResult = new Object();
        doReturn(expectedCallableResult).when(contextMetadataBuilder).build(mergedContext, mergedMetadataParams, data);

        final Object result = spy.getContextMetadata(uuid, metadataParams);

        assertSame(result, expectedResult);
        verify(contextMetadataBuilder, never()).build(any(Object.class), any(Object.class), anyMap());
        final Callable callable = callableCaptor.getValue();
        assertNotNull(callable);
        assertSame(callable.call(), expectedCallableResult);
    }

    @Test
    public void callAndGet_success() throws ExecutionException, InterruptedException {
        final UUID contextUuid = UUID.randomUUID();
        final Callable callable = mock(Callable.class);
        final Future future = mock(Future.class);
        final Object expectedResult = new Object();
        when(future.get()).thenReturn(expectedResult);
        when(contextExecutorService.runWithContext(contextUuid, callable)).thenReturn(future);

        final Object result = contextsManager.callAndGet(contextUuid, callable);

        assertSame(result, expectedResult);
    }

    @Test
    public void callAndGet_CancellationException_OperationCancelledException() throws ExecutionException, InterruptedException {
        final UUID contextUuid = UUID.randomUUID();
        final Callable callable = mock(Callable.class);
        final Future future = mock(Future.class);
        when(future.get()).thenThrow(new CancellationException());
        when(contextExecutorService.runWithContext(contextUuid, callable)).thenReturn(future);
        Exception exception = null;
        try {
            contextsManager.callAndGet(contextUuid, callable);
        } catch (Exception e) {
            exception = e;
        }

        assertTrue(exception instanceof OperationCancelledException);
    }

    @Test
    public void callAndGet_ExecutionExceptionWithNonRuntimeException_JSExceptionWrapper_withCause() throws ExecutionException, InterruptedException {
        final UUID contextUuid = UUID.randomUUID();
        final Callable callable = mock(Callable.class);
        final Future future = mock(Future.class);
        final Exception expectedCause = new Exception();
        when(future.get()).thenThrow(new ExecutionException(expectedCause));
        when(contextExecutorService.runWithContext(contextUuid, callable)).thenReturn(future);
        Exception exception = null;
        try {
            contextsManager.callAndGet(contextUuid, callable);
        } catch (Exception e) {
            exception = e;
        }

        assertTrue(exception instanceof JSExceptionWrapper);
        assertSame(exception.getCause(), expectedCause);
    }

    @Test
    public void callAndGet_ExecutionExceptionWithRuntimeException_runtimeExceptionIsThrown() throws ExecutionException, InterruptedException {
        final UUID contextUuid = UUID.randomUUID();
        final Callable callable = mock(Callable.class);
        final Future future = mock(Future.class);
        final Exception expected = new RuntimeException();
        when(future.get()).thenThrow(new ExecutionException(expected));
        when(contextExecutorService.runWithContext(contextUuid, callable)).thenReturn(future);
        Exception exception = null;
        try {
            contextsManager.callAndGet(contextUuid, callable);
        } catch (Exception e) {
            exception = e;
        }

        assertSame(exception, expected);
    }

    @Test
    public void callAndGet_Exception_JSExceptionWrapper_withSourceException() throws ExecutionException, InterruptedException {
        final UUID contextUuid = UUID.randomUUID();
        final Callable callable = mock(Callable.class);
        final Future future = mock(Future.class);
        final RuntimeException expectedCause = new RuntimeException();
        when(future.get()).thenThrow(expectedCause);
        when(contextExecutorService.runWithContext(contextUuid, callable)).thenReturn(future);
        Exception exception = null;
        try {
            contextsManager.callAndGet(contextUuid, callable);
        } catch (Exception e) {
            exception = e;
        }

        assertTrue(exception instanceof JSExceptionWrapper);
        assertSame(exception.getCause(), expectedCause);
    }

    @Test
    public void executeQuery() throws Exception {
        final UUID uuid = UUID.randomUUID();
        final Object query = new Object();
        final HashMap<String, String[]> queryParameters = new HashMap<String, String[]>();
        final Object context = new Object();
        final HashMap<String, Object> data = new HashMap<String, Object>();
        when(contextDataStorage.get(uuid)).thenReturn(new ContextDataPair(context, data));
        final ContextQueryExecutor queryExecutor = mock(ContextQueryExecutor.class);
        doReturn(queryExecutor).when(spy).getQueryExecutor(query, context);
        final Object queryExecutorResult = new Object();
        doReturn(queryExecutorResult).when(queryExecutor).executeQuery(query, context, queryParameters, data);
        final ArgumentCaptor<Callable> callableCaptor = ArgumentCaptor.forClass(Callable.class);
        final Object expectedResult = new Object();
        doReturn(expectedResult).when(spy).callAndGet(same(uuid), callableCaptor.capture());

        final Object result = spy.executeQuery(uuid, query, queryParameters);

        assertSame(result, expectedResult);
        verify(jrsBeanValidator).validate(query);
        verify(queryExecutor, never()).executeQuery(any(), any(), any(), any());
        final Callable callable = callableCaptor.getValue();
        assertSame(callable.call(), queryExecutorResult);
    }

    @Test
    public void executeQueryForMetadata() throws Exception {
        final UUID uuid = UUID.randomUUID();
        final Object query = new Object();
        final Object context = new Object();
        final HashMap<String, Object> data = new HashMap<String, Object>();
        when(contextDataStorage.get(uuid)).thenReturn(new ContextDataPair(context, data));
        final ContextQueryExecutor queryExecutor = mock(ContextQueryExecutor.class);
        doReturn(queryExecutor).when(spy).getQueryExecutor(query, context);
        final Object queryExecutorResult = new Object();
        doReturn(queryExecutorResult).when(queryExecutor).executeQueryForMetadata(query, context, data);
        final ArgumentCaptor<Callable> callableCaptor = ArgumentCaptor.forClass(Callable.class);
        final Object expectedResult = new Object();
        doReturn(expectedResult).when(spy).callAndGet(same(uuid), callableCaptor.capture());

        final Object result = spy.executeQueryForMetadata(uuid, query);

        assertSame(result, expectedResult);
        verify(jrsBeanValidator).validate(query);
        verify(queryExecutor, never()).executeQueryForMetadata(any(), any(), any());
        final Callable callable = callableCaptor.getValue();
        assertSame(callable.call(), queryExecutorResult);
    }

    @Test(expected = MandatoryParameterNotFoundException.class)
    public void createRawContext() {
        spy.createRawContext(null);
    }

    @Test(expected = MandatoryParameterNotFoundException.class)
    public void updateRawContext() {
        spy.updateRawContext(UUID.randomUUID(), null);
    }
}
