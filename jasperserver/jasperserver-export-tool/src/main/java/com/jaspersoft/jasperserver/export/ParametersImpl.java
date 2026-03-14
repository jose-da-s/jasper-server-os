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

package com.jaspersoft.jasperserver.export;

import com.jaspersoft.jasperserver.api.JSException;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class ParametersImpl implements Parameters {

	private final Map params;
	
	public ParametersImpl() {
		this.params = new LinkedHashMap();
	}
	
	public Iterator getParameterNames() {
		return params.keySet().iterator();
	}

	public boolean hasParameter(String parameterName) {
		return params.containsKey(parameterName);
	}

	public String getParameterValue(String parameterName) {
		String value;
		Object paramValue = params.get(parameterName);
		if (paramValue == null) {
			value = null;
		} else if (paramValue instanceof List) {
			List valuesList = (List) paramValue;
			int valuesCount = valuesList.size();
			if (valuesCount == 0) {
				value = null;
			} else if (valuesCount == 1) {
				value = (String) valuesList.get(0);
			} else {
				throw new JSException("jsexception.parameter.has.multiple.values", new Object[] {parameterName});
			}
		} else {
			value = (String) paramValue;
		}
		return value;
	}

	public String[] getParameterValues(String parameterName) {
		String[] values;
		Object value = params.get(parameterName);
		if (value == null) {
			values = null;
		} else if (value instanceof List) {
			List valuesList = (List) value;
			values = new String[valuesList.size()];
			values = (String[]) valuesList.toArray(values);
		} else {
			values = new String[]{(String) value};
		}
		return values;
	}
	
	public Parameters addParameter(String parameterName) {
		if (!params.containsKey(parameterName)) {
			params.put(parameterName, null);
		}
		return this;
	}
	
	public Parameters addParameterValue(String parameterName, String parameterValue) {
		Object value = params.get(parameterName);
		if (value == null) {
			params.put(parameterName, parameterValue);
		} else if (value instanceof List) {
			((List) value).add(parameterValue);
		} else {
			List values = new ArrayList();
			values.add(value);
			values.add(parameterValue);
			params.put(parameterName, values);
		}
		return this;
	}

	public Parameters addParameterValues(String parameterName, String[] parameterValues) {
		Object value = params.get(parameterName);
		if (value == null || !(value instanceof List)) {
			List values = new ArrayList();
			if (value != null) {
				values.add(value);
			}			
			for (int i = 0; i < parameterValues.length; i++) {
				values.add(parameterValues[i]);
			}
			params.put(parameterName, values);
		} else {
			List values = (List) value;
			for (int i = 0; i < parameterValues.length; i++) {
				values.add(parameterValues[i]);
			}
		}
		return this;
	}

	@Override
	public Parameters setParameterValue(String parameterName, String parameterValue) {
		Assert.notNull(parameterName, "Cannot set parameter value: parameter name must not be null");
		params.put(parameterName, parameterValue);

		return this;
	}
}
