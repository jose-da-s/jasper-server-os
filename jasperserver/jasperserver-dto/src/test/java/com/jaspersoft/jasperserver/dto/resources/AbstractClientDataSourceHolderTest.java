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

package com.jaspersoft.jasperserver.dto.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.jaspersoft.jasperserver.dto.common.DeepCloneable;
import com.jaspersoft.jasperserver.dto.utils.JSONSerializer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olexandr Dahno <odahno@tibco.com>
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AbstractClientDataSourceHolderTest {

    private static final String TEST_URI = "TEST_URI";
    private final JSONSerializer jsonSerializer = new JSONSerializer();

    @ParameterizedTest
    @MethodSource(value = "dataSources")
    void dataSource(DataSourceHolder holder) {
        ClientReportUnit instance = new ClientReportUnit();
        instance.setDataSource(holder.dataSource);

        JsonNode jsonNode = jsonNodeFromInstance(instance);

        JsonNode actual = jsonNode.findValue("dataSource");
        JsonNode expected = jsonNodeFromJson(constructJson(holder.name));

        assertEquals(expected, actual);
    }

    private String constructJson(String key) {
        return "{\"" + key + "\":{\"uri\":\"" + TEST_URI + "\"}}";
    }

    /*
     * Helpers
     */

    protected JsonNode jsonNodeFromInstance(AbstractClientDataSourceHolder instance) {
        return jsonSerializer.contentFromInstance(instance);
    }

    protected JsonNode jsonNodeFromJson(String json) {
        return jsonSerializer.deserializeJson(json, Object.class);
    }

    private List<DataSourceHolder> dataSources() {
        return Arrays.asList(
                new DataSourceHolder(new ClientReference().setUri(TEST_URI), "dataSourceReference"),
                new DataSourceHolder(new ClientAwsDataSource().setUri(TEST_URI), "awsDataSource"),
                new DataSourceHolder(new ClientBeanDataSource().setUri(TEST_URI), "beanDataSource"),
                new DataSourceHolder(new ClientCustomDataSource().setUri(TEST_URI), "customDataSource"),
                new DataSourceHolder(new ClientJdbcDataSource().setUri(TEST_URI), "jdbcDataSource"),
                new DataSourceHolder(new ClientJndiJdbcDataSource().setUri(TEST_URI), "jndiJdbcDataSource"),
                new DataSourceHolder(new ClientVirtualDataSource().setUri(TEST_URI), "virtualDataSource"),
                new DataSourceHolder(new ClientSemanticLayerDataSource().setUri(TEST_URI), "semanticLayerDataSource"),
                new DataSourceHolder(new ClientAdhocDataView().setUri(TEST_URI), "advDataSource"),
                new DataSourceHolder(new ClientAzureSqlDataSource().setUri(TEST_URI), "azureSqlDataSource"),
                new DataSourceHolder(new AnotherDataSource().setUri(TEST_URI), "AbstractClientDataSourceHolderTest$AnotherDataSource")
        );
    }

    private class DataSourceHolder {
        private ClientReferenceableDataSource dataSource;
        private String name;

        private DataSourceHolder(ClientReferenceableDataSource dataSource, String name) {
            this.dataSource = dataSource;
            this.name = name;
        }
    }

    // Use this class to show that a data source will have key as the class name,
    // because the class isn't in annotation of dataSource method
    private class AnotherDataSource implements ClientReferenceableDataSource {
        private String uri;
        private Integer version;

        @Override
        public DeepCloneable deepClone() {
            return null;
        }

        public AnotherDataSource setUri(String uri) {
            this.uri = uri;
            return this;
        }

        @Override
        public String getUri() {
            return uri;
        }

        @Override
        public Integer getVersion() {
            return version;
        }

        public AnotherDataSource setVersion(Integer version) {
            this.version = version;
            return this;
        }
    }

}