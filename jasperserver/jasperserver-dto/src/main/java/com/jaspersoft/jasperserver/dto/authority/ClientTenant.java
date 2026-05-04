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

import jakarta.xml.bind.annotation.XmlRootElement;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;

/**
 * <p></p>
 *
 * @author Zakhar.Tomchenco
 * @version $Id$
 */
@XmlRootElement(name = "organization")
public class ClientTenant implements DeepCloneable<ClientTenant> {

    private String id = null;
    private String alias = null;
    private String parentId = null;
    private String tenantName = null;
    private String tenantDesc = null;
    private String tenantNote = null;
    private String tenantUri = null;
    private String tenantFolderUri = null;
    private String theme = null;

    public ClientTenant() {
    }

    public ClientTenant(ClientTenant other) {
        checkNotNull(other);

        this.id = other.getId();
        this.alias = other.getAlias();
        this.parentId = other.getParentId();
        this.tenantName = other.getTenantName();
        this.tenantDesc = other.getTenantDesc();
        this.tenantNote = other.getTenantNote();
        this.tenantUri = other.getTenantUri();
        this.tenantFolderUri = other.getTenantFolderUri();
        this.theme = other.getTheme();
    }

    @Override
    public ClientTenant deepClone() {
        return new ClientTenant(this);
    }

    public String getTenantDesc() {
        return tenantDesc;
    }

    public ClientTenant setTenantDesc(String tenantDesc) {
        this.tenantDesc = tenantDesc;
        return this;
    }

    public String getTenantNote() {
        return tenantNote;
    }

    public ClientTenant setTenantNote(String tenantNote) {
        this.tenantNote = tenantNote;
        return this;
    }

    public String getId() {
        return id;
    }

    public ClientTenant setId(String pid) {
        id = pid;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public ClientTenant setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public String getParentId() {
        return parentId;
    }

    public ClientTenant setParentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    public String getTenantName() {
        return tenantName;
    }

    public ClientTenant setTenantName(String tenantName) {
        this.tenantName = tenantName;
        return this;
    }

    public String getTenantUri() {
        return tenantUri;
    }

    public ClientTenant setTenantUri(String tenantUri) {
        this.tenantUri = tenantUri;
        return this;
    }

    public String getTenantFolderUri() {
        return tenantFolderUri;
    }

    public ClientTenant setTenantFolderUri(String tenantFolderUri) {
        this.tenantFolderUri = tenantFolderUri;
        return this;
    }

    public String getTheme() {
        return theme;
    }

    public ClientTenant setTheme(String theme) {
        this.theme = theme;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientTenant that = (ClientTenant) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (alias != null ? !alias.equals(that.alias) : that.alias != null) return false;
        if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) return false;
        if (tenantName != null ? !tenantName.equals(that.tenantName) : that.tenantName != null) return false;
        if (tenantDesc != null ? !tenantDesc.equals(that.tenantDesc) : that.tenantDesc != null) return false;
        if (tenantNote != null ? !tenantNote.equals(that.tenantNote) : that.tenantNote != null) return false;
        if (tenantUri != null ? !tenantUri.equals(that.tenantUri) : that.tenantUri != null) return false;
        if (tenantFolderUri != null ? !tenantFolderUri.equals(that.tenantFolderUri) : that.tenantFolderUri != null)
            return false;
        return theme != null ? theme.equals(that.theme) : that.theme == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (alias != null ? alias.hashCode() : 0);
        result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
        result = 31 * result + (tenantName != null ? tenantName.hashCode() : 0);
        result = 31 * result + (tenantDesc != null ? tenantDesc.hashCode() : 0);
        result = 31 * result + (tenantNote != null ? tenantNote.hashCode() : 0);
        result = 31 * result + (tenantUri != null ? tenantUri.hashCode() : 0);
        result = 31 * result + (tenantFolderUri != null ? tenantFolderUri.hashCode() : 0);
        result = 31 * result + (theme != null ? theme.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientTenant{" +
                "id='" + id + '\'' +
                ", alias='" + alias + '\'' +
                ", parentId='" + parentId + '\'' +
                ", tenantName='" + tenantName + '\'' +
                ", tenantDesc='" + tenantDesc + '\'' +
                ", tenantNote='" + tenantNote + '\'' +
                ", tenantUri='" + tenantUri + '\'' +
                ", tenantFolderUri='" + tenantFolderUri + '\'' +
                ", theme='" + theme + '\'' +
                '}';
    }
}
