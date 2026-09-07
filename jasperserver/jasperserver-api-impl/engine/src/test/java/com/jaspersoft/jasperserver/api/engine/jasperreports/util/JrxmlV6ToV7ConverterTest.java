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

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JRPrintElement;
import net.sf.jasperreports.engine.JRPrintFrame;
import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.junit.Test;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link JrxmlV6ToV7Converter} and {@link CustomJRXmlLoader} integration.
 */
public class JrxmlV6ToV7ConverterTest {

    // ─── hello_world_jasper6 tests ───────────────────────────────

    @Test
    public void testConvertHelloWorld_rootElementCleaned() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/hello_world_jasper6.jrxml");
        Document doc = parseDom(converted);
        Element root = doc.getDocumentElement();

        assertEquals("jasperReport", root.getTagName());
        assertEquals("simple_report", root.getAttribute("name"));
        assertEquals("java", root.getAttribute("language"));

        // xmlns / xsi:schemaLocation must be removed
        assertFalse(root.hasAttribute("xmlns"));
        assertFalse(root.hasAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation"));
        assertNull(root.getAttributeNodeNS("http://www.w3.org/2000/xmlns/", "xsi"));
    }

    @Test
    public void testConvertHelloWorld_bandUnwrapped() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/hello_world_jasper6.jrxml");
        Document doc = parseDom(converted);

        // <title> band attrs hoisted onto <title>; no <band> child remains
        NodeList titles = doc.getElementsByTagName("title");
        assertEquals(1, titles.getLength());
        Element title = (Element) titles.item(0);
        assertEquals("50", title.getAttribute("height"));

