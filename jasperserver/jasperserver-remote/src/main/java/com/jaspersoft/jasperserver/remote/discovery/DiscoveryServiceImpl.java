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

package com.jaspersoft.jasperserver.remote.discovery;

import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceLookup;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConverter;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.view.domain.FilterCriteria;
import com.jaspersoft.jasperserver.core.util.type.GenericTypeProcessorRegistry;
import com.jaspersoft.jasperserver.dto.discovery.VisualizationMetadata;
import com.jaspersoft.jasperserver.remote.exception.ResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.services.DiscoveryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>Performs discovery of different abilities of resources</p>
 *
 * @author Zakhar.Tomchenco
 * @version $Id: $
 */

@Service("discoveryService")
public class DiscoveryServiceImpl implements DiscoveryService {
    @jakarta.annotation.Resource
    private DiscoveryStrategyProvider provider;

    @jakarta.annotation.Resource(name = "concreteRepository")
    protected RepositoryService repositoryService;

    @jakarta.annotation.Resource
    private GenericTypeProcessorRegistry genericTypeProcessorRegistry;

    @Override
    public VisualizationMetadata discover(String uri) {
        Resource resource = getResource(uri);

        if (resource != null){
            DiscoveryStrategy strategy = provider.getDiscoveryStrategyFor(resource);
            VisualizationMetadata res = new VisualizationMetadata();

            res.setParameters(strategy.discoverParameters(resource));
            res.setOutputParameters(strategy.discoverOutputParameters(resource));

            res.setRepositoryType(toClientResourceType(resource.getClass()));

            res.setUri(resource.getURIString());

            return res;
        }

        throw new ResourceNotFoundException(uri);
    }

    protected Resource getResource(String uri) {
        Resource resource = repositoryService.getResource(ExecutionContextImpl.getRuntimeExecutionContext(), uri);

        if (resource == null) {
            FilterCriteria criteria = new FilterCriteria();
            String[] path = uri.split(Folder.SEPARATOR);

            criteria.addFilterElement(FilterCriteria.createPropertyEqualsFilter("name", path[path.length - 1]));

            ResourceLookup[] searchResult = repositoryService.findResource(null, criteria);

            if (searchResult.length > 0) {
                String parentFolderUri = uri.substring(0, uri.length() - path[path.length - 1].length() - Folder.SEPARATOR.length());
                List<String> candidates = new ArrayList<String>();

                for (ResourceLookup res : searchResult) {
                    if (res.getParentFolder().endsWith(parentFolderUri) || uri.endsWith(res.getURIString())) {
                        candidates.add(res.getURIString());
                    }
                }

                if (candidates.size() > 0) {
                    Collections.sort(candidates);
                    resource = repositoryService.getResource(ExecutionContextImpl.getRuntimeExecutionContext(), candidates.get(0));
                }
            }
        }

        return resource;
    }

    protected String toClientResourceType(Class serverResourceType){
        String serverResourceTypeString = "";

        if (serverResourceType.getInterfaces() != null && serverResourceType.getInterfaces().length > 0){
            for (Class interf : serverResourceType.getInterfaces()){
                if (Resource.class.isAssignableFrom(interf)){
                    serverResourceTypeString = interf.getName();
                }
            }
        } else {
            serverResourceTypeString = serverResourceType.getName();
        }

        final ToClientConverter typeProcessor = genericTypeProcessorRegistry.getTypeProcessor(serverResourceTypeString, ToClientConverter.class, false);
        return typeProcessor != null ? typeProcessor.getClientResourceType() : "unknown";
    }
}

