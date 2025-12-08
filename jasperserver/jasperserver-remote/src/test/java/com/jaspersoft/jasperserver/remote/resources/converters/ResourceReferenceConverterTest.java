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

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.DataType;
import com.jaspersoft.jasperserver.api.metadata.common.domain.RepositoryConfiguration;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.DataTypeImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConversionOptions;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConverter;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.AwsReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.client.AwsReportDataSourceImpl;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.client.JdbcReportDataSourceImpl;
import com.jaspersoft.jasperserver.dto.resources.ClientAwsDataSource;
import com.jaspersoft.jasperserver.dto.resources.ClientDataType;
import com.jaspersoft.jasperserver.dto.resources.ClientFile;
import com.jaspersoft.jasperserver.dto.resources.ClientJdbcDataSource;
import com.jaspersoft.jasperserver.dto.resources.ClientReference;
import com.jaspersoft.jasperserver.dto.resources.ClientReferenceable;
import com.jaspersoft.jasperserver.dto.resources.ClientReferenceableDataType;
import com.jaspersoft.jasperserver.dto.resources.ClientResource;
import com.jaspersoft.jasperserver.dto.resources.ClientUriHolder;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.exception.MandatoryParameterNotFoundException;
import com.jaspersoft.jasperserver.remote.exception.ReferencedResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.services.PermissionsService;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.UUID;

import static com.jaspersoft.jasperserver.dto.resources.ResourceMediaType.DATA_TYPE_CLIENT_TYPE;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
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
public class ResourceReferenceConverterTest {
    public static final ExecutionContext CTX = ExecutionContextImpl.getRuntimeExecutionContext();
    private String testUri = "/test/resource/uri";
    private ResourceReferenceConverter converter;
    private ResourceConverterProvider resourceConverterProvider;
    private RepositoryService repositoryService;
    private PermissionsService permissionsService;
    private RepositoryConfiguration configurationBean;
    private ToServerConversionOptions options = ToServerConversionOptions.getDefault();
    private Resource localResource;
    private final ClientDataType expectedClientObject = new ClientDataType().setUri(testUri);
    private final ClientReference expectedClientReference = new ClientReference().setUri(testUri).setVersion(0);
    private ResourceReference resourceReferenceWithLocalResource;
    private ResourceReference resourceReference;


    @BeforeMethod
    public void initMocks() {
        if (resourceConverterProvider != null) {
            reset(resourceConverterProvider);
        }
        configurationBean = mock(RepositoryConfiguration.class);
        when(configurationBean.getResourceIdNotSupportedSymbols()).thenReturn("@#$%");
        resourceConverterProvider = mock(ResourceConverterProvider.class);
        repositoryService = mock(RepositoryService.class);
        converter = new ResourceReferenceConverter(resourceConverterProvider, repositoryService, permissionsService, configurationBean);
        localResource = new DataTypeImpl();
        localResource.setURIString(testUri);

        resourceReferenceWithLocalResource = new ResourceReference(localResource);
        resourceReference = new ResourceReference(testUri);
   }

    @Test
    public void toClient_nullProducesNull(){
        assertNull(converter.toClient(null, null));
    }

    @Test
    public void toClient_localResource_expanded() {
        final Resource localResource = new DataTypeImpl();
        final String testUri = "/test/resource/uri";
        localResource.setURIString(testUri);
        final ResourceReference serverObject = new ResourceReference(localResource);
        final ClientDataType expectedClientObject = new ClientDataType();
        expectedClientObject.setUri(testUri);

        ToClientConverter defaultToClientConverter = mockClientConverter(localResource, expectedClientObject);
        mockClientConverterForPairServerResourceAndClientType(localResource, defaultToClientConverter.getClientResourceType(), expectedClientObject);

        final ClientUriHolder result = converter.toClient(serverObject, new ToClientConversionOptions().setExpanded(true));
        assertSame(result, expectedClientObject);
        assertEquals(result.getUri(), testUri);
    }

