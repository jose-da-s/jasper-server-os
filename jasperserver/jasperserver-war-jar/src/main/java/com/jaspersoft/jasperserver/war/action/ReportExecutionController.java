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
package com.jaspersoft.jasperserver.war.action;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.JSShowOnlyErrorMessage;
import com.jaspersoft.jasperserver.api.engine.common.service.EngineService;
import com.jaspersoft.jasperserver.api.engine.common.service.GlobalReportExecutionAccessor;
import com.jaspersoft.jasperserver.api.engine.export.HyperlinkProducerFactoryFlowFactory;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.ReportUnitResult;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.DataCacheProvider;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.CopyDestinationExistsException;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.ExportUtil;
import com.jaspersoft.jasperserver.api.metadata.common.service.JSResourceNotFoundException;
import com.jaspersoft.jasperserver.war.action.hyperlinks.ReportContextFactory;
import com.jaspersoft.jasperserver.war.util.SessionObjectSerieAccessor;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.ReportContext;
import net.sf.jasperreports.engine.export.JRHyperlinkProducerFactory;
import net.sf.jasperreports.json.export.JsonExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.json.export.SimpleJsonExporterConfiguration;
import net.sf.jasperreports.json.export.SimpleJsonExporterOutput;
import net.sf.jasperreports.json.export.SimpleJsonReportConfiguration;
import net.sf.jasperreports.web.JRInteractiveException;
import net.sf.jasperreports.j2ee.web.WebReportContext;
import net.sf.jasperreports.interactivity.actions.AbstractAction;
import net.sf.jasperreports.interactivity.actions.Action;
import net.sf.jasperreports.interactivity.actions.MultiAction;
import net.sf.jasperreports.web.servlets.JasperPrintAccessor;
import net.sf.jasperreports.web.servlets.ReportExecutionStatus;
import net.sf.jasperreports.web.servlets.ReportPageStatus;
import net.sf.jasperreports.jackson.util.JacksonUtil;
import net.sf.jasperreports.interactivity.util.RequirejsModuleMapping;
import net.sf.jasperreports.web.util.WebHtmlResourceHandler;
import net.sf.jasperreports.j2ee.web.WebUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
@Controller
public class ReportExecutionController {

	private static final Log log = LogFactory.getLog(ViewReportAction.class);

	public static final String REPORT_EXECUTION_PREFIX = "flowReportExecution";
	public static final String REPORT_EXECUTION_ID_PREFIX = "flowReportExecutionId";

	public static final String REPORT_CONTEXT_HTML_PRINT_ID = "htmlReportJRPrintId";
	public static final String REPORT_CONTEXT_HTML_FLOW_KEY = "htmlReportFlowExecutionKey";

    public static final String JASPER_PRINT_PARAM_NAME = "jasperPrintName";
    public static final String JASPER_PRINT_ATTRIBUTE_NAME = "jasperPrintName";//same as MTReportExecutionHyperlinkProducerFactory.reportResultRequestName

    private static final String LICENSE_MANAGER = "com.jaspersoft.ji.license.LicenseManager";


	private static final View NULL_VIEW = new View() {
		public String getContentType() {
			return null;
		}

		public void render(Map model, HttpServletRequest request,
				HttpServletResponse response) throws Exception {
			// NOP
		}
	};

