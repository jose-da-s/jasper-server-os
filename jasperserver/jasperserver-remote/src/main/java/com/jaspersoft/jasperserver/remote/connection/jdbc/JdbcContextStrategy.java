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
package com.jaspersoft.jasperserver.remote.connection.jdbc;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.core.util.type.MultipleTypeProcessor;
import com.jaspersoft.jasperserver.dto.resources.ClientJdbcDataSource;
import com.jaspersoft.jasperserver.dto.resources.ClientJndiJdbcDataSource;
import com.jaspersoft.jasperserver.dto.resources.ClientResource;
import com.jaspersoft.jasperserver.dto.resources.ClientVirtualDataSource;
import com.jaspersoft.jasperserver.remote.connection.ContextManagementStrategy;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@Service
public class JdbcContextStrategy<ConnectionDescriptionType extends ClientResource<ConnectionDescriptionType>> implements
        ContextManagementStrategy<ConnectionDescriptionType, ConnectionDescriptionType>, MultipleTypeProcessor {
    private static final List<Class<?>> SUPPORTED_PROCESSING_CLASSES = (List)Arrays.asList(ClientJdbcDataSource.class,
            ClientJndiJdbcDataSource.class, ClientVirtualDataSource.class);

    @Resource
    private ClientJdbcConnector clientJdbcConnector;

    @Override
    public ConnectionDescriptionType createContext(ExecutionContext ctx, ConnectionDescriptionType contextDescription, Map<String, Object> data) throws IllegalParameterValueException {
        clientJdbcConnector.testConnection(contextDescription);
        return contextDescription;
    }

    @Override
    public void deleteContext(ConnectionDescriptionType contextDescription, Map<String, Object> data) {
    }

    @Override
    public ConnectionDescriptionType getContextForClient(ConnectionDescriptionType contextDescription, Map<String, Object> data, Map<String, String[]> additionalProperties) {
        ConnectionDescriptionType result = contextDescription;
        if(contextDescription instanceof ClientJdbcDataSource){
            result = (ConnectionDescriptionType) new ClientJdbcDataSource((ClientJdbcDataSource) contextDescription)
                    .setPassword(null);
        }
        return  result;
    }

    @Override
    public List<Class<?>> getProcessableTypes(Class<?> processorClass) {
        return ContextManagementStrategy.class == processorClass ? SUPPORTED_PROCESSING_CLASSES : null;
    }
}

