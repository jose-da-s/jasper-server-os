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
package com.jaspersoft.jasperserver.remote.reports;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.remote.services.ExportExecution;
import com.jaspersoft.jasperserver.remote.services.ReportExecution;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.ReportContext;
import net.sf.jasperreports.engine.export.AbstractHtmlExporter;
import net.sf.jasperreports.engine.export.HtmlResourceHandler;
import net.sf.jasperreports.export.SimpleHtmlExporterConfiguration;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.web.util.WebHtmlResourceHandler;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@Service
public class FullHtmlExportStrategy extends AbstractHtmlExportStrategy {
    @Resource
    private List<String> htmlReportHeaderIncludes;

    @Resource(name = "concreteRepository")
    private RepositoryService repositoryService;

    @Override
    protected AbstractHtmlExporter prepareExporter(ReportExecution reportExecution, ExportExecution exportExecution, String contextPath) {
        final AbstractHtmlExporter exporter = super.prepareExporter(reportExecution, exportExecution, contextPath);
        JasperReportsContext jasperReportsContext = exporter.getJasperReportsContext();

        final boolean allowInlineScripts = exportExecution.getOptions().isAllowInlineScripts();
        if (reportExecution.getOptions().isInteractive() && allowInlineScripts) {
            jasperReportsContext.setProperty("com.jaspersoft.jasperreports.highcharts.html.export.type", "standalone");
        }

        ReportContext reportContext = reportExecution.getFinalReportUnitResult().getReportContext();
        if (!allowInlineScripts) {
            if (reportContext != null) {
                reportContext.setParameterValue(ReportContext.REQUEST_PARAMETER_APPLICATION_DOMAIN, exportExecution.getOptions().getBaseUrl());
                exporter.setReportContext(reportContext);
            }
        }

        StringBuilder htmlHeader = new StringBuilder();
        for (String currentInclude : htmlReportHeaderIncludes) {
            htmlHeader.append(currentInclude.replaceAll("\\{contextPath\\}", contextPath));
        }

        SimpleHtmlExporterConfiguration exporterConfiguration = new SimpleHtmlExporterConfiguration();
        exporterConfiguration.setHtmlHeader(htmlHeader.toString());

        String resourcesPath = jasperReportsContext.getProperty("net.sf.jasperreports.web.resources.base.path");
        String reportUnitUri = reportExecution.getReportUnitResult().getReportUnitURI();
        ExecutionContext executionContext = ExecutionContextImpl.getRuntimeExecutionContext();
        com.jaspersoft.jasperserver.api.metadata.common.domain.Resource reportUnitResource = repositoryService.getResource(
                executionContext, reportUnitUri);

        SimpleHtmlExporterOutput htmlExporterOutput = new SimpleHtmlExporterOutput(new StringBuilder());
        htmlExporterOutput.setResourceHandler(new HtmlResourceHandler() {
            @Override
            public String getResourcePath(String id) {
                if (!isUrl(id)) {
                    if(((ReportUnit)reportUnitResource).getResourceLocal(id) != null) {
                        return contextPath + "/rest_v2/resources" + reportUnitUri + "_files/" + id;
                    }

                    return contextPath + (resourcesPath != null ? resourcesPath : "/reportresource?resource=") + id;
                }

                return id;
            }

            @Override
            public void handleResource(String s, byte[] bytes) {
                //NOOP
            }
        });
        htmlExporterOutput.setFontHandler(new WebHtmlResourceHandler(contextPath + "/reportresource?&font={0}"));

        exporter.setExporterOutput(htmlExporterOutput);
        exporter.setConfiguration(exporterConfiguration);
        return exporter;
    }

    private boolean isUrl(String id) {
        try {
            new URL(id);
        } catch (MalformedURLException e) {
            return false;
        }

        return true;
    }
}
