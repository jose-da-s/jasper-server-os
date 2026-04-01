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

import com.jaspersoft.jasperserver.dto.executions.validation.CheckInMemoryDataSourceType;
import com.jaspersoft.jasperserver.dto.resources.ClientReference;
import com.jaspersoft.jasperserver.dto.resources.ClientReferenceable;

import jakarta.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Arrays;
import java.util.Objects;

import static com.jaspersoft.jasperserver.dto.resources.ResourceMediaType.REFERENCE_CLIENT_TYPE;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * For internal use. Subject to refinement.
 * @author schubar
 * @version $Id$
 * @since 15.02.2016
 */
@XmlRootElement(name = "queryExecution")
@XmlType(propOrder = {"query"})
public class ClientDomainQueryExecution extends AbstractClientExecution<ClientDomainQueryExecution>{
    @NotNull
    private String query;

    public ClientDomainQueryExecution() {
    }

    public ClientDomainQueryExecution(ClientDomainQueryExecution source) {
        super(source);
        query = source.getQuery();
    }

    public ClientDomainQueryExecution(String query, ClientReferenceable dataSource) {
        setQuery(query);
        setDataSource(dataSource);
    }

    public ClientDomainQueryExecution(String query, String dataSourceUri) {
        this(query, new ClientReference(dataSourceUri));
    }

    @Override
    public ClientQueryParams getParams() {
        return super.getParams();
    }

    public String getQuery() {
        return query;
    }

    public ClientDomainQueryExecution setQuery(String query) {
        this.query = query;
        return this;
    }

    @Override
    public ClientDomainQueryExecution deepClone() {
        return new ClientDomainQueryExecution(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientDomainQueryExecution)) return false;
        if (!super.equals(o)) return false;
        ClientDomainQueryExecution that = (ClientDomainQueryExecution) o;
        return Objects.equals(query, that.query);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), query);
    }

    @Override
    public String toString() {
        return "ClientICQueryExecution{" +
                "query='" + query +
                "} " + super.toString();
    }
}
