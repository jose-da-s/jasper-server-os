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

package com.jaspersoft.jasperserver.dto.thumbnails;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Grant Bacon <gbacon@tibco.com>
 */
public class ResourceThumbnailsListWrapper implements DeepCloneable<ResourceThumbnailsListWrapper> {
    private List<ResourceThumbnail> thumbnails;


    public ResourceThumbnailsListWrapper() {}

    public ResourceThumbnailsListWrapper(ResourceThumbnailsListWrapper other) {
        checkNotNull(other);

        this.thumbnails = copyOf(other.getThumbnails());
    }

    public ResourceThumbnailsListWrapper(List<ResourceThumbnail> thumbnails) {
        this.thumbnails = thumbnails;
    }

    @XmlElement(name = "thumbnail")
    public List<ResourceThumbnail> getThumbnails() {
        return thumbnails;
    }

    public ResourceThumbnailsListWrapper setThumbnails(List<ResourceThumbnail> thumbnails) {
        this.thumbnails = thumbnails;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceThumbnailsListWrapper)) return false;

        ResourceThumbnailsListWrapper that = (ResourceThumbnailsListWrapper) o;

        if (thumbnails != null ? !thumbnails.equals(that.thumbnails) : that.thumbnails != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return thumbnails != null ? thumbnails.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ResourceThumbnailsListWrapper{" +
                "thumbnails=" + thumbnails +
                '}';
    }

    /*
     * DeepCloneable
     */

    @Override
    public ResourceThumbnailsListWrapper deepClone() {
        return new ResourceThumbnailsListWrapper(this);
    }
}
