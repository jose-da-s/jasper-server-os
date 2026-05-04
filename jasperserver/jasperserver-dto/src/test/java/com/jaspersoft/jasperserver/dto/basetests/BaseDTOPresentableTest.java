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

package com.jaspersoft.jasperserver.dto.basetests;

import com.fasterxml.jackson.databind.JsonNode;
import com.jaspersoft.jasperserver.dto.common.DeepCloneable;
import com.jaspersoft.jasperserver.dto.utils.JSONSerializer;
import com.jaspersoft.jasperserver.dto.utils.XMLSerializer;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import jakarta.xml.bind.JAXBElement;

import static com.jaspersoft.jasperserver.dto.utils.CustomAssertions.assertNodesEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Andriy Tivodar <ativodar@tibco>
 * @author Olexandr Dahno <odahno@tibco.com>
 */


public abstract class BaseDTOPresentableTest<T extends DeepCloneable> extends BaseDTOTest<T> {

    private final JSONSerializer jsonSerializer = new JSONSerializer();
    private final XMLSerializer xmlSerializer = new XMLSerializer();

    /*
     * JSON
     */

    @Test
    public void serializationInstanceWithDefaultParametersInJSON() {
        JsonNode expected = jsonSerializer.contentFromInstance(testInstanceWithDefaultParameters);
        JsonNode actual = jsonSerializer.contentFromResourceAtPath(
                pathForEmptyJSON(dtoClass()),
                dtoClass()
        );
        assertNodesEquals(expected, actual);
    }

    @Test
    public void serializationFullyConfiguredInstanceInJSON() {
        JsonNode expected = jsonSerializer.contentFromInstance(fullyConfiguredTestInstance);
        JsonNode actual = jsonSerializer.contentFromResourceAtPath(
                pathForJSON(dtoClass()),
                dtoClass()
        );
        assertNodesEquals(expected, actual);
    }

    @Test
    public void deserializationInstanceWithDefaultParametersInJSON() {
        T actual = jsonSerializer.deserializeFromResourceAtPath(
                pathForEmptyJSON(dtoClass()),
                dtoClass()
        );
        assertEquals(testInstanceWithDefaultParameters, actual);
    }

    @Test
    public void deserializationFullyConfiguredInstanceInJSON() {
         T actual = jsonSerializer.deserializeFromResourceAtPath(
                pathForJSON(dtoClass()),
                dtoClass()
        );
        assertEquals(fullyConfiguredTestInstance, actual);
    }

    /*
     * XML
     */

    @Test
    public void serializationInstanceWithDefaultParametersInXML() {
        JAXBElement<T> expected = xmlSerializer.contentFromInstance(testInstanceWithDefaultParameters);
        JAXBElement<T> actual = xmlSerializer.contentFromResourceAtPath(
                pathForEmptyXML(dtoClass()),
                dtoClass()
        );

        assertEquals(expected.getValue(), actual.getValue());
    }

    @Test
    public void serializationFullyConfiguredInstanceInXML() {
        JAXBElement<T> expected = xmlSerializer.contentFromInstance(fullyConfiguredTestInstance);
        JAXBElement<T> actual = xmlSerializer.contentFromResourceAtPath(
                pathForXML(dtoClass()),
                dtoClass()
        );

        assertEquals(expected.getValue(), actual.getValue());
    }

    /*
     * Helpers
     */

    public T deserializeJSONAtPath(String path) {
        return jsonSerializer.deserializeFromResourceAtPath(
                path,
                dtoClass()
        );
    }

    private Class<T> dtoClass() {
        Class<T> result = (Class<T>) fullyConfiguredTestInstance.getClass();
        return result; 
   }

    private String pathForJSON(Class c) {
        return pathForResourceFromClass(c) + ".json";
    }

    private String pathForXML(Class c) {
        return pathForResourceFromClass(c) + ".xml";
    }

    private String pathForEmptyXML(Class c) {
        return pathForResourceFromClass(c) + "_empty.xml";
    }

    private String pathForEmptyJSON(Class c) {
        return pathForResourceFromClass(c) + "_empty.json";
    }

    private String pathForResourceFromClass(Class c) {
        return "fixtures/" + c.getName().replace(".", "/");
    }
}
