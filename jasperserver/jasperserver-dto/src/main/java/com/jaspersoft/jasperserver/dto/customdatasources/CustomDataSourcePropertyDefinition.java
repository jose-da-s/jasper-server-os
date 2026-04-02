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
package com.jaspersoft.jasperserver.dto.customdatasources;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;
import com.jaspersoft.jasperserver.dto.resources.ClientProperty;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@XmlRootElement(name = "propertyDefinition")
public class CustomDataSourcePropertyDefinition implements DeepCloneable<CustomDataSourcePropertyDefinition> {
    private String name;
    private String label;
    private String defaultValue;
    private List<ClientProperty> properties;

    public CustomDataSourcePropertyDefinition() {
    }

    public CustomDataSourcePropertyDefinition(CustomDataSourcePropertyDefinition source) {
        checkNotNull(source);

        this.name = source.getName();
        this.defaultValue = source.getDefaultValue();
        this.label = source.getLabel();
        this.properties = copyOf(source.getProperties());
    }

    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    public List<ClientProperty> getProperties() {
        return properties;
    }

    public CustomDataSourcePropertyDefinition setProperties(List<ClientProperty> properties) {
        this.properties = properties;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public CustomDataSourcePropertyDefinition setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getName() {
        return name;
    }

    public CustomDataSourcePropertyDefinition setName(String name) {
        this.name = name;
        return this;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public CustomDataSourcePropertyDefinition setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomDataSourcePropertyDefinition that = (CustomDataSourcePropertyDefinition) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (defaultValue != null ? !defaultValue.equals(that.defaultValue) : that.defaultValue != null) return false;
        return properties != null ? properties.equals(that.properties) : that.properties == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CustomDataSourcePropertyDefinition{" +
                "name='" + name + '\'' +
                ", label='" + label + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                ", properties=" + properties +
                '}';
    }

    /*
     * DeepCloneable
     */

    @Override
    public CustomDataSourcePropertyDefinition deepClone() {
        return new CustomDataSourcePropertyDefinition(this);
    }
}
