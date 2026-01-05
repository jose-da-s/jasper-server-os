/*
 * Copyright (C) 2025 the Jasper Server OS Authors
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

package com.jaspersoft.jasperserver.search.service.impl;

import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.logging.diagnostic.domain.DiagnosticAttribute;
import com.jaspersoft.jasperserver.api.logging.diagnostic.helper.DiagnosticAttributeBuilder;
import com.jaspersoft.jasperserver.api.logging.diagnostic.service.Diagnostic;
import com.jaspersoft.jasperserver.api.logging.diagnostic.service.DiagnosticCallback;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceLookup;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.search.SearchCriteriaFactory;
import com.jaspersoft.jasperserver.api.search.SearchFilter;
import com.jaspersoft.jasperserver.api.search.SearchSorter;
import com.jaspersoft.jasperserver.api.search.TransformerFactory;
import com.jaspersoft.jasperserver.search.common.CustomFilter;
import com.jaspersoft.jasperserver.search.common.CustomSorter;
import com.jaspersoft.jasperserver.search.common.RepositorySearchConfiguration;
import com.jaspersoft.jasperserver.search.common.ResourceDetails;
import com.jaspersoft.jasperserver.search.filter.AccessTypeFilter;
import com.jaspersoft.jasperserver.search.filter.ResourceTypeFilter;
import com.jaspersoft.jasperserver.search.filter.TextFilter;
import com.jaspersoft.jasperserver.search.mode.AccessType;
import com.jaspersoft.jasperserver.search.mode.SearchMode;
import com.jaspersoft.jasperserver.search.mode.SearchModeSettingsResolver;
import com.jaspersoft.jasperserver.search.service.ChildrenLoaderService;
import com.jaspersoft.jasperserver.search.service.RepositorySearchCriteria;
import com.jaspersoft.jasperserver.search.service.RepositorySearchResult;
import com.jaspersoft.jasperserver.search.service.RepositorySearchService;
import com.jaspersoft.jasperserver.search.service.ResourceService;
import com.jaspersoft.jasperserver.search.sorter.ByLabelSorter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link RepositorySearchService}.
 *
 * @author Yuriy Plakosh
 * @version $Id$
 */
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class RepositorySearchServiceImpl implements RepositorySearchService, Diagnostic {
    private static final Log log = LogFactory.getLog(RepositorySearchServiceImpl.class);

    protected RepositoryService repositoryService;
    private ResourceService resourceService;
    protected TransformerFactory transformerFactory;
    private Map<String, ChildrenLoaderService> childrenLoaders;
    protected SearchCriteriaFactory defaultSearchCriteriaFactory;
    private SearchModeSettingsResolver searchModeSettingsResolver;
    protected Map<String, List<String>> filterOptionToResourceTypes;
    protected ByLabelSorter sorter;

    @javax.annotation.Resource
    private TextFilter textFilter;

    @javax.annotation.Resource
    private AccessTypeFilter accessTypeFilter;

    public ByLabelSorter getSorter() {
        return sorter;
    }

    public void setSorter(ByLabelSorter sorter) {
        this.sorter = sorter;
    }

    public void setSearchModeSettingsResolver(SearchModeSettingsResolver searchModeSettingsResolver) {
        this.searchModeSettingsResolver = searchModeSettingsResolver;
    }

    public void setDefaultSearchCriteriaFactory(SearchCriteriaFactory defaultSearchCriteriaFactory) {
        this.defaultSearchCriteriaFactory = defaultSearchCriteriaFactory;
    }

    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public void setTransformerFactory(TransformerFactory transformerFactory) {
        this.transformerFactory = transformerFactory;
    }

    public void setFilterOptionToResourceTypes(Map<String, List<String>> filterOptionToResourceTypes) {
        this.filterOptionToResourceTypes = filterOptionToResourceTypes;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<ResourceDetails> getResults(ExecutionContext context, SearchCriteriaFactory searchCriteriaFactory,
                                            List<SearchFilter> filters, SearchSorter sorter, int current, int max) {
        List<ResourceLookup> resources = getLookups(context, searchCriteriaFactory, filters, sorter, current, max);
        return getResourceDetailsList(resources);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<ResourceDetails> getResults(ExecutionContext context, RepositorySearchCriteria criteria) {
        List<ResourceLookup> resources = getLookups(context, criteria);
        return getResourceDetailsList(resources);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<ResourceLookup> getLookups(ExecutionContext context, SearchCriteriaFactory searchCriteriaFactory, List<SearchFilter> filters, SearchSorter sorter, int current, int max) {
        return repositoryService.getResources(context, searchCriteriaFactory,
                filters, sorter, transformerFactory, current, max);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<ResourceLookup> getLookups(ExecutionContext context, RepositorySearchCriteria criteria) {
        context = putCriteriaToContext(context, criteria);
        // SearchMode.SEARCH is used by default
        final RepositorySearchConfiguration configuration = getConfiguration(criteria.getSearchMode() != null ? criteria.getSearchMode() : SearchMode.SEARCH);
        return getLookups(context, obtainFactory(criteria), createAllFiltersList(configuration, criteria), getSorter(configuration, criteria.getSortBy()), criteria.getStartIndex(), criteria.getMaxCount());
    }

    @SuppressWarnings("unchecked")
	@Override
    @Transactional(propagation = Propagation.REQUIRED)
    public RepositorySearchResult<ResourceLookup> getLookupsForFullPage(ExecutionContext context,
                                                                        RepositorySearchCriteria clientCriteria) {
        RepositorySearchAccumulator<ResourceLookup> result = null;

        RepositorySearchCriteria criteria;
        try {
            criteria = clientCriteria.clone();
        } catch (CloneNotSupportedException e) { throw new JSException(e.getMessage()); }

        if (criteria != null) {

            context = putCriteriaToContext(context, criteria);
            final SearchCriteriaFactory criteriaFactory = obtainFactory(criteria);
            final RepositorySearchConfiguration conf =
                    getConfiguration(criteria.getSearchMode() != null ? criteria.getSearchMode() : SearchMode.SEARCH);

            final List<SearchFilter> filters = createAllFiltersList(conf, criteria);
            final SearchSorter sorter = getSorter(conf, criteria.getSortBy());

            int startIndex = criteria.getStartIndex();
            int itemsPerPage = criteria.getMaxCount();
            int currentLimit = itemsPerPage;

            int totalCount = getResultsCount(null, criteria);
            if(startIndex>totalCount){
                return RepositorySearchAccumulator.EMPTY_RESULT;
            }
            result = new RepositorySearchAccumulator<ResourceLookup>(startIndex, itemsPerPage, totalCount);
            
            do {
                criteria.setStartIndex(startIndex);
                criteria.setMaxCount(currentLimit);

                List<ResourceLookup> currentLookups = repositoryService.getResources(context, criteriaFactory,
                        filters, sorter, transformerFactory, criteria.getStartIndex(), criteria.getMaxCount());

                debug(criteria, currentLookups, result);
                if (itemsPerPage == 0) {
                    result.fill(criteria, currentLookups);
                    break;
                } else if ((currentLookups.size() + result.size()) > itemsPerPage) {
                    currentLimit = itemsPerPage - result.size();
                } else {
                    result.fill(criteria, currentLookups);
                    startIndex = result.getNextOffset();
                    int nexLimit = currentLimit + (currentLimit - currentLookups.size());
                    currentLimit = (conf.getMaxItemsPerQuery() > 0 && conf.getMaxItemsPerQuery() < nexLimit)
                            ? conf.getMaxItemsPerQuery() : nexLimit;
                }
            } while (criteria.isForceFullPage() && startIndex < result.getTotalCount() && !result.isFull());

        }
        return result == null ? RepositorySearchAccumulator.EMPTY_RESULT : result;
    }

    private void debug(RepositorySearchCriteria criteria, List<ResourceLookup> lookups,
                       RepositorySearchAccumulator<ResourceLookup> result) {
        if (log.isDebugEnabled()) {
            log.debug("Searching -> \n" +
                    "startIndex: " + criteria.getStartIndex() +
                    ", limit: " + criteria.getMaxCount() +
                    " <> found: " + lookups.size() +
                    ", total: " + result.getTotalCount());
        }
    }

    public int getResultsCount(ExecutionContext context, RepositorySearchCriteria criteria) {
        context = putCriteriaToContext(context, criteria);
        // SearchMode.SEARCH is used by default
        final RepositorySearchConfiguration configuration = getConfiguration(criteria.getSearchMode() != null ? criteria.getSearchMode() : SearchMode.SEARCH);
        return getResultsCount(context, obtainFactory(criteria), createAllFiltersList(configuration, criteria), getSorter(configuration, criteria.getSortBy()));
    }

    @Override
    public Map<DiagnosticAttribute, DiagnosticCallback> getDiagnosticData() {
        final RepositorySearchCriteria repositorySearchCriteria = new RepositorySearchCriteriaImpl.Builder().setFolderUri("/")
                .setSearchText(null).setStartIndex(0).setMaxCount(0)
                .setSearchMode(SearchMode.SEARCH).setSortBy("name").getCriteria();
        final RepositorySearchConfiguration configuration = getConfiguration(repositorySearchCriteria.getSearchMode());
        final SearchCriteriaFactory factory = defaultSearchCriteriaFactory.newFactory(Resource.class.getCanonicalName());
        final SearchSorter searchSorter = this.getSorter();

        return new DiagnosticAttributeBuilder()
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.TOTAL_REPORTS_COUNT, new DiagnosticCallback<Integer>() {
                @Override
                public Integer getDiagnosticAttributeValue() {
                    final List<SearchFilter> allFiltersList = new ArrayList<SearchFilter>(createAllFiltersList(configuration, repositorySearchCriteria));
                    repositorySearchCriteria.setResourceTypes(filterOptionToResourceTypes.get("resourceTypeFilter-reports"));
                    return repositoryService.getResourcesCount(putCriteriaToContext(null, repositorySearchCriteria), factory, allFiltersList,
                            searchSorter, transformerFactory);
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.TOTAL_REPORT_OUTPUTS_COUNT, new DiagnosticCallback<Integer>() {
                @Override
                public Integer getDiagnosticAttributeValue() {
                    final List<SearchFilter> allFiltersList = new ArrayList<SearchFilter>(createAllFiltersList(configuration, repositorySearchCriteria));
                    repositorySearchCriteria.setResourceTypes(filterOptionToResourceTypes.get("resourceTypeFilter-reportOutput"));
                    return repositoryService.getResourcesCount(putCriteriaToContext(null, repositorySearchCriteria), factory, allFiltersList,
                            searchSorter, transformerFactory);
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.TOTAL_FOLDERS_COUNT, new DiagnosticCallback<Integer>() {
                @Override
                public Integer getDiagnosticAttributeValue() {
                    return repositoryService.getFoldersCount(null);
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.TOTAL_DATA_SOURCES_COUNT, new DiagnosticCallback<Integer>() {
                @Override
                public Integer getDiagnosticAttributeValue() {
                    final List<SearchFilter> allFiltersList = new ArrayList<SearchFilter>(createAllFiltersList(configuration, repositorySearchCriteria));
                    repositorySearchCriteria.setResourceTypes(filterOptionToResourceTypes.get("resourceTypeFilter-dataSources"));
                    return repositoryService.getResourcesCount(putCriteriaToContext(null, repositorySearchCriteria), factory, allFiltersList,
                            searchSorter, transformerFactory);
                }
            }).build();
    }

    /**
     * @param context  - execution context
     * @param criteria - search criteria
     * @deprecated criteria shouldn't be in context, temporary solution
     */
    protected ExecutionContext putCriteriaToContext(ExecutionContext context, RepositorySearchCriteria criteria) {
        ExecutionContext nullSafeContext = context != null ? context : ExecutionContextImpl.getRuntimeExecutionContext();
        if (nullSafeContext.getAttributes() == null)
            nullSafeContext.setAttributes(new ArrayList());
        nullSafeContext.getAttributes().add(criteria);
        return nullSafeContext;
    }

    public SearchSorter getSorter(RepositorySearchConfiguration configuration, String sortBy) {
        SearchSorter result = null;
        SearchSorter defaultSorter = null;
        for (CustomSorter sorter : configuration.getCustomSorters()) {
            if (sorter.getId().equals(sortBy)) {
                result = sorter.getSearchSorter();
                break;
            } else if(sorter.isDefault()){
                defaultSorter = sorter.getSearchSorter();
            }
        }
        return result != null ? result : defaultSorter;
    }

    protected RepositorySearchConfiguration getConfiguration(SearchMode searchMode) {
        return searchModeSettingsResolver.getSettings(searchMode).getRepositorySearchConfiguration();
    }

    protected List<SearchFilter> createAllFiltersList(RepositorySearchConfiguration configuration, RepositorySearchCriteria criteria) {
        List<SearchFilter> filterList = new ArrayList<SearchFilter>();
        filterList.addAll(configuration.getSystemFilters());
        filterList.addAll(getRestrictionsFilters(configuration));

        // Hack - pro text filter adds restriction, which replace found repost options with their reports
        //In order to avoid it, in Web services we changing text filter to CE text filer explicitly
        for (int aTextFilterIndex = 0; aTextFilterIndex < filterList.size(); aTextFilterIndex ++){
            if (filterList.get(aTextFilterIndex) instanceof TextFilter){
                filterList.remove(aTextFilterIndex);
                filterList.add(aTextFilterIndex, textFilter);
            }
        }

        // ES hack for RepoFolder lookup
        if(criteria.getLookupClass()!=null && criteria.getLookupClass().indexOf("RepoFolder")>0){
        	Iterator<SearchFilter> it = filterList.iterator();
        	while(it.hasNext()){
        		SearchFilter filter = it.next();
        		if(filter instanceof ResourceTypeFilter){
        			it.remove();
        		}
        	}
        }
        
        if (!criteria.getAccessType().equals(AccessType.ALL)){
            if (criteria.getCustomFilters() == null){
                criteria.setCustomFilters(new LinkedList<SearchFilter>());
            }
            criteria.getCustomFilters().add(accessTypeFilter);
        }

        if (criteria != null && criteria.getCustomFilters() != null && !criteria.getCustomFilters().isEmpty())
            filterList.addAll(criteria.getCustomFilters());

        return filterList;
    }

    protected List<SearchFilter> getRestrictionsFilters(RepositorySearchConfiguration configuration) {
        Set<String> customFilerIds = new HashSet<String>(configuration.getCustomFiltersMap().keySet());
        List<SearchFilter> filters = new ArrayList<SearchFilter>();
        if (customFilerIds != null && !customFilerIds.isEmpty())
            for (CustomFilter filter : configuration.getCustomFilters()) {
                if (customFilerIds.contains(filter.getId())) {
                    filters.add(filter.getFilter());
                }
            }
        return filters;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public int getResultsCount(ExecutionContext context, SearchCriteriaFactory searchCriteriaFactory,
                               List<SearchFilter> filters, SearchSorter sorter) {
        return repositoryService.getResourcesCount(context, searchCriteriaFactory, filters, sorter, transformerFactory);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<ResourceDetails> getResourceChildren(String type, String resourceUri) {
        ChildrenLoaderService childrenLoaderService = childrenLoaders.get(type);

        if (childrenLoaderService != null) {
            return getResourceDetailsList(childrenLoaderService.getChildren(resourceUri));
        }

        return Collections.emptyList();
    }

    private List<ResourceDetails> getResourceDetailsList(List<ResourceLookup> resources) {
        List<ResourceDetails> list = new ArrayList<ResourceDetails>();

        for (Resource resource : resources) {
            ResourceDetails resourceDetails = resourceService.getResourceDetails(resource);

            list.add(resourceDetails);
        }

        return list;
    }

    public void setChildrenLoaders(Map<String, ChildrenLoaderService> childrenLoaders) {
        this.childrenLoaders = childrenLoaders;
    }

    private SearchCriteriaFactory obtainFactory(RepositorySearchCriteria criteria){
        SearchCriteriaFactory res;
        if (criteria.isExcludeFolders() || !criteria.getAccessType().equals(AccessType.ALL)) {
            if (criteria.getResourceTypes() != null && criteria.getResourceTypes().size() == 1 && criteria.getCustomFilters() != null && !criteria.getCustomFilters().isEmpty()){
                res = defaultSearchCriteriaFactory.newFactory(criteria.getResourceTypes().get(0));
                criteria.getResourceTypes().clear();
            } else {
                res = defaultSearchCriteriaFactory.newFactory(null);
            }
        } else {
            String sortBy = criteria.getSortBy();
            res = defaultSearchCriteriaFactory.newFactory(criteria.getLookupClass()!=null? criteria.getLookupClass():StringUtils.equals(sortBy, PARAM_SORT_BY_POPULARITY) ?
                    Resource.class.getName() : ResourceLookup.class.getName());
        }

        return res;
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    public List getResultsCountList(ExecutionContext context, SearchCriteriaFactory searchCriteriaFactory,
                               List<SearchFilter> filters, SearchSorter sorter) {
        return repositoryService.getResourcesCountList(context, searchCriteriaFactory, filters, sorter, transformerFactory);
    }

    public List getResultsCountList(ExecutionContext context, RepositorySearchCriteria criteria) {
        context = putCriteriaToContext(context, criteria);
        // SearchMode.SEARCH is used by default
        final RepositorySearchConfiguration configuration = getConfiguration(criteria.getSearchMode() != null ? criteria.getSearchMode() : SearchMode.SEARCH);
        return getResultsCountList(context, obtainFactory(criteria), createAllFiltersList(configuration, criteria), getSorter(configuration, criteria.getSortBy()));
    }

    

}
