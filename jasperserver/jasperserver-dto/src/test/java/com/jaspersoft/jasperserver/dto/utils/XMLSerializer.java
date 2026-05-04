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

import org.apache.commons.io.IOUtils;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Olexandr Dahno <odahno@tibco.com>
 */

public class XMLSerializer {

    /*
     * Public API
     */
    public <T> JAXBElement<T> contentFromResourceAtPath(String path, Class<T> expectedClass) {

        String stringRepresentation = representationFromResourceAtPath(expectedClass, path);

        JAXBElement<T> element = null;
        try {
            XMLInputFactory f = XMLInputFactory.newInstance();
            XMLStreamReader reader = f.createXMLStreamReader(IOUtils.toInputStream(stringRepresentation));
            element = getUnmarshaller(expectedClass).unmarshal(reader, expectedClass);
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return element;
    }

    public <T> JAXBElement<T> contentFromInstance(T instance) {
        JAXBElement<T> element =  new JAXBElement(
                new QName(instance.getClass().getSimpleName()),
                instance.getClass(),
                instance
        );
        return element;
    }

    /*
     * Helpers
     */

    private Unmarshaller getUnmarshaller(Class... docClass) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(docClass);
        return jc.createUnmarshaller();
    }

    private Marshaller getMarshaller(Class... docClass) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(docClass);
        Marshaller m = context.createMarshaller();
        m.setProperty(jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.setProperty(jakarta.xml.bind.Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        return m;
    }

    private String representationFromResourceAtPath(Class c, String resourcePath) {
        String resource;
        try {
            resource = new String(Files.readAllBytes(Paths.get(c.getClassLoader().getResource(resourcePath).toURI())), Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } catch (URISyntaxException e){
            throw new IllegalArgumentException(e);
        }
        return resource.replace("\r\n", "\n").replace("\r", "\n");
    }

}
