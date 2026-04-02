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

import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.dto.importexport.ImportTask;
import com.jaspersoft.jasperserver.dto.importexport.State;
import com.jaspersoft.jasperserver.export.service.ImportExportService;
import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.remote.services.async.ImportExportTask;
import com.jaspersoft.jasperserver.remote.services.async.ImportRunnable;
import com.jaspersoft.jasperserver.remote.services.async.Task;
import com.jaspersoft.jasperserver.remote.services.async.TasksManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl.getRuntimeExecutionContext;
import static com.jaspersoft.jasperserver.export.service.ImportExportService.SECRET_KEY;
import static com.jaspersoft.jasperserver.export.util.EncryptionParams.KEY_ALIAS_PARAMETER;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.status;
import static org.apache.commons.lang.StringUtils.join;

/**
 * @author Zakhar.Tomchenco
 */

@Component
@Path("/import")
@Scope("prototype")
public class ImportJaxrsService extends CommonImportExportService {
    private static final Log log = LogFactory.getLog(ImportJaxrsService.class);

    @Resource
    private ImportExportService synchImportExportService;

    @Resource
    private TasksManager basicTaskManager;

    @Resource(name="messageSource")
    private MessageSource messageSource;

    @Resource(name = "concreteRepository")
    protected RepositoryService repository;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response handleMultipartUpload(
            @FormDataParam("file") InputStream stream,
            @DefaultValue("true")@FormDataParam("update")  Boolean update,
            @DefaultValue("true")@FormDataParam("skip-user-update") Boolean skipUserUpdate,
            @DefaultValue("true")@FormDataParam("include-audit-events") Boolean includeAuditEvents,
            @DefaultValue("true")@FormDataParam("include-access-events") Boolean includeAccessEvents,
            @DefaultValue("true")@FormDataParam("include-monitoring-events") Boolean includeMonitoringEvents,
            @DefaultValue("true")@FormDataParam("include-server-settings") Boolean includeSettings,
            @DefaultValue("true")@FormDataParam("skip-themes") Boolean skipThemes,
            @FormDataParam("organization") String organizationId,
            @DefaultValue("fail")@FormDataParam("broken-dependencies") String strategy,
            @DefaultValue("true")@FormDataParam("merge-organization") Boolean mergeOrganization,
            @DefaultValue("")@FormDataParam("secret-key") String secretKey,
            @DefaultValue("")@FormDataParam("secret-uri") String secretUri,
            @DefaultValue("")@FormDataParam("key-alias") String keyAlias
    ) throws Exception {

        // In case of form submission from browser we for NULL if param is absent and FALSE if it set
        // (because browsers put value 'on' or empty string and jersey parses it to FALSE)
        // That is why @DefaultValue("true") is set and also we have to negate values
        return handleUpload(stream, !update, !skipUserUpdate, !includeAuditEvents, !includeAccessEvents,
                !includeMonitoringEvents, !includeSettings, !skipThemes, organizationId, !mergeOrganization, strategy,
                secretKey, secretUri, keyAlias);

    }

    @POST
    @Consumes("application/zip")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response handleDirectUpload(InputStream stream,
            @DefaultValue("false")@QueryParam("update") Boolean update,
            @DefaultValue("false")@QueryParam("skipUserUpdate") Boolean skipUserUpdate,
            @DefaultValue("false")@QueryParam("includeAuditEvents") Boolean includeAuditEvents,
            @DefaultValue("false")@QueryParam("includeAccessEvents") Boolean includeAccessEvents,
            @DefaultValue("false")@QueryParam("includeMonitoringEvents") Boolean includeMonitoringEvents,
            @DefaultValue("false")@QueryParam("includeServerSettings") Boolean includeSettings,
            @DefaultValue("false")@QueryParam("skipThemes") Boolean skipThemes,
            @QueryParam("organization") String organizationId,
            @DefaultValue("fail")@QueryParam("brokenDependencies") String strategy,
            @DefaultValue("false")@QueryParam("mergeOrganization") Boolean mergeOrganization,
            @QueryParam("keyAlias") String keyAlias) throws Exception {

        return handleUpload(stream, update, skipUserUpdate, includeAuditEvents, includeAccessEvents,
                includeMonitoringEvents, includeSettings, skipThemes, organizationId, mergeOrganization, strategy,
                null, null, keyAlias);
    }

