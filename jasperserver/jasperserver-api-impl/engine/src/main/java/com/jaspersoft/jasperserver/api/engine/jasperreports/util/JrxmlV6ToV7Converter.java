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

import com.fasterxml.jackson.annotation.JsonTypeName;
import net.sf.jasperreports.charts.JRCategoryDataset;
import net.sf.jasperreports.charts.JRGanttDataset;
import net.sf.jasperreports.charts.JRHighLowDataset;
import net.sf.jasperreports.charts.JRPieDataset;
import net.sf.jasperreports.charts.JRTimePeriodDataset;
import net.sf.jasperreports.charts.JRTimeSeriesDataset;
import net.sf.jasperreports.charts.JRValueDataset;
import net.sf.jasperreports.charts.JRXyDataset;
import net.sf.jasperreports.charts.JRXyzDataset;
import net.sf.jasperreports.charts.type.ChartTypeEnum;
import net.sf.jasperreports.engine.xml.JRXmlConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DOM-based converter that rewrites legacy JRXML v6 (JasperReports 6.x)
 * into v7-compatible XML before the modern {@code JRXmlLoader} parses it.
 */
public final class JrxmlV6ToV7Converter {

    private static final Log log = LogFactory.getLog(JrxmlV6ToV7Converter.class);
    private static final String LEGACY_REPORT_NAMESPACE = "http://jasperreports.sourceforge.net/jasperreports";

    // Frequently used XML tag / attribute name constants
    private static final String TAG_ELEMENT = "element";
    private static final String TAG_COMPONENT = "component";
    private static final String TAG_COLUMN = "column";
    private static final String TAG_QUERY = "query";
    private static final String TAG_PARAMETER = "parameter";
    private static final String TAG_DESCRIPTION = "description";
    private static final String TAG_REPORT_ELEMENT = "reportElement";
    private static final String TAG_TEXT_FIELD = "textField";
    private static final String TAG_CELL_CONTENTS = "cellContents";
    private static final String TAG_GROUP = "group";
    private static final String TAG_GROUP_HEADER = "groupHeader";
    private static final String TAG_GROUP_FOOTER = "groupFooter";
    private static final String TAG_COLUMN_HEADER = "columnHeader";
    private static final String TAG_COLUMN_FOOTER = "columnFooter";
    private static final String TAG_DETAIL = "detail";
    private static final String TAG_DETAIL_CELL = "detailCell";
    private static final String TAG_EXPRESSION = "expression";
    private static final String TAG_PARAGRAPH = "paragraph";
    private static final String TAG_STYLE = "style";
    private static final String TAG_CHART = "chart";
    private static final String TAG_PLOT = "plot";
    private static final String TAG_DATASET = "dataset";
    private static final String TAG_BARBECUE = "barbecue";
    private static final String TAG_CODEEXPRESSION = "codeExpression";
    private static final String TAG_CONNECTIONEXPRESSION = "connectionExpression";
    private static final String TAG_SERIESEXPRESSION = "seriesExpression";
    private static final String TAG_CATEGORYEXPRESSION = "categoryExpression";
    private static final String TAG_VALUEEXPRESSION = "valueExpression";
    private static final String TAG_TITLEEXPRESSION = "titleExpression";
    private static final String TAG_SUBTITLEEXPRESSION = "subtitleExpression";
    private static final String TAG_LEGENDEXPRESSION = "legendExpression";

    private static final String ATTR_FONT_SIZE = "fontSize";
    private static final String ATTR_STRETCH_TYPE = "stretchType";
    private static final String ATTR_LANGUAGE = "language";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_KIND = "kind";
    private static final String ATTR_SCHEMA_LOCATION = "schemaLocation";
    private static final String ATTR_XMLNS = "xmlns";
    private static final String PREFIX_XMLNS = "xmlns";
    private static final String PREFIX_XSI = "xsi";

    private static final Pattern IS_BOOLEAN_PATTERN = Pattern.compile("^is([A-Z])");

    /**
     * v6 boolean attributes removed in v7 that need a different attribute
     * name and/or value (rather than just stripping the {@code is} prefix).
     * Value is {@code [newAttrName, valueWhenTrue, valueWhenFalse]};
     * a {@code null} value entry means "remove the attribute".
     */
    private static final Map<String, String[]> OBSOLETE_BOOLEAN_CONVERSIONS = new LinkedHashMap<>();
    static {
        // isStretchWithOverflow="true" → textAdjust="StretchHeight"; "false" → remove
        OBSOLETE_BOOLEAN_CONVERSIONS.put("isStretchWithOverflow",
                new String[]{"textAdjust", "StretchHeight", null});
    }

    private static final Set<String> ELEMENT_KINDS = new LinkedHashSet<>(Arrays.asList(
            TAG_TEXT_FIELD, "staticText", "subreport", "line",
            "componentElement", "break", "image", "frame", "crosstab",
            "rectangle", "ellipse"
    ));

    private static final Set<String> CDATA_ELEMENTS = new LinkedHashSet<>(Arrays.asList(
            TAG_EXPRESSION, "anchorNameExpression", "text", TAG_DESCRIPTION, TAG_QUERY, TAG_CODEEXPRESSION,
            TAG_CONNECTIONEXPRESSION, TAG_TITLEEXPRESSION, TAG_SUBTITLEEXPRESSION, TAG_LEGENDEXPRESSION,
            TAG_SERIESEXPRESSION, TAG_CATEGORYEXPRESSION, TAG_VALUEEXPRESSION
    ));

    /**
     * v6 element tags that may contain a {@code <graphicElement>} wrapper
     * which must be flattened in v7.
     */
    private static final Set<String> GRAPHIC_ELEMENT_KINDS = new LinkedHashSet<>(Arrays.asList(
            "line", "rectangle", "ellipse"
    ));

    private static final Set<String> EXPRESSION_RENAMES = new LinkedHashSet<>(Arrays.asList(
            "groupExpression", "bucketExpression", "variableExpression",
            "datasetParameterExpression", "measureExpression", "imageExpression",
            "textFieldExpression",
            "hyperlinkParameterExpression"
    ));

    private static final Set<String> BAND_PARENTS = new LinkedHashSet<>(Arrays.asList(
            "background", "title", "pageHeader", TAG_COLUMN_HEADER,
            TAG_DETAIL, TAG_COLUMN_FOOTER, "pageFooter", "lastPageFooter",
            "summary", "noData", TAG_GROUP_HEADER, TAG_GROUP_FOOTER
    ));

    /**
     * Section parents backing {@code JRDesignSection} in JR 7. They contain
     * multiple {@code <band>} children and must NOT be unwrapped.
     */
    private static final Set<String> SECTION_PARENTS = new LinkedHashSet<>(Arrays.asList(
            TAG_DETAIL, TAG_GROUP_HEADER, TAG_GROUP_FOOTER
    ));

    private static final Set<String> STUDIO_PROPERTY_PREFIXES = new LinkedHashSet<>(Arrays.asList(
            "com.jaspersoft.studio.unit."
    ));

    private static final Set<String> STUDIO_PROPERTY_NAMES = new LinkedHashSet<>(Arrays.asList(
            "com.jaspersoft.studio.data.defaultdataadapter",
            "com.jaspersoft.studio.layout",
            "com.jaspersoft.studio.components.table.model.column.name"
    ));

    /**
     * Child sort order inside converted {@code <element>} nodes; lower comes
     * first. Unknown children get position 50 (between property and box).
     */
    private static final Map<String, Integer> ELEMENT_CHILD_ORDER = new LinkedHashMap<>();
    static {
        ELEMENT_CHILD_ORDER.put(TAG_PARAGRAPH, 10);
        ELEMENT_CHILD_ORDER.put("text", 20);
        ELEMENT_CHILD_ORDER.put(TAG_EXPRESSION, 30);
        ELEMENT_CHILD_ORDER.put("anchorNameExpression", 40);
        ELEMENT_CHILD_ORDER.put("property", 45);
        ELEMENT_CHILD_ORDER.put("box", 60);
        ELEMENT_CHILD_ORDER.put("hyperlinkParameter", 70);
    }

    /**
     * v6 attributes that lived on {@code <textElement>} but in v7 belong on a
     * {@code <paragraph>} child (rather than the new {@code <element/>}).
     */
    private static final Set<String> PARAGRAPH_ATTRIBUTES = new LinkedHashSet<>(Arrays.asList(
            "lineSpacing", "lineSpacingSize", "firstLineIndent",
            "leftIndent", "rightIndent", "spacingBefore", "spacingAfter",
            "tabStopWidth"
    ));

