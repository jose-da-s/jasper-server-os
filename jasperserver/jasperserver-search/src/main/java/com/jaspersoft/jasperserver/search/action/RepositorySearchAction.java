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

package com.jaspersoft.jasperserver.search.action;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.util.StaticExecutionContextProvider;
import com.jaspersoft.jasperserver.api.engine.common.service.SecurityContextProvider;
import com.jaspersoft.jasperserver.api.engine.common.service.impl.NavigationActionModelSupport;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.RepositoryConfiguration;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceLookup;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.FavoriteResourceService;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.search.*;
import com.jaspersoft.jasperserver.search.common.*;
import com.jaspersoft.jasperserver.search.filter.TextFilter;
import com.jaspersoft.jasperserver.search.mode.SearchMode;
import com.jaspersoft.jasperserver.search.model.permission.Permission;
import com.jaspersoft.jasperserver.search.service.RepositorySearchService;
import com.jaspersoft.jasperserver.search.service.ResourceService;
import com.jaspersoft.jasperserver.search.state.State;
import com.jaspersoft.jasperserver.search.strategy.ResourceLoadStrategy;
import com.jaspersoft.jasperserver.search.util.JSONConverter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.webflow.core.collection.SharedAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Action class that contains actions related to repository search functionality.
 *
 * @author Yuriy Plakosh
 * @author Stas Chubar
 * @version $Id$
 */
public class RepositorySearchAction extends BaseSearchAction {
    protected final Log log = LogFactory.getLog(this.getClass());

    // Request scope attributes.
    private static final String ATTRIBUTE_ORGANIZATION_ID = "organizationId";
    private static final String ATTRIBUTE_PUBLIC_FOLDER_URI = "publicFolderUri";
    private static final String ATTRIBUTE_TEMP_FOLDER_URI = "tempFolderUri";
    private static final String ATTRIBUTE_ROOT_FOLDER_URI = "rootFolderUri";
    private static final String ATTRIBUTE_ORGANIZATIONS_FOLDER_URI = "organizationsFolderUri";
    private static final String ATTRIBUTE_FOLDER_SEPARATOR = "folderSeparator";
    private static final String ATTRIBUTE_AJAX_RESPONSE_MODEL = "ajaxResponseModel";
    private static final String ATTRIBUTE_STATE = "state";
    private static final String ATTRIBUTE_CONFIGURATION = "configuration";
    private static final String ATTRIBUTE_PERMISSIONS = "permissions";
    private static final String ATTRIBUTE_JSON_CONFIGURATION = "jsonConfiguration";
    private static final String ATTRIBUTE_MODE = "mode";
    private static final String ATTRIBUTE_SYSTEM_CONFIRM = "systemConfirm";
    private static final String ATTRIBUTE_IS_ANALYSIS_FEATURE_ENABLED = "isAnalysisFeatureEnabled";
    private static final String ATTRIBUTE_IS_DASHBOARD_FEATURE_ENABLED = "isDashboardFeatureEnabled";
    private static final String ATTRIBUTE_IS_ADHOC_FEATURE_ENABLED = "isAdHocFeatureEnabled";
    private static final String ATTRIBUTE_IS_FOLDER_SET = "isFolderSet";

    // Session attributes.
    private static final String ATTRIBUTE_REPOSITORY_SYSTEM_CONFIRM = "repositorySystemConfirm";

    // Request parameters.
    private static final String PARAMETER_ROLLBACK_POSITION = "position";
    private static final String PARAMETER_SORT_BY = "sortBy";
    private static final String PARAMETER_TEXT = "text";
    private static final String PARAMETER_FILTER_ID = "filterId";
    private static final String PARAMETER_FILTER_OPTION = "filterOption";
    private static final String PARAMETER_FOLDER_URI = "folderUri";
    private static final String PARAMETER_RESOURCE_TYPE = "resourceType";
    private static final String PARAMETER_RESOURCE_URI = "resourceUri";
    private static final String PARAMETER_SEARCH_TEXT = "searchText";
    private static final String PARAMETER_IS_FOLDER = "isFolder";

    protected SecurityContextProvider securityContextProvider;
    protected RepositoryConfiguration configuration;
    protected RepositorySearchService repositorySearchService;
    protected SearchCriteriaFactory searchCriteriaFactory;
    protected ResourceLoadStrategy resourceLoadStrategy;
    protected RepositoryService repository;
    protected RepositoryService unsecuredRepository;
    protected NavigationActionModelSupport navigationActionModelSupport;
    @jakarta.annotation.Resource(name = "${bean.favoriteResourceService}")
    protected FavoriteResourceService favoriteResourceService;

