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

import com.jaspersoft.jasperserver.dto.importexport.ExportTask;
import com.jaspersoft.jasperserver.export.BaseExporterImporter;
import com.jaspersoft.jasperserver.export.service.ImportExportService;
import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.export.util.EncryptionParams;
import com.jaspersoft.jasperserver.remote.services.async.ExportRunnable;
import com.jaspersoft.jasperserver.remote.services.async.ImportExportTask;
import com.jaspersoft.jasperserver.remote.services.async.TasksManager;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.jaspersoft.jasperserver.export.service.ImportExportService.SECRET_KEY;
import static com.jaspersoft.jasperserver.export.util.EncryptionParams.KEY_ALIAS_PARAMETER;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

/**
 * @author: Zakhar.Tomchenco
 */
@Component
@Path("/export")
@Scope("prototype")
public class ExportJaxrsService extends CommonImportExportService {

    @Resource
    private ImportExportService synchImportExportService;

    @Resource
    private TasksManager basicTaskManager;

    @Resource(name="messageSource")
    private MessageSource messageSource;

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createNewTask(final ExportTask data) throws ErrorDescriptorException {

        Map<String, String> params = new HashMap<>();
        if (data.getParameters() != null){
            for (String par : data.getParameters()) {
                params.put(par, Boolean.TRUE.toString());
            }
        }

        if (data.getKeyAlias() != null) params.put(KEY_ALIAS_PARAMETER, data.getKeyAlias());

        ExportRunnable exportRunnable = new ExportRunnable(params, data.getUris(), data.getScheduledJobs(),
                data.getRoles(), data.getUsers(), data.getResourceTypes(), LocaleContextHolder.getLocale());
        exportRunnable.setService(synchImportExportService);
        exportRunnable.setMessageSource(messageSource);
        exportRunnable.setOrganizationId(data.getOrganization());
        basicTaskManager.startTask(new ImportExportTask<>(exportRunnable));

        return Response.ok(exportRunnable.getState()).build();
    }

    @GET
    @Path("/{id}/state")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getStateOfTheTask(@PathParam("id") final String taskId) throws ErrorDescriptorException {
        return Response.ok(basicTaskManager.getTaskState(taskId)).build();
    }

    @GET
    @Path("/{id}/{name}")
    @Produces("application/zip")
    public Response downloadFile(@PathParam("id") final String taskId, @PathParam("name") String name) throws ErrorDescriptorException {
        Response response = Response.ok(basicTaskManager.getTask(taskId).getResult()).build();
        basicTaskManager.finishTask(taskId);
        return response;
    }

    @DELETE
    @Path("/{id}")
    public Response cancel(@PathParam("id")String id){
        basicTaskManager.finishTask(id);
        return Response.noContent().build();
    }


}

