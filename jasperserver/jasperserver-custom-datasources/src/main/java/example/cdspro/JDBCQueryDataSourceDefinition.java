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

package example.cdspro;

import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.CustomDomainMetaDataImpl;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.DataAdapterDefinition;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomDomainMetaData;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomReportDataSource;
import net.sf.jasperreports.dataadapters.DataAdapter;
import net.sf.jasperreports.dataadapters.DataAdapterService;
import net.sf.jasperreports.data.jdbc.JdbcDataAdapter;
import net.sf.jasperreports.data.jdbc.JdbcDataAdapterImpl;
import net.sf.jasperreports.engine.JasperReportsContext;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;


/**
 * @author ichan
 * custom report data source definition for JDBC SQL
 * This class is meant to be instantiated as a spring bean that registers a custom data source with the system.
 */
public class JDBCQueryDataSourceDefinition extends DataAdapterDefinition {

    public JDBCQueryDataSourceDefinition() {
        // add additional field
        Set<String> additionalPropertySet = getAdditionalPropertySet();
        additionalPropertySet.add("query");


        // define default values for the following properties
        Map<String, String> propertyDefaultValueMap = getPropertyDefaultValueMap();
        propertyDefaultValueMap.put("name", "JDBCQueryDataSource");
        propertyDefaultValueMap.put("queryExecuterMode", "true");
        propertyDefaultValueMap.put("query", "");
        propertyDefaultValueMap.put("password", "");
        propertyDefaultValueMap.put("savePassword", "true");

        // hide the following properties from UI
        Set<String> hiddenPropertySet = getHiddenPropertySet();
        hiddenPropertySet.add("name");
        hiddenPropertySet.add("queryExecuterMode");
        hiddenPropertySet.add("columnNames");
        hiddenPropertySet.add("properties");
        hiddenPropertySet.add("classpath");
        hiddenPropertySet.add("serverAddress");
        hiddenPropertySet.add("database");
       hiddenPropertySet.add("savePassword");

        // set query executor factory
        Map<String, String> queryExecuterMap = new HashMap<String, String>();
        queryExecuterMap.put("JDBCQuery", "net.sf.jasperreports.engine.query.JRJdbcQueryExecuterFactory");
        setQueryExecuterMap(queryExecuterMap);
    }

    @Override
    public Map<String, Object>  customizePropertyValueMap(CustomReportDataSource customReportDataSource, Map<String, Object>  propertyValueMap) {
        if (propertyValueMap.get("url") != null) {
            propertyValueMap.put("url", ((String) propertyValueMap.get("url")).trim());
        }
        return propertyValueMap;
    }

    /*
    * This function is used for retrieving the metadata layer of the custom data source in form of CustomDomainMetaData
    * CustomDomainMetaData contains information JRFields, query, query language and field name mapping (actual JRField name, name used in domain)
    */
    @Override
    public CustomDomainMetaData getCustomDomainMetaData(CustomReportDataSource customDataSource) throws Exception {

        // METADATA DISCOVERY
        Map<String, Object> propertyValueMap = new HashMap<String, Object>();
        propertyValueMap = getDataSourceServicePropertyMap(customDataSource, propertyValueMap);
        propertyValueMap = customizePropertyValueMap(customDataSource, propertyValueMap);

        // create JDBC DATA ADAPTER from Custom Report Data Source properties
        // map custom report data source properties to data adapter
        JdbcDataAdapter jdbcDataAdapter = new JdbcDataAdapterImpl();
        jdbcDataAdapter = (JdbcDataAdapter) setupDataAdapter(jdbcDataAdapter, propertyValueMap);

        if (getValidator() != null) getValidator().validatePropertyValues(customDataSource, null);
        String query = (String)propertyValueMap.get("query");

        JDBCQueryDataSourceService jdbcDataAdapterService = new JDBCQueryDataSourceService(getJasperReportsContext(), jdbcDataAdapter);
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = jdbcDataAdapterService.getConnection();

            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            ResultSetMetaData rsmd = resultSet.getMetaData();
            List<String> columnNames = new ArrayList<String>();
            List<String> columnTypes = new ArrayList<String>();
            List<String> columnDescriptions = new ArrayList<String>();
            Map<String, String> fieldMapping = new HashMap<String, String>();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                int typeCode = rsmd.getColumnType(i);
                String jdbcType = getJdbcTypeName(typeCode);
                String dbVendorType = rsmd.getColumnTypeName(i);
                String javaTypeForJdbcType = getJavaTypeForJdbcType(jdbcType, dbVendorType);
                String columnName = rsmd.getColumnName(i);
                columnNames.add(columnName);
                columnTypes.add(javaTypeForJdbcType);
                fieldMapping.put(columnName, rsmd.getColumnLabel(i));
            }

