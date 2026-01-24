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
package com.jaspersoft.jasperserver.remote.resources.converters;

import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.api.ExceptionListWrapper;
import com.jaspersoft.jasperserver.api.JSValidationException;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceLookup;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.FolderImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.ResourceLookupImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConversionOptions;
import com.jaspersoft.jasperserver.api.metadata.common.service.ResourceFactory;
import com.jaspersoft.jasperserver.api.metadata.user.domain.ObjectPermission;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.ObjectPermissionImpl;
import com.jaspersoft.jasperserver.core.util.type.GenericTypeProcessorRegistry;
import com.jaspersoft.jasperserver.dto.resources.ClientFolder;
import com.jaspersoft.jasperserver.dto.resources.ClientReference;
import com.jaspersoft.jasperserver.dto.resources.ClientReferenceableDataSource;
import com.jaspersoft.jasperserver.dto.resources.ClientResource;
import com.jaspersoft.jasperserver.dto.resources.domain.ClientDomain;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.exception.MandatoryParameterNotFoundException;
import com.jaspersoft.jasperserver.remote.exception.ResourceGroupProfileAttributeErrorDescriptor;
import com.jaspersoft.jasperserver.remote.resources.ClientTypeHelper;
import com.jaspersoft.jasperserver.remote.resources.attachments.AttachmentsProcessor;
import com.jaspersoft.jasperserver.remote.resources.validation.ResourceValidator;
import com.jaspersoft.jasperserver.remote.services.PermissionsService;
import com.jaspersoft.jasperserver.remote.validation.ClientValidator;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.security.core.Authentication;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
public class ResourceConverterImplTest {
    private static final String TEST_CLIENT_OBJECT_NAME = "testClientObjectName";
    @InjectMocks
    private ResourceConverterImpl converter;
    @Mock
    private PermissionsService permissionsService;
    @Mock
    private GenericTypeProcessorRegistry genericTypeProcessorRegistry;
    @Mock
    private ClientTypeHelper clientTypeHelper;
    @Mock
    private Validator validator;
    @Mock
    private ClientValidator domainValidator;

    private final ObjectPermission permission = new ObjectPermissionImpl();
    private ToServerConversionOptions options;
    private List<Exception> exceptions = new ArrayList<Exception>();
    private ExecutionContext ctx  = ExecutionContextImpl.getRuntimeExecutionContext();

    @BeforeMethod
    public void setUp() {
        converter = mock(ResourceConverterImpl.class);
        permission.setPermissionMask(1);
        MockitoAnnotations.initMocks(this);
        when(permissionsService.getEffectivePermission(nullable(Resource.class), nullable(Authentication.class))).thenReturn(permission);
        options = ToServerConversionOptions.getDefault().setAdditionalProperties(Collections.<String, String[]>emptyMap());
    }

    @AfterMethod
    public void resetMocks() {
        reset(permissionsService, genericTypeProcessorRegistry, clientTypeHelper, validator, domainValidator);
    }

    @Test(expectedExceptions = JSValidationException.class)
    public void validateResource() {
        final Resource resource = mock(Resource.class);
        final String testResourceType = "testResourceType";
        when(resource.getResourceType()).thenReturn(testResourceType);
        final ResourceValidator validator = mock(ResourceValidator.class);
        final HashMap<String, String[]> additionalParameters = new HashMap<String, String[]>();
        doThrow(JSValidationException.class).when(validator).validate(ctx, resource, false, additionalParameters);
        when(genericTypeProcessorRegistry.getTypeProcessor(testResourceType, ResourceValidator.class, false)).thenReturn(validator);
        doCallRealMethod().when(converter).validateResource(ctx, resource, false, additionalParameters);
        converter.validateResource(ctx, resource, false, additionalParameters);
    }

    @Test
    public void toServer_withValidation() throws MandatoryParameterNotFoundException, IllegalParameterValueException {
        ResourceLookup serverObject = new ResourceLookupImpl();
        ResourceLookup anotherServerObject = new ResourceLookupImpl();
        final ClientFolder clientObject = new ClientFolder();
        serverObject.setURIString("/test/uri");
        when(converter.toServer(ctx, clientObject, serverObject, options)).thenCallRealMethod();
        when(converter.genericFieldsToServer(ctx, clientObject, serverObject, options)).thenReturn(anotherServerObject);
        when(converter.resourceSpecificFieldsToServer(ctx, clientObject, anotherServerObject, exceptions, options)).thenReturn(anotherServerObject);
        converter.toServer(ctx, clientObject, serverObject, options);
        verify(converter).validateResource(ctx, anotherServerObject, false, options.getAdditionalProperties());
    }

