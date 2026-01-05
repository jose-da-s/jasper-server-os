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
import {Form} from 'prototype';
import jQuery from 'jquery';
import resource from './resource.base';
import {ValidationModule, matchAny} from "../util/utils.common";
import buttonManager from '../core/core.events.bis';
import resourceLocator from './resource.locate';
import layoutModule from "../core/core.layout";

var addFileResource = {
    PAGE_ID: 'addResourceFile',
    TYPE_ID: 'fileType',
    PATH_ID: 'filePath',
    LABEL_ID: 'addFileInputResourceLabelID',
    RESOURCE_ID_ID: 'addFileInputResourceID',
    DESCRIPTION_ID: 'addFileInputDescription',
    FOLDER_URI_ID: 'folderUri',
    SAVE_BUTTON_ID: 'save',
    typeToExtMap: {
        'accessGrantSchema': ['xml'],
        'css': ['css'],
        'font': ['ttf'],
        'img': [
            'jpg',
            'jpeg',
            'gif',
            'bmp',
            'png'
        ],
        'jar': ['jar'],
        'jrxml': ['jrxml'],
        'prop': ['properties'],
        'jrtx': ['jrtx'],
        'xml': [
            'xml',
            'agxml'
        ],
        'config': ['config'],
        'cer': [
            'p12',
            'pfx'
        ],
        'contentResource': [
            'docx',
            'doc',
            'ppt',
            'pptx',
            'xls',
            'xlsx',
            'ods',
            'odt',
            'odp',
            'pdf',
            'rtf',
            'html',
            'txt',
            'csv',
            'json'
        ],
        'secureFile': ['*'],
        'mongoDbSchema': ['*'],
        'key': ['key'],
        'pub': ['pub'],
        'ppk': ['ppk']
    },
    _canGenerateId: true,
    initialize: function (options) {
        this._form = jQuery('#' + layoutModule.PAGE_BODY_ID).find('form')[0];
        this._type = jQuery('#' + this.TYPE_ID)[0];
        this._path = jQuery('#' + this.PATH_ID)[0];
        this._label = jQuery('#' + this.LABEL_ID)[0];
        this._resourceId = jQuery('#' + this.RESOURCE_ID_ID)[0];
        this._description = jQuery('#' + this.DESCRIPTION_ID)[0];
        this._folderUri = jQuery('#' + this.FOLDER_URI_ID)[0];
        this._saveButton = jQuery('#' + this.SAVE_BUTTON_ID)[0];
        this._isEditMode = options.isEditMode;
        this._type.validationEntry = {
            validator: this._typeValidator.bind(this),
            element: this._type,
            onValid: function () {
                if (!this._manual) {
                    var fileName = this._getFileNameFromPath(this._path.getValue());
                    this._label.value = fileName;
                    if (!this._resourceId.readOnly) {
                        this._resourceId.value = resource.generateResourceId(this._label.getValue());
                    }
                }
            }.bind(this)
        };
        this._path.validator = this._pathValidator.bind(this);
        this._label.validator = resource.labelValidator.bind(this);
        this._resourceId.validator = resource.resourceIdValidator.bind(this);
        this._description.validator = resource.descriptionValidator.bind(this);
        resourceLocator.initialize({
            resourceInput: 'folderUri',
            browseButton: 'browser_button',
            treeId: 'addFileTreeRepoLocation',
            providerId: 'repositoryExplorerTreeFoldersProvider',
            dialogTitle: resource.messages['resource.Add.Files.Title']
        });
        this._initEvents();
    },
    _initEvents: function () {
        jQuery(this._saveButton).on('click', function (e) {
            if (!this._isDataValid()) {
                e.preventDefault();
            }
        }.bindAsEventListener(this));
        jQuery(this._type).on('change', function (e) {
            this._validateTypeAndPath();
        }.bind(this));
        jQuery(this._path).on('change', function (e) {
            this._validateTypeAndPath();
        }.bind(this));
        this._form && new Form.Observer(this._form, 0.3, function () {
            if (this._folderUri.getValue().blank()) {
                buttonManager.disable('#save');
            } else {
                buttonManager.enable('#save');
            }
        }.bindAsEventListener(this));
        jQuery(this._form).on('keyup', function (e) {
            var element = e.target;
            var targetElements = [
                this._label,
                this._resourceId,
                this._description
            ];
            if (targetElements.include(element)) {
                ValidationModule.validate(resource.getValidationEntries([element]));
                if (element == this._resourceId && this._resourceId.getValue() != resource.generateResourceId(this._label.getValue())) {
                    this._canGenerateId = false;
                }
                if (element == this._label && !this._isEditMode && this._canGenerateId) {
                    this._resourceId.setValue(resource.generateResourceId(this._label.getValue()));
                    ValidationModule.validate(resource.getValidationEntries([this._resourceId]));
                }
            }
            this._manual = true;
        }.bindAsEventListener(this));
        jQuery(this._form).on('keydown', function (e) {
            var targetElement = matchAny(e.target, ['#' + this.DESCRIPTION_ID], true);
            if (!targetElement) {
                if (e.keyCode == 13) {
                    this._saveButton.focus();
                }
            }
        }.bind(this));
    },
    _isDataValid: function () {
        var elementsToValidate = [
            this._label,
            this._resourceId,
            this._description,
            this._path,
            this._type
        ];
        return ValidationModule.validate(resource.getValidationEntries(elementsToValidate));
    },
    _validateTypeAndPath: function () {
        var elementsToValidate = [
            this._type,
            this._path
        ];
        ValidationModule.validate(resource.getValidationEntries(elementsToValidate));
    },
    _typeValidator: function (value) {
        var isValid = true;
        var errorMessage = '';
        var result = {
            isValid: isValid,
            errorMessage: errorMessage
        };
        var extension = this._getExtension();
        if (extension.blank()) {
            return result;
        }
        if (this.typeToExtMap[value] && this.typeToExtMap[value].include('*')) {
            return result;
        }
        var types = this._getTypesForExtension(extension);
        if (!types.include(value)) {
            result.errorMessage = resource.messages['typeIsNotValid'];
            result.isValid = false;
        }
        return result;
    },
    _pathValidator: function (value) {
        var isValid = true;
        var errorMessage = '';
        if (!this._isEditMode && value.blank()) {
            errorMessage = resource.messages['pathIsEmpty'];
            isValid = false;
        }
        return {
            isValid: isValid,
            errorMessage: errorMessage
        };
    },
    _getTypesForExtension: function (extension) {
        var types = [];
        for (var type in this.typeToExtMap) {
            if (this.typeToExtMap[type] && this.typeToExtMap[type].include(extension.toLowerCase())) {
                types.push(type);
            }
        }
        return types;
    },
    _getExtension: function () {
        var filename = this._path.getValue();
        if (filename.blank()) {
            return '';
        }
        var dotPosition = filename.lastIndexOf('.');
        if (dotPosition == -1) {
            return '';
        }
        return filename.substr(dotPosition + 1);
    },
    _getFileNameFromPath: function (path) {
        var startPos = path.lastIndexOf('\\') > 0 ? path.lastIndexOf('\\') + 1 : 0;
        return path.substring(startPos, path.length);
    }
};

export default addFileResource;