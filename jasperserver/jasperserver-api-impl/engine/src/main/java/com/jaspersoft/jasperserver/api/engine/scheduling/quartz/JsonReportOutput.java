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

package com.jaspersoft.jasperserver.api.engine.scheduling.quartz;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.json.export.SimpleJsonMetadataReportConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ContentResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.DataContainer;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPropertiesHolder;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.json.export.JsonMetadataExporter;


/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class JsonReportOutput extends AbstractReportOutput
{

	private static final Log log = LogFactory.getLog(JsonReportOutput.class);

	public JsonReportOutput()
	{
	}

    @Override
    protected DataContainer export(ReportJobContext jobContext,
                                   JasperPrint jasperPrint,
                                   Integer startPageIndex,
                                   Integer endPageIndex) {
        try {
            JsonMetadataExporter exporter = new JsonMetadataExporter(getJasperReportsContext());

            if (jasperPrint == null) {
                throw new IllegalArgumentException("JasperPrint is required");
            }
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));

            DataContainer dataContainer = jobContext.createDataContainer(this);
            OutputStream dataOut = dataContainer.getOutputStream();

            try (dataOut) {
                String encoding = jobContext.getCharacterEncoding();
                Writer writer = new OutputStreamWriter(dataOut, encoding);

                exporter.setExporterOutput(new SimpleWriterExporterOutput(writer));

                SimpleJsonMetadataReportConfiguration reportConfig = new SimpleJsonMetadataReportConfiguration();
                if (startPageIndex != null) {
                    reportConfig.setStartPageIndex(startPageIndex);
                }
                if (endPageIndex != null) {
                    reportConfig.setEndPageIndex(endPageIndex);
                }

                exporter.setConfiguration(reportConfig);

                exporter.exportReport();
                return dataContainer;

            } catch (IOException e) {
                throw new JSExceptionWrapper(e);
            }

        } catch (JRException e) {
            throw new JSExceptionWrapper(e);
        }
    }

	@Override
	public String getFileExtension()
	{
		return "json";
	}
	
	@Override
	public String getFileType() {
		return ContentResource.TYPE_JSON;
	}
	
	@Override
	public Boolean isPaginationPreferred(JRPropertiesHolder propertiesHolder){
		Boolean isPaginationPreferred = super.isPaginationPreferred(propertiesHolder);
		return isPaginationPreferred;
		// TODO lucianc
/*		if (isPaginationPreferred == null)
		{
			if (propertiesHolder != null)
			{
				isPaginationPreferred = JRPropertiesUtil.getInstance(getJasperReportsContext()).getBooleanProperty(propertiesHolder.getPropertiesMap(), PptxExportParametersBean.PROPERTY_PPTX_PAGINATED);
			}
		}
		return isPaginationPreferred;
*/	}
}
