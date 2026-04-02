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
package com.jaspersoft.jasperserver.remote.services;

import com.jaspersoft.jasperserver.api.JSValidationException;
import com.jaspersoft.jasperserver.api.engine.common.service.ReportExecutionStatusInformation;
import com.jaspersoft.jasperserver.api.engine.common.service.SchedulerReportExecutionStatusSearchCriteria;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.PaginationParameters;
import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.remote.exception.ResourceNotFoundException;

import jakarta.ws.rs.core.Response;
import java.util.Map;
import java.util.Set;

/**
 * Facade service to run reports
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
public interface RunReportService {

    public static final String CONTEXT_PATH_ATTACHMENTS_PREFIX_TEMPLATE_PLACEHOLDER = "{contextPath}";
    public static final String REPORT_EXECUTION_ID_ATTACHMENTS_PREFIX_TEMPLATE_PLACEHOLDER = "{reportExecutionId}";
    public static final String EXPORT_EXECUTION_ID_ATTACHMENTS_PREFIX_TEMPLATE_PLACEHOLDER = "{exportExecutionId}";


    /**
     *
     * @param reportUnitURI - URI of the report to run
     * @param rawParameters - map with parameters in raw state(raw mean, that all parameters are strings or arrays of strings)
     * @param options - report execution options
     * @return report output resource
     * @throws ErrorDescriptorException is thrown in case if error occurs during report execution or export
     */
    ReportOutputResource getReportOutputFromRawParameters(String reportUnitURI, Map<String, String[]> rawParameters, ReportExecutionOptions options, ExportExecutionOptions exportExecutionOptions) throws ErrorDescriptorException;

    /**
     * Run report and generate report execution metadata.
     *
     * @param reportUnitURI - URI of the report unit
     * @param rawParameters - input parameters in a raw representation (Map<String, String[]>)
     * @param executionOptions - options for a report execution procedure
     * @param exportOptions - options for a report export
     * @return report execution metadata
     * @throws ErrorDescriptorException in case if report execution metadata generation fails.
     *                          See RemoteException.getErrorDesctiptor() to get detailed error descriptions.
     */
    ReportExecution getReportExecutionFromRawParameters(String reportUnitURI, Map<String, String[]> rawParameters,
            ReportExecutionOptions executionOptions, ExportExecutionOptions exportOptions, Map<String, Object> sessionParams) throws ErrorDescriptorException, JSValidationException;

    /**
     * Starting of report execution according to given ReportExecution object.
     *
     * @param reportExecution - report execution to start
     * @param options - - report execution options,
     *                     allows to override execution options stored in report execution object (e.g. freshData flag).
     */
    void startReportExecution(ReportExecution reportExecution, ReportExecutionOptions options);

    /**
     * Starting of report execution according to given ReportExecution object.
     *
     * @param reportExecution - report execution to start
     */
    void startReportExecution(ReportExecution reportExecution);

    /**
     * Get current state of report execution by report execution ID
     *
     * @param executionId - report execution ID
     * @return report execution metadata
     * @throws ResourceNotFoundException if required resource isn't found
     */
    ReportExecution getReportExecution(String executionId) throws ResourceNotFoundException;
    
    ReportExecution getRelatedExecution(ReportExecution reportExecution, PaginationParameters paginationParameters) throws ResourceNotFoundException;

    /**
     * Get report output resource. Synchronous export.
     *
     * @param executionId - report execution ID
     * @param exportId - export execution ID
     * @return export output resource.
     * @throws ErrorDescriptorException in case if report export fails. See RemoteException.getErrorDesctiptor() to get detailed error descriptions.
     */
    ReportOutputResource getOutputResource(String executionId, String exportId) throws ErrorDescriptorException;

    /**
     * Get report output resource. Synchronous export.
     *
     * @param executionId - report execution ID
     * @param exportOptions - options for export
     * @return export output resource.
     * @throws ErrorDescriptorException in case if report export fails. See RemoteException.getErrorDesctiptor() to get detailed error descriptions.
     */
    ReportOutputResource getOutputResource(String executionId, ExportExecutionOptions exportOptions) throws ErrorDescriptorException;

    /**
     * Get report output attachment.
     *
     * @param executionId - report execution ID
     * @param exportId - export execution ID
     * @param attachmentName - name of required attachment
     * @return report attachment (e.g. image)
     * @throws ResourceNotFoundException in case if required resource is not found
     */
    ReportOutputResource getAttachment(String executionId, String exportId, String attachmentName) throws ResourceNotFoundException;

    /**
     * Get report execution additional info, such as report bookmarks and parts, that is usually ready when the report execution runs to completion
     */
    Response getReportInfo(String executionId);

    /**
     * Get the page status
     */
   Response getReportExecutionPageStatus(String executionId, String pages);

    /**
     * Get the execution status and additional report info when cancelling the async execution
     */
    Response getStatusForAsyncCancelledExecution(String executionId);
        
    /**
     * Run a report action such as sort/filter/format/etc.
     */
    Response runReportAction(String executionId, String actionData);

    /**
     * Asynchronous start of export procedure.
     *
     * @param executionId - report execution ID
     * @param exportOptions - options for export
     * @return export metadata
     * @throws ErrorDescriptorException in case if report export fails. See RemoteException.getErrorDesctiptor() to get detailed error descriptions.
     */
    ExportExecution executeExport(String executionId, ExportExecutionOptions exportOptions) throws ErrorDescriptorException;

    /**
     *
     * @param executionId - report execution ID
     * @param exportId - export execution ID
     * @return export execution object
     * @throws ResourceNotFoundException in case if either report execution or export execution isn't found
     */
    ExportExecution getExportExecution(String executionId, String exportId) throws ResourceNotFoundException;

    /**
     * Search for currently running reports
     *
     * @param searchCriteria - search criteria
     * @return set of currently running report's information
     * @throws ErrorDescriptorException if any error occurs
     */
    Set<ReportExecutionStatusInformation> getCurrentlyRunningReports(SchedulerReportExecutionStatusSearchCriteria searchCriteria)
            throws ErrorDescriptorException;

    /**
     * Report execution cancellation.
     *
     * @param executionId - report execution ID
     * @throws ErrorDescriptorException if any error occurs
     */
    Boolean cancelReportExecution(String executionId) throws ErrorDescriptorException;

    /**
     * Remove report execution from session.
     *
     * @param executionId - report execution ID
     * @throws ErrorDescriptorException if any error occurs
     */
    Boolean deleteReportExecution(String executionId) throws ErrorDescriptorException;

}