    @Test
    public void toServer_OptionsWithNullProperties() throws MandatoryParameterNotFoundException, IllegalParameterValueException {
        ResourceLookup serverObject = new ResourceLookupImpl();
        ResourceLookup anotherServerObject = new ResourceLookupImpl();
        final ClientFolder clientObject = new ClientFolder();
        serverObject.setURIString("/test/uri");
        options.setAdditionalProperties(null);
        when(converter.toServer(ctx, clientObject, serverObject, options)).thenCallRealMethod();
        when(converter.genericFieldsToServer(ctx, clientObject, serverObject, options)).thenReturn(anotherServerObject);
        when(converter.resourceSpecificFieldsToServer(ctx, clientObject, anotherServerObject, exceptions, options)).thenReturn(anotherServerObject);
        converter.toServer(ctx, clientObject, serverObject, options);
        verify(converter).validateResource(ctx, anotherServerObject, false, new HashMap<String, String[]>());
    }



    @Test(expectedExceptions = ExceptionListWrapper.class)
    public void toServer_withValidation_withConstraintViolations() throws MandatoryParameterNotFoundException, IllegalParameterValueException {
        ResourceLookup serverObject = new ResourceLookupImpl();
        ResourceLookup anotherServerObject = new ResourceLookupImpl();
        final ClientFolder clientObject = new ClientFolder();
        Set<ConstraintViolation<ClientFolder>> constraintViolations = new HashSet<ConstraintViolation<ClientFolder>>();
        ConstraintViolationImpl constraintViolation = mock(ConstraintViolationImpl.class);
        constraintViolations.add(constraintViolation);
        serverObject.setURIString("/test/uri");
        when(converter.toServer(ctx, clientObject, serverObject, options)).thenCallRealMethod();
        doReturn(constraintViolations).when(validator).validate(clientObject);
        when(converter.genericFieldsToServer(ctx, clientObject, serverObject, options)).thenReturn(anotherServerObject);
        when(converter.resourceSpecificFieldsToServer(ctx, clientObject, anotherServerObject, exceptions, options)).thenReturn(anotherServerObject);
        converter.toServer(ctx, clientObject, serverObject, options);
        verify(converter).validateResource(ctx, anotherServerObject, false, options.getAdditionalProperties());
    }

    @Test
    public void toServer_withMandatoryParameterExceptionAndViolations() {
        ResourceLookup serverObject = new ResourceLookupImpl();
        ResourceLookup anotherServerObject = new ResourceLookupImpl();
        final ClientDomain clientObject = new ClientDomain();
        ClientReferenceableDataSource datasource = new ClientReference();
        clientObject.setDataSource(datasource);
        serverObject.setURIString("/test/uri");
        Set<ConstraintViolation<ClientFolder>> constraintViolations = new HashSet<ConstraintViolation<ClientFolder>>();
        ConstraintViolationImpl constraintViolation = mock(ConstraintViolationImpl.class);
        constraintViolations.add(constraintViolation);
        doReturn(constraintViolations).when(validator).validate(clientObject);
        when(converter.toServer(ctx, clientObject, serverObject, options)).thenCallRealMethod();
        when(converter.genericFieldsToServer(ctx, clientObject, serverObject, options)).thenThrow(MandatoryParameterNotFoundException.class);
        try{
            converter.toServer(ctx, clientObject, serverObject, options);
        } catch (ExceptionListWrapper list) {
            assertTrue(list.getExceptions().size() == 2);
        }
    }
    @Test(expectedExceptions = MandatoryParameterNotFoundException.class)
    public void toServer_withValidation_withMandatoryParameterNotFoundException() {
        ResourceLookup serverObject = new ResourceLookupImpl();
        ResourceLookup anotherServerObject = new ResourceLookupImpl();
        final ClientDomain clientObject = new ClientDomain();
        ClientReferenceableDataSource datasource = new ClientReference();
        clientObject.setDataSource(datasource);
        serverObject.setURIString("/test/uri");
        when(converter.toServer(ctx, clientObject, serverObject, options)).thenCallRealMethod();
        when(converter.genericFieldsToServer(ctx, clientObject, serverObject, options)).thenThrow(MandatoryParameterNotFoundException.class);
        converter.toServer(ctx, clientObject, serverObject, options);
    }

    @Test(expectedExceptions = ExceptionListWrapper.class)
    public void toServer_withValidation_withHibernateValidatorFailure() throws MandatoryParameterNotFoundException, IllegalParameterValueException {
        ResourceLookup serverObject = new ResourceLookupImpl();
        ResourceLookup anotherServerObject = new ResourceLookupImpl();
        final ClientFolder clientObject = new ClientFolder();
        Set<ConstraintViolation<ClientFolder>> constraintViolations = new HashSet<ConstraintViolation<ClientFolder>>();
        ConstraintViolationImpl constraintViolation = mock(ConstraintViolationImpl.class);
        constraintViolations.add(constraintViolation);
        serverObject.setURIString("/test/uri");
        when(converter.toServer(ctx, clientObject, serverObject, options)).thenCallRealMethod();
        doThrow(new ConcurrentModificationException()).when(validator).validate(clientObject);
        when(converter.genericFieldsToServer(ctx, clientObject, serverObject, options)).thenReturn(anotherServerObject);
        when(converter.resourceSpecificFieldsToServer(ctx, clientObject, anotherServerObject, exceptions, options)).thenReturn(anotherServerObject);
        converter.toServer(ctx, clientObject, serverObject, options);
        verify(converter).validateResource(ctx, anotherServerObject, false, options.getAdditionalProperties());
    }

