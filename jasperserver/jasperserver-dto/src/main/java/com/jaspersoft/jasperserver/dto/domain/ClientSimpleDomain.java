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
package com.jaspersoft.jasperserver.dto.domain;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;
import com.jaspersoft.jasperserver.dto.connection.metadata.TableMetadata;
import com.jaspersoft.jasperserver.dto.resources.AbstractClientDataSourceHolder;

import jakarta.xml.bind.annotation.XmlRootElement;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@XmlRootElement(name = "simpleDomain")
public class ClientSimpleDomain extends AbstractClientDataSourceHolder<ClientSimpleDomain> implements DeepCloneable<ClientSimpleDomain> {
    private TableMetadata metadata;

    public ClientSimpleDomain(){}

    public ClientSimpleDomain(ClientSimpleDomain source){
        super(source);
        metadata = copyOf(source.getMetadata());
    }

    public TableMetadata getMetadata() {
        return metadata;
    }

    public ClientSimpleDomain setMetadata(TableMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ClientSimpleDomain that = (ClientSimpleDomain) o;

        if (metadata != null ? !metadata.equals(that.metadata) : that.metadata != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientSimpleDomain{" +
                "metadata=" + metadata +
                "} " + super.toString();
    }

    /*
     * DeepCloneable
     */

    @Override
    public ClientSimpleDomain deepClone() {
        return new ClientSimpleDomain(this);
    }
}
