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

import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.api.metadata.view.domain.FilterCriteria;
import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.Argument;
import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.ResourceDescriptor;
import com.jaspersoft.jasperserver.api.search.SearchFilter;
import com.jaspersoft.jasperserver.remote.ResourceHandler;
import com.jaspersoft.jasperserver.remote.ServiceException;
import com.jaspersoft.jasperserver.remote.ServicesConfiguration;
import com.jaspersoft.jasperserver.remote.handlers.ReportUnitHandler;
import com.jaspersoft.jasperserver.remote.services.ResourcesListRemoteService;
import com.jaspersoft.jasperserver.remote.services.ResourcesManagementRemoteService;
import com.jaspersoft.jasperserver.search.common.ResourceDetails;
import com.jaspersoft.jasperserver.search.filter.FolderFilter;
import com.jaspersoft.jasperserver.search.mode.SearchMode;
import com.jaspersoft.jasperserver.search.service.RepositorySearchCriteria;
import com.jaspersoft.jasperserver.search.service.RepositorySearchService;
import com.jaspersoft.jasperserver.search.service.impl.RepositorySearchCriteriaImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Login REST service
 * The dirty job of loggin a user and sending out an error is done by the RESTLoginAuthenticationFilter.
 * This service just return a succesful login message
 *
 * @author gtoffoli
 * @version $Id$
 */
@Component("resourcesListRemoteService")
public class ResourcesListRemoteServiceImpl implements ResourcesListRemoteService {

    private final static Log log = LogFactory.getLog(ResourcesListRemoteServiceImpl.class);
    @jakarta.annotation.Resource
    private ResourcesManagementRemoteService resourcesManagementRemoteService;
    @jakarta.annotation.Resource(name = "concreteRepository")
    private RepositoryService repository;
    @jakarta.annotation.Resource(name = "remoteServiceConfiguration")
    private ServicesConfiguration servicesConfiguration;
    @jakarta.annotation.Resource
    private RepositorySearchService repositorySearchService;
    @jakarta.annotation.Resource
    private FolderFilter folderFilter;

    /**
     * Return a list of ResourceDescriptor(s)
     *
     * @param uri
     * @return
     * @throws ServiceException
     */
    public List listResources(String uri) throws ServiceException {
        return listResources(uri, 0);
    }

    /**
     * @param uri
     * @param maxItems - The maximum number of items (or 0 to get all the items)
     * @return
     * @throws ServiceException
     */
    public List<ResourceDescriptor> listResources(String uri, int maxItems) throws ServiceException {
        if (log.isDebugEnabled()) {
            log.debug("list for uri: " + uri);
            log.debug("Max items: " + maxItems);
        }

        // The result of our list action
        List<ResourceDescriptor> listOfResources = new ArrayList<ResourceDescriptor>();

        // The resource to list based on the uri
        Resource resource = resourcesManagementRemoteService.locateResource(uri);

        // If the uri indicates the root, just list the root directory
        if (resource == null) {
            if (log.isWarnEnabled()) {
                log.warn("No resource " + uri + " found");
            }
            throw new ServiceException(ServiceException.RESOURCE_NOT_FOUND, "Invalid uri or not existing resource");
        }

        if (resource instanceof Folder) {
            List folders = repository.getSubFolders(null, uri);
            // This filters with object level security.
            // Will only get folders the user has access to
            filterFolderList(folders);

            if (folders == null) return listOfResources;

            // We avoid to read or cache the attachments if we are just listing our resources...
            Map options = new HashMap();
            options.put(Argument.NO_RESOURCE_DATA_ATTACHMENT, Boolean.TRUE);

            for (int i = 0; i < folders.size(); ++i) {
                Resource folderRes = (Resource) folders.get(i);
                listOfResources.add(resourcesManagementRemoteService.createResourceDescriptor(folderRes));
            }

            // create a criteria for finding things with a common parent folder.
            FilterCriteria filterCriteria = new FilterCriteria();
            filterCriteria.addFilterElement(FilterCriteria.createParentFolderFilter(uri));

            // This filters with object level security
            // Will only get resources the user has access to

            List units = repository.loadClientResources(filterCriteria);

            if (units != null) {
                for (Iterator it = units.iterator(); units != null && it.hasNext(); ) {
                    Resource fileRes = (Resource) it.next();
                    try {
                        listOfResources.add(resourcesManagementRemoteService.createResourceDescriptor(fileRes, options));
                    } catch (Exception ex) {
                        log.error(ex);
                    }
                }
            }
        } else if (resource instanceof ReportUnit) {
            // The list inside a report unit should return the whole content of the report unit.
            // For this porpuse, we force some special parameters to look into the report unit...
            Map options = new HashMap();
            options.put(ReportUnitHandler.OPTION_REPORT_UNIT_CONTENTS, Boolean.TRUE);
            listOfResources = resourcesManagementRemoteService.createResourceDescriptor(resourcesManagementRemoteService.locateResource(uri)).getChildren();
        }

        // check for max resources...
        if (maxItems > 0 && maxItems < listOfResources.size()) {
            if (log.isDebugEnabled()) {
                log.debug("There are " + listOfResources.size() + " found. Getting indexes from 0 to " + maxItems);
            }
            listOfResources = listOfResources.subList(0, maxItems);
        }

        return listOfResources;
    }

