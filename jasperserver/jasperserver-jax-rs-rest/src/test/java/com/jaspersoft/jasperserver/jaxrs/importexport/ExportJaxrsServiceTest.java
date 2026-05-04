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

package com.jaspersoft.jasperserver.jaxrs.importexport;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResourceData;
import com.jaspersoft.jasperserver.api.metadata.common.service.JSResourceNotFoundException;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.security.encryption.PlainCipher;
import com.jaspersoft.jasperserver.dto.importexport.ExportTask;
import com.jaspersoft.jasperserver.remote.services.async.TasksManager;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class ExportJaxrsServiceTest {
    @InjectMocks
    private ExportJaxrsService service = new ExportJaxrsService();

    //    @InjectMocks
    @Spy
    private TasksManager basicTaskManager;

    @Mock
    private ExecutorService executor;

    @Mock
    private PlainCipher importExportCipher;

    @Mock
    protected PlainCipher passwordEncoder;

    @Mock
    private RepositoryService repository;

    @BeforeClass
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeMethod
    public void setUp() {
        when(importExportCipher.getCipherTransformation()).thenReturn("AES");
    }

    @AfterMethod
    public void resetMock() {
        reset(basicTaskManager);
        reset(passwordEncoder);
    }

    @Test
    public void shouldCreateNewTask() {

        service.createNewTask(new ExportTask()
                .setUris(Collections.singletonList("/public/diagnostic/JSDiagnosticReport"))
                .setKeyAlias("deprecatedHttpParameterEncSecret")
                .setParameters(Collections.singletonList("repository-permissions"))
        );

        verify(basicTaskManager, times(1))
                .startTask(argThat(argument -> argument.getParameters().get("keyalias").equals("deprecatedHttpParameterEncSecret")));

        reset(basicTaskManager);
    }
}