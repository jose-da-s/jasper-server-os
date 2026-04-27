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
package com.jaspersoft.jasperserver.dto.adhoc.query.field;

import com.jaspersoft.jasperserver.dto.adhoc.datasource.ClientDataSourceField;
import com.jaspersoft.jasperserver.dto.adhoc.datasource.ClientDataSourceLevel;

import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * @author Andriy Godovanets
 */
public class ClientQueryLevel extends ClientQueryGroup implements Serializable {
    private String dimension;
    private String hierarchyName;

    public ClientQueryLevel() {
        // No op
    }

    public ClientQueryLevel(ClientQueryLevel source) {
        super(source);
        dimension = source.getDimension();
        hierarchyName = source.getHierarchyName();
    }

    @Override
    public ClientQueryLevel deepClone() {
        return new ClientQueryLevel(this);
    }

    /**
     * Name of the dimension to which current level belongs to.
     *
     * If not specified, then dimension name equal to level name
     */
    public String getDimension() {
        return dimension;
    }

    @NotNull
    @XmlElement(name = "dimension")
    public ClientQueryLevel setDimension(String dimension) {
        this.dimension = dimension;
        return this;
    }

    /**
     * Name of the hierarhcy to which current level belongs to.
     *
     * If not specified, then null
     */
    @XmlElement(name = "hierarchy")
    public String getHierarchyName() {
        return hierarchyName;
    }

    public ClientQueryLevel setHierarchyName(String hierarchyName) {
        this.hierarchyName = hierarchyName;
        return this;
    }

    @Override
    public ClientQueryLevel setCategorizer(String categorizer) {
        return (ClientQueryLevel) super.setCategorizer(categorizer);
    }

    public ClientQueryLevel setDataSourceField(ClientDataSourceLevel field) {
        super.setDataSourceField(field);
        if (field != null) {
            setDimension(field.getDimensionName());
            setHierarchyName(field.getHierarchyName());
        } else {
            dimension = null;
            hierarchyName = null;
        }
        return this;
    }

    @Override
    public ClientQueryLevel setFieldName(String fieldName) {
        return (ClientQueryLevel) super.setFieldName(fieldName);
    }

    @Override
    @XmlTransient
    public ClientQueryLevel setDataSourceField(ClientDataSourceField field) {
        return (ClientQueryLevel) super.setDataSourceField(field);
    }

    @Override
    public ClientQueryLevel setId(String id) {
        return (ClientQueryLevel) super.setId(id);
    }

    @Override
    public ClientQueryLevel setIncludeAll(Boolean includeAll) {
        return (ClientQueryLevel) super.setIncludeAll(includeAll);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientQueryLevel)) return false;
        if (!super.equals(o)) return false;

        ClientQueryLevel that = (ClientQueryLevel) o;

        if (getDimension() != null ? !getDimension().equals(that.getDimension()) : that.getDimension() != null)
            return false;
        return getHierarchyName() != null ? getHierarchyName().equals(that.getHierarchyName()) : that.getHierarchyName() == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getDimension() != null ? getDimension().hashCode() : 0);
        result = 31 * result + (getHierarchyName() != null ? getHierarchyName().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ClientQueryLevel{");
        sb.append("id='").append(getId()).append('\'');
        sb.append(", fieldName='").append(getFieldName()).append('\'');
        sb.append(", dimension='").append(dimension).append('\'');
        sb.append(", hierarchyName='").append(hierarchyName).append('\'');
        sb.append(", categorizer='").append(getCategorizer()).append('\'');
        sb.append(", includeAll='").append(getIncludeAll()).append('\'');
        sb.append('}');
        return sb.toString();
    }


    public static class ClientLevelAggregationsRef extends ClientQueryLevel {
        public static final String AGGREGATIONS_ID = "__levelAggregations__";
        public static final String AGGREGATIONS_LEVEL_NAME = "Measures";

        public ClientLevelAggregationsRef() {
            setId(AGGREGATIONS_ID);
            setFieldName(AGGREGATIONS_LEVEL_NAME);
        }

        public ClientLevelAggregationsRef(ClientLevelAggregationsRef other) {
            super(other);
        }

        @XmlTransient
        @Override
        public String getId() {
            return super.getId();
        }

        @XmlTransient
        @Override
        public String getFieldName() {
            return super.getFieldName();
        }

        @Override
        public ClientLevelAggregationsRef deepClone() {
            return new ClientLevelAggregationsRef(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ClientLevelAggregationsRef)) return false;
            return super.equals(o);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ClientLevelAggregationsRef{");
            sb.append("id='").append(getId()).append('\'');
            sb.append(", fieldName='").append(getFieldName()).append('\'');
            sb.append(", dimension='").append(getDimension()).append('\'');
            sb.append(", hierarchyName='").append(getHierarchyName()).append('\'');
            sb.append(", categorizer='").append(getCategorizer()).append('\'');
            sb.append(", includeAll=").append(getIncludeAll()).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }

    public static class ClientAllLevel extends ClientQueryLevel {
        // Default ALL field name in OLAP
        public static final String DEFAULT_ALL_LEVEL_FIELD_NAME = "(All)";

        public ClientAllLevel() {
            setFieldName(DEFAULT_ALL_LEVEL_FIELD_NAME);
        }

        public ClientAllLevel(ClientQueryLevel level) {
            super(level);
        }

        /**
         * By default in OLAP we use (All) as field name
         * @param dimensionName name of the dimension
         */
        public ClientAllLevel(String dimensionName) {
            setDimension(dimensionName);
        }

        @NotNull
        @Override
        public String getDimension() {
            return super.getDimension();
        }

        @Override
        public ClientAllLevel deepClone() {
            return new ClientAllLevel(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ClientAllLevel)) return false;
            return super.equals(o);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ClientAllLevel{");
            sb.append("id='").append(getId()).append('\'');
            sb.append(", fieldName='").append(getFieldName()).append('\'');
            sb.append(", dimension='").append(getDimension()).append('\'');
            sb.append(", hierarchyName='").append(getHierarchyName()).append('\'');
            sb.append(", categorizer='").append(getCategorizer()).append('\'');
            sb.append(", includeAll=").append(getIncludeAll()).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }
}
