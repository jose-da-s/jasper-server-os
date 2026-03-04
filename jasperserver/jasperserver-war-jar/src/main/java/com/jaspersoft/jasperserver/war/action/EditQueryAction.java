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
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.CustomReportDataSourceServiceFactory;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.CustomDataSourceDefinition;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Query;
import com.jaspersoft.jasperserver.api.metadata.common.domain.RepositoryConfiguration;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.*;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.helper.DataSourceJsonHelper;
import com.jaspersoft.jasperserver.war.common.JasperServerConstImpl;
import com.jaspersoft.jasperserver.war.dto.BaseDTO;
import com.jaspersoft.jasperserver.war.dto.QueryWrapper;
import com.jaspersoft.jasperserver.war.dto.ReportDataSourceWrapper;
import com.jaspersoft.jasperserver.war.model.impl.TypedTreeDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ScopeType;

import java.util.List;

/**
 * @author Ionut Nedelcu (ionutned@users.sourceforge.net)
 * @version $Id
 */
public class EditQueryAction extends FormAction {
    private static final String ATTRIBUTE_RESOURCE_ID_NOT_SUPPORTED_SYMBOLS = "resourceIdNotSupportedSymbols";

	private static final String DATA_SOURCE_PARENT_TYPE = "query";
	private static final String FORM_OBJECT_KEY = "query";
	private static final String PARENT_FOLDER_ATTR = "parentFolder";
	private static final String CURRENT_QUERY_ATTR = "currentQuery";
	private static final String IS_EDIT = "isEdit";//FIXME use wrapper to disable name in UI
	private static final String DATASOURCE_OBJECT_KEY = "dataResource";
    private static final String DATASOURCE_TREE_DATA_PROVIDER = "dsTreeDataProvider";

	private RepositoryService repository;
	private JasperServerConstImpl constants = new JasperServerConstImpl();
    private TypedTreeDataProvider typedTreeDataProvider;
    private CustomReportDataSourceServiceFactory customDataSourceFactory;

	private String queryLanguagesRequestAttrName;
	private String[] queryLanguages;

    protected MessageSource messages;
    private RepositoryConfiguration configuration;

    @Autowired
    private DataSourceJsonHelper dataSourceJsonHelper;

    public RepositoryService getRepository() {
		return repository;
	}

	public void setRepository(RepositoryService repository) {
		this.repository = repository;
	}

    public void setDataSourceTreeDataProvider(TypedTreeDataProvider typedTreeDataProvider) {
        this.typedTreeDataProvider = typedTreeDataProvider;
    }

    public void setCustomDataSourceFactory(CustomReportDataSourceServiceFactory customDataSourceFactory) {
        this.customDataSourceFactory = customDataSourceFactory;
    }

    protected void initBinder(RequestContext context, DataBinder binder) {
		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
		binder.setAllowedFields(
				"query.name",
				"query.label",
				"query.description",
				"query.language",
				"query.sql"
		);
	}


	/**
	 *
	 */
	public EditQueryAction(){
		setFormObjectClass(QueryWrapper.class); //custom form backing object class
		setFormObjectName(FORM_OBJECT_KEY);
		setFormObjectScope(ScopeType.FLOW); 		//this is a multi-page wizard!
	}

    public void setMessages(MessageSource messages) {
        this.messages = messages;
    }


	/**
	 *
	 */
	public Object createFormObject(RequestContext context)
	{
		Query query;
		QueryWrapper wrapper;
		ExecutionContextImpl executionContext = new ExecutionContextImpl();

		String isEdit = (String)context.getFlowScope().get(IS_EDIT);
		if (isEdit == null) {
			isEdit = (String)context.getRequestParameters().get(IS_EDIT);
			context.getFlowScope().put(IS_EDIT, isEdit);
		}
		if (isEdit != null)
		{
			String currentQuery = (String) context.getFlowScope().get(CURRENT_QUERY_ATTR);
			if (currentQuery == null) {
				currentQuery = (String)context.getRequestParameters().get(CURRENT_QUERY_ATTR);
				context.getFlowScope().put(CURRENT_QUERY_ATTR, currentQuery);
			}
			query = (Query) repository.getResource(executionContext, currentQuery);
			if(query == null){
				throw new JSException("jsexception.could.not.find.resource.with.uri", new Object[] {currentQuery});
			}
			wrapper = new QueryWrapper(query);
			wrapper.setMode(BaseDTO.MODE_STAND_ALONE_EDIT);
		}
		else
		{
			query = (Query) repository.newResource(executionContext, Query.class);
			String parentFolder = (String) context.getFlowScope().get(PARENT_FOLDER_ATTR);
			if (parentFolder == null) {
			   parentFolder = (String)context.getRequestParameters().get("ParentFolderUri");
			   context.getFlowScope().put(PARENT_FOLDER_ATTR, parentFolder);	
			}
			if (parentFolder == null || parentFolder.trim().length() == 0)
				parentFolder = "/";
			query.setParentFolder(parentFolder);
			query.setLanguage(getQueryLanguages()[0]);
			wrapper = new QueryWrapper(query);
			wrapper.setMode(BaseDTO.MODE_STAND_ALONE_NEW);
		}

		return wrapper;
	}


