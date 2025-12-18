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
package com.jaspersoft.jasperserver.war.action;

import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPropertiesHolder;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.JasperPrint;

import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.execution.RequestContext;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.engine.common.service.EngineService;
import com.jaspersoft.jasperserver.api.engine.jasperreports.common.ExportParameters;
import com.jaspersoft.jasperserver.api.engine.jasperreports.common.PdfExportParametersBean;


/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class ReportPdfExporter extends AbstractReportExporter 
{

	private EngineService engine;
	private PdfExportParametersBean exportParameters;

	public void export(RequestContext context, ExecutionContext executionContext, JasperPrint jasperPrint, OutputStream outputStream) throws JRException
	{
		AttributeMap<Object> flowAttrs = context.getFlowScope();
		String reportUnitURI = flowAttrs.getRequiredString(getFlowAttributeReportUnitURI());

		engine.exportToPdf(executionContext, reportUnitURI, jasperPrint, outputStream, null, null, exportParameters.isOverrideReportHints(), null, null);
	}

	protected String getContentType(RequestContext context) {
		return "application/pdf";
	}

	protected void setAdditionalResponseHeaders(RequestContext context, HttpServletResponse response) {
		super.setAdditionalResponseHeaders(context, response);
		response.setHeader("Content-Disposition", "inline; filename=\"" + getFilename(context) + "\"");
	}

	protected String getDownloadFileExtension() {
		return "pdf";
	}

	public EngineService getEngine() {
		return engine;
	}

	public void setEngine(EngineService engine) {
		this.engine = engine;
	}
	/**
	 * @return Returns the exportParameters.
	 */
	public ExportParameters getExportParameters(RequestContext context) {
		return null;
	}

	/**
	 * @return Returns the exportParameters.
	 */
	public PdfExportParametersBean getExportParameters() {
		return exportParameters;
	}

	/**
	 * @param exportParameters The exportParameters to set.
	 */
	public void setExportParameters(PdfExportParametersBean exportParameters) {
		this.exportParameters = exportParameters;
	}
	
	@Override
	protected Boolean isPaginationPreferred(JRPropertiesHolder propertiesHolder) {
		Boolean isPaginationPreferred = super.isPaginationPreferred(propertiesHolder);
		if (isPaginationPreferred == null)
		{
			if (propertiesHolder != null) 
			{
				isPaginationPreferred = JRPropertiesUtil.getInstance(getJasperReportsContext()).getBooleanProperty(propertiesHolder.getPropertiesMap(), PdfExportParametersBean.PROPERTY_PDF_PAGINATED);
			}
		}
		return isPaginationPreferred;
	}

	@Override
	protected Integer getMaxPageHeight(JRPropertiesHolder propertiesHolder) {
		Integer maxPageHeight = super.getMaxPageHeight(propertiesHolder);
		if (maxPageHeight == null && propertiesHolder != null) {
			maxPageHeight = JRPropertiesUtil.getInstance(getJasperReportsContext()).getIntegerProperty(
					propertiesHolder, PdfExportParametersBean.PROPERTY_PDF_MAX_PAGE_HEIGHT);
		}
		return maxPageHeight;
	}

	@Override
	protected Integer getMaxPageWidth(JRPropertiesHolder propertiesHolder) {
		Integer maxPageWidth = super.getMaxPageWidth(propertiesHolder);
		if (maxPageWidth == null && propertiesHolder != null) {
			maxPageWidth = JRPropertiesUtil.getInstance(getJasperReportsContext()).getIntegerProperty(
					propertiesHolder, PdfExportParametersBean.PROPERTY_PDF_MAX_PAGE_WIDTH);
		}
		return maxPageWidth;
	}
	
	
}
