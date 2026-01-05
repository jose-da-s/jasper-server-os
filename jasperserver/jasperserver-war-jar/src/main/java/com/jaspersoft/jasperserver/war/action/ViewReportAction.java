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


import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.jaspersoft.jasperserver.api.JSProfileAttributeException;
import com.jaspersoft.jasperserver.api.JSShowOnlyErrorMessage;
import com.jaspersoft.jasperserver.api.JSValidationException;
import com.jaspersoft.jasperserver.api.common.domain.ValidationErrors;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.common.util.LocaleHelper;
import com.jaspersoft.jasperserver.api.engine.common.domain.Request;
import com.jaspersoft.jasperserver.api.engine.common.service.SecurityContextProvider;
import com.jaspersoft.jasperserver.api.engine.common.service.VirtualizerFactory;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.PaginationParameters;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.ReportUnitRequest;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.ReportUnitResult;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.EhcacheEngineService;
import com.jaspersoft.jasperserver.api.logging.audit.context.AuditContext;
import com.jaspersoft.jasperserver.api.logging.audit.domain.AuditEvent;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.RepositoryConfiguration;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.util.RepositoryUtils;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.api.security.IPadSupportFilter;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlValidationError;
import com.jaspersoft.jasperserver.api.engine.export.HyperlinkProducerFactoryFlowFactory;
import com.jaspersoft.jasperserver.war.util.JSExceptionUtils;
import com.jaspersoft.jasperserver.war.util.SessionObjectSerieAccessor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.json.export.GenericElementJsonHandler;
import net.sf.jasperreports.engine.fill.JRFillInterruptedException;
import net.sf.jasperreports.web.JRInteractiveException;
import net.sf.jasperreports.web.JRInteractiveRuntimeException;
import net.sf.jasperreports.j2ee.web.WebReportContext;
import net.sf.jasperreports.interactivity.actions.AbstractAction;
import net.sf.jasperreports.interactivity.actions.Action;
import net.sf.jasperreports.interactivity.actions.MultiAction;
import net.sf.jasperreports.interactivity.commands.CommandStack;
import net.sf.jasperreports.web.servlets.AsyncJasperPrintAccessor;
import net.sf.jasperreports.web.servlets.JasperPrintAccessor;
import net.sf.jasperreports.web.servlets.ReportExecutionStatus;
import net.sf.jasperreports.web.servlets.ReportPageStatus;
import net.sf.jasperreports.jackson.util.JacksonUtil;
import net.sf.jasperreports.web.util.WebConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.binding.convert.ConversionExecutionException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.DataBinder;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.web.util.JavaScriptUtils;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.core.collection.SharedAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.RequestContext;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.jaspersoft.jasperserver.api.logging.audit.domain.AuditEventType.INPUT_CONTROLS_QUERY;

/**
 * @author Ionut Nedelcu (ionutned@users.sourceforge.net)
 * @version $Id$
 */
public class ViewReportAction extends ReportParametersAction
{
	public final static String IC_REFRESH_KEY = EhcacheEngineService.IC_REFRESH_KEY;
	protected final static Log log = LogFactory.getLog(ViewReportAction.class);

	public static final String REPORTUNIT_URI = "reportUnit";
    private static final String ATTRIBUTE_ORGANIZATION_ID = "organizationId";
    private static final String ATTRIBUTE_PUBLIC_FOLDER_URI = "publicFolderUri";
    private static final String ATTRIBUTE_TEMP_FOLDER_URI = "tempFolderUri";
    public static final String ATTRIBUTE_EMPTY_REPORT_MESSAGE = "emptyReportMessage";

	private static final String REPORT_EXECUTION_ID = "reportExecutionId";

	public static final String ATTRIBUTE_HAS_INVISIBLE_IC_VALIDATION_ERRORS = "hasInvisibleICValidationErrors";
	public static final String ATTRIBUTE_INVISIBLE_IC_VALIDATION_ERROR_MESSAGES = "invisibleICValidationErrorMessages";
    
    public static final String ATTRIBUTE_DIRECT_EXPORT = "directExport";
    public static final String ATTRIBUTE_INPUT_CONTROLS_EXPORT = "inputControlsExport";
    
    protected static final String FLOW_ATTRIBUTE_WAIT_FOR_FINAL_REPORT_RESULT = "waitForFinalReportResult";

	private String flowAttributeInhibitRequestParsing;
	private String requestParameterPageIndex;
	private String flowAttributePageIndex;
	private String requestAttributeHtmlLinkHandlerFactory;
	private String flowAttributeDepth;//TODO remove?
	private String flowAttributeJasperPrintName;
	private String flowAttributeReportRequestId = "reportRequestId";//default value
	private HyperlinkProducerFactoryFlowFactory<HttpServletRequest, HttpServletResponse> hyperlinkProducerFactory;
	private String flowAttributeIsSubflow;
	private String requestParameterReportOutput;
	private String flowAttributeReportOutput;
	private String flowAttributeUseClientTimezone;
	private SessionObjectSerieAccessor jasperPrintAccessor;
	private WebflowReportContextAccessor reportContextAccessor;
	private VirtualizerFactory virtualizerFactory;
	private Map configuredExporters;
	private List exportersSupportedByiPad;
	private String attributeReportControlsLayout;
	private String attributeReportForceControls;
	private String attributeSavedInputsState;
	private String attributeControlsHidden;
    private String attributeDashboardParametersHasError;
	private String attributePaginationParameters = "paginationParameters";//default value
	private String attributeReportLocale;
    private String parameterReportLocale;
    private AuditContext auditContext;
	private String requestParameterAnchor;
    private String flowAttributeInitialPageIndex;
    private String flowAttributeInitialAnchor;

    // Next four properties have default values.
    private boolean defaultAsyncReport = true;
    private boolean defaultRecordDataSnapshot = true;
    private String parameterAsyncReport = "asyncReport";
    private String attributeAsyncReport = "asyncReport";

    private SecurityContextProvider securityContextProvider;
    private RepositoryConfiguration configuration;
    private JasperReportsContext jasperReportsContext;

    @Resource(name = "uiExceptionRouter")
    private UIExceptionRouter uiExceptionRouter;
    
    private int waitForFinalReportTime;