	/**
	 *
	 */
	public Event initAction(RequestContext context) throws Exception
	{
		MutableAttributeMap rs = context.getRequestScope();
		rs.put(FORM_OBJECT_KEY, getFormObject(context));
        context.getExternalContext().getSessionMap().put(
                    DATASOURCE_TREE_DATA_PROVIDER, typedTreeDataProvider);

        context.getFlowScope().put(ATTRIBUTE_RESOURCE_ID_NOT_SUPPORTED_SYMBOLS,
                configuration.getResourceIdNotSupportedSymbols());

		return success();
	}


	/**
	 *
	 */
	public Event saveQuery(RequestContext context) throws Exception
	{
		QueryWrapper wrapper = (QueryWrapper) getFormObject(context);
		if (wrapper.isStandAloneMode()) {
			try {
				repository.saveResource(null, wrapper.getQuery());
                if (!wrapper.isEditMode()) {
                    context.getExternalContext().getSessionMap().put("repositorySystemConfirm",
                            messages.getMessage("resource.query.queryAdded",
                                    new String[] {wrapper.getQuery().getName(),
                                    wrapper.getQuery().getParentFolder()},
                                    LocaleContextHolder.getLocale()));
                }
				return yes();
			} catch (JSDuplicateResourceException e) {
				getFormErrors(context).rejectValue("query.name", "QueryValidator.error.duplicate");
				return error();
			}
		}
		return success();
	}


	/**
	 *
	 */
	public Event locateDataSource(RequestContext context) throws Exception {
		//log("In locate data source");
		QueryWrapper queryWrapper = (QueryWrapper) getFormObject(context);
		ResourceReference dsRef = queryWrapper.getQuery().getDataSource();
		ReportDataSourceWrapper rdWrapper = new ReportDataSourceWrapper();
		rdWrapper.setParentType(DATA_SOURCE_PARENT_TYPE);
		rdWrapper.setMode(BaseDTO.MODE_SUB_FLOW_NEW);
		if (dsRef == null) {
			//log("Found no previous ReportDataSource, creating new");
//			ReportDataSource ds = (ReportDataSource) repository.newResource(
//					null, JdbcReportDataSource.class);
//			dsRef = new ResourceReference(ds);
//			rdWrapper.setSource(constants.getFieldChoiceLocal());
//			rdWrapper.setType(constants.getJDBCDatasourceType());
//			rdWrapper.setReportDataSource(ds);

			rdWrapper.setSource(constants.getFieldChoiceNone());
			rdWrapper.setReportDataSource(null);

		} else {
			// if the dataSource exists decide source and type and set in
			// wrapper
			if (dsRef.isLocal()) {
				rdWrapper.setSource(constants.getFieldChoiceLocal());
				ReportDataSource ds = (ReportDataSource) dsRef.getLocalResource();
                final String editedDataSourceJson = (String) context.getFlowScope().get(ReportDataSourceWrapper.ATTRIBUTE_DATA_SOURCE_JSON);
                if(editedDataSourceJson != null){
                    rdWrapper.setDataSourceJson(editedDataSourceJson);
                } else {
                    rdWrapper.setDataSourceUri(ds.getURIString());
                }
				if (JdbcReportDataSource.class.isAssignableFrom(ds.getClass())) {
					//log("Found JDBCReportDataSource");
					rdWrapper.setType(constants.getJDBCDatasourceType());
				} else if (JndiJdbcReportDataSource.class.isAssignableFrom(ds.getClass())) {
						rdWrapper.setType(constants.getJNDIDatasourceType());
				} else if (VirtualReportDataSource.class.isAssignableFrom(ds.getClass())) {
						rdWrapper.setType(constants.getVirtualDatasourceType());
                } else if (BeanReportDataSource.class.isAssignableFrom(ds.getClass())) {
					rdWrapper.setType(constants.getBeanDatasourceType());
				} else if (ds instanceof CustomReportDataSource) {
                    CustomReportDataSource cds = (CustomReportDataSource) ds;
                    // look up definition & use it to init defaults & set prop defs
                    CustomDataSourceDefinition customDef = customDataSourceFactory.getDefinition(cds);
                    rdWrapper.setType(customDef.getName());
				}
                rdWrapper.setMode(BaseDTO.MODE_SUB_FLOW_EDIT);
				rdWrapper.setReportDataSource(ds);
			} else {
				// DataSource object is a lookup
				//log("Found ReportDataSourceLookup");
				rdWrapper.setSource(constants.getFieldChoiceRepo());
				rdWrapper.setSelectedUri(dsRef.getReferenceURI());
			}
		}
		// Set the object into scope with the name that the reportDataSourceFlow
		// can pickup
		context.getFlowScope().put(DATASOURCE_OBJECT_KEY, rdWrapper);
		return success();
	}


