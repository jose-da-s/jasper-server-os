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
import com.jaspersoft.jasperserver.api.engine.jasperreports.common.ExportParameters;
import com.jaspersoft.jasperserver.api.engine.jasperreports.common.TxtExportParametersBean;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPropertiesHolder;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleTextReportConfiguration;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.WriterExporterOutput;


/**
 * @author sanda zaharia (shertage@users.sourceforge.net)
 * @version $Id$
 */
public class ReportTextExporter extends AbstractReportExporter
{

	private static final String DIALOG_NAME = "txtExportParams";
	
	private TxtExportParametersBean exportParameters;
	
	/**
	 * @return Returns the exportParameters.
	 */
	public TxtExportParametersBean getExportParameters() {
		return exportParameters;
	}

	/**
	 * @return Returns the exportParameters.
	 */
	public ExportParameters getExportParameters(RequestContext context) {
		return context.getFlowScope().get(ReportTextExporter.DIALOG_NAME)== null? exportParameters : (ExportParameters)context.getFlowScope().get(ReportTextExporter.DIALOG_NAME);
	}

	/**
	 * @param exportParameters The exportParameters to set.
	 */
	public void setExportParameters(TxtExportParametersBean exportParameters) {
		this.exportParameters = exportParameters;
	}

	public void export(RequestContext context, ExecutionContext executionContext, JasperPrint jasperPrint, OutputStream outputStream) throws JRException
	{
		JRTextExporter exporter = new JRTextExporter(getJasperReportsContext());
		
		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		String encoding = JRPropertiesUtil.getInstance(getJasperReportsContext()).getProperty(jasperPrint, WriterExporterOutput.PROPERTY_CHARACTER_ENCODING);
		SimpleWriterExporterOutput exporterOutput = new SimpleWriterExporterOutput(outputStream, encoding);
		exporter.setExporterOutput(exporterOutput);

		TxtExportParametersBean exportParams = (TxtExportParametersBean)getExportParameters(context);
		
		SimpleTextReportConfiguration txtReportConfig = new SimpleTextReportConfiguration();
		
		if (exportParams.isOverrideReportHints()) {
			txtReportConfig.setOverrideHints(Boolean.TRUE);
		}
		
		if (exportParams.getCharacterHeight() != null)
			txtReportConfig.setCharHeight(exportParams.getCharacterHeight());
		if (exportParams.getCharacterWidth() != null)
			txtReportConfig.setCharWidth(exportParams.getCharacterWidth());
		if (exportParams.getPageHeight() != null)
			txtReportConfig.setPageHeightInChars(exportParams.getPageHeight());
		if (exportParams.getPageWidth() != null)
			txtReportConfig.setPageWidthInChars(exportParams.getPageWidth());
		
		exporter.setConfiguration(txtReportConfig);
		
		exporter.exportReport();
	}

	protected String getContentType(RequestContext context) {
		return "application/txt";
	}

	protected void setAdditionalResponseHeaders(RequestContext context, HttpServletResponse response) {
		super.setAdditionalResponseHeaders(context, response);
		response.setHeader("Content-Disposition", "inline; filename=\"" + getFilename(context) + "\"");
	}

	protected String getDownloadFileExtension() {
		return "txt";
	}

	@Override
	protected Boolean isPaginationPreferred(JRPropertiesHolder propertiesHolder) {
		Boolean isPaginationPreferred = super.isPaginationPreferred(propertiesHolder);
		if (isPaginationPreferred == null)
		{
			if (propertiesHolder != null) 
			{
				isPaginationPreferred = JRPropertiesUtil.getInstance(getJasperReportsContext()).getBooleanProperty(propertiesHolder.getPropertiesMap(), TxtExportParametersBean.PROPERTY_TEXT_PAGINATED);
			}
		}
		return isPaginationPreferred;
	}
}

