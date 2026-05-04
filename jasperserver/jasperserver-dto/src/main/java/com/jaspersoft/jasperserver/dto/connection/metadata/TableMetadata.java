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
package com.jaspersoft.jasperserver.dto.connection.metadata;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@XmlRootElement(name="table")
public class TableMetadata implements DeepCloneable<TableMetadata> {
    private List<ColumnMetadata> columns;
    private String queryLanguage;

    public TableMetadata() {
    }

    public TableMetadata(TableMetadata source) {
        checkNotNull(source);

        columns = copyOf(source.getColumns());
        queryLanguage = source.getQueryLanguage();
    }

    public List<ColumnMetadata> getColumns() {
        return columns;
    }

    public TableMetadata setColumns(List<ColumnMetadata> columns) {
        this.columns = columns;
        return this;
    }

    public String getQueryLanguage() {
        return queryLanguage;
    }

    public TableMetadata setQueryLanguage(String queryLanguage) {
        this.queryLanguage = queryLanguage;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TableMetadata that = (TableMetadata) o;

        if (columns != null ? !columns.equals(that.columns) : that.columns != null) return false;
        if (queryLanguage != null ? !queryLanguage.equals(that.queryLanguage) : that.queryLanguage != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = columns != null ? columns.hashCode() : 0;
        result = 31 * result + (queryLanguage != null ? queryLanguage.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TableMetadata{" +
                "columns=" + columns +
                ", queryLanguage='" + queryLanguage + '\'' +
                '}';
    }

    /*
     * DeepCloneable
     */

    @Override
    public TableMetadata deepClone() {
        return new TableMetadata(this);
    }
}