    @Test
    public void toServer_withDomainValidation() throws MandatoryParameterNotFoundException, IllegalParameterValueException {
        ResourceLookup serverObject = new ResourceLookupImpl();
        ResourceLookup anotherServerObject = new ResourceLookupImpl();
        final ClientFolder clientObject = new ClientFolder();
        serverObject.setURIString("/test/uri");
        when(genericTypeProcessorRegistry.getTypeProcessor(clientObject.getClass(), ClientValidator.class, false)).thenReturn(domainValidator);
        doReturn(new LinkedList<Exception>()).when(domainValidator).validate(ctx, clientObject);
        when(converter.toServer(ctx, clientObject, serverObject, options)).thenCallRealMethod();
        when(converter.genericFieldsToServer(ctx, clientObject, serverObject, options)).thenReturn(anotherServerObject);
        when(converter.resourceSpecificFieldsToServer(ctx, clientObject, anotherServerObject, exceptions, options)).thenReturn(anotherServerObject);
        converter.toServer(ctx, clientObject, serverObject, options);
        verify(converter).validateResource(ctx, anotherServerObject, false, options.getAdditionalProperties());
}

    @Test
    public void toServer_withDomainValidation_withValidationErrors() throws MandatoryParameterNotFoundException, IllegalParameterValueException {
        ResourceLookup serverObject = new ResourceLookupImpl();
        ResourceLookup anotherServerObject = new ResourceLookupImpl();
        final ClientFolder clientObject = new ClientFolder();
        serverObject.setURIString("/test/uri");
        when(genericTypeProcessorRegistry.getTypeProcessor(clientObject.getClass(), ClientValidator.class, false)).thenReturn(domainValidator);
        doReturn(new LinkedList<Exception>()).when(domainValidator).validate(ctx, clientObject);
        when(converter.toServer(ctx, clientObject, serverObject, options)).thenCallRealMethod();
        when(converter.genericFieldsToServer(ctx, clientObject, serverObject, options)).thenReturn(anotherServerObject);
        when(converter.resourceSpecificFieldsToServer(ctx, clientObject, anotherServerObject, exceptions, options)).thenReturn(anotherServerObject);
        converter.toServer(ctx, clientObject, serverObject, options);
        verify(converter).validateResource(ctx, anotherServerObject, false, options.getAdditionalProperties());
        verify(genericTypeProcessorRegistry).getTypeProcessor(clientObject.getClass(), ClientValidator.class, false);
        verify(domainValidator).validate(ctx, clientObject);
    }

    @Test
    public void toServer_withValidation_slipRepoFieldsValidation() throws MandatoryParameterNotFoundException, IllegalParameterValueException {
        options.setSkipRepoFieldsValidation(true);
        ResourceLookup serverObject = new ResourceLookupImpl();
        ResourceLookup anotherServerObject = new ResourceLookupImpl();
        final ClientFolder clientObject = new ClientFolder();
        serverObject.setURIString("/test/uri");
        when(converter.toServer(ctx, clientObject, serverObject, options)).thenCallRealMethod();
        when(converter.genericFieldsToServer(ctx, clientObject, serverObject, options)).thenReturn(anotherServerObject);
        when(converter.resourceSpecificFieldsToServer(ctx, clientObject, anotherServerObject, exceptions, options)).thenReturn(anotherServerObject);
        converter.toServer(ctx, clientObject, serverObject, options);
        verify(converter).validateResource(ctx, anotherServerObject, true, options.getAdditionalProperties());
    }

    @Test
    public void toServer_withValidation_optionsNull() throws MandatoryParameterNotFoundException, IllegalParameterValueException {
        ResourceLookup serverObject = new ResourceLookupImpl();
        ResourceLookup anotherServerObject = new ResourceLookupImpl();
        final ClientFolder clientObject = new ClientFolder();
        serverObject.setURIString("/test/uri");
        when(converter.toServer(ctx, clientObject, serverObject, null)).thenCallRealMethod();
        when(converter.genericFieldsToServer(ctx, clientObject, serverObject, null)).thenReturn(anotherServerObject);
        when(converter.resourceSpecificFieldsToServer(ctx, clientObject, anotherServerObject, exceptions, null)).thenReturn(anotherServerObject);
        final ArgumentCaptor<Map> mapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        doReturn(new ArrayList<Exception>()).when(converter).validateResource(any(ExecutionContext.class), same(anotherServerObject), eq(false), mapArgumentCaptor.capture());
        converter.toServer(ctx, clientObject, serverObject, null);
        final Map additionalParameters = mapArgumentCaptor.getValue();
        assertNotNull(additionalParameters);
        assertTrue(additionalParameters.isEmpty());
    }

