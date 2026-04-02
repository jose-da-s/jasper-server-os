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
import com.jaspersoft.jasperserver.dto.resources.domain.validation.NoNullElements;
import com.jaspersoft.jasperserver.dto.resources.domain.validation.ValidReferences;
import com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils;
import org.apache.commons.lang3.ObjectUtils;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@ValidReferences
public class Schema implements DeepCloneable<Schema>, Serializable {

    @NotNull
    @Valid
    private List<ResourceElement> resources;

    @NotNull
    @Valid
    @NoNullElements(errorCode = PresentationGroupElement.DOMAIN_SCHEMA_PRESENTATION_CONTAINS_NULL_ELEMENT,
            message = "Domain schema presentation can't contain null elements")
    private List<PresentationGroupElement> presentation;

    public Schema(){}
    public Schema (Schema source){
        checkNotNull(source);

        resources = copyOf(source.getResources());
        presentation = copyOf(source.getPresentation());
    }

    @Override
    public Schema deepClone() {
        return new Schema(this);
    }

    @XmlElementWrapper(name = "resources")
    @XmlElements({
            @XmlElement(name = "group", type = ResourceGroupElement.class),
            @XmlElement(name = "queryGroup", type = QueryResourceGroupElement.class),
            @XmlElement(name = "element", type = ResourceSingleElement.class),
            @XmlElement(name = "constantsGroup", type = ConstantsResourceGroupElement.class),
            @XmlElement(name = "joinGroup", type = JoinResourceGroupElement.class)
    })

    public List<ResourceElement> getResources() {
        return resources;
    }

    public Schema setResources(List<ResourceElement> resources) {
        this.resources = resources;
        return this;
    }

    @XmlElementWrapper(name="presentation")
    @XmlElement(name="dataIsland")
    @NotNull
    public List<PresentationGroupElement> getPresentation() {
        return presentation;
    }

    public Schema setPresentation(List<PresentationGroupElement> presentation) {
        this.presentation = presentation;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Schema)) return false;

        Schema schema = (Schema) o;
        if (presentation != null ? !presentation.equals(schema.presentation) : schema.presentation != null) return false;
        if ((resources != null && schema.resources != null) && !ValueObjectUtils.equalGroupElements(resources, schema.resources)) return false;
        if ((resources == null && schema.resources != null) || (resources != null && schema.resources == null)) return false;

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
        return "Schema{" +
                "resources=" + resources +
                ", presentation=" + presentation +
                '}';
    }
}
