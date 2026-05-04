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
package com.jaspersoft.jasperserver.dto.resources.domain;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;
import com.jaspersoft.jasperserver.dto.resources.ClientResource;

import jakarta.validation.Valid;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@XmlRootElement(name = "domainSchema")
public class ClientDomainSchema extends ClientResource<ClientDomainSchema> implements DeepCloneable<ClientDomainSchema> {
    @Valid
    private final Schema schema;

    public ClientDomainSchema(){
        schema = new Schema();
    }

    public ClientDomainSchema(Schema schema){
        this.schema = schema != null ? new Schema(schema) : new Schema();
    }

    public ClientDomainSchema(ClientDomainSchema source){
        super(source);
        this.schema = copyOf(source.getSchema());
    }

    @Override
    public ClientDomainSchema deepClone() {
        return new ClientDomainSchema(this);
    }

    @XmlTransient
    public Schema getSchema() {
        return schema;
    }

    public ClientDomainSchema setSchema(Schema schema){
        this.schema.setPresentation(schema != null ? schema.getPresentation() : null);
        this.schema.setResources(schema != null ? schema.getResources() : null);
        return this;
    }

    @XmlElementWrapper(name = "resources")
    @XmlElements({
            @XmlElement(name="group", type = ResourceGroupElement.class),
            @XmlElement(name="queryGroup", type = QueryResourceGroupElement.class),
            @XmlElement(name="element", type = ResourceSingleElement.class),
            @XmlElement(name = "constantsGroup", type = ConstantsResourceGroupElement.class),
            @XmlElement(name="joinGroup", type = JoinResourceGroupElement.class)
    })
    public List<ResourceElement> getResources() {
        return schema.getResources();
    }

    public ClientDomainSchema setResources(List<ResourceElement> resources) {
        schema.setResources(resources);
        return this;
    }

    @XmlElementWrapper(name="presentation")
    @XmlElement(name="dataIsland")
    public List<PresentationGroupElement> getPresentation() {
        return schema.getPresentation();
    }

    public ClientDomainSchema setPresentation(List<PresentationGroupElement> presentation) {
        schema.setPresentation(presentation);
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientDomainSchema)) return false;
        if (!super.equals(o)) return false;

        ClientDomainSchema that = (ClientDomainSchema) o;

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
        return "ClientDomainSchema{" +
                "schema=" + schema +
                "} " + super.toString();
    }
}
