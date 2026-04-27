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
import net.sf.jasperreports.engine.export.JRExporterContext;
import net.sf.jasperreports.engine.export.JRXlsAbstractExporter;
import net.sf.jasperreports.export.AbstractXlsExporterConfiguration;
import net.sf.jasperreports.export.AbstractXlsReportConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.XlsExporterConfiguration;
import net.sf.jasperreports.export.XlsReportConfiguration;

import org.springframework.webflow.execution.RequestContext;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.engine.jasperreports.common.ExportParameters;
import com.jaspersoft.jasperserver.api.engine.jasperreports.common.XlsExportParametersBean;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.ExportUtil;


/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class ReportExcelExporter extends AbstractReportExporter
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
		return context.getFlowScope().get(ReportExcelExporter.DIALOG_NAME)== null? exportParameters : (ExportParameters)context.getFlowScope().get(ReportExcelExporter.DIALOG_NAME);
	}

	/**
	 * @param exportParameters The exportParameters to set.
	 */
	public void setExportParameters(XlsExportParametersBean exportParameters) {
		this.exportParameters = exportParameters;
	}

	public void export(RequestContext context, ExecutionContext executionContext, JasperPrint jasperPrint, OutputStream outputStream) throws JRException
	{
		ExportUtil exportUtil = ExportUtil.getInstance(getJasperReportsContext());
		JRXlsAbstractExporter<? extends XlsReportConfiguration, ? extends XlsExporterConfiguration, ? extends JRExporterContext> exporter = 
				exportUtil.createXlsExporter();

		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

		XlsExportParametersBean exportParams = (XlsExportParametersBean)getExportParameters(context);
		
		AbstractXlsReportConfiguration xlsReportConfig = exportUtil.createXlsReportConfiguration();
		
		if (exportParams.isOverrideReportHints()) {
			xlsReportConfig.setOverrideHints(Boolean.TRUE);
		}

		xlsReportConfig.setUseTimeZone(true);
		
		if (exportParams.getOnePagePerSheet() != null)
			xlsReportConfig.setOnePagePerSheet(exportParams.getOnePagePerSheet());
		if (exportParams.getDetectCellType() != null)
			xlsReportConfig.setDetectCellType(exportParams.getDetectCellType());
		if (exportParams.getRemoveEmptySpaceBetweenRows() != null)
			xlsReportConfig.setRemoveEmptySpaceBetweenRows(exportParams.getRemoveEmptySpaceBetweenRows());
		if (exportParams.getRemoveEmptySpaceBetweenColumns() != null)
			xlsReportConfig.setRemoveEmptySpaceBetweenColumns(exportParams.getRemoveEmptySpaceBetweenColumns());
		if (exportParams.getWhitePageBackground() != null)
			xlsReportConfig.setWhitePageBackground(exportParams.getWhitePageBackground());
		if (exportParams.getIgnoreGraphics() != null)
			xlsReportConfig.setIgnoreGraphics(exportParams.getIgnoreGraphics());
		if (exportParams.getCollapseRowSpan() != null)
			xlsReportConfig.setCollapseRowSpan(exportParams.getCollapseRowSpan());
		if (exportParams.getIgnoreCellBorder() != null)
			xlsReportConfig.setIgnoreCellBorder(exportParams.getIgnoreCellBorder());
		if (exportParams.getFontSizeFixEnabled() != null)
			xlsReportConfig.setFontSizeFixEnabled(exportParams.getFontSizeFixEnabled());
		if (exportParams.getMaximumRowsPerSheet() != null)
			xlsReportConfig.setMaxRowsPerSheet(exportParams.getMaximumRowsPerSheet());
		if(exportParams.getXlsFormatPatternsMap() != null && !exportParams.getXlsFormatPatternsMap().isEmpty())
			xlsReportConfig.setFormatPatternsMap(exportParams.getXlsFormatPatternsMap());
		
		exportUtil.setConfiguration(exporter, xlsReportConfig);

		AbstractXlsExporterConfiguration xlsExporterConfig = exportUtil.createXlsExporterConfiguration();
		xlsExporterConfig.setCreateCustomPalette(Boolean.TRUE);
		exportUtil.setConfiguration(exporter, xlsExporterConfig);
		
		exporter.exportReport();
	}

	protected String getContentType(RequestContext context) {
		return "application/xls";
	}

	protected void setAdditionalResponseHeaders(RequestContext context, HttpServletResponse response) {
		super.setAdditionalResponseHeaders(context, response);
		response.setHeader("Content-Disposition", "inline; filename=\"" + getFilename(context) + "\"");
	}

	protected String getDownloadFileExtension() {
		return "xls";
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

