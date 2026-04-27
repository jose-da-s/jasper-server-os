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
package com.jaspersoft.jasperserver.jaxrs.resources;

import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConversionOptions;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConverter;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.dto.resources.ClientResourceLookup;
import com.jaspersoft.jasperserver.jaxrs.common.RestConstants;
import com.jaspersoft.jasperserver.remote.exception.MandatoryParameterNotFoundException;
import com.jaspersoft.jasperserver.remote.exception.NotAcceptableException;
import com.jaspersoft.jasperserver.remote.exception.ReferencedResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.resources.ClientTypeHelper;
import com.jaspersoft.jasperserver.remote.resources.converters.ResourceConverterProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@Component
public class ContentNegotiationHandler {
    @Autowired
    private HttpServletRequest request;
    @jakarta.annotation.Resource(name = "concreteRepository")
    private RepositoryService repository;
    @jakarta.annotation.Resource
    private ResourceConverterProvider resourceConverterProvider;

    public boolean isAcceptable(Object sourceObject, String sourceMimeType, String targetMimeType){
        final boolean result;
        final boolean requiresConversion = requiresConversion(sourceMimeType, targetMimeType);
        if(requiresConversion && sourceObject instanceof ClientResourceLookup){
            final Resource resource = getResource((ClientResourceLookup) sourceObject);
            result = getToClientConverter(resource, targetMimeType) != null;
        } else {
            result = !requiresConversion;
        }
        return result;
    }

    protected ToClientConverter getToClientConverter(Resource repositoryResource, String targetMimeType){
        final String clientType = ClientTypeHelper.extractClientType(targetMimeType);
        if(targetMimeType != null && clientType == null) throw new NotAcceptableException();
        return resourceConverterProvider.getToClientConverter(
                repositoryResource.getResourceType(),
                clientType);
    }

    protected Resource getResource(ClientResourceLookup resourceLookup){
        final String uri = resourceLookup.getUri();
        if(uri == null || uri.isEmpty()){
            throw new MandatoryParameterNotFoundException("uri");
        }
        final Resource resource = repository.getResource(ExecutionContextImpl.getRuntimeExecutionContext(), uri);
        if(resource == null){
            throw new ReferencedResourceNotFoundException(uri, "uri");
        }
        return resource;
    }

    public Object handle(Object sourceObject, String sourceMimeType, String targetMimeType,
            Map<String, String[]> additionalProperties){
        Object result = null;
        if (requiresConversion(sourceMimeType, targetMimeType)) {
            if(sourceObject instanceof ClientResourceLookup){
                final Resource resource = getResource((ClientResourceLookup) sourceObject);
                final ToClientConverter toClientConverter = getToClientConverter(resource, targetMimeType);
                if(toClientConverter != null) {
                    String expanded = request.getParameter(RestConstants.QUERY_PARAM_EXPANDED);
                    final ToClientConversionOptions options = ToClientConversionOptions.getDefault()
                            .setAcceptMediaType(targetMimeType)
                            .setExpanded(expanded != null ? expanded.equalsIgnoreCase("true") : false)
                            .setAdditionalProperties(additionalProperties);
                    result = toClientConverter.toClient(resource, options);
                }
            }
        } else {
            result = sourceObject;
        }
        if(result == null){
            throw new NotAcceptableException();
        }
        return result;
    }

    protected boolean requiresConversion(String sourceMimeType, String targetMimeTypee){
        // if accept is not generic and doesn't correspond to content type, then conversion should happen
        return !(targetMimeTypee == null ||
                "application/json".equalsIgnoreCase(targetMimeTypee) ||
                "application/xml".equalsIgnoreCase(targetMimeTypee) ||
                sourceMimeType.equalsIgnoreCase(targetMimeTypee));
    }
}