    @Test
    public void toServer_validationSuppressed() throws MandatoryParameterNotFoundException, IllegalParameterValueException {
        ResourceLookup serverObject = new ResourceLookupImpl();
        ResourceLookup anotherServerObject = new ResourceLookupImpl();
        final ClientFolder clientObject = new ClientFolder();
        ToServerConversionOptions toServerConversionOptions = ToServerConversionOptions.getDefault().setSuppressValidation(true);
        serverObject.setURIString("/test/uri");
        when(converter.toServer(ctx, clientObject, serverObject, toServerConversionOptions)).thenCallRealMethod();
        when(converter.genericFieldsToServer(ctx, clientObject, serverObject, toServerConversionOptions)).thenReturn(anotherServerObject);
        when(converter.resourceSpecificFieldsToServer(ctx, clientObject, anotherServerObject, exceptions, toServerConversionOptions)).thenReturn(anotherServerObject);
        converter.toServer(ctx, clientObject, serverObject, toServerConversionOptions);
        verify(converter, never()).validateResource(any(ExecutionContext.class), any(Resource.class), eq(true), any(Map.class));
    }

    @Test
    public void internalToClient() throws ParseException {
        ResourceLookup serverObject = new ResourceLookupImpl();
        final String testResourceUri = "/test/resource/uri";
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        final String testDescription = "testDescription";
        final String testLabel = "testLabel";
        final int testVersion = 123;
        final Date creationDate = simpleDateFormat.parse("1996-05-25T22:15:26.235-0700");
        final Date updateDate = simpleDateFormat.parse("1999-08-31T03:21:55.156-0300");
        final String creationDateString = "creationDate";
        final String updateDateString = "updateDate";

        permission.setPermissionMask(1);
        serverObject.setResourceType("ignoredResourceType");
        serverObject.setURIString(testResourceUri);
        serverObject.setCreationDate(creationDate);
        serverObject.setUpdateDate(updateDate);
        serverObject.setDescription(testDescription);
        serverObject.setLabel(testLabel);
        serverObject.setVersion(testVersion);
        final ClientFolder expectedClientObject = new ClientFolder();
        DateFormat dateFormatMock = mock(DateFormat.class);
        when(dateFormatMock.format(eq(creationDate), nullable(StringBuffer.class), nullable(FieldPosition.class))).thenReturn(new StringBuffer(creationDateString));
        when(dateFormatMock.format(eq(updateDate), nullable(StringBuffer.class), nullable(FieldPosition.class))).thenReturn(new StringBuffer(updateDateString));
        when(converter.getDateTimeFormat()).thenReturn(dateFormatMock);
        when(converter.genericFieldsToClient(expectedClientObject, serverObject, null)).thenCallRealMethod();
        final ClientResource clientObject = converter.genericFieldsToClient(expectedClientObject, serverObject, null);
        assertSame(clientObject, expectedClientObject);
        assertEquals(clientObject.getCreationDate(), creationDateString);
        assertEquals(clientObject.getUpdateDate(), updateDateString);
        assertEquals(clientObject.getDescription(), testDescription);
        assertEquals(clientObject.getLabel(), testLabel);
        assertEquals(clientObject.getUri(), testResourceUri);
        assertEquals(clientObject.getVersion().intValue(), testVersion);
        assertEquals(clientObject.getPermissionMask().intValue(), permission.getPermissionMask());
    }

    @Test
    public void toServer_withAttachments() throws MandatoryParameterNotFoundException, IllegalParameterValueException {
        final HashMap<String, InputStream> attachments = new HashMap<String, InputStream>();
        final ToServerConversionOptions toServerConversionOptions = ToServerConversionOptions.getDefault().setAttachments(attachments);
        final ResourceLookupImpl serverObject = new ResourceLookupImpl();
        final ClientFolder clientObject = new ClientFolder();
        when(converter.toServer(ctx, clientObject, serverObject, toServerConversionOptions)).thenCallRealMethod();
        when(converter.genericFieldsToServer(ctx, clientObject, serverObject, toServerConversionOptions)).thenReturn(serverObject);
        when(converter.resourceSpecificFieldsToServer(ctx, clientObject, serverObject, exceptions, toServerConversionOptions)).thenReturn(serverObject);
        when(converter.getClientTypeClass()).thenReturn(ClientResource.class);
        final AttachmentsProcessor attachmentsProcessor = mock(AttachmentsProcessor.class);
        when(genericTypeProcessorRegistry.getTypeProcessor(ClientResource.class, AttachmentsProcessor.class, false)).thenReturn(attachmentsProcessor);
        when(attachmentsProcessor.processAttachments(serverObject, attachments)).thenReturn(serverObject);
        final Resource result = converter.toServer(ctx, clientObject, serverObject, toServerConversionOptions);
        assertSame(result, serverObject);
    }

