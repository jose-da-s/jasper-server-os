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

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.util.StaticExecutionContextProvider;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ContentResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.FolderImpl;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.api.metadata.view.domain.FilterCriteria;
import com.jaspersoft.jasperserver.war.tags.PaginatorTag;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.MessageSource;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class RepositoryAction extends FormAction {

	protected final Log log = LogFactory.getLog(this.getClass());

	protected static final Class[] RESOURCE_TYPES = {ReportUnit.class, ContentResource.class};

	private RepositoryService repository;
	private String flowAttributeFolder;
	private MessageSource messages;//FIXME not used

	/*
		 * method to get the reposervice object arguments: none returns:
		 * RepositoryService
		 */
	public RepositoryService getRepository() {
		return repository;
	}

	/*
	 * method to set the reposervice object arguments: RepositoryService
	 * returns: void
	 */
	public void setRepository(RepositoryService repository) {
		this.repository = repository;
	}

	public MessageSource getMessages()
	{
		return messages;
	}

	public void setMessages(MessageSource messages)
	{
		this.messages = messages;
	}

	public RepositoryAction()
	{
//		setFormObjectClass(CreateReportWizardDTO.class); 
//		// custom form backing object class							
//		setFormObjectName(FORM_OBJECT_KEY);
//		setFormObjectScope(ScopeType.FLOW); // this is a multi-page wizard!
//		setValidator(new CreateReportFlowValidator());
	}

	public Event initAction(RequestContext context)
	{
		String folderURI = context.getRequestParameters().get("folder");

		if (folderURI == null)
		{
			folderURI = context.getFlowScope().getString(getFlowAttributeFolder());
			if (folderURI == null)
			{
				folderURI = "/";
			}
		}
		else
		{
			context.getFlowScope().put(getFlowAttributeFolder(), folderURI);
		}

		List pathFolders = getPathFolders(folderURI);
		context.getRequestScope().put("pathFolders", pathFolders);

		ExecutionContext executionContext = StaticExecutionContextProvider.getExecutionContext();

		List folders = repository.getSubFolders(executionContext, folderURI);

		Class[] resourceTypes = getResourceTypes();
		FilterCriteria[] criterias = new FilterCriteria[resourceTypes.length];
		for (int i = 0; i < resourceTypes.length; i++)
		{
			FilterCriteria criteria = FilterCriteria.createFilter(resourceTypes[i]);
			criteria.addFilterElement(FilterCriteria.createParentFolderFilter(folderURI));
			criterias[i] = criteria;
		}

		List resourceLookups = repository.loadResourcesList(executionContext, criterias);
		
		List resources = new ArrayList();
		resources.addAll(folders);
		resources.addAll(resourceLookups);

		context.getRequestScope().put("resources", resources);

		return success();
	}
	
	protected Class[] getResourceTypes()
	{
		return RESOURCE_TYPES;
	}

	public Event goToPage(RequestContext context)
	{
		context.getFlowScope().put(
			PaginatorTag.CURRENT_PAGE_REQUEST_PARAMETER, 
			context.getRequestParameters().get(PaginatorTag.CURRENT_PAGE_REQUEST_PARAMETER)
			);

		return success();
	}

	protected List getPathFolders(String uri)
	{
		List pathFolders = new ArrayList();

		Folder parentFolder = null;

		StringTokenizer tkzer = new StringTokenizer(uri, Folder.SEPARATOR, false);
		while(tkzer.hasMoreTokens())
		{
			String token = tkzer.nextToken();

			Folder folder = new FolderImpl();
			folder.setName(token);
			folder.setParentFolder(parentFolder);

			pathFolders.add(folder);

			parentFolder = folder;
		}

		return pathFolders;
	}

	public String getFlowAttributeFolder() {
		return flowAttributeFolder;
	}

	public void setFlowAttributeFolder(String flowAttributeFolder) {
		this.flowAttributeFolder = flowAttributeFolder;
	}
}
