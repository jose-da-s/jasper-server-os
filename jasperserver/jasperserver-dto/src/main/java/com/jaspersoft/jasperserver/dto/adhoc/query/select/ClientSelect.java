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
package com.jaspersoft.jasperserver.dto.adhoc.query.select;

import com.jaspersoft.jasperserver.dto.adhoc.query.ast.ClientQueryClause;
import com.jaspersoft.jasperserver.dto.adhoc.query.ast.ClientQueryVisitor;
import com.jaspersoft.jasperserver.dto.adhoc.query.field.ClientQueryAggregatedField;
import com.jaspersoft.jasperserver.dto.adhoc.query.field.ClientQueryField;
import com.jaspersoft.jasperserver.dto.adhoc.query.validation.CheckClientSelect;
import com.jaspersoft.jasperserver.dto.adhoc.query.validation.CheckQueryDetailsNull;
import com.jaspersoft.jasperserver.dto.adhoc.query.validation.groups.MultiAxisQueryValidationGroup;
import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Andriy Godovanets
 * @author Stas Chubar <schubar@tibco.com>
 *
 * @version $Id$
 */

@CheckClientSelect
public class ClientSelect implements ClientQueryClause, DeepCloneable<ClientSelect>, Serializable {
    @Valid
    @CheckQueryDetailsNull(groups = MultiAxisQueryValidationGroup.class)
    private List<ClientQueryField> fields;
    @Valid
    private List<ClientQueryField> distinctFields;

    @Valid
    private List<ClientQueryAggregatedField> aggregations;

    public ClientSelect() {
        // no op
    }

    public ClientSelect(ClientSelect source) {
        checkNotNull(source);

        fields = copyOf(source.getFields());
        distinctFields = copyOf(source.getDistinctFields());
        aggregations = copyOf(source.getAggregations());
    }

    public ClientSelect(List<ClientQueryField> fields) {
        setFields(new ArrayList<ClientQueryField>(fields));
    }

    @Override
    public ClientSelect deepClone() {
        return new ClientSelect(this);
    }

    public ClientQueryField getField(int index) {
        return fields.get(index);
    }

    public ClientQueryField getField(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("Cannot search query field by null id");
        }
        Iterator<ClientQueryField> itr = fields.iterator();
        while (itr.hasNext()) {
            ClientQueryField field = itr.next();
            if (id.equals(field.getId()) || field.getFieldName().equals(id)) {
                return field;
            }
        }
        return null;
    }

    @XmlElementWrapper(name = "fields")
    @XmlElement(name = "field")
    public List<ClientQueryField> getFields() {
        return fields;
    }

    public ClientSelect setFields(List<ClientQueryField> fields) {
        this.fields = (fields != null) ? fields : new ArrayList<ClientQueryField>();
        return this;
    }

    public ClientSelect setFieldsStr(List<String> fields) {
        this.fields = (fields == null) ? new ArrayList<>() :
                fields.stream().map(s -> new ClientQueryField(s)).collect(Collectors.toList());
        return this;
    }

    @XmlElementWrapper(name = "distinctFields")
    @XmlElement(name = "distinctField")
    public List<ClientQueryField> getDistinctFields() {
        return distinctFields;
    }

    public ClientSelect setDistinctFields(List<ClientQueryField> distinctFields) {
        this.distinctFields = distinctFields;
        return this;
    }

    @XmlElementWrapper(name = "aggregations")
    @XmlElement(name = "aggregation")
    public List<ClientQueryAggregatedField> getAggregations() {
        return aggregations;
    }

    public ClientSelect setAggregations(List<ClientQueryAggregatedField> aggregations) {
        this.aggregations = (aggregations != null) ? aggregations : new ArrayList<ClientQueryAggregatedField>();
        return this;
    }

    public ClientSelect setAggregationsStr(List<String> aggregations) {
        this.aggregations = (aggregations == null) ? new ArrayList<>() :
                aggregations.stream().map(s -> new ClientQueryAggregatedField(s)).collect(Collectors.toList());
        return this;
    }

    @Override
    public void accept(ClientQueryVisitor visitor) {
        if (this.fields != null) {
            for (ClientQueryField field: this.fields) {
                field.accept(visitor);
            }
        }

        if (getDistinctFields() != null) {
            for (ClientQueryField field : getDistinctFields()) {
                field.accept(visitor);
            }
        }

        if (this.aggregations != null) {
            for (ClientQueryAggregatedField aggregatedField: this.aggregations) {
                aggregatedField.accept(visitor);
            }
        }
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientSelect that = (ClientSelect) o;

        if (getFields() != null ? !getFields().equals(that.getFields()) : that.getFields() != null) return false;
        if (getDistinctFields() != null ? !getDistinctFields().equals(that.getDistinctFields()) : that.getDistinctFields() != null)
            return false;
        return getAggregations() != null ? getAggregations().equals(that.getAggregations()) : that.getAggregations() == null;
    }

    @Override
    public int hashCode() {
        int result = getFields() != null ? getFields().hashCode() : 0;
        result = 31 * result + (getDistinctFields() != null ? getDistinctFields().hashCode() : 0);
        result = 31 * result + (getAggregations() != null ? getAggregations().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientSelect{" +
                "fields=" + fields +
                ", distinctFields=" + distinctFields +
                ", aggregations=" + aggregations +
                '}';
    }
}
