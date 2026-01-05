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

package com.jaspersoft.jasperserver.api.engine.jasperreports.util;


import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomDomainMetaData;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomReportDataSource;
import net.sf.jasperreports.data.xlsx.XlsxDataAdapter;
import net.sf.jasperreports.data.xlsx.XlsxDataAdapterImpl;
import net.sf.jasperreports.poi.data.JRXlsxDataSource;
import net.sf.jasperreports.engine.design.JRDesignField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author ichan
 * CSV specific data source definition to create data connector (custom report data source) for CSV source
 * This class is meant to be instantiated as a spring bean that registers a custom data source with the system.
 */
public class XlsxDataSourceDefinition extends AbstractTextDataSourceDefinition {

    private static String FILE_NAME_PROP = "fileName";


    public XlsxDataSourceDefinition() {

        // searching column data type under the following order:
        typeList = new Class[] {Boolean.class, Number.class, java.util.Date.class, String.class};

        // define default values for the following properties
        Map<String, String> propertyDefaultValueMap = getPropertyDefaultValueMap();
        propertyDefaultValueMap.put("name", "xlsx");
        propertyDefaultValueMap.put("datePattern", "yyyy-MM-dd HH:mm:ss");
        propertyDefaultValueMap.put("queryExecuterMode", "false");
        propertyDefaultValueMap.put("useFirstRowAsHeader", "true");

        // hide the following properties from UI
        Set<String> hiddenPropertySet = getHiddenPropertySet();
        hiddenPropertySet.add("name");
        hiddenPropertySet.add("dataFile");
        hiddenPropertySet.add("queryExecuterMode");
        hiddenPropertySet.add("columnIndexes");
        hiddenPropertySet.add("columnNames");
    }

    /*
     * This function is used for retrieving the metadata layer of the data connector in form of TableSourceMetadata
     * TableSourceMetadata contains information JRFields, query, query language and field name mapping (actual JRField name, name used in domain)
     *
     * NOTE:  Thread Repository Context needs to be set up properly before looking up csv file within repo
     */
    public CustomDomainMetaData getCustomDomainMetaData(CustomReportDataSource customDataSource) throws Exception {

        // METADATA DISCOVERY
        Map<String, Object> propertyValueMap = new HashMap<String, Object>();
        propertyValueMap = getDataSourceServicePropertyMap(customDataSource, propertyValueMap);
        propertyValueMap = customizePropertyValueMap(customDataSource, propertyValueMap);

        // GET CSV DATA ADAPTER from Custom Report Data Source
        // map custom report data source properties to data adapter
        XlsxDataAdapter xlsxDataAdapter = new XlsxDataAdapterImpl();
        xlsxDataAdapter = (XlsxDataAdapter) setupDataAdapter(xlsxDataAdapter, propertyValueMap);

        boolean useFirstRowAsHeader = (Boolean) propertyValueMap.get("useFirstRowAsHeader");
        boolean containsColumnNames = AbstractTextDataSourceDefinition.containsValue((List) propertyValueMap.get("columnNames"));
        List<String> fieldNames;
        Map<String, String> fieldMapping;
        JRXlsxDataSource xlsxDataSource;
        if (!useFirstRowAsHeader && !containsColumnNames) {
            // force to use first row as header, so we can get the column count
            // then we can assign arbitrary names to columns
            xlsxDataAdapter.setUseFirstRowAsHeader(true);
            // get JRDataSource from data adapter
            xlsxDataSource = (JRXlsxDataSource)getJRDataSource(xlsxDataAdapter);
            // get default JRField names
            fieldNames = getDefaultFieldNames(xlsxDataSource);
            // create default name mapping <JRField Name, Display Name in Domain>
            fieldMapping = AbstractTextDataSourceDefinition.getDefaultFieldMapping(fieldNames);
            propertyValueMap.put("columnNames", fieldNames);
            xlsxDataSource.close();
            // create a new JRDataSource without using first row as header
            // therefore, we can detect the data type starting from the first row of data
            xlsxDataAdapter = (XlsxDataAdapter) setupDataAdapter(xlsxDataAdapter, propertyValueMap);
            xlsxDataSource = (JRXlsxDataSource)getJRDataSource(xlsxDataAdapter);
            xlsxDataSource.next();
        } else {
            // get JRDataSource from data adapter
            xlsxDataSource = (JRXlsxDataSource)getJRDataSource(xlsxDataAdapter);
            // get default JRField names
            fieldNames = getFieldNames(xlsxDataSource);
            // create default name mapping <JRField Name, Display Name in Domain>
            fieldMapping = AbstractTextDataSourceDefinition.getFieldMapping(fieldNames);
        }
        // create TableSourceMetadata object
        CustomDomainMetaDataImpl sourceMetadata = new CustomDomainMetaDataImpl();
        sourceMetadata.setQueryLanguage(getQueryLanguage());
        sourceMetadata.setFieldNames(fieldNames);
        sourceMetadata.setFieldMapping(fieldMapping);
        // set default column data type based on the actual data
        sourceMetadata.setFieldTypes(getFieldTypes(xlsxDataSource, sourceMetadata.getJRFieldList()));

        xlsxDataSource.close();
        return sourceMetadata;

    }

