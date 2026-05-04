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

import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.api.JSDuplicateResourceException;
import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ContentResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResourceData;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.RepositoryConfiguration;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceLookup;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.ContentResourceImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.FileResourceImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.FolderImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConversionOptions;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConverter;
import com.jaspersoft.jasperserver.api.metadata.common.service.JSResourceVersionNotMatchException;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.search.SearchCriteriaFactory;
import com.jaspersoft.jasperserver.dto.common.ClientTypeUtility;
import com.jaspersoft.jasperserver.dto.resources.ClientResource;
import com.jaspersoft.jasperserver.remote.exception.AccessDeniedException;
import com.jaspersoft.jasperserver.remote.exception.FolderAlreadyExistsException;
import com.jaspersoft.jasperserver.remote.exception.FolderNotFoundException;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.exception.MandatoryParameterNotFoundException;
import com.jaspersoft.jasperserver.remote.exception.NotAFileException;
import com.jaspersoft.jasperserver.remote.exception.NotAcceptableException;
import com.jaspersoft.jasperserver.remote.exception.ResourceAlreadyExistsException;
import com.jaspersoft.jasperserver.remote.exception.ResourceInUseException;
import com.jaspersoft.jasperserver.remote.exception.ResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.exception.VersionNotMatchException;
import com.jaspersoft.jasperserver.remote.resources.converters.ResourceConverterProvider;
import com.jaspersoft.jasperserver.remote.resources.converters.ToServerConversionOptions;
import com.jaspersoft.jasperserver.remote.resources.converters.ToServerConverter;
import com.jaspersoft.jasperserver.remote.resources.operation.CopyMoveOperationStrategy;
import com.jaspersoft.jasperserver.remote.services.SingleRepositoryService;
import org.hibernate.JDBCException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Component("singleRepositoryService")
@Transactional(rollbackFor = Exception.class)
public class SingleRepositoryServiceImpl implements SingleRepositoryService {

    private Pattern nameWithNumber = Pattern.compile("^.*_\\d+$", Pattern.CASE_INSENSITIVE);

    @jakarta.annotation.Resource(name = "configurationBean")
    private RepositoryConfiguration configuration;

    @jakarta.annotation.Resource
    private UriHardModifyProtectionChecker uriHardModifyProtectionChecker;

    @jakarta.annotation.Resource(name = "concreteRepository")
    protected RepositoryService repositoryService;

    @jakarta.annotation.Resource
    protected Set<String> fileResourceTypes;
    @jakarta.annotation.Resource
    private ResourceConverterProvider resourceConverterProvider;
    @jakarta.annotation.Resource
    private SearchCriteriaFactory searchCriteriaFactory;
    @jakarta.annotation.Resource
    private Map<String, CopyMoveOperationStrategy> copyMoveStrategies;

    private DefaultCopyMoveStrategy defaultCopyMoveStrategy = new DefaultCopyMoveStrategy();

    public static final String SCALABLE_QUERY_ENGINE = "scalable-query-engine";

    @Override
    public Resource getResource(String uri) {
        Resource resource = repositoryService.getResource(ExecutionContextImpl.getRestrictedRuntimeExecutionContext(), uri);
        if (resource == null) {
            resource = repositoryService.getFolder(null, uri);
        }
        return resource;
    }

    @Override
    public Resource getResource(String uri, String source) {
        ExecutionContext ctx = null;
        if (source != null && source.equals(SCALABLE_QUERY_ENGINE))
            ctx = ExecutionContextImpl.getRuntimeExecutionContext();
        else
            ctx = ExecutionContextImpl.getRestrictedRuntimeExecutionContext();
        Resource resource = repositoryService.getResource(ctx, uri);
        if (resource == null) {
            resource = repositoryService.getFolder(null, uri);
        }
        return resource;
    }

