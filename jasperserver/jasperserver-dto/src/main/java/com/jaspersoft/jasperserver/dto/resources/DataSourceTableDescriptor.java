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

package com.jaspersoft.jasperserver.dto.resources;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author hgokulak
 * @version $Id$
 */
@XmlRootElement(name = "dataSourceTableDescriptor")
public class DataSourceTableDescriptor implements DeepCloneable<DataSourceTableDescriptor> {
    private String datasourceTableName;
    private String schemaName;

    public DataSourceTableDescriptor() {
    }
    public DataSourceTableDescriptor(DataSourceTableDescriptor source){
        checkNotNull(source);

        schemaName = copyOf(source.getSchemaName());
        datasourceTableName = copyOf(source.getDatasourceTableName());
    }

    @XmlElement(name = "schemaName")
    public String getSchemaName() {
        return schemaName;
    }

    public DataSourceTableDescriptor setSchemaName(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    @XmlElement(name = "datasourceTableName")
    public String getDatasourceTableName() {
        return datasourceTableName;
    }

    public DataSourceTableDescriptor setDatasourceTableName(String datasourceTableName) {
        this.datasourceTableName = datasourceTableName;
        return this;
    }

    @Override
    public DataSourceTableDescriptor deepClone() {
        return new DataSourceTableDescriptor(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataSourceTableDescriptor that = (DataSourceTableDescriptor) o;
        return Objects.equals(schemaName, that.schemaName) &&
                Objects.equals(datasourceTableName, that.datasourceTableName);

    }

    @Override
    public int hashCode() {
        int result = schemaName != null ? schemaName.hashCode() : 0;
        result = 31 * result + (datasourceTableName != null ? datasourceTableName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DataSourceTableDescriptor{" +
                "schemaName=" + schemaName +
                ", datasourceTableName=" + datasourceTableName +
                '}';
    }
}
