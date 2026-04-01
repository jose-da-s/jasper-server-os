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

package com.jaspersoft.jasperserver.dto.executions;

import com.jaspersoft.jasperserver.dto.adhoc.query.ClientMultiAxisQuery;
import com.jaspersoft.jasperserver.dto.resources.ClientReference;
import com.jaspersoft.jasperserver.dto.resources.ClientReferenceable;

import jakarta.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Vasyl Spachynskyi
 * @version $Id$
 * @since 15.02.2016
 */
@XmlRootElement(name = "queryExecution")
@XmlType(propOrder = {"query"})
public class ClientMultiAxisQueryExecution extends ClientQueryExecution<ClientMultiAxisQuery,
        ClientMultiAxisQueryExecution> {
    @Valid
    private ClientMultiAxisQuery query;

    public ClientMultiAxisQueryExecution() {
    }

    public ClientMultiAxisQueryExecution(ClientMultiAxisQueryExecution source) {
        super(source);
        query = copyOf(source.getQuery());
    }

    public ClientMultiAxisQueryExecution(ClientMultiAxisQuery clientQuery, ClientReferenceable dataSource) {
        setQuery(clientQuery);
        setDataSource(dataSource);
    }

    public ClientMultiAxisQueryExecution(ClientMultiAxisQuery clientQuery, String dataSourceUri) {
        this(clientQuery, new ClientReference(dataSourceUri));
    }

    @XmlElement
    @Override
    public ClientMultiAxisQuery getQuery() {
        return query;
    }

    @Override
    public ClientMultiAxisQueryExecution setQuery(ClientMultiAxisQuery query) {
        this.query = query;
        return this;
    }

    @Override
    public ClientMultiAxisQueryExecution deepClone() {
        return new ClientMultiAxisQueryExecution(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ClientMultiAxisQueryExecution that = (ClientMultiAxisQueryExecution) o;

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
        return "ClientMultiAxisQueryExecution{" +
                "query=" + query +
                "} " + super.toString();
    }
}
