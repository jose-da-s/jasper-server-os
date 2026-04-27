/*
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
package com.jaspersoft.jasperserver.dto.resources.domain;

import jakarta.validation.constraints.Size;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
public class PresentationElement<T extends PresentationElement<T>> extends SchemaElement<T> {
    public static final int LABEL_MAX_LENGTH = 840;
    public static final int LABEL_ID_MAX_LENGTH = 20000;
    public static final int DESCRIPTION_MAX_LENGTH = 20000;
    public static final int DESCRIPTION_ID_MAX_LENGTH = 20000;
    @Size(max = LABEL_MAX_LENGTH, message = "domain.schema.presentation.element.label.length.limit")
    private String label;
    @Size(max = LABEL_ID_MAX_LENGTH, message = "domain.schema.presentation.element.labelId.length.limit")
    private String labelId;
    @Size(max = DESCRIPTION_MAX_LENGTH, message = "domain.schema.presentation.element.description.length.limit")
    private String description;
    @Size(max = DESCRIPTION_ID_MAX_LENGTH, message = "domain.schema.presentation.element.descriptionId.length.limit")
    private String descriptionId;
    private String resourcePath;

    public PresentationElement() {
    }

    public PresentationElement(PresentationElement source) {
        super(source);
        description = source.getDescription();
        descriptionId = source.getDescriptionId();
        label = source.getLabel();
        labelId = source.getLabelId();
        resourcePath = source.getResourcePath();
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public T setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
        return (T) this;
    }

    @Override
    public T deepClone() {
        return (T) new PresentationElement(this);
    }

    public String getLabelId() {
        return labelId;
    }

    public T setLabelId(String labelId) {
        this.labelId = labelId;
        return (T) this;
    }

    public String getDescriptionId() {
        return descriptionId;
    }

    public T setDescriptionId(String descriptionId) {
        this.descriptionId = descriptionId;
        return (T) this;
    }

    public String getLabel() {
        return label;
    }

    public T setLabel(String label) {
        this.label = label;
        return (T) this;
    }

    public String getDescription() {
        return description;
    }

    public T setDescription(String description) {
        this.description = description;
        return (T) this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PresentationElement)) return false;
        if (!super.equals(o)) return false;

        PresentationElement<?> element = (PresentationElement<?>) o;

        if (label != null ? !label.equals(element.label) : element.label != null) return false;
        if (labelId != null ? !labelId.equals(element.labelId) : element.labelId != null) return false;
        if (description != null ? !description.equals(element.description) : element.description != null) return false;
        if (descriptionId != null ? !descriptionId.equals(element.descriptionId) : element.descriptionId != null)
            return false;
        return resourcePath != null ? resourcePath.equals(element.resourcePath) : element.resourcePath == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (labelId != null ? labelId.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (descriptionId != null ? descriptionId.hashCode() : 0);
        result = 31 * result + (resourcePath != null ? resourcePath.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PresentationElement{" +
                "label='" + label + '\'' +
                ", labelId='" + labelId + '\'' +
                ", description='" + description + '\'' +
                ", descriptionId='" + descriptionId + '\'' +
                ", resourcePath='" + resourcePath + '\'' +
                "} " + super.toString();
    }
}
