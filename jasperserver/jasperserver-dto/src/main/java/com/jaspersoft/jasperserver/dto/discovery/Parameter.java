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
package com.jaspersoft.jasperserver.dto.discovery;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlRootElement;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;

/**
 * <p></p>
 *
 * @author Zakhar Tomchenko
 * @version $Id: $
 */
@XmlRootElement(name = "parameter")
public class Parameter implements DeepCloneable<Parameter> {
    String id;
    String label;
    String valueType;
    String uri;
    boolean multipleValues;

    public Parameter() {}

    public Parameter(Parameter other) {
        checkNotNull(other);

        id = other.id;
        label = other.label;
        valueType = other.valueType;
        uri = other.uri;
        multipleValues = other.multipleValues;
    }

    public String getId() {
        return id;
    }

    public Parameter setId(String id) {
        this.id = id;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public Parameter setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public Parameter setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public String getValueType() {
        return valueType;
    }

    public Parameter setValueType(String type) {
        this.valueType = type;
        return this;
    }

    public boolean isMultipleValues() {
        return multipleValues;
    }

    public Parameter setMultipleValues(boolean multipleValues) {
        this.multipleValues = multipleValues;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Parameter parameter = (Parameter) o;

        if (multipleValues != parameter.multipleValues) return false;
        if (id != null ? !id.equals(parameter.id) : parameter.id != null) return false;
        if (label != null ? !label.equals(parameter.label) : parameter.label != null) return false;
        if (uri != null ? !uri.equals(parameter.uri) : parameter.uri != null) return false;
        if (valueType != null ? !valueType.equals(parameter.valueType) : parameter.valueType != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (valueType != null ? valueType.hashCode() : 0);
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        result = 31 * result + (multipleValues ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Parameter{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", valueType='" + valueType + '\'' +
                ", uri='" + uri + '\'' +
                ", multipleValues=" + multipleValues +
                '}';
    }

    /*
     * DeepCloneable
     */

    @Override
    public Parameter deepClone() {
        return new Parameter(this);
    }
}
