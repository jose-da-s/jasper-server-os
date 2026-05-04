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
package com.jaspersoft.jasperserver.dto.connection;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlRootElement;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@XmlRootElement(name = "hive")
public class HadoopHiveConnection implements DeepCloneable<HadoopHiveConnection> {
    private String jdbcURL;
    public HadoopHiveConnection(){
    }
    public HadoopHiveConnection(HadoopHiveConnection source){
        checkNotNull(source);

        jdbcURL = source.getJdbcURL();
    }

    public String getJdbcURL() {
        return jdbcURL;
    }

    public HadoopHiveConnection setJdbcURL(String jdbcURL) {
        this.jdbcURL = jdbcURL;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HadoopHiveConnection that = (HadoopHiveConnection) o;

        if (jdbcURL != null ? !jdbcURL.equals(that.jdbcURL) : that.jdbcURL != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return jdbcURL != null ? jdbcURL.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "HadoopHiveConnection{" +
                "jdbcURL='" + jdbcURL + '\'' +
                "} " + super.toString();
    }

    /*
     * DeepCloneable
     */

    @Override
    public HadoopHiveConnection deepClone() {
        return new HadoopHiveConnection(this);
    }
}
