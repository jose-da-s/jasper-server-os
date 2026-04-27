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
import com.jaspersoft.jasperserver.dto.adhoc.query.ast.ClientQueryExpression;
import com.jaspersoft.jasperserver.dto.adhoc.query.ast.ClientQueryVisitor;
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
public class ClientQueryField implements ClientField, ClientIdentifiable<String>, ClientQueryExpression,
        IExpressionContainer,  DeepCloneable<ClientQueryField>, Serializable {
    private String id;
    private String type;
    private boolean measure;
    @NotEmpty
    private String field;
    private ClientExpressionContainer expressionContainer;

    public ClientQueryField() {
        // no op
    }

    public ClientQueryField(String field) {
        QueryPatternsUtil.NameAliasExpression nameAliasExpression = QueryPatternsUtil.parseNameAliasExpression(field);
        this.field = nameAliasExpression.name;
        this.id = nameAliasExpression.alias;
        if (nameAliasExpression.expression != null) {
            this.expressionContainer = new ClientExpressionContainer(nameAliasExpression.expression);

        }
    }


    public ClientQueryField(ClientQueryField source) {
        checkNotNull(source);
        id = source.getId();
        type = source.getType();
        measure = source.isMeasure();
        field = source.getFieldName();
        expressionContainer = copyOf(source.getExpressionContainer());
    }

    @Override
    public String getId() {
        return id;
    }

    public ClientQueryField setId(String id) {
        this.id = id;
        return this;
    }

    @XmlElement(name = "field")
    public String getFieldName() {
        return field;
    }

    public ClientQueryField setFieldName(String fieldName) {
        this.field = fieldName;
        return this;
    }

    /**
     * This name is calculated query field name: if id is define, that this is id, else it's fieldName.
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

    @XmlTransient
    public boolean isMeasure() {
        return measure;
    }

    public ClientQueryField setDataSourceField(ClientDataSourceField field) {
        if (field != null) {
            type = field.getType();
            measure = field.isMeasure();
            setFieldName(field.getName());
        } else {
            type = null;
            measure = false;
            this.field = null;
        }
        return this;
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
    public ClientQueryField setExpressionContainer(ClientExpressionContainer expressionContainer) {
        this.expressionContainer = expressionContainer;
        return this;
    }

    @Override
    public void accept(ClientQueryVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientQueryField that = (ClientQueryField) o;

        if (isMeasure() != that.isMeasure()) return false;
        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;
        if (getType() != null ? !getType().equals(that.getType()) : that.getType() != null) return false;
        if (expressionContainer != null ? !expressionContainer.equals(that.expressionContainer) : that.expressionContainer != null) {
            return false;
        }
        return getFieldName() != null ? getFieldName().equals(that.getFieldName()) : that.getFieldName() == null;

    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getType() != null ? getType().hashCode() : 0);
        result = 31 * result + (isMeasure() ? 1 : 0);
        result = 31 * result + (getFieldName() != null ? getFieldName().hashCode() : 0);
        result = 31 * result + (expressionContainer != null ? expressionContainer.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientQueryField{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", measure=" + measure +
                ", field='" + field + '\'' +
                ", expression=" + expressionContainer +
                '}';
    }

    @Override
    public ClientQueryField deepClone() {
        return new ClientQueryField(this);
    }
}
