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
package com.jaspersoft.jasperserver.api.engine.jasperreports.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jaspersoft.jasperserver.core.util.XMLUtil;
import net.sf.jasperreports.engine.JRExpression;
import net.sf.jasperreports.engine.JRExpressionChunk;
import net.sf.jasperreports.engine.JRImage;
import net.sf.jasperreports.engine.JRReport;
import net.sf.jasperreports.engine.JRStaticText;
import net.sf.jasperreports.engine.JRStyle;
import net.sf.jasperreports.engine.JRSubreport;
import net.sf.jasperreports.engine.JRTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.util.JRElementsVisitor;
import net.sf.jasperreports.engine.util.JRVisitorSupport;

import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.FileResourceImpl;


/**
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 * @version $Id: AbstractAttributedObject.java 2140 2006-02-21 06:41:21Z tony $
 */
public class ResourceCollector extends JRVisitorSupport
{	
	private static final String REPO_URL_PROTOCOL = "repo:";
	
	private List<Resource> resources = new ArrayList<>();
	private Set<String> resourceNames = new HashSet<>();

	/**
	 * 
	 */
	private ResourceCollector()
	{
	}

	/**
	 * 
	 */
	public static Resource[] getResources(InputStream jrxmlStream)
	{
		JasperDesign jasperDesign = null;

		try
		{
			// check for XXE vulnerability first and then load
			jasperDesign = CustomJRXmlLoader.load(XMLUtil.checkForXXE(jrxmlStream));
		}
		catch (Exception e)
		{
			throw new JSExceptionWrapper(e);
		}

		ResourceCollector collector = new ResourceCollector();
		collector.collect(jasperDesign);
		
		return collector.getResources();
	}

	/**
	 * 
	 */
	private Resource[] getResources()
	{
		return resources.toArray(new Resource[resources.size()]);
	}

	/**
	 * 
	 */
	private void collect(JRReport report)
	{
		if (report != null)
		{
			JRStyle[] styles = report.getStyles();
			if (styles != null)
			{
				for (int i = 0; i < styles.length; i++)
				{
					collect(styles[i]);
				}
			}
			
			JRElementsVisitor.visitReport(report, this);
		}
	}

	/**
	 * 
	 */
	private void collect(JRStyle style)
	{
		collectFont(style.getOwnPdfFontName()); 

		JRStyle[] conditionalStyles = style.getConditionalStyles();
		if (conditionalStyles != null)
		{
			for (int i = 0; i < conditionalStyles.length; i++)
			{
				collect(conditionalStyles[i]);
			}
		}
	}

	public void visitImage(JRImage image) {
		collectFromExpression(image.getExpression(), FileResource.TYPE_IMAGE);
	}

	public void visitStaticText(JRStaticText staticText) {
		collectFont(staticText.getOwnPdfFontName());
	}

	public void visitSubreport(JRSubreport subreport) {
		collectFromExpression(subreport.getExpression(), FileResource.TYPE_JRXML);
	}

	public void visitTextField(JRTextField textField) {
		collectFont(textField.getOwnPdfFontName());
	}

	/**
	 * 
	 */
	private void collectFont(String pdfFontName)
	{
		if (
			pdfFontName != null 
			&& pdfFontName.startsWith(REPO_URL_PROTOCOL)
			&& pdfFontName.indexOf('/') < 0
			)
		{
			String resourceName = pdfFontName.substring(REPO_URL_PROTOCOL.length());
			collectResource(resourceName, FileResource.TYPE_FONT);
		}
	}

	/**
	 * 
	 */
	private void collectFromExpression(JRExpression expression, String resourceType)
	{
		if (
			expression != null 
			&& expression.getChunks() != null
			&& expression.getChunks().length == 1
			)
		{
			JRExpressionChunk chunk = expression.getChunks()[0];
			if (chunk.getType() == JRExpressionChunk.TYPE_TEXT)
			{
				String resourceName = chunk.getText().trim();
				if (
					resourceName.startsWith("\"" + REPO_URL_PROTOCOL)
					&& resourceName.indexOf('/') < 0
					&& resourceName.endsWith("\"")
					)
				{
					resourceName = resourceName.substring(REPO_URL_PROTOCOL.length() + 1, resourceName.length() - 1);
					collectResource(resourceName, resourceType);
				}
			}
		}
	}

	protected void collectResource(String name, String type)
	{
		if (!resourceNames.contains(name))
		{
			FileResource resource = new FileResourceImpl();
			resource.setName(name);
			resource.setLabel(resource.getName());
			resource.setDescription(resource.getName());
			resource.setFileType(type);

			resources.add(resource);
			resourceNames.add(name);
		}
	}

}
