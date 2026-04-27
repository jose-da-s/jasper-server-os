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
package com.jaspersoft.jasperserver.dto.query;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Paul Lysak
 * Date: 11.02.13
 * Time: 17:35
 */
@XmlRootElement(name = "queryResult")
@XmlType(propOrder = {"names", "rows"})
public class QueryResult implements DeepCloneable<QueryResult> {

    private List<String> names = new ArrayList<String>();
    private List<QueryResultRow> rows = new ArrayList<QueryResultRow>();

    public QueryResult() {
    }

    public QueryResult(List<String> names, QueryResultRow... rows) {
        this.names = names;
        this.rows.addAll(Arrays.asList(rows));
    }

    public QueryResult(QueryResult other) {
        checkNotNull(other);

        this.names = copyOf(other.getNames());
        this.rows = copyOf(other.getRows());
    }

    @Override
    public QueryResult deepClone() {
        return new QueryResult(this);
    }

    @XmlElement(name = "name")
    @XmlElementWrapper(name = "names")
    public List<String> getNames() {
        return names;
    }

    public QueryResult setNames(List<String> names) {
        this.names = names == null ? new ArrayList<String>() : names;
        return this;
    }

    @XmlElement(name = "row")
    @XmlElementWrapper(name = "values")
    public List<QueryResultRow> getRows() {
        return rows;
    }

    public QueryResult setRows(List<QueryResultRow> rows) {
        this.rows = rows == null ? new ArrayList<QueryResultRow>() : rows;
        return this;
    }

    @Override
    public String toString() {
        return "QueryResult{" +
                "names=" + names +
                ", rows=" + rows +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueryResult that = (QueryResult) o;

        if (!names.equals(that.names)) return false;
        return rows.equals(that.rows);
    }

    @Override
    public int hashCode() {
        int result = names.hashCode();
        result = 31 * result + rows.hashCode();
        return result;
    }
}
