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

package com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.resource.service;

import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.jaxrs.common.RestConstants;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.MediaTypes;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.activity.GenericRequest;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.representation.HypermediaRepresentation;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.resource.activity.ReadResourceActivity;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.resource.activity.SearchResourcesActivity;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.resource.representation.ResourceLookupCollectionRepresentation;
import com.jaspersoft.jasperserver.search.mode.AccessType;
import com.jaspersoft.jasperserver.search.mode.SearchMode;
import com.jaspersoft.jasperserver.search.service.RepositorySearchCriteria;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;

/**
 * @author: Igor.Nesterenko
 * @version: $Id$
 */

@Component
@Path("/hypermedia/resources")
@Scope("prototype")
public class HypermediaResourceLookupService {

    private static final Boolean DEFAULT_RECURSIVE = false;
    private static final Boolean DEFAULT_SHOW_HIDDEN = false;
    public static Integer DEFAULT_OFFSET = 0;
    public static Integer DEFAULT_LIMIT = 100;

    @Resource
    private SearchResourcesActivity searchResourceLookupActivity;

    @Resource
    private SearchResourcesActivity favoritesLookupActivity;

    @Resource
    private ReadResourceActivity resourceLookupActivity;

    @Context
    private UriInfo uriInfo;

    public static RepositorySearchCriteria convertToSearchCriteria(RepositorySearchCriteria criteria, MultivaluedMap<String, String> queryParameters){


        for (String paramName : queryParameters.keySet()) {
            List<String> values = queryParameters.get(paramName);
            String value = values != null ? values.get(0) : null;

            if (RestConstants.QUERY_PARAM_SEARCH_QUERY.equals(paramName)){
                criteria.setSearchText(value);
            }
            if ("folderUri".equals(paramName)){
                criteria.setFolderUri(value);
            }
            if("type".equals(paramName)){
                criteria.setResourceTypes(values);
            }
            if("favorites".equals(paramName)){
                criteria.setFavorites(Boolean.valueOf(value));
            }
            if("accessType".equals(paramName)){
                AccessType accessType =  AccessType.ALL;
                if (value != null && !"".equals(value)){
                    if (AccessType.MODIFIED.name().equalsIgnoreCase(value)){
                        accessType = AccessType.MODIFIED;
                    }
                    if (AccessType.VIEWED.name().equalsIgnoreCase(value)){
                        accessType = AccessType.VIEWED;
                    }
                }

                criteria.setAccessType(accessType);
            }
            if (RestConstants.QUERY_PARAM_OFFSET.equals(paramName)){
                Integer startIndex = value == null ? DEFAULT_OFFSET : Integer.parseInt(value);
                criteria.setStartIndex(startIndex);
            }
            if(RestConstants.QUERY_PARAM_LIMIT.equals(paramName)){
                Integer maxCount = value == null ? DEFAULT_LIMIT : Integer.parseInt(value);
                criteria.setMaxCount(maxCount);
            }
            if(RestConstants.QUERY_PARAM_SORT_BY.equals(paramName)){
                criteria.setSortBy(value);
            }
            criteria.setSearchMode(SearchMode.SEARCH);
            if("recursive".equals(paramName)){
                Boolean recursively = value == null ? DEFAULT_RECURSIVE : Boolean.parseBoolean(value);
                criteria.setSearchMode(recursively ? SearchMode.BROWSE : SearchMode.SEARCH);
            }
            if("showHiddenItems".equals(paramName)){
                boolean showHidden = value == null ? DEFAULT_SHOW_HIDDEN  : Boolean.parseBoolean(value);
                criteria.setShowHidden(showHidden);
            }

            if("forceFullPage".equals(paramName)){
                boolean showHidden = value == null ? true  : Boolean.parseBoolean(value);
                criteria.setForceFullPage(showHidden);
            }
            if("relativeUriToExclude".equals(paramName)){
                criteria.setExcludeRelativePaths(values);
            }
        }

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User){
            User user = (User) principal;
            criteria.setUser(user);
        }

        return  criteria;
    }

    @GET
    @Path("/{uri}")
    @Produces({MediaTypes.APPLICATION_HAL_JSON})
    public HypermediaRepresentation getResourceLookup(@PathParam("uri") String uri){

        resourceLookupActivity.setGenericRequest(new GenericRequest()
                .addParam("uri", uri)
                .setExpanded(true)
        );

        return  (HypermediaRepresentation)resourceLookupActivity.proceed();
    }

    @GET
    @Produces({MediaTypes.APPLICATION_HAL_JSON})
    public Response getResourceLookupRepresentation() {
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        List<String> favValue = queryParams.get("favorites");
        if (favValue != null && favValue.get(0).equals("true"))
            searchResourceLookupActivity = favoritesLookupActivity;

        RepositorySearchCriteria searchCriteria = convertToSearchCriteria(searchResourceLookupActivity.getCriteria(), uriInfo.getQueryParameters());

        searchResourceLookupActivity.setCriteria(searchCriteria);

        ResourceLookupCollectionRepresentation representation = (ResourceLookupCollectionRepresentation) searchResourceLookupActivity.proceed();

        return Response.ok(representation).build();
    }


}

