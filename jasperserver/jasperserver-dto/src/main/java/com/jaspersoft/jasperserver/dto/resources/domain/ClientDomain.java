/*
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
package com.jaspersoft.jasperserver.dto.resources.domain;

import com.jaspersoft.jasperserver.dto.resources.BaseSemanticLayerDataSource;
import com.jaspersoft.jasperserver.dto.resources.ResourceMediaType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 */
@XmlRootElement(name = ResourceMediaType.DOMAIN_CLIENT_TYPE)
public class ClientDomain extends BaseSemanticLayerDataSource<ClientDomain, Schema> {
    @Valid
    @NotNull
    private Schema schema;

    public ClientDomain(){}
    public ClientDomain(ClientDomain source){
        super(source);
        schema = copyOf(source.getSchema());
    }

    @Override
    public ClientDomain deepClone() {
        return new ClientDomain(this);
    }

    public Schema getSchema() {
        return schema;
    }

    public ClientDomain setSchema(Schema schema) {
        this.schema = schema;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientDomain)) return false;
        if (!super.equals(o)) return false;

        ClientDomain that = (ClientDomain) o;

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
        return "ClientDomain{" +
                "schema=" + schema +
                "} " + super.toString();
    }
}
