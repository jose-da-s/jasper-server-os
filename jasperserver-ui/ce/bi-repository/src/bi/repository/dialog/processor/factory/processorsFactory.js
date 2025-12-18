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

import _ from 'underscore';
import defaultSettings from 'js-sdk/src/jrs.configs';
import repositoryResourceTypes from '../../../enum/repositoryResourceTypes';
import commonMessagesBundle from 'js-sdk/src/i18n/CommonBundle.properties';
function cssClassItemProcessor(item) {
    switch (item.value.resourceType) {
    case repositoryResourceTypes.REPORT_UNIT:
        item.cssClass = 'report';
        break;
    case repositoryResourceTypes.DOMAIN_TOPIC:
        item.cssClass = 'domain topic';
        break;
    case repositoryResourceTypes.SEMANTIC_LAYER_DATA_SOURCE:
        item.cssClass = 'domain';
        break;
    default:
        break;
    }
    return item;
}
var processors = {
    folderTreeProcessor: {
        processItem: function (item) {
            item._readonly = !(item.value.permissionMask == 1 || item.value.permissionMask & 4);
            return item;
        }
    },
    treeNodeProcessor: {
        processItem: function (item) {
            item._node = repositoryResourceTypes.FOLDER === item.value.resourceType;
            return item;
        }
    },
    i18nItemProcessor: {
        processItem: function (item) {
            item.i18n = commonMessagesBundle;
            return item;
        }
    },
    filterPublicFolderProcessor: {
        processItem: function (item) {
            if (item.value.uri !== '/root' + defaultSettings.publicFolderUri && item.value.uri !== '/root' + defaultSettings.tempFolderUri) {
                return item;
            }
        }
    },
    filterEmptyFoldersProcessor: {
        processItem: function (item) {
            if (!(item.value.resourceType === 'folder' && item.value._links.content === '')) {
                return item;
            }
        }
    },
    cssClassItemProcessor: { processItem: cssClassItemProcessor },
    fakeUriProcessor: {
        processItem: function (item) {
            if (item.value.uri && item.value.uri.match(/^\/root/)) {
                item.value.uri = item.value.uri.replace('/root', '');
            }
            return item;
        }
    }
};
function processorsFactory(processorName) {
    return processors[processorName];
}
export default processorsFactory;