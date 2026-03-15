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
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.json.data.JsonDataSource;
import net.sf.jasperreports.engine.design.JRDesignField;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Utility class which helps with various tasks related to JSON datasource.
 */
public final class JsonDataSourceUtils {

    /**
     * "Special" string which we tag a "special" field with.
     */
    private static final String TAG = "tag";

    /**
     * Runs the query against the resource and returns fields in the query result.
     * @param fileName - the JSON resource
     * @param query - the query
     * @return row iterator produced by the query
     * @throws JRException - in case of any error
     */
    public static RowExtractor getRowExtractor(String fileName, String query) throws JRException {
        return new RowExtractor(fileName, query);
    }

    /**
     * Runs the query against the resource and returns fields in the query result.
     * @param is - the JSON input stream
     * @param query - the query
     * @return row iterator produced by the query
     * @throws JRException - in case of any error
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
     * Creates a field from the map entry.
     * @param fieldEntry - the map entry
     */
    static void createField(List<JRField> fields, ArrayList<String> parentPath, Map.Entry<String, JsonNode> fieldEntry) {
        if (fieldEntry.getValue().isBoolean()) {
            fields.add(createField(fieldEntry.getKey(), Boolean.class.getName(), parentPath));
        } else if (fieldEntry.getValue().isInt()) {
            fields.add(createField(fieldEntry.getKey(), Integer.class.getName(), parentPath));
        } else if (fieldEntry.getValue().isLong()) {
            fields.add(createField(fieldEntry.getKey(), Long.class.getName(), parentPath));
        } else if (fieldEntry.getValue().isDouble()) {
            fields.add(createField(fieldEntry.getKey(), Double.class.getName(), parentPath));
        } else if (fieldEntry.getValue().isBigDecimal()) {
            fields.add(createField(fieldEntry.getKey(), BigDecimal.class.getName(), parentPath));
        } else if (fieldEntry.getValue().isBigInteger()) {
            fields.add(createField(fieldEntry.getKey(), BigInteger.class.getName(), parentPath));
        } else if (fieldEntry.getValue().isTextual()) {
            fields.add((createField(fieldEntry.getKey(), String.class.getName(), parentPath)));
        } else if (fieldEntry.getValue() instanceof ObjectNode) {
            ArrayList<String> cloneParentPath = (ArrayList<String>)parentPath.clone();
            cloneParentPath.add(fieldEntry.getKey());
            for (Iterator iterator = ((ObjectNode) fieldEntry.getValue()).fields(); iterator.hasNext(); ) {
                Object nextObject = iterator.next();
                createField(fields,  cloneParentPath, (Map.Entry) nextObject);
            }
        } else {
            // unknown object type, cast to string for now
            createField(fieldEntry.getKey(), String.class.getName(), parentPath);
        }
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

    private JsonDataSourceUtils() {
        // prohibit instantiation
    }

    /**
     * Helps extract fields from the result of the query execution.
     */
    public static final class RowExtractor extends JsonDataSource {

        public RowExtractor(String fileName, String selectExpression) throws JRException {
            super(fileName, selectExpression);
            // at this point the resource is fully read and can be released.
            //close(); // no longer required
        }

        public RowExtractor(InputStream is, String selectExpression) throws JRException {
            super(is, selectExpression);
            // at this point the resource is fully read and can be released.
            //close(); // no longer required
        }

        @Override
        protected JsonNode getJsonData(JsonNode rootNode, String jsonExpression) throws JRException {
            // This method is called once during the instantiation and once for each getFieldValue() call.
            // Here we let the instantiation call trough but intercept the call from inside getFieldValue().
            // We call getFieldValue() ourselves and pass a special field with a special name so here we can recognize it
            // and return back the root node.
            if (jsonExpression == TAG) {
                return rootNode;
            }
            return super.getJsonData(rootNode, jsonExpression);
        }

        private JsonNode getRow() throws JRException {
            // getting just the first row is enough
            return next() ? (JsonNode) getFieldValue(createTaggedField()) : null;
        }

        public List<JRField> getNextRowFields()  throws JRException {
            JsonNode row = getRow();
            if (row == null) return null;
            List<JRField> fields = new ArrayList<JRField>();
            Iterator<Map.Entry<String, JsonNode>> fieldIter = row.fields();
            while (fieldIter.hasNext()) {
                createField(fields, new ArrayList<String>(), fieldIter.next());
            }
            return fields;
        }
    }
}
