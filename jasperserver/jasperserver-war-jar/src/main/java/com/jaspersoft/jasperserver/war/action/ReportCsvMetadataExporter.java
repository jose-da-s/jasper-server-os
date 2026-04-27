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
package com.jaspersoft.jasperserver.war.action;

import java.io.OutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.webflow.execution.RequestContext;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.engine.jasperreports.common.CsvExportParametersBean;
import com.jaspersoft.jasperserver.api.engine.jasperreports.common.ExportParameters;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPropertiesHolder;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRCsvMetadataExporter;
import net.sf.jasperreports.export.SimpleCsvMetadataExporterConfiguration;
import net.sf.jasperreports.export.SimpleCsvMetadataReportConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.WriterExporterOutput;


/**
 * @author Sanda Zaharia (szaharia@users.sourceforge.net)
 */
public class ReportCsvMetadataExporter extends AbstractReportExporter
{
	
	private static final String DIALOG_NAME = "csvMetadataExportParams";

	private CsvExportParametersBean exportParameters;
	private String contentType = "application/octet-stream";
	private String contentDisposition = "attachment";

	public void export(RequestContext context, ExecutionContext executionContext, JasperPrint jasperPrint, OutputStream outputStream) throws JRException
	{
		JRCsvMetadataExporter exporter = new JRCsvMetadataExporter(getJasperReportsContext());

		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		String encoding = JRPropertiesUtil.getInstance(getJasperReportsContext()).getProperty(jasperPrint, WriterExporterOutput.PROPERTY_CHARACTER_ENCODING);
		SimpleWriterExporterOutput exporterOutput = new SimpleWriterExporterOutput(outputStream, encoding);
		exporter.setExporterOutput(exporterOutput);
		
		CsvExportParametersBean exportParams = (CsvExportParametersBean)getExportParameters(context);
		
		SimpleCsvMetadataReportConfiguration csvReportConfig = new SimpleCsvMetadataReportConfiguration();
		if (exportParams.isOverrideReportHints()) {
			csvReportConfig.setOverrideHints(Boolean.TRUE);
		}
		exporter.setConfiguration(csvReportConfig);

		SimpleCsvMetadataExporterConfiguration csvExporterConfig = new SimpleCsvMetadataExporterConfiguration();
		csvExporterConfig.setFieldDelimiter(exportParams.getFieldDelimiter());
		exporter.setConfiguration(csvExporterConfig);
		
		exporter.exportReport();
	}

	protected String getContentType(RequestContext context) {
		return contentType;
	}

	protected void setAdditionalResponseHeaders(RequestContext context, HttpServletResponse response) {
		super.setAdditionalResponseHeaders(context, response);
		response.setHeader("Content-Disposition", contentDisposition 
				+ "; filename=\"" + getFilename(context) + "\"");
	}

	protected String getDownloadFileExtension() {
		return "csv";
	}

	/**
	 * @return Returns the exportParameters.
	 */
	public CsvExportParametersBean getExportParameters() {
		return exportParameters;
	}

	/**
	 * @param exportParameters The exportParameters to set.
	 */
	public void setExportParameters(CsvExportParametersBean exportParameters) {
		this.exportParameters = exportParameters;
	}

	/**
	 * @return Returns the exportParameters.
	 */
	public ExportParameters getExportParameters(RequestContext context) {
		//if request 
		return context.getFlowScope().get(ReportCsvMetadataExporter.DIALOG_NAME) == null ? exportParameters : (ExportParameters)context.getFlowScope().get(ReportCsvMetadataExporter.DIALOG_NAME);
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setContentDisposition(String contentDisposition) {
		this.contentDisposition = contentDisposition;
	}

	@Override
	protected Boolean isPaginationPreferred(JRPropertiesHolder propertiesHolder) {
		Boolean isPaginationPreferred = super.isPaginationPreferred(propertiesHolder);
		if (isPaginationPreferred == null)
		{
			if (propertiesHolder != null) 
			{
				isPaginationPreferred = JRPropertiesUtil.getInstance(getJasperReportsContext()).getBooleanProperty(propertiesHolder.getPropertiesMap(), CsvExportParametersBean.PROPERTY_CSV_PAGINATED);
			}
		}
		return isPaginationPreferred;
	}
}

