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

package com.jaspersoft.jasperserver.dto.adhoc.component;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Andriy Godovanets
 * @version $Id$
 */
public class ClientGenericComponent implements ClientComponent, Serializable {
    private Map<String, Object> propertyMap;
    private String componentType;
    private List<? extends ClientComponent> components;

    public ClientGenericComponent() {
        propertyMap = new LinkedHashMap<String, Object>();
        components = new ArrayList<ClientComponent>();
    }

    public ClientGenericComponent(ClientGenericComponent component) {
        checkNotNull(component);

        propertyMap = copyOf(component.getProperties());
        componentType = component.getComponentType();
        components = copyOf(component.getComponents());
    }

    @Override
    public ClientGenericComponent deepClone() {
        return new ClientGenericComponent(this);
    }

    @Override
    public Map<String, Object> getProperties() {
        return propertyMap;
    }

    @SuppressWarnings("unchecked")
    public ClientGenericComponent setProperties(Map<String, Object> properties) {
        this.propertyMap = new LinkedHashMap<String, Object>();
        if (properties != null) {
            this.propertyMap.putAll(properties);
        }
        return this;
    }

    @Override
    public Object getProperty(String key) {
        return propertyMap.get(key);
    }

    @SuppressWarnings("unchecked")
    protected ClientGenericComponent setProperty(String key, Object value) {
        propertyMap.put(key, value);
        return this;
    }

    @Override
    public String getComponentType() {
        return componentType;
    }

    @SuppressWarnings("unchecked")
    public ClientGenericComponent setComponentType(String type) {
        this.componentType = type;
        return this;
    }


    @XmlElementWrapper(name = "components")
    @XmlElement(type = ClientGenericComponent.class, name = "components")
    @Override
    public List<ClientComponent> getComponents() {
        return (List<ClientComponent>) components;
    }

    public ClientGenericComponent setComponents(List<ClientGenericComponent> components) {
        this.components = components != null ? components : new ArrayList<ClientComponent>();
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientGenericComponent)) return false;
        ClientGenericComponent that = (ClientGenericComponent) o;
        return Objects.equals(propertyMap, that.propertyMap) &&
                Objects.equals(componentType, that.componentType) &&
                Objects.equals(components, that.components);
    }

    @Override
    public int hashCode() {
        return Objects.hash(propertyMap, componentType, components);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ClientGenericComponent{");
        sb.append("propertyMap=").append(propertyMap);
        sb.append(", componentType='").append(componentType).append('\'');
        sb.append(", components=").append(components);
        sb.append('}');
        return sb.toString();
    }


}
