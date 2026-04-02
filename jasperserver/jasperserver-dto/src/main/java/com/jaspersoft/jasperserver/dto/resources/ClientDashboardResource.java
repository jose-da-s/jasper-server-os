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
package com.jaspersoft.jasperserver.dto.resources;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;

import java.io.Serializable;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author Zakhar.Tomchenco
 * @version $Id$
 */
public class ClientDashboardResource implements DeepCloneable<ClientDashboardResource>, Serializable {
    private String name;
    private String type;
    private ClientReferenceable resource;

    public ClientDashboardResource() {
    }

    public ClientDashboardResource(ClientDashboardResource other) {
        checkNotNull(other);

        name = other.getName();
        type = other.getType();
        resource = copyOf(other.getResource());
    }

    public String getName() {
        return name;
    }

    public ClientDashboardResource setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public ClientDashboardResource setType(String type) {
        this.type = type;
        return this;
    }

    @XmlElements({
            /*ClientReference is included here to serve as resource reference*/
            @XmlElement(type = ClientReference.class, name = "resourceReference"),
            @XmlElement(type = ClientFile.class, name = "file"),
            @XmlElement(type = ClientInputControl.class, name = "inputControl"),
            @XmlElement(type = ClientReportUnit.class, name = "ClientReportUnit"),
            @XmlElement(type = ClientAdhocDataView.class, name = "ClientAdhocDataView")
    })
    public ClientReferenceable getResource() {
        return resource;
    }

    public ClientDashboardResource setResource(ClientReferenceable resource) {
        this.resource = resource;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientDashboardResource that = (ClientDashboardResource) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (resource != null ? !resource.equals(that.resource) : that.resource != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (resource != null ? resource.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientDashboardResource{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", resource=" + resource +
                '}';
    }

    @Override
    public ClientDashboardResource deepClone() {
        return new ClientDashboardResource(this);
    }
}
