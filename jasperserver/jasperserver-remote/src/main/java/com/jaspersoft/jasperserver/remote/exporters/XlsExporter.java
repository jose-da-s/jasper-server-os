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
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.ExportUtil;
import net.sf.jasperreports.export.*;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author Giulio Toffoli (original sanda zaharia (shertage@users.sourceforge.net))
 * @version $Id$
 */
@Service("remoteXlsExporter")
@Scope("prototype")
public class XlsExporter extends AbstractExporter {

    public static final String ONE_PAGE_PER_SHEET = "onePagePerSheet";
    @Resource(name = "xlsExportParameters")
    private XlsExportParametersBean exportParams;

    @Resource
    protected MessageSource messageSource;

    public XlsExporter() {
    	super(XlsExportParametersBean.PROPERTY_XLS_PAGINATED);
	}
    
    @Override
    public Exporter createExporter() throws Exception {
        return ExportUtil.getInstance(getJasperReportsContext()).createXlsExporter();
    }

    @Override
    public ExporterConfiguration createExporterConfiguration() {
        return new SimpleXlsExporterConfiguration();
    }

    @Override
    public void configureExporter(Exporter exporter, Map<?, ?> exportParameters, ExporterConfiguration exporterConfiguration) throws Exception {
        AbstractXlsReportConfiguration reportConfig = createReportConfiguration();

        if (exportParams != null) {
            if (exportParams.isOverrideReportHints())
                reportConfig.setOverrideHints(Boolean.TRUE);
            if (exportParams.getOnePagePerSheet() != null)
                reportConfig.setOnePagePerSheet(exportParams.getOnePagePerSheet());
            if(exportParameters.get(ONE_PAGE_PER_SHEET) != null)
                reportConfig.setOnePagePerSheet(Boolean.valueOf(String.valueOf(getSingleParameterValue(ONE_PAGE_PER_SHEET, (Map<String, Object>) exportParameters))));
            if (exportParams.getDetectCellType() != null)
                reportConfig.setDetectCellType(exportParams.getDetectCellType());
            if (exportParams.getRemoveEmptySpaceBetweenRows() != null)
                reportConfig.setRemoveEmptySpaceBetweenRows(exportParams.getRemoveEmptySpaceBetweenRows());
            if (exportParams.getRemoveEmptySpaceBetweenColumns() != null)
                reportConfig.setRemoveEmptySpaceBetweenColumns(exportParams.getRemoveEmptySpaceBetweenColumns());
            if (exportParams.getWhitePageBackground() != null)
                reportConfig.setWhitePageBackground(exportParams.getWhitePageBackground());
            if (exportParams.getIgnoreGraphics() != null)
                reportConfig.setIgnoreGraphics(exportParams.getIgnoreGraphics());
            if (exportParams.getCollapseRowSpan() != null)
                reportConfig.setCollapseRowSpan(exportParams.getCollapseRowSpan());
            if (exportParams.getIgnoreCellBorder() != null)
                reportConfig.setIgnoreCellBorder(exportParams.getIgnoreCellBorder());
            if (exportParams.getFontSizeFixEnabled() != null)
                reportConfig.setFontSizeFixEnabled(exportParams.getFontSizeFixEnabled());
            if (exportParams.getMaximumRowsPerSheet() != null)
                reportConfig.setMaxRowsPerSheet(exportParams.getMaximumRowsPerSheet());
            if (exportParams.getXlsFormatPatternsMap() != null && !exportParams.getXlsFormatPatternsMap().isEmpty())
                reportConfig.setFormatPatternsMap(exportParams.getXlsFormatPatternsMap());
        }

        ((AbstractXlsExporterConfiguration) exporterConfiguration).setCreateCustomPalette(Boolean.TRUE);

        exporter.setConfiguration(reportConfig);
    }

    /**
     * @param exportParams The exportParams to set.
     */
    public void setExportParams(XlsExportParametersBean exportParams) {
        this.exportParams = exportParams;
    }

    @Override
    public String getContentType() {
        return "application/xls";
    }

    @Override
    protected ExporterOutput getExporterOutput(OutputStream output) {
        return new SimpleOutputStreamExporterOutput(output);
    }

    public AbstractXlsReportConfiguration createReportConfiguration(){
        return new SimpleXlsReportConfiguration();
    }
}
