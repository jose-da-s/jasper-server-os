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
package com.jaspersoft.jasperserver.remote.connection;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConversionOptions;
import com.jaspersoft.jasperserver.core.util.type.GenericParametersHelper;
import com.jaspersoft.jasperserver.core.util.type.MultipleTypeProcessor;
import com.jaspersoft.jasperserver.dto.common.ClientTypeUtility;
import com.jaspersoft.jasperserver.dto.common.validations.SupportsValidation;
import com.jaspersoft.jasperserver.dto.resources.ClientDomainTopic;
import com.jaspersoft.jasperserver.dto.resources.ClientReportUnit;
import com.jaspersoft.jasperserver.dto.resources.ClientResource;
import com.jaspersoft.jasperserver.dto.resources.ClientSemanticLayerDataSource;
import com.jaspersoft.jasperserver.dto.resources.domain.ClientDomain;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.resources.converters.ResourceConverterProvider;
import com.jaspersoft.jasperserver.remote.resources.converters.ToServerConversionOptions;
import com.jaspersoft.jasperserver.remote.resources.converters.ToServerConverter;
import org.springframework.stereotype.Service;

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
@SupportsValidation
public class ResourceConvertingContextStrategy implements GenericTypeContextStrategy<ClientResource, Resource>,
        MultipleTypeProcessor {
    private static final List<Class<?>> SUPPORTED_PROCESSING_CLASSES = (List) Arrays.asList(ClientDomain.class,
            ClientSemanticLayerDataSource.class, ClientReportUnit.class, ClientDomainTopic.class);
    public static final String CLIENT_TYPE_KEY = "clientType";
    @jakarta.annotation.Resource
    private ResourceConverterProvider resourceConverterProvider;
    @Override
    public Resource createContext(ExecutionContext ctx, ClientResource contextDescription, Map<String, Object> contextData) throws IllegalParameterValueException {
        final String clientType = ClientTypeUtility.extractClientType(contextDescription.getClass());
        contextData.put(CLIENT_TYPE_KEY, clientType);
        return resourceConverterProvider.getToServerConverter(contextDescription)
                .toServer(ctx, contextDescription, ToServerConversionOptions.getDefault().setSkipRepoFieldsValidation(true));
    }

    @Override
    public void deleteContext(Resource contextDescription, Map<String, Object> contextData) {
        // nothing to clean up
    }

    @Override
    public ClientResource getContextForClient(Resource contextDescription, Map<String, Object> contextData,
            Map<String, String[]> additionalProperties) {
        final String clientType = (String) contextData.get(CLIENT_TYPE_KEY);
        return resourceConverterProvider.getToClientConverter(contextDescription.getResourceType(), clientType)
                .toClient(contextDescription, ToClientConversionOptions.getDefault()
                        .setAdditionalProperties(additionalProperties));
    }

    @Override
    public List<Class<?>> getProcessableTypes(Class<?> processorClass) {
        return processorClass == ContextManagementStrategy.class ? SUPPORTED_PROCESSING_CLASSES : null;
    }

    @Override
    public Class<?> getConcreteInternalType(ClientResource context) {
        final ToServerConverter<? super ClientResource, ? extends Resource, ToServerConversionOptions> toServerConverter =
                resourceConverterProvider.getToServerConverter(context);
        return GenericParametersHelper.getGenericTypeArgument(toServerConverter.getClass(), ToServerConverter.class, 1);
    }
}

