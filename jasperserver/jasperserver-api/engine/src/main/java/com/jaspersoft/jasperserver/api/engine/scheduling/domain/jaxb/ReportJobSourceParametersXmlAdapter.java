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
package com.jaspersoft.jasperserver.api.engine.scheduling.domain.jaxb;

import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobSource;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.types.date.RelativeDateRange;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
public class ReportJobSourceParametersXmlAdapter extends XmlAdapter<ReportParametersMapWrapper, Map<String, Object>> {
    @Override
    public ReportParametersMapWrapper marshal(Map<String, Object> v) throws Exception {
        HashMap<String, Object> result = null;
        if (v != null && !v.isEmpty()) {
            result = new HashMap<String, Object>();
            for (String currentKey : v.keySet()) {
                Object currentValue = v.get(currentKey);
                if (JRParameter.REPORT_TIME_ZONE.equals(currentKey) && currentValue instanceof TimeZone) {
                    currentValue = ((TimeZone) currentValue).getID();
                }else if (ReportJobSource.REFERENCE_WIDTH_PARAMETER_NAME.equals(currentKey) || ReportJobSource.REFERENCE_HEIGHT_PARAMETER_NAME.equals(currentKey)){
                    continue;
                } else if(currentValue instanceof Collection){
                    final ValuesCollection collectionWrapper = new ValuesCollection();
                    collectionWrapper.setCollection((Collection<Object>) currentValue);
                    currentValue = collectionWrapper;
                } else if(currentValue instanceof RelativeDateRange){
                    currentValue = new RelativeDateRangeWrapper((RelativeDateRange)currentValue);
                }
                result.put(currentKey, currentValue);
            }
        }
        return result != null ? new ReportParametersMapWrapper(result) : null;
    }

    @Override
    public Map<String, Object> unmarshal(ReportParametersMapWrapper v) throws Exception {
        Map<String, Object> result = null;
        if (v != null) {
            final HashMap<String, Object> parameterValues = v.getParameterValues();
            if (parameterValues != null && !parameterValues.isEmpty()) {
                result = new HashMap<String, Object>();
                for (String currentKey : parameterValues.keySet()) {
                    Object currentValue = parameterValues.get(currentKey);
                    if (JRParameter.REPORT_TIME_ZONE.equals(currentKey) && currentValue instanceof String){
                        currentValue = TimeZone.getTimeZone((String) currentValue);
                    }else if (ReportJobSource.REFERENCE_WIDTH_PARAMETER_NAME.equals(currentKey) || ReportJobSource.REFERENCE_HEIGHT_PARAMETER_NAME.equals(currentKey)){
                        continue;
                    }else if(currentValue instanceof ValuesCollection){
                        currentValue = ((ValuesCollection)currentValue).getCollection();
                    } else if(currentValue instanceof XMLGregorianCalendar){
                        currentValue = ((XMLGregorianCalendar)currentValue).toGregorianCalendar().getTime();
                    } else if(currentValue instanceof RelativeDateRangeWrapper){
                        currentValue = ((RelativeDateRangeWrapper) currentValue).getRelativeDateRange();
                    } else if(currentValue instanceof Map){
                        Map map = (Map) currentValue;
                        Object expression = map.get("expression");
                        if(expression instanceof String && map.size() == 1){
                            // the only string entry named "expression" means,
                            // that it's JSON unmarshalling of RelativeDateRangeWrapper
                            currentValue = new RelativeDateRange((String)expression);
                        }
                    }
                    result.put(currentKey, currentValue);
                }
            }
        }
        return result;
    }
}
