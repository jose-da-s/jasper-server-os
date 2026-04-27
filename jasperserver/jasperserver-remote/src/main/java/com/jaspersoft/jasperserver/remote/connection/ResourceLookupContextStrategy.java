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
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.remote.exception.ReferencedResourceAccessDeniedException;
import com.jaspersoft.jasperserver.remote.exception.ResourceTypeNotSupportedException;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConversionOptions;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConverter;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributesResolver;
import com.jaspersoft.jasperserver.dto.resources.ClientResource;
import com.jaspersoft.jasperserver.dto.resources.ClientResourceLookup;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.exception.MandatoryParameterNotFoundException;
import com.jaspersoft.jasperserver.remote.exception.ReferencedResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.exception.ResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.resources.converters.ResourceConverterProvider;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@Component
public class ResourceLookupContextStrategy implements
        ContextManagementStrategy<ClientResourceLookup, ClientResourceLookup>,
        ContextQueryExecutor<Object, ClientResourceLookup>, GenericTypeMetadataBuilder<ClientResourceLookup>,
        ContextParametrizedMetadataBuilder<ClientResourceLookup, Object> {
    private static final String INNER_UUID = "innerUuid";
    @jakarta.annotation.Resource(name = "concreteRepository")
    private RepositoryService repository;
    @jakarta.annotation.Resource
    private ResourceConverterProvider resourceConverterProvider;
    @jakarta.annotation.Resource
    private ContextsManager contextsManager;
    @jakarta.annotation.Resource
    private ProfileAttributesResolver profileAttributesResolver;

    protected ClientResource getFullClientResource(ClientResourceLookup resourceLookup){
        ClientResource clientResource = null;
        if(resourceLookup != null){
            if (resourceLookup.getUri() == null) {
                throw new MandatoryParameterNotFoundException("uri");
            }
            if(profileAttributesResolver.containsAttribute(resourceLookup.getUri())){
                throw new IllegalParameterValueException("uri", resourceLookup.getUri());
            }
            Resource repositoryResource = null;
            try {
                String uri = resourceLookup.getUri();
                // Check if URI is valid before fetching it
                if (uri.isEmpty() || !uri.startsWith(Folder.SEPARATOR)) {
                    throw new ReferencedResourceNotFoundException(resourceLookup.getUri(), "uri");
                }
                repositoryResource = repository
                        .getResource(ExecutionContextImpl.getRuntimeExecutionContext(), resourceLookup.getUri());
            } catch (AccessDeniedException e) {
                throw new ReferencedResourceAccessDeniedException(resourceLookup.getUri(), "uri");
            }
            if(repositoryResource != null) {
                String clientType = resourceLookup.getResourceType();
                ToClientConverter<? super Resource, ? extends ClientResource, ToClientConversionOptions> toClientConverter;
                if (clientType != null) {
                    toClientConverter = resourceConverterProvider.getToClientConverter(repositoryResource.getResourceType(), clientType);
                } else {
                    toClientConverter = resourceConverterProvider.getToClientConverter(repositoryResource.getResourceType());
                    clientType = toClientConverter.getClientResourceType();
                }
                if (contextsManager.getContextDescriptionClass(clientType) == null) {
                    // this client type isn't supported. Let's fail here
                    throw new ResourceTypeNotSupportedException(clientType);
                }

                clientResource = toClientConverter
                        .toClient(repositoryResource, getToClientConversionOptionsForFullResource());
            } else {
                throw new ReferencedResourceNotFoundException(resourceLookup.getUri(), "uri");
            }
        }
        return clientResource;
    }

    protected ToClientConversionOptions getToClientConversionOptionsForFullResource() {
        ToClientConversionOptions toClientConversionOptions = ToClientConversionOptions.getDefault();
        // we will need to include secure data in order to get full resource
        // and this method seems like it is only in used for contextsManager (create uuid/ lookup metadata)
        // otherwise, a field like "password" will be ignored.  See bug JRS-19403
        toClientConversionOptions.setAllowSecureDataConversation(true);
        return toClientConversionOptions;
    }

    @Override
    public boolean isMetadataSupported(ExecutionContext ctx, ClientResourceLookup resourceLookup, String metadataClientType){
        return "repository.resourceLookup.metadata".equalsIgnoreCase(metadataClientType) ||
                contextsManager.isMetadataSupported(ctx, getFullClientResource(resourceLookup), metadataClientType);
    }

    @Override
    public String getMetadataClientResourceType(ClientResourceLookup connectionDescription) {
        return contextsManager.getMetadataClientResourceType(getFullClientResource(connectionDescription));
    }

    protected UUID getInnerUuid(Map<String, Object> data){
        final Object uuid = data.get(INNER_UUID);
        if(uuid instanceof UUID){
            return (UUID) uuid;
        } else {
            throw new ResourceNotFoundException();

        }
    }

    @Override
    public ClientResourceLookup createContext(ExecutionContext ctx, ClientResourceLookup contextDescription, Map<String, Object> data) throws IllegalParameterValueException {
        final UUID uuid = contextsManager.createContext(getFullClientResource(contextDescription));
        data.put(INNER_UUID, uuid);
        return contextDescription;
    }

    @Override
    public void deleteContext(ClientResourceLookup contextDescription, Map<String, Object> data) {
        contextsManager.removeContext(getInnerUuid(data));
    }

    @Override
    public ClientResourceLookup getContextForClient(ClientResourceLookup contextDescription, Map<String, Object> data, Map<String, String[]> additionalProperties) {
        // nothing to secure in resource lookup
        return contextDescription;
    }

    @Override
    public Object build(ClientResourceLookup context, Map<String, String[]> options, Map<String, Object> contextData) {
        return contextsManager.getContextMetadata(getInnerUuid(contextData), options);
    }

    @Override
    public Object build(ClientResourceLookup connection, Object options, Map<String, Object> contextData) {
        return contextsManager.getContextMetadata(getInnerUuid(contextData), options);
    }

    @Override
    public Object executeQuery(Object query, ClientResourceLookup connection, Map<String, String[]> queryParameters, Map<String, Object> data) {
        return contextsManager.executeQuery(getInnerUuid(data), query, queryParameters);
    }

    @Override
    public Object executeQueryForMetadata(Object query, ClientResourceLookup connection, Map<String, Object> data) {
        return contextsManager.executeQueryForMetadata(getInnerUuid(data), query);
    }
}

