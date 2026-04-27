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

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@XmlRootElement(name = ResourceMediaType.VIRTUAL_DATA_SOURCE_CLIENT_TYPE)
public class ClientVirtualDataSource extends ClientResource<ClientVirtualDataSource> implements ClientReferenceableDataSource {
    private List<ClientSubDataSourceReference> subDataSources;

    public ClientVirtualDataSource(ClientVirtualDataSource other) {
        super(other);
        subDataSources = copyOf(other.getSubDataSources());
    }

    public ClientVirtualDataSource() {
    }

    @Override
    public ClientVirtualDataSource deepClone() {
        return new ClientVirtualDataSource(this);
    }

    @XmlElementWrapper(name = "subDataSources")
    @XmlElement(name = "subDataSource")
    public List<ClientSubDataSourceReference> getSubDataSources() {
        return subDataSources;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ClientVirtualDataSource that = (ClientVirtualDataSource) o;

        if (subDataSources != null) {
            if (that.subDataSources != null) {
                return new HashSet(subDataSources).equals(new HashSet(that.subDataSources));
            } else return false;
        } else return that.subDataSources == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (subDataSources != null ? new HashSet(subDataSources).hashCode() : 0);
        return result;
    }

    public ClientVirtualDataSource setSubDataSources(List<ClientSubDataSourceReference> subDataSources) {
        this.subDataSources = subDataSources;
        return this;
    }

    @Override
    public String toString() {
        return "ClientVirtualDataSource{" +
                "subDataSources=" + subDataSources +
                ", version=" + getVersion() +
                ", permissionMask=" + getPermissionMask() +
                ", uri='" + getUri() + '\'' +
                ", label='" + getLabel() + '\'' +
                '}';
    }
}