    @jakarta.annotation.Resource(name="resourceService")
    protected ResourceService resourceService;

    @jakarta.annotation.Resource
    protected TextFilter textFilter;

    protected boolean methodOverride = false;

    public void setSecurityContextProvider(SecurityContextProvider securityContextProvider) {
        this.securityContextProvider = securityContextProvider;
    }

    public void setConfiguration(RepositoryConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setRepositorySearchService(RepositorySearchService repositorySearchService) {
        this.repositorySearchService = repositorySearchService;
    }

    public void setSearchCriteriaFactory(SearchCriteriaFactory searchCriteriaFactory) {
        this.searchCriteriaFactory = searchCriteriaFactory;
    }

    public void setResourceLoadStrategy(ResourceLoadStrategy resourceLoadStrategy) {
        this.resourceLoadStrategy = resourceLoadStrategy;
    }


	public void setRepository(RepositoryService repository) {
		this.repository = repository;
	}

    public void setUnsecuredRepository(RepositoryService unsecuredRepository) {
        this.unsecuredRepository = unsecuredRepository;
    }

    public void setNavigationActionModelSupport(NavigationActionModelSupport navigationActionModelSupport) {
        this.navigationActionModelSupport = navigationActionModelSupport;
    }

    public Event init(RequestContext context) throws JSONException {
        initSearchHolder(context);
        initState(context);

        SearchMode mode = getMode(context);

        context.getRequestScope().put(ATTRIBUTE_ORGANIZATION_ID,
                securityContextProvider.getContextUser().getTenantId());
        context.getRequestScope().put(ATTRIBUTE_PUBLIC_FOLDER_URI, configuration.getPublicFolderUri());
        context.getRequestScope().put(ATTRIBUTE_TEMP_FOLDER_URI, configuration.getTempFolderUri());
        context.getRequestScope().put(ATTRIBUTE_ROOT_FOLDER_URI, Folder.SEPARATOR);
        context.getRequestScope().put(ATTRIBUTE_ORGANIZATIONS_FOLDER_URI, configuration.getOrganizationsFolderUri());
        context.getRequestScope().put(ATTRIBUTE_FOLDER_SEPARATOR, Folder.SEPARATOR);
        context.getRequestScope().put(ATTRIBUTE_STATE, getSearchHolder(context).getState(mode).toJson());
        context.getRequestScope().put(ATTRIBUTE_CONFIGURATION, getConfiguration(context));
        context.getRequestScope().put(ATTRIBUTE_PERMISSIONS, Permission.values());
        context.getRequestScope().put(ATTRIBUTE_JSON_CONFIGURATION,
                getConverter(context).createJSONConfiguration());
        context.getRequestScope().put(ATTRIBUTE_MODE, mode.toString());
        context.getRequestScope().put(ATTRIBUTE_IS_ANALYSIS_FEATURE_ENABLED, isAnalysisFeatureSupported());
        context.getRequestScope().put(ATTRIBUTE_IS_DASHBOARD_FEATURE_ENABLED, isFeatureSupported("DB"));
        context.getRequestScope().put(ATTRIBUTE_IS_ADHOC_FEATURE_ENABLED, isFeatureSupported("AHD"));

        SharedAttributeMap sessionMap = context.getExternalContext().getSessionMap();
        if (sessionMap.contains(ATTRIBUTE_REPOSITORY_SYSTEM_CONFIRM)) {
            context.getRequestScope().put(ATTRIBUTE_SYSTEM_CONFIRM,
                    sessionMap.get(ATTRIBUTE_REPOSITORY_SYSTEM_CONFIRM));
            sessionMap.remove(ATTRIBUTE_REPOSITORY_SYSTEM_CONFIRM);
        }

        return success();
    }

    @Override
    protected void initState(RequestContext context) {
        SearchHolder searchHolder = getSearchHolder(context);
        SearchMode mode = getMode(context);
        searchHolder.setLastMode(mode);

        String searchText = getDecodedRequestParameter(context, PARAMETER_SEARCH_TEXT);
        boolean isNewSearch = searchText != null;
        State state = searchHolder.getState(mode);

        // Creating state if necessary.
        if (state == null || isNewSearch) {
            state = getInitialStateResolver(context).getInitialState(getConfiguration(context));

            if (isNewSearch) {
                state.updateText(searchText);
            }

            searchHolder.putState(mode, state);
        }

        // Apply filters if set.
        String filterId = getParameter(context, PARAMETER_FILTER_ID);
        String filterOption = getParameter(context, PARAMETER_FILTER_OPTION);
        if (isFilterValid(context, filterId, filterOption)) {
            state.updateFilter(filterId, filterOption, isDefaultFilterOption(context, filterId, filterOption));
        }

        // Apply sorter if set.
        String sortBy = getParameter(context, PARAMETER_SORT_BY);
        if (isSorterValid(context, sortBy)) {
            state.updateSorter(sortBy);
        }

        // Apply folder if set (BROWSE mode only).
        String folderUri = getParameter(context, PARAMETER_FOLDER_URI);
        if (mode == SearchMode.BROWSE && isFolderValid(folderUri)) {
            state.updateFolder(folderUri);
            context.getRequestScope().put(ATTRIBUTE_IS_FOLDER_SET, true);
        }
    }

    public Event isServerAvailable(RequestContext context) throws Exception {
        context.getRequestScope().put(ATTRIBUTE_AJAX_RESPONSE_MODEL,
                getConverter(context).createOKJSONResponse("Yes"));
        return success();
    }

    public Event getDisplayPath(RequestContext context) throws JSONException {
        String resourceUri = getParameter(context, PARAMETER_RESOURCE_URI);
        Boolean isFolder = (Boolean)getParameter(context, PARAMETER_IS_FOLDER, Boolean.class);
        if (isFolder == null) {
            isFolder = false;
        }

        String displayResourceUri;
        if (!StringUtils.isEmpty(resourceUri)) {
            displayResourceUri = getDisplayPath(context, resourceUri, isFolder);
        } else {
            displayResourceUri = resourceUri;
        }

        context.getRequestScope().put(ATTRIBUTE_AJAX_RESPONSE_MODEL,
                getConverter(context).createOKJSONResponse(displayResourceUri));

        return success();
    }

    private String getDisplayPath(RequestContext context, String uri, boolean isFolder) {
        ExecutionContext executionContext = exContext(context);
        Resource resource;
        if (isFolder) {
            resource = unsecuredRepository.getFolder(executionContext, uri);
        } else {
            resource = unsecuredRepository.getResource(executionContext, uri);
        }

        String displayPath = "";
        if (resource != null) {
            displayPath = getDisplayPath(resource.getName(), "");
        } else {
            return displayPath;
        }

        String parentUri = resource.getParentFolder();
        while (parentUri != null) {
            Folder folder = unsecuredRepository.getFolder(executionContext, parentUri);
            parentUri = folder.getParentURI();
            displayPath = getDisplayPath(folder.getName(), displayPath);
        }

        return displayPath;
    }

    private String getDisplayPath(String name, String displayPath) {
        if (Folder.SEPARATOR.equals(name) && displayPath.isEmpty()) {
            return Folder.SEPARATOR;
        } else if (Folder.SEPARATOR.equals(name)) {
            return displayPath;
        } else {
            return Folder.SEPARATOR + name + displayPath;
        }
    }

    public Event search(RequestContext context) throws JSONException {
        String text = getParameter(context, PARAMETER_TEXT);

        getSearchHolder(context).getState(getMode(context)).updateText(text);

        return success();
    }

    public Event sort(RequestContext context) throws JSONException {
        String sortBy = getParameter(context, PARAMETER_SORT_BY);

        if (isSorterValid(context, sortBy)) {
            getSearchHolder(context).getState(getMode(context)).updateSorter(sortBy);
        } else {
            return error();
        }

        return success();
    }

    public Event filter(RequestContext context) throws JSONException {
        String filterId = getParameter(context, PARAMETER_FILTER_ID);
        String filterOption = getParameter(context, PARAMETER_FILTER_OPTION);

        if (isFilterValid(context, filterId, filterOption)) {
            getSearchHolder(context).getState(getMode(context)).updateFilter(filterId, filterOption,
                    isDefaultFilterOption(context, filterId, filterOption));
        } else {
            return error();
        }

        return success();
    }

    public Event browse(RequestContext context) throws JSONException {
        String folderUri = getParameter(context, PARAMETER_FOLDER_URI);

        getSearchHolder(context).getState(getMode(context)).updateFolder(folderUri);

        return success();
    }

    public Event rollback(RequestContext context) {
        int rollbackPosition = (Integer) getParameter(context, PARAMETER_ROLLBACK_POSITION, Integer.class);

        getSearchHolder(context).getState(getMode(context)).rollback(rollbackPosition, getConfiguration(context));

        return success();
    }

    public Event next(RequestContext context) {
        SearchMode mode = getMode(context);
        State state = getSearchHolder(context).getState(mode);
        List<SearchFilter> filters = createAllFiltersList(context);
        ExecutionContext executionContext = exContext(context);

        // Updating results count if required.
        if (state.getResultIndex() == 0) {
            int resultsCount = 0;

            if (!this.methodOverride) {
                try {
                    resultsCount = repositorySearchService.getResultsCount(executionContext, searchCriteriaFactory,
                            filters, getSorter(context, state.getSortBy()));
                } catch (InvalidDataAccessResourceUsageException exception) {
                    this.methodOverride = true;
                    return next(context);
                }
            } else {
                // See http://bugzilla.jaspersoft.com/show_bug.cgi?id=31126
                // Fixes ReportOptions quantity problem with MS SQL
                List<SearchFilter> safeFilters = new ArrayList<SearchFilter>(filters.size());
                for (SearchFilter filter : filters) {
                    safeFilters.add(filter instanceof TextFilter ? textFilter : filter);
                }

                ExecutionContext safeContext = exContext(context);
                safeContext.getAttributes().add(QueryModificationEvaluator.FORCE_REPO_RESOURCE);

                resultsCount = repositorySearchService.getResultsCount(safeContext, searchCriteriaFactory,
                        safeFilters, getSorter(context, state.getSortBy()));
            }

            state.updateResultState(0, resultsCount);
        }

        List<ResourceDetails> results;
        if (state.getResultIndex() >= state.getResultsCount()) {
            results = Collections.emptyList();
        } else {
            results = getResults(context, executionContext, state, filters);
        }

        try {
            JSONConverter converter = getConverter(context);
            JSONObject jsonResults = converter.createResult(results, state,
                    state.getFilterPath(getConfiguration(context), messages));
            JSONObject response = converter.createJSONResponse(jsonResults);

            context.getRequestScope().put(ATTRIBUTE_AJAX_RESPONSE_MODEL, response.toString());
        } catch (JSONException e) {
            return error(e);
        }

        return success();
    }

    protected List<ResourceDetails> getResults(RequestContext context, ExecutionContext executionContext, State state, List<SearchFilter> filters) {
        RepositorySearchConfiguration repositorySearchConfiguration = getConfiguration(context);
        int itemsPerPage = repositorySearchConfiguration.getItemsPerPage();

        List<ResourceDetails> results = new ArrayList<ResourceDetails>(itemsPerPage * 2);
        do {
            int itemsToLoad = resourceLoadStrategy.getItemsToLoadCount(itemsPerPage, results.size());

            List<ResourceLookup> accessibleResults = performSearch(context, executionContext, state, filters, itemsToLoad);
            List<String> uriList = new ArrayList<>(accessibleResults.size());

            for (Resource resource : accessibleResults) {
                results.add(resourceService.getResourceDetails(resource));
                uriList.add(resource.getURIString());
            }
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<String> favResources = favoriteResourceService.getFavoritesURIList(user, uriList);
            for (ResourceDetails resource : results) {
                if (favResources.stream().anyMatch(r -> r.equals(resource.getURIString()))) {
                        resource.setFavorite();
                }
            }
            String favoriteFilter = state.getCustomFiltersMap().get("favoriteFilter");
            if (favoriteFilter != null && favoriteFilter.equals("favoriteFilter-favorites")) {
                for (ResourceDetails resource : results)
                    if(resource.hasChildren() && !hasFavoriteChildren(resource)){
                        resource.setHasChildren(false);
                    }
            }
            state.updateResultState(state.getResultIndex() + itemsToLoad, state.getResultsCount());
        } while (results.size() < itemsPerPage && state.getResultIndex() < state.getResultsCount());

        return results;
    }

    private boolean hasFavoriteChildren(ResourceDetails resource) {
        List<ResourceDetails> childResourceList = repositorySearchService.getResourceChildren(resource.getResourceType(), resource.getURIString());
        List<String> childResourceURIList = childResourceList.stream().map(res->res.getURIString()).collect(Collectors.toList());
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> favResources = favoriteResourceService.getFavoritesURIList(user, childResourceURIList);
        return favResources.isEmpty()?false:true;
    }

    protected List<ResourceLookup> performSearch(RequestContext context, ExecutionContext executionContext, State state, List<SearchFilter> filters, int itemsToLoad) {
        return repositorySearchService.getLookups(executionContext,
                searchCriteriaFactory, filters, getSorter(context, state.getSortBy()),
                state.getResultIndex(), itemsToLoad);
    }

    @SuppressWarnings({"unchecked"})
    private ExecutionContext exContext(RequestContext context) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ExecutionContext exContext = StaticExecutionContextProvider.getExecutionContext();

        if (exContext.getAttributes() == null) {
            exContext.setAttributes(new ArrayList());
        }

        exContext.getAttributes().add(authentication.getPrincipal());

        SearchMode mode = getMode(context);
        SearchAttributes searchAttributes = new SearchAttributes();
        searchAttributes.setMode(mode);
        searchAttributes.setState(getSearchHolder(context).getState(mode));

        exContext.getAttributes().add(searchAttributes);

        return exContext;
	}

