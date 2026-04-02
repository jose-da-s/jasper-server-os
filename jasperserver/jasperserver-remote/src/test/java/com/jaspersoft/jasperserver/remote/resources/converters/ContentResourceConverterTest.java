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

import com.jaspersoft.jasperserver.api.common.crypto.CipherFactory;
import com.jaspersoft.jasperserver.api.common.crypto.KeystoreManagerFactory;
import com.jaspersoft.jasperserver.api.common.crypto.PasswordCipherer;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.common.util.spring.StaticApplicationContext;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ContentResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.ContentResourceImpl;
import com.jaspersoft.jasperserver.api.security.encryption.PlainCipher;
import com.jaspersoft.jasperserver.dto.common.ClientTypeUtility;
import com.jaspersoft.jasperserver.dto.resources.ClientFile;

import static java.lang.System.getenv;
import jakarta.xml.bind.DatatypeConverter;

import java.util.ArrayList;

import com.jaspersoft.jasperserver.test.ks.JrsAnnotationConfigContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import static org.springframework.util.ResourceUtils.getFile;


import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@ContextConfiguration(loader= JrsAnnotationConfigContextLoader.class)
public class ContentResourceConverterTest extends AbstractTestNGSpringContextTests {
    
    final static String ksLocation = getenv("ks");
    final static String kspLocation = getenv("ksp");
    private ExecutionContext ctx  = ExecutionContextImpl.getRuntimeExecutionContext();

    @Configuration
    @ComponentScan("com.jaspersoft.jasperserver.api.common.crypto")
    static class ContextConfiguration {

        public static final String PASSWORD_ENC_SECRET = "passwordEncSecret";
        public static final String DEPRICATED_PASSWORD_ENC_SECRET = "deprecatedPasswordEncSecret";

        @Bean(name = "passwordEncoder")
        public CipherFactory passwordEncoderFactory() throws Exception {
            CipherFactory factory = new CipherFactory();
            factory.setCipherClass(PasswordCipherer.class);
            factory.setConfId(PASSWORD_ENC_SECRET);
            factory.setFallbackFactory(getPasswordEncoderFallbackFactory());
            return factory;
        }

        @Bean(name = "passwordEncoder_7_2")
        public CipherFactory getPasswordEncoderFallbackFactory() throws  Exception {
            CipherFactory factory = new CipherFactory();
            factory.setCipherClass(PasswordCipherer.class);
            factory.setConfId(DEPRICATED_PASSWORD_ENC_SECRET);
            return factory;
        }

        @Bean(name = "passwordEncoder")
        public PlainCipher passwordEncoder() throws Exception {
            return passwordEncoderFactory().getObject();
        }

        @Bean(name = "keystoreManager")
        public KeystoreManagerFactory keystoreManagerFactory() throws Exception {
            return new KeystoreManagerFactory();
        }
    }


    @Qualifier("passwordEncoder")    
    @Autowired
    PlainCipher cipher;
    
    @BeforeClass
    public static void setUp() throws Exception {
//        final File file = getFile(ContentResourceConverterTest.class.getResource("/enc.properties"));
//        KeystoreManager.init(ksLocation, kspLocation, file);
    }
    
    private ContentResourceConverter converter = new ContentResourceConverter();
    private byte[] data = new byte[]{1,2,3,4,5,6,7,8,9,0};
    private String encoded = DatatypeConverter.printBase64Binary(data);
    
    @Test
    public void correctClientServerResourceType(){
        assertEquals(converter.getClientResourceType(), ClientTypeUtility.extractClientType(ClientFile.class));
        assertEquals(converter.getServerResourceType(), ContentResource.class.getName());
    }

    @Test
    public void resourceSpecificFieldsToServer() throws Exception{
        final String expectedFileType = ContentResource.TYPE_ODS;
        final ClientFile clientObject = new ClientFile();
        final ContentResource serverObject = new ContentResourceImpl();
        clientObject.setType(ClientFile.FileType.ods);
        clientObject.setContent(encoded);
        final ContentResource result = converter.resourceSpecificFieldsToServer(ctx, clientObject, serverObject, new ArrayList<Exception>(), null);
        assertNotNull(result);
        assertEquals(result.getFileType(), expectedFileType);
        assertEquals(result.getData(), data);
    }
    
    @Test
    public void resourceSpecificFieldsToServerSecureFile() throws Exception{
        // Let's populate the JRS Application Context.
        StaticApplicationContext.setApplicationContext( this.applicationContext );

        final String expectedFileType = ClientFile.FileType.secureFile.name();
        final ClientFile clientObject = new ClientFile();
        final ContentResource serverObject = new ContentResourceImpl();
        serverObject.setFileType(ClientFile.FileType.secureFile.name());
        clientObject.setType(ClientFile.FileType.secureFile);
        clientObject.setContent(encoded);
        final ContentResource result = converter.resourceSpecificFieldsToServer(ctx, clientObject, serverObject, new ArrayList<Exception>(), null);
        assertNotNull(result);
        assertEquals(result.getFileType(), expectedFileType);
        // We expect the data to be equal to the decripted result data
        byte[] decriptedData = PasswordCipherer.getInstance().decodePassword(new String(result.getData())).getBytes();
        assertEquals(decriptedData, data);
    }


    @Test
    public void resourceSpecificFieldsToClient(){
        final String expectedFileType = ContentResource.TYPE_ODS;
        final ClientFile clientObject = new ClientFile();
        final ContentResource serverObject = new ContentResourceImpl();
        serverObject.setFileType(expectedFileType);
        serverObject.setData(data);
        final ClientFile result = converter.resourceSpecificFieldsToClient(clientObject, serverObject, null);
        assertNotNull(result);
        assertEquals(result.getType().name(), expectedFileType);
        assertEquals(result.getContent(), null);
    }
}
