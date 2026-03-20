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

import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.dto.resources.ClientResource;
import com.jaspersoft.jasperserver.dto.resources.ClientResourceListWrapper;
import com.jaspersoft.jasperserver.dto.resources.ClientResourceLookup;
import com.jaspersoft.jasperserver.dto.resources.ResourceMediaType;
import com.jaspersoft.jasperserver.jaxrs.common.RestConstants;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.exception.MandatoryParameterNotFoundException;
import com.jaspersoft.jasperserver.remote.resources.ClientTypeHelper;
import com.jaspersoft.jasperserver.remote.resources.converters.ResourceConverterProvider;
import com.jaspersoft.jasperserver.remote.services.BatchRepositoryService;
import com.jaspersoft.jasperserver.remote.services.SingleRepositoryService;
import com.jaspersoft.jasperserver.search.mode.AccessType;
import com.jaspersoft.jasperserver.search.service.RepositorySearchResult;
import org.apache.commons.lang.CharEncoding;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.message.internal.MediaTypes;
import org.glassfish.jersey.server.ContainerRequest;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p></p>
 *
 * @author Zakhar.Tomchenco
 * @version $Id$
 */
@Service
@Path("/resources")
@Scope("prototype")
public class RepositoryJaxrsService {

    @Resource
    protected BatchRepositoryService batchRepositoryService;
    @Resource
    protected ResourceDetailsJaxrsService resourceDetailsJaxrsService;
    @Resource
    private SingleRepositoryService singleRepositoryService;
    @Resource
    private ResourceConverterProvider resourceConverterProvider;
    @Context
    private Providers providers;
    @Context
    private HttpHeaders httpHeaders;
    @Context
    private ContainerRequest request;

