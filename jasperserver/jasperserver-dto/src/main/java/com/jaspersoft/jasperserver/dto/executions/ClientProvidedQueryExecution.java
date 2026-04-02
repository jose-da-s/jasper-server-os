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

import com.jaspersoft.jasperserver.dto.resources.ClientReference;
import com.jaspersoft.jasperserver.dto.resources.ClientReferenceable;

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @author Vasyl Spachynskyi
 * @version $Id$
 * @since 15.02.2016
 */
@XmlRootElement(name = "queryExecution")
public class ClientProvidedQueryExecution extends AbstractClientExecution<ClientProvidedQueryExecution> {

    public ClientProvidedQueryExecution() {
    }

    public ClientProvidedQueryExecution(ClientReferenceable dataSource) {
        setDataSource(dataSource);
    }

    public ClientProvidedQueryExecution(ClientProvidedQueryExecution source) {
        super(source);
    }

    public ClientProvidedQueryExecution(String dataSourceUri) {
        this(new ClientReference(dataSourceUri));
    }

    @Override
    public ClientQueryParams getParams() {
        return super.getParams();
    }

    @Override
    public ClientProvidedQueryExecution deepClone() {
        return new ClientProvidedQueryExecution(this);
    }

    @Override
    public String toString() {
        return "ClientProvidedQueryExecution{} " + super.toString();
    }
}
