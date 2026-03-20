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
package com.jaspersoft.jasperserver.jaxrs.connection;

import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.api.ExceptionListWrapper;
import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.common.error.handling.SecureExceptionHandler;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.jaxrs.common.JaxrsEntityParser;
import com.jaspersoft.jasperserver.jaxrs.resources.ContentNegotiationHandler;
import com.jaspersoft.jasperserver.remote.connection.ContextCreationFailedException;
import com.jaspersoft.jasperserver.remote.connection.ContextsManager;
import com.jaspersoft.jasperserver.remote.connection.SecureExceptionWrapperInContextCreationFailure;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.exception.MandatoryParameterNotFoundException;
import com.jaspersoft.jasperserver.remote.exception.NotAcceptableException;
import com.jaspersoft.jasperserver.remote.exception.ResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.exception.ResourceTypeNotSupportedException;
import com.jaspersoft.jasperserver.remote.exception.UnsupportedMediaTypeException;
import com.jaspersoft.jasperserver.remote.exception.UnsupportedOperationErrorDescriptorException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@Service
// this service should be available through multiple path: rest_v2/connections and rest_v2/contexts
@Path("/{path:connections|contexts}")
public class ContextsJaxrsService {
    private final static Log log = LogFactory.getLog(ContextsJaxrsService.class);
    private static final Pattern EXTRACT_CONTEXT_TYPE_PATTERN = Pattern.compile("application/([^\\.]+)\\.([^(\\.|\\+)]+)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern CONTEXT_METADATA_PATTERN = Pattern.compile("application/([^\\.]+)(\\.[^.]+)?\\.metadata\\+.+",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern EXTRACT_CLIENT_TYPE_PATTERN = Pattern.compile("application/(.+)\\+json",
            Pattern.CASE_INSENSITIVE);
    @Context
    private Providers providers;
    @Context
    private HttpHeaders httpHeaders;
    @Resource
    private ContextsManager contextsManager;
    @Resource
    private ContentNegotiationHandler contentNegotiationHandler;
    @Resource(name = "processedExceptionsForCreatingContext")
    private List<String> processedExceptionsForCreatingContext;

    @Autowired
    private SecureExceptionWrapperInContextCreationFailure secureExceptionWrapper;
    @POST
    public Response createContext(InputStream stream, @HeaderParam(HttpHeaders.CONTENT_TYPE) MediaType mediaType,
            @HeaderParam(HttpHeaders.ACCEPT) MediaType accept, @Context HttpServletRequest request, @Context UriInfo uriInfo) throws URISyntaxException, IllegalParameterValueException, IOException {
        final Class<?> contextClass = getContextClass(mediaType);
        final String acceptString = accept != null ? accept.toString() : "";
        final boolean isMetadataRequested = CONTEXT_METADATA_PATTERN.matcher(acceptString).matches();
        final Object context = parseEntity(contextClass, stream, mediaType);
        if(context == null){
            throw new MandatoryParameterNotFoundException("body");
        }
        final String contentTypeString = mediaType.toString();
        if(isMetadataRequested){
            final Matcher matcher = EXTRACT_CLIENT_TYPE_PATTERN.matcher(acceptString);
            String metadataClientType = null;
            if(matcher.find()){
                metadataClientType = matcher.group(1);
            }
            if(metadataClientType == null
                    || !contextsManager.isMetadataSupported(ExecutionContextImpl.getRuntimeExecutionContext(), context, metadataClientType)) {
                throw new NotAcceptableException();
            }
        } else if(!contentNegotiationHandler.isAcceptable(context, contentTypeString, acceptString)){
            throw new NotAcceptableException();
        }
        final UUID contextId;
        try {
            contextId = contextsManager.createContext(context);
        } catch (ResourceTypeNotSupportedException e){
            if(isMetadataRequested){
                log.debug("Unable to create context of given type", e);
                throw new NotAcceptableException();
            } else {
                throw e;
            }
        } catch (Exception ex) {
            if (isProcessedException(ex, processedExceptionsForCreatingContext)) {
                if (ex instanceof ContextCreationFailedException) {
                    ErrorDescriptor errorDescriptor = secureExceptionWrapper.handleException(new ErrorDescriptor().setErrorCode("connection.failed").setMessage(ex.getMessage()).setParameters(((ContextCreationFailedException) ex).getErrorDescriptor().getParameters()));
                    throw new ErrorDescriptorException(errorDescriptor, ex.getCause());
                } else {
                    throw ex;
                }
            } else {
                // just in case there is unknown exception,
                // wrap unprocessed exception with error descriptor with error message only
                log.debug("Create Context: Uncatch unprocessed exception: " + ex.getClass().getName(), ex);
                throw new ErrorDescriptorException(ex.getMessage());
            }
        }
        final UriBuilder uriBuilder = UriBuilder
                .fromPath(uriInfo.getBaseUri().getPath())
                .path(uriInfo.getPath() != null ? uriInfo.getPath() : "")
                .path(contextId.toString());

        final Map<String, String[]> additionalProperties = request.getParameterMap();
        if (isMetadataRequested) {
            final Object contextMetadata;
            try {
                contextMetadata = contextsManager.getContextMetadata(contextId, additionalProperties);
            } catch (UnsupportedOperationErrorDescriptorException e){
                throw new NotAcceptableException();
            }
            if (contextMetadata == null) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            final String metadataClientResourceType = contextsManager
                    .getMetadataClientResourceType(context);

            return Response.ok(contextMetadata, clientTypeToMimeType(metadataClientResourceType))
                    .location(uriBuilder.path("metadata").build()).build();
        } else {
            final Object sourceContext = contextsManager.getContext(contextId, additionalProperties);
            final Object targetContext = contentNegotiationHandler.handle(sourceContext, contentTypeString,
                    acceptString, additionalProperties);
            return Response.created(uriBuilder.build()).entity(targetContext).build();
        }
    }