    @Override
    public FileResourceData getFileResourceData(String uri) {
        Resource resource = repositoryService.getResource(null, uri);
        if (resource == null) {
            throw new IllegalStateException("Resource " + uri + " not found");
        }
        return getFileResourceData(resource);
    }

    @Override
    public FileResourceData getFileResourceData(Resource resource) {
        if (resource instanceof FileResource) {
            return repositoryService.getResourceData(null, resource.getURIString());
        }
        if (resource instanceof ContentResource) {
            return repositoryService.getContentResourceData(null, resource.getURIString());
        }
        throw new IllegalStateException(resource.getURIString() + " is not a file");
    }

    @Override
    public void deleteResource(String uri) throws IllegalParameterValueException, AccessDeniedException {
        if (uri == null || "".equals(uri)) {
            throw new IllegalParameterValueException("uri", uri);
        }
        if(uriHardModifyProtectionChecker.isHardModifyProtected(uri)){
            throw new AccessDeniedException("", uri);
        }

        try {
            Resource resource = repositoryService.getResource(null, uri);
            if (resource != null) {
                List<ResourceLookup> dependentResources = repositoryService.getDependentResources(null, uri, searchCriteriaFactory, 0, 0);

                if (dependentResources == null || dependentResources.isEmpty() || isAllTemporary(dependentResources)){
                    if (dependentResources != null){
                        for (ResourceLookup resourceLookup: dependentResources){
                            repositoryService.deleteResource(null, resourceLookup.getURIString());
                        }
                    }

                    repositoryService.deleteResource(null, uri);
                } else {
                    throw new ResourceInUseException(dependentResources);
                }
            } else {
                resource = repositoryService.getFolder(null, uri);
                if (resource != null) {
                    repositoryService.deleteFolder(null, uri);
                }
            }
        } catch (org.springframework.security.access.AccessDeniedException sse) {
            throw new AccessDeniedException(uri);
        } catch (JSExceptionWrapper w){
            throw getRootException(w);
        }
    }

    @Override
    public Resource createResource(Resource serverResource, String parentUri, boolean createFolders, boolean dryRun) throws ErrorDescriptorException {
        if (createFolders) {
            if(!dryRun) ensureFolderUri(parentUri);
        } else if (!repositoryService.folderExists(null, parentUri)) {
            throw new FolderNotFoundException(parentUri);
        }

        serverResource.setParentFolder(parentUri);
        if (serverResource.getName() == null || "".equals(serverResource.getName())) {
            serverResource.setName(generateName(parentUri, serverResource.getLabel()));
        }
        if(!dryRun) {
            try {
                if (serverResource instanceof Folder) {
                    repositoryService.saveFolder(null, (Folder) serverResource);
                } else {
                    repositoryService.saveResource(null, serverResource);
                }
            } catch (JSDuplicateResourceException e) {
                throw new AccessDeniedException(e.getMessage(), new String[]{serverResource.getName(), parentUri});
            } catch (JSExceptionWrapper w) {
                throw getRootException(w);
            }
        }

        return dryRun ? serverResource : getResource(serverResource.getURIString());
    }

