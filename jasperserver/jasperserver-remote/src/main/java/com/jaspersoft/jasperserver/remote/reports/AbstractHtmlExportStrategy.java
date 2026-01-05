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


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.ReportUnitResult;
import com.jaspersoft.jasperserver.dto.executions.ExecutionStatus;
import net.sf.jasperreports.export.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;

import com.jaspersoft.jasperserver.api.common.error.handling.SecureExceptionHandler;
import com.jaspersoft.jasperserver.api.common.util.CharacterEncodingProvider;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.ExportUtil;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.remote.services.ExportExecution;
import com.jaspersoft.jasperserver.remote.services.ExportExecutionOptions;
import com.jaspersoft.jasperserver.remote.services.ReportExecution;
import com.jaspersoft.jasperserver.remote.services.ReportExecutionOptions;
import com.jaspersoft.jasperserver.remote.services.ReportOutputPages;
import com.jaspersoft.jasperserver.remote.services.ReportOutputResource;
import com.jaspersoft.jasperserver.remote.services.RunReportService;
import com.jaspersoft.jasperserver.remote.utils.AuditHelper;

import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.ReportContext;
import net.sf.jasperreports.engine.SimpleJasperReportsContext;
import net.sf.jasperreports.engine.export.AbstractHtmlExporter;
import net.sf.jasperreports.engine.export.HtmlResourceHandler;
import net.sf.jasperreports.engine.export.MapHtmlResourceHandler;
import net.sf.jasperreports.engine.type.ImageTypeEnum;
import net.sf.jasperreports.engine.util.JRTypeSniffer;
import net.sf.jasperreports.renderers.util.RendererUtil;
import net.sf.jasperreports.web.util.WebHtmlResourceHandler;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
public abstract class AbstractHtmlExportStrategy implements HtmlExportStrategy {
    private final static Log log = LogFactory.getLog(AbstractHtmlExportStrategy.class);
    @Resource(name = "jasperReportsRemoteContext")
    private JasperReportsContext jasperReportsContext;
    @Resource
    private AuditHelper auditHelper;
    @Value("${deploy.base.url:}")
    private String deployBaseUrl;
    @Resource
    private SecureExceptionHandler secureExceptionHandler;
    @Resource
    private CharacterEncodingProvider encodingProvider;

    @Override
    public void export(ReportExecution reportExecution, ExportExecution exportExecution, JasperPrint jasperPrint) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final String contextPath;
        final ExportExecutionOptions exportExecutionOptions = exportExecution.getOptions();
        String baseUrl = exportExecutionOptions.getBaseUrl();
        final ReportExecutionOptions reportExecutionOptions = reportExecution.getOptions();
        if (baseUrl != null) {
            // if baseUrl is specified fro this export execution, then use it first
            contextPath = baseUrl;
        } else if (deployBaseUrl != null && !deployBaseUrl.isEmpty()) {
            // no baseUrl is specified fro this export execution, but it is specified for JRS
            contextPath = deployBaseUrl;
        } else {
            // no baseUrl is specified, use contextPath from request or no prefix at all
            contextPath = (reportExecutionOptions.getContextPath() != null ? reportExecutionOptions.getContextPath() : "");
        }
        final AbstractHtmlExporter exporter = prepareExporter(reportExecution, exportExecution, contextPath);
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        SimpleHtmlExporterOutput htmlExporterOutput = new SimpleHtmlExporterOutput(outputStream, encodingProvider.getCharacterEncoding());
        // collecting the images into a map
        Map<String, byte[]> imagesMap = new LinkedHashMap<String, byte[]>();

