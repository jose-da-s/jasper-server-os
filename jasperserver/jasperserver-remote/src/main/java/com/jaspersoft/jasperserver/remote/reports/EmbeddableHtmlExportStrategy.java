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

import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.ReportUnitResult;
import com.jaspersoft.jasperserver.dto.executions.ExecutionStatus;
import com.jaspersoft.jasperserver.remote.services.*;
import com.jaspersoft.jasperserver.api.engine.export.HyperlinkProducerFactoryFlowFactory;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.AbstractHtmlExporter;
import net.sf.jasperreports.engine.export.JRHyperlinkProducerFactory;
import net.sf.jasperreports.export.SimpleHtmlExporterConfiguration;
import net.sf.jasperreports.json.export.GenericElementJsonHandler;
import net.sf.jasperreports.json.export.JsonExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlReportConfiguration;
import net.sf.jasperreports.json.export.SimpleJsonExporterOutput;
import net.sf.jasperreports.json.export.SimpleJsonReportConfiguration;
import net.sf.jasperreports.web.servlets.JasperPrintAccessor;
import net.sf.jasperreports.web.util.WebHtmlResourceHandler;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@Service
public class EmbeddableHtmlExportStrategy extends AbstractHtmlExportStrategy {
    private static final String ATTACHMENT_REPORT_COMPONENTS_JSON = "reportComponents.json";

    @Resource(name = "viewReportHyperlinkProducerFactory")
    private HyperlinkProducerFactoryFlowFactory<HttpServletRequest, HttpServletResponse> hyperlinkProducerFactory;

    @Override
    public void export(ReportExecution reportExecution, ExportExecution exportExecution, JasperPrint jasperPrint) {
        super.export(reportExecution, exportExecution, jasperPrint);

        final String contextPath;
        final String baseUrl = exportExecution.getOptions().getBaseUrl();
        final String deployBaseUrl = getDeployBaseUrl();
        final ReportExecutionOptions reportExecutionOptions = reportExecution.getOptions();
        if (baseUrl != null) {
            // if baseUrl is specified for this export execution, then use it first
            contextPath = baseUrl;
        } else if (deployBaseUrl != null && !deployBaseUrl.isEmpty()) {
            // no baseUrl is specified for this export execution, but it is specified for JRS
            contextPath = deployBaseUrl;
        } else {
            // no baseUrl is specified, use contextPath from request or no prefix at all
            contextPath = (reportExecutionOptions.getContextPath() != null ? reportExecutionOptions.getContextPath() : "");
        }
        putReportComponentsAttachment(reportExecution, exportExecution, contextPath);
    }

    @Override
    protected AbstractHtmlExporter prepareExporter(final ReportExecution reportExecution, ExportExecution exportExecution, String contextPath) {
        final AbstractHtmlExporter exporter = super.prepareExporter(reportExecution, exportExecution, contextPath);
        SimpleHtmlExporterConfiguration htmlConfig = new SimpleHtmlExporterConfiguration();
        htmlConfig.setHtmlHeader("");
        htmlConfig.setHtmlFooter("");
        // there is no valid request and response in the async export context.
        // Let's proxy request to forward required parameters to hyperlink producers
        HttpServletRequest requestProxy = createRequestProxy(reportExecution, contextPath);
        SimpleHtmlReportConfiguration reportConfig = new SimpleHtmlReportConfiguration();
        JRHyperlinkProducerFactory hpFactory = hyperlinkProducerFactory.getHyperlinkProducerFactory(requestProxy, null);
        reportConfig.setHyperlinkProducerFactory(hpFactory);

        ReportUnitResult reportUnitResult;
        Boolean ignoreCancelledReportExecution = exportExecution.getOptions().getIgnoreCancelledReportExecution();
        if (ignoreCancelledReportExecution && ExecutionStatus.cancelled.equals(reportExecution.getStatus())) {
            reportUnitResult = reportExecution.getReportUnitResult();
        } else {
            reportUnitResult = reportExecution.getFinalReportUnitResult();
        }
        ReportContext reportContext = reportUnitResult.getReportContext();

        if (reportContext != null) {
            reportContext.setParameterValue(ReportContext.REQUEST_PARAMETER_APPLICATION_DOMAIN, exportExecution.getOptions().getBaseUrl());
            if (exportExecution.getOptions().getClearContextCache()) {
                reportContext.setParameterValue("net.sf.jasperreports.search.term.highlighter", null);
            }
            exporter.setReportContext(reportContext);
        }

        exporter.setConfiguration(reportConfig);
        exporter.setConfiguration(htmlConfig);
        return exporter;
    }

