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
package com.jaspersoft.jasperserver.dto.common;

import org.junit.Test;
import org.junit.Assert;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import static org.junit.Assert.assertEquals;

/**
 * @author serhii.blazhyievskyi
 * @version $Id$
 */
public class ClientTypeUtilityTest {
    public static final String NAME_FROM_XML_ROOT_ELEMENT_ANNOTATION = "nameFromXmlRootElementAnnotation";
    public static final String NAME_FROM_XML_TYPE_ANNOTATION = "nameFromXmlTypeAnnotation";

    @Test
    public void extractClientType_XmlRootElementAnnotation() {
        assertEquals(NAME_FROM_XML_ROOT_ELEMENT_ANNOTATION, ClientTypeUtility.extractClientType(TestClientObjectWithXmlRootAnnotation.class));
    }

    @Test
    public void extractClientType_XmlTypeAnnotation() {
        assertEquals(NAME_FROM_XML_TYPE_ANNOTATION, ClientTypeUtility.extractClientType(TestClientObjectWithXmlTypeAnnotation.class));
    }

    @Test
    public void extractClientType_NoAnnotation() {
        assertEquals("testClientObjectWithoutAnnotation", ClientTypeUtility.extractClientType(TestClientObjectWithoutAnnotation.class));
    }

    @XmlRootElement(name = NAME_FROM_XML_ROOT_ELEMENT_ANNOTATION)
    private static class TestClientObjectWithXmlRootAnnotation {
    }

    @XmlType(name = NAME_FROM_XML_TYPE_ANNOTATION)
    private static class TestClientObjectWithXmlTypeAnnotation {
    }

    private static class TestClientObjectWithoutAnnotation {
    }

}