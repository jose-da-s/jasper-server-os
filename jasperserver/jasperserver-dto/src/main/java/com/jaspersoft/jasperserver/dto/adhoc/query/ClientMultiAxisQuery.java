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
package com.jaspersoft.jasperserver.dto.adhoc.query;

import com.jaspersoft.jasperserver.dto.adhoc.query.group.ClientMultiAxisGroupBy;
import com.jaspersoft.jasperserver.dto.adhoc.query.order.ClientGenericOrder;
import com.jaspersoft.jasperserver.dto.adhoc.query.order.ClientOrder;
import com.jaspersoft.jasperserver.dto.adhoc.query.order.ClientPathOrder;
import com.jaspersoft.jasperserver.dto.adhoc.query.order.ClientTopOrBottomNOrder;
import com.jaspersoft.jasperserver.dto.adhoc.query.select.ClientSelect;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Andriy Godovanets
 * @author Stas Chubar <schubar@tibco.com>
 *
 * @version $Id$
 */
@XmlRootElement(name = "multiAxisQuery")
public class ClientMultiAxisQuery extends ClientQuery {
    @Valid
    @NotNull
    private ClientMultiAxisGroupBy groupBy;

    @Valid
    private List<ClientOrder> orderBy;


    public ClientMultiAxisQuery() {
        // no op
    }

    public ClientMultiAxisQuery(ClientMultiAxisQuery query) {
        super(query);
        groupBy = copyOf(query.getGroupBy());
        orderBy = (List<ClientOrder>) copyOf(query.getOrderBy());
    }

    @Override
    public ClientSelect getSelect() {
        return super.getSelect();
    }

    public ClientMultiAxisQuery setSelect(ClientSelect select) {
        super.setSelect(select);
        return this;
    }

    @XmlElement(name = "groupBy", type=ClientMultiAxisGroupBy.class)
    public ClientMultiAxisGroupBy getGroupBy() {
        return groupBy;
    }

    public ClientMultiAxisQuery setGroupBy(ClientMultiAxisGroupBy groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    @XmlElementWrapper(name = "orderBy")
    @XmlElements({
            @XmlElement(name = "member",
                    type = ClientPathOrder.class),
            @XmlElement(name = "level",
                    type = ClientGenericOrder.class),
            @XmlElement(name = "topN",
                    type = ClientTopOrBottomNOrder.ClientTopNOrder.class),
            @XmlElement(name = "bottomN",
                    type = ClientTopOrBottomNOrder.ClientBottomNOrder.class)})
    @Override
    public List<? extends ClientOrder> getOrderBy() {
        return orderBy;
    }

    public ClientMultiAxisQuery setOrderBy(List<? extends ClientOrder> orderBy) {
        this.orderBy = (List<ClientOrder>) orderBy;
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

        ClientMultiAxisQuery that = (ClientMultiAxisQuery) o;

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
        return "ClientMultiAxisQuery{" +
                "groupBy=" + groupBy +
                ", orderBy=" + orderBy +
                "} " + super.toString();
    }

    @Override
    public ClientMultiAxisQuery deepClone() {
        return new ClientMultiAxisQuery(this);
    }
}
