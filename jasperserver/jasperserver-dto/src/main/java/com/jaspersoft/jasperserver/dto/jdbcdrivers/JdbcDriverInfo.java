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
package com.jaspersoft.jasperserver.dto.jdbcdrivers;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;
import com.jaspersoft.jasperserver.dto.resources.ClientProperty;

import jakarta.xml.bind.annotation.XmlElement;
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
@XmlRootElement
public class JdbcDriverInfo implements DeepCloneable<JdbcDriverInfo>{
    private String name;
    private String label;
    private Boolean available;
    private String jdbcUrl;
    private Boolean isDefault;
    private String jdbcDriverClass;
    private Boolean allowSpacesInDbName;
    private List<ClientProperty> defaultValues;

    public JdbcDriverInfo(){
    }

    public JdbcDriverInfo(JdbcDriverInfo source){
        checkNotNull(source);

        name = source.getName();
        label = source.getLabel();
        available = source.getAvailable();
        jdbcUrl = source.getJdbcUrl();
        isDefault = source.isDefault();
        jdbcDriverClass = source.getJdbcDriverClass();
        allowSpacesInDbName = source.getAllowSpacesInDbName();
        defaultValues = copyOf(source.getDefaultValues());
    }

    @Override
    public JdbcDriverInfo deepClone() {
        return new JdbcDriverInfo(this);
    }

    public Boolean getAllowSpacesInDbName() {
        return allowSpacesInDbName;
    }

    public JdbcDriverInfo setAllowSpacesInDbName(Boolean allowSpacesInDbName) {
        this.allowSpacesInDbName = allowSpacesInDbName;
        return this;
    }

    public String getName() {
        return name;
    }

    public JdbcDriverInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public JdbcDriverInfo setLabel(String label) {
        this.label = label;
        return this;
    }

    public Boolean getAvailable() {
        return available;
    }

    public JdbcDriverInfo setAvailable(Boolean available) {
        this.available = available;
        return this;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public JdbcDriverInfo setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        return this;
    }

    @XmlElement(name = "isDefault")
    public Boolean isDefault() {
        return isDefault;
    }

    public JdbcDriverInfo setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
        return this;
    }

    public String getJdbcDriverClass() {
        return jdbcDriverClass;
    }

    public JdbcDriverInfo setJdbcDriverClass(String jdbcDriverClass) {
        this.jdbcDriverClass = jdbcDriverClass;
        return this;
    }

    public List<ClientProperty> getDefaultValues() {
        return defaultValues;
    }

    public JdbcDriverInfo setDefaultValues(List<ClientProperty> defaultValues) {
        this.defaultValues = defaultValues;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JdbcDriverInfo)) return false;

        JdbcDriverInfo that = (JdbcDriverInfo) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (available != null ? !available.equals(that.available) : that.available != null) return false;
        if (jdbcUrl != null ? !jdbcUrl.equals(that.jdbcUrl) : that.jdbcUrl != null) return false;
        if (isDefault != null ? !isDefault.equals(that.isDefault) : that.isDefault != null) return false;
        if (jdbcDriverClass != null ? !jdbcDriverClass.equals(that.jdbcDriverClass) : that.jdbcDriverClass != null)
            return false;
        if (allowSpacesInDbName != null ? !allowSpacesInDbName.equals(that.allowSpacesInDbName) : that.allowSpacesInDbName != null)
            return false;
        return defaultValues != null ? defaultValues.equals(that.defaultValues) : that.defaultValues == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (available != null ? available.hashCode() : 0);
        result = 31 * result + (jdbcUrl != null ? jdbcUrl.hashCode() : 0);
        result = 31 * result + (isDefault != null ? isDefault.hashCode() : 0);
        result = 31 * result + (jdbcDriverClass != null ? jdbcDriverClass.hashCode() : 0);
        result = 31 * result + (allowSpacesInDbName != null ? allowSpacesInDbName.hashCode() : 0);
        result = 31 * result + (defaultValues != null ? defaultValues.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "JdbcDriverInfo{" +
                "name='" + name + '\'' +
                ", label='" + label + '\'' +
                ", available=" + available +
                ", jdbcUrl='" + jdbcUrl + '\'' +
                ", isDefault=" + isDefault +
                ", jdbcDriverClass='" + jdbcDriverClass + '\'' +
                ", allowSpacesInDbName=" + allowSpacesInDbName +
                ", defaultValues=" + defaultValues +
                '}';
    }
}