        String attachmentsPrefix = exportExecutionOptions.getAttachmentsPrefix() != null ?
                exportExecutionOptions.getAttachmentsPrefix() : reportExecutionOptions.getDefaultAttachmentsPrefixTemplate();
        if (attachmentsPrefix != null) {
            attachmentsPrefix = attachmentsPrefix
                    .replace(RunReportService.CONTEXT_PATH_ATTACHMENTS_PREFIX_TEMPLATE_PLACEHOLDER, reportExecutionOptions.getContextPath() != null ? reportExecutionOptions.getContextPath() : "")
                    .replace(RunReportService.REPORT_EXECUTION_ID_ATTACHMENTS_PREFIX_TEMPLATE_PLACEHOLDER, reportExecution.getRequestId())
                    .replace(RunReportService.EXPORT_EXECUTION_ID_ATTACHMENTS_PREFIX_TEMPLATE_PLACEHOLDER, exportExecution.getId());
        }

        String resourcePattern = ((attachmentsPrefix != null) ? attachmentsPrefix : "images/") + "{0}";
        HtmlResourceHandler resourceHandler = new MapHtmlResourceHandler(
                new WebHtmlResourceHandler(resourcePattern),
                imagesMap
        );

        htmlExporterOutput.setImageHandler(resourceHandler);
        htmlExporterOutput.setResourceHandler(resourceHandler);
        exporter.setExporterOutput(htmlExporterOutput);
        final ReportOutputPages pages = exportExecutionOptions.getPages();
        try {
            exporter.exportReport();
        } catch (ErrorDescriptorException e) {
            auditHelper.addExceptionToAllAuditEvents(e);
            throw e;
        } catch (JRRuntimeException e) {
            if (JRAbstractExporter.EXCEPTION_MESSAGE_KEY_PAGE_INDEX_OUT_OF_RANGE.equals(e.getMessageKey())
            		|| JRAbstractExporter.EXCEPTION_MESSAGE_KEY_START_PAGE_INDEX_OUT_OF_RANGE.equals(e.getMessageKey())
            		|| JRAbstractExporter.EXCEPTION_MESSAGE_KEY_END_PAGE_INDEX_OUT_OF_RANGE.equals(e.getMessageKey())) {
                final String pagesString = pages.toString();
                throw new ErrorDescriptorException(new ErrorDescriptor().setMessage(
                        "Page number out of range : " + pagesString + " of "
                        + (reportExecution.getTotalPages() != null ? " Total pages: " + reportExecution.getTotalPages() : "")
                        + " (while exporting the report)").setErrorCode("page.number.out.of.range")
                        .addParameters(pages, "" + reportExecution.getTotalPages()));
            } else {
                throw new ErrorDescriptorException(e, secureExceptionHandler);
            }
        } catch (Exception e) {
            log.debug("Error exporting report", e);
            auditHelper.addExceptionToAllAuditEvents(e);
            throw new ErrorDescriptorException(
                    new ErrorDescriptor()
                            .setErrorCode("webservices.error.errorExportingReportUnit").setParameters(e.getMessage()), e
            );
        } finally {
            try {
                outputStream.close();
            } catch (IOException ex) {
                log.error("caught exception: " + ex.getMessage(), ex);
            }
        }
        exportExecution.setOutputResource(new ReportOutputResource().setContentType("text/html")
                .setData(outputStream.toByteArray()).setPages(pages != null ? pages.toString() : null));
        putImages(imagesMap, exportExecution.getAttachments());
    }

    protected AbstractHtmlExporter prepareExporter(ReportExecution reportExecution, ExportExecution exportExecution, final String contextPath) {
        // use new instance of jasper reports context to allow modifications
        SimpleJasperReportsContext context = new SimpleJasperReportsContext();
        context.setParent(jasperReportsContext);
        final AbstractHtmlExporter exporter = ExportUtil.getInstance(context).createHtmlExporter();
        final ExportExecutionOptions exportExecutionOptions = exportExecution.getOptions();
        ReportOutputPages pages = exportExecutionOptions.getPages();
        SimpleHtmlReportConfiguration htmlReportConfiguration = new SimpleHtmlReportConfiguration();
        if(pages != null) {
            if (pages.getPage() != null) {
                htmlReportConfiguration.setPageIndex(pages.getPage() - 1);
            } else if (pages.getStartPage() != null && pages.getEndPage() != null) {
                htmlReportConfiguration.setStartPageIndex(pages.getStartPage() - 1);
                htmlReportConfiguration.setEndPageIndex(pages.getEndPage() - 1);
            }
        }
        // JR requires HttpServletRequest instance to get contextPath from it.
        // Seems it's the only field queried from the request object.
        // We need to do the trick with proxy to send contextPath to JR without having real request object.
        // We can't just inject HttpServletRequest, because in case of asynchronous export this class is invoked
        // from a different thread without valid request bound to it.
        HttpServletRequest proxy = (HttpServletRequest) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class<?>[]{HttpServletRequest.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Object result = null;
                        if ("getContextPath".equals(method.getName())) {
                            result = contextPath;
                        }
                        return result;
                    }
                });
        exporter.getExporterContext().setValue(ExportUtil.HTTP_SERVLET_REQUEST, proxy);
        SimpleHtmlExporterOutput htmlExporterOutput = new SimpleHtmlExporterOutput((OutputStream)null);
        htmlExporterOutput.setFontHandler(new WebHtmlResourceHandler(contextPath + "/reportresource?&font={0}"));

        ReportUnitResult reportUnitResult;
        Boolean ignoreCancelledReportExecution = exportExecution.getOptions().getIgnoreCancelledReportExecution();
        if (ignoreCancelledReportExecution && ExecutionStatus.cancelled.equals(reportExecution.getStatus())) {
            reportUnitResult = reportExecution.getReportUnitResult();
        } else {
            reportUnitResult = reportExecution.getFinalReportUnitResult();
        }
        ReportContext reportContext = reportUnitResult.getReportContext();
        if (reportContext != null)
        {
            reportContext.setParameterValue("contextPath", contextPath);// context path to be used by the font handler in JSON exporter
        }

        exporter.setExporterOutput(htmlExporterOutput);
        exporter.setConfiguration(htmlReportConfiguration);
        
        return exporter;
    }


    /**
     * Place images to output container.
     *
     * @param imagesMap - map containing the images
     * @param outputContainer - output container to fill with images
     * @throws ErrorDescriptorException if any error occurs
     */
    protected void putImages(Map<String, byte[]> imagesMap, Map<String, ReportOutputResource> outputContainer) throws ErrorDescriptorException {
        try {
            if (imagesMap != null && !imagesMap.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("imagesMap : " + Arrays.asList(imagesMap.keySet().toArray()));
                }
                for (String name : imagesMap.keySet()) {
                    byte[] data = imagesMap.get(name);
                    if (log.isDebugEnabled()) {
                        log.debug("Adding image for HTML: " + name);
                    }
					outputContainer.put(name, new ReportOutputResource()
                            .setContentType(imageMimeType(data)).setData(data).setFileName(name));
                }
            }
        } catch (Throwable e) {
			ErrorDescriptor ed = secureExceptionHandler.handleException(e, new ErrorDescriptor().setErrorCode("webservices.error.errorAddingImage"));

			log.error(ed.getMessage(), e);
            throw new ErrorDescriptorException(ed, e);
        }
    }

	protected String imageMimeType(byte[] data) {
		String mimeType = null;
		ImageTypeEnum imageType = JRTypeSniffer.getImageTypeValue(data);
		if (imageType != ImageTypeEnum.UNKNOWN) {
			mimeType = imageType.getMimeType();
		} else if (RendererUtil.getInstance(jasperReportsContext).isSvgData(data)) {
			mimeType = RendererUtil.SVG_MIME_TYPE;
		}
		return mimeType;
	}

    protected JasperReportsContext getJasperReportsContext() {
        return this.jasperReportsContext;
    }

    protected String getDeployBaseUrl() {
        return this.deployBaseUrl;
    }
}