    public ClientResource saveOrUpdate(ClientResource clientResource, boolean overwrite, boolean createFolders,
            String clientType, boolean dryRun, Map<String, String[]> additionalProperties) throws ErrorDescriptorException {
        final String uri = clientResource.getUri();
        Resource resource = getResource(uri);

        // asking not toServerConverter, but toClientConverter for types compatibility to avoid NullPointerException,
        // because BinaryDataResourceConverter returns null as serverResourceType
        if (resource != null) {
            // is it different type of resource?
            if (resourceConverterProvider.getToClientConverter(resource.getResourceType(), ClientTypeUtility.extractClientType(clientResource.getClass())) == null) {
                if (overwrite) {
                    if(!dryRun) deleteResource(uri);
                    resource = null;
                } else {
                    throw new ResourceAlreadyExistsException(uri);
                }
            } else {
                if (!new Integer(resource.getVersion()).equals(clientResource.getVersion())){
                    if (overwrite) {
                        if(!dryRun) deleteResource(uri);
                        resource = null;
                    } else {
                        throw new VersionNotMatchException();
                    }
                }
            }
        }
        resource = ((ToServerConverter<ClientResource, Resource, ToServerConversionOptions>)resourceConverterProvider.getToServerConverter(clientResource))
                .toServer(ExecutionContextImpl.getRuntimeExecutionContext(), clientResource,
                        resource, ToServerConversionOptions.getDefault().setOwnersUri(uri).setAdditionalProperties(additionalProperties));
        if(resource.isNew()){
            resource = createResource(resource, resource.getParentPath(), createFolders, dryRun);
        } else {
            resource = updateResource(resource, dryRun);
        }
        ToClientConverter<? super Resource, ? extends ClientResource, ToClientConversionOptions> toClientConverter = resourceConverterProvider.getToClientConverter(resource);
        if(clientType != null && !clientType.isEmpty()) {
            toClientConverter = resourceConverterProvider.getToClientConverter(resource.getResourceType(),
                    clientType);
            if (toClientConverter == null) {
                throw new NotAcceptableException();
            }
        }
        return toClientConverter.toClient(resource, ToClientConversionOptions.getDefault()
                .setAdditionalProperties(additionalProperties));
    }

    @Override
    public Resource updateResource(Resource resource, boolean dryRun) throws ResourceNotFoundException, VersionNotMatchException {
        if(!dryRun) {
            try {
                if (resource instanceof Folder) {
                    repositoryService.saveFolder(null, (Folder) resource);
                } else {
                    repositoryService.saveResource(null, resource);
                }
            } catch (JSResourceVersionNotMatchException e) {
                throw new VersionNotMatchException();
            } catch (JSExceptionWrapper w) {
                throw getRootException(w);
            }
        }
        return dryRun ? resource : getResource(resource.getURIString());
    }

    @Override
    public String copyResource(String sourceUri, String destinationUri, boolean createFolders, boolean overwrite, String renameTo) throws ResourceNotFoundException, AccessDeniedException, ResourceAlreadyExistsException, IllegalParameterValueException {
        Resource resource = prepareSource(sourceUri);
        Resource destination = prepareDestination(resource, destinationUri, createFolders);

        return prepareStrategy(destination)
                .copyResource(resource, destination, overwrite, renameTo);
    }

    @Override
    public String moveResource(String sourceUri, String destinationUri, boolean createFolders, boolean overwrite,
            String renameTo) throws ResourceNotFoundException, AccessDeniedException, ResourceAlreadyExistsException,
            IllegalParameterValueException {
        Resource resource = prepareSource(sourceUri);
        Resource destination = prepareDestination(resource, destinationUri, createFolders);

        return prepareStrategy(destination).moveResource(resource, destination, overwrite, renameTo);
    }

    @Override
    public Resource createFileResource(InputStream stream, String parentFolderUri, String name, String label,
            String description, String type, boolean createFolders, boolean dryRun) throws ErrorDescriptorException {
        Resource file = fileResourceTypes.contains(type) ? new FileResourceImpl() : new ContentResourceImpl();
        file.setLabel(label);
        file.setName(name);
        file.setDescription(description);
        file.setCreationDate(new Date());

        if (fileResourceTypes.contains(type)) {
            ((FileResource) file).readData(stream);
            ((FileResource) file).setFileType(type);
        } else {
            ((ContentResource) file).readData(stream);
            ((ContentResource) file).setFileType(type);
        }

        return createResource(file, parentFolderUri, createFolders, dryRun);
    }

