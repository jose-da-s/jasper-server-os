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
package com.jaspersoft.jasperserver.dto.common;

import com.jaspersoft.jasperserver.dto.resources.ClientProperty;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@XmlRootElement
public class ErrorDescriptor implements DeepCloneable<ErrorDescriptor>, Serializable {
    public static final String ERROR_CODE_UNEXPECTED_ERROR = "unexpected.error";

    private String message;
    private String errorCode;
    private String[] parameters;
    private List<ClientProperty> properties;
    private List<ErrorDescriptor> details;
    private String errorUid;

    private Throwable exception;

    public ErrorDescriptor() {
    }

    public ErrorDescriptor(final ErrorDescriptor ed) {
        checkNotNull(ed);

        this.message = ed.getMessage();
        this.errorCode = ed.getErrorCode();
        this.parameters = ed.getParameters();
        this.errorUid = ed.getErrorUid();
        this.exception = ed.getException();
        this.properties = copyOf(ed.getProperties());
        this.details = copyOf(ed.getDetails());
    }

    @XmlElementWrapper(name = "details")
    @XmlElement(name = "detail")
    public List<ErrorDescriptor> getDetails() {
        return details;
    }

    public ErrorDescriptor setDetails(List<ErrorDescriptor> details) {
        this.details = details;
        return this;
    }

    public ErrorDescriptor addDetails(ErrorDescriptor... details) {
        final List<ErrorDescriptor> descriptorList = Arrays.asList(details);
        if (this.details == null) {
            this.details = new ArrayList<ErrorDescriptor>(descriptorList);
        } else {
            this.details.addAll(descriptorList);
        }
        return this;
    }

    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    public List<ClientProperty> getProperties() {
        return properties;
    }

    public ErrorDescriptor setProperties(List<ClientProperty> properties) {
        this.properties = properties;
        return this;
    }

    public ErrorDescriptor addProperties(ClientProperty... properties) {
        final List<ClientProperty> clientProperties = Arrays.asList(properties);
        if (this.properties == null) {
            this.properties = new ArrayList<ClientProperty>(clientProperties);
        } else {
            this.properties.addAll(clientProperties);
        }
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ErrorDescriptor setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public ErrorDescriptor setErrorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    public String[] getParameters() {
        return parameters;
    }

    /**
     * @deprecated replaced by {@link #setProperties(List)}
     * */
    public ErrorDescriptor setParameters(String... parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * @deprecated replaced by {@link #addProperties(ClientProperty...)}
     * */
    public ErrorDescriptor addParameters(Object... args) {
        if (args != null && args.length > 0) {
            List<String> values = new LinkedList<String>();
            for (Object arg : args) {
                if (arg != null) {
                    values.add(arg.toString());
                } else {
                    values.add(null);
                }
            }
            if (!values.isEmpty()) {
                parameters = values.toArray(new String[values.size()]);
            }
        }

        return this;
    }

    public String getErrorUid() {
        return errorUid;
    }

    public ErrorDescriptor setErrorUid(String errorUid) {
        this.errorUid = errorUid;
        return this;
    }

    public ErrorDescriptor setException(Throwable exception) {
        this.exception = exception;
        return this;
    }

    @XmlTransient
    public Throwable getException() {
        return exception;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ErrorDescriptor)) return false;

        ErrorDescriptor that = (ErrorDescriptor) o;

        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        if (errorCode != null ? !errorCode.equals(that.errorCode) : that.errorCode != null) return false;
        if (!Arrays.equals(parameters, that.parameters)) return false;
        if ((properties == null & that.properties != null) || (properties != null & that.properties == null)) return false;
        if (properties != null && that.properties != null) {
            if (!(properties.containsAll(that.properties) && that.properties.containsAll(properties))) return false;
        }
        if (details != null ? !details.equals(that.details) : that.details != null) return false;
        if (errorUid != null ? !errorUid.equals(that.errorUid) : that.errorUid != null) return false;
        return exception != null ? exception.equals(that.exception) : that.exception == null;
    }

    @Override
    public int hashCode() {
        int result = message != null ? message.hashCode() : 0;
        result = 31 * result + (errorCode != null ? errorCode.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(parameters);
        int propertiesHashCode = 0;
        if (properties != null) {
            for (ClientProperty property : properties) {
                propertiesHashCode += property.hashCode();
            }
        }
        result = 31 * result + propertiesHashCode;
        result = 31 * result + (details != null ? details.hashCode() : 0);
        result = 31 * result + (errorUid != null ? errorUid.hashCode() : 0);
        result = 31 * result + (exception != null ? exception.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ErrorDescriptor{" +
                "message='" + message + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", parameters=" + Arrays.toString(parameters) +
                ", properties=" + properties +
                ", details=" + details +
                ", errorUid='" + errorUid + '\'' +
                ", exception=" + exception +
                '}';
    }

    @Override
    public ErrorDescriptor deepClone() {
        return new ErrorDescriptor(this);
    }
}