    protected Response handleUpload(InputStream stream, Boolean update, Boolean skipUserUpdate,
                                    Boolean includeAuditEvents,Boolean includeAccessEvents,
                                    Boolean includeMonitoringEvents,Boolean includeSettings,
                                    Boolean skipThemes,
                                    String organizationId,
                                    Boolean mergeOrganization,
                                    String brokenDependenciesStrategy,
                                    String secretKey, String secretUri, String keyAlias) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("update", String.valueOf(update));
        params.put("skip-user-update", String.valueOf(skipUserUpdate));
        params.put("include-audit-events", String.valueOf(includeAuditEvents));
        params.put("include-access-events", String.valueOf(includeAccessEvents));
        params.put("include-monitoring-events", String.valueOf(includeMonitoringEvents));
        params.put("include-server-settings", String.valueOf(includeSettings));
        params.put("skip-themes", String.valueOf(skipThemes));
        params.put("merge-organization", String.valueOf(mergeOrganization));

        if (keyAlias != null) params.put(KEY_ALIAS_PARAMETER, keyAlias);

        try {
            processSecretUri(secretUri).ifPresent(key -> params.put(SECRET_KEY, key));
        } catch (InvalidEncryptionKey e) {
            return status(BAD_REQUEST).entity(e.error)
                    .build();
        }
        try {
            processSecretKey(secretKey)
                    .ifPresent(key -> params.put(SECRET_KEY, key));
        } catch (InvalidEncryptionKey e) {
            return status(BAD_REQUEST).entity(e.error)
                    .build();
        }


        ImportRunnable importRunner = new ImportRunnable(params, stream, LocaleContextHolder.getLocale());
        importRunner.setService(synchImportExportService);
        importRunner.setMessageSource(messageSource);
        importRunner.setOrganizationId(organizationId);
        importRunner.setBrokenDependenciesStrategy(brokenDependenciesStrategy);

        basicTaskManager.startTask(new ImportExportTask<>(importRunner));

        return Response.ok(importRunner.getState()).build();
    }

    @GET
    @Path("/{id}/state")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getStateOfTheTask(@PathParam("id") final String taskId) throws ErrorDescriptorException {
        State taskState = basicTaskManager.getTaskState(taskId);
        if (!taskState.getPhase().equals(Task.INPROGRESS) && !taskState.getPhase().equals(Task.PENDING)){
            basicTaskManager.finishTask(taskId);
        }
        return Response.ok(taskState).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTasksIds() throws ErrorDescriptorException {
        return Response.ok(basicTaskManager.getTaskIds()).build();
    }

    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getParamsOfTheTask(@PathParam("id") final String taskId) throws ErrorDescriptorException {
        Task task = basicTaskManager.getTask(taskId);

        ImportTask taskDto = new ImportTask();
        taskDto.setOrganization(task.getOrganizationId());
        taskDto.setBrokenDependencies(task.getBrokenDependenciesStrategy());

        Map parameters = task.getParameters();
        if (parameters == null) {
            taskDto.setParameters(null);
        } else {
            List<String> parametersList = new ArrayList<String>();
            for (Object key : parameters.keySet()) {
                Object value = parameters.get(key);
                if (key instanceof String) {
                    if (value instanceof Boolean) {
                        if ((Boolean) value) {
                            parametersList.add((String) key);
                        }
                    } else if (value instanceof String) {
                        if (Boolean.parseBoolean((String) value)) {
                            parametersList.add((String) key);
                        }
                    }
                }
            }
            taskDto.setParameters(parametersList.isEmpty() ? null : parametersList);
        }

        return Response.ok(taskDto).build();
    }

    @PUT
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response putTask(ImportTask taskDto, @PathParam("id") String taskId)  throws ErrorDescriptorException {
        Task task = basicTaskManager.getTask(taskId);

        final Map<String, String> params;
        if (taskDto.getParameters() != null) {
            params = taskDto.getParameters()
                    .stream().collect(
                            Collectors.toMap(Function.identity(), p -> Boolean.toString(StringUtils.isNotBlank(p))));
        } else {
            params = new HashMap<>();
        }

        if (taskDto.getKeyAlias() != null) params.put(KEY_ALIAS_PARAMETER, taskDto.getKeyAlias());
        try {
            processSecretUri(taskDto.getSecretUri()).ifPresent(key -> params.put(SECRET_KEY, key));
        } catch (InvalidEncryptionKey e) {
            return status(BAD_REQUEST)
                    .entity(e.error)
                    .build();
        }
        try {
            processSecretKey(taskDto.getSecretKey()).ifPresent(key -> params.put(SECRET_KEY, key));
        } catch (InvalidEncryptionKey e) {
            return status(BAD_REQUEST)
                    .entity(e.error)
                    .build();
        }

        task.updateTask(params, taskDto.getOrganization(), taskDto.getBrokenDependencies());
        basicTaskManager.restartTask(task);

        return Response.ok(task.getState()).build();
    }

    @DELETE
    @Path("/{id}")
    public Response cancel(@PathParam("id")String id){
        basicTaskManager.finishTask(id);

        return Response.noContent().build();
    }
}