    public SearchSorter getSorter(RequestContext context, String id) {
        for (CustomSorter sorter : getConfiguration(context).getCustomSorters()) {
            if (sorter.getId().equals(id)) {
                return sorter.getSearchSorter();
            }
        }

        return null;
    }

    private List<SearchFilter> createAllFiltersList(RequestContext context) {
        List<SearchFilter> filterList = new ArrayList<SearchFilter>();

        RepositorySearchConfiguration configuration = getConfiguration(context);

        filterList.addAll(configuration.getSystemFilters());
        filterList.addAll(getRestrictionsFilters(context));

        return filterList;
    }

    private List<SearchFilter> getRestrictionsFilters(RequestContext context) {
        SearchMode mode = getMode(context);
        State state = getSearchHolder(context).getState(mode);
        List<SearchFilter> filters = new ArrayList<SearchFilter>();

        for (Map.Entry<String, String> entry : state.getCustomFiltersMap().entrySet()) {
            for (CustomFilter filter : getConfiguration(context).getCustomFilters()) {
                if (filter.getId().equals(entry.getKey())) {
                    filters.add(filter.getFilter());
                }
            }
        }

        return filters;
    }

    private boolean isDefaultFilterOption(RequestContext context, String filterId, String filterOption) {
        RepositorySearchConfiguration configuration = getConfiguration(context);

        for (CustomFilter customFilter : configuration.getCustomFilters()) {
            if (customFilter.getId().equals(filterId)) {
                return customFilter.getDefaultOption().equals(filterOption);
            }
        }

        return false;
    }

