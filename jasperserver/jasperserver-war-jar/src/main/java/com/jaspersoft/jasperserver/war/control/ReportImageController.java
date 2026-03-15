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

import java.awt.Color;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.AbstractView;

import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.ReportUnitResult;
import com.jaspersoft.jasperserver.war.util.SessionObjectSerieAccessor;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintImage;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.type.ImageTypeEnum;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.type.OnErrorTypeEnum;
import net.sf.jasperreports.engine.util.JRImageLoader;
import net.sf.jasperreports.engine.util.JRTypeSniffer;
import net.sf.jasperreports.renderers.DataRenderable;
import net.sf.jasperreports.renderers.Renderable;
import net.sf.jasperreports.renderers.ResourceRenderer;
import net.sf.jasperreports.renderers.util.RendererUtil;
import net.sf.jasperreports.repo.RepositoryUtil;

/**
 * Controller for JasperServer report images.
 * 
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class ReportImageController  implements Controller
{
	private JasperReportsContext jasperReportsContext;

	private SessionObjectSerieAccessor jasperPrintAccessor;
	private String jasperPrintNameParameter;
	private String imageNameParameter;

	protected static class ImageView extends AbstractView {

		private final String imageMimeType;
		private final byte[] imageData;

		public ImageView(final String imageMimeType, final byte[] imageData) {
			this.imageMimeType = imageMimeType;
			this.imageData = imageData;
		}

		protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
			if (imageData != null && imageData.length > 0) {
				if (imageMimeType != null) {
					response.setHeader("Content-Type", imageMimeType);
				}
				response.setContentLength(imageData.length);
				ServletOutputStream ouputStream = response.getOutputStream();
				ouputStream.write(imageData, 0, imageData.length);
				ouputStream.flush();
				ouputStream.close();
			} else {
				response.getOutputStream().close();
			}
		}
		
	}
	
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws JRException
	{
		String jasperPrintName = request.getParameter(getJasperPrintNameParameter());
		ReportUnitResult result = (ReportUnitResult) getJasperPrintAccessor().getObject(request, jasperPrintName);
		JasperPrint jasperPrint = (result == null ? null : result.getJasperPrintAccessor().getJasperPrint());
		if (jasperPrint == null) {
			throw new JSException("jsexception.jasperprint.not.found", new Object[] {jasperPrintName});
		}
		
		byte[] imageData = null;
		String imageMimeType = null;

		String imageName = request.getParameter(getImageNameParameter());
		if ("px".equals(imageName))
		{
			imageData = RepositoryUtil.getInstance(getJasperReportsContext()).getBytesFromLocation(JRImageLoader.PIXEL_IMAGE_RESOURCE);
			imageMimeType = ImageTypeEnum.GIF.getMimeType();
		}
		else
		{
			JRPrintImage image = HtmlExporter.getImage(Arrays.asList(new JasperPrint[]{jasperPrint}), imageName);
			
			Renderable renderer = image.getRenderer();
			
			Dimension dimension = new Dimension(image.getWidth(), image.getHeight());
			Color backcolor = ModeEnum.OPAQUE == image.getMode() ? image.getBackcolor() : null;
			
			RendererUtil rendererUtil = RendererUtil.getInstance(getJasperReportsContext());
			
			try
			{
				imageData = process(renderer, dimension, backcolor);
			}
			catch (Exception e)
			{
				try
				{
					Renderable onErrorRenderer = rendererUtil.handleImageError(e, image.getOnErrorType());
					if (onErrorRenderer != null)
					{
						imageData = process(onErrorRenderer, dimension, backcolor);
					}
				}
				catch (Exception ex)
				{
					throw new JSException(ex);
				}
			}
			
			imageMimeType = 
				rendererUtil.isSvgData(imageData)
				? RendererUtil.SVG_MIME_TYPE
				: JRTypeSniffer.getImageTypeValue(imageData).getMimeType();
		}

		View view = new ImageView(imageMimeType, imageData);
		return new ModelAndView(view);
	}

	protected byte[] process(
		Renderable renderer,
		Dimension dimension,
		Color backcolor
		) throws JRException
	{
		RendererUtil rendererUtil = RendererUtil.getInstance(getJasperReportsContext());
		
		if (renderer instanceof ResourceRenderer)
		{
			renderer = //hard to use a cache here and it would be just for some icon type of images, if any 
					rendererUtil.getNonLazyRenderable(
					((ResourceRenderer)renderer).getResourceLocation(), 
					OnErrorTypeEnum.ERROR
					);
		}

		DataRenderable dataRenderer = 
				rendererUtil.getDataRenderable(
				renderer,
				dimension,
				backcolor
				);
		
		return dataRenderer.getData(getJasperReportsContext());
	}

	public SessionObjectSerieAccessor getJasperPrintAccessor() {
		return jasperPrintAccessor;
	}

	public void setJasperPrintAccessor(
			SessionObjectSerieAccessor jasperPrintAccessor) {
		this.jasperPrintAccessor = jasperPrintAccessor;
	}

	public String getJasperPrintNameParameter() {
		return jasperPrintNameParameter;
	}

	public void setJasperPrintNameParameter(String jasperPrintNameAttribute) {
		this.jasperPrintNameParameter = jasperPrintNameAttribute;
	}

	public String getImageNameParameter() {
		return imageNameParameter;
	}

	public void setImageNameParameter(String imageNameParameter) {
		this.imageNameParameter = imageNameParameter;
	}

	public JasperReportsContext getJasperReportsContext() {
		return jasperReportsContext;
	}

	public void setJasperReportsContext(JasperReportsContext jasperReportsContext) {
		this.jasperReportsContext = jasperReportsContext;
	}
}