	private EngineService engineService;
	private SessionObjectSerieAccessor jasperPrintAccessor;
	private DataCacheProvider dataCacheProvider;
    private JasperReportsContext jasperReportsContext;
    private WebflowReportContextAccessor reportContextAccessor;
    private HyperlinkProducerFactoryFlowFactory<HttpServletRequest, HttpServletResponse> hyperlinkProducerFactory;
    @Resource(name="reportExecutionAccessor")
    private GlobalReportExecutionAccessor reportExecutionAccessor;
    private ReportContextFactory reportContextFactory;
    @Autowired
    private ApplicationContext applicationContext;
    @RequestMapping("/viewReportCancel.html")
	public ModelAndView viewReportCancel(HttpServletRequest req, HttpServletResponse res) {
		String flowExecutionKey = req.getParameter("_flowExecutionKey");
		String sessionName = REPORT_EXECUTION_PREFIX + flowExecutionKey;
        ReportExecutionAttributes execution =
			(ReportExecutionAttributes) req.getSession().getAttribute(sessionName);

		if (execution == null) {
			if (log.isDebugEnabled()) {
				log.debug("No report execution to cancel");
			}
		} else {
			boolean canceled = engineService.cancelExecution(execution.getRequestId());

			if (log.isDebugEnabled()) {
                log.debug("Report execution " + execution.getRequestId()
						+ " cancel status: " + canceled);
			}
		}

        //can not return NULL_VIEW here because empty response will
        //leeds to parser error on client side
        return new ModelAndView("json:result", Collections.singletonMap("result", "ok"));
	}
    @RequestMapping("/viewReportAsyncCancel.html")
	public ModelAndView viewReportAsyncCancel(HttpServletRequest req, HttpServletResponse res) throws Exception {
		ReportUnitResult result = getReportResult(req);
		String requestId = result == null ? null : result.getRequestId();

		LinkedHashMap<String, Object> actionResult = new LinkedHashMap<String, Object>();
		if (requestId == null) {
			if (log.isDebugEnabled()) {
				log.debug("No async report execution to cancel");
			}
		} else {
			boolean canceled = engineService.cancelExecution(requestId);

			if (log.isDebugEnabled()) {
                log.debug("Report execution " + requestId
						+ " cancel status: " + canceled);
			}

			JasperPrintAccessor resultPrintAccessor = result.getJasperPrintAccessor();
			try {
				// this will wait for the report to end
				resultPrintAccessor.getFinalJasperPrint();
			} catch (JRRuntimeException e) {
				// we don't need to handle the exception here, we're doing getReportStatus() below
			}

			putReportStatusResult(res, result, actionResult);
		}

		return new ModelAndView("json:result", Collections.singletonMap("result", actionResult));
	}

	protected String getReportName(HttpServletRequest req) {
		return req.getParameter(JASPER_PRINT_PARAM_NAME);
	}

	protected ReportUnitResult getReportResult(HttpServletRequest req, String jasperPrintName) {
		//needed by getHyperlinkProducerFactory().getHyperlinkProducerFactory()
		if (jasperPrintName != null && req.getAttribute(JASPER_PRINT_PARAM_NAME) == null)
		{
			req.setAttribute(JASPER_PRINT_PARAM_NAME, jasperPrintName);
		}

		ReportUnitResult result = (ReportUnitResult) getJasperPrintAccessor().getObject(req, jasperPrintName);
		return result;
	}

