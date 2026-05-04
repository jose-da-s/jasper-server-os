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

package com.jaspersoft.jasperserver.war.themes.action;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.util.StaticExecutionContextProvider;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.user.domain.Tenant;
import com.jaspersoft.jasperserver.api.metadata.user.domain.TenantQualified;
import com.jaspersoft.jasperserver.api.metadata.user.service.TenantService;
import com.jaspersoft.jasperserver.war.themes.ThemeFolderExistsException;
import com.jaspersoft.jasperserver.war.themes.ThemeService;
import com.jaspersoft.jasperserver.war.util.JSONConverterBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author asokolnikov
 */
public class ThemeAction extends FormAction {

    public static final String AJAX_CONTENT_TYPE = "ajaxResponseContentType";
    public static final String AJAX_RESPONSE_MODEL = "ajaxResponseModel";
    public final static String DOWNLOAD_FILE_NAME = "downloadFileName";
    public final static String CONTENT_TYPE = "contentType";
    public final static String AJAX_ERROR = "ajaxerror";
    public static final String FOLDER_URI = "folderUri";
    public static final String FILE_CONTENT= "fileContent";
    public static final String SUCCESSFUL = "Successful";
    public static final String IGNORED = "Ignored";
    public static final String REFRESH = "refresh";
    public static final String THEMES_NOT_A_THEME = "repository.themes.notATheme";
    public static final String THEMES_CANNOT_DOWNLOAD = "repository.themes.cannotDownloadTheme";
    public static final String THEMES_CANNOT_UPLOAD = "repository.themes.cannotUploadTheme";

    protected final Log log = LogFactory.getLog(this.getClass());

    private TenantService tenantService;
    protected MessageSource messages;
    private ThemeService themeService;
    private ThemeResolver themeResolver;
    private RepositoryService repositoryService;

    public Event setActiveTheme(RequestContext context) throws Exception {

        String folderUri = context.getRequestParameters().get(FOLDER_URI);

        // check if it is a theme folder
        if (themeService.isThemeFolder(null, folderUri)) {
            String themeName = folderUri.substring(folderUri.lastIndexOf("/") + 1);
            Tenant tenant = tenantService.getTenantBasedOnRepositoryUri(null, folderUri);
            if (tenant == null) {
                tenant = tenantService.getTenant(null, TenantService.ORGANIZATIONS);
            }
            themeService.setActiveTheme(null, tenant.getId(), themeName);
            JSONObject ret = new JSONConverterBase().createOKJSONResponse(SUCCESSFUL);
            // clear cached theme if the theme change was for the current organization
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() != null && auth.getPrincipal() instanceof TenantQualified) {
                String userTenantId = ((TenantQualified) auth.getPrincipal()).getTenantId();
                if ( (userTenantId == null && tenant.getId().equals(TenantService.ORGANIZATIONS)) ||  tenant.getId().equals(userTenantId) ) {
                    themeResolver.setThemeName(
                            (HttpServletRequest) context.getExternalContext().getNativeRequest(),
                            (HttpServletResponse) context.getExternalContext().getNativeResponse(),
                            null);

                    // client needs to refresh
                    JSONObject data = new JSONObject();
                    data.put(REFRESH, true);
                    ret.put(JSONConverterBase.RESPONSE_DATA, data);
                }
            }
            context.getRequestScope().put(AJAX_RESPONSE_MODEL, ret.toString());
            return success();
        }

        JSONObject ret = new JSONConverterBase().createErrorJSONResponse(IGNORED);
        context.getRequestScope().put(AJAX_RESPONSE_MODEL, ret.toString());

