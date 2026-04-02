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
 *//*

package com.jaspersoft.jasperserver.jaxrs.resources;

import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ContentResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.DataType;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResourceData;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.ContentResourceImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.DataTypeImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.FolderImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.InputControlImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConversionOptions;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConverter;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.client.ReportUnitImpl;
import com.jaspersoft.jasperserver.dto.resources.ClientDataType;
import com.jaspersoft.jasperserver.dto.resources.ClientFile;
import com.jaspersoft.jasperserver.dto.resources.ClientFolder;
import com.jaspersoft.jasperserver.dto.resources.ClientInputControl;
import com.jaspersoft.jasperserver.dto.resources.ClientProperty;
import com.jaspersoft.jasperserver.dto.resources.ClientResource;
import com.jaspersoft.jasperserver.dto.resources.ResourceMediaType;
import com.jaspersoft.jasperserver.remote.exception.AccessDeniedException;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.exception.MandatoryParameterNotFoundException;
import com.jaspersoft.jasperserver.remote.exception.NotAcceptableException;
import com.jaspersoft.jasperserver.remote.exception.ResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.resources.converters.ResourceConverterProvider;
import com.jaspersoft.jasperserver.remote.resources.converters.ToServerConversionOptions;
import com.jaspersoft.jasperserver.remote.resources.converters.ToServerConverter;
import com.jaspersoft.jasperserver.remote.services.SingleRepositoryService;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

*/
/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 *//*

public class ResourceDetailsJaxrsServiceTest {
    @InjectMocks
    private ResourceDetailsJaxrsService service = new ResourceDetailsJaxrsService();
    @Mock
    private ResourceConverterProvider resourceConverterProvider;
    @Mock
    private SingleRepositoryService repositoryService;
    @Mock
    private ToServerConverter serverConverter;
    @Mock
    private ToClientConverter clientConverter;
    @Mock
    private Map<String, String> contentTypeMapping;

    final private String uri = "test/folder";
    final private String uri2 = "/test/r";
    final private Resource folder = new FolderImpl();
    final private Resource res = new InputControlImpl();
    final private ClientInputControl clientRes = new ClientInputControl();

    @BeforeClass
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeMethod
    public void cleanUp() throws Exception{
        Mockito.reset(repositoryService, resourceConverterProvider, serverConverter, contentTypeMapping);
        when(resourceConverterProvider.getToServerConverter(nullable(ClientResource.class))).thenReturn(serverConverter);
        when(resourceConverterProvider.getToClientConverter(nullable(Resource.class))).thenReturn(clientConverter);
        when(resourceConverterProvider.getToClientConverter(nullable(String.class), nullable(String.class))).thenReturn(clientConverter);

        when(serverConverter.toServer(eq(clientRes), nullable(ToServerConversionOptions.class))).thenReturn(res);
        when(clientConverter.toClient(eq(res), nullable(ToClientConversionOptions.class))).thenReturn(clientRes);

        when(serverConverter.getServerResourceType()).thenReturn("server");
        when(clientConverter.getClientResourceType()).thenReturn("client");

        folder.setURIString("/");
        clientRes.setUri(Folder.SEPARATOR + uri);
        res.setURIString(Folder.SEPARATOR + uri);
        res.setVersion(0);

        clientRes.setLabel("client");
        res.setLabel("server");
    }

