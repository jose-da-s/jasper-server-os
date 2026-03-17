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

package com.jaspersoft.jasperserver.war.action;

import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.JdbcReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.VirtualReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.client.JdbcReportDataSourceImpl;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.client.VirtualReportDataSourceImpl;
import com.jaspersoft.jasperserver.war.dto.ReportDataSourceWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.webflow.test.MockRequestContext;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * @author Paul Lysak
 */

public class DataSourceActionTest {
    private DataSourceAction dsAction;
    private ReportDataSourceWrapper formObject;
    private MockRequestContext context;
    private ObjectMapper jsonMapper = new ObjectMapper();

    @Before
    public void initAction() {
        dsAction = new DataSourceAction();
        context = new MockRequestContext();
        formObject = new ReportDataSourceWrapper();
        dsAction.getFormObjectScope().getScope(context).put(dsAction.getFormObjectName(), formObject);
    }

    @Test
    public void testBindSubDatasourcesEmptyOk() throws Exception {
        VirtualReportDataSource vds = new VirtualReportDataSourceImpl();
        formObject.setReportDataSource(vds);
        dsAction.bindSubDatasources(context);
        Assert.assertEquals("Sub-datasources map should be empty", 0, vds.getDataSourceUriMap().size());
    }

    private void assertSubDsUri(VirtualReportDataSource vds, String subDsID, String subDsURI) {
        ResourceReference subDsRef = vds.getDataSourceUriMap().get(subDsID);
        Assert.assertNotNull("Should find subDS by id", subDsRef);
        Assert.assertEquals("subDS URI", subDsURI, subDsRef.getReferenceURI());

    }

    @Test
    public void testBindSubDatasourcesOneOk() throws Exception {
        VirtualReportDataSource vds = new VirtualReportDataSourceImpl();
        formObject.setReportDataSource(vds);
        formObject.getNamedProperties().put(DataSourceAction.SUB_DATASOURCES_JSON_KEY,
                "[{\"dsName\": \"n1\", \"dsId\": \"id1\", \"dsUri\": \"/someUri\"}]");
        dsAction.bindSubDatasources(context);
        Assert.assertEquals("Sub-datasources map size", 1, vds.getDataSourceUriMap().size());
        assertSubDsUri(vds, "id1", "/someUri");
    }

    @Test
    public void testBindSubDatasourcesMultipleOk() throws Exception {
        VirtualReportDataSource vds = new VirtualReportDataSourceImpl();
        formObject.setReportDataSource(vds);
        formObject.getNamedProperties().put(DataSourceAction.SUB_DATASOURCES_JSON_KEY,
                "[{\"dsName\": \"name1\", \"dsId\": \"id1\", \"dsUri\": \"/someUri\"}," +
                        " {\"dsName\": \"name2\", \"dsId\": \"id2\", \"dsUri\": \"/anotherUri\"}]");
        dsAction.bindSubDatasources(context);
        Assert.assertEquals("Sub-datasources map size", 2, vds.getDataSourceUriMap().size());
        assertSubDsUri(vds, "id1", "/someUri");
        assertSubDsUri(vds, "id2", "/anotherUri");
    }


    @Test
    public void testInitSubDatasourcesEmptyOk() {
        VirtualReportDataSource vds = new VirtualReportDataSourceImpl();
        formObject.setReportDataSource(vds);
        vds.getDataSourceUriMap().clear();

        dsAction.initSubDatasources(formObject);
        String subDsJSON = (String)formObject.getNamedProperties().get(DataSourceAction.SUB_DATASOURCES_JSON_KEY);
        Assert.assertEquals("subDS JSON", "[]", subDsJSON);
    }

    /**
     * Sort maps in JSON representation to get uniform order
     * @param model
     */
    private void sortModel(Object model) {
        if(model instanceof List) {
            for(Object item: (List)model) {
                sortModel(item);
            }
        } else if(model instanceof LinkedHashMap) {
            LinkedHashMap mapModel = (LinkedHashMap)model;
            TreeMap treeMap = new TreeMap(mapModel);
            mapModel.clear();
            mapModel.putAll(treeMap);
            for(Object item: mapModel.values()) {
                sortModel(item);
            }
        }
    }

    private void assertJsonEquivalent(String comment, String expected, String actual) throws IOException {
        Assert.assertNotNull(comment, actual);
        List actVal = jsonMapper.readValue(actual, List.class);
        sortModel(actVal);
        String actualJsonNormalized = jsonMapper.writeValueAsString(actVal);
        List expVal = jsonMapper.readValue(expected, List.class);
        sortModel(expVal);
        String expectedJsonNormalized = jsonMapper.writeValueAsString(expVal);
        Assert.assertEquals(comment, expectedJsonNormalized, actualJsonNormalized);
    }

    @Test
    public void testInitSubDatasourcesOneOk() throws IOException {
        VirtualReportDataSource vds = new VirtualReportDataSourceImpl();
        formObject.setReportDataSource(vds);
        vds.getDataSourceUriMap().put("someID", new ResourceReference("/someURI"));
        RepositoryService repository =EasyMock.createMock(RepositoryService.class);
        dsAction.setRepository(repository);
        JdbcReportDataSource sampleDs = new JdbcReportDataSourceImpl();
        sampleDs.setName("n1");
        EasyMock.expect(repository.getResource(null, "/someURI")).andReturn(sampleDs);
        EasyMock.replay(repository);

        dsAction.initSubDatasources(formObject);

        EasyMock.verify(repository);

        String actualJson = (String)formObject.getNamedProperties().get(DataSourceAction.SUB_DATASOURCES_JSON_KEY);
        String expectedJson = "[{\"dsName\": \"n1\", \"dsId\": \"someID\", \"dsUri\": \"/someURI\"}]";
        assertJsonEquivalent("subDS JSON", expectedJson, actualJson);
    }
}
