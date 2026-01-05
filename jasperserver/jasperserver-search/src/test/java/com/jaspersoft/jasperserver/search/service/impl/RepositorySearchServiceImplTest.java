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

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.logging.diagnostic.domain.DiagnosticAttribute;
import com.jaspersoft.jasperserver.api.logging.diagnostic.domain.DiagnosticAttributeImpl;
import com.jaspersoft.jasperserver.api.logging.diagnostic.helper.DiagnosticAttributeBuilder;
import com.jaspersoft.jasperserver.api.logging.diagnostic.service.DiagnosticCallback;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceLookup;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.HibernateRepositoryServiceImpl;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.api.search.SearchCriteriaFactory;
import com.jaspersoft.jasperserver.api.search.SearchFilter;
import com.jaspersoft.jasperserver.api.search.SearchSorter;
import com.jaspersoft.jasperserver.api.search.TransformerFactory;
import com.jaspersoft.jasperserver.search.common.RepositorySearchConfiguration;
import com.jaspersoft.jasperserver.search.filter.ResourceTypeSearchCriteriaFactory;
import com.jaspersoft.jasperserver.search.mode.SearchMode;
import com.jaspersoft.jasperserver.search.mode.SearchModeSettings;
import com.jaspersoft.jasperserver.search.mode.SearchModeSettingsResolver;
import com.jaspersoft.jasperserver.search.service.RepositorySearchCriteria;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
public class RepositorySearchServiceImplTest {

    private RepositorySearchServiceImpl repositorySearchService = spy(new RepositorySearchServiceImpl());

    private RepositorySearchConfiguration configuration = mock(RepositorySearchConfiguration.class);

    private SearchModeSettings searchModeSettings = mock(SearchModeSettings.class);

    private SearchModeSettingsResolver resolver = mock(SearchModeSettingsResolver.class);

    @Before
    public void setUp() {
        doReturn(configuration).when(searchModeSettings).getRepositorySearchConfiguration();
        doReturn(searchModeSettings).when(resolver).getSettings(eq(SearchMode.SEARCH));
    }

    @Test
    public void getResultsFromSearchCriteriaWithFolders() {
        final String resourceTypeToCheck = Folder.class.getName();
        final ResourceTypeSearchCriteriaFactory defaultSearchCriteriaFactory = new ResourceTypeSearchCriteriaFactory();

        RepositoryService repositoryService = new HibernateRepositoryServiceImpl() {
            @Override
            public List<ResourceLookup> getResources(ExecutionContext context, SearchCriteriaFactory searchCriteriaFactory, List<SearchFilter> filters, SearchSorter sorter, TransformerFactory transformerFactory, int current, int max) {
                assertEquals(ResourceLookup.class.getName(), ((ResourceTypeSearchCriteriaFactory) searchCriteriaFactory).getResourceType());
                return new ArrayList<>();
            }
        };

        RepositorySearchCriteria searchCriteria = new RepositorySearchCriteriaImpl.Builder()
                .setSearchMode(SearchMode.SEARCH)
                .setResourceTypes(resourceTypeToCheck)
                .getCriteria();

        RepositorySearchServiceImpl service = new RepositorySearchServiceImpl();
        service.setSearchModeSettingsResolver(resolver);
        service.setDefaultSearchCriteriaFactory(defaultSearchCriteriaFactory);
        service.setRepositoryService(repositoryService);
        service.getResults(null, searchCriteria);

        assertFalse(searchCriteria.getResourceTypes() == null || searchCriteria.getResourceTypes().isEmpty());
    }

    @Test
    public void getResultsFromSearchCriteriaWithoutFolders() {
        final String resourceTypeToCheck = ReportUnit.class.getName();

        final ResourceTypeSearchCriteriaFactory defaultSearchCriteriaFactory = new ResourceTypeSearchCriteriaFactory();
        RepositoryService repositoryService = new HibernateRepositoryServiceImpl() {
            @Override
            public List<ResourceLookup> getResources(ExecutionContext context, SearchCriteriaFactory searchCriteriaFactory, List<SearchFilter> filters, SearchSorter sorter, TransformerFactory transformerFactory, int current, int max) {
                assertEquals(Resource.class.getName(), ((ResourceTypeSearchCriteriaFactory) searchCriteriaFactory).getResourceType());
                return new ArrayList<>();
            }
        };


        RepositorySearchCriteria searchCriteria = new RepositorySearchCriteriaImpl.Builder()
                .setSearchMode(SearchMode.SEARCH)
                .setResourceTypes(resourceTypeToCheck)
                .setExcludeFolders(true).getCriteria();

        RepositorySearchServiceImpl service = new RepositorySearchServiceImpl();
        service.setSearchModeSettingsResolver(resolver);
        service.setDefaultSearchCriteriaFactory(defaultSearchCriteriaFactory);
        service.setRepositoryService(repositoryService);
        service.getResults(null, searchCriteria);

        assertFalse(searchCriteria.getResourceTypes() == null || searchCriteria.getResourceTypes().isEmpty());
    }

