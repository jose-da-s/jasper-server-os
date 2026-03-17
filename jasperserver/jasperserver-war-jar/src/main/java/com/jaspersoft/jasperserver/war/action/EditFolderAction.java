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

import com.jaspersoft.jasperserver.api.JSDuplicateResourceException;
import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.common.util.StaticExecutionContextProvider;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.FolderImpl;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.war.dto.FolderWrapper;
import org.springframework.validation.DataBinder;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ScopeType;

import java.util.List;

/**
 * @author Ionut Nedelcu (ionutned@users.sourceforge.net)
 * @version $Id$
 */
public class EditFolderAction extends FormAction
{
	private static final String FORM_OBJECT_KEY = "folderWrapper";
	private static final String PARENT_FOLDER_ATTR = "parentFolder";
	private static final String CURRENT_FOLDER_ATTR = "currentFolder";
	private static final String IS_EDIT_FOLDER = "isEdit";


	private RepositoryService repository;

	public RepositoryService getRepository() {
		return repository;
	}

	public void setRepository(RepositoryService repository) {
		this.repository = repository;
	}


	protected void initBinder(RequestContext context, DataBinder binder) {
		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
		binder.setAllowedFields(
			"actualFolder.name",
				"actualFolder.label",
				"actualFolder.description"
		);
	}


	/**
	 *
	 */
	public EditFolderAction(){
		setFormObjectClass(FolderWrapper.class); //custom form backing object class
		setFormObjectName(FORM_OBJECT_KEY);
		setFormObjectScope(ScopeType.FLOW); 		//this is a multi-page wizard!
	}


	/**
	 *
	 */
	public Object createFormObject(RequestContext context)
	{
		Folder folder;
		FolderWrapper wrapper;
		String isEditFolder = (String)context.getFlowScope().get(IS_EDIT_FOLDER);
		if (isEditFolder == null) {
		   isEditFolder = (String)context.getRequestParameters().get("isEdit"); 	
		   context.getFlowScope().put(IS_EDIT_FOLDER, isEditFolder);	
		}
		if (isEditFolder != null)
		{
			ExecutionContextImpl executionContext = new ExecutionContextImpl();
			String currentFolder = (String) context.getFlowScope().get(CURRENT_FOLDER_ATTR);
			if (currentFolder == null) {
			   currentFolder = (String)context.getRequestParameters().get("CurrentFolder");	
			   context.getFlowScope().put(CURRENT_FOLDER_ATTR, currentFolder);
			}
			folder = repository.getFolder(executionContext, currentFolder);
			if(folder == null){
				throw new JSException("jsexception.folder.not.found.at", new Object[] {currentFolder});
			}
			wrapper = new FolderWrapper(folder);
			wrapper.setEdit(true);
		}
		else
		{
			folder = new FolderImpl();
			String parentFolder = (String) context.getFlowScope().get(PARENT_FOLDER_ATTR);
			if (parentFolder == null) {
				parentFolder = (String)context.getRequestParameters().get("ParentFolderUri");
				context.getFlowScope().put(PARENT_FOLDER_ATTR, parentFolder);
			}
			if (parentFolder == null || parentFolder.trim().length() == 0)
				parentFolder = "/";
			folder.setParentFolder(parentFolder);
			wrapper = new FolderWrapper(folder);
			//	Get a list of all folders in the current folder to validate name uniqueness
			List folders=repository.getSubFolders(StaticExecutionContextProvider.getExecutionContext(), parentFolder);
			wrapper.setAllFolders(folders);
		}		
		return wrapper;
	}


	/**
	 *
	 */
	public Event saveFolder(RequestContext context) throws Exception
	{
		FolderWrapper wrapper = (FolderWrapper) getFormObject(context);
		try {
			repository.saveFolder(null, wrapper.getActualFolder());
		} catch (JSDuplicateResourceException e) {
			getFormErrors(context).rejectValue("actualFolder.name", "FolderValidator.error.duplicate");
			return error();
		}

		return success();
	}

	/**
	 *
	 */
	public Event setupEditForm(RequestContext context) throws Exception
	{
		MutableAttributeMap rs = context.getRequestScope();
		rs.put(FORM_OBJECT_KEY, getFormObject(context));
		return success();
	}

}
