/*
 * Copyright (C) 2025 the Jasper Server OS Authors
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

package com.jaspersoft.jasperserver.remote.exporters;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.engine.common.service.EngineService;
import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.Argument;
import com.jaspersoft.jasperserver.remote.ReportExporter;
import com.jaspersoft.jasperserver.remote.services.ReportOutputPages;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.StyleResolver;
import net.sf.jasperreports.export.ExporterInputItem;
import net.sf.jasperreports.export.SimpleExporterInputItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Parameterized test to verify that exportReport method in all ReportExporter
 * implementations works correctly without any exception.
 */
@RunWith(Parameterized.class)
public class ReportExporterExceptionAvoidanceTest {

    private static final Logger logger = LogManager.getLogger(ReportExporterExceptionAvoidanceTest.class);

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"CsvExporter", CsvExporter.class},
                {"CsvMetadataExporter", CsvMetadataExporter.class},
                {"DocxExporter", DocxExporter.class},
                {"HtmlExporter", HtmlExporter.class},
                {"JsonMetadataExporter", JsonMetadataExporter.class},
                {"OdsExporter", OdsExporter.class},
                {"OdtExporter", OdtExporter.class},
                {"PdfExporter", PdfExporter.class},
                {"PptxExporter", PptxExporter.class},
                {"RtfExporter", RtfExporter.class},
                {"XlsExporter", XlsExporter.class},
                {"XlsxExporter", XlsxExporter.class},
                {"XlsxMetadataExporter", XlsxMetadataExporter.class},
                {"XmlExporter", XmlExporter.class}
        });
    }

    private final Class<? extends ReportExporter> exporterClass;
    private ReportExporter exporter;

    private final JasperReportsContext jasperReportsContext = new SimpleJasperReportsContext();

    @Mock
    private EngineService engineService;

    @Mock
    private ExecutionContext executionContext;

    @Mock
    private JasperPrint jasperPrint;

    @Mock
    private JRDefaultStyleProvider defaultStyleProvider;

    private final StyleResolver styleResolver = StyleResolver.getInstance();

    @Mock
    private JRPropertiesMap propertiesMap;

    private AutoCloseable mocks;

    private final String exporterName;

    public ReportExporterExceptionAvoidanceTest(String name, Class<? extends AbstractExporter> exporterClass) {
        this.exporterName = name;
        this.exporterClass = exporterClass;
    }

    @Before
    public void setUp() throws Exception {
        mocks = MockitoAnnotations.openMocks(this);
        exporter = createExporter();

        // Configure JasperPrint mock with required style providers
        setupJasperPrintMock();

        // Set required dependencies
        ReflectionTestUtils.setField(exporter, "jasperReportsContext", jasperReportsContext);
    }

    @After
    public void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    /**
     * Set up the JasperPrint mock to handle style resolver requests
     */
    private void setupJasperPrintMock() {
        // Basic JasperPrint configuration
        Mockito.when(defaultStyleProvider.getStyleResolver()).thenReturn(styleResolver);
        Mockito.when(jasperPrint.getDefaultStyleProvider()).thenReturn(defaultStyleProvider);

        Mockito.when(jasperPrint.hasProperties()).thenReturn(false);
        Mockito.when(jasperPrint.getPropertiesMap()).thenReturn(propertiesMap);

        Mockito.when(propertiesMap.getPropertyNames()).thenReturn(new String[]{});

        // Fix for XlsExporter: mock getPageFormat to return a valid PrintPageFormat
        PrintPageFormat mockPageFormat = Mockito.mock(PrintPageFormat.class);
        Mockito.when(mockPageFormat.getPageWidth()).thenReturn(595);
        Mockito.when(mockPageFormat.getPageHeight()).thenReturn(842);
        Mockito.when(jasperPrint.getPageFormat()).thenReturn(mockPageFormat);
    }


    private ReportExporter createExporter() throws Exception {
        return exporterClass.getDeclaredConstructor().newInstance();
    }

    /**
     * Verifies that the exportReport method can handle exporter input items without throwing exceptions.
     *
     * <p>Test passes if the export operation completes without throwing any exceptions.
     * The actual output content is not validated in this test.</p>
     */
    @Test
    public void shouldExportReportWithoutExceptions() {
        // Given
        OutputStream outputStream = new ByteArrayOutputStream();
        List<ExporterInputItem> inputItems = Collections.singletonList(new SimpleExporterInputItem(jasperPrint));
        Map<String, Object> exportParameters = new HashMap<>();
        String reportUnitUri = "test/report";

        // Set up page parameters to test that part of the code
        ReportOutputPages pages = new ReportOutputPages();
        pages.setPage(1);
        exportParameters.put(Argument.RUN_OUTPUT_PAGES, pages);

        // When & Then - We expect no exception
        try {
            exporter.exportReport(inputItems, outputStream, engineService, exportParameters, executionContext, reportUnitUri);
            // If we get here without exception, the test passed
        } catch (Exception e) {
            logger.error("Exception occurred during exportReport for {}: {}", exporterName, e.getMessage(), e);
            fail("Unexpected exception: " + e.getMessage());
        }
    }
}
