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

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
public class ConstantsResourceGroupElement extends ResourceElement<ConstantsResourceGroupElement>
        implements GroupElement<SchemaElement, ConstantsResourceGroupElement>, DeepCloneable<ConstantsResourceGroupElement> {
    @Valid
    private List<SchemaElement> elements;

    public ConstantsResourceGroupElement(){}

    public ConstantsResourceGroupElement(ConstantsResourceGroupElement source){
        super(source);
        elements = copyOf(source.getElements());
    }

    @Override
    public ConstantsResourceGroupElement deepClone() {
        return new ConstantsResourceGroupElement(this);
    }

    @XmlElementWrapper(name = "elements")
    @XmlElements({
            @XmlElement(name = "element", type = ResourceSingleElement.class)
    })
    public List<SchemaElement> getElements() {
        return elements;
    }

    public ConstantsResourceGroupElement setElements(List<SchemaElement> elements) {
        this.elements = elements;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConstantsResourceGroupElement)) return false;
        if (!super.equals(o)) return false;

        ConstantsResourceGroupElement that = (ConstantsResourceGroupElement) o;

        if (elements != null ? !elements.equals(that.elements) : that.elements != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (elements != null ? elements.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConstantsResourceGroupElement{" +
                "elements=" + elements +
                "} " + super.toString();
    }
}
