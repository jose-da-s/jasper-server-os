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

package com.jaspersoft.jasperserver.search.model;


import com.jaspersoft.jasperserver.common.actionModel.service.impl.ActionModelServiceImpl;
import com.jaspersoft.jasperserver.api.metadata.user.domain.Role;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.RoleImpl;
import com.jaspersoft.jasperserver.common.actionModel.model.ActionModelSupport;
import com.jaspersoft.jasperserver.common.actionModel.model.ActionModel;
import com.jaspersoft.jasperserver.search.mode.SearchMode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom2.Document;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

/**
 * Singleton class for the primary navigation.
 */
public class SearchActionModelSupport implements ActionModelSupport, InitializingBean {
    private static final Log log = LogFactory.getLog(SearchActionModelSupport.class);

    private static final String ACTION_MODEL_CONTEXT = "search";
    private static final String ARGUMENT_DELIMITER_REGEXP = "@@";
    private static final String ROLE_DELIMITER_REGEXP = ",";
    private static final String MODE_DELIMITER_REGEXP = ",";
    private static final String TENANT_DELIMITER_REGEXP = "\\|";

    private static SearchActionModelSupport instance;

    @Autowired
    @Qualifier("messageSource")
    private MessageSource messages;
    private boolean isProVersion = false;
    private boolean isOpenInEditor = false;
    private SearchMode searchMode;
    private HttpServletRequest request;


    public void setSearchMode(SearchMode searchMode) {
        this.searchMode = searchMode;
    }

    /**
     * @see com.jaspersoft.jasperserver.api.engine.common.service.ActionModelSupport#getClientActionModelDocument()
     */
    public String getClientActionModelDocument(){
        Document document =  ActionModelServiceImpl.getInstance().getActionModelMenu(ACTION_MODEL_CONTEXT);
        String data;
        try {
            data = ActionModel.getInstance().generateClientActionModel(this, document);
        } catch (Exception e) {
            log.error("Unable to create navigation menu");
            e.printStackTrace();
            data = "";
        }
        return data;
    }

    public String getClientActionModelDocument(HttpServletRequest request){
        String json = "";
        if(request != null){
            this.request = request;
            json = getClientActionModelDocument();
        }else{
            log.error("Unable to create context menu. Most likely programmer error");
            throw new RuntimeException("Unable to create context menu.");
        }
        return json;
    }

    /**
     * Used to check if user has all an allowable role
     * @param allowedRoleNames roles that are allowed
     * @return boolean indicating a match was found
     */
    public boolean checkAuthenticationRoles(String allowedRoleNames){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User principal = (User)authentication.getPrincipal();

        Set<Role> userRoles = (Set<Role>) principal.getRoles();
        Set<Role> allowedRoles = toRoles(allowedRoleNames.split(ROLE_DELIMITER_REGEXP));

        for(Role role : allowedRoles) {
            if (userRoles.contains(role)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Used to check search mode
     * @param allowedSearchModes search modes that are allowed
     * @return boolean indicating a match was found
     */
    public boolean checkMode(String allowedSearchModes){

        Set<SearchMode> modes = new HashSet<SearchMode>();

        for (String name : allowedSearchModes.split(MODE_DELIMITER_REGEXP)) {
            modes.add(SearchMode.valueOf(name.toUpperCase()));
        }

        return modes.contains(this.searchMode);
    }

    public boolean checkModeAndDevice(String allowedSearchModes) {
        return checkMode(allowedSearchModes) && isSupportedDevice();
    }

    public boolean checkOpenInEditor(String allowedSearchModes) {
        return isProVersion && isOpenInEditor && checkModeAndDevice(allowedSearchModes);
    }

    private Set<Role> toRoles(String[] roleNames) {
        Set<Role> roles = new HashSet<Role>();

        for (String name : roleNames) {
            String parts[] = name.trim().split(TENANT_DELIMITER_REGEXP);

            Role role = new RoleImpl();
            role.setRoleName(parts[0]);
            role.setTenantId((parts.length > 1) ? parts[1] : null);

            roles.add(role);
        }

        return roles;
    }

    /**
     * Helper method to determine if we are running the Pro or Enterprise edition
     * @return boolean indicating success..
     */
    public boolean isProVersion(){
        return isProVersion;
    }

    public boolean isSupportedDevice() {
        String userAgent = this.request.getHeader("user-agent");
        return userAgent.indexOf("iPad") == -1;
    }

    public boolean isOpenInEditor() {
        return isOpenInEditor;
    }

    /**
     * Singleton getter method
     * @return singleton object
     */
    public static synchronized SearchActionModelSupport getInstance(String searchMode){
        SearchActionModelSupport.instance.setSearchMode(SearchMode.valueOf(searchMode.toUpperCase()));
        return SearchActionModelSupport.instance;
    }


    public void afterPropertiesSet() throws Exception {
        instance = this;
    }

    /**
     * @see com.jaspersoft.jasperserver.api.engine.common.service.ActionModelSupport#getMessage(String)
     * @param label i18n code
     */
    public String getMessage(String label){
        return messages.getMessage(label, null,LocaleContextHolder.getLocale());
    }

    /*
     * Spring setters and getters for bean
     */
    public void setDefaultMessageSource(MessageSource messages) {
        this.messages = messages;
    }

    public void setProVersion(boolean proVersion) {
        isProVersion = proVersion;
    }

    public void setOpenInEditor(boolean openInEditor) {
        isOpenInEditor = openInEditor;
    }

}

