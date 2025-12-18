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
package com.jaspersoft.jasperserver.war.dto;

import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.war.common.JasperServerConst;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FileResourceWrapper extends BaseDTO implements Serializable {
	private FileResource fileResource;

	private Resource resource;

	private byte[] newData;

	private String newUri;

	private Map allTypes;

	private boolean suggested;

	private List allResources;
	
	private List existingResources;

	private String source;

	private String fileName;

	private Object parentFlowObject;

	private boolean located;
	
	private List allFolders;
	
	private String folder;

	public boolean isLocated() {
		return located;
	}

	public void setLocated(boolean located) {
		this.located = located;
	}

	public Object getParentFlowObject() {
		return parentFlowObject;
	}

	public void setParentFlowObject(Object parentFlowObject) {
		this.parentFlowObject = parentFlowObject;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	@Deprecated
	public List getAllResources() {
		return allResources;
	}

	@Deprecated
	public void setAllResources(List allResources) {
		this.allResources = allResources;
	}

	public boolean isSuggested() {
		return suggested;
	}

	public void setSuggested(boolean suggested) {
		this.suggested = suggested;
	}

	public Map getAllTypes() {
		//TODO remove this as we also have ConfigurationBean.getAllFileResourceTypes
		if (allTypes == null) {
			allTypes = new LinkedHashMap();
			allTypes.put(FileResource.TYPE_IMAGE,
					JasperServerConst.TYPE_RSRC_IMAGE);
			allTypes.put(FileResource.TYPE_FONT,
					JasperServerConst.TYPE_RSRC_FONT);
			allTypes.put(FileResource.TYPE_JAR,
					JasperServerConst.TYPE_RSRC_CLASS_JAR);
			allTypes.put(FileResource.TYPE_JRXML,
					JasperServerConst.TYPE_RSRC_SUB_REPORT);
			allTypes.put(FileResource.TYPE_RESOURCE_BUNDLE,
					JasperServerConst.TYPE_RSRC_RESOURCE_BUNDLE);
			allTypes.put(FileResource.TYPE_STYLE_TEMPLATE,
					JasperServerConst.TYPE_RSRC_STYLE_TEMPLATE);
			allTypes.put(FileResource.TYPE_ACCESS_GRANT_SCHEMA,
						 JasperServerConst.TYPE_RSRC_ACCESS_GRANT_SCHEMA); // pro-only
			allTypes.put(FileResource.TYPE_XML,
					JasperServerConst.TYPE_RSRC_XML_FILE);
            allTypes.put(FileResource.TYPE_CSS,
                    JasperServerConst.TYPE_RSRC_CSS_FILE);
            allTypes.put(FileResource.TYPE_MONGODB_JDBC_CONFIG,
                    JasperServerConst.TYPE_RSRC_MONGODB_JDBC_CONFIG);
            allTypes.put(FileResource.TYPE_AZURE_CERTIFICATE,
                    JasperServerConst.TYPE_RSRC_AZURE_CERTIFICATE);
            allTypes.put(FileResource.TYPE_SECURE_FILE,
                    JasperServerConst.TYPE_SECURE_FILE);
		}
		return allTypes;
	}

	public void setAllTypes(Map allTypes) {
		this.allTypes = allTypes;
	}

	public FileResource getFileResource() {
		return fileResource;
	}

	public void setFileResource(FileResource fileResource) {
		this.fileResource = fileResource;
	}

	public void afterBind() {
		if (getSource() != null
				&& fileResource != null
				&& getSource().equals(
						JasperServerConst.FIELD_CHOICE_FILE_SYSTEM)) {
			fileResource.setReferenceURI(null);
		}
	}

	public byte[] getNewData() {
		return newData;
	}

	public void setNewData(byte[] newData) {
		if (newData != null && newData.length != 0) {
			fileResource.setData(newData);
		}
		this.newData = newData;
	}

	public String getNewUri() {
		return newUri;
	}

	public void setNewUri(String newUri) {
		if (newUri != null && newUri.trim().length() != 0)
			fileResource.setReferenceURI(newUri);
		this.newUri = newUri;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

    @Deprecated
	public List getExistingResources() {
		return existingResources;
	}

    @Deprecated
	public void setExistingResources(List existingResources) {
		this.existingResources = existingResources;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	@Deprecated
	public List getAllFolders() {
		return allFolders;
	}

	@Deprecated
	public void setAllFolders(List allFolders) {
		this.allFolders = allFolders;
	}
}