    /**
     * Key is used for detecting parameters which don't have default values on UI.
     */
    public static final String PARAMETERS_WITHOUT_DEFAULT_VALUES = "parametersWithoutDefaultValues";

	public static final String INPUT_CONTROL_VALUES_FROM_REQUEST = "inputControlValuesFromRequest";

    /**
     * Flag enables/disables showing dialog for mandatory input controls without default value.
     */
    private Boolean showDialogForMandatoryInputControlsWithoutDefaultValue = false;

    public void setConfiguration(RepositoryConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setSecurityContextProvider(SecurityContextProvider securityContextProvider) {
        this.securityContextProvider = securityContextProvider;
    }

    public void setJasperReportsContext(JasperReportsContext jasperReportsContext) {
        this.jasperReportsContext = jasperReportsContext;
    }

	public JasperReportsContext getJasperReportsContext() {
		return jasperReportsContext;
	}

    protected void createInputControlsAuditEvent() {
        auditContext.doInAuditContext(new AuditContext.AuditContextCallback() {
            public void execute() {
                auditContext.createAuditEvent(INPUT_CONTROLS_QUERY.toString());
            }
        });
    }

    protected void setResourceUriToInputControlsAuditEvent(final String resourceUri) {
        auditContext.doInAuditContext(INPUT_CONTROLS_QUERY.toString(), new AuditContext.AuditContextCallbackWithEvent() {
            public void execute(AuditEvent auditEvent) {
                if (auditEvent.getResourceUri() == null) {
                    auditEvent.setResourceUri(resourceUri);
                }
            }
        });
    }

    protected void closeInputControlsAuditEvent() {
        auditContext.doInAuditContext(INPUT_CONTROLS_QUERY.toString(), new AuditContext.AuditContextCallbackWithEvent() {
            public void execute(AuditEvent auditEvent) {
                auditContext.closeAuditEvent(auditEvent);
            }
        });
    }

	public Event checkForParams(RequestContext context) {
        createInputControlsAuditEvent();
		MutableAttributeMap flowScope = context.getFlowScope();

		Integer depth = flowScope.getInteger(getFlowAttributeDepth());
		if (depth == null) {
			depth = new Integer(0);
			flowScope.put(getFlowAttributeDepth(), depth);
		}

		boolean isSubflow = !context.getFlowExecutionContext().getActiveSession().isRoot();
		flowScope.put(getFlowAttributeIsSubflow(), Boolean.valueOf(isSubflow));
		
		// remember return Parent folder
		
		String folderURI = context.getRequestParameters().get("ParentFolderUri");
		if (folderURI != null) {
		   flowScope.put("ParentFolderUri", folderURI);
		}
		
		String standAlone = context.getRequestParameters().get("standAlone");
	    if (standAlone != null) {
		   flowScope.put("standAlone", standAlone);
	    }

		String reportOutput = context.getRequestParameters().get(getRequestParameterReportOutput());
		if (reportOutput != null) {
			flowScope.put(getFlowAttributeReportOutput(), reportOutput);
		}
		
		try {
			Integer initialPageIndex = context.getRequestParameters().getInteger(requestParameterPageIndex);

			if (initialPageIndex != null) {
				if (log.isDebugEnabled()) {
					log.debug("Setting initial page index to " + initialPageIndex);
				}

				flowScope.put(flowAttributeInitialPageIndex, initialPageIndex);
			}
		} catch (ConversionExecutionException e) {
			log.debug("Unable to parse page index parameter with value "
					+ context.getRequestParameters().get(requestParameterPageIndex));
		}

		String initialAnchor = context.getRequestParameters().get(requestParameterAnchor);
		if (initialAnchor != null) {
			if (log.isDebugEnabled()) {
				log.debug("Setting initial anchor to " + initialAnchor);
			}

			flowScope.put(flowAttributeInitialAnchor, initialAnchor);
		}

		setReportLocale(context);
		setAsyncReport(context);

		boolean parseRequest = toParseRequest(context);
		flowScope.put(getFlowAttributeUseClientTimezone(), Boolean.valueOf(!parseRequest));

        flowScope.put(PARAMETERS_WITHOUT_DEFAULT_VALUES, getMandatoryParametersWithoutDefaultValuesAsJSON(context));

		flowScope.put(ATTRIBUTE_HAS_INVISIBLE_IC_VALIDATION_ERRORS, false);
		Map<String, String[]> inputControlValuesFromRequest = new HashMap<>();;
		try {
			// Use formatted IC values from request only when there are no visible ICs
			if (!hasVisibleInputControls(context)) {
				inputControlValuesFromRequest = getFormattedInputControlValuesFromRequest(context);
			}
		} catch (JSValidationException e) {
			flowScope.put(ATTRIBUTE_HAS_INVISIBLE_IC_VALIDATION_ERRORS, true);
			String validationMessage = formatInputControlsValidationErrorMessage(e.getErrors());
			validationMessage = validationMessage.replaceAll("</?p>", "");
			context.getFlashScope().put(ATTRIBUTE_INVISIBLE_IC_VALIDATION_ERROR_MESSAGES, validationMessage);
		}

		flowScope.put(INPUT_CONTROL_VALUES_FROM_REQUEST, convertObjectToJSONString(inputControlValuesFromRequest));

        try {
			super.checkForParams(context);
        } catch (Exception e) {
			if ("true".equals(context.getRequestParameters().get(VIEW_AS_DASHBOARD_FRAME))) {
                //Then we run report as dashboard frame there is no input control validation
                //so only one validation is here - if we get exception just do not show this frame
                //this is not the best solution, so we need to think about better one.
                context.getFlashScope().put(getAttributeDashboardParametersHasError(), "true");
            } else {
            	// JRS-15040:
            	// if exception is JSExceptionWrapper then try to unwrap it as a profile attirbute exception
            	// if succeeded throw unwrapped one, otherwise throw originally wrapped exception.
            	// moved the code into one catch so we do not lose logic about hiding
            	// any exception for dashboard views.
            	if(e instanceof JSExceptionWrapper ){
	            	JSException jsException = JSExceptionUtils.extractCause(e, JSProfileAttributeException.class);
	    			if (jsException != null) {
	    				throw jsException;
	    			}  else {
	    				throw new RuntimeException(e);
	    			}
	    		} else{
            		throw new RuntimeException(e);
            	}
            }
        }

        closeInputControlsAuditEvent();
        return success();
	}

    /**
     * Returns list of parameter names which don't have default values in JSON format.
     * @param context RequestContext
     * @return JSONArray
     */
    protected String getMandatoryParametersWithoutDefaultValuesAsJSON(RequestContext context) {
        if (!showDialogForMandatoryInputControlsWithoutDefaultValue) {
            return convertObjectToJSONString(Collections.emptySet());
        }

        Set<String> parametersWithoutDefaults = getParametersWithoutDefaultValues(context);

        // Remove from set parameters names which input controls are not mandatory.
        for (InputControl ic : getInputControls(context)) {
            if (!ic.isMandatory()) parametersWithoutDefaults.remove(ic.getName());
        }

        return convertObjectToJSONString(parametersWithoutDefaults);
    }


	protected void setReportLocale(RequestContext context) {
		Locale locale = (Locale) context.getFlowScope().get(getAttributeReportLocale(), Locale.class);
		if (locale == null) {
			String localeCode = context.getRequestParameters().get(getParameterReportLocale());
			if (localeCode == null) {
				locale = LocaleContextHolder.getLocale();
			} else {
				locale = LocaleHelper.getInstance().getLocale(localeCode);
			}
			context.getFlowScope().put(getAttributeReportLocale(), locale);
		}
	}

	protected void setAsyncReport(RequestContext context) {
		Boolean asyncReportParam = context.getRequestParameters().getBoolean(getParameterAsyncReport());
		if (asyncReportParam != null) {
			context.getFlowScope().put(getAttributeAsyncReport(), asyncReportParam);
		}
	}

	protected void setReportUnitAttributes(RequestContext context, ReportUnit reportUnit) {
		super.setReportUnitAttributes(context, reportUnit);
		MutableAttributeMap flowScope = context.getFlowScope();

		boolean isDrill = context.getCurrentEvent() != null && "drillReport".equals(context.getCurrentEvent().getId());
		String standAlone = flowScope.getString("standAlone");
		String reportOutputType = flowScope.getString("reportOutput");

		// Originally this functionality was triggered using HTML form post that ended-up as server-side redirect.
		// After I've changed it be triggered by ajax => window.open to be able validate and show dialog on error.
		// Change caused a regression in adhoc export (JRS-13386 and JRS-14458).
		// Fixing by detecting export using "standAlone" flag (hardcoded in ReportGeneratorController#displayTempReportUnit)
		// and expecting "reportOutput" type to NOT match "html" (it's not enabled as an option on UI, thought does exist).
		// I included check for "html" to avoid braking IC inside report viewer,
		final boolean isExport = Boolean.valueOf(standAlone) &&
				!(reportOutputType == null || reportOutputType.isEmpty() || reportOutputType.equals("html"));

		flowScope.put(getAttributeReportControlsLayout(), new Byte(reportUnit.getControlsLayout()));
		flowScope.put(getAttributeReportForceControls(),
			isDrill || isExport ? Boolean.FALSE : Boolean.valueOf(reportUnit.isAlwaysPromptControls()));
	}

	protected boolean toParseRequest(RequestContext context) {
		Boolean inhibitRequestParsingAttr = context.getFlowScope().getBoolean(getFlowAttributeInhibitRequestParsing());
		boolean parseRequest = inhibitRequestParsingAttr == null || !inhibitRequestParsingAttr.booleanValue();
		return parseRequest;
	}

	protected void addReportExecutionParameters(RequestContext context, Map<String, Object> parameterValues) {
		if (virtualizerFactory != null) {
			parameterValues.put(JRParameter.REPORT_VIRTUALIZER, virtualizerFactory.getVirtualizer());
		}
		
		setIgnorePaginationParameter(context, parameterValues);
		setReportLocaleParameter(context, parameterValues);

		ReportContext reportContext = getReportContext(context);
		parameterValues.put(JRParameter.REPORT_CONTEXT, reportContext);
		parameterValues.put("HTTP_SERVLET_REQUEST", context.getExternalContext().getNativeRequest());//FIXME use constant
		((WebflowReportContext) reportContext).setParameterValue(JRParameter.REPORT_LOCALE, context.getFlowScope().get(getAttributeReportLocale(), Locale.class));
		
		 /* begin: JIVE actions */
		Action action = getAction(reportContext, jasperReportsContext);

		JSController controller = new JSController(jasperReportsContext);
		try {
			controller.runAction(reportContext, action);
		} catch (JRInteractiveException e) {
			throw new JRInteractiveRuntimeException(e.getMessage());
		}
		/* end */
	}

	protected void setReportLocaleParameter(RequestContext context, Map<String, Object> parameterValues) {
		Locale locale = (Locale) context.getFlowScope().get(getAttributeReportLocale(), Locale.class);
		if (locale != null) {
			parameterValues.put(JRParameter.REPORT_LOCALE, locale);
		}
	}

	protected void setIgnorePaginationParameter(RequestContext context, Map<String, Object> parameterValues) {
		PaginationParameters paginationParams = (PaginationParameters) context.getRequestScope().get(
				getAttributePaginationParameters(), PaginationParameters.class);
		if (paginationParams != null) {
			paginationParams.setReportParameters(parameterValues);
		}
	}

    public Event chooseExportMode(RequestContext context) {
        String reportOutput = (String)context.getFlowScope().get("reportOutput");
        Boolean reportForceControls = (Boolean)context.getFlowScope().get("reportForceControls");
        Boolean hasInputControls = (Boolean)context.getFlowScope().get("hasInputControls");
        //Hack to fix bug #31084: looking for some parameter which indicates that controls dialog should not be shown
        //in this case it is "action" request parameter which normally should not be there, it it's present - do not show dialog.
        //More correct fix will be to export report using not HTTP API but direct export
        //unfortunately this will take much efforts and will be more error-prone, so it could not be implemented
        //near to code freeze.
        String action = context.getRequestParameters().get("action");

        if (reportOutput == null || reportOutput.isEmpty() || reportOutput.equals("html")) {
            return getEventFactorySupport().event(this, "viewReport");
        } else if (hasInputControls && reportForceControls && action == null) {
        	context.getFlowScope().put(ATTRIBUTE_INPUT_CONTROLS_EXPORT, true);
            return getEventFactorySupport().event(this, "showInputControlsByExport");
        } else {
        	context.getFlowScope().put(ATTRIBUTE_DIRECT_EXPORT, true);
            return getEventFactorySupport().event(this, "exportReport");
        }
    }

	public Event runReport(RequestContext context) {
		// remove current executed report, and cancel the execution if still in progress
		removeCurrentJasperPrint(context);

		ReportUnitResult result = executeReport(context);
		waitForFinalReport(context, result);
		setJasperPrint(context, result);

		Integer pageIndex = getInitialPageIndex(result, context);
		if (pageIndex == null) {
			context.getFlowScope().remove(getFlowAttributePageIndex());
		} else {
			context.getFlowScope().put(getFlowAttributePageIndex(), pageIndex);
		}

		return success();
	}

	protected void waitForFinalReport(RequestContext context, ReportUnitResult result) {
		if (waitForFinalReportTime <= 0) {
			return;
		}

		JasperPrintAccessor printAccessor = result.getJasperPrintAccessor();
		if (!(printAccessor instanceof AsyncJasperPrintAccessor)) {//use an interface instead?
			return;
		}

		Boolean waitResult = (Boolean) context.getFlowScope().get(FLOW_ATTRIBUTE_WAIT_FOR_FINAL_REPORT_RESULT, Boolean.class);
		// only waiting for the first and if the previous waits were not in vain
		if (waitResult == null || waitResult) {
			long t0 = System.currentTimeMillis();
			boolean done = ((AsyncJasperPrintAccessor) printAccessor).waitForFinalJasperPrint(waitForFinalReportTime);
			if (log.isDebugEnabled()) {
				log.debug("waited for final report " + (System.currentTimeMillis() - t0) + ", done " + done);
			}

			context.getFlowScope().put(FLOW_ATTRIBUTE_WAIT_FOR_FINAL_REPORT_RESULT, done);
		}
	}

	protected Integer getInitialPageIndex(ReportUnitResult result, RequestContext context) {
		JasperPrintAccessor printAccessor = result.getJasperPrintAccessor();
		// first check whether there's an initial page index attribute
		Integer pageIndex = context.getFlowScope().getInteger(flowAttributeInitialPageIndex);
		if (pageIndex == null) {
			// then check whether there's an anchor attribute
			String anchor = context.getFlowScope().getString(flowAttributeInitialAnchor);
			if (anchor != null) {
				// this will wait for the report to complete, so that we'll have all anchors
				JasperPrint jasperPrint = printAccessor.getFinalJasperPrint();
				Map anchorIndexes = jasperPrint.getAnchorIndexes();
				JRPrintAnchorIndex anchorIndex = (JRPrintAnchorIndex) anchorIndexes.get(anchor);
				if (anchorIndex != null) {
					pageIndex = anchorIndex.getPageIndex();

					if (log.isDebugEnabled()) {
						log.debug("Resolved anchor " + anchor + " to page " + pageIndex);
					}
				} else {
					if (log.isDebugEnabled()) {
						log.debug("Anchor " + anchor + " not found in report");
					}
				}
			}
		} else {
			// checking whether the page exists, waiting until the page has been generated
			ReportPageStatus pageStatus = printAccessor.pageStatus(pageIndex - 1, null);
			if (pageStatus.pageExists()) {
				if (log.isDebugEnabled()) {
					log.debug("Using initial page index " + pageIndex);
				}

				--pageIndex;
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Page index " + pageIndex + " out of range for report with "
							+ printAccessor.getReportStatus().getTotalPageCount() + " pages");
				}

				// resetting page index because the page doesn't exist
				pageIndex = null;
			}
		}
		return pageIndex;
	}

    protected ReportUnitResult executeReport(RequestContext context) {
        long currentTime = System.currentTimeMillis();

        Map<String, Object> parameterValues = null;
        context.getFlashScope().put(ATTRIBUTE_EMPTY_REPORT_MESSAGE,
                getMessages().getMessage("jasper.report.view.empty", null, LocaleContextHolder.getLocale()));

        try {
        	ReportUnit reportUnit = getReportUnit(context);
        	if (reportUnit != null && reportUnit.getDataSource() != null) {
				getJasperReportsContext().setProperty("DUMMY_REPORT",
						Boolean.toString(reportLoadingService.isDummyReport(getExecutionContext(context), reportUnit.getDataSource())));
			}

			parameterValues = getReportParameterValuesFromRequest(context);
            if (context!=null&&context.getAttributes()!=null)
            	context.getAttributes().remove(IC_REFRESH_KEY);
        } catch (JSValidationException e) {
            final String validationMessage = formatInputControlsValidationErrorMessage(e.getErrors());
            context.getFlashScope().put(ATTRIBUTE_EMPTY_REPORT_MESSAGE, validationMessage);
            return createEmptyReportUnitResult(getReportURI(context));
        }
        ReportContext reportContext = getReportContext(context);
        int initialStackSize = 0;
		CommandStack commandStack = (CommandStack)reportContext.getParameterValue(AbstractAction.PARAM_COMMAND_STACK);
		if (commandStack != null) {
			initialStackSize = commandStack.getExecutionStackSize();
		}

		addReportExecutionParameters(context, parameterValues);
		
		String reportUnitUri = getReportURI(context);
		ReportUnitRequest request = new ReportUnitRequest(reportUnitUri, parameterValues);
		
		request.setCreateAuditEvent(true);
		request.setStartTime(currentTime);
		
		boolean async = isAsyncReport(context);
		request.setAsynchronous(async);
		
		String freshDataParam = context.getRequestParameters().get(Request.PARAM_NAME_FRESH_DATA);
		request.setUseDataSnapshot(!Boolean.parseBoolean(freshDataParam));
		request.setRecordDataSnapshot(isDefaultRecordDataSnapshot());
		
		ReportUnitResult result = null;
		try {
			result = executeReport(context, request);
		} catch (RuntimeException t) {
			undoAction(reportContext, initialStackSize);

            //throw UI exception which will not show error trace if it exists for specified cause.
            JSShowOnlyErrorMessage uiException = uiExceptionRouter.getUIException(t);
            if (uiException != null) {
                throw uiException;
            }

			JSException jsException = JSExceptionUtils.extractCause(t, JSProfileAttributeException.class);
            if (jsException != null) {
            	throw jsException;
			}

			throw t;
		}
		//TODO set virtualizer readonly
        
        setReportRequestId(context, request.getId());
		return result;
	}

    protected String formatInputControlsValidationErrorMessage(ValidationErrors validationErrors) {
        List<InputControlValidationError> errors = validationErrors.getErrors();

        StringBuilder builder = new StringBuilder();
        for (InputControlValidationError error: errors) {
            if (builder.length() > 0) {
                builder.append(", ");
            }

            builder.append(RepositoryUtils.getName(error.getInputControlUri()));
        }

        return getMessages().getMessage("jasper.report.view.controls.validation.failed",
                new Object[] {builder.toString()}, LocaleContextHolder.getLocale());
    }

    private ReportUnitResult createEmptyReportUnitResult(String uri) {
        JasperPrint jasperPrint = new JasperPrint();
        return new ReportUnitResult(uri, jasperPrint, null);
    }

    protected void saveInputControlsState(RequestContext context, Map<String, String[]> requestValues) {
        context.getFlowScope().put(getAttributeSavedInputsState(), requestValues);
    }

    protected Map<String, String[]> getInputControlsState(RequestContext context) {
        return  (Map<String, String[]>) context.getFlowScope().get(getAttributeSavedInputsState(), Map.class, Collections.EMPTY_MAP);
    }

    private void undoAction(ReportContext webReportContext, int initialStackSize) {
		CommandStack commandStack = (CommandStack)webReportContext.getParameterValue(AbstractAction.PARAM_COMMAND_STACK);
		if (commandStack != null) {
			for (int i = 0; i < (commandStack.getExecutionStackSize() - initialStackSize); i++) {
				commandStack.undo();
			}
		}
	}

    protected boolean isAsyncReport(RequestContext context) {
    	if (isDashboard(context)) {
    		// dashboard executions are not async because there's no toolbar/etc
    		return false;
    	}

    	Boolean asyncAttribute = context.getFlowScope().getBoolean(getAttributeAsyncReport());
    	return asyncAttribute == null ? defaultAsyncReport : asyncAttribute.booleanValue();
    }

	private Action getAction(ReportContext webReportContext, JasperReportsContext jrContext)
	{
		String jsonData = (String) webReportContext.getParameterValue("jr_action");	//FIXME use constant
		Action result = null;
		List<AbstractAction> actions = JacksonUtil.getInstance(jrContext).loadAsList(jsonData, AbstractAction.class);
		if (actions != null)
		{
			if (actions.size() == 1) {
				result = actions.get(0);
			} else if (actions.size() > 1){
				result = new MultiAction(actions);
			}

			((AbstractAction)result).init(jrContext, webReportContext);
		}
		return result;
	}

	protected void startReportExecution(RequestContext context, ReportUnitRequest request) {
		FlowExecutionKey key = context.getFlowExecutionContext().getKey();
		if (key != null) {
			// put flow key on session for ReportExecutionController
			SharedAttributeMap sessionScope = context.getExternalContext().getSessionMap();
			String sessionName = ReportExecutionController.REPORT_EXECUTION_PREFIX + key.toString();
			ReportExecutionAttributes reportExecution = new ReportExecutionAttributes(request.getId());
			sessionScope.put(sessionName, reportExecution);
		}
	}

	protected void endReportExecution(RequestContext context) {
		FlowExecutionKey key = context.getFlowExecutionContext().getKey();
		if (key != null) {
			SharedAttributeMap sessionScope = context.getExternalContext().getSessionMap();
			String sessionName = ReportExecutionController.REPORT_EXECUTION_PREFIX + key.toString();
			sessionScope.remove(sessionName);
		}
	}

	protected ReportUnitResult executeReport(RequestContext context,
			ReportUnitRequest request) {
		startReportExecution(context, request);
		try {
			ReportContext reportContext = getReportContext(context);
			request.setReportContext(reportContext);
			return (ReportUnitResult) getEngine().execute(getExecutionContext(context), request);
        } catch (JRFillInterruptedException e) {
            throw new ReportCanceledException(e);
        } catch (JRRuntimeException e){
            int indexIO = ExceptionUtils.indexOfThrowable(e, IOException.class);
            if (indexIO != -1){
                throw new JSShowOnlyErrorMessage(((Exception) ExceptionUtils.getThrowableList(e).get(indexIO)).getMessage());
            }
            throw e;
        } finally {
			endReportExecution(context);
        }
	}

	protected void setJasperPrint(RequestContext context, ReportUnitResult result) {
		ServletExternalContext externalContext = (ServletExternalContext) context.getExternalContext();
		//FIXME we could use the request id here
		String name = getJasperPrintAccessor().putObject((HttpServletRequest) externalContext.getNativeRequest(), result);

        setJasperPrintId(context, name);

        // place the jasperPrintAccessor on the reportContext to be used by JR Actions
        WebflowReportContext reportContext = getReportContextAccessor().getContext(context);
        reportContext.setParameterValue(WebReportContext.REPORT_CONTEXT_PARAMETER_JASPER_PRINT_ACCESSOR, result.getJasperPrintAccessor());
	}


	protected void removeCurrentJasperPrint(RequestContext context) {
		String jasperPrintName = getJasperPrintId(context);
		if (jasperPrintName != null) {
			ServletExternalContext externalContext = (ServletExternalContext) context.getExternalContext();

			ReportUnitResult result = (ReportUnitResult)getJasperPrintAccessor().getObject(
                    (HttpServletRequest) externalContext.getNativeRequest(), jasperPrintName);
			if (result != null) {
				// cancel the report execution if still in progress
				cancelReportExecution(context, result);

				if (virtualizerFactory != null) {
					virtualizerFactory.disposeReport(result);
				}

				ReportContext reportContext = result.getReportContext();
				if (reportContext != null) {
					JasperPrintAccessor reportContextJRPrint = (JasperPrintAccessor) reportContext.getParameterValue(
							WebReportContext.REPORT_CONTEXT_PARAMETER_JASPER_PRINT_ACCESSOR);
					//testing for object identity
					if (reportContextJRPrint == result.getJasperPrintAccessor()) {
						//it's safer to remove the JasperPrint object from the context
						//to reduce the risk of the object not getting garbage collected
						reportContext.removeParameterValue(WebReportContext.REPORT_CONTEXT_PARAMETER_JASPER_PRINT_ACCESSOR);
					}
				}
			}
			getJasperPrintAccessor().removeObject((HttpServletRequest) externalContext.getNativeRequest(), jasperPrintName);
		}
	}

	protected void removeCurrentReportContext(RequestContext context) {
		getReportContextAccessor().removeContext(context);
	}

	protected ReportUnitResult getCurrentReportResult(RequestContext context) {
		ReportUnitResult result = null;
		String jasperPrintName = getJasperPrintId(context);
		if (jasperPrintName != null) {
			ServletExternalContext externalContext = (ServletExternalContext) context.getExternalContext();

			result = (ReportUnitResult)getJasperPrintAccessor().getObject(
                    (HttpServletRequest) externalContext.getNativeRequest(), jasperPrintName);
		}
		return result;
	}

	protected void cancelReportExecution(RequestContext context, ReportUnitResult result)
	{
		JasperPrintAccessor printAccessor = result.getJasperPrintAccessor();
		if (printAccessor != null
				&& printAccessor.getReportStatus().getStatus() == ReportExecutionStatus.Status.RUNNING)
		{
			String requestId = getReportRequestId(context);
			if (requestId == null)
			{
				if (log.isDebugEnabled())
				{
					log.debug("No request Id found to cancel");
				}
			}
			else
			{
				if (log.isDebugEnabled())
				{
					log.debug("Canceling request " + requestId);
				}

				boolean canceled = getEngine().cancelExecution(requestId);
				if (canceled)
				{
					log.debug("Request " + requestId + " canceled: " + canceled);
				}
			}
		}
	}

	protected String getJasperPrintId(RequestContext context) {
		return context.getFlowScope().getString(getFlowAttributeJasperPrintName());
	}

	protected void setJasperPrintId(RequestContext context, String jasperPrintId) {
        context.getFlowScope().put(getFlowAttributeJasperPrintName(), jasperPrintId);
	}

	protected String getReportRequestId(RequestContext context) {
        return context.getFlowScope().getString(getFlowAttributeReportRequestId());
	}

	protected void setReportRequestId(RequestContext context, String requestId) {
        context.getFlowScope().put(getFlowAttributeReportRequestId(), requestId);
	}

	public Event navigate(RequestContext context) {
		Integer pageIndex = (Integer) context.getRequestParameters().getNumber(getRequestParameterPageIndex(), Integer.class);
        MutableAttributeMap flowScope = context.getFlowScope();
        if (pageIndex == null) {
			flowScope.remove(getFlowAttributePageIndex());
		} else {
			flowScope.put(getFlowAttributePageIndex(), pageIndex);
		}

        Event flowEvent;
        String ajaxParamValue = context.getRequestParameters().get("ajax");
        if (ajaxParamValue != null && ajaxParamValue.equals("true")) {
               flowEvent = result("ajaxPaginator");
        }else{
            flowScope.put(flowAttributeInitialPageIndex, pageIndex);
            flowEvent = result("directPaginator");
        }
        return flowEvent;
	}

	public Event initFlowScope(RequestContext context) {
		getReportContextAccessor().initFlowScope(context);
        ReportContext reportContext = getReportContext(context);
		reportContext.setParameterValue(WebConstants.REQUEST_PARAMETER_REPORT_URI, getReportURI(context));
        reportContext.setParameterValue(GenericElementJsonHandler.PARAMETER_CLEAR_CONTEXT_CACHE, Boolean.TRUE);
		return success();
	}

	public Event resetSearchCache(RequestContext context) {
		ReportContext reportContext = getReportContext(context);
		reportContext.setParameterValue("net.sf.jasperreports.search.term.highlighter", null);
		return success();
	}

	public Event prepareReportView(RequestContext context) throws Exception {
		if (getHyperlinkProducerFactory() != null) {
			context.getRequestScope().put(getRequestAttributeHtmlLinkHandlerFactory(), getHyperlinkProducerFactory());
		}
        MutableAttributeMap requestScope = context.getRequestScope();

        Object isIPadAttr = context.getExternalContext().getRequestMap().get(IPadSupportFilter.IS_IPAD);
        boolean isIPad = (isIPadAttr != null && Boolean.valueOf(isIPadAttr.toString()));

        Map exporters;

        if (isIPad && getExportersSupportedByiPad() != null) {
            exporters = new HashMap();
            for (Object o : getConfiguredExporters().entrySet()) {
                Map.Entry entry = (Map.Entry) o;

                if (getExportersSupportedByiPad().contains(entry.getKey())) {
                    exporters.put(entry.getKey(), entry.getValue());
                }
            }
        } else {
            exporters = getConfiguredExporters();
        }

		requestScope.put("configuredExporters", exporters);
		requestScope.put(ATTRIBUTE_ORGANIZATION_ID, securityContextProvider.getContextUser().getTenantId());
        requestScope.put(ATTRIBUTE_PUBLIC_FOLDER_URI, configuration.getPublicFolderUri());
        requestScope.put(ATTRIBUTE_TEMP_FOLDER_URI, configuration.getTempFolderUri());

        String executionId = context.getExternalContext().getRequestParameterMap().get("_executionId");
        if (executionId != null) {
            requestScope.put(REPORT_EXECUTION_ID, executionId);
        }

        requestScope.put(REPORT_PARAMETER_VALUES, new JSONObject(getInputControlsState(context)).toString());
        // UI needs access to POST parameters.
        requestScope.put(ALL_REQUEST_PARAMETERS, getRequestParametersAsJSON(context));

		JRPropertiesUtil propertiesUtil = JRPropertiesUtil.getInstance(jasperReportsContext);
		Integer tabsMaxCount = propertiesUtil.getIntegerProperty("com.jaspersoft.jasperserver.viewer.report.tabs.max.count", 4);
		Integer tabsLabelsMaxLength = propertiesUtil.getIntegerProperty("com.jaspersoft.jasperserver.viewer.report.tabs.labels.max.length", 16);
		requestScope.put("reportViewerTabsMaxCount", tabsMaxCount);
		requestScope.put("reportViewerTabsLabelsMaxLength", tabsLabelsMaxLength);

		StringBuilder exportersList = new StringBuilder("[");
		boolean firstItem = true;
		Map<String, ExporterConfigurationBean> configuredExporters = (Map<String, ExporterConfigurationBean>) exporters;
		for (Map.Entry<String, ExporterConfigurationBean> configuredExporter : configuredExporters.entrySet()) {
			if (!firstItem) {
				exportersList.append(",");
			} else {
				firstItem = false;
			}
			ExporterConfigurationBean exporter = configuredExporter.getValue();
			String exporterKey = configuredExporter.getKey();
			ReportUnit reportUnit = getReportUnit(context);
			String exportFilename = null;
			if (exporter.getCurrentExporter() != null && reportUnit != null) {
				exportFilename = exporter.getCurrentExporter().getDownloadFilename(null, reportUnit.getName());
			}

			String descriptionMessage = JavaScriptUtils.javaScriptEscape(
					getMessages().getMessage(exporter.getDescriptionKey(), null, LocaleContextHolder.getLocale()));

			exportersList.append("{\"type\": \"simpleAction\",");
			exportersList.append("\"text\": \"" + descriptionMessage + "\",");
			exportersList.append("\"action\": \"Report.exportReport\",");
			exportersList.append("\"actionArgs\": [\"" + exporterKey + "\"");
			if (StringUtils.isNotEmpty(exportFilename)) {
				exportersList.append(", \"" + exportFilename + "\"");
			}
			exportersList.append("]}");
		}
		exportersList.append("]");
		context.getFlowScope().put("exportersList", exportersList.toString());

		return success();
	}

	public Event cleanSession(RequestContext context) {
        removeCurrentJasperPrint(context);
        removeCurrentReportContext(context);

		return success();
	}

	public Event cleanAfterExport(RequestContext context) {
		//dispose the JasperPrint after a direct export
		//leave the report context if input controls were shown, it might be still needed
		Boolean directExport = context.getFlowScope().getBoolean(ATTRIBUTE_DIRECT_EXPORT);
		Boolean inputControlsExport = context.getFlowScope().getBoolean(ATTRIBUTE_INPUT_CONTROLS_EXPORT);

		if ((directExport != null && directExport)
				|| (inputControlsExport != null && inputControlsExport)) {
			removeCurrentJasperPrint(context);
		}

		if (directExport != null && directExport) {
			removeCurrentReportContext(context);
		}

		return success();
	}

	protected void initBinder(RequestContext context, DataBinder binder) {
		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
	}

    /* TODO this property may appear redundant due to changes in createWrappers */
	public String getFlowAttributeInhibitRequestParsing() {
		return flowAttributeInhibitRequestParsing;
	}

	public void setFlowAttributeInhibitRequestParsing(
			String flowAttributeInhibitRequestParsing) {
		this.flowAttributeInhibitRequestParsing = flowAttributeInhibitRequestParsing;
	}

	public String getFlowAttributePageIndex() {
		return flowAttributePageIndex;
	}

	public void setFlowAttributePageIndex(String flowAttributePageIndex) {
		this.flowAttributePageIndex = flowAttributePageIndex;
	}

	public String getRequestParameterPageIndex() {
		return requestParameterPageIndex;
	}

	public void setRequestParameterPageIndex(String requestParameterPageIndex) {
		this.requestParameterPageIndex = requestParameterPageIndex;
	}

	public String getRequestAttributeHtmlLinkHandlerFactory() {
		return requestAttributeHtmlLinkHandlerFactory;
	}

	public void setRequestAttributeHtmlLinkHandlerFactory(
			String requestAttributeHtmlLinkHandlerFactory) {
		this.requestAttributeHtmlLinkHandlerFactory = requestAttributeHtmlLinkHandlerFactory;
	}

	public String getFlowAttributeDepth() {
		return flowAttributeDepth;
	}

	public void setFlowAttributeDepth(String flowAttributeDepth) {
		this.flowAttributeDepth = flowAttributeDepth;
	}

	public String getFlowAttributeJasperPrintName() {
		return flowAttributeJasperPrintName;
	}

	public void setFlowAttributeJasperPrintName(
			String flowAttributeJasperPrintName) {
		this.flowAttributeJasperPrintName = flowAttributeJasperPrintName;
	}

	public HyperlinkProducerFactoryFlowFactory<HttpServletRequest, HttpServletResponse> getHyperlinkProducerFactory() {
		return hyperlinkProducerFactory;
	}

	public void setHyperlinkProducerFactory(
			HyperlinkProducerFactoryFlowFactory<HttpServletRequest, HttpServletResponse> hyperlinkProducerFactory) {
		this.hyperlinkProducerFactory = hyperlinkProducerFactory;
	}

	public String getFlowAttributeIsSubflow() {
		return flowAttributeIsSubflow;
	}

	public void setFlowAttributeIsSubflow(String requestAttributeIsSubflow) {
		this.flowAttributeIsSubflow = requestAttributeIsSubflow;
	}

	public String getFlowAttributeReportOutput() {
		return flowAttributeReportOutput;
	}

	public void setFlowAttributeReportOutput(String flowAttributeReportOutput) {
		this.flowAttributeReportOutput = flowAttributeReportOutput;
	}

	public String getRequestParameterReportOutput() {
		return requestParameterReportOutput;
	}

	public void setRequestParameterReportOutput(String requestParameterReportOutput) {
		this.requestParameterReportOutput = requestParameterReportOutput;
	}

	public String getFlowAttributeUseClientTimezone() {
		return flowAttributeUseClientTimezone;
	}

	public void setFlowAttributeUseClientTimezone(
			String flowAttributeUseClientTimezone) {
		this.flowAttributeUseClientTimezone = flowAttributeUseClientTimezone;
	}

	public SessionObjectSerieAccessor getJasperPrintAccessor() {
		return jasperPrintAccessor;
	}

	public void setJasperPrintAccessor(
			SessionObjectSerieAccessor jasperPrintAccessor) {
		this.jasperPrintAccessor = jasperPrintAccessor;
	}

	/**
	 *
	 */
	public VirtualizerFactory getVirtualizerFactory() {
		return virtualizerFactory;
	}

	/**
	 *
	 */
	public void setVirtualizerFactory(VirtualizerFactory virtualizerFactory) {
		this.virtualizerFactory = virtualizerFactory;
	}

	/**
	 * @return Returns the configuredExporters.
	 */
	public Map getConfiguredExporters() {
		return configuredExporters;
	}

	/**
	 * @param configuredExporters The configuredExporters to set.
	 */
	public void setConfiguredExporters(Map configuredExporters) {
		this.configuredExporters = configuredExporters;
	}

    public List getExportersSupportedByiPad() {
        return exportersSupportedByiPad;
    }

    public void setExportersSupportedByiPad(List exportersSupportedByiPad) {
        this.exportersSupportedByiPad = exportersSupportedByiPad;
    }

    public String getAttributeReportControlsLayout() {
		return attributeReportControlsLayout;
	}

	public void setAttributeReportControlsLayout(
			String attributeReportControlsLayout) {
		this.attributeReportControlsLayout = attributeReportControlsLayout;
	}

    /* TODO this property may appear redundant due to changes in createWrappers */
	public String getAttributeReportForceControls() {
		return attributeReportForceControls;
	}

	public void setAttributeReportForceControls(String attributeReportForceControls) {
		this.attributeReportForceControls = attributeReportForceControls;
	}

	public String getAttributeSavedInputsState() {
		return attributeSavedInputsState;
	}

	public void setAttributeSavedInputsState(String attributeLastInputsState) {
		this.attributeSavedInputsState = attributeLastInputsState;
	}

	public String getAttributeControlsHidden() {
		return attributeControlsHidden;
	}

	public void setAttributeControlsHidden(String attributeControlsHidden) {
		this.attributeControlsHidden = attributeControlsHidden;
	}

	public String getAttributeReportLocale() {
		return attributeReportLocale;
	}

	public void setAttributeReportLocale(String attributeReportLocale) {
		this.attributeReportLocale = attributeReportLocale;
	}

	public String getAttributePaginationParameters() {
		return attributePaginationParameters;
	}

	public void setAttributePaginationParameters(String attributePaginationParameters) {
		this.attributePaginationParameters = attributePaginationParameters;
	}

    public String getAttributeDashboardParametersHasError() {
        return attributeDashboardParametersHasError;
    }

    public void setAttributeDashboardParametersHasError(String attributeDashboardParametersHasError) {
        this.attributeDashboardParametersHasError = attributeDashboardParametersHasError;
    }

    public String getParameterReportLocale() {
		return parameterReportLocale;
	}

	public void setParameterReportLocale(String parameterReportLocale) {
		this.parameterReportLocale = parameterReportLocale;
	}

    public AuditContext getAuditContext() {
        return auditContext;
    }

    public void setAuditContext(AuditContext auditContext) {
        this.auditContext = auditContext;
    }

	public String getRequestParameterAnchor() {
		return requestParameterAnchor;
	}

	public void setRequestParameterAnchor(String requestParameterAnchor) {
		this.requestParameterAnchor = requestParameterAnchor;
	}

	public String getFlowAttributeInitialPageIndex() {
		return flowAttributeInitialPageIndex;
	}

	public void setFlowAttributeInitialPageIndex(
			String flowAttributeInitialPageIndex) {
		this.flowAttributeInitialPageIndex = flowAttributeInitialPageIndex;
	}

	public String getFlowAttributeInitialAnchor() {
		return flowAttributeInitialAnchor;
	}

	public void setFlowAttributeInitialAnchor(String flowAttributeInitialAnchor) {
		this.flowAttributeInitialAnchor = flowAttributeInitialAnchor;
	}

	public String getFlowAttributeReportRequestId() {
		return flowAttributeReportRequestId;
	}

	public void setFlowAttributeReportRequestId(String flowAttributeReportRequestId) {
		this.flowAttributeReportRequestId = flowAttributeReportRequestId;
	}

	public ReportContext getReportContext(RequestContext context) {
		return getReportContextAccessor().getContext(context);
    }

	public String getParameterAsyncReport() {
		return parameterAsyncReport;
	}

	public void setParameterAsyncReport(String parameterAsyncReport) {
		this.parameterAsyncReport = parameterAsyncReport;
	}

	public String getAttributeAsyncReport() {
		return attributeAsyncReport;
	}

	public void setAttributeAsyncReport(String attributeAsyncReport) {
		this.attributeAsyncReport = attributeAsyncReport;
	}

	public boolean isDefaultAsyncReport() {
		return defaultAsyncReport;
	}

	public void setDefaultAsyncReport(boolean defaultAsyncReport) {
		this.defaultAsyncReport = defaultAsyncReport;
	}

	public boolean isDefaultRecordDataSnapshot() {
		return defaultRecordDataSnapshot;
	}

	public void setDefaultRecordDataSnapshot(boolean defaultRecordDataSnapshot) {
		this.defaultRecordDataSnapshot = defaultRecordDataSnapshot;
	}

    public Boolean getShowDialogForMandatoryInputControlsWithoutDefaultValue() {
        return showDialogForMandatoryInputControlsWithoutDefaultValue;
    }

    public void setShowDialogForMandatoryInputControlsWithoutDefaultValue(Boolean showDialogForMandatoryInputControlsWithoutDefaultValue) {
        this.showDialogForMandatoryInputControlsWithoutDefaultValue = showDialogForMandatoryInputControlsWithoutDefaultValue;
    }

	public WebflowReportContextAccessor getReportContextAccessor() {
		return reportContextAccessor;
	}

	public void setReportContextAccessor(WebflowReportContextAccessor reportContextAccessor) {
		this.reportContextAccessor = reportContextAccessor;
	}

	public int getWaitForFinalReportTime() {
		return waitForFinalReportTime;
	}

	public void setWaitForFinalReportTime(int waitForFinalReportTime) {
		this.waitForFinalReportTime = waitForFinalReportTime;
	}


	public Event initInputControls(RequestContext context) {
		context.getAttributes().put(IC_REFRESH_KEY,Boolean.TRUE);
		ExecutionContextImpl.getRuntimeExecutionContext().getAttributes().add(IC_REFRESH_KEY);
		return success();
	}

}
