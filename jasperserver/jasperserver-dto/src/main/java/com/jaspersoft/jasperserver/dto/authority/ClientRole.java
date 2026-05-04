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
package com.jaspersoft.jasperserver.dto.authority;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;

/**
 * <p></p>
 *
 * @author Zakhar.Tomchenco
 * @version $Id$
 */
@XmlRootElement(name = "role")
public class ClientRole implements DeepCloneable<ClientRole> {
    private String name;
    private boolean externallyDefined = false;
    private String tenantId;

    public ClientRole(ClientRole other) {
        checkNotNull(other);

        this.name = other.getName();
        this.externallyDefined = other.isExternallyDefined();
        this.tenantId = other.getTenantId();
    }

    public ClientRole() {
    }

    @Override
    public ClientRole deepClone() {
        return new ClientRole(this);
    }

    @XmlElement(name = "name")
    public String getName() {
        return name;
    }

    public ClientRole setName(String name) {
        this.name = name;
        return this;
    }

    @XmlElement(name = "externallyDefined")
    public boolean isExternallyDefined() {
        return externallyDefined;
    }

    public ClientRole setExternallyDefined(boolean externallyDefined) {
        this.externallyDefined = externallyDefined;
        return this;
    }

    @XmlElement(name = "tenantId")
    public String getTenantId() {
        return tenantId;
    }

    public ClientRole setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientRole that = (ClientRole) o;

        if (externallyDefined != that.externallyDefined) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return tenantId != null ? tenantId.equals(that.tenantId) : that.tenantId == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (externallyDefined ? 1 : 0);
        result = 31 * result + (tenantId != null ? tenantId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientRole{" +
                "name='" + name + '\'' +
                ", externallyDefined=" + externallyDefined +
                ", tenantId='" + tenantId + '\'' +
                '}';
    }
}
