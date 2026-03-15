/*
 * Copyright (C) 2025 the Jasper Server OS Authors
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

import com.jaspersoft.jasperserver.dto.common.ResourceLocation;

import javax.xml.bind.annotation.XmlRootElement;

import java.util.Objects;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@XmlRootElement(name = "reference")
public class ClientReference implements ClientReferenceableDataSource, ClientReferenceableDataType,
        ClientReferenceableFile, ClientReferenceableQuery, ClientReferenceableInputControl, ClientReferenceableListOfValues,
        ResourceLocation {

    private String uri;
    // intentionally an object
    private Integer version;

    public ClientReference() {
    }

    public ClientReference(String uri) {
        this.uri = uri;
    }

    public ClientReference(String uri, int version) {
        this.uri = uri;
        this.version = version;
    }

    public ClientReference(ClientReference other) {
        checkNotNull(other);

        this.uri = other.getUri();
        this.version = other.getVersion();
    }

    public String getUri() {
        return uri;
    }

    public ClientReference setUri(String referenceUri) {
        this.uri = referenceUri;
        return this;
    }

    public Integer getVersion() {
        return version;
    }

    public ClientReference setVersion(Integer version) {
        this.version = version;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientReference)) return false;
        ClientReference that = (ClientReference) o;
        return Objects.equals(version, that.version) && Objects.equals(uri, that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, version);
    }

    @Override
    public String toString() {
        return "ClientReference{" +
                "uri='" + uri + '\'' +
                (version != null ? ", version ='" + version + '\'' : "") +
                '}';
    }

    /*
     * DeepCloneable
     */

    @Override
    public ClientReference deepClone() {
        return new ClientReference(this);
    }
}
