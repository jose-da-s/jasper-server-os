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

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author Zakhar.Tomchenco
 * @version $Id$
 */
@XmlRootElement(name = "resources")
public class HypermediaResourceListWrapper implements DeepCloneable<HypermediaResourceListWrapper> {
    private List<HypermediaResourceLookup> resourceLookups;
    private HypermediaResourceLookupLinks links;

    public HypermediaResourceListWrapper() {
    }

    public HypermediaResourceListWrapper(List<HypermediaResourceLookup> resourceLookups) {
        this.resourceLookups = resourceLookups;
    }

    public HypermediaResourceListWrapper(HypermediaResourceListWrapper other) {
        checkNotNull(other);

        this.resourceLookups = copyOf(other.getResourceLookups());
        this.links = copyOf(other.getLinks());
    }

    @Override
    public HypermediaResourceListWrapper deepClone() {
        return new HypermediaResourceListWrapper(this);
    }

    @XmlElement(name = "resourceLookup")
    public List<HypermediaResourceLookup> getResourceLookups() {
        return resourceLookups;
    }

    public HypermediaResourceListWrapper setResourceLookups(List<HypermediaResourceLookup> resourceLookups) {
        this.resourceLookups = resourceLookups;
        return this;
    }

    @XmlElement(name = "_links")
    public HypermediaResourceLookupLinks getLinks() {
        return links;
    }

    public HypermediaResourceListWrapper setLinks(HypermediaResourceLookupLinks links) {
        this.links = links;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HypermediaResourceListWrapper that = (HypermediaResourceListWrapper) o;

        if (resourceLookups != null ? !resourceLookups.equals(that.resourceLookups) : that.resourceLookups != null)
            return false;
        return links != null ? links.equals(that.links) : that.links == null;
    }

    @Override
    public int hashCode() {
        int result = resourceLookups != null ? resourceLookups.hashCode() : 0;
        result = 31 * result + (links != null ? links.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HypermediaResourceListWrapper{" +
                "resourceLookups=" + resourceLookups +

                '}';
    }
}
