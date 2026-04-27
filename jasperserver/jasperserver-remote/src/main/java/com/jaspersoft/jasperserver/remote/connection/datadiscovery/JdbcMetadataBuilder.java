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
package com.jaspersoft.jasperserver.remote.connection.datadiscovery;

import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.jaspersoft.jasperserver.api.engine.common.domain.JdbcMetaConfiguration;
import com.jaspersoft.jasperserver.dto.common.JavaAliasConverter;
import com.jaspersoft.jasperserver.dto.connection.metadata.PartialMetadataOptions;
import com.jaspersoft.jasperserver.dto.resources.DataSourceTableDescriptor;
import com.jaspersoft.jasperserver.dto.resources.domain.AbstractResourceGroupElement;
import com.jaspersoft.jasperserver.dto.resources.domain.ResourceGroupElement;
import com.jaspersoft.jasperserver.dto.resources.domain.ResourceMetadataSingleElement;
import com.jaspersoft.jasperserver.dto.resources.domain.SchemaElement;
import org.apache.commons.lang3.StringUtils;
import jakarta.ws.rs.BadRequestException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.support.rowset.ResultSetWrappingSqlRowSet;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.jaspersoft.jasperserver.remote.common.ThreadInterruptionHelper.checkInterrupted;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@Component
public class JdbcMetadataBuilder implements MetadataBuilder<Connection>,
        ParametrizedMetadataBuilder<Connection, PartialMetadataOptions> {
    public static final String GROUP_KIND_SCHEMA = "schema";
    public static final String GROUP_KIND_DATASOURCE = "dataSource";
    @Resource(name="jdbcMetaConfiguration")
    private JdbcMetaConfiguration jdbcMetaConfiguration;
    private SpecialCharacterEscape characterEscape;
    private final static Log log = LogFactory.getLog(JdbcMetadataBuilder.class);

    @Override
    public SchemaElement build(Connection connection, PartialMetadataOptions options) {
        final List<DataSourceTableDescriptor> includes = options.getIncludes();
        final List<DataSourceTableDescriptor> expands = options.getExpands();
        final boolean loadReferences = Boolean.TRUE.equals(options.getLoadReferences());

        List<SchemaElement> items = null;
        try {
            checkInterrupted();
            final DatabaseMetaData metaData = connection.getMetaData();
            final String databaseProductName = characterEscape != null ? metaData.getDatabaseProductName() : null;
            final List<String> schemas = getAllSchemas(metaData);
            if (includes != null && includes.size() > 0) {
                items = includeMetadata(includes, metaData, loadReferences, schemas, databaseProductName);
            } else if(expands != null && expands.size() > 0){
                items = expandMetadata(expands, metaData, loadReferences, schemas, databaseProductName);
            } else {
                items = getSchemaElements(metaData, loadReferences, schemas, databaseProductName, new HashMap<String, List<String>>());
            }
        } catch (SQLException e) {
            throw new JSExceptionWrapper(e);
        }
        if (items == null || items.isEmpty()) {
            return null;
        }
        return new ResourceGroupElement().setElements(items);
    }

    /** This is called when we do GET request for metadata endpoint.
     *  Throw Bad request if it includes any parameter.
     **/
    public SchemaElement build(Connection connection, Map<String, String[]> options) {
        if(options != null && options.size() > 0) {
            throw new BadRequestException();
        }
        return build(connection, new PartialMetadataOptions());
    }

    protected List<SchemaElement> expandMetadata(List<DataSourceTableDescriptor> expands, DatabaseMetaData metaData, boolean loadReferences, List<String> schemas, String databaseProductName) throws SQLException {
        List<SchemaElement> result;
        final Map<String, List<String>> expandsMap = new HashMap<String, List<String>>();
        final List<DataSourceTableDescriptor> parsedExpands = parseAndFilterValidSchemas(expands, schemas, metaData, databaseProductName);
        if (parsedExpands != null) {
            for (DataSourceTableDescriptor expand : parsedExpands) {
                if(expand != null) {
                    List<String> tableList;
                    if(expand.getSchemaName() != null) {
                        tableList = expandsMap.get(expand.getSchemaName());
                        if (tableList == null) {
                            tableList = new ArrayList<String>();
                            tableList.add(expand.getDatasourceTableName());
                            expandsMap.put(expand.getSchemaName(), tableList);
                        } else {
                            tableList.add(expand.getDatasourceTableName());
                            expandsMap.put(expand.getSchemaName(), tableList);
                        }
                    } else {
                        expandsMap.put(expand.getDatasourceTableName(), null);
                    }
                }
            }
        }
        result = getSchemaElements(metaData, loadReferences, schemas, databaseProductName, expandsMap);
        return result;
    }

    private List<SchemaElement> getSchemaElements(DatabaseMetaData metaData, boolean loadReferences, List<String> schemas, String databaseProductName, Map<String, List<String>> expandsMap) throws SQLException {
        List<SchemaElement> result;
        if (!schemas.isEmpty()) {
            result = new ArrayList<SchemaElement>();
            for (String schema : schemas) {
                result.add(getSchemaMetadata(schema, expandsMap.get(schema), metaData, databaseProductName, loadReferences));
        }
        } else {
            // this data source doesn't have any schema. Let's load tables then
            // why expandsMap.keySet() - in this case tables are keys.
            result = buildTablesMetadata(null, metaData, expandsMap.keySet(), databaseProductName, loadReferences);
        }
        return result;
    }

    protected List<String> getAllSchemas(DatabaseMetaData metaData) throws SQLException {
        List<String> result = new ArrayList<String>();
        checkInterrupted();
        final ResultSet resultSet = metaData.getSchemas();
        /**
         * JRS-20660 - In order to have the TYPE_SCROLL_INSENSITIVE property set for the resultSet we wrap
         * the resultSet using CachedRowSet.
         */
        setResult(result, resultSet);
        return result;
    }

    protected void setResult(List<String> result, ResultSet resultSet) throws SQLException {
    	RowSetFactory aFactory = RowSetProvider.newFactory();
    	CachedRowSet cachedRowSet = aFactory.createCachedRowSet();
        cachedRowSet.populate(resultSet);

        // CachedRowSet is wrapped by ResultSetWrappingSqlRowSet to allow using Column labels for rs.getString()
        ResultSetWrappingSqlRowSet resultSetWrappingSqlRowSet = getResultSetWrappingSqlRowSet(cachedRowSet);

        if (resultSetWrappingSqlRowSet.isBeforeFirst()) {
            while (resultSetWrappingSqlRowSet.next()) {
                result.add(resultSetWrappingSqlRowSet.getString("TABLE_SCHEM"));
            }
        }
        resultSet.close();
        cachedRowSet.close();
    }

    protected List<DataSourceTableDescriptor> parseAndFilterValidSchemas(List<DataSourceTableDescriptor> dataSourceTableDescriptors, List<String> schemas, DatabaseMetaData databaseMetaData, String databaseProductName){
        List<DataSourceTableDescriptor> result = new ArrayList<DataSourceTableDescriptor>();
        for (DataSourceTableDescriptor dataSourceTableDescriptor : dataSourceTableDescriptors) {
            if(dataSourceTableDescriptor != null){
                if(dataSourceTableDescriptor.getSchemaName() != null && !schemas.isEmpty()){
                    // this is schema include. We have to make sure, that such schema exists
                    if(schemas.contains(dataSourceTableDescriptor.getSchemaName()) ||
                            (characterEscape != null &&
                                        schemas.contains(characterEscape.escape(dataSourceTableDescriptor.getSchemaName(), databaseMetaData, databaseProductName)))){
                        // yes, this schema is valid. Keep this include
                        result.add(dataSourceTableDescriptor);
                    }
                } else if (dataSourceTableDescriptor.getSchemaName() == null && dataSourceTableDescriptor.getDatasourceTableName() != null && !schemas.isEmpty()) {
                    //do nothing when no schema name is mentioned for non-schemaless Datasource
                } else {
                    result.add(dataSourceTableDescriptor);
                }
            }
        }
        return result;
    }

    protected List<SchemaElement> includeMetadata(List<DataSourceTableDescriptor> includes, DatabaseMetaData metaData, boolean loadReferences, List<String> schemas, String databaseProductName) throws SQLException {
        final List<DataSourceTableDescriptor> parsedIncludes = parseAndFilterValidSchemas(includes, schemas, metaData, databaseProductName);
        final boolean hasSchemas = !schemas.isEmpty();
        List<SchemaElement> result = new ArrayList<SchemaElement>();
        for (DataSourceTableDescriptor include : parsedIncludes) {
            SchemaElement currentItem = null;
            if(include.getSchemaName() != null && hasSchemas) {
                if (!StringUtils.isEmpty(include.getDatasourceTableName())) {
                    currentItem = getTableMetadata(include.getSchemaName(), include.getDatasourceTableName(), metaData, databaseProductName, loadReferences);
                } else {
                    currentItem = getSchemaMetadata(include.getSchemaName(), new ArrayList<String>(), metaData, databaseProductName, loadReferences);
                }
            } else if(!hasSchemas && include.getDatasourceTableName() != null) {
                    currentItem = getTableMetadata(null, include.getDatasourceTableName(), metaData, databaseProductName, loadReferences);
            } else {
                return getSchemaElements(metaData, loadReferences, schemas, databaseProductName, new HashMap<String, List<String>>());
            }
            if (currentItem != null) {
                result.add(currentItem);
            }
        }
        return result;
    }

    protected SchemaElement getSchemaMetadata(String schema, List<String> expand, DatabaseMetaData metaData,
                                              String databaseProductName, boolean loadReferences) throws SQLException {
        checkInterrupted();
        ResourceGroupElement result = new ResourceGroupElement().setKind(GROUP_KIND_SCHEMA).setName(schema);
        if (expand != null) {
            final List<SchemaElement> tableItems;
            final Set<String> tableNamesToExpand = new HashSet<String>();
            for (String tableName : expand) {
                tableNamesToExpand.add(tableName);
            }
            tableItems = buildTablesMetadata(schema, metaData, tableNamesToExpand, databaseProductName, loadReferences);
            result.setElements(tableItems);
        }
        return result;
    }

    protected List<SchemaElement> buildTablesMetadata(String schema, DatabaseMetaData metaData,
                                                      Set<String> tableNamesToExpand, String databaseProductName, boolean loadReferences) {
        List<SchemaElement> tableItems = new ArrayList<SchemaElement>();
        ResultSet tables = null;
        try {
            final String escapedSchema = schema != null && characterEscape != null ? characterEscape.escape(schema, metaData, databaseProductName) : schema;
            checkInterrupted();
            tables = metaData.getTables(null,
                    escapedSchema,
                    null, getSupportedTableTypes());
            while (tables.next()) {
                final String tableName = tables.getString("TABLE_NAME");
                SchemaElement tableItem;
                if (tableNamesToExpand.contains(tableName)) {
                    tableItem = getTableMetadata(schema, tableName, metaData, databaseProductName, loadReferences);
                } else {
                    tableItem = new ResourceGroupElement().setName(tableName).setKind("table");
                }
                tableItems.add(tableItem);
            }
        } catch (SQLException e) {
            throw new JSExceptionWrapper(e);
        }
        finally {
            try {
                if (tables != null) {
                    tables.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
        return tableItems;
    }

    protected SchemaElement getTableMetadata(String schema, String table,
                                             DatabaseMetaData metaData, String databaseProductName, boolean loadReferences) throws SQLException {
        AbstractResourceGroupElement result = null;
        final String escapedSchema = schema != null && characterEscape != null ?
                characterEscape.escape(schema, metaData, databaseProductName) : schema;
        final String escapedTableName = table != null && characterEscape != null ?
                characterEscape.escape(table, metaData, databaseProductName) : table;
        checkInterrupted();
        final ResultSet tables = metaData.getTables(null, escapedSchema, escapedTableName,
                getSupportedTableTypes());
        RowSetFactory aFactory = RowSetProvider.newFactory();
    	CachedRowSet cachedRowSet = aFactory.createCachedRowSet();
        cachedRowSet.populate(tables);
        if (cachedRowSet.isBeforeFirst()) {
            result = new ResourceGroupElement().setName(table).setKind("table");
            List<SchemaElement> columnsMetadata = fillColumnsMetadata(schema, table, metaData, databaseProductName, loadReferences);
            if (!columnsMetadata.isEmpty()) {
                result.setElements(columnsMetadata);
            }
        }
        tables.close();
        cachedRowSet.close();
        return result;
    }

    protected String[] getSupportedTableTypes() {
        return (String[])jdbcMetaConfiguration.getTableTypes().toArray(new String[jdbcMetaConfiguration.getTableTypes().size()]);
    }

    protected void setJdbcMetaConfiguration(JdbcMetaConfiguration jdbcMetaConfiguration) {
        this.jdbcMetaConfiguration = jdbcMetaConfiguration;
    }

    protected SchemaElement getColumnMetadata(String schema, String table, String column,
                                              DatabaseMetaData metaData, String databaseProductName, boolean loadReferences) {
        SchemaElement columnItem = null;
        try {
            List<SchemaElement> columnsMetadata = fillColumnsMetadata(schema, table, metaData, databaseProductName, loadReferences);
            for (SchemaElement element : columnsMetadata) {
                if (element.getName().equals(column)) {
                    columnItem = element;
                    break;
                }
            }
        } catch (SQLException e) {
            throw new JSExceptionWrapper(e);
        }
        return columnItem;
    }

    private List<SchemaElement> fillColumnsMetadata(String schema, String table, DatabaseMetaData metaData,
                                                    String databaseProductName, boolean loadReferences) throws SQLException {
        List<SchemaElement> columnsMetadata = new ArrayList<SchemaElement>();
        final String escapedSchema = characterEscape != null ? characterEscape.escape(schema, metaData, databaseProductName) : schema;
        final String escapedTableName = characterEscape != null ? characterEscape.escape(table, metaData, databaseProductName) : table;
        checkInterrupted();
        final ResultSet columns = metaData.getColumns(null, escapedSchema, escapedTableName, null);
        RowSetFactory aFactory = RowSetProvider.newFactory();
    	CachedRowSet cachedRowSet = aFactory.createCachedRowSet();
    	cachedRowSet.populate(columns);

        // CachedRowSet is wrapped by ResultSetWrappingSqlRowSet to allow using Column labels for rs.getString()
        ResultSetWrappingSqlRowSet resultSetWrappingSqlRowSet = getResultSetWrappingSqlRowSet(cachedRowSet);

        if (resultSetWrappingSqlRowSet.isBeforeFirst()) {
            List<String> primaryKeys = new ArrayList<String>();
            Map<String, String> foreignKeyMap = new HashMap<String, String>();
            if (loadReferences) {
                checkInterrupted();
                ResultSet primaryKeySet = metaData.getPrimaryKeys(null, escapedSchema, escapedTableName);
                while (primaryKeySet.next()) {
                    primaryKeys.add(primaryKeySet.getString(4));
                }
                primaryKeySet.close();
                checkInterrupted();
                final ResultSet foreignKeysSet = metaData.getImportedKeys(null, escapedSchema, escapedTableName);
                while (foreignKeysSet.next()) {
                    String foreignKeyColumnName = foreignKeysSet.getString("FKCOLUMN_NAME");
                    String primaryKeyTableName = foreignKeysSet.getString("PKTABLE_NAME");
                    String primaryKeyColumnName = foreignKeysSet.getString("PKCOLUMN_NAME");
                    String primaryKeySchemaName = foreignKeysSet.getString("PKTABLE_SCHEM");
                    foreignKeyMap.put(foreignKeyColumnName,
                            (primaryKeySchemaName != null ? primaryKeySchemaName + "." : "")
                                    + primaryKeyTableName + "." + primaryKeyColumnName);
                }
                foreignKeysSet.close();
            }
            final boolean hasPrimaryKeys = !primaryKeys.isEmpty();
            final boolean hasForeignKeys = !foreignKeyMap.isEmpty();
            while (resultSetWrappingSqlRowSet.next()) {
                final String columnName = resultSetWrappingSqlRowSet.getString("COLUMN_NAME");
                final int typeCode = resultSetWrappingSqlRowSet.getInt("DATA_TYPE");
                final String javaType = jdbcMetaConfiguration.getJavaType(resultSetWrappingSqlRowSet.getString("TYPE_NAME"), typeCode);
                final String type = javaType != null ? JavaAliasConverter.toAlias(javaType) : null;
                final ResourceMetadataSingleElement columnItem = new ResourceMetadataSingleElement().setName(columnName)
                        .setType(type);
                if (hasPrimaryKeys && primaryKeys.contains(columnName)) {
                    columnItem.setIsIdentifier(true);
                }
                if (hasForeignKeys && foreignKeyMap.containsKey(columnName)) {
                    columnItem.setReferenceTo(foreignKeyMap.get(columnName));
                }
                columnsMetadata.add(columnItem);
            }
        }
        columns.close();
        cachedRowSet.close();
        return columnsMetadata;
    }

    public void setCharacterEscape(SpecialCharacterEscape characterEscape) {
        this.characterEscape = characterEscape;
    }

    protected CachedRowSet getCachedRowSet() throws SQLException {
    	RowSetFactory aFactory = RowSetProvider.newFactory();
    	return aFactory.createCachedRowSet();
    }

    protected ResultSetWrappingSqlRowSet getResultSetWrappingSqlRowSet(CachedRowSet cachedRowSet) {
        return new ResultSetWrappingSqlRowSet(cachedRowSet);
    }
}

