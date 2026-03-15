/*
 * Copyright (C) 2025 the Jasper Server OS Authors
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
package com.jaspersoft.jasperserver.api.engine.common.service;

import com.jaspersoft.jasperserver.api.JasperServerAPI;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.ValidationResult;
import com.jaspersoft.jasperserver.api.engine.common.domain.Request;
import com.jaspersoft.jasperserver.api.engine.common.domain.Result;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControlsContainer;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceLookup;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomDomainMetaData;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomDomainMetaDataProvider;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceServiceFactory;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import net.sf.jasperreports.export.ExporterInputItem;
import org.apache.commons.collections.OrderedMap;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Service that provides methods related to report execution.
 *
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @author Ionut Nedelcu
 * @version $Id: AbstractAttributedObject.java 2140 2006-02-21 06:41:21Z tony $
 * @see ReportUnit
 * @since 1.0
 */
@JasperServerAPI
public interface EngineService
{
	/**
	 * Key name to use for datasourceURI in the parameter map
	 * This is done in a queryManipulator if you want value queries to be cacheable
	 */
	public static final String JS_DATASOURCE_URI = "datasourceURI";
	
	/**
	 * Executes a report.
	 * 
	 * <p>
	 * The report execution requests specifies the report to execute and the
	 * parameters to use.
	 * </p>
	 * 
     * @param context the caller execution context
	 * @param request the report execution request
	 * @return the report execution result containing the filled report
	 */
	public Result execute(ExecutionContext context, Request request);

	/**
	 * Searches for repository resource references inside a JRXML report.
	 * 
	 * <p>
	 * The JRXML template is searched for references to resources local to
	 * the report unit (using <code>repo:localResurceName</code> URIs).
	 * References to local images, subreports and PDF fonts are detected.
	 * </p>
	 * 
	 * @param jrxmlReference resource references which contains JRXML file data
	 * @return an array of detected local resource references.  The objects in
	 * the array have their expected name and type set.
	 */
	public Resource[] getResources(ResourceReference jrxmlReference); //FIXME move this to a different interface or service

	/**
	 * Validates a report unit by attempting to compile the uploaded JRXMLs.
	 * 
     * @param context the caller execution context
	 * @param reportUnit the report unit object to validate
	 * @return the validation result
	 * @see ValidationResult#getValidationState()
	 */
	public ValidationResult validate(ExecutionContext context, ReportUnit reportUnit);

	/**
	 * Returns the main report of a report unit.
	 * 
     * @param context the caller execution context
	 * @param reportUnitURI the repository path of the report unit
	 * @return the compiled report used as main report by the unit
	 */
	public JasperReport getMainJasperReport(ExecutionContext context, String reportUnitURI);

	/**
	 * Returns the main report from passed instance of {@link InputControlsContainer}.
	 *
     * @param context the caller execution context
	 * @param container instance of {@link InputControlsContainer}
	 * @return the compiled report used as main report by the unit
	 */
	public JasperReport getMainJasperReport(ExecutionContext context, InputControlsContainer container);

	/**
	 * Disposes of any system resources created during the life of this engine.
	 * 
	 * <p>
	 * This method should be called when the engine no longer used.
	 * </p>
	 */
	public void release();

	/**
	 * Clears the caches kept by the engine for a specific resource.
	 * 
	 * <p>
	 * This method is called when the resource changes or is deleted.
	 * </p>
	 * 
	 * @param resourceItf the resource type
	 * @param resourceURI the repository path of the resource
	 */
	public void clearCaches(Class resourceItf, String resourceURI);

	/**
	 * Creates a data source service for a data source resource.
	 * 
	 * <p>
	 * The engine uses data source factories registered for the specific
	 * data source types.
	 * </p>
	 * 
	 * @param dataSource the data source resource
	 * @return a data source service
	 * @see ReportDataSourceServiceFactory
	 */
	public ReportDataSourceService createDataSourceService(ReportDataSource dataSource);

	/**
	 * Exports a executed report to PDF.
	 * 
	 * <p>
	 * The report unit resources are loaded in order to resolve PDF fonts
	 * uploaded as repository resources.
	 * </p>
	 * 
     * @param context the caller execution context
	 * @param reportUnitURI the repository path of the report unit
	 */
	void exportToPdf(ExecutionContext context,
                     String reportUnitURI,
                     JasperPrint jasperPrint,
                     OutputStream outputStream,
                     Integer startPageIndex,
                     Integer endPageIndex,
                     Boolean overrideReportHints,
                     List<ExporterInputItem> inputItems,
                     Integer pageIndex
                     );