	protected ReportUnitResult getReportResult(HttpServletRequest req) {
        String jasperPrintName = req.getParameter(JASPER_PRINT_PARAM_NAME);
        if (jasperPrintName == null){
            jasperPrintName = req.getHeader(JASPER_PRINT_PARAM_NAME);
	}
        ReportUnitResult result = (ReportUnitResult) getJasperPrintAccessor().getObject(req, jasperPrintName);
        if(result == null){
            result = (ReportUnitResult) reportExecutionAccessor.getReportResult(jasperPrintName);
        }
        return result;

     }
    @RequestMapping("/viewReportPageUpdateCheck.html")
	public ModelAndView viewReportPageUpdateCheck(HttpServletRequest req, HttpServletResponse res) throws Exception {
		ReportUnitResult reportResult = getReportResult(req);
		JasperPrintAccessor printAccessor = reportResult == null ? null : reportResult.getJasperPrintAccessor();
		if (printAccessor == null) {
			return null;
		}

		String pageIdxParam = req.getParameter("pageIndex");
		Integer pageIndex = pageIdxParam == null ? null : Integer.valueOf(pageIdxParam);
		String pageTimestampParam = req.getParameter("pageTimestamp");
		Long pageTimestamp = pageTimestampParam == null ? null : Long.valueOf(pageTimestampParam);

		if (log.isDebugEnabled()) {
			log.debug("report page update check for " + reportResult.getRequestId()
					+ ", pageIndex: " + pageIndex + ", pageTimestamp: " + pageTimestamp);
		}

		LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
		putReportStatusResult(res, reportResult, result);
        ReportExecutionStatus reportStatus = printAccessor.getReportStatus();
        ReportPageStatus pageStatus = null;

        if (pageIndex != null && pageTimestamp != null) {
			pageStatus = printAccessor.pageStatus(pageIndex, pageTimestamp);
			boolean modified = pageStatus.hasModified();
			result.put("pageModified", modified);

			if (log.isDebugEnabled()) {
				log.debug("page modified " + modified);
			}
		}

		return new ModelAndView("json:result", Collections.singletonMap("result", result));
	}
    @RequestMapping("/runReportAction.html")
    public ModelAndView runReportAction(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String reportContextId = req.getParameter("jr_ctxid"); // FIXME use constant
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        if (reportContextId != null && req.getParameterMap().containsKey("jr_action")) {
            boolean shouldRefreshExecutionOutput = false;
            ReportContext reportContext = reportContextAccessor.getContextById(req, reportContextId);
            JasperReportsContext currentJasperReportsContext = jasperReportsContext;
            if (reportContext == null) {
                ReportUnitResult reportUnitResult = (ReportUnitResult) reportExecutionAccessor.getReportResult(reportContextId);
                if (reportUnitResult != null) {
                    reportContext = reportUnitResult.getReportContext();
                    currentJasperReportsContext = reportExecutionAccessor.getJasperReportsContext(reportContextId);
                    shouldRefreshExecutionOutput = true;

                    //make sure that the latest JasperPrint is available for the action
                    if (reportContext.getParameterValue("net.sf.jasperreports.web.jasper_print.accessor") != null){
                        reportContext.setParameterValue("net.sf.jasperreports.web.jasper_print.accessor", reportUnitResult.getJasperPrintAccessor());
                    }
                }
            }
            if (reportContext == null) {
                res.setStatus(400);
                result.put("msg", "Wrong parameters!");
            } else {
                Action action = getAction(req, reportContext, currentJasperReportsContext);
                JSController controller = new JSController(currentJasperReportsContext);

                if (reportContext.getParameterValue(WebReportContext.REPORT_CONTEXT_PARAMETER_JASPER_PRINT_ACCESSOR) == null) {
                    ReportUnitResult reportUnitResult = (ReportUnitResult) reportExecutionAccessor.getReportResult(reportContextId);
                    if (reportUnitResult != null) {
                        reportContext.setParameterValue(WebReportContext.REPORT_CONTEXT_PARAMETER_JASPER_PRINT_ACCESSOR, reportUnitResult.getJasperPrintAccessor());
                    }
                }

                try {
                    // clear search stuff before performing an action
                    if (action.requiresRefill()) {
                        reportContext.setParameterValue("net.sf.jasperreports.search.term.highlighter", null);
                    }

                    controller.runAction(reportContext, action);
                    result.put("contextid", reportContextId);

                    // FIXMEJIVE: actions shoud return their own ActionResult that would contribute with JSON object to the output
                    JsonNode actionResult = (JsonNode) reportContext.getParameterValue("net.sf.jasperreports.web.actions.result.json");
                    if (actionResult != null) {
                        result.put("actionResult", actionResult);
                        reportContext.setParameterValue("net.sf.jasperreports.web.actions.result.json", null);
                    }

                } catch (JRInteractiveException e) {
                    res.setStatus(500);
                    result = new LinkedHashMap<String, Object>();
                    result.put("msg", "The server encountered an error!");
                    result.put("devmsg", e.getMessage());
                } catch (CopyDestinationExistsException e){
                    res.setStatus(403);
                    result = new LinkedHashMap<String, Object>();
                    result.put("msg", e.getMessage());
                    result.put("code", "resource.already.exists");
                } catch (JSResourceNotFoundException e){
                    res.setStatus(403);
                    result = new LinkedHashMap<String, Object>();
                    result.put("msg", "Folder not found");
                    result.put("code", "folder.not.found");
                } catch (AccessDeniedException e){
                    res.setStatus(403);
                    result = new LinkedHashMap<String, Object>();
                    result.put("msg", e.getMessage());
                    result.put("code", "access.denied");
                }
                finally {
                    if (shouldRefreshExecutionOutput) {
                        reportExecutionAccessor.refreshOutput(reportContextId);
                    }
                }
            }
        } else {
            res.setStatus(400);
            result.put("msg", "Wrong parameters!");
        }

        return new ModelAndView("json:result", Collections.singletonMap("result", result));
    }

