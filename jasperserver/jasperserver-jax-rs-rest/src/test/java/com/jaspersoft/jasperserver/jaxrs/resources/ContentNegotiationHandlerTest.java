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
package com.jaspersoft.jasperserver.jaxrs.resources;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.FolderImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConversionOptions;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConverter;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.dto.resources.ClientResourceLookup;
import com.jaspersoft.jasperserver.jaxrs.common.RestConstants;
import com.jaspersoft.jasperserver.remote.exception.MandatoryParameterNotFoundException;
import com.jaspersoft.jasperserver.remote.exception.NotAcceptableException;
import com.jaspersoft.jasperserver.remote.exception.ReferencedResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.resources.converters.ResourceConverterProvider;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
public class ContentNegotiationHandlerTest {
    @InjectMocks
    private ContentNegotiationHandler handler = new ContentNegotiationHandler();
    private ContentNegotiationHandler handlerSpy;
    @Mock
    private HttpServletRequest request;
    @Mock
    private RepositoryService repository;
    @Mock
    private ResourceConverterProvider resourceConverterProvider;

    @BeforeClass
    public void init(){
        MockitoAnnotations.initMocks(this);
        handlerSpy = spy(handler);
    }

    @Test
    public void isAcceptable_doesntRequireConversion_true(){
        final String sourceMimeType = "someSourceMimeType";
        final String targetMimeType = "someTargetMimeType";
        when(handlerSpy.requiresConversion(sourceMimeType, targetMimeType)).thenReturn(false);
        final boolean acceptable = handlerSpy.isAcceptable(new ClientResourceLookup(), sourceMimeType, targetMimeType);
        assertTrue(acceptable);
    }
    
    @Test
    public void isAcceptable_requireConversionAndNotResourceLookup_false(){
        final String sourceMimeType = "someSourceMimeType";
        final String targetMimeType = "someTargetMimeType";
        when(handlerSpy.requiresConversion(sourceMimeType, targetMimeType)).thenReturn(true);
        final boolean acceptable = handlerSpy.isAcceptable(new Object(), sourceMimeType, targetMimeType);
        assertFalse(acceptable);
    }

    @Test
    public void isAcceptable_requireConversionResourceLookup_true(){
        final String sourceMimeType = "someSourceMimeType";
        final String targetMimeType = "someTargetMimeType";
        when(handlerSpy.requiresConversion(sourceMimeType, targetMimeType)).thenReturn(true);
        final ClientResourceLookup resourceLookup = new ClientResourceLookup();
        final FolderImpl resource = new FolderImpl();
        doReturn(resource).when(handlerSpy).getResource(resourceLookup);
        doReturn(mock(ToClientConverter.class)).when(handlerSpy).getToClientConverter(resource, targetMimeType);
        final boolean acceptable = handlerSpy.isAcceptable(resourceLookup, sourceMimeType, targetMimeType);
        assertTrue(acceptable);
    }

    @Test
    public void isAcceptable_requireConversionResourceLookup_false(){
        final String sourceMimeType = "someSourceMimeType";
        final String targetMimeType = "someTargetMimeType";
        when(handlerSpy.requiresConversion(sourceMimeType, targetMimeType)).thenReturn(true);
        final ClientResourceLookup resourceLookup = new ClientResourceLookup();
        final FolderImpl resource = new FolderImpl();
        doReturn(resource).when(handlerSpy).getResource(resourceLookup);
        doReturn(null).when(handlerSpy).getToClientConverter(resource, targetMimeType);
        final boolean acceptable = handlerSpy.isAcceptable(resourceLookup, sourceMimeType, targetMimeType);
        assertFalse(acceptable);
    }

    @Test
    public void getToClientConverter(){
        final String targetMimeType = "application/repository.folder+json";
        final FolderImpl resource = new FolderImpl();
        final ToClientConverter expectedConverter = mock(ToClientConverter.class);
        doReturn(expectedConverter).when(resourceConverterProvider)
                .getToClientConverter(resource.getResourceType(), "folder");
        final ToClientConverter toClientConverter = handler.getToClientConverter(resource, targetMimeType);
        assertSame(toClientConverter, expectedConverter);
    }

    @Test(expectedExceptions = MandatoryParameterNotFoundException.class)
    public void getResource_noUri_exception(){
        handler.getResource(new ClientResourceLookup());
    }

    @Test(expectedExceptions = ReferencedResourceNotFoundException.class)
    public void getResource_withUriResourceNotFound_exception(){
        final String uri = "someUri";
        doReturn(null).when(repository).getResource(any(ExecutionContext.class), eq(uri));
        handler.getResource(new ClientResourceLookup().setUri(uri));
    }

    @Test
    public void getResource_withUriResourceFound_success(){
        final String uri = "someUri";
        final FolderImpl resource = new FolderImpl();
        doReturn(resource).when(repository).getResource(any(ExecutionContext.class), eq(uri));
        final Resource result = handler.getResource(new ClientResourceLookup().setUri(uri));
        assertSame(result, resource);
    }

