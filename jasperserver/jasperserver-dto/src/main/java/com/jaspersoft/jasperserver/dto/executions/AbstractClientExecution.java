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
package com.jaspersoft.jasperserver.dto.executions;

import com.jaspersoft.jasperserver.dto.adhoc.component.ClientGenericComponent;
import com.jaspersoft.jasperserver.dto.common.DeepCloneable;
import com.jaspersoft.jasperserver.dto.executions.validation.CheckInMemoryDataSourceType;
import com.jaspersoft.jasperserver.dto.resources.*;
import com.jaspersoft.jasperserver.dto.resources.domain.ClientDomain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.util.List;
import java.util.Map;

import static com.jaspersoft.jasperserver.dto.resources.ResourceMediaType.ADHOC_DATA_VIEW_CLIENT_TYPE;
import static com.jaspersoft.jasperserver.dto.resources.ResourceMediaType.AWS_DATA_SOURCE_CLIENT_TYPE;
import static com.jaspersoft.jasperserver.dto.resources.ResourceMediaType.AZURE_SQL_DATA_SOURCE_CLIENT_TYPE;
import static com.jaspersoft.jasperserver.dto.resources.ResourceMediaType.BEAN_DATA_SOURCE_CLIENT_TYPE;
import static com.jaspersoft.jasperserver.dto.resources.ResourceMediaType.CUSTOM_DATA_SOURCE_CLIENT_TYPE;
import static com.jaspersoft.jasperserver.dto.resources.ResourceMediaType.DOMAIN_CLIENT_TYPE;
import static com.jaspersoft.jasperserver.dto.resources.ResourceMediaType.JDBC_DATA_SOURCE_CLIENT_TYPE;
import static com.jaspersoft.jasperserver.dto.resources.ResourceMediaType.JNDI_JDBC_DATA_SOURCE_CLIENT_TYPE;
import static com.jaspersoft.jasperserver.dto.resources.ResourceMediaType.REFERENCE_CLIENT_TYPE;
import static com.jaspersoft.jasperserver.dto.resources.ResourceMediaType.SEMANTIC_LAYER_DATA_SOURCE_CLIENT_TYPE;
import static com.jaspersoft.jasperserver.dto.resources.ResourceMediaType.TOPIC_TYPE;
import static com.jaspersoft.jasperserver.dto.resources.ResourceMediaType.VIRTUAL_DATA_SOURCE_CLIENT_TYPE;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;


/**
 * @author Vasyl Spachynskyi
 * @version $Id$
 * @since 13.01.2016
 */
public abstract class AbstractClientExecution<T extends AbstractClientExecution<T>> implements DeepCloneable<T> {
    @Valid
    private ClientQueryParams params;
    @NotNull
    @CheckInMemoryDataSourceType
    private ClientReferenceable dataSource;
    private String id;
    private ExecutionStatusObject status;

    /**
     * Optional. Used as hint to the executor
     */
    private List<ClientReferenceable> resources;

    public AbstractClientExecution() {
    }

    public AbstractClientExecution(AbstractClientExecution<T> clientExecution) {
        checkNotNull(clientExecution);

        params = copyOf(clientExecution.getParams());
        dataSource = copyOf(clientExecution.dataSource);
        id = clientExecution.id;
        status = copyOf(clientExecution.status);
    }

    public ClientQueryParams getParams() {
        return params;
    }

    // safety of the unchecked cast to T is assured by the rule of usage T generic parameter.
    @SuppressWarnings("unchecked")
    public T setParams(ClientQueryParams params) {
        this.params = params;
        return (T) this;
    }