    private Action getAction(HttpServletRequest request, ReportContext webReportContext, JasperReportsContext jrContext) {
        String jsonData = request.getParameter("jr_action");	//FIXME use constant
        Action result = null;
        List<AbstractAction> actions = JacksonUtil.getInstance(jrContext).loadAsList(jsonData, AbstractAction.class);
        if (actions != null) {
            if (actions.size() == 1) {
                result = actions.get(0);
                AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
                beanFactory.autowireBeanProperties(result, AutowireCapableBeanFactory.AUTOWIRE_NO, false);
                beanFactory.initializeBean(result, result.getClass().getName());
            } else if (actions.size() > 1) {
                result = new MultiAction(actions);
            }

            ((AbstractAction) result).init(jrContext, webReportContext);
        }
        return result;
    }

    private final View REPORT_COMPONENTS_VIEW = new View() {
        public String getContentType() {
            return "application/json; charset=UTF-8";
        }

        public void render(Map model, HttpServletRequest request,
                           HttpServletResponse response) throws Exception {

            response.setContentType(getContentType());

            try {
                String reportName = getReportName(request);
                ReportUnitResult reportResult = getReportResult(request);
                JasperPrintAccessor jasperPrintAccessor = reportResult == null ? null : reportResult.getJasperPrintAccessor();
                if (jasperPrintAccessor == null) {
                    return;
                }

                request.setAttribute(JASPER_PRINT_ATTRIBUTE_NAME, reportName);//MTReportExecutionHyperlinkProducerFactory needs this
                request.setAttribute("reportResult", reportResult);

                ReportExecutionStatus reportStatus = jasperPrintAccessor.getReportStatus();
                if (reportStatus.getStatus() == ReportExecutionStatus.Status.ERROR)
                {
                    throw new JRRuntimeException("Error occurred during report generation", reportStatus.getError());
                }

                boolean hasPages = jasperPrintAccessor.pageStatus(0, null).pageExists();

                JsonExporter exporter = new JsonExporter(jasperReportsContext);

                SimpleJsonReportConfiguration jsonReportConfig = new SimpleJsonReportConfiguration();
                SimpleJsonExporterConfiguration jsonExporterConfig = new SimpleJsonExporterConfiguration();

                ReportPageStatus pageStatus = null;
				boolean isReportComponentsExportOnly = false;

                if (hasPages)
                {
                    String reportPage = request.getParameter("pageIndex");
					int pageIdx;
					if (reportPage == null) {
						isReportComponentsExportOnly = true;
						pageIdx = 0;
					} else {
						pageIdx = Integer.parseInt(reportPage);
					}

                    pageStatus = jasperPrintAccessor.pageStatus(pageIdx, null);

                    if (!pageStatus.pageExists())
                    {
                        throw new JRRuntimeException("Page " + pageIdx + " not found in report");
                    }

                    jsonReportConfig.setPageIndex(pageIdx);
                }

                ReportContext reportContext = reportContextFactory.getReportContext(reportResult.getReportContext());

                prepareExport(request, reportName, reportContext);

				exporter.setReportContext(reportContext);
                exporter.setExporterInput(new SimpleExporterInput(jasperPrintAccessor.getJasperPrint()));
				SimpleJsonExporterOutput jsonExporterOutput = new SimpleJsonExporterOutput(response.getWriter());
				String contextPath = (String)reportContext.getParameterValue("contextPath");// contextPath prepare by the html exporter in advance
				if (contextPath  == null)
				{
					contextPath = request.getContextPath();
				}
				jsonExporterOutput.setFontHandler(new WebHtmlResourceHandler(response.encodeURL(contextPath + "/reportresource?&font={0}")));
				jsonExporterOutput.setResourceHandler(new WebHtmlResourceHandler(response.encodeURL(contextPath + "/rest_v2/resources" + reportResult.getReportUnitURI() + "_files/{0}")));
                exporter.setExporterOutput(jsonExporterOutput);
				exporter.getExporterContext().setValue(ExportUtil.HTTP_SERVLET_REQUEST, request);//key does not really matter here, as first value of type request is taken, regardless of key
				jsonExporterConfig.setReportComponentsExportOnly(isReportComponentsExportOnly);

				JRHyperlinkProducerFactory hyperlinkFactory = getHyperlinkProducerFactory().getHyperlinkProducerFactory(
                        request, response);
				jsonReportConfig.setHyperlinkProducerFactory(hyperlinkFactory);
                exporter.setConfiguration(jsonReportConfig);
                exporter.exportReport();
                reportContext.setParameterValue("net.sf.jasperreports.engine.export.clear.json.cache", Boolean.FALSE);
            } catch(JRRuntimeException e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(),e);
                }
                response.setStatus(500);
                PrintWriter pw = response.getWriter();
                Map<String, Object> result = new HashMap<String, Object>();
                Map<String, String> map = new LinkedHashMap<String, String>();
                map.put("msg", e.getMessage());
                if (e.getCause() != null) {
                    map.put("devmsg", e.getCause().getMessage());
                } else {
                    map.put("devmsg", e.getMessage());
                }
                result.put("result", map);
                pw.write(JacksonUtil.getInstance(DefaultJasperReportsContext.getInstance()).getJsonString(result));
            } catch (ErrorDescriptorException e){
                response.getWriter().write(new ObjectMapper().writeValueAsString(e.getErrorDescriptor()));
            }
        }

        protected void prepareExport(HttpServletRequest request,
                                     String reportName, ReportContext reportContext) {
            String flowExecutionKey = null;
            if (reportContext != null) {
                String htmlReportName = (String) reportContext.getParameterValue(REPORT_CONTEXT_HTML_PRINT_ID);
                if (htmlReportName != null && htmlReportName.equals(reportName)) {
                    flowExecutionKey = (String) reportContext.getParameterValue(REPORT_CONTEXT_HTML_FLOW_KEY);
                }
            }

            // ReportExecutionHyperlinkProducerFactory needs this
            request.setAttribute("flowExecutionKey", flowExecutionKey);
        }
    };
    @RequestMapping("/getReportComponents.html")
    public ModelAndView getReportComponents(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView(REPORT_COMPONENTS_VIEW);
    }
    @RequestMapping("/getRequirejsConfig.html")
    public ModelAndView getRequirejsConfig(HttpServletRequest request, HttpServletResponse response) throws Exception {
        WebUtil webUtil = WebUtil.getInstance(getJasperReportsContext());

        List<RequirejsModuleMapping> requirejsMappings = getJasperReportsContext().getExtensions(RequirejsModuleMapping.class);
        Map<String, String> modulePaths = new LinkedHashMap<String, String>();

        for (RequirejsModuleMapping requirejsMapping : requirejsMappings) {
            String moduleName = requirejsMapping.getName();
            if (!modulePaths.containsKey(moduleName)) {
                String modulePath = requirejsMapping.getPath();

                if (requirejsMapping.isClasspathResource()) {
                    modulePath = ".." + webUtil.getResourceUri(request) + modulePath; //todo check ...
                }

                modulePaths.put(moduleName, modulePath);
            }
        }

        request.setAttribute("contextPath", request.getContextPath());
        request.setAttribute("modulePaths", modulePaths);

        return new ModelAndView("modules/requirejs.config");
    }


    protected void putReportStatusResult(HttpServletResponse res,
			ReportUnitResult reportResult, LinkedHashMap<String, Object> result) throws Exception {
		JasperPrintAccessor printAccessor = reportResult.getJasperPrintAccessor();
		ReportExecutionStatus reportStatus = printAccessor.getReportStatus();
		result.put("lastPartialPageIndex", reportStatus.getCurrentPageCount() - 1);

		String status;
		switch (reportStatus.getStatus()) {
		case FINISHED:
			status = "finished";
			Integer totalPageCount = reportStatus.getTotalPageCount();
			result.put("lastPageIndex", totalPageCount - 1);

			ReportContext reportContext = reportResult.getReportContext();
                DataCacheProvider.SnapshotSaveStatus snapshotSaveStatus =
					dataCacheProvider.getSnapshotSaveStatus(reportContext);
			if (snapshotSaveStatus != null) {
				result.put("snapshotSaveStatus", snapshotSaveStatus.toString());
			}

			if (log.isDebugEnabled()) {
				log.debug("report finished " + totalPageCount + " pages; snapshot status " + snapshotSaveStatus);
			}
			break;
		case ERROR:
			status = "error";
			handleReportUpdateError(res, reportStatus);
			break;
		case CANCELED:
			status = "canceled";

			if (log.isDebugEnabled()) {
				log.debug("report canceled");
			}
			break;
		case RUNNING:
		default:
			status = "running";

			if (log.isDebugEnabled()) {
				log.debug("report running");
			}
			break;
		}

		result.put("status", status);
	}

	protected void handleReportUpdateError(HttpServletResponse res, ReportExecutionStatus reportStatus) throws Exception {
		Throwable error = reportStatus.getError();
		if (log.isDebugEnabled()) {
			log.debug("report error " + error);// only message
		}
		// set a header so that the UI knows it's a report execution error
		res.setHeader("reportError", "true");
		// set as a header because we don't have other way to pass it
		res.setHeader("lastPartialPageIndex", Integer.toString(reportStatus.getCurrentPageCount() - 1));

		// throw an exception to get to the error page
		if (error instanceof Exception) {
			// copied from ViewReportAction.executeReport
			// note that the message is not localized
            int indexIO = ExceptionUtils.indexOfThrowable(error, IOException.class);
            if (indexIO != -1) {
                Exception sourceException = (Exception) ExceptionUtils.getThrowableList(error).get(indexIO);
				throw new JSShowOnlyErrorMessage(sourceException.getMessage());
            }

			throw (Exception) error;
		}

		throw new JSException("jsexception.view.report.error", error);
	}

	public EngineService getEngineService() {
		return engineService;
	}

	public void setEngineService(EngineService engineService) {
		this.engineService = engineService;
	}

	public SessionObjectSerieAccessor getJasperPrintAccessor() {
		return jasperPrintAccessor;
	}

	public void setJasperPrintAccessor(
			SessionObjectSerieAccessor jasperPrintAccessor) {
		this.jasperPrintAccessor = jasperPrintAccessor;
	}

	public DataCacheProvider getDataCacheProvider() {
		return dataCacheProvider;
	}

	public void setDataCacheProvider(DataCacheProvider dataCacheProvider) {
		this.dataCacheProvider = dataCacheProvider;
	}

    public JasperReportsContext getJasperReportsContext() {
        return this.jasperReportsContext;
    }

    public void setJasperReportsContext(JasperReportsContext jasperReportsContext) {
        this.jasperReportsContext = jasperReportsContext;
    }

    public WebflowReportContextAccessor getReportContextAccessor() {
        return this.reportContextAccessor;
    }

    public void setReportContextAccessor(WebflowReportContextAccessor reportContextAccessor) {
        this.reportContextAccessor = reportContextAccessor;
    }

	public HyperlinkProducerFactoryFlowFactory<HttpServletRequest, HttpServletResponse> getHyperlinkProducerFactory() {
		return hyperlinkProducerFactory;
	}

	public void setHyperlinkProducerFactory(
			HyperlinkProducerFactoryFlowFactory<HttpServletRequest, HttpServletResponse> hyperlinkProducerFactory) {
		this.hyperlinkProducerFactory = hyperlinkProducerFactory;
	}

    public void setReportContextFactory(ReportContextFactory reportContextFactory) {
        this.reportContextFactory = reportContextFactory;
    }
}
