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

package com.jaspersoft.jasperserver.api.engine.scheduling.quartz;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.jaspersoft.jasperserver.api.engine.common.service.EngineService;
import com.jaspersoft.jasperserver.api.engine.jasperreports.common.PdfExportParametersBean;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ContentResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.DataContainer;

import net.sf.jasperreports.engine.JRPropertiesHolder;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.JasperPrint;

/**
 * @author sanda zaharia (shertage@users.sourceforge.net)
 * @version $Id$
 */
public class PdfReportOutput extends AbstractReportOutput 
{

	private static final Log log = LogFactory.getLog(PdfReportOutput.class);

	private PdfExportParametersBean exportParams;
	
	public PdfReportOutput()
	{
	}

	@Override
	protected DataContainer export(ReportJobContext jobContext, JasperPrint jasperPrint,
			Integer startPageIndex, Integer endPageIndex) {
		boolean close = true;
		DataContainer pdfData = jobContext.createDataContainer(this);
		OutputStream pdfDataOut = pdfData.getOutputStream();
		
		try {
			EngineService engineService = jobContext.getEngineService();
			engineService.exportToPdf(jobContext.getExecutionContext(), jobContext.getReportUnitURI(), jasperPrint, pdfDataOut, startPageIndex, endPageIndex, null, null, null);
			
			close = false;
			pdfDataOut.close();
		} catch (IOException e) {
			throw new JSExceptionWrapper(e);
		} finally {
			if (close) {
				try {
					pdfDataOut.close();
				} catch (IOException e) {
					log.error("Error closing stream", e);
				}
			}
		}
		return pdfData;
	}

	/**
	 * @return Returns the exportParams.
	 */
	public PdfExportParametersBean getExportParams() {
		return exportParams;
	}

	/**
	 * @param exportParams The exportParams to set.
	 */
	public void setExportParams(PdfExportParametersBean exportParams) {
		this.exportParams = exportParams;
	}
	
	@Override
	public Boolean isPaginationPreferred(JRPropertiesHolder propertiesHolder){
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
	
	@Override
	public String getFileExtension() {
		return "pdf";
	}
	
	@Override
	public String getFileType() {
		return ContentResource.TYPE_PDF;
	}
}
