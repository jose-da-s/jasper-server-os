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

import com.jaspersoft.jasperserver.api.metadata.common.domain.RepositoryConfiguration;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.dto.resources.ClientReferenceable;
import com.jaspersoft.jasperserver.remote.services.PermissionsService;
import org.springframework.stereotype.Service;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@Service
public class ResourceReferenceConverterProvider {
    @jakarta.annotation.Resource
    private ResourceConverterProvider resourceConverterProvider;
    @jakarta.annotation.Resource(name = "concreteRepository")
    private RepositoryService repositoryService;
    @jakarta.annotation.Resource(name = "permissionsService")
    private PermissionsService permissionsService;
    @jakarta.annotation.Resource(name = "configurationBean")
    private RepositoryConfiguration configuration;

    // generic type is controlled by corresponding ReferenceClassRestriction. So, cast is safe.
    @SuppressWarnings("unchecked")
    public <T extends ClientReferenceable> ResourceReferenceConverter<T> getConverterForType(Class<T> referenceableClass){
        return new ResourceReferenceConverter(resourceConverterProvider, repositoryService, permissionsService, configuration,
                new ResourceReferenceConverter.ReferenceClassRestriction(referenceableClass));
    }
}