    @Test
    public void toClient_nonLocalResource_expanded() {
        final Resource localResource = new DataTypeImpl();
        final String testUri = "/test/resource/uri";
        localResource.setURIString(testUri);
        final ResourceReference serverObject = new ResourceReference(testUri);
        final ClientDataType expectedClientObject = new ClientDataType();
        expectedClientObject.setUri(testUri);

        ToClientConverter defaultToClientConverter = mockClientConverter(localResource, expectedClientObject);
        mockClientConverterForPairServerResourceAndClientType(localResource, defaultToClientConverter.getClientResourceType(), expectedClientObject);

        when(repositoryService.getResource(any(ExecutionContext.class), eq(testUri))).thenReturn(localResource);
        final ClientUriHolder result = converter.toClient(serverObject, new ToClientConversionOptions().setExpanded(true));
        assertSame(result, expectedClientObject);
        assertEquals(result.getUri(), testUri);
    }

    @Test
    public void toClient_nonLocalResource_inMemoryServerObject() {
        final String testUri = "/test/resource/uri";
        final ResourceReference serverObject = spy(new ResourceReference(testUri));

        ToClientConversionOptions options = new ToClientConversionOptions()
                .setExpanded(false)
                .setInMemoryResource(true);
        final ClientUriHolder result = converter.toClient(serverObject, options);

        verifyNoInteractions(repositoryService);
        verifyNoInteractions(resourceConverterProvider);
        assertEquals(result.getUri(), testUri);
    }

    @Test
    public void toClient_localResource_notInMemoryServerObject() {
        final Resource localResource = new DataTypeImpl();
        final String testUri = "/test/resource/uri";
        localResource.setURIString(testUri);
        final ResourceReference serverObject = spy(new ResourceReference(testUri));

        ToClientConversionOptions options = new ToClientConversionOptions()
                .setExpanded(false)
                .setInMemoryResource(true);
        final ClientUriHolder result = converter.toClient(serverObject, options);

        verifyNoInteractions(repositoryService);
        verifyNoInteractions(resourceConverterProvider);
        assertEquals(result.getUri(), testUri);
    }

    @Test
    public void toClient_localResource_inMemoryServerObject() {
        final Resource localResource = new DataTypeImpl();
        final String testUri = "/test/resource/uri";
        localResource.setURIString(testUri);
        final ResourceReference serverObject = new ResourceReference(localResource);
        final ClientDataType expectedClientObject = new ClientDataType();
        expectedClientObject.setUri(testUri);

        ToClientConverter defaultToClientConverter = mockClientConverter(localResource, expectedClientObject);
        mockClientConverterForPairServerResourceAndClientType(localResource,
                defaultToClientConverter.getClientResourceType(), expectedClientObject);

        ToClientConversionOptions options = new ToClientConversionOptions()
                .setExpanded(false)
                .setInMemoryResource(true);
        final ClientUriHolder result = converter.toClient(serverObject, options);

        assertSame(result, expectedClientObject);
        assertEquals(result.getUri(), testUri);
        verifyNoInteractions(repositoryService);
    }

    @Test
    public void toClient_expandTargetType_expanded() {
        final Resource localResource = new DataTypeImpl();
        final String testUri = "/test/resource/uri";
        localResource.setURIString(testUri);
        final ResourceReference serverObject = new ResourceReference(testUri);
        final ClientDataType expectedClientObject = new ClientDataType();
        expectedClientObject.setUri(testUri);

        mockClientConverter(localResource, null);
        mockClientConverterForPairServerResourceAndClientType(localResource, "targetClientType", expectedClientObject);

        when(repositoryService.getResource(any(ExecutionContext.class), eq(testUri))).thenReturn(localResource);
        final ClientUriHolder result = converter.toClient(serverObject,
                new ToClientConversionOptions().setExpanded(false).setExpandTypes(Collections.singleton("targetClientType")));
        assertSame(result, expectedClientObject);
        assertEquals(result.getUri(), testUri);
    }

