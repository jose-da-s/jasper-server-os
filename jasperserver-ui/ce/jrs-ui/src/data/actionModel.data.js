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

export default {
    'folder_mutton': [
        {
            'type': 'simpleAction',
            'clientTest': 'canCreateFolder',
            'className': 'up',
            'text': 'Add Folder',
            'action': 'invokeFolderAction',
            'actionArgs': ['CreateFolder']
        },
        {
            'type': 'selectAction',
            'clientTest': 'canResourceBeCreated',
            'className': 'flyout',
            'text': 'Add Resource',
            'children': [
                {
                    'type': 'optionAction',
                    'clientTest': 'canResourceBeCreated',
                    'clientTestArgs': ['ReportDataSource'],
                    'className': 'up',
                    'text': 'Data Source',
                    'action': 'invokeCreate',
                    'actionArgs': ['ReportDataSource']
                },
                {
                    'type': 'optionAction',
                    'clientTest': 'canResourceBeCreated',
                    'clientTestArgs': ['DataType'],
                    'className': 'up',
                    'text': 'Datatype',
                    'action': 'invokeCreate',
                    'actionArgs': ['DataType']
                },
                {
                    'type': 'optionAction',
                    'clientTest': 'canResourceBeCreated',
                    'clientTestArgs': ['SemanticLayerDataSource'],
                    'className': 'up',
                    'text': 'Domain',
                    'action': 'invokeCreate',
                    'actionArgs': ['SemanticLayerDataSource']
                },
                {
                    'type': 'selectAction',
                    'clientTest': 'canResourceBeCreated',
                    'clientTestArgs': ['FileResource'],
                    'className': 'flyout',
                    'text': 'File',
                    'children': [
                        {
                            'type': 'optionAction',
                            'className': 'up',
                            'text': 'Access Grant Schema',
                            'action': 'invokeCreate',
                            'actionArgs': [
                                'FileResource',
                                'accessGrantSchema'
                            ]
                        },
                        {
                            'type': 'optionAction',
                            'className': 'up',
                            'text': 'Font',
                            'action': 'invokeCreate',
                            'actionArgs': [
                                'FileResource',
                                'font'
                            ]
                        },
                        {
                            'type': 'optionAction',
                            'className': 'up',
                            'text': 'Image',
                            'action': 'invokeCreate',
                            'actionArgs': [
                                'FileResource',
                                'img'
                            ]
                        },
                        {
                            'type': 'optionAction',
                            'className': 'up',
                            'text': 'CSS',
                            'action': 'invokeCreate',
                            'actionArgs': [
                                'FileResource',
                                'css'
                            ]
                        },
                        {
                            'type': 'optionAction',
                            'className': 'up',
                            'text': 'JAR',
                            'action': 'invokeCreate',
                            'actionArgs': [
                                'FileResource',
                                'jar'
                            ]
                        },
                        {
                            'type': 'optionAction',
                            'className': 'up',
                            'text': 'JRXML',
                            'action': 'invokeCreate',
                            'actionArgs': [
                                'FileResource',
                                'jrxml'
                            ]
                        },
                        {
                            'type': 'optionAction',
                            'className': 'up',
                            'text': 'Resource Bundle',
                            'action': 'invokeCreate',
                            'actionArgs': [
                                'FileResource',
                                'prop'
                            ]
                        },
                        {
                            'type': 'optionAction',
                            'className': 'up',
                            'text': 'Style Template',
                            'action': 'invokeCreate',
                            'actionArgs': [
                                'FileResource',
                                'jrtx'
                            ]
                        },
                        {
                            'type': 'optionAction',
                            'className': 'up',
                            'text': 'XML',
                            'action': 'invokeCreate',
                            'actionArgs': [
                                'FileResource',
                                'xml'
                            ]
                        }
                    ]
                },
                {
                    'type': 'optionAction',
                    'clientTest': 'canResourceBeCreated',
                    'clientTestArgs': ['InputControl'],
                    'className': 'up',
                    'text': 'Input Control',
                    'action': 'invokeCreate',
                    'actionArgs': ['InputControl']
                },
                {
                    'type': 'optionAction',
                    'clientTest': 'canResourceBeCreated',
                    'clientTestArgs': ['ReportUnit'],
                    'className': 'up',
                    'text': 'JasperReport',
                    'action': 'invokeCreate',
                    'actionArgs': ['ReportUnit']
                },
                {
                    'type': 'optionAction',
                    'clientTest': 'canResourceBeCreated',
                    'clientTestArgs': ['ListOfValues'],
                    'className': 'up',
                    'text': 'List of Values',
                    'action': 'invokeCreate',
                    'actionArgs': ['ListOfValues']
                },
                {
                    'type': 'optionAction',
                    'clientTest': 'canResourceBeCreated',
                    'clientTestArgs': ['Query'],
                    'className': 'up',
                    'text': 'Query',
                    'action': 'invokeCreate',
                    'actionArgs': ['Query']
                }
            ]
        },
        {
            'type': 'separator',
            'className': 'separator'
        },
        { 'type': 'separator' },
        {
            'type': 'simpleAction',
            'clientTest': 'canFolderBeCopied',
            'className': 'up',
            'text': 'Copy',
            'action': 'invokeFolderAction',
            'actionArgs': ['CopyFolder']
        },
        {
            'type': 'simpleAction',
            'clientTest': 'canFolderBeMoved',
            'className': 'up',
            'text': 'Cut',
            'action': 'invokeFolderAction',
            'actionArgs': ['MoveFolder']
        },
        {
            'type': 'simpleAction',
            'clientTest': 'canFolderBeCopiedOrMovedToFolder',
            'className': 'up',
            'text': 'Paste',
            'action': 'invokeFolderAction',
            'actionArgs': ['PasteFolder']
        },
        {
            'type': 'simpleAction',
            'clientTest': 'canAllBeCopiedOrMovedToFolder',
            'className': 'up',
            'text': 'Paste',
            'action': 'invokeFolderAction',
            'actionArgs': ['PasteResources']
        },
        {
            'type': 'simpleAction',
            'clientTest': 'canFolderBeDeleted',
            'className': 'up',
            'text': 'Delete',
            'action': 'invokeFolderAction',
            'actionArgs': ['DeleteFolder']
        },
        { 'type': 'separator' },
        {
            'type': 'simpleAction',
            'clientTest': 'isNonActiveThemeFolder',
            'className': 'up',
            'text': 'Set as Active Theme',
            'action': 'invokeFolderAction',
            'actionArgs': ['SetActiveThemeFolder']
        },
        {
            'type': 'simpleAction',
            'clientTest': 'isThemeFolder',
            'className': 'up',
            'text': 'Download the Theme',
            'action': 'invokeFolderAction',
            'actionArgs': ['DownloadTheme']
        },
        { 'type': 'separator' },
        {
            'type': 'simpleAction',
            'clientTest': 'isThemeRootFolder',
            'className': 'up',
            'text': 'Upload a Theme',
            'action': 'invokeFolderAction',
            'actionArgs': ['UploadTheme']
        },
        { 'type': 'separator' },
        {
            'type': 'simpleAction',
            'clientTest': 'canFolderPermissionsBeAssigned',
            'className': 'up',
            'text': 'Permissions...',
            'action': 'invokeFolderAction',
            'actionArgs': ['AssignPermissions']
        },
        {
            'type': 'simpleAction',
            'clientTest': 'canFolderPropertiesBeShowed',
            'className': 'up',
            'text': 'Properties...',
            'action': 'invokeFolderAction',
            'actionArgs': ['ShowFolderProperties']
        },
        {
            'type': 'simpleAction',
            'clientTest': 'canFolderPropertiesBeEdited',
            'className': 'up',
            'text': 'Properties...',
            'action': 'invokeFolderAction',
            'actionArgs': ['EditFolderProperties']
        }
    ],
    'resource_menu': [
        {
            'type': 'simpleAction',
            'clientTest': 'canBeRun',
            'className': 'up',
            'text': 'Run',
            'action': 'invokeRedirectAction',
            'actionArgs': ['RunResourceAction']
        },
        {
            'type': 'simpleAction',
            'clientTest': 'canBeRunInBackground',
            'className': 'up',
            'text': 'Run in Background...',
            'action': 'invokeRedirectAction',
            'actionArgs': ['RunInBackgroundResourceAction']
        },
        {
            'type': 'simpleAction',
            'clientTest': 'canBeScheduled',
            'className': 'up',
            'text': 'Schedule...',
            'action': 'invokeRedirectAction',
            'actionArgs': ['ScheduleAction']
        },
        {
            'type': 'simpleAction',
            'clientTest': 'canResourceBeEdited',
            'className': 'up',
            'text': 'Edit',
            'action': 'invokeRedirectAction',
            'actionArgs': ['EditResourceAction']
        },
        {
            'type': 'simpleAction',
            'clientTest': 'canBeOpenedInDesigner',
            'className': 'up',
            'text': 'Open in Designer...',
            'action': 'invokeRedirectAction',
            'actionArgs': ['OpenResourceAction']
        },
        {
            'type': 'simpleAction',
            'clientTest': 'canBeOpened',
            'className': 'up',
            'text': 'Open',
            'action': 'invokeRedirectAction',
            'actionArgs': ['OpenResourceAction']
        },
        { 'type': 'separator' },
        {
            'type': 'simpleAction',
            'clientTest': 'canBeAddedToFavorites',
            'className': 'up',
            'text': 'Add to Favorites',
            'action': 'invokeResourceAction',
            'actionArgs': ['ResourceFavoriteAction']
        },
        {
            'type': 'simpleAction',
            'clientTest': 'canBeRemovedFromFavorites',
            'className': 'up',
            'text': 'Remove from Favorites',
            'action': 'invokeResourceAction',
            'actionArgs': ['ResourceFavoriteAction']
        },
        { 'type': 'separator' },
        {
            'type': 'simpleAction',
            'clientTest': 'canResourceBeCopied',
            'className': 'up',
            'text': 'Copy',
            'action': 'invokeResourceAction',
            'actionArgs': ['Copy']
        },
        {
            'type': 'simpleAction',
            'clientTest': 'canResourceBeMoved',
            'className': 'up',
            'text': 'Cut',
            'action': 'invokeResourceAction',
            'actionArgs': ['Move']
        },
        {
            'type': 'simpleAction',
            'clientTest': 'canResourceBeDeleted',
            'className': 'up',
            'text': 'Delete',
            'action': 'invokeResourceAction',
            'actionArgs': ['Delete']
        },
        { 'type': 'separator' },
        {
            'type': 'simpleAction',
            'clientTest': 'canResourcePermissionsBeAssigned',
            'className': 'up',
            'text': 'Permissions...',
            'action': 'invokeResourceAction',
            'actionArgs': ['AssignPermissionsToResourceAction']
        },
        {
            'type': 'simpleAction',
            'clientTest': 'canResourcePropertiesBeShowed',
            'className': 'up',
            'text': 'Properties...',
            'action': 'invokeResourceAction',
            'actionArgs': ['ShowProperties']
        },
        {
            'type': 'simpleAction',
            'clientTest': 'canResourcePropertiesBeEdited',
            'className': 'up',
            'text': 'Properties...',
            'action': 'invokeResourceAction',
            'actionArgs': ['EditProperties']
        }
    ],
    'resource_bulk_menu': [
        {
            'type': 'simpleAction',
            'clientTest': 'canAllBeRun',
            'className': 'up',
            'text': 'Run',
            'action': 'invokeBulkAction',
            'actionArgs': ['Run']
        },
        {
            'type': 'simpleAction',
            'clientTest': 'canAllBeEdited',
            'className': 'up',
            'text': 'Edit',
            'action': 'invokeBulkAction',
            'actionArgs': ['Edit']
        },
        {
            'type': 'simpleAction',
            'clientTest': 'canAllBeOpened',
            'className': 'up',
            'text': 'Open',
            'action': 'invokeBulkAction',
            'actionArgs': ['Open']
        },
        { 'type': 'separator' },
        {
            'type': 'simpleAction',
            'clientTest': 'canBeAddedToFavorites',
            'className': 'up',
            'text': 'Add to Favorites',
            'action': 'invokeResourceAction',
            'actionArgs': ['ResourceFavoriteAction']
        },
        {
            'type': 'simpleAction',
            'clientTest': 'canBeRemovedFromFavorites',
            'className': 'up',
            'text': 'Remove from Favorites',
            'action': 'invokeResourceAction',
            'actionArgs': ['ResourceFavoriteAction']
        },
        { 'type': 'separator' },
        {
            'type': 'simpleAction',
            'clientTest': 'canAllBeCopied',
            'className': 'up',
            'text': 'Copy',
            'action': 'invokeBulkAction',
            'actionArgs': ['Copy']
        },
        {
            'type': 'simpleAction',
            'clientTest': 'canAllBeMoved',
            'className': 'up',
            'text': 'Cut',
            'action': 'invokeBulkAction',
            'actionArgs': ['Move']
        },
        {
            'type': 'simpleAction',
            'clientTest': 'canAllBeDeleted',
            'className': 'up',
            'text': 'Delete',
            'action': 'invokeBulkAction',
            'actionArgs': ['Delete']
        },
        { 'type': 'separator' },
        {
            'type': 'simpleAction',
            'clientTest': 'canAllPropertiesBeShowed',
            'className': 'up',
            'text': 'Properties...',
            'action': 'invokeBulkAction',
            'actionArgs': ['ShowProperties']
        },
        {
            'type': 'simpleAction',
            'clientTest': 'canAllPropertiesBeEdited',
            'className': 'up',
            'text': 'Properties...',
            'action': 'invokeBulkAction',
            'actionArgs': ['EditProperties']
        }
    ]
};