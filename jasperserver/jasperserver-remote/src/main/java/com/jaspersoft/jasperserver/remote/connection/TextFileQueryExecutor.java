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
package com.jaspersoft.jasperserver.remote.connection;

import com.jaspersoft.jasperserver.api.JSNotImplementedException;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.common.error.handling.SecureExceptionHandler;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.CustomReportDataSourceServiceFactory;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.DataAdapterDefinition;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomReportDataSource;
import com.jaspersoft.jasperserver.dto.connection.query.TextFileCastConversionRule;
import com.jaspersoft.jasperserver.dto.connection.query.TextFileQuery;
import com.jaspersoft.jasperserver.dto.resources.ClientCustomDataSource;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.exception.MandatoryParameterNotFoundException;
import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.remote.resources.converters.CustomDataSourceResourceConverter;
import com.jaspersoft.jasperserver.remote.resources.converters.ToServerConversionOptions;
import com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.converters.DataConverterService;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.data.JRCsvDataSource;
import net.sf.jasperreports.engine.design.JRDesignField;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@Service
public class TextFileQueryExecutor implements ContextQueryExecutor<TextFileQuery, ClientCustomDataSource> {
    @Resource(name = "customDataSourceServiceFactory")
    private CustomReportDataSourceServiceFactory customDataSourceFactory;
    @Resource
    private CustomDataSourceResourceConverter customDataSourceResourceConverter;
    @Resource
    private DataConverterService dataConverterService;
    @Resource
    private SecureExceptionHandler secureExceptionHandler;

    @Override
    public Object executeQuery(final TextFileQuery query, ClientCustomDataSource connection,
            Map<String, String[]> queryParameters, Map<String, Object> data) {
        if(query == null || connection == null){
            return null;
        }
        final List<List<String>> resultSet = new ArrayList<List<String>>();
        final CustomReportDataSource customReportDataSource = customDataSourceResourceConverter.toServer(ExecutionContextImpl.getRuntimeExecutionContext(),
                connection, ToServerConversionOptions.getDefault().setSuppressValidation(true));
        final DataAdapterDefinition definition = (DataAdapterDefinition) customDataSourceFactory.getDefinition(customReportDataSource);
        try {
            final JRCsvDataSource jrDataSource = (JRCsvDataSource) definition.getJRDataSource(customReportDataSource);
            final Map<String, String> columnTypes = new HashMap<String, String>(){{
                if(query.getConvert() != null && query.getConvert().getRules() != null){
                    final List<TextFileCastConversionRule> rules = query.getConvert().getRules();
                    for(int i = 0; i < rules.size(); i++){
                        TextFileCastConversionRule rule = rules.get(i);
                        if(rule.getColumn() == null) throw new MandatoryParameterNotFoundException("query.convert.rules[" + i + "].column");
                        if(rule.getType() == null) throw new MandatoryParameterNotFoundException("query.convert.rules[" + i + "].type");
                        put(rule.getColumn(), rule.getType());
                    }
                }
            }};
            boolean hasNext = true;
            if(query.getOffset() != null) for(int i = 0; hasNext && i < query.getOffset(); i++) hasNext = jrDataSource.next();
            int maxCount = query.getLimit() != null ? query.getLimit() : Integer.MAX_VALUE;
            for(int i = 0; i < maxCount; i++){
                if(jrDataSource.next()){
                    final List<String> columns = query.getSelect() != null ? query.getSelect().getColumns() : null;
                    if(columns != null){
                        final List<String> row = new ArrayList<String>(columns.size());
                        for(String column : columns){
                            JRDesignField field = new JRDesignField();
                            field.setName(column);
                            final String valueClassName = columnTypes.get(column);
                            if(valueClassName != null) {
                                field.setValueClassName(valueClassName);
                            }
                            try {
                                final Object value = jrDataSource.getFieldValue(field);
                                row.add(dataConverterService.formatSingleValue(value));
                            } catch (JRRuntimeException e) {
                                if(e.getCause() instanceof ClassNotFoundException){
                                    throw new IllegalParameterValueException("query.convert.rules[" + column + "].type", valueClassName);
                                } else throw e;
                            } catch (JRException e){
                                if("data.csv.field.value.not.retrieved".equals(e.getMessageKey())){
                                    throw new IllegalParameterValueException("query.convert.rules[" + column + "].type", valueClassName);
                                } else throw e;
                            }
                        }
                        resultSet.add(row);
                    }
                } else {
                    break;
                }
            }
        } catch (ErrorDescriptorException e){
            throw e;
        } catch (Exception e) {
            throw new ErrorDescriptorException(e, secureExceptionHandler);
        }
        return resultSet;
    }

    @Override
    public Object executeQueryForMetadata(TextFileQuery query, ClientCustomDataSource connection, Map<String, Object> data) {
        throw new JSNotImplementedException("executeQueryForMetadata() isn't implemented for TextFileQueryExecutor");
    }
}

