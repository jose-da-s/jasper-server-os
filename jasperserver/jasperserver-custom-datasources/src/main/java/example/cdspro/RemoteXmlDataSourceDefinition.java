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
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomDomainMetaData;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomReportDataSource;
import net.sf.jasperreports.dataadapters.DataAdapter;
import net.sf.jasperreports.dataadapters.DataAdapterService;
import net.sf.jasperreports.data.xml.RemoteXmlDataAdapter;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.jaxen.data.JaxenXmlDataSource;
import net.sf.jasperreports.engine.design.JRDesignField;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A definition of an XML datasource which accesses a remote XML file.
 */
public class RemoteXmlDataSourceDefinition extends AbstractTextDataSourceDefinition {

    private static final long serialVersionUID = -8892282755896154869L;

    @Override
    public CustomDomainMetaData getCustomDomainMetaData(CustomReportDataSource customReportDataSource) throws Exception {
        Map<String, Object> properties = getDataSourceServicePropertyMap(customReportDataSource, new HashMap<String, Object>());

        String uri = (String) properties.get("fileName");
        String query = (String) properties.get("selectExpression");

        JaxenXmlDataSource jrDataSource = new JaxenXmlDataSource(uri, query);
        List<JRField> fields = createStringFields(jrDataSource, query);
        setFieldTypes(jrDataSource, fields);

        return CustomDomainMetadataUtils.createCustomDomainMetaData(getQueryLanguage(), fields);
    }

    private void setFieldTypes(JaxenXmlDataSource jrDataSource, List<JRField> fields) throws JRException {
        jrDataSource.moveFirst();
        List<String> types = getFieldTypes(jrDataSource, fields);
        Iterator<String> typeIter = types.iterator();
        Iterator<JRField> fieldIter = fields.iterator();
        while (typeIter.hasNext() && fieldIter.hasNext()) {
            ((JRDesignField) fieldIter.next()).setValueClassName(typeIter.next());
        }
    }

    @Override
    public DataAdapterService getDataAdapterService(JasperReportsContext jasperReportsContext, DataAdapter dataAdapter) {
        return new DataSourceContributingRemoteXmlDataAdapterService(jasperReportsContext, (RemoteXmlDataAdapter) dataAdapter);
    }

    private List<JRField> createStringFields(JaxenXmlDataSource jrDataSource, String query) throws Exception {
        jrDataSource.moveFirst();
        XmlDataSourceCompatibleJRFieldListBuilder fieldListBuilder = new XmlDataSourceCompatibleJRFieldListBuilder();
        int nodeCount = query.split(Pattern.quote("|")).length;
        if (nodeCount <= 0)  nodeCount = 1;
        for (int nodeIndex = 0; jrDataSource.next() && nodeIndex < nodeCount; nodeIndex++) {
            // adding attribute fields
            NamedNodeMap attrFields = jrDataSource.getCurrentNode().getAttributes();
            if (attrFields != null) {
                int fieldCount = attrFields.getLength();
                for (int i = 0; i < fieldCount; i++) {
                    Node xmlField = attrFields.item(i);
                    String attrName = xmlField.getNodeName();
                    if (attrName.indexOf(':') < 0) { // don't add if namespace prefix present. Fix later (Bug 41535).
                        fieldListBuilder.field("@" + attrName);
                    }
                }
            }
            NodeList xmlFields = jrDataSource.getCurrentNode().getChildNodes();
            int fieldCount = xmlFields.getLength();
            for (int i = 0; i < fieldCount; i++) {
                Node xmlField = xmlFields.item(i);
                if (xmlField.getNodeType() == Node.ELEMENT_NODE) {
                    String fieldName = xmlField.getNodeName();
                    if (fieldName.indexOf(':') < 0) { // don't add if namespace prefix present. Fix later (Bug 41535).
                        fieldListBuilder.field(fieldName);
                    }
                } else if  (xmlField.getNodeType() == Node.TEXT_NODE) {
                    fieldListBuilder.field(".", "col_" + nodeIndex);
                }
            }
        }
        return fieldListBuilder.build();
    }

    @Override
    public Map<String, Object> customizePropertyValueMap(CustomReportDataSource customReportDataSource, Map<String, Object> propertyValueMap) {
        return propertyValueMap;
    }

}