    public Map<String, Object>  customizePropertyValueMap(CustomReportDataSource customReportDataSource, Map<String, Object>  propertyValueMap) {
        if (propertyValueMap.get(FILE_NAME_PROP) != null) {
            // remove organization information from REPO path and set it back to property map
            // for example:  original repo path:  repo:/reports/interactive/CsvData|organization_1
            // new path:  repo:/reports/interactive/CsvData
            propertyValueMap.put(FILE_NAME_PROP, getSourceFileLocation(customReportDataSource));
            log.debug("Set Source File Location for Data Adapter to " + propertyValueMap.get(FILE_NAME_PROP));
        }
        return propertyValueMap;
    }

    // get JRField names from JRDataSource
    private List<String> getFieldNames(JRXlsxDataSource xlsxDataSource) throws Exception {
        // SET COLUMN NAMES
        if (!xlsxDataSource.next()) return null;
        Map<String, Integer> columnNames = xlsxDataSource.getColumnNames();
        String columnNameArray[] = new String[columnNames.size()];
        for (Map.Entry<String, Integer> entry : columnNames.entrySet()) {
            isValidFieldName(entry.getKey());
            System.out.println("KEY = " + entry.getKey() + ", VAL = " + entry.getValue());
            columnNameArray[entry.getValue()] = entry.getKey();
        }
        return Arrays.asList(columnNameArray);
    }

    /*
     * get arbitrary JRField names from JRDataSource (if header is not included in the data)
     * should be like COLUMN_1, COLUMN_2, COLUMN_3...
     */
    private List<String> getDefaultFieldNames(JRXlsxDataSource xlsxDataSource) throws Exception {
        if (!xlsxDataSource.next()) return null;
        int fieldCount = 0;
        Map<String, Integer> columnNames = xlsxDataSource.getColumnNames();
        for (Map.Entry<String, Integer> entry : columnNames.entrySet()) {
            if (entry.getValue() >= fieldCount) fieldCount = entry.getValue() + 1;
        }
        List<String> fieldNames = new ArrayList<String>();
        for (int i = 0; i < fieldCount; i++) {
            fieldNames.add("COLUMN_" + i);
        }
        return fieldNames;
    }

    /*
     * OVERRIDE
     * String value "true" and "false" don't consider as boolean value for XLSX
     * XLSX has its defined Boolean type
     */
    @Override
    protected Class getBooleanType(String value) {
        return null;
    }

    // if it is number, get the field value as double first
    // then call getNumericType() to find out whether it is integer/ long/ double
    @Override
    protected Class getFieldType(Class type) {
        if (type == Number.class) return Double.class;
        else return type;
    }

    /*
     * XLSX defines date object as numeric field
     * Currently, we can only search for "DATE" keyword from field label in order to determine whether it is DATE object
     */
    @Override
    protected Class getNumericType(Object obj, JRDesignField field) {
        Class numericType = super.getNumericType(obj, field);
        if ((numericType != null) && (field.getName().toUpperCase().indexOf("DATE") >= 0)) {
            numericType = java.util.Date.class;
        }
        return numericType;
    }

    /**
     * obtain file location from REPO path (removing the tenant information)
     * for example:  original repo path:  repo:/reports/interactive/CsvData|organization_1
     * new path:  repo:/reports/interactive/CsvData
     **/
    private String getSourceFileLocation(CustomReportDataSource customDataSource) {
        String fileName = (String) customDataSource.getPropertyMap().get(FILE_NAME_PROP);
        int sepIndex = fileName.lastIndexOf("|");
        if (fileName.startsWith("repo:/") && (sepIndex > 0)) {
            return fileName.substring(0, sepIndex);
        }
        return fileName;
    }

}
