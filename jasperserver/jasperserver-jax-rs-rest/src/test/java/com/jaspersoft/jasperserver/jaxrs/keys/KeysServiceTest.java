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

package com.jaspersoft.jasperserver.jaxrs.keys;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResourceData;
import com.jaspersoft.jasperserver.api.metadata.common.service.JSResourceNotFoundException;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.security.encryption.PlainCipher;
import com.jaspersoft.jasperserver.crypto.JrsKeystore;
import com.jaspersoft.jasperserver.crypto.KeyProperties;
import com.jaspersoft.jasperserver.crypto.KeystoreManager;
import com.jaspersoft.jasperserver.dto.importexport.ImportTask;
import com.jaspersoft.jasperserver.dto.importexport.State;
import com.jaspersoft.jasperserver.jaxrs.importexport.ImportJaxrsService;
import com.jaspersoft.jasperserver.remote.services.async.ImportExportTask;
import com.jaspersoft.jasperserver.remote.services.async.ImportRunnable;
import com.jaspersoft.jasperserver.remote.services.async.Task;
import com.jaspersoft.jasperserver.remote.services.async.TasksManager;
import org.apache.commons.io.input.NullInputStream;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jakarta.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class KeysServiceTest {

    @InjectMocks
    private KeysJaxrsService service = new KeysJaxrsService();

    @Mock
    private KeystoreManager keystoreManager;

    @Mock
    private JrsKeystore keystore;

    private List<KeyProperties> values = asList(
            KeyProperties.builder()
                    .keyAlias("testkey")
                    .keyAlg("AES")
                    .keyLabel("Test Key")
                    .keyVisible(true).build(),

            KeyProperties.builder()
                    .keyAlias("testkey2")
                    .keyAlg("AES")
                    .keySize(128)
                    .keyLabel("Test Key II")
                    .keyVisible(false).build(),

            KeyProperties.builder()
                    .keyAlias("testkey3")
                    .keyAlg("RSA")
                    .keySize(1024)
                    .keyLabel("Test Key III")
                    .keyVisible(true).build()

    );

    @BeforeClass
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeMethod
    public void setUp() {
        when(keystoreManager.getKeystore(any())).thenReturn(keystore);
        when(keystore.getKeyProperties()).thenReturn(values);
    }

    @AfterMethod
    public void resetMock() {
        reset(keystoreManager);
        reset(keystore);
    }

    @Test
    public void shouldGetKeyAliases() {
        final List<KeysJaxrsService.KeyProps> aliases = service.getKeyAliases();

        assertEquals(1, aliases.size());
        assertEquals("Test Key", aliases.get(0).label);
    }

}