	/**
	 *
	 */
	public Event saveDatasourceFromLocate(RequestContext context) throws Exception {
		ReportDataSourceWrapper resource = (ReportDataSourceWrapper) context
				.getFlowScope().get(DATASOURCE_OBJECT_KEY);
		QueryWrapper queryWrapper = (QueryWrapper) getFormObject(context);
		if (resource.getSource().equals(constants.getFieldChoiceRepo())) {
			queryWrapper.getQuery().setDataSourceReference(
					resource.getSelectedUri());
		} else if (resource.getSource().equals(constants.getFieldChoiceNone())) {
			queryWrapper.getQuery().setDataSource((ResourceReference) null);
    	}
		return success();
	}

    public Event saveDatasourceFromSubflow(RequestContext context) throws Exception {
        ReportDataSourceWrapper resource = (ReportDataSourceWrapper) context
                .getFlowScope().get(DATASOURCE_OBJECT_KEY);
        QueryWrapper queryWrapper = (QueryWrapper) getFormObject(context);
        final ParameterMap requestParameters = context.getRequestParameters();
        final String dataSourceJson = requestParameters.get("dataSourceJson");
        final String dataSourceType = requestParameters.get("dataSourceType");
        final ReportDataSource reportDataSource;
        if(dataSourceJson != null && dataSourceType != null){
            // save localDataSource JSON to send it back to the client if the data source page is opened again.
            context.getFlowScope().put(ReportDataSourceWrapper.ATTRIBUTE_DATA_SOURCE_JSON, dataSourceJson);
            reportDataSource = dataSourceJsonHelper.parse(dataSourceJson, dataSourceType);
            reportDataSource.setURIString(queryWrapper.getQuery().getURIString() + "_files");
        } else {
            reportDataSource = resource.getReportDataSource();
        }
        queryWrapper.getQuery().setDataSource(reportDataSource);
        return success();
    }

	public Event prepareQueryTextEdit(RequestContext context) {
		context.getRequestScope().put(getQueryLanguagesRequestAttrName(), getQueryLanguages());
		return success();
	}


	public Event validateQuery(RequestContext context) throws Exception 
	{
		Errors errors = getFormErrors(context);
		
		QueryWrapper wrapper = (QueryWrapper)getFormObject(context);
		
		getValidator().validate(wrapper, errors);
		
		List fieldErrors = errors.getFieldErrors();
		if (fieldErrors != null && !fieldErrors.isEmpty())
		{
			FieldError error = (FieldError)fieldErrors.get(0);
			String field = error.getField();
			
			if (
				"query.name".equals(field)
				|| "query.label".equals(field)
				|| "query.description".equals(field)
				)
			{
				return result("editQueryForm");
			} 
			else if ("query.sql".equals(field))
			{
				return result("editQueryTextForm");
			}
		}

		return success();
	}


	public String[] getQueryLanguages() {
		return queryLanguages;
	}

	public void setQueryLanguages(String[] queryLanguages) {
		this.queryLanguages = queryLanguages;
	}

	public String getQueryLanguagesRequestAttrName() {
		return queryLanguagesRequestAttrName;
	}

	public void setQueryLanguagesRequestAttrName(
			String queryLanguagesRequestAttrName) {
		this.queryLanguagesRequestAttrName = queryLanguagesRequestAttrName;
	}

    public void setConfiguration(RepositoryConfiguration configuration) {
        this.configuration = configuration;
    }
}
