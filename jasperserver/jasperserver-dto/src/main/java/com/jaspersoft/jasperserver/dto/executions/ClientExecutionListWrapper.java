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
package com.jaspersoft.jasperserver.dto.executions;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Volodya Sabadosh
 * @version $Id$
 * @since 10.07.2017
 */
@XmlRootElement(name = "executions")
public class ClientExecutionListWrapper implements DeepCloneable {
    private List<AbstractClientExecution> executions;

    public ClientExecutionListWrapper() {
    }

    public ClientExecutionListWrapper(ClientExecutionListWrapper other) {
        checkNotNull(other);

        this.executions = copyOf(other.getExecutions());
    }

    @XmlElementWrapper(name = "executions")
    @XmlElements({
            @XmlElement(name = "providedQueryExecution", type = ClientProvidedQueryExecution.class),
            @XmlElement(name = "multiLevelQueryExecution", type = ClientMultiLevelQueryExecution.class),
            @XmlElement(name = "multiAxisQueryExecution", type = ClientMultiAxisQueryExecution.class),
            @XmlElement(name = "icQueryExecution", type = ClientICQueryExecution.class),
            @XmlElement(name = "domainQueryExecution", type = ClientDomainQueryExecution.class)})
    public List<AbstractClientExecution> getExecutions() {
        return executions;
    }

    public ClientExecutionListWrapper setExecutions(List<AbstractClientExecution> executions) {
        this.executions = executions;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientExecutionListWrapper that = (ClientExecutionListWrapper) o;

        if (executions != null ? !executions.equals(that.executions) : that.executions != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return executions != null ? executions.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ExecutionListWrapper{" +
                "executions=" + executions +
                '}';
    }

    @Override
    public ClientExecutionListWrapper deepClone() {
        return new ClientExecutionListWrapper(this);
    }
}
