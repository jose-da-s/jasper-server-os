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

package com.jaspersoft.jasperserver.export.modules.common;

import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.ReportLoadingService;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.MaterializedDataParameter;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResourceData;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.service.JSResourceNotFoundException;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.export.modules.common.rd.DateRangeDTO;
import com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.InputControlHandler;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.util.Pair;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.CustomJRXmlLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.types.date.DateRange;
import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exolab.castor.types.AnyNode;
import org.exolab.castor.types.DateTime;
import org.exolab.castor.xml.handlers.SQLTimestampFieldHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.sql.Date;
import java.sql.Time;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class DefaultReportParametersTranslator implements
		ReportParametersTranslator {

	private static final Log log = LogFactory.getLog(DefaultReportParametersTranslator.class);
	
	// Null substitute value
    public static final String NULL_SUBSTITUTE = InputControlHandler.NULL_SUBSTITUTION_VALUE;

    private RepositoryService repository;

    private ReportLoadingService reportLoadingService;

    public void setReportLoadingService(ReportLoadingService reportLoadingService) {
        this.reportLoadingService = reportLoadingService;
    }

    public ReportParameterValueBean[] getBeanParameterValues(
			String reportUnitURI, Map values) {
		ReportParameterValueBean[] beanValues;
		if (values == null || values.isEmpty()) {
			beanValues = null;
		} else {
			beanValues = new ReportParameterValueBean[values.size()];
			int idx = 0;
			for (Iterator it = values.entrySet().iterator(); it.hasNext(); ++idx) {
				Map.Entry entry = (Map.Entry) it.next();
				String name = (String) entry.getKey();
				Object value = entry.getValue();
				ReportParameterValueBean param = toParameterValueBean(name, value);
				beanValues[idx] = param;
			}
		}
		return beanValues;
	}

	protected ReportParameterValueBean toParameterValueBean(String name, Object value) {
		Object[] values;
		String valueType = null;
		if (value == null) {
			values = new Object[] { toBeanParameterValue(null) };
		} else if (value.getClass().isArray()) {
			int count = Array.getLength(value);
			values = new Object[count];
			for (int idx = 0; idx < count; idx++) {
				values[idx] = toBeanParameterValue(Array.get(value, idx));
			}
		} else if (value instanceof Collection) {
			Collection valueCollection = (Collection) value;
            values = new Object[valueCollection.size()];

            Iterator iterator = valueCollection.iterator();
            for (int i = 0; iterator.hasNext(); i++) {
                values[i] = toBeanParameterValue(iterator.next());
            }
		} else {
			values = new Object[] { toBeanParameterValue(value) };
			valueType = ReportParameterValueBean.VALUE_TYPE_SINGLE;
		}

		ReportParameterValueBean paramBean = new ReportParameterValueBean(name, values, valueType);
		return paramBean;
	}

    protected Object toBeanParameterValue(Object value) {
        // Null substitute
        if (value == null) {
            return NULL_SUBSTITUTE;
        }

        if (value instanceof DateRange) {
            return new DateRangeDTO((DateRange)value);
        }
        
        if (value instanceof MaterializedDataParameter) {
        		MaterializedDataParameter param = (MaterializedDataParameter) value;
        		return new MaterializedDataParameterBean(toBeanParameterValue(param.getParameterValue()), 
        				toBeanParameterValue(param.getEffectiveValue()));
        }
        
        //used via MaterializedDataParameter for DateRanges
        if (value instanceof Pair) {
        		Pair<?, ?> pair = (Pair<?, ?>) value;
        		return new PairBean(toBeanParameterValue(pair.first()), toBeanParameterValue(pair.second()));
        }

        return value;
    }

    public Map<String, Object> getParameterValues(String reportUnitURI,
                                                  ReportParameterValueBean[] beanValues, ExecutionContext context) {
        if (beanValues == null || beanValues.length == 0) {
            return null;
        } else {
            ReportUnit reportUnit = (ReportUnit) getRepository().getResource(
                    null, reportUnitURI, ReportUnit.class);
			if (reportUnit == null) {
				throw new JSResourceNotFoundException("jsexception.resource.of.type.not.found",
						new Object[] {"\"" + reportUnitURI + "\"", ReportUnit.class});
			}
            return getParameterValues(reportUnit, beanValues, context);
        }
    }

    protected void populateParameterValues(ReportUnit reportUnit, ReportParameterValueBean[] beanValues,
			Map<String, Object> values, ExecutionContext context) {
        Map<String, InputControl> inputControls = collectInputControls(reportUnit, context);
		JRReport jReport = getReport(reportUnit);
		for (int i = 0; i < beanValues.length; i++) {
			ReportParameterValueBean beanParam = beanValues[i];
			String name = (String) beanParam.getName();
            Object[] beanValue = beanParam.getValues();
			if (beanValue == null) {
				InputControl control = inputControls.get(name);
				if (control != null && isMulti(control)) {
					beanValue = new Object[]{};
				} else {
					beanValue = new Object[]{null};
				}
			}
            Object value = toParameterValue(reportUnit, inputControls, jReport, beanParam, beanValue);
			values.put(name, value);
		}
	}

	public Map<String, Object> getParameterValues(ReportUnit reportUnit,
			ReportParameterValueBean[] beanValues, ExecutionContext context) {
		Map<String, Object> values;
		if (beanValues == null || beanValues.length == 0) {
			values = null;
		} else {
			values = new HashMap<String, Object>();
			populateParameterValues(reportUnit, beanValues, values, context);
		}
		return values;
	}

	protected JRReport getReport(ReportUnit reportUnit) {
		ResourceReference mainReportRef = reportUnit.getMainReport();
		InputStream jrxmlStream = null;
		if (mainReportRef.isLocal()) {
			FileResource mainReportRes = (FileResource) mainReportRef.getLocalResource();
			if (mainReportRes.hasData()) {
				jrxmlStream = new ByteArrayInputStream(mainReportRes.getData());
			}
		}
		
		if (jrxmlStream == null) {
			String jrxmlUri = mainReportRef.getTargetURI();
			FileResourceData jrxmlData = getRepository().getResourceData(null, jrxmlUri);
			jrxmlStream = jrxmlData.getDataStream();
		}

		try {
			JasperDesign design = CustomJRXmlLoader.load(jrxmlStream);
			return design;
		} catch (JRException e) {
			throw new JSExceptionWrapper(e);
		} finally {
			try {
				jrxmlStream.close();
			} catch (IOException e) {
				log.warn("Error closing resource data stream", e);
			}
		}
	}

    protected Map<String, InputControl> collectInputControls(ReportUnit reportUnit, ExecutionContext context) {
        Map<String, InputControl> controls = new HashMap<String, InputControl>();
        final List<InputControl> inputControls = reportLoadingService.getInputControls(context, reportUnit);
        if (inputControls != null) {
            for (InputControl inputControl : inputControls) {
                controls.put(inputControl.getName(), inputControl);
            }
        }

        return controls;
    }

	protected Object toParameterValue(ReportUnit reportUnit, Map inputControls,
			JRReport jReport, ReportParameterValueBean beanParam, Object[] beanValue) {
		String name = (String) beanParam.getName();
		Object value;
		InputControl control = (InputControl) inputControls.get(name);
		if (control == null) {
			JRParameter parameter = getParameter(jReport, name);
			if (parameter == null) {
				if (beanParam.isSingleValue()) {
					value = toSingleValue(reportUnit, name, beanValue);
				} else {
					value = beanValue;
				}
			} else if (parameter.getValueClass().isArray()) {
				value = toArrayValue(parameter.getValueClass(), beanValue);
			} else if (Collection.class.isAssignableFrom(parameter
					.getValueClass())) {
				value = toCollectionValue(parameter.getValueClass(), beanValue);
			} else if (Object.class.equals(parameter.getValueClass())) {
				if (beanValue == null || beanValue.length == 0) {
					value = null;
				} else if (beanValue.length == 1) {
					value = beanValue[0];
				} else {
					value = beanValue;
				}
			} else {
				value = toSingleValue(reportUnit, name, beanValue);
			}
		} else if (isMulti(control)) {
            value = toCollectionValue(Collection.class, beanValue);
		} else {
			value = toSingleValue(reportUnit, name, beanValue);
		}
		return value;
	}

	protected boolean isMulti(InputControl control) {
		byte type = control.getInputControlType();
		return type == InputControl.TYPE_MULTI_SELECT_LIST_OF_VALUES
				|| type == InputControl.TYPE_MULTI_SELECT_LIST_OF_VALUES_CHECKBOX
				|| type == InputControl.TYPE_MULTI_SELECT_QUERY
				|| type == InputControl.TYPE_MULTI_SELECT_QUERY_CHECKBOX
				|| type == InputControl.TYPE_MULTI_VALUE;
	}

	protected JRParameter getParameter(JRReport report, String name) {
		JRParameter parameter = null;
		JRParameter[] parameters = report.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			if (name.equals(parameters[i].getName())) {
				parameter = parameters[i];
				break;
			}
		}
		return parameter;
	}

	protected Object toSingleValue(ReportUnit reportUnit, String name,
			Object[] beanValue) {
		Object value;
		if (beanValue == null || beanValue.length == 0) {
			value = null;
		} else if (beanValue.length == 1) {
			value = toValue(beanValue[0]);
		} else {
			throw new JSException(
					"jsexception.import.multiple.values.for.single.parameter",
					new Object[] { name, reportUnit.getURIString() });
		}
		return value;
	}

	protected Object toArrayValue(Class valueClass, Object[] beanValue) {
		Class componentType = valueClass.getComponentType();
		Object value;
		if (beanValue == null) {
			value = Array.newInstance(componentType, 0);
		} else {
			value = Array.newInstance(componentType, beanValue.length);
			for (int i = 0; i < beanValue.length; i++) {
				Array.set(value, i, toValue(beanValue[i]));
			}
		}
		return value;
	}

	protected Object toCollectionValue(Class valueClass, Object[] beanValue) {
		Collection value;
		if (valueClass.equals(Object.class)
				|| valueClass.equals(Collection.class)
				|| valueClass.equals(Set.class)) {
			value = new ListOrderedSet();
			if (beanValue != null) {
				for (int i = 0; i < beanValue.length; i++) {
					value.add(toValue(beanValue[i]));
				}
			}
		} else if (valueClass.equals(List.class)) {
			if (beanValue == null) {
				value = new ArrayList(0);
			} else {
				value = new ArrayList(beanValue.length);
				for (int i = 0; i < beanValue.length; i++) {
					value.add(toValue(beanValue[i]));
				}
			}
		} else {
			throw new JSException(
					"jsexception.unknown.parameter.type.for.multiple.value.input",
					new Object[] { valueClass.getName() });
		}
		return value;
	}

    protected Object toValue(Object beanValue) {
        //Null substitution
        if (DefaultReportParametersTranslator.NULL_SUBSTITUTE.equals(beanValue) || beanValue == null) {
            return null;
        }

        if (beanValue instanceof DateRangeDTO) {
            return ((DateRangeDTO)beanValue).toDateRange();
        }
        
        if (beanValue instanceof MaterializedDataParameterBean) {
        		MaterializedDataParameterBean param = (MaterializedDataParameterBean) beanValue;
        		return new MaterializedDataParameter(toValue(param.getParameterValue()), toValue(param.getEffectiveValue()));
        }
        
        if (beanValue instanceof PairBean) {
        		PairBean pair = (PairBean) beanValue;
        		return new Pair<Object, Object>(toValue(pair.getFirst()), toValue(pair.getSecond()));
        }
        
        if (beanValue instanceof AnyNode) {
        		Object dateValue = parseDateNode((AnyNode) beanValue);
        		return dateValue == null ? beanValue : dateValue;
        }

        return beanValue;
    }

	public RepositoryService getRepository() {
		return repository;
	}

	public void setRepository(RepositoryService repository) {
		this.repository = repository;
	}

	protected static Object parseDateNode(AnyNode node) {
		Object dateValue = null;
		String type = getNodeAttributeValue(node, "type");
		if ("date".equals(type)) {
			String strValue = node.getStringValue();
			if (strValue != null) {
				try {
					DateTime dateTime = new DateTime(strValue);
					dateValue = dateTime.toDate();
				} catch (ParseException e) {
					throw new JSExceptionWrapper(e);
				}
			}
		} else if ("sql-time".equals(type)) {
			String strValue = node.getStringValue();
			if (strValue != null) {
				try {
					Time time = Time.valueOf(strValue);
					dateValue = time;
				} catch (IllegalArgumentException e) {
					throw new JSExceptionWrapper(e);
				}
			}
        } else if ("sql-timestamp".equals(type)) {
            String strValue = node.getStringValue();
            if (strValue != null) {
            		//perhaps we could reuse the handler, for now creating a fresh one each time as it doesn't seem expensive
            		SQLTimestampFieldHandler timestampHandler = new SQLTimestampFieldHandler();
            		dateValue = timestampHandler.convertUponSet(strValue);
            }
		} else if ("sql-date".equals(type)) {
			String strValue = node.getStringValue();
			if (strValue != null) {
				try {
					DateTime dateTime = new DateTime(strValue);
					dateValue = new Date(dateTime.toLong());
				} catch (ParseException e) {
					throw new JSExceptionWrapper(e);
				}
			}
		}
		return dateValue;
	}
	
	private static String getNodeAttributeValue(AnyNode node, String attributeName) {
		String val = null;
		for (AnyNode attr = node.getFirstAttribute();
			attr != null;
			attr = attr.getNextSibling()) {
			if (attributeName.equals(attr.getLocalName())) {
				val = attr.getStringValue();
				break;
			}
		}
		return val;
	}
	
}
