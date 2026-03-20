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

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.metadata.common.domain.DataSource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceLookup;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.VirtualReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributesResolver;
import com.jaspersoft.jasperserver.api.search.SearchCriteriaFactory;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static com.jaspersoft.jasperserver.remote.resources.validation.ValidationHelper.empty;

/**
 * <p></p>
 *
 * @author Zakhar.Tomchenco
 * @version $Id$
 */
@Component
public class VirtualDataSourceResourceValidator extends GenericResourceValidator<VirtualReportDataSource> {

    private final Pattern specCharacters = Pattern.compile("^\\d|[\\s\\*\\(\\)\\-\\+=:;\\.\\,\"\\\\/<>\\[\\]'!%]+");
    @jakarta.annotation.Resource(name = "concreteRepository")
    private RepositoryService service;
    @jakarta.annotation.Resource
    private SearchCriteriaFactory searchCriteriaFactory;
    @jakarta.annotation.Resource
    ProfileAttributesResolver profileAttributesResolver;

    @Override
    protected void internalValidate(ExecutionContext ctx, VirtualReportDataSource resource, List<Exception> errors, Map<String, String[]> additionalParameters) {
        if (resource.getDataSourceUriMap() == null || resource.getDataSourceUriMap().isEmpty()) {
            errors.add(new IllegalParameterValueException("A virtual data source should aggregate at least 1 datasource", "SubDataSources", ""));
        } else {
            Set<String> uris = new HashSet<String>();
            String uri;
            for (String id : resource.getDataSourceUriMap().keySet()) {
                if (empty(id)) {
                    errors.add(new IllegalParameterValueException("An id must be specified", "SubDataSource.id", ""));
                } else if (!profileAttributesResolver.containsAttribute(id) && specCharacters.matcher(id).find()) {
                    errors.add(new IllegalParameterValueException("An id should not contain symbols *()-+=:;.,\\\"\\\\/<>[]'! spaces and curly braces", "SubDataSource.id", id));
                } else if (!uris.add((uri = resource.getDataSourceUriMap().get(id).getTargetURI()))) {
                    errors.add(new IllegalParameterValueException("Duplicated URI: " + uri, "SubDataSource.uri", uri));
                } else {
                    if (empty(uri)) {
                        errors.add(new IllegalParameterValueException("Wrong URI: empty", "SubDataSource.uri", ""));
                    } else {
                        if (!uri.startsWith(Folder.SEPARATOR)) {
                            errors.add(new IllegalParameterValueException("Wrong URI: " + uri + ". Must be absolute and start from '/'", "SubDataSource.uri", uri));
                        } else {
                            Resource subDataSource = service.getResource(null, uri);
                            if (subDataSource == null) {
                                errors.add(new IllegalParameterValueException("Wrong URI: " + uri + ". Such resource does not exist.", "SubDataSource.uri", uri));
                            } else if (!(subDataSource instanceof DataSource)) {
                                errors.add(new IllegalParameterValueException("Wrong URI: " + uri + ". This is not a datasource.", "SubDataSource.uri", uri));
                            }
                        }
                    }
                }
            }

            if (errors.isEmpty() && !resource.isNew()) {
                validateUsage(resource);
            }
        }
    }

    private void validateUsage(VirtualReportDataSource resource) {
        Resource rawResource = service.getResource(null, resource.getURIString());
        if (rawResource instanceof VirtualReportDataSource) {
            VirtualReportDataSource existing = (VirtualReportDataSource) rawResource;
            List<ResourceLookup> dependentResources = service.getDependentResources(null, existing.getURIString(), searchCriteriaFactory, 0, 0);
            if (dependentResources != null && !dependentResources.isEmpty()) {
                if (resource.getDataSourceUriMap().keySet().containsAll(existing.getDataSourceUriMap().keySet())) {
                    for (String dsId : existing.getDataSourceUriMap().keySet()) {
                        if (!resource.getDataSourceUriMap().get(dsId).getTargetURI().equals(existing.getDataSourceUriMap().get(dsId).getTargetURI())) {
                            throw new AccessDeniedException("Cannot replace subdatasource " + dsId + ": resource in use. \n" + getDependentUri(dependentResources));
                        }
                    }
                } else {
                    throw new AccessDeniedException("Cannot remove subdatasource: resource in use. \n" + getDependentUri(dependentResources));
                }
            }
        }
    }

    private String getDependentUri(List<ResourceLookup> lookups){
        StringBuilder builder = new StringBuilder("Dependent resources:\n").append(lookups.get(0).getURIString());

        for (int i = 1; i<lookups.size(); i++){
            builder.append(",\n").append(lookups.get(i).getURIString());
        }
        return builder.toString();
    }
}

