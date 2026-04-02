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

package com.jaspersoft.jasperserver.dto.executions;

import com.jaspersoft.jasperserver.dto.adhoc.query.ClientMultiLevelQuery;
import com.jaspersoft.jasperserver.dto.resources.ClientReference;
import com.jaspersoft.jasperserver.dto.resources.ClientReferenceable;

import jakarta.validation.Valid;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Vasyl Spachynskyi
 * @version $Id$
 * @since 15.02.2016
 */
@XmlRootElement(name = "queryExecution")
@XmlType(propOrder = {"query"})
public class ClientMultiLevelQueryExecution extends ClientQueryExecution<ClientMultiLevelQuery,
        ClientMultiLevelQueryExecution> {
    @Valid
    private ClientMultiLevelQuery query;

    public ClientMultiLevelQueryExecution() {
    }

    public ClientMultiLevelQueryExecution(ClientMultiLevelQueryExecution source) {
        super(source);
        query = copyOf(source.getQuery());
    }

    public ClientMultiLevelQueryExecution(ClientMultiLevelQuery query, ClientReferenceable dataSource) {
        setQuery(query);
        setDataSource(dataSource);
    }

    public ClientMultiLevelQueryExecution(ClientMultiLevelQuery query, String dataSourceUri) {
        this(query, new ClientReference(dataSourceUri));
    }

    @Override
    public ClientQueryParams getParams() {
        return super.getParams();
    }

    @XmlElement(type = ClientMultiLevelQuery.class)
    @Override
    public ClientMultiLevelQuery getQuery() {
        return query;
    }

    public ClientMultiLevelQueryExecution setQuery(ClientMultiLevelQuery query) {
        this.query = query;
        return this;
    }

    @Override
    public ClientMultiLevelQueryExecution deepClone() {
        return new ClientMultiLevelQueryExecution(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ClientMultiLevelQueryExecution that = (ClientMultiLevelQueryExecution) o;

        return query != null ? query.equals(that.query) : that.query == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (query != null ? query.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientMultiLevelQueryExecution{" +
                "query=" + query +
                "} " + super.toString();
    }
}
