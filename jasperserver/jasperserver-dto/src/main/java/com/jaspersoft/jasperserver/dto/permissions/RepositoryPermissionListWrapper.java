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

package com.jaspersoft.jasperserver.dto.permissions;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author Zakhar.Tomchenco
 * @version $Id$
 */
@XmlRootElement(name = "permissions")
public class RepositoryPermissionListWrapper implements DeepCloneable<RepositoryPermissionListWrapper> {
    private List<RepositoryPermission> permissions;

    public RepositoryPermissionListWrapper() {
    }

    public RepositoryPermissionListWrapper(List<RepositoryPermission> permissions) {
        this.permissions = permissions;
    }

    public RepositoryPermissionListWrapper(RepositoryPermissionListWrapper other) {
        checkNotNull(other);

        this.permissions = copyOf(other.getPermissions());
    }

    @Override
    public RepositoryPermissionListWrapper deepClone() {
        return new RepositoryPermissionListWrapper(this);
    }

    @XmlElement(name = "permission")
    public List<RepositoryPermission> getPermissions() {
        return permissions;
    }

    public RepositoryPermissionListWrapper setPermissions(List<RepositoryPermission> permissions) {
        this.permissions = permissions;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RepositoryPermissionListWrapper that = (RepositoryPermissionListWrapper) o;

        if (permissions != null ? !permissions.equals(that.permissions) : that.permissions != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return permissions != null ? permissions.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "RepositoryPermissionListWrapper{" +
                "permissions=" + permissions +
                '}';
    }
}
