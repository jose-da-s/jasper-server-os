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
/* global alert, confirm*/
/**
 * Local context initialization.
 */

import {$ , Template} from 'prototype';
import jQuery from 'jquery';
import _ from 'underscore';
import {
    isIPad,
    isWebKitEngine,
    isSupportsTouch,
    isRightClick,
    deepClone
} from "../util/utils.common";
import layoutModule from '../core/core.layout';
import webHelpModule from '../components/components.webHelp';
import orgModule from '../manage/mng.common';
import buttonManager from '../core/core.events.bis';
import dialogs from '../components/components.dialogs';
import actionModel from '../actionModel/actionModel.modelGenerator';
import __jrsConfigs__ from 'js-sdk/src/jrs.configs';
import AlertDialog from 'js-sdk/src/common/component/dialog/AlertDialog';
import i18n from '../i18n/jasperserver_messages.properties';
import xssUtil from 'js-sdk/src/common/util/xssUtil';
import ConfirmationDialog from 'js-sdk/src/common/component/dialog/ConfirmationDialog';
import domUtil from 'js-sdk/src/common/util/domUtil';
import navigateBackUtils from '../navigateBack/utils/navigateBackUtils';

//var localContext = window;
//////////////////////////
// Tests for action model
//////////////////////////
function invokeAction(actionName) {
    var action = repositorySearch.Action['create' + actionName].call();
    action.invokeAction();
}
function invokeFolderAction(actionName, folder, options) {
    var theFolder = folder ? folder : repositorySearch.model.getContextFolder();
    var action = repositorySearch.folderActionFactory[actionName](theFolder, options);
    action.invokeAction();
}
function invokeResourceAction(actionName, resource, options) {
    var theResource = resource ? resource : repositorySearch.model.getSelectedResources()[0];
    var action = repositorySearch.resourceActionFactory[actionName](theResource, options);
    action.invokeAction();
}
function invokeBulkAction(actionName, resourceName, event) {
    var resources = repositorySearch.model.getSelectedResources();
    var action = repositorySearch.bulkActionFactory[actionName](resources, event);
    action.invokeAction();
}

var invokeRedirectAction = _.debounce(function (actionName, options) {
    var action = repositorySearch.RedirectAction['create' + actionName].call();
    action.invokeAction(options);
}, 500)

function invokeCreate(resourceTypeSuffix, fileType) {
    var action = repositorySearch.RedirectAction.createCreateResourceAction(resourceTypeSuffix, fileType);
    action.invokeAction();
}
function isCopyOrMove() {
    return repositorySearch.CopyMoveController.isCopy() || repositorySearch.CopyMoveController.isMove();
}
function isFolderCopyOrMove() {
    return repositorySearch.CopyMoveController.isCopyFolder() || repositorySearch.CopyMoveController.isMoveFolder();
}
function isResourceCopyOrMove() {
    return repositorySearch.CopyMoveController.isCopyResource() || repositorySearch.CopyMoveController.isMoveResource();
}
function canCreateFolder() {
    var folder = repositorySearch.model.getContextFolder();
    return folder && !folder.isOrganizationsRoot() && folder.isEditable();
}
function canFolderBeDeleted() {
    var folder = repositorySearch.model.getContextFolder();
    return folder && !folder.isOrganization() && !folder.isRoot() && !folder.isPublic() && !folder.isTemp() && !folder.isOrganizationsRoot() && folder.isRemovable() && !folder.isActiveThemeFolder() && !folder.isThemeRootFolder();
}
function canFolderBeEdited(folder) {
    folder = folder ? folder : repositorySearch.model.getContextFolder();
    return folder && folder.isEditable();
}
function canFolderBeCopied(folder) {
    folder = folder ? folder : repositorySearch.model.getContextFolder();
    return folder && !folder.isOrganization() && !folder.isRoot() && !folder.isOrganizationsRoot() && folder.isReadable();
}
function canFolderBeCopiedOrMovedToFolder(folder) {
    var toFolder = folder ? folder : repositorySearch.model.getContextFolder();
    var canFolderBeCopiedOrMoved = isFolderCopyOrMove() && !toFolder.isOrganizationsRoot() && canFolderBeEdited(toFolder);
    if (!canFolderBeCopiedOrMoved) {
        return false;
    }
    var theFolder = repositorySearch.CopyMoveController.object;
    return toFolder.URI !== theFolder.URI;
}
function canFolderBeMoved(folder) {
    folder = folder ? folder : repositorySearch.model.getContextFolder();
    return folder && !folder.isOrganization() && !folder.isRoot() && !folder.isPublic() && !folder.isTemp() && !folder.isOrganizationsRoot() && folder.isRemovable() && !folder.isActiveThemeFolder() && !folder.isThemeRootFolder();
}
function canFolderBeExported(folder) {
    folder = folder ? folder : repositorySearch.model.getContextFolder();
    var pathElements = folder.URI.split('/');
    return !((folder.URI === '/' || pathElements[pathElements.length - 1] === 'organizations' || pathElements[pathElements.length - 2] === 'organizations') && pathElements[pathElements.length - 1] !== 'org_template');
}
function canFolderPermissionsBeAssigned() {
    var folder = repositorySearch.model.getContextFolder();
    return folder && folder.isAdministrable();
}
function canFolderPropertiesBeShowed() {
    var folder = repositorySearch.model.getContextFolder();
    return folder && folder.isReadable() && !canFolderPropertiesBeEdited();
}
function canFolderPropertiesBeEdited() {
    var folder = repositorySearch.model.getContextFolder();
    return folder && folder.isEditable() && !folder.isOrganization() && !folder.isOrganizationsRoot() && !folder.isRoot();
}
function canResourceBeCreated(type) {
    var folder = repositorySearch.model.getContextFolder();
    return folder && !folder.isOrganization() && !folder.isRoot() && !folder.isOrganizationsRoot() && folder.isEditable();
}
function canBeRun(resource) {
    resource = resource ? resource : repositorySearch.model.getSelectedResources()[0];
    return resource && resource.isReadable() && repositorySearch.runActionFactory[resource.typeSuffix()] && !repositorySearch.isflowRedirectRunning;
}
function canBeGenerated(resource) {
    return false;
}
function canBeConverted(resource) {
    return false;
}
function canResourceBeEdited(resource) {
    resource = resource ? resource : repositorySearch.model.getSelectedResources()[0];
    var hasEditActionImpl = repositorySearch.editActionFactory[resource.typeSuffix()] || ResourcesUtils.isCustomDataSource(resource);
    var allowed = resource && resource.isEditable() && hasEditActionImpl;    // Edit menu item is forbidden.
    // Edit menu item is forbidden.
    if (['DashboardResource'].include(resource.typeSuffix())) {
        return false;
    } else {
        return allowed;
    }
}
function canBeOpenedInDesigner(resource) {
    resource = resource ? resource : repositorySearch.model.getSelectedResources()[0];
    return resource && resource.isEditable() && repositorySearch.openActionFactory[resource.typeSuffix()] && resource.typeSuffix() !== 'ContentResource';
}
function canBeOpened(resource) {
    resource = resource ? resource : repositorySearch.model.getSelectedResources()[0];
    return resource && resource.isReadable() && repositorySearch.openActionFactory[resource.typeSuffix()] && resource.typeSuffix() === 'ContentResource';
}
function canBeRunInBackground(resource) {
    return window.canBeScheduled(resource);
}
function canBeScheduled(resource) {
    resource = resource ? resource : repositorySearch.model.getSelectedResources()[0];
    return resource && resource.isReadable() && [
        repositorySearch.ResourceType.REPORT_UNIT,
        repositorySearch.ResourceType.ADHOC_REPORT_UNIT,
        repositorySearch.ResourceType.REPORT_OPTIONS
    ].include(resource.resourceType);
}
function canResourceBeCopied(resource) {
    resource = resource ? resource : repositorySearch.model.getSelectedResources()[0];
    return resource && resource.isReadable();
}
function canResourceBeMoved(resource) {
    resource = resource ? resource : repositorySearch.model.getSelectedResources()[0];
    return resource && resource.isRemovable();
}
function canResourceBeDeleted(resource) {
    resource = repositorySearch.model.getSelectedResources()[0];
    return resource && resource.isRemovable();
}
function canResourcePropertiesBeShowed() {
    var resource = repositorySearch.model.getSelectedResources()[0];
    return resource && resource.isReadable() && !resource.isEditable();
}
function canResourcePropertiesBeEdited(resource) {
    resource = resource ? resource : repositorySearch.model.getSelectedResources()[0];
    return resource && resource.isEditable();
}
function canResourcePermissionsBeAssigned() {
    var resource = repositorySearch.model.getSelectedResources()[0];
    return resource && resource.isAdministrable();
}
function canAllBeRun() {
    var resources = repositorySearch.model.getSelectedResources();
    var detected = resources.detect(function (resource) {
        return !window.canBeRun(resource);
    });
    return resources.length > 0 && detected === undefined;
}
function canAllBeEdited() {
    var resources = repositorySearch.model.getSelectedResources();
    var detected = resources.detect(function (resource) {
        return !window.canResourceBeEdited(resource);
    });
    return resources.length > 0 && detected === undefined && !isIPad();
}
function canAllBeOpened() {
    var resources = repositorySearch.model.getSelectedResources();
    var detected = resources.detect(function (resource) {
        return !window.canBeOpenedInDesigner(resource) && !window.canBeOpened(resource);
    });
    return resources.length > 0 && detected === undefined;
}
function canAllBeCopied() {
    var resources = repositorySearch.model.getSelectedResources();
    var detected = resources.detect(function (resource) {
        return !canResourceBeCopied(resource);
    });
    return resources.length > 0 && detected === undefined;
}
function canAllBeMoved() {
    var resources = repositorySearch.model.getSelectedResources();
    var detected = resources.detect(function (resource) {
        return !canResourceBeMoved(resource);
    });
    return resources.length > 0 && detected === undefined;
}
function canAllBeCopiedOrMovedToFolder(folder) {
    folder = folder ? folder : repositorySearch.model.getContextFolder();
    var canAllBeCopiedOrMoved = isResourceCopyOrMove() && !folder.isOrganizationsRoot() && canFolderBeEdited(folder);
    if (!canAllBeCopiedOrMoved) {
        return false;
    }
    var resources = repositorySearch.CopyMoveController.isBulkAction() ? repositorySearch.CopyMoveController.object : [repositorySearch.CopyMoveController.object];
    var allow = true;
    if (folder.isThemeRootFolder() || folder.isThemeFolder()) {
        var allFiles = resources.detect(function (resource) {
            return !resource.resourceType.endsWith('.FileResource');
        }) == null;
        allow = allFiles;
    }
    return allow;
}
function canAllBePasted() {
    return canAllBeCopiedOrMovedToFolder(repositorySearch.model.getSelectedFolder());
}
function canAllPropertiesBeShowed() {
    var resources = repositorySearch.model.getSelectedResources();
    var detected = resources.detect(function () {
        return !canResourcePropertiesBeShowed();
    });
    return resources.length > 0 && detected === undefined;
}
function canAllPropertiesBeEdited() {
    var resources = repositorySearch.model.getSelectedResources();
    var detected = resources.detect(function (resource) {
        return !canResourcePropertiesBeEdited(resource);
    });
    return resources.length > 0 && detected === undefined;
}
function canBeAddedToFavorites() {
    let resources = repositorySearch.model.getSelectedResources();
    return resources.every((resource)=>{
        return !resource.isFavorite;
    });
}
function canBeRemovedFromFavorites() {
    let resources = repositorySearch.model.getSelectedResources();
    return resources.every((resource)=>{
        return resource.isFavorite;
    });
}