    // these two were defined in their respective element digester factory classes, which
    // have been removed
    private static final String JRXmlConstants_ELEMENT_meterPlot = "meterPlot";
    private static final String JRXmlConstants_ELEMENT_thermometerPlot = "thermometerPlot";
    private static final String JRXmlConstants_ELEMENT_spiderPlot = "spiderPlot";
    private static final String JRXmlConstants_ELEMENT_spiderDataset = "spiderDataset";
    
    

    private static final LinkedHashMap<String, ChartTypeEnum> CHART_ELEMENT_TYPES = new LinkedHashMap<>();
    static {
        CHART_ELEMENT_TYPES.put(JRXmlConstants.ELEMENT_pieChart, ChartTypeEnum.PIE);
        CHART_ELEMENT_TYPES.put(JRXmlConstants.ELEMENT_pie3DChart, ChartTypeEnum.PIE3D);
        CHART_ELEMENT_TYPES.put(JRXmlConstants.ELEMENT_barChart, ChartTypeEnum.BAR);
        CHART_ELEMENT_TYPES.put(JRXmlConstants.ELEMENT_bar3DChart, ChartTypeEnum.BAR3D);
        CHART_ELEMENT_TYPES.put(JRXmlConstants.ELEMENT_bubbleChart, ChartTypeEnum.BUBBLE);
        CHART_ELEMENT_TYPES.put(JRXmlConstants.ELEMENT_stackedBarChart, ChartTypeEnum.STACKEDBAR);
        CHART_ELEMENT_TYPES.put(JRXmlConstants.ELEMENT_stackedBar3DChart, ChartTypeEnum.STACKEDBAR3D);
        CHART_ELEMENT_TYPES.put(JRXmlConstants.ELEMENT_lineChart, ChartTypeEnum.LINE);
        CHART_ELEMENT_TYPES.put(JRXmlConstants.ELEMENT_highLowChart, ChartTypeEnum.HIGHLOW);
        CHART_ELEMENT_TYPES.put(JRXmlConstants.ELEMENT_candlestickChart, ChartTypeEnum.CANDLESTICK);
        CHART_ELEMENT_TYPES.put(JRXmlConstants.ELEMENT_areaChart, ChartTypeEnum.AREA);
        CHART_ELEMENT_TYPES.put(JRXmlConstants.ELEMENT_scatterChart, ChartTypeEnum.SCATTER);
        CHART_ELEMENT_TYPES.put(JRXmlConstants.ELEMENT_timeSeriesChart, ChartTypeEnum.TIMESERIES);
        CHART_ELEMENT_TYPES.put(JRXmlConstants.ELEMENT_xyAreaChart, ChartTypeEnum.XYAREA);
        CHART_ELEMENT_TYPES.put(JRXmlConstants.ELEMENT_xyBarChart, ChartTypeEnum.XYBAR);
        CHART_ELEMENT_TYPES.put(JRXmlConstants.ELEMENT_xyLineChart, ChartTypeEnum.XYLINE);
        CHART_ELEMENT_TYPES.put(JRXmlConstants.ELEMENT_meterChart, ChartTypeEnum.METER);
        CHART_ELEMENT_TYPES.put(JRXmlConstants.ELEMENT_thermometerChart, ChartTypeEnum.THERMOMETER);
        CHART_ELEMENT_TYPES.put(JRXmlConstants.ELEMENT_multiAxisChart, ChartTypeEnum.MULTI_AXIS);
        CHART_ELEMENT_TYPES.put(JRXmlConstants.ELEMENT_stackedAreaChart, ChartTypeEnum.STACKEDAREA);
        CHART_ELEMENT_TYPES.put(JRXmlConstants.ELEMENT_ganttChart, ChartTypeEnum.GANTT);
    }

    private static final Set<String> CHART_LABELING_ELEMENT_TYPES = new LinkedHashSet<>(Arrays.asList(
    		JRXmlConstants.ELEMENT_chartTitle,
			JRXmlConstants.ELEMENT_chartSubtitle,
			JRXmlConstants.ELEMENT_chartLegend
	));

    private static final Set<String> SPIDER_CHART_LABELING_SUBELEMENT_TYPES_TO_ATTACH_PREFIX = new LinkedHashSet<>(Arrays.asList(
    		"font"
	));

    private static final Set<String> SPIDER_CHART_LABELING_ATTR_TYPES_TO_ATTACH_PREFIX = new LinkedHashSet<>(Arrays.asList(
    		"position", "color", "textColor", "backgroundColor"
	));

    private static final Set<String> CHART_PLOT_ELEMENT_TYPES = new LinkedHashSet<>(Arrays.asList(
        JRXmlConstants.ELEMENT_piePlot,
        JRXmlConstants.ELEMENT_pie3DPlot,
        JRXmlConstants.ELEMENT_barPlot,
        JRXmlConstants.ELEMENT_bar3DPlot,
        JRXmlConstants.ELEMENT_bubblePlot,
        JRXmlConstants.ELEMENT_linePlot,
        JRXmlConstants.ELEMENT_highLowPlot,
        JRXmlConstants.ELEMENT_candlestickPlot,
        JRXmlConstants.ELEMENT_areaPlot,
        JRXmlConstants.ELEMENT_scatterPlot,
        JRXmlConstants.ELEMENT_timeSeriesPlot,
        JRXmlConstants_ELEMENT_meterPlot,
        JRXmlConstants_ELEMENT_thermometerPlot,
        JRXmlConstants.ELEMENT_multiAxisPlot,
        JRXmlConstants_ELEMENT_spiderPlot
    ));
    private static final LinkedHashMap<String, String> CHART_DATASET_ELEMENT_TYPES = new LinkedHashMap<>();
    static {
		CHART_DATASET_ELEMENT_TYPES.put(
				JRXmlConstants.ELEMENT_categoryDataset,
				((JsonTypeName) JRCategoryDataset.class.getAnnotation(JsonTypeName.class)).value());
		CHART_DATASET_ELEMENT_TYPES.put(
				JRXmlConstants.ELEMENT_timeSeriesDataset,
				((JsonTypeName) JRTimeSeriesDataset.class.getAnnotation(JsonTypeName.class)).value());
		CHART_DATASET_ELEMENT_TYPES.put(
				JRXmlConstants.ELEMENT_timePeriodDataset,
				((JsonTypeName) JRTimePeriodDataset.class.getAnnotation(JsonTypeName.class)).value());
		CHART_DATASET_ELEMENT_TYPES.put(
				JRXmlConstants.ELEMENT_xyzDataset,
				((JsonTypeName) JRXyzDataset.class.getAnnotation(JsonTypeName.class)).value());
		CHART_DATASET_ELEMENT_TYPES.put(
				JRXmlConstants.ELEMENT_xyDataset,
				((JsonTypeName) JRXyDataset.class.getAnnotation(JsonTypeName.class)).value());
		CHART_DATASET_ELEMENT_TYPES.put(
				JRXmlConstants.ELEMENT_pieDataset,
				((JsonTypeName) JRPieDataset.class.getAnnotation(JsonTypeName.class)).value());
		CHART_DATASET_ELEMENT_TYPES.put(
				JRXmlConstants.ELEMENT_valueDataset,
				((JsonTypeName) JRValueDataset.class.getAnnotation(JsonTypeName.class)).value());
		CHART_DATASET_ELEMENT_TYPES.put(
				JRXmlConstants.ELEMENT_highLowDataset,
				((JsonTypeName) JRHighLowDataset.class.getAnnotation(JsonTypeName.class)).value());
		CHART_DATASET_ELEMENT_TYPES.put(
				JRXmlConstants.ELEMENT_ganttDataset,
				((JsonTypeName) JRGanttDataset.class.getAnnotation(JsonTypeName.class)).value());
		CHART_DATASET_ELEMENT_TYPES.put(JRXmlConstants_ELEMENT_spiderDataset, "");
    }

    private static final Set<String> CHART_DATASET_SERIES_ELEMENT_TYPES = new LinkedHashSet<>(Arrays.asList(
    		JRXmlConstants.ELEMENT_pieSeries,
    		JRXmlConstants.ELEMENT_categorySeries,
    		JRXmlConstants.ELEMENT_xyzSeries,
    		JRXmlConstants.ELEMENT_xySeries,
    		JRXmlConstants.ELEMENT_timeSeries,
    		JRXmlConstants.ELEMENT_timePeriodSeries,
    		JRXmlConstants.ELEMENT_ganttSeries
	));
    private static final Set<String> CHART_PLOT_AXIS_FORMAT_ELEMENT_TYPES = new LinkedHashSet<>(Arrays.asList(
			JRXmlConstants.ELEMENT_categoryAxisFormat,
			JRXmlConstants.ELEMENT_valueAxisFormat,
			JRXmlConstants.ELEMENT_timeAxisFormat,
			JRXmlConstants.ELEMENT_xAxisFormat,
			JRXmlConstants.ELEMENT_yAxisFormat
	));

