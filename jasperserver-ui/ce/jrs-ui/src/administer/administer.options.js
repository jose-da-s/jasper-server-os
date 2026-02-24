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

import layoutModule from '../core/core.layout';
import webHelpModule from '../components/components.webHelp';
import Administer from './administer.base';
import {matchAny, matchMeOrUp} from "../util/utils.common";
import buttonManager from '../core/core.events.bis';
import dialogs from '../components/components.dialogs';
import {isProVersion} from "../namespace/namespace";
import jQuery from 'jquery';

var Options = {
    SAVE_PFX: 'save',
    TOGGLE_PFX: 'toggle',
    CANCEL_PFX: 'cancel',
    ERROR_PFX: 'error_',
    INPUT_PFX: 'input_',
    initialize: function () {
        if (jQuery('#serverSettingsMenu').length > 0) {
            layoutModule.resizeOnClient('serverSettingsMenu', 'settings');
        }
        webHelpModule.setCurrentContext('admin');
        jQuery('#display').on('click', function (e) {
            var elem = e.target;    // observe navigation
            // observe navigation
            for (var pattern in Administer.menuActions) {
                if (matchAny(elem, [pattern], true) && !jQuery(matchMeOrUp(elem.parentNode, 'li')).hasClass('selected')) {
                    document.location = Administer.menuActions[pattern]();
                    return;
                }
            }
            var button = matchAny(elem, ['#' + Options.SAVE_PFX], true);
            if (button && !button.disabled) {
                Event.stop(e);
                Options.saveValue(button.name);
                return;
            }
            const toggleButton = matchAny(elem, ['#' + Options.TOGGLE_PFX], true);
            if (toggleButton) {
                Event.stop(e);
                const name = toggleButton.getElementsByTagName("input")[0].name;
                Options.saveValue(name);
                return;
            }

            button = matchAny(elem, ['#' + Options.CANCEL_PFX], true);
            if (button && !button.disabled) {
                Event.stop(e);
                Options.resetValue(button.name, button.value);
            }
            // observe inputs
            if (matchAny(elem, [
                '.checkBox > input',
                'select'
            ])) {
                Options.switchButtons(elem, true);
            }
        });    /*
         // observe inputs
         $('display').observe('change', function(e) {
         var elem = e.element();

         if (matchAny(elem,['input','select'])) {
         Options.switchButtons(elem, true);
         }
         });
         */
        /*
         // observe inputs
         $('display').observe('change', function(e) {
         var elem = e.element();

         if (matchAny(elem,['input','select'])) {
         Options.switchButtons(elem, true);
         }
         });
         */
        jQuery('#display').on('keydown', function (e) {
            var elem = e.target;
            if (jQuery(elem).is('input')) {
                Options.switchButtons(elem, true);
            }
        });
    },
    saveValue: function (nm) {
        const input = document.getElementById(Options.INPUT_PFX + nm);
        let inputValue;
        if(nm ==='showDistinctRowValues'){
            inputValue = input.type == 'checkbox' ? !input.checked : input.value
        }else{
            inputValue = input.type == 'checkbox' ? input.checked : input.value
        }
        const params = {
            name: nm,
            value: inputValue,
            _flowExecutionKey: Administer.flowExecutionKey,
            _eventId: 'saveSingleProperty'
        };

        const url = 'flow.html?' + Object.toQueryString(params);
        Administer._sendRequest(url, params, Options._updateCallback);
    },
    resetValue: function (nm, vl) {
        var input = document.getElementById(Options.INPUT_PFX + nm);
        if (input.type == 'checkbox') {
            input.checked = String(vl) == 'true';
        } else {
            input.value = vl;
        }
        Options.switchButtons(input, false);
        jQuery(document.body).find('[for="' + Options.INPUT_PFX + nm + '"]').removeClass(layoutModule.ERROR_CLASS)[0];
    },
    flushCache: function () {
        var url = isProVersion() ? 'flow.html?_flowExecutionKey=' + Administer.flowExecutionKey + '&_eventId=flushCache' : 'flush.html';
        Administer._sendRequest(url, null, Options._flushCallback);
    },
    switchButtons: function (input, enable) {
        if (typeof input == 'string') {
            input = document.getElementById(Options.INPUT_PFX + input);
        }
        Options._enableButton(jQuery(matchMeOrUp(input, 'li')).find('button')[0], enable);
        Options._enableButton(jQuery(matchMeOrUp(input, 'li')).find('button')[1], enable);
    },
    _enableButton: function (button, enable) {
        if (enable) {
            buttonManager.enable(button);
        } else {
            buttonManager.disable(button);
        }
    },
    _updateCallback: function (response) {
        if (response.error) {
            jQuery(document.body).find('[for="' + Options.INPUT_PFX + response.optionName + '"]').addClass(layoutModule.ERROR_CLASS)[0];
            jQuery('#' + Options.ERROR_PFX + response.optionName).html(Administer.getMessage(response.error))[0];
        } else {
            Options.switchButtons(response.optionName, false);
            dialogs.systemConfirm.show(Administer.getMessage(response.result));
            jQuery(document.body).find('[for="' + Options.INPUT_PFX + response.optionName + '"]').removeClass(layoutModule.ERROR_CLASS)[0]
        }
    },
    _flushCallback: function (response) {
        if (response.error) {
            dialogs.systemConfirm.show(Administer.getMessage('JAM_018_ERROR') + ': ' + response.error);
        } else {
            dialogs.systemConfirm.show(Administer.getMessage(response.result));
        }
    }
};

export default Options;