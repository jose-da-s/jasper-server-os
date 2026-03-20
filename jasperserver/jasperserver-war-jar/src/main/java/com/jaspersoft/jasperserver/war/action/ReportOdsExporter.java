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
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOdsExporterConfiguration;
import net.sf.jasperreports.export.SimpleOdsReportConfiguration;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

import org.springframework.webflow.execution.RequestContext;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.engine.jasperreports.common.ExportParameters;
import com.jaspersoft.jasperserver.api.engine.jasperreports.common.OdsExportParametersBean;


/**
 * @author sanda zaharia (shertage@users.sourceforge.net)
 * @version $Id$
 */
public class ReportOdsExporter extends AbstractReportExporter
{

	private static final String DIALOG_NAME = "odsExportParams";
	
	private OdsExportParametersBean exportParameters;
	
	/**
	 * @return Returns the exportParameters.
	 */
	public OdsExportParametersBean getExportParameters() {
		return exportParameters;
	}

	/**
	 * @return Returns the exportParameters.
	 */
	public ExportParameters getExportParameters(RequestContext context) {
		return context.getFlowScope().get(ReportOdsExporter.DIALOG_NAME)== null? exportParameters : (ExportParameters)context.getFlowScope().get(ReportOdsExporter.DIALOG_NAME);
	}

	/**
	 * @param exportParameters The exportParameters to set.
	 */
	public void setExportParameters(OdsExportParametersBean exportParameters) {
		this.exportParameters = exportParameters;
	}

	public void export(RequestContext context, ExecutionContext executionContext, JasperPrint jasperPrint, OutputStream outputStream) throws JRException
	{
		JROdsExporter exporter = new JROdsExporter(getJasperReportsContext());

		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

		OdsExportParametersBean exportParams = (OdsExportParametersBean)getExportParameters(context);
		
		SimpleOdsReportConfiguration odsReportConfig = new SimpleOdsReportConfiguration();
		
		if (exportParams.isOverrideReportHints()) {
			odsReportConfig.setOverrideHints(Boolean.TRUE);
		}
		
		if (exportParams.getOnePagePerSheet() != null)
			odsReportConfig.setOnePagePerSheet(exportParams.getOnePagePerSheet());
		if (exportParams.getDetectCellType() != null)
			odsReportConfig.setDetectCellType(exportParams.getDetectCellType());
		if (exportParams.getRemoveEmptySpaceBetweenRows() != null)
			odsReportConfig.setRemoveEmptySpaceBetweenRows(exportParams.getRemoveEmptySpaceBetweenRows());
		if (exportParams.getRemoveEmptySpaceBetweenColumns() != null)
			odsReportConfig.setRemoveEmptySpaceBetweenColumns(exportParams.getRemoveEmptySpaceBetweenColumns());
		if (exportParams.getWhitePageBackground() != null)
			odsReportConfig.setWhitePageBackground(exportParams.getWhitePageBackground());
		if (exportParams.getIgnoreGraphics() != null)
			odsReportConfig.setIgnoreGraphics(exportParams.getIgnoreGraphics());
		if (exportParams.getCollapseRowSpan() != null)
			odsReportConfig.setCollapseRowSpan(exportParams.getCollapseRowSpan());
		if (exportParams.getIgnoreCellBorder() != null)
			odsReportConfig.setIgnoreCellBorder(exportParams.getIgnoreCellBorder());
		if (exportParams.getFontSizeFixEnabled() != null)
			odsReportConfig.setFontSizeFixEnabled(exportParams.getFontSizeFixEnabled());
		if (exportParams.getMaximumRowsPerSheet() != null)
			odsReportConfig.setMaxRowsPerSheet(exportParams.getMaximumRowsPerSheet());
		if(exportParams.getXlsFormatPatternsMap() != null && !exportParams.getXlsFormatPatternsMap().isEmpty())
			odsReportConfig.setFormatPatternsMap(exportParams.getXlsFormatPatternsMap());

		exporter.setConfiguration(odsReportConfig);

		SimpleOdsExporterConfiguration odsExporterConfig = new SimpleOdsExporterConfiguration();
		odsExporterConfig.setCreateCustomPalette(Boolean.TRUE);
		exporter.setConfiguration(odsExporterConfig);
		
		exporter.exportReport();
	}

	protected String getContentType(RequestContext context) {
		return "application/vnd.oasis.opendocument.spreadsheet";
	}

	protected void setAdditionalResponseHeaders(RequestContext context, HttpServletResponse response) {
		super.setAdditionalResponseHeaders(context, response);
		response.setHeader("Content-Disposition", "inline; filename=\"" + getFilename(context) + "\"");
	}

	protected String getDownloadFileExtension() {
		return "ods";
	}

	@Override
	protected Boolean isPaginationPreferred(JRPropertiesHolder propertiesHolder) {
		Boolean isPaginationPreferred = super.isPaginationPreferred(propertiesHolder);
		if (isPaginationPreferred == null)
		{
			if (propertiesHolder != null) 
			{
				isPaginationPreferred = JRPropertiesUtil.getInstance(getJasperReportsContext()).getBooleanProperty(propertiesHolder.getPropertiesMap(), OdsExportParametersBean.PROPERTY_XLS_PAGINATED);
			}
		}
		return isPaginationPreferred;
	}
}