    @Test
    public void requiresConversion(){
        assertFalse(handler.requiresConversion("", null));
        assertFalse(handler.requiresConversion("", "application/json"));
        assertFalse(handler.requiresConversion("", "application/xml"));
        assertFalse(handler.requiresConversion("someMimeType", "someMimeType"));
        assertTrue(handler.requiresConversion("someMimeType1", "someMimeType2"));
    }

    @Test
    public void handle_doesntRequireConversion_sourceObject(){
        final String sourceMimeType = "someSourceMimeType";
        final String targetMimeType = "someTargetMimeType";
        final Object expectedResult = new Object();
        doReturn(false).when(handlerSpy).requiresConversion(sourceMimeType, targetMimeType);
        final Object result = handlerSpy.handle(expectedResult, sourceMimeType, targetMimeType, null);
        assertSame(result, expectedResult);
    }

    @Test(expectedExceptions = NotAcceptableException.class)
    public void handle_requireConversionNotResourceLookup_notAcceptableException(){
        final String sourceMimeType = "someSourceMimeType";
        final String targetMimeType = "someTargetMimeType";
        final Object object = new Object();
        doReturn(true).when(handlerSpy).requiresConversion(sourceMimeType, targetMimeType);
        handlerSpy.handle(object, sourceMimeType, targetMimeType, null);
    }

    @Test(expectedExceptions = NotAcceptableException.class)
    public void handle_requireConversionNoConverter_notAcceptableException(){
        final String sourceMimeType = "someSourceMimeType";
        final String targetMimeType = "someTargetMimeType";
        final ClientResourceLookup context = new ClientResourceLookup();
        final FolderImpl resource = new FolderImpl();
        doReturn(resource).when(handlerSpy).getResource(context);
        doReturn(null).when(handlerSpy).getToClientConverter(resource, targetMimeType);
        doReturn(true).when(handlerSpy).requiresConversion(sourceMimeType, targetMimeType);
        handlerSpy.handle(context, sourceMimeType, targetMimeType, null);
    }

    @Test
    public void handle_requireConversionWithConverterExpanded_success(){
        final String sourceMimeType = "someSourceMimeType";
        final String targetMimeType = "someTargetMimeType";
        final ClientResourceLookup context = new ClientResourceLookup();
        final FolderImpl resource = new FolderImpl();
        doReturn(resource).when(handlerSpy).getResource(context);
        final ToClientConverter toClientConverter = mock(ToClientConverter.class);
        doReturn(toClientConverter).when(handlerSpy).getToClientConverter(resource, targetMimeType);
        doReturn(true).when(handlerSpy).requiresConversion(sourceMimeType, targetMimeType);
        doReturn("true").when(request).getParameter(RestConstants.QUERY_PARAM_EXPANDED);
        final HashMap<String, String[]> additionalProperties = new HashMap<String, String[]>();
        final Object expectedResult = new Object();
        final ArgumentCaptor<ToClientConversionOptions> optionsArgumentCaptor = ArgumentCaptor.forClass(ToClientConversionOptions.class);
        doReturn(expectedResult).when(toClientConverter).toClient(same(resource), optionsArgumentCaptor.capture());
        final Object result = handlerSpy.handle(context, sourceMimeType, targetMimeType, additionalProperties);
        assertSame(result, expectedResult);
        final ToClientConversionOptions options = optionsArgumentCaptor.getValue();
        assertNotNull(options);
        assertSame(options.getAdditionalProperties(), additionalProperties);
    }

    @Test
    public void handle_requireConversionWithConverterNotExpanded_success(){
        final String sourceMimeType = "someSourceMimeType";
        final String targetMimeType = "someTargetMimeType";
        final ClientResourceLookup context = new ClientResourceLookup();
        final FolderImpl resource = new FolderImpl();
        doReturn(resource).when(handlerSpy).getResource(context);
        final ToClientConverter toClientConverter = mock(ToClientConverter.class);
        doReturn(toClientConverter).when(handlerSpy).getToClientConverter(resource, targetMimeType);
        doReturn(true).when(handlerSpy).requiresConversion(sourceMimeType, targetMimeType);
        doReturn(null).when(request).getParameter(RestConstants.QUERY_PARAM_EXPANDED);
        Map<String, String[]> additionalProperties  = new HashMap<String, String[]>();
        final ToClientConversionOptions expectedOptions = ToClientConversionOptions.getDefault()
                .setAcceptMediaType(targetMimeType)
                .setExpanded(false)
                .setAdditionalProperties(additionalProperties);
        final Object expectedResult = new Object();
        doReturn(expectedResult).when(toClientConverter).toClient(same(resource), eq(expectedOptions));
        final Object result = handlerSpy.handle(context, sourceMimeType, targetMimeType, additionalProperties);
        assertSame(result, expectedResult);
    }
}

