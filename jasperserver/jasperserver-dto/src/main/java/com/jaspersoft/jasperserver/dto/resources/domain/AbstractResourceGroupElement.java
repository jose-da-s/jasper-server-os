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

import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientExpressionContainer;

import jakarta.validation.Valid;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlElements;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * This class is abstract. Use ResourceGroupElement.Builder if you need to instantiate group itself.
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */


public abstract class AbstractResourceGroupElement<T extends AbstractResourceGroupElement<T>> extends ResourceElement<T>
        implements GroupElement<SchemaElement, T>  {
    @Valid
    private List<SchemaElement> elements;
    @Valid
    private ClientExpressionContainer filterExpression;

    public AbstractResourceGroupElement(){}
    public AbstractResourceGroupElement(AbstractResourceGroupElement<T> source) {
        super(source);
        elements = copyOf(source.getElements());
        filterExpression = copyOf(source.getFilterExpression());
    }

    @XmlElementWrapper(name = "elements")
    @XmlElements({
            @XmlElement(name = "reference", type = ReferenceElement.class),
            @XmlElement(name = "group", type = ResourceGroupElement.class),
            @XmlElement(name = "queryGroup", type = QueryResourceGroupElement.class),
            // here different types of resources are marked as 'element'. It's not by mistake.
            // ResourceMetadataSingleElement is used in metadata service only. It doesn't come from the client. So, no conflict here.
            @XmlElement(name = "element", type = ResourceMetadataSingleElement.class),
            @XmlElement(name = "element", type = ResourceSingleElement.class)
    })

    public List<SchemaElement> getElements() {
        return elements;
    }

    public T setElements(List<SchemaElement> elements) {
        this.elements = elements;
        return (T) this;
    }

    public ClientExpressionContainer getFilterExpression() {
        return filterExpression;
    }

    public T setFilterExpression(ClientExpressionContainer filterExpression) {
        this.filterExpression = filterExpression;
        return (T) this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractResourceGroupElement)) return false;
        if (!super.equals(o)) return false;

        AbstractResourceGroupElement that = (AbstractResourceGroupElement) o;

        if (elements != null ? !elements.equals(that.elements) : that.elements != null) return false;
        if (filterExpression != null ? !filterExpression.equals(that.filterExpression) : that.filterExpression != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (elements != null ? elements.hashCode() : 0);
        result = 31 * result + (filterExpression != null ? filterExpression.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AbstractResourceGroupElement{" +
                "elements=" + elements +
                ", filterExpression='" + filterExpression + '\'' +
                "} " + super.toString();
    }
}
