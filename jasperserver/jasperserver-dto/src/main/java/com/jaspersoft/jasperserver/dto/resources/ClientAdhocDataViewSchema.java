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
import com.jaspersoft.jasperserver.dto.resources.domain.AbstractResourceGroupElement;
import com.jaspersoft.jasperserver.dto.resources.domain.ConstantsResourceGroupElement;
import com.jaspersoft.jasperserver.dto.resources.domain.PresentationElement;
import com.jaspersoft.jasperserver.dto.resources.domain.PresentationGroupElement;
import com.jaspersoft.jasperserver.dto.resources.domain.PresentationSingleElement;
import com.jaspersoft.jasperserver.dto.resources.domain.ResourceElement;
import com.jaspersoft.jasperserver.dto.resources.domain.ResourceGroupElement;
import com.jaspersoft.jasperserver.dto.resources.domain.ResourceSingleElement;
import com.jaspersoft.jasperserver.dto.resources.domain.SchemaElement;
import com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlElements;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author Volodya Sabadosh
 * @version $Id: $
 */
public class ClientAdhocDataViewSchema implements DeepCloneable<ClientAdhocDataViewSchema>, Serializable {
    private List<ResourceElement> resources;
    private List<PresentationElement> presentation;

    public ClientAdhocDataViewSchema(){}

    public ClientAdhocDataViewSchema(ClientAdhocDataViewSchema source){
        checkNotNull(source);

        presentation = copyOf(source.getPresentation());
        resources = copyOf(source.getResources());
    }

    @Override
    public ClientAdhocDataViewSchema deepClone() {
        return new ClientAdhocDataViewSchema(this);
    }

    @XmlElementWrapper(name = "resources")
    @XmlElements({
            @XmlElement(name = "group", type = ResourceGroupElement.class),
            @XmlElement(name = "element", type = ResourceSingleElement.class),
    })
    public List<ResourceElement> getResources() {
        return resources;
    }

    public ClientAdhocDataViewSchema setResources(List<ResourceElement> resources) {
        this.resources = resources;
        return this;
    }

    @XmlElementWrapper(name="presentation")
    @XmlElements({
            @XmlElement(name = "group", type = PresentationGroupElement.class),
            @XmlElement(name = "element", type = PresentationSingleElement.class)
    })
    public List<PresentationElement> getPresentation() {
        return presentation;
    }

    public ClientAdhocDataViewSchema setPresentation(List<PresentationElement> presentation) {
        this.presentation = presentation;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientAdhocDataViewSchema)) return false;

        ClientAdhocDataViewSchema schema = (ClientAdhocDataViewSchema) o;

        if (presentation != null ? !presentation.equals(schema.presentation) : schema.presentation != null)
            return false;
        if (!ValueObjectUtils.equalGroupElements(resources, schema.resources))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = resources != null ? ValueObjectUtils.sortResourceGroupElement(resources).hashCode() : 0;
        result = 31 * result + (presentation != null ? presentation.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientAdhocDataViewSchema{" +
                "resources=" + resources +
                ", presentation=" + presentation +
                '}';
    }
}