	/**
	 * Executes a query  against a data source.
	 * 
     * @param context the caller execution context
	 * @param queryReference the reference to the query resource
	 * @param keyColumn the name of the result column to be used as record key
	 * @param resultColumns the names of the result columns
	 * @param defaultDataSourceReference a reference to a data source to be used 
	 * as fallback when the query resource does not specify a data source
	 * @return an ordered map containing records indexed by keys.
	 * Keys are the values of the key column, and a record is represented
	 * by an array containing one <code>java.lang.String</code> value for each
	 * result column. 
	 * @see EngineService#executeQuery(ExecutionContext, ResourceReference, String, String[], ResourceReference, Map)
	 */
	public OrderedMap executeQuery(ExecutionContext context, 
			ResourceReference queryReference, String keyColumn, String[] resultColumns, 
			ResourceReference defaultDataSourceReference);
	
	/**
	 * Executes a query against a data source.
	 * 
	 * <p>
	 * This method is used for executing report input control queries.
	 * </p>
	 * 
     * @param context the caller execution context
	 * @param queryReference the reference to the query resource
	 * @param keyColumn the name of the result column to be used as record key
	 * @param resultColumns the names of the result columns
	 * @param defaultDataSourceReference a reference to a data source to be used 
	 * as fallback when the query resource does not specify a data source
	 * @param parameterValues a map of parameter values indexed by names
	 * @return an ordered map containing records indexed by keys.
	 * Keys are the values of the key column, and a record is represented
	 * by an array containing one <code>java.lang.String</code> value for each
	 * result column. 
	 * @since 3.5.0
	 */
	public OrderedMap executeQuery(ExecutionContext context, 
			ResourceReference queryReference, String keyColumn, String[] resultColumns, 
			ResourceReference defaultDataSourceReference,
			Map parameterValues);

	/**
	 * Executes a query against a data source.
	 *
	 * <p>
	 * This method is used for executing report input control queries.
	 * </p>
	 *
     * @param context the caller execution context
	 * @param queryReference the reference to the query resource
	 * @param keyColumn the name of the result column to be used as record key
	 * @param resultColumns the names of the result columns
	 * @param defaultDataSourceReference a reference to a data source to be used
	 * as fallback when the query resource does not specify a data source
	 * @param parameterValues a map of parameter values indexed by names
     * @param parameterTypes a map of parameter types, for case when value in parameterValues is null
     * and it's impossible to figure out it's class.
     * @param formatValueColumns a flag which indicates whether to convert
     *  result values to string.
	 * @return an ordered map containing records indexed by keys.
	 * Keys are the values of the key column, and a record is represented
	 * by an array containing one <code>java.lang.String</code>
     * (or <code>java.lang.Object</code> if formatValueColumns was set to false) value for each result column.
	 * @since 4.7.0
	 */
	public OrderedMap executeQuery(ExecutionContext context,
			ResourceReference queryReference, String keyColumn, String[] resultColumns,
			ResourceReference defaultDataSourceReference,
			Map parameterValues, Map<String, Class<?>> parameterTypes, boolean formatValueColumns);

	/**
	 * Lists data sources that support a specific query language.
	 * 
	 * <p>
	 * The engine determines the query languages supported by a data source
	 * type based on its configuration, and lists data sources for all types
	 * that support the specified language.
	 * </p>
	 * 
     * @param context the caller execution context
	 * @param queryLanguage the query language
	 * @return the list of repository data source that support the query
	 * language
	 * @since 1.2.1
	 * @see #getDataSourceTypes(ExecutionContext, String)
	 */
	public ResourceLookup[] getDataSources(ExecutionContext context, String queryLanguage);

	/**
	 * Determines the data source types that support a specific query language.
	 * 
     * @param context the caller execution context
	 * @param queryLanguage the query language
	 * @return the set of data source types (as resource interfaces) that support
	 * the query language
	 * @since 1.2.1
	 */
	public Set getDataSourceTypes(ExecutionContext context, String queryLanguage);
	
	/**
	 * Determines the query language of a JRXML resource.
	 * 
     * @param context the caller execution context
	 * @param jrxmlResource the references to the JRXML resource
	 * @return the language of the JRXML report, or <code>null</code> if the 
	 * report doesn't include a query
	 * @since 1.2.1
	 */
	public String getQueryLanguage(ExecutionContext context, ResourceReference jrxmlResource);

	/**
	 * Evaluates the default values for the input controls of a report unit.
     *
     * Curently deprecated. You should use the getReportInputControlsInformation method
     * and get the values from there: ReportInputControlsInformation#getDefaultValuesMap()
	 *
	 * <p>
	 * Default values for the input controls are defined as parameter default
	 * value expression in the report JRXML.
	 * </p>
	 * 
     * @param context the caller execution context
	 * @param reportURI the repository path of the report unit
	 * @param initialParameters a map containing parameter values indexed by names
	 * to be used when evaluating the default values of the report parameters.
	 * @return a map containing default values for the report input controls
	 * indexed by input names
	 * @since 2.0.0
	 * @see #getReportInputControlsInformation(ExecutionContext, String, Map)
	 */
	@Deprecated
    public Map getReportInputControlDefaultValues(ExecutionContext context, String reportURI, Map initialParameters);


