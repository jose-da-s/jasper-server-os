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

package com.jaspersoft.jasperserver.dto.adhoc.query;

import com.jaspersoft.jasperserver.dto.adhoc.filters.FilterTest;
import com.jaspersoft.jasperserver.dto.executions.ClientMultiAxisQueryExecution;
import com.jaspersoft.jasperserver.dto.executions.ClientMultiLevelQueryExecution;
import org.apache.commons.io.IOUtils;

import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.io.StringWriter;

/**
 * @author Grant Bacon <gbacon@tibco.com>
 * @date 2/2/16 8:16AM
 */
public class QueryExecutionRequestTest extends FilterTest {

    protected static final String DATASOURCE_URI = "/public/Samples/Ad_Hoc_Views/04__Product_Results_by_Store_Type";

    @Override
    protected String xml(Object value) throws Exception {
        StringWriter w = new StringWriter();
        getMarshaller(value.getClass()).marshal(value, w);
        return w.toString();
    }

    protected <T> String xmlForEntity(Object value, Class<T> entity) throws JAXBException {
        StringWriter w = new StringWriter();
        getMarshaller(entity).marshal(value, w);
        return w.toString();
    }

    @Override
    @Deprecated
    protected <T> T dto(String xml) throws IOException, JAXBException {
        return (T) getUnmarshaller(ClientMultiLevelQueryExecution.class, ClientMultiAxisQueryExecution.class).unmarshal(IOUtils.toInputStream(xml));
    }

    protected <T> T dtoForEntity(String xml, Class<T> entity) throws IOException, JAXBException {
        return (T) getUnmarshaller(entity).unmarshal(IOUtils.toInputStream(xml));
    }

    protected ClientMultiLevelQueryExecution request(ClientMultiLevelQuery cq) {
        return new ClientMultiLevelQueryExecution(cq, DATASOURCE_URI);
    }

    protected ClientMultiAxisQueryExecution request(ClientMultiAxisQuery cq) {
        return new ClientMultiAxisQueryExecution(cq, DATASOURCE_URI);
    }


}