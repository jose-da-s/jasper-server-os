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

import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.api.engine.common.service.ReportExecutionStatusInformation;
import com.jaspersoft.jasperserver.api.engine.common.service.SchedulerReportExecutionStatusSearchCriteria;
import com.jaspersoft.jasperserver.dto.executions.ExecutionStatus;
import com.jaspersoft.jasperserver.dto.executions.ExecutionStatusObject;
import com.jaspersoft.jasperserver.dto.executions.ExportExecutionStatusObject;
import com.jaspersoft.jasperserver.dto.reports.ReportParameter;
import com.jaspersoft.jasperserver.dto.reports.ReportParameters;
import com.jaspersoft.jasperserver.remote.common.CallTemplate;
import com.jaspersoft.jasperserver.remote.common.RemoteServiceWrapper;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.services.*;
import io.opentelemetry.extension.annotations.SpanAttribute;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id: ReportExecutionsJaxrsService.java 26714 2012-12-12 10:18:22Z ykovalchyk $
 */
@Service
@Path("/reportExecutions")
@Scope("prototype")
@CallTemplate(ReportsServiceCallTemplate.class)
@SecurityScheme(
		name = "basicSecurity",
		type = SecuritySchemeType.HTTP,
		scheme = "basic")
@SecurityRequirement(name = "basicSecurity")
@Tag(
	name = "The reportExecutions Service", 
	description = "> Runs a report in asynchronous mode\n\n"
		+ "> As described in [The reports Service](#/The%20reports%20Service) section, synchronous report execution blocks the "
		+ "client waiting for the response. When managing large reports that may take minutes to complete, or when running a large "
		+ "number of reports simultaneously, synchronous report execution slows down the client or uses many threads, each waiting "
		+ "for a report.\n\n"
		+ "> The `rest_v2/reportExecutions` service provides asynchronous report execution, so that the client does not need to wait for "
		+ "report output. Instead, the client obtains a request ID and periodically checks the status of the report to know when it is ready "
		+ "(also called polling). When the report is finished, the client can download the output. The client can also send an asynchronous "
		+ "request for other export formats (PDF, Excel, and others) of the same report. Again the client can check the status of the export "
		+ "and download the result when the export has completed.\n\n"
		+ "> Reports being scheduled on the server also run asynchronously, and reportExecutions allows you to access jobs that are triggered "
		+ "by the scheduler. Finally, the `reportExecutions` service allows the client to stop any report execution or job that has been triggered.\n"
)
public class ReportExecutionsJaxrsService extends RemoteServiceWrapper<RunReportService> {

	@Resource(name = "userAndRoleService")
	UserAndRoleService userAndRoleService;

	@Resource(name = "runReportService")
    public void setRemoteService(RunReportService remoteService) {
        this.remoteService = remoteService;
    }

    @GET
    @Path("/{executionId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Operation(
		operationId = "ReportExecutionsJaxrsService_0040",
		summary = "Request for report execution details.", 
		description = "## Requesting Report Execution Details\n\n"
			+ "> Once the report is ready, your client must determine the names of the files to download "
			+ "by requesting the `ReportExecution` descriptor again.\n\n"
			+ "> The `ReportExecution` descriptor now contains the list of exports for the report, including the "
			+ "report output itself and any other file attachments. File attachments such as images and JavaScript "
			+ "occur only with HTML export.", 
		responses = { 
			@ApiResponse(
				responseCode = "200", 
				description = "Success.\n\nThe result contains the `ReportExecution` descriptor.",
				content = @Content(schema = @Schema(implementation = ReportExecution.class))
			),
			@ApiResponse(
				responseCode = "404", 
				description = "Not found.\n\nThe report execution ID specified in the request does not exist."
			) 
		}
	)        
    public Response getReportExecution(@Parameter(description = "The report execution ID") @PathParam("executionId") @SpanAttribute("executionId") final String executionId) {
        return callRemoteService(new ConcreteCaller<Response>() {
            @Override
            public Response call(RunReportService remoteService) throws ErrorDescriptorException {
                return Response.ok(remoteService.getReportExecution(getExecutionId(executionId))).build();
            }
        });
    }

