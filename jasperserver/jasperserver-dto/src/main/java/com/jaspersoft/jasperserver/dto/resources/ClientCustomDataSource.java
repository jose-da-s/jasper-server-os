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

import com.jaspersoft.jasperserver.dto.adhoc.query.validation.groups.QueryExecutionValidationGroup;
import com.jaspersoft.jasperserver.dto.resources.validation.ValidResourceReferences;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@XmlRootElement(name = ResourceMediaType.CUSTOM_DATA_SOURCE_CLIENT_TYPE)
public class ClientCustomDataSource extends ClientResource<ClientCustomDataSource> implements ClientReferenceableDataSource {
    private String serviceClass;
    private String dataSourceName;
    private List<ClientProperty> properties;

    @ValidResourceReferences(groups = QueryExecutionValidationGroup.class)
    private Map<String, ClientReferenceableFile> resources;

    public String getDataSourceName() {
        return dataSourceName;
    }

    public ClientCustomDataSource(){
    }

    public ClientCustomDataSource(ClientCustomDataSource source){
        super(source);
        serviceClass = source.getServiceClass();
        dataSourceName = source.getDataSourceName();
        properties = copyOf(source.getProperties());
        resources = copyOf(source.getResources());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientCustomDataSource)) return false;
        if (!super.equals(o)) return false;

        ClientCustomDataSource that = (ClientCustomDataSource) o;

        if (dataSourceName != null ? !dataSourceName.equals(that.dataSourceName) : that.dataSourceName != null)
            return false;
        if (properties != null ? (that.properties == null || !new HashSet(properties).equals(new HashSet(that.properties))) : that.properties != null) return false;
        if (resources != null ? !resources.equals(that.resources) : that.resources != null) return false;
        if (serviceClass != null ? !serviceClass.equals(that.serviceClass) : that.serviceClass != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (serviceClass != null ? serviceClass.hashCode() : 0);
        result = 31 * result + (dataSourceName != null ? dataSourceName.hashCode() : 0);
        result = 31 * result + (properties != null ?  new HashSet(properties).hashCode() : 0);
        result = 31 * result + (resources != null ? resources.hashCode() : 0);
        return result;
    }

    @XmlJavaTypeAdapter(FilesMapXmlAdapter.class)
    @XmlElement(name = "resources")
    public Map<String, ClientReferenceableFile> getResources() {
        return resources;
    }

    public ClientCustomDataSource setResources(Map<String, ClientReferenceableFile> resources) {
        this.resources = resources;
        return this;
    }

    public ClientCustomDataSource setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
        return this;
    }

    public String getServiceClass() {
        return serviceClass;
    }

    public ClientCustomDataSource setServiceClass(String serviceClass) {
        this.serviceClass = serviceClass;
        return this;
    }

    public List<ClientProperty> getProperties() {
        return properties;
    }

    public ClientCustomDataSource setProperties(List<ClientProperty> properties) {
        String prefix = "repo:";
        this.properties = properties;
        /**
         * for testing purpose:
         * add resource reference of JSON custom data source to client custom data source
         */
        if (properties != null) {
            for (ClientProperty property : properties) {
                if (property.getKey().equals("fileName")) {
                    String value = property.getValue();
                    if ((property.getValue() != null) && property.getValue().startsWith(prefix)) {
                        Map<String, ClientReferenceableFile> sourceResources = new HashMap<String, ClientReferenceableFile>();
                        String uri = value.substring(prefix.length());
                        sourceResources.put("dataFile", new ClientReference(uri));
                        setResources(sourceResources);
                    }
                }
            }
        }
        return this;
    }

    @Override
    public String toString() {
        return "ClientCustomDataSource{" +
                "serviceClass='" + serviceClass + '\'' +
                ", dataSourceName='" + dataSourceName + '\'' +
                ", properties=" + properties +
                ", resources=" + resources +
                '}';
    }

    @Override
    public ClientCustomDataSource deepClone() {
        return new ClientCustomDataSource(this);
    }
}
