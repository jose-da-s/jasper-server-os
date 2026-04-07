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

package com.jaspersoft.jasperserver.dto.serverinfo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;
import org.apache.commons.io.IOUtils;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringWriter;

/**
 * @author Grant Bacon <gbacon@tibco.com>
 * @version $Id$
 * @date 5/6/16 12:02 p.m.
 */
public class ServerInfoTest {


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

    	JakartaXmlBindAnnotationModule jaxbModule = new JakartaXmlBindAnnotationModule();
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


    protected String xml(Object value) throws Exception {
        StringWriter w = new StringWriter();
        getMarshaller(ServerInfo.class).marshal(value, w);
        return w.toString().replace("\r\n", "\n").replace("\r", "\n");
    }

    protected <T> T dto(String xml) throws IOException, JAXBException {
        return (T) getUnmarshaller(ServerInfo.class).unmarshal(IOUtils.toInputStream(xml));
    }

    protected <T> T dtoForEntity(String xml, Class<T> entity) throws IOException, JAXBException {
        return (T) getUnmarshaller(entity).unmarshal(IOUtils.toInputStream(xml));
    }


}