    @Test
    public void toServer_updateMode() throws Exception {
        final String testResourceType = "testResourceType";
        final String testResourceUri = "/test/resource/uri";
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        final String testDescription = "testDescription";
        final String testLabel = "testLabel";
        final int testVersion = 123;
        final String creationDateString = "creationDate";
        final String updateDateString = "updateDate";
        final Date creationDate = simpleDateFormat.parse("1996-05-25T22:15:26.235-0700");
        final Date updateDate = simpleDateFormat.parse("1999-08-31T03:21:55.156-0300");
        ClientFolder clientResource = new ClientFolder();
        clientResource.setDescription(testDescription);
        clientResource.setLabel(testLabel);
        clientResource.setUpdateDate(updateDateString);
        clientResource.setUri(testResourceUri);
        clientResource.setVersion(testVersion);
        clientResource.setCreationDate(creationDateString);
        ResourceLookup serverObject = new ResourceLookupImpl();
        serverObject.setURIString(testResourceUri);
        DateFormat dateFormatMock = mock(DateFormat.class);
        when(dateFormatMock.parse(creationDateString)).thenReturn(creationDate);
        when(dateFormatMock.parse(updateDateString)).thenReturn(updateDate);
        when(converter.getDateTimeFormat()).thenReturn(dateFormatMock);
        when(converter.getServerResourceType()).thenReturn(testResourceType);
        when(converter.toServer(ctx, clientResource, serverObject, options)).thenCallRealMethod();
        when(converter.genericFieldsToServer(ctx, clientResource, serverObject, options)).thenCallRealMethod();
        when(converter.resourceSpecificFieldsToServer( ctx, clientResource, serverObject, exceptions, options)).thenReturn(serverObject);
        final Resource resource = converter.toServer(ctx, clientResource, serverObject, options);
        assertSame(resource, serverObject);
        assertEquals(resource.getURIString(), clientResource.getUri());
        assertEquals(resource.getDescription(), clientResource.getDescription());
        assertEquals(resource.getLabel(), clientResource.getLabel());
        assertNull(resource.getResourceType());
        assertNull(resource.getUpdateDate());
        assertNull(resource.getCreationDate());
        assertEquals(resource.getVersion(), clientResource.getVersion().intValue());
    }

    @Test
    public void toServer_noVersion() throws Exception {
        final String testResourceType = "testResourceType";
        final String testResourceUri = "/test/resource/uri";
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        final String testDescription = "testDescription";
        final String testLabel = "testLabel";
        final String creationDateString = "creationDate";
        final String updateDateString = "updateDate";
        final Date creationDate = simpleDateFormat.parse("1996-05-25T22:15:26.235-0700");
        final Date updateDate = simpleDateFormat.parse("1999-08-31T03:21:55.156-0300");
        ClientFolder clientResource = new ClientFolder();
        clientResource.setDescription(testDescription);
        clientResource.setLabel(testLabel);
        clientResource.setUpdateDate(updateDateString);
        clientResource.setUri(testResourceUri);
        clientResource.setCreationDate(creationDateString);
        ResourceLookup serverObject = new ResourceLookupImpl();
        serverObject.setURIString(testResourceUri);
        DateFormat dateFormatMock = mock(DateFormat.class);
        when(dateFormatMock.parse(creationDateString)).thenReturn(creationDate);
        when(dateFormatMock.parse(updateDateString)).thenReturn(updateDate);
        when(converter.getDateTimeFormat()).thenReturn(dateFormatMock);
        when(converter.getServerResourceType()).thenReturn(testResourceType);
        when(converter.toServer(ctx, clientResource, serverObject, options)).thenCallRealMethod();
        when(converter.genericFieldsToServer(ctx, clientResource, serverObject, options)).thenCallRealMethod();
        when(converter.resourceSpecificFieldsToServer(ctx, clientResource, serverObject, exceptions, options)).thenReturn(serverObject);
        final Resource resource = converter.toServer(ctx, clientResource, serverObject, options);

        assertEquals(resource.getVersion(), Resource.VERSION_NEW);
    }

