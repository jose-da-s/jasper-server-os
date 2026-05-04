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

package com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.content.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @author: Igor.Nesterenko
 * @version: ${Id}
 */
@XmlRootElement(name = "contentReference")
public class ContentReference {

    private String id;
    private String title;
    private String description;
    private String url;
    private String group;

    public ContentReference() {
        super();
    }

    public ContentReference(ContentReference that) {
        id = that.id;
        title = that.title;
        description = that.description;
        url = that.url;
        group = that.group;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public int hashCode() {
        return (new HashCodeBuilder()
                .append(id)
                .append(description)
                .append(title)
                .append(url)
                .append(group)
        ).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof ContentReference)) {
            return false;
        }
        ContentReference that = (ContentReference)obj;
        return new EqualsBuilder()
                .append(this.getId(), that.getId())
                .append(this.getDescription(), that.getDescription())
                .append(this.getUrl(), that.getUrl())
                .append(this.getTitle(), that.getTitle())
                .append(this.getGroup(), that.getGroup())
                .isEquals();
    }
}
