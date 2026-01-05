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

import com.fasterxml.jackson.databind.JsonNode;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractTextDataSource;
import net.sf.jasperreports.engine.data.JsonData;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.json.JRJsonNode;
import net.sf.jasperreports.json.util.DefaultJsonQLExecuter;
import net.sf.jasperreports.json.util.JsonQLExecuter;
import net.sf.jasperreports.json.util.JsonUtil;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Utility class which helps with various tasks related to JSON datasource.
 */
public final class JsonQLDataSourceUtils {

    /**
     * "Special" string which we tag a "special" field with.
     */
    private static final String TAG = "tag";

    /**
     * Runs the query against the resource and returns fields in the query result.
     * @param fileName - the JSON resource
     * @param query - the query
     * @return row iterator produced by the query
     * @throws net.sf.jasperreports.engine.JRException - in case of any error
     */
    public static RowExtractor getRowExtractor(String fileName, String query) throws JRException {
        return new RowExtractor(new File(fileName), query);
    }

    /**
     * Runs the query against the resource and returns fields in the query result.
     * @param is - the JSON input stream
     * @param query - the query
     * @return row iterator produced by the query
     * @throws net.sf.jasperreports.engine.JRException - in case of any error
     */
    public static RowExtractor getRowExtractor(InputStream is, String query) throws JRException {
        return new RowExtractor(is, query);
    }

    /**
     * Creates a special field with a recognizable name.
     */
    private static JRField createTaggedField() {
        JRDesignField taggedField = new JRDesignField();
        taggedField.setName(TAG);
        taggedField.setValueClassName(Object.class.getName());
        return taggedField;
    }

    /**
     * Creates a field with given name and value class. The name is used as the field name as well as description.
     * @param fieldName - the field name
     * @param valueClassName - the field value class name
     */
    private static JRField createField(String fieldName, String valueClassName, ArrayList<String> parentPath) {
        JRDesignField field = new JRDesignField();
        field.setName(getField(parentPath, fieldName));
        field.setValueClassName(valueClassName);
        field.setDescription(getPath(parentPath, fieldName));
        return field;
    }

    static String getPath(ArrayList<String> parentPath, String currentField) {
        String path = "";
        for (String parent : parentPath) path = path + parent + ".";
        return path + currentField;
    }
    static String getField(ArrayList<String> parentPath, String currentField) {
        String path = "";
        for (String parent : parentPath) path = path + parent + "_";
        return path + currentField;
    }

    private JsonQLDataSourceUtils() {
        // prohibit instantiation
    }

    /**
     * Helps extract fields from the result of the query execution.
     */
    public static final class RowExtractor extends JRAbstractTextDataSource implements JsonData {

        private JRJsonNode root;
        private String selectExpression;
        private JRJsonNode currentJsonNode;
        private List<JRJsonNode> nodes;
        private int currentNodeIndex;
        private JsonQLExecuter jsonQLExecuter;

        public RowExtractor(File file, String selectExpression) throws JRException {
            this((JsonNode) JsonUtil.parseJson(file), selectExpression);
        }

        public RowExtractor(InputStream jsonInputStream, String selectExpression) throws JRException {
            this((JsonNode) JsonUtil.parseJson(jsonInputStream), selectExpression);
        }

        protected RowExtractor(JsonNode jacksonJsonTree, String selectExpression) throws JRException {
            this((JRJsonNode)(new JRJsonNode((JRJsonNode)null, jacksonJsonTree)), selectExpression);
        }

        protected RowExtractor(JRJsonNode root, String selectExpression) throws JRException {
            this.currentNodeIndex = -1;
            this.root = root;
            this.selectExpression = selectExpression;
            this.jsonQLExecuter = new DefaultJsonQLExecuter();
            this.moveFirst();
        }

        public void moveFirst() throws JRException {
            if(this.root.getDataNode() != null && !this.root.getDataNode().isMissingNode()) {
                this.currentJsonNode = null;
                this.nodes = this.jsonQLExecuter.selectNodes(this.root, this.selectExpression);
                this.currentNodeIndex = -1;
            } else {
                throw new JRException("data.json.no.data", (Object[])null);
            }
        }

        public boolean next() throws JRException {
            if(this.nodes != null && this.currentNodeIndex < this.nodes.size() - 1) {
                this.currentJsonNode = (JRJsonNode)this.nodes.get(++this.currentNodeIndex);
                return true;
            } else {
                return false;
            }
        }

        public List<JRField> getNextRowFields() throws JRException {
            if (!next()) return null;
            JsonNode row = currentJsonNode.getDataNode();
            if (row == null) return null;
            List<JRField> fields = new ArrayList<JRField>();
            Iterator<Map.Entry<String, JsonNode>> fieldIter = row.fields();
            while (fieldIter.hasNext()) {
                JsonDataSourceUtils.createField(fields, new ArrayList<String>(), fieldIter.next());
            }
            return fields;
        }

        public Object getFieldValue(JRField jrField) throws JRException {
            return null;
        }

        public RowExtractor subDataSource() throws JRException {
            return this.subDataSource((String)null);
        }

        public RowExtractor subDataSource(String selectExpression) throws JRException {
            if(this.currentJsonNode == null) {
                throw new JRException("data.json.no.data", (Object[])null);
            } else {
                RowExtractor subDataSource = new RowExtractor(this.currentJsonNode, selectExpression);
                subDataSource.setTextAttributes(this);
                return subDataSource;
            }
        }

    }
}
