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
package com.jaspersoft.jasperserver.jaxrs.authority;

import com.jaspersoft.jasperserver.api.metadata.user.domain.Tenant;
import com.jaspersoft.jasperserver.api.metadata.user.service.AttributesSearchCriteria;
import com.jaspersoft.jasperserver.api.metadata.user.service.TenantService;
import com.jaspersoft.jasperserver.dto.authority.ClientAttribute;
import com.jaspersoft.jasperserver.dto.authority.hypermedia.HypermediaAttributesListWrapper;
import com.jaspersoft.jasperserver.jaxrs.common.RestConstants;
import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.remote.helpers.RecipientIdentity;
import com.jaspersoft.jasperserver.remote.resources.converters.HypermediaOptions;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Providers;
import java.io.InputStream;
import java.util.Set;

/**
 * @author Volodya Sabadosh
 * @version $Id$
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Path("/attributes")
@Scope("prototype")
public class ServerAttributesJaxrsService {
    @Resource(name = "attributesJaxrsService")
    private AttributesJaxrsService attributesJaxrsService;

    @Context
    private Providers providers;

    @Context
    private HttpHeaders httpHeaders;

    @GET
    @Produces({"application/attributes.collection+xml", "application/attributes.collection+json",
            "application/attributes.collection.hal+json", "application/attributes.collection.hal+xml"})
    public Response getAttributes(@QueryParam("name") Set<String> attrNames,
                                  @QueryParam("group") Set<String> groups,
                                  @QueryParam(RestConstants.QUERY_PARAM_OFFSET) Integer startIndex,
                                  @QueryParam(RestConstants.QUERY_PARAM_LIMIT) Integer limit,
                                  @QueryParam("recursive") Boolean recursive,
                                  @QueryParam("holder") String holder,
                                  @QueryParam("_embedded") String embedded,
                                  @HeaderParam(HttpHeaders.ACCEPT) String accept,
                                  @QueryParam("includeInherited") Boolean effective) throws ErrorDescriptorException {
        AttributesSearchCriteria searchCriteria = new AttributesSearchCriteria.Builder()
                .setHolder(holder)
                .setStartIndex(startIndex == null ? 0 : startIndex)
                .setMaxRecords(limit == null ? 0 : limit)
                .setNames(attrNames)
                .setGroups(groups)
                .setRecursive(Boolean.TRUE == recursive)
                .setEffective(Boolean.TRUE == effective)
                .build();
        HypermediaOptions hypermediaOptions = attributesJaxrsService.getHypermediaOptions(accept, embedded);

        return attributesJaxrsService.getAttributes(searchCriteria, hypermediaOptions);
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/hal+json", "application/hal+xml"})
    public Response getAttributes(@QueryParam("name") Set<String> attrNames,
                                  @QueryParam("_embedded") String embedded,
                                  @HeaderParam(HttpHeaders.ACCEPT) String accept) throws ErrorDescriptorException {
        HypermediaOptions hypermediaOptions = attributesJaxrsService.getHypermediaOptions(accept, embedded);
        return attributesJaxrsService.getAttributesOfRecipient(getHolder(), attrNames, hypermediaOptions);
    }

    @PUT
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/hal+json", "application/hal+xml"})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/hal+json", "application/hal+xml"})
    public Response putAttributes(HypermediaAttributesListWrapper newCollection,
                                  @QueryParam("name") Set<String> attrNames,
                                  @HeaderParam(HttpHeaders.CONTENT_TYPE) String mediaType,
                                  @HeaderParam(HttpHeaders.ACCEPT) String accept,
                                  @QueryParam("_embedded") String embedded) throws ErrorDescriptorException {
        HypermediaOptions hypermediaOptions = attributesJaxrsService.getHypermediaOptions(accept, embedded);
        return attributesJaxrsService.putAttributes(newCollection.getProfileAttributes(), getHolder(), attrNames, hypermediaOptions, mediaType);
    }

    @DELETE
    public Response deleteAttributes(@QueryParam("name") Set<String> attrNames) throws ErrorDescriptorException {
        return attributesJaxrsService.deleteAttributes(getHolder(), attrNames);
    }

    @GET
    @Path("/{attrName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/hal+json", "application/hal+xml"})
    public Response getSpecificAttribute(@PathParam("attrName") String attrName,
                                         @HeaderParam(HttpHeaders.ACCEPT) String accept,
                                         @QueryParam("_embedded") String embedded) throws ErrorDescriptorException {
        HypermediaOptions hypermediaOptions = attributesJaxrsService.getHypermediaOptions(accept, embedded);
        return attributesJaxrsService.getSpecificAttributeOfRecipient(getHolder(), attrName, hypermediaOptions);
    }

    @PUT
    @Path("/{attrName}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/hal+xml", "application/hal+json"})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/hal+xml","application/hal+json"})
    public Response putAttribute(InputStream stream,
                                 @PathParam("attrName") String attrName,
                                 @HeaderParam(HttpHeaders.ACCEPT) String accept,
                                 @HeaderParam(HttpHeaders.CONTENT_TYPE)MediaType mediaType,
                                 @QueryParam("_embedded") String embedded) throws ErrorDescriptorException {
        HypermediaOptions hypermediaOptions = attributesJaxrsService.getHypermediaOptions(accept, embedded);
        ClientAttribute clientAttribute = AttributesJaxrsService.parseEntity(stream, mediaType, providers, httpHeaders);
        return attributesJaxrsService.putAttribute(clientAttribute, getHolder(), attrName, hypermediaOptions, mediaType.toString());
    }

    @DELETE
    @Path("/{attrName}")
    public Response deleteAttribute(@PathParam("attrName") String attrName) throws ErrorDescriptorException {
        return attributesJaxrsService.deleteAttribute(getHolder(), attrName);
    }

    public RecipientIdentity getHolder() {
        return new RecipientIdentity(Tenant.class, TenantService.ORGANIZATIONS);
    }

}

