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

import Tree from 'js-sdk/src/common/component/tree/Tree';
import $ from 'jquery';
import _ from 'underscore';
import i18n from 'js-sdk/src/i18n/CommonBundle.properties';
import TreeDataLayer from 'js-sdk/src/common/component/tree/TreeDataLayer';
import processorsFactory from '../dialog/processor/factory/processorsFactory';
import TooltipTreePlugin from 'js-sdk/src/common/component/tree/plugin/TooltipPlugin';
import repositoryResourceTypes from '../enum/repositoryResourceTypes';
import resourcesTreeGetDataUriFnUtil from '../util/resourcesTreeGetDataUriFnUtil';
import extractRootLevelDataFromHtmlResponse from '../util/extractRootLevelDataFromHtmlResponse';
import tooltipTemplate from '../dialog/resourceChooser/template/repositoryFolderChooserDialogTooltipTemplate.htm';
import repositoryFoldersTreeLevelTemplate from '../dialog/resourceChooser/template/repositoryFoldersTreeLevelTemplate.htm';
import jrsConfigs from 'js-sdk/src/jrs.configs';

var LIST_ITEM_HEIGHT = 22;
var TREE_BUFFER_SIZE = 5000;

var repositoryFoldersTreeFactory = function (options) {
    options = options || {};
    if (!options.types) {
        throw 'Resources types required';
    }
    if (!options.listView) {
        return getTreeInstance(options);
    } else {
        return getTreeListInstance(options);
    }
};
function getTreeConstructor(options) {
    var tooltipOptions = _.extend({}, {
        i18n: i18n,
        contentTemplate: tooltipTemplate
    }, options.tooltipOptions);
    return options.constr || Tree.use(TooltipTreePlugin, tooltipOptions).create();
}
function getTreeInstance(options) {
    options = options || {};
    var processors = options.processors = _.map(options.processors, function (processorName) {
        return processorsFactory(processorName);
    });
    var TreeConstructor = getTreeConstructor(options);
    return TreeConstructor.instance({
        listItemHeight: LIST_ITEM_HEIGHT,
        bufferSize: options.treeBufferSize || TREE_BUFFER_SIZE,
        additionalCssClasses: 'folders',
        collapserSelector: options.collapserSelector,
        openClass: options.openClass,
        closedClass: options.closedClass,
        levelDataId: 'uri',
        itemsTemplate: options.treeItemsTemplate || repositoryFoldersTreeLevelTemplate,
        collapsed: true,
        lazyLoad: true,
        rootless: true,
        getDataUri: resourcesTreeGetDataUriFnUtil({
            getFolderUri: getFolderUri,
            contextPath: jrsConfigs.contextPath,
            recursive: false,
            type: options.types,
            containerType: options.containerType,
            forceTotalCount: true,
            forceFullPage: true
        }),
        selection: {
            allowed: true,
            multiple: false
        },
        customDataLayers: {
            //workaround for correct viewing of '/public' and '/' folder labels
            '/': getCustomRootDataLayer(options)
        },
        processors: processors,
        getDataArray: function (data) {
            if (data && data[repositoryResourceTypes.RESOURCE_LOOKUP]) {
                data = data[repositoryResourceTypes.RESOURCE_LOOKUP];
                var levelDataId = this.levelDataId, first = _.first(data);
                if (!first[levelDataId].match(/^\/public/)) {
                    data = _.map(data, function (resource) {
                        resource.uri = '/root' + resource[levelDataId];
                        return resource;
                    });
                }
                return data;
            }
            return [];
        },
        getDataSize: function (data, status, xhr) {
            return +xhr.getResponseHeader('Total-Count');
        },
        getDataLayer: customizedGetDataLayerFunc
    });
}
function getTreeRootLevelProcessors(processors) {
    var rootLevelProcessors = [];
    var filterPublicFolderProcessor = processorsFactory('filterPublicFolderProcessor');
    _.each(processors, function (processor) {
        if (processor !== filterPublicFolderProcessor) {
            rootLevelProcessors.push(processor);
        }
    });
    return rootLevelProcessors;
}
function getCustomRootDataLayer(options) {
    var processors = options.processors;
    var rootLevelProcessors = getTreeRootLevelProcessors(processors);
    var uri = '/flow.html?_flowId=searchFlow&method=getNode&provider=repositoryExplorerTreeFoldersProvider&uri=/&depth=1';
    return _.extend(new TreeDataLayer({
        dataUriTemplate: options.contextPath + uri,
        processors: rootLevelProcessors,
        getDataArray: function (data) {
            data = extractRootLevelDataFromHtmlResponse(data);

            var publicFolder = _.find(data.children, function (item) {
                    return item.uri === '/public';
                }), fakeRoot = [{
                    id: '/root',
                    label: data.label,
                    uri: '/',
                    resourceType: 'folder',
                    permissionMask: computePermissionMask(data.extra),
                    _links: { content: '@fakeContentLink' }
                }];
            if (publicFolder) {
                fakeRoot.push({
                    id: '/public',
                    label: publicFolder.label,
                    uri: '/public',
                    resourceType: 'folder',
                    permissionMask: computePermissionMask(publicFolder.extra),
                    _links: { content: '@fakeContentLink' }
                });
            }
            return fakeRoot;
        }
    }), {
        accept: 'text/html',
        dataType: 'text'
    });
}
function getTreeListInstance(options) {
    var processors = options.processors;
    var TreeConstructor = getTreeConstructor(options);
    return TreeConstructor.instance({
        itemsTemplate: options.treeItemsTemplate || repositoryFoldersTreeLevelTemplate,
        listItemHeight: LIST_ITEM_HEIGHT,
        selection: {
            allowed: true,
            multiple: false
        },
        rootless: true,
        collapsed: true,
        lazyLoad: true,
        getDataUri: resourcesTreeGetDataUriFnUtil({
            getFolderUri: getFolderUri,
            contextPath: jrsConfigs.contextPath,
            recursive: true,
            type: options.types,
            containerType: options.containerType,
            forceTotalCount: true,
            forceFullPage: true
        }),
        levelDataId: 'uri',
        cache: {
            searchKey: 'searchString',
            pageSize: 100
        },
        processors: processors,
        customDataLayers: {},
        getDataLayer: customizedGetDataLayerFunc,
        getDataArray: function (data) {
            return data && data[repositoryResourceTypes.RESOURCE_LOOKUP] ? data[repositoryResourceTypes.RESOURCE_LOOKUP] : [];
        },
        getDataSize: function (data, status, xhr) {
            return +xhr.getResponseHeader('Total-Count');
        }
    });
}

function customizedGetDataLayerFunc(level) {
    var resourceType;
    level = level.item ? level : this.getLevel(level.id);
    if (level) {
        resourceType = level.item.value.resourceType;
        return this.customDataLayers && this.customDataLayers[level.id] ? this.customDataLayers[level.id] : this.defaultDataLayer;
    }
}

function getFolderUri(id) {
    if (id.match(/^\/root/)) {
        return id.replace("/root", "");
    } else {
        return id;
    }
}
function computePermissionMask(extra) {
    var mask = 2;
    extra.isWritable && (mask = mask | 4);
    extra.isRemovable && (mask = mask | 16);
    extra.isAdministrable && (mask = 1);
    return mask;
}
repositoryFoldersTreeFactory.settings = jrsConfigs;
export default repositoryFoldersTreeFactory;