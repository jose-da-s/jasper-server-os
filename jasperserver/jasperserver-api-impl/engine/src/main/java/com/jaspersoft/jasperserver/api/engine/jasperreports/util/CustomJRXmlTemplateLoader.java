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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRTemplate;
import net.sf.jasperreports.engine.xml.JRXmlTemplateLoader;

/**
 * Loads JRTX supporting both legacy (6.x) and modern formats.
 * Legacy files are converted via {@link JrxmlTemplateV6ToV7Converter} before delegating to {@link JRXmlTemplateLoader}.
 */
public class CustomJRXmlTemplateLoader {

    private CustomJRXmlTemplateLoader() {
        /* This utility class should not be instantiated */
    }

    private static final Log log = LogFactory.getLog(CustomJRXmlTemplateLoader.class);

    private static final String LEGACY_NAMESPACE = "http://jasperreports.sourceforge.net/jasperreports/template";
    private static final Map<String, String> LEGACY_TO_MODERN_FIELD_NAMES = Map.of(
    		"isDefault", "default",
    		"isBold", "bold",
    		"isItalic", "italic",
    		"isUnderline", "underline",
    		"isStrikeThrough", "strikethrough",
    		"isBlankWhenNull", "blankWhenNull",
    		"isPdfEmbedded", "pdfEmbedded"
	);

    /** Load a JRTX style template, auto-detecting legacy vs. modern format. */
    public static JRTemplate load(InputStream is) throws JRException {
        byte[] data = readAll(is);
        if (detectLegacyJrtx(data)) {
            log.debug("Detected legacy JRTX; converting v6 -> v7");
            try {
                data = JrxmlV6ToV7Converter.convertJrtx(data);
            } catch (Exception e) {
                throw new JRException("Failed to convert legacy JRTX v6 to v7", e);
            }
        }
        return JRXmlTemplateLoader.load(new ByteArrayInputStream(data));
    }

    /**
     * Returns {@code true} when any &lt;style&gt; element contains a field of type isXyz (they no longer have the is-
     * prefix in JR7: isDefault -> default, isBold -> bold, etc.)
     */
    protected static boolean detectLegacyJrtx(byte[] data) {
    	try {
	        XMLInputFactory factory = XMLInputFactory.newInstance();
	        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
	        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
	        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
	        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(data));
            while (reader.hasNext()) {
	            int event = reader.next();
	            if (event != XMLStreamReader.START_ELEMENT) {
	                continue;
	            }
	
	            // Templates can declare namespaces, but the element local name remains "style".
	            if (!"style".equals(reader.getLocalName())) {
	                continue;
	            }
	
	            if (!LEGACY_NAMESPACE.equals(reader.getNamespaceURI())) {
				    continue;
				}
	
	            for (int i = 0; i < reader.getAttributeCount(); i++) {
	                String attrName = reader.getAttributeLocalName(i);
	                if (LEGACY_TO_MODERN_FIELD_NAMES.containsKey(attrName)) {
	                    return true;
	                }
	            }
            }
	    } catch (Exception e) {
	        log.debug("Error detecting JRTX version", e);
	    }
      return false;
    }

    private static byte[] readAll(InputStream is) throws JRException {
        try {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new JRException("Failed to read JRTX input stream", e);
        }
    }

    // ---------------------------------------------------------------------
    // Test / tooling helpers
    // ---------------------------------------------------------------------

    /** Convert legacy JRTX to v7 bytes; returns input unchanged if already modern. */
    public static byte[] convertLegacyToV7(InputStream is) throws JRException {
        byte[] data = readAll(is);
        if (!detectLegacyJrtx(data)) {
            return data;
        }
        try {
            return JrxmlV6ToV7Converter.convertJrtx(data);
        } catch (Exception e) {
            throw new JRException("Failed to convert legacy JRTX v6 to v7", e);
        }
    }

    /** UTF-8 string variant of {@link #convertLegacyToV7(InputStream)}. */
    public static String convertLegacyToV7String(InputStream is) throws JRException {
        return new String(convertLegacyToV7(is), StandardCharsets.UTF_8);
    }

}