function canAllBeDeleted() {
    var resources = repositorySearch.model.getSelectedResources();
    var detected = resources.detect(function (resource) {
        return !canResourceBeDeleted(resource);
    });
    return resources.length > 0 && detected === undefined;
}
function isThemeFolder() {
    var folder = repositorySearch.model.getContextFolder();
    return folder && folder.isThemeFolder();
}
function isNonActiveThemeFolder() {
    var folder = repositorySearch.model.getContextFolder();
    return isThemeFolder() && !folder.isActiveThemeFolder();
}
function isThemeRootFolder() {
    var folder = repositorySearch.model.getContextFolder();
    return folder && folder.isThemeRootFolder();
}
function canThemeBeReuploaded() {
    var folder = repositorySearch.model.getContextFolder();
    return folder && folder.isThemeFolder() && canFolderBeEdited(folder);
}
function isPermissionsChanged() {
    var permissionDialogs = repositorySearch.dialogsPool.getAllPermissionsDialogs();
    return permissionDialogs.detect(function (dialog) {
        return dialog.isChanged();
    });
}
function isPropertiesChanged() {
    var propertyDialogs = repositorySearch.dialogsPool.getAllPropertiesDialogs();
    return propertyDialogs.detect(function (dialog) {
        return dialog.isChanged();
    });
}    ////////////////////////////
// Repository search object
////////////////////////////
////////////////////////////
// Repository search object
////////////////////////////
var repositorySearch = {
    _container: null,
    mode: null,
    isFlowRedirectRunning: false,
    flowExecutionKey: '',
    messages: [],
    Mode: {
        SEARCH: 'search',
        BROWSE: 'browse',
        LIBRARY: 'library'
    },
    ResourceType: {
        REPORT_UNIT: 'com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit',
        ADHOC_REPORT_UNIT: 'com.jaspersoft.ji.adhoc.AdhocReportUnit',
        DASHBOARD_RESOURCE: 'com.jaspersoft.ji.adhoc.DashboardResource',
        DASHBOARD: 'com.jaspersoft.ji.dashboard.DashboardModelResource',
        REPORT_OPTIONS: 'com.jaspersoft.ji.report.options.metadata.ReportOptions',
        REPORT_DATA_SOURCE: 'com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportDataSource',
        JDBC_REPORT_DATA_SOURCE: 'com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.JdbcReportDataSource',
        JNDI_JDBC_REPORT_DATA_SOURCE: 'com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.JndiJdbcReportDataSource',
        VIRTUAL_REPORT_DATA_SOURCE: 'com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.VirtualReportDataSource',
        CUSTOM_REPORT_DATA_SOURCE: 'com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomReportDataSource',
        BEAN_REPORT_DATA_SOURCE: 'com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.BeanReportDataSource',
        QUERY: 'com.jaspersoft.jasperserver.api.metadata.common.domain.Query',
        INPUT_CONTROL: 'com.jaspersoft.jasperserver.api.metadata.common.domain.InputControl',
        LIST_OF_VALUES: 'com.jaspersoft.jasperserver.api.metadata.common.domain.ListOfValues',
        LIST_OF_VALUES_ITEM: 'com.jaspersoft.jasperserver.api.metadata.common.domain.ListOfValuesItem',
        DATA_TYPE: 'com.jaspersoft.jasperserver.api.metadata.common.domain.DataType',
        CONTENT_RESOURCE: 'com.jaspersoft.jasperserver.api.metadata.common.domain.ContentResource',
        FILE_RESOURCE: 'com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource',
        SEMANTIC_LAYER_DATA_SOURCE: 'com.jaspersoft.commons.semantic.datasource.SemanticLayerDataSource',
        DATA_DEFINER_UNIT: 'com.jaspersoft.commons.semantic.DataDefinerUnit'
    },
    SearchAction: {
        NEXT: 'next',
        GET_RESOURCE_CHILDREN: 'getResourceChildren'
    },
    Event: {
        SEARCH_RUN: 'search:run',
        SEARCH_BROWSE: 'search:browse',
        SEARCH_FILTER: 'search:filter',
        SEARCH_SORT: 'search:sort',
        SEARCH_NEXT: 'search:next',
        SEARCH_ROLLBACK: 'search:rollback',
        SEARCH_SEARCH: 'search:search',
        SEARCH_CHILDREN: 'search:children',
        STATE_CHANGED: 'state:changed',
        FILTER_PATH_CHANGED: 'filterPath:changed',
        RESULT_CHANGED: 'result:changed',
        CHILDREN_LOADED: 'resourceChildren:loaded',
        RESULT_NEXT: 'result:next',
        RESULT_ERROR: 'result:error',
        REDIRECT_ERROR: 'redirect:error',
        FLOW_REDIRECT_RUNNING: 'flowRedirect:running'
    },
    InfoAction: { GET_DISPLAY_PATH: 'getDisplayPath' },
    PermissionAction: {
        BROWSE: 'permissionBrowse',
        SEARCH: 'permissionSearch',
        NEXT: 'permissionNext',
        UPDATE: 'permissionsUpdate'
    },
    PermissionEvent: {
        BROWSE: 'permission:browse',
        SEARCH: 'permission:search',
        NEXT: 'permission:next',
        UPDATE: 'permission:update',
        LOADED: 'permission:loaded',
        UPDATED: 'permission:updated',
        ERROR: 'permission:error'
    },
    FolderAction: {
        CREATE: 'createFolder',
        COPY: 'copyFolder',
        MOVE: 'moveFolder',
        UPDATE: 'updateFolder',
        DELETE: 'deleteFolder'
    },
    FolderEvent: {
        DELETED: 'folder:deleted',
        DELETE_ERROR: 'folder:deleteError',
        CREATED: 'folder:created',
        CREATE_ERROR: 'folder:createError',
        COPIED: 'folder:copied',
        COPY_ERROR: 'folder:copyError',
        MOVED: 'folder:moved',
        MOVE_ERROR: 'folder:moveError',
        UPDATED: 'folder:updated',
        UPDATE_ERROR: 'folder:updateError'
    },
    ResourceAction: {
        DELETE: 'deleteResources',
        COPY: 'copyResources',
        MOVE: 'moveResources',
        UPDATE: 'updateResource',
        GENERATE: 'generate',
        CONVERT: 'convert'
    },
    ResourceEvent: {
        DELETED: 'resource:deleted',
        DELETE_ERROR: 'resource:deleteError',
        COPIED: 'resource:copied',
        COPY_ERROR: 'resource:copyError',
        MOVED: 'resource:moved',
        MOVE_ERROR: 'resource:moveError',
        UPDATED: 'resource:updated',
        UPDATE_ERROR: 'resource:updateError',
        GENERATED: 'resource:generated',
        GENERATE_ERROR: 'resource:generateError',
        CONVERTED: 'resource:converted',
        CONVERT_ERROR: 'resource:convertError'
    },
    RedirectType: {
        FLOW_REDIRECT: 0,
        LOCATION_REDIRECT: 1,
        WINDOW_REDIRECT: 2
    },
    ThemeAction: {
        SETTHEME: 'setActiveTheme',
        DOWNLOAD_THEME: 'downloadTheme',
        REUPLOAD: 'reuploadTheme'
    },
    ThemeEvent: {
        UPDATED: 'theme:updated',
        REUPLOADED: 'theme:reuploaded',
        THEME_ERROR: 'theme:error'
    },
    ResourcesFavoriteAction: {
        FILTER_ID: 'favoriteFilter',
        ALL_RESOURCES: 'favoriteFilter-all',
        FAVORITE_RESOURCES: 'favoriteFilter-favorites'
    },
    initialize: function (localContext) {
        var options = localContext.rsInitOptions;
        repositorySearch.mode = options.mode;
        repositorySearch.flowExecutionKey = options.flowExecutionKey;
        layoutModule.resizeOnClient(document.body.id == 'repoSearch' ? 'searchFilters' : 'folders', 'results');
        webHelpModule.setCurrentContext(repositorySearch.mode === repositorySearch.Mode.SEARCH ? 'search' : 'repo');
        options.enableDnD = repositorySearch.mode == repositorySearch.Mode.BROWSE && !isIPad();
        repositorySearch.model.setPublicFolderUri(options.publicFolderUri);
        repositorySearch.model.setTempFolderUri(options.tempFolderUri);
        repositorySearch.model.setRootFolderUri(options.rootFolderUri);
        repositorySearch.model.setOrganizationsFolderUri(options.organizationsFolderUri);
        repositorySearch.model.setFolderSeparator(options.folderSeparator);
        repositorySearch.model.setAdministrator(options.isAdministrator);
        repositorySearch.model.setConfiguration(options.configuration);
        repositorySearch.model.setServerState(options.state);
        repositorySearch.CursorManager.initialize();
        repositorySearch.actionModel.initialize();
        this.navigateToResourcePage();

        if (repositorySearch.mode == repositorySearch.Mode.BROWSE) {
            repositorySearch.toolbar.initialize(repositorySearch.actionModel.bulkActions);
            repositorySearch.foldersPanel.initialize(options);
        } else {
            repositorySearch.filtersPanel.initialize(repositorySearch.model.getFiltersConfiguration(), repositorySearch.model.getFiltersState());
            repositorySearch.secondarySearchBox.initialize(repositorySearch.model.getTextState());
            repositorySearch.filterPath.initialize();
        }
        repositorySearch.resultsPanel.initialize(options);
        if(repositorySearch.mode === repositorySearch.Mode.LIBRARY){
            repositorySearch.favoriteSwitchControl.initialize(options);
        }
        repositorySearch.sortersPanel.initialize(repositorySearch.model.getSortersConfiguration(), repositorySearch.model.getSortState());
        repositorySearch.initFolderEvents();
        repositorySearch.initResourceEvents();
        repositorySearch.initThemeEvents();
        repositorySearch.observe('search:filter', function (event) {
            var action = new repositorySearch.ServerAction.createSearchAction('filter', {
                filterId: event.memo.filterId,
                filterOption: event.memo.optionId
            });
            action.invokeAction();
        });
        repositorySearch.observe('search:sort', function (event) {
            var action = new repositorySearch.ServerAction.createSearchAction('sort', { sortBy: event.memo.sorterId });
            action.invokeAction();
        });
        repositorySearch.observe('search:browse', function (event) {
            var action = new repositorySearch.ServerAction.createSearchAction('browse', { folderUri: event.memo.uri });
            action.invokeAction();
            repositorySearch.actionModel.refreshToolbar();
        });
        repositorySearch.observe('search:search', function (event) {
            var action = new repositorySearch.ServerAction.createSearchAction('search', { text: event.memo.text });
            action.invokeAction();
        });
        repositorySearch.observe('search:next', function (event) {
            var action = new this.ServerAction.createSearchAction(this.SearchAction.NEXT, {});
            action.invokeAction();
        }.bindAsEventListener(repositorySearch));
        repositorySearch.observe('search:rollback', function (event) {
            var action = new repositorySearch.ServerAction.createSearchAction('rollback', { position: event.memo.position });
            action.invokeAction();
        });
        repositorySearch.observe('search:children', function (event) {
            var resource = event.memo.resource;
            var item = event.memo.item;
            var action = new this.ServerAction.createSearchAction(this.SearchAction.GET_RESOURCE_CHILDREN, {
                resourceUri: resource.URIString,
                resourceType: resource.resourceType,
                item: item
            });
            action.invokeAction();
        }.bindAsEventListener(repositorySearch));
        repositorySearch.observe('permission:browse', function (event) {
            var action = new repositorySearch.ServerAction.createPermissionAction(this.PermissionAction.BROWSE, event.memo);
            action.invokeAction();
        }.bindAsEventListener(repositorySearch));
        repositorySearch.observe('permission:search', function (event) {
            var action = new repositorySearch.ServerAction.createPermissionAction(this.PermissionAction.SEARCH, event.memo);
            action.invokeAction();
        }.bindAsEventListener(repositorySearch));
        repositorySearch.observe('permission:next', function (event) {
            var action = new repositorySearch.ServerAction.createPermissionAction(this.PermissionAction.NEXT, event.memo);
            action.invokeAction();
        }.bindAsEventListener(repositorySearch));
        repositorySearch.observe('permission:update', function (event) {
            var uri = event.memo.uri;
            var entities = event.memo.entities;
            var finishEdit = event.memo.finishEdit;
            if (entities.length > 0) {
                var data = entities.collect(function (entity) {
                    return entity.toPermissionData();
                });
                var action = new repositorySearch.ServerAction.createPermissionAction(this.PermissionAction.UPDATE, {
                    uri: uri,
                    entitiesWithPermission: Object.toJSON(data),
                    finishEdit: finishEdit
                });
                action.invokeAction();
            } else {
                var dialog = repositorySearch.dialogsPool.getPermissionsDialog(uri);
                finishEdit && dialog.hide();
            }
        }.bindAsEventListener(repositorySearch));
        function jsonToEntities(entities, type) {
            if (entities) {
                var clazz = type == 'USER' ? orgModule.User : orgModule.Role;
                return entities.collect(function (entity) {
                    return new clazz(entity);
                });
            } else {
                return [];
            }
        }
        repositorySearch.observe('permission:loaded', function (event) {
            var data = event.memo.responseData;
            var dialog = repositorySearch.dialogsPool.getPermissionsDialog(event.memo.inputData.uri);
            dialog.stopWaiting();
            if (data) {
                if (event.memo.doSet) {
                    dialog.setEntities(jsonToEntities(data.entities, data.type));
                } else {
                    dialog.addEntities(jsonToEntities(data.entities, data.type));
                }
                if (data.type === 'USER') {
                    buttonManager.select(dialog._byUsersButton.parentNode);
                    buttonManager.unSelect(dialog._byRolesButton.parentNode);
                } else {
                    buttonManager.unSelect(dialog._byUsersButton.parentNode);
                    buttonManager.select(dialog._byRolesButton.parentNode);
                }
            }
        }.bindAsEventListener(repositorySearch));
        repositorySearch.observe('permission:updated', function (event) {
            var uri = event.memo.inputData.uri;
            var finishEdit = event.memo.inputData.finishEdit;
            var dialog = repositorySearch.dialogsPool.getPermissionsDialog(uri);
            dialog.enable();
            if (finishEdit) {
                dialog.hide();
            } else {
                var data = event.memo.responseData;
                data && dialog.updateEntities(jsonToEntities(data.entities, data.type));
            }
        }.bindAsEventListener(repositorySearch));
        repositorySearch.observe('permission:error', function (event) {
            alert(Object.toJSON(event.memo));
        }.bindAsEventListener(repositorySearch));
        repositorySearch.observe('resourceChildren:loaded', function (event) {
            var item = event.memo.inputData.item;
            var resource = item.getValue();
            var data = event.memo.responseData;
            if (Object.isArray(data)) {
                var resources = [];
                data.each(function (rJson) {
                    var r = new Resource(rJson);
                    resource.addChild(r);
                    resources.push(r);
                });
                repositorySearch.resultsPanel.setResources(resources, item);
                if (resource.isLoaded() && item.isLoading) {
                    item.setLoading(false);
                }
                if (jQuery(item._getElement()).hasClass('cursor')){
                    jQuery('.subfocus').removeClass('subfocus');
                    jQuery(item._getElement()).addClass('subfocus').addClass('focus-visible');
                }
            }
        }.bindAsEventListener(repositorySearch));
        repositorySearch.observe('state:changed', function (event) {
            repositorySearch.sortersPanel.enableItems();
            repositorySearch.updateUI(event.memo.state);
        });
        repositorySearch.observe('filterPath:changed', function (event) {
            repositorySearch.filterPath.setPathItems(event.memo.filterPath);
        });
        repositorySearch.observe('result:changed', function (event) {
            var data = event.memo.resources;
            var resources = data.collect(function (rData) {
                return new Resource(rData);
            });
            repositorySearch.resultsPanel.setResources(resources);

            var list = repositorySearch.resultsPanel.getList(),
                items = list.getItems();

            list.setCursor(items[0]);
        });
        repositorySearch.observe('result:next', function (event) {
            var data = event.memo.resources;
            var resources = data.collect(function (rData) {
                return new Resource(rData);
            });
            repositorySearch.resultsPanel.addResources(resources);
        });
        repositorySearch.observe('result:error', this.defaultErrorHandler);
        repositorySearch.observe('redirect:error', this.defaultErrorHandler);
        repositorySearch.observe('flowRedirect:running', function () {
            repositorySearch.isflowRedirectRunning = true;
            repositorySearch.toolbar.refresh();
        });    // First call
        // First call
        if (repositorySearch.Mode.BROWSE != options.mode) {
            repositorySearch.fire(repositorySearch.Event.SEARCH_SEARCH, { text: repositorySearch.model.getTextState() });
        }
        if (!localContext.rsInitOptions.systemConfirm.blank()) {
            dialogs.systemConfirm.show(localContext.rsInitOptions.systemConfirm);
        }    //Show popup dialog if error message present
        //Show popup dialog if error message present
        if (!localContext.rsInitOptions.errorPopupMessage.blank()) {
            dialogs.errorPopup.show(localContext.rsInitOptions.errorPopupMessage);
        }
        this._disableBfCacheIfSafari();
    },
    navigateToResourcePage: function(){
        let mode= repositorySearch.mode;
        if(mode && history.length > 1) {
            let locObj = window.primaryNavModule.navigationPaths[mode],
                queryParams = window.primaryNavModule.navigationPaths[mode].params;
            queryParams = queryParams ? '?' + queryParams : '';

            const destination = __jrsConfigs__.urlContext + '/' + locObj.url + queryParams;
            navigateBackUtils.setLocation(destination);
        }
    },
    showContextMenu: function (e) {
        var event = e.memo.targetEvent;
        if (repositorySearch.mode === repositorySearch.Mode.BROWSE) {
            if (repositorySearch.foldersPanel.isFolderContextMenu(event)) {
                const label = jQuery(e.memo.targetEvent?.target).closest('li').find('p').text().trim();
                repositorySearch.actionModel.showFolderMenu(event, undefined, label);
            }
        }
        if (repositorySearch.resultsPanel.isResourceContextMenu(event)) {
            var resources = repositorySearch.model.getSelectedResources();
            if (resources.length > 1) {
                repositorySearch.actionModel.showResourceBulkMenu(event);
            } else {
                repositorySearch.actionModel.showResourceMenu(event);
            }
        }
    },
    _disableBfCacheIfSafari: function () {
        if (isWebKitEngine()) {
            domUtil.disableBackForwordCache();
        }
    },
    updateUI: function (state) {
        // Check sort state.
        if (state.sortBy != repositorySearch.model.getSortState()) {
            jQuery(repositorySearch.sortersPanel).find(state.sortBy, true)[0];
        }    // Check text state.
        // Check text state.
        if (state.text != repositorySearch.model.getTextState()) {
            if (repositorySearch.mode != repositorySearch.Mode.BROWSE) {
                repositorySearch.secondarySearchBox.setText(state.text);
            }
        }    // Check filters states.
        // Check filters states.
        for (var filterId in state.customFilters) {
            if (state.customFilters[filterId] != repositorySearch.model.getFiltersState()[filterId]) {
                jQuery(repositorySearch.filtersPanel).find(filterId, state.customFilters[filterId], true)[0];
            }
        }
        // TODO: select folder
        repositorySearch.model.setServerState(state);
    },
    initFolderEvents: function () {
        this.observe('folder:deleted', function (event) {
            var folder = event.memo.inputData.folder;
            var parentFolder = folder.getParentFolder();
            var foldersPanel = repositorySearch.foldersPanel;
            foldersPanel.refreshFolder(parentFolder);
            if (parentFolder.URI === '/') {
                //force root folder selection
                foldersPanel.tree._selectOrEditNode(undefined, parentFolder.node, false);
            }
        });
        this.observe('folder:deleteError', this.defaultErrorHandler);
        this.observe('folder:created', function (event) {
            var toFolder = event.memo.inputData.toFolder;
            var targetFolder = event.memo.responseData;
            repositorySearch.foldersPanel.createAndSelectFolder(targetFolder, toFolder);
        });
        this.observe('folder:createError', this.defaultErrorHandler);
        this.observe('folder:copied', function (event) {
            var folder = event.memo.inputData.folder;
            var toFolder = event.memo.inputData.toFolder;
            repositorySearch.CopyMoveController.cancel();
            repositorySearch.foldersPanel.moveOrCopyFolder(folder, toFolder, true);
        });
        this.observe('folder:copyError', this.defaultErrorHandler);
        this.observe('folder:moved', function (event) {
            var folder = event.memo.inputData.folder;
            var toFolder = event.memo.inputData.toFolder;
            repositorySearch.CopyMoveController.cancel();
            repositorySearch.foldersPanel.moveOrCopyFolder(folder, toFolder);
        });
        this.observe('folder:moveError', this.defaultErrorHandler);
        this.observe('folder:updated', function (event) {
            var folder = event.memo.inputData.folder;
            var label = event.memo.responseData.label;
            var desc = event.memo.responseData.desc;
            repositorySearch.foldersPanel.updateFolder(folder, label, desc);
        });
        this.observe('folder:updateError', this.defaultErrorHandler);
    },
    initResourceEvents: function () {
        this.observe('resource:deleted', function (event) {
            var resources = event.memo.inputData.resources;
            var response = event.memo.responseData;
            if (response.dependentResources) {
                dialogs.dependentResources.show(response.dependentResources, null, {
                    okOnly: true,
                    canSave: false,
                    topMessage: repositorySearch.messages['dialog.dependencies.resources.message'],
                    bottomMessage: repositorySearch.messages['dialog.dependencies.resources.deleteMessage']
                });
            } else {
                repositorySearch.resultsPanel.removeResources(resources);
            }
        });
        this.observe('resource:deleteError', this.defaultErrorHandler);
        this.observe('resource:copied', function (event) {
            var folder = event.memo.inputData.folder;
            repositorySearch.foldersPanel.reselectFolder(folder);
        });
        this.observe('resource:copyError', this.defaultErrorHandler);
        this.observe('resource:moved', function (event) {
            var toFolder = event.memo.inputData.folder;
            var selectedFolder = repositorySearch.model.getSelectedFolder();
            repositorySearch.foldersPanel.reselectFolder(toFolder);
            repositorySearch.foldersPanel.reselectFolder(selectedFolder);
        });
        this.observe('resource:moveError', this.defaultErrorHandler);
        this.observe('resource:updated', function (event) {
            repositorySearch.resultsPanel.updateResource(new Resource(event.memo.responseData));
        });
        this.observe('resource:updateError', this.defaultErrorHandler);
        this.observe('resource:generated', function (event) {
            var inputData = event.memo.inputData;
            var data = event.memo.responseData;
            var resource = new Resource(data);
            if (inputData.run) {
                var action = repositorySearch.RedirectAction.createRunResourceAction(resource, false);
                action.invokeAction();
            } else {
                if (repositorySearch.mode == repositorySearch.Mode.BROWSE) {
                    if (inputData.location == repositorySearch.foldersPanel.getSelectedUri()) {
                        repositorySearch.foldersPanel.doBrowse();
                    }
                } else {
                    repositorySearch.fire(repositorySearch.Event.SEARCH_SEARCH, { text: repositorySearch.model.getTextState() });
                }
            }
            dialogs.systemConfirm.show(repositorySearch.messages['RM_REPORT_CREATED'], 5000);
        });
        this.observe('resource:generateError', function (event) {
            var error = event.memo.responseData;
            var msg = error.msg;
            var data = error.data;
            var action;
            if(msg && msg.indexOf("SYSTEM_CONFIRM_REQUIRED") > -1) {
                if (data) {
                    var dialog = new ConfirmationDialog({text: Utils.restOfString(msg, "SYSTEM_CONFIRM_REQUIRED:")});
                    dialog.on("button:yes", function(){
                        data.overwrite = true;

                        var name = repositorySearch.ResourceAction.GENERATE;
                        action = new repositorySearch.ServerAction.createGenerateAction(name, data);
                        action.invokeAction();
                        dialog.remove();
                    });
                    dialog.on("button:no", function(){
                        dialog.remove();
                    });
                    dialog.open();
                }
            } else {
                repositorySearch.defaultErrorHandler(event);
            }
        });
        this.observe('resource:converted', function (event) {
            dialogs.systemConfirm.show(event.memo.responseData || event.memo, 5000);
            if (repositorySearch.mode == repositorySearch.Mode.BROWSE) {
                repositorySearch.foldersPanel.doBrowse();
            } else {
                repositorySearch.fire(repositorySearch.Event.SEARCH_SEARCH, { text: repositorySearch.model.getTextState() });
            }
        });
        this.observe('resource:convertError', this.defaultErrorHandler);
    },
    defaultErrorHandler: function (event) {
        this.errorDialog && this.errorDialog.remove();
        this.errorDialog = new AlertDialog({ title: i18n['dialog.dependencies.title'], id: 'dependenciesDialogId', titleContainerId: 'dependenciesDialogTitle', bodyContainerId: 'dependenciesDialogBody' });
        this.errorDialog.setMessage(event.memo.responseData || event.memo);
        this.errorDialog.open();

        repositorySearch.sortersPanel.enableItems();
    },
    initThemeEvents: function () {
        this.observe('theme:updated', function (event) {
            var data = event.memo.responseData;
            if (data && data.refresh) {
                document.body.style.cursor = 'wait';
                window.location.reload(true);
            } else {
                var folder = repositorySearch && repositorySearch.model && repositorySearch.model.getContextFolder() && repositorySearch.model.getContextFolder().getParentFolder();
                if (folder && repositorySearch.foldersPanel && repositorySearch.foldersPanel.refreshFolder) {
                    repositorySearch.foldersPanel.refreshFolder(folder);
                }
            }
        });
        this.observe('theme:reuploaded', function (event) {
            if (event.memo.responseData.isActiveTheme) {
                var uri = event.memo.responseData.themeUri;
                var action = new repositorySearch.ServerAction.createFolderAction(repositorySearch.ThemeAction.SETTHEME, { folderUri: uri });
                action.invokeAction();
            }
        });
        this.observe('theme:error', function (event) {
            var data = event.memo.responseData;
            if (data) {
                alert(data);
            }
        });
    },
    getMessage: function (messageId, object) {
        var message = repositorySearch.messages[messageId];
        return message ? new Template(message).evaluate(object ? object : {}) : '';
    },
    /**
     * TODO: add comment.
     *
     * @param action
     */
    _createAction: function (actionFactory, actionName) {
        var action;
        if (actionName.endsWith('FolderAction')) {
            var folder = repositorySearch.model.getContextFolder();
            action = actionFactory['create' + actionName].call(null, folder);
        } else if (actionName.endsWith('ResourceAction') || actionName.endsWith('RunAction')) {
            var resource = repositorySearch.model.getContextResource();
            action = actionFactory['create' + actionName].call(null, resource);
        } else {
            action = actionFactory['create' + actionName].call();
        }
        return action;
    },
    observe: function (eventName, handler) {
        this._getContainer().observe(eventName, handler);
    },
    stopObserving: function (eventName, handler) {
        this._getContainer().stopObserving(eventName, handler);
    },
    fire: function (eventName, memo) {
        this._getContainer().fire(eventName, memo);
    },
    _getContainer: function () {
        if (!this._container) {
            this._container = document.body;
        }
        return this._container;
    }
};    //////////////////////////////////
// Repository search model object
//////////////////////////////////
//////////////////////////////////
// Repository search model object
//////////////////////////////////
repositorySearch.model = {
    _organizationsFolderUri: null,
    _rootFolderUri: null,
    _tempFolderUri: null,
    _publicFolderUri: null,
    _folderSeparator: null,
    _configuration: {},
    _serverState: {},
    _uiState: {
        selectedFolder: null,
        contextFolder: null,
        contextResource: null,
        selectedResources: []
    },
    _isAdministrator: false,
    setOrganizationsFolderUri: function (uri) {
        this._organizationsFolderUri = uri;
    },
    getOrganizationsFolderUri: function () {
        return this._organizationsFolderUri;
    },
    setRootFolderUri: function (uri) {
        this._rootFolderUri = uri;
    },
    getRootFolderUri: function () {
        return this._rootFolderUri;
    },
    setTempFolderUri: function (uri) {
        this._tempFolderUri = uri;
    },
    getTempFolderUri: function () {
        return this._tempFolderUri;
    },
    setPublicFolderUri: function (uri) {
        this._publicFolderUri = uri;
    },
    getPublicFolderUri: function () {
        return this._publicFolderUri;
    },
    setFolderSeparator: function (folderSeparator) {
        this._folderSeparator = folderSeparator;
    },
    getFolderSeparator: function () {
        return this._folderSeparator;
    },
    setConfiguration: function (configuration) {
        this._configuration = configuration;
    },
    /**
     * Gets configuration of the repository search.
     */
    getConfiguration: function () {
        return this._configuration;
    },
    /**
     * Gets configuration of filters
     */
    getFiltersConfiguration: function () {
        return this.getConfiguration().filters;
    },
    /**
     * Gets configuration of sorters.
     */
    getSortersConfiguration: function () {
        return this.getConfiguration().sorters;
    },
    setServerState: function (state) {
        this._serverState = state;
    },
    /**
     * Gets Server state of the repository search.
     */
    getServerState: function () {
        return this._serverState;
    },
    /**
     * Gets UI state of the repository search.
     */
    getUIState: function () {
        return this._uiState;
    },
    setAdministrator: function (isAdministrator) {
        this._isAdministrator = isAdministrator;
    },
    isAdministrator: function () {
        return this._isAdministrator;
    },
    /**
     * Gets state of filters.
     */
    getFiltersState: function () {
        return this.getServerState().customFilters;
    },
    /**
     * Gets state of sorters.
     */
    getSortState: function () {
        return this.getServerState().sortBy;
    },
    /**
     * Gets state of text.
     */
    getTextState: function () {
        return this.getServerState().text;
    },
    /**
     * Gets state of folderUri.
     */
    getFolderUriState: function () {
        return this.getServerState().folderUri;
    },
    setSelectedFolder: function (folder) {
        this.getUIState().selectedFolder = folder;

        return folder;
    },
    getSelectedFolder: function () {
        return this.getUIState().selectedFolder;
    },
    setContextFolder: function (folder) {
        this.getUIState().contextFolder = folder;

        return folder;
    },
    getContextFolder: function () {
        return this.getUIState().contextFolder;
    },
    setSelectedResources: function (resources) {
        this.getUIState().selectedResources = resources;

        return resources;
    },
    getSelectedResources: function () {
        return this.getUIState().selectedResources;
    }
};    /////////////////////////////////////////
// Repository search action model object
/////////////////////////////////////////
/////////////////////////////////////////
// Repository search action model object
/////////////////////////////////////////
repositorySearch.actionModel = {
    bulkActions: {
        RUN: {
            buttonId: 'run',
            action: invokeBulkAction,
            actionArgs: 'Run',
            test: canAllBeRun
        },
        EDIT: {
            buttonId: 'edit',
            action: invokeBulkAction,
            actionArgs: 'Edit',
            test: canAllBeEdited
        },
        OPEN: {
            buttonId: 'open',
            action: invokeBulkAction,
            actionArgs: 'Open',
            test: canAllBeOpened
        },
        COPY: {
            buttonId: 'copy',
            action: invokeBulkAction,
            actionArgs: 'Copy',
            test: canAllBeCopied
        },
        CUT: {
            buttonId: 'cut',
            action: invokeBulkAction,
            actionArgs: 'Move',
            test: canAllBeMoved
        },
        PASTE: {
            buttonId: 'paste',
            action: invokeFolderAction,
            actionArgs: 'PasteResources',
            test: canAllBePasted
        },
        REMOVE: {
            buttonId: 'remove',
            action: invokeBulkAction,
            actionArgs: 'Delete',
            test: canAllBeDeleted
        }
    },
    _holderId: 'searchActionModel',
    _folderMenu: 'folder_mutton',
    _resourceMenu: 'resource_menu',
    _resourceBulkMenu: 'resource_bulk_menu',
    initialize: function () {
        var page = jQuery('#' + layoutModule.PAGE_BODY_ID)[0];
        page && page.observe(isSupportsTouch() ? 'touchstart' : 'click', function (event) {
            if (isIPad()) {
                !isRightClick(event) && event.touches.length == 1 && actionModel.hideMenu();
            } else {
                !isRightClick(event) && actionModel.hideMenu();
            }
        });    //var treeContainer = ($(repositorySearch.foldersPanel.getTreeId()).parent());
        //treeContainer.observe('scroll', function(event) {!isRightClick(event) && actionModel.hideMenu()});
    },
    showFolderMenu: function (event, coordinates, label) {
        actionModel.showDynamicMenu(this._folderMenu, event, null, coordinates, this._holderId, undefined, label);
    },
    showResourceBulkMenu: function (event, coordinates) {
        actionModel.showDynamicMenu(this._resourceBulkMenu, event, null, coordinates, this._holderId);
    },
    showResourceMenu: function (event, coordinates) {
        actionModel.showDynamicMenu(this._resourceMenu, event, null, coordinates, this._holderId);
    },
    refreshToolbar: function (event) {
        repositorySearch.toolbar.refresh();
    }
};
repositorySearch.CopyMoveController = {
    object: null,
    _move: false,
    _copy: false,
    _dndMode: false,
    onMove: function () {
        repositorySearch.CursorManager.move();
        repositorySearch.actionModel.refreshToolbar();
    },
    onCopy: function () {
        repositorySearch.CursorManager.copy();
        repositorySearch.actionModel.refreshToolbar();
    },
    onCancel: function () {
        repositorySearch.CursorManager.none();
        repositorySearch.actionModel.refreshToolbar();
    },
    isObjectInstanceOf: function (type) {
        var object = this.object;
        if (!object) {
            return false;
        }
        if (this.isBulkAction()) {
            return object.length == object.findAll(function (o) {
                return o instanceof type;
            }).length;
        } else {
            return object instanceof type;
        }
    },
    isMoveResource: function () {
        return this._move && this.isObjectInstanceOf(Resource);
    },
    isMoveFolder: function () {
        return this._move && this.isObjectInstanceOf(Folder);
    },
    isCopyResource: function () {
        return this._copy && this.isObjectInstanceOf(Resource);
    },
    isCopyFolder: function () {
        return this._copy && this.isObjectInstanceOf(Folder);
    },
    isBulkAction: function () {
        return Object.isArray(this.object);
    },
    move: function (object, isDnD) {
        this.cancel();
        this.object = object;
        this._move = true;
        this._copy = false;
        this._dndMode = isDnD;
        this.onMove();
    },
    copy: function (object, isDnD) {
        this.cancel();
        this.object = object;
        this._move = false;
        this._copy = true;
        this._dndMode = isDnD;
        this.onCopy();
    },
    cancel: function () {
        this.object = null;
        this._move = false;
        this._copy = false;
        this.onCancel();
    },
    //    dnd: function(dndMode) {
    //        this._dndMode = dndMode;
    //    },
    //
    isMove: function () {
        return this._move;
    },
    isCopy: function () {
        return this._copy;
    },
    isDnd: function () {
        return this._dndMode;
    }
};
repositorySearch.CursorManager = {
    container: null,
    className: [
        'copy_cursor',
        'move_cursor'
    ],
    initialize: function () {
        this.container = jQuery('#display')[0];
    },
    copy: function () {
        this.none();
        jQuery(this.container).addClass(this.className[0]);
    },
    move: function () {
        this.none();
        jQuery(this.container).addClass(this.className[1]);
    },
    none: function () {
        this.className.each(function (name, index) {
            jQuery(this.container).removeClass(this.className[index]);
        }.bind(this));
    }
};    /////////////////////////////////////////
// Repository search resource object
/////////////////////////////////////////
/////////////////////////////////////////
// Repository search resource object
/////////////////////////////////////////
var Resource = function (json) {
    this._json = json;
    this.name = json.name;
    this.label = json.label;
    this.description = json.description;
    this.date = json.date;
    this.dateTimestamp = json.dateTimestamp;
    this.dateTime = json.dateTime;
    this.updateDate = json.updateDate;
    this.updateDateTimestamp = json.updateDateTimestamp;
    this.updateDateTime = json.updateDateTime;
    this.URI = json.URI;
    this.URIString = json.URIString;
    this.parentUri = json.parentUri;
    this.parentFolder = json.parentFolder;
    this.resourceType = json.resourceType;
    this.type = json.type;
    if (json.fileType) {
        this.fileType = json.fileType;
    }
    this.isScheduled = json.scheduled;
    this.isFavorite = json.favorite;
    this._permissions = json.permissions;
    this.hasChildren = json.hasChildren;
    this.isChild = false;
    this.parentResource = null;
    this.isOpen = false;
    this._children = [];
};
Resource.addMethod('clone', function () {
    return new Resource(this._json);
});
Resource.addMethod('isReadable', function () {
    return this._permissions.include('r');
});
Resource.addMethod('isEditable', function () {
    return this._permissions.include('e');
});
Resource.addMethod('isRemovable', function () {
    return this._permissions.include('d');
});
Resource.addMethod('isAdministrable', function () {
    return this._permissions.include('a');
});
Resource.addMethod('typeEquals', function (type) {
    return this.resourceType === type;
});
Resource.addMethod('typeSuffix', function () {
    return this.resourceType.split('.').last();
});
Resource.addMethod('isLoaded', function () {
    return this._children.size() > 0;
});
Resource.addMethod('isFolder', function () {
    return false;
});
Resource.addMethod('getChildren', function (resource) {
    return this._children;
});
Resource.addMethod('addChild', function (resource) {
    resource.isChild = true;
    resource.parentResource = this;
    this._children.push(resource);
});
Resource.addMethod('updateChild', function (resource) {
    resource.isChild = true;
    resource.parentResource = this;
    var childIndex = -1;
    this._children.each(function (child, index) {
        if (child.URIString == resource.URIString) {
            childIndex = index;
        }
    });
    if (childIndex > -1) {
        this._children[childIndex] = resource;
    } else {
        this._children.push(resource);
    }
});
Resource.addMethod('permissionsToString', function () {
    var result = '';
    if (this.isEditable()) {
        result += repositorySearch.messages['permission.modify'];
    } else {
        result += repositorySearch.messages['permission.readOnly'];
    }
    if (this.isRemovable()) {
        result += ', ' + repositorySearch.messages['permission.delete'];
    }
    if (this.isAdministrable()) {
        result += ', ' + repositorySearch.messages['permission.administer'];
    }
    return result;
});    /////////////////////////////////////////
// Repository search folder object
/////////////////////////////////////////
/////////////////////////////////////////
// Repository search folder object
/////////////////////////////////////////
var Folder = function (node) {
    this.node = node;
    this.name = node.param.id;
    this.label = xssUtil.unescape(node.name);
    this.desc = node.param.extra.desc ? xssUtil.unescape(node.param.extra.desc) : '';
    this.description = xssUtil.unescape(this.desc);
    this.date = node.param.extra.date ? xssUtil.unescape(node.param.extra.date) : '';
    this.URI = node.param.uri;
    this.URIString = node.param.uri;
};
Folder.prototype = deepClone(Resource.prototype);
Folder.addMethod('clone', function () {
    return new Folder(this.node);
});
Folder.addMethod('getParentFolder', function () {
    return this.isRoot() ? null : new Folder(this.node.parent);
});
Folder.addMethod('getReadableUri', function () {
    if (this.isRoot()) {
        return '';
    }
    return this.getParentFolder().getReadableUri() + repositorySearch.model.getFolderSeparator() + this.label;
});
Folder.addMethod('isRoot', function () {
    return this.URI == repositorySearch.model.getRootFolderUri() || this.node.parent == null;
});
Folder.addMethod('isPublic', function () {
    return this.URI == repositorySearch.model.getPublicFolderUri();
});
Folder.addMethod('isTemp', function () {
    return this.URI == repositorySearch.model.getTempFolderUri();
});
Folder.addMethod('isOrganization', function () {
    return this.URI.match(repositorySearch.model.getOrganizationsFolderUri() + '/[^/]+$');
});
Folder.addMethod('isOrganizationsRoot', function () {
    return this.URI.match(repositorySearch.model.getOrganizationsFolderUri() + '$');
});
Folder.addMethod('isReadable', function () {
    return true;
});
Folder.addMethod('isEditable', function () {
    return this.node.param.extra.isWritable;
});
Folder.addMethod('isRemovable', function () {
    return this.node.param.extra.isRemovable;
});
Folder.addMethod('isAdministrable', function () {
    return this.node.param.extra.isAdministrable;
});
Folder.addMethod('isSelected', function () {
    return this.node.isSelected();
});
Folder.addMethod('equals', function (folder) {
    return folder && this.URI == folder.URI;
});
Folder.addMethod('isThemeFolder', function () {
    return this.node.param.extra && this.node.param.extra.isThemeFolder;
});
Folder.addMethod('isThemeRootFolder', function () {
    return this.node.param.extra && this.node.param.extra.isThemeRootFolder;
});
Folder.addMethod('isActiveThemeFolder', function () {
    return this.node.param.extra && this.node.param.extra.isActiveThemeFolder;
});
Folder.addMethod('isFolder', function () {
    return true;
});
var ResourcesUtils = {
    getResourceUris: function (resources) {
        var uriList = [];
        resources.each(function (resource) {
            uriList.push(resource.URIString ? resource.URIString : resource.URI);
        });
        return uriList;
    },
    getResourceUriAndTypeList: function (resources) {
        var list = [];
        resources.each(function (resource) {
            var uri = resource.URIString ? resource.URIString : resource.URI;
            list.push({
                URIString: uri,
                type: resource.type
            });
        });
        return list;
    },
    getSelectedResourceUris: function (resources) {
        return resources.map((resource)=>{
            return {
                uri: resource.URIString ? resource.URIString : resource.URI
            };
        });
    },
    checkNameLength: function (value) {
        return !value.blank() && value.length <= repositorySearch.model.getConfiguration().resourceNameMaxLength;
    },
    checkDescriptionLength: function (value) {
        return value.length <= repositorySearch.model.getConfiguration().resourceDescriptionMaxLength;
    },
    labelValidator: function (value) {
        var isValid = true;
        var errorMessage = '';
        if (value.blank() || (value.length > repositorySearch.model.getConfiguration().resourceLabelMaxLength)) {
            errorMessage = repositorySearch.messages['RE_INVALID_NAME_SIZE'].replace('{0}', repositorySearch.model.getConfiguration().resourceLabelMaxLength);
            isValid = false;
        }
        return {
            isValid: isValid,
            errorMessage: errorMessage
        };
    },
    descriptionValidator: function (value) {
        var isValid = true;
        var errorMessage = '';
        if (value.length > repositorySearch.model.getConfiguration().resourceDescriptionMaxLength) {
            errorMessage = repositorySearch.messages['RE_INVALID_DESC_SIZE'].replace('{0}', repositorySearch.model.getConfiguration().resourceDescriptionMaxLength);
            isValid = false;
        }
        return {
            isValid: isValid,
            errorMessage: errorMessage
        };
    },
    _fileTypeValidatorTemplate: function (value, expectedType) {
        var isValid = true;
        var errorMessage = '';
        if (!value || value.blank()) {
            errorMessage = repositorySearch.messages['RE_ENTER_FILE_NAME'];
            isValid = false;
        } else if (!value.toLowerCase().endsWith(expectedType)) {
            errorMessage = repositorySearch.messages['RE_INVALID_FILE_TYPE'].replace('{0}', expectedType);
            isValid = false;
        }
        return {
            isValid: isValid,
            errorMessage: errorMessage
        };
    },
    zipFileTypeValidator: function (value) {
        return ResourcesUtils._fileTypeValidatorTemplate(value, '.zip');
    },
    isCustomDataSource: function (resource) {
        return _.contains(Utils.getInitConfiguration().customDataSources, resource.resourceType);
    }
};
var Utils = {
    restOfString: function (str, fromStr) {
        var from = str.lastIndexOf(fromStr) + fromStr.length;
        return str.substring(from, str.length).trim();
    },
    getInitOptions: function () {
        return window.localContext.rsInitOptions || __jrsConfigs__.repositorySearch['localContext'].rsInitOptions;
    },
    getInitConfiguration: function () {
        return Utils.getInitOptions().configuration;
    }
};    ////////////////////////////////////
// Repository Search Initialization
////////////////////////////////////
////////////////////////////////////
// Repository Search Initialization
////////////////////////////////////
document.observe('element:contextmenu', repositorySearch.showContextMenu.bindAsEventListener(repositorySearch));
/*document.observe('key:escape', function (event) {
    actionModel.hideMenu();
    Event.stop(event);
});*/
document.observe('key:delete', function (event) {
    // for bug 25864 - to prevent warning message appearing when trying to delete text in any text input
    if (event.target instanceof HTMLInputElement || event.target instanceof HTMLTextAreaElement) {
        return;
    }
    if (canAllBeDeleted())
        invokeBulkAction('Delete');
});

