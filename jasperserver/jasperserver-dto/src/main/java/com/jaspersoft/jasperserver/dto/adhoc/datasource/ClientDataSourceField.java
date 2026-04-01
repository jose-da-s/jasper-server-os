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

package com.jaspersoft.jasperserver.dto.adhoc.datasource;

import com.jaspersoft.jasperserver.dto.adhoc.query.ClientAggregate;
import com.jaspersoft.jasperserver.dto.adhoc.query.ClientField;
import com.jaspersoft.jasperserver.dto.adhoc.query.field.ClientFormattable;
import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

import java.io.Serializable;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;

/**
 * @author Andriy Godovanets
 */
public class ClientDataSourceField implements ClientField, ClientAggregate, ClientFormattable, DeepCloneable<ClientDataSourceField>, Serializable {
    @NotNull
    private String name;
    private String type;
    private String format;

    private String hierarchyName;

    private String fieldExpression;
    private String aggregateFunction;
    private String aggregateExpression;

    private String aggregateFirstLevelFunction;

    private String aggregateArg;
    private String aggregateType;

    public ClientDataSourceField() {
        // No op
    }

    public ClientDataSourceField(ClientDataSourceField field) {
        checkNotNull(field);

        this
                .setName(field.getName())
                .setType(field.getType())
                .setFormat(field.getFormat())
                .setHierarchyName(field.getHierarchyName())
                .setAggregateFunction(field.getAggregateFunction())
                .setAggregateFirstLevelFunction(field.getAggregateFirstLevelFunction())
                .setAggregateExpression(field.getAggregateExpression())
                .setFieldExpression(field.getFieldExpression())
                .setAggregateArg(field.getAggregateArg())
                .setAggregateType(field.getAggregateType());
    }

    @Override
    public String getName() {
        return name;
    }

    public ClientDataSourceField setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getType() {
        return type;
    }

    public ClientDataSourceField setType(String type) {
        this.type = type;
        return this;
    }

    public String getFormat() {
        return format;
    }

    public ClientDataSourceField setFormat(String format) {
        this.format = format;
        return this;
    }

    @XmlTransient
    public boolean isMeasure() {
        return false;
    }


    public String getHierarchyName() {
        return hierarchyName;
    }

    public ClientDataSourceField setHierarchyName(String hierarchyName) {
        this.hierarchyName = hierarchyName;
        return this;
    }

    @Override
    public String getAggregateFunction() {
        return aggregateFunction;
    }

    @Override
    public String getAggregateFirstLevelFunction() {
        return aggregateFirstLevelFunction;
    }


    public ClientDataSourceField setAggregateFirstLevelFunction(String aggregateFirstLevelFunction) {
        this.aggregateFirstLevelFunction = aggregateFirstLevelFunction;
        return this;
    }

    public ClientDataSourceField setAggregateFunction(String aggregateFunction) {
        this.aggregateFunction = aggregateFunction;
        return this;
    }

    @Override
    public String getAggregateExpression() {
        return aggregateExpression;
    }

    public ClientDataSourceField setAggregateExpression(String aggregateExpression) {
        this.aggregateExpression = aggregateExpression;
        return this;
    }

    @Override
    public String getFieldExpression() {
        return fieldExpression;
    }


    public ClientDataSourceField setFieldExpression(String fieldExpression) {
        this.fieldExpression = fieldExpression;
        return this;
    }

    @Override
    public String getAggregateArg() {
        return aggregateArg;
    }

    public ClientDataSourceField setAggregateArg(String aggregateArg) {
        this.aggregateArg = aggregateArg;
        return this;
    }

    @Override
    public String getAggregateType() {
        return aggregateType;
    }

    public ClientDataSourceField setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientDataSourceField that = (ClientDataSourceField) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (format != null ? !format.equals(that.format) : that.format != null) return false;
        if (hierarchyName != null ? !hierarchyName.equals(that.hierarchyName) : that.hierarchyName != null)
            return false;
        if (aggregateFunction != null ? !aggregateFunction.equals(that.aggregateFunction) : that.aggregateFunction != null)
            return false;
        if (aggregateExpression != null ? !aggregateExpression.equals(that.aggregateExpression) : that.aggregateExpression != null)
            return false;
        if (fieldExpression != null ? !fieldExpression.equals(that.fieldExpression) : that.fieldExpression != null)
            return false;
        if (aggregateFirstLevelFunction != null ? !aggregateFirstLevelFunction.equals(that.aggregateFirstLevelFunction) : that.aggregateFirstLevelFunction != null)
            return false;
        if (aggregateArg != null ? !aggregateArg.equals(that.aggregateArg) : that.aggregateArg != null) return false;
        return aggregateType != null ? aggregateType.equals(that.aggregateType) : that.aggregateType == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (format != null ? format.hashCode() : 0);
        result = 31 * result + (hierarchyName != null ? hierarchyName.hashCode() : 0);
        result = 31 * result + (aggregateFunction != null ? aggregateFunction.hashCode() : 0);
        result = 31 * result + (aggregateFirstLevelFunction != null ? aggregateFirstLevelFunction.hashCode() : 0);
        result = 31 * result + (aggregateArg != null ? aggregateArg.hashCode() : 0);
        result = 31 * result + (aggregateType != null ? aggregateType.hashCode() : 0);
        result = 31 * result + (fieldExpression != null ? fieldExpression.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ClientDataSourceField{");
        sb.append("type='").append(type).append('\'');
        sb.append(", hierarchyName='").append(hierarchyName).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public ClientDataSourceField deepClone() {
        return new ClientDataSourceField(this);
    }
}
