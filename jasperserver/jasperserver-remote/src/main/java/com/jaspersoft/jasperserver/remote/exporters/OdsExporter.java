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
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.OutputStream;

/**
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@Service("remoteOdsExporter")
@Scope("prototype")
public class OdsExporter extends XlsExporter {
    @Override
    public String getContentType() {
        return "application/vnd.oasis.opendocument.spreadsheet";
    }

    @Override
    public Exporter createExporter() throws Exception {
        return new JROdsExporter(getJasperReportsContext());
    }

    @Resource(name = "odsExportParameters")
    @Override
    public void setExportParams(XlsExportParametersBean exportParams) {
        super.setExportParams(exportParams);
    }

    @Override
    public ExporterConfiguration createExporterConfiguration() {
        return new SimpleOdsExporterConfiguration();
    }

    @Override
    protected ExporterOutput getExporterOutput(OutputStream output) {
        return new SimpleOutputStreamExporterOutput(output);
    }

    @Override
    public AbstractXlsReportConfiguration createReportConfiguration(){
        return new SimpleOdsReportConfiguration();
    }
}