    private static Logger log = LogManager.getLogger(RepositoryJaxrsService.class);

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, ResourceMediaType.FOLDER_XML, ResourceMediaType.FOLDER_JSON})
    public Response getResources(
            @QueryParam(RestConstants.QUERY_PARAM_SEARCH_QUERY) String q,
            @QueryParam("folderUri") String folderUri,
            @QueryParam("type") List<String> type,
            @QueryParam("excludeType") List<String> excludeType,
            @QueryParam("excludeFolder") List<String> excludeFolders,
            @QueryParam("accessType") String accessTypeString,
            @QueryParam("favorites") @DefaultValue("false")Boolean favorites,
            @QueryParam(RestConstants.QUERY_PARAM_OFFSET) Integer start,
            @QueryParam(RestConstants.QUERY_PARAM_LIMIT) Integer limit,
            @QueryParam("recursive") @DefaultValue("true") Boolean recursive,
            @QueryParam("showHiddenItems") @DefaultValue("false") Boolean showHiddenItems,
            @QueryParam("forceTotalCount") @DefaultValue("false") Boolean forceTotalCount,
            @QueryParam(RestConstants.QUERY_PARAM_SORT_BY) String sortBy,
            @QueryParam(RestConstants.QUERY_PARAM_EXPANDED) Boolean expanded,
            @QueryParam(RestConstants.QUERY_PARAM_EXPAND_TYPE) Set<String> expandTypes,
            @QueryParam("forceFullPage") @DefaultValue("false") Boolean forceFullPage,
            @HeaderParam(HttpHeaders.ACCEPT)String accept,
            @Context final HttpServletRequest httpServletRequest) throws ErrorDescriptorException, IOException {

        Response res;
        if (ResourceMediaType.FOLDER_JSON.equals(accept) || ResourceMediaType.FOLDER_XML.equals(accept)) {
            res = getResourceDetails("", accept, expanded, expandTypes, null, httpServletRequest);
        } else {
            AccessType accessType =  AccessType.ALL;
            if (accessTypeString != null && !"".equals(accessTypeString)){
                if (AccessType.MODIFIED.name().equalsIgnoreCase(accessTypeString)){
                    accessType = AccessType.MODIFIED;
                }
                if (AccessType.VIEWED.name().equalsIgnoreCase(accessTypeString)){
                    accessType = AccessType.VIEWED;
                }
            }
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            RepositorySearchResult<ClientResourceLookup> result =
                    batchRepositoryService.getResources(q, folderUri, type, excludeType, null, excludeFolders,
                            start, limit,
                            recursive, showHiddenItems,
                            sortBy, accessType, favorites, user,
                            forceFullPage);

            final int iStart = result.getClientOffset();
            final int iLimit = result.getClientLimit();
            Response.ResponseBuilder response;

            int realResultSize = result.size();
            boolean isForceTotalCount = (forceTotalCount != null && forceTotalCount);

            if (realResultSize != 0) {
                response = Response.status(Response.Status.OK).entity(new ClientResourceListWrapper(result.getItems()));

                response.header(RestConstants.HEADER_START_INDEX, iStart)
                        .header(RestConstants.HEADER_RESULT_COUNT, realResultSize);

                int totalCount = realResultSize;
                if (isForceTotalCount || iStart == 0 || forceFullPage) {
                    totalCount = forceFullPage
                            ? (((realResultSize < iLimit) && !isForceTotalCount) ? realResultSize : result.getTotalCount())
                            : batchRepositoryService.getResourcesCount(q, folderUri, type, excludeType, excludeFolders, recursive, showHiddenItems, accessType, user);
                }

                if (iStart == 0 || isForceTotalCount || iLimit == 0 || forceFullPage) {
                    response.header(RestConstants.HEADER_TOTAL_COUNT, totalCount);
                }

                if (forceFullPage && (result.getNextOffset() < result.getTotalCount())) {
                    response.header(RestConstants.HEADER_NEXT_OFFSET, result.getNextOffset());
                }
            } else {
                response = Response.status(Response.Status.NO_CONTENT);
                response.header(RestConstants.HEADER_TOTAL_COUNT, 0);
            }

            res = response.build();
        }
        return res;
    }

    @DELETE
    public Response deleteResources(@QueryParam("resourceUri") List<String> uris) throws ErrorDescriptorException {
        if (uris == null || uris.isEmpty()) {
            //no URI is specified as a parameter. It means delete request on root resource.
            // Call single delete logic. And let it forbid such a request.
            return deleteResource("");
        }
        batchRepositoryService.deleteResources(uris);
        return Response.noContent().build();
    }

    @GET
    @Path("/{uri: .+}")
    public Response getResourceDetails(@PathParam(ResourceDetailsJaxrsService.PATH_PARAM_URI)String uri, 
            @HeaderParam(HttpHeaders.ACCEPT)String accept,
            @QueryParam(RestConstants.QUERY_PARAM_EXPANDED) Boolean expanded,
            @QueryParam(RestConstants.QUERY_PARAM_EXPAND_TYPE) Set<String> expandTypes,
            @QueryParam(RestConstants.QUERY_PARAM_INCLUDE) List<String> includes,
            @Context final HttpServletRequest request) throws ErrorDescriptorException {
       return resourceDetailsJaxrsService.getResourceDetails(Folder.SEPARATOR + uri.replaceAll("/$", ""), accept,
               expanded, expandTypes, includes, request.getParameterMap(), request);
    }

    @DELETE
    @Path("/{uri: .+}")
    public Response deleteResource(@PathParam(ResourceDetailsJaxrsService.PATH_PARAM_URI) String uri) throws ErrorDescriptorException {
        return resourceDetailsJaxrsService.deleteResource(Folder.SEPARATOR + uri.replaceAll("/$", ""));
    }

    @POST
    @Path("/{uri: .+}")
    public Response defaultPostHandler(
            @PathParam(ResourceDetailsJaxrsService.PATH_PARAM_URI) String _uri,
            @HeaderParam(HttpHeaders.CONTENT_LOCATION) String sourceUri,
            @HeaderParam("Content-Disposition")String disposition,
            @HeaderParam("Content-Description")String description,
            @HeaderParam(HttpHeaders.CONTENT_TYPE)MediaType mediaType,
            @HeaderParam(HttpHeaders.ACCEPT)String accept,
            @QueryParam(RestConstants.QUERY_PARAM_EXPANDED)@DefaultValue("false")Boolean expanded,
            @QueryParam(RestConstants.QUERY_PARAM_EXPAND_TYPE) Set<String> expandTypes,
            @QueryParam(RestConstants.QUERY_PARAM_CREATE_FOLDERS)@DefaultValue("true")Boolean createFolders,
            @QueryParam(RestConstants.QUERY_PARAM_DRY_RUN)@DefaultValue("false")Boolean dryRun,
            @QueryParam("overwrite")@DefaultValue("false")Boolean overwrite,
            @QueryParam("renameTo") String renameTo,
            @Context final HttpServletRequest httpServletRequest) throws ErrorDescriptorException, IOException {
        Response response = null;
        String uri = Folder.SEPARATOR + _uri.replaceAll("/$", "");
        sourceUri = decodeHeaderContentLocation(sourceUri);
        final Map<String, String[]> parameterMap = httpServletRequest.getParameterMap();
        if(mediaType != null && MediaTypes.typeEqual(mediaType, MediaType.MULTIPART_FORM_DATA_TYPE)){
            final ClientResource result = resourceDetailsJaxrsService.createResourceViaForm(
                    request.readEntity(FormDataMultiPart.class),
                    uri,
                    createFolders, accept, dryRun, parameterMap);
            response =  Response.status(Response.Status.CREATED).entity(result).build();
        } else {
            InputStream stream = request.readEntity(InputStream.class);
            ClientResource resourceLookup = null;
            try {
                resourceLookup = parseEntity(stream, mediaType);
            } catch (IllegalParameterValueException e) {
                // wrong media type for resource creation request, let's try default post handler
                response = resourceDetailsJaxrsService.defaultPostHandler(stream, uri, sourceUri,
                        disposition, description, mediaType != null ? mediaType.toString() : null, accept, createFolders,
                        overwrite, renameTo, dryRun, parameterMap, httpServletRequest);
            }
            if (response == null) {
                final ClientResource createdResource = resourceDetailsJaxrsService
                        .createResource(resourceLookup, uri, createFolders, dryRun, accept, parameterMap);

                if (expanded != null && expanded) {
                    response = Response.fromResponse(resourceDetailsJaxrsService
                            .getResourceDetails(createdResource.getUri(), accept, true, expandTypes,  null,
                                    parameterMap, httpServletRequest))
                            .status(Response.Status.CREATED).build();
                } else {
                    response = Response.status(Response.Status.CREATED).entity(createdResource).build();
                }
            }
        }
        return response;
    }

    @POST
    public Response defaultPostHandlerForRoot(
            @HeaderParam(HttpHeaders.CONTENT_LOCATION) String sourceUri,
            @HeaderParam("Content-Disposition")String disposition,
            @HeaderParam("Content-Description")String description,
            @HeaderParam(HttpHeaders.CONTENT_TYPE)MediaType rawMimeType,
            @HeaderParam(HttpHeaders.ACCEPT)String accept,
            @QueryParam(RestConstants.QUERY_PARAM_EXPANDED)@DefaultValue("false")Boolean expanded,
            @QueryParam(RestConstants.QUERY_PARAM_EXPAND_TYPE) Set<String> expandTypes,
            @QueryParam(RestConstants.QUERY_PARAM_CREATE_FOLDERS)@DefaultValue("true")Boolean createFolders,
            @QueryParam(RestConstants.QUERY_PARAM_DRY_RUN)@DefaultValue("false")Boolean dryRun,
            @QueryParam("overwrite")@DefaultValue("false")Boolean overwrite,
            @QueryParam("renameTo") String renameTo,
            @Context final HttpServletRequest httpServletRequest) throws ErrorDescriptorException, IOException{
        return defaultPostHandler("", sourceUri,disposition, description, rawMimeType, accept, expanded, expandTypes,
                createFolders, dryRun, overwrite, renameTo, httpServletRequest);
    }

    /**
     *
     * @param entityStream - entity input stream
     * @param mediaType - the entity media type
     * @return client entity
     * @throws IllegalParameterValueException if media type string doesn't correspond to repository resource media type pattern.
     */
    // As far as clientTypeClass represents <? extents ClientResource> cast to ClientResource is safe.
    @SuppressWarnings("unchecked")
    protected ClientResource parseEntity(InputStream entityStream, MediaType mediaType) throws IllegalParameterValueException {
        final String clientType = ClientTypeHelper.extractClientType(mediaType);
        if(clientType == null){
            throw new IllegalParameterValueException("resource Media-Type", mediaType != null ? mediaType.toString() : "null");
        }
        final Class<? extends ClientResource> clientTypeClass = resourceConverterProvider.getClientTypeClass(clientType);
        // code below comes from com.sun.jersey.multipart.BodyPart#getEntityAs(Class<T> clazz)
        Annotation annotations[] = new Annotation[0];
        MessageBodyReader reader =
                providers.getMessageBodyReader(clientTypeClass, clientTypeClass, annotations, mediaType);
        if (reader == null) {
            throw new IllegalArgumentException("No available MessageBodyReader for class " + clientTypeClass.getName() + " and media type " + mediaType);
        }
        try {
            return (ClientResource) reader.readFrom(clientTypeClass, clientTypeClass, annotations, mediaType, httpHeaders.getRequestHeaders(),
                    entityStream);
        } catch (IOException e) {
            throw new JSExceptionWrapper(e);
        }
    }

    @PUT
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response defaultPutHandlerForRoot(
            @HeaderParam(HttpHeaders.CONTENT_LOCATION) String sourceUri,
            @HeaderParam("Content-Disposition")String disposition,
            @HeaderParam("Content-Description")String description,
            @HeaderParam(HttpHeaders.CONTENT_TYPE)MediaType mediaType,
            @HeaderParam(HttpHeaders.ACCEPT)String accept,
            @QueryParam(RestConstants.QUERY_PARAM_EXPANDED)@DefaultValue("false")Boolean expanded,
            @QueryParam(RestConstants.QUERY_PARAM_EXPAND_TYPE) Set<String> expandTypes,
            @QueryParam(RestConstants.QUERY_PARAM_CREATE_FOLDERS)@DefaultValue("true")Boolean createFolders,
            @QueryParam(RestConstants.QUERY_PARAM_DRY_RUN)@DefaultValue("false")Boolean dryRun,
            @QueryParam("overwrite")@DefaultValue("false")Boolean overwrite,
            @QueryParam("renameTo") String renameTo,
            @Context final HttpServletRequest httpServletRequest) throws ErrorDescriptorException, IOException {
        return defaultPutHandler("", sourceUri, disposition, description, mediaType, accept, expanded, expandTypes,
                createFolders, dryRun, overwrite, renameTo, httpServletRequest);
    }

    @PUT
    @Path("/{uri: .+}")
    public Response defaultPutHandler(
            @PathParam(ResourceDetailsJaxrsService.PATH_PARAM_URI) String _uri,
            @HeaderParam(HttpHeaders.CONTENT_LOCATION) String sourceUri,
            @HeaderParam("Content-Disposition")String disposition,
            @HeaderParam("Content-Description")String description,
            @HeaderParam(HttpHeaders.CONTENT_TYPE)MediaType mediaType,
            @HeaderParam(HttpHeaders.ACCEPT)String accept,
            @QueryParam(RestConstants.QUERY_PARAM_EXPANDED)@DefaultValue("false")Boolean expanded,
            @QueryParam(RestConstants.QUERY_PARAM_EXPAND_TYPE) Set<String> expandTypes,
            @QueryParam(RestConstants.QUERY_PARAM_CREATE_FOLDERS)@DefaultValue("true")Boolean createFolders,
            @QueryParam(RestConstants.QUERY_PARAM_DRY_RUN)@DefaultValue("false")Boolean dryRun,
            @QueryParam("overwrite")@DefaultValue("false")Boolean overwrite,
            @QueryParam("renameTo") String renameTo,
            @Context final HttpServletRequest httpServletRequest) throws ErrorDescriptorException, IOException {
        String uri = Folder.SEPARATOR + _uri.replaceAll("/$", "");
        sourceUri = decodeHeaderContentLocation(sourceUri);
        ClientResource resourceLookup = null;
        Response response = null;
        final Map<String, String[]> parameterMap = httpServletRequest.getParameterMap();
        if(mediaType != null && MediaTypes.typeEqual(mediaType, MediaType.MULTIPART_FORM_DATA_TYPE)){
            response = resourceDetailsJaxrsService.updateResourceViaForm(request.readEntity(FormDataMultiPart.class),
                    uri, createFolders, accept, dryRun, parameterMap);
        } else {
            InputStream stream = request.readEntity(InputStream.class);
            try {

                resourceLookup = parseEntity(stream, mediaType);
            } catch (IllegalParameterValueException e) {
                // wrong media type for resource creation request, let's try default put handler
                response = resourceDetailsJaxrsService.defaultPutHandler(stream, uri, sourceUri,
                        disposition, description, mediaType != null ? mediaType.toString() : null, accept, createFolders,
                        overwrite, renameTo, dryRun, parameterMap, httpServletRequest);
            }
            if (response == null) {
                if (resourceLookup == null) {
                    throw new MandatoryParameterNotFoundException("resource body");
                }
                resourceLookup.setUri(uri);
                final ClientResource updatedResource = singleRepositoryService.saveOrUpdate(resourceLookup, overwrite,
                        createFolders, ClientTypeHelper.extractClientType(accept), dryRun, parameterMap);
                int createdVersion = com.jaspersoft.jasperserver.api.metadata.common.domain.Resource.VERSION_NEW + 1;
                // if current version is '0' (new version for the resource to be created is '-1') and previous version isn't '0',
                // then send 201 (Created), otherwise - 200 (OK)
                Response.Status status = updatedResource.getVersion() == createdVersion
                        && (resourceLookup.getVersion() == null || resourceLookup.getVersion() != createdVersion)
                        ? Response.Status.CREATED : Response.Status.OK;

                if (expanded != null && expanded) {
                    response = Response.fromResponse(resourceDetailsJaxrsService.getResourceDetails(
                            updatedResource.getUri(), mediaType.toString(), true, expandTypes, null, parameterMap, httpServletRequest))
                            .status(status).build();
                } else {
                    response = Response.status(status).entity(updatedResource).build();
                }
            }
        }
        return response;
    }

    protected String decodeHeaderContentLocation(String contentLocation) {
        if (contentLocation == null) return null;
        try {
            return URLDecoder.decode(contentLocation, CharEncoding.UTF_8);
        } catch (UnsupportedEncodingException ex) {
            log.debug("Cannot decode content lcation using UTF8 [" + contentLocation + "]");
            return contentLocation;
        }
    }

}

