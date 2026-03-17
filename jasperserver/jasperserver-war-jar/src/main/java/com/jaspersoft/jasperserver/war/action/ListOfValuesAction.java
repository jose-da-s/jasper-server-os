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
import com.jaspersoft.jasperserver.api.common.crypto.PasswordCipherer;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.common.util.StaticExecutionContextProvider;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ListOfValues;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ListOfValuesItem;
import com.jaspersoft.jasperserver.api.metadata.common.domain.RepositoryConfiguration;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceLookup;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.ListOfValuesItemImpl;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.view.domain.FilterCriteria;
import com.jaspersoft.jasperserver.war.dto.BaseDTO;
import com.jaspersoft.jasperserver.war.dto.ListOfValuesDTO;
import com.jaspersoft.jasperserver.war.validation.ListOfValuesValidator;
import org.springframework.validation.DataBinder;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ScopeType;

import java.util.Arrays;

/**
 * @author Ionut Nedelcu (ionutned@users.sourceforge.net)
 * @version $Id$
 */
public class ListOfValuesAction extends FormAction {
    private static final String ATTRIBUTE_RESOURCE_ID_NOT_SUPPORTED_SYMBOLS = "resourceIdNotSupportedSymbols";

	private static final String FORM_OBJECT_KEY = "listOfValuesDTO";
	private static final String LISTOFVALUES_ATTR = "listOfValues";
	private static final String PARENT_FOLDER_ATTR = "parentFolder";
	private static final String IS_EDIT = "isEdit";//FIXME use wrapper to disable name in UI
    private static final String SECURE_VALUE_SUBSTITUTION =  "*******";

	private RepositoryService repository;
    private RepositoryConfiguration configuration;

    public RepositoryService getRepository() {
		return repository;
	}

	public void setRepository(RepositoryService repository) {
		this.repository = repository;
	}

    public void setConfiguration(RepositoryConfiguration configuration) {
        this.configuration = configuration;
    }

    protected void initBinder(RequestContext context, DataBinder binder) {
		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
        binder.setAllowedFields(
                "listOfValues.name",
                "listOfValues.label",
                "listOfValues.description",
                "newLabel",
                "newValue"
        );
	}

	/**
	 *
	 */
	public ListOfValuesAction(){
		setFormObjectClass(ListOfValuesDTO.class); //custom form backing object class
		setFormObjectName(FORM_OBJECT_KEY);
		setFormObjectScope(ScopeType.FLOW); 		//this is a multi-page wizard!
		setValidator(new ListOfValuesValidator());
	}


	/**
	 *
	 */
	public Event lovList(RequestContext context)
	{
		ResourceLookup[] resources = repository.findResource(
				StaticExecutionContextProvider.getExecutionContext(),
				FilterCriteria.createFilter(ListOfValues.class));

		context.getRequestScope().put("resources", Arrays.asList(resources));
		return success();
	}


	/**
	 *
	 */
	public Event setupEditForm(RequestContext context) throws Exception	{
		MutableAttributeMap rs = context.getRequestScope();
		rs.put("folders", repository.getAllFolders(null));
		rs.put(FORM_OBJECT_KEY, getFormObject(context));

        context.getFlowScope().put(ATTRIBUTE_RESOURCE_ID_NOT_SUPPORTED_SYMBOLS,
                configuration.getResourceIdNotSupportedSymbols());

		return success();
	}