    @Test
    public void toClient_expandTwoTargetClientTypesThatMapsToOneServerType_resourceExpandedWithFirstClientType() {
        final Resource localResource = new DataTypeImpl();
        final String testUri = "/test/resource/uri";
        localResource.setURIString(testUri);
        final ResourceReference serverObject = new ResourceReference(testUri);
        final ClientDataType expectedClientObject = new ClientDataType();
        expectedClientObject.setUri(testUri);

        mockClientConverter(localResource, null);
        mockClientConverterForPairServerResourceAndClientType(localResource, "targetClientType1", expectedClientObject);
        mockClientConverterForPairServerResourceAndClientType(localResource, "targetClientType2", null);

        when(repositoryService.getResource(any(ExecutionContext.class), eq(testUri))).thenReturn(localResource);
        final ClientUriHolder result = converter.toClient(serverObject,
                new ToClientConversionOptions().setExpanded(false).setExpandTypes(Collections.singleton("targetClientType1")));
        assertSame(result, expectedClientObject);
        assertEquals(result.getUri(), testUri);
    }

    @Test
    public void toClient_localResource_collapsed() {
        final Resource localResource = new DataTypeImpl();
        final String testUri = "/test/resource/uri";
        localResource.setURIString(testUri);
        final ResourceReference serverObject = new ResourceReference(localResource);
        final ClientDataType expectedClientObject = new ClientDataType();
        expectedClientObject.setUri(testUri);
        when(resourceConverterProvider.getToClientConverter(localResource)).thenReturn(new ToClientConverter() {
            @Override
            public Object toClient(Object serverObject, Object options) {
                assertSame(serverObject, localResource);
                return expectedClientObject;
            }

            @Override
            public String getClientResourceType() {
                return null;
            }
        });
        final ClientUriHolder result = converter.toClient(serverObject, null);
        assertEquals(result.getUri(), testUri);
        assertTrue(result instanceof ClientReference);
    }

    @Test
    public void toClient_reference() {
        final String expectedReferenceUri = "/test/reference/uri";
        final ResourceReference serverObject = new ResourceReference(expectedReferenceUri);
        final ClientReference expectedReference = new ClientReference().setVersion(0);
        expectedReference.setUri(expectedReferenceUri);
        final ClientUriHolder result = converter.toClient(serverObject, null);
        assertTrue(new ReflectionEquals(expectedReference).matches(result));
    }

    @Test
    public void toClient_expansionIsNotEnabled_returnClientReference() {
        ToClientConversionOptions options = mock(ToClientConversionOptions.class);
        when(options.isExpansionEnabled(resourceReference.isLocal())).thenReturn(false);

        final ClientUriHolder result = converter.toClient(resourceReference, options);
        assertEquals(result, expectedClientReference);
    }

    @Test
    public void toClient_localResourceAndIsExpansionEnabledIsTrueAndIsExpandedIsTrue_returnFullResource() {
        ToClientConverter defaultToClientConverter = mockClientConverter(resourceReferenceWithLocalResource.getLocalResource(), null);
        mockClientConverterForPairServerResourceAndClientType(resourceReferenceWithLocalResource.getLocalResource(),
                defaultToClientConverter.getClientResourceType(), expectedClientObject);

        ToClientConversionOptions options = mock(ToClientConversionOptions.class);
        when(options.isExpansionEnabled(true)).thenReturn(true);
        when(options.isExpanded(DATA_TYPE_CLIENT_TYPE, true)).thenReturn(true);

        final ClientReferenceable result = converter.toClient(resourceReferenceWithLocalResource, options);
        assertEquals(result, expectedClientObject);
    }

    @Test
    public void toClient_localResourceAndIsExpansionEnabledIsTrueAndIsExpandedIsFalse_returnReferenceResource() {
        mockClientConverter(resourceReferenceWithLocalResource.getLocalResource(), expectedClientObject);

        ToClientConversionOptions options = mock(ToClientConversionOptions.class);
        when(options.isExpansionEnabled(true)).thenReturn(true);
        when(options.isExpanded(DATA_TYPE_CLIENT_TYPE, true)).thenReturn(false);

        final ClientReferenceable result = converter.toClient(resourceReferenceWithLocalResource, options);
        assertEquals(result, expectedClientReference.deepClone().setVersion(-1));
    }

