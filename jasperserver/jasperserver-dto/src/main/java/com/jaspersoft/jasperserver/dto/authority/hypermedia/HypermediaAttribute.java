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
package com.jaspersoft.jasperserver.dto.authority.hypermedia;

import com.jaspersoft.jasperserver.dto.authority.ClientAttribute;
import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author Volodya Sabadosh
 * @version $Id$
 */
@XmlRootElement(name = "attribute")
public class HypermediaAttribute extends ClientAttribute<HypermediaAttribute> implements DeepCloneable<HypermediaAttribute> {
    private HypermediaAttributeEmbeddedContainer embedded;
    private HypermediaAttributeLinks links;

    public HypermediaAttribute(ClientAttribute other) {
        super(other);
    }

    public HypermediaAttribute() {
    }

    public HypermediaAttribute(HypermediaAttribute other) {
        super(other);
        embedded = copyOf(other.getEmbedded());
        links = copyOf(other.getLinks());
    }

    @Override
    public HypermediaAttribute deepClone() {
        return new HypermediaAttribute(this);
    }

    @XmlElement(name = "_embedded")
    public HypermediaAttributeEmbeddedContainer getEmbedded() {
        return embedded;
    }

    public HypermediaAttribute setEmbedded(HypermediaAttributeEmbeddedContainer embedded) {
        this.embedded = embedded;
        return this;
    }

    @XmlElement(name = "_links")
    public HypermediaAttributeLinks getLinks() {
        return links;
    }

    public HypermediaAttribute setLinks(HypermediaAttributeLinks links) {
        this.links = links;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HypermediaAttribute)) return false;
        if (!super.equals(o)) return false;

        HypermediaAttribute that = (HypermediaAttribute) o;

        if (embedded != null ? !embedded.equals(that.embedded) : that.embedded != null) return false;
        return links != null ? links.equals(that.links) : that.links == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (embedded != null ? embedded.hashCode() : 0);
        result = 31 * result + (links != null ? links.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "embedded=" + embedded +
                ", links=" + links +
                "} " + super.toString();
    }
}
