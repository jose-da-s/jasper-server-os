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
package com.jaspersoft.jasperserver.api.metadata.user.domain.client;

import com.jaspersoft.jasperserver.api.metadata.common.domain.PermissionUriProtocol;
import com.jaspersoft.jasperserver.api.metadata.user.domain.ProfileAttribute;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeLevel;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * @author sbirney
 */

@XmlRootElement(name = "profileAttribute")
public class ProfileAttributeImpl implements ProfileAttribute, Serializable {
    private String attrName;
    private String attrValue;
    private String group;
    private boolean secure = false;
    private String description;
    private ProfileAttributeLevel level;
    @XmlTransient
    private String uri = "/";

    @XmlTransient
    private Object principal;

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String s) {
        this.attrName = s;
    }

    public String getAttrValue() {
        return attrValue;
    }

    public void setAttrValue(String s) {
        this.attrValue = s;
    }

    @XmlTransient
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setPrincipal(Object o) {
        this.principal = o;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    @Override
    public ProfileAttributeLevel getLevel() {
        return level;
    }

    @Override
    public void setLevel(ProfileAttributeLevel level) {
        this.level = level;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @XmlTransient
    public String getUri() {
        return this.uri;
    }

    public void setUri(String attrName, String holderUri) {
        this.uri = holderUri.concat("/attributes/").concat(attrName);
    }

    @Override
    public String getURI() {
        return PermissionUriProtocol.ATTRIBUTE.addPrefix(getPath());
    }

    @Override
    public String getPath() {
        return getUri();
    }

    @Override
    public String getProtocol() {
        return PermissionUriProtocol.ATTRIBUTE.toString();
    }

    @Override
    public String getParentURI() {
        String parentPath = getParentPath();
        return parentPath == null ? null : PermissionUriProtocol.ATTRIBUTE.addPrefix(parentPath);
    }

    @Override
    public String getParentPath() {
        return PermissionUriProtocol.ATTRIBUTE.getParentUri(getUri());
    }

    @Override
    public Serializable getIdentifier() {
        return getURI();
    }

    @Override
    public String getType() {
        return null;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("attrName", getAttrName())
                .append("attrValue", getAttrValue())
                .append("principal", getPrincipal())
                .append("secure", Boolean.valueOf(isSecure()))
                .append("group", getGroup())
                .append("description", getDescription())
                .toString();
    }

    public boolean equals(Object other) {
        if (!(other instanceof ProfileAttributeImpl)) return false;
        ProfileAttributeImpl castOther = (ProfileAttributeImpl) other;
        return new EqualsBuilder()
                .append(this.getAttrName(), castOther.getAttrName())
                .append(this.getPrincipal(), castOther.getPrincipal())
                .append(this.getGroup(), castOther.getGroup())
                .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getAttrName())
                .append(getPrincipal())
                .append(getGroup())
                .toHashCode();
    }
}