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

import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.util.StaticExecutionContextProvider;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.search.common.ResourceDetails;
import com.jaspersoft.jasperserver.search.service.ResourceService;
import com.jaspersoft.jasperserver.search.util.JSONConverter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.Assert;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;
import java.util.Set;

/**
 */
public class ResourceAction extends BaseSearchAction {
    protected final Log log = LogFactory.getLog(this.getClass());

    public static final String AJAX_RESPONSE_MODEL = "ajaxResponseModel";

    private ResourceService resourceService;

    public Event init(RequestContext context) throws Exception {
        Assert.notNull(resourceService,"ResourceService must be configured");

        return success();
    }

    public Event update(RequestContext context) throws Exception {
        JSONConverter jsonConverter = getConverter(context);
        JSONObject response;
        try {
            Resource r = jsonConverter.jsonToResource(getParameter(context, PARAMETER_SELECTED_RESOURCE));
            ResourceDetails resource = resourceService.update(r.getURIString(), r.getLabel(), r.getDescription());

            response = jsonConverter.createJSONResponse(jsonConverter.resourceToJson(resource));
        } catch (Exception e) {
            response = jsonConverter.createErrorJSONResponse(getErrorMessage(e));
            log.debug("SEARCH_ERROR: Can't update resource properties.", e);
        }
        context.getRequestScope().put(AJAX_RESPONSE_MODEL, response.toString());

        return success();
    }

    public Event delete(RequestContext context) throws Exception {
        JSONConverter jsonConverter = getConverter(context);
        JSONObject response;
        try {
            List<Resource> selectedResources = jsonConverter.jsonArrayToResources(
                    getParameter(context, PARAMETER_SELECTED_RESOURCES));

            if (CollectionUtils.isEmpty(selectedResources)) {
                throw new JSException("No resources to delete.");
            }

            List<ResourceDetails> dependentResources = resourceService.check(exContext(context), selectedResources);

            if (dependentResources.isEmpty()) {
                resourceService.delete(selectedResources);
                response = jsonConverter.deleteResourcesJSONResponse(selectedResources);
            } else {
                response = jsonConverter.dependentResourcesJSONResponse(dependentResources);
            }
        } catch (JSExceptionWrapper e) {
            response = jsonConverter.createErrorJSONResponse(messages.getMessage("jsp.flowRemoveError.errorMsg2", null,
                    LocaleContextHolder.getLocale()));
            log.debug("SEARCH_ERROR: Can't delete resource.", e);
        } catch (Exception e) {
            response = jsonConverter.createErrorJSONResponse(getErrorMessage(e));
            log.debug("SEARCH_ERROR: Can't delete resource.", e);
        }
        context.getRequestScope().put(AJAX_RESPONSE_MODEL, response.toString());

        return success();
    }

    public Event copy(RequestContext context) throws Exception {
        JSONConverter jsonConverter = getConverter(context);
        JSONObject response;
        try {
            Set<String> selectedResources = jsonConverter.jsonStringArrayToSet(
                    getParameter(context, PARAMETER_SELECTED_RESOURCES));
            String folder = getParameter(context, PARAMETER_DEST_FOLDER_URI);

            resourceService.copy(selectedResources, folder);

            response = jsonConverter.createOKJSONResponse("Resources copied successfully.");
        } catch (Exception e) {
            response = jsonConverter.createErrorJSONResponse(getErrorMessage(e));
            log.debug("SEARCH_ERROR: Can't copy resource.", e);
        }
        context.getRequestScope().put(AJAX_RESPONSE_MODEL, response.toString());

        return success();
    }

    public Event move(RequestContext context) throws Exception {
        JSONConverter jsonConverter = getConverter(context);
        JSONObject response;
        try {
            Set<String> selectedResources = jsonConverter.jsonStringArrayToSet(
                    getParameter(context, PARAMETER_SELECTED_RESOURCES));
            String folder = getParameter(context, PARAMETER_DEST_FOLDER_URI);

            resourceService.move(selectedResources, folder);

            response = jsonConverter.createOKJSONResponse("Resources moved successfully.");
        } catch (Exception e) {
            response = jsonConverter.createErrorJSONResponse(getErrorMessage(e));
            log.debug("SEARCH_ERROR: Can't move resource.", e);
        }
        context.getRequestScope().put(AJAX_RESPONSE_MODEL, response.toString());

        return success();
    }

    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    protected ExecutionContext exContext(RequestContext context) {
        return StaticExecutionContextProvider.getExecutionContext();
    }
}