    @Test
    public void toClient_externalResourceAndIsExpansionEnabledIsTrueAndIsExpandedIsFalse_returnReferenceResource() {
        when(repositoryService.getResource(any(ExecutionContext.class), eq(testUri))).thenReturn(localResource);
        mockClientConverter(localResource, expectedClientObject);

        ToClientConversionOptions options = mock(ToClientConversionOptions.class);
        when(options.isExpansionEnabled(true)).thenReturn(true);
        when(options.isExpanded(DATA_TYPE_CLIENT_TYPE, true)).thenReturn(false);

        final ClientReferenceable result = converter.toClient(resourceReference, options);
        assertEquals(result, expectedClientReference);
    }

    @Test
    public void toClient_externalResourceAndIsExpansionEnabledAreTrueAndIsExpandedIsTrue_returnFullResource() {
        when(repositoryService.getResource(any(ExecutionContext.class), eq(testUri))).thenReturn(localResource);
        ToClientConverter defaultToClientConverter = mockClientConverter(localResource, null);
         mockClientConverterForPairServerResourceAndClientType(localResource,
                 defaultToClientConverter.getClientResourceType(), expectedClientObject);

        ToClientConversionOptions options = mock(ToClientConversionOptions.class);
        when(options.isExpansionEnabled(false)).thenReturn(true);
        when(options.isExpanded(DATA_TYPE_CLIENT_TYPE, false)).thenReturn(true);

        final ClientReferenceable result = converter.toClient(resourceReference, options);
        assertEquals(result, expectedClientObject);
    }

    @Test
    public void toServer_create_noInitialReference() throws Exception {
        final ClientJdbcDataSource clientObject = new ClientJdbcDataSource();
        final ResourceReferenceConverter referenceConverter = mock(ResourceReferenceConverter.class);
        when(referenceConverter.toServer(CTX, clientObject, null)).thenCallRealMethod();
        final ResourceReference resourceReference = referenceConverter.toServer(CTX, clientObject, null);
        assertNull(resourceReference);
        verify(referenceConverter).toServer(CTX, clientObject, null);
    }

    @Test
    public void toServer() throws Exception {
        final ClientReference reference = new ClientReference();
        final ResourceReferenceConverter converterMock = mock(ResourceReferenceConverter.class);
        when(converterMock.toServer(any(ExecutionContext.class), any(ClientReferenceable.class), isNull(ResourceReference.class), any(ToServerConversionOptions.class))).thenCallRealMethod();
        converterMock.toServer(CTX, reference, null, options);
        verify(converterMock).toServerReference(any(ExecutionContext.class), same(reference), isNull(ResourceReference.class), same(options));
        final ClientDataType dataType = new ClientDataType();
        converterMock.toServer(CTX, dataType, null, options);
        verify(converterMock).toServerLocalResource(any(ExecutionContext.class), same(dataType), isNull(ResourceReference.class), same(options));
        final ResourceReference result = converter.toServer(CTX, null, null);
        assertNull(result);
    }

    @Test(expectedExceptions = MandatoryParameterNotFoundException.class)
    public void validateReference_uriIsNull_exception() throws Exception {
        converter.validateAndGetReference(ExecutionContextImpl.getRuntimeExecutionContext(), null, options.getOwnersUri());
    }

    @Test(expectedExceptions = ReferencedResourceNotFoundException.class)
    public void validateReference_resourceDoesntExist() throws Exception {
        converter.validateAndGetReference(ExecutionContextImpl.getRuntimeExecutionContext(), "/test/resource/uri", options.getOwnersUri());
    }

