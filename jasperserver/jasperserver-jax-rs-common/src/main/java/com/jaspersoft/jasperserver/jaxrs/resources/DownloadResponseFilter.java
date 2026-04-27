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

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import java.util.List;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@Provider
public class DownloadResponseFilter implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        String download = null;
        final UriInfo uriInfo = requestContext.getUriInfo();
        final List<PathSegment> pathSegments = uriInfo.getPathSegments();
        String downloadName = pathSegments.get(pathSegments.size() - 1).getPath();
        final MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        if(queryParameters != null) {
            download = queryParameters.getFirst("download");
            final String fileNameParam = queryParameters.getFirst("downloadFileName");
            if(fileNameParam != null && !fileNameParam.isEmpty()) {
                downloadName = fileNameParam;
            }
        }
        if("true".equalsIgnoreCase(download) && responseContext.getEntity() != null){
            responseContext.getHeaders().add("Content-Disposition", "attachment; filename=\"" + downloadName + "\"");
        }
    }
}
