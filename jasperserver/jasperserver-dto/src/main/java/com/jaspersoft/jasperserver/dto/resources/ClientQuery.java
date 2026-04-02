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

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@XmlRootElement(name = ResourceMediaType.QUERY_CLIENT_TYPE)
@XmlType(name = ResourceMediaType.QUERY_CLIENT_TYPE)
public class ClientQuery extends AbstractClientDataSourceHolder<ClientQuery> implements ClientReferenceableQuery {
    private String value;
    private String language;

    public ClientQuery(ClientQuery other) {
        super(other);
        this.value = other.getValue();
        this.language = other.getLanguage();
    }

    public ClientQuery() {
    }

    public String getValue() {
        return value;
    }

    public ClientQuery setValue(String value) {
        this.value = value;
        return this;
    }

    public String getLanguage() {
        return language;
    }

    public ClientQuery setLanguage(String language) {
        this.language = language;
        return this;
    }

    @Override
    public String toString() {
        return "ClientQuery{" +
                "value='" + value + '\'' +
                ", language='" + language + '\'' +
                ", version=" + getVersion() +
                ", permissionMask=" + getPermissionMask() +
                ", uri='" + getUri() + '\'' +
                ", label='" + getLabel() + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ClientQuery that = (ClientQuery) o;

        if (language != null ? !language.equals(that.language) : that.language != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (language != null ? language.hashCode() : 0);
        return result;
    }

    /*
     * DeepCloneable
     */

    @Override
    public ClientQuery deepClone() {
        return new ClientQuery(this);
    }
}
