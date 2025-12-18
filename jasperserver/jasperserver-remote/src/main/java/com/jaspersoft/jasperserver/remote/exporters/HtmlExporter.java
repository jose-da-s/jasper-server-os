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

import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import net.sf.jasperreports.export.*;
import net.sf.jasperreports.web.util.WebHtmlResourceHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.jaspersoft.jasperserver.api.engine.jasperreports.util.ExportUtil;

import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.SimpleJasperReportsContext;
import net.sf.jasperreports.engine.export.AbstractHtmlExporter;
import java.util.LinkedHashMap;
import net.sf.jasperreports.engine.ReportContext;
import net.sf.jasperreports.engine.export.HtmlResourceHandler;
import net.sf.jasperreports.engine.export.MapHtmlResourceHandler;

import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.Argument;

/*  2012-09-13  thorick: restored 24858 to fix build   */
/*  2012-09-13  thorick: backout 24858 to fix build
import com.jaspersoft.jasperserver.war.util.JRHtmlExportUtils;
*/
/*  2012-09-13  thorick: backout 24858 to fix build
import net.sf.jasperreports.engine.JasperReportsContext;
*/
/*  2012-09-13  thorick: backout 24858 to fix build
import org.springframework.beans.factory.annotation.Autowired;
*/
/*  2012-09-13  thorick: backout 24858 to fix build
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
*/
/*  2012-09-13  thorick: backout 24858 to fix build
import java.util.List;
*/

/**
 * @author sanda zaharia (shertage@users.sourceforge.net)
 * @@version $Id: HtmlWSExporter.java 19933 2010-12-11 15:27:37Z tmatyashovsky $
 */
@Service("remoteHtmlExporter")
@Scope("prototype")
public class HtmlExporter extends AbstractExporter {

    public static final String CONTEXT_PATH_PARAM_NAME = "contextPath";
    public static final String BASE_URL_PARAM_NAME = "baseUrl";
    public static final String REPORT_CONTEXT_PARAM_NAME = "reportContext";
    public static final String INTERACTIVE_PARAM_NAME = "interactive";
    public static final String ALLOW_INLINE_SCRIPTS_PARAM_NAME = "allowInlineScripts";

    @Resource
    private List<String> htmlReportHeaderIncludes;

    @Value("${deploy.base.url:}")
    private String deployBaseUrl;

    @Override
    public Exporter createExporter() throws Exception {
        return ExportUtil.getInstance(getJasperReportsContext()).createHtmlExporter();
    }

    @Override
    public ExporterConfiguration createExporterConfiguration() {
        return new SimpleHtmlExporterConfiguration();
    }

    @Override
    public void configureExporter(Exporter exporter, Map<?,?> exportParameters, ExporterConfiguration exporterConfiguration) throws Exception {
        StringBuilder htmlHeader = new StringBuilder();
        final String contextPath;
        if(exportParameters.get(BASE_URL_PARAM_NAME) != null){
            // if baseUrl is specified for this export execution, then use it first
            contextPath = (String) exportParameters.get(BASE_URL_PARAM_NAME);
        } else if(deployBaseUrl != null && !deployBaseUrl.isEmpty()){
            // no baseUrl is specified for this export execution, but it is specified for JRS
            contextPath = deployBaseUrl;
        } else {
            // no baseUrl is specified, use contextPath from request or no prefix at all
            contextPath = (String) (exportParameters.get(CONTEXT_PATH_PARAM_NAME) != null ?
                    exportParameters.get(CONTEXT_PATH_PARAM_NAME) : "");
        }
        if(htmlReportHeaderIncludes != null){
            for (String currentInclude : htmlReportHeaderIncludes) {
                htmlHeader.append(currentInclude.replaceAll("\\{contextPath\\}", contextPath));
            }
        }
        ((SimpleHtmlExporterConfiguration) exporterConfiguration).setHtmlHeader(htmlHeader.toString());
        // JR requires HttpServletRequest instance to get contextPath from it.
        // Seems it's the only field queried from the request object.
        // We need to do the trick with proxy to send contextPath to JR without having real request object.
        // We can't just inject HttpServletRequest, because in case of asynchronous export this class is invoked
        // from a different thread without valid request bound to it.
        HttpServletRequest proxy = (HttpServletRequest)Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class<?>[]{HttpServletRequest.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object result = null;
                if("getContextPath".equals(method.getName())){
                    result = contextPath;
                }
                return result;
            }
        });
        //proxy set to reportContext below

        if (exportParameters.get(ReportExportConfiguration.PROPERTY_IGNORE_PAGE_MARGINS) != null) {
            SimpleHtmlReportConfiguration reportConfig = new SimpleHtmlReportConfiguration();
            reportConfig.setIgnorePageMargins((Boolean)exportParameters.get(ReportExportConfiguration.PROPERTY_IGNORE_PAGE_MARGINS));
            exporter.setConfiguration(reportConfig);
        }

        AbstractHtmlExporter htmlExporter = (AbstractHtmlExporter)exporter;

        // collecting the images into a map
        Map<String, byte[]> imagesMap = new LinkedHashMap<>();

        String resourcePattern;
        if (exportParameters.get(Argument.RUN_OUTPUT_IMAGES_URI) != null)
        {
             resourcePattern = exportParameters.get(Argument.RUN_OUTPUT_IMAGES_URI) + "{0}";
        }
        else
        {
            resourcePattern = "images/{0}";
        }
        SimpleHtmlExporterOutput htmlExporterOutput = (SimpleHtmlExporterOutput) htmlExporter.getExporterOutput();
        HtmlResourceHandler resourceHandler =
        	new MapHtmlResourceHandler(
            	new WebHtmlResourceHandler(resourcePattern),
            	imagesMap
        		);
        htmlExporterOutput.setImageHandler(resourceHandler);
        htmlExporterOutput.setResourceHandler(resourceHandler);
        ReportContext reportContext = (ReportContext) exportParameters.get(REPORT_CONTEXT_PARAM_NAME);
        if (reportContext != null)
        {
            reportContext.setParameterValue("contextPath", contextPath);// context path to be used by the font handler in JSON exporter
            reportContext.setParameterValue(HttpServletRequest.class.getName(), proxy); //before done via exporter.setParameter(ExportUtil.PARAMETER_HTTP_REQUEST, proxy);
        }
        htmlExporter.setReportContext(reportContext);
        final boolean allowInlineScripts = exportParameters.get(ALLOW_INLINE_SCRIPTS_PARAM_NAME) != null ? (Boolean)exportParameters.get(ALLOW_INLINE_SCRIPTS_PARAM_NAME) : false;
        final boolean interactive = exportParameters.get(INTERACTIVE_PARAM_NAME) != null ? (Boolean)exportParameters.get(INTERACTIVE_PARAM_NAME) : false;
        if(interactive && allowInlineScripts){
            htmlExporter.getJasperReportsContext().setProperty("com.jaspersoft.jasperreports.highcharts.html.export.type", "standalone");
        }
        htmlExporterOutput.setFontHandler(new WebHtmlResourceHandler(contextPath + "/reportresource?&font={0}"));
    }

    @Override
    public String getContentType() {
        return "text/html";
    }

    @Override
    public JasperReportsContext getJasperReportsContext() {
        // return copy of jasper reports context to allow modifications
        SimpleJasperReportsContext context = new SimpleJasperReportsContext();
        context.setParent(super.getJasperReportsContext());
        return context;
    }

    @Override
    protected ExporterOutput getExporterOutput(OutputStream output) {
        return new SimpleHtmlExporterOutput(output);
    }
}