    @Test(groups = "GET")
    public void getResourceDetails_xml()  throws Exception{
        final DataType serverObject = new DataTypeImpl();
        final ClientDataType clientDataType = new ClientDataType();
        final String uri = "/test/resource/uri";
        final String clientResourceType = "testClientResourceType";
        when(repositoryService.getResource(eq(uri))).thenReturn(serverObject);
        when(resourceConverterProvider.getToClientConverter(serverObject)).thenReturn((ToClientConverter)new ToClientConverter<Resource, ClientResource, ToClientConversionOptions>() {
            @Override
            public ClientResource toClient(Resource serverObject, ToClientConversionOptions options) {
                return clientDataType;
            }

            @Override
            public String getClientResourceType() {
                return clientResourceType;
            }
        });
        final Response response = service.getResourceDetails(uri, MediaType.APPLICATION_XML, null, null, null, null);
        assertNotNull(response);
        assertSame(response.getEntity(), clientDataType);
        final List<Object> contentTypeHeaders = response.getMetadata().get(HttpHeaders.CONTENT_TYPE);
        assertNotNull(contentTypeHeaders);
        assertFalse(contentTypeHeaders.isEmpty());
        assertEquals(contentTypeHeaders.get(0), ResourceMediaType.RESOURCE_XML_TEMPLATE.replace(ResourceMediaType.RESOURCE_TYPE_PLACEHOLDER, clientResourceType));
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test(groups = "GET")
    public void getResourceDetails_json() throws Exception{
        final DataType serverObject = new DataTypeImpl();
        final ClientDataType clientDataType = new ClientDataType();
        final String uri = "test/resource/uri";
        final String clientResourceType = "testClientResourceType";
        when(repositoryService.getResource(eq(uri))).thenReturn(serverObject);
        when(resourceConverterProvider.getToClientConverter(serverObject)).thenReturn((ToClientConverter)new ToClientConverter<Resource, ClientResource, ToClientConversionOptions>() {
            @Override
            public ClientResource toClient(Resource serverObject, ToClientConversionOptions options) {
                return clientDataType;
            }

            @Override
            public String getClientResourceType() {
                return clientResourceType;
            }
        });
        final Response response = service.getResourceDetails(uri, MediaType.APPLICATION_JSON, null, null, null, null);
        assertNotNull(response);
        assertSame(response.getEntity(), clientDataType);
        final List<Object> contentTypeHeaders = response.getMetadata().get(HttpHeaders.CONTENT_TYPE);
        assertNotNull(contentTypeHeaders);
        assertFalse(contentTypeHeaders.isEmpty());
        assertEquals(contentTypeHeaders.get(0), ResourceMediaType.RESOURCE_JSON_TEMPLATE.replace(ResourceMediaType.RESOURCE_TYPE_PLACEHOLDER, clientResourceType));
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test(expectedExceptions = {ResourceNotFoundException.class})
    public void getResourceDetails_null() throws Exception {
        final String uri = "test/resource/uri";
        when(repositoryService.getResource(Folder.SEPARATOR + uri)).thenThrow(ResourceNotFoundException.class);
        service.getResourceDetails(uri, MediaType.APPLICATION_JSON, null, null, null, null);
    }

    @Test
    public void getResourceDetails_notAcceptable(){
        final String uri = "/test/resource/uri";
        final ReportUnitImpl reportUnit = new ReportUnitImpl();
        when(repositoryService.getResource(uri)).thenReturn(reportUnit);
        final ToClientConverter toClientConverterMock = mock(ToClientConverter.class);
        when(toClientConverterMock.toClient(any(Resource.class), any(ToClientConversionOptions.class)))
                .thenThrow(new NotAcceptableException());
        when(resourceConverterProvider.getToClientConverter(reportUnit.getResourceType(),
                ResourceMediaType.REPORT_UNIT_CLIENT_TYPE)).thenReturn(toClientConverterMock);
        final ToClientConverter defaultToClientConverterMock = mock(ToClientConverter.class);
        when(resourceConverterProvider.getToClientConverter(reportUnit)).thenReturn(defaultToClientConverterMock);
        when(defaultToClientConverterMock.getClientResourceType()).thenReturn("expectedType");
        NotAcceptableException resultException = null;
        try {
            service.getResourceDetails(uri, ResourceMediaType.REPORT_UNIT_JSON, null, null, null, null);
        } catch (NotAcceptableException e) {
            resultException = e;
        }
        assertNotNull(resultException);
        assertEquals(resultException.getErrorDescriptor(), new NotAcceptableException().getErrorDescriptor()
                .addProperties(
                        new ClientProperty("acceptableType", MediaType.APPLICATION_XML),
                        new ClientProperty("acceptableType", MediaType.APPLICATION_JSON),
                        new ClientProperty("acceptableType", ResourceMediaType.RESOURCE_MEDIA_TYPE_PREFIX +
                                "expectedType" + ResourceMediaType.RESOURCE_XML_TYPE),
                        new ClientProperty("acceptableType", ResourceMediaType.RESOURCE_MEDIA_TYPE_PREFIX +
                                "expectedType" + ResourceMediaType.RESOURCE_JSON_TYPE)
                ));
    }

    @Test(groups = "GET")
    public void getResourceDetails_file_json() throws Exception{
        final ContentResource serverObject = new ContentResourceImpl();
        final ClientFile clientFile = new ClientFile();
        final String uri = "test/resource/uri";
        final String clientResourceType = ResourceMediaType.FILE_CLIENT_TYPE;
        when(repositoryService.getResource(eq(uri))).thenReturn(serverObject);
        when(resourceConverterProvider.getToClientConverter(serverObject.getResourceType(), clientResourceType)).thenReturn((ToClientConverter)new ToClientConverter<Resource, ClientResource, ToClientConversionOptions>() {
            @Override
            public ClientResource toClient(Resource serverObject, ToClientConversionOptions options) {
                return clientFile;
            }

            @Override
            public String getClientResourceType() {
                return clientResourceType;
            }
        });
        final Response response = service.getResourceDetails(uri, ResourceMediaType.FILE_JSON, null, null, null, null);
        assertNotNull(response);
        assertSame(response.getEntity(), clientFile);
        final List<Object> contentTypeHeaders = response.getMetadata().get(HttpHeaders.CONTENT_TYPE);
        assertNotNull(contentTypeHeaders);
        assertFalse(contentTypeHeaders.isEmpty());
        assertEquals(contentTypeHeaders.get(0), ResourceMediaType.RESOURCE_JSON_TEMPLATE.replace(ResourceMediaType.RESOURCE_TYPE_PLACEHOLDER, clientResourceType));
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test(groups = "GET")
    public void getResourceDetails_file_binary() throws Exception{
        final ContentResource serverObject = new ContentResourceImpl();
        final ClientFile clientDataType = new ClientFile();
        final String uri = "test/resource/uri";
        final String clientResourceType = ResourceMediaType.FILE_CLIENT_TYPE;
        serverObject.setName("index.html");

        when(repositoryService.getResource(eq(uri))).thenReturn(serverObject);
        when(resourceConverterProvider.getToClientConverter(serverObject)).thenReturn((ToClientConverter)new ToClientConverter<Resource, ClientResource, ToClientConversionOptions>() {
            @Override
            public ClientResource toClient(Resource serverObject, ToClientConversionOptions options) {
                return clientDataType;
            }

            @Override
            public String getClientResourceType() {
                return clientResourceType;
            }
        });
        when(repositoryService.getFileResourceData(serverObject)).thenReturn(new FileResourceData(new byte[10]));

        final Response response = service.getResourceDetails(uri, null, null, null, null, null);

        assertNotNull(response);
        final List<Object> contentTypeHeaders = response.getMetadata().get(HttpHeaders.CONTENT_TYPE);
        assertNotNull(contentTypeHeaders);
        assertFalse(contentTypeHeaders.isEmpty());
        assertEquals(contentTypeHeaders.get(0).toString(), MediaType.APPLICATION_OCTET_STREAM);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test(groups = "GET", expectedExceptions = ResourceNotFoundException.class)
    public void getResourceDetails_uuid() throws Exception{
        final ContentResource serverObject = new ContentResourceImpl();
        final ClientFile clientFile = new ClientFile();
        final String uri = "/uuid:abc";
        final String clientResourceType = ResourceMediaType.FILE_CLIENT_TYPE;
        when(repositoryService.getResource(eq(uri))).thenReturn(serverObject);
        when(resourceConverterProvider.getToClientConverter(serverObject.getResourceType(), clientResourceType)).thenReturn((ToClientConverter)new ToClientConverter<Resource, ClientResource, ToClientConversionOptions>() {
            @Override
            public ClientResource toClient(Resource serverObject, ToClientConversionOptions options) {
                return clientFile;
            }

            @Override
            public String getClientResourceType() {
                return clientResourceType;
            }
        });
        final Response response = service.getResourceDetails(uri, ResourceMediaType.FILE_JSON, null, null, null, null);
    }

    @Test(groups = "GET", expectedExceptions = ResourceNotFoundException.class)
    public void getResourceDetails_uuid2() throws Exception{
        final ContentResource serverObject = new ContentResourceImpl();
        final ClientFile clientFile = new ClientFile();
        final String uri = "/uuid:0f85ea9b-63f0-4083-844d-ce2e4e1bc51c";
        final String clientResourceType = ResourceMediaType.FILE_CLIENT_TYPE;
        when(repositoryService.getResource(eq(uri))).thenReturn(serverObject);
        when(resourceConverterProvider.getToClientConverter(serverObject.getResourceType(), clientResourceType)).thenReturn((ToClientConverter)new ToClientConverter<Resource, ClientResource, ToClientConversionOptions>() {
            @Override
            public ClientResource toClient(Resource serverObject, ToClientConversionOptions options) {

                return clientFile;
            }

            @Override
            public String getClientResourceType() {
                return clientResourceType;
            }
        });
        final Response response = service.getResourceDetails(uri, ResourceMediaType.FILE_JSON, null, null, null, null);
    }



    @Test(groups = "DELETE")
    public void testDeleteResource_delegation() throws Exception{
        Response response = service.deleteResource(uri);

        verify(repositoryService).deleteResource(uri);
    }

    @Test(groups = "DELETE")
    public void testDeleteResource_Status() throws Exception{
        Response response = service.deleteResource(uri);

        assertEquals(response.getStatus(), HttpServletResponse.SC_NO_CONTENT);
    }

    @Test(groups = "POST")
    public void testCreateResource_convertsToServer() throws Exception {
        String name = "ups";
        Resource created = new DataTypeImpl();
        Mockito.when(repositoryService.getResource(anyString())).thenReturn(res);
        Mockito.when(repositoryService.createResource(any(Resource.class), anyString(), anyBoolean(), eq(false))).thenReturn(created);
        Mockito.when(repositoryService.getUniqueName(anyString(), anyString())).thenReturn(name);

        final HashMap<String, String[]> additionalProperties = new HashMap<String, String[]>();
        final ArgumentCaptor<ToClientConversionOptions> optionsArgumentCaptor = ArgumentCaptor.forClass(ToClientConversionOptions.class);
        final ClientFolder expectedResult = new ClientFolder();
        doReturn(expectedResult).when(clientConverter).toClient(same(created), optionsArgumentCaptor.capture());

        final ClientResource result = service.createResource(clientRes, uri, true, false, null, additionalProperties);

        assertSame(result, expectedResult);
        Mockito.verify(serverConverter).toServer(clientRes, ToServerConversionOptions.getDefault().setOwnersUri(uri + Folder.SEPARATOR + name).setResetVersion(true));
        final ToClientConversionOptions options = optionsArgumentCaptor.getValue();
        assertNotNull(options);
        assertSame(options.getAdditionalProperties(), additionalProperties);
    }

    @Test(groups = "POST")
    public void testCreateResource_dryRun() throws Exception {
        String name = "ups";
        Mockito.when(repositoryService.getResource(anyString())).thenReturn(res);
        Mockito.when(repositoryService.getUniqueName(anyString(), anyString())).thenReturn(name);

        service.createResource(clientRes, uri, true, true, null, null);
        Mockito.verify(repositoryService).createResource(any(Resource.class), anyString(), anyBoolean(), eq(true));
    }

    @Test(groups = "COPY")
    public void testCopyResource_copy() throws Exception {
        Mockito.when(repositoryService.getResource(nullable(String.class))).thenReturn(res);
        Response response = service.defaultPostHandler(null,uri + uri, uri,"", "", "", null, true, true, null, false, null);

        verify(repositoryService).copyResource(uri, uri + uri, true, true, null);
    }

    @Test(groups = "COPY")
    public void testCopyResource_status() throws Exception {
        Mockito.when(repositoryService.getResource(nullable(String.class))).thenReturn(res);
        Response response = service.defaultPostHandler(null, uri, uri + uri, "", "", "", null, true, true, null, false, null);

        assertEquals(response.getStatus(), HttpServletResponse.SC_OK);
    }

    @Test(groups = "COPY", expectedExceptions = {ResourceNotFoundException.class}, dependsOnMethods = {"testCopyResource_copy"})
    public void testCopyResource_notFound() throws Exception {
        doThrow(ResourceNotFoundException.class).when(repositoryService).copyResource(anyString(), anyString(),
                anyBoolean(), anyBoolean(), isNull(String.class));

        Response response = service.defaultPostHandler(null, uri, uri + uri, "", "", "", "",true, true, null, false, null);
    }

    @Test(groups = "COPY", expectedExceptions = {AccessDeniedException.class}, dependsOnMethods = {"testCopyResource_copy"})
    public void testCopyResource_accessDenied() throws Exception {
        doThrow(AccessDeniedException.class).when(repositoryService).copyResource(anyString(), anyString(), anyBoolean(),
                anyBoolean(), isNull(String.class));

        Response response = service.defaultPostHandler(null, uri, uri + uri, "", "", "","", true, true, null, false, null);
    }

    @Test(groups = "MOVE")
    public void testMoveResource_move() throws Exception {
        Mockito.when(repositoryService.getResource(nullable(String.class))).thenReturn(res);
        Response response = service.defaultPutHandler(null,Folder.SEPARATOR +  uri + uri, uri, "", null ,"", null, true,
                true, null, false, null);

        verify(repositoryService).moveResource(uri, Folder.SEPARATOR + uri + uri, true, true, null);
    }

    @Test(groups = "MOVE")
    public void testMoveResource_status() throws Exception {
        Mockito.when(repositoryService.getResource(nullable(String.class))).thenReturn(res);
        Response response = service.defaultPutHandler(null, Folder.SEPARATOR + uri, uri + uri, "", null ,"",null, true,
                true, null, false, null);

        assertEquals(response.getStatus(), HttpServletResponse.SC_OK);
    }

    @Test(groups = "MOVE", expectedExceptions = {ResourceNotFoundException.class}, dependsOnMethods = {"testCopyResource_copy"})
    public void testMoveResource_notFound() throws Exception {
        doThrow(ResourceNotFoundException.class).when(repositoryService).moveResource(anyString(), anyString(),
                anyBoolean(), anyBoolean(), isNull(String.class));

        Response response = service.defaultPutHandler(null, uri, uri + uri, "", null, "",null, true, true, null, false, null);
    }

    @Test(groups = "MOVE", expectedExceptions = {AccessDeniedException.class}, dependsOnMethods = {"testCopyResource_copy"})
    public void testMoveResource_accessDenied() throws Exception {
        doThrow(AccessDeniedException.class).when(repositoryService).moveResource(anyString(), anyString(), anyBoolean(),
                anyBoolean(), isNull(String.class));

        Response response = service.defaultPutHandler(null, uri, uri + uri, "", null, "", null,true, true, null, false, null);
    }

    @Test(groups = "CREATE_RESOURCE_MULTIPART")
    public void createResourceViaForm_fileCreation() throws ErrorDescriptorException {
        ResourceDetailsJaxrsService serviceMock = mock(ResourceDetailsJaxrsService.class);
        final FormDataMultiPart multiPart = new FormDataMultiPart();
        final String expectedLabel = "testLabel";
        final String expectedDescription = "testDescription";
        final String expectedType = "testType";
        final InputStream expectedData = mock(InputStream.class);
        final FormDataBodyPart bodyPartMock = mock(FormDataBodyPart.class);
        when(bodyPartMock.getEntityAs(InputStream.class)).thenReturn(expectedData);
        when(bodyPartMock.getName()).thenReturn("data");
        multiPart.bodyPart(bodyPartMock);
        multiPart.field("label", expectedLabel);
        multiPart.field("description", expectedDescription);
        multiPart.field("type", expectedType);
        final String expectedMimeType = "someMimeType";
        final HashMap<String, String[]> additionalProperties = new HashMap<String, String[]>();
        when(serviceMock.createResourceViaForm(multiPart, uri, Boolean.FALSE, expectedMimeType, false, additionalProperties)).thenCallRealMethod();
        when(serviceMock.createFileViaForm(eq(expectedData), eq(uri), eq(expectedLabel), eq(expectedDescription), eq(expectedType), eq(Boolean.FALSE), eq(false))).thenReturn(clientRes);
        final ClientResource resource = serviceMock.createResourceViaForm(multiPart, uri, Boolean.FALSE, expectedMimeType, false, additionalProperties);
        assertSame(resource, clientRes);
    }

    @Test(groups = "CREATE_RESOURCE_MULTIPART")
    public void createResourceViaForm_resourceCreation() throws ErrorDescriptorException {
        final FormDataMultiPart multiPart = new FormDataMultiPart();
        FormDataBodyPart bodyPartMock = mock(FormDataBodyPart.class);
        when(bodyPartMock.getEntityAs(ClientInputControl.class)).thenReturn(clientRes);
        when(bodyPartMock.getName()).thenReturn("resource");
        when(bodyPartMock.getMediaType()).thenReturn(MediaType.valueOf(ResourceMediaType.INPUT_CONTROL_XML));
        multiPart.bodyPart(bodyPartMock);
        bodyPartMock = mock(FormDataBodyPart.class);
        final InputStream expectedPartInputStream = mock(InputStream.class);
        when(bodyPartMock.getEntityAs(InputStream.class)).thenReturn(expectedPartInputStream);
        final String expectedPartName = "testPartName";
        when(bodyPartMock.getName()).thenReturn(expectedPartName);
        multiPart.bodyPart(bodyPartMock);
        when(resourceConverterProvider.getClientTypeClass(ResourceMediaType.INPUT_CONTROL_CLIENT_TYPE)).thenReturn((Class) ClientInputControl.class);
        final ToServerConverter toServerConverter = mock(ToServerConverter.class);
        final ArgumentCaptor<ToServerConversionOptions> toServerConversionOptionsArgumentCaptor = ArgumentCaptor.forClass(ToServerConversionOptions.class);
        when(toServerConverter.toServer(eq(clientRes), toServerConversionOptionsArgumentCaptor.capture())).thenReturn(res);
        when(resourceConverterProvider.getToServerConverter(clientRes)).thenReturn((ToServerConverter) toServerConverter);
        when(repositoryService.createResource(res, uri, Boolean.FALSE, false)).thenReturn(res);
        final ClientResource resource = service.createResourceViaForm(multiPart, uri, Boolean.FALSE, null, false, null);
        assertSame(resource, clientRes);
        assertNotNull(toServerConversionOptionsArgumentCaptor.getValue());
        Map<String, InputStream> parts = toServerConversionOptionsArgumentCaptor.getValue().getAttachments();
        assertNotNull(parts);
        assertTrue(parts.size() == 1);
        final Map.Entry<String, InputStream> entry = parts.entrySet().iterator().next();
        assertEquals(entry.getKey(), expectedPartName);
        assertSame(entry.getValue(), expectedPartInputStream);
    }

    @Test(groups = "CREATE_RESOURCE_MULTIPART", expectedExceptions = IllegalParameterValueException.class)
    public void createResourceViaForm_invalidResourceMediaType() throws ErrorDescriptorException {
        final FormDataMultiPart multiPart = new FormDataMultiPart();
        final FormDataBodyPart bodyPartMock = mock(FormDataBodyPart.class);
        when(bodyPartMock.getEntityAs(ClientResource.class)).thenReturn(clientRes);
        when(bodyPartMock.getName()).thenReturn("resource");
        when(bodyPartMock.getMediaType()).thenReturn(MediaType.WILDCARD_TYPE);
        multiPart.bodyPart(bodyPartMock);
        service.createResourceViaForm(multiPart, uri, Boolean.FALSE, null, false, null);
    }

    @Test(groups = "CREATE_FILE")
    public void extractType_mimeType_caseInsensitive(){
        assertEquals(service.extractType(ClientFile.FileType.accessGrantSchema.getMimeType().toUpperCase(), null),
                ClientFile.FileType.accessGrantSchema.toString());
        assertEquals(service.extractType(ClientFile.FileType.accessGrantSchema.getMimeType().toLowerCase(), null),
                ClientFile.FileType.accessGrantSchema.toString());

    }

    @Test(groups = "CREATE_FILE")
    public void testCreateFile() throws Exception {
        String name = "test.pdf", description = "test", type = "application/pdf; charset=UTF-8";
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream, Folder.SEPARATOR + uri, name, name, description, ContentResource.TYPE_PDF,  true, false)).thenReturn(res);

        Response response = service.defaultPostHandler(stream, Folder.SEPARATOR + uri, null, "attachment; filename="+name,
                description, type, "",true, true, null, false, null);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        verify(repositoryService).createFileResource(stream, Folder.SEPARATOR + uri, name, name, description, ContentResource.TYPE_PDF,  true, false);
    }

    @Test(groups = "CREATE_FILE", expectedExceptions = {IllegalParameterValueException.class})
    public void testCreateFile_no_disposition() throws Exception {
        String name = "test.pdf", description = "test", type = "application/pdf; charset=UTF-8";
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream,uri ,name,name,description,type,true, false)).thenReturn(res);

        Response response = service.defaultPostHandler(stream, uri, null, null, type, description,"", true, true, null,
                false, null);
    }

