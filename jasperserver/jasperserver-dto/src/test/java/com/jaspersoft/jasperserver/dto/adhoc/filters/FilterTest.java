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

package com.jaspersoft.jasperserver.dto.adhoc.filters;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.jaspersoft.jasperserver.dto.adhoc.query.ClientMultiLevelQuery;
import com.jaspersoft.jasperserver.dto.adhoc.query.ClientQuery;
import com.jaspersoft.jasperserver.dto.adhoc.query.ClientWhere;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientExpression;
import org.apache.commons.io.IOUtils;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 *
 * @author Grant Bacon <gbacon@tibco.com>
 * @author Stas Chubar
 * @version $Id$
 */
public class FilterTest {

    protected static final ObjectMapper MAPPER = newObjectMapper();

    public static ObjectMapper newObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();

        return configure(mapper);
    }

    public static Marshaller getMarshaller(Class... docClass) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(docClass);
        Marshaller m = context.createMarshaller();
        m.setProperty(jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.setProperty(jakarta.xml.bind.Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        return m;
    }

    public static Unmarshaller getUnmarshaller(Class... docClass) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(docClass);
        Unmarshaller u = jc.createUnmarshaller();
        return u;
    }

    private static ObjectMapper configure(ObjectMapper mapper) {

        JaxbAnnotationModule jaxbModule = new JaxbAnnotationModule();
        mapper.registerModule(jaxbModule);
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        mapper.configure(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME, true);
        mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);

        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return mapper;
    }

    private static String fixture(String filename, Charset charset) {
        try {
            return new String(Files.readAllBytes(Paths.get(FilterTest.class.getClassLoader().getResource(filename).toURI())), charset);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String fixture(String filename) {
        return fixture("fixtures/" + filename, Charset.forName("UTF-8")).replace("\r\n", "\n").replace("\r", "\n");
    }

    protected String jsonFixture(String name) throws java.io.IOException {
        return json(MAPPER.readValue(fixture(name), Map.class));
    }

    protected String json(Object value) throws JsonProcessingException {
        return MAPPER.writeValueAsString(value).replace("\r\n", "\n").replace("\r", "\n");
    }

    protected JsonNode toJsonObject(Object value) throws JsonProcessingException {
        return MAPPER.valueToTree(value);
    }

    protected JsonNode toJsonObject(String value) throws IOException {
        return MAPPER.readTree(value);
    }

    protected String xml(Object value) throws Exception {
        StringWriter w = new StringWriter();
        getMarshaller(ClientWhere.class).marshal(value, w);
        return w.toString().replace("\r\n", "\n").replace("\r", "\n");
    }

    // jaxb exception because xml versions can throw them
    protected <T> T dtoFromJSONString(String jsonString, Class<T> expectedType) throws java.io.IOException, JAXBException {
        return MAPPER.readValue(jsonString, expectedType);
    }

    protected <T> T dtoFromXMLString(String xml, Class<T> expectedType) throws java.io.IOException, JAXBException {
        return (T) getUnmarshaller(expectedType).unmarshal(IOUtils.toInputStream(xml));
    }

    protected <T> T dto(String xml) throws java.io.IOException, JAXBException {
        return (T) getUnmarshaller(ClientWhere.class).unmarshal(IOUtils.toInputStream(xml));
    }

    protected ClientQuery select(ClientExpression filter) {
        ClientWhere where = new ClientWhere(filter);
        return new ClientMultiLevelQuery().setWhere(where);
    }

    protected ClientQuery select(String filter) {
        ClientWhere where = new ClientWhere(filter);
        return new ClientMultiLevelQuery().setWhere(where);
    }

}