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
package com.jaspersoft.jasperserver.dto.executions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;
import com.jaspersoft.jasperserver.dto.adhoc.query.ClientMultiAxisQuery;
import com.jaspersoft.jasperserver.dto.adhoc.query.ClientMultiLevelQuery;
import com.jaspersoft.jasperserver.dto.resources.ClientReference;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Volodya Sabadosh
 * @version $Id: $
 * @since 10.07.2017
 */
public class QueryExecutionListWrapperSerializationToJsonTest {
    private static ObjectMapper mapper;
    private static final String ALL_EXECUTION_TYPE_LIST = "executions/allExecutionTypeList.json";
    @BeforeClass
    public static void init() {
        mapper = createObjectMapper();
    }

    @Test
    public void executionsToJson() throws Exception {
        ClientExecutionListWrapper expectedExecutionWrapper
                = getObjectFromJson(ALL_EXECUTION_TYPE_LIST, ClientExecutionListWrapper.class);

        List<AbstractClientExecution> executions = new ArrayList<AbstractClientExecution>();

        ClientProvidedQueryExecution providedQueryExecution = new ClientProvidedQueryExecution();
        providedQueryExecution.setDataSource(new ClientReference("testDsUri"));
        providedQueryExecution.setParams(new ClientQueryParams().setOffset(new int[]{0}).setPageSize(new int[]{10}));

        ClientMultiLevelQueryExecution multiLevelQueryExecution = new ClientMultiLevelQueryExecution();
        multiLevelQueryExecution.setDataSource(new ClientReference("testDsUri"));
        multiLevelQueryExecution.setParams(new ClientQueryParams().setOffset(new int[]{0}).setPageSize(new int[]{10}));
        multiLevelQueryExecution.setQuery(new ClientMultiLevelQuery());

        ClientMultiAxisQueryExecution multiAxisQueryExecution = new ClientMultiAxisQueryExecution();
        multiAxisQueryExecution.setDataSource(new ClientReference("testDsUri"));
        multiAxisQueryExecution.setParams(new ClientQueryParams().setOffset(new int[]{0}).setPageSize(new int[]{10}));
        multiAxisQueryExecution.setQuery(new ClientMultiAxisQuery());

        executions.add(providedQueryExecution);
        executions.add(multiLevelQueryExecution);
        executions.add(multiAxisQueryExecution);

        ClientExecutionListWrapper executionListWrapper = new ClientExecutionListWrapper().setExecutions(executions);

        assertThat(executionListWrapper, equalTo(expectedExecutionWrapper));
    }

    public static ObjectMapper createObjectMapper() {
        mapper = new ObjectMapper();
        AnnotationIntrospector primary = new JakartaXmlBindAnnotationIntrospector();
        AnnotationIntrospector secondary = new JacksonAnnotationIntrospector();
        AnnotationIntrospector pair = AnnotationIntrospector.pair(primary, secondary);
        mapper.setAnnotationIntrospector(pair);
        // Serialize dates using ISO8601 format
        // Jackson uses timestamps by default, so use StdDateFormat to get ISO8601
        mapper.setDateFormat(new StdDateFormat());
        // Prevent exceptions from being thrown for unknown properties
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // Use XML wrapper name as JSON property name
        mapper.configure(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME, true);
        // ignore fields with null values
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    private <T> T getObjectFromJson(String path, Class<T> aClass) throws IOException {
        InputStream inputStream = QueryExecutionListWrapperSerializationToJsonTest.class.getClassLoader().getResourceAsStream(path);
        return mapper.readValue(inputStream, aClass);
    }
}