    @Override
    public Resource updateFileResource(InputStream stream, String parentUri, String name, String label, String description, String type, boolean dryRun) throws ErrorDescriptorException {
        String uri = parentUri.endsWith(Folder.SEPARATOR) ? parentUri + name : parentUri + Folder.SEPARATOR + name;
        Resource file = getResource(uri);

        if (file instanceof FileResource) {
            ((FileResource) file).readData(stream);
            ((FileResource) file).setFileType(type);
            ((FileResource) file).setReferenceURI(null);
        } else if (file instanceof ContentResource) {
            ((ContentResource) file).readData(stream);
            ((ContentResource) file).setFileType(type);
        } else {
            throw new NotAFileException(parentUri + Folder.SEPARATOR + name);
        }

        file.setLabel(label == null ? name : label);
        file.setDescription(description);

        return updateResource(file, dryRun);
    }

    @Override
    public String getUniqueName(String parenUri, String name) throws MandatoryParameterNotFoundException{
        if (parenUri == null ){
            throw new MandatoryParameterNotFoundException("parentUri");
        }

        if (name == null ){
            throw new MandatoryParameterNotFoundException("label");
        }
        return generateName(parenUri, name);
    }

    private Resource prepareSource(String sourceUri) throws ResourceNotFoundException, AccessDeniedException, ResourceAlreadyExistsException, IllegalParameterValueException {
        if (sourceUri == null || "".equals(sourceUri)) {
            throw new IllegalParameterValueException("sourceUri", sourceUri);
        }

        if(uriHardModifyProtectionChecker.isHardModifyProtected(sourceUri)){
            throw new AccessDeniedException("", sourceUri);
        }

        Resource resource = getResource(sourceUri);
        if (resource == null) {
            throw new ResourceNotFoundException(sourceUri);
        }
        return resource;
    }

    private Resource prepareDestination(Resource resource, String destinationUri, boolean createFolders) {
        Resource destination = getResource(destinationUri);

        if (destination == null){
            if (createFolders) {
                destination = ensureFolderUri(destinationUri);
            } else {
                destination = repositoryService.getFolder(null, destinationUri);
                if (destination == null) {
                    throw new FolderNotFoundException(destinationUri);
                }
            }
        }

        return destination;
    }

    private CopyMoveOperationStrategy prepareStrategy(Resource destination) {
        if (copyMoveStrategies.containsKey(destination.getClass().getName())) {
            return copyMoveStrategies.get(destination.getClass().getName());
        }

        return defaultCopyMoveStrategy;
    }

    protected Folder ensureFolderUri(String uri) throws AccessDeniedException, ResourceAlreadyExistsException, IllegalParameterValueException {
        try {
            uri = "".equals(uri) ? Folder.SEPARATOR : uri;
            Folder folder = repositoryService.getFolder(null, uri);
            if (folder == null) {
                if (repositoryService.getResource(null, uri) != null) {
                    throw new ResourceAlreadyExistsException(uri);
                }

                int lastSeparator = uri.lastIndexOf(Folder.SEPARATOR);
                String label = uri.substring(lastSeparator + 1, uri.length());
                if (!label.equals(transformLabelToName(label))){
                    throw new IllegalParameterValueException("folder.name", label);
                }

                folder = new FolderImpl();
                folder.setParentFolder(ensureFolderUri(uri.substring(0, lastSeparator)));
                folder.setName(label);
                folder.setLabel(label);

                repositoryService.saveFolder(null, folder);
            } else {
                // /Public and /public should be different folders
                if (!folder.getURIString().equals(uri)){
                    throw new FolderAlreadyExistsException(uri, folder.getURIString());
                }
            }
            return folder;
        } catch (org.springframework.security.access.AccessDeniedException spe) {
            throw new AccessDeniedException("Access denied", uri);
        }
    }

    protected String generateName(String parentUri, String label){
        String name = transformLabelToName(label);
        String uri = parentUri + Folder.SEPARATOR + name;
        Resource resource = repositoryService.getResource(null, uri);
        if (resource == null) {
            resource = repositoryService.getFolder(null, uri);
        }
        if (resource != null){
            if (nameWithNumber.matcher(name).matches()){
                int divider = name.lastIndexOf("_");
                Integer number = Integer.parseInt(name.substring(divider + 1)) + 1;
                name = name.substring(0, divider + 1) + number.toString();
            }
            else {
                name = name.concat("_1");
            }
            name = generateName(parentUri, name);
        }
        return name;
    }

