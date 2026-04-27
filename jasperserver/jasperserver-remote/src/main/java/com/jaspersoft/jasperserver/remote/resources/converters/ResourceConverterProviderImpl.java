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
package com.jaspersoft.jasperserver.remote.resources.converters;

import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceLookup;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConversionOptions;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConverter;
import com.jaspersoft.jasperserver.dto.common.ClientTypeUtility;
import com.jaspersoft.jasperserver.dto.resources.ClientResource;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@Service("resourceConverterProvider")
public class ResourceConverterProviderImpl implements ResourceConverterProvider {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private BinaryDataResourceConverter binaryDataResourceConverter;
    @jakarta.annotation.Resource
    private List<Class<?>> disabledResourceTypes;
    private List<String> disabledResourceClientTypes = new ArrayList<String>();
    private Map<String, ToClientConverter<? super Resource, ? extends ClientResource, ToClientConversionOptions>> toClientConverters;
    private Map<String, ToServerConverter<? super ClientResource, ? extends Resource, ToServerConversionOptions>> toServerConverters;
    private Map<String, ResourceConverter<? extends Resource, ? extends ClientResource>> resourceConverters;
    private volatile boolean initialized = false;

    public ToClientConverter<? super Resource, ? extends ClientResource, ToClientConversionOptions> getToClientConverter(String serverType) throws IllegalParameterValueException {
        prepareConverters();
        final ToClientConverter<? super Resource, ? extends ClientResource, ToClientConversionOptions> toClientConverter = toClientConverters.get(serverType);
        if(toClientConverter == null){
            throw new IllegalParameterValueException("type", serverType);
        }
        return toClientConverter;
    }

    public ToClientConverter<? super Resource, ? extends ClientResource, ToClientConversionOptions> getToClientConverter(String serverType, String clientType){
        prepareConverters();
        return (ToClientConverter<? super Resource, ? extends ClientResource, ToClientConversionOptions>) resourceConverters.get(getCombinedConverterKey(serverType, clientType));
    }

    public ToServerConverter<? super ClientResource, ? extends Resource, ToServerConversionOptions> getToServerConverter(String serverType, String clientType){
        prepareConverters();
        return (ToServerConverter<? super ClientResource, ? extends Resource, ToServerConversionOptions>) resourceConverters.get(getCombinedConverterKey(serverType, clientType));
    }

    @Override
    public ToClientConverter<? super Resource, ? extends ClientResource, ToClientConversionOptions> getToClientConverter(Resource serverObject) {
        try {
            return getToClientConverter(serverObject instanceof ResourceLookup ? ResourceLookup.class.getName() : serverObject.getResourceType());
        } catch (IllegalParameterValueException e) {
            throw new IllegalStateException("Couldn't find converter for " + serverObject.getResourceType());
        }
    }

    @Override
    public ToServerConverter<? super ClientResource, ? extends Resource, ToServerConversionOptions> getToServerConverter(ClientResource clientObject) throws IllegalParameterValueException {
        return getToServerConverter(ClientTypeUtility.extractClientType(clientObject.getClass()));
    }

    public ToServerConverter<? super ClientResource, ? extends Resource, ToServerConversionOptions> getToServerConverter(String clientType) throws IllegalParameterValueException {
        prepareConverters();
        final ToServerConverter<? super ClientResource, ? extends Resource, ToServerConversionOptions> toServerConverter = toServerConverters.get(clientType != null ? clientType.toLowerCase() : null);
        if(toServerConverter == null){
            throw new IllegalParameterValueException("type", clientType);
        }
        return toServerConverter;
    }

    @Override
    public Class<? extends ClientResource> getClientTypeClass(String clientType) throws IllegalParameterValueException {
        final ResourceConverter<? extends Resource, ? extends ClientResource> resourceConverter =
                (ResourceConverter<? extends Resource, ? extends ClientResource>) getToServerConverter(clientType);
        return resourceConverter.getClientTypeClass();
    }

    // corresponding resourceType of converter assures type safety in further usage
    @SuppressWarnings("unchecked")
    protected void prepareConverters() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    toClientConverters = new HashMap<String, ToClientConverter<? super Resource, ? extends ClientResource, ToClientConversionOptions>>();
                    toServerConverters = new HashMap<String, ToServerConverter<? super ClientResource,
                            ? extends Resource, ToServerConversionOptions>>();
                    resourceConverters = new HashMap<String, ResourceConverter<? extends Resource, ? extends ClientResource>>();
                    final List<ResourceConverter<? super Resource, ? extends ClientResource>> converters = getConverters();
                    if (getConverters() != null) {
                        for (ResourceConverter currentConverter : converters) {
                            final String serverResourceType = currentConverter.getServerResourceType();
                            final String clientResourceType = currentConverter.getClientResourceType().toLowerCase();
                            if(!disabledResourceClientTypes.contains(clientResourceType)) {
                                if (!currentConverter.getClass().isAnnotationPresent(VirtualResourceConverter.class)) {
                                    // disallow virtual type to be used as default toClient converter
                                    toClientConverters.put(serverResourceType, currentConverter);
                                }
                                toServerConverters.put(clientResourceType, currentConverter);
                                resourceConverters.put(getCombinedConverterKey(serverResourceType, clientResourceType), currentConverter);
                            }
                        }
                    }
                    toServerConverters.put(binaryDataResourceConverter.getClientResourceType(), (ToServerConverter)binaryDataResourceConverter);
                    initialized = true;
                }
            }
        }
    }

    protected String getCombinedConverterKey(String serverResourceType, String clientResourceType){
        return serverResourceType + "<=>" + (clientResourceType != null ? clientResourceType.toLowerCase() : "null");
    }

    // cast is safe, spring application context assure safety
    @SuppressWarnings("unchecked")
    protected List<ResourceConverter<? super Resource, ? extends ClientResource>> getConverters() {
        final Map<String, ResourceConverter> convertersMap = context.getBeansOfType(ResourceConverter.class);
        return (List) new ArrayList<ResourceConverter>(convertersMap.values());
    }

    @PostConstruct
    public void initialize(){
        for (Class<?> disabledResourceType : disabledResourceTypes) {
            disabledResourceClientTypes.add(ClientTypeUtility.extractClientType(disabledResourceType).toLowerCase());
        }
    }
}

