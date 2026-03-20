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

package com.jaspersoft.jasperserver.search.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jaspersoft.jasperserver.core.util.JSONUtil;
import com.jaspersoft.jasperserver.search.common.ItemsExistException;
import com.jaspersoft.jasperserver.search.common.RepositorySearchConfiguration;
import com.jaspersoft.jasperserver.search.mode.SearchMode;
import com.jaspersoft.jasperserver.search.mode.SearchModeSettingsResolver;
import com.jaspersoft.jasperserver.search.state.InitialStateResolver;
import com.jaspersoft.jasperserver.search.util.JSONConverter;
import com.jaspersoft.jasperserver.war.action.BaseFormAction;
import org.springframework.context.i18n.LocaleContextHolder;
import com.jaspersoft.jasperserver.api.JSException;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.core.collection.SharedAttributeMap;
import org.springframework.webflow.execution.RequestContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.Collection;

/**
 * Base search action handler.
 *
 * @author Yuriy Plakosh
 */
public class BaseSearchAction extends BaseFormAction implements Serializable {
    // Session attributes.
    private static final String ATTRIBUTE_SEARCH_HOLDER = "searchHolder";

    // Request parameters.
    public static final String PARAMETER_SOURCE_FOLDER_URI = "sourceFolderUri";
    public static final String PARAMETER_FOLDER = "folder";
    public static final String PARAMETER_SELECTED_RESOURCES = "selectedResources";
    public static final String PARAMETER_IGNORE_DEPENDENT_RESOURCES = "ignoreDependentResources";
    public static final String PARAMETER_DEST_FOLDER_URI = "destFolderUri";
    public static final String PARAMETER_SELECTED_RESOURCE = "selectedResource";
    private static final String PARAMETER_MODE = "mode";
    private static final String PARAMETER_LAST_MODE = "lastMode";

    private SearchModeSettingsResolver searchModeSettingsResolver;
    private ObjectMapper objectMapper = new ObjectMapper();

    public void setSearchModeSettingsResolver(SearchModeSettingsResolver searchModeSettingsResolver) {
        this.searchModeSettingsResolver = searchModeSettingsResolver;
    }

    protected JSONConverter getConverter(RequestContext context) {
        return searchModeSettingsResolver.getSettings(getMode(context)).getJsonConverter();
    }

    protected RepositorySearchConfiguration getConfiguration(RequestContext context) {
        return searchModeSettingsResolver.getSettings(getMode(context)).getRepositorySearchConfiguration();
    }

    protected InitialStateResolver getInitialStateResolver(RequestContext context) {
        return searchModeSettingsResolver.getSettings(getMode(context)).getInitialStateResolver();
    }

    protected String getErrorMessage(Exception e) {
        String errorMsg;
        if (e instanceof ItemsExistException) {
            Collection<String> items = ((ItemsExistException)e).getItems();
            ArrayNode itemsJArr = objectMapper.createArrayNode();
            for(String item: items) {
                itemsJArr.add(item);
            }
            ObjectNode msgObject = objectMapper.createObjectNode();
            msgObject.put("existingLabels", itemsJArr);
            return JSONUtil.toJSON(msgObject);
        } else if (e instanceof JSException) {
            JSException jse = (JSException)e;
            errorMsg = messages.getMessage(jse.getMessage(), jse.getArgs(), LocaleContextHolder.getLocale());
        } else {
            errorMsg = e.getMessage();
        }

        return errorMsg;
    }

    protected void initSearchHolder(RequestContext context) {
        // Creating search holder if necessary.
        SearchHolder searchHolder = getSearchHolder(context);
        if (searchHolder == null) {
            searchHolder = new SearchHolder();
            context.getExternalContext().getSessionMap().put(ATTRIBUTE_SEARCH_HOLDER, searchHolder);
        }
    }

    protected SearchHolder getSearchHolder(RequestContext context) {
        //SharedAttributeMap session = context.getExternalContext().getSessionMap();
        //return (SearchHolder)session.get(ATTRIBUTE_SEARCH_HOLDER);

        //Bug 38179
        HttpSession sess =((HttpServletRequest)context.getExternalContext().getNativeRequest()).getSession();
        return (SearchHolder)sess.getAttribute(ATTRIBUTE_SEARCH_HOLDER);
    }

    protected SearchMode getMode(RequestContext context) {
        ParameterMap map = context.getRequestParameters();

        SearchMode mode;
        String lastModeParameter = (String)map.get(PARAMETER_LAST_MODE, String.class);
        if (Boolean.parseBoolean(lastModeParameter)) {
            mode = getSearchHolder(context).getLastMode(); 
        } else {
            String modeParameter = (String)map.get(PARAMETER_MODE, String.class);

            mode = SearchMode.getMode(modeParameter);
        }

        return mode;
    }

    protected Object getParameter(RequestContext context, String name, Class clazz) {
        ParameterMap map = context.getRequestParameters();

        if (map.contains(name)) {
            return map.get(name, clazz);
        } else {
            return null;
        }
    }

    @Override
    protected void initState(RequestContext context) {
    }
}