    @XmlElements({
            /*ClientReference is included here to serve as resource reference*/
            @XmlElement(type = ClientReference.class, name = REFERENCE_CLIENT_TYPE),
            @XmlElement(type = ClientDomain.class, name = DOMAIN_CLIENT_TYPE),
            /* Non supported for now */
            @XmlElement(type = ClientAwsDataSource.class, name = AWS_DATA_SOURCE_CLIENT_TYPE),
            @XmlElement(type = ClientBeanDataSource.class, name = BEAN_DATA_SOURCE_CLIENT_TYPE),
            @XmlElement(type = ClientCustomDataSource.class, name = CUSTOM_DATA_SOURCE_CLIENT_TYPE),
            @XmlElement(type = ClientJdbcDataSource.class, name = JDBC_DATA_SOURCE_CLIENT_TYPE),
            @XmlElement(type = ClientJndiJdbcDataSource.class, name = JNDI_JDBC_DATA_SOURCE_CLIENT_TYPE),
            @XmlElement(type = ClientVirtualDataSource.class, name = VIRTUAL_DATA_SOURCE_CLIENT_TYPE),
            @XmlElement(type = ClientSemanticLayerDataSource.class, name = SEMANTIC_LAYER_DATA_SOURCE_CLIENT_TYPE),
            @XmlElement(type = ClientAdhocDataView.class, name = ADHOC_DATA_VIEW_CLIENT_TYPE),
            @XmlElement(type = ClientTopic.class, name = TOPIC_TYPE),
            @XmlElement(type = ClientAzureSqlDataSource.class, name = AZURE_SQL_DATA_SOURCE_CLIENT_TYPE)
    })
    public ClientReferenceable getDataSource() {
        return dataSource;
    }

    // safety of the unchecked cast to T is assured by the rule of usage T generic parameter.
    @SuppressWarnings("unchecked")
    public T setDataSource(ClientReferenceable dataSource) {
        this.dataSource = dataSource;
        return (T) this;
    }
    @XmlElement(name = "id")
    public String getId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    public T setId(String id) {
        this.id = id;
        return (T) this;
    }

    @XmlElement(name = "status")
    public ExecutionStatusObject getStatus() {
        return status;
    }

    @SuppressWarnings("unchecked")
    public T setStatus(ExecutionStatusObject status) {
        this.status = status;
        return (T) this;
    }

    @XmlElementWrapper(name = "resources")
    @XmlElements({
            @XmlElement(name = "reference", type = ClientReference.class),
            @XmlElement(name = "file", type = ClientFile.class),
            @XmlElement(name = "reportUnit", type = ClientReportUnit.class),
            @XmlElement(name = "adhocDataView", type = ClientAdhocDataView.class),
            @XmlElement(name = "semanticLayerDataSource", type = ClientSemanticLayerDataSource.class),
            @XmlElement(name = "customDataSource", type = ClientCustomDataSource.class),
            @XmlElement(name = "jdbcDataSource", type = ClientJdbcDataSource.class),
            @XmlElement(name = "jndiJdbcDataSource", type = ClientJndiJdbcDataSource.class),
            @XmlElement(name = "azureSqlDataSource", type = ClientAzureSqlDataSource.class),
            @XmlElement(name = "awsDataSource", type = ClientAwsDataSource.class),
            @XmlElement(name = TOPIC_TYPE, type = ClientTopic.class)
    })
    public List<ClientReferenceable> getResources() {
        return resources;
    }

    public T setResources(List<ClientReferenceable> resources) {
        this.resources = resources;
        return (T) this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractClientExecution<?> that = (AbstractClientExecution) o;

        if (dataSource != null ? !dataSource.equals(that.dataSource) : that.dataSource != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (params != null ? !params.equals(that.params) : that.params != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (resources != null ? !resources.equals(that.resources) : that.resources != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = params != null ? params.hashCode() : 0;
        result = 31 * result + (dataSource != null ? dataSource.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (resources != null ? resources.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AbstractClientExecution{" +
                "params=" + params +
                ", dataSource=" + dataSource +
                ", id='" + id + '\'' +
                ", status=" + status +
                ", resources=" + resources +
                '}';
    }

    @Override
    public abstract T deepClone();
}