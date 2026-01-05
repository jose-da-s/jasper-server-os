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
package com.jaspersoft.jasperserver.war.control;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.j2ee.web.WebResourceHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.AbstractView;

/**
 * Controller for JasperServer report resources.
 * 
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 * @version $Id: ReportImageController.java 20676 2011-07-25 12:59:03Z tdanciu $
 */
public class ReportResourceController implements Controller
{
	private static final Log log = LogFactory.getLog(ReportResourceController.class);
	
	private JasperReportsContext jasperReportsContext;

	protected static class ResourceView extends AbstractView 
	{
		public ResourceView() 
		{
		}

		protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception 
		{
		}
	}
	
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws JRException
	{
		List<WebResourceHandler> resourceHandlers = getJasperReportsContext().getExtensions(WebResourceHandler.class);
		if (resourceHandlers != null) 
		{
			for (WebResourceHandler handler: resourceHandlers) 
			{
				if (handler.handleResource(getJasperReportsContext(), request, response)) 
				{
					break;
				}
			}
		}
		View view = new ResourceView();
		return new ModelAndView(view);
	}

	public JasperReportsContext getJasperReportsContext() {
		return jasperReportsContext;
	}

	public void setJasperReportsContext(JasperReportsContext jasperReportsContext) {
		this.jasperReportsContext = jasperReportsContext;
	}
}
