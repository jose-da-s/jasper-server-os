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
package com.jaspersoft.jasperserver.jaxrs.report;

import com.jaspersoft.jasperserver.api.engine.common.domain.Request;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.remote.common.CallTemplate;
import com.jaspersoft.jasperserver.remote.common.RemoteServiceWrapper;
import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.remote.services.ExportExecutionOptions;
import com.jaspersoft.jasperserver.remote.services.ReportExecutionOptions;
import com.jaspersoft.jasperserver.remote.services.ReportOutputPages;
import com.jaspersoft.jasperserver.remote.services.ReportOutputResource;
import com.jaspersoft.jasperserver.remote.services.RunReportService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

/**
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@Service
@Path("/reports")
@Scope("prototype")
@CallTemplate(ReportsServiceCallTemplate.class)
public class RunReportsJaxrsService extends RemoteServiceWrapper<RunReportService> {

    @Resource
    private ReportExecutionsJaxrsService reportExecutionsJaxrsService;

    @Resource(name = "runReportService")
    public void setRemoteService(RunReportService remoteService) {
        this.remoteService = remoteService;
    }

    @GET
    @Path("/{reportUnitURI: .+}.{outputFormat}")
    public Response getReportOutputResource(
            @PathParam("reportUnitURI") final String reportUnitURI,
            @PathParam("outputFormat") final String outputFormat,
            @HeaderParam("x-jrs-base-url") final String baseUrl,
            @QueryParam("baseUrl") final String baseUrlFromQuery,
            @QueryParam("page") final Integer page,
            @QueryParam("pages") final String pages,
            @QueryParam("transformerKey") final String transformerKey,
            @QueryParam("ignorePagination") final Boolean ignorePagination,
            @QueryParam("reportContainerWidth") final Integer reportContainerWidth,
            @QueryParam("attachmentsPrefix") final String attachmentsPrefix,
            @QueryParam("allowInlineScripts") @DefaultValue("true") final Boolean allowInlineScripts,
            @QueryParam("markupType") final String markupType,
            @QueryParam("anchor") final String anchor,
            @Context final HttpServletRequest request,
            @QueryParam(Request.PARAM_NAME_FRESH_DATA) @DefaultValue("false") final Boolean freshData,
            /* rest_v2 service should be interactive by default, therefore default value for "interactive" is "true" */
            @QueryParam("interactive") @DefaultValue("true") final Boolean interactive,
            @QueryParam("saveDataSnapshot") @DefaultValue("false") final Boolean saveDataSnapshot) {
        return callRemoteService(new ConcreteCaller<Response>() {
            public Response call(RunReportService remoteService) throws ErrorDescriptorException {
                String requestURI = request.getRequestURI();
                try {
                    requestURI = URLDecoder.decode(requestURI, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalStateException("Unknown transfer encoding: UTF-8");
                }

                // parameters map can be safely cast to Map<String, String[]>
                @SuppressWarnings("unchecked")
                Map<String, String[]> parameterMap = request.getParameterMap();
                final ReportExecutionOptions reportExecutionOptions = new ReportExecutionOptions()
                        .setTransformerKey(transformerKey)
                        .setDefaultAttachmentsPrefixTemplate(ReportExecutionHelper
                                .getDefaultAttachmentsPrefixTemplateFromRequest(
                                        requestURI.substring(request.getContextPath().length(),
                                                requestURI.indexOf(reportUnitURI)).replace("/reports/", "/reportExecutions/")))
                        .setFreshData(freshData)
                        .setSaveDataSnapshot(saveDataSnapshot)
                        .setInteractive(interactive)
                        .setReportContainerWidth(reportContainerWidth)
                        .setContextPath(request.getContextPath());
                if(ignorePagination != null){
                    reportExecutionOptions.setIgnorePagination(ignorePagination);
                }
                final ExportExecutionOptions exportOptions = new ExportExecutionOptions().setOutputFormat(outputFormat)
                        .setPages(ReportOutputPages.valueOf(page != null ? page.toString() : pages))
                        .setMarkupType(markupType)
                        .setAnchor(anchor)
                        .setBaseUrl(baseUrlFromQuery != null && !baseUrlFromQuery.isEmpty() ? baseUrlFromQuery : baseUrl)
                        .setAttachmentsPrefix(attachmentsPrefix).setAllowInlineScripts(allowInlineScripts);
                ReportOutputResource reportOutputResource = remoteService.getReportOutputFromRawParameters(Folder.SEPARATOR + reportUnitURI, parameterMap, reportExecutionOptions, exportOptions);
                return ReportExecutionHelper.buildResponseFromOutputResource(reportOutputResource);
            }
        });
    }

    /**
     * @deprecated use ReportExecutionsJaxrsService.getReportsRuntimeInformation()
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getReportsRuntimeInformation(
            @QueryParam("reportURI") final String reportURI,
            @QueryParam("jobID") final String jobID,
            @QueryParam("jobLabel") final String jobLabel,
            @QueryParam("userName") final String userName,
            @QueryParam("fireTimeFrom") final String fireTimeFrom,
            @QueryParam("fireTimeTo") final String fireTimeTo) {
        return reportExecutionsJaxrsService.getReportsRuntimeInformation(reportURI, jobID, jobLabel, userName, fireTimeFrom, fireTimeTo);
    }

    /**
     * @deprecated use ReportExecutionsJaxrsService.cancelReportExecution()
     */
    @PUT
    @Path("/{executionId}/status")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response cancelReportExecution(@PathParam("executionId") final String executionId, ReportExecutionStatusEntity statusEntity) {
        return reportExecutionsJaxrsService.cancelReportExecution(getExecutionId(executionId), statusEntity);
    }

    private String getExecutionId(String executionID) {
        if (executionID != null && executionID.toLowerCase().startsWith("uuid:")) {
            return executionID.substring(5);
        }
        return executionID;
    }

}

