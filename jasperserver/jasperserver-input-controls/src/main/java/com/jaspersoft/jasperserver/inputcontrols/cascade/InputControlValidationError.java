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

package com.jaspersoft.jasperserver.inputcontrols.cascade;

import com.jaspersoft.jasperserver.api.common.domain.impl.ValidationErrorImpl;

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @author inesterenko
 */

@XmlRootElement(name = "error")
public class InputControlValidationError extends ValidationErrorImpl {

    private String inputControlUri;

    private String invalidValue;
    private Object[] args;

    public InputControlValidationError(String errorCode, Object[] arguments, String defaultMessage, String inputControlUri, String invalidValue) {
        super(errorCode, arguments, defaultMessage, null);
        this.args = arguments;
        this.inputControlUri =  inputControlUri;
        this.invalidValue = invalidValue;
    }

    public String getInputControlUri() {
        return inputControlUri;
    }

    public void setInputControlUri(String inputControlUri) {
        this.inputControlUri = inputControlUri;
    }

    public String getInvalidValue() {
        return invalidValue;
    }

    public void setInvalidValue(String invalidValue) {
        this.invalidValue = invalidValue;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

}
