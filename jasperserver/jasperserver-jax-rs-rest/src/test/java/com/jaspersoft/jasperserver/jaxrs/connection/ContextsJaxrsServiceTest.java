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
package com.jaspersoft.jasperserver.jaxrs.connection;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.common.error.handling.ExceptionOutputManager;
import com.jaspersoft.jasperserver.api.common.error.handling.SecureExceptionHandlerImpl;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.remote.connection.ContextCreationFailedException;
import com.jaspersoft.jasperserver.remote.exception.ExportExecutionRejectedException;
import com.jaspersoft.jasperserver.remote.services.ReportExecution;
import org.springframework.context.MessageSource;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.jaspersoft.jasperserver.jaxrs.resources.ContentNegotiationHandler;
import com.jaspersoft.jasperserver.remote.connection.ContextsManager;
import com.jaspersoft.jasperserver.remote.exception.NotAcceptableException;
import com.jaspersoft.jasperserver.remote.exception.ResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.exception.UnsupportedMediaTypeException;
import com.jaspersoft.jasperserver.remote.exception.UnsupportedOperationErrorDescriptorException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
public class ContextsJaxrsServiceTest {
    @InjectMocks
    private ContextsJaxrsService service = new ContextsJaxrsService();
    @Mock
    private Providers providers;
    @Mock
    private HttpHeaders httpHeaders;
    @Mock
    private HttpServletRequest request;
    @Mock
    private ContextsManager contextsManager;
    @Mock
    private InputStream inputStream;
    @Mock
    private ContentNegotiationHandler contentNegotiationHandler;
    @Mock
    UriInfo uriInfo;
    private ContextsJaxrsService spyService;

    ExecutionContext ctx = ExecutionContextImpl.getRuntimeExecutionContext();
    @Mock
    private MessageSource messageSourceMock;

    @Mock
    private ExceptionOutputManager outputManagerMock;

    private static final String SERVER_ERROR_MESSAGE = "RevenueDetailReport.pdf " +
            "は、リポジトリには保存されませんでした。エラーにより処理が中断されました。";
    private SecureExceptionHandlerImpl exceptionHandler;
    @BeforeClass
    public void init() {
        MockitoAnnotations.initMocks(this);
        exceptionHandler = new SecureExceptionHandlerImpl();
        exceptionHandler.setExceptionOutputManager(outputManagerMock);
        exceptionHandler.setMessageSource(messageSourceMock);
        spyService = spy(service);
    }

    @BeforeMethod
    public void resetMocks() {
        reset(providers, httpHeaders, request, contextsManager, inputStream, spyService);
    }

    @Test
    public void getConnectionClass() {
        final String connectionType = "lfc";
        final String type = "application/connections." + connectionType + "+json";
        Class expectedClass = Number.class;
        when(contextsManager.getContextDescriptionClass(connectionType)).thenReturn(expectedClass);
        final Class<?> result = service.getContextClass(MediaType.valueOf(type));
        assertSame(result, expectedClass);
    }

    @Test(expectedExceptions = WebApplicationException.class)
    public void getConnectionClass_notExistentType_exception() {
        service.getContextClass(MediaType.valueOf("application/connections.notExistentType+xml"));
    }

    @Test(expectedExceptions = UnsupportedMediaTypeException.class)
    public void getConnectionMetadata_notSupportedType_exception() throws IOException {
        doReturn(null).when(contextsManager).getMetadataParamsClass("application/connections.notExistentType+xml");
        service.getContextMetadata(UUID.randomUUID().toString(), inputStream, MediaType.valueOf("application/connections.notExistentType+xml"));
    }

    @Test(expectedExceptions = UnsupportedMediaTypeException.class)
    public void getConnectionMetadata_ContextNotSupportsMetadata_exception() throws IOException {
        final Class<Number> connectionClass = Number.class;
        final Object expectedConnectionObject = new Object();
        final MediaType mediaType = MediaType.valueOf("application/connections.notExistentType+xml");
        doReturn(connectionClass).when(contextsManager).getMetadataParamsClass("notExistentType");
        doReturn(expectedConnectionObject).when(spyService).parseEntity(connectionClass, inputStream, mediaType);
        doThrow(UnsupportedOperationErrorDescriptorException.class).when(contextsManager).getContextMetadata(any(UUID.class), eq(expectedConnectionObject));

        spyService.getContextMetadata(UUID.randomUUID().toString(), inputStream, mediaType);
    }


