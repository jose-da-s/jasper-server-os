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

import com.jaspersoft.jasperserver.api.engine.jasperreports.common.XlsExportParametersBean;
import net.sf.jasperreports.export.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author Sanda Zaharia
 */
@Service("remoteXlsxMetadataExporter")
@Scope("prototype")
public class XlsxMetadataExporter extends AbstractExporter{

    private static Logger log = LogManager.getLogger(XlsxMetadataExporter.class);

    public static final String ONE_PAGE_PER_SHEET = "onePagePerSheet";
    @Resource(name = "xlsExportParameters")
    private XlsExportParametersBean exportParams;

    @Resource
    protected MessageSource messageSource;

    public XlsxMetadataExporter() {
        super(XlsExportParametersBean.PROPERTY_XLS_PAGINATED);
    }

    @Override
    public void configureExporter(Exporter exporter, Map<?, ?> exportParameters, ExporterConfiguration exporterConfiguration) {
        if (exportParams != null) { SimpleXlsMetadataReportConfiguration config = new SimpleXlsMetadataReportConfiguration();
            if (exportParams.isOverrideReportHints()) {
                config.setOverrideHints(true);}
            if (exportParams.getOnePagePerSheet() != null) {
                config.setOnePagePerSheet(exportParams.getOnePagePerSheet());}
            if (exportParameters.get(ONE_PAGE_PER_SHEET) != null) {
                config.setOnePagePerSheet(Boolean.parseBoolean(String.valueOf(getSingleParameterValue(ONE_PAGE_PER_SHEET, (Map<String, Object>) exportParameters))));}
            if (exportParams.getDetectCellType() != null) {
                config.setDetectCellType(exportParams.getDetectCellType());}
            if (exportParams.getRemoveEmptySpaceBetweenRows() != null) {
                config.setRemoveEmptySpaceBetweenRows(exportParams.getRemoveEmptySpaceBetweenRows());}
            if (exportParams.getRemoveEmptySpaceBetweenColumns() != null) {
                config.setRemoveEmptySpaceBetweenColumns(exportParams.getRemoveEmptySpaceBetweenColumns());}
            if (exportParams.getWhitePageBackground() != null) {
                config.setWhitePageBackground(exportParams.getWhitePageBackground());}
            if (exportParams.getIgnoreGraphics() != null) {
                config.setIgnoreGraphics(exportParams.getIgnoreGraphics());}
            if (exportParams.getCollapseRowSpan() != null) {
                config.setCollapseRowSpan(exportParams.getCollapseRowSpan());}
            if (exportParams.getIgnoreCellBorder() != null) {
                config.setIgnoreCellBorder(exportParams.getIgnoreCellBorder());}
            if (exportParams.getFontSizeFixEnabled() != null) {
                config.setFontSizeFixEnabled(exportParams.getFontSizeFixEnabled());}
            if (exportParams.getMaximumRowsPerSheet() != null) {
                config.setMaxRowsPerSheet(exportParams.getMaximumRowsPerSheet());}
            if (exportParams.getXlsFormatPatternsMap() != null && !exportParams.getXlsFormatPatternsMap().isEmpty()) {
                config.setFormatPatternsMap(exportParams.getXlsFormatPatternsMap());}

            exporter.setConfiguration(config);
        }
        ((SimpleXlsExporterConfiguration) exporterConfiguration).setCreateCustomPalette(Boolean.TRUE);
    }

    /**
     * @param exportParams The exportParams to set.
     */
    public void setExportParams(XlsExportParametersBean exportParams) {
        this.exportParams = exportParams;
    }

    @Override
    public Exporter createExporter() throws Exception {
        return new net.sf.jasperreports.engine.export.ooxml.XlsxMetadataExporter(getJasperReportsContext());
    }

    @Override
    public ExporterConfiguration createExporterConfiguration() {
        return new SimpleXlsxExporterConfiguration();
    }

    @Override
    public String getContentType() {
        return "application/xlsx";
    }

    @Override
    protected ExporterOutput getExporterOutput(OutputStream output) {
        return new SimpleOutputStreamExporterOutput(output);
    }
}