	/**
	 *
	 */
	public Object createFormObject(RequestContext context)
	{
		ListOfValues listOfValues;
		ListOfValuesDTO listOfValuesDTO;
		ExecutionContextImpl executionContext = new ExecutionContextImpl();

		String isEdit = (String)context.getFlowScope().get(IS_EDIT);
		if (isEdit == null) {
			isEdit = (String)context.getRequestParameters().get(IS_EDIT);
			context.getFlowScope().put(IS_EDIT, isEdit);
		}
		if (isEdit != null)
		{
			String currentDataType = (String) context.getFlowScope().get(LISTOFVALUES_ATTR);
			if (currentDataType == null) {
				currentDataType = (String)context.getRequestParameters().get("resource");
				context.getFlowScope().put(LISTOFVALUES_ATTR, currentDataType);
			}

			listOfValues = (ListOfValues) repository.getResource(executionContext, currentDataType);
			if(listOfValues == null){
				context.getFlowScope().remove("prevForm");
				throw new JSException("jsexception.could.not.find.resource.with.uri", new Object[] {currentDataType});
			}

            listOfValuesDTO = new ListOfValuesDTO(hideEncryptedValues(listOfValues));
			listOfValuesDTO.setMode(BaseDTO.MODE_STAND_ALONE_EDIT);
		}
		else
		{
			listOfValues = (ListOfValues) repository.newResource(executionContext, ListOfValues.class);
			String parentFolder = (String) context.getFlowScope().get(PARENT_FOLDER_ATTR);
			if (parentFolder == null) {
				parentFolder = (String)context.getRequestParameters().get("ParentFolderUri");
				context.getFlowScope().put(PARENT_FOLDER_ATTR, parentFolder);
			}
			if (parentFolder == null || parentFolder.trim().length() == 0)
				parentFolder = "/";
			listOfValues.setParentFolder(parentFolder);
			listOfValuesDTO = new ListOfValuesDTO(listOfValues);
			listOfValuesDTO.setMode(BaseDTO.MODE_STAND_ALONE_NEW);
		}

		return listOfValuesDTO;
	}

    ListOfValues hideEncryptedValues(ListOfValues listOfValues) {
        if (listOfValues.getValues() != null) {
            for (ListOfValuesItem listOfValuesItem : listOfValues.getValues()) {
				if (PasswordCipherer.getInstance().isEncrypted(listOfValuesItem.getValue().toString())) {
                    listOfValuesItem.setValue(SECURE_VALUE_SUBSTITUTION);
                }
            }
        }
        return listOfValues;
    }

	/**
	 *
	 */
	public Event addLovItem(RequestContext context) throws Exception
	{
		ListOfValuesDTO listOfValuesDTO = (ListOfValuesDTO) getFormObject(context);
		ListOfValues listOfValues = listOfValuesDTO.getListOfValues();
		ListOfValuesItem item = new ListOfValuesItemImpl();
		item.setLabel(listOfValuesDTO.getNewLabel());
		item.setValue(listOfValuesDTO.getNewValue());
		ListOfValuesItem[] values = listOfValues.getValues();
		for (int i = 0; i < values.length; i++) {
			if (values[i].getLabel().equals(item.getLabel())) {
                getFormErrors(context).rejectValue("newLabel", "ListOfValuesValidator.error.duplicate");
                return error();
			}
		}

    	listOfValues.addValue(item);
		listOfValuesDTO.setNewLabel(null);
		listOfValuesDTO.setNewValue(null);
		return success();
	}


	/**
	 *
	 */
	public Event removeLovItem(RequestContext context) throws Exception
	{
		ListOfValuesDTO listOfValuesDTO = (ListOfValuesDTO) getFormObject(context);
		ListOfValues listOfValues = listOfValuesDTO.getListOfValues();
		String[] items;
		try {
			items = context.getRequestParameters().getArray("itemToDelete");
		} catch (IllegalArgumentException e) {
			// when only one pair is selected for deletion
			items=new String[1];
			items[0]=context.getRequestParameters().get("itemToDelete");
		}
		if(items!=null && items[0]!=null){
			ListOfValuesItem[] values = listOfValues.getValues();
			for (int i = 0; i < values.length; i++) {
				for (int j = 0; j < items.length; j++) {
					if (values[i].getLabel().equals(items[j])) {
						listOfValues.removeValue(values[i]);
						break;
					}
				}
			}
		}
		return success();
	}

	/**
	 *
	 */
	public Event writeLov(RequestContext context) throws Exception
	{
		ListOfValuesDTO listOfValuesDTO = (ListOfValuesDTO) getFormObject(context);
		if (listOfValuesDTO.isStandAloneMode()) {
			try {
				repository.saveResource(null, listOfValuesDTO.getListOfValues());
				return yes();
			}
			catch (JSDuplicateResourceException e) {
				getFormErrors(context).rejectValue("listOfValues.name", "ListOfValuesValidator.error.duplicate");
				return error();
			}
		}

		return success();
	}

}
