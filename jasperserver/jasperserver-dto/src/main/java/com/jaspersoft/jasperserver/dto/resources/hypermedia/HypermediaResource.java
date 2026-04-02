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

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;
import com.jaspersoft.jasperserver.dto.resources.ClientResource;

import jakarta.xml.bind.annotation.XmlElement;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * Created by borys.kolesnykov on 9/23/2014.
 */
public class HypermediaResource extends ClientResource<HypermediaResource> implements DeepCloneable<HypermediaResource> {

    private HypermediaResourceLinks links;
    private HypermediaEmbeddedContainer embedded;

    public HypermediaResource() {
    }

    public HypermediaResource(ClientResource other) {
        super(other);
        if(other instanceof HypermediaResource) {
            links = copyOf(((HypermediaResource)other).getLinks());
            embedded = copyOf(((HypermediaResource)other).getEmbedded());
        } else {
            throw new IllegalArgumentException("Expected HypermediaResource class, but was: " + other.getClass().getName());
        }
    }

    @Override
    public HypermediaResource deepClone() {
        return new HypermediaResource(this);
    }

    @XmlElement(name = "_embedded")
    public HypermediaEmbeddedContainer getEmbedded() {
        return embedded;
    }

    public HypermediaResource setEmbedded(HypermediaEmbeddedContainer embedded) {
        this.embedded = embedded;
        return this;
    }

    @XmlElement(name = "_links")
    public HypermediaResourceLinks getLinks() {
        return links;
    }

    public HypermediaResource setLinks(HypermediaResourceLinks links) {
        this.links = links;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        HypermediaResource that = (HypermediaResource) o;

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
        return "HypermediaResource{" +
                "links=" + links +
                ", embedded=" + embedded +
                "} " + super.toString();
    }
}