    /**
     * Get resources which satisfy the given criteria.
     * Since there is not a good or precise way to filter the type of resource,
     * this method allows to provide a specific wsType (that can be null).
     *
     * @param criteria
     * @param maxItems number of maximum items returned (0 for not set a limit)
     * @param wsTypes  - Can be null. It is the list of wsType allowed in the returned resources.
     * @return
     * @throws ServiceException
     */
    public List<ResourceDescriptor> getResources(FilterCriteria criteria, int maxItems, List<String> wsTypes) throws ServiceException {
        // The result of our list action
        List<ResourceDescriptor> listOfResources = new ArrayList<ResourceDescriptor>();

        if (criteria == null) return listOfResources;
        @SuppressWarnings("unchecked")
        List<Resource> lookups = repository.loadClientResources(criteria);
        if (lookups != null && !lookups.isEmpty()) {

            for (Resource currentResource : lookups) {
                ResourceDescriptor rd = resourcesManagementRemoteService.createResourceDescriptor(currentResource);
                if (wsTypes == null || wsTypes.contains(rd.getWsType())) {
                    listOfResources.add(rd);
                    if (maxItems > 0 && listOfResources.size() == maxItems) {
                        break;
                    }
                }
            }
        }
        return listOfResources;
    }

    protected List<ResourceDescriptor> convertToResourceDescriptors(List<Resource> resources) {
        List<ResourceDescriptor> result = null;
        if (resources != null && !resources.isEmpty()) {
            result = new ArrayList<ResourceDescriptor>();
            for (Resource currentResource : resources) {
                try {
                    ResourceDescriptor resourceDescriptor = resourcesManagementRemoteService.createResourceDescriptor(currentResource);
                    if (resourceDescriptor != null)
                        result.add(resourceDescriptor);
                } catch (Exception ex) {
                    // details of this resource are corrupted or not fully accessible, just exclude from results and log an error.
                    log.error("Couldn't build resource descriptor for resource " + currentResource.getURI() +
                            ". Resource is excluded from search results", ex);
                    continue;
                }
            }
        }
        return result;
    }

    public List getResources(String uri, String queryString, List<String> wsTypes, boolean recursive, int maxItems, int startIndex) throws ServiceException {
        final RepositorySearchCriteria repositorySearchCriteria = new RepositorySearchCriteriaImpl.Builder().setFolderUri(uri)
                .setSearchText(queryString).setStartIndex(startIndex).setMaxCount(maxItems).setExcludeFolders(true)
                .setSearchMode(recursive ? SearchMode.SEARCH : SearchMode.BROWSE).setSortBy("name").getCriteria();
        fillCriteriaByHandlers(uri, queryString, wsTypes, recursive, maxItems, startIndex, repositorySearchCriteria);
        if(!("/".equals(uri) && recursive)){
            // folderFilter (CE) is explicitly added to override proFolderFilter functionality (i.e. public folder is added always as additional search target).
            // Public folder needs to be included to search results only if it is explicitly requested
            // or recursive search in root is requested
            repositorySearchCriteria.addCustomFilter(folderFilter);
        }
        final List<ResourceDetails> results = repositorySearchService.getResults(ExecutionContextImpl.getRuntimeExecutionContext(), repositorySearchCriteria);
        return convertToResourceDescriptors((List) results);
    }

    protected void fillCriteriaByHandlers(String uri, String queryString, List<String> wsTypes, boolean recursive, int maxItems, int startIndex, RepositorySearchCriteria repositorySearchCriteria){
        if (wsTypes != null && !wsTypes.isEmpty()) {
            List<String> resourceTypes = repositorySearchCriteria.getResourceTypes();
            if(resourceTypes == null){
                resourceTypes = new ArrayList<String>();
                repositorySearchCriteria.setResourceTypes(resourceTypes);
            }
            for (String currentWsType : wsTypes) {
                // The resource type is the one defined as wsType, so we need to translate it in a real class...
                ResourceHandler resHandler = resourcesManagementRemoteService.getHandler(currentWsType);
                if (resHandler == null) {
                    throw new ServiceException(ServiceException.RESOURCE_BAD_REQUEST, "Invalid resource type: " + currentWsType);
                }
                resourceTypes.add(resHandler.getResourceType().getName());
                SearchFilter customSearchFilter = resHandler.getSearchFilter(uri, queryString, currentWsType, recursive, maxItems, startIndex);
                if(customSearchFilter != null){
                    repositorySearchCriteria.addCustomFilter(customSearchFilter);
                }
            }
        }
    }

    private void filterFolderList(List folderList) {
        if (folderList == null || folderList.isEmpty()) {
            return;
        }

        Set<String> roles = getCurrentUserRoles();
        for (Iterator i = folderList.iterator(); i.hasNext(); ) {
            Folder folder = (Folder) i.next();
            if (servicesConfiguration.getTempFolder().equals(folder.getURIString())) {
                boolean accessDenied = true;
                if (roles != null && roles.size() > 0) {
                    for (String role : roles) {
                        if (servicesConfiguration.getRoleToAccessTempFolder().equals(role)) {
                            accessDenied = false;
                            break;
                        }
                    }
                }

                if (accessDenied) {
                    i.remove();
                }
            }
        }
    }


    private Set<String> getCurrentUserRoles() {
        Set<String> roleNames = new HashSet<String>();

        Authentication authenticationToken = SecurityContextHolder.getContext().getAuthentication();
        if (authenticationToken == null) {
            return roleNames;
        }

        if (authenticationToken.getPrincipal() instanceof UserDetails) {
            UserDetails contextUserDetails = (UserDetails) authenticationToken.getPrincipal();
            for (GrantedAuthority authority : contextUserDetails.getAuthorities()) {
                roleNames.add(authority.getAuthority());
            }

        }

        return roleNames;
    }
}

