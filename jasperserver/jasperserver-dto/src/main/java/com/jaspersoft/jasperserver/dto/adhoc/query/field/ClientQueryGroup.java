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
import com.jaspersoft.jasperserver.dto.adhoc.query.ClientField;
import com.jaspersoft.jasperserver.dto.adhoc.query.ClientIdentifiable;
import com.jaspersoft.jasperserver.dto.adhoc.query.IExpressionContainer;
import com.jaspersoft.jasperserver.dto.adhoc.query.QueryPatternsUtil;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientExpressionContainer;
import com.jaspersoft.jasperserver.dto.adhoc.query.validation.NotEmpty;
import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;

import java.io.Serializable;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Andriy Godovanets
 */
public class ClientQueryGroup implements ClientField, ClientIdentifiable<String>, IExpressionContainer, DeepCloneable<ClientQueryGroup>, Serializable {

    private String id;
    // TODO Andriy G: remove field type. It doesn't affect the query resultset
    private String type;
    private String categorizer;
    private String fieldName;
    private Boolean includeAll;
    private ClientExpressionContainer expressionContainer;

    public ClientQueryGroup() {
        // no op
    }

    public ClientQueryGroup(String group) {
        QueryPatternsUtil.NameAliasExpression nameAliasExpression = QueryPatternsUtil.parseNameAliasExpression(group);
        this.fieldName = nameAliasExpression.name;
        this.id = nameAliasExpression.alias;
        if (nameAliasExpression.expression != null)
            this.expressionContainer = new ClientExpressionContainer(nameAliasExpression.expression);
    }

    public ClientQueryGroup(ClientQueryGroup source) {
        checkNotNull(source);

        id = source.getId();
        type = source.getType();
        categorizer = source.getCategorizer();
        fieldName = source.getFieldName();
        includeAll = source.getIncludeAll();
        expressionContainer = copyOf(source.getExpressionContainer());
    }

    @Override
    public ClientQueryGroup deepClone() {
        return new ClientQueryGroup(this);
    }

    /**
     * @return Name of the bucketing expression that determines which data rows will be grouped together
     */
    public String getCategorizer() {
        return categorizer;
    }

    public ClientQueryGroup setCategorizer(String categorizer) {
        this.categorizer = categorizer;
        return this;
    }

    /**
     * @return Query group identifier (similar to SQL alias) for further reference within query: order, expansions
     */
    @Override
    public String getId() {
        return id;
    }

    public ClientQueryGroup setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @return Name of the field to group by
     */
    @NotEmpty
    @XmlElement(name = "field")
    public String getFieldName() {
        return fieldName;
    }

    public ClientQueryGroup setFieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    /**
     * This name is calculated query group name: if id is define, that this is id, else it's fieldName.
     * This name is related to Field Reference
     *
     * @return Client field name
     */
    @Override
    @XmlTransient
    public String getName() {
        return getFieldName();
    }

    @Override
    @XmlTransient
    public String getType() {
        return type;
    }

    @Override
    @XmlTransient
    public String getFieldExpression() {
        if (expressionContainer == null) return null;
        return expressionContainer.getString();
    }

    @Override
    @XmlElement(name = "expression")
    public ClientExpressionContainer getExpressionContainer() {
        return expressionContainer;
    }

    @Override
    public ClientQueryGroup setExpressionContainer(ClientExpressionContainer expressionContainer) {
        this.expressionContainer = expressionContainer;
        return this;
    }


    /**
     * @return Control over inclusion extra rows to the query result that represent rolled up aggregations
     */
    public Boolean getIncludeAll() {
        return includeAll;
    }

    public ClientQueryGroup setIncludeAll(Boolean includeAll) {
        this.includeAll = includeAll;
        return this;
    }

    public ClientQueryGroup setDataSourceField(ClientDataSourceField field) {
        if (field == null) {
            fieldName = null;
            type = null;
        } else {
            fieldName = field.getName();
            type = field.getType();
        }

        return this;
    }

    public static class ClientAllGroup extends ClientQueryGroup {
        public static final String ALL_GROUP_ID = "__allGroup__";

        public ClientAllGroup() {
            setId(ALL_GROUP_ID);
            setFieldName(ALL_GROUP_ID);
        }

        public ClientAllGroup(ClientAllGroup other) {
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
        public ClientAllGroup deepClone() {
            return new ClientAllGroup(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ClientAllGroup)) return false;
            return super.equals(o);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ClientAllGroup{");
            sb.append("categorizer='").append(getCategorizer()).append('\'');
            sb.append(", id='").append(getId()).append('\'');
            sb.append(", type='").append(getType()).append('\'');
            sb.append(", fieldName='").append(getFieldName()).append('\'');
            sb.append(", expression=").append(getExpressionContainer());
            sb.append(", includeAll=").append(getIncludeAll());
            sb.append('}');
            return sb.toString();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientQueryGroup)) return false;

        ClientQueryGroup that = (ClientQueryGroup) o;

        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;
        if (getType() != null ? !getType().equals(that.getType()) : that.getType() != null) return false;
        if (getCategorizer() != null ? !getCategorizer().equals(that.getCategorizer()) : that.getCategorizer() != null)
            return false;
        if (getFieldName() != null ? !getFieldName().equals(that.getFieldName()) : that.getFieldName() != null)
            return false;
        if (expressionContainer != null ? !expressionContainer.equals(that.expressionContainer) : that.expressionContainer != null) {
            return false;
        }
        return getIncludeAll() != null ? getIncludeAll().equals(that.getIncludeAll()) : that.getIncludeAll() == null;

    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getType() != null ? getType().hashCode() : 0);
        result = 31 * result + (getCategorizer() != null ? getCategorizer().hashCode() : 0);
        result = 31 * result + (getFieldName() != null ? getFieldName().hashCode() : 0);
        result = 31 * result + (getIncludeAll() != null ? getIncludeAll().hashCode() : 0);
        result = 31 * result + (expressionContainer != null ? expressionContainer.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ClientQueryGroup{");
        sb.append("categorizer='").append(categorizer).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", fieldName='").append(fieldName).append('\'');
        sb.append(", expression=").append(expressionContainer);
        sb.append(", includeAll=").append(includeAll);
        sb.append('}');
        return sb.toString();
    }
}
