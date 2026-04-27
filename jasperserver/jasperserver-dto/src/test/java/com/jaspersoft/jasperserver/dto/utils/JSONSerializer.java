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

package com.jaspersoft.jasperserver.dto.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Olexandr Dahno <odahno@tibco.com>
 */

public class JSONSerializer {

    private ObjectMapper mapper;

    public JSONSerializer() {
        mapper = new ObjectMapper();
        configure();
    }

    /*
     * Public API
     */

    public <T> JsonNode contentFromResourceAtPath(String path, Class<T> expectedClass) {
        String stringRepresentation = representationFromResourceAtPath(expectedClass, path);
        return deserializeJson(stringRepresentation, expectedClass);
    }

    public <T> JsonNode deserializeJson(String json, Class<T> expectedClass) {
        JsonNode node = null;
        try {
            node = mapper.readTree(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return node;
    }

    public <T> JsonNode contentFromInstance(T instance) {
        return mapper.valueToTree(instance);
    }

    public <T>T deserializeFromResourceAtPath(String path, Class<T> expectedClass) {
        String stringRepresentation = representationFromResourceAtPath(expectedClass, path);
        T instance = null;
        try {
            instance = mapper.readValue(stringRepresentation, expectedClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return instance;
    }

    /*
     * Helpers
     */

    private void configure() {
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
        mapper.configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true);
        // ignore fields with null values
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private String representationFromResourceAtPath(Class c, String resourcePath) {
        String resource;
        try {
            resource = new String(Files.readAllBytes(Paths.get(c.getClassLoader().getResource(resourcePath).toURI())), Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        } catch (URISyntaxException e){
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        } catch( Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
        return resource.replace("\r\n", "\n").replace("\r", "\n");
    }

}
