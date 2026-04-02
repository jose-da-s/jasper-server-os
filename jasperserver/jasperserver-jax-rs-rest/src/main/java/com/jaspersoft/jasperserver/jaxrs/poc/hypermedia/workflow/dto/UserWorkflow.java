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
package com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.workflow.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @author Igor.Nesterenko
 * @version $Id$
 */

@XmlRootElement(name = "workflow")
public class UserWorkflow {

    private String parentName;
    private String name;
    private String label;
    private String description;

    private String category;
    //Links to specific images embedded in sprite
    private String imageUri = "images/home_icons_sprite.png";
    private String contentReferenceId;

    public UserWorkflow() {
        super();
    }

    public UserWorkflow(UserWorkflow that) {
        super();
        name =  that.name;
        label = that.label;
        imageUri = that.imageUri;
        parentName = that.parentName;
        description =that.description;
        category = that.category;
        contentReferenceId = that.contentReferenceId;
    }

    public String getImageUri() {
        return imageUri;
    }

    public UserWorkflow setImageUri(String imageUri) {
        this.imageUri = imageUri;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public UserWorkflow setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public UserWorkflow setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public UserWorkflow setCategory(String category) {
        this.category = category;
        return this;
    }

    public String getName() {
        return name;
    }

    public UserWorkflow setName(String name) {
        this.name = name;
        return this;
    }

    public String getParentName() {
        return parentName;
    }

    public UserWorkflow setParentName(String parentName) {
        this.parentName = parentName;
        return this;
    }

    public String getContentReferenceId() {
        return contentReferenceId;
    }

    public UserWorkflow setContentReferenceId(String contentReferenceId) {
        this.contentReferenceId = contentReferenceId;
        return this;
    }

    @Override
    public int hashCode() {
        return (new HashCodeBuilder()
                .append(name)
                .append(description)
                .append(category)
                .append(label)
                .append(imageUri)
                .append(contentReferenceId)
        ).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof UserWorkflow)) {
            return false;
        }
        UserWorkflow that = (UserWorkflow)obj;
        return new EqualsBuilder()
                .append(this.getName(), that.getName())
                .append(this.getDescription(), that.getDescription())
                .append(this.getCategory(), that.getCategory())
                .append(this.getImageUri(), that.getImageUri())
                .append(this.getLabel(), that.getLabel())
                .append(this.getContentReferenceId(), that.getContentReferenceId())
                .isEquals();
    }


}
