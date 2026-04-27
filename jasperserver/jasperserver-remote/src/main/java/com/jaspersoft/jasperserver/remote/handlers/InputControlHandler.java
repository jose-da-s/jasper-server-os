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
package com.jaspersoft.jasperserver.remote.handlers;

import com.jaspersoft.jasperserver.api.engine.common.service.EngineService;
import com.jaspersoft.jasperserver.api.metadata.common.domain.DataType;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ListOfValues;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Query;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.Argument;
import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.InputControlQueryDataRow;
import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.ResourceDescriptor;
import com.jaspersoft.jasperserver.remote.ServiceException;
import net.sf.jasperreports.engine.JRParameter;
import org.apache.commons.collections.OrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author gtoffoli
 * @version $Id$
 */
@Service
public class InputControlHandler extends RepositoryResourceHandler {

    @jakarta.annotation.Resource
    private EngineService engineService;

    public void setEngineService(EngineService engineService) {
        this.engineService = engineService;
    }

    public Class getResourceType() {
        return InputControl.class;
    }
    protected static final Log log = LogFactory.getLog(InputControlHandler.class);

    protected void doGet(Resource resource, ResourceDescriptor descriptor, Map options) throws ServiceException
    {
        InputControl fileResource = (InputControl) resource;
        descriptor.setWsType(ResourceDescriptor.TYPE_INPUT_CONTROL);
        descriptor.setHasData(false);
        descriptor.setIsReference(false);

        descriptor.setMandatory(fileResource.isMandatory());
        descriptor.setReadOnly(fileResource.isReadOnly());
        descriptor.setVisible(fileResource.isVisible());

        descriptor.setControlType(fileResource.getInputControlType());

        ResourceReference childReference = null;
        if (descriptor.getControlType() == ResourceDescriptor.IC_TYPE_SINGLE_VALUE) {
            childReference = fileResource.getDataType();
        } else if (descriptor.getControlType() == ResourceDescriptor.IC_TYPE_SINGLE_SELECT_LIST_OF_VALUES
                || descriptor.getControlType() == ResourceDescriptor.IC_TYPE_SINGLE_SELECT_LIST_OF_VALUES_RADIO
                || descriptor.getControlType() == ResourceDescriptor.IC_TYPE_MULTI_SELECT_LIST_OF_VALUES
                || descriptor.getControlType() == ResourceDescriptor.IC_TYPE_MULTI_SELECT_LIST_OF_VALUES_CHECKBOX) {
            childReference = fileResource.getListOfValues();
        } else if (descriptor.getControlType() == ResourceDescriptor.IC_TYPE_SINGLE_SELECT_QUERY
                || descriptor.getControlType() == ResourceDescriptor.IC_TYPE_SINGLE_SELECT_QUERY_RADIO
                || descriptor.getControlType() == ResourceDescriptor.IC_TYPE_MULTI_SELECT_QUERY
                || descriptor.getControlType() == ResourceDescriptor.IC_TYPE_MULTI_SELECT_QUERY_CHECKBOX) {

            childReference = fileResource.getQuery();
            descriptor.setQueryValueColumn(fileResource.getQueryValueColumn());
            descriptor.setQueryVisibleColumns(fileResource.getQueryVisibleColumns());
        }

        if (childReference != null) {
            ResourceDescriptor childRd = null;

            if (childReference.isLocal()) {
                childRd = getResourcesManagementRemoteService().createResourceDescriptor(childReference.getLocalResource());
            } else {
                // childRd =
                // getResourceDescriptor(childReference.getReferenceLookup().getURIString());
                childRd = new ResourceDescriptor();
                childRd.setWsType(ResourceDescriptor.TYPE_REFERENCE);
                childRd.setReferenceUri(childReference.getReferenceURI());
            }

            if (childRd != null) {
                descriptor.getChildren().add(childRd);
            }
        }

        if (options != null
                && options.containsKey(Argument.IC_GET_QUERY_DATA)) {
            // get the datasource against wich get the query data...
            String dsUri = (String) options.get(Argument.IC_GET_QUERY_DATA);
            if ("null".equalsIgnoreCase(dsUri))
            {
                dsUri = null;
            }

            java.util.List data = getInputControlItems(fileResource, dsUri, (Map)options.get(Argument.PARAMS_ARG));

            descriptor.setQueryData(data);
        }
    }

    /**
     * execute a query against a named datasource to get the data for an
     * InputControl. Return a list of InputControlQueryDataRow
     *
     * @param control
     * @param datasourceUri
     * @param params
     * @return
     * @throws ServiceException
     */
    protected java.util.List getInputControlItems(InputControl control, String datasourceUri, Map params) throws ServiceException {

        ResourceReference fallbackDataSource = null;
        if (datasourceUri != null && datasourceUri.trim().length() > 0) {
            fallbackDataSource = new ResourceReference(datasourceUri);
        }

        ResourceReference queryRef = control.getQuery();

        String valueColumn = control.getQueryValueColumn();
        String[] visibleColumns = control.getQueryVisibleColumns();

        Map parameters = (params == null) ? new HashMap() : params;
        // TODO : read REPORT_MAX_COUNT from configuration
        parameters.put(JRParameter.REPORT_MAX_COUNT, 100000);

        OrderedMap results = engineService.executeQuery(null,
                queryRef, valueColumn, visibleColumns, fallbackDataSource,
                parameters);

        List rows;
        if (results == null || results.isEmpty()) {
            rows = new ArrayList(0);
        } else {
            rows = new ArrayList(results.size());
            for (Iterator it = results.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                Object keyValue = entry.getKey();
                String[] columnValues = (String[]) entry.getValue();

                InputControlQueryDataRow ic = new InputControlQueryDataRow();
                ic.setValue(keyValue);

                for (int i = 0; i < columnValues.length; i++) {
                    String value = columnValues[i];
                    ic.getColumnValues().add((value != null) ? value : "");
                }
                rows.add(ic);
            }
        }

        return rows;
    }

