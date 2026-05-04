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
package com.jaspersoft.jasperserver.war.control;

import java.io.OutputStream;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.AbstractView;

import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ContentResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResourceData;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.DataContainerStreamUtil;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.type.ImageTypeEnum;
import net.sf.jasperreports.engine.util.JRTypeSniffer;
import net.sf.jasperreports.renderers.util.RendererUtil;

public class ContentRepositoryFileView extends AbstractView
{
	public static final String REPOSITORY_PATH = "repositoryPath";

	RepositoryService repository;

	public ContentRepositoryFileView(RepositoryService repository)
	{
		this.repository = repository;
	}


	protected void renderMergedOutputModel(Map map, HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		String pathinfo = request.getPathInfo();
		int start = pathinfo.indexOf('/', 1);
		String  repoPath = pathinfo.substring(start, pathinfo.length());

		if (repoPath == null || repoPath.length() == 0)
			return;

		OutputStream out = response.getOutputStream();

		ContentResource file = (ContentResource) repository.getResource(null, repoPath, ContentResource.class);
		if (file == null) {
			throw new JSException("jsexception.could.not.find.content.resource.with.uri",
					new Object[]{repoPath});
		}
		
		String fileType = file.getFileType();
		String name = file.getName();

		if (fileType == null)
			throw new JSException("jsexception.undefined.file.type");

		if (fileType.equals(ContentResource.TYPE_UNSPECIFIED) && name.contains(".") && !name.endsWith(".")) {
			fileType = name.substring(name.lastIndexOf(".") + 1);
			if (fileType.contains("_")){
				fileType = fileType.substring(0, fileType.indexOf("_"));
			}
		}

		FileResourceData fileData = repository.getContentResourceData(null, repoPath);
		try {

	        response.setHeader("Pragma", "");
	        response.setHeader("Cache-Control", "no-store");

	        byte[] readData = null;
			if (fileType.equals(ContentResource.TYPE_PDF)) {
				response.setContentType("application/pdf");
			} else if (fileType.equals(ContentResource.TYPE_XLS)) {
				response.setContentType("application/xls");
				response.setHeader("Content-Disposition", "inline");
	        } else if (fileType.equals(ContentResource.TYPE_XLSX)) {
				response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
				response.setHeader("Content-Disposition", "inline");
	        } else if (fileType.equals(ContentResource.TYPE_DOCX)) {
				response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
				response.setHeader("Content-Disposition", "inline");
			} else if (fileType.equals(ContentResource.TYPE_RTF)) {
				response.setContentType("application/rtf");
			} else if (fileType.equals(ContentResource.TYPE_CSV)) {
				response.setContentType("text/csv");
	        } else if (fileType.equals(ContentResource.TYPE_ODT)) {
				response.setContentType("application/vnd.oasis.opendocument.text");
	        } else if (fileType.equals(ContentResource.TYPE_ODS)) {
				response.setContentType("application/vnd.oasis.opendocument.spreadsheet");
	        } else if (fileType.equals(ContentResource.TYPE_PPTX)) {
				response.setContentType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
	        } else if (fileType.equals(ContentResource.TYPE_IMAGE) && fileData.hasData()) {
	        	readData = DataContainerStreamUtil.readDataAndClose(fileData.getDataStream());
	        	if (JRTypeSniffer.getImageTypeValue(readData) == ImageTypeEnum.UNKNOWN
	        			&& RendererUtil.getInstance(DefaultJasperReportsContext.getInstance()).isSvgData(readData)) {
					response.setContentType(RendererUtil.SVG_MIME_TYPE);
	        	} else {
					response.setContentType(JRTypeSniffer.getImageTypeValue(readData).getMimeType());
					response.setHeader("Content-Disposition", "attachment");
				}
			}

			if (fileData.hasData()) {
				response.setContentLength(fileData.dataSize());
				if (readData == null) {
					DataContainerStreamUtil.pipeDataAndCloseInput(fileData.getDataStream(), out);
				} else {
					out.write(readData);
				}
			} else {
				response.setContentLength(0);
			}		
		} finally {
			fileData.dispose();
		}
		
		out.flush();
	}
}

