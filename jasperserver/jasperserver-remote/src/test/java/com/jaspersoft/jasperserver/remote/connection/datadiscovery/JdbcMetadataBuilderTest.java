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
package com.jaspersoft.jasperserver.remote.connection.datadiscovery;

import com.jaspersoft.jasperserver.api.engine.common.domain.JdbcMetaConfigurationImpl;
import com.jaspersoft.jasperserver.dto.resources.DataSourceTableDescriptor;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * <p></p>
 *
 * @author Ivan.Chan
 * @version $Id: JdbcMetadataBuilderTest.java 68376 2017-11-28 13:53:46Z tiefimen $
 */
public class JdbcMetadataBuilderTest {

    JdbcMetadataBuilder jdbcMetadataBuilder = new JdbcMetadataBuilder();

    @BeforeClass
    public void init(){
        jdbcMetadataBuilder = spy(jdbcMetadataBuilder);
    }

    @Test
    public void parseAndFilterValidSchema() throws Exception {
        List<DataSourceTableDescriptor> list = new ArrayList<DataSourceTableDescriptor>();
        list.add(new DataSourceTableDescriptor().setSchemaName("public"));
        final List<DataSourceTableDescriptor> result = jdbcMetadataBuilder.parseAndFilterValidSchemas(list,
                new ArrayList<String>(Arrays.asList("public", "information_schema, pg_catalog")), null, "PostgreSQL");
        List expectedResult = new ArrayList<DataSourceTableDescriptor>();
        expectedResult.add(new DataSourceTableDescriptor().setSchemaName("public"));
        assertEquals(result, expectedResult);
    }

    @Test
    public void parseAndFilterValidSchema_MySQLNoSchemaCase() throws Exception {
        List<DataSourceTableDescriptor> list = new ArrayList<DataSourceTableDescriptor>();
        list.add(new DataSourceTableDescriptor().setDatasourceTableName("account"));
        final List<DataSourceTableDescriptor> result = jdbcMetadataBuilder.parseAndFilterValidSchemas(list,
                new ArrayList<String>(), null, "MySQL");
        List expectedResult = new ArrayList<String[]>();
        expectedResult.add(new DataSourceTableDescriptor().setDatasourceTableName("account"));
        assertEquals(result, expectedResult);
    }

    @Test
    public void parseAndFilterValidSchema_IncludeEmptySchema() throws Exception {
        List<DataSourceTableDescriptor> list = new ArrayList<DataSourceTableDescriptor>();
        list.add(new DataSourceTableDescriptor().setSchemaName("public"));
        list.add(new DataSourceTableDescriptor().setSchemaName("public").setDatasourceTableName(""));
        final List<DataSourceTableDescriptor> result = jdbcMetadataBuilder.parseAndFilterValidSchemas(list,
                new ArrayList<String>(Arrays.asList("public", "information_schema, pg_catalog")), null, "PostgreSQL");
        List expectedResult = new ArrayList<String[]>();
        expectedResult.add(new DataSourceTableDescriptor().setSchemaName("public"));
        expectedResult.add(new DataSourceTableDescriptor().setSchemaName("public").setDatasourceTableName(""));
        assertEquals(result, expectedResult);
    }

    @Test (expectedExceptions = InvalidResultSetAccessException.class)
    public void setResult_validateIsBeforeFirst() throws SQLException {

        List<String> result = new ArrayList<>();
        List<String> expectedResult = new ArrayList<>();

        // set the Connection and ResultSet
        final Connection connection = mock(Connection.class);
        final Statement statement = mock(Statement.class);
        doReturn(statement).when(connection).createStatement();
        final ResultSet resultSet = mock(ResultSet.class);
        final ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        doReturn(resultSetMetaData).when(resultSet).getMetaData();
        doReturn(true).when(resultSet).isBeforeFirst();
        doReturn(true).doReturn(false).when(resultSet).next();
        doReturn(1).when(resultSetMetaData).getColumnCount();
        doReturn("public").when(resultSet).getString("TABLE_SCHEM");

        jdbcMetadataBuilder.setResult(result, resultSet);
    }

    @Test
    public void getSupportedTableTypes() {
        JdbcMetaConfigurationImpl jdbcMetaConfiguration = new JdbcMetaConfigurationImpl();
        ArrayList<String> tableList = new ArrayList<String>();
        tableList.add("TABLE");
        tableList.add("MATERIALIZED VIEW");
        jdbcMetaConfiguration.setTableTypes(tableList);
        jdbcMetadataBuilder.setJdbcMetaConfiguration(jdbcMetaConfiguration);
        String[] res = jdbcMetadataBuilder.getSupportedTableTypes();
        assertEquals(res[0], "TABLE");
        assertEquals(res[1], "MATERIALIZED VIEW");
    }
}