        NodeList bands = title.getElementsByTagName("band");
        assertEquals(0, bands.getLength());
    }

    @Test
    public void testConvertHelloWorld_staticTextBecameElement() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/hello_world_jasper6.jrxml");
        Document doc = parseDom(converted);

        // Should find <element kind="staticText">
        NodeList elements = doc.getElementsByTagName("element");
        assertTrue("Expected at least one <element> node", elements.getLength() > 0);

        boolean foundStaticText = false;
        for (int i = 0; i < elements.getLength(); i++) {
            Element el = (Element) elements.item(i);
            if ("staticText".equals(el.getAttribute("kind"))) {
                foundStaticText = true;

                // reportElement attrs merged onto <element>
                assertEquals("0", el.getAttribute("x"));
                assertEquals("10", el.getAttribute("y"));
                assertEquals("200", el.getAttribute("width"));
                assertEquals("30", el.getAttribute("height"));

                // isBold → bold
                assertTrue("Expected 'bold' attribute", el.hasAttribute("bold"));
                assertFalse("'isBold' should have been renamed", el.hasAttribute("isBold"));
                assertEquals("true", el.getAttribute("bold"));

                // size → fontSize
                assertFalse("'size' should have been renamed to 'fontSize'", el.hasAttribute("size"));
                assertEquals("16.0", el.getAttribute("fontSize"));

                // <reportElement> / <textElement> children removed
                assertEquals(0, el.getElementsByTagName("reportElement").getLength());
                assertEquals(0, el.getElementsByTagName("textElement").getLength());
            }
        }
        assertTrue("Expected <element kind='staticText'>", foundStaticText);
    }

    // ─── complex_legacy_v6.jrtx tests ─────────────────────────────────

    @Test
    public void testConvertComplexJrtx_styleFontSizeNormalized() throws Exception {
        byte[] converted = convertJrtxResource("jrtx/unit_test/complex_legacy_v6.jrtx");
        Document doc = parseDom(converted);

        NodeList styles = doc.getElementsByTagName("style");
        for (int i = 0; i < styles.getLength(); i++) {
            Element style = (Element) styles.item(i);
            if ("addressLabel".equals(style.getAttribute("name"))) {
                assertEquals("fontSize should be normalised to one decimal",
                        "8.0", style.getAttribute("fontSize"));
                // isBold → bold
                assertFalse(style.hasAttribute("isBold"));
                assertTrue(style.hasAttribute("bold"));
            }
        }
    }

    // ─── complex_legacy_v6.jrxml tests ─────────────────────────────────

    @Test
    public void testConvertComplex_styleFontSizeNormalized() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/complex_legacy_v6.jrxml");
        Document doc = parseDom(converted);

        NodeList styles = doc.getElementsByTagName("style");
        for (int i = 0; i < styles.getLength(); i++) {
            Element style = (Element) styles.item(i);
            if ("BaseStyle".equals(style.getAttribute("name"))) {
                assertEquals("fontSize should be normalised to one decimal",
                        "10.0", style.getAttribute("fontSize"));
                // isBold → bold
                assertFalse(style.hasAttribute("isBold"));
                assertTrue(style.hasAttribute("bold"));
            }
            if ("HeaderStyle".equals(style.getAttribute("name"))) {
                assertEquals("14.0", style.getAttribute("fontSize"));
                assertTrue(style.hasAttribute("bold"));
                assertEquals("true", style.getAttribute("bold"));
            }
        }
    }

    @Test
    public void testConvertComplex_studioPropertiesRemoved() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/complex_legacy_v6.jrxml");
        String xml = new String(converted, StandardCharsets.UTF_8);

        assertFalse("studio data adapter property should be removed",
                xml.contains("com.jaspersoft.studio.data.defaultdataadapter"));
        assertFalse("studio unit.height property should be removed",
                xml.contains("com.jaspersoft.studio.unit.height"));
        assertFalse("studio unit.width property should be removed",
                xml.contains("com.jaspersoft.studio.unit.width"));
        assertFalse("studio unit.x property should be removed",
                xml.contains("com.jaspersoft.studio.unit.x"));
        assertFalse("studio unit.y property should be removed",
                xml.contains("com.jaspersoft.studio.unit.y"));
        assertFalse("studio layout property should be removed",
                xml.contains("com.jaspersoft.studio.layout"));

        // But non-studio properties should remain
        assertTrue("Non-studio properties should be preserved",
                xml.contains("net.sf.jasperreports.export.pdf.tagged"));
    }

    @Test
    public void testConvertComplex_textFieldExpressionRenamed() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/complex_legacy_v6.jrxml");
        String xml = new String(converted, StandardCharsets.UTF_8);

        // textFieldExpression → expression
        assertFalse("textFieldExpression should be renamed to expression",
                xml.contains("<textFieldExpression"));
        assertTrue("Expected <expression> elements in output",
                xml.contains("<expression"));
    }

    @Test
    public void testConvertComplex_variableExpressionRenamed() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/complex_legacy_v6.jrxml");
        String xml = new String(converted, StandardCharsets.UTF_8);

        assertFalse("variableExpression should be renamed to expression",
                xml.contains("<variableExpression"));
    }

    @Test
    public void testConvertComplex_groupExpressionRenamed() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/complex_legacy_v6.jrxml");
        String xml = new String(converted, StandardCharsets.UTF_8);

        assertFalse("groupExpression should be renamed to expression",
                xml.contains("<groupExpression"));
    }

    @Test
    public void testConvertComplex_textAlignmentRenamed() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/complex_legacy_v6.jrxml");
        Document doc = parseDom(converted);

        // textAlignment → hTextAlign
        NodeList elements = doc.getElementsByTagName("element");
        boolean foundHTextAlign = false;
        for (int i = 0; i < elements.getLength(); i++) {
            Element el = (Element) elements.item(i);
            if (el.hasAttribute("hTextAlign")) {
                foundHTextAlign = true;
            }
            assertFalse("textAlignment should have been renamed to hTextAlign",
                    el.hasAttribute("textAlignment"));
        }
        assertTrue("Expected at least one element with hTextAlign", foundHTextAlign);
    }

    @Test
    public void testConvertComplex_verticalAlignmentRenamed() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/complex_legacy_v6.jrxml");
        Document doc = parseDom(converted);

        // verticalAlignment → vTextAlign
        NodeList elements = doc.getElementsByTagName("element");
        boolean foundVTextAlign = false;
        for (int i = 0; i < elements.getLength(); i++) {
            Element el = (Element) elements.item(i);
            if (el.hasAttribute("vTextAlign")) {
                foundVTextAlign = true;
            }
            assertFalse("verticalAlignment should have been renamed to vTextAlign",
                    el.hasAttribute("verticalAlignment"));
        }
        assertTrue("Expected at least one element with vTextAlign", foundVTextAlign);
    }

    @Test
    public void testConvertComplex_isBooleanPrefixRemovedGlobally() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/complex_legacy_v6.jrxml");
        String xml = new String(converted, StandardCharsets.UTF_8);

        // isBlankWhenNull → blankWhenNull
        assertFalse("isBlankWhenNull should have been renamed",
                xml.contains("isBlankWhenNull"));
        assertTrue("Expected blankWhenNull attribute",
                xml.contains("blankWhenNull"));

        // isStretchWithOverflow → textAdjust="StretchHeight"
        assertFalse("isStretchWithOverflow should have been removed",
                xml.contains("isStretchWithOverflow"));
        assertFalse("stretchWithOverflow is not a valid v7 attribute",
                xml.contains("stretchWithOverflow"));
        assertTrue("isStretchWithOverflow='true' should become textAdjust='StretchHeight'",
                xml.contains("textAdjust"));

        // is*Page / isDefault → no 'is' prefix
        assertFalse("isSummaryNewPage should have been renamed",
                xml.contains("isSummaryNewPage"));
        assertFalse("isStartNewPage should have been renamed",
                xml.contains("isStartNewPage"));
        assertFalse("isReprintHeaderOnEachPage should have been renamed",
                xml.contains("isReprintHeaderOnEachPage"));
        assertFalse("isDefault should have been renamed",
                xml.contains("isDefault"));
    }

    @Test
    public void testConvertComplex_allBandsUnwrapped() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/complex_legacy_v6.jrxml");
        Document doc = parseDom(converted);

        for (String parentTag : new String[]{"title", "pageHeader", "columnHeader",
                "columnFooter", "pageFooter", "summary"}) {
            NodeList parents = doc.getElementsByTagName(parentTag);
            for (int i = 0; i < parents.getLength(); i++) {
                Element parent = (Element) parents.item(i);
                NodeList bands = parent.getElementsByTagName("band");
                assertEquals("Expected no <band> child inside <" + parentTag + ">",
                        0, bands.getLength());
                // height attribute hoisted onto parent
                assertTrue("Expected height attribute on <" + parentTag + ">",
                        parent.hasAttribute("height"));
            }
        }
    }

    @Test
    public void testConvertComplex_imageConvertedToElement() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/complex_legacy_v6.jrxml");
        Document doc = parseDom(converted);

        NodeList elements = doc.getElementsByTagName("element");
        boolean foundImage = false;
        for (int i = 0; i < elements.getLength(); i++) {
            Element el = (Element) elements.item(i);
            if ("image".equals(el.getAttribute("kind"))) {
                foundImage = true;
                assertEquals("33333333-3333-3333-3333-333333333333", el.getAttribute("uuid"));
            }
        }
        assertTrue("Expected <element kind='image'>", foundImage);
    }

    @Test
    public void testConvertComplex_imageExpressionRenamed() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/complex_legacy_v6.jrxml");
        String xml = new String(converted, StandardCharsets.UTF_8);

        assertFalse("imageExpression should be renamed to expression",
                xml.contains("<imageExpression"));
    }

    @Test
    public void testConvertComplex_lineConvertedToElement() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/complex_legacy_v6.jrxml");
        Document doc = parseDom(converted);

        NodeList elements = doc.getElementsByTagName("element");
        boolean foundLine = false;
        for (int i = 0; i < elements.getLength(); i++) {
            Element el = (Element) elements.item(i);
            if ("line".equals(el.getAttribute("kind"))) {
                foundLine = true;
                assertEquals("44444444-4444-4444-4444-444444444444", el.getAttribute("uuid"));
            }
        }
        assertTrue("Expected <element kind='line'>", foundLine);
    }

    @Test
    public void testConvertTableLegacy_componentConvertedCloseToV7() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/table_legacy_v6.jrxml");
        Document doc = parseDom(converted);
        String xml = new String(converted, StandardCharsets.UTF_8);

        assertFalse("Converted table should not keep component namespace declarations", xml.contains("xmlns:jr"));
        assertFalse("Studio layout metadata should be removed", xml.contains("com.jaspersoft.studio.layout"));
        assertFalse("Studio table column metadata should be removed",
                xml.contains("com.jaspersoft.studio.components.table.model.column.name"));

        Element outerComponentElement = findElementByKind(doc.getElementsByTagName("element"), "component");
        assertNotNull("Expected outer <element kind='component'>", outerComponentElement);
        assertEquals("7786bb05-503a-4b6a-991a-a7bcf3abad07", outerComponentElement.getAttribute("uuid"));
        assertEquals("555", outerComponentElement.getAttribute("width"));
        assertEquals("200", outerComponentElement.getAttribute("height"));

        NodeList components = doc.getElementsByTagName("component");
        assertEquals("Expected a single nested <component>", 1, components.getLength());

        Element table = (Element) components.item(0);
        assertEquals("table", table.getAttribute("kind"));
        assertEquals(1, table.getElementsByTagName("datasetRun").getLength());
        assertEquals(2, table.getElementsByTagName("column").getLength());
        assertEquals(2, table.getElementsByTagName("tableHeader").getLength());
        assertEquals(2, table.getElementsByTagName("detailCell").getLength());

        NodeList columns = table.getElementsByTagName("column");
        Element firstColumn = (Element) columns.item(0);
        Element secondColumn = (Element) columns.item(1);
        assertEquals("single", firstColumn.getAttribute("kind"));
        assertEquals("130", firstColumn.getAttribute("width"));
        assertEquals("single", secondColumn.getAttribute("kind"));
        assertEquals("140", secondColumn.getAttribute("width"));
    }

    @Test
    public void testConvertDefaultNamespaceTable_preservesAttributesAndGroupKind() throws Exception {
        String legacy = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<jasperReport xmlns=\"http://jasperreports.sourceforge.net/jasperreports\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "name=\"default_ns_table\" pageWidth=\"100\" pageHeight=\"100\" columnWidth=\"100\" "
                + "leftMargin=\"0\" rightMargin=\"0\" topMargin=\"0\" bottomMargin=\"0\">\n"
                + "  <detail>\n"
                + "    <band height=\"20\">\n"
                + "      <componentElement>\n"
                + "        <reportElement x=\"0\" y=\"0\" width=\"100\" height=\"20\" uuid=\"outer-uuid\"/>\n"
                + "        <table xmlns=\"http://jasperreports.sourceforge.net/jasperreports/components\" whenNoDataType=\"AllSectionsNoDetail\">\n"
                + "          <columnGroup width=\"100\" uuid=\"group-uuid\">\n"
                + "            <columnHeader height=\"10\"/>\n"
                + "            <column width=\"100\" uuid=\"column-uuid\">\n"
                + "              <detailCell height=\"10\"/>\n"
                + "            </column>\n"
                + "          </columnGroup>\n"
                + "        </table>\n"
                + "      </componentElement>\n"
                + "    </band>\n"
                + "  </detail>\n"
                + "</jasperReport>\n";

        byte[] converted = JrxmlV6ToV7Converter.convert(legacy.getBytes(StandardCharsets.UTF_8));
        Document doc = parseDom(converted);

        NodeList components = doc.getElementsByTagName("component");
        assertEquals("Expected the default-namespace table to become a v7 component",
                1, components.getLength());

        Element table = (Element) components.item(0);
        assertEquals("table", table.getAttribute("kind"));
        assertEquals("AllSectionsNoDetail", table.getAttribute("whenNoDataType"));

        NodeList columns = table.getElementsByTagName("column");
        assertEquals("Expected the group column plus its nested single column",
                2, columns.getLength());

        Element groupColumn = (Element) columns.item(0);
        Element singleColumn = (Element) columns.item(1);
        assertEquals("group", groupColumn.getAttribute("kind"));
        assertEquals("group-uuid", groupColumn.getAttribute("uuid"));
        assertEquals("100", groupColumn.getAttribute("width"));
        assertEquals("single", singleColumn.getAttribute("kind"));
        assertEquals("column-uuid", singleColumn.getAttribute("uuid"));
    }

    // ─── Integration: CustomJRXmlLoader.load() ───────────────────

    @Test
    public void testCustomLoaderLoadHelloWorld() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("jrxml/unit_test/hello_world_jasper6.jrxml")) {
            assertNotNull("Test resource not found", is);
            net.sf.jasperreports.engine.design.JasperDesign design = CustomJRXmlLoader.load(is);
            assertNotNull("JasperDesign should not be null", design);
            assertEquals("simple_report", design.getName());
        }
    }

    @Test
    public void testCustomLoaderLoadComplexLegacy() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("jrxml/unit_test/complex_legacy_v6.jrxml")) {
            assertNotNull("Test resource not found", is);
            net.sf.jasperreports.engine.design.JasperDesign design = CustomJRXmlLoader.load(is);
            assertNotNull("JasperDesign should not be null", design);
            assertEquals("complex_legacy_report", design.getName());
        }
    }

    @Test
    public void testCustomLoaderLoadLegacyTable() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("jrxml/unit_test/table_legacy_v6.jrxml")) {
            assertNotNull("Test resource not found", is);
            net.sf.jasperreports.engine.design.JasperDesign design = CustomJRXmlLoader.load(is);
            assertNotNull("JasperDesign should not be null", design);
            assertEquals("template_Table", design.getName());
        }
    }

    @Test
    public void testCustomLoaderLoadLegacyQueryDefaultsToSql() throws Exception {
        try (InputStream is = new ByteArrayInputStream(createLegacyUsersReport().getBytes(StandardCharsets.UTF_8))) {
            net.sf.jasperreports.engine.design.JasperDesign design = CustomJRXmlLoader.load(is);

            assertNotNull("Converted legacy report should keep its query", design.getQuery());
            assertEquals("Legacy queryString without explicit language should default to SQL",
                    "sql", design.getQuery().getLanguage());
            assertTrue(design.getQuery().getText().contains("FROM JIUser"));
        }
    }

    @Test
    public void testCustomLoaderRenderLegacyUsersReportWithData() throws Exception {
        net.sf.jasperreports.engine.design.JasperDesign design;
        try (InputStream is = new ByteArrayInputStream(createLegacyUsersReport().getBytes(StandardCharsets.UTF_8))) {
            design = CustomJRXmlLoader.load(is);
        }

        JasperPrint jasperPrint = JasperFillManager.fillReport(
                JasperCompileManager.compileReport(design),
                new HashMap<>(),
                new JRMapCollectionDataSource(List.of(createUserRow("jasperadmin", "jasperadmin@example.com", Boolean.TRUE)))
        );

        assertEquals("Legacy report should render a page when rows are supplied",
                1, jasperPrint.getPages().size());
        assertFalse("Rendered page should contain visible elements",
                jasperPrint.getPages().get(0).getElements().isEmpty());
    }

    @Test
    public void testCustomLoaderRenderLegacyUsersReportWithoutDataProducesNoPagesByDefault() throws Exception {
        net.sf.jasperreports.engine.design.JasperDesign design;
        try (InputStream is = new ByteArrayInputStream(createLegacyUsersReport().getBytes(StandardCharsets.UTF_8))) {
            design = CustomJRXmlLoader.load(is);
        }

        JasperPrint jasperPrint = JasperFillManager.fillReport(
                JasperCompileManager.compileReport(design),
                new HashMap<>(),
                new JRMapCollectionDataSource(List.of())
        );

        assertEquals("This report has only columnHeader/detail bands and no whenNoDataType override, so no rows means no pages",
                0, jasperPrint.getPages().size());
    }

    @Test
    public void testCustomLoaderRenderExactStudioLegacyUsersReportWithData() throws Exception {
        net.sf.jasperreports.engine.design.JasperDesign design;
        try (InputStream is = new ByteArrayInputStream(createStudioLegacyUsersReport().getBytes(StandardCharsets.UTF_8))) {
            design = CustomJRXmlLoader.load(is);
        }

        JasperPrint jasperPrint = JasperFillManager.fillReport(
                JasperCompileManager.compileReport(design),
                new HashMap<>(),
                new JRMapCollectionDataSource(List.of(createUserRow("jasperadmin", "jasperadmin@example.com", Boolean.TRUE)))
        );

        assertEquals("Studio-authored legacy report should render a page when rows are supplied",
                1, jasperPrint.getPages().size());

        List<String> printedTexts = extractPrintedTexts(jasperPrint);
        assertTrue("Column header should be rendered", printedTexts.contains("username"));
        assertTrue("Column header should be rendered", printedTexts.contains("emailaddress"));
        assertTrue("Column header should be rendered", printedTexts.contains("enabled"));
        assertTrue("Detail text should be rendered", printedTexts.contains("jasperadmin"));
        assertTrue("Detail text should be rendered", printedTexts.contains("jasperadmin@example.com"));
        assertTrue("Boolean detail value should be rendered",
                printedTexts.stream().anyMatch(text -> "true".equalsIgnoreCase(text)));
    }

    @Test
    public void testCustomLoaderRenderExactStudioLegacyUsersReportWithoutDataProducesNoPagesByDefault() throws Exception {
        net.sf.jasperreports.engine.design.JasperDesign design;
        try (InputStream is = new ByteArrayInputStream(createStudioLegacyUsersReport().getBytes(StandardCharsets.UTF_8))) {
            design = CustomJRXmlLoader.load(is);
        }

        JasperPrint jasperPrint = JasperFillManager.fillReport(
                JasperCompileManager.compileReport(design),
                new HashMap<>(),
                new JRMapCollectionDataSource(List.of())
        );

        assertEquals("The attached Studio-authored report has only columnHeader/detail bands and no whenNoDataType override, so no rows means no pages",
                0, jasperPrint.getPages().size());
    }

    @Test
    public void testDetectLegacyJRXML() throws Exception {
        byte[] data = loadResource("jrxml/unit_test/hello_world_jasper6.jrxml");
        assertTrue("hello_world_jasper6.jrxml should be detected as legacy",
                CustomJRXmlLoader.detectLegacyJRXML(data));
    }

    @Test
    public void testNonLegacyNotDetected() throws Exception {
        String v7 = "<jasperReport name=\"test\" language=\"java\"><title height=\"50\"/></jasperReport>";
        byte[] data = v7.getBytes(StandardCharsets.UTF_8);
        assertFalse("V7 JRXML without legacy namespace should not be detected as legacy",
                CustomJRXmlLoader.detectLegacyJRXML(data));
    }

    // ─── jasper6_json tests ──────────────────────────────────────

    @Test
    public void testConvertJsonMaster_rootElementCorrect() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/jasper6_json.jrxml");
        Document doc = parseDom(converted);
        Element root = doc.getDocumentElement();

        assertEquals("jasperReport", root.getTagName());
        assertEquals("Json_Master", root.getAttribute("name"));
        assertEquals("java", root.getAttribute("language"));
        assertEquals("AllSectionsNoDetail", root.getAttribute("whenNoDataType"));
        assertFalse(root.hasAttribute("xmlns"));
    }

    @Test
    public void testConvertJsonMaster_parameterDescriptionRenamed() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/jasper6_json.jrxml");
        String xml = new String(converted, StandardCharsets.UTF_8);

        assertFalse("parameterDescription should be renamed to description",
                xml.contains("<parameterDescription"));
        assertTrue("Expected <description> element for parameter",
                xml.contains("<description"));
    }

    @Test
    public void testConvertJsonMaster_defaultValueExpressionPreserved() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/jasper6_json.jrxml");
        String xml = new String(converted, StandardCharsets.UTF_8);

        assertTrue("defaultValueExpression should be preserved",
                xml.contains("<defaultValueExpression"));
        assertTrue("JSON default value should contain the test data",
                xml.contains("firstName"));
    }

    @Test
    public void testConvertJsonMaster_subreportConverted() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/jasper6_json.jrxml");
        Document doc = parseDom(converted);

        NodeList elements = doc.getElementsByTagName("element");
        boolean foundSubreport = false;
        for (int i = 0; i < elements.getLength(); i++) {
            Element el = (Element) elements.item(i);
            if ("subreport".equals(el.getAttribute("kind"))) {
                foundSubreport = true;
                assertEquals("554", el.getAttribute("width"));
                assertEquals("211", el.getAttribute("height"));
            }
        }
        assertTrue("Expected <element kind='subreport'>", foundSubreport);
    }

    @Test
    public void testConvertJsonMaster_dataSourceExpressionPreserved() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/jasper6_json.jrxml");
        String xml = new String(converted, StandardCharsets.UTF_8);

        assertTrue("dataSourceExpression should be preserved (not renamed)",
                xml.contains("<dataSourceExpression"));
        assertTrue("dataSourceExpression should contain JsonDataSource",
                xml.contains("JsonDataSource"));
    }

    @Test
    public void testConvertJsonMaster_subreportExpressionRenamed() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/jasper6_json.jrxml");
        String xml = new String(converted, StandardCharsets.UTF_8);

        assertFalse("subreportExpression should be renamed to expression",
                xml.contains("<subreportExpression"));
        assertTrue("Expected subreport's expression element",
                xml.contains("Json_Sub.jasper"));
    }

    @Test
    public void testConvertJsonMaster_queryConverted() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/jasper6_json.jrxml");
        String xml = new String(converted, StandardCharsets.UTF_8);

        assertFalse("queryString should be renamed to query",
                xml.contains("<queryString"));
        assertTrue("Expected <query> element",
                xml.contains("<query"));
    }

    @Test
    public void testConvertJsonMaster_bandUnwrapped() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/jasper6_json.jrxml");
        Document doc = parseDom(converted);

        NodeList titles = doc.getElementsByTagName("title");
        assertEquals(1, titles.getLength());
        Element title = (Element) titles.item(0);
        assertEquals("211", title.getAttribute("height"));
        assertEquals(0, title.getElementsByTagName("band").getLength());
    }

    @Test
    public void testCustomLoaderLoadJsonMaster() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("jrxml/unit_test/jasper6_json.jrxml")) {
            assertNotNull("Test resource not found", is);
            net.sf.jasperreports.engine.design.JasperDesign design = CustomJRXmlLoader.load(is);
            assertNotNull("JasperDesign should not be null", design);
            assertEquals("Json_Master", design.getName());
            assertNotNull("whenNoDataType should be set",
                    design.getWhenNoDataType());
        }
    }

    // ─── jasperserver_users_v6 tests ─────────────────────────────

    @Test
    public void testConvertUsers_rootElementCorrect() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/jasperserver_users_v6.jrxml");
        Document doc = parseDom(converted);
        Element root = doc.getDocumentElement();

        assertEquals("jasperReport", root.getTagName());
        assertEquals("jasperserver-users", root.getAttribute("name"));
        assertEquals("java", root.getAttribute("language"));
    }

    @Test
    public void testConvertUsers_queryConverted() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/jasperserver_users_v6.jrxml");
        Document doc = parseDom(converted);

        NodeList queries = doc.getElementsByTagName("query");
        assertEquals(1, queries.getLength());
        Element query = (Element) queries.item(0);
        assertEquals("sql", query.getAttribute("language"));
        assertTrue(query.getTextContent().contains("FROM JIUser"));
    }

    @Test
    public void testConvertUsers_elementsConverted() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/jasperserver_users_v6.jrxml");
        Document doc = parseDom(converted);

        NodeList elements = doc.getElementsByTagName("element");
        int staticTextCount = 0;
        int textFieldCount = 0;
        for (int i = 0; i < elements.getLength(); i++) {
            Element el = (Element) elements.item(i);
            if ("staticText".equals(el.getAttribute("kind"))) staticTextCount++;
            if ("textField".equals(el.getAttribute("kind"))) textFieldCount++;
        }
        assertEquals("Expected 3 staticText elements (column headers)", 3, staticTextCount);
        assertEquals("Expected 3 textField elements (detail fields)", 3, textFieldCount);
    }

    @Test
    public void testConvertUsers_noLegacyElements() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/jasperserver_users_v6.jrxml");
        String xml = new String(converted, StandardCharsets.UTF_8);

        assertFalse("No <reportElement> should remain", xml.contains("<reportElement"));
        assertFalse("No <textFieldExpression> should remain", xml.contains("<textFieldExpression"));
        assertFalse("No <queryString> should remain", xml.contains("<queryString"));
    }

    @Test
    public void testCustomLoaderLoadAndRenderUsers() throws Exception {
        net.sf.jasperreports.engine.design.JasperDesign design;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("jrxml/unit_test/jasperserver_users_v6.jrxml")) {
            assertNotNull("Test resource not found", is);
            design = CustomJRXmlLoader.load(is);
        }

        JasperPrint jasperPrint = JasperFillManager.fillReport(
                JasperCompileManager.compileReport(design),
                new HashMap<>(),
                new JRMapCollectionDataSource(List.of(
                        createUserRow("admin", "admin@test.com", Boolean.TRUE),
                        createUserRow("user1", "user1@test.com", Boolean.FALSE)
                ))
        );

        assertEquals("Should render 1 page with data", 1, jasperPrint.getPages().size());
        List<String> texts = extractPrintedTexts(jasperPrint);
        assertTrue("Should contain 'admin'", texts.contains("admin"));
        assertTrue("Should contain 'user1'", texts.contains("user1"));
        assertTrue("Should contain column header 'username'", texts.contains("username"));
        assertTrue("Should contain column header 'emailaddress'", texts.contains("emailaddress"));
    }

    // ─── example_v6 tests ────────────────────────────────────────

    @Test
    public void testConvertExample_languageForcedToJava() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/example_v6.jrxml");
        Document doc = parseDom(converted);
        Element root = doc.getDocumentElement();

        assertEquals("Language should be forced to 'java' (was 'groovy')",
                "java", root.getAttribute("language"));
    }

    @Test
    public void testConvertExample_stylesWithBoxPreserved() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/example_v6.jrxml");
        Document doc = parseDom(converted);

        NodeList styles = doc.getElementsByTagName("style");
        assertTrue("Expected styles to be preserved", styles.getLength() > 0);

        boolean foundBox = false;
        for (int i = 0; i < styles.getLength(); i++) {
            Element style = (Element) styles.item(i);
            if (style.getElementsByTagName("box").getLength() > 0) {
                foundBox = true;
            }
        }
        assertTrue("Styles with <box> should be preserved", foundBox);
    }

    @Test
    public void testConvertExample_textAlignmentConverted() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/example_v6.jrxml");
        Document doc = parseDom(converted);

        NodeList elements = doc.getElementsByTagName("element");
        boolean foundHTextAlign = false;
        boolean foundVTextAlign = false;
        for (int i = 0; i < elements.getLength(); i++) {
            Element el = (Element) elements.item(i);
            if (el.hasAttribute("hTextAlign")) foundHTextAlign = true;
            if (el.hasAttribute("vTextAlign")) foundVTextAlign = true;
            assertFalse("textAlignment should be renamed", el.hasAttribute("textAlignment"));
            assertFalse("verticalAlignment should be renamed", el.hasAttribute("verticalAlignment"));
        }
        assertTrue("Expected hTextAlign on title staticText", foundHTextAlign);
        assertTrue("Expected vTextAlign on title staticText", foundVTextAlign);
    }

    @Test
    public void testConvertExample_fontAttributesMerged() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/example_v6.jrxml");
        Document doc = parseDom(converted);

        NodeList elements = doc.getElementsByTagName("element");
        for (int i = 0; i < elements.getLength(); i++) {
            Element el = (Element) elements.item(i);
            // Title staticText should have font attributes merged
            if ("staticText".equals(el.getAttribute("kind")) && el.hasAttribute("fontSize")) {
                assertEquals("30.0", el.getAttribute("fontSize"));
                assertEquals("true", el.getAttribute("bold"));
            }
        }
        String xml = new String(converted, StandardCharsets.UTF_8);
        assertFalse("No <font> elements should remain", xml.contains("<font "));
        assertFalse("No <textElement> should remain", xml.contains("<textElement"));
        assertFalse("isBold should be renamed to bold", xml.contains("isBold"));
    }

    @Test
    public void testConvertExample_noLegacyElements() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/example_v6.jrxml");
        String xml = new String(converted, StandardCharsets.UTF_8);

        assertFalse("No <reportElement> should remain", xml.contains("<reportElement"));
        assertFalse("No <textFieldExpression> should remain", xml.contains("<textFieldExpression"));
        assertFalse("No namespace declarations should remain", xml.contains("xmlns"));
    }

    @Test
    public void testConvertExample_bandsHandledCorrectly() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/example_v6.jrxml");
        Document doc = parseDom(converted);

        // title: band unwrapped
        NodeList titles = doc.getElementsByTagName("title");
        assertEquals(1, titles.getLength());
        Element title = (Element) titles.item(0);
        assertEquals("79", title.getAttribute("height"));
        assertEquals(0, title.getElementsByTagName("band").getLength());

        // columnHeader: band unwrapped
        NodeList colHeaders = doc.getElementsByTagName("columnHeader");
        assertEquals(1, colHeaders.getLength());
        Element colHeader = (Element) colHeaders.item(0);
        assertEquals("50", colHeader.getAttribute("height"));

        // detail: band kept
        NodeList details = doc.getElementsByTagName("detail");
        assertEquals(1, details.getLength());
        Element detail = (Element) details.item(0);
        NodeList detailBands = detail.getElementsByTagName("band");
        assertEquals("detail should keep its band", 1, detailBands.getLength());
        assertEquals("122", ((Element) detailBands.item(0)).getAttribute("height"));

        // pageFooter: band unwrapped
        NodeList footers = doc.getElementsByTagName("pageFooter");
        assertEquals(1, footers.getLength());
        Element footer = (Element) footers.item(0);
        assertEquals("50", footer.getAttribute("height"));
    }

    @Test
    public void testCustomLoaderLoadAndRenderExample() throws Exception {
        net.sf.jasperreports.engine.design.JasperDesign design;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("jrxml/unit_test/example_v6.jrxml")) {
            assertNotNull("Test resource not found", is);
            design = CustomJRXmlLoader.load(is);
        }

        // Fill with sample data
        List<Map<String, ?>> data = new ArrayList<>();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", "ITEM-001");
        row.put("name", "Widget");
        row.put("total", 29.99);
        data.add(row);

        Map<String, Object> params = new HashMap<>();
        params.put("HEADER", "<b>Test Header</b>");
        params.put("FOOTER", "Test Footer");

        JasperPrint jasperPrint = JasperFillManager.fillReport(
                JasperCompileManager.compileReport(design),
                params,
                new JRMapCollectionDataSource(data)
        );

        assertEquals("Should render 1 page", 1, jasperPrint.getPages().size());
        List<String> texts = extractPrintedTexts(jasperPrint);
        assertTrue("Should contain title text",
                texts.stream().anyMatch(t -> t.contains("Jasper Example Basket Report")));
        assertTrue("Should contain data field 'ITEM-001'", texts.contains("ITEM-001"));
        assertTrue("Should contain data field 'Widget'", texts.contains("Widget"));
        assertTrue("Should contain footer", texts.contains("Test Footer"));
    }

    // ─── subreportParameter conversion test ──────────────────────

    @Test
    public void testConvertSubreportParameterRenamed() throws Exception {
        String legacy = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<jasperReport xmlns=\"http://jasperreports.sourceforge.net/jasperreports\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "name=\"subreport_param_test\" pageWidth=\"100\" pageHeight=\"100\" columnWidth=\"100\" "
                + "leftMargin=\"0\" rightMargin=\"0\" topMargin=\"0\" bottomMargin=\"0\">\n"
                + "  <title>\n"
                + "    <band height=\"50\">\n"
                + "      <subreport>\n"
                + "        <reportElement x=\"0\" y=\"0\" width=\"100\" height=\"50\"/>\n"
                + "        <subreportParameter name=\"MY_PARAM\">\n"
                + "          <subreportParameterExpression><![CDATA[\"hello\"]]></subreportParameterExpression>\n"
                + "        </subreportParameter>\n"
                + "        <subreportExpression><![CDATA[\"Sub.jasper\"]]></subreportExpression>\n"
                + "      </subreport>\n"
                + "    </band>\n"
                + "  </title>\n"
                + "</jasperReport>\n";

        byte[] converted = JrxmlV6ToV7Converter.convert(legacy.getBytes(StandardCharsets.UTF_8));
        String xml = new String(converted, StandardCharsets.UTF_8);

        assertFalse("subreportParameter should be renamed to parameter",
                xml.contains("<subreportParameter"));
        assertFalse("subreportParameterExpression should be renamed to expression",
                xml.contains("<subreportParameterExpression"));
        assertTrue("Expected <parameter name=\"MY_PARAM\">",
                xml.contains("name=\"MY_PARAM\""));
    }

    @Test
    public void testConvertTableHyperlink_loadsViaJRXmlLoader() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/table_hyperlink_v6.jrxml");
        // Round-trip via the modern JRXmlLoader (Jackson). Reproduces the production
        // path used by CustomJRXmlLoader and exercises polymorphic deserialisation
        // of <hyperlinkParameter> children inside table group cells, which requires
        // the converter to emit kind="..." as the first attribute on each <element>.
        net.sf.jasperreports.engine.JasperReport jr =
                JasperCompileManager.compileReport(
                        new ByteArrayInputStream(converted));
        assertNotNull(jr);
    }

    @Test
    public void testConvertTableHyperlink_kindAttributeIsFirst() throws Exception {
        byte[] converted = convertResource("jrxml/unit_test/table_hyperlink_v6.jrxml");
        String xml = new String(converted, StandardCharsets.UTF_8);
        // 'kind="..."' must be the first attribute of every <element> tag
        // so Jackson can resolve polymorphic subtypes.
        Matcher m = Pattern.compile("<element(\\s+[^>]*?)>").matcher(xml);
        int checked = 0;
        while (m.find()) {
            String attrs = m.group(1).stripLeading();
            assertTrue("<element> tag must have kind= as the first attribute, got: " + attrs,
                    attrs.startsWith("kind=\""));
            checked++;
        }
        assertTrue("Expected at least one <element> tag", checked > 0);
    }

    @Test
    public void testMinimalHyperlinkParameterLoads() throws Exception {
        // Minimal v7 textField with a hyperlinkParameter — should load directly without the converter.
        String v7 = """
                <jasperReport name="t" language="java" pageWidth="100" pageHeight="100" columnWidth="100" leftMargin="0" rightMargin="0" topMargin="0" bottomMargin="0">
                    <summary height="50">
                        <element kind="textField" x="0" y="0" width="100" height="20" linkType="ReportExecution">
                            <expression><![CDATA["x"]]></expression>
                            <hyperlinkParameter name="jr.uri">
                                <expression><![CDATA["/reports/MapReport.jasper"]]></expression>
                            </hyperlinkParameter>
                        </element>
                    </summary>
                </jasperReport>
                """;
        net.sf.jasperreports.engine.design.JasperDesign design =
                net.sf.jasperreports.engine.xml.JRXmlLoader.load(new ByteArrayInputStream(v7.getBytes(StandardCharsets.UTF_8)));
        assertNotNull(design);
    }

    // ─── Helpers ─────────────────────────────────────────────────

    private byte[] convertResource(String resourcePath) throws Exception {
        byte[] data = loadResource(resourcePath);
        return JrxmlV6ToV7Converter.convert(data);
    }

    private byte[] convertJrtxResource(String resourcePath) throws Exception {
        byte[] data = loadResource(resourcePath);
        return JrxmlV6ToV7Converter.convertJrtx(data);
    }

    private byte[] loadResource(String resourcePath) throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            assertNotNull("Test resource not found: " + resourcePath, is);
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int len;
            while ((len = is.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            return baos.toByteArray();
        }
    }

    private Document parseDom(byte[] xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xml));
    }

    private Element findElementByKind(NodeList elements, String kind) {
        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);
            if (kind.equals(element.getAttribute("kind"))) {
                return element;
            }
        }
        return null;
    }

    private Map<String, Object> createUserRow(String username, String emailAddress, Boolean enabled) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("username", username);
        row.put("emailaddress", emailAddress);
        row.put("enabled", enabled);
        return row;
    }

    private List<String> extractPrintedTexts(JasperPrint jasperPrint) {
        List<String> texts = new ArrayList<>();
        jasperPrint.getPages().forEach(page -> collectPrintedTexts(page.getElements(), texts));
        return texts;
    }

    private void collectPrintedTexts(List<? extends JRPrintElement> elements, List<String> texts) {
        for (JRPrintElement element : elements) {
            if (element instanceof JRPrintText printText) {
                String text = printText.getFullText();
                if (text != null && !text.isBlank()) {
                    texts.add(text.trim());
                }
            } else if (element instanceof JRPrintFrame printFrame) {
                collectPrintedTexts(printFrame.getElements(), texts);
            }
        }
    }

    private String createLegacyUsersReport() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <jasperReport xmlns="http://jasperreports.sourceforge.net/jasperreports"
                              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                              xsi:schemaLocation="http://jasperreports.sourceforge.net/jasperreports http://jasperreports.sourceforge.net/xsd/jasperreport.xsd"
                              name="jasperserver-users"
                              pageWidth="595"
                              pageHeight="842"
                              columnWidth="555"
                              leftMargin="20"
                              rightMargin="20"
                              topMargin="20"
                              bottomMargin="20"
                              uuid="3431f537-52df-4829-9709-aa673c21d48a">
                    <property name="com.jaspersoft.studio.data.defaultdataadapter" value="PG jasperserverdev"/>
                    <property name="com.jaspersoft.studio.data.sql.tables" value=""/>
                    <queryString>
                        <![CDATA[SELECT
                username, emailaddress, enabled
                FROM JIUser]]>
                    </queryString>
                    <field name="username" class="java.lang.String"/>
                    <field name="emailaddress" class="java.lang.String"/>
                    <field name="enabled" class="java.lang.Boolean"/>
                    <background>
                        <band splitType="Stretch"/>
                    </background>
                    <columnHeader>
                        <band height="30" splitType="Stretch">
                            <staticText>
                                <reportElement x="0" y="0" width="185" height="30" uuid="6cfc8208-2b71-463e-8944-a73875b52ead"/>
                                <text><![CDATA[username]]></text>
                            </staticText>
                            <staticText>
                                <reportElement x="185" y="0" width="185" height="30" uuid="6c09c2dc-d1d1-4ff9-a541-cb0ff843cf3b"/>
                                <text><![CDATA[emailaddress]]></text>
                            </staticText>
                            <staticText>
                                <reportElement x="370" y="0" width="185" height="30" uuid="74597519-666a-45ea-8a32-099b99a22f5c"/>
                                <text><![CDATA[enabled]]></text>
                            </staticText>
                        </band>
                    </columnHeader>
                    <detail>
                        <band height="30" splitType="Stretch">
                            <textField>
                                <reportElement x="0" y="0" width="185" height="30" uuid="b64e155b-7c45-43d7-aac3-947c869e944f"/>
                                <textFieldExpression><![CDATA[$F{username}]]></textFieldExpression>
                            </textField>
                            <textField>
                                <reportElement x="185" y="0" width="185" height="30" uuid="9e423632-22f0-4231-a620-40942a544c21"/>
                                <textFieldExpression><![CDATA[$F{emailaddress}]]></textFieldExpression>
                            </textField>
                            <textField>
                                <reportElement x="370" y="0" width="185" height="30" uuid="e0be8cf1-723b-481e-9174-0b7061331352"/>
                                <textFieldExpression><![CDATA[$F{enabled}]]></textFieldExpression>
                            </textField>
                        </band>
                    </detail>
                </jasperReport>
                """;
    }

    private String createStudioLegacyUsersReport() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <!-- Created with Jaspersoft Studio version 8.3.0.20250730-1008 using JasperReports Library version 6.20.3-415f9428cffdb6805c6f85bbb29ebaf18813a2ab  -->
                <jasperReport xmlns="http://jasperreports.sourceforge.net/jasperreports" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://jasperreports.sourceforge.net/jasperreports http://jasperreports.sourceforge.net/xsd/jasperreport.xsd" name="jasperserver-users" pageWidth="595" pageHeight="842" columnWidth="555" leftMargin="20" rightMargin="20" topMargin="20" bottomMargin="20" uuid="3431f537-52df-4829-9709-aa673c21d48a">
                	<property name="com.jaspersoft.studio.data.defaultdataadapter" value="PG jasperserverdev"/>
                	<property name="com.jaspersoft.studio.data.sql.tables" value=""/>
                	<queryString>
                		<![CDATA[SELECT
                username, emailaddress, enabled
                FROM JIUser]]>
                	</queryString>
                	<field name="username" class="java.lang.String">
                		<property name="com.jaspersoft.studio.field.name" value="username"/>
                		<property name="com.jaspersoft.studio.field.label" value="username"/>
                		<property name="com.jaspersoft.studio.field.tree.path" value="jiuser"/>
                	</field>
                	<field name="emailaddress" class="java.lang.String">
                		<property name="com.jaspersoft.studio.field.name" value="emailaddress"/>
                		<property name="com.jaspersoft.studio.field.label" value="emailaddress"/>
                		<property name="com.jaspersoft.studio.field.tree.path" value="jiuser"/>
                	</field>
                	<field name="enabled" class="java.lang.Boolean">
                		<property name="com.jaspersoft.studio.field.name" value="enabled"/>
                		<property name="com.jaspersoft.studio.field.label" value="enabled"/>
                		<property name="com.jaspersoft.studio.field.tree.path" value="jiuser"/>
                	</field>
                	<background>
                		<band splitType="Stretch"/>
                	</background>
                	<columnHeader>
                		<band height="30" splitType="Stretch">
                			<staticText>
                				<reportElement x="0" y="0" width="185" height="30" uuid="6cfc8208-2b71-463e-8944-a73875b52ead">
                					<property name="com.jaspersoft.studio.spreadsheet.connectionID" value="a718e357-7c3f-4291-9e84-3225e3d28e17"/>
                				</reportElement>
                				<text><![CDATA[username]]></text>
                			</staticText>
                			<staticText>
                				<reportElement x="185" y="0" width="185" height="30" uuid="6c09c2dc-d1d1-4ff9-a541-cb0ff843cf3b">
                					<property name="com.jaspersoft.studio.spreadsheet.connectionID" value="608dde3a-4215-4552-9b10-b6b31d5ede89"/>
                				</reportElement>
                				<text><![CDATA[emailaddress]]></text>
                			</staticText>
                			<staticText>
                				<reportElement x="370" y="0" width="185" height="30" uuid="74597519-666a-45ea-8a32-099b99a22f5c">
                					<property name="com.jaspersoft.studio.spreadsheet.connectionID" value="10637d48-d96b-411e-9470-890ad25778e5"/>
                				</reportElement>
                				<text><![CDATA[enabled]]></text>
                			</staticText>
                		</band>
                	</columnHeader>
                	<detail>
                		<band height="30" splitType="Stretch">
                			<textField>
                				<reportElement x="0" y="0" width="185" height="30" uuid="b64e155b-7c45-43d7-aac3-947c869e944f">
                					<property name="com.jaspersoft.studio.spreadsheet.connectionID" value="a718e357-7c3f-4291-9e84-3225e3d28e17"/>
                				</reportElement>
                				<textFieldExpression><![CDATA[$F{username}]]></textFieldExpression>
                			</textField>
                			<textField>
                				<reportElement x="185" y="0" width="185" height="30" uuid="9e423632-22f0-4231-a620-40942a544c21">
                					<property name="com.jaspersoft.studio.spreadsheet.connectionID" value="608dde3a-4215-4552-9b10-b6b31d5ede89"/>
                				</reportElement>
                				<textFieldExpression><![CDATA[$F{emailaddress}]]></textFieldExpression>
                			</textField>
                			<textField>
                				<reportElement x="370" y="0" width="185" height="30" uuid="e0be8cf1-723b-481e-9174-0b7061331352">
                					<property name="com.jaspersoft.studio.spreadsheet.connectionID" value="10637d48-d96b-411e-9470-890ad25778e5"/>
                				</reportElement>
                				<textFieldExpression><![CDATA[$F{enabled}]]></textFieldExpression>
                			</textField>
                		</band>
                	</detail>
                </jasperReport>
                """;
    }
}

