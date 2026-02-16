/*
 * Copyright (C) 2025 the Jasper Server OS Authors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * Copyright (C) 2005 - 2022 TIBCO Software Inc. All rights reserved.
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
/**
 * @version: $Id$
 */
import {
    isNotNullORUndefined,
    getAsFunction
} from '../util/utils.common';

import actionModel from './actionModel.modelGenerator';
import actionModelJson from './actionModel.json.property';
import __jrsConfigs__ from 'js-sdk/src/jrs.configs';
import jQuery from 'jquery';

var primaryNavModule = {
    NAVIGATION_MENU_CLASS: 'menu vertical dropDown',
    ACTION_MODEL_TAG: 'navigationActionModel',
    CONTEXT_POSTFIX: '_mutton',
    NAVIGATION_MUTTON_DOM_ID: 'navigation_mutton',
    NAVIGATION_MENU_PARENT_CLASS: '.js-navigationOptions',
    /**
     * Navigation paths used in the navigation menu
     */
    navigationPaths: {
        browse: {
            url: 'flow.html',
            params: '_flowId=searchFlow'
        },
        home: { url: 'home.html' },
        library: {
            url: 'flow.html',
            params: '_flowId=searchFlow&mode=library'
        },
        logOut: { url: 'exituser.html' },
        search: {
            url: 'flow.html',
            params: '_flowId=searchFlow&mode=search'
        },
        favorites: {
            url: 'flow.html',
            params: '_flowId=searchFlow&mode=search&filterId=favoriteFilter&filterOption=favoriteFilter-favorites'
        },
        report: {
            url: 'flow.html',
            params: '_flowId=searchFlow&mode=search&filterId=resourceTypeFilter&filterOption=resourceTypeFilter-reports&searchText='
        },
        jobs: { url: 'scheduler/main.html' },
        event: {
            url: 'flow.html',
            params: '_flowId=logEventFlow'
        },
        adminHome: {
            url: 'flow.html',
            params: '_flowId=adminHomeFlow'
        },
        generalSettings: {
            url: 'flow.html',
            params: '_flowId=generalSettingsFlow'
        },
        organization: {
            url: 'flow.html',
            params: '_flowId=tenantFlow'
        },
        etl: { url: 'etl' },
        user: {
            url: 'flow.html',
            params: '_flowId=userListFlow'
        },
        role: {
            url: 'flow.html',
            params: '_flowId=roleListFlow'
        },
        designerOptions: {
            url: 'flow.html',
            params: '_flowId=designerOptionsFlow'
        },
        designerCache: {
            url: 'flow.html',
            params: '_flowId=designerCacheFlow'
        },
        awsSettings: {
            url: 'flow.html',
            params: '_flowId=awsSettingsFlow'
        },
        designer: {
            url: 'flow.html',
            params: '_flowId=adhocFlow'
        },
        dashboard: { url: 'dashboard/designer.html' },
        legacyDashboard: {
            url: 'flow.html',
            params: '_flowId=dashboardDesignerFlow&createNew=true'
        },
        domain: { url: 'domaindesigner.html' },
        dataSource: {
            url: 'flow.html',
            params: '_flowId=addDataSourceFlow&ParentFolderUri=' + encodeURIComponent('/datasources')
        },
        logSettings: { url: 'log_settings.html' },
        adminConsole: { url: 'adminConsole.html' },
        createReport: { url: 'view/view/modules/adhoc/createReport' }
    },
    /**
     * List of dom Id's for pages that require user confirmation before leaving.
     */
    bodyIds: {
        'designer': 'designerBase.confirmAndLeave',
        'dashboardDesigner': 'designerBase.confirmAndLeave',
        'repoBrowse': 'repositorySearch.confirmAndLeave',
        'repoSearch': 'repositorySearch.confirmAndLeave',
        'manage_users': 'orgModule.confirmAndLeave',
        'manage_roles': 'orgModule.confirmAndLeave',
        'manage_orgs': 'orgModule.confirmAndLeave',
        'domainDesigner_tables': 'domain.designer.confirmAndLeave',
        'domainDesigner_derivedTables': 'domain.designer.confirmAndLeave',
        'domainDesigner_joins': 'domain.designer.confirmAndLeave',
        'domainDesigner_calculatedFields': 'domain.designer.confirmAndLeave',
        'domainDesigner_filters': 'domain.designer.confirmAndLeave',
        'domainDesigner_display': 'domain.designer.confirmAndLeave',
        'dataChooserDisplay': 'domain.chooser.confirmAndLeave',
        'dataChooserFields': 'domain.chooser.confirmAndLeave',
        'dataChooserPreFilters': 'domain.chooser.confirmAndLeave',
        'dataChooserSaveAsTopic': 'domain.chooser.confirmAndLeave',
        'reportViewer': 'Report.confirmAndLeave'
    },
    globalActions: {
        'logOut': function (bodyId) {
            return primaryNavModule.bodyIds[bodyId];
        }
    },
    /**
     * This method initializes the primary menu. This needs to be called only once.
     */
    initializeNavigation: function () {
        var navKey;
        var navId;
        var navObject;
        if (jQuery('#' + this.ACTION_MODEL_TAG)[0] === null) {
            return;
        }
        actionModelJson.JSON = jQuery.parseJSON(!!jQuery('#' + this.ACTION_MODEL_TAG)[0] ? jQuery('#' + this.ACTION_MODEL_TAG)[0].text : '{}');
        var re = /[A-Za-z]+[_]{1}[A-Za-z]+/;    //go through json and get keys. Keys == action model context == nav menu muttons
        //go through json and get keys. Keys == action model context == nav menu muttons
        for (navKey in actionModelJson.JSON) {
            navId = re.exec(navKey)[0];    //strip out ids
            //strip out ids
            navObject = actionModelJson.JSON[navKey][0];    //get labels
            //get labels
            if (isNotNullORUndefined(navObject)) {
                this.createMutton(navId, navObject.text);
            } else {
                if (navId === 'main_home' || navId === 'main_library') {
                    var leaf = jQuery('#' + navId)[0];
                    leaf && jQuery(leaf).removeClass('hidden');
                }
            }
        }
    },
    /**
     * helper to create dom object
     */
    createMutton: function (domId, label) {
        var mutton = jQuery('#' + this.NAVIGATION_MUTTON_DOM_ID)[0].clone('true');
        var textPlacement = jQuery(mutton).children('.button');
        jQuery(mutton).attr('id', domId);    //TODO: see if we can do this with builder (maybe not)
        jQuery(mutton).find('p').attr('id', domId + '_label');    //TODO: see if we can do this with builder (maybe not)
        //TODO: see if we can do this with builder (maybe not)
        var text = document.createTextNode(label);
        textPlacement.append(text);
        var navigationMenuParent = jQuery(this.NAVIGATION_MENU_PARENT_CLASS)[0];
        navigationMenuParent && navigationMenuParent.append(mutton);
    },
    /* Show the drop-down menu for a given top-level menu item on the menu bar. */
    showNavButtonMenu: function (event, object, label) {
        var elementId = jQuery(object).attr('id');
        actionModel.showDropDownMenu(event, object, elementId + this.CONTEXT_POSTFIX, this.NAVIGATION_MENU_CLASS, this.ACTION_MODEL_TAG, label);
        jQuery('#' + 'menu')[0].parentId = elementId;
    },
    /**
     * Used to determine if a element is part of the navigation button
     * @param object
     */
    isNavButton: function (object) {
        return jQuery(object).hasClass('mutton') || jQuery(object).hasClass('icon');
    },
    /**
     * Object based method used to create a url based on the navigation path object
     * @param place Place where to go described in primaryNavModule.navigationPaths
     * @param params Request parameters
     */
    setNewLocation: function (place, params) {
        var locObj = this.navigationPaths[place], queryParams = params || locObj.params;
        if (!locObj)
            return;
        queryParams = queryParams ? '?' + queryParams : '';
        var destination = __jrsConfigs__.urlContext + '/' + locObj.url + queryParams;    // without try/catch location.href change in combination with canceled onbeforeunload event throws
        // error in IE.
        // without try/catch location.href change in combination with canceled onbeforeunload event throws
        // error in IE.
        try {
            if (locObj.url === this.navigationPaths.logOut.url) {
                var submitMe = document.getElementById("exitUser") || document.createElement("form");
                submitMe.setAttribute("id", "exitUser");
                submitMe.action = destination;
                submitMe.method = "post";
                jQuery('body').append(submitMe);
                submitMe.submit();
            } else {
                window.location.href = destination;
            }
        } catch (e) {
        }
    },
    /**
     * Method used to create a url based on the navigation path object
     * @param option
     */
    navigationOption: function (option) {
        var bodyId = jQuery(document.body).attr('id'), execFunction = null;
        if (primaryNavModule.globalActions[option]) {
            execFunction = primaryNavModule.globalActions[option](bodyId);
        } else if (primaryNavModule.bodyIds[bodyId]) {
            execFunction = primaryNavModule.bodyIds[bodyId];
        }
        if (execFunction) {
            var executableFunction = getAsFunction(execFunction);
            var answer = executableFunction(option);
            if (typeof answer == 'function') {
                answer(function () {
                    primaryNavModule.setNewLocation(option);
                });
                return;
            } else if (!answer) {
                return;
            }
        }
        primaryNavModule.setNewLocation(option);
    },
    /* ==== EVENTS ==== */
    /**
     * Event for fired on mouse over. Used to show a menu.
     * @param event
     * @param object
     */
    onMenuHeaderMouseOver: function (event, object) {
        this.showNavButtonMenu(event, object);
    }
};

// expose into global scope
window.primaryNavModule = primaryNavModule;

export default primaryNavModule;