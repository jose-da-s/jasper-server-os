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

import jakarta.xml.bind.annotation.XmlRootElement;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;

/**
 * @author Grant Bacon <gbacon@jaspersoft.com>
 * @version $Id: $
 */

@XmlRootElement
public class ResourceThumbnail implements DeepCloneable<ResourceThumbnail> {

    private String uri;
    private String thumbnailData;

    public ResourceThumbnail() {}

    public ResourceThumbnail(ResourceThumbnail source) {
        checkNotNull(source);

        uri = source.getUri();
        thumbnailData = source.getThumbnailData();
    }

    public String getUri() {
        return uri;
    }

    public ResourceThumbnail setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public String getThumbnailData() {
        return thumbnailData;
    }

    public ResourceThumbnail setThumbnailData(String thumbnailData) {
        this.thumbnailData = thumbnailData;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceThumbnail that = (ResourceThumbnail) o;
        if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;
        if (thumbnailData != null ? !thumbnailData.equals(that.thumbnailData) : that.thumbnailData != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uri != null ? uri.hashCode() : 0;
        result = 31 * result + (thumbnailData != null ? thumbnailData.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ResourceThumbnail{" +
                "uri='" + uri + '\'' +
                ", thumbnailData='" + thumbnailData + '\'' +
                "} " + super.toString();
    }

    /*
     * DeepCloneable
     */

    @Override
    public ResourceThumbnail deepClone() {
        return new ResourceThumbnail(this);
    }
}
