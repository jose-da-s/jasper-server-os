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
package com.jaspersoft.jasperserver.jaxrs.permission;

import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InternalURI;
import com.jaspersoft.jasperserver.api.metadata.common.domain.PermissionUriProtocol;
import com.jaspersoft.jasperserver.api.metadata.user.domain.ObjectPermission;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.InternalURIDefinition;
import com.jaspersoft.jasperserver.dto.permissions.RepositoryPermission;
import com.jaspersoft.jasperserver.dto.permissions.RepositoryPermissionListWrapper;
import com.jaspersoft.jasperserver.jaxrs.common.RestConstants;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.remote.exception.ResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.helpers.RecipientIdentity;
import com.jaspersoft.jasperserver.remote.helpers.RecipientIdentityResolver;
import com.jaspersoft.jasperserver.remote.resources.converters.PermissionConverter;
import com.jaspersoft.jasperserver.remote.services.PermissionsService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

import static com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl.getRuntimeExecutionContext;

/**
 * @author Zakhar.Tomchenco
 * @version $Id$
 */
@Component
@Path("/permissions")
@Scope("prototype")
public class RepositoryPermissionsJaxrsService {

    @Resource(name = "concretePermissionsService")
    private PermissionsService service;

    @Resource(name = "concretePermissionsRecipientIdentityResolver")
    private RecipientIdentityResolver permissionRecipientIdentityResolver;

    @Resource(name = "permissionConverter")
    private PermissionConverter converter;

