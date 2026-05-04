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
package com.jaspersoft.jasperserver.remote.resources.converters;

import com.jaspersoft.jasperserver.api.common.crypto.PasswordCipherer;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.FileResourceImpl;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.dto.common.ClientTypeUtility;
import com.jaspersoft.jasperserver.dto.resources.ClientFile;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jakarta.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
public class FileResourceConverterTest {
    @InjectMocks
    private FileResourceConverter converter = new FileResourceConverter();
    @Mock
    private RepositoryService repositoryService;
    @Mock
    private PasswordCipherer passwordCipherer;

    private byte[] data = new byte[]{1,2,3,4,5,6,7,8,9,0};
    private String stringData = new String(data);
    private String encoded = DatatypeConverter.printBase64Binary(data);
    private ExecutionContext ctx;


    @BeforeClass
    public void init(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void correctClientServerResourceType(){
        assertEquals(converter.getClientResourceType(), ClientTypeUtility.extractClientType(ClientFile.class));
        assertEquals(converter.getServerResourceType(), FileResource.class.getName());
    }

   /* @Test
    public void resourceSpecificFieldsToServer() throws Exception{
        final String expectedFileType = FileResource.TYPE_JRXML;
        final ClientFile clientObject = new ClientFile();
        final FileResource serverObject = new FileResourceImpl();
        clientObject.setType(ClientFile.FileType.jrxml);
        clientObject.setContent(encoded);
        final FileResource result = converter.resourceSpecificFieldsToServer(clientObject, serverObject, new ArrayList<Exception>(), null);
        assertNotNull(result);
        assertEquals(result.getFileType(), expectedFileType);
        assertEquals(result.getData(), data);
    }*/

   /* @Test
    public void resourceSpecificFieldsToServer_reference_toFullResource() throws Exception{
        final String expectedFileType = FileResource.TYPE_JRXML;
        final ClientFile clientObject = new ClientFile();
        final FileResource serverObject = new FileResourceImpl(), referenced = new FileResourceImpl();

        serverObject.setReferenceURI("aff");
        referenced.setFileType(expectedFileType);
        clientObject.setType(ClientFile.FileType.jrxml);
        clientObject.setContent(encoded);

        when(repositoryService.getResource(nullable(ExecutionContext.class), anyString())).thenReturn(referenced);

        final FileResource result = converter.resourceSpecificFieldsToServer(clientObject, serverObject, new ArrayList<Exception>(), null);

        assertNotNull(result);
        assertNotNull(result.getFileType());
        assertEquals(result.getData(), data);
    }*/

    @Test(expectedExceptions = {IllegalParameterValueException.class})
    public void resourceSpecificFieldsToServer_reference_TypeDifferent() throws Exception{
        final ClientFile clientObject = new ClientFile();
        final FileResource serverObject = new FileResourceImpl(), referenced = new FileResourceImpl();

        serverObject.setReferenceURI("aff");
        referenced.setFileType(FileResource.TYPE_ACCESS_GRANT_SCHEMA);
        clientObject.setType(ClientFile.FileType.jrxml);
        clientObject.setContent(encoded);

        when(repositoryService.getResource(nullable(ExecutionContext.class), anyString())).thenReturn(referenced);

        final FileResource result = converter.resourceSpecificFieldsToServer(  ctx
                , clientObject, serverObject, new ArrayList<Exception>(), null);
    }

    @Test(expectedExceptions = {IllegalParameterValueException.class})
    public void resourceSpecificFieldsToServer_TypeDifferent() throws Exception{
        final ClientFile clientObject = new ClientFile();
        final FileResource serverObject = new FileResourceImpl();

        serverObject.setFileType(FileResource.TYPE_ACCESS_GRANT_SCHEMA);
        clientObject.setType(ClientFile.FileType.jrxml);
        clientObject.setContent(encoded);

        final FileResource result = converter.resourceSpecificFieldsToServer(    ExecutionContextImpl.getRuntimeExecutionContext()
                , clientObject, serverObject, new ArrayList<Exception>(), null);
    }

    @Test
    public void resourceSpecificFieldsToClient(){
        final String expectedFileType = FileResource.TYPE_JRXML;
        final ClientFile clientObject = new ClientFile();
        final FileResource serverObject = new FileResourceImpl();
        serverObject.setFileType(expectedFileType);
        serverObject.setData(data);
        final ClientFile result = converter.resourceSpecificFieldsToClient(clientObject, serverObject, null);
        assertNotNull(result);
        assertEquals(result.getType().name(), expectedFileType);
        assertEquals(result.getContent(), null);
    }

    @Test
    public void resourceSpecificFieldsToClient_reference(){
        final String expectedFileType = FileResource.TYPE_JRXML;
        final String referencedUri = "/test";
        final ClientFile clientObject = new ClientFile();
        final FileResource serverObject = new FileResourceImpl(), referenced = new FileResourceImpl();

        referenced.setFileType(expectedFileType);
        serverObject.setData(data);
        serverObject.setReferenceURI(referencedUri);

        when(repositoryService.getResource(nullable(ExecutionContext.class), eq(referencedUri))).thenReturn(referenced);

        final ClientFile result = converter.resourceSpecificFieldsToClient(clientObject, serverObject, null);

        assertNotNull(result);
        assertEquals(result.getType().name(), expectedFileType);
    }

    @Test
    public void resourceSpecificFieldsToClient_unsupportedFileTypeBecomesUnspecified(){
        final FileResourceImpl serverObject = new FileResourceImpl();
        serverObject.setFileType("someNotSupportedFileType");
        final ClientFile clientFile = converter.resourceSpecificFieldsToClient(new ClientFile(), serverObject, null);
        assertSame(clientFile.getType(), ClientFile.FileType.unspecified);
    }

    @Test
    public void resourceSpecificFieldsToServer_securedFile_encodedData() {
        final String encodedData = "encodedData";
        final ClientFile clientObject = new ClientFile();
        clientObject.setType(ClientFile.FileType.secureFile);
        clientObject.setContent(encoded);

        doReturn(encodedData).when(passwordCipherer).encodePassword(stringData);

        ctx = ExecutionContextImpl.getRuntimeExecutionContext();
        FileResource result = converter.resourceSpecificFieldsToServer(ctx
                ,
                clientObject, new FileResourceImpl(), Collections.emptyList(),
                null);

        assertEquals(encodedData.getBytes(), result.getData());
    }
}
