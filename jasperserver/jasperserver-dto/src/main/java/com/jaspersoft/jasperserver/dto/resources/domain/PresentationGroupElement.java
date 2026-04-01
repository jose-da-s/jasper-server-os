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

import com.jaspersoft.jasperserver.dto.resources.domain.validation.NoNullElements;

import jakarta.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@XmlRootElement(name = "group")
public class PresentationGroupElement extends PresentationElement<PresentationGroupElement>
        implements GroupElement<PresentationElement, PresentationGroupElement> {
    public static final String DOMAIN_SCHEMA_PRESENTATION_CONTAINS_NULL_ELEMENT = "domain.schema.presentation.contains.null.element";
    @Valid
    @NoNullElements(errorCode = DOMAIN_SCHEMA_PRESENTATION_CONTAINS_NULL_ELEMENT,
            message = "Domain schema presentation group can't contain null elements")
    private List<PresentationElement> elements;
    private String kind;

    public PresentationGroupElement(){}

    public PresentationGroupElement(PresentationGroupElement source){
        super(source);
        this.elements = copyOf(source.getElements());
        this.kind = source.kind;
    }

    @Override
    public PresentationGroupElement deepClone() {
        return new PresentationGroupElement(this);
    }

    @XmlElementWrapper(name = "elements")
    @XmlElements({
            @XmlElement(name="group", type = PresentationGroupElement.class),
            @XmlElement(name="element", type = PresentationSingleElement.class)
    })
    public List<PresentationElement> getElements() {
        return elements;
    }

    public String getKind() {
        return kind;
    }

    public PresentationGroupElement setKind(String kind) {
        this.kind = kind;
        return this;
    }

    public PresentationGroupElement setElements(List<PresentationElement> elements) {
        this.elements = elements;
        return this;
    }

    public PresentationGroupElement addElements(PresentationElement ... elements){
        final List<PresentationElement> presentationElements = Arrays.asList(elements);
        if(this.elements == null){
            this.elements = presentationElements;
        } else {
            this.elements.addAll(presentationElements);
        }
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PresentationGroupElement that = (PresentationGroupElement) o;

        if (elements != null ? !elements.equals(that.elements) : that.elements != null) return false;
        if (kind != null ? !kind.equals(that.kind) : that.kind != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (elements != null ? elements.hashCode() : 0);
        result = 31 * result + (kind != null ? kind.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PresentationGroupElement{" +
                "elements=" + elements +
                ", kind='" + kind + '\'' +
                "} " + super.toString();
    }
}
