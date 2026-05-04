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

package com.jaspersoft.jasperserver.api.engine.common.service.impl;

import com.jaspersoft.jasperserver.api.metadata.user.domain.Role;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.common.actionModel.model.ActionModelSupport;
import com.jaspersoft.jasperserver.common.actionModel.model.ActionModel;
import com.jaspersoft.jasperserver.common.actionModel.service.impl.ActionModelServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom2.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for the primary navigation.
 *
 * @author Papanii Okai
 */
public class NavigationActionModelSupport implements ActionModelSupport {
    private static String action_model_context;

    @Autowired
    @Qualifier("messageSource")
    private MessageSource messages;

    private static final String ROLES_DELIMITER = ",";
    private static final String NO_TENANT_ID = "null";
    private static boolean isProVersion = false;
    private static boolean isAvailableAdminConsole = true;

    private static Log log = LogFactory.getLog(NavigationActionModelSupport.class);
    private HttpServletRequest request;

    /**
     * External facing method called by all menu generating pages
     * @param context name of context listed in application context;
     * @return JSON string of menu created.
     */
    public String getClientActionModelDocument(String context, HttpServletRequest request){
        String json = "";
        if(context != null && context.length() > 0){
            action_model_context = context;
            this.request = request;
            json = getClientActionModelDocument();
        }else{
            log.error("Unable to create "+ context +" menu. Most likely programmer error");
            throw new RuntimeException("Unable to create menu.");
        }
        return json;
    }

    /**
     * @see com.jaspersoft.jasperserver.common.actionModel.model.ActionModelSupport#getClientActionModelDocument()
     */
    public String getClientActionModelDocument(){
        Document document =  ActionModelServiceImpl.getInstance().getActionModelMenu(action_model_context);
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


    /**
     * Helper method to determine if we are running the Pro or Enterprise edition
     * @return boolean indicating success..
     */
    public boolean isProVersion(){
        return isProVersion;
    }


    /**
     * Helper method to determine if we are running the community edition
     * @return boolean indicating success..
     */
    public boolean isCEVersion(){
        return !isProVersion();
    }

    /**
     * Determines if feature is available in for user based on version
     * @param id feature id we are checking for
     * @return boolean indicating success
     */
    public boolean isAvailableProFeature(String id){
        //return false for CE. Overrides in Pro
        return false;
    }

    public boolean isSupportedDevice() {
        String userAgent = this.request.getHeader("user-agent");
        return userAgent.indexOf("iPad") == -1;
    }
    
    /**
     * Determines if feature is available in for user based on version
     * @return boolean indicating success
     */
    public boolean isMainFeaturesDisabled() {
        //return false for CE. Overrides in Pro
        return false;
    }

    /**
     * Determines if users count exceeded
     * @return boolean exceeded success
     */
    public boolean isUsersExceeded() {
        //return false for CE. Overrides in Pro
        return false;
    }

    /**
     * Determines if grace period has passed
     * @return boolean passed success
     */
    public boolean banUserRole() {
        //return false for CE. Overrides in Pro
        return false;
    }

    /**
     * Determines if manage organization menu item can be displayed when MT feature is not supported
     * @return boolean can be displayed
     */
    public boolean showMTMenuItem() {
        //return false for CE. Overrides in Pro
        return false;
    }
    /**
     * Determines if madminConsole can be displayed
     * @return boolean can be displayed
     */
    public boolean isAvailableAdminConsole() {
        return isAvailableAdminConsole;
    }

    /**
     * Helper method used to retrieve the logged in users roles
     * @return set of roles
     */
    private Set getAuthenticationRoles(){
        SecurityContext secContext = SecurityContextHolder.getContext();
        Authentication authentication = secContext.getAuthentication();
        Object principalObject = authentication.getPrincipal();
        if (principalObject.toString().equals("anonymousUser")){
                  return  new HashSet();
        }
        User principal = (User) principalObject;

        if(principal == null){
            log.error("Authentication is null.");
            throw new RuntimeException("[Error]: Authentication is null.");
        }
        return principal.getRoles();
    }


    /**
     * Used to check if user has an allowable role
     * @param allowedRoles roles that are allowed
     * @return boolean indicating a match was found
     */
    public boolean checkAuthenticationRoles(String allowedRoles){
        String roles[] = allowedRoles.split(ROLES_DELIMITER);
        for(String role : roles){
            if(checkRole(role.trim())){
                return true;
            }
        }
        return false;
    }


    /**
     * Checking a specific role
     * @param role role we are checking
     * @return boolean indicating success
     */
    private boolean checkRole(String role){
        String tenantIdArray[] = role.split("\\|");
        String expectedTenantId = (tenantIdArray.length > 1) ? tenantIdArray[1] : NO_TENANT_ID;
        String expectedRole = tenantIdArray[0];
        Set userRoles = getAuthenticationRoles();

        for(Object obj : userRoles){
            String roleName = ((Role)obj).getRoleName();
            String roleTenantId = ((Role)obj).getTenantId();
            if(roleTenantId == null){
                roleTenantId = NO_TENANT_ID;  
            }

            if(roleTenantId.equals(expectedTenantId)){
                if(roleName.equals(expectedRole)){
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Used to get function id. //todo: talk Andrew about it.
     * @return
     */
    public int getFunctionId(){
        return 3;
    }


    /**
     * @see com.jaspersoft.jasperserver.common.actionModel.model.ActionModelSupport#getMessage(String)
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

    public MessageSource getDefaultMessageSource() {
        return messages;
    }

    public static void setProVersion(boolean proVersion) {
        isProVersion = proVersion;
    }
    public static void setAvailableAdminConsole(boolean availableAdminConsole) {
        isAvailableAdminConsole = availableAdminConsole;
    }
    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
}

