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

package com.jaspersoft.jasperserver.test;

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
import com.jaspersoft.jasperserver.crypto.EncryptionEngine;
import com.jaspersoft.jasperserver.crypto.KeystoreManager;
import static com.jaspersoft.jasperserver.crypto.conf.Defaults.BuildEnc;
import com.jaspersoft.jasperserver.search.common.CustomFilter;
import com.jaspersoft.jasperserver.search.common.RepositorySearchConfiguration;
import com.jaspersoft.jasperserver.search.common.ResourceDetails;
import com.jaspersoft.jasperserver.search.common.SearchAttributes;
import com.jaspersoft.jasperserver.search.mode.SearchMode;
import com.jaspersoft.jasperserver.search.mode.SearchModeSettingsResolver;
import com.jaspersoft.jasperserver.search.service.RepositorySearchService;
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
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>Tests search functionality.</p>
 *
 * @author Yuriy Plakosh
 * @author Stas Chubar
 * @version $Id$
 */
public class SearchServiceTestNG extends BaseServiceSetupTestNG {
    protected static Log log = LogFactory.getLog(SearchServiceTestNG.class);
    private static final String ROOT_TEST_FOLDER_NAME = "root_search_test_folder";
    private static final String ROOT_TEST_FOLDER_URI = "/" + ROOT_TEST_FOLDER_NAME;

    private InitialStateResolver initialStateResolver;
    private SearchCriteriaFactory searchCriteriaFactory;
    private SearchModeSettingsResolver searchModeSettingsResolver;
    private RepositorySearchService repositorySearchService;
    private SearchSorter defaultSorter;