    protected boolean isProcessedException(Exception ex, List<String> processedExceptionsForCreatingContext) {
        if (processedExceptionsForCreatingContext != null) {
            for (String processedException : processedExceptionsForCreatingContext) {
                if (ex.getClass().getName().equals(processedException)) {
                    return true;
                }
                Class processedExceptionClass = null;
                try {
                    processedExceptionClass = Class.forName(processedException);
                } catch (Exception c) {
                    log.debug("Create Context - can't find class <" + processedException +">");
                }
                if (processedExceptionClass != null) {
                    try {
                        Class subClass = ex.getClass().asSubclass(processedExceptionClass);
                        if (subClass != null) return true;
                    } catch (Exception c) {
                        // not instance of current processed exception.  Continue
                    }
                }
            }
        }
        return false;
    }

    protected String clientTypeToMimeType(String clientType){
        return "application/" + clientType + "+json";
    }

    protected Object parseEntity(Class<?> contextClass, InputStream stream, MediaType mediaType) throws IOException {
        return JaxrsEntityParser.newInstance(providers, httpHeaders).parseEntity(contextClass, stream, mediaType);
    }

    @GET
    @Path("/{uuid}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getContextDetails(@PathParam("uuid") String uuid,
            @Context HttpServletRequest request) throws ResourceNotFoundException {
        final Object context = contextsManager.getContext(getUUID(uuid), request.getParameterMap());
        return context != null ? Response.ok(context).build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/{uuid}")
    public Response removeContextDetails(@PathParam("uuid") String uuid) throws ResourceNotFoundException {
        try {
            contextsManager.removeContext(getUUID(uuid));
        } catch (ResourceNotFoundException e){
            // no such context. Do nothing. It's expected state after this operation completion.
        }
        return Response.noContent().build();
    }

    @GET
    @Path("/{uuid}/metadata")
    public Response getContextMetadata(@PathParam("uuid") String uuid, @Context HttpServletRequest request) {
        final Object contextMetadata = contextsManager.getContextMetadata(getUUID(uuid), request.getParameterMap());
        if (contextMetadata == null) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        final String metadataClientResourceType = contextsManager
                .getMetadataClientResourceType(contextsManager.getContext(getUUID(uuid), request.getParameterMap()));
        return Response.ok(contextMetadata, clientTypeToMimeType(metadataClientResourceType)).build();
    }

    @POST
    @Path("/{uuid}/metadata")
    public Response getContextMetadata(@PathParam("uuid") String uuid, InputStream stream,
            @HeaderParam(HttpHeaders.CONTENT_TYPE) MediaType mediaType) throws IOException {
        Class<?> paramsClass = contextsManager.getMetadataParamsClass(getType(mediaType));
        if (paramsClass == null) {
            throw new UnsupportedMediaTypeException(mediaType.toString());
        }
        final Object params = parseEntity(paramsClass, stream, mediaType);
        Object contextMetadata;
        try {
            contextMetadata = contextsManager.getContextMetadata(getUUID(uuid), params);
        } catch (UnsupportedOperationErrorDescriptorException e) {
            throw new UnsupportedMediaTypeException(mediaType.toString());
        }
        return  contextMetadata != null ? Response.ok(contextMetadata).build() : Response.noContent().build();
    }

    protected String getType(MediaType mediaType){
        if(mediaType ==  null){
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MandatoryParameterNotFoundException("Content-Type").getErrorDescriptor()).build());
        }
        Matcher matcher = EXTRACT_CONTEXT_TYPE_PATTERN.matcher(mediaType.toString());
        return matcher.find() ? matcher.group(2) : null;
    }

    protected Class<?> getContextClass(MediaType mediaType) {
        Class<?> contextClass = contextsManager.getContextDescriptionClass(getType(mediaType));
        if (contextClass == null) {
            throw new WebApplicationException(Response.Status.UNSUPPORTED_MEDIA_TYPE);
        }
        return contextClass;
    }

    protected Class<?> getQueryClass(MediaType mediaType){
        Class<?> queryClass = String.class;
        final String type = getType(mediaType);
        if(type != null) {
            queryClass = contextsManager.getQueryClass(type);
            if (queryClass == null) {
                throw new WebApplicationException(Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE)
                        .entity(new ErrorDescriptor()
                                .setErrorCode("mediatype.not.supported")
                                .setMessage(mediaType.getSubtype() + " is not supported")).build());
            }
        }
        return queryClass;
    }

    protected static UUID getUUID(String uuid) throws ResourceNotFoundException {
        try {
            if (uuid.toLowerCase().startsWith("uuid:")) uuid = uuid.substring(5);
            return UUID.fromString(uuid);
        } catch (Exception ex) {
            throw new ResourceNotFoundException("Invalid UUID: " + uuid);
        }
    }
}

