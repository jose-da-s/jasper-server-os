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
package com.jaspersoft.jasperserver.search.service.impl;

import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomReportDataSource;
import com.jaspersoft.jasperserver.search.service.ResourceTypeResolver;

/**
 * Created by stas on 3/7/14.
 */
public class CustomReportDataSourceTypeResolver implements ResourceTypeResolver {

    @jakarta.annotation.Resource
    protected RepositoryService repositoryService;

    @Override
    public String getResourceType(Resource resource) {
        if (resource == null) { return null; }

        if (resource instanceof CustomReportDataSource) {
            return getResourceType((CustomReportDataSource) resource);

        } else if (resource.getResourceType().equals(CustomReportDataSource.class.getCanonicalName())) {
            return getResourceType(resource.getURIString());

        } else {
            return resource.getResourceType();
        }
    }

    public String getFileResourceFileType(Resource resource) {
        if (resource.getResourceType().equals(FileResource.class.getCanonicalName())) {
            FileResource fileResource = (FileResource) repositoryService.getResource(null, resource.getURIString());
            return fileResource.getFileType();
        }
        return null;
    }

    private String getResourceType(String uri) {
        if (uri == null) { return null; }

        CustomReportDataSource dataSource = (CustomReportDataSource) repositoryService.getResource(null, uri);
        return (dataSource == null) ? null : getResourceType(dataSource);
    }

    protected String getResourceType(CustomReportDataSource dataSource) {
        return dataSource.getDataSourceName();
    }

    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }
}

