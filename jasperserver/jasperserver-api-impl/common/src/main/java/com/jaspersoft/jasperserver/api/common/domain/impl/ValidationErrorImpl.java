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
package com.jaspersoft.jasperserver.api.common.domain.impl;

import com.jaspersoft.jasperserver.api.common.domain.ValidationError;
import com.jaspersoft.jasperserver.dto.resources.ClientProperty;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
@XmlRootElement(name = "error")
public class ValidationErrorImpl implements ValidationError, Serializable {
	
	private static final long serialVersionUID = 1L;
    private String errorCode;
	private Object[] arguments;
    private List<ClientProperty> properties;
	private String defaultMessage;
	private String field;

    public ValidationErrorImpl(){}

	public ValidationErrorImpl(String errorCode, Object[] arguments, String defaultMessage, String field) {
		this.errorCode = errorCode;
		this.arguments = arguments;
		this.defaultMessage = defaultMessage;
		this.field = field;
	}
	
	public ValidationErrorImpl(String errorCode, Object[] arguments, String defaultMessage) {
		this(errorCode, arguments, defaultMessage, null);
	}

	public String getErrorCode() {
		return errorCode;
	}

	public Object[] getErrorArguments() {
		return arguments;
	}

	public String getDefaultMessage() {
		return defaultMessage;
	}

	public String getField() {
		return field;
	}

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    public void setErrorArguments(Object... arguments){
        this.setArguments(arguments);
    }

    public void setDefaultMessage(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public void setField(String field) {
        this.field = field;
    }

    public List<ClientProperty> getProperties() {
        return properties;
    }

    public ValidationErrorImpl setProperties(List<ClientProperty> properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ValidationErrorImpl)) return false;

        ValidationErrorImpl that = (ValidationErrorImpl) o;

        if (errorCode != null ? !errorCode.equals(that.errorCode) : that.errorCode != null) return false;
        if (!Arrays.equals(arguments, that.arguments)) return false;
        if (properties != null ? !properties.equals(that.properties) : that.properties != null) return false;
        if (defaultMessage != null ? !defaultMessage.equals(that.defaultMessage) : that.defaultMessage != null) return false;
        return field != null ? field.equals(that.field) : that.field == null;
    }

    @Override
    public int hashCode() {
        int result = errorCode != null ? errorCode.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(arguments);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        result = 31 * result + (defaultMessage != null ? defaultMessage.hashCode() : 0);
        result = 31 * result + (field != null ? field.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ValidationErrorImpl{" +
                "errorCode='" + errorCode + '\'' +
                ", arguments=" + Arrays.toString(arguments) +
                ", properties=" + properties +
                ", defaultMessage='" + defaultMessage + '\'' +
                ", field='" + field + '\'' +
                '}';
    }
}