    @Test
    public void getConnectionMetadata_metadataReturned() throws IOException {
        final Class<Number> connectionClass = Number.class;
        final Object expectedConnectionObject = new Object();
        final MediaType mediaType = MediaType.valueOf("application/connections.notExistentType+xml");
        doReturn(connectionClass).when(contextsManager).getMetadataParamsClass("notExistentType");
        doReturn(expectedConnectionObject).when(spyService).parseEntity(connectionClass, inputStream, mediaType);
        doReturn(new Object()).when(contextsManager).getContextMetadata(any(UUID.class), eq(expectedConnectionObject));

        final Response response = spyService.getContextMetadata(UUID.randomUUID().toString(), inputStream, mediaType);
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test(expectedExceptions = WebApplicationException.class)
    public void getConnectionClass_nullMediaType_exception() {
        service.getContextClass(null);
    }

    @Test
    public void createConnection_establish() throws Exception {
        final MediaType connectionType = MediaType.valueOf("application/connections.type+json");
        final MediaType acceptType = MediaType.valueOf("application/json");
        final Class<Number> connectionClass = Number.class;
        doReturn(connectionClass).when(spyService).getContextClass(connectionType);
        final Object expectedConnectionObject = new Object();
        doReturn(expectedConnectionObject).when(spyService).parseEntity(connectionClass, inputStream, connectionType);
        final UUID expectedUuid = UUID.randomUUID();
        when(contextsManager.createContext(expectedConnectionObject)).thenReturn(expectedUuid);
        final Object contextFromContextManager = new Object();
        final HashMap<String, String[]> parametersMap = new HashMap<String, String[]>();
        when(contextsManager.getContext(expectedUuid, parametersMap)).thenReturn(contextFromContextManager);
        when(contentNegotiationHandler.isAcceptable(
                same(expectedConnectionObject), eq(connectionType.toString()), eq(acceptType.toString()))).thenReturn(true);
        doReturn(parametersMap).when(request).getParameterMap();
        final Object expectedEntity = new Object();
        when(contentNegotiationHandler.handle(
                same(contextFromContextManager), eq(connectionType.toString()), eq(acceptType.toString()), same(parametersMap))).thenReturn(expectedEntity);
        final String someRequestUrl = "/jasperserver-pro/contexts/";
        when(request.getRequestURL()).thenReturn(new StringBuffer(someRequestUrl));
        when(uriInfo.getBaseUri()).thenReturn(new URI(someRequestUrl));
        final Response response = spyService.createContext(inputStream, connectionType, acceptType, request, uriInfo);
        assertEquals(response.getMetadata().getFirst(HttpHeaders.LOCATION).toString(), someRequestUrl + expectedUuid);
        assertSame(response.getEntity(), expectedEntity);
    }

    @Test(expectedExceptions = NotAcceptableException.class)
    public void createConnection_notAcceptable() throws IOException, URISyntaxException {
        final MediaType connectionType = MediaType.valueOf("application/connections.type+json");
        final MediaType acceptType = MediaType.valueOf("application/json");
        final Class<Number> connectionClass = Number.class;
        doReturn(connectionClass).when(spyService).getContextClass(connectionType);
        final Object expectedConnectionObject = new Object();
        doReturn(expectedConnectionObject).when(spyService).parseEntity(connectionClass, inputStream, connectionType);
        when(contentNegotiationHandler.isAcceptable(
                same(expectedConnectionObject),
                eq(connectionType.toString()),
                eq(acceptType.toString()))).thenReturn(false);
        spyService.createContext(inputStream, connectionType, acceptType, request, uriInfo);
    }

    @Test
    public void createConnection_metadata() throws Exception {
        final MediaType connectionType = MediaType.valueOf("application/connections.type+json");
        final MediaType metadataType = MediaType.valueOf("application/connections.type.metadata+json");
        final Class<Number> connectionClass = Number.class;
        doReturn(connectionClass).when(spyService).getContextClass(connectionType);
        doReturn(connectionClass).when(spyService).getContextClass(metadataType);
        final InputStream streamMock = mock(InputStream.class);
        final Object expectedConnectionObject = new Object();
        doReturn(expectedConnectionObject).when(spyService).parseEntity(connectionClass, streamMock, connectionType);
        final UUID expectedUuid = UUID.randomUUID();
        when(contextsManager.isMetadataSupported(ctx, expectedConnectionObject, "connections.type.metadata")).thenReturn(true);
        when(contextsManager.createContext(expectedConnectionObject)).thenReturn(expectedUuid);
        when(contextsManager.getContextMetadata(expectedUuid, new HashMap<String, String[]>())).thenReturn(expectedConnectionObject);
        final String someRequestUrl = "someRequestUrl";
        when(request.getRequestURL()).thenReturn(new StringBuffer(someRequestUrl));
        when(uriInfo.getBaseUri()).thenReturn(new URI(someRequestUrl));
        final Response response = spyService.createContext(streamMock, connectionType, metadataType, request, uriInfo);
        assertEquals(response.getMetadata().getFirst(HttpHeaders.LOCATION).toString(), someRequestUrl + "/" + expectedUuid + "/metadata");
        assertSame(response.getEntity(), expectedConnectionObject);
    }

    @Test(expectedExceptions = NotAcceptableException.class)
    public void createConnection_metadata_notSupported() throws Exception {
        final MediaType connectionType = MediaType.valueOf("application/connections.type+json");
        final MediaType metadataType = MediaType.valueOf("application/connections.type.metadata+json");
        final Class<Number> connectionClass = Number.class;
        doReturn(connectionClass).when(spyService).getContextClass(connectionType);
        doReturn(connectionClass).when(spyService).getContextClass(metadataType);
        final InputStream streamMock = mock(InputStream.class);
        final Object expectedConnectionObject = new Object();
        doReturn(expectedConnectionObject).when(spyService).parseEntity(connectionClass, streamMock, connectionType);
        when(contextsManager.isMetadataSupported(ctx, expectedConnectionObject, "connections.type.metadata")).thenReturn(false);
        spyService.createContext(streamMock, connectionType, metadataType, request, uriInfo);
    }

    @Test(expectedExceptions = ResourceNotFoundException.class)
    public void getUUID_1() throws Exception {
        ContextsJaxrsService.getUUID("uuid:abc");
    }
    @Test
    public void getUUID_2() throws Exception {
        ContextsJaxrsService.getUUID("uuid:0f85ea9b-63f0-4083-844d-ce2e4e1bc51c");
    }
    @Test(expectedExceptions = ResourceNotFoundException.class)
    public void getUUID_3() throws Exception {
        ContextsJaxrsService.getUUID("uuid:zz85ea9b-63f0-4083-844d-ce2e4e1bc51c");
    }

    @Test
    public void isProcessedException() {
        assertTrue(service.isProcessedException(new IllegalStateException(), Arrays.asList("java.lang.RuntimeException")));
        assertFalse(service.isProcessedException(new RuntimeException(), Arrays.asList("java.lang.IllegalStateException")));
        assertFalse(service.isProcessedException(new RuntimeException(), null));
        assertFalse(service.isProcessedException(new IllegalStateException(), Arrays.asList("java.lang.WritePendingException", "java.lang.WritePendingException")));
        assertTrue(service.isProcessedException(new IllegalStateException(), Arrays.asList("java.lang.WritePendingException", "java.lang.WritePendingException",
                "java.lang.RuntimeException")));
        assertFalse(service.isProcessedException(new IllegalStateException(), Arrays.asList("ABC", "EGH")));
    }
    @Test
    public void checkNullForStackTrace_WhenStackTraceIsNotAllowed_InContextCreationFailure () {
        final Exception exception =
                new ContextCreationFailedException("Object", "jndiName", "Invalid JNDI name", new Throwable(" password authentication failed for user postgres"), exceptionHandler);
        doReturn(false).when(outputManagerMock).isExceptionMessageAllowed();
        doReturn(false).when(outputManagerMock).isStackTraceAllowed();
        ErrorDescriptor retrievedDescriptor = exceptionHandler.handleException(exception,
                new ErrorDescriptor().setErrorCode("connection.failed").setMessage(exception.getMessage()));
        assertEquals("generic.error.message", retrievedDescriptor.getMessage());
        String[] parameters = retrievedDescriptor.getParameters();
        assertNull(parameters);

    }

    @Test
    public void checkNullForOriginalParametersWhenExportExecutionFailed () {

        final ExportExecutionRejectedException exception1 =
                new ExportExecutionRejectedException(new ErrorDescriptor().setMessage("export failed").setErrorCode("export.failed"));
        doReturn(false).when(outputManagerMock).isExceptionMessageAllowed();
        assertNull(exception1.getErrorDescriptor().getParameters());
        ErrorDescriptor errorDescriptor = exceptionHandler.handleException(exception1,
                exception1.getErrorDescriptor(), null);

    }

    @Test
    public void checkNullForOriginalParametersWhenReportExecutionFailed () {
        ReportExecution execution = new ReportExecution();
        execution.setErrorDescriptor(new ErrorDescriptor().setErrorCode("report.execution.failed").setMessage("Report execution failed"));
        assertNull(execution.getErrorDescriptor().getParameters());
    }

}

