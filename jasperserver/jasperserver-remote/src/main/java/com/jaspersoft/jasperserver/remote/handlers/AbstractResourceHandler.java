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

import com.jaspersoft.jasperserver.api.common.domain.ValidationError;
import com.jaspersoft.jasperserver.api.common.domain.ValidationErrorFilter;
import com.jaspersoft.jasperserver.api.common.domain.ValidationErrors;
import com.jaspersoft.jasperserver.api.common.domain.impl.UniversalValidationErrorFilter;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.Argument;
import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.ResourceDescriptor;
import com.jaspersoft.jasperserver.api.search.SearchFilter;
import com.jaspersoft.jasperserver.remote.ResourceActionResolver;
import com.jaspersoft.jasperserver.remote.ResourceHandler;
import com.jaspersoft.jasperserver.remote.ResourceResolverAdapter;
import com.jaspersoft.jasperserver.remote.ServiceException;
import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.remote.services.LegacyRunReportService;
import com.jaspersoft.jasperserver.remote.services.PermissionsService;
import com.jaspersoft.jasperserver.remote.services.ResourcesManagementRemoteService;
import com.jaspersoft.jasperserver.ws.xml.ByteArrayDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.List;
import java.util.Map;


/**
 * This is a base class for a resource handler.
 *
 * @author Giulio Toffoli
 * @version $Id$
 */
public abstract class AbstractResourceHandler implements ResourceHandler, ResourceResolverAdapter {

    private static final Log log = LogFactory.getLog(AbstractResourceHandler.class);

    @jakarta.annotation.Resource(name = "concreteRepository")
    private RepositoryService repository;
    @Autowired
    private MessageSource messageSource;
    @jakarta.annotation.Resource
    private ResourcesManagementRemoteService resourcesManagementRemoteService;
    @jakarta.annotation.Resource(name = "remoteResourceActionResolver")
    private ResourceActionResolver remoteResourceActionResolver;
    @jakarta.annotation.Resource(name = "legacyRunReportService")
    private LegacyRunReportService runReportService;
    @jakarta.annotation.Resource(name = "concretePermissionsService")
    private PermissionsService permissionsService;

    protected ResourceActionResolver getResourceActionResolver(){
        return remoteResourceActionResolver;
    }