    private JrxmlV6ToV7Converter() { }

    /**
     * Convert legacy JRXML v6 bytes into v7-compatible bytes.
     *
     * @param legacyData raw v6 JRXML content (UTF-8)
     * @return converted v7 JRXML bytes
     */
    public static byte[] convert(byte[] legacyData)
            throws ParserConfigurationException, SAXException, IOException, TransformerException {
        Document doc = parseSecure(legacyData);

        Element root = doc.getDocumentElement();
        if (root == null || !"jasperReport".equals(root.getLocalName())) {
            log.warn("Root element is not <jasperReport>; returning data unchanged");
            return legacyData;
        }

        convertRootElement(root);
        convertQueryString(doc);
        convertStyles(doc);
        convertParameterDescriptions(doc);
        convertFieldDescriptions(doc);
        convertSubreportParameters(doc);
        convertLabelAndIcon(doc);
        convertElements(doc);
        convertCrosstab(doc);
        convertCharts(doc);
        convertDataset(doc);
        renameExpressions(doc);
        convertGroups(doc);
        convertDatasetParameters(doc);
        unwrapBands(doc);
        cleanBands(doc);
        convertComponents(doc);
        // Second pass for expressions that may have been inside namespaced
        // component subtrees (e.g. jr:table) not yet renamed at first pass
        renameExpressions(doc);
        cleanProperties(doc);
        renameBooleanPrefixAttributes(doc.getDocumentElement());
        renameAlignmentAttributes(doc.getDocumentElement());
        renameGeneralAttributes(doc.getDocumentElement());
        remapStretchTypeValues(doc.getDocumentElement());
        cleanEmptyTextNodes(doc);
        stripNamespaces(doc);
        ensureAllCDATA(doc);
        propagateStyleToBox(doc);
        stripComments(doc);
        updateStudioVersion(doc);

        return serialize(doc);
    }

    /**
     * Convert legacy JRTX v6 bytes into v7-compatible bytes.
     *
     * @param legacyData raw v6 JRTX content (UTF-8)
     * @return converted v7 JRTX bytes
     */
    public static byte[] convertJrtx(byte[] legacyData)
            throws ParserConfigurationException, SAXException, IOException, TransformerException {
        Document doc = parseSecure(legacyData);

        Element root = doc.getDocumentElement();
        if (root == null || !"jasperTemplate".equals(root.getLocalName())) {
            log.warn("Root element is not <jasperTemplate>; returning data unchanged");
            return legacyData;
        }

        convertJrtxRootElement(root);
        convertStyles(doc);
        stripNamespaces(doc);
        stripComments(doc);

        return serialize(doc);
    }

    // ─── Root element ────────────────────────────────────────────

    private static void convertRootElement(Element root) {
        String name = root.getAttribute("name");
        if (name.isEmpty()) {
            name = "report";
        }

        // Collect non-removed attributes
        List<Attr> keep = new ArrayList<>();
        Set<String> remove = new HashSet<>(Arrays.asList(
                ATTR_XMLNS, ATTR_SCHEMA_LOCATION, ATTR_LANGUAGE, ATTR_NAME
        ));
        NamedNodeMap attrs = root.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr a = (Attr) attrs.item(i);
            if (shouldKeepRootAttribute(a, remove)) {
                keep.add(a);
            }
        }

        // Clear all attributes
        while (root.getAttributes().getLength() > 0) {
            root.removeAttributeNode((Attr) root.getAttributes().item(0));
        }

        // Set name and language first
        root.setAttribute("name", name);
        root.setAttribute(ATTR_LANGUAGE, "java");

