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
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.CalendarFormatProvider;
import com.jaspersoft.jasperserver.api.metadata.common.domain.DataType;
import com.jaspersoft.jasperserver.api.metadata.common.domain.RepositoryConfiguration;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceLookup;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.view.domain.FilterCriteria;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlValidationException;
import com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.converters.DataConverterService;
import com.jaspersoft.jasperserver.war.common.JasperServerUtil;
import com.jaspersoft.jasperserver.war.dto.BaseDTO;
import com.jaspersoft.jasperserver.war.dto.DataTypeWrapper;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.DataBinder;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ScopeType;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * @author Ionut Nedelcu (ionutned@users.sourceforge.net)
 * @version $Id$
 */
public class EditDataTypeAction extends FormAction {
    private static final String ATTRIBUTE_RESOURCE_ID_NOT_SUPPORTED_SYMBOLS = "resourceIdNotSupportedSymbols";

	private static final String FORM_OBJECT_KEY = "dataType";
	private static final String PARENT_FOLDER_ATTR = "parentFolder";
	private static final String CURRENT_DATATYPE_ATTR = "currentDataType";
	private static final String IS_EDIT = "isEdit";//FIXME use wrapper to disable name in UI

	private RepositoryService repository;
    private DataConverterService dataConverterService;
    @Resource(name = "isoCalendarFormatProvider")
    protected CalendarFormatProvider isoCalendarFormatProvider;
    @Resource(name = "messagesCalendarFormatProvider")
    protected CalendarFormatProvider messagesCalendarFormatProvider;

    protected MessageSource messages;
    private RepositoryConfiguration configuration;

	public RepositoryService getRepository() {
		return repository;
	}