    @DELETE
    @Path("/{executionId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Operation(
		operationId = "ReportExecutionsJaxrsService_0100",
		summary = "Removes a report execution.", 
		description = "## Removing Report Execution\n\n"
			+ "> Use the `DELETE` method to remove a report execution from cache. If the report execution is still running, "
			+ "it will be automatically stopped and then removed.", 
		responses = { 
			@ApiResponse(
				responseCode = "204", 
				description = " No content.\n\nThere is no content to return:\n\n* When the operation succeeds and the report execution "
					+ "is removed\n\n* When the specified execution ID is not found on the server"
			) 
		}
	)        
    public Response deleteReportExecution(@Parameter(description = "The report execution ID") @PathParam("executionId") @SpanAttribute("executionId") final String executionId) {
        return callRemoteService(new ConcreteCaller<Response>() {
            @Override
            public Response call(RunReportService remoteService) throws ErrorDescriptorException {
                remoteService.deleteReportExecution(getExecutionId(executionId));
                return Response.status(Response.Status.NO_CONTENT).build();
            }
        });
    }

    @GET
    @Path("/{executionId}/exports/{exportId}/outputResource")
	@Operation(
		operationId = "ReportExecutionsJaxrsService_0070",
		summary = "Request for report output.", 
		description = "## Requesting Report Output\n\n"
			+ "> After requesting a report execution and waiting synchronously or asynchronously for it to "
			+ "finish, your client is ready to download the report output.\n\n"
			+ "> Every export format of the report has an ID that is used to retrieve it. For example, the HTML export in the "
			+ "previous example has the ID 195a65cb-1762-450a-be2b-1196a02bb625. To download the main report output, "
			+ "specify the export ID in the method.", 
		responses = { 
			@ApiResponse(
				responseCode = "200", 
				description = "Success.\n\nThe content is the main output of the report, in the format specified by the `contentType` property of the `outputResource` descriptor, for example: `text/html`.", 
				content = @Content(schema = @Schema(example = ResponseExample.REPORT_RUN_EXPORT_OUTPUT)),
				headers = {
					@Header(
						name = "output-final", 
						description = "This value indicates whether the output is in its final form or not. When false, report items such as total page count are not finalized, but output is available early. You should reload the output resource again until this value is true.",
						schema = @Schema(type = "boolean", example = "true")
					)
				}
			),
			@ApiResponse(
				responseCode = "403", 
				description = "Forbidden.\n\nWhen invalid values are provided for export options in the request body. For instance if we provide `outputFormat`=`fhgfdhg`." 
			), 
			@ApiResponse(
				responseCode = "404", 
				description = "Not found.\n\nWhen the report execution ID or the export ID specified in the request does not exist."
			) 
		}
	)        
    public Response getOutputResource(@Parameter(description = "The report execution ID") @PathParam("executionId") @SpanAttribute("executionId") final String executionId,
    		@Parameter(description = "The report export ID") @PathParam("exportId") final String exportId,
    		@Parameter(description = "Flag to suppress the response Content-Disposition header") @QueryParam("suppressContentDisposition") @DefaultValue("false") final Boolean suppressContentDisposition) {
        return callRemoteService(new ConcreteCaller<Response>() {
            @Override
            public Response call(RunReportService remoteService) throws ErrorDescriptorException {
                ReportOutputResource reportOutputResource = remoteService.getOutputResource(getExecutionId(executionId), getExportId(exportId));
                return ReportExecutionHelper.buildResponseFromOutputResource(reportOutputResource, suppressContentDisposition);
            }
        });
    }

