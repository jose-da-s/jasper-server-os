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
import com.jaspersoft.jasperserver.dto.common.TimeString;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Paul Lysak
 * Date: 11.02.13
 * Time: 17:37
 */
@XmlSeeAlso({TimeString.class})
public class QueryResultRow implements DeepCloneable<QueryResultRow> {
    private List<Object> values = new ArrayList<Object>();

    public QueryResultRow() {

    }

    public QueryResultRow(Object... values) {
        this.values.addAll(Arrays.asList(values));
    }

    public QueryResultRow(QueryResultRow other) {
        checkNotNull(other);

        this.values = copyOf(other.getValues());
    }

    @Override
    public QueryResultRow deepClone() {
        return new QueryResultRow(this);
    }

    @XmlElement(name = "value")
    public List<Object> getValues() {
        return values;
    }

    public QueryResultRow setValues(List<Object> values) {
        this.values = values == null ? new ArrayList<Object>() : values;
        return this;
    }

    @Override
    public String toString() {
        return "QueryResultRow{" +
                "values=" + values +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueryResultRow that = (QueryResultRow) o;

        return values.equals(that.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }
}
