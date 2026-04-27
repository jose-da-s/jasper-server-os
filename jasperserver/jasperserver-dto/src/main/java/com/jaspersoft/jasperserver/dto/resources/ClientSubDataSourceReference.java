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

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@XmlRootElement(name = "subDataSource")
public class ClientSubDataSourceReference implements DeepCloneable<ClientSubDataSourceReference> {
    private String id;
    private String uri;

    public ClientSubDataSourceReference(ClientSubDataSourceReference other) {
        checkNotNull(other);

        this.id = other.getId();
        this.uri = other.getUri();
    }

    public ClientSubDataSourceReference() {
    }

    @Override
    public ClientSubDataSourceReference deepClone() {
        return new ClientSubDataSourceReference(this);
    }

    public String getId() {
        return id;
    }

    public ClientSubDataSourceReference setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientSubDataSourceReference that = (ClientSubDataSourceReference) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        return result;
    }

    public String getUri() {
        return uri;
    }

    public ClientSubDataSourceReference setUri(String uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public String toString() {
        return "ClientSubDataSourceReference{" +
                "id='" + id + '\'' +
                ", uri='" + uri + '\'' +

                '}';
    }
}
