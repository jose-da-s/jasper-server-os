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

package com.jaspersoft.jasperserver.repository.test;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.FolderImpl;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.JdbcReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.JndiJdbcReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.api.search.SearchCriteriaFactory;
import com.jaspersoft.jasperserver.api.search.SearchFilter;
import com.jaspersoft.jasperserver.api.search.SearchSorter;
import com.jaspersoft.jasperserver.search.common.CustomFilter;
import com.jaspersoft.jasperserver.search.common.RepositorySearchConfiguration;
import com.jaspersoft.jasperserver.search.common.ResourceDetails;
import com.jaspersoft.jasperserver.search.common.SearchAttributes;
import com.jaspersoft.jasperserver.search.mode.SearchMode;
import com.jaspersoft.jasperserver.search.mode.SearchModeSettingsResolver;
import com.jaspersoft.jasperserver.search.service.RepositorySearchService;
import com.jaspersoft.jasperserver.search.service.ResourceService;
import com.jaspersoft.jasperserver.search.state.InitialStateResolver;
import com.jaspersoft.jasperserver.search.state.State;
import com.jaspersoft.jasperserver.util.test.BaseServiceSetupTestNG;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>Tests resources  functionality.</p>
 *
 * @author Stas Chubar
 * @version $Id$
 */
public class ResourceServiceTestNG extends BaseServiceSetupTestNG {
    protected static Log log = LogFactory.getLog(ResourceServiceTestNG.class);
    private static final String ROOT_TEST_FOLDER_NAME = "root_search_test_folder";
    private static final String ROOT_TEST_FOLDER_URI = "/" + ROOT_TEST_FOLDER_NAME;

    private InitialStateResolver initialStateResolver;
    private SearchCriteriaFactory searchCriteriaFactory;
    private SearchModeSettingsResolver searchModeSettingsResolver;
    private RepositorySearchService repositorySearchService;
    private ResourceService resourceService;
    private SearchSorter defaultSorter;

    public ResourceServiceTestNG(){
        log.info("SearchServiceTestNG => constructor() called");
    }

    @jakarta.annotation.Resource(name = "searchInitialStateResolver")
    public void setSearchInitialStateResolver(InitialStateResolver initialStateResolver) {
        this.initialStateResolver = initialStateResolver;
    }

    @jakarta.annotation.Resource(name = "searchModeSettingsResolver")
    public void setSearchModeSettingsResolver(SearchModeSettingsResolver searchModeSettingsResolver) {
        this.searchModeSettingsResolver = searchModeSettingsResolver;
    }

    @jakarta.annotation.Resource(name = "searchCriteriaFactory")
    public void setSearchCriteriaFactory(SearchCriteriaFactory searchCriteriaFactory) {
        this.searchCriteriaFactory = searchCriteriaFactory;
    }

    @jakarta.annotation.Resource(name = "repositorySearchService")
    public void setRepositorySearchService(RepositorySearchService repositorySearchService) {
        this.repositorySearchService = repositorySearchService;
    }

    @jakarta.annotation.Resource(name = "resourceService")
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @BeforeMethod
    public void log(Method m) {
        log.info(msg("%s#%s called.", this.getClass().getName(), m.getName()));
    }

    @Test()
    public void shouldCheckDependentReportsForDataSource() throws Exception {
        setAuthenticatedUser(USER_JASPERADMIN);

        List<ResourceDetails> results = resourceService.check(null, testResources("/datasources/JServerJNDIDS"));
        AssertJUnit.assertEquals("Should find zero dependent reports.", 0, results.size());
    }

    @Test()
    public void shouldCheckDependentReportsForNonDataSource() throws Exception {
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);

        List<ResourceDetails> results = resourceService.check(null, testResources("/reports/samples/AllAccounts"));
        AssertJUnit.assertEquals("Should find zero dependent reports.", 0, results.size());
    }

    @Test()
    public void shouldCheckDependentReportsForMixedResources() throws Exception {
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);

        List<ResourceDetails> results = resourceService.check(null,
                testResources("/datasources/JServerJNDIDS", "/reports/samples/AllAccounts"));

        AssertJUnit.assertEquals("Should find zero dependent reports.", 0, results.size());
    }

    private List<Resource> testResources(String ... uris) {
        List<Resource> resources = new ArrayList<Resource>();

        for (String uri : uris) {
            Resource resource = new ResourceDetails();
            resource.setURIString(uri);
            resources.add(resource);
        }

        return resources;
    }
}

