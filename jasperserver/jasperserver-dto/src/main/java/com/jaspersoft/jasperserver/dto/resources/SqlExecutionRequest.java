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

package com.jaspersoft.jasperserver.dto.resources;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author serhii.blazhyievskyi
 * @version $Id$
 */
@XmlRootElement(name = "sqlExecutionRequest")
public class SqlExecutionRequest extends ClientResource<SqlExecutionRequest> implements ClientReferenceableDataSource {

    @NotNull
    private String sql;

    @NotNull
    private String dataSourceUri;

    public SqlExecutionRequest() {
    }

    public SqlExecutionRequest(SqlExecutionRequest other) {
        super(other);
        this.sql = other.getSql();
        this.dataSourceUri = other.getDataSourceUri();
    }

    @Override
    public SqlExecutionRequest deepClone() {
        return new SqlExecutionRequest(this);
    }

    public String getDataSourceUri() {
        return dataSourceUri;
    }

    public SqlExecutionRequest setDataSourceUri(String dataSourceUri) {
        this.dataSourceUri = dataSourceUri;
        return this;
    }

    public String getSql() {
        return sql;
    }

    public SqlExecutionRequest setSql(String sql) {
        this.sql = sql;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SqlExecutionRequest)) return false;
        if (!super.equals(o)) return false;

        SqlExecutionRequest that = (SqlExecutionRequest) o;

        if (getSql() != null ? !getSql().equals(that.getSql()) : that.getSql() != null) return false;
        return getDataSourceUri() != null ? getDataSourceUri().equals(that.getDataSourceUri()) : that.getDataSourceUri() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getSql() != null ? getSql().hashCode() : 0);
        result = 31 * result + (getDataSourceUri() != null ? getDataSourceUri().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SqlExecutionRequest{" +
                "sql='" + sql + '\'' +
                ", dataSourceUri='" + dataSourceUri + '\'' +
                '}';
    }
}