            // create CustomDomainMetaDataImpl object
            CustomDomainMetaDataImpl sourceMetadata = new CustomDomainMetaDataImpl();
            sourceMetadata.setQueryLanguage(getQueryLanguage());
            sourceMetadata.setFieldNames(columnNames);
            sourceMetadata.setFieldMapping(fieldMapping);
            // set default column data type based on the actual data
            sourceMetadata.setFieldTypes(columnTypes);
            sourceMetadata.setQueryText(query);
            sourceMetadata.setFieldDescriptions(columnDescriptions);
            return sourceMetadata;
        } catch (SQLException ex) {
            throw ex;
        } finally {
            if (resultSet != null) { try { resultSet.close(); } catch (Exception ex) {};  }
            if (statement != null) { try { statement.close(); } catch (Exception ex) {};  }
            if (connection != null) { try { connection.close(); } catch (Exception ex) {}; }
        }
    }

    /**
	*  Return data adapter service for this custom data source
	**/
	@Override
    public DataAdapterService getDataAdapterService(JasperReportsContext jasperReportsContext, DataAdapter dataAdapter) {
          return new JDBCQueryDataSourceService(jasperReportsContext, (JdbcDataAdapter)dataAdapter);
    }

    /***  helper functions to convert JDBC data types to JAVA data type ***/

    private static Map<String, String> jdbc2JavaTypeMapping = new HashMap<String, String>();
    private static Map<Integer, String> codeToJdbcTypeMapping = new HashMap<Integer, String>();
    private static final Map<Integer, String> JDBC_TYPES_BY_CODE;
    static {

        Map<Integer, String> map = new HashMap<Integer, String>();
        try {
            Field[] fields = Types.class.getFields();
            for (Field f : fields) {
                map.put((Integer)f.get(null), f.getName());
            }
        } catch (Exception ex) {
            throw new RuntimeException("Cannot access java.sql.Types !");
        }
        JDBC_TYPES_BY_CODE = Collections.unmodifiableMap(map);

        jdbc2JavaTypeMapping.put("BIGINT", "java.lang.Long");
        jdbc2JavaTypeMapping.put("BIT", "java.lang.Boolean");
        jdbc2JavaTypeMapping.put("BOOLEAN", "java.lang.Boolean");
        jdbc2JavaTypeMapping.put("CHAR", "java.lang.String");
        jdbc2JavaTypeMapping.put("DATE", "java.util.Date");
        jdbc2JavaTypeMapping.put("DECIMAL", "java.math.BigDecimal");
        jdbc2JavaTypeMapping.put("DOUBLE", "java.lang.Double");
        jdbc2JavaTypeMapping.put("FLOAT", "java.lang.Float");
        jdbc2JavaTypeMapping.put("INTEGER", "java.lang.Integer");
        jdbc2JavaTypeMapping.put("LONGVARCHAR", "java.lang.String");
        jdbc2JavaTypeMapping.put("NUMERIC", "java.math.BigDecimal");
        jdbc2JavaTypeMapping.put("REAL", "java.lang.Double");
        jdbc2JavaTypeMapping.put("SMALLINT", "java.lang.Short");
        jdbc2JavaTypeMapping.put("TIME", "java.sql.Time");
        jdbc2JavaTypeMapping.put("TIMESTAMP", "java.sql.Timestamp");
        jdbc2JavaTypeMapping.put("TINYINT", "java.lang.Byte");
        jdbc2JavaTypeMapping.put("VARCHAR", "java.lang.String");
        jdbc2JavaTypeMapping.put("NVARCHAR", "java.lang.String");

        codeToJdbcTypeMapping.put(-101, "TIMESTAMP");
        codeToJdbcTypeMapping.put(-102, "TIMESTAMP");
        codeToJdbcTypeMapping.put(100, "FLOAT");
        codeToJdbcTypeMapping.put(101, "DOUBLE");
    }

    private String getJavaTypeForJdbcType(String sqlType, String dbVendorType) {
        Object o = jdbc2JavaTypeMapping.get(sqlType);
        if (o == null) {
            return null;
        }
        if (o instanceof String) {
            return (String) o;
        }
        if (o instanceof Map) {
            Map m = (Map) o;
            return (String) m.get(dbVendorType);
        }
        throw new JSException("Invalid jdbc2JavaTypeMapping configuration!");
    }

    private String getJdbcTypeName(int typeCode) {
        // Look for user defined types
        if (codeToJdbcTypeMapping.containsKey(typeCode)) {
            return codeToJdbcTypeMapping.get(typeCode);
            // If not, look defaults
        } else {
            return JDBC_TYPES_BY_CODE.get(typeCode);
        }
    }

}

