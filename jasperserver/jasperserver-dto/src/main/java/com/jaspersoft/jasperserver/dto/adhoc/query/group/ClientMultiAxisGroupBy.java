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
package com.jaspersoft.jasperserver.dto.adhoc.query.group;

import com.jaspersoft.jasperserver.dto.adhoc.query.group.axis.ClientGroupAxisEnum;
import com.jaspersoft.jasperserver.dto.adhoc.query.group.axis.ClientLevelAxis;

import jakarta.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;
import static java.util.Arrays.asList;

/**
 * @author Andriy Godovanets
 */
@XmlRootElement
public class ClientMultiAxisGroupBy implements ClientGroupBy<ClientLevelAxis>, Serializable {

    @Valid
    protected ClientLevelAxis rows;

    @Valid
    protected ClientLevelAxis columns;

    public ClientMultiAxisGroupBy() {
    }

    public ClientMultiAxisGroupBy(ClientMultiAxisGroupBy source) {
        checkNotNull(source);

        rows = copyOf(source.getRows());
        columns = copyOf(source.getColumns());
    }

    @Override
    public ClientMultiAxisGroupBy deepClone() {
        return new ClientMultiAxisGroupBy(this);
    }

    @Override
    public ClientLevelAxis getAxis(ClientGroupAxisEnum axis) {
        return get(axis.toString());
    }

    @Override
    public ClientLevelAxis getAxis(int index) {
        switch (index) {
            case 0: return columns;
            case 1: return rows;
            default: throw new IllegalArgumentException("Not supported axis index: " + index);
        }
    }

    public ClientLevelAxis get(Object key) {
        if (key.equals(ClientGroupAxisEnum.ROWS.toString())) {
            return this.rows;
        } else if (key.equals(ClientGroupAxisEnum.COLUMNS.toString())) {
            return this.columns;
        }

        return null;
    }

    @Override
    @XmlTransient
    public List<ClientLevelAxis> getAxes() {
        return asList(rows, columns);
    }

    public void addAxis(ClientGroupAxisEnum axis, ClientLevelAxis clientLevelAxis) {
        if (axis.equals(ClientGroupAxisEnum.ROWS)) {
            this.rows = clientLevelAxis;
        } else if (axis.equals(ClientGroupAxisEnum.COLUMNS)) {
            this.columns = clientLevelAxis;
        } else {
            throw new IllegalArgumentException(String.format("unsupported axis %s for MultiAxisGroupBy", axis.toString()));
        }
    }

    @XmlElement(name = "rows")
    public ClientLevelAxis getRows() {
        return rows;
    }

    public ClientMultiAxisGroupBy setRows(ClientLevelAxis rows) {
        this.rows = rows;
        return this;
    }

    @XmlElement(name = "columns")
    public ClientLevelAxis getColumns() {
        return columns;
    }

    public ClientMultiAxisGroupBy setColumns(ClientLevelAxis columns) {
        this.columns = columns;
        return this;
    }

    /**
     * FIXME (Jade 2 - Andriy, Stas, Grant) Need to be refactored
     */
    public Set<String> keySet() {
        Set<String> keys = new HashSet<String>();
        if (this.rows != null) {
            keys.add(ClientGroupAxisEnum.ROWS.toString());
        }
        if (this.columns != null) {
            keys.add(ClientGroupAxisEnum.COLUMNS.toString());
        }
        return keys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientMultiAxisGroupBy that = (ClientMultiAxisGroupBy) o;

        if (rows != null ? !rows.equals(that.rows) : that.rows != null) return false;
        return columns != null ? columns.equals(that.columns) : that.columns == null;
    }

    @Override
    public int hashCode() {
        int result = rows != null ? rows.hashCode() : 0;
        result = 31 * result + (columns != null ? columns.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientMultiAxisGroupBy{" +
                "rows=" + rows +
                ", columns=" + columns +
                '}';
    }
}
