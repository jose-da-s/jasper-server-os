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

import resource from './resource.base';
import resourceLocator from './resource.locate';
import {ValidationModule, isIE, isIE7, isIE8, isIE9, isIE10} from "../util/utils.common";
import jQuery from 'jquery';
import layoutModule from "../core/core.layout";

var resourceReport = {
    SET_UP_PAGE_ID: 'addReport_SetUp',
    LABEL_ID: 'label',
    RESOURCE_ID_ID: 'resourceID',
    DESCRIPTION_ID: 'reportUnit\\.description',
    FILE_PATH_ID: 'filePath',
    RESOURCE_URI_ID: 'resourceUri',
    FILE_SYSTEM_SOURCE_ID: 'FILE_SYSTEM',
    CONTENT_REPOSITORY_SOURCE_ID: 'CONTENT_REPOSITORY',
    RESOURCE_NAME_ID: 'resourceName',
    EDIT_RESOURCE_BUTTON_ID: 'editResourceButton',
    REMOVE_RESOURCE_BUTTON_ID: 'removeResourceButton',
    ADD_RESOURCE_BUTTON_ID: 'addResourceButton',
    EDIT_CONTROL_BUTTON_ID: 'editControlButton',
    REMOVE_CONTROL_BUTTON_ID: 'removeControlButton',
    ADD_CONTROL_BUTTON_ID: 'addControlButton',
    FILE_NAME_ID: 'fileName',
    FILE_UPLOAD_BUTTON_ID: 'fake_upload_button',
    SAVE_BUTTON_ID: 'done',
    JRXML_FILE_PATH_COOKIE: 'jrxmlFilePath',

    _canGenerateId: true,
    initialize: function (options) {
        this._setUpPage = jQuery('#' + this.SET_UP_PAGE_ID)[0];

        if (this._setUpPage) {
            this._form = jQuery('#' + layoutModule.PAGE_BODY_ID).find('form')[0];
            this._label = jQuery('#' + this.LABEL_ID)[0];
            this._resourceId = jQuery('#' + this.RESOURCE_ID_ID)[0];
            this._description = jQuery('#' + this.DESCRIPTION_ID)[0];
            this._filePath = jQuery('#' + this.FILE_PATH_ID)[0];
            this._resourceUri = jQuery('#' + this.RESOURCE_URI_ID)[0];
            this._fileName = jQuery('#' + this.FILE_NAME_ID)[0];

            this._fileSystemSource = jQuery('#' + this.FILE_SYSTEM_SOURCE_ID)[0];
            this._contentRepositorySource = jQuery('#' + this.CONTENT_REPOSITORY_SOURCE_ID)[0];

            this._saveButton = jQuery('#' + this.SAVE_BUTTON_ID);

            this._isEditMode = options ? options.isEditMode : false;
            this._initialSource = this._fileSystemSource.checked ? this._fileSystemSource : this._contentRepositorySource;
            this._jrxmlFileResourceAlreadyUploaded = options.jrxmlFileResourceAlreadyUploaded;
            this._label.validator = resource.labelValidator.bind(this);
            this._resourceId.validator = resource.resourceIdValidator.bind(this);
            this._description.validator = resource.descriptionValidator.bind(this);
            this._filePath.validator = this._filePathValidator.bind(this);
            this._resourceUri.validator = this._resourceUriValidator.bind(this);

            this._initEvents();
            this._adjustFileSelectorPosition();
            this._fileName.value = this._jrxmlFileResourceAlreadyUploaded;
        }    // Resource locator.
        // Resource locator.
        var resourceOptions = {
            fileUploadInput: 'filePath',
            fakeFileUploadInput: 'fake_upload_button',
            fakeFileUploadInputText: 'fileName',
            resourceInput: 'resourceUri',
            browseButton: 'browser_button',
            uploadButton: 'upload_button',
            treeId: 'resourceTreeRepoLocation',
            dialogTitle: resource.messages['resource.Report.Title'],
            selectLeavesOnly: true
        };
        if (options && jQuery('#' + resourceOptions.browseButton)[0]) {
            if (options.type == 'fileResource') {
                resourceOptions.providerId = 'fileResourceTreeDataProvider';
            } else if (options.type == 'jrxml') {
                resourceOptions.providerId = 'jrxmlTreeDataProvider';
            } else if (options.type == 'folder') {
                resourceOptions.treeId = 'addFileTreeRepoLocation';
                resourceOptions.providerId = 'repositoryExplorerTreeFoldersProvider';
                resourceOptions.resourceInput = 'folderUri';
            }
            resourceLocator.initialize(resourceOptions);
            resourceLocator._updateResourceSelectorState(jQuery('input[type=radio]:checked').attr('id'));
        }
    },
    _initEvents: function () {
        var self = this;    //opens file upload selection dialog, when user clicks on ordinary button
        //opens file upload selection dialog, when user clicks on ordinary button
        jQuery('#' + this.FILE_UPLOAD_BUTTON_ID).click(function (e) {
            e.preventDefault();
            jQuery('#' + self.FILE_PATH_ID).trigger('click');
        });
        this._saveButton.on('click', function (e) {
            if (!self._isDataValid()) {
                e.preventDefault();
            }
        });
        jQuery(this._form).on('keyup', function (e) {
            var element = e.target;
            var targetElements = [
                self._label,
                self._resourceId,
                self._description
            ];
            if (targetElements.include(element)) {
                ValidationModule.validate(resource.getValidationEntries([element]));
                if (element == self._resourceId && self._resourceId.getValue() != resource.generateResourceId(self._label.getValue())) {
                    self._canGenerateId = false;
                }

                if (element == self._label && !self._isEditMode && self._canGenerateId) {
                    self._resourceId.setValue(resource.generateResourceId(self._label.getValue()));

                    ValidationModule.validate(resource.getValidationEntries([self._resourceId]));
                }
            }
        });
        this._filePath.observe('change', function () {
            if (isIE()) {
                //IE can for security reasons change real patch to file on 'c:\fakepath\'
                //is should not be shown to user, in this case show only filename
                if (this.value.toLowerCase().indexOf('c:\\fakepath\\') != -1) {
                    self._fileName.value = this.value.substring('c:\\fakepath\\'.length, this.value.length);
                } else {
                    self._fileName.value = this.value;
                }
            } else {
                self._fileName.value = this.files[0].name;
            }
            jQuery('#' + 'fileUpload').removeClass('error');
            self._adjustFileSelectorPosition();
        });
    },
    _isDataValid: function () {
        var elementsToValidate = [
            this._label,
            this._resourceId,
            this._description,
            this._filePath,
            this._resourceUri
        ];
        if (isIE()) {
            this.file = this._filePath.value;
        } else {
            this.file = this._filePath.files[0];
        }
        this.html = jQuery(this._filePath).html();

        return ValidationModule.validate(resource.getValidationEntries(elementsToValidate));
    },
    _filePathValidator: function (value) {
        var isValid = true;
        var errorMessage = '';
        if (this._fileSystemSource.checked && value.blank() && (!this._isEditMode || this._initialSource != this._fileSystemSource) && !this._jrxmlFileResourceAlreadyUploaded) {
            errorMessage = resource.messages['filePathIsEmpty'];
            isValid = false;
        }
        if (isValid)
            ValidationModule.hideError(jQuery('#' + 'fileName')[0]);
        else
            ValidationModule.showError(jQuery('#' + 'fileName')[0], errorMessage);
        return {
            isValid: isValid,
            errorMessage: errorMessage
        };
    },
    _resourceUriValidator: function (value) {
        var isValid = true;
        var errorMessage = '';
        if (this._contentRepositorySource.checked && value.blank()) {
            errorMessage = resource.messages['resourceUriIsEmpty'];
            isValid = false;
        }

        return {
            isValid: isValid,
            errorMessage: errorMessage
        };
    },
    _adjustFileSelectorPosition: function () {
        var fp = jQuery('#filePath');
        if (isIE7() || isIE8() || isIE9() || isIE10()) {
            var top = 20, right = 0, width = 95, height = 30, hasError = fp.parents('label').hasClass('error');
            if (hasError) {
                // adjust the position because of the error message
                top += 13;
            }
            fp.css({
                opacity: '0',
                position: 'absolute',
                right: right,
                top: top,
                width: width,
                height: height
            });
        } else {
            fp.css({
                position: 'fixed',
                right: '-1000px',
                top: '-1000px'
            });
        }
    },
    editResource: function (resourceName) {
        jQuery('#' + this.RESOURCE_NAME_ID)[0].setValue(resourceName);
        jQuery('#' + this.EDIT_RESOURCE_BUTTON_ID)[0].click();
    },

    removeResource: function(resourceName) {
        jQuery('#' + this.RESOURCE_NAME_ID)[0].setValue(resourceName);
        jQuery('#' + this.REMOVE_RESOURCE_BUTTON_ID)[0].click();
    },

    addResource: function() {
        jQuery('#' + this.ADD_RESOURCE_BUTTON_ID)[0].click();
    },

    editControl: function(resourceName) {
        jQuery('#' + this.RESOURCE_NAME_ID)[0].setValue(resourceName);
        jQuery('#' + this.EDIT_CONTROL_BUTTON_ID)[0].click();
    },

    removeControl: function(resourceName) {
        jQuery('#' + this.RESOURCE_NAME_ID)[0].setValue(resourceName);
        jQuery('#' + this.REMOVE_CONTROL_BUTTON_ID)[0].click();
    },

    addControl: function() {
        jQuery('#' + this.ADD_CONTROL_BUTTON_ID)[0].click();
    }
};


export default resourceReport;

