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
package com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.ValidationResult;
import com.jaspersoft.jasperserver.api.engine.common.domain.Request;
import com.jaspersoft.jasperserver.api.engine.common.domain.Result;
import com.jaspersoft.jasperserver.api.engine.common.service.EngineService;
import com.jaspersoft.jasperserver.api.engine.common.service.ReportExecutionStatusInformation;
import com.jaspersoft.jasperserver.api.engine.common.service.ReportExecutionStatusSearchCriteria;
import com.jaspersoft.jasperserver.api.engine.common.service.ReportInputControlsInformation;
import com.jaspersoft.jasperserver.api.engine.common.service.SchedulerReportExecutionStatusSearchCriteria;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControlsContainer;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceLookup;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomDomainMetaData;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceService;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.export.ExporterInputItem;
import org.apache.commons.collections.OrderedMap;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id: EngineBaseDecorator.java 23440 2012-06-21 13:11:21Z afomin $
 */
public class EngineBaseDecorator implements EngineService
{

	private EngineService decoratedEngine;

	public void clearCaches(Class resourceItf, String resourceURI)
	{
		decoratedEngine.clearCaches(resourceItf, resourceURI);
	}

	public ReportDataSourceService createDataSourceService(ReportDataSource dataSource)
	{
		return decoratedEngine.createDataSourceService(dataSource);
	}

	public Result execute(ExecutionContext context, Request request)
	{
		return decoratedEngine.execute(context, request);
	}

	public OrderedMap executeQuery(ExecutionContext context, ResourceReference queryReference, String keyColumn, String[] resultColumns, ResourceReference defaultDataSourceReference)
	{
		return decoratedEngine.executeQuery(context, queryReference, keyColumn, resultColumns, defaultDataSourceReference);
	}

	public OrderedMap executeQuery(ExecutionContext context,
			ResourceReference queryReference, String keyColumn,
			String[] resultColumns,
			ResourceReference defaultDataSourceReference, Map parameterValues)
	{
		return decoratedEngine.executeQuery(context, 
				queryReference, keyColumn, 
				resultColumns, 
				defaultDataSourceReference, parameterValues);
	}

    public OrderedMap executeQuery(
            ExecutionContext context, ResourceReference queryReference, String keyColumn, String[] resultColumns,
            ResourceReference defaultDataSourceReference, Map parameterValues, Map<String, Class<?>> parameterTypes, boolean formatValueColumns) {
        return decoratedEngine.executeQuery(context,
                queryReference, keyColumn,
                resultColumns,
                defaultDataSourceReference, parameterValues, parameterTypes, formatValueColumns);
    }

    @Override
    public void exportToPdf(ExecutionContext context,
                            String reportUnitURI,
                            JasperPrint jasperPrint,
                            OutputStream outputStream,
                            Integer startPageIndex,
                            Integer endPageIndex,
                            Boolean overrideReportHints,
                            List<ExporterInputItem> inputItems,
                            Integer pageIndex) {
        decoratedEngine.exportToPdf(context, reportUnitURI, jasperPrint, outputStream, startPageIndex, endPageIndex, overrideReportHints, inputItems, pageIndex);
    }

	public Set getDataSourceTypes(ExecutionContext context, String queryLanguage)
	{
		return decoratedEngine.getDataSourceTypes(context, queryLanguage);
	}

	public ResourceLookup[] getDataSources(ExecutionContext context, String queryLanguage)
	{
		return decoratedEngine.getDataSources(context, queryLanguage);
	}

	public JasperReport getMainJasperReport(ExecutionContext context, String reportUnitURI)
	{
		return decoratedEngine.getMainJasperReport(context, reportUnitURI);
	}

    public JasperReport getMainJasperReport(ExecutionContext context, InputControlsContainer container) {
        return decoratedEngine.getMainJasperReport(context, container);
    }

    public String getQueryLanguage(ExecutionContext context, ResourceReference jrxmlResource)
	{
		return decoratedEngine.getQueryLanguage(context, jrxmlResource);
	}

	public Resource[] getResources(ResourceReference jrxmlReference)
	{
		return decoratedEngine.getResources(jrxmlReference);
	}

	public void release()
	{
		decoratedEngine.release();
	}

	public ValidationResult validate(ExecutionContext context, ReportUnit reportUnit)
	{
		return decoratedEngine.validate(context, reportUnit);
	}

	public Map getReportInputControlDefaultValues(ExecutionContext context, String reportURI, Map initialParameters)
	{
		return decoratedEngine.getReportInputControlDefaultValues(context, reportURI, initialParameters);
	}
	
	public ReportInputControlsInformation getReportInputControlsInformation(ExecutionContext context, InputControlsContainer icContainer, Map initialParameters){
        return decoratedEngine.getReportInputControlsInformation(context, icContainer, initialParameters);
    }

	public EngineService getDecoratedEngine()
	{
		return decoratedEngine;
	}
	
	public void setDecoratedEngine(EngineService decoratedEngine)
	{
		this.decoratedEngine = decoratedEngine;
	}

	public ReportInputControlsInformation getReportInputControlsInformation(
			ExecutionContext context, String reportURI, Map initialParameters) {
		return decoratedEngine.getReportInputControlsInformation(context, reportURI, initialParameters);
    }

    public ExecutionContext getRuntimeExecutionContext() {
    	return null;
    }
	public boolean cancelExecution(String requestId) {
		return decoratedEngine.cancelExecution(requestId);
	}

    public List<ReportExecutionStatusInformation> getReportExecutionStatusList() {
        return decoratedEngine.getReportExecutionStatusList();
    }

    public List<ReportExecutionStatusInformation> getReportExecutionStatusList(ReportExecutionStatusSearchCriteria searchCriteria) {
        return decoratedEngine.getReportExecutionStatusList(searchCriteria);
    }

    public List<ReportExecutionStatusInformation> getSchedulerReportExecutionStatusList() {
        return decoratedEngine.getSchedulerReportExecutionStatusList();
    }

    public List<ReportExecutionStatusInformation> getSchedulerReportExecutionStatusList(SchedulerReportExecutionStatusSearchCriteria searchCriteria) {
        return decoratedEngine.getSchedulerReportExecutionStatusList(searchCriteria);
    }

    @Override
    public CustomDomainMetaData getMetaDataFromConnector(CustomReportDataSource customReportDataSource) throws Exception {
        return decoratedEngine.getMetaDataFromConnector(customReportDataSource);
    }

    @Override
    public boolean isCustomDomainMetadataProvider(CustomReportDataSource customReportDataSource) {
        return decoratedEngine.isCustomDomainMetadataProvider(customReportDataSource);
    }

    @Override
    public boolean isJDBCDiscoverySupported(CustomReportDataSource customReportDataSource) {
        return decoratedEngine.isJDBCDiscoverySupported(customReportDataSource);
    }
}