    @Test
    public void toServer_resetVersion() throws Exception {
        final String testResourceType = "testResourceType";
        final String testResourceUri = "/test/resource/uri";
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        final String testDescription = "testDescription";
        final String testLabel = "testLabel";
        final int testVersion = 123;
        final String creationDateString = "creationDate";
        final String updateDateString = "updateDate";
        final Date creationDate = simpleDateFormat.parse("1996-05-25T22:15:26.235-0700");
        final Date updateDate = simpleDateFormat.parse("1999-08-31T03:21:55.156-0300");
        ClientFolder clientResource = new ClientFolder();
        clientResource.setDescription(testDescription);
        clientResource.setLabel(testLabel);
        clientResource.setUpdateDate(updateDateString);
        clientResource.setUri(testResourceUri);
        clientResource.setVersion(testVersion);
        clientResource.setCreationDate(creationDateString);
        ResourceLookup serverObject = new ResourceLookupImpl();
        serverObject.setURIString(testResourceUri);
        DateFormat dateFormatMock = mock(DateFormat.class);
        when(dateFormatMock.parse(creationDateString)).thenReturn(creationDate);
        when(dateFormatMock.parse(updateDateString)).thenReturn(updateDate);
        when(converter.getDateTimeFormat()).thenReturn(dateFormatMock);
        when(converter.getServerResourceType()).thenReturn(testResourceType);
        when(converter.toServer(any(ExecutionContext.class), eq(clientResource), eq(serverObject), any(ToServerConversionOptions.class))).thenCallRealMethod();
        when(converter.genericFieldsToServer(any(ExecutionContext.class), eq(clientResource), eq(serverObject), any(ToServerConversionOptions.class))).thenCallRealMethod();
        when(converter.resourceSpecificFieldsToServer(any(ExecutionContext.class), eq(clientResource), eq(serverObject), any(List.class), any(ToServerConversionOptions.class))).thenReturn(serverObject);
        final Resource resource = converter.toServer(ctx, clientResource, serverObject, ToServerConversionOptions.getDefault().setResetVersion(true));

        assertEquals(resource.getVersion(), Resource.VERSION_NEW);
    }

    @Test
    public void genericFieldsToServer_resourceToUpdateIsNew() throws MandatoryParameterNotFoundException, IllegalParameterValueException {
        ClientResource expectedClientObject = new ClientFolder().setLabel("testLabel");
        Resource expectedNewObject = new FolderImpl();
        when(converter.getNewResourceInstance()).thenReturn(expectedNewObject);
        when(converter.genericFieldsToServer(ctx, expectedClientObject, null, null)).thenCallRealMethod();
        when(converter.getDateTimeFormat()).thenReturn(mock(DateFormat.class));
        final Resource result = converter.genericFieldsToServer(ctx, expectedClientObject, null, null);
        assertSame(result, expectedNewObject);
        assertEquals(result.getVersion(), Resource.VERSION_NEW);
    }

    @Test
    public void genericFieldToClient_inMemoryResource_skipsVersionAndPermissionMask() {
        final ResourceLookup serverObject = new ResourceLookupImpl();
        serverObject.setVersion(10);
        ObjectPermission objectPermission = mock(ObjectPermission.class);
        ToClientConversionOptions options = new ToClientConversionOptions()
                .setInMemoryResource(true);

        when(objectPermission.getPermissionMask()).thenReturn(1);
        when(permissionsService.getEffectivePermission(eq(serverObject), ArgumentMatchers.<Authentication>any())).thenReturn(objectPermission);
        when(converter.getDateTimeFormat()).thenReturn(mock(DateFormat.class));
        when(converter.genericFieldsToClient(any(ClientResource.class), any(Resource.class), any(ToClientConversionOptions.class))).thenCallRealMethod();

        ClientResource clientResource = converter.genericFieldsToClient(new ClientFolder(), serverObject, options);

        assertNull(clientResource.getVersion());
        assertNull(clientResource.getPermissionMask());
        verifyNoInteractions(permissionsService);
    }

