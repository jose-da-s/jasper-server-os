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

package com.jaspersoft.jasperserver.api.engine.jasperreports.util;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.export.AbstractHtmlExporter;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRExporterContext;
import net.sf.jasperreports.engine.export.JRXlsAbstractExporter;
import net.sf.jasperreports.poi.export.JRXlsExporter;
import net.sf.jasperreports.export.AbstractXlsExporterConfiguration;
import net.sf.jasperreports.export.AbstractXlsReportConfiguration;
import net.sf.jasperreports.export.HtmlExporterConfiguration;
import net.sf.jasperreports.export.HtmlReportConfiguration;
import net.sf.jasperreports.export.SimpleXlsExporterConfiguration;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;
import net.sf.jasperreports.export.XlsExporterConfiguration;
import net.sf.jasperreports.export.XlsReportConfiguration;


/**
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 * @version $Id: ExportUtil.java 19932 2010-12-11 15:24:29Z tmatyashovsky $
 */
public class ExportUtil 
{
	public static final String HTTP_SERVLET_REQUEST = "HttpServletRequest";

	private final JasperReportsContext jasperReportsContext;
	
	private ExportUtil(JasperReportsContext jasperReportsContext)
	{
		if (jasperReportsContext == null)
		{
			jasperReportsContext = DefaultJasperReportsContext.getInstance();
		}
		this.jasperReportsContext = jasperReportsContext;
		JRPropertiesUtil propUtil = JRPropertiesUtil.getInstance(jasperReportsContext);
	}

	public static ExportUtil getInstance(JasperReportsContext jasperReportsContext)
	{
		return new ExportUtil(jasperReportsContext);
	}

	public AbstractHtmlExporter<HtmlReportConfiguration, HtmlExporterConfiguration> createHtmlExporter()
	{
		return new HtmlExporter(jasperReportsContext);
	}

	public JRXlsAbstractExporter<? extends XlsReportConfiguration, ? extends XlsExporterConfiguration, ? extends JRExporterContext> createXlsExporter()
	{
		return new JRXlsExporter(jasperReportsContext);
	}

	public AbstractXlsReportConfiguration createXlsReportConfiguration()
	{
		return new SimpleXlsReportConfiguration();
	}

	public AbstractXlsExporterConfiguration createXlsExporterConfiguration()
	{
		return new SimpleXlsExporterConfiguration();
	}

	public void setConfiguration(
		JRXlsAbstractExporter<? extends XlsReportConfiguration, ? extends XlsExporterConfiguration, ? extends JRExporterContext> exporter,
		XlsReportConfiguration xlsReportConfig
		)
	{
		((JRXlsExporter)exporter).setConfiguration(xlsReportConfig);
	}

	public void setConfiguration(
		JRXlsAbstractExporter<? extends XlsReportConfiguration, ? extends XlsExporterConfiguration, ? extends JRExporterContext> exporter,
		XlsExporterConfiguration xlsExporterConfig
		)
	{
		((JRXlsExporter)exporter).setConfiguration(xlsExporterConfig);
	}

}
