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
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.PaginationParameters;
import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.Argument;
import com.jaspersoft.jasperserver.remote.ReportExporter;
import com.jaspersoft.jasperserver.remote.services.ReportOutputPages;
import net.sf.jasperreports.export.*;
import net.sf.jasperreports.engine.JRPropertiesHolder;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReportsContext;

import javax.annotation.Resource;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author gtoffoli
 * @version $Id$
 */
public abstract class AbstractExporter implements ReportExporter {
	
    @Resource(name = "jasperReportsRemoteContext")
    private JasperReportsContext jasperReportsContext;
    
    private final String paginatedProperty;
    
    public AbstractExporter() {
    	this(null);
    }
    
    protected AbstractExporter(String paginatedProperty) {
    	this.paginatedProperty = paginatedProperty;
    }

    @SuppressWarnings("deprecation")
	@Override
    public void exportReport(JasperPrint jasperPrint, OutputStream output, EngineService engineService, Map<?, ?> exportParameters, ExecutionContext executionContext, String reportUnitURI) throws Exception {
    	exportReport(Collections.singletonList(new SimpleExporterInputItem(jasperPrint)),
    			output, engineService, exportParameters, executionContext, reportUnitURI);
    }

    @Override
    public void exportReport(List<ExporterInputItem> inputItems,
                             OutputStream output,
                             EngineService engineService,
                             Map<?, ?> exportParameters,
                             ExecutionContext executionContext,
                             String reportUnitURI) throws Exception {

        final Exporter exporter = createExporter();
        final ExporterConfiguration exporterConfiguration = createExporterConfiguration();

        // Handle generic parameters....
        exporter.setExporterInput(new SimpleExporterInput(inputItems));
        exporter.setExporterOutput(getExporterOutput(output));

        // Handle exporterConfigurations which are able to set pages...
        if(exporterConfiguration instanceof SimpleReportExportConfiguration config){
            // Be sure the page number is correctly set, so PAGE 1 is PAGE 1...
            // JasperReports uses a 0 based page system, while we prefer a 1 based page system
            if (exportParameters.get(Argument.RUN_OUTPUT_PAGE) != null) {
                int pageIndex = Integer.parseInt((String) exportParameters.get(Argument.RUN_OUTPUT_PAGE));
                pageIndex--; // transform a 1 index page to 0 indexed page...
                config.setPageIndex(pageIndex);
            } else if (exportParameters.get(Argument.RUN_OUTPUT_PAGES) != null) {
                // cast is safe because of known key
                @SuppressWarnings("unchecked")
                ReportOutputPages pages = (ReportOutputPages) exportParameters.get(Argument.RUN_OUTPUT_PAGES);
                if(pages.getPage() != null){
                    config.setPageIndex(pages.getPage() - 1);
                } else if(pages.getStartPage() != null && pages.getEndPage() != null) {
                    config.setStartPageIndex( pages.getStartPage() - 1);
                    config.setEndPageIndex(pages.getEndPage() - 1);
                }
            }
        }

        // Give the opportunity to each exporter to better configure itself...
        configureExporter(exporter, exportParameters, exporterConfiguration);
        exporter.setConfiguration(exporterConfiguration);
        exporter.exportReport();
    }

    protected ExporterOutput getExporterOutput(OutputStream output) {
        return new SimpleWriterExporterOutput(output);
    }

    public abstract Exporter createExporter() throws Exception;

    public abstract ExporterConfiguration createExporterConfiguration();

    public void configureExporter(Exporter exporter, Map<?, ?> exportParameters, ExporterConfiguration exporterConfiguration) throws Exception {
        // do nothing by default
    }

    protected Object getSingleParameterValue(String parameterName, Map<String, Object> exportParameters) {
        Object result = null;
        if (exportParameters.get(parameterName) != null) {
            if (exportParameters.get(parameterName) instanceof String[]) {
                if (((String[]) exportParameters.get(parameterName)).length > 0)
                    result = ((String[]) exportParameters.get(parameterName))[0];
            } else
                result = exportParameters.get(parameterName);
        }
        return result;
    }

	public JasperReportsContext getJasperReportsContext() {
		return jasperReportsContext;
	}
	
    @Override
	public PaginationParameters getPaginationParameters(JRPropertiesHolder propertiesHolder) {
		PaginationParameters pagination = new PaginationParameters();
		if (paginatedProperty != null) {
			Boolean paginated = JRPropertiesUtil.getInstance(jasperReportsContext).getBooleanProperty(
					propertiesHolder, paginatedProperty);
			pagination.setPaginated(paginated);
		}
		return pagination;
	}

}
