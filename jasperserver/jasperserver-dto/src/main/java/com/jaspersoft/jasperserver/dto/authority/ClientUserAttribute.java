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
 * @author Volodya Sabadosh
 * @version $Id: Id$
 */
@Deprecated
@XmlRootElement(name = "attribute")
public class ClientUserAttribute implements DeepCloneable<ClientUserAttribute> {
    private String name;
    private String value;
    private Boolean secure = null;
    private Boolean inherited = null;
    private String description;
    private Integer permissionMask;
    private String holder;

    public ClientUserAttribute(ClientUserAttribute other) {
        checkNotNull(other);

        this.name = other.getName();
        this.value = other.getValue();
        this.secure = other.isSecure();
        this.description = other.getDescription();
        this.permissionMask = other.getPermissionMask();
        this.inherited = other.isInherited();
        this.holder = other.getHolder();
    }

    public ClientUserAttribute() {
    }

    @Override
    public ClientUserAttribute deepClone() {
        return new ClientUserAttribute(this);
    }

    public String getName() {
        return name;
    }

    public ClientUserAttribute setName(String name) {
        this.name = name;
        return this;
    }

    public String getHolder() {
        return holder;
    }

    public ClientUserAttribute setHolder(String holder) {
        this.holder = holder;
        return this;
    }

    public String getValue() {
        return value;
    }

    public ClientUserAttribute setValue(String value) {
        this.value = value;
        return this;
    }

    public ClientUserAttribute setSecure(Boolean secure) {
        this.secure = secure;
        return this;
    }

    @XmlElement(name = "secure")
    public Boolean isSecure() {
        return secure;
    }

    @XmlElement(name = "inherited")
    public Boolean isInherited() {
        return inherited;
    }

    public ClientUserAttribute setInherited(Boolean inherited) {
        this.inherited = inherited;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ClientUserAttribute setDescription(String description) {
        this.description = description;
        return this;
    }

    public Integer getPermissionMask() {
        return permissionMask;
    }

    public ClientUserAttribute setPermissionMask(Integer permissionMask) {
        this.permissionMask = permissionMask;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientUserAttribute that = (ClientUserAttribute) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (secure != null ? !secure.equals(that.secure) : that.secure != null) return false;
        if (inherited != null ? !inherited.equals(that.inherited) : that.inherited != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (permissionMask != null ? !permissionMask.equals(that.permissionMask) : that.permissionMask != null)
            return false;
        return holder != null ? holder.equals(that.holder) : that.holder == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (secure != null ? secure.hashCode() : 0);
        result = 31 * result + (inherited != null ? inherited.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (permissionMask != null ? permissionMask.hashCode() : 0);
        result = 31 * result + (holder != null ? holder.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientUserAttribute{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", secure=" + secure +
                ", inherited=" + inherited +
                ", description='" + description + '\'' +
                ", permissionMask=" + permissionMask +
                ", holder='" + holder + '\'' +
                '}';
    }
}
