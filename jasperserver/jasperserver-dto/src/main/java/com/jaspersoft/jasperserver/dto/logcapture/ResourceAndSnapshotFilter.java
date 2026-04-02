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
package com.jaspersoft.jasperserver.dto.logcapture;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;

/**
 * @author Yakiv Tymoshenko
 * @version $Id$
 * @since 10.02.2015
 */
@XmlRootElement
@XmlType(propOrder = {"resourceUri", "includeDataSnapshots"})
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ResourceAndSnapshotFilter implements DeepCloneable<ResourceAndSnapshotFilter> {
    private String resourceUri;
    private Boolean includeDataSnapshots;

    public ResourceAndSnapshotFilter() {
    }

    public ResourceAndSnapshotFilter(ResourceAndSnapshotFilter other) {
        checkNotNull(other);

        this.resourceUri = other.getResourceUri();
        this.includeDataSnapshots = other.getIncludeDataSnapshots();
    }

    @Override
    public ResourceAndSnapshotFilter deepClone() {
        return new ResourceAndSnapshotFilter(this);
    }

    /*
        Don't name it "isExportEnabled" because it would look like a property in resulting JSON/XML.
     */
    public boolean exportEnabled() {
        return includeDataSnapshots != null
                && (includeDataSnapshots && resourceUri != null && resourceUri.length() > 0);
    }

    public boolean exportDatasnapshotEnabled() {
        return Boolean.TRUE.equals(includeDataSnapshots) && resourceUriSet();
    }

    public boolean resourceUriSet() {
        if (resourceUri == null || resourceUri.trim().isEmpty()) {
            return false;
        }
        return true;
    }

    public boolean resourceUriMatch(String otherUri) {
        return resourceUriSet() && resourceUri.equals(otherUri);
    }


    @XmlElement(name = "uri")
    public String getResourceUri() {
        return resourceUri;
    }

    public ResourceAndSnapshotFilter setResourceUri(String resourceUri) {
        this.resourceUri = resourceUri;
        return this;
    }

    @XmlElement(name = "includeDataSnapshot")
    public Boolean getIncludeDataSnapshots() {
        return includeDataSnapshots;
    }

    public ResourceAndSnapshotFilter setIncludeDataSnapshots(Boolean includeDataSnapshots) {
        this.includeDataSnapshots = includeDataSnapshots;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceAndSnapshotFilter)) return false;

        ResourceAndSnapshotFilter that = (ResourceAndSnapshotFilter) o;

        if (getResourceUri() != null ? !getResourceUri().equals(that.getResourceUri()) : that.getResourceUri() != null)
            return false;
        return !(getIncludeDataSnapshots() != null ? !getIncludeDataSnapshots().equals(that.getIncludeDataSnapshots()) : that.getIncludeDataSnapshots() != null);

    }

    @Override
    public int hashCode() {
        int result = getResourceUri() != null ? getResourceUri().hashCode() : 0;
        result = 31 * result + (getIncludeDataSnapshots() != null ? getIncludeDataSnapshots().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ResourceAndSnapshotFilter{" +
                "resourceUri='" + resourceUri + '\'' +
                ", includeDataSnapshots=" + includeDataSnapshots +
                '}';
    }
}
