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
package com.jaspersoft.jasperserver.dto.adhoc.query;

import com.jaspersoft.jasperserver.dto.adhoc.query.ast.ClientQueryVisitor;
import com.jaspersoft.jasperserver.dto.adhoc.query.from.ClientFrom;
import com.jaspersoft.jasperserver.dto.adhoc.query.group.ClientGroupBy;
import com.jaspersoft.jasperserver.dto.adhoc.query.group.axis.ClientAxis;
import com.jaspersoft.jasperserver.dto.adhoc.query.order.ClientOrder;
import com.jaspersoft.jasperserver.dto.adhoc.query.select.ClientSelect;
import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.validation.Valid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Andriy Godovanets
 * @author Stas Chubar <schubar@tibco.com>
 *
 * @version $Id$
 */
public abstract class ClientQuery implements DeepCloneable, Serializable {
    @Valid
    private ClientSelect select;

    @Valid
    private ClientFrom from;

    @Valid
    private ClientWhere where;

    private Integer limit;

    public ClientQuery() {
    }

    public ClientQuery(ClientQuery query) {
        checkNotNull(query);

        select = copyOf(query.getSelect());
        from = copyOf(query.getFrom());
        where = copyOf(query.getWhere());
        limit = query.getLimit();
    }

    public ClientWhere getWhere() {
        return where;
    }

    public ClientQuery setWhere(ClientWhere where) {
        this.where = where;
        return this;
    }

    public ClientSelect getSelect() {
        return select;
    }

    public ClientQuery setSelect(ClientSelect select) {
        this.select = select;
        return this;
    }

    public ClientFrom getFrom() {
        return from;
    }

    public ClientQuery setFrom(ClientFrom from) {
        this.from = from;
        return this;
    }

    public abstract <T extends ClientGroupBy> T getGroupBy();

    public abstract List<? extends ClientOrder> getOrderBy();

    public Integer getLimit() {
        return limit;
    }

    public ClientQuery setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public List<ClientField> getSelectedFields() {
        List<ClientField> fields = new ArrayList<ClientField>();
        if (select != null) {
            if (select.getFields() != null) {
                for (Object field : select.getFields()) {
                    if (field instanceof ClientIdentifiable && field instanceof ClientField) {
                        fields.add((ClientField) field);
                    }
                }
            }
            if (select.getDistinctFields() != null) {
                for (Object field : select.getDistinctFields()) {
                    if (field instanceof ClientIdentifiable && field instanceof ClientField) {
                        fields.add((ClientField) field);
                    }
                }
            }
        }

        if (getGroupBy() != null) {
            for (ClientAxis axis : ((ClientGroupBy<? extends ClientAxis>) getGroupBy()).getAxes()) {
                if (axis != null) {
                    for (Object field : axis.getItems()) {
                        if (field instanceof ClientIdentifiable && field instanceof ClientField) {
                            fields.add((ClientField) field);
                        }
                    }
                }
            }
        }

        return fields;
    }

    public void accept(ClientQueryVisitor visitor) {
        if (visitor == null) {
            return;
        }

        if (this.select != null) {
            this.select.accept(visitor);
        }
        if (this.where != null) {
            this.where.accept(visitor);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientQuery query = (ClientQuery) o;

        if (select != null ? !select.equals(query.select) : query.select != null) return false;
        if (from != null ? !from.equals(query.from) : query.from != null) return false;
        if (where != null ? !where.equals(query.where) : query.where != null) return false;
        if (getGroupBy() != null ? !getGroupBy().equals(query.getGroupBy()) : query.getGroupBy() != null) return false;
        if (getOrderBy() != null ? !getOrderBy().equals(query.getOrderBy()) : query.getOrderBy() != null) return false;
        return limit != null ? limit.equals(query.limit) : query.limit == null;

    }

    @Override
    public int hashCode() {
        int result = select != null ? select.hashCode() : 0;
        result = 31 * result + (from != null ? from.hashCode() : 0);
        result = 31 * result + (where != null ? where.hashCode() : 0);
        result = 31 * result + (getGroupBy() != null ? getGroupBy().hashCode() : 0);
        result = 31 * result + (getOrderBy() != null ? getOrderBy().hashCode() : 0);
        result = 31 * result + (limit != null ? limit.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientQuery{" +
                "select=" + select +
                ", from=" + from +
                ", where=" + where +
                ", groupBy=" + getGroupBy() +
                ", orderBy=" + getOrderBy() +
                ", limit=" + limit +
                '}';
    }
}
