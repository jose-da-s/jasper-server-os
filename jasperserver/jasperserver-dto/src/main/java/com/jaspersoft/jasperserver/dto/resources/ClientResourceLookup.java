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

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@XmlRootElement(name = ResourceMediaType.RESOURCE_LOOKUP_CLIENT_TYPE)
public class ClientResourceLookup extends ClientResource<ClientResourceLookup> implements DeepCloneable<ClientResourceLookup>{
    private String resourceType;
    private String thumbnailData;
    private String lastAccessTime;

    public ClientResourceLookup(ClientResourceLookup other) {
        super(other);
        this.resourceType = other.getResourceType();
        this.thumbnailData = other.getThumbnailData();
        this.lastAccessTime = other.getLastAccessTime();
    }

    public ClientResourceLookup() {
    }

    @Override
    public ClientResourceLookup deepClone() {
        return new ClientResourceLookup(this);
    }

    public String getThumbnailData() {
        return thumbnailData;
    }

    public ClientResourceLookup setThumbnailData(String thumbnailData) {
        this.thumbnailData = thumbnailData;
        return this;
    }

    public String getResourceType() {
        return resourceType;
    }

    public ClientResourceLookup setResourceType(String resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    public ClientResourceLookup setLastAccessTime(String lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
        return this;
    }

    public String getLastAccessTime() {
        return lastAccessTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ClientResourceLookup that = (ClientResourceLookup) o;

        if (resourceType != null ? !resourceType.equals(that.resourceType) : that.resourceType != null) return false;
        return thumbnailData != null ? thumbnailData.equals(that.thumbnailData) : that.thumbnailData == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (resourceType != null ? resourceType.hashCode() : 0);
        result = 31 * result + (thumbnailData != null ? thumbnailData.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientResourceLookup{" +
                "version=" + getVersion() +
                ", permissionMask=" + getPermissionMask() +
                ", creationDate='" + getCreationDate() + '\'' +
                ", updateDate='" + getUpdateDate() + '\'' +
                ", label='" + getLabel() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", uri='" + getUri() + '\'' +
                ", resourceType='" + resourceType + '\'' +
                '}';
    }
}