    public void setRunReportService(LegacyRunReportService runReportService) {
        this.runReportService = runReportService;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void setRepository(RepositoryService repository) {
        this.repository = repository;
    }

    public void setPermissionsService(PermissionsService permissionsService) {
        this.permissionsService = permissionsService;
    }

    public void setRemoteResourceActionResolver(ResourceActionResolver remoteResourceActionResolver) {
        this.remoteResourceActionResolver = remoteResourceActionResolver;
    }
    public void setResourcesManagementRemoteService(ResourcesManagementRemoteService resourcesManagementRemoteService) {
        this.resourcesManagementRemoteService = resourcesManagementRemoteService;
    }

    public void setResourceActionHandler(ResourceActionResolver resourceActionResolver) {
        //do nothing. ResourceActionResolver is injected by Spring
    }

    public RepositoryService getRepository() {
        return repository;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public ResourcesManagementRemoteService getResourcesManagementRemoteService() {
        return resourcesManagementRemoteService;
    }

    public ResourceActionResolver getRemoteResourceActionResolver() {
        return remoteResourceActionResolver;
    }

    public LegacyRunReportService getRunReportService() {
        return runReportService;
    }

    @Override
    public SearchFilter getSearchFilter(String uri, String queryString, String wsType, boolean recursive, int maxItems, int startIndex) {
        // no custom search filter by default
        return null;
    }

    public ResourceDescriptor get(Resource resource, Map options) throws ServiceException {

        ResourceDescriptor descriptor = new ResourceDescriptor();
        setCommonAttributes(resource, descriptor, options);

        doGet(resource, descriptor, options);
        return descriptor;
    }

    /**
     * Set the common attributes of the resources:
     * uri, id (name), label, description, type, parent uri, version and creation date
     *
     * @param resource
     * @param descriptor
     */
    protected void setCommonAttributes(Resource resource, ResourceDescriptor descriptor, Map options) {
        descriptor.setUriString(resource.getURIString());
        descriptor.setDescription(resource.getDescription());
        descriptor.setLabel(resource.getLabel());
        descriptor.setName(resource.getName());
        descriptor.setResourceType(resource.getResourceType());
        descriptor.setParentFolder(resource.getParentFolder());
        descriptor.setVersion(resource.getVersion());
        descriptor.setCreationDate(resource.getCreationDate());

        //Add security permission mask template of current user for target resource.
        try {
            descriptor.setResourceProperty(ResourceDescriptor.PROP_SECURITY_PERMISSION_MASK, permissionsService.
                    getAppliedPermissionMaskForObjectAndCurrentUser(resource.getURIString()));
        } catch (ErrorDescriptorException e) {
            //TODO Handler exception.
        }

        if (resource instanceof FileResource){
            if (options!=null && options.containsKey("GET_LOCAL_RESOURCE") && (Boolean)options.get("GET_LOCAL_RESOURCE")){
                byte[] byteArr = repository.getResourceData(null, resource.getURI()).getData();
                ByteArrayDataSource bads = new ByteArrayDataSource(byteArr);
                runReportService.getReportAttachments(descriptor.getUriString()).put(descriptor.getUriString(), bads);
                descriptor.setData(byteArr);
            }

            if (!resource.getParentFolder().endsWith("_files")){
                byte[] byteArr = repository.getResourceData(null, resource.getURI()).getData();
                descriptor.setData(byteArr);
            }
        }
    }

    /**
     *
     * Performs the specific work to get a resource.
     *
     * @param resource
     * @param descriptor
     * @param options
     * @throws ServiceException
     */
    protected abstract void doGet(Resource resource,
            ResourceDescriptor descriptor, Map options) throws ServiceException;


    /**
     * Use the UniversalValidationErrorFilter to validate the resource.
     * Use the configured ResourceActionResolver to check if the resource can be written or not.
     *
     * @param resource
     * @throws ServiceException
     */
    protected void validate(Resource resource) throws ServiceException {
        ValidationErrorFilter filter = resource.isNew() ? UniversalValidationErrorFilter.getInstance() : null;

        ValidationErrors errors = null;

        if (resource instanceof Folder) errors = repository.validateFolder(null, (Folder)resource, filter);
        else errors = repository.validateResource(null, resource, filter);
        if (log.isDebugEnabled()) {
            log.debug(errors.getErrors().size()+" Error were found");
        }
         if (errors.isError()){

                String errorString = "";
                List errorsList = errors.getErrors();
                for (int i=0; i < errorsList.size(); ++i)
                {
                    ValidationError ve = (ValidationError)errorsList.get(i);
                    if (errorString.length() > 0) errorString += "\n";
                    errorString += messageSource.getMessage(ve.getErrorCode(), ve.getErrorArguments(), ve.getDefaultMessage(), LocaleContextHolder.getLocale());
                }
                throw new ServiceException(ServiceException.RESOURCE_BAD_REQUEST, errorString);
         }
    }

    /**
     * Create or update a resource.
     * This methos is the same as update(descriptor, options,true);
     *
     * @param descriptor
     * @return
     * @throws ServiceException
     */

    public ResourceDescriptor update(ResourceDescriptor descriptor, Map options) throws ServiceException {
        return update(descriptor, options,true);
    }

    /**
     * Implementation of the update method. This default implementation perform most of the generic work, creating
     * a new instance of a resource if it does not exist.
     * The method update on existing resources is called.
     *
     * @param descriptor
     * @param options
     * @param save
     * @return
     * @throws ServiceException
     */
    public ResourceDescriptor update(ResourceDescriptor descriptor, Map options, boolean save) throws ServiceException {
        // Check if the resource must be saved or modified inside another resource...
        Resource parentResource = getParentResource(descriptor);
        ResourceContainer parentHandler = null;
        ResourceHandler pResourceHandler;
        
        if (parentResource != null)
        {
            pResourceHandler = resourcesManagementRemoteService.getHandler(parentResource);
            if (pResourceHandler instanceof ResourceContainer)
            {
                parentHandler = (ResourceContainer)pResourceHandler;
            }
        }

        if (descriptor.getIsNew()) {

            if (log.isDebugEnabled()) {
                log.debug("creating " + descriptor.getUriString());
            }
            if (parentHandler == null) {
                
                Resource resource = createResource(descriptor);
                validate(resource);
                if (save){
                    if (resource instanceof Folder){
                        repository.saveFolder(null, (Folder)resource);
                    }
                    else{
                        repository.saveResource(null, resource);
                    }
                }
                return resourcesManagementRemoteService.createResourceDescriptor(resource);

            } else {
                
                Resource subResource = parentHandler.addSubResource(parentResource, descriptor);

                if (subResource != null) {
                    validate(parentResource);

                    // We assume here that a folder does not have nested children...
                    if (save) repository.saveResource(null, parentResource);
                    return resourcesManagementRemoteService.createResourceDescriptor(subResource);
                }
                else
                {
                    // The error here is actually something like: cannot create the resource.
                    throw new ServiceException(ServiceException.FORBIDDEN,
                                    messageSource.getMessage("webservices.error.cannotCreateResource",
                                            new String[]{parentResource.getParentFolder()}, LocaleContextHolder.getLocale()));
                }
            }
        } else { // We are modifying the resource...

            if (log.isDebugEnabled()) {
                log.debug("modifying " + descriptor.getUriString());
            }
            if (parentHandler == null) {
                Class resourceType = getResourceType();

                Resource resource = null;
                if (ResourceDescriptor.TYPE_FOLDER.equals(descriptor.getWsType()))
                {
                    resource = repository.getFolder(null, descriptor.getUriString());
                }
                else
                {
                   resource = repository.getResource(null, descriptor.getUriString(), resourceType);
                }
                if (resource == null) {

                    throw new ServiceException(ServiceException.RESOURCE_NOT_FOUND, messageSource.getMessage("webservices.error.resourceNotFound", new Object[]{}, LocaleContextHolder.getLocale()));

                } else {

                    updateResource(resource, descriptor, options);
                    validate(resource);

                    if (save){
                        if (resource instanceof Folder){
                            repository.saveFolder(null, (Folder)resource);
                        }
                        else{
                            repository.saveResource(null, resource);
                        }
                    }
                    return resourcesManagementRemoteService.createResourceDescriptor(resource);
                }
                
            } else { // We need to ask the parent to handle the change...

                    Resource subResource = parentHandler.addSubResource(parentResource, descriptor);
                    if (subResource != null) {
                        // validate the sub resource before save it...
                        validate(subResource);
                        if (save) repository.saveResource(null, parentResource);
                        return resourcesManagementRemoteService.createResourceDescriptor(subResource);
                    }
                    else
                    {
                        // The error here is actually something like: cannot update the resource.
                        throw new ServiceException(ServiceException.GENERAL_ERROR,
                                        messageSource.getMessage("webservices.error.cannotCreateResource",
                                                new String[]{parentResource.getParentFolder()}, LocaleContextHolder.getLocale()));
                    }
            }
        } // End if/else is new resource.
    }


    /**
     * This method looks into the options map to see is the resource to update is actually a child of a particular report unit...
     *
     * @param options
     * @return
     * @throws ServiceException
     * @deprecated The server is able to check if the resource belogs to another resource automatically
     */
    protected Resource getModifyingParentResource(Map options) throws ServiceException {
        Resource resource = null;

        if (options!=null && options.containsKey(Argument.MODIFY_REPORTUNIT))
        {
            String reportUnitUrl = (String)options.get(Argument.MODIFY_REPORTUNIT);
            if (reportUnitUrl != null && reportUnitUrl.length() > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Put: adding/modifying resource in reportUnit " + reportUnitUrl);
                }
                ReportUnit reportUnit = (ReportUnit) repository.getResource(null, reportUnitUrl);
                if (reportUnit == null) {
                    throw new ServiceException(
                            ServiceException.RESOURCE_NOT_FOUND, messageSource.getMessage(
                            "webservices.error.reportUnitNotFound", new Object[]{}, LocaleContextHolder.getLocale()));
                }
                resource = reportUnit;
            }

        }
        // for now, only report units can act as parent resources
        
        
        return resource;
    }

    /**
     * Ask the repository to create a new resource of the type specified by this handler with the name and the
     * description specified by the descriptor.
     * The method uses updateResource() to fill the empty resource created.
     *
     *
     * @param descriptor
     * @return
     * @throws ServiceException
     */
    protected Resource createResource(ResourceDescriptor descriptor) throws ServiceException
    {
        ResourceDescriptor childRD;
        Class resourceType = getResourceType();
        Resource resource = repository.newResource(null, resourceType);
        resource.setParentFolder(descriptor.getParentFolder());
        resource.setVersion(Resource.VERSION_NEW);
        resource.setName(descriptor.getName());
        resource.setURIString(descriptor.getUriString());
        updateResource(resource, descriptor, null);

        return resource;
    }

    /**
     * convenient method to create a new resource of generic type. This method looks for the wsType in the descriptor, looks for an handler of
     * this type of resource and create it.
     * If an handler is not found for this resource, or if the handler does not implements the AbstractResourceHandler class,
     * the method returns null.
     *
     *
     * @param descriptor
     * @return
     */
    protected Resource createChildResource(ResourceDescriptor descriptor) throws ServiceException {
            String childType = descriptor.getWsType();
            ResourceHandler handler = resourcesManagementRemoteService.getHandler(childType);

            if (handler == null || !(handler instanceof AbstractResourceHandler) )
            {
                if (log.isWarnEnabled()) {
                    log.warn(handler.getClass().getName() + " does not implement AbstractResourceHandler");
                }
                return null;
            }
            return  ((AbstractResourceHandler)handler).createResource(descriptor);
    }

    /**
     * Specific code to update a resource, in particular to read the resource properties from the
     * resource descriptor, and store them into the passed resource implementation.
     *
     * The default implementation just set the label and the description.
     *
     * Attention: name, uri and wsType should not be modified. The porpuse of override this method
     * is to add code specific for the type of resource your are dealing with.
     *
     * @param resource
     * @param descriptor
     * @throws ServiceException
     */
    protected void updateResource(Resource resource, ResourceDescriptor descriptor, Map options)
    {
        resource.setLabel(descriptor.getLabel());
        resource.setDescription(descriptor.getDescription());
        resource.setParentFolder(descriptor.getParentFolder());
        resource.setURIString(descriptor.getUriString());
        resource.setName(descriptor.getName());
    }


    /**
     * Checks if the descriptor is some kind of data source
     *
     * @param rd
     * @return
     */
    public boolean isDataSource(ResourceDescriptor rd) {

        String type = rd.getWsType();

        ResourceHandler handler = (ResourceHandler) resourcesManagementRemoteService.getHandler(type);

        if (handler!=null){
            Class resourceType = handler.getResourceType();
            ResourceHandler parentHandler = (ResourceHandler) resourcesManagementRemoteService.getHandler(ResourceDescriptor.TYPE_DATASOURCE);

            Class parentResourceType = parentHandler.getResourceType();
            return parentResourceType.isAssignableFrom(resourceType);
        }
        return false;
    }


    /**
     * Get the parent resource of this descriptor. A folder is not a parent resource,
     * it must be another resource...
     * Usually parent resources ends with _files, (but this may be not the rule we are doing a strong assumption here)...
     * so we have to do a couple of tests first...
     * @param descriptor
     * @return
     */
    private Resource getParentResource(ResourceDescriptor descriptor) {

        String resourceUri = descriptor.getUriString();

        // get the parent resource uri....
        int lastSeparatorPos = descriptor.getUriString().lastIndexOf(Folder.SEPARATOR);
        String parentResourceUri = (lastSeparatorPos == 0) ? null : resourceUri.substring(0, lastSeparatorPos);

        if (parentResourceUri == null) return null;
        else if (parentResourceUri.endsWith("_files"))
        {
            parentResourceUri = parentResourceUri.substring(0, parentResourceUri.length() - "_files".length());
            if (log.isDebugEnabled()) {
                log.debug("getParentResource: removing the local resource suffix parent resource uri= "+ parentResourceUri);
            }
        }


        try {
            return resourcesManagementRemoteService.locateResource(parentResourceUri);
        } catch (Exception ex)
        {
            // resource not found, we can ignore that.
        }
        return null;
        
    }

    public void delete(ResourceDescriptor descriptor) throws ServiceException {
        if (remoteResourceActionResolver.isResourceDeletable(descriptor.getUriString())) {

            if (descriptor.getWsType().equals(ResourceDescriptor.TYPE_FOLDER)){
                repository.deleteFolder(null, descriptor.getUriString());
            }
            else{

                // Check if the resource must be saved or modified inside another resource...
                Resource parentResource = getParentResource(descriptor);
                
                if (parentResource != null)
                {
                    ResourceHandler pResourceHandler = (ResourceHandler) resourcesManagementRemoteService.getHandler(parentResource);
                    if (pResourceHandler instanceof ResourceContainer)
                    {
                        ((ResourceContainer)pResourceHandler).deleteSubResource(parentResource, descriptor);
                        repository.saveResource(null, parentResource);
                    }
                    else {
                        repository.deleteResource(null, descriptor.getUriString());
                    }
                }
            }
            
        } else {
            throw new ServiceException(ServiceException.FORBIDDEN,
                    messageSource.getMessage("webservices.error.notDeletableResource", new Object[]{}, LocaleContextHolder.getLocale()));
        }
    }
}

