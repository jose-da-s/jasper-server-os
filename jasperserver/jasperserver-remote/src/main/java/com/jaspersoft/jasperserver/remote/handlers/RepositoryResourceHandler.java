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
package com.jaspersoft.jasperserver.remote.handlers;

import com.jaspersoft.jasperserver.api.common.domain.ValidationErrorFilter;
import com.jaspersoft.jasperserver.api.common.domain.ValidationErrors;
import com.jaspersoft.jasperserver.api.common.domain.impl.UniversalValidationErrorFilter;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.view.domain.FilterElement;
import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.Argument;
import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.Request;
import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.ResourceDescriptor;
import com.jaspersoft.jasperserver.remote.*;

import com.jaspersoft.jasperserver.core.util.ResourceUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author gtoffoli
 * @version $Id$
 */
public abstract class RepositoryResourceHandler extends AbstractResourceHandler {

    private static final Log log = LogFactory.getLog(RepositoryResourceHandler.class);
    

    /**
     * Perform a validation of the passed in resource and save it if validation is passed, otherwise it throws a ServiceException
     *
     * @param repository
     * @param resource
     * @param serviceContext
     * @throws ServiceException
     */
    protected void saveValidated(RepositoryService repository, Resource resource, RepositoryRemoteServiceContext serviceContext) throws ServiceException {
        ValidationErrorFilter filter = resource.isNew() ? UniversalValidationErrorFilter.getInstance() : null; // getting exception when doing new file resource
        ValidationErrors errors = repository.validateResource(null, resource, filter);

        if (errors.isError()) {
            throw new ServiceException(HttpServletResponse.SC_FORBIDDEN, errors.toString());
        }

        repository.saveResource(null, resource);
    }

    /*
    protected Resource toNewResource(ResourceDescriptor descriptor, RepositoryRemoteServiceContext serviceContext) throws ServiceException {
        Class resourceType = getResourceType();
        Resource resource = serviceContext.getRepository().newResource(null, resourceType);
        resource.setParentFolder(descriptor.getParentFolder());
        resource.setVersion(Resource.VERSION_NEW);
        resource.setName(descriptor.getName());
        copyToResource(resource, descriptor, serviceContext);

        return resource;
    }

    public void copyToResource(Resource resource, ResourceDescriptor descriptor, RepositoryRemoteServiceContext serviceContext) throws ServiceException {
        resource.setLabel(descriptor.getLabel());
        resource.setDescription(descriptor.getDescription());
        updateResource(resource, descriptor, new java.util.HashMap());
    }

    protected Resource toChildResource(ResourceDescriptor descriptor, RepositoryRemoteServiceContext context) throws ServiceException {
        String childType = descriptor.getWsType();
        RepositoryResourceHandler childHandler = (RepositoryResourceHandler) context.getHandlerRegistry().getHandler(childType);

        return childHandler.toNewResource(descriptor, context);
    }
     
     */

//	public void getAttachments(Resource resource, Map arguments, ResourceDescriptor descriptor, ResultAttachments attachments, RepositoryRemoteServiceContext serviceContext) {
//		// default implementation does nothing
//	}
    

    public void move(Request request, RepositoryRemoteServiceContext serviceContext) throws ServiceException {
        String sourceURI = request.getResourceDescriptor().getUriString();
        String destinationURI = getDestinationURI(request, serviceContext);

        if (log.isDebugEnabled()) {
            log.debug("Moving resource " + sourceURI + " to folder " + destinationURI);
        }

        if (!getResourceActionResolver().canCreateResource(destinationURI)) {
            throw new ServiceException(ServiceException.GENERAL_ERROR,
                    serviceContext.getMessage("webservices.error.cannotCreateResource", new String[]{destinationURI}));
        }

        serviceContext.getRepository().moveResource(null, sourceURI, destinationURI);
    }

    public ResourceDescriptor copy(Request request, RepositoryRemoteServiceContext serviceContext) throws ServiceException {
        String sourceURI = request.getResourceDescriptor().getUriString();
        String destinationURI = getDestinationURI(request, serviceContext);

        if (log.isDebugEnabled()) {
            log.debug("Copying resource " + sourceURI + " to URI " + destinationURI);
        }

        if (!getResourceActionResolver().canCreateResource(ResourceUtil.getParentFolder(destinationURI))) {
            throw new ServiceException(ServiceException.GENERAL_ERROR,
                    serviceContext.getMessage("webservices.error.cannotCreateResource",
                    new String[]{ResourceUtil.getParentFolder(destinationURI)}));
        }

        // TODO multi resource copy?
        Resource copy = serviceContext.getRepository().copyResource(null, sourceURI, destinationURI);

        return get(copy, new java.util.HashMap());
    }

    protected Map getDefaultDescribeArguments() {
        return null;
    }

    protected String getDestinationURI(Request request,
            RepositoryRemoteServiceContext serviceContext) throws ServiceException {
        String destinationURI = request.getArgumentValue(Argument.DESTINATION_URI);
        if (destinationURI == null) {
            throw new ServiceException(ServiceException.GENERAL_REQUEST_ERROR,
                    serviceContext.getMessage("webservices.error.request.no.destination.URI", null));
        }
        return destinationURI;
    }

    /*
    public List listResources(Request request, RepositoryRemoteServiceContext serviceContext) throws ServiceException {
        try {
            Class resourceType = getResourceType();

            String parentFolder = request.getArgumentValue(Argument.PARENT_DIRECTORY);
            String ancestorFolder = request.getArgumentValue(Argument.START_FROM_DIRECTORY);

            if (log.isDebugEnabled()) {
                log.debug("Listing resources of type " + resourceType
                        + (parentFolder != null
                        ? ("from folder " + parentFolder)
                        : (ancestorFolder != null
                        ? (" starting from folder " + ancestorFolder)
                        : "")));
            }

            FilterCriteria filter = FilterCriteria.createFilter(resourceType);

            if (parentFolder != null) {
                filter.addFilterElement(FilterCriteria.createParentFolderFilter(parentFolder));
            } else if (ancestorFolder != null
                    // do not set ancestor folder filter if root
                    && !Folder.SEPARATOR.equals(ancestorFolder)) {
                filter.addFilterElement(FilterCriteria.createAncestorFolderFilter(ancestorFolder));
            }

            FilterElement additionalCriteria = additionalListResourcesFilterCriteria(request);
            if (additionalCriteria != null) {
                filter.addFilterElement(additionalCriteria);
            }

            List resources = serviceContext.getRepository().loadClientResources(filter);
            List descriptors;
            if (resources == null || resources.isEmpty()) {
                descriptors = new ArrayList(0);
            } else {
                descriptors = new ArrayList(resources.size());
                for (Iterator it = resources.iterator(); it.hasNext();) {
                    Resource resource = (Resource) it.next();
                    descriptors.add(serviceContext.createResourceDescriptor(resource));
                }
            }
            return descriptors;
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage());
        }
         
    }
  
     */

    protected FilterElement additionalListResourcesFilterCriteria(Request request) {
        return null;
    }
}

