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
package com.jaspersoft.jasperserver.api.engine.jasperreports.util;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Loads JRXML supporting both legacy (6.x) and modern formats.
 * Legacy files are converted via {@link JrxmlV6ToV7Converter} before delegating to {@link JRXmlLoader}.
 */
public class CustomJRXmlLoader {

    private CustomJRXmlLoader() {
        /* This utility class should not be instantiated */
    }


    private static final Log log = LogFactory.getLog(CustomJRXmlLoader.class);

    private static final String LEGACY_NAMESPACE = "http://jasperreports.sourceforge.net/jasperreports";
    private static final String LEGACY_ROOT_ELEMENT = "jasperReport";

    /** Load a JRXML report design, auto-detecting legacy vs. modern format. */
    public static JasperDesign load(InputStream is) throws JRException {
        byte[] data = readAll(is);
        if (detectLegacyJRXML(data)) {
            log.debug("Detected legacy JRXML; converting v6 -> v7");
            try {
                data = JrxmlV6ToV7Converter.convert(data);
            } catch (Exception e) {
                throw new JRException("Failed to convert legacy JRXML v6 to v7", e);
            }
        }
        return JRXmlLoader.load(new ByteArrayInputStream(data));
    }

    /** Returns {@code true} when the root element is {@code jasperReport} in the legacy namespace. */
    protected static boolean detectLegacyJRXML(byte[] data) {
        return detectRootElement(data, LEGACY_ROOT_ELEMENT, LEGACY_NAMESPACE);
    }

    protected static boolean detectRootElement(byte[] data, String expectedRoot, String expectedNs) {
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
            XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(data));
            try {
                while (reader.hasNext()) {
                    if (reader.next() == XMLStreamReader.START_ELEMENT) {
                        return expectedRoot.equals(reader.getLocalName())
                                && expectedNs.equals(reader.getNamespaceURI());
                    }
                }
            } finally {
                reader.close();
            }
        } catch (Exception e) {
            log.debug("Error detecting JRXML root element", e);
        }
        return false;
    }

    private static byte[] readAll(InputStream is) throws JRException {
        try {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new JRException("Failed to read JRXML input stream", e);
        }
    }

    // ---------------------------------------------------------------------
    // Test / tooling helpers
    // ---------------------------------------------------------------------

    /** Convert legacy JRXML to v7 bytes; returns input unchanged if already modern. */
    public static byte[] convertLegacyToV7(InputStream is) throws JRException {
        byte[] data = readAll(is);
        if (!detectLegacyJRXML(data)) {
            return data;
        }
        try {
            return JrxmlV6ToV7Converter.convert(data);
        } catch (Exception e) {
            throw new JRException("Failed to convert legacy JRXML v6 to v7", e);
        }
    }

    /** UTF-8 string variant of {@link #convertLegacyToV7(InputStream)}. */
    public static String convertLegacyToV7String(InputStream is) throws JRException {
        return new String(convertLegacyToV7(is), StandardCharsets.UTF_8);
    }

    /**
     * Produce a deterministic, comparable JRXML representation: strips comments and the XML
     * declaration, collapses ignorable whitespace, sorts attributes, and re-indents.
     */
    public static String normalizeXml(byte[] xml) throws JRException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setIgnoringComments(true);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(
                    new StringReader(new String(xml, StandardCharsets.UTF_8))));
            stripIgnorableWhitespace(doc.getDocumentElement());
            sortAttributesRecursive(doc.getDocumentElement());

            TransformerFactory tf = TransformerFactory.newInstance();
            trySetIndentNumber(tf, 2);
            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty(OutputKeys.METHOD, "xml");
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            StringWriter sw = new StringWriter();
            t.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString().trim();
        } catch (Exception e) {
            throw new JRException("Failed to normalize JRXML for comparison", e);
        }
    }

    /** Recursively remove whitespace-only text nodes between elements. */
    private static void stripIgnorableWhitespace(Node node) {
        NodeList children = node.getChildNodes();
        boolean hasElementChild = false;
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                hasElementChild = true;
                break;
            }
        }
        List<Node> toRemove = new ArrayList<>();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (hasElementChild
                    && child.getNodeType() == Node.TEXT_NODE
                    && child.getNodeValue() != null
                    && child.getNodeValue().trim().isEmpty()) {
                toRemove.add(child);
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                stripIgnorableWhitespace(child);
            }
        }
        for (Node n : toRemove) {
            node.removeChild(n);
        }
    }

    /** Set the {@code indent-number} attribute on the factory if supported by the implementation. */
    private static void trySetIndentNumber(TransformerFactory tf, int indent) {
        try {
            tf.setAttribute("indent-number", indent);
        } catch (IllegalArgumentException ignored) {
            // not all transformer impls support this attribute
        }
    }

    /** Recursively sort element attributes by qualified name. */
    private static void sortAttributesRecursive(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            NamedNodeMap attrs = node.getAttributes();
            if (attrs != null && attrs.getLength() > 1) {
                List<String[]> pairs = new ArrayList<>(attrs.getLength());
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node a = attrs.item(i);
                    pairs.add(new String[]{a.getNodeName(), a.getNodeValue()});
                }
                pairs.sort(Comparator.comparing(p -> p[0]));
                Element el = (Element) node;
                for (String[] p : pairs) {
                    el.removeAttribute(p[0]);
                }
                for (String[] p : pairs) {
                    el.setAttribute(p[0], p[1]);
                }
            }
        }
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            sortAttributesRecursive(children.item(i));
        }
    }
}
