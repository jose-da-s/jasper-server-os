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

import com.jaspersoft.jasperserver.dto.adhoc.query.field.ClientQueryGroup;
import com.jaspersoft.jasperserver.dto.adhoc.query.group.ClientQueryGroupBy;
import com.jaspersoft.jasperserver.dto.adhoc.query.order.ClientGenericOrder;
import com.jaspersoft.jasperserver.dto.adhoc.query.select.ClientSelect;

import jakarta.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;
import java.util.stream.Collectors;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Andriy Godovanets
 * @author Stas Chubar <schubar@tibco.com>
 *
 * @version $Id$
 */
@XmlRootElement(name = "multiLevelQuery")
public class ClientMultiLevelQuery extends ClientQuery {
    @Valid
    private ClientQueryGroupBy groupBy;

    @Valid
    private List<ClientGenericOrder> orderBy;

    public ClientMultiLevelQuery() {
    }

    public ClientMultiLevelQuery(ClientMultiLevelQuery query) {
        super(query);
        groupBy = copyOf(query.getGroupBy());
        orderBy = copyOf(query.getOrderBy());
    }

    public ClientMultiLevelQuery setSelect(ClientSelect select) {
        super.setSelect(select);
        return this;
    }

    @XmlTransient
    public ClientQueryGroupBy getGroupBy() {
        return groupBy;
    }

    public ClientMultiLevelQuery setGroupBy(ClientQueryGroupBy groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    @XmlElementWrapper(name = "groupBy")
    @XmlElements({
            @XmlElement(name = "group", type = ClientQueryGroup.class),
            @XmlElement(name = "allGroup", type = ClientQueryGroup.ClientAllGroup.class)
    })
    public List<? extends ClientQueryGroup> getGroups() {
        return groupBy != null ? groupBy.getGroups() : null;
    }

    public ClientMultiLevelQuery setGroups(List<? extends ClientQueryGroup> groups) {
        ClientQueryGroupBy clientQueryGroupBy = new ClientQueryGroupBy().setGroups(groups);
        setGroupBy(clientQueryGroupBy);
        return this;
    }


    @XmlElementWrapper(name = "orderBy")
    @XmlElement(name = "field")
    @Override
    public List<ClientGenericOrder> getOrderBy() {
        return orderBy;
    }

    public ClientMultiLevelQuery setOrderBy(List<ClientGenericOrder> orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public ClientMultiLevelQuery setOrderByStr(List<String> orderBy) {
        this.orderBy = orderBy.stream().map(s -> new ClientGenericOrder(s)).collect(Collectors.toList());
        return this;
    }

    @XmlTransient
    public List<ClientField> getSelectedFields() {
        return super.getSelectedFields();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ClientMultiLevelQuery that = (ClientMultiLevelQuery) o;

        if (groupBy != null ? !groupBy.equals(that.groupBy) : that.groupBy != null) return false;
        return orderBy != null ? orderBy.equals(that.orderBy) : that.orderBy == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (groupBy != null ? groupBy.hashCode() : 0);
        result = 31 * result + (orderBy != null ? orderBy.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientMultiLevelQuery{" +
                "groupBy=" + groupBy +
                ", orderBy=" + orderBy +
                "} " + super.toString();
    }

    @Override
    public ClientMultiLevelQuery deepClone() {
        return new ClientMultiLevelQuery(this);
    }
}