// expose to global scope
window.canResourceBeCreated = canResourceBeCreated;
window.invokeResourceAction = invokeResourceAction;
window.invokeAction = invokeAction;
window.invokeFolderAction = invokeFolderAction;
window.invokeCreate = invokeCreate;
window.invokeBulkAction = invokeBulkAction;
window.invokeRedirectAction = invokeRedirectAction;
window.isPropertiesChanged = isPropertiesChanged;
window.isPermissionsChanged = isPermissionsChanged;
window.isCopyOrMove = isCopyOrMove;
window.isFolderCopyOrMove = isFolderCopyOrMove;
window.isResourceCopyOrMove = isResourceCopyOrMove;
window.isThemeFolder = isThemeFolder;
window.isNonActiveThemeFolder = isNonActiveThemeFolder;
window.isThemeRootFolder = isThemeRootFolder;
window.canThemeBeReuploaded = canThemeBeReuploaded;
window.canFolderBeCopied = canFolderBeCopied;
window.canFolderBeDeleted = canFolderBeDeleted;
window.canFolderBeEdited = canFolderBeEdited;
window.canFolderBeMoved = canFolderBeMoved;
window.canFolderBeCopiedOrMovedToFolder = canFolderBeCopiedOrMovedToFolder;
window.canAllBeCopiedOrMovedToFolder = canAllBeCopiedOrMovedToFolder;
window.canFolderBeExported = canFolderBeExported;
window.canFolderPermissionsBeAssigned = canFolderPermissionsBeAssigned;
window.canFolderPropertiesBeShowed = canFolderPropertiesBeShowed;
window.canFolderPropertiesBeEdited = canFolderPropertiesBeEdited;
window.canBeRun = canBeRun;
window.canBeGenerated = canBeGenerated;
window.canBeConverted = canBeConverted;
window.canBeOpened = canBeOpened;
window.canBeRunInBackground = canBeRunInBackground;
window.canResourceBeEdited = canResourceBeEdited;
window.canBeOpenedInDesigner = canBeOpenedInDesigner;
window.canCreateFolder = canCreateFolder;
window.canBeScheduled = canBeScheduled;
window.canResourceBeCopied = canResourceBeCopied;
window.canResourceBeMoved = canResourceBeMoved;
window.canResourceBeDeleted = canResourceBeDeleted;
window.canResourcePropertiesBeShowed = canResourcePropertiesBeShowed;
window.canResourcePropertiesBeEdited = canResourcePropertiesBeEdited;
window.canResourcePermissionsBeAssigned = canResourcePermissionsBeAssigned;
window.canAllBeRun = canAllBeRun;
window.canAllBeEdited = canAllBeEdited;
window.canAllBeOpened = canAllBeOpened;
window.canAllBeCopied = canAllBeCopied;
window.canAllBeMoved = canAllBeMoved;
window.canAllBePasted = canAllBePasted;
window.canAllPropertiesBeShowed = canAllPropertiesBeShowed;
window.canAllPropertiesBeEdited = canAllPropertiesBeEdited;
window.canAllBeDeleted = canAllBeDeleted;
window.canBeAddedToFavorites = canBeAddedToFavorites;
window.canBeRemovedFromFavorites = canBeRemovedFromFavorites;


export {
    repositorySearch,
    ResourcesUtils,
    invokeResourceAction,
    invokeFolderAction,
    isPropertiesChanged,
    isPermissionsChanged,
    Folder,
    canFolderBeCopied,
    canFolderBeMoved,
    canFolderBeCopiedOrMovedToFolder,
    canAllBeCopiedOrMovedToFolder,
    canBeRun,
    canBeOpened,
    invokeBulkAction,
    invokeRedirectAction,
    canBeScheduled,
    canBeAddedToFavorites,
    canBeRemovedFromFavorites
};