    @Test(groups = "CREATE_FILE", expectedExceptions = {IllegalParameterValueException.class})
    public void testCreateFile_illegal_disposition() throws Exception {
        String name = "test.pdf", description = "test", type = "application/pdf; charset=UTF-8";
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream,uri,name,name,description,type,true, false)).thenReturn(res);

        Response response = service.defaultPostHandler(stream, uri, null, type, type, description,"", true, true, null,
                false, null);
    }

    @Test(groups = "CREATE_FILE", expectedExceptions = {IllegalParameterValueException.class})
    public void testCreateFile_no_file_name() throws Exception {
        String name = "", description = "test", type = "application/pdf; charset=UTF-8";
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream,uri,name,name,description,type,true, false)).thenReturn(res);

        Response response = service.defaultPostHandler(stream, uri, null, "attachment; filename="+name, type,"",
                description, true, true, null, false, null);
    }

    @Test(groups = "CREATE_FILE", expectedExceptions = {IllegalParameterValueException.class})
    public void testCreateFile_extracts_type_from_name() throws Exception {
        String name = "", description = "test", type = "application/pdf; charset=UTF-8";
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream,uri,name,name,description,type,true, false)).thenReturn(res);

        Response response = service.defaultPostHandler(stream, uri, null, "attachment; filename="+name, type,"",
                description, true, true, null, false, null);
    }

    @Test(groups = "CREATE_FILE")
    public void testCreateFile_simple_type() throws Exception {
        String name = "test.pdf", description = "test", type = "application/pdf; charset=UTF-8";
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream, Folder.SEPARATOR + uri, name, name, description, ContentResource.TYPE_PDF,  true, false)).thenReturn(res);

        Response response = service.defaultPostHandler(stream, Folder.SEPARATOR + uri, null, "attachment; filename="+name,
                description, type, null, true, true, null, false, null);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        verify(repositoryService).createFileResource(stream, Folder.SEPARATOR + uri, name,  name,description, ContentResource.TYPE_PDF,  true, false);
    }

    @Test(groups = "CREATE_FILE")
    public void testCreateFile_wildcard_type() throws Exception {
        String name = "test.pdf", description = "test", type = "application/pdf; charset=UTF-8";
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream, Folder.SEPARATOR + uri, name,  name,description,
                ContentResource.TYPE_PDF,  true, false)).thenReturn(res);

        Response response = service.defaultPostHandler(stream, Folder.SEPARATOR + uri, null, "attachment; filename="+name,
                description, type, null, true, true, null, false, null);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        verify(repositoryService).createFileResource(stream, Folder.SEPARATOR + uri, name, name, description,
                ContentResource.TYPE_PDF,  true, false);
    }

    @Test(groups = "CREATE_FILE")
    public void testCreateFile_extension() throws Exception {
        String name = "test.pdf", description = "test", type = "application/pdf; charset=UTF-8";
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream, Folder.SEPARATOR + uri, name,  name, description, ContentResource.TYPE_PDF,  true, false)).thenReturn(res);
        when(contentTypeMapping.containsKey("pdf")).thenReturn(true);

        Response response = service.defaultPostHandler(stream, Folder.SEPARATOR + uri, null, "attachment; filename="+name,
                description, "",type, true, true, null, false, null);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        verify(repositoryService).createFileResource(stream, Folder.SEPARATOR + uri, name,  name, description,
                ContentResource.TYPE_PDF,  true, false);
    }

    @Test(groups = "CREATE_FILE")
    public void testCreateFile_multipart() throws Exception {
        String name = "test.pdf", description = "test", type = ContentResource.TYPE_PDF;
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream, Folder.SEPARATOR + uri, name,  name, description, ContentResource.TYPE_PDF,  true, false)).thenReturn(res);

        final ClientResource clientResource = service.createFileViaForm(stream, Folder.SEPARATOR + uri, name, description, type, true, false);
        assertSame(clientResource, clientRes);
        verify(repositoryService).createFileResource(stream, Folder.SEPARATOR + uri, name,  name, description, ContentResource.TYPE_PDF,  true, false);

    }

    @Test(groups = "CREATE_FILE", expectedExceptions = {MandatoryParameterNotFoundException.class})
    public void testCreateFile_multipart_empty_stream() throws Exception {
        String name = "test.pdf", description = "test", type = ContentResource.TYPE_PDF;
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream,uri,name,name,description,type,true, false)).thenReturn(res);
        final ClientResource clientResource = service.createFileViaForm(null, uri, name, description, type, true, false);
        assertSame(clientRes, clientResource);
    }

    @Test(groups = "CREATE_FILE", expectedExceptions = {MandatoryParameterNotFoundException.class})
    public void testCreateFile_multipart_empty_label() throws Exception {
        String name = "test.pdf", description = "test", type = ContentResource.TYPE_PDF;
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream,uri,name,name,description,type,true, false)).thenReturn(res);

        final ClientResource clientResource = service.createFileViaForm(stream, uri, null, description, type, true, false);
        assertSame(clientRes, clientResource);
    }

    @Test(groups = "CREATE_FILE", expectedExceptions = {MandatoryParameterNotFoundException.class})
    public void testCreateFile_multipart_empty_uri() throws Exception {
        String name = "test.pdf", description = "test", type = ContentResource.TYPE_PDF;
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream,uri,name,name,description,type,true, false)).thenReturn(res);

        final ClientResource clientResource = service.createFileViaForm(stream, null, name, description, type, true, false);
        assertSame(clientRes, clientResource);
    }

    @Test(groups = "CREATE_FILE", expectedExceptions = {MandatoryParameterNotFoundException.class})
    public void testCreateFile_multipart_empty_type() throws Exception {
        String name = "test.pdf", description = "test", type = ContentResource.TYPE_PDF;
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream,uri,name,name,description,type,true, false)).thenReturn(res);

        final ClientResource clientResource = service.createFileViaForm(stream, uri, name, description, null, true, false);
        assertSame(clientRes, clientResource);
    }

    @Test(groups = "UPDATE_FILE")
    public void testUpdateFile() throws Exception {
        String name = "test.pdf", description = "test", type = "application/pdf; charset=UTF-8";
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.updateFileResource(stream, Folder.SEPARATOR + uri, name,name, description, ContentResource.TYPE_PDF, false)).thenReturn(res);
        when(repositoryService.getResource(Folder.SEPARATOR + uri + Folder.SEPARATOR + name)).thenReturn(res);

        Response response = service.defaultPutHandler(stream, Folder.SEPARATOR + uri + Folder.SEPARATOR + name, null,
                null, description, type,null, true, true, null, false, null);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        verify(repositoryService).updateFileResource(stream, Folder.SEPARATOR + uri, name, name,description, ContentResource.TYPE_PDF, false);
    }

    @Test(groups = "UPDATE_FILE")
    public void testUpdateFile_create() throws Exception {
        String name = "test.pdf", description = "test", type = "application/pdf; charset=UTF-8";
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream, Folder.SEPARATOR + uri, name,  name, description, ContentResource.TYPE_PDF,  true, false)).thenReturn(res);

        Response response = service.defaultPutHandler(stream, Folder.SEPARATOR + uri + Folder.SEPARATOR + name, null,
                null, description, type,null, true, true, null, false, null);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        verify(repositoryService).createFileResource(stream, Folder.SEPARATOR + uri, name, name, description, ContentResource.TYPE_PDF,  true, false);
    }

    @Test(groups = "UPDATE_FILE", expectedExceptions = {IllegalParameterValueException.class})
    public void testUpdateFile_extracts_type_from_name() throws Exception {
        String name = "", description = "test", type = "application/pdf; charset=UTF-8";
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream,uri,name,name,description,type,true, false)).thenReturn(res);
        when(repositoryService.getResource(Folder.SEPARATOR + uri + Folder.SEPARATOR + name)).thenReturn(res);

        Response response = service.defaultPutHandler(stream, uri + Folder.SEPARATOR + name, null,  null, description,
                type,null,true, true, null, false, null);
    }

    @Test(groups = "UPDATE_FILE")
    public void testUpdateFile_simple_type() throws Exception {
        String name = "test.pdf", description = "test", type = "application/pdf; charset=UTF-8";
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream, Folder.SEPARATOR + uri, name,  name, description, ContentResource.TYPE_PDF,  true, false)).thenReturn(res);
        when(repositoryService.getResource(Folder.SEPARATOR + uri + Folder.SEPARATOR + name)).thenReturn(res);

        Response response = service.defaultPutHandler(stream,Folder.SEPARATOR +  uri + Folder.SEPARATOR + name, null,
                null, description, type, null,true, true, null, false, null);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        verify(repositoryService).updateFileResource(stream, Folder.SEPARATOR + uri, name, name, description, ContentResource.TYPE_PDF, false);
    }

    @Test(groups = "UPDATE_FILE")
    public void testUpdateFile_wildcard_type() throws Exception {
        String name = "test.pdf", description = "test", type = "application/pdf; charset=UTF-8";
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream, Folder.SEPARATOR + uri, name,  name, description, ContentResource.TYPE_PDF,  true, false)).thenReturn(res);
        when(repositoryService.getResource(Folder.SEPARATOR + uri + Folder.SEPARATOR + name)).thenReturn(res);

        Response response = service.defaultPutHandler(stream, Folder.SEPARATOR + uri + Folder.SEPARATOR + name, null,
                null, description, type,null, true, true, null, false, null);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        verify(repositoryService).updateFileResource(stream, Folder.SEPARATOR + uri, name, name, description, ContentResource.TYPE_PDF, false);
    }

    @Test(groups = "UPDATE_FILE")
    public void testUpdateFile_extension() throws Exception {
        String name = "test.pdf", description = "test", type = "application/pdf; charset=UTF-8";
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream, Folder.SEPARATOR + uri, name,  name, description, ContentResource.TYPE_PDF,  true, false)).thenReturn(res);
        when(contentTypeMapping.containsKey("pdf")).thenReturn(true);
        when(repositoryService.getResource(Folder.SEPARATOR + uri + Folder.SEPARATOR + name)).thenReturn(res);

        Response response = service.defaultPutHandler(stream, Folder.SEPARATOR + uri + Folder.SEPARATOR + name, null,
                null, description, type,null, true, true, null, false, null);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        verify(repositoryService).updateFileResource(stream, Folder.SEPARATOR + uri, name, name, description, ContentResource.TYPE_PDF, false);
    }

    @Test(groups = "UPDATE_FILE")
    public void testUpdateFile_root() throws Exception {
        String name = "test.pdf", description = "test", type = "application/pdf; charset=UTF-8";
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream, Folder.SEPARATOR, name,  name, description, ContentResource.TYPE_PDF,  true, false)).thenReturn(res);
        when(contentTypeMapping.containsKey("pdf")).thenReturn(true);
        when(repositoryService.getResource(Folder.SEPARATOR + name)).thenReturn(res);

        Response response = service.defaultPutHandler(stream, Folder.SEPARATOR + name, null, null, description, type,
                null,true, true, null, false, null);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        verify(repositoryService).updateFileResource(stream, Folder.SEPARATOR, name, name, description, ContentResource.TYPE_PDF, false);
    }

    @Test(groups = "UPDATE_FILE")
    public void testUpdateFile_multipart() throws Exception {
        String name = "test.pdf", description = "test", type = ContentResource.TYPE_PDF;
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream, Folder.SEPARATOR + uri, name, name, description, ContentResource.TYPE_PDF,  true, false)).thenReturn(res);
        when(repositoryService.getResource(Folder.SEPARATOR + uri + Folder.SEPARATOR + name)).thenReturn(res);

        Response response = service.updateFileViaForm(stream, uri + Folder.SEPARATOR + name, name,  description, type, false);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        verify(repositoryService).updateFileResource(stream, Folder.SEPARATOR + uri, name, name, description, ContentResource.TYPE_PDF, false);
    }

    @Test(groups = "UPDATE_FILE")
    public void testUpdateFile_multipart_root() throws Exception {
        String name = "test.pdf", description = "test", type = ContentResource.TYPE_PDF;
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream, Folder.SEPARATOR, name,  name, description, ContentResource.TYPE_PDF,  true, false)).thenReturn(res);
        when(repositoryService.getResource(Folder.SEPARATOR + name)).thenReturn(res);

        Response response = service.updateFileViaForm(stream, name, name,  description, type, false);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        verify(repositoryService).updateFileResource(stream, Folder.SEPARATOR, name, name, description, ContentResource.TYPE_PDF, false);
    }

    @Test(groups = "UPDATE_FILE")
    public void testUpdateFile_multipart_create() throws Exception {
        String name = "test.pdf", description = "test", type = ContentResource.TYPE_PDF;
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream, Folder.SEPARATOR + uri, name, name, description, ContentResource.TYPE_PDF,  true, false)).thenReturn(res);

        Response response = service.updateFileViaForm(stream, uri + Folder.SEPARATOR + name, name,  description, type, false);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        verify(repositoryService).createFileResource(stream, Folder.SEPARATOR + uri, name, name, description, ContentResource.TYPE_PDF,  true, false);
    }

    @Test(groups = "UPDATE_FILE", expectedExceptions = {MandatoryParameterNotFoundException.class})
    public void testUpdateFile_multipart_empty_stream() throws Exception {
        String name = "test.pdf", description = "test", type = ContentResource.TYPE_PDF;
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream,uri,name,name,description,type,true, false)).thenReturn(res);
        when(repositoryService.getResource(Folder.SEPARATOR + uri + Folder.SEPARATOR + name)).thenReturn(res);

        Response response = service.updateFileViaForm(null, uri + Folder.SEPARATOR + name, name,  description, type, false);
    }

    @Test(groups = "UPDATE_FILE")
    public void testUpdateFile_multipart_empty_label() throws Exception {
        String name = "test.pdf", description = "test", type = ContentResource.TYPE_PDF;
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream,uri,name,name,description,type,true, false)).thenReturn(res);
        when(repositoryService.getResource(Folder.SEPARATOR + uri + Folder.SEPARATOR + name)).thenReturn(res);

        Response response = service.updateFileViaForm(stream, uri + Folder.SEPARATOR + name, null,  description, type, false);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        verify(repositoryService).updateFileResource(stream, Folder.SEPARATOR + uri, name, name, description, ContentResource.TYPE_PDF, false);
    }

    @Test(groups = "UPDATE_FILE", expectedExceptions = {MandatoryParameterNotFoundException.class})
    public void testUpdateFile_multipart_empty_uri() throws Exception {
        String name = "test.pdf", description = "test", type = ContentResource.TYPE_PDF;
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream,uri,name,name,description,type,true, false)).thenReturn(res);
        when(repositoryService.getResource(Folder.SEPARATOR + uri + Folder.SEPARATOR + name)).thenReturn(res);

        Response response = service.updateFileViaForm(stream, null, name,  description, type, false);
    }

    @Test(groups = "UPDATE_FILE", expectedExceptions = {MandatoryParameterNotFoundException.class})
    public void testUpdateFile_multipart_empty_type() throws Exception {
        String name = "test.pdf", description = "test", type = ContentResource.TYPE_PDF;
        InputStream stream = new ByteArrayInputStream(new byte[5]);

        when(repositoryService.createFileResource(stream,uri,name,name,description,type,true, false)).thenReturn(res);
        when(repositoryService.getResource(Folder.SEPARATOR + uri + Folder.SEPARATOR + name)).thenReturn(res);

        Response response = service.updateFileViaForm(stream, uri, name,  description, null, false);
    }

    @Test
    public void toResponse_hasNoData(){
        final Response response = service.toResponse(new FileResourceData((byte[]) null), null, null);
        assertEquals(response.getStatus(), 204);
    }
}
*/

