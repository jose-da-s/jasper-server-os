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
package com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.representation;

import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.Relation;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.representation.embedded.EmbeddedContainer;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.representation.embedded.EmbeddedElement;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashMap;

/**
 * @author Igor.Nesterenko
 * @version $Id$
 */

@XmlRootElement
public class HypermediaRepresentation implements EmbeddedElement {

    private Relation parentRelation;

    protected EmbeddedContainer links = new EmbeddedContainer();

    protected EmbeddedContainer embedded = new EmbeddedContainer();

    public HypermediaRepresentation() {
        super();
    }

    public HypermediaRepresentation(Relation relation) {
        super();
        this.parentRelation = relation;
    }

    @XmlElement(name = "_links")
    public LinkedHashMap<Relation, EmbeddedElement> getLinks() {
        return links.toMap();
    }

    public HypermediaRepresentation addLink(EmbeddedElement embeddedElement){
        if (embeddedElement != null){
            LinkedHashMap<Relation, EmbeddedElement> map = embeddedElement.toMap();
            for (Relation relation : map.keySet()) {
                links.put(relation, map.get(relation));
            }
        }
        return this;
    }

    @XmlElement(name = "_embedded")
    public LinkedHashMap<Relation, EmbeddedElement> getEmbedded() {
        return embedded.toMap();
    }

    @Override
    public LinkedHashMap<Relation, EmbeddedElement> toMap() {

        LinkedHashMap<Relation, EmbeddedElement> map = null;
        if (parentRelation != null){
            map = new LinkedHashMap<Relation, EmbeddedElement>();
            map.put(parentRelation, this);
        }
        return map;
    }

    public HypermediaRepresentation addEmbedded(Relation relation, HypermediaRepresentation representation) {
        if (representation != null){
            embedded.put(relation, representation);
        }
        return this;
    }


    public HypermediaRepresentation setEmbedded(EmbeddedContainer embedded) {
        this.embedded = embedded;
        return this;
    }
}