        // Restore kept attributes
        for (Attr a : keep) {
            root.setAttribute(a.getName(), a.getValue());
        }
    }

    private static boolean shouldKeepRootAttribute(Attr a, Set<String> dropNames) {
        String localName = a.getLocalName() != null ? a.getLocalName() : a.getName();
        if (dropNames.contains(localName)) {
            return false;
        }
        String prefix = a.getPrefix();
        return !PREFIX_XMLNS.equals(prefix)
                && !PREFIX_XSI.equals(prefix);
    }

    private static void convertJrtxRootElement(Element root) {
        // Collect non-removed attributes
        List<Attr> keep = new ArrayList<>();
        Set<String> remove = new HashSet<>(Arrays.asList(
                ATTR_XMLNS, ATTR_SCHEMA_LOCATION
        ));
        NamedNodeMap attrs = root.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr a = (Attr) attrs.item(i);
            if (shouldKeepRootAttribute(a, remove)) {
                keep.add(a);
            }
        }

        // Clear all attributes
        while (root.getAttributes().getLength() > 0) {
            root.removeAttributeNode((Attr) root.getAttributes().item(0));
        }

        // Restore kept attributes
        for (Attr a : keep) {
            root.setAttribute(a.getName(), a.getValue());
        }
    }

    // ─── queryString → query ──────────────────────────────────────

    private static void convertQueryString(Document doc) {
        for (Element el : findElements(doc.getDocumentElement(), "queryString")) {
            Element query = renameElement(doc, el, TAG_QUERY);
            if (!query.hasAttribute(ATTR_LANGUAGE) || query.getAttribute(ATTR_LANGUAGE).trim().isEmpty()) {
                query.setAttribute(ATTR_LANGUAGE, "sql");
            }
        }
    }

    // ─── Style fontSize normalisation ────────────────────────────

    private static void convertStyles(Document doc) {
        for (Element style : findElements(doc.getDocumentElement(), TAG_STYLE)) {
            renameBooleanPrefixOnElement(style);
            normalizeFontSize(style);
            migrateParagraphAttributes(doc, style);
        }
    }

    private static void normalizeFontSize(Element el) {
        if (!el.hasAttribute(ATTR_FONT_SIZE)) {
            return;
        }
        String val = el.getAttribute(ATTR_FONT_SIZE);
        try {
            double d = Double.parseDouble(val);
            el.setAttribute(ATTR_FONT_SIZE, String.format(Locale.ROOT, "%.1f", d));
        } catch (NumberFormatException ignored) {
            // leave the original value as-is
        }
    }

    /**
     * Move v6 paragraph-style attributes (lineSpacing, firstLineIndent, ...)
     * from the parent element onto a child {@code <paragraph>}.
     */
    private static void migrateParagraphAttributes(Document doc, Element parent) {
        Element paragraph = null;
        for (String attrName : PARAGRAPH_ATTRIBUTES) {
            if (!parent.hasAttribute(attrName)) {
                continue;
            }
            String val = parent.getAttribute(attrName);
            parent.removeAttribute(attrName);
            if (paragraph == null) {
                paragraph = getOrCreateChild(doc, parent, TAG_PARAGRAPH);
            }
            paragraph.setAttribute(attrName, val);
        }
    }

    private static Element getOrCreateChild(Document doc, Element parent, String name) {
        Element child = getChildElement(parent, name);
        if (child == null) {
            child = doc.createElement(name);
            parent.appendChild(child);
        }
        return child;
    }

    // ─── parameterDescription → description ─────────────────────

    private static void convertParameterDescriptions(Document doc) {
        for (Element el : findElements(doc.getDocumentElement(), "parameterDescription")) {
            renameElement(doc, el, TAG_DESCRIPTION);
        }
    }

    // ─── fieldDescription → description ─────────────────────────

    private static void convertFieldDescriptions(Document doc) {
        for (Element el : findElements(doc.getDocumentElement(), "fieldDescription")) {
            renameElement(doc, el, TAG_DESCRIPTION);
        }
    }

    // ─── subreportParameter → parameter ──────────────────────────

    private static void convertSubreportParameters(Document doc) {
        for (Element el : findElements(doc.getDocumentElement(), "subreportParameter")) {
            Element renamed = renameElement(doc, el, TAG_PARAMETER);
            Element inner = getChildElement(renamed, "subreportParameterExpression");
            if (inner != null) {
                renameElement(doc, inner, TAG_EXPRESSION);
            }
        }
    }

    // ─── c:label → labelTextField, c:icon → iconTextField ────────

    private static void convertLabelAndIcon(Document doc) {
        for (Element label : findElementsNS(doc, "label")) {
            convertLabel(doc, label);
        }
        for (Element icon : findElementsNS(doc, "icon")) {
            convertIcon(doc, icon);
        }
    }

    private static void convertLabel(Document doc, Element label) {
        Element renamed = renameElement(doc, label, "labelTextField");
        Element textField = getChildElement(renamed, TAG_TEXT_FIELD);
        if (textField != null) {
            moveChildren(textField, renamed);
        }
        Element reportElement = getChildElement(renamed, TAG_REPORT_ELEMENT);
        if (reportElement != null) {
            moveAttributes(reportElement, renamed);
            removeElement(reportElement);
        }
        Element textElement = getChildElement(renamed, "textElement");
        if (textElement != null) {
            copyAttributes(textElement, renamed);
            Element font = getChildElement(textElement, "font");
            if (font != null) {
                copyFontAttributes(font, renamed);
            }
            removeElement(textElement);
        }
        if (textField != null) {
            removeElement(textField);
        }
        renameBooleanPrefixOnElement(renamed);
    }

    private static void convertIcon(Document doc, Element icon) {
        Element renamed = renameElement(doc, icon, "iconTextField");
        Element textField = getChildElement(renamed, TAG_TEXT_FIELD);
        if (textField != null) {
            copyAttributes(textField, renamed);
            moveChildren(textField, renamed);
            removeElement(textField);
        }
        Element reportElement = getChildElement(renamed, TAG_REPORT_ELEMENT);
        if (reportElement != null) {
            copyAttributes(reportElement, renamed);
            removeElement(reportElement);
        }
    }

    // ─── Element flattening (workConvertElements) ────────────────

    private static void convertElements(Document doc) {
        for (String tag : ELEMENT_KINDS) {
            // Re-query each time since DOM is mutated
            for (Element el : findElements(doc.getDocumentElement(), tag)) {
                convertSingleElement(doc, el, tag);
            }
        }
    }

    private static void convertSingleElement(Document doc, Element original, String originalTag) {
        String kind = "componentElement".equals(originalTag) ? TAG_COMPONENT : originalTag;

        // Save original attributes, rename to <element>, set kind first
        List<String[]> savedAttrs = snapshotAttributes(original);
        Element n = renameElement(doc, original, TAG_ELEMENT);
        clearAttributes(n);
        n.setAttribute("kind", kind);

        // Merge reportElement attributes onto the new element
        Element reportElement = getChildElement(n, TAG_REPORT_ELEMENT);
        mergeReportElementAttributes(n, reportElement);

        // Restore original element attributes (override)
        for (String[] kv : savedAttrs) {
            n.setAttribute(kv[0], kv[1]);
        }

        if (reportElement != null) {
            moveChildren(reportElement, n);
        }

        if (GRAPHIC_ELEMENT_KINDS.contains(originalTag)) {
            flattenGraphicElement(n);
        }

        renameChildIfPresent(doc, n, "textFieldExpression", TAG_EXPRESSION);
        renameChildIfPresent(doc, n, "subreportExpression", TAG_EXPRESSION);

        Element textElement = getChildElement(n, "textElement");
        if (textElement != null) {
            flattenTextElement(doc, n, textElement);
            removeElement(textElement);
        }
        if (reportElement != null) {
            removeElement(reportElement);
        }

        // Move box to end
        Element box = getChildElement(n, "box");
        if (box != null) {
            n.removeChild(box);
            n.appendChild(box);
        }

        renameBooleanPrefixOnElement(n);

        // v6 <image> uses hAlign/vAlign; JR 7 expects hImageAlign/vImageAlign.
        if ("image".equals(kind)) {
            renameAttributeIfPresent(n, "hAlign", "hImageAlign");
            renameAttributeIfPresent(n, "vAlign", "vImageAlign");
        }

        // Sort children: paragraph, text, expression, property, box, then everything else
        sortElementChildren(n);
    }

    private static void mergeReportElementAttributes(Element target, Element reportElement) {
        if (reportElement == null) {
            return;
        }
        // uuid first
        String uuid = reportElement.getAttribute("uuid");
        if (!uuid.isEmpty()) {
            target.setAttribute("uuid", uuid);
        }
        NamedNodeMap reAttrs = reportElement.getAttributes();
        for (int i = 0; i < reAttrs.getLength(); i++) {
            Attr a = (Attr) reAttrs.item(i);
            String ln = a.getLocalName() != null ? a.getLocalName() : a.getName();
            if ("uuid".equals(ln) || "evaluationTime".equals(ln)) {
                continue;
            }
            target.setAttribute(ln, a.getValue());
        }
    }

    /**
     * v7 has no graphicElement; merge its attributes onto the parent and
     * hoist its {@code <pen>} child up.
     */
    private static void flattenGraphicElement(Element parent) {
        Element graphic = getChildElement(parent, "graphicElement");
        if (graphic == null) {
            return;
        }
        copyAttributes(graphic, parent);
        Element pen = getChildElement(graphic, "pen");
        if (pen != null) {
            graphic.removeChild(pen);
            parent.appendChild(pen);
        }
        removeElement(graphic);
    }

    private static void renameChildIfPresent(Document doc, Element parent, String childName, String newName) {
        Element child = getChildElement(parent, childName);
        if (child != null) {
            renameElement(doc, child, newName);
        }
    }

    /**
     * Promote v6 {@code <textElement>} attributes/font onto {@code target} and
     * extract paragraph-style attributes into a child {@code <paragraph>}.
     */
    private static void flattenTextElement(Document doc, Element target, Element textElement) {
        Element paragraphFromAttrs = null;
        NamedNodeMap teAttrs = textElement.getAttributes();
        for (int i = 0; i < teAttrs.getLength(); i++) {
            Attr a = (Attr) teAttrs.item(i);
            String ln = a.getLocalName() != null ? a.getLocalName() : a.getName();
            if ("textAlignment".equals(ln)) {
                target.setAttribute("hTextAlign", a.getValue());
            } else if ("verticalAlignment".equals(ln)) {
                target.setAttribute("vTextAlign", a.getValue());
            } else if (PARAGRAPH_ATTRIBUTES.contains(ln)) {
                if (paragraphFromAttrs == null) {
                    paragraphFromAttrs = getOrCreateChild(doc, textElement, TAG_PARAGRAPH);
                }
                paragraphFromAttrs.setAttribute(ln, a.getValue());
            } else {
                target.setAttribute(ln, a.getValue());
            }
        }
        Element font = getChildElement(textElement, "font");
        if (font != null) {
            promoteFontAttributes(target, font);
        }
        // Move paragraph out before the caller removes textElement
        Element paragraph = getChildElement(textElement, TAG_PARAGRAPH);
        if (paragraph != null) {
            textElement.removeChild(paragraph);
            target.appendChild(paragraph);
        }
    }

    private static void promoteFontAttributes(Element target, Element font) {
        NamedNodeMap fontAttrs = font.getAttributes();
        for (int i = 0; i < fontAttrs.getLength(); i++) {
            Attr a = (Attr) fontAttrs.item(i);
            String ln = a.getLocalName() != null ? a.getLocalName() : a.getName();
            // v6 <font size="14"> → v7 fontSize="14.0"
            if ("size".equals(ln)) {
                String val = a.getValue();
                try {
                    double d = Double.parseDouble(val);
                    target.setAttribute(ATTR_FONT_SIZE, String.format(Locale.ROOT, "%.1f", d));
                } catch (NumberFormatException e) {
                    target.setAttribute(ATTR_FONT_SIZE, val);
                }
            } else {
                target.setAttribute(ln, a.getValue());
            }
        }
    }

    private static void renameAttributeIfPresent(Element el, String oldName, String newName) {
        if (el.hasAttribute(oldName)) {
            String val = el.getAttribute(oldName);
            el.removeAttribute(oldName);
            el.setAttribute(newName, val);
        }
    }

    private static void sortElementChildren(Element parent) {
        List<Node> children = new ArrayList<>();
        Node child = parent.getFirstChild();
        while (child != null) {
            children.add(child);
            child = child.getNextSibling();
        }
        if (children.isEmpty()) return;

        children.sort((a, b) -> {
            int ai = indexInOrder(a);
            int bi = indexInOrder(b);
            return Integer.compare(ai, bi);
        });

        for (Node c : children) {
            parent.appendChild(c); // re-appending moves the node
        }
    }

    private static int indexInOrder(Node node) {
        if (node.getNodeType() != Node.ELEMENT_NODE) return 9999;
        String name = node.getLocalName() != null ? node.getLocalName() : node.getNodeName();
        Integer idx = ELEMENT_CHILD_ORDER.get(name);
        return idx != null ? idx : 50; // unknown elements between property and box
    }

    // ─── Band unwrapping ─────────────────────────────────────────

    private static void unwrapBands(Document doc) {
        for (String parentTag : BAND_PARENTS) {
            for (Element parent : findElements(doc.getDocumentElement(), parentTag)) {
                renameBooleanPrefixOnElement(parent);
                Element band = getChildElement(parent, "band");
                if (band == null) {
                    continue;
                }
                if (SECTION_PARENTS.contains(parentTag)) {
                    // Section parents back JRDesignSection in JR 7 and expect
                    // nested <band> children. Just clean the band, do NOT unwrap.
                    renameBooleanPrefixOnElement(band);
                } else {
                    moveAttributes(band, parent);
                    moveChildren(band, parent);
                    removeElement(band);
                }
            }
        }
    }

    private static void cleanBands(Document doc) {
        for (Element band : findElements(doc.getDocumentElement(), "band")) {
            renameBooleanPrefixOnElement(band);
        }
    }

    // ─── Expression renaming ─────────────────────────────────────

    private static void renameExpressions(Document doc) {
        for (String exprTag : EXPRESSION_RENAMES) {
            for (Element expr : findElements(doc.getDocumentElement(), exprTag)) {
                Element renamed = renameElement(doc, expr, TAG_EXPRESSION);
                // Ensure inner text is preserved (CDATA)
                ensureCDATA(doc, renamed);
            }
        }
    }

    // ─── Group boolean cleanup ───────────────────────────────────

    private static void convertGroups(Document doc) {
        for (Element group : findElements(doc.getDocumentElement(), TAG_GROUP)) {
            renameBooleanPrefixOnElement(group);
        }
    }

    // ─── Crosstab conversions ────────────────────────────────────

    private static void convertCrosstab(Document doc) {
        // crosstabRowHeader → header, unwrap cellContents
        for (Element el : findElements(doc.getDocumentElement(), "crosstabRowHeader")) {
            renameBooleanPrefixOnElement(el);
            Element renamed = renameElement(doc, el, "header");
            unwrapChildContents(renamed, TAG_CELL_CONTENTS);
        }
        // crosstabTotalRowHeader → totalHeader
        for (Element el : findElements(doc.getDocumentElement(), "crosstabTotalRowHeader")) {
            renameBooleanPrefixOnElement(el);
            Element renamed = renameElement(doc, el, "totalHeader");
            unwrapChildContents(renamed, TAG_CELL_CONTENTS);
        }
        // crosstabColumnHeader → header
        for (Element el : findElements(doc.getDocumentElement(), "crosstabColumnHeader")) {
            Element renamed = renameElement(doc, el, "header");
            mergeCellContentsAttributes(renamed);
        }
        // crosstabTotalColumnHeader → totalHeader
        for (Element el : findElements(doc.getDocumentElement(), "crosstabTotalColumnHeader")) {
            Element renamed = renameElement(doc, el, "totalHeader");
            mergeCellContentsAttributes(renamed);
        }
        // crosstabCell → cell, cellContents → contents
        for (Element el : findElements(doc.getDocumentElement(), "crosstabCell")) {
            Element renamed = renameElement(doc, el, "cell");
            for (Element cc : getChildElements(renamed, TAG_CELL_CONTENTS)) {
                renameElement(doc, cc, "contents");
            }
        }
    }

    private static void convertCharts(Document doc) {
    	for (String chartTag : CHART_ELEMENT_TYPES.keySet()) {
			for (Element el : findElements(doc.getDocumentElement(), chartTag)) {
				// <xyzChart> -> <element kind="chart" chartType="XYZ">
				Element renamed = renameElement(doc, el, TAG_ELEMENT);
				renamed.setAttribute(ATTR_KIND, "chart");
				renamed.setAttribute("chartType", CHART_ELEMENT_TYPES.get(chartTag).name());
				// move contents up from <chart>, <chart><reportElement>, <chartTitle>, <chartSubtitle>, and <chartLegend>
				unwrapChildContents(renamed, TAG_CHART);
				unwrapChildContents(renamed, TAG_REPORT_ELEMENT);
				for (String labelingTag : CHART_LABELING_ELEMENT_TYPES) {
					unwrapChildContents(renamed, labelingTag);
				}
				convertChartPlot(doc, renamed);
				convertChartDataset(doc, renamed);
			}
		}
    	// spider charts need special treatment
    	for (Element el : findElementsNS(doc.getDocumentElement(), "spiderChart")) {
    		Element renamed = renameElement(doc, el, TAG_COMPONENT);
    		filterNamespaceAttrs(renamed, "");
    		renamed.setAttribute("kind", "spiderChart");
			convertChartPlot(doc, renamed);
			convertChartDataset(doc, renamed);
			Element chartSettings = getChildElement(renamed, "chartSettings");
			for (Element spiderFont : findElements(chartSettings, "font")) {
				renameAttributeIfPresent(spiderFont, "size", ATTR_FONT_SIZE);
			}
			if (chartSettings != null) {
				Element chartTitle = getChildElement(chartSettings, "chartTitle");
				// each of these three has children "font" and "color" so we have to rename those
				// children to "titleFont"/"subtitleFont", etc. before unwrapping their contents
				if (chartTitle != null) {
					addPrefixToChildContents(doc, chartTitle, "title");
					unwrapChildContents(chartSettings, "chartTitle");
				}
				Element chartSubtitle = getChildElement(chartSettings, "chartSubtitle");
				if (chartSubtitle != null) {
					addPrefixToChildContents(doc, chartSubtitle, "subtitle");
					unwrapChildContents(chartSettings, "chartSubtitle");
				}
				Element chartLegend = getChildElement(chartSettings, "chartLegend");
				if (chartLegend != null) {
					renameAttributeIfPresent(chartLegend, "textColor", "color");
					addPrefixToChildContents(doc, chartLegend, "legend");
					unwrapChildContents(chartSettings, "chartLegend");
				}
			}
    	}
    }

    private static void addPrefixToChildContents(Document doc, Element parent, String prefix) {
    	for (String elementName : SPIDER_CHART_LABELING_SUBELEMENT_TYPES_TO_ATTACH_PREFIX) {
			for (Element titleSetting: getChildElements(parent, elementName)) {
				String oldTag = titleSetting.getLocalName();
				String newTag = prefix + oldTag.substring(0, 1).toUpperCase() + oldTag.substring(1);
				renameElement(doc, titleSetting, newTag);
			}
    	}
    	for (String attrName : SPIDER_CHART_LABELING_ATTR_TYPES_TO_ATTACH_PREFIX) {
    		String newAttrName = prefix + attrName.substring(0, 1).toUpperCase() + attrName.substring(1);
			renameAttributeIfPresent(parent, attrName, newAttrName);
		}
    }

    private static void convertChartPlot(Document doc, Element chartElement) {
		Element plot = getChildElementAny(chartElement, CHART_PLOT_ELEMENT_TYPES);
		if (plot == null) {
			return;
		}

		Element plotRenamed = renameElement(doc, plot, TAG_PLOT);
		unwrapChildContents(plotRenamed, TAG_PLOT);
		for (String axisFormatTag : CHART_PLOT_AXIS_FORMAT_ELEMENT_TYPES) {
			Element axisFormat = getChildElement(plotRenamed, axisFormatTag);
			if (axisFormat != null) {
				unwrapChildContents(axisFormat, "axisFormat");
			}
		}
		for (Element seriesColor : getChildElements(plotRenamed, "seriesColor")) {
			renameAttributeIfPresent(seriesColor, "seriesOrder", "order");
		}
    }

    private static void convertChartDataset(Document doc, Element chartElement) {
		Element dataset = getChildElementAny(chartElement, CHART_DATASET_ELEMENT_TYPES.keySet());
		if (dataset == null) {
			return;
		}

		Element datasetRenamed = renameElement(doc, dataset, TAG_DATASET);
		String datasetKind = CHART_DATASET_ELEMENT_TYPES.get(dataset.getLocalName());
		if (!datasetKind.isEmpty()) {
			datasetRenamed.setAttribute(ATTR_KIND, datasetKind);
		}
		unwrapChildContents(datasetRenamed, TAG_DATASET);
		for (Element series : getChildElementsAny(datasetRenamed, CHART_DATASET_SERIES_ELEMENT_TYPES)) {
			series = renameElement(doc, series, "series");
		}
		Element datasetRun = getChildElement(datasetRenamed, "datasetRun");
		if (datasetRun != null) {
			for (Element datasetParameter : getChildElements(datasetRun, "datasetParameter")) {
				renameElement(doc, datasetParameter, TAG_PARAMETER);
			}
		}
	}

    private static void mergeCellContentsAttributes(Element parent) {
        Element cc = getChildElement(parent, TAG_CELL_CONTENTS);
        if (cc != null) {
            moveAttributes(cc, parent);
            removeElement(cc);
        }
    }

    private static void unwrapChildContents(Element parent, String childName) {
        Element cc = getChildElement(parent, childName);
        if (cc != null) {
            moveAttributes(cc, parent);
            moveChildren(cc, parent);
            removeElement(cc);
        }
    }

    // ─── Dataset conversions ─────────────────────────────────────

    private static void convertDataset(Document doc) {
        for (Element el : findElements(doc.getDocumentElement(), "subDataset")) {
            renameElement(doc, el, "dataset");
        }
    }

    private static void convertDatasetParameters(Document doc) {
        for (Element el : findElements(doc.getDocumentElement(), "datasetParameter")) {
            renameElement(doc, el, TAG_PARAMETER);
        }
    }

    // ─── Component conversions ───────────────────────────────────


    private static final String[] BARCODE4J_TAGS = {
            "Code128", "QRCode", "EAN13", "EAN128",
            "DataMatrix", "PDF417", "Code39", "Codabar", "UPCA", "UPCE",
            "Interleaved2Of5", "RoyalMailCustomer", "USPSIntelligentMail", "POSTNET"
    };

    private static void convertComponents(Document doc) {
        convertTableComponent(doc);
        convertColumnComponents(doc);
        convertListComponents(doc);
        convertSimpleNsRenames(doc);
        convertIconLabel(doc);
        convertBarbecue(doc);
        convertBarcode4j(doc);
    }

    private static void convertTableComponent(Document doc) {
        for (Element el : findElementsNS(doc, "table")) {
            Element renamed = renameElement(doc, el, TAG_COMPONENT);
            filterNamespaceAttrs(renamed, "table");
        }
    }

    private static void convertColumnComponents(Document doc) {
        // c:column → <column kind="single">
        for (Element el : findElementsNS(doc, TAG_COLUMN)) {
            Element renamed = renameElement(doc, el, TAG_COLUMN);
            renamed.setAttribute("kind", "single");
            for (Element cf : findElementsNS(renamed, TAG_COLUMN_FOOTER)) {
                renameElement(doc, cf, TAG_COLUMN_FOOTER);
            }
            for (Element dc : findElementsNS(renamed, TAG_DETAIL_CELL)) {
                renameElement(doc, dc, TAG_DETAIL_CELL);
            }
        }
        // c:columnGroup → <column kind="group">
        for (Element el : findElementsNS(doc, "columnGroup")) {
            Element renamed = renameElement(doc, el, TAG_COLUMN);
            renamed.setAttribute("kind", TAG_GROUP);
        }
    }

    private static void convertListComponents(Document doc) {
        // c:list / jr:list → <component kind="list">
        for (Element el : findElementsNS(doc, "list")) {
            Element renamed = renameElement(doc, el, TAG_COMPONENT);
            filterNamespaceAttrs(renamed, "list");
        }
        // c:listContents / jr:listContents → <contents>
        for (Element el : findElementsNS(doc, "listContents")) {
            Element renamed = renameElement(doc, el, "contents");
            filterNamespaceAttrs(renamed, "");
        }
    }

    /** Strip namespace prefixes from elements that keep their name in v7. */
    private static void convertSimpleNsRenames(Document doc) {
        for (Element el : findElementsNS(doc, "tableFooter")) {
            renameElement(doc, el, "tableFooter");
        }
        for (Element el : findElementsNS(doc, TAG_COLUMN_HEADER)) {
            if (el.getPrefix() != null && !el.getPrefix().isEmpty()) {
                renameElement(doc, el, TAG_COLUMN_HEADER);
            }
        }
        for (Element el : findElementsNS(doc, "tableHeader")) {
            renameElement(doc, el, "tableHeader");
        }
    }

    private static void convertIconLabel(Document doc) {
        for (Element el : findElementsNS(doc, "iconLabel")) {
            String saveName = el.getLocalName();
            Element renamed = renameElement(doc, el, TAG_COMPONENT);
            List<String[]> keep = new ArrayList<>();
            NamedNodeMap attrs = renamed.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                Attr a = (Attr) attrs.item(i);
                String prefix = a.getPrefix();
                String ln = a.getLocalName() != null ? a.getLocalName() : a.getName();
                if (PREFIX_XMLNS.equals(prefix) || PREFIX_XSI.equals(prefix)
                        || ATTR_SCHEMA_LOCATION.equals(ln)) {
                    continue;
                }
                keep.add(new String[]{ln, a.getValue()});
            }
            clearAttributes(renamed);
            renamed.setAttribute("kind", saveName);
            for (String[] kv : keep) {
                renamed.setAttribute(kv[0], kv[1]);
            }
        }
    }

    private static void convertBarbecue(Document doc) {
        for (Element el : findElementsNS(doc, TAG_BARBECUE)) {
            Element renamed = renameElement(doc, el, TAG_COMPONENT);
			renamed.setAttribute(ATTR_KIND, TAG_BARBECUE);
            filterNamespaceAttrs(renamed, "");
            for (Element ce : findElementsNS(renamed, TAG_CODEEXPRESSION)) {
            	Element codeExpression = renameElement(doc, ce, TAG_CODEEXPRESSION);
                ensureCDATA(doc, codeExpression);
            }
        }
    }

    private static void convertBarcode4j(Document doc) {
        for (String barcodeTag : BARCODE4J_TAGS) {
            for (Element el : findElementsNS(doc, barcodeTag)) {
                String saveName = el.getLocalName();
                Element renamed = renameElement(doc, el, TAG_COMPONENT);
                filterNamespaceAttrs(renamed, "barcode4j:" + saveName);
                for (Element ce : findElementsNS(renamed, TAG_CODEEXPRESSION)) {
                    Element codeExpression = renameElement(doc, ce, TAG_CODEEXPRESSION);
                    ensureCDATA(doc, codeExpression);
                }
            }
        }
    }

    // ─── Property cleanup ────────────────────────────────────────

    private static void cleanProperties(Document doc) {
        for (Element prop : findElements(doc.getDocumentElement(), "property")) {
            renameBooleanPrefixOnElement(prop);
            String name = prop.getAttribute("name");
            if (name.isEmpty()) {
                continue;
            }
            if (STUDIO_PROPERTY_NAMES.contains(name)
                    || STUDIO_PROPERTY_PREFIXES.stream().anyMatch(name::startsWith)) {
                removeElement(prop);
            }
        }
    }

    // ─── Boolean attribute prefix removal (global) ───────────────

    /** Recursively rename {@code isXxx} attributes to {@code xxx}. */
    private static void renameBooleanPrefixAttributes(Element element) {
        renameBooleanPrefixOnElement(element);
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                renameBooleanPrefixAttributes((Element) child);
            }
        }
    }

    private static void renameBooleanPrefixOnElement(Element element) {
        // First pass: handle obsolete boolean attributes that need special conversion
        for (Map.Entry<String, String[]> entry : OBSOLETE_BOOLEAN_CONVERSIONS.entrySet()) {
            String oldAttrName = entry.getKey();
            if (element.hasAttribute(oldAttrName)) {
                String[] conversion = entry.getValue();
                String value = element.getAttribute(oldAttrName);
                element.removeAttribute(oldAttrName);
                if ("true".equalsIgnoreCase(value) && conversion[1] != null) {
                    element.setAttribute(conversion[0], conversion[1]);
                } else if (!"true".equalsIgnoreCase(value) && conversion[2] != null) {
                    element.setAttribute(conversion[0], conversion[2]);
                }
                // else: attribute is simply removed
            }
        }

        // Second pass: generic is-prefix stripping for remaining boolean attributes
        List<String[]> renames = new ArrayList<>();
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr a = (Attr) attrs.item(i);
            String name = a.getLocalName() != null ? a.getLocalName() : a.getName();
            Matcher m = IS_BOOLEAN_PATTERN.matcher(name);
            if (m.find()) {
                String newName = m.group(1).toLowerCase() + name.substring(3);
                renames.add(new String[]{a.getName(), newName, a.getValue()});
            }
        }
        for (String[] r : renames) {
            element.removeAttribute(r[0]);
            element.setAttribute(r[1], r[2]);
        }
    }

    // ─── Legacy alignment attribute renaming (global) ──────────

    /** v6 attribute name → v7 attribute name mapping for alignment properties. */
    private static final Map<String, String> ALIGNMENT_RENAMES = new LinkedHashMap<>();
    static {
        ALIGNMENT_RENAMES.put("textAlignment", "hTextAlign");
        ALIGNMENT_RENAMES.put("verticalAlignment", "vTextAlign");
    }

    /** v6 attribute name → v7 attribute name mapping for general attribute renames. */
    private static final Map<String, String> ATTRIBUTE_RENAMES = new LinkedHashMap<>();
    static {
        ATTRIBUTE_RENAMES.put("hyperlinkType", "linkType");
        ATTRIBUTE_RENAMES.put("hyperlinkTarget", "linkTarget");
    }

    /** v6 stretchType values that changed in v7. */
    private static final Map<String, String> STRETCH_TYPE_VALUE_RENAMES = new LinkedHashMap<>();
    static {
        STRETCH_TYPE_VALUE_RENAMES.put("RelativeToBandHeight", "ContainerHeight");
        STRETCH_TYPE_VALUE_RENAMES.put("RelativeToTallestObject", "ContainerBottom");
    }

    /** Recursively rename legacy alignment attributes to their v7 equivalents. */
    private static void renameAlignmentAttributes(Element element) {
        renameAlignmentOnElement(element);
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                renameAlignmentAttributes((Element) child);
            }
        }
    }

    private static void renameAlignmentOnElement(Element element) {
        List<String[]> renames = new ArrayList<>();
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr a = (Attr) attrs.item(i);
            String name = a.getLocalName() != null ? a.getLocalName() : a.getName();
            String newName = ALIGNMENT_RENAMES.get(name);
            if (newName != null) {
                renames.add(new String[]{a.getName(), newName, a.getValue()});
            }
        }
        for (String[] r : renames) {
            element.removeAttribute(r[0]);
            element.setAttribute(r[1], r[2]);
        }
    }

    // ─── General attribute renaming (global) ─────────────────────

    /** Recursively rename v6 attributes (e.g. hyperlinkType → linkType) to v7. */
    private static void renameGeneralAttributes(Element element) {
        renameGeneralAttributesOnElement(element);
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                renameGeneralAttributes((Element) child);
            }
        }
    }

    private static void renameGeneralAttributesOnElement(Element element) {
        List<String[]> renames = new ArrayList<>();
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr a = (Attr) attrs.item(i);
            String name = a.getLocalName() != null ? a.getLocalName() : a.getName();
            String newName = ATTRIBUTE_RENAMES.get(name);
            if (newName != null) {
                renames.add(new String[]{a.getName(), newName, a.getValue()});
            }
        }
        for (String[] r : renames) {
            element.removeAttribute(r[0]);
            element.setAttribute(r[1], r[2]);
        }
    }

    // ─── stretchType value remapping (global) ────────────────────

    /** Recursively remap deprecated stretchType values (e.g. RelativeToBandHeight → ContainerHeight). */
    private static void remapStretchTypeValues(Element element) {
        if (element.hasAttribute(ATTR_STRETCH_TYPE)) {
            String val = element.getAttribute(ATTR_STRETCH_TYPE);
            String mapped = STRETCH_TYPE_VALUE_RENAMES.get(val);
            if (mapped != null) {
                element.setAttribute(ATTR_STRETCH_TYPE, mapped);
            }
        }
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                remapStretchTypeValues((Element) child);
            }
        }
    }

    // ─── Empty text node cleanup ─────────────────────────────────

    private static void cleanEmptyTextNodes(Document doc) {
        String[] structuralElements = {
                "band", "jasperReport", TAG_ELEMENT, TAG_STYLE, "field",
                TAG_GROUP, "variable", "box", TAG_GROUP_HEADER, TAG_GROUP_FOOTER,
                "pageFooter", TAG_COLUMN_HEADER, TAG_COLUMN_FOOTER, "pageHeader", TAG_DETAIL,
                TAG_QUERY, "dataset", "cell", "datasetRun", "summary", "title",
                TAG_COLUMN, TAG_COMPONENT, "hyperlinkParameter", TAG_PARAMETER,
                TAG_DETAIL_CELL, "sortField"
        };
        for (String tag : structuralElements) {
            for (Element el : findElements(doc.getDocumentElement(), tag)) {
                removeWhitespaceTextChildren(el);
            }
        }
    }

    private static void removeWhitespaceTextChildren(Element element) {
        List<Node> toRemove = new ArrayList<>();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                String text = child.getTextContent();
                if (text == null || text.isBlank()) {
                    toRemove.add(child);
                }
            }
        }
        for (Node n : toRemove) {
            element.removeChild(n);
        }
    }

    // ─── Namespace stripping ─────────────────────────────────────

    /**
     * Recursively strip XML namespace URIs from all elements. JR 7's
     * {@code JRXmlLoader} expects namespace-less elements.
     */
    private static void stripNamespaces(Document doc) {
        stripElementNamespace(doc, doc.getDocumentElement());
    }

    private static void stripElementNamespace(Document doc, Element element) {
        // Remove namespace from this element
        if (element.getNamespaceURI() != null) {
            doc.renameNode(element, null, element.getLocalName());
        }

        // Remove any xmlns:* attributes that may linger
        List<String> toRemove = new ArrayList<>();
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr a = (Attr) attrs.item(i);
            if (a.getName().startsWith(ATTR_XMLNS)) {
                toRemove.add(a.getName());
            }
        }
        for (String name : toRemove) {
            element.removeAttribute(name);
        }

        // Recurse into children
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                stripElementNamespace(doc, (Element) child);
            }
        }
    }

    // ═══ DOM utility helpers ═════════════════════════════════════

    private static Document parseSecure(byte[] data)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        // XXE protection
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(data));
    }

    private static byte[] serialize(Document doc) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, String.join(" ", CDATA_ELEMENTS));
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
        transformer.transform(new DOMSource(doc), new StreamResult(out));

        // Post-process: fix self-closing query tags that should have empty CDATA
        String result = out.toString(StandardCharsets.UTF_8);
        result = result.replaceAll("<query([^>]*?)/>", "<query$1><![CDATA[]]></query>");
        // Ensure newline between comment and root element
        result = result.replace("--><jasperReport", "-->\n<jasperReport");
        // The JDK Transformer sorts attributes alphabetically; Jackson's
        // polymorphic XML deserializer in JR 7 needs the kind discriminator
        // to appear first on element/column/component tags.
        result = moveKindAttributeFirst(result);
        return result.getBytes(StandardCharsets.UTF_8);
    }

    private static final Set<String> KIND_POLYMORPHIC_TAGS = new LinkedHashSet<>(Arrays.asList(
            TAG_ELEMENT, TAG_COLUMN, TAG_COMPONENT
    ));

    private static final Map<String, Pattern> KIND_TAG_PATTERNS;
    static {
        Map<String, Pattern> m = new LinkedHashMap<>();
        for (String tag : KIND_POLYMORPHIC_TAGS) {
            // Match <tag ...>  or <tag .../>  where attributes may span
            // multiple lines (Transformer indent=yes splits them).
            m.put(tag, Pattern.compile("<" + tag + "(\\s+[^>]*?)(/?)>", Pattern.DOTALL));
        }
        KIND_TAG_PATTERNS = m;
    }

    /**
     * Rewrites every opening tag of a kind-polymorphic element so that its
     * {@code kind="..."} attribute appears before any other attribute. The JDK
     * Transformer sorts attributes alphabetically, but Jackson's polymorphic
     * XML deserializer in JR 7 requires the type-discriminator first.
     */
    static String moveKindAttributeFirst(String xml) {
        String current = xml;
        for (Map.Entry<String, Pattern> entry : KIND_TAG_PATTERNS.entrySet()) {
            String tag = entry.getKey();
            Matcher m = entry.getValue().matcher(current);
            StringBuilder sb = new StringBuilder(current.length());
            int last = 0;
            while (m.find()) {
                sb.append(current, last, m.start());
                String attrs = m.group(1);
                String selfClose = m.group(2);
                sb.append(rewriteAttrsKindFirst(tag, attrs, selfClose));
                last = m.end();
            }
            sb.append(current, last, current.length());
            current = sb.toString();
        }
        return current;
    }

    private static final Pattern ATTR_PATTERN = Pattern.compile(
            "(\\S+?)=\"([^\"]*)\"");

    private static String rewriteAttrsKindFirst(String tag, String attrs, String selfClose) {
        Matcher am = ATTR_PATTERN.matcher(attrs);
        String kindPair = null;
        List<String> others = new ArrayList<>();
        while (am.find()) {
            String name = am.group(1);
            String pair = name + "=\"" + am.group(2) + "\"";
            if ("kind".equals(name) && kindPair == null) {
                kindPair = pair;
            } else {
                others.add(pair);
            }
        }
        if (kindPair == null) {
            // No kind attribute - leave original tag verbatim.
            return "<" + tag + attrs + selfClose + ">";
        }
        StringBuilder out = new StringBuilder();
        out.append('<').append(tag).append(' ').append(kindPair);
        for (String p : others) {
            out.append(' ').append(p);
        }
        out.append(selfClose).append('>');
        return out.toString();
    }

    /**
     * Find all descendant elements with the given local name (no namespace).
     * Returns a snapshot copy so the DOM can be mutated while iterating.
     */
    static List<Element> findElements(Element root, String localName) {
        List<Element> result = new ArrayList<>();
        collectElements(root, localName, result);
        return result;
    }

    private static void collectElements(Node node, String localName, List<Element> result) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) child;
                String ln = el.getLocalName() != null ? el.getLocalName() : el.getNodeName();
                if (localName.equals(ln)) {
                    result.add(el);
                }
                collectElements(el, localName, result);
            }
        }
    }

    /** Find elements by local name regardless of namespace prefix (e.g. c:table, jr:list). */
    private static List<Element> findElementsNS(Node root, String localName) {
        List<Element> result = new ArrayList<>();
        collectElementsNS(root, localName, result);
        return result;
    }

    private static void collectElementsNS(Node node, String localName, List<Element> result) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) child;
                String ln = el.getLocalName() != null ? el.getLocalName() : el.getNodeName();
                String namespaceUri = el.getNamespaceURI();
                if (localName.equals(ln)
                        && namespaceUri != null
                        && !LEGACY_REPORT_NAMESPACE.equals(namespaceUri)) {
                    result.add(el);
                }
                collectElementsNS(el, localName, result);
            }
        }
    }

    private static List<Element> findElementsNS(Element root, String localName) {
        return findElementsNS((Node) root, localName);
    }

    private static Element getChildElement(Element parent, String localName) {
    	return getChildElementAny(parent, Collections.singleton(localName));
    }

    private static Element getChildElementAny(Element parent, Set<String> localName) {
		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				String ln = child.getLocalName() != null ? child.getLocalName() : child.getNodeName();
				if (localName.contains(ln)) {
					return (Element) child;
				}
			}
		}
		return null;
    }

    private static List<Element> getChildElements(Element parent, String localName) {
    	return getChildElementsAny(parent, Collections.singleton(localName));
    }

    private static List<Element> getChildElementsAny(Element parent, Set<String> localName) {
        List<Element> result = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                String ln = child.getLocalName() != null ? child.getLocalName() : child.getNodeName();
                if (localName.contains(ln)) {
                    result.add((Element) child);
                }
            }
        }
        return result;
    }

    /** Rename an element in-place, transplanting all attributes and children. */
    private static Element renameElement(Document doc, Element old, String newName) {
        Element replacement = doc.createElement(newName);

        // Copy attributes
        NamedNodeMap attrs = old.getAttributes();
        while (attrs.getLength() > 0) {
            Attr a = (Attr) attrs.item(0);
            old.removeAttributeNode(a);
            // Drop namespace attributes during rename
            if (!a.getName().startsWith(ATTR_XMLNS)) {
                replacement.setAttribute(a.getLocalName() != null ? a.getLocalName() : a.getName(), a.getValue());
            }
        }

        // Move children
        while (old.hasChildNodes()) {
            replacement.appendChild(old.getFirstChild());
        }

        // Replace in tree
        if (old.getParentNode() != null) {
            old.getParentNode().replaceChild(replacement, old);
        }
        return replacement;
    }

    private static void moveChildren(Element from, Element to) {
        while (from.hasChildNodes()) {
            to.appendChild(from.getFirstChild());
        }
    }

    private static void moveAttributes(Element from, Element to) {
        NamedNodeMap attrs = from.getAttributes();
        List<Attr> list = new ArrayList<>();
        for (int i = 0; i < attrs.getLength(); i++) {
            list.add((Attr) attrs.item(i));
        }
        for (Attr a : list) {
            String name = a.getLocalName() != null ? a.getLocalName() : a.getName();
            to.setAttribute(name, a.getValue());
        }
    }

    private static void copyAttributes(Element from, Element to) {
        NamedNodeMap attrs = from.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr a = (Attr) attrs.item(i);
            String name = a.getLocalName() != null ? a.getLocalName() : a.getName();
            to.setAttribute(name, a.getValue());
        }
    }

    /** Copy font attributes, renaming {@code size} to a decimal {@code fontSize} (e.g. "14" → "14.0"). */
    private static void copyFontAttributes(Element font, Element to) {
        NamedNodeMap attrs = font.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr a = (Attr) attrs.item(i);
            String ln = a.getLocalName() != null ? a.getLocalName() : a.getName();
            if ("size".equals(ln)) {
                String val = a.getValue();
                try {
                    double d = Double.parseDouble(val);
                    to.setAttribute(ATTR_FONT_SIZE, String.format(Locale.ROOT, "%.1f", d));
                } catch (NumberFormatException e) {
                    to.setAttribute(ATTR_FONT_SIZE, val);
                }
            } else {
                to.setAttribute(ln, a.getValue());
            }
        }
    }

    private static void clearAttributes(Element el) {
        while (el.getAttributes().getLength() > 0) {
            el.removeAttributeNode((Attr) el.getAttributes().item(0));
        }
    }

    private static List<String[]> snapshotAttributes(Element el) {
        List<String[]> result = new ArrayList<>();
        NamedNodeMap attrs = el.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr a = (Attr) attrs.item(i);
            String name = a.getLocalName() != null ? a.getLocalName() : a.getName();
            result.add(new String[]{name, a.getValue()});
        }
        return result;
    }

    private static void removeElement(Element el) {
        if (el.getParentNode() != null) {
            el.getParentNode().removeChild(el);
        }
    }

    private static void filterNamespaceAttrs(Element el, String kind) {
        List<String[]> keep = new ArrayList<>();
        NamedNodeMap attrs = el.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr a = (Attr) attrs.item(i);
            String prefix = a.getPrefix();
            String ln = a.getLocalName() != null ? a.getLocalName() : a.getName();
            boolean drop = PREFIX_XMLNS.equals(prefix)
                    || PREFIX_XSI.equals(prefix)
                    || ATTR_SCHEMA_LOCATION.equals(ln)
                    || a.getName().startsWith(PREFIX_XMLNS);
            if (!drop) {
                keep.add(new String[]{ln, a.getValue()});
            }
        }
        clearAttributes(el);
        if (kind != null && !kind.isEmpty()) {
            el.setAttribute("kind", kind);
        }
        for (String[] kv : keep) {
            el.setAttribute(kv[0], kv[1]);
        }
    }

    private static void ensureCDATA(Document doc, Element element) {
        if (hasNonEmptyCDATA(element)) {
            return;
        }
        // Try to convert an existing non-blank text node to CDATA
        if (convertFirstTextNodeToCDATA(doc, element)) {
            return;
        }
        // No meaningful content: drop empty CDATA sections and add an empty
        // text node so CDATA_SECTION_ELEMENTS produces <tag><![CDATA[]]></tag>
        // rather than a self-closing tag.
        removeEmptyCDATASections(element);
        element.appendChild(doc.createTextNode(""));
    }

    private static boolean hasNonEmptyCDATA(Element element) {
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.CDATA_SECTION_NODE
                    && child.getTextContent() != null
                    && !child.getTextContent().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static boolean convertFirstTextNodeToCDATA(Document doc, Element element) {
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                String text = child.getTextContent();
                if (text != null && !text.isBlank()) {
                    element.replaceChild(doc.createCDATASection(text), child);
                    return true;
                }
            }
        }
        return false;
    }

    private static void removeEmptyCDATASections(Element element) {
        List<Node> toRemove = new ArrayList<>();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.CDATA_SECTION_NODE
                    && (child.getTextContent() == null || child.getTextContent().isEmpty())) {
                toRemove.add(child);
            }
        }
        for (Node n : toRemove) {
            element.removeChild(n);
        }
    }
 
    /** Ensure CDATA wrapping on text-content elements that JR 7 expects. */
    private static void ensureAllCDATA(Document doc) {
        for (String tag : CDATA_ELEMENTS) {
            for (Element el : findElements(doc.getDocumentElement(), tag)) {
                ensureCDATA(doc, el);
            }
        }
    }

    /** Copy the parent {@code element}'s {@code style} attribute onto its child {@code <box>}. */
    private static void propagateStyleToBox(Document doc) {
        for (Element el : findElements(doc.getDocumentElement(), TAG_ELEMENT)) {
            String style = el.getAttribute(TAG_STYLE);
            if (!style.isEmpty()) {
                Element box = getChildElement(el, "box");
                if (box != null) {
                    box.setAttribute(TAG_STYLE, style);
                }
            }
        }
    }

    /** Strip XML comment nodes */
    private static void stripComments(Document doc) {
        // Remove all comment nodes that are direct children of the document
        List<Node> toRemove = new ArrayList<>();
        NodeList docChildren = doc.getChildNodes();
        for (int i = 0; i < docChildren.getLength(); i++) {
            Node child = docChildren.item(i);
            if (child.getNodeType() == Node.COMMENT_NODE) {
                toRemove.add(child);
            }
        }
        for (Node n : toRemove) {
            doc.removeChild(n);
        }

    }

    /** insert/update the Jaspersoft Studio version comment. */
    private static void updateStudioVersion(Document doc) {
        // Insert Studio version comment before the root element
        Comment studioComment = doc.createComment(
                " Created with Jaspersoft Studio version 7.0.0.final"
                        + " using JasperReports Library version 7.0.0"
                        + "-b478feaa9aab4375eba71de77b4ca138ad2f62aa  ");
        doc.insertBefore(studioComment, doc.getDocumentElement());
    }
}