    @Test
    public void getResultsFromSearchCriteriaWithoutFolders_folder() {
        final String resourceTypeToCheck = Folder.class.getName();

        final ResourceTypeSearchCriteriaFactory defaultSearchCriteriaFactory = new ResourceTypeSearchCriteriaFactory();
        RepositoryService repositoryService = new HibernateRepositoryServiceImpl() {
            @Override
            public List<ResourceLookup> getResources(ExecutionContext context, SearchCriteriaFactory searchCriteriaFactory, List<SearchFilter> filters, SearchSorter sorter, TransformerFactory transformerFactory, int current, int max) {
                assertEquals(Resource.class.getName(), ((ResourceTypeSearchCriteriaFactory) searchCriteriaFactory).getResourceType());
                return new ArrayList<>();
            }
        };

        RepositorySearchCriteria searchCriteria = new RepositorySearchCriteriaImpl.Builder()
                .setSearchMode(SearchMode.SEARCH)
                .setResourceTypes(resourceTypeToCheck)
                .setExcludeFolders(true)
                .getCriteria();

        RepositorySearchServiceImpl service = new RepositorySearchServiceImpl();
        service.setSearchModeSettingsResolver(resolver);
        service.setDefaultSearchCriteriaFactory(defaultSearchCriteriaFactory);
        service.setRepositoryService(repositoryService);
        service.getResults(null, searchCriteria);
        assertFalse(searchCriteria.getResourceTypes() == null || searchCriteria.getResourceTypes().isEmpty());
    }

    @Test
    public void getDiagnosticDataTest() {
        //Setup mock behavior
        RepositoryService repositoryService = mock(RepositoryService.class);
        Map<String, List<String>> filterOptionToResourceTypes = spy(new HashMap<>());
        SearchCriteriaFactory defaultSearchCriteriaFactory = mock(SearchCriteriaFactory.class);
        TransformerFactory transformerFactory = mock(TransformerFactory.class);

        repositorySearchService.setRepositoryService(repositoryService);
        repositorySearchService.setFilterOptionToResourceTypes(filterOptionToResourceTypes);
        repositorySearchService.setDefaultSearchCriteriaFactory(defaultSearchCriteriaFactory);
        repositorySearchService.setTransformerFactory(transformerFactory);

        doReturn(configuration).when(repositorySearchService).getConfiguration(any(SearchMode.class));

        SearchCriteriaFactory factory = mock(SearchCriteriaFactory.class);
        doReturn(factory).when(defaultSearchCriteriaFactory).newFactory(anyString());

        SearchFilter searchFilter1 = mock(SearchFilter.class);
        SearchFilter searchFilter2 = mock(SearchFilter.class);
        SearchFilter searchFilter3 = mock(SearchFilter.class);

        ArrayList<SearchFilter> allFiltersList = new ArrayList<>();
        allFiltersList.add(searchFilter1);
        allFiltersList.add(searchFilter2);
        allFiltersList.add(searchFilter3);

        ExecutionContext executionContext = mock(ExecutionContext.class);

        doReturn(allFiltersList).when(repositorySearchService).createAllFiltersList(eq(configuration), any(RepositorySearchCriteria.class));
        doReturn(executionContext).when(repositorySearchService).putCriteriaToContext(nullable(ExecutionContext.class), any(RepositorySearchCriteria.class));

        Map<DiagnosticAttribute, DiagnosticCallback> resultDiagnosticData = repositorySearchService.getDiagnosticData();

        // Testing total size of diagnostic attributes collected from SessionRegistryDiagnosticService
        assertEquals(4, resultDiagnosticData.size());

        doReturn(30).when(repositoryService).getResourcesCount(eq(executionContext), eq(factory), eq(allFiltersList), nullable(SearchSorter.class), eq(transformerFactory));

        // Test getting total report count
        int totalReportCount = (Integer) resultDiagnosticData.
                get(new DiagnosticAttributeImpl(DiagnosticAttributeBuilder.TOTAL_REPORTS_COUNT, null, null))
                .getDiagnosticAttributeValue();

        verify(filterOptionToResourceTypes).get("resourceTypeFilter-reports");
        assertEquals(30, totalReportCount);

        // Refresh getResourceCount for repository service mock
        reset(repositoryService);
        doReturn(40).when(repositoryService).getResourcesCount(eq(executionContext), eq(factory), eq(allFiltersList), nullable(SearchSorter.class), eq(transformerFactory));

        // Test getting total reports output count
        int totalReportsOutputCount = (Integer) resultDiagnosticData.
                get(new DiagnosticAttributeImpl(DiagnosticAttributeBuilder.TOTAL_REPORT_OUTPUTS_COUNT, null, null))
                .getDiagnosticAttributeValue();

        verify(filterOptionToResourceTypes).get("resourceTypeFilter-reportOutput");
        assertEquals(40, totalReportsOutputCount);

        // Refresh getResourceCount for repository service mock
        reset(repositoryService);
        doReturn(50).when(repositoryService).getResourcesCount(eq(executionContext), eq(factory), eq(allFiltersList), nullable(SearchSorter.class), eq(transformerFactory));

        //Refresh getResourceCount for repository service mock
        reset(repositoryService);
        doReturn(60).when(repositoryService).getResourcesCount(eq(executionContext), eq(factory), eq(allFiltersList), nullable(SearchSorter.class), eq(transformerFactory));

        //Test getting total data sources count
        int totalDataSourcesCount = (Integer) resultDiagnosticData.
                get(new DiagnosticAttributeImpl(DiagnosticAttributeBuilder.TOTAL_DATA_SOURCES_COUNT, null, null)).getDiagnosticAttributeValue();

        verify(filterOptionToResourceTypes).get("resourceTypeFilter-dataSources");
        assertEquals(60, totalDataSourcesCount);

        doReturn(2).when(repositoryService).getFoldersCount(nullable(String.class));

        //Test getting total data sources count
        int totalFoldersCount = (Integer) resultDiagnosticData.
                get(new DiagnosticAttributeImpl(DiagnosticAttributeBuilder.TOTAL_FOLDERS_COUNT, null, null))
                .getDiagnosticAttributeValue();

        assertEquals(2, totalFoldersCount);
    }

}