    private String transformLabelToName(String label){
        return label.replaceAll(configuration.getResourceIdNotSupportedSymbols(), "_");
    }

    private boolean isAllTemporary(Collection<ResourceLookup> resources){
        String tempPrefix = (configuration.getTempFolderUri() == null ? "/temp" : configuration.getTempFolderUri()).concat(Folder.SEPARATOR);
        boolean res = true;

        for (Resource r: resources){
            res &= r.getURIString().startsWith(tempPrefix);
        }

        return res;
    }

    private class DefaultCopyMoveStrategy implements CopyMoveOperationStrategy {

        @Override
        public String copyResource(Resource resource, Resource destination, boolean overwrite, String renameTo)
                throws ResourceNotFoundException, AccessDeniedException, ResourceAlreadyExistsException, IllegalParameterValueException {
            String newName = renameTo == null ? resource.getName() : transformLabelToName(renameTo);
            String newUri = handleOverwrite(newName, destination, overwrite);
            // underlying service requires full uri, not just parent uri
            // after setting new parent uri we obtain the new uri of resource

            if (resource instanceof Folder) {
                if (destination.getURIString().startsWith(resource.getURIString() + Folder.SEPARATOR)){
                    throw new IllegalParameterValueException("sourceUri", resource.getURIString());
                }
                repositoryService.copyRenameFolder(null, resource.getURIString(), newUri, renameTo);
            } else {
                repositoryService.copyRenameResource(null, resource.getURIString(), newUri, renameTo);
            }

            return newUri;
        }

        @Override
        public String moveResource(Resource resource, Resource destination, boolean overwrite, String renameTo) throws ResourceNotFoundException, AccessDeniedException, ResourceAlreadyExistsException, IllegalParameterValueException {
            String newName = renameTo == null ? resource.getName() : transformLabelToName(renameTo);
            String newUri = handleOverwrite(newName, destination, overwrite);

            if (resource instanceof Folder) {
                if (destination.getURIString().startsWith(resource.getURIString() + Folder.SEPARATOR)){
                    throw new IllegalParameterValueException("sourceUri", resource.getURIString());
                }
                repositoryService.moveFolder(null, resource.getURIString(), destination.getURIString());
            } else {
                repositoryService.moveResource(null, resource.getURIString(), destination.getURIString());
            }

            return newUri;
        }

        private String handleOverwrite(String name, Resource destination, boolean overwrite){
            String newUri = (destination.getURIString().endsWith(Folder.SEPARATOR) ? destination.getURIString() :
                    destination.getURIString() + Folder.SEPARATOR) + name;
            Resource existing = getResource(newUri);

            if (existing != null) {
                if (overwrite) {
                    deleteResource(newUri);
                } else {
                    throw new ResourceAlreadyExistsException(newUri);
                }
            }

            return newUri;
        }
    }

    protected static RuntimeException getRootException(JSExceptionWrapper w) {
        Exception original = w.getOriginalException();
        if (original != null) {
            // Workaround:  Progress Oracle driver throws simple java.sql.Exception for data integrity violation
            if (original instanceof DataIntegrityViolationException) {
				return (DataIntegrityViolationException) original;
            } else if (original.getMessage() != null) {
                String originalMessage = original.getMessage().toLowerCase();
                if ((originalMessage.indexOf("integrity") >= 0) && (originalMessage.indexOf("violate") >= 0)) {
                    return new DataIntegrityViolationException(original.getMessage(), original);
                }
            }
            if (original instanceof RuntimeException) {
                if (original instanceof JDBCException) {
                    return new DataIntegrityViolationException(original.getMessage(), original);
                } else {
                    return (RuntimeException) original;
                }
            }
        }
        return w;
    }


}

