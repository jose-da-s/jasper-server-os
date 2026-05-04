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
import com.jaspersoft.jasperserver.dto.resources.ClientResourceLookup;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * Created by borys.kolesnykov on 9/22/2014.
 */
@XmlRootElement(name = "_embedded")
public class HypermediaEmbeddedContainer implements DeepCloneable<HypermediaEmbeddedContainer> {

    private List<ClientResourceLookup> resourceLookup;

    public HypermediaEmbeddedContainer() {
    }

    public HypermediaEmbeddedContainer(HypermediaEmbeddedContainer other) {
        checkNotNull(other);

        resourceLookup = copyOf(other.getResourceLookup());
    }

    @Override
    public HypermediaEmbeddedContainer deepClone() {
        return new HypermediaEmbeddedContainer(this);
    }

    public List<ClientResourceLookup> getResourceLookup() {
        return resourceLookup;
    }

    public HypermediaEmbeddedContainer setResourceLookup(List<ClientResourceLookup> resourceLookup) {
        this.resourceLookup = resourceLookup;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HypermediaEmbeddedContainer that = (HypermediaEmbeddedContainer) o;

        return resourceLookup != null ? resourceLookup.equals(that.resourceLookup) : that.resourceLookup == null;
    }

    @Override
    public int hashCode() {
        return resourceLookup != null ? resourceLookup.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "HypermediaEmbeddedContainer{" +
                "resourceLookup=" + resourceLookup +
                '}';
    }
}
