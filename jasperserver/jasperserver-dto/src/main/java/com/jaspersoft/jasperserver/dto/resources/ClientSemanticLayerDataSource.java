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
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p>SemanticLayerDataSource belongs to PRO codebase, but ClientSemanticLayerDataSource should be placed to CE because of usage in AbstractClientDataSourceHolder</p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@XmlRootElement(name = ResourceMediaType.SEMANTIC_LAYER_DATA_SOURCE_CLIENT_TYPE)
public class ClientSemanticLayerDataSource extends BaseSemanticLayerDataSource<ClientSemanticLayerDataSource, ClientReferenceableFile> {
    private ClientReferenceableFile schema;

    public ClientSemanticLayerDataSource(ClientSemanticLayerDataSource other) {
        super(other);
        schema = copyOf(other.getSchema());
    }

    public ClientSemanticLayerDataSource() {
    }

    @Override
    public ClientSemanticLayerDataSource deepClone() {
        return new ClientSemanticLayerDataSource(this);
    }


    @XmlElements({
            @XmlElement(name = "schemaFileReference", type = ClientReference.class),
            @XmlElement(name = "schemaFile", type = ClientFile.class)
    })
    public ClientReferenceableFile getSchema() {
        return schema;
    }

    public ClientSemanticLayerDataSource setSchema(ClientReferenceableFile schema) {
        this.schema = schema;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientSemanticLayerDataSource)) return false;
        if (!super.equals(o)) return false;

        ClientSemanticLayerDataSource that = (ClientSemanticLayerDataSource) o;

        if (schema != null ? !schema.equals(that.schema) : that.schema != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (schema != null ? schema.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientSemanticLayerDataSource{" +
                "schema=" + schema +
                "} " + super.toString();
    }
}