        return success();
    }

    public Event downloadTheme(RequestContext context) throws Exception {

        String folderUri = context.getRequestParameters().get(FOLDER_URI);
        if (folderUri == null) {
            log.error("cannot download theme : folderURI is null");
            String errorMsg = messages.getMessage(THEMES_CANNOT_DOWNLOAD, new Object[] { "folderURI is null" }, LocaleContextHolder.getLocale());
            JSONObject ret = new JSONConverterBase().createErrorJSONResponse(errorMsg);
            context.getRequestScope().put(AJAX_RESPONSE_MODEL, ret.toString());
            return error();
        }
        // check if it is a theme folder
        if (themeService.isThemeFolder(null, folderUri)) {

            try {
                String themeName = folderUri.substring(folderUri.lastIndexOf("/") + 1);
                ExecutionContext executionContext = StaticExecutionContextProvider.getExecutionContext();
                byte[] buffer = themeService.getZipedTheme(executionContext, folderUri);

                context.getRequestScope().put(DOWNLOAD_FILE_NAME, themeName + ".zip");
                context.getRequestScope().put(AJAX_RESPONSE_MODEL, buffer);
                context.getRequestScope().put(CONTENT_TYPE, "application/zip");

                return success();
            } catch (Exception ex) {
                log.error("cannot download theme", ex);
                String errorMsg = messages.getMessage(THEMES_CANNOT_DOWNLOAD, new Object[] { ex.getMessage() }, LocaleContextHolder.getLocale());
//                context.getRequestScope().put(AJAX_ERROR, errorMsg);
                JSONObject ret = new JSONConverterBase().createErrorJSONResponse(errorMsg);
                context.getRequestScope().put(AJAX_RESPONSE_MODEL, ret.toString());
                return error();
            }
        }

        String errorMsg = messages.getMessage(THEMES_NOT_A_THEME, null, LocaleContextHolder.getLocale());
//        context.getRequestScope().put(AJAX_ERROR, errorMsg);
        JSONObject ret = new JSONConverterBase().createErrorJSONResponse(errorMsg);
        context.getRequestScope().put(AJAX_RESPONSE_MODEL, ret.toString());
        return error();
    }

    public Event reuploadTheme(RequestContext context) throws Exception {
        String themeName = context.getRequestParameters().get("themeName");
        String folderUri = context.getRequestParameters().get(FOLDER_URI);

        try {
            ExecutionContext executionContext = StaticExecutionContextProvider.getExecutionContext();

            byte[] fileContent;
            MultipartFile multipartFile = context.getRequestParameters().getMultipartFile("themeZip");

            if (multipartFile != null) {
                fileContent = multipartFile.getBytes();
            } else {
                fileContent = (byte[]) context.getFlowScope().get(FILE_CONTENT);
                context.getFlowScope().remove(FILE_CONTENT);
            }
            themeService.addZippedTheme(executionContext, folderUri, themeName, fileContent, true);


            String themeUri = folderUri + "/" + themeName;
            boolean isActiveTheme = themeService.isActiveThemeFolder(executionContext, themeUri);

            JSONObject jsonObject = new JSONObject().put("isActiveTheme", isActiveTheme);
            jsonObject.put("themeUri", themeUri);

            JSONObject ret = new JSONConverterBase().createJSONResponse(jsonObject);
            context.getRequestScope().put(AJAX_RESPONSE_MODEL, ret.toString());

            return success();
        } catch (Exception ex) {
            log.error("cannot reupload theme", ex);
            String errorMsg = messages.getMessage(THEMES_CANNOT_UPLOAD, new Object[] { ex.getMessage() }, LocaleContextHolder.getLocale());
            JSONObject ret = new JSONConverterBase().createErrorJSONResponse(errorMsg);
            context.getRequestScope().put(AJAX_RESPONSE_MODEL, ret.toString());

            return error();
        }
    }

    public Event uploadTheme(RequestContext context) throws Exception {

        String themeName = context.getRequestParameters().get("themeName");
        String folderUri = context.getRequestParameters().get(FOLDER_URI);

        byte[] fileContent = (byte[]) context.getFlowScope().get(FILE_CONTENT);

        context.getRequestScope().put(AJAX_CONTENT_TYPE, "text/html");

        try {
            ExecutionContext executionContext = StaticExecutionContextProvider.getExecutionContext();

            themeService.addZippedTheme(executionContext, folderUri, themeName, fileContent);

            String themeUri = folderUri + "/" + themeName;
            boolean isActiveTheme = themeService.isActiveThemeFolder(executionContext, themeUri);
        } catch (ThemeFolderExistsException ex) {
            //to notify client-side about folder existing
            log.info("theme folder already exists");

            JSONObject jsonObject = new JSONObject().put("themeExist", "themeExist");
            JSONObject ret = new JSONConverterBase().createJSONResponse(jsonObject);
            context.getRequestScope().put(AJAX_RESPONSE_MODEL, ret.toString());
            return success();
        } catch (Exception ex) {
            log.error("cannot upload theme", ex);
            String errorMsg = messages.getMessage(THEMES_CANNOT_UPLOAD, new Object[] { ex.getMessage() }, LocaleContextHolder.getLocale());
            JSONObject ret = new JSONConverterBase().createErrorJSONResponse(errorMsg);
            context.getRequestScope().put(AJAX_RESPONSE_MODEL, ret.toString());
            return error();
        }

        return success();
    }

    public byte[] getFileContent(RequestContext context) throws Exception {
        MultipartFile multipartFile = context.getRequestParameters().getMultipartFile("themeZip");
        return multipartFile.getBytes();
    }

    public TenantService getTenantService() {
        return tenantService;
    }

    public void setTenantService(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    public MessageSource getMessages() {
        return messages;
    }

    public void setMessages(MessageSource messages) {
        this.messages = messages;
    }

    public ThemeService getThemeService() {
        return themeService;
    }

    public void setThemeService(ThemeService themeService) {
        this.themeService = themeService;
    }

    public ThemeResolver getThemeResolver() {
        return themeResolver;
    }

    public void setThemeResolver(ThemeResolver themeResolver) {
        this.themeResolver = themeResolver;
    }

    public RepositoryService getRepositoryService() {
        return repositoryService;
    }

    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }
}

