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

import com.jaspersoft.jasperserver.api.engine.jasperreports.util.AbstractTextDataSourceDefinition;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomDomainMetaData;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomReportDataSource;
import net.sf.jasperreports.dataadapters.DataAdapter;
import net.sf.jasperreports.dataadapters.DataAdapterService;
import net.sf.jasperreports.data.json.JsonDataAdapter;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.design.JRDesignField;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines JSON datasource which can be read from a file or repository.
 */
public class JsonQLDataSourceDefinition extends AbstractTextDataSourceDefinition {

    private static final long serialVersionUID = -1698557680384988822L;
    private RepositoryService repositoryService;

    public JsonQLDataSourceDefinition() {
        // define default values for the following properties
        Map<String, String> propertyDefaultValueMap = getPropertyDefaultValueMap();
        propertyDefaultValueMap.put("useConnection", "true");
        propertyDefaultValueMap.put("language", "jsonql");
        propertyDefaultValueMap.put("datePattern", "yyyy-MM-dd");
        propertyDefaultValueMap.put("numberPattern", "#,##0.##");
    }

    @Override
    public CustomDomainMetaData getCustomDomainMetaData(CustomReportDataSource customDataSource) throws Exception {

        Map<String, Object> propertyValueMap = new HashMap<String, Object>();
        propertyValueMap = getDataSourceServicePropertyMap(customDataSource, propertyValueMap);

        List<JRField> jrFields = getFields(customDataSource, propertyValueMap);

        return CustomDomainMetadataUtils.createCustomDomainMetaData(getQueryLanguage(), jrFields);
    }

    private List<JRField> getFields(CustomReportDataSource customDataSource, Map<String, Object> propertyValueMap) throws JRException {
        String query = (String) propertyValueMap.get("selectExpression");
        ResourceReference resourceRef = null;
        if (customDataSource.getResources() != null) {
            resourceRef = customDataSource.getResources().get("dataFile");
        }

        JsonQLDataSourceUtils.RowExtractor rowExtractor = null;
        // if no resource then it's a file in a file system
        InputStream is = null;
        if (resourceRef == null) {
            String fileName = (String) propertyValueMap.get("fileName");
            if (fileName.toLowerCase().startsWith("http:") || fileName.toLowerCase().startsWith("https:")) {
                try {
                    is = (new URL(fileName)).openStream();
                    rowExtractor = JsonQLDataSourceUtils.getRowExtractor(is, query);
                    is.close();
                } catch (Exception ex) {
                    throw new JRException(ex.getMessage());
                }
            } else {
                rowExtractor = JsonQLDataSourceUtils.getRowExtractor((String) propertyValueMap.get("fileName"), query);
            }
        } else {
            is = JsonDataSourceDefinition.getResourceInputStream(resourceRef, repositoryService);
            rowExtractor = JsonQLDataSourceUtils.getRowExtractor(is, query);
        }
        List<JRField> jrFields = rowExtractor.getNextRowFields();
        for (int i = 0; (getRowCountForMetadataDiscovery() < 0) || (i < getRowCountForMetadataDiscovery()); i++) {
             List<JRField> newRowFields = rowExtractor.getNextRowFields();
            if (newRowFields == null) break;
            for (int j = 0; j < newRowFields.size(); j++) {
                String name = newRowFields.get(j).getName();
                JRField curField = findField(jrFields, name);
                if (curField == null) {
                    jrFields.add(newRowFields.get(j));
                } else {
                    ((JRDesignField)curField).setValueClassName(getCompatibleDataType(curField.getValueClassName(), newRowFields.get(j).getValueClassName()));
                }
            }
        }


        return jrFields;
    }

    private JRField findField(List<JRField>jrFields, String name) {
        for (JRField jrField: jrFields) {
            if (jrField.getName().equals(name)) return jrField;
        }
        return null;
    }

    @Override
    public DataAdapterService getDataAdapterService(JasperReportsContext jasperReportsContext, DataAdapter dataAdapter) {
        return new DataSourceContributingJsonDataAdapterService(jasperReportsContext, (JsonDataAdapter) dataAdapter, repositoryService);
    }

    @Override
    public Map<String, Object> customizePropertyValueMap(CustomReportDataSource customReportDataSource, Map<String, Object> propertyValueMap) {
        return propertyValueMap;
    }

    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

}