	@GET
	@Path("/{executionId}/rawParameterValues")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Operation(
			operationId = "ReportExecutionsJaxrsService_0080",
			summary = "Request for report execution raw parameter values.",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Success.\n\nThe content is the raw report parameters(input control values) used to run the report."
					),
					@ApiResponse(
							responseCode = "404",
							description = "Not found.\n\nWhen the report execution ID or the export ID specified in the request does not exist."
					)
			}
	)
	public Response getRawParameterValues(@Parameter(description = "The report execution ID") @PathParam("executionId") final String executionId) {
		return callRemoteService(new ConcreteCaller<Response>() {
			@Override
			public Response call(RunReportService remoteService) throws ErrorDescriptorException {
				final ReportExecution reportExecution = remoteService.getReportExecution(getExecutionId(executionId));
				return Response.ok(reportExecution.getRawParameters()).build();
			}
		});
	}

    @POST
    @Path("/{executionId}/parameters")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Operation(
		operationId = "ReportExecutionsJaxrsService_0090",
		summary = "Updates the report execution parameters.", 
		description = "## Modifying Report Parameters\n\n"
			+ "> You can update the report parameters, also known as input controls, through a separate method before running a report execution again.", 
		responses = { 
			@ApiResponse(
				responseCode = "204", 
				description = "No Content.\n\nThere is no content to return" 
			),
			@ApiResponse(
				responseCode = "404", 
				description = "Not found.\n\nThe report execution ID specified in the request does not exist."
			) 
		}
	)        
    public Response getReportInputParametersViaPost(
    		@Parameter(description = "The report execution ID") @PathParam("executionId") @SpanAttribute("executionId") final String executionId,
    		@Parameter(description = "Specifies that new parameters must force the server to get fresh data by querying the data source.") @QueryParam("freshData") @DefaultValue("false") Boolean freshData, 
    		@RequestBody(
				description = "List of report parameters with updated values",
				content = @Content(
					array = @ArraySchema(schema = @Schema(description = "Report parameter with updated values.",  implementation = ReportParameter.class))
				)
    		) List<ReportParameter> parameterList) {
        final ReportExecution reportExecution = remoteService.getReportExecution(getExecutionId(executionId));
        reportExecution.setRawParameters(new ReportParameters(parameterList).getRawParameters());
        remoteService.startReportExecution(reportExecution, new ReportExecutionOptions(reportExecution.getOptions()).setFreshData(freshData));
        return Response.noContent().build();
    }

    /**
     * Asynchronous exporting.
     *
     * @param executionId - report unit request ID
     * @return Response with ExportExecution instance as entity
     */
    @POST
    @Path("/{executionId}/exports")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Operation(
		operationId = "ReportExecutionsJaxrsService_0050",
		summary = "Export the report.", 
		description = "## Exporting a Report Asynchronously\n\n"
			+ "> After running a report and downloading its content in a given format, you can request the same report "
			+ "in other formats. As with exporting report formats through the user interface, the report does not run "
			+ "again because the export process is independent of the report.", 
		responses = {
			@ApiResponse(
				responseCode = "200", 
				description = "Success.\n\nThe result contains an `ExportExecution` descriptor.", 
				content = @Content(schema = @Schema(implementation = ExportExecution.class))
			),
			@ApiResponse(
				responseCode = "404", 
				description = "Not found.\n\nThe report execution ID specified in the request does not exist." 
			) 
		}
	)    
    public Response export(
    		@Parameter(description = "The report execution ID") @PathParam("executionId") @SpanAttribute("executionId") final String executionId,
    		@RequestBody(
    				content = @Content(
    					schema = @Schema(
    						description = "Send an export descriptor in JSON format to specify the format and details of your request.",  
    						implementation = ExportExecutionOptions.class
    					)
    				)
    			) final ExportExecutionOptions exportOptions) {
        return callRemoteService(new ConcreteCaller<Response>() {
            @Override
            public Response call(RunReportService remoteService) throws ErrorDescriptorException {
                return Response.ok(remoteService.executeExport(getExecutionId(executionId), exportOptions)).build();
            }
        });
    }

    @GET
    @Path("/{executionId}/exports/{exportId}/attachments/{attachment}")
	@Operation(
		operationId = "ReportExecutionsJaxrsService_0080",
		summary = "Download file attachments for HTML output.", 
		description = "## Downloading File Attachments\n\n"
			+ "> To download file attachments for HTML output, use this method. You must download all attachments to display the HTML "
			+ "content properly. The given URL is the default path, but it can be modified with the `attachmentsPrefix` property in the "
			+ "`reportExecutionRequest`, as described in [Running a Report Asynchronously](#/Running%20a%20Report%20Asynchronously).", 
		responses = {
			@ApiResponse(
				responseCode = "200", 
				description = "Success.\n\nThe content is the attachment in the format specified in the `contentType` property of the "
						+ "attachment descriptor, for example:\n\n"
						+ "`image/png`"
			),
			@ApiResponse(
				responseCode = "404", 
				description = "Not found.\n\nWhen the report execution ID or export ID specified in the request does not exist.Also when the requested attachment is not found." 
			) 
		}
	)    
    public Response getAttachment(
    		@Parameter(description = "The report execution ID") @PathParam("executionId") @SpanAttribute("executionId") final String executionId,
    		@Parameter(description = "The report export ID") @PathParam("exportId") final String exportId, 
    		@Parameter(description = "The name of the file attachment") final @PathParam("attachment") String attachmentName) {
        return callRemoteService(new ConcreteCaller<Response>() {
            @Override
            public Response call(RunReportService remoteService) throws ErrorDescriptorException {
                final ReportOutputResource attachment = remoteService.getAttachment(getExecutionId(executionId),
						getExportId(exportId), attachmentName);
                return ReportExecutionHelper.buildResponseFromOutputResource(attachment);
            }
        });
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Operation(
		operationId = "ReportExecutionsJaxrsService_0110",
		summary = "Find running reports and jobs.", 
		description = "## Finding Running Reports and Jobs\n\n"
			+ "> The `reportExecutions` service provides a method to search for reports that are running on the server, "
			+ "including report jobs triggered by the scheduler."
			+ "To search for running reports, use the search arguments with this URL.\n\n"
			+ "> For security purposes, the search for running reports is has the following restrictions:\n\n"
			+ "> * The system administrator (`superuser`) can see and cancel any report running on the server.\n\n"
				+ "> * An organization admin (jasperadmin) can see every running report by "
				+ "a user of the same organization or one of its child organizations.\n\n"
				+ "> * A regular user can see report that he initiated.",
		responses = {
			@ApiResponse(
				responseCode = "200", 
				description = "Success.\n\nThe response contains a list of summary `reportExecution` descriptors",
				content = @Content(
					array = @ArraySchema(schema = @Schema(description = "A `reportExecution` descriptor.",  implementation = ReportExecutionStatusInformation.class))
				)
			),
			@ApiResponse(
				responseCode = "204", 
				description = "No Content.\n\nWhen the search results are empty." 
			) 
		}
	)    
    public Response getReportsRuntimeInformation(
    		@Parameter(description = "Matches the repository URI of the running report, relative the currently logged-in user’s organization.", example = "/public/Samples/Reports/AllAccounts") @QueryParam("reportURI") final String reportURI,
    		@Parameter(description = "For scheduler jobs, this argument matches the ID of the job that triggered the running report.", example = "1234") @QueryParam("jobID") final String jobID,
    		@Parameter(description = "For scheduler jobs, this argument matches the name of the job that triggered the running report.", example = "AllAccounts_Job") @QueryParam("jobLabel") final String jobLabel,
    		@Parameter(description = "For scheduler jobs, this argument matches the user ID that created the job.", example = "jasperadmin|organization_1") @QueryParam("userName") final String userName,
    		@Parameter(description = "For scheduler jobs, this fire time argument defines the start of a range of time "
    				+ "that matches if the job that is currently running was triggered during this time. You can specify "
    				+ "either or both of the arguments. Specify the date and time in the following pattern: `yyyy-MM-dd'T'HH:mmZ`.", example = "2020-02-10T10:10GMT") @QueryParam("fireTimeFrom") final String fireTimeFrom,
    		@Parameter(description = "For scheduler jobs, this fire time argument defines the end of a range of time "
    				+ "that matches if the job that is currently running was triggered during this time. You can specify "
    				+ "either or both of the arguments. Specify the date and time in the following pattern: `yyyy-MM-dd'T'HH:mmZ`.", example = "2020-02-20T10:10GMT") @QueryParam("fireTimeTo") final String fireTimeTo) {
        return callRemoteService(new ConcreteCaller<Response>() {
            public Response call(RunReportService remoteService) throws ErrorDescriptorException {
                SchedulerReportExecutionStatusSearchCriteria criteria = null;
                if (StringUtils.isNotEmpty(reportURI)
                        || StringUtils.isNotEmpty(jobID)
                        || StringUtils.isNotEmpty(jobLabel)
                        || StringUtils.isNotEmpty(userName)
                        || StringUtils.isNotEmpty(fireTimeFrom)
                        || StringUtils.isNotEmpty(fireTimeTo)) {
                    criteria = new SchedulerReportExecutionStatusSearchCriteria();
                    criteria.setReportURI(StringUtils.isNotEmpty(reportURI) ? reportURI : null);
                    criteria.setJobLabel(StringUtils.isNotEmpty(jobLabel) ? jobLabel : null);
                    criteria.setUserName(StringUtils.isNotEmpty(userName) ? userName : null);
                    criteria.setJobID(StringUtils.isNotEmpty(jobID) ? Long.valueOf(jobID) : null);
                    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
                    try {
                        criteria.setFireTimeFrom(StringUtils.isNotEmpty(fireTimeFrom) ? simpleDateFormat.parse(fireTimeFrom) : null);
                    } catch (ParseException e) {
                        throw new IllegalParameterValueException("fireTimeFrom", fireTimeFrom);
                    }
                    try {
                        criteria.setFireTimeTo(StringUtils.isNotEmpty(fireTimeTo) ? simpleDateFormat.parse(fireTimeTo) : null);
                    } catch (ParseException e) {
                        throw new IllegalParameterValueException("fireTimeTo", fireTimeTo);
                    }
                }
                Set<ReportExecutionStatusInformation> currentlyRunningReports = remoteService.getCurrentlyRunningReports(criteria);
                if (currentlyRunningReports != null && !currentlyRunningReports.isEmpty())
                    return Response.ok(new ReportExecutionsSetWrapper(currentlyRunningReports)).build();
                else
                    return Response.status(Response.Status.NO_CONTENT).build();
            }
        });
    }

    @GET
    @Path("/{executionId}/exports/{exportId}/status")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Operation(
		summary = "Returns the export execution status.", 
		hidden = true
	)    
    public Response getExportExecutionStatus(@PathParam("executionId") @SpanAttribute("executionId") final String executionId,
            @PathParam("exportId") final String exportId){
        return callRemoteService(new ConcreteCaller<Response>() {
            @Override
            public Response call(RunReportService remoteService) throws ErrorDescriptorException {
                final String status = remoteService.getExportExecution(getExecutionId(executionId),
						getExportId(exportId)).getStatus().toString();
                ReportExecutionStatusEntity statusEntity = new ReportExecutionStatusEntity();
                statusEntity.setValue(status);
                return Response.ok(statusEntity).build();
            }
        });
    }

    @GET
    @Path("/{executionId}/exports/{exportId}/status")
    @Produces({"application/status+json", "application/status+xml"})
	@Operation(
		operationId = "ReportExecutionsJaxrsService_0060",
		summary = "Request for the export execution status.", 
		description = "## Polling Export Execution\n\n"
			+ "> As with the execution of the main report, you can also poll the execution of the export process. "
			+ "This service supports the extended status value that includes an appropriate message.\n\n"
			+ "When the status is \"ready\" your client can download the new export output and any attachments "
			+ "as described in [Requesting Report Output](#/Requesting%20Report%20Output).", 
		responses = {
			@ApiResponse(
				responseCode = "200", 
				description = "Success.\n\nThe response contains the export status. In the extended format, "
						+ "error reports contain error messages suitable for display.",
				content = {
				    @Content(mediaType= MediaType.APPLICATION_JSON, schema = @Schema(example = ResponseExample.REPORT_EXECUTION_STATUS_JSON)),
				    @Content(mediaType= MediaType.APPLICATION_XML, schema = @Schema(example = ResponseExample.REPORT_EXECUTION_STATUS_XML))
				}
			),
			@ApiResponse(
				responseCode = "404", 
				description = "Not found.\n\nThe report execution ID or the export ID specified in the request does not exist." 
			) 
		}
	)    
    public Response getExportExecutionStatusObject(
    		@Parameter(description = "The report execution ID") @PathParam("executionId") @SpanAttribute("executionId") final String executionId,
    		@Parameter(description = "The report export ID") @PathParam("exportId") final String exportId){
        return callRemoteService(new ConcreteCaller<Response>() {
            @Override
            public Response call(RunReportService remoteService) throws ErrorDescriptorException {
				final ExportExecution exportExecution = remoteService.getExportExecution(getExecutionId(executionId), getExportId(exportId));
				final ExecutionStatus executionStatus = exportExecution.getStatus();

				ExportExecutionStatusObject statusObject = new ExportExecutionStatusObject();
				statusObject.setValue(executionStatus);
				statusObject.setErrorDescriptor(exportExecution.getErrorDescriptor());

				ReportOutputResource outputResource = exportExecution.getOutputResource();
				if (executionStatus == ExecutionStatus.ready &&
						outputResource != null &&
						"text/html".equals(outputResource.getContentType())) {

					if (outputResource.getDataTimestampMessage() != null) {
						statusObject.setDataTimestampMessage(outputResource.getDataTimestampMessage());
					}
				}

				return Response.ok(statusObject).build();
            }
        });
    }

    @GET
    @Path("/{executionId}/status")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Operation(
		summary = "Returns the report execution status.", 
		hidden = true
	)    
    public Response getReportExecutionStatus(@PathParam("executionId") @SpanAttribute("executionId") final String executionId){
        return callRemoteService(new ConcreteCaller<Response>() {
            @Override
            public Response call(RunReportService remoteService) throws ErrorDescriptorException {
                final String status = remoteService.getReportExecution(getExecutionId(executionId)).getStatus().toString();
                ReportExecutionStatusEntity statusEntity = new ReportExecutionStatusEntity();
                statusEntity.setValue(status);
                return Response.ok(statusEntity).build();
            }
        });
    }

    @GET
    @Path("/{executionId}/status")
    @Produces({"application/status+json", "application/status+xml"})
	@Operation(
		operationId = "ReportExecutionsJaxrsService_0020",
		summary = "Request for the report execution status.", 
		description = "## Polling Report Execution\n\n"
			+ "> When requesting reports asynchronously, use this method to poll the status of the report execution. "
			+ "The report execution ID in the URL is the one returned in the `reportExecution` descriptor.", 
		responses = {
			@ApiResponse(
				responseCode = "200", 
				description = "Success.\n\nThe response contains the report execution status. In the extended format, "
						+ "error reports contain error messages suitable for display.",
				content = {
				    @Content(mediaType= MediaType.APPLICATION_JSON, schema = @Schema(example = ResponseExample.REPORT_EXECUTION_STATUS_JSON)),
				    @Content(mediaType= MediaType.APPLICATION_XML, schema = @Schema(example = ResponseExample.REPORT_EXECUTION_STATUS_XML))
				}
			),
			@ApiResponse(
				responseCode = "404", 
				description = "Not found.\n\nThe report execution ID specified in the request does not exist." 
			) 
		}
	)    
    public Response getReportExecutionStatusObject(@Parameter(description = "The report execution ID") @PathParam("executionId") @SpanAttribute("executionId") final String executionId){
        return callRemoteService(new ConcreteCaller<Response>() {
            @Override
            public Response call(RunReportService remoteService) throws ErrorDescriptorException {
                final ReportExecution reportExecution = remoteService.getReportExecution(getExecutionId(executionId));
                return Response.ok( new ExecutionStatusObject()
                                .setValue(reportExecution.getStatus())
                                .setErrorDescriptor(reportExecution.getErrorDescriptor())
                ).build();
            }
        });
    }

    @GET
    @Path("/{executionId}/info")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Operation(
		operationId = "ReportExecutionsJaxrsService_0014",
		summary = "Request for the report execution additional info.",
		description = "## Requesting Report Execution Additional Info\n\n"
				+ "> Use this method to retrieve additional info from report execution, such as report bookmarks and parts.",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "Success.\n\nThe result contains report additional info such as bookmarks and report parts.",
				content = {
					@Content(mediaType= MediaType.APPLICATION_JSON, schema = @Schema(example = ResponseExample.REPORT_EXECUTION_INFO_JSON)),
					@Content(mediaType= MediaType.APPLICATION_XML, schema = @Schema(example = ResponseExample.REPORT_EXECUTION_INFO_XML))
				}
			),
			@ApiResponse(
				responseCode = "404",
				description = "Not found.\n\nThe report execution ID specified in the request does not exist."
			)
		}
	)
	public Response getReportInfo(@PathParam("executionId") @SpanAttribute("executionId") final String executionId) {
        return callRemoteService(new ConcreteCaller<Response>() {
            @Override
            public Response call(RunReportService remoteService) throws ErrorDescriptorException {
                return remoteService.getReportInfo(getExecutionId(executionId));
            }
        });
    }

    @GET
    @Path("/{executionId}/pages/{pages}/status")
    @Produces(MediaType.APPLICATION_JSON)
	@Operation(
		operationId = "ReportExecutionsJaxrsService_0081",
		summary = "Request for the page status.",
		description = "## Requesting Page Status\n\n"
			+ "> When requesting reports asynchronously, use the following method to poll the page status "
			+ "during the report execution. The `{executionId}` in the URL is the one returned in the `ReportExecution` "
			+ "descriptor. This service returns a response containing `reportStatus`, `pageFinal` and `pageTimestamp` attributes.",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "Success.\n\nThe result contains the page status, in terms of `reportStatus`, `pageFinal` "
						+ "and `pageTimestamp` attributes. Error reports contain error messages suitable for display.",
				content = @Content(schema = @Schema(example = ResponseExample.REPORT_EXECUTION_PAGE_STATUS))
			),
			@ApiResponse(
				responseCode = "404",
				description = "Not found.\n\nThe specified report execution ID does not exist."
			)
		}
	)
	public Response getReportExecutionPageStatus(@PathParam("executionId") @SpanAttribute("executionId") String executionId, @PathParam("pages") String pages) {
        return callRemoteService(new ConcreteCaller<Response>() {
            @Override
            public Response call(RunReportService remoteService) throws ErrorDescriptorException {
                return remoteService.getReportExecutionPageStatus(getExecutionId(executionId), pages);
            }
        });
    }

    @POST
    @Path("/{executionId}/runAction")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
	@Operation(
		operationId = "ReportExecutionsJaxrsService_0160",
		summary = "Runs the report.",
		hidden = true
		//FIXMEDOC response headers?
	)
    public Response runReportAction(@PathParam("executionId") @SpanAttribute("executionId") String executionId, @FormParam("action") String actionData) {
        return callRemoteService(new ConcreteCaller<Response>() {
            @Override
            public Response call(RunReportService remoteService) throws ErrorDescriptorException {
                return remoteService.runReportAction(getExecutionId(executionId), actionData);
            }
        });
    }

    @PUT
    @Path("/{executionId}/status")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Operation(
		operationId = "ReportExecutionsJaxrsService_0030",
		summary = "Stop a running report or job.", 
		description = "## Stopping Running Reports and Jobs\n\n"
			+ "> To stop a report that is running and cancel its output, "
			+ "use the `PUT` method and specify a status of `\"cancelled\"` in the body of the request.", 
		responses = {
			@ApiResponse(	
				responseCode = "200", 
				description = "Success.\n\nWhen the report execution was successfully stopped, the server replies with the same status.", 
				content = {
				    @Content(mediaType= MediaType.APPLICATION_JSON, schema = @Schema(example = ResponseExample.CANCEL_REPORT_EXECUTION_JSON)),
				    @Content(mediaType= MediaType.APPLICATION_XML, schema = @Schema(example = ResponseExample.CANCEL_REPORT_EXECUTION_XML))
				}
			),
			@ApiResponse(
				responseCode = "204", 
				description = "No content.\n\nWhen the report specified by the report execution ID is not running, "
					+ "either because it finished running, failed, or was stopped by another process."
			),
			@ApiResponse(
				responseCode = "404", 
				description = "Not found.\n\nThe report execution ID specified in the request does not exist." 
			) 
		}
	)    
    public Response cancelReportExecution(@Parameter(description = "The report execution ID") @PathParam("executionId") @SpanAttribute("executionId") final String executionId,
    		@RequestBody(
				content = {
				    @Content(mediaType= MediaType.APPLICATION_JSON, schema = @Schema(description = "Send a `status` descriptor in JSON format with the value `cancelled`", example = ResponseExample.CANCEL_REPORT_EXECUTION_JSON)),
				    @Content(mediaType= MediaType.APPLICATION_XML, schema = @Schema(description = "Send a `status` descriptor in XML format with the value `cancelled`", example = ResponseExample.CANCEL_REPORT_EXECUTION_XML))
				}
			) ReportExecutionStatusEntity statusEntity) {
        Response response;
        if (statusEntity != null && ReportExecutionStatusEntity.VALUE_CANCELLED.equals(statusEntity.getValue()))
            response = callRemoteService(new ConcreteCaller<Response>() {
                public Response call(RunReportService remoteService) throws ErrorDescriptorException {
                    final Boolean cancellationResult = remoteService.cancelReportExecution(getExecutionId(executionId));

                    if (statusEntity.getAsyncCancel()) {
                    	return remoteService.getStatusForAsyncCancelledExecution(getExecutionId(executionId));
					}

                    return cancellationResult ? Response.ok(new ReportExecutionStatusEntity()).build()
                            : Response.status(Response.Status.NO_CONTENT).build();
                }
            });
        else
            response = Response.status(Response.Status.BAD_REQUEST).build();
        return response;
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Operation(
		operationId = "ReportExecutionsJaxrsService_0010",
		summary = "Run the report asynchronously.", 
		description = "## Running a Report Asynchronously\n\n"
			+ "> In order to run a report asynchronously, the `reportExecutions` service provides a method to specify all the "
			+ "parameters needed to launch a report. Report parameters are all sent as a `reportExecutionRequest` object. "
			+ "The response from the server contains the request ID needed to track the execution until completion. Further it will be "
			+ "considered as report execution ID.", 
		responses = {
			@ApiResponse(	
				responseCode = "200", 
				description = "Success.\n\nThe content contains a `ReportExecution` descriptor.", 
				content = @Content(schema = @Schema(implementation = ReportExecution.class))
			),
			@ApiResponse(
				responseCode = "403", 
				description = "Forbidden.\n\nWhen the logged-in user does not have permission to access the report in the request."
			),
			@ApiResponse(
				responseCode = "404", 
				description = "Not found.\n\nWhen the report URI specified in the request does not exist." 
			) 
		}
	)    
    public Response getReportOutputMetadata(
    		@RequestBody(
				content = @Content(
					schema = @Schema(
						description = "A complete `ReportExecutionRequest` object in either XML or JSON format.",  
						implementation = ReportExecutionRequest.class
					)
				)
			) final ReportExecutionRequest reportExecutionRequest,
            @Context final HttpServletRequest request) {
        return callRemoteService(new ConcreteCaller<Response>() {
            public Response call(RunReportService remoteService) throws ErrorDescriptorException {
                final ReportExecutionOptions reportExecutionOptions = new ReportExecutionOptions()
                        .setIgnorePagination(reportExecutionRequest.getIgnorePagination())
						.setReportContainerWidth(reportExecutionRequest.getReportContainerWidth())
                        .setTransformerKey(reportExecutionRequest.getTransformerKey())
                        .setFreshData(reportExecutionRequest.getFreshData())
                        .setSaveDataSnapshot(reportExecutionRequest.getSaveDataSnapshot())
                        .setInteractive(reportExecutionRequest.getInteractive())
                        .setAsync(reportExecutionRequest.getAsync())
                        .setDefaultAttachmentsPrefixTemplate(ReportExecutionHelper
                                .getDefaultAttachmentsPrefixTemplateFromRequest(request.getRequestURI().replace(request.getContextPath(), "") + "/"))
                        .setContextPath(request.getContextPath());

                Map<String, Object> sessionParams = null;
                if (request.getSession().getAttribute("DRILL_RESULT_SET") != null ||
						request.getSession().getAttribute("DRILL_CELL") != null) {
					sessionParams = new HashMap<>();

					sessionParams.put("DRILL_RESULT_SET", request.getSession().getAttribute("DRILL_RESULT_SET"));
					sessionParams.put("DRILL_CELL", request.getSession().getAttribute("DRILL_CELL"));
				}

                final ExportExecutionOptions exportOptions = new ExportExecutionOptions().setOutputFormat(reportExecutionRequest.getOutputFormat())
                        .setPages(ReportOutputPages.valueOf(reportExecutionRequest.getPages()))
                        .setMarkupType(reportExecutionRequest.getMarkupType())
                        .setBaseUrl(reportExecutionRequest.getBaseUrl())
                        .setAttachmentsPrefix(reportExecutionRequest.getAttachmentsPrefix())
                        .setAllowInlineScripts(reportExecutionRequest.isAllowInlineScripts())
                        .setAnchor(reportExecutionRequest.getAnchor());
                return Response.ok(remoteService.getReportExecutionFromRawParameters(reportExecutionRequest.getReportUnitUri(),
                        reportExecutionRequest.getParameters() != null ?
                                reportExecutionRequest.getParameters().getRawParameters() :
                                new HashMap<String, String[]>(), reportExecutionOptions, exportOptions, sessionParams)).build();
            }
        });
    }

	private String getExecutionId(String executionID) {
		if (executionID != null && executionID.toLowerCase().startsWith("uuid:")) {
			return executionID.substring(5);
		}
		return executionID;
	}

	private String getExportId(String exportID) {
		return getExecutionId(exportID);
	}

}