    @Test
    public void genericFieldToClient_notInMemoryResource_setsVersionAndPermissionMask() {
        final Integer version = 10;
        final Integer permissionMask = 1;
        final ResourceLookup serverObject = new ResourceLookupImpl();
        serverObject.setVersion(version);
        ObjectPermission objectPermission = mock(ObjectPermission.class);
        ToClientConversionOptions options = new ToClientConversionOptions()
                .setInMemoryResource(false);

        when(objectPermission.getPermissionMask()).thenReturn(permissionMask);
        when(permissionsService.getEffectivePermission(eq(serverObject), ArgumentMatchers.<Authentication>any())).thenReturn(objectPermission);
        when(converter.getDateTimeFormat()).thenReturn(mock(DateFormat.class));
        when(converter.genericFieldsToClient(any(ClientResource.class), any(Resource.class), any(ToClientConversionOptions.class))).thenCallRealMethod();

        ClientResource clientResource = converter.genericFieldsToClient(new ClientFolder(), serverObject, options);

        assertEquals(clientResource.getVersion(), version);
        assertEquals(clientResource.getPermissionMask(), permissionMask);
    }
    @Test
    public void toServer_exceptionsListIsForwardedTo_resourceSpecificFieldsToServer(){
        final ClientFolder clientObject = new ClientFolder();
        final FolderImpl serverObject = new FolderImpl();
        final ToServerConversionOptions options = ToServerConversionOptions.getDefault();
        final RuntimeException runtimeException = new RuntimeException();
        when(converter.genericFieldsToServer(ctx, clientObject, serverObject, options)).thenReturn(serverObject);
        when(converter.resourceSpecificFieldsToServer(any(ExecutionContext.class), same(clientObject), same(serverObject), any(List.class), same(options))).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final List<Exception> exceptions = (List<Exception>) invocation.getArguments()[3];
                assertNotNull(exceptions);
                exceptions.add(runtimeException);
                return serverObject;
            }
        });
        ExceptionListWrapper exceptionListWrapper = null;
        when(converter.toServer(ctx, clientObject, serverObject, options)).thenCallRealMethod();
        try {
            converter.toServer(ctx, clientObject, serverObject, options);
        } catch (Exception e) {
            assertTrue(e instanceof ExceptionListWrapper);
            exceptionListWrapper = (ExceptionListWrapper) e;
        }
        assertNotNull(exceptionListWrapper);
        final List<? extends Exception> resultExceptions = exceptionListWrapper.getExceptions();
        assertNotNull(resultExceptions);
        assertEquals(resultExceptions.size(), 1);
        assertSame(resultExceptions.get(0), runtimeException);
    }


    @Test
    public void toClient() {
        ResourceLookup serverObject = new ResourceLookupImpl();
        serverObject.setURIString("/test/uri");
        ClientResource expectedClientObject = new ClientFolder();
        when(converter.getNewClientObjectInstance()).thenReturn(expectedClientObject);
        final ToClientConversionOptions options = ToClientConversionOptions.getDefault();
        when(converter.toClient(serverObject, options)).thenCallRealMethod();
        converter.toClient(serverObject, options);
        verify(converter).genericFieldsToClient(expectedClientObject, serverObject, options);
        verify(converter, never()).resourceSecureFieldsToClient(expectedClientObject, serverObject, options);
    }

    @Test
    public void toClient_allowSecureDataConversationIsEnabled_checkResourceSecuredFieldsToClientIsCalled() {
        ResourceLookup serverObject = new ResourceLookupImpl();
        serverObject.setURIString("/test/uri");
        ClientResource expectedClientObject = new ClientFolder();
        when(converter.getNewClientObjectInstance()).thenReturn(expectedClientObject);
        final ToClientConversionOptions options = ToClientConversionOptions.getDefault().setAllowSecureDataConversation(true);
        when(converter.toClient(serverObject, options)).thenCallRealMethod();
        converter.toClient(serverObject, options);
        verify(converter).resourceSecureFieldsToClient(nullable(ClientResource.class), nullable(Resource.class), nullable(ToClientConversionOptions.class));
    }

    @Test
    public void getNewClientObjectInstance() {
        ResourceConverterImpl<Folder, ClientFolder> converter = new ResourceConverterImpl<Folder, ClientFolder>() {
            @Override
            protected Folder resourceSpecificFieldsToServer(ExecutionContext ctx, ClientFolder clientObject, Folder resultToUpdate, List<Exception> exceptions, ToServerConversionOptions options) throws IllegalParameterValueException, MandatoryParameterNotFoundException {
                return resultToUpdate;
            }

            @Override
            protected ClientFolder resourceSpecificFieldsToClient(ClientFolder client, Folder serverObject, ToClientConversionOptions options) {
                return client;
            }
        };
        final ClientFolder newClientObjectInstance = converter.getNewClientObjectInstance();
        assertNotNull(newClientObjectInstance);
    }

    @Test
    public void addValidExceptions() {
        ArrayList<Exception> includedExceptions = new ArrayList<Exception>();
        ArrayList<Exception> allFoundExceptions = new ArrayList<Exception>();
        allFoundExceptions.add(new ErrorDescriptorException(new ResourceGroupProfileAttributeErrorDescriptor()));
        ToServerConversionOptions toServerConversionOptions = new ToServerConversionOptions();
        HashMap<String, String[]> additionalProperties = new HashMap<String, String[]>();
        additionalProperties.put(ToServerConversionOptions.SKIP_DATA_BASE_METADATA_CHECK, new String[]{"true"});
        toServerConversionOptions.setAdditionalProperties(additionalProperties);
        converter.addValidExceptions(includedExceptions, allFoundExceptions, toServerConversionOptions);
        assertEquals(includedExceptions.size(), 0);
    }

    @Test
    public void addValidExceptions_WithNullValues() {
        ArrayList<Exception> includedExceptions = new ArrayList<Exception>();
        ArrayList<Exception> allFoundExceptions = new ArrayList<Exception>();
        allFoundExceptions.add(new ErrorDescriptorException(new ResourceGroupProfileAttributeErrorDescriptor()));
        ToServerConversionOptions toServerConversionOptions = new ToServerConversionOptions();
        HashMap<String, String[]> additionalProperties = new HashMap<String, String[]>();
        additionalProperties.put(ToServerConversionOptions.SKIP_DATA_BASE_METADATA_CHECK, null);
        toServerConversionOptions.setAdditionalProperties(additionalProperties);
        converter.addValidExceptions(includedExceptions, allFoundExceptions, toServerConversionOptions);
        assertEquals(includedExceptions.size(), 0);
    }

    @Test
    public void getClientResourceType() {
        when(clientTypeHelper.getClientResourceType()).thenReturn(TEST_CLIENT_OBJECT_NAME);
        when(converter.getClientResourceType()).thenCallRealMethod();
        assertEquals(converter.getClientResourceType(), TEST_CLIENT_OBJECT_NAME);
    }

    @Test
    public void getClientTypeClass_rawConverter() {

        final ResourceConverterImpl resourceConverter = new ResourceConverterImpl() {

            @Override
            protected ClientResource resourceSpecificFieldsToClient(ClientResource client, Resource serverObject, ToClientConversionOptions options) {
                return client;
            }

            @Override
            protected Resource resourceSpecificFieldsToServer(ExecutionContext ctx, ClientResource clientObject, Resource resultToUpdate, List list, ToServerConversionOptions options) throws IllegalParameterValueException, MandatoryParameterNotFoundException {
                return resultToUpdate;
            }
        };
        IllegalStateException exception = null;
        try {
            resourceConverter.getClientTypeClass();
        } catch (IllegalStateException ex) {
            exception = ex;
        }
        assertNotNull(exception);
    }

    @Test
    public void getClientTypeClass() {
        final ResourceConverterImpl resourceConverter = new ResourceConverterImpl<Folder, ClientFolder>() {
            @Override
            protected Folder resourceSpecificFieldsToServer(ExecutionContext ctx, ClientFolder clientObject, Folder resultToUpdate, List<Exception> exceptions, ToServerConversionOptions options) throws IllegalParameterValueException, MandatoryParameterNotFoundException {
                return resultToUpdate;
            }

            @Override
            protected ClientFolder resourceSpecificFieldsToClient(ClientFolder client, Folder serverObject, ToClientConversionOptions options) {
                return client;
            }
        };
        final Class<?> clientTypeClass = resourceConverter.getClientTypeClass();
        assertTrue(clientTypeClass == ClientFolder.class);
    }

    @Test
    public void toServer() throws Exception {
        ClientResource expectedClientObject = new ClientFolder();
        Resource expectedNewObject = new FolderImpl();
        when(converter.toServer(ctx, expectedClientObject, null, null)).thenReturn(expectedNewObject);
        when(converter.toServer(ctx, expectedClientObject, null)).thenCallRealMethod();
        final Resource resource = converter.toServer(ctx, expectedClientObject, null);
        assertSame(resource, expectedNewObject);
    }

    @Test
    public void getNewResourceInstance() {
        when(converter.getNewResourceInstance()).thenCallRealMethod();
        final String expectedResourceTypeName = "testResourceTypeName";
        when(converter.getServerResourceType()).thenReturn(expectedResourceTypeName);
        converter.objectFactory = mock(ResourceFactory.class);
        final FolderImpl expectedFolder = new FolderImpl();
        when(converter.objectFactory.newResource(null, expectedResourceTypeName)).thenReturn(expectedFolder);
        final Object folder = converter.getNewResourceInstance();
        assertSame(folder, expectedFolder);
    }

    @Test
    public void getServerResourceType() {
        // correct resource converter for folder resources
        ResourceConverterImpl<Folder, ClientFolder> folderConverter = new ResourceConverterImpl<Folder, ClientFolder>() {
            @Override
            protected Folder resourceSpecificFieldsToServer(ExecutionContext ctx, ClientFolder clientObject, Folder resultToUpdate, List<Exception> exceptions, ToServerConversionOptions options) throws IllegalParameterValueException, MandatoryParameterNotFoundException {
                return resultToUpdate;
            }

            @Override
            protected ClientFolder resourceSpecificFieldsToClient(ClientFolder client, Folder serverObject, ToClientConversionOptions options) {
                return client;
            }
        };
        final String extractedResourceName = folderConverter.getServerResourceType();
        assertEquals(extractedResourceName, Folder.class.getName());

        // incorrect resource converter for unknown resource type
        ResourceConverterImpl<?, ?> rawConverter = new ResourceConverterImpl() {
            @Override
            protected Resource resourceSpecificFieldsToServer(ExecutionContext ctx, ClientResource clientObject, Resource resultToUpdate, List exceptions, ToServerConversionOptions options) throws IllegalParameterValueException, MandatoryParameterNotFoundException {
                return resultToUpdate;
            }

            @Override
            protected ClientResource resourceSpecificFieldsToClient(ClientResource client, Resource serverObject, ToClientConversionOptions options) {
                return client;
            }
        };
        IllegalStateException exception = null;
        try {
            rawConverter.getServerResourceType();
        } catch (IllegalStateException ex) {
            exception = ex;
        }
        assertNotNull(exception);
    }
}
