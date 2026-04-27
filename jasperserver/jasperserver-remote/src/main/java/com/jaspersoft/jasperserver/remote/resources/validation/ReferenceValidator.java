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
package com.jaspersoft.jasperserver.remote.resources.validation;

import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.remote.exception.InvalidReferencedResourceTypeException;
import com.jaspersoft.jasperserver.remote.exception.MandatoryParameterNotFoundException;
import com.jaspersoft.jasperserver.remote.exception.ReferencedResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.resources.converters.ResourceConverterProvider;
import org.springframework.stereotype.Service;

import java.util.Arrays;


/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@Service
public class ReferenceValidator {
    @jakarta.annotation.Resource
    private RepositoryService concreteRepository;
    @jakarta.annotation.Resource
    private ResourceConverterProvider resourceConverterProvider;

    public void validateReference(String uri, String referenceAttributeName, boolean isMandatory, String... expectedTypes) {
        if (uri == null) {
            if (isMandatory) {
                throw new MandatoryParameterNotFoundException(referenceAttributeName);
            }
        } else {
            final Resource resource = concreteRepository.getResource(ExecutionContextImpl.getRuntimeExecutionContext(), uri);
            if (resource == null) {
                throw new ReferencedResourceNotFoundException(uri, referenceAttributeName);
            }
            final String clientResourceType = resourceConverterProvider.getToClientConverter(resource).getClientResourceType();
            if (!Arrays.asList(expectedTypes).contains(clientResourceType)) {
                throw new InvalidReferencedResourceTypeException(clientResourceType, referenceAttributeName, uri, expectedTypes);
            }
        }
    }
}