    public SearchServiceTestNG(){
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

    @BeforeClass()
    public void onSetUp() throws Exception {
        log.info("Setting up test data ...");

        log.info("onSetUp() => creating " + ROOT_TEST_FOLDER_URI);
        Folder rootTestFolder = new FolderImpl();
        rootTestFolder.setName(ROOT_TEST_FOLDER_NAME);
        rootTestFolder.setLabel("Root Search Test Folder");
        rootTestFolder.setDescription("Root search test folder which stores all testing resources");
        getUnsecureRepositoryService().saveFolder(null, rootTestFolder);

        addHibernateRepositoryDataSourceResources();
        addHibernateRepositoryReportResources();

        this.defaultSorter = searchModeSettingsResolver.getSettings(SearchMode.SEARCH).
                getRepositorySearchConfiguration().getCustomSorters().get(0).getSearchSorter();
    }

    @AfterClass()
    public void onTearDown() throws Exception {
        log.info("SearchServiceTestNG => onTearDown() called");

        deleteHibernateRepositoryReportResources();
        deleteHibernateRepositoryDataSourceResources();

        log.info("onTearDown() => deleting " + ROOT_TEST_FOLDER_URI);
        getUnsecureRepositoryService().deleteFolder(null, ROOT_TEST_FOLDER_URI);
    }

    @Test()
    public void basicSearch() throws Exception {
        log.info("SearchServiceTestNG => basicSearch() called");
        AssertJUnit.assertNotNull(getRepositoryService());

        SearchMode mode = SearchMode.SEARCH;
        
        State currentState = createDefaultSearchState(mode);
        currentState.updateFolder(ROOT_TEST_FOLDER_URI);

        List<SearchFilter> filters = createAllFiltersList(mode, currentState);

        int resultCount = this.repositorySearchService.getResultsCount(exContext(currentState),
                this.searchCriteriaFactory, filters, this.defaultSorter);

        AssertJUnit.assertEquals(4, resultCount);

        List<ResourceDetails> results = this.repositorySearchService.getResults(exContext(currentState),
                this.searchCriteriaFactory, filters, this.defaultSorter, 0, 100);

        AssertJUnit.assertEquals(4, results.size());
    }

    @Test()
    public void textBasedSearch() throws Exception {
        // Searching for resources with 'JNDI' text.
        SearchMode mode = SearchMode.SEARCH;
        
        State currentState = createDefaultSearchState(mode);
        currentState.updateText("JNDI");
        currentState.updateFolder(ROOT_TEST_FOLDER_URI);

        List<SearchFilter> filters = createAllFiltersList(mode, currentState);

        int resultCount = this.repositorySearchService.getResultsCount(exContext(currentState),
                this.searchCriteriaFactory, filters, this.defaultSorter);

        AssertJUnit.assertEquals(1, resultCount);

        List<ResourceDetails> results = this.repositorySearchService.getResults(exContext(currentState),
                this.searchCriteriaFactory, filters, this.defaultSorter, 0, 100);

        AssertJUnit.assertEquals(1, results.size());
        AssertJUnit.assertEquals("JServerJNDIDS", results.get(0).getName());

        // Searching for resources with 'Employee' text.
        currentState = createDefaultSearchState(mode);
        currentState.updateText("Employee");
        currentState.updateFolder(ROOT_TEST_FOLDER_URI);

        filters = createAllFiltersList(mode, currentState);

        resultCount = this.repositorySearchService.getResultsCount(exContext(currentState), this.searchCriteriaFactory,
                filters, this.defaultSorter);

        AssertJUnit.assertEquals(1, resultCount);

        results = this.repositorySearchService.getResults(exContext(currentState), this.searchCriteriaFactory, filters,
                this.defaultSorter, 0, 100);

        AssertJUnit.assertEquals(1, results.size());
        AssertJUnit.assertEquals("Employees", results.get(0).getName());
    }

    @Test()
    public void resourceTypeBasedSearch() throws Exception {
        SearchMode mode = SearchMode.SEARCH;
        
        State currentState = createDefaultSearchState(mode);
        currentState.updateFilter("resourceTypeFilter", "resourceTypeFilter-dataSources", false);
        currentState.updateFolder(ROOT_TEST_FOLDER_URI);

        List<SearchFilter> filters = createAllFiltersList(mode, currentState);

        int resultCount = this.repositorySearchService.getResultsCount(exContext(currentState),
                this.searchCriteriaFactory, filters, this.defaultSorter);

        AssertJUnit.assertEquals(3, resultCount);

        List<ResourceDetails> results = this.repositorySearchService.getResults(exContext(currentState),
                this.searchCriteriaFactory, filters, this.defaultSorter, 0, 100);

        AssertJUnit.assertEquals(3, results.size());
    }

    private State createDefaultSearchState(SearchMode mode) {
        RepositorySearchConfiguration configuration =
                searchModeSettingsResolver.getSettings(mode).getRepositorySearchConfiguration();

        return initialStateResolver.getInitialState(configuration);
    }

    private void addHibernateRepositoryDataSourceResources() {
        log.info("addHibernateRepositoryDataSourceResources() called");

        log.info("addHibernateRepositoryDataSourceResources() => creating " + ROOT_TEST_FOLDER_URI + "/search_ds");
        Folder dsFolder = new FolderImpl();
        dsFolder.setName("search_ds");
        dsFolder.setLabel("Search Data Sources");
        dsFolder.setDescription("Search Data Sources used by reports");
        dsFolder.setParentFolder(ROOT_TEST_FOLDER_URI);
        getUnsecureRepositoryService().saveFolder(null, dsFolder);

        createJndiDS();
        createRepoDS();
        createJdbcDS();
    }

    private void createJndiDS() {
        JndiJdbcReportDataSource datasource = (JndiJdbcReportDataSource) getUnsecureRepositoryService().newResource(null, JndiJdbcReportDataSource.class);
        datasource.setName("JServerJNDIDS");
        datasource.setLabel("JServer JNDI Data Source");
        datasource.setDescription("JServer JNDI Data Source");
        datasource.setJndiName(getJdbcProps().getProperty("test.jndi"));
        datasource.setParentFolder(ROOT_TEST_FOLDER_URI + "/search_ds");

        getUnsecureRepositoryService().saveResource(null, datasource);
    }

    private void createRepoDS() {
        JndiJdbcReportDataSource datasource = (JndiJdbcReportDataSource) getUnsecureRepositoryService().newResource(null, JndiJdbcReportDataSource.class);
        datasource.setName("repositoryDS");
        datasource.setLabel("Jasperserver Repository SQL data source");
        datasource.setDescription("Jasperserver Repository SQL data source for reporting");
        datasource.setJndiName(getJdbcProps().getProperty("metadata.jndi"));
        datasource.setParentFolder(ROOT_TEST_FOLDER_URI + "/search_ds");

        getUnsecureRepositoryService().saveResource(null, datasource);
    }

    private void createJdbcDS() {
        JdbcReportDataSource datasource =
                (JdbcReportDataSource) getUnsecureRepositoryService().newResource(null, JdbcReportDataSource.class);
        datasource.setName("JServerJdbcDS");
        datasource.setLabel("JServer Jdbc Data Source");
        datasource.setDescription("JServer Jdbc Data Source");
        datasource.setParentFolder(ROOT_TEST_FOLDER_URI + "/search_ds");

        datasource.setDriverClass(getJdbcProps().getProperty("test.jdbc.driverClassName"));
        datasource.setConnectionUrl(getJdbcProps().getProperty("test.jdbc.url"));
        datasource.setUsername(getJdbcProps().getProperty("test.jdbc.username"));

		String passwd = getJdbcProps().getProperty("test.jdbc.password");
		if (EncryptionEngine.isEncrypted(passwd)) {
			KeystoreManager ksManager = KeystoreManager.getInstance();
			passwd = EncryptionEngine.decrypt(ksManager.getKeystore(null).getKey(BuildEnc.getConfId()), passwd);
		}
		datasource.setPassword(passwd);

        getUnsecureRepositoryService().saveResource(null, datasource);
    }

    private void deleteHibernateRepositoryDataSourceResources() {
        log.info("deleteHibernateRepositoryDataSourceResources() called");

        deleteJdbcDS();
        deleteRepoDS();
        deleteJndiDS();

        log.info("deleteHibernateRepositoryDataSourceResources() => deleting " + ROOT_TEST_FOLDER_URI + "/search_ds");
        getUnsecureRepositoryService().deleteFolder(null, ROOT_TEST_FOLDER_URI + "/search_ds");
    }

    private void deleteJndiDS() {
        getUnsecureRepositoryService().deleteResource(null, ROOT_TEST_FOLDER_URI + "/search_ds/JServerJNDIDS");
    }

    private void deleteRepoDS() {
        getUnsecureRepositoryService().deleteResource(null, ROOT_TEST_FOLDER_URI + "/search_ds/repositoryDS");
    }

    private void deleteJdbcDS() {
        getUnsecureRepositoryService().deleteResource(null, ROOT_TEST_FOLDER_URI + "/search_ds/JServerJdbcDS");
    }

    private void addHibernateRepositoryReportResources()  {
        log.info("addHibernateRepositoryReportResources() called");

        log.info("addHibernateRepositoryReportResources() => creating " + ROOT_TEST_FOLDER_URI + "/search_reports");
        Folder reportsFolder = new FolderImpl();
        reportsFolder.setName("search_reports");
        reportsFolder.setLabel("Search Reports");
        reportsFolder.setDescription("Search Reports");
        reportsFolder.setParentFolder(ROOT_TEST_FOLDER_URI);
        getUnsecureRepositoryService().saveFolder(null, reportsFolder);

        createEmployees(reportsFolder);
    }

    private void setCommon(Resource res, String id) {
        res.setName(id);
        res.setLabel(id + " Label");
        res.setDescription(id + " description");
    }

    private void createEmployees(Folder folder) {
        log.info("createEmployees() => creating " + ROOT_TEST_FOLDER_URI + "/search_reports/Employees");

        FileResource reportRes = (FileResource) getUnsecureRepositoryService().newResource(null, FileResource.class);
        reportRes.setFileType(FileResource.TYPE_JRXML);
        setCommon(reportRes, "EmployeesJRXML");

        InputStream jrxml = getClass().getResourceAsStream("/reports/jasper/Employees.jrxml");
        reportRes.readData(jrxml);

        ReportUnit unit = (ReportUnit) getUnsecureRepositoryService().newResource(null, ReportUnit.class);
        unit.setName("Employees");
        unit.setLabel("Employee List");
        unit.setDescription("Employee List");
        unit.setParentFolder(folder);

        unit.setDataSourceReference(ROOT_TEST_FOLDER_URI + "/search_ds/JServerJNDIDS");
        unit.setMainReport(reportRes);

        getUnsecureRepositoryService().saveResource(null, unit);
    }

    private void deleteHibernateRepositoryReportResources()  {
        log.info("deleteHibernateRepositoryReportResources() called");

        deleteEmployees();

        log.info("deleteHibernateRepositoryReportResources() => deleting " + ROOT_TEST_FOLDER_URI + "/search_reports");
        getUnsecureRepositoryService().deleteFolder(null, ROOT_TEST_FOLDER_URI + "/search_reports");
    }

    private void deleteEmployees() {
        log.info("deleteEmployees() => deleting " + ROOT_TEST_FOLDER_URI + "/search_reports/Employees");
        getUnsecureRepositoryService().deleteResource(null, ROOT_TEST_FOLDER_URI + "/search_reports/Employees");
    }

    private ExecutionContext exContext(State state) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ExecutionContext exContext = new ExecutionContextImpl();

        if (exContext.getAttributes() == null) {
            exContext.setAttributes(new ArrayList());
        }

        AssertJUnit.assertNotNull(authentication.getPrincipal());
        AssertJUnit.assertNotNull(state);

        List attributes = exContext.getAttributes();
        
        attributes.add(authentication.getPrincipal());

        SearchAttributes searchAttributes = new SearchAttributes();
        searchAttributes.setMode(SearchMode.SEARCH);
        searchAttributes.setState(state);

        attributes.add(searchAttributes);

        return exContext;
    }

    private RepositorySearchConfiguration getConfiguration(SearchMode mode) {
        return searchModeSettingsResolver.getSettings(mode).getRepositorySearchConfiguration();
    }
    
    private List<SearchFilter> createAllFiltersList(SearchMode mode, State state) {
        List<SearchFilter> filterList = new ArrayList<SearchFilter>();

        RepositorySearchConfiguration configuration = getConfiguration(mode);

        filterList.addAll(configuration.getSystemFilters());
        filterList.addAll(getRestrictionsFilters(mode, state));

        return filterList;
    }

    private List<SearchFilter> getRestrictionsFilters(SearchMode mode, State state) {
        List<SearchFilter> filters = new ArrayList<SearchFilter>();

        for (Map.Entry<String, String> entry : state.getCustomFiltersMap().entrySet()) {
            for (CustomFilter filter : getConfiguration(mode).getCustomFilters()) {
                if (filter.getId().equals(entry.getKey())) {
                    filters.add(filter.getFilter());
                }
            }
        }

        return filters;
    }

}

