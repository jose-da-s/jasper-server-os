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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportDataSource;
import net.sf.jasperreports.engine.*;
import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Query;

import net.sf.jasperreports.engine.base.JRBaseDataset;
import net.sf.jasperreports.engine.base.JRBaseField;
import net.sf.jasperreports.engine.base.JRBaseObjectFactory;
import net.sf.jasperreports.engine.base.JRBaseParameter;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignQuery;
import net.sf.jasperreports.engine.query.JRQueryExecuter;
import net.sf.jasperreports.engine.query.QueryExecuterFactory;
import net.sf.jasperreports.engine.type.WhenResourceMissingTypeEnum;
import net.sf.jasperreports.engine.util.JRClassLoader;
import net.sf.jasperreports.engine.util.JRQueryExecuterUtils;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class JRQueryExecuterAdapter {
	public static final String JASPER_QL = "jasperQL";
	
	private static final Log log = LogFactory.getLog(JRQueryExecuterAdapter.class);
	
	public static JRParameter makeParameter(String name, Class type) {
		JRDesignParameter parameter = new JRDesignParameter();
		parameter.setName(name);
		parameter.setValueClass(type);
                parameter.setForPrompting(false);
		return parameter;
	}
	
	public static OrderedMap executeQuery(final Query query, 
			final String keyColumn, final String[] resultColumns, 
			Map parameterValues) {
		return executeQuery(query, keyColumn, resultColumns, parameterValues, null);
	}

    public static OrderedMap executeQuery(final Query query,
			final String keyColumn, final Class keyColumnClass, final String[] resultColumns,
			Map parameterValues) {
		return executeQuery(query, keyColumn, keyColumnClass, resultColumns, parameterValues, null, null, true, null);
	}

    public static OrderedMap executeQuery(final Query query,
                                          final String keyColumn, final String[] resultColumns,
                                          Map parameterValues, List additionalParameters) {
        return executeQuery(query, keyColumn, resultColumns, parameterValues, null, additionalParameters, true, null);
    }

	public static OrderedMap executeQuery(final Query query, 
			final String keyColumn, final String[] resultColumns, 
			Map parameterValues, Map<String, Class<?>> parameterTypes, List additionalParameters, boolean formatValueColumns, ReportDataSource dataSource) {
        return executeQuery(query, keyColumn, Object.class, resultColumns, parameterValues, parameterTypes, additionalParameters, formatValueColumns, dataSource);
    }

    public static OrderedMap executeQuery(final Query query,
			final String keyColumn, Class keyColumnClass, final String[] resultColumns,
			Map parameterValues, Map<String, Class<?>> parameterTypes, List additionalParameters, boolean formatValueColumns, ReportDataSource dataSource) {


		try {
			QueryExecuterFactory queryExecuterFactory = JRQueryExecuterUtils.getInstance(DefaultJasperReportsContext.getInstance()).getExecuterFactory(query.getLanguage());
			
			JRParameter[] dsParameters = getDatasetParameters(queryExecuterFactory, 
					parameterValues, parameterTypes, additionalParameters);
			JRField[] fields = getDatasetFields(keyColumn, keyColumnClass, resultColumns);
			JRQuery dsQuery = makeReportQuery(query);
			JSDataset dataset = new JSDataset(query.getName(), dsParameters, fields, dsQuery);
			
			Map parametersMap = new HashMap();
			for (int i = 0; i < dsParameters.length; i++) {
				JRParameter parameter = dsParameters[i];
				parametersMap.put(parameter.getName(), parameter);
			}

            JasperReportsContext jasperReportsContext = DefaultJasperReportsContext.getInstance();
            JRQueryExecuter executer = queryExecuterFactory.createQueryExecuter(
                    jasperReportsContext,
                    dataset,
                    parametersMap
            );


			try {

				setExecuterRefereneceDS(query, dataSource, executer);

				JRDataSource ds = executer.createDatasource();
				OrderedMap values = new LinkedMap();
				while (ds.next()) {
					Object valueColumn = ds.getFieldValue(dataset.getField(keyColumn));
					
					Object[] visibleColumnValues = new Object[resultColumns.length];
					for (int idx = 0; idx < resultColumns.length; ++idx) {
						Object fieldValue = ds.getFieldValue(dataset.getField(resultColumns[idx]));
						visibleColumnValues[idx] = convertColumnValue(fieldValue, formatValueColumns);
					}

					values.put(valueColumn, convertVisibleColumnsValues(visibleColumnValues, formatValueColumns));
				}
				
				return values;
			} finally {
				executer.close();
			}
		} catch (JRException e) {
			log.error("Error while executing query", e);
			throw new JSExceptionWrapper(e);
		}

	}

	/**
	 * If the language is JASPER_QL(JSS) then set the reference datasource for the executor
	 * @param query
	 * @param dataSource
	 * @param executer
	 */
	public static void setExecuterRefereneceDS(Query query, ReportDataSource dataSource, JRQueryExecuter executer) {
		if(query.getLanguage().equals(JASPER_QL)) {
			JRJaperQLExecuter jasperQLExecuter = (JRJaperQLExecuter) executer;
			jasperQLExecuter.setDataSource(dataSource);
		}
	}

	private static Object convertVisibleColumnsValues(Object[] visibleColumnsValues, boolean formatValueColumns) {
        if (!formatValueColumns) {
            return visibleColumnsValues;
        } else {
            return Arrays.copyOf(visibleColumnsValues, visibleColumnsValues.length, String[].class);
        }
    }

    private static Object convertColumnValue(Object fieldValue, boolean formatValueColumns) {
        if (!formatValueColumns) {
            return fieldValue;
        } else {
            return fieldValue == null ? null : fieldValue.toString();
        }
    }

    protected static JRQuery makeReportQuery(Query query) {
		JRDesignQuery reportQuery = new JRDesignQuery();
		reportQuery.setLanguage(query.getLanguage());
		reportQuery.setText(query.getSql());
		return reportQuery;
	}

	protected static JRField[] getDatasetFields(final String keyColumn, Class keyColumnClass, final String[] resultColumns) {
		List fields = new ArrayList(resultColumns.length + 1);
        fields.add(new ColumnField(keyColumn, keyColumnClass));
		for (int idx = 0; idx < resultColumns.length; ++idx) {
			String resultColumn = resultColumns[idx];
			if (!resultColumn.equals(keyColumn))
				//do not define the field again if the key column is also a result column
			{
				fields.add(new ColumnField(resultColumn, String.class));
			}
		}
		return (JRField[]) fields.toArray(new JRField[fields.size()]);
	}

	protected static JRParameter[] getDatasetParameters(QueryExecuterFactory queryExecuterFactory,
			Map parameterValues, List additionalParameters) {
        return getDatasetParameters(queryExecuterFactory, parameterValues, null, additionalParameters);
    }

	protected static JRParameter[] getDatasetParameters(QueryExecuterFactory queryExecuterFactory,
			Map<String, Object> parameterValues, Map<String, Class<?>> parameterTypes, List additionalParameters) {
		boolean jdbcConnectionParam = parameterValues.containsKey(JRParameter.REPORT_CONNECTION);
		
		List<ValueParameter> dsParameters = new ArrayList<ValueParameter>();
		
		dsParameters.add(new ValueParameter(JRParameter.REPORT_PARAMETERS_MAP, Map.class, parameterValues));

        // Also add the given parameters as top level parameters for parameterized queries
        for (Map.Entry<String, Object> entry : parameterValues.entrySet()) {
            // Determine class of the value
            Class<?> clazz = null;
            if (entry.getValue() != null) {
                clazz = entry.getValue().getClass();
            } else if (parameterTypes != null && parameterTypes.get(entry.getKey()) != null) {
                clazz = parameterTypes.get(entry.getKey());
            }

            // If class is null, then is doesn't make sense to add this parameter,
            // because without class it can't be set into sql statement.
            if (clazz != null) {
                dsParameters.add(new ValueParameter(entry.getKey(), clazz, entry.getValue()));
            }
        }

		dsParameters.add(new ValueParameter(JRParameter.REPORT_MAX_COUNT, Integer.class, 
				parameterValues.get(JRParameter.REPORT_MAX_COUNT)));

		if (jdbcConnectionParam) {
			Object value = parameterValues.get(JRParameter.REPORT_CONNECTION);
			dsParameters.add(new ValueParameter(JRParameter.REPORT_CONNECTION, Connection.class, value));
		}
		
		Object[] builtinParameters = queryExecuterFactory.getBuiltinParameters(); 
		if (builtinParameters != null) {
			for (int i = 0; i < builtinParameters.length - 1; i += 2) {
				String name = (String) builtinParameters[i];
				Object paramType = builtinParameters[i + 1];
				Class<?> type = loadParameterType(paramType);
				
				Object value = parameterValues.get(name);
				dsParameters.add(new ValueParameter(name, type, value));
			}
		}
		
		if (additionalParameters != null && !additionalParameters.isEmpty()) {
			for (Iterator it = additionalParameters.iterator(); it.hasNext();) {
				JRParameter parameter = (JRParameter) it.next();
				Object value = parameterValues.get(parameter.getName());
				dsParameters.add(new ValueParameter(parameter, value));
			}
		}
		
		JRParameter[] params = new JRParameter[dsParameters.size()];
		return (JRParameter[]) dsParameters.toArray(params);
	}

	public static Class<?> loadParameterType(Object paramType) {
		try {
			Class<?> type;
			// the type can be given either as a class or as a class name
			if (paramType instanceof Class) {
				type = (Class<?>) paramType;
			} else if (paramType instanceof String) {
				String paramClass = JRClassLoader.getClassRealName((String) paramType);
				type = JRClassLoader.loadClassForName(paramClass);
			} else {
				throw new JSException("Unknown query executer parameter type class " + paramType.getClass().getName());
			}
			return type;
		} catch (ClassNotFoundException e) {
			throw new JSException("Failed to load query executer paramter type " + paramType, e);
		}
	}

	
	public static JRQueryExecuter createQueryExecuter(JasperReport report, Map parameterValues, Query query) {
		try {
			QueryExecuterFactory queryExecuterFactory = JRQueryExecuterUtils.getInstance(DefaultJasperReportsContext.getInstance()).getExecuterFactory(query.getLanguage());

			ReportQueryDataset dataset = new ReportQueryDataset(report, query, queryExecuterFactory);

			JRBaseObjectFactory jrObjectFactory = new ShallowJRObjectFactory(report);			
			Map parametersMap = new HashMap();
			JRParameter[] parameters = dataset.getParameters();
			for (int i = 0; i < parameters.length; i++) {
				JRParameter parameter = parameters[i];
				
				Object value;
				if (JRParameter.REPORT_PARAMETERS_MAP.equals(parameter.getName())) {
					value = parameterValues;
				} else {
					value = parameterValues.get(parameter.getName());
				}
				
				ParameterValueDecorator parameterValue = new ParameterValueDecorator(parameter, value, jrObjectFactory);
				parametersMap.put(parameter.getName(), parameterValue);
			}

            JasperReportsContext jasperReportsContext = DefaultJasperReportsContext.getInstance();
            JRQueryExecuter executer = queryExecuterFactory.createQueryExecuter(
                    jasperReportsContext,
                    dataset,
                    parametersMap
            );
			return executer;
		} catch (JRException e) {
			throw new JSExceptionWrapper(e);
		}
	}

	
	protected static class ColumnField extends JRBaseField {
		
		public ColumnField(final String column, final Class type) {
			this.name = column;
			this.valueClass = type;
			this.valueClassName = type.getName();
		}
		
	}
	
	
	protected static class JSDataset extends JRBaseDataset {
		
		private final Map fieldsMap;
		
		public JSDataset(final String name, final JRParameter[] parameters, final JRField[] fields, final JRQuery query) {
			super(false);
			
			this.name = name;
			this.parameters = parameters;
			this.fields = fields;
			this.query = query;
			
			fieldsMap = new HashMap();
			for (int i = 0; i < fields.length; i++) {
				JRField field = fields[i];
				fieldsMap.put(field.getName(), field);
			}
		}
		
		public JRField getField(String column) {
			return (JRField) fieldsMap.get(column);
		}
		
	}
	
	protected static class ValueParameter extends JRBaseParameter implements JRValueParameter {
		
		private Object value;
		
		public ValueParameter(JRParameter parameter, Object value) {
			this(parameter.getName(), parameter.getValueClass(),
					value);
		}
		
		public ValueParameter(String name, Class type, Object value) {
			this.name = name;
			this.valueClass = type;
			this.valueClassName = type.getName();
			
			this.isSystemDefined = true;
			this.isForPrompting = false;
			
			this.value = value;
		}
		
		public Object getValue() {
			return value;
		}
		
		public void setValue(Object value) {
			this.value = value;
		}
		
	}
	
	
	protected static class ReportQueryDataset implements JRDataset {
		
		private final JRDataset reportDataset;
		private final JRParameter[] parameters;
		private final JRQuery query;
		
		public ReportQueryDataset(JasperReport report, Query query, QueryExecuterFactory queryExecuterFactory) {
			this.reportDataset = report.getMainDataset();
			this.query = getQuery(query);
			this.parameters = getParams(queryExecuterFactory);
		}
		
		private JRQuery getQuery(Query query) {
			JRDesignQuery designQuery = new JRDesignQuery();
			designQuery.setLanguage(query.getLanguage());
			designQuery.setText(query.getSql());
			return designQuery;
		}

		private JRParameter[] getParams(QueryExecuterFactory queryExecuterFactory) {
			JRParameter[] paramArray;
			Object[] builtinParameters = queryExecuterFactory.getBuiltinParameters();
			if (builtinParameters == null || builtinParameters.length == 0) {
				paramArray = this.reportDataset.getParameters();
			} else {
				JRParameter[] reportParams = this.reportDataset.getParameters();
				List params = new ArrayList(reportParams.length + builtinParameters.length / 2);
				
				Set paramNames = new HashSet();
				for (int i = 0; i < reportParams.length; i++) {
					JRParameter parameter = reportParams[i];
					params.add(parameter);
					paramNames.add(parameter.getName());
				}
				
				for (int i = 0; i < builtinParameters.length - 1; i += 2) {
					String name = (String) builtinParameters[i];
					if (!paramNames.contains(name)) {
						Object paramType = builtinParameters[i + 1];
						Class<?> type = loadParameterType(paramType);
						params.add(new ValueParameter(name, type, null));
					}
				}

				paramArray = new JRParameter[params.size()];
				paramArray = (JRParameter[]) params.toArray(paramArray);
			}
			return paramArray;
		}

		public String getName() {
			return reportDataset.getName();
		}

		public String getScriptletClass() {
			return reportDataset.getScriptletClass();
		}

		public JRParameter[] getParameters() {
			return parameters;
		}

		public JRQuery getQuery() {
			return query;
		}

		public JRScriptlet[] getScriptlets() {
			return reportDataset.getScriptlets();
		}

		public JRField[] getFields() {
			return reportDataset.getFields();
		}

		public JRVariable[] getVariables() {
			return reportDataset.getVariables();
		}

		public JRGroup[] getGroups() {
			return reportDataset.getGroups();
		}

		public boolean isMainDataset() {
			return true;
		}

		public String getResourceBundle() {
			return reportDataset.getResourceBundle();
		}

		public WhenResourceMissingTypeEnum getWhenResourceMissingType() {
            return reportDataset.getWhenResourceMissingType();
        }

		public void setWhenResourceMissingType(byte type) {
			//nothing
		}

		public void setWhenResourceMissingType(WhenResourceMissingTypeEnum type) {
			//nothing
		}

		public JRPropertiesMap getPropertiesMap() {
			return reportDataset.getPropertiesMap();
		}

		public JRPropertiesHolder getParentProperties() {
			return reportDataset.getParentProperties();
		}

		public boolean hasProperties() {
			return reportDataset.hasProperties();
		}

		public JRExpression getFilterExpression() {
			return reportDataset.getFilterExpression();
		}
		
		public JRSortField[] getSortFields() {
			return reportDataset.getSortFields();
		}
		
		public Object clone() {
			throw new JSException("Clone not supported");
		}

		public UUID getUUID() {
			return reportDataset.getUUID();
		}

		@Override
		public DatasetPropertyExpression[] getPropertyExpressions() {
			return reportDataset.getPropertyExpressions();
		}
		
	}
	
	
	protected static class ParameterValueDecorator extends JRBaseParameter implements JRValueParameter {
		
		private Object value;
		
		public ParameterValueDecorator(final JRParameter parameter, final Object value, JRBaseObjectFactory jrObjectFactory) {
			super(parameter, jrObjectFactory);
			this.value = value;
		}

		public Object getValue() {
			return value;
		}
		
		public void setValue(Object value) {
			this.value = value;
		}
		
	}
	
	protected static class ShallowJRObjectFactory extends JRBaseObjectFactory {

		protected ShallowJRObjectFactory(JRDefaultStyleProvider defaultStyleProvider) {
			super(defaultStyleProvider);
		}

		public JRExpression getExpression(JRExpression expression) {
			return expression;
		}
		
	}
	
}
