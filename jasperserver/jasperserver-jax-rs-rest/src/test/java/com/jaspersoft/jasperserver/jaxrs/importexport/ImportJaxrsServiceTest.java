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

import com.amazonaws.util.StringInputStream;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResourceData;
import com.jaspersoft.jasperserver.api.metadata.common.service.JSResourceNotFoundException;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.security.encryption.PlainCipher;
import com.jaspersoft.jasperserver.dto.importexport.ExportTask;
import com.jaspersoft.jasperserver.dto.importexport.ImportTask;
import com.jaspersoft.jasperserver.dto.importexport.State;
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
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class ImportJaxrsServiceTest {

    @InjectMocks
    private ImportJaxrsService service = new ImportJaxrsService();

    @Spy
    private TasksManager basicTaskManager;

    @Spy
    private ImportRunnable runner;

    @Mock
    private PlainCipher importExportCipher;

    @Mock
    protected PlainCipher passwordEncoder;

    @Mock
    private RepositoryService repository;

    public static final String TEST_KEY = "0x81 0x8d 0x3e 0x9b 0x1b 0xe0 0x9b 0x57 0x5a 0x93 0x4f 0xe6 0x43 0x90 0xb5 0x5e";
    public static final String INVALID_KEY = "0x81 0x8d 0x3e 0x9b 0x1b 0xe0 0x9b 0x57 0x5a 0x93 0x4f 0xe6 0x43 0x90 0xb5";

    @BeforeClass
    public void init() throws Exception {
        runner = new ImportRunnable(new HashMap<>(), new ByteArrayInputStream("".getBytes()));
        MockitoAnnotations.initMocks(this);
    }

    @BeforeMethod
    public void setUp() {
        when(importExportCipher.getCipherTransformation()).thenReturn("AES");
        when(runner.getState()).thenReturn(new State().setPhase(Task.PENDING));
        when(basicTaskManager.getTask("1")).thenReturn(new ImportExportTask(runner));

    }

    @AfterMethod
    public void resetMock() {
        reset(basicTaskManager);
        reset(runner);
        reset(passwordEncoder);
    }

    @Test
    public void shouldPutNewTask() {
//        when(runner.getState()).thenReturn(new State().setPhase(Task.PENDING));
//        when(basicTaskManager.getTask("1")).thenReturn(new ImportExportTask(runner));

        service.putTask(new ImportTask()
                .setKeyAlias("deprecatedHttpParameterEncSecret")
                .setParameters(Collections.singletonList("repository-permissions")), "1"
        );

        verify(basicTaskManager, times(1))
                .restartTask(argThat(argument -> argument.getParameters().get("keyalias").equals("deprecatedHttpParameterEncSecret")));

    }


    @Test
    public void shouldFailImportWithWrongSecretKey() {
        final ImportTask task = new ImportTask()
                .setSecretKey("deprecatedHttpParameterEncSecret")
                .setParameters(Collections.singletonList("repository-permissions"));
        final Response response = service.putTask(task, "1");

        assertEquals(response.getStatus(), 400);
    }

    @Test
    public void shouldFailImportWithInvalidSecretKey() {
        final ImportTask task = new ImportTask()
                .setSecretKey(INVALID_KEY)
                .setParameters(Collections.singletonList("repository-permissions"));
        final Response response = service.putTask(task, "1");

        assertEquals(response.getStatus(), 400);
    }

    @Test
    public void shouldImportWithSecretKey() {
        final ImportTask task = new ImportTask()
                .setSecretKey(TEST_KEY)
                .setParameters(Collections.singletonList("repository-permissions"));
        final Response response = service.putTask(task, "1");

        assertEquals(response.getStatus(), 200);

        verify(basicTaskManager, times(1))
                .restartTask(argThat(argument -> argument.getParameters().get("secret-key").equals(TEST_KEY)));
    }

    @Test
    public void shouldFailToExportWithMissingSecretUri() {

        final FileResourceData data = new FileResourceData(TEST_KEY.getBytes());
        final String testUri = "/public/key";
        when(repository.getResourceData(any(ExecutionContext.class), eq(testUri))).thenThrow(JSResourceNotFoundException.class);
        when(passwordEncoder.decode(eq(TEST_KEY))).thenReturn(TEST_KEY);

        final ImportTask task = new ImportTask()
                .setSecretUri(testUri)
                .setParameters(Collections.singletonList("repository-permissions"));
        final Response response = service.putTask(task, "1");

        assertEquals(response.getStatus(), 400);
    }


    @Test
    public void shouldFailToExportWithSecretUriIfDataInvalid() {

        final FileResourceData invalidData = new FileResourceData(TEST_KEY.getBytes());
        final String testUri = "/public/key";
        when(repository.getResourceData(any(ExecutionContext.class), eq(testUri))).thenReturn(invalidData);
        when(passwordEncoder.decode(eq(TEST_KEY))).thenReturn("invalid");

        final ImportTask task = new ImportTask()
                .setSecretUri(testUri)
                .setParameters(Collections.singletonList("repository-permissions"));
        final Response response = service.putTask(task, "1");

        assertEquals(response.getStatus(), 400);

    }

    @Test
    public void shouldFailToExportWithSecretUriIfDataWrong() {

        final FileResourceData data = new FileResourceData(TEST_KEY.getBytes());
        final String testUri = "/public/key";
        when(repository.getResourceData(any(ExecutionContext.class), eq(testUri))).thenReturn(data);
        when(passwordEncoder.decode(eq(TEST_KEY))).thenReturn(INVALID_KEY);

        final ImportTask task = new ImportTask()
                .setSecretUri(testUri)
                .setParameters(Collections.singletonList("repository-permissions"));
        final Response response = service.putTask(task, "1");

        assertEquals(response.getStatus(), 400);
    }

    @Test
    public void shouldExportWithSecretUri() {

        final FileResourceData data = new FileResourceData(TEST_KEY.getBytes());
        final String testUri = "/public/key";
        when(repository.getResourceData(any(ExecutionContext.class), eq(testUri))).thenReturn(data);
        when(passwordEncoder.decode(eq(TEST_KEY))).thenReturn(TEST_KEY);

        final ImportTask task = new ImportTask()
                .setSecretUri(testUri)
                .setParameters(Collections.singletonList("repository-permissions"));
        final Response response = service.putTask(task, "1");

        assertEquals(response.getStatus(), 200);

        verify(basicTaskManager, times(1))
                .restartTask(argThat(argument -> argument.getParameters().get("secret-key").equals(TEST_KEY)));

    }

    @Test
    public void shouldUploadMultipart() throws Exception {
        service.handleMultipartUpload(new NullInputStream(0), true, true, true, true, true, true, true, null, "fail", true,
                "", "", "deprecatedHttpParameterEncSecret");

        verify(basicTaskManager, times(1))
                .startTask(argThat(argument -> argument.getParameters().get("keyalias").equals("deprecatedHttpParameterEncSecret")));

    }
}