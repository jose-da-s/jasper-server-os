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

import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.search.service.FolderService;
import com.jaspersoft.jasperserver.search.util.JSONConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.Assert;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 */
public class FolderAction extends BaseSearchAction {
    protected final Log log = LogFactory.getLog(this.getClass());

    public static final String AJAX_RESPONSE_MODEL = "ajaxResponseModel";

    private FolderService folderService;

    public Event init(RequestContext context) throws Exception {
        Assert.notNull(folderService,"FolderService must be configured");

        return success();
    }

    public Event create(RequestContext context) throws Exception {
        JSONConverter jsonConverter = getConverter(context);
        JSONObject response;
        try {
            String destinationFolderUri = getParameter(context, PARAMETER_DEST_FOLDER_URI);
            Folder folder = jsonConverter.jsonToFolder(getParameter(context, PARAMETER_FOLDER));

            Folder newFolder = folderService.create(destinationFolderUri, folder.getLabel(), folder.getDescription());

            response = jsonConverter.createJSONResponse(jsonConverter.folderToJson(newFolder));
        } catch (Exception e) {
            response = jsonConverter.createErrorJSONResponse(getErrorMessage(e));
            log.debug("SEARCH_ERROR: Can't create folder.", e);
        }
        context.getRequestScope().put(AJAX_RESPONSE_MODEL, response.toString());

        return success();
    }

    public Event update(RequestContext context) throws Exception {
        JSONConverter jsonConverter = getConverter(context);
        JSONObject response;
        try {
            Folder folder = jsonConverter.jsonToFolder(getParameter(context, PARAMETER_FOLDER));

            Folder newFolder = folderService.update(folder.getURIString(), folder.getLabel(), folder.getDescription());

            response = jsonConverter.createJSONResponse(jsonConverter.folderToJson(newFolder));
        } catch (Exception e) {
            response = jsonConverter.createErrorJSONResponse(getErrorMessage(e));
            log.debug("SEARCH_ERROR: Can't update folder.", e);
        }
        context.getRequestScope().put(AJAX_RESPONSE_MODEL, response.toString());

        return success();
    }

    public Event delete(RequestContext context) throws Exception {
        JSONConverter jsonConverter = getConverter(context);
        JSONObject response;
        try {
            String folderUri = getParameter(context, PARAMETER_SOURCE_FOLDER_URI);

            folderService.delete(folderUri);

            response = jsonConverter.createOKJSONResponse("Folder deleted successfully.");
        } catch (JSExceptionWrapper e) {
            response = jsonConverter.createErrorJSONResponse(messages.getMessage("jsp.flowRemoveError.errorMsg3", null,
                    LocaleContextHolder.getLocale()));
            log.debug("SEARCH_ERROR: Can't delete folder.", e);
        } catch (Exception e) {
            response = jsonConverter.createErrorJSONResponse(getErrorMessage(e));
            log.debug("SEARCH_ERROR: Can't delete folder.", e);
        }
        context.getRequestScope().put(AJAX_RESPONSE_MODEL, response.toString());

        return success();
    }


    public Event copy(RequestContext context) throws Exception {
        JSONConverter jsonConverter = getConverter(context);
        JSONObject response;
        try {
            String folderUri = getParameter(context, PARAMETER_SOURCE_FOLDER_URI);
            String destinationFolderUri = getParameter(context, PARAMETER_DEST_FOLDER_URI);

            folderService.copy(folderUri, destinationFolderUri);

            response = jsonConverter.createOKJSONResponse("Folder copied successfully.");
        } catch (Exception e) {
            response = jsonConverter.createErrorJSONResponse(getErrorMessage(e));
            log.debug("SEARCH_ERROR: Can't copy folder.", e);
        }
        context.getRequestScope().put(AJAX_RESPONSE_MODEL, response.toString());

        return success();
    }

    public Event move(RequestContext context) throws Exception {
        JSONConverter jsonConverter = getConverter(context);
        JSONObject response;
        try {
            String folderUri = getParameter(context, PARAMETER_SOURCE_FOLDER_URI);
            String destinationFolderUri = getParameter(context, PARAMETER_DEST_FOLDER_URI);

            folderService.move(folderUri, destinationFolderUri);

            response = jsonConverter.createOKJSONResponse("Folder moved successfully.");
        } catch (Exception e) {
            response = jsonConverter.createErrorJSONResponse(getErrorMessage(e));
            log.debug("SEARCH_ERROR: Can't move folder.", e);
        }
        context.getRequestScope().put(AJAX_RESPONSE_MODEL, response.toString());

        return success();
    }

    public void setFolderService(FolderService folderService) {
        this.folderService = folderService;
    }
}