    private boolean isFilterValid(RequestContext context, String filterId, String filterOption) {
        if (filterId != null && filterOption != null) {
            RepositorySearchConfiguration configuration = getConfiguration(context);

            for (CustomFilter customFilter : configuration.getCustomFilters()) {
                if (customFilter.getId().equals(filterId)) {
                    for (Option option : customFilter.getOptions()) {
                        if (option.getId().equals(filterOption)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean isSorterValid(RequestContext context, String sortBy) {
        if (sortBy != null) {
            RepositorySearchConfiguration configuration = getConfiguration(context);

            for (CustomSorter customSorter : configuration.getCustomSorters()) {
                if (customSorter.getId().equals(sortBy)) {
                        return true;
                }
            }
        }

        return false;
    }

    private boolean isFolderValid(String folderUri) {
        return (folderUri != null && repository.folderExists(null, folderUri));
    }

    public Event getChildren(RequestContext context) throws Exception {
        JSONConverter jsonConverter = getConverter(context);
        JSONObject response;

        try {
            String resourceType = getParameter(context, PARAMETER_RESOURCE_TYPE);
            String resourceUri = getParameter(context, PARAMETER_RESOURCE_URI);

            List<ResourceDetails> resourceList = repositorySearchService.getResourceChildren(resourceType, resourceUri);
            List<String> uriList = new ArrayList<>(resourceList.size());
            for (ResourceDetails resource : resourceList) {
                uriList.add(resource.getURIString());
            }
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<String> favResources = favoriteResourceService.getFavoritesURIList(user, uriList);
            for (ResourceDetails resource : resourceList) {
                if (favResources.stream().anyMatch(r -> r.equals(resource.getURIString()))) {
                    resource.setFavorite();
                }
            }
            State state = getSearchHolder(context).getState(getMode(context));
            String favoriteFilter = state.getCustomFiltersMap().get("favoriteFilter");

            if (favoriteFilter != null && favoriteFilter.equals("favoriteFilter-favorites")) {
                List<ResourceDetails> favresourceList = resourceList.stream().filter(r->r.isFavorite()).collect(Collectors.toList());
                resourceList=favresourceList;
            }

            final JSONArray resources = jsonConverter.resourcesToJson(resourceList);

            response = jsonConverter.createJSONResponse(resources);
        } catch (Exception e) {
            response = jsonConverter.createErrorJSONResponse(getErrorMessage(e));
            log.error("SEARCH_ERROR: Can't load resource children.", e);
        }
        context.getRequestScope().put(ATTRIBUTE_AJAX_RESPONSE_MODEL, response.toString());

        return success();
    }

    protected Boolean isAnalysisFeatureSupported() {
        /* For CE analysis feature is always supported. */
        return true;
    }

    protected Boolean isFeatureSupported(String id) {
        return navigationActionModelSupport.isAvailableProFeature(id);
    }
}