    protected void putReportComponentsAttachment(ReportExecution reportExecution, ExportExecution exportExecution, String contextPath) {
        ReportUnitResult reportUnitResult = reportExecution.getReportUnitResult();
        JasperPrintAccessor jasperPrintAccessor = reportUnitResult != null ? reportUnitResult.getJasperPrintAccessor() : null;

        if (jasperPrintAccessor == null) {
            throw new JRRuntimeException("No jasperPrintAccessor to prepare components attachment with!");
        }

        ReportOutputPages pages = exportExecution.getOptions().getPages();
        ReportOutputResource attachment = getComponentsAttachment(reportExecution, exportExecution.getOptions(),
                jasperPrintAccessor, pages, reportUnitResult.getReportContext(), contextPath);

        exportExecution.getAttachments().put(ATTACHMENT_REPORT_COMPONENTS_JSON, attachment);
    }

    protected ReportOutputResource getComponentsAttachment(ReportExecution reportExecution,
           ExportExecutionOptions exportExecutionOptions, JasperPrintAccessor jasperPrintAccessor,
           ReportOutputPages pages, ReportContext reportContext, String contextPath) {

        JsonExporter exporter = new JsonExporter(getJasperReportsContext());

        SimpleJsonReportConfiguration configuration = new SimpleJsonReportConfiguration();
        if (pages != null) {
            if (pages.getPage() != null) {
                configuration.setPageIndex(pages.getPage() - 1);
            } else if (pages.getStartPage() != null && pages.getEndPage() != null) {
                configuration.setStartPageIndex(pages.getStartPage() - 1);
                configuration.setEndPageIndex(pages.getEndPage() - 1);
            }
        }

        reportContext.setParameterValue(GenericElementJsonHandler.PARAMETER_CLEAR_CONTEXT_CACHE, exportExecutionOptions.getClearContextCache());
        exporter.setReportContext(reportContext);
        exporter.setExporterInput(new SimpleExporterInput(jasperPrintAccessor.getJasperPrint()));

        String charset = StandardCharsets.UTF_8.name();
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        SimpleJsonExporterOutput output = new SimpleJsonExporterOutput(bytesOut, charset);
        output.setFontHandler(new WebHtmlResourceHandler(contextPath + "/reportresource?&font={0}"));
        output.setResourceHandler(new WebHtmlResourceHandler(contextPath + "/rest_v2/resources" + reportExecution.getReportURI() + "_files/{0}"));
        exporter.setExporterOutput(output);

        HttpServletRequest requestProxy = createRequestProxy(reportExecution, contextPath);
        configuration.setHyperlinkProducerFactory(hyperlinkProducerFactory.getHyperlinkProducerFactory(requestProxy, null));
        exporter.setConfiguration(configuration);

        try {
            exporter.exportReport();
        } catch (JRException e) {
            throw new JRRuntimeException("Failed to create components attachment!", e);
        }


        ReportOutputResource attachment = new ReportOutputResource();
        attachment.setData(bytesOut.toByteArray());
        attachment.setContentType("application/json; charset=" + charset);

        return attachment;
    }

    private HttpServletRequest createRequestProxy(ReportExecution reportExecution, String contextPath) {
        return (HttpServletRequest) Proxy.newProxyInstance(this.getClass().getClassLoader(),
            new Class<?>[]{HttpServletRequest.class}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    Object result = null;
                    if ("getAttribute".equals(method.getName())) {
                        result = "reportResult".equals(args[0]) ? reportExecution.getReportUnitResult() : null;
                    } else if ("getContextPath".equals(method.getName())) {
                        result = contextPath;
                    }
                    return result;
                }
            });
    }
}
