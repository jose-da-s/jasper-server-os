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

import com.jaspersoft.jasperserver.dto.authority.hypermedia.HypermediaAttribute;
import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;

/**
 * <p></p>
 *
 * @author Zakhar.Tomchenco
 * @author Volodya Sabadosh
 * @author Vlad Zavadskii
 * @version $Id: Id$
 */
@XmlRootElement(name = "attribute")
@XmlSeeAlso({HypermediaAttribute.class})
public class ClientAttribute<BuilderType extends ClientAttribute<BuilderType>> implements DeepCloneable<BuilderType> {
    private String name;
    private String value;
    private Boolean secure = false;
    private Boolean inherited = false;
    private String description;
    private Integer permissionMask;
    private String holder;

    public ClientAttribute(ClientAttribute other) {
        checkNotNull(other);

        this.name = other.getName();
        this.value = other.getValue();
        this.secure = other.isSecure();
        this.description = other.getDescription();
        this.permissionMask = other.getPermissionMask();
        this.inherited = other.isInherited();
        this.holder = other.getHolder();
    }

    public ClientAttribute() {
    }

    // definition of subclasses assures cast safety.
    @SuppressWarnings("unchecked")
    @Override
    public BuilderType deepClone() {
        return (BuilderType) new ClientAttribute(this);
    }

    public String getName() {
        return name;
    }

    // definition of subclasses assures cast safety.
    @SuppressWarnings("unchecked")
    public BuilderType setName(String name) {
        this.name = name;
        return (BuilderType) this;
    }

    public String getHolder() {
        return holder;
    }

    // definition of subclasses assures cast safety.
    @SuppressWarnings("unchecked")
    public BuilderType setHolder(String holder) {
        this.holder = holder;
        return (BuilderType) this;
    }

    public String getValue() {
        return value;
    }

    // definition of subclasses assures cast safety.
    @SuppressWarnings("unchecked")
    public BuilderType setValue(String value) {
        this.value = value;
        return (BuilderType) this;
    }

    @XmlElement(name = "secure")
    public Boolean isSecure() {
        return secure;
    }

    // definition of subclasses assures cast safety.
    @SuppressWarnings("unchecked")
    public BuilderType setSecure(Boolean secure) {
        this.secure = secure;
        return (BuilderType) this;
    }

    @XmlElement(name = "inherited")
    public Boolean isInherited() {
        return inherited;
    }

    // definition of subclasses assures cast safety.
    @SuppressWarnings("unchecked")
    public BuilderType setInherited(Boolean inherited) {
        this.inherited = inherited;
        return (BuilderType) this;
    }

    public String getDescription() {
        return description;
    }

    // definition of subclasses assures cast safety.
    @SuppressWarnings("unchecked")
    public BuilderType setDescription(String description) {
        this.description = description;
        return (BuilderType) this;
    }

    public Integer getPermissionMask() {
        return permissionMask;
    }

    // definition of subclasses assures cast safety.
    @SuppressWarnings("unchecked")
    public BuilderType setPermissionMask(Integer permissionMask) {
        this.permissionMask = permissionMask;
        return (BuilderType) this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientAttribute)) return false;

        ClientAttribute that = (ClientAttribute) o;

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
        return getClass().getSimpleName() + "{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", secure='" + secure + '\'' +
                ", inherited='" + inherited + '\'' +
                ", description='" + description + '\'' +
                ", permissionMask='" + permissionMask + '\'' +
                ", holder='" + holder + '\'' +
                '}';
    }
}