    public void setDataConverterService(DataConverterService dataConverterService) {
        this.dataConverterService = dataConverterService;
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
				"dataType.name",
				"dataType.label",
				"dataType.description",
				"dataType.dataTypeType",
				"dataType.regularExpr",
				"minValueText",
				"maxValueText",
				"dataType.minValue",
				"dataType.maxValue",
				"dataType.strictMin",
				"dataType.strictMax"
		);
	}


	/**
	 *
	 */
	public EditDataTypeAction(){
		setFormObjectClass(DataTypeWrapper.class); //custom form backing object class
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
		DataType dataType;
		DataTypeWrapper wrapper;
		ExecutionContextImpl executionContext = new ExecutionContextImpl();

		String isEdit = (String)context.getFlowScope().get(IS_EDIT);
		if (isEdit == null) {
			isEdit = (String)context.getRequestParameters().get("isEdit");
			context.getFlowScope().put(IS_EDIT, isEdit);
		}
		
		if (isEdit != null)
		{
			String currentDataType = (String) context.getFlowScope().get(CURRENT_DATATYPE_ATTR);
			if (currentDataType == null) {
				currentDataType = (String)context.getRequestParameters().get("resource");
				context.getFlowScope().put(CURRENT_DATATYPE_ATTR, currentDataType);
			}
			dataType = (DataType) repository.getResource(executionContext, currentDataType);
			if(dataType == null){
				context.getFlowScope().remove("prevForm");
				throw new JSException("jsexception.could.not.find.resource.with.uri", new Object[] {currentDataType});
			}
			wrapper = new DataTypeWrapper(dataType);
			wrapper.setMode(BaseDTO.MODE_STAND_ALONE_EDIT);
			byte type = dataType.getDataTypeType();
			if (JasperServerUtil.isDateType(type)) {
				DateFormat df = getFormat(type);
				if(dataType.getMinValue() != null){
					wrapper.setMinValueText(df.format((Date)dataType.getMinValue()));
				}
				if(dataType.getMaxValue() != null){
					wrapper.setMaxValueText(df.format((Date)dataType.getMaxValue()));
				}
			}
		}
		else
		{
			dataType = (DataType) repository.newResource(executionContext, DataType.class);
			String parentFolder = (String) context.getFlowScope().get(PARENT_FOLDER_ATTR);
			if (parentFolder == null) {
				parentFolder = (String)context.getRequestParameters().get("ParentFolderUri");
				context.getFlowScope().put(PARENT_FOLDER_ATTR, parentFolder);
			}
			if (parentFolder == null || parentFolder.trim().length() == 0)
				parentFolder = "/";
			dataType.setParentFolder(parentFolder);
			wrapper = new DataTypeWrapper(dataType);
			wrapper.setMode(BaseDTO.MODE_STAND_ALONE_NEW);

			FilterCriteria criteria = FilterCriteria.createFilter();
			criteria.addFilterElement(FilterCriteria.createParentFolderFilter(parentFolder));
			ResourceLookup[] allDataTypes = repository.findResource(executionContext, criteria);
			wrapper.setAllDataTypes(allDataTypes);
		}

		return wrapper;
	}


	/**
	 *
	 */
	public Event saveDataType(RequestContext context) throws Exception
	{
		final DataTypeWrapper wrapper = (DataTypeWrapper) getFormObject(context);

        final DataType dataType = wrapper.getDataType();
        byte type = dataType.getDataTypeType();
        if (type != DataType.TYPE_TEXT && type != DataType.TYPE_NUMBER) {
            dataType.setMaxValue(convertRestrictionValue(wrapper.getMaxValueText(), dataType));
            dataType.setMinValue(convertRestrictionValue(wrapper.getMinValueText(), dataType));
        }

        if (wrapper.isStandAloneMode())
			try {
				repository.saveResource(null, wrapper.getDataType());
                if (!wrapper.isEditMode()) {
                    context.getExternalContext().getSessionMap().put("repositorySystemConfirm",
                            messages.getMessage("resource.dataType.dataTypeAdded",
                                    new String[] {wrapper.getDataType().getName(),
                                    wrapper.getDataType().getParentFolder()},
                                    LocaleContextHolder.getLocale()));
                }
				return yes();
			}
			catch (JSDuplicateResourceException e) {
				getFormErrors(context).rejectValue("dataType.name", "DataTypeValidator.error.duplicate");
				return error();
			}

		return success();
	}

	/**
	 *
	 */
	public Event setupEditForm(RequestContext context) throws Exception {
		MutableAttributeMap rs = context.getRequestScope();
		rs.put(FORM_OBJECT_KEY, getFormObject(context));

        context.getFlowScope().put(ATTRIBUTE_RESOURCE_ID_NOT_SUPPORTED_SYMBOLS,
                configuration.getResourceIdNotSupportedSymbols());

		return success();
	}

    public Event resetEditFormAndBindType(RequestContext context) throws Exception {
        MutableAttributeMap rs = context.getRequestScope();

        DataTypeWrapper wrapper = (DataTypeWrapper) getFormObject(context);
        DataType dataType = wrapper.getDataType();

        byte newDataType = dataType.getDataTypeType();
        resetDatatype(dataType);
        dataType.setDataTypeType(newDataType);

        rs.put(FORM_OBJECT_KEY, wrapper);

        return success();
    }

    private void resetDatatype(DataType dataType){
        dataType.setDataTypeType(DataType.TYPE_TEXT);
        dataType.setDecimals(null);
        dataType.setMaxLength(null);
        dataType.setMaxValue(null);
        dataType.setMinValue(null);
        dataType.setRegularExpr(null);
        dataType.setStrictMax(false);
        dataType.setStrictMin(false);
    }

	private DateFormat getFormat(byte type) {
        switch (type) {
            case DataType.TYPE_DATE:
                return messagesCalendarFormatProvider.getDateFormat();
            case DataType.TYPE_DATE_TIME:
                return messagesCalendarFormatProvider.getDatetimeFormat();
            case DataType.TYPE_TIME:
                return messagesCalendarFormatProvider.getTimeFormat();
            default:
                return messagesCalendarFormatProvider.getDateFormat();
        }
    }

private Comparable convertRestrictionValue(String restrictionValueString, DataType dataType) throws InputControlValidationException, ParseException {
        Comparable result = null;
        if (restrictionValueString != null && !"".equals(restrictionValueString)) {
            final String valueToConvert;
            switch (dataType.getDataTypeType()) {
                case DataType.TYPE_DATE: {
                    // convert localized date string to ISO8601
                    valueToConvert = isoCalendarFormatProvider.getDateFormat()
                            .format(messagesCalendarFormatProvider.getDateFormat().parse(restrictionValueString));
                }
                break;
                case DataType.TYPE_TIME: {
                    // convert localized time string to ISO8601
                    valueToConvert = isoCalendarFormatProvider.getTimeFormat()
                            .format(messagesCalendarFormatProvider.getTimeFormat().parse(restrictionValueString));
                }
                break;
                case DataType.TYPE_DATE_TIME: {
                    // convert localized dateTime string to ISO8601
                    valueToConvert = isoCalendarFormatProvider.getDatetimeFormat()
                            .format(messagesCalendarFormatProvider.getDatetimeFormat().parse(restrictionValueString));
                }
                break;
                default:
                    valueToConvert = restrictionValueString;
            }
            result = (Comparable) dataConverterService.convertSingleValue(valueToConvert, dataType);
        }
        return result;
    }
}
