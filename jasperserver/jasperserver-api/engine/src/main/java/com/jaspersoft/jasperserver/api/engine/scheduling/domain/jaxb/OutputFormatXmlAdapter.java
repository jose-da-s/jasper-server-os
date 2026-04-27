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
package com.jaspersoft.jasperserver.api.engine.scheduling.domain.jaxb;

import com.jaspersoft.jasperserver.api.common.domain.jaxb.AbstractEnumXmlAdapter;
import com.jaspersoft.jasperserver.api.common.domain.jaxb.NamedPropertyHolder;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJob;
import com.jaspersoft.jasperserver.dto.common.ExportType;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This adapter is used for ReportJob.outputFormats serialization.
 * ReportJobOutputFormatsWrapper is used for serialization because of no ability to use @XmlElementWrapper together with @XmlJavaTypeAdapter.
 * See http://java.net/jira/browse/JAXB-787
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
public class OutputFormatXmlAdapter extends XmlAdapter<ReportJobOutputFormatsWrapper, Set<Byte>> {

    private enum SupportedOutputFormat implements NamedPropertyHolder<Byte>{
        PDF(ReportJob.OUTPUT_FORMAT_PDF),
        HTML(ReportJob.OUTPUT_FORMAT_HTML),
        XLS(ReportJob.OUTPUT_FORMAT_XLS),
        RTF(ReportJob.OUTPUT_FORMAT_RTF),
        CSV(ReportJob.OUTPUT_FORMAT_CSV),
        ODT(ReportJob.OUTPUT_FORMAT_ODT),
        TXT(ReportJob.OUTPUT_FORMAT_TXT),
        DOCX(ReportJob.OUTPUT_FORMAT_DOCX),
        ODS(ReportJob.OUTPUT_FORMAT_ODS),
        XLSX(ReportJob.OUTPUT_FORMAT_XLSX),
        XLS_NOPAG(ReportJob.OUTPUT_FORMAT_XLS_NOPAG),
        XLSX_NOPAG(ReportJob.OUTPUT_FORMAT_XLSX_NOPAG),
        DATA_SNAPSHOT(ReportJob.OUTPUT_FORMAT_DATA_SNAPSHOT),
        PPTX(ReportJob.OUTPUT_FORMAT_PPTX),
        JSON(ReportJob.OUTPUT_FORMAT_JSON),
        PNG(ReportJob.OUTPUT_FORMAT_PNG),
        DATA_CSV(ReportJob.OUTPUT_FORMAT_DATA_CSV),
        DATA_XLSX(ReportJob.OUTPUT_FORMAT_DATA_XLSX);


        private final Byte byteValue;

        private SupportedOutputFormat(Byte byteValue) {
            this.byteValue = byteValue;
        }


        public Byte getProperty() {
            return this.byteValue;
        }
    }

    private enum DetailedDashboardSupportedOutputFormat implements NamedPropertyHolder<Byte>{
        PDF(ReportJob.OUTPUT_FORMAT_PDF_DETAILED),
        XLS_NOPAG(ReportJob.OUTPUT_FORMAT_XLS_DETAILED),
        RTF(ReportJob.OUTPUT_FORMAT_RTF_DETAILED),
        CSV(ReportJob.OUTPUT_FORMAT_CSV_DETAILED),
        ODT(ReportJob.OUTPUT_FORMAT_ODT_DETAILED),
        DOCX(ReportJob.OUTPUT_FORMAT_DOCX_DETAILED),
        ODS(ReportJob.OUTPUT_FORMAT_ODS_DETAILED),
        XLSX_NOPAG(ReportJob.OUTPUT_FORMAT_XLSX_DETAILED),
        PPTX(ReportJob.OUTPUT_FORMAT_PPTX_DETAILED);

        private final Byte byteValue;

        private DetailedDashboardSupportedOutputFormat(Byte byteValue) {
            this.byteValue = byteValue;
        }

        public Byte getProperty() {
            return this.byteValue;
        }
    }

    private Map<String, SingleOutputFormatAdapter> exportTypeAdapters;

    public OutputFormatXmlAdapter() {
        exportTypeAdapters = new LinkedHashMap<>();
        SingleOutputFormatAdapter screenshotOutputFormatAdapter = new SingleOutputFormatAdapter(SupportedOutputFormat.values());
        exportTypeAdapters.put(ExportType.DEFAULT.name(), screenshotOutputFormatAdapter);
        SingleOutputFormatAdapter detailedOutputFormatAdapter = new SingleOutputFormatAdapter(DetailedDashboardSupportedOutputFormat.values());
        exportTypeAdapters.put(ExportType.DASHBOARD_DETAILED.name(), detailedOutputFormatAdapter);
    }

    @Override
    public Set<Byte> unmarshal(ReportJobOutputFormatsWrapper v) throws Exception {
        Set<Byte> result = null;
        if (v != null && v.getFormats() != null && !v.getFormats().isEmpty()) {
            result = new HashSet<Byte>();
            String exportType = v.getExportType() == null ? ExportType.DEFAULT.name() : v.getExportType();
            SingleOutputFormatAdapter adapter = exportTypeAdapters.get(exportType);
            for (String currentValue : v.getFormats()) {
                Byte currentByteValue = adapter.unmarshal(currentValue);
                if (currentByteValue == null) {
                    throw new InvalidOutputFormatException(currentValue);
                }
                result.add(currentByteValue);
            }
        }
        return result;
    }

    @Override
    public ReportJobOutputFormatsWrapper marshal(Set<Byte> v) throws Exception {
        ReportJobOutputFormatsWrapper result = null;
        if (v != null && !v.isEmpty()) {
            Set<String> set = new HashSet<String>();
            String exportType = null;
            for (Byte currentValue : v) {
                for (Entry<String, SingleOutputFormatAdapter> exportTypeEntry : exportTypeAdapters.entrySet()) {
                    String currentStringValue = exportTypeEntry.getValue().marshal(currentValue);
                    if (currentStringValue != null) {
                        exportType = exportTypeEntry.getKey();
                        set.add(currentStringValue);
                    }
                }
            }
            if(!set.isEmpty()){
                result = new ReportJobOutputFormatsWrapper();
                result.setExportType(exportType);
                result.setFormats(set);
            }
        }
        return result;
    }

    private class SingleOutputFormatAdapter extends AbstractEnumXmlAdapter<Byte> {
        private NamedPropertyHolder<Byte>[] values;

        public SingleOutputFormatAdapter(NamedPropertyHolder<Byte>[] values) {
            this.values = values;
        }

        @Override
        protected NamedPropertyHolder<Byte>[] getEnumConstantsArray() {
            return values;
        }
    }
}
