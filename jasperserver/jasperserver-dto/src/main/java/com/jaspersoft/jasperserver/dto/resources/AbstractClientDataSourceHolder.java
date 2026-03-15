/*
 * Copyright (C) 2025 the Jasper Server OS Authors
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

import com.jaspersoft.jasperserver.dto.resources.domain.ClientDomain;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
public abstract class AbstractClientDataSourceHolder<BuilderType extends AbstractClientDataSourceHolder<BuilderType>> extends ClientResource<BuilderType> {

    private ClientReferenceableDataSource dataSource;

    public AbstractClientDataSourceHolder() {
    }

    public AbstractClientDataSourceHolder(AbstractClientDataSourceHolder other) {
        super(other);
        dataSource = copyOf(other.getDataSource());
    }

    @XmlElements({
            /*ClientReference is included here to serve as resource reference*/
            @XmlElement(type = ClientReference.class, name = "dataSourceReference"),
            @XmlElement(type = ClientAwsDataSource.class, name = "awsDataSource"),
            @XmlElement(type = ClientBeanDataSource.class, name = "beanDataSource"),
            @XmlElement(type = ClientCustomDataSource.class, name = "customDataSource"),
            @XmlElement(type = ClientJdbcDataSource.class, name = "jdbcDataSource"),
            @XmlElement(type = ClientJndiJdbcDataSource.class, name = "jndiJdbcDataSource"),
            @XmlElement(type = ClientVirtualDataSource.class, name = "virtualDataSource"),
            @XmlElement(type = ClientSemanticLayerDataSource.class, name = "semanticLayerDataSource"),
            @XmlElement(type = ClientDomain.class, name = "domain"),
            @XmlElement(type = ClientAdhocDataView.class, name = "advDataSource"),
            @XmlElement(type = ClientAzureSqlDataSource.class, name = "azureSqlDataSource"),
    })
    public ClientReferenceableDataSource getDataSource() {
        return dataSource;
    }

    // unchecked cast to BuilderType safety assured by the rule of usage BuilderType generic parameter.
    @SuppressWarnings("unchecked")
    public BuilderType setDataSource(ClientReferenceableDataSource dataSource) {
        this.dataSource = dataSource;
        return (BuilderType) this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AbstractClientDataSourceHolder that = (AbstractClientDataSourceHolder) o;

        if (dataSource != null ? !dataSource.equals(that.dataSource) : that.dataSource != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (dataSource != null ? dataSource.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AbstractClientDataSourceHolder{" +
                "dataSource=" + dataSource +
                ", version=" + getVersion() +
                ", permissionMask=" + getPermissionMask() +
                ", uri='" + getUri() + '\'' +
                ", label='" + getLabel() + '\'' +
                '}';
    }
}
