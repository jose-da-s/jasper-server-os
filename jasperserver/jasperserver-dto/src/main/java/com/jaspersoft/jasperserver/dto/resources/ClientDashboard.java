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

import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk, Zakhar.Tomchenko
 * @version $Id: ClientDashboard.java 36344 2013-12-03 17:36:14Z ztomchenco $
 */
@XmlRootElement(name = ResourceMediaType.DASHBOARD_CLIENT_TYPE)
public class ClientDashboard extends ClientResource<ClientDashboard> implements DeepCloneable<ClientDashboard> {
    private List<ClientDashboardFoundation> foundations;
    private List<ClientDashboardResource> resources;
    private String defaultFoundation;

    public ClientDashboard() {}

    public ClientDashboard(ClientDashboard other) {
        super(other);
        foundations = copyOf(other.foundations);
        resources = copyOf(other.resources);
        defaultFoundation = other.defaultFoundation;
    }

    public List<ClientDashboardFoundation> getFoundations() {
        return foundations;
    }

    public ClientDashboard setFoundations(List<ClientDashboardFoundation> foundations) {
        this.foundations = foundations;
        return this;
    }

    public List<ClientDashboardResource> getResources() {
        return resources;
    }

    public String getDefaultFoundation() {
        return defaultFoundation;
    }

    public ClientDashboard setDefaultFoundation(String defaultFoundation) {
        this.defaultFoundation = defaultFoundation;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ClientDashboard that = (ClientDashboard) o;

        if (defaultFoundation != null ? !defaultFoundation.equals(that.defaultFoundation) : that.defaultFoundation != null)
            return false;
        if (foundations != null ? !foundations.equals(that.foundations) : that.foundations != null) return false;
        if (resources != null ? !resources.equals(that.resources) : that.resources != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (foundations != null ? foundations.hashCode() : 0);
        result = 31 * result + (resources != null ? resources.hashCode() : 0);
        result = 31 * result + (defaultFoundation != null ? defaultFoundation.hashCode() : 0);
        return result;
    }

    public ClientDashboard setResources(List<ClientDashboardResource> resources) {
        this.resources = resources;
        return this;
    }

    @Override
    public String toString() {
        return "ClientDashboard{" +
                "version=" + getVersion() +
                ", permissionMask=" + getPermissionMask() +
                ", uri='" + getUri() + '\'' +
                ", label='" + getLabel() + '\'' +
                '}';
    }
    @Override
    public ClientDashboard deepClone() {
        return new ClientDashboard(this);
    }
}