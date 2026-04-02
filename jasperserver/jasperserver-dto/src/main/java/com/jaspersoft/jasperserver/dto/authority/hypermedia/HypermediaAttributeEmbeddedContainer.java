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
package com.jaspersoft.jasperserver.dto.authority.hypermedia;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;
import com.jaspersoft.jasperserver.dto.permissions.RepositoryPermission;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author Volodya Sabadosh
 * @version $Id $
 */
@XmlRootElement(name = "_embedded")
public class HypermediaAttributeEmbeddedContainer implements DeepCloneable<HypermediaAttributeEmbeddedContainer> {
    private List<RepositoryPermission> repositoryPermissions;

    public HypermediaAttributeEmbeddedContainer() {
    }

    public HypermediaAttributeEmbeddedContainer(HypermediaAttributeEmbeddedContainer other) {
        checkNotNull(other);

        this.repositoryPermissions = copyOf(other.getRepositoryPermissions());
    }

    @Override
    public HypermediaAttributeEmbeddedContainer deepClone() {
        return new HypermediaAttributeEmbeddedContainer(this);
    }

    @XmlElement(name = "permission")
    public List<RepositoryPermission> getRepositoryPermissions() {
        return repositoryPermissions;
    }

    public HypermediaAttributeEmbeddedContainer setRepositoryPermissions(List<RepositoryPermission> repositoryPermissions) {
        this.repositoryPermissions = repositoryPermissions;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HypermediaAttributeEmbeddedContainer)) return false;

        HypermediaAttributeEmbeddedContainer that = (HypermediaAttributeEmbeddedContainer) o;

        return repositoryPermissions != null ? repositoryPermissions.equals(that.repositoryPermissions) : that.repositoryPermissions == null;
    }

    @Override
    public int hashCode() {
        return repositoryPermissions != null ? repositoryPermissions.hashCode() : 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "repositoryPermissions=" + repositoryPermissions +
                '}';
    }
}