    @Test()
    public void validateReference_additionalRestrictionIsCalled() throws Exception {
        final String referenceUri = "/test/resource/uri";
        final DataType resource = new DataTypeImpl();
        final ClientDataType clientTargetResource = new ClientDataType();
        when(repositoryService.getResource(any(ExecutionContext.class), eq(referenceUri))).thenReturn(resource);
        when(resourceConverterProvider.getToClientConverter(resource)).thenReturn(new ToClientConverter() {
            @Override
            public Object toClient(Object serverObject, Object options) {
                return clientTargetResource;
            }

            @Override
            public String getClientResourceType() {
                return null;
            }
        });
        final ClientReferenceRestriction restriction = mock(ClientReferenceRestriction.class);
        final IllegalParameterValueException expectedException = new IllegalParameterValueException("");
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                throw expectedException;
            }
        }).when(restriction).validateReference(clientTargetResource);
        IllegalParameterValueException exception = null;
        try {
            converter.addReferenceRestriction(restriction).validateAndGetReference(ExecutionContextImpl.getRuntimeExecutionContext(), referenceUri, options.getOwnersUri());
        } catch (IllegalParameterValueException e) {
            exception = e;
        }
        assertSame(exception, expectedException);
    }

    @Test
    public void toServerReference_theSameReferenceUri_noUpdate() throws Exception {
        final String expectedResourceUri = "/test/resource/uri";
        final ClientReference clientObject = new ClientReference();
        clientObject.setUri(expectedResourceUri);
        final ResourceReference resultToUpdate = new ResourceReference(expectedResourceUri);
        final ResourceReference result = converter.toServerReference(CTX, clientObject, resultToUpdate, options);
        assertSame(result, resultToUpdate);
        assertFalse(result.isLocal());
        assertEquals(result.getReferenceURI(), expectedResourceUri);
    }

    @Test
    public void toServerReference_validationIsCalled() throws Exception {
        final ClientReference reference = new ClientReference();
        final String referenceUri = "/test/resource/uri";
        reference.setUri(referenceUri);
        final ResourceReferenceConverter converterMock = mock(ResourceReferenceConverter.class);
        when(converterMock.toServerReference(CTX, reference, null, options)).thenCallRealMethod();
        converterMock.toServerReference(CTX, reference, null, options);
        verify(converterMock).validateAndGetReference(CTX, referenceUri, options.getOwnersUri(), options);
    }

    @Test
    public void toServerReference_initialResourceReferenceIsNull_newInstanceIsCreated() throws Exception {
        final String expectedResourceUri = "/test/resource/uri";
        final ClientReference clientObject = new ClientReference();
        clientObject.setUri(expectedResourceUri);
        // let repository mock find resource with our test URI
        when(repositoryService.getResource(any(ExecutionContext.class), eq(expectedResourceUri))).thenReturn(mock(Resource.class));
        // new ResourceReference is created if initially is null and client object contains referenceUri
        ResourceReference result = converter.toServerReference(CTX, clientObject, null, options);
        assertNotNull(result);
        assertFalse(result.isLocal());
        assertEquals(result.getReferenceURI(), expectedResourceUri);
    }

    @Test
    public void toServerReference_anotherReferenceUri_referenceIsUpdated() throws Exception {
        final String expectedResourceUri = "/test/resource/uri";
        final ClientReference clientObject = new ClientReference();
        clientObject.setUri(expectedResourceUri);
        // let repository mock find resource with our test URI
        when(repositoryService.getResource(any(ExecutionContext.class), eq(expectedResourceUri))).thenReturn(mock(Resource.class));
        final ResourceReference resultToUpdate = new ResourceReference("/another/resource/uri");
        ResourceReference result = converter.toServerReference(CTX, clientObject, resultToUpdate, options);
        assertSame(result, resultToUpdate);
        assertFalse(result.isLocal());
        // existing ResourceReference instance is updated with new reference URI
        assertEquals(result.getReferenceURI(), expectedResourceUri);
    }

    @DataProvider(name = "InvalidResourceURIs")
    public static Object[][] createInvalidResourceURIs() {
        return new Object[][] {
                new Object[]{new String("")},
                new Object[]{new String("invalid")}
        };
    }

    @Test(expectedExceptions = ReferencedResourceNotFoundException.class, dataProvider = "InvalidResourceURIs")
    public void toServerReference_dataSourceReferenceUri_invalid(final String invalidResourceUri) throws Exception {
        final ClientReference clientObject = new ClientReference();
        clientObject.setUri(invalidResourceUri);
        converter.toServerReference(CTX, clientObject, null, options);
    }

    @Test
    public void toServerReference_sameUri_localResourceIsNotChangedToReference() throws Exception {
        final String expectedResourceUri = "/test/resource/uri";
        final ClientReference clientObject = new ClientReference();
        clientObject.setUri(expectedResourceUri);
        // let repository mock find resource with our test URI
        when(repositoryService.getResource(isNull(ExecutionContext.class), eq(expectedResourceUri))).thenReturn(mock(Resource.class));
        // reference to update is local resource.
        final ResourceReference resultToUpdate = new ResourceReference(new DataTypeImpl());
        resultToUpdate.getLocalResource().setURIString(expectedResourceUri);
        ResourceReference result = converter.toServerReference(CTX, clientObject, resultToUpdate, options);
        assertSame(result, resultToUpdate);
        assertTrue(result.isLocal());
    }


    @Test
    public void toServerReference_changedUri_noContext_resultisReference() throws Exception {
        final String expectedResourceUri = "/test/resource/uri_files/local";
        final ClientReference clientObject = new ClientReference();
        clientObject.setUri(expectedResourceUri);
        Resource testResource = mock(Resource.class);
        // let repository mock find resource with our test URI
        when(repositoryService.getResource(any(ExecutionContext.class), eq(expectedResourceUri))).thenReturn(testResource);
        // reference some another resource
        final ResourceReference resultToUpdate = new ResourceReference(new DataTypeImpl());
        resultToUpdate.getLocalResource().setURIString("/some/another/resource");
        ResourceReference result = converter.toServerReference(CTX, clientObject, resultToUpdate, options);
        assertEquals(result.getTargetURI(), expectedResourceUri);
        assertFalse(result.isLocal());
    }


    @Test
    public void toServerReference_changedUri_noResultToUpdate_noContext_resultisReference() throws Exception {
        final String expectedResourceUri = "/test/resource/uri_files/local";
        final ClientReference clientObject = new ClientReference();
        clientObject.setUri(expectedResourceUri);
        Resource testResource = new DataTypeImpl();
        testResource.setURIString(expectedResourceUri);
        // let repository mock find resource with our test URI
        when(repositoryService.getResource(any(ExecutionContext.class), eq(expectedResourceUri))).thenReturn(testResource);
        ResourceReference result = converter.toServerReference(CTX, clientObject, null, options);
        assertEquals(result.getTargetURI(), expectedResourceUri);
        assertFalse(result.isLocal());
    }

    @Test
    public void toServerReference_changedUri_noResultToUpdate_localResourceIsNotChangedToReference() throws Exception {
        final String parentUri = "/test/resource/uri";
        final String expectedResourceUri = parentUri + "_files/local";
        final ClientReference clientObject = new ClientReference();
        clientObject.setUri(expectedResourceUri);
        Resource testResource = new DataTypeImpl();
        testResource.setURIString(expectedResourceUri);
        // let repository mock find resource with our test URI
        when(repositoryService.getResource(any(ExecutionContext.class), eq(expectedResourceUri))).thenReturn(testResource);
        // reference some another resource
        final ResourceReference resultToUpdate = new ResourceReference(new DataTypeImpl());
        resultToUpdate.getLocalResource().setURIString("/some/another/resource");
        ResourceReference result = converter.toServerReference(CTX, clientObject, null, ToServerConversionOptions.getDefault().setOwnersUri(parentUri));
        assertEquals(result.getTargetURI(), expectedResourceUri);
        assertTrue(result.isLocal());
    }

    @Test
    public void toServerReference_changedUri_notReallyLocal() throws Exception {
        final String parentUri = "/test/resource/uri";
        final String expectedResourceUri = parentUri + "_files_and_folders";
        final ClientReference clientObject = new ClientReference();
        clientObject.setUri(expectedResourceUri);
        Resource testResource = new DataTypeImpl();
        testResource.setURIString(expectedResourceUri);
        // let repository mock find resource with our test URI
        when(repositoryService.getResource(any(ExecutionContext.class), eq(expectedResourceUri))).thenReturn(testResource);
        // reference some another resource
        final ResourceReference resultToUpdate = new ResourceReference(new DataTypeImpl());
        resultToUpdate.getLocalResource().setURIString("/some/another/resource");
        ResourceReference result = converter.toServerReference(CTX, clientObject, null, ToServerConversionOptions.getDefault().setOwnersUri(parentUri));
        assertEquals(result.getTargetURI(), expectedResourceUri);
        assertFalse(result.isLocal());
    }

    @Test
    public void toServerLocalResource_resourceReferenceIsNull() throws Exception {
        final ClientAwsDataSource localResource = new ClientAwsDataSource().setLabel("L");
        final AwsReportDataSource expectedLocalResource = new AwsReportDataSourceImpl();
        final AwsDataSourceResourceConverter awsDataSourceResourceConverter = mock(AwsDataSourceResourceConverter.class);
        when(resourceConverterProvider.getToServerConverter(localResource)).thenReturn((ToServerConverter) awsDataSourceResourceConverter);
        when(awsDataSourceResourceConverter.toServer(CTX, localResource, options)).thenReturn(expectedLocalResource);
        final ResourceReference result = converter.toServerLocalResource(CTX,localResource, null, options);
        assertNotNull(result);
        assertTrue(result.isLocal());
        assertSame(result.getLocalResource(), expectedLocalResource);
    }

    @Test
    public void toServerLocalResource_referenceIsUpdated() throws Exception {
        final ClientAwsDataSource localResource = new ClientAwsDataSource().setLabel("L");
        final ResourceReference referenceToUpdate = new ResourceReference("/test/resource/uri");
        final AwsReportDataSource expectedLocalResource = new AwsReportDataSourceImpl();
        final AwsDataSourceResourceConverter awsDataSourceResourceConverter = mock(AwsDataSourceResourceConverter.class);
        when(resourceConverterProvider.getToServerConverter(localResource)).thenReturn((ToServerConverter) awsDataSourceResourceConverter);
        when(awsDataSourceResourceConverter.toServer(CTX, localResource, options)).thenReturn(expectedLocalResource);
        final ResourceReference result = converter.toServerLocalResource(CTX,  localResource, referenceToUpdate, options);
        assertSame(result, referenceToUpdate);
        assertTrue(result.isLocal());
        assertSame(result.getLocalResource(), expectedLocalResource);
    }

    @Test
    public void toServerLocalResource_localResourceIsReplaced() throws Exception {
        final ClientAwsDataSource localResource = new ClientAwsDataSource().setLabel("L");;
        final ResourceReference referenceToUpdate = new ResourceReference(new JdbcReportDataSourceImpl());
        final AwsReportDataSource expectedLocalResource = new AwsReportDataSourceImpl();
        final AwsDataSourceResourceConverter awsDataSourceResourceConverter = mock(AwsDataSourceResourceConverter.class);
        when(resourceConverterProvider.getToServerConverter(localResource)).thenReturn((ToServerConverter) awsDataSourceResourceConverter);
        when(awsDataSourceResourceConverter.toServer(CTX, localResource, options)).thenReturn(expectedLocalResource);
        final ResourceReference result = converter.toServerLocalResource(CTX, localResource, referenceToUpdate, options);
        assertSame(result, referenceToUpdate);
        assertTrue(result.isLocal());
        assertSame(result.getLocalResource(), expectedLocalResource);
    }

    @Test
    public void toServerLocalResource_versionIsReset() throws Exception {
        final ClientAwsDataSource localResource = new ClientAwsDataSource().setLabel("L");;
        localResource.setVersion(100);
        final ResourceReference referenceToUpdate = new ResourceReference(new JdbcReportDataSourceImpl());
        final AwsReportDataSource expectedLocalResource = new AwsReportDataSourceImpl();
        final AwsDataSourceResourceConverter awsDataSourceResourceConverter = mock(AwsDataSourceResourceConverter.class);
        when(resourceConverterProvider.getToServerConverter(localResource)).thenReturn((ToServerConverter) awsDataSourceResourceConverter);
        when(awsDataSourceResourceConverter.toServer(any(ExecutionContext.class), eq(localResource), any(ToServerConversionOptions.class))).thenReturn(expectedLocalResource);
        final ResourceReference result = converter.toServerLocalResource(CTX, localResource, referenceToUpdate, ToServerConversionOptions.getDefault().setResetVersion(true));
        assertSame(result, referenceToUpdate);
        assertTrue(result.isLocal());
        assertSame(result.getLocalResource(), expectedLocalResource);
        assertEquals(result.getLocalResource().getVersion(), Resource.VERSION_NEW);
    }

    @Test(expectedExceptions = IllegalParameterValueException.class)
    public void referenceClassRestriction_exception() throws Exception {
        new ResourceReferenceConverter.ReferenceClassRestriction(ClientReferenceableDataType.class).validateReference(new ClientJdbcDataSource());
    }

    @Test
    public void referenceClassRestriction_validReference() throws Exception {
        IllegalParameterValueException exception = null;
        try {
            new ResourceReferenceConverter.ReferenceClassRestriction(ClientReferenceableDataType.class).validateReference(new ClientDataType());
        } catch (IllegalParameterValueException e) {
            exception = e;
        }
        assertNull(exception);
    }

    @Test(expectedExceptions = IllegalParameterValueException.class)
    public void fileTypeRestriction_exception() throws Exception {
        final ClientFile file = new ClientFile();
        file.setType(ClientFile.FileType.css);
        new ResourceReferenceConverter.FileTypeRestriction(ClientFile.FileType.jrxml).validateReference(file);
    }

    @Test(expectedExceptions = MandatoryParameterNotFoundException.class)
    public void fileTypeRestrictionTypeIsNull_exception() throws Exception {
        final ClientFile file = new ClientFile();
        new ResourceReferenceConverter.FileTypeRestriction(ClientFile.FileType.jrxml).validateReference(file);
    }

    @Test
    public void fileTypeRestriction_validReference() throws Exception {
        final ClientFile file = new ClientFile();
        file.setType(ClientFile.FileType.jrxml);
        IllegalParameterValueException exception = null;
        try {
            // no exception for non file resources. Another restriction checks resource types
            new ResourceReferenceConverter.FileTypeRestriction(ClientFile.FileType.jrxml).validateReference(new ClientDataType());
            // no exception for file of correct type
            new ResourceReferenceConverter.FileTypeRestriction(ClientFile.FileType.jrxml).validateReference(file);
        } catch (IllegalParameterValueException e) {
            exception = e;
        }
        assertNull(exception);
    }


    private void getUUIDFromUri_NULL() {
        assertNull(ResourceReferenceConverter.getUUIDFromUri(null));
    }

    private void getUUIDFromUri_noProtocol() {
        assertNull(ResourceReferenceConverter.getUUIDFromUri(UUID.randomUUID().toString()));
    }

    private void getUUIDFromUri_withProtocol() {
        assertNotNull(ResourceReferenceConverter.getUUIDFromUri("uuid:" + UUID.randomUUID().toString()));
    }

    private void getUUIDFromUri_invalid() {
        assertNull(ResourceReferenceConverter.getUUIDFromUri("ABC"));
    }

    private ToClientConverter mockClientConverter(Resource serverObj, ClientResource clientObj) {
        ToClientConverter toClientConverter = new ToClientConverter() {
            @Override
            public Object toClient(Object serverObject, Object options) {
                assertSame(serverObject, serverObj);
                return clientObj;
            }

            @Override
            public String getClientResourceType() {
                return DATA_TYPE_CLIENT_TYPE;
            }
        };
        when(resourceConverterProvider.getToClientConverter(serverObj)).thenReturn(toClientConverter);

        return toClientConverter;
    }

    private ToClientConverter mockClientConverterForPairServerResourceAndClientType(Resource serverObj, String clientType, ClientResource clientObj) {
        ToClientConverter toClientConverter = new ToClientConverter() {
            @Override
            public Object toClient(Object serverObject, Object options) {
                assertSame(serverObject, serverObj);
                return clientObj;
            }

            @Override
            public String getClientResourceType() {
                return DATA_TYPE_CLIENT_TYPE;
            }
        };
        when(resourceConverterProvider.getToClientConverter(serverObj.getResourceType(), clientType)).thenReturn(toClientConverter);

        return toClientConverter;
    }
}
