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

import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceLookup;
import com.jaspersoft.jasperserver.api.search.SearchCriteriaFactory;
import com.jaspersoft.jasperserver.util.test.BaseServiceSetupTestNG;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.collections.TransformerUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import static org.testng.AssertJUnit.*;

/**
 *
 * @author schubar
 */
public class RepositoryServiceDependentResourcesTest  extends BaseServiceSetupTestNG {

    protected final static Log logger = LogFactory.getLog(RepositoryServiceDependentResourcesTest.class);

    private SearchCriteriaFactory searchCriteriaFactory;

    private static String[] uriList = new String[]{
            "/analysis/reports/EmployeeAccounts",
            "/analysis/reports/Employees",
            "/reports/samples/AllAccounts",
            "/reports/samples/Cascading_multi_select_report",
            "/reports/samples/EmployeeAccounts",
            "/reports/samples/Employees",
            "/reports/samples/Freight",
            "/reports/samples/SalesByMonth",
            "/reports/samples/StandardChartsAegeanReport",
            "/reports/samples/StandardChartsEyeCandyReport",
            "/reports/samples/StandardChartsReport"
    };

    private String expectedOrder;
    private String expectedOrderForTopFive;

    @BeforeMethod
    public void log(Method m) {
        logger.info(msg("@@@ Running -> %s", m.getName()));

        expectedOrder = ArrayUtils.toString(uriList);
        expectedOrderForTopFive = ArrayUtils.toString(ArrayUtils.subarray(uriList, 0, 5));
    }

    @Test
    public void shouldFindAllDependantReportsForDataSource() {
        assertNotNull("RepositoryService service is not wired.", getRepositoryService());
        assertNotNull("SearchCriteriaFactory service is not wired.", searchCriteriaFactory);

        String uri = "/datasources/JServerJNDIDS";

        List<ResourceLookup> resources = getRepositoryService().
                getDependentResources(null, uri, searchCriteriaFactory, 0, 20);

        assertEquals("Should find 11 dependant resources.", 11, resources.size());

        assertEquals("All resources should be lookup's.", 11,
                CollectionUtils.countMatches(resources, PredicateUtils.instanceofPredicate(ResourceLookup.class)));
        
        Collection types = CollectionUtils.collect(resources, TransformerUtils.invokerTransformer("getResourceType"));
        
        assertEquals("All lookup's should have type ReportUnit.", 11,
                CollectionUtils.countMatches(types,
                        PredicateUtils.equalPredicate("com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit")));

        String sortOrder = ArrayUtils.toString(
                CollectionUtils.collect(resources, TransformerUtils.invokerTransformer("getURIString")).toArray());

        assertEquals("Resources should be sorted in order.", expectedOrder, sortOrder);
    }

    @Test
    public void shouldFindFirstFiveDependantReportsForDataSource() {
        assertNotNull("RepositoryService service is not wired.", getRepositoryService());
        assertNotNull("SearchCriteriaFactory service is not wired.", searchCriteriaFactory);

        String uri = "/datasources/JServerJNDIDS";

        List<ResourceLookup> resources = getRepositoryService().
                getDependentResources(null, uri, searchCriteriaFactory, 0, 5);

        assertEquals("Should find 5 dependant resources.", 5, resources.size());

        assertEquals("All resources should be lookup's.", 5,
                CollectionUtils.countMatches(resources, PredicateUtils.instanceofPredicate(ResourceLookup.class)));

        String sortOrder = ArrayUtils.toString(
                CollectionUtils.collect(resources, TransformerUtils.invokerTransformer("getURIString")).toArray());

        assertEquals("Resources should be sorted in order.", expectedOrderForTopFive, sortOrder);

    }

    @jakarta.annotation.Resource(name = "searchCriteriaFactory")
    public void setSearchCriteriaFactory(SearchCriteriaFactory searchCriteriaFactory) {
        this.searchCriteriaFactory = searchCriteriaFactory;
    }

}

