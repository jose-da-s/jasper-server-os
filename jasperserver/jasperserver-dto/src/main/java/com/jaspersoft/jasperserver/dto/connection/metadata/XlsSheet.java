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
@XmlRootElement(name = "sheet")
public class XlsSheet implements DeepCloneable<XlsSheet> {
    private String name;
    private List<String> columns;

    public XlsSheet() {
    }

    public XlsSheet(XlsSheet source) {
        checkNotNull(source);

        name = source.getName();
        columns = copyOf(source.getColumns());
    }

    public String getName() {
        return name;
    }

    public XlsSheet setName(String name) {
        this.name = name;
        return this;
    }

    public List<String> getColumns() {
        return columns;
    }

    public XlsSheet setColumns(List<String> columns) {
        this.columns = columns;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XlsSheet xlsSheet = (XlsSheet) o;

        if (columns != null ? !columns.equals(xlsSheet.columns) : xlsSheet.columns != null) return false;
        if (name != null ? !name.equals(xlsSheet.name) : xlsSheet.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (columns != null ? columns.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "XlsSheet{" +
                "name='" + name + '\'' +
                ", columns=" + columns +
                "} " + super.toString();
    }

    /*
     * DeepCloneable
     */

    @Override
    public XlsSheet deepClone() {
        return new XlsSheet(this);
    }
}
