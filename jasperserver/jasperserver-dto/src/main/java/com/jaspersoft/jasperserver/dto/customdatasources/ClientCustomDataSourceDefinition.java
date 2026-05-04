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
package com.jaspersoft.jasperserver.dto.customdatasources;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
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
@XmlRootElement(name = "definition")
public class ClientCustomDataSourceDefinition implements DeepCloneable<ClientCustomDataSourceDefinition>{
    private String name;
    private List<String> queryTypes;
    private List<CustomDataSourcePropertyDefinition> propertyDefinitions;
    private Boolean testable;

    public ClientCustomDataSourceDefinition(){
    }

    public ClientCustomDataSourceDefinition(ClientCustomDataSourceDefinition source){
        checkNotNull(source);

        name = source.getName();
        queryTypes = copyOf(source.getQueryTypes());
        propertyDefinitions = copyOf(source.getPropertyDefinitions());
        testable = source.getTestable();
    }

    public Boolean getTestable() {
        return testable;
    }

    public ClientCustomDataSourceDefinition setTestable(Boolean testable) {
        this.testable = testable;
        return this;
    }

    public String getName() {
        return name;
    }

    public ClientCustomDataSourceDefinition setName(String name) {
        this.name = name;
        return this;
    }

    @XmlElementWrapper(name = "queryTypes")
    @XmlElement(name = "queryType")
    public List<String> getQueryTypes() {
        return queryTypes;
    }

    public ClientCustomDataSourceDefinition setQueryTypes(List<String> queryTypes) {
        this.queryTypes = queryTypes;
        return this;
    }

    @XmlElementWrapper(name = "propertyDefinitions")
    @XmlElement(name = "propertyDefinition")
    public List<CustomDataSourcePropertyDefinition> getPropertyDefinitions() {
        return propertyDefinitions;
    }

    public ClientCustomDataSourceDefinition setPropertyDefinitions(List<CustomDataSourcePropertyDefinition> propertyDefinitions) {
        this.propertyDefinitions = propertyDefinitions;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientCustomDataSourceDefinition that = (ClientCustomDataSourceDefinition) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (propertyDefinitions != null ? !propertyDefinitions.equals(that.propertyDefinitions) : that.propertyDefinitions != null)
            return false;
        if (queryTypes != null ? !queryTypes.equals(that.queryTypes) : that.queryTypes != null) return false;
        if (testable != null ? !testable.equals(that.testable) : that.testable != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (queryTypes != null ? queryTypes.hashCode() : 0);
        result = 31 * result + (propertyDefinitions != null ? propertyDefinitions.hashCode() : 0);
        result = 31 * result + (testable != null ? testable.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientCustomDataSourceDefinition{" +
                "name='" + name + '\'' +
                ", queryTypes=" + queryTypes +
                ", propertyDefinitions=" + propertyDefinitions +
                ", testable=" + testable +
                '}';
    }

    /*
     * DeepCloneable
     */

    @Override
    public ClientCustomDataSourceDefinition deepClone() {
        return new ClientCustomDataSourceDefinition(this);
    }
}
