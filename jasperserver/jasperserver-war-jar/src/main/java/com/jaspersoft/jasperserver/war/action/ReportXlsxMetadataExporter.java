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

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPropertiesHolder;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.ooxml.XlsxMetadataExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxMetadataExporterConfiguration;
import net.sf.jasperreports.export.SimpleXlsxMetadataReportConfiguration;

import org.springframework.webflow.execution.RequestContext;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.engine.jasperreports.common.ExportParameters;
import com.jaspersoft.jasperserver.api.engine.jasperreports.common.XlsExportParametersBean;


/**
 * @author Sanda Zaharia
 */
public class ReportXlsxMetadataExporter extends AbstractReportExporter
{

	private static final String DIALOG_NAME = "excelExportParams";
	
	private XlsExportParametersBean exportParameters;
	
	/**
	 * @return Returns the exportParameters.
	 */
	public XlsExportParametersBean getExportParameters() {
		return exportParameters;
	}

	/**
	 * @return Returns the exportParameters.
	 */
	public ExportParameters getExportParameters(RequestContext context) {
		return context.getFlowScope().get(DIALOG_NAME)== null? exportParameters : (ExportParameters)context.getFlowScope().get(DIALOG_NAME);
	}

	/**
	 * @param exportParameters The exportParameters to set.
	 */
	public void setExportParameters(XlsExportParametersBean exportParameters) {
		this.exportParameters = exportParameters;
	}

	public void export(RequestContext context, ExecutionContext executionContext, JasperPrint jasperPrint, OutputStream outputStream) throws JRException
	{
		XlsxMetadataExporter exporter = new XlsxMetadataExporter(getJasperReportsContext());
		
		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

		XlsExportParametersBean exportParams = (XlsExportParametersBean)getExportParameters(context);

		SimpleXlsxMetadataReportConfiguration xlsxReportConfig = new SimpleXlsxMetadataReportConfiguration();
		
		if (exportParams.isOverrideReportHints()) {
			xlsxReportConfig.setOverrideHints(Boolean.TRUE);
		}
		
		if (exportParams.getOnePagePerSheet() != null)
			xlsxReportConfig.setOnePagePerSheet(exportParams.getOnePagePerSheet());
		if (exportParams.getDetectCellType() != null)
			xlsxReportConfig.setDetectCellType(exportParams.getDetectCellType());
		if (exportParams.getRemoveEmptySpaceBetweenRows() != null)
			xlsxReportConfig.setRemoveEmptySpaceBetweenRows(exportParams.getRemoveEmptySpaceBetweenRows());
		if (exportParams.getRemoveEmptySpaceBetweenColumns() != null)
			xlsxReportConfig.setRemoveEmptySpaceBetweenColumns(exportParams.getRemoveEmptySpaceBetweenColumns());
		if (exportParams.getWhitePageBackground() != null)
			xlsxReportConfig.setWhitePageBackground(exportParams.getWhitePageBackground());
		if (exportParams.getIgnoreGraphics() != null)
			xlsxReportConfig.setIgnoreGraphics(exportParams.getIgnoreGraphics());
		if (exportParams.getCollapseRowSpan() != null)
			xlsxReportConfig.setCollapseRowSpan(exportParams.getCollapseRowSpan());
		if (exportParams.getIgnoreCellBorder() != null)
			xlsxReportConfig.setIgnoreCellBorder(exportParams.getIgnoreCellBorder());
		if (exportParams.getFontSizeFixEnabled() != null)
			xlsxReportConfig.setFontSizeFixEnabled(exportParams.getFontSizeFixEnabled());
		if (exportParams.getMaximumRowsPerSheet() != null)
			xlsxReportConfig.setMaxRowsPerSheet(exportParams.getMaximumRowsPerSheet());
		if(exportParams.getXlsFormatPatternsMap() != null && !exportParams.getXlsFormatPatternsMap().isEmpty())
			xlsxReportConfig.setFormatPatternsMap(exportParams.getXlsFormatPatternsMap());

		exporter.setConfiguration(xlsxReportConfig);

		SimpleXlsxMetadataExporterConfiguration xlsxExporterConfig = new SimpleXlsxMetadataExporterConfiguration();
		xlsxExporterConfig.setCreateCustomPalette(Boolean.TRUE);
		exporter.setConfiguration(xlsxExporterConfig);
		
		exporter.exportReport();
	}

	protected String getContentType(RequestContext context) {
		return "application/xlsx";
	}

	protected void setAdditionalResponseHeaders(RequestContext context, HttpServletResponse response) {
		super.setAdditionalResponseHeaders(context, response);
		response.setHeader("Content-Disposition", "inline; filename=\"" + getFilename(context) + "\"");
	}

	protected String getDownloadFileExtension() {
		return "xlsx";
	}

	@Override
	protected Boolean isPaginationPreferred(JRPropertiesHolder propertiesHolder) {
		Boolean isPaginationPreferred = super.isPaginationPreferred(propertiesHolder);
		if (isPaginationPreferred == null)
		{
			if (propertiesHolder != null) 
			{
				isPaginationPreferred = JRPropertiesUtil.getInstance(getJasperReportsContext()).getBooleanProperty(propertiesHolder.getPropertiesMap(), XlsExportParametersBean.PROPERTY_XLS_PAGINATED);
			}
		}
		return isPaginationPreferred;
	}
}

