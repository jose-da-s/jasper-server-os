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
package com.jaspersoft.jasperserver.remote.services.impl;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceLookup;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.user.service.TenantService;
import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.OperationResult;
import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.ResourceDescriptor;
import com.jaspersoft.jasperserver.remote.ResourceHandler;
import com.jaspersoft.jasperserver.remote.ResourceHandlerRegistry;
import com.jaspersoft.jasperserver.remote.ServiceException;
import com.jaspersoft.jasperserver.remote.handlers.ReportUnitHandler;
import com.jaspersoft.jasperserver.remote.services.ResourcesManagementRemoteService;
import com.jaspersoft.jasperserver.remote.utils.AuditHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.jaspersoft.jasperserver.api.logging.audit.domain.AuditEventType.DELETE_RESOURCE;

/**
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@Component("resourcesManagementRemoteService")
public class ResourcesManagementRemoteServiceImpl implements ResourcesManagementRemoteService {

    private final static Log log = LogFactory.getLog(ResourcesManagementRemoteServiceImpl.class);

    @jakarta.annotation.Resource(name = "concreteRepository")
    private RepositoryService repository;
    @jakarta.annotation.Resource
    private AuditHelper auditHelper;
    @Autowired
    private MessageSource messageSource;
    @jakarta.annotation.Resource(name = "remoteResourceHandlerRegistry")
    private ResourceHandlerRegistry handlerRegistry;

    public ResourceHandler getHandler(Resource resource) {
        return handlerRegistry.getHandler(resource);
    }

    public ResourceHandler getHandler(String wsType) {
        return handlerRegistry.getHandler(wsType);
    }

    public void setHandlerRegistry(ResourceHandlerRegistry handlerRegistry) {
        this.handlerRegistry = handlerRegistry;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void setAuditHelper(AuditHelper auditHelper) {
        this.auditHelper = auditHelper;
    }

    public void setRepository(RepositoryService repository) {
        this.repository = repository;
    }

    public void copyResource(String sourceURI, String destinationURI) {
        Resource source = locateResource(sourceURI);
        if (source == null) {
            throw new IllegalStateException("could not locate resource: " + sourceURI);
        } else if (source instanceof Folder) {
            repository.copyFolder(null, sourceURI, destinationURI);
        } else {
            repository.copyResource(null, sourceURI, destinationURI);

        }
    }

    public void moveResource(String sourceURI, String destinationURI) {
        Resource resource = locateResource(sourceURI);

        if (resource instanceof Folder)
            repository.moveFolder(null, sourceURI, destinationURI);
        else
            repository.moveResource(null, sourceURI, destinationURI);
    }

    /**
     * Return a descriptor
     * Attachments, if requested, are placed inside the attachments list.
     * If the list is null, no attachment is returned.
     * the local should be true ONLY if the first call from RESTResource is for a local resource
     *
     * @throws com.jaspersoft.jasperserver.api.JSException
     *
     */
    public ResourceDescriptor getResource(String resourceURI, Map<String, Object> options) throws ServiceException {
        Resource resource = locateResource(resourceURI);

        if (resource == null) {
            if (log.isWarnEnabled()) {
                log.warn("Get: null resourceDescriptor for " + resourceURI);
            }
            throw new ServiceException(HttpServletResponse.SC_NOT_FOUND, "Could not locate resource at URI: " + resourceURI);
        } else {
            ResourceDescriptor rd = createResourceDescriptor(resource, processDescriptorOptions(options));
            return rd;
        }
    }

    public Resource locateResource(String uri) throws ServiceException {
        return locateResource(uri, null);
    }

    /**
     * Find a resource from an uri. This method is exensively used by the services to
     * manage the repository.
     *
     * @param uri
     * @return
     * @throws ServiceException
     */
    public Resource locateResource(String uri, ExecutionContext context) throws ServiceException {

        try {

            Resource res = null;

            String folderName;

            int sep = uri.lastIndexOf(Folder.SEPARATOR);
            if (sep >= 0) {
                folderName = uri.substring(0, sep);
            } else {
                // No separator: error
                throw new ServiceException(HttpServletResponse.SC_NOT_FOUND, "jsexception.invalid.uri", new Object[]{uri});
            }

            // Check if the folder is a RU first...
            if (folderName.endsWith("_files")) {
                String parentUri = folderName.substring(0, folderName.length() - "_files".length());
                if (log.isWarnEnabled()) {
                    log.warn("Loading uri: " + parentUri);
                }
                Resource parentRes = repository.getResource(context, parentUri);
                if (parentRes != null) {
                    // The parent folder is a RU...
                    // Get the resource (quick way to check accessibility....)
                    Resource folderResource = locateResource(folderName);
                    ResourceDescriptor ruRd = createResourceDescriptor(folderResource);

                    if (log.isWarnEnabled()) {
                        log.warn("Loaded RU " + res);
                    }
                    if (ruRd == null) {
                        // The user can not access to this RU...
                        return null;
                    }

                    res = repository.getResource(context, uri);
                    if (log.isWarnEnabled()) {
                        log.warn("Loaded resource " + uri + " " + res);
                    }
                }
            }

            if (res == null) {
                if (folderName.length() == 0) {
                    folderName = "/";
                }
                res = repository.getResource(context, uri);
            }

            if (res == null) // try to look for a folder...
            {
                Folder folder = repository.getFolder(context, uri);
                if (folder != null) {
                    res = folder;
                } else {
                    throw new ServiceException(HttpServletResponse.SC_NOT_FOUND, "Could not locate resource: " + uri);
                }
            }
            return res;

        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            if (ex instanceof AccessDeniedException) {
                throw new ServiceException(ServiceException.FORBIDDEN, ex.getLocalizedMessage());
            }
            throw new ServiceException(ex);
        }
    }


    /**
     * This method add to the options map the flag OPTION_REPORT_UNIT_CONTENTS.
     * This is used to force the load method of the ReportUnit handler to provide not just the
     * basic informations about the report unit, but even the children descriptors.
     *
     * @param options
     * @return
     */
    protected Map processDescriptorOptions(Map options) {
        if (options == null) {
            options = new HashMap();
        }
        if (!options.containsKey(ReportUnitHandler.OPTION_REPORT_UNIT_CONTENTS)) {
            options.put(ReportUnitHandler.OPTION_REPORT_UNIT_CONTENTS, Boolean.TRUE);
        }
        return options;
    }

    /**
     * Return a list of ResourceDescriptor(s)
     *
     * @throws com.jaspersoft.jasperserver.api.JSException
     *
     */
    public void deleteResource(String uri) throws ServiceException {
        //setting up audit for action
        long currentTime = System.currentTimeMillis();
        auditHelper.createAuditEvent(DELETE_RESOURCE.toString());

        // starting action execution
        OperationResult or;
        if (log.isDebugEnabled()) {
            log.debug("list for uri: " + uri);
        }


        // The resource to list based on the uri
        if (uri == null) {
            if (log.isWarnEnabled()) {
                log.warn("Delete: null resourceDescriptor for " + uri);
            }
            throw new ServiceException(ServiceException.RESOURCE_BAD_REQUEST, messageSource.getMessage("remote.error.resourceNotFound", new Object[]{}, LocaleContextHolder.getLocale()));
        }

        // 1. Check if the user has access to the resource....
        Resource resource = locateResource(uri);
        if (resource == null) {
            if (log.isWarnEnabled()) {
                log.warn("Delete: no access or not existing resource " + uri);
            }
            throw new ServiceException(ServiceException.RESOURCE_NOT_FOUND, messageSource.getMessage("remote.error.resourceNotFound", new Object[]{}, LocaleContextHolder.getLocale()));
        }

        ResourceHandler rh = getHandler(resource);
        if (rh == null) {
            if (log.isWarnEnabled()) {
                log.warn("Delete: no resource handler found for resource " + uri);
            }
            throw new ServiceException(ServiceException.RESOURCE_NOT_FOUND, "Unsupported resource type");
        }

        try {
            rh.delete(rh.get(resource, new HashMap()));
        } catch (ServiceException ex) {
            ex.setErrorCode(ServiceException.FORBIDDEN);
            throw ex;
        }
    }

    /**
     * Generic entry point to create a new resource.
     *
     * @param resourceDescriptor
     */
    public ResourceDescriptor putResource(ResourceDescriptor resourceDescriptor) throws ServiceException {
        final String uriString = resourceDescriptor.getUriString();
        if(Pattern.matches(".*/" + TenantService.ORGANIZATIONS + "/[^/]+$", uriString)){
            // 'organizations' is technical folder. No resources is permitted to be created here
            throw new ServiceException(ServiceException.FORBIDDEN, "Creation of resources in " + TenantService.ORGANIZATIONS + " folder isn't allowed");
        }
        if(!ResourceDescriptor.TYPE_FOLDER.equals(resourceDescriptor.getWsType()) && Pattern.matches("/[^/]+$|.*/" + TenantService.ORGANIZATIONS + "(/[^/]+){2}$", uriString)){
            // only folders can be created in root folder or organization's root folder
            throw new ServiceException(ServiceException.FORBIDDEN, "Creation of resources in root folder isn't allowed");
        }

        try {
            ResourceHandler handler = getHandler(resourceDescriptor.getWsType());

            if (handler == null) {
                throw new ServiceException(ServiceException.RESOURCE_BAD_REQUEST, "The resource type is not supported by this server");
            }
            return handler.update(resourceDescriptor, null, true);
        } catch (ServiceException se) {
            throw se;
        } catch (AccessDeniedException ex) {
            // Let's try to be as specific as possible with the errors...
            throw new ServiceException(ServiceException.FORBIDDEN, ex.getLocalizedMessage());
        }
    }

    /**
     * Create a ResourceDescriptor from a Resource.
     * The real type of this resource is saved in WsType
     *
     * @param resource
     * @param options
     * @return
     * @throws ServiceException
     */
    public ResourceDescriptor createResourceDescriptor(Resource resource, Map options) throws ServiceException {
        if (resource == null) {
            return null;
        }

        if (resource instanceof ResourceLookup) {
            if (Folder.class.getName().equals(resource.getResourceType())){
                resource = repository.getFolder(ExecutionContextImpl.getRuntimeExecutionContext(), resource.getURI());
            }
            else{
                resource = repository.getResource(ExecutionContextImpl.getRuntimeExecutionContext(), resource.getURI());
            }
        }

        ResourceHandler resourceHandler = getHandler(resource);
        if (resourceHandler == null) {
            throw new ServiceException("No resource handler found for class " + resource.getClass().getName());
        }

        return resourceHandler.get(resource, options);
    }

    /**
     * the same as createResourceDescriptor( resource, null)
     *
     * @param resource
     * @return
     * @throws com.jaspersoft.jasperserver.remote.ServiceException
     *
     */
    public ResourceDescriptor createResourceDescriptor(Resource resource) throws ServiceException {
        return createResourceDescriptor(resource, null);
    }

    public ResourceDescriptor createResourceDescriptor(ResourceReference reference, Map options) throws ServiceException {
        if (reference.isLocal()) {
            return createResourceDescriptor(reference.getLocalResource());
        } else {
            ResourceDescriptor childRd = new ResourceDescriptor();
            childRd.setWsType(ResourceDescriptor.TYPE_REFERENCE);

            ResourceDescriptor rd = createResourceDescriptor(reference.getReferenceURI());
            childRd.setReferenceType(rd.getWsType());
            childRd.setUriString(reference.getReferenceURI());
            childRd.setReferenceUri(reference.getReferenceURI());

            return childRd;
        }
    }


    /**
     * Same as createResourceDescriptor(uri, null)
     *
     * @param uri
     * @return
     */
    public ResourceDescriptor createResourceDescriptor(String uri) throws ServiceException {
        return createResourceDescriptor(uri, null);
    }


    /**
     * Creates a descriptor checking the special options in the map.
     *
     * @param uri
     * @param options
     * @return
     * @throws ServiceException
     */
    public ResourceDescriptor createResourceDescriptor(String uri, Map options) throws ServiceException {
        Resource res = locateResource(uri);
        return createResourceDescriptor(res, options);
    }


    //updates the resource name and attributes but not location
    public void updateResource(String resourceURI, ResourceDescriptor newRd, boolean save) {
        Resource resource = locateResource(resourceURI);
        ResourceDescriptor oldRD = createResourceDescriptor(resource);
        ResourceHandler handler = getHandler(oldRD.getWsType());
        handler.update(newRd, null, save);
        if (log.isDebugEnabled()) {
            log.debug("UpdateResource resource " + newRd.getUriString() + " was updated resource " + resourceURI);
        }

    }
}