    /**
	 * Returns runtime input control information used at report execution time.
	 * 
	 * <p>
	 * The runtime input control information contains the types, localized 
	 * labels and default values for the report input controls.
	 * </p>
	 * 
     * @param context the caller execution context
	 * @param reportURI the repository path of the report unit
	 * @param initialParameters a map containing parameter values indexed by names
	 * to be used when evaluating the default values of the report parameters.
	 * @return runtime input control information
	 * @since 3.0.0
	 */
	public ReportInputControlsInformation getReportInputControlsInformation(
			ExecutionContext context, String reportURI, Map initialParameters);

    /**
     * Returns runtime input control information used at report execution time.
     *
     * <p>
     * The runtime input control information contains the types, localized
     * labels and default values for the report input controls.
     * </p>
     *
     * @param context the caller execution context
     * @param icContainer resource containing the input controls
     * @param initialParameters a map containing parameter values indexed by names
     * to be used when evaluating the default values of the report parameters.
     * @return runtime input control information
     * @since 3.0.0
     */
	public ReportInputControlsInformation getReportInputControlsInformation(
			ExecutionContext context, InputControlsContainer icContainer, Map initialParameters);


    public ExecutionContext getRuntimeExecutionContext();

    /**
     * Cancels/interrupts a report execution request.
     * 
     * @param requestId the request Id, as specified by {@link Request#getId()}
     * @return whether the report execution has been canceled
     */
    boolean cancelExecution(String requestId);

    /**
	 *
	 * return a list of report job execution status object which contains running jobs information
	 *
	 * @return all the up and running execution jobs.  Key:  requestID, Value: report job execution status
     * @since 4.7
	 */
    public List<ReportExecutionStatusInformation> getReportExecutionStatusList();

    /**
	 *
	 * return a list of report execution status object which contains running jobs information
	 *
     * @param searchCriteria  contains status search information.  Please look at ReportExecutionStatusSearchCriteria
     *        for all the valid key search attributes.
	 * @return all the up and running execution jobs.  Key:  requestID, Value: report job execution status
     * @since 4.7
	 */
    public List<ReportExecutionStatusInformation> getReportExecutionStatusList(ReportExecutionStatusSearchCriteria searchCriteria);

       /**
	 * Return all the running report execution list that kick off by scheduler only.
	 *
	 * @see com.jaspersoft.jasperserver.api.engine.common.service.ReportExecutionStatusInformation
     * @return a list of report job execution status object which contains running jobs information
	 * @since 4.7
	 */
    List<ReportExecutionStatusInformation> getSchedulerReportExecutionStatusList();

    /**
	 * Return the scheduler running report execution list by filtering search criteria.
	 *
	 * @see com.jaspersoft.jasperserver.api.engine.common.service.ReportExecutionStatusInformation
     * @see SchedulerReportExecutionStatusSearchCriteria
     * @param searchCriteria  contains job search information.  Please look at SchedulerReportExecutionStatusSearchCriteria
     *        for all the valid key search attributes.
     * @return a list of report job execution status object which contains running jobs information
	 * @since 4.7
	 */
    List<ReportExecutionStatusInformation> getSchedulerReportExecutionStatusList(SchedulerReportExecutionStatusSearchCriteria searchCriteria);

    /**
     *  This function is used for retrieving the metadata layer of the data connector in form of TableSourceMetadata
     *  TableSourceMetadata contains information JRFields, query, query language and field name mapping (actual JRField name, name used in domain)
     *  Currently, this function only supports data adapter CustomReportDataSource object such as CSV, XLS and XLSX data adapter CustomReportDataSource object.
     *  @return the metadata or <code>null</code> if the datasource doesn't provide metadata
     *  @throws Exception if any error occurs during metadata retrieval
     */
    public CustomDomainMetaData getMetaDataFromConnector(CustomReportDataSource customReportDataSource) throws Exception;

	/**
	 * Checks if the datasource provides metadata information.
	 * A datasource is considered as providing metadata if either it's service or definition implement {@link CustomDomainMetaDataProvider}.
	 * @param customReportDataSource - the custom datasource
	 * @return <code>true</code> if the custom report provides metadata information; <code>false</code> otherwise
	 */
	public boolean isCustomDomainMetadataProvider(CustomReportDataSource customReportDataSource);

    public boolean isJDBCDiscoverySupported(CustomReportDataSource customReportDataSource);

}
