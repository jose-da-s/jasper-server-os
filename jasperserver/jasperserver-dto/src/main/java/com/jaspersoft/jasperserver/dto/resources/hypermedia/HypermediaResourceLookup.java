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

package com.jaspersoft.jasperserver.dto.resources.hypermedia;

import com.jaspersoft.jasperserver.dto.resources.ClientResourceLookup;

import jakarta.xml.bind.annotation.XmlElement;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * Created by borys.kolesnykov on 9/22/2014.
 */
public class HypermediaResourceLookup extends ClientResourceLookup {

    private HypermediaResourceLinks links;
    private HypermediaEmbeddedContainer embedded;

    public HypermediaResourceLookup() {
        super();
    }

    public HypermediaResourceLookup(HypermediaResourceLookup other) {
        super(other);
        links = copyOf(other.getLinks());
        embedded = copyOf(other.getEmbedded());
    }

    public HypermediaResourceLookup(ClientResourceLookup other) {
        super(other);
    }

    @Override
    public HypermediaResourceLookup deepClone() {
        return new HypermediaResourceLookup(this);
    }

    @XmlElement(name = "_embedded")
    public HypermediaEmbeddedContainer getEmbedded() {
        return embedded;
    }

    public HypermediaResourceLookup setEmbedded(HypermediaEmbeddedContainer embedded) {
        this.embedded = embedded;
        return this;
    }

    @XmlElement(name = "_links")
    public HypermediaResourceLinks getLinks() {
        return links;
    }

    public HypermediaResourceLookup setLinks(HypermediaResourceLinks links) {
        this.links = links;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        HypermediaResourceLookup that = (HypermediaResourceLookup) o;

        if (links != null ? !links.equals(that.links) : that.links != null) return false;
        return embedded != null ? embedded.equals(that.embedded) : that.embedded == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (links != null ? links.hashCode() : 0);
        result = 31 * result + (embedded != null ? embedded.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HypermediaResourceLookup{" +
                "links=" + links +
                ", embedded=" + embedded +
                "} " + super.toString();
    }
}