    @Override
    protected void updateResource(Resource resource, ResourceDescriptor descriptor, Map options) throws ServiceException {

        super.updateResource(resource, descriptor, options);

        InputControl inputControl = (InputControl) resource;

        inputControl.setMandatory(descriptor.isMandatory());
        inputControl.setReadOnly(descriptor.isReadOnly());
        inputControl.setVisible(descriptor.isVisible());

        inputControl.setInputControlType(descriptor.getControlType());

        // Look in children...
        if (inputControl.getInputControlType() == ResourceDescriptor.IC_TYPE_SINGLE_VALUE) {
            // We have to set the datatype...
            if (descriptor.getChildren().isEmpty()) {
                throw new ServiceException(ServiceException.GENERAL_ERROR2,
                        getMessageSource().getMessage(
                                "webservices.error.missingDataType", new Object[]{}, LocaleContextHolder.getLocale()));
            }
            ResourceDescriptor rd = (ResourceDescriptor) descriptor.getChildren().get(0);
            if (rd.getWsType().equals(ResourceDescriptor.TYPE_REFERENCE)) {
                inputControl.setDataTypeReference(rd.getReferenceUri());
            } else {
                DataType dataType = (DataType) createChildResource(rd);
                inputControl.setDataType(dataType);
            }
        } // Look in children...
        else if (inputControl.getInputControlType() == ResourceDescriptor.IC_TYPE_SINGLE_SELECT_LIST_OF_VALUES
                || inputControl.getInputControlType() == ResourceDescriptor.IC_TYPE_SINGLE_SELECT_LIST_OF_VALUES_RADIO
                || inputControl.getInputControlType() == ResourceDescriptor.IC_TYPE_MULTI_SELECT_LIST_OF_VALUES
                || inputControl.getInputControlType() == ResourceDescriptor.IC_TYPE_MULTI_SELECT_LIST_OF_VALUES_CHECKBOX) {

            // We have to set the datatype...
            if (descriptor.getChildren().isEmpty()) {
                throw new ServiceException(ServiceException.GENERAL_ERROR2,
                        getMessageSource().getMessage(
                                "webservices.error.missingLOV", new Object[]{}, LocaleContextHolder.getLocale()));
            }
            ResourceDescriptor rd = (ResourceDescriptor) descriptor.getChildren().get(0);
            if (rd.getWsType().equals(ResourceDescriptor.TYPE_REFERENCE)) {
                inputControl.setListOfValuesReference(rd.getReferenceUri());
            } else {
                ListOfValues lovResource = (ListOfValues) createChildResource(rd);
                inputControl.setListOfValues(lovResource);
            }
        } else if (inputControl.getInputControlType() == ResourceDescriptor.IC_TYPE_SINGLE_SELECT_QUERY
                || inputControl.getInputControlType() == ResourceDescriptor.IC_TYPE_SINGLE_SELECT_QUERY_RADIO
                || inputControl.getInputControlType() == ResourceDescriptor.IC_TYPE_MULTI_SELECT_QUERY
                || inputControl.getInputControlType() == ResourceDescriptor.IC_TYPE_MULTI_SELECT_QUERY_CHECKBOX) {

            // We have to set the datatype...
            if (descriptor.getChildren().isEmpty()) {
                throw new ServiceException(ServiceException.GENERAL_ERROR2,
                        getMessageSource().getMessage(
                                "webservices.error.missingQuery", new Object[]{}, LocaleContextHolder.getLocale()));
            }
            ResourceDescriptor rd = (ResourceDescriptor) descriptor.getChildren().get(0);

            inputControl.setQueryValueColumn(descriptor.getQueryValueColumn());
            String[] visibleColumns = descriptor.getQueryVisibleColumns();

            // remove all old visible columns first...
            String[] oldColumns = inputControl.getQueryVisibleColumns();
            if (oldColumns != null) {
                for (int i = 0; i < oldColumns.length; ++i) {
                    inputControl.removeQueryVisibleColumn(oldColumns[i]);
                }
            }

            for (int i = 0; i < visibleColumns.length; ++i) {
                inputControl.addQueryVisibleColumn(visibleColumns[i]);
            }

            if (rd.getWsType().equals(ResourceDescriptor.TYPE_REFERENCE)) {
                inputControl.setQueryReference(rd.getReferenceUri());
            } else {
                Query queryResource = (Query) createChildResource(rd);
                inputControl.setQuery(queryResource);
            }
        }
    }


    
}

