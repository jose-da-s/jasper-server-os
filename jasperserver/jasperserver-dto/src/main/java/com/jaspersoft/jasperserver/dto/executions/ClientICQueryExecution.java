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

import com.jaspersoft.jasperserver.dto.adhoc.query.ClientMultiLevelQuery;
import com.jaspersoft.jasperserver.dto.executions.validation.CheckInMemoryDataSourceType;
import com.jaspersoft.jasperserver.dto.resources.ClientReference;
import com.jaspersoft.jasperserver.dto.resources.ClientReferenceable;

import jakarta.validation.Valid;
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
public class ClientICQueryExecution extends AbstractClientExecution<ClientICQueryExecution>{
    @NotNull
    @CheckInMemoryDataSourceType
    private ClientReferenceable query;

    private String selectedField;
    private String[] visibleFields;
    private boolean format;

    public ClientICQueryExecution() {
    }

    public ClientICQueryExecution(ClientICQueryExecution source) {
        super(source);
        query = copyOf(source.getQuery());
        selectedField = source.getSelectedField();
        visibleFields = Arrays.copyOf(source.getVisibleFields(), source.getVisibleFields().length);
        format = this.isFormat();
    }

    public ClientICQueryExecution(ClientReferenceable query, ClientReferenceable dataSource) {
        setQuery(query);
        setDataSource(dataSource);
    }

    public ClientICQueryExecution(String queryUri, String dataSourceUri) {
        this( new ClientReference(queryUri), new ClientReference(dataSourceUri));
    }

    @Override
    public ClientQueryParams getParams() {
        return super.getParams();
    }

    public String getSelectedField() {
        return selectedField;
    }

    public ClientICQueryExecution setSelectedField(String selectedField) {
        this.selectedField = selectedField;
        return this;
    }

    public String[] getVisibleFields() {
        return visibleFields;
    }

    public ClientICQueryExecution setVisibleFields(String[] visibleFields) {
        this.visibleFields = visibleFields;
        return this;
    }

    public boolean isFormat() {
        return format;
    }

    public ClientICQueryExecution setFormat(boolean format) {
        this.format = format;
        return this;
    }

    @XmlElement(type = ClientReference.class, name = REFERENCE_CLIENT_TYPE)
    public ClientReferenceable getQuery() {
        return query;
    }

    public ClientICQueryExecution setQuery(ClientReferenceable query) {
        this.query = query;
        return this;
    }

    @Override
    public ClientICQueryExecution deepClone() {
        return new ClientICQueryExecution(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientICQueryExecution)) return false;
        if (!super.equals(o)) return false;
        ClientICQueryExecution that = (ClientICQueryExecution) o;
        return format == that.format && Objects.equals(query, that.query) && Objects.equals(selectedField, that.selectedField) && Arrays.equals(visibleFields, that.visibleFields);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), query, selectedField, format);
        result = 31 * result + Arrays.hashCode(visibleFields);
        return result;
    }

    @Override
    public String toString() {
        return "ClientICQueryExecution{" +
                "query=" + query +
                ", selectedField='" + selectedField + '\'' +
                ", visibleFields='" + visibleFields + '\'' +
                ", format='" + format + '\'' +

                "} " + super.toString();
    }
}