    @GET
    @Path("/{uri:.+}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getPermissionsEntryPoint(@Context UriInfo uriInfo) throws ErrorDescriptorException {
        PermissionComplexKey permissionComplexKey = new PermissionComplexKey(uriInfo).invoke();
        MultivaluedMap<String, String> queryParams = permissionComplexKey.getQueryParams();

        Response response;
        if (permissionComplexKey.getRecipientUri() == null){
            response = getPermissions(permissionComplexKey.getResourceUri(),
                    "true".equalsIgnoreCase(queryParams.getFirst("effectivePermissions")),
                    queryParams.getFirst("recipientType"),
                    queryParams.getFirst("recipientId"),
                    "true".equalsIgnoreCase(queryParams.getFirst("resolveAll")),
                    parseIntParam(RestConstants.QUERY_PARAM_OFFSET, queryParams.getFirst(RestConstants.QUERY_PARAM_OFFSET)),
                    parseIntParam(RestConstants.QUERY_PARAM_LIMIT, queryParams.getFirst(RestConstants.QUERY_PARAM_LIMIT)));
        } else {
            response = getPermission(permissionComplexKey.getResourceUri(), permissionComplexKey.getRecipientUri());
        }
        return response;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getPermissionsEntryPointRoot(@Context UriInfo uriInfo) throws ErrorDescriptorException {
        return getPermissionsEntryPoint(uriInfo);
    }

    @POST
    @Consumes({"application/collection+xml", "application/collection+json"})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createPermissions(RepositoryPermissionListWrapper data) throws ErrorDescriptorException {
        List<RepositoryPermission> permissions = data.getPermissions();
        List<ObjectPermission> server = new ArrayList<ObjectPermission>(permissions.size());
        for (RepositoryPermission permission : permissions){
            server.add(converter.toServer(getRuntimeExecutionContext(), permission, null));
        }

        service.createPermissions(server);
        permissions.clear();
        for (ObjectPermission permission : server){
            permissions.add(converter.toClient(permission, null));
        }

        return Response.status(Response.Status.CREATED).entity(new RepositoryPermissionListWrapper(permissions)).build();
    }

    @DELETE
    @Path("/{uri:.+}")
    public Response deletePermissionsEntryPoint(@Context UriInfo uriInfo) throws ErrorDescriptorException {
        PermissionComplexKey permissionComplexKey = new PermissionComplexKey(uriInfo).invoke();

        if (permissionComplexKey.getRecipientUri() == null){
            deletePermissions(permissionComplexKey.getResourceUri());
        } else {
            deletePermission(permissionComplexKey.getResourceUri(), permissionComplexKey.getRecipientUri());
        }
        return Response.noContent().build();
    }

    @DELETE
    public Response deletePermissionsEntryPointRoot(@Context UriInfo uriInfo) throws ErrorDescriptorException {
        return deletePermissionsEntryPoint(uriInfo);
    }

    @PUT
    @Path("/{uri:.+}")
    @Consumes({"application/collection+xml", "application/collection+json"})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updatePermissions(RepositoryPermissionListWrapper data,
            @PathParam("uri") String resourceUri) throws ErrorDescriptorException {
        List<RepositoryPermission> permissions = data.getPermissions();
        List<ObjectPermission> server = new ArrayList<ObjectPermission>(permissions.size());
        for (RepositoryPermission permission : permissions){
            server.add(converter.toServer(getRuntimeExecutionContext()
                    , permission.setUri(resourceUri), null));
        }
        InternalURI internalURI = new InternalURIDefinition(Folder.SEPARATOR + resourceUri, PermissionUriProtocol.RESOURCE);
        service.putPermissions(internalURI, server);
        permissions.clear();
        for (ObjectPermission permission : server){
            permissions.add(converter.toClient(permission, null));
        }

        return Response.ok().entity(new RepositoryPermissionListWrapper(permissions)).build();
    }

    @PUT
    @Consumes({"application/collection+xml", "application/collection+json"})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updatePermissionsRoot(RepositoryPermissionListWrapper data) throws ErrorDescriptorException {
        return updatePermissions(data, "");
    }

    @PUT
    @Path("/{uri:.+}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updatePermission(RepositoryPermission permission,
            @Context UriInfo uriInfo) throws ErrorDescriptorException {

        PermissionComplexKey permissionComplexKey = new PermissionComplexKey(uriInfo).invoke();
        permission.setUri(permissionComplexKey.getResourceUri());
        permission.setRecipient(permissionComplexKey.getRecipientUri());

        service.putPermission(converter.toServer(getRuntimeExecutionContext(), permission, null));

        return Response.ok().entity(permission).build();
    }

    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updatePermissionRoot(RepositoryPermission permission,
            @Context UriInfo uriInfo) throws ErrorDescriptorException {
        return updatePermission(permission, uriInfo);
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createPermission(RepositoryPermission permission) throws ErrorDescriptorException {
        service.createPermission(converter.toServer(getRuntimeExecutionContext(), permission, null));
        return Response.status(Response.Status.CREATED).entity(permission).build();
    }

    private Response getPermission(String resourceUri, String recipientUri) throws ErrorDescriptorException {
        RecipientIdentity identity = permissionRecipientIdentityResolver.toIdentity(recipientUri);
        ObjectPermission permission =  service.getPermission(resourceUri,identity.getRecipientClass(), identity.getId());

        if (permission == null){
            throw new ResourceNotFoundException(resourceUri+";"+ recipientUri);
        }

        return Response.ok(converter.toClient(permission, null)).build();
    }

    private Response getPermissions(String resourceUri, boolean effectivePermissions, String recipientType, String recipientId, boolean resolveAll, int startIndex, int limit) throws ErrorDescriptorException {
        Class<?> recipientClass = permissionRecipientIdentityResolver.getClassForProtocol(recipientType);
        List<ObjectPermission> permissions = service.getPermissions(resourceUri, recipientClass, recipientId, effectivePermissions, resolveAll);
        int totalCount = permissions.size();

        if (totalCount < startIndex) {
            permissions.clear();
        } else {
            if (limit > 0) {
                if (startIndex + limit > totalCount) {
                    permissions = permissions.subList(startIndex, totalCount);
                } else {
                    permissions = permissions.subList(startIndex, startIndex + limit);
                }
            } else {
                if (startIndex > 0){
                    permissions = permissions.subList(startIndex, totalCount);
                }
            }
        }

        List<RepositoryPermission> client = new ArrayList<RepositoryPermission>(permissions.size());
        for (ObjectPermission permission : permissions) {
            client.add(converter.toClient(permission, null));
        }

        Response response;
        if (permissions.size() == 0) {
            response = Response.status(Response.Status.NO_CONTENT)
                    .header(RestConstants.HEADER_START_INDEX, startIndex)
                    .header(RestConstants.HEADER_RESULT_COUNT, permissions.size())
                    .header(RestConstants.HEADER_TOTAL_COUNT, totalCount)
                    .build();
        } else {
            response = Response.ok()
                    .entity(new RepositoryPermissionListWrapper(client))
                    .header(RestConstants.HEADER_START_INDEX, startIndex)
                    .header(RestConstants.HEADER_RESULT_COUNT, permissions.size())
                    .header(RestConstants.HEADER_TOTAL_COUNT, totalCount)
                    .build();
        }
        return response;
    }

    private void deletePermissions(String resourceUri) throws ErrorDescriptorException {
        List<ObjectPermission> permissions = service.getPermissions(resourceUri, null, null, false, false);

        for (ObjectPermission permission : permissions){
            service.deletePermission(permission);
        }
    }

    private void deletePermission(String resourceUri, String recipientUri) throws ErrorDescriptorException {
        RecipientIdentity identity = permissionRecipientIdentityResolver.toIdentity(recipientUri);
        ObjectPermission permission =  service.getPermission(resourceUri,identity.getRecipientClass(), identity.getId());

        if (permission == null) {
            throw new ResourceNotFoundException(resourceUri + ";" + recipientUri);
        }
        service.deletePermission(permission);
    }

    private int parseIntParam(String name, String value) throws IllegalParameterValueException {
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (Exception e) {
                throw new IllegalParameterValueException(name, value);
            }
        }
        return 0;
    }

    protected class PermissionComplexKey {
        private UriInfo uriInfo;
        private MultivaluedMap<String, String> queryParams;
        private String resourceUri;
        private String recipientUri;

        public PermissionComplexKey(UriInfo uriInfo) {
            this.uriInfo = uriInfo;
        }

        public MultivaluedMap<String, String> getQueryParams() {
            return queryParams;
        }

        public String getResourceUri() {
            return resourceUri;
        }

        public String getRecipientUri() {
            return recipientUri;
        }

        public PermissionComplexKey invoke() {
            List<PathSegment> segments = uriInfo.getPathSegments();
            queryParams = uriInfo.getQueryParameters(true);
            if (queryParams == null){
                queryParams = new MultivaluedHashMap<String, String>();
            }

            StringBuilder resourceUri = new StringBuilder();
            recipientUri = null;
            if (segments.size() > 1){
                for (int i = 1; i < segments.size() && recipientUri == null; i++) {
                    resourceUri.append(Folder.SEPARATOR).append(segments.get(i).getPath());
                    this.recipientUri = segments.get(i).getMatrixParameters().getFirst("recipient");
                }
                this.resourceUri = resourceUri.toString();
            } else {
                this.resourceUri = Folder.SEPARATOR;
                this.recipientUri = segments.get(0).getMatrixParameters().getFirst("recipient");
            }

            return this;
        }
    }
}

