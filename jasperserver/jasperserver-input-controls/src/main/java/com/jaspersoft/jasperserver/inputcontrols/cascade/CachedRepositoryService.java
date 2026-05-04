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
package com.jaspersoft.jasperserver.inputcontrols.cascade;

import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.inputcontrols.cascade.cache.ControlLogicCacheManager;
import com.jaspersoft.jasperserver.inputcontrols.cascade.cache.SessionCache;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides cached access to the repository.
 *
 * @author Anton Fomin
 * @version $Id$
 */
@Service
public class CachedRepositoryService {

    private static final String URI_RESOURCE_TYPE_MAPPING_CACHE_KEY = "uriResourceTypeMapping";

    @jakarta.annotation.Resource
    private ControlLogicCacheManager controlLogicCacheManager;

    @jakarta.annotation.Resource(name = "concreteRepository")
    private RepositoryService repositoryService;

    @jakarta.annotation.Resource(name = "inputControlsCachingEnabled")
    private boolean doCache;

    protected Map<String, Class<? super Resource>> getUriResourceTypeMapping() {
        final SessionCache sessionCache = controlLogicCacheManager.getSessionCache();
        Map<String, Class<? super Resource>> uriResourceTypeMapping = sessionCache.getCacheInfo(Map.class, URI_RESOURCE_TYPE_MAPPING_CACHE_KEY);
        if (uriResourceTypeMapping == null)
            synchronized (sessionCache) {
                uriResourceTypeMapping = sessionCache.getCacheInfo(Map.class, URI_RESOURCE_TYPE_MAPPING_CACHE_KEY);
                if (uriResourceTypeMapping == null) {
                    uriResourceTypeMapping = new HashMap<String, Class<? super Resource>>();
                    sessionCache.setCacheInfo(Map.class, URI_RESOURCE_TYPE_MAPPING_CACHE_KEY, uriResourceTypeMapping);
                }
            }
        return uriResourceTypeMapping;
    }

    public <T> T getResource(Class<T> resourceType, ResourceReference reference) throws CascadeResourceNotFoundException {
        T resource = null;
        if (reference != null) {
            if (reference.isLocal()) {
                resource = (T) reference.getLocalResource();
            } else {
                resource = getResource(resourceType, reference.getReferenceURI());
            }
        }
        return resource;
    }

    public <T> T getResource(Class<T> resourceType, String resourceUri) throws CascadeResourceNotFoundException {
        final SessionCache sessionCache = controlLogicCacheManager.getSessionCache();
        final Map<String, Class<? super Resource>> uriResourceTypeMapping = getUriResourceTypeMapping();
        Class<? super Resource> concreteResourceType = uriResourceTypeMapping.get(resourceUri);
        T result = concreteResourceType != null && resourceType.isAssignableFrom(concreteResourceType) ? (T) sessionCache.getCacheInfo(concreteResourceType, resourceUri) : null;

        if (!doCache || result == null) {
            synchronized (sessionCache) {
                result = concreteResourceType != null && resourceType.isAssignableFrom(concreteResourceType) ? (T) sessionCache.getCacheInfo(concreteResourceType, resourceUri) : null;

                if (result == null) {
                    final Resource resource = repositoryService.getResource(ExecutionContextImpl.getRuntimeExecutionContext(), resourceUri);
                    if (resource == null || !resourceType.isAssignableFrom(resource.getClass()))
                        throw new CascadeResourceNotFoundException(resourceUri, resourceType.getName());
                    result = (T) resource;
                    if (concreteResourceType == null)
                        try {
                            concreteResourceType = (Class<? super Resource>) Class.forName(resource.getResourceType());
                        } catch (ClassNotFoundException e) {
                            throw new IllegalStateException("Resource type class not found", e);
                        }
                    sessionCache.setCacheInfo(concreteResourceType, resourceUri, resource);
                    uriResourceTypeMapping.put(resourceUri, concreteResourceType);
                }
            }
        }
        return result;
    }

    public void updateResource(Resource resource) {
        repositoryService.saveResource(null, resource);
        if (doCache) {
            controlLogicCacheManager.clearCache();
        }
    }

    public ControlLogicCacheManager getControlLogicCacheManager() {
        return controlLogicCacheManager;
    }

    public void setControlLogicCacheManager(ControlLogicCacheManager controlLogicCacheManager) {
        this.controlLogicCacheManager = controlLogicCacheManager;
    }

    public boolean isDoCache() {
        return doCache;
    }

    public void setDoCache(boolean doCache) {
        this.doCache = doCache;
    }

    public RepositoryService getRepositoryService() {
        return repositoryService;
    }

    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }
}

