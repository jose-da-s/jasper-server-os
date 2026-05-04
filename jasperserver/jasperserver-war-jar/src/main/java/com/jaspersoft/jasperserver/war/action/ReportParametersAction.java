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
package com.jaspersoft.jasperserver.war.action;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.JSValidationException;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.common.domain.impl.ValidationErrorsImpl;
import com.jaspersoft.jasperserver.api.common.util.StaticExecutionContextProvider;
import com.jaspersoft.jasperserver.api.engine.common.service.EngineService;
import com.jaspersoft.jasperserver.api.engine.common.service.ReportInputControlInformation;
import com.jaspersoft.jasperserver.api.engine.common.service.ReportInputControlsInformation;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.EhcacheEngineService;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.EngineServiceImpl;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.ReportLoadingService;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.CalendarFormatProvider;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.MessageSourceLoader;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.RepositoryContext;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.RepositoryUtil;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControlsContainer;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.service.ObjectPermissionService;
import com.jaspersoft.jasperserver.dto.reports.inputcontrols.InputControlState;
import com.jaspersoft.jasperserver.inputcontrols.cascade.CascadeResourceNotFoundException;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlValidationError;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlsLogicService;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlsValidationException;
import com.jaspersoft.jasperserver.inputcontrols.util.ReportParametersUtils;
import com.jaspersoft.jasperserver.war.util.ServletContextWrapper;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import jakarta.servlet.ServletRequest;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Ionut Nedelcu (ionutned@users.sourceforge.net)	
 * @version $Id$
 */
public abstract class ReportParametersAction extends FormAction implements ReportInputControlsAction
{

	public final static String IC_REFRESH_KEY = EhcacheEngineService.IC_REFRESH_KEY;
    private static final Log log = LogFactory.getLog(ReportParametersAction.class);
    protected static final String AJAX_RESPONSE_MODEL = "ajaxResponseModel";
    public static final String VIEW_AS_DASHBOARD_FRAME = "viewAsDashboardFrame";
    public static final String IS_DASHBOARD = "isDashboard";
    public static final String IS_IC_REORDERING_ENABLED = "isIcReorderingEnabled";
    /**
     * Key is used for sending applied report parameters to UI
     * when return from drill through in view report flow and when return back to parameters page in scheduling flow.
     */
    public static final String REPORT_PARAMETER_VALUES = "reportParameterValues";

    /**
     * Key is used for sending all request parameters, including POST, to UI
     * when running report with parameters in URL or request body (POST request).
     */
    public static final String ALL_REQUEST_PARAMETERS = "allRequestParameters";

    public static final String ESCAPED_REPORT_DESCRIPTION_KEY = "escapedReportDescription";

    private static final String COLUMN_VALUE_SEPARATOR = " | ";
    private static final int COLUMN_VALUE_SEPARATOR_LENGTH = COLUMN_VALUE_SEPARATOR.length();

    private String reportUnitAttrName;
    private String reportUnitObjectAttrName;
    private String messageSourceObjectAttrName;
    private String controlsDisplayFormAttrName;
    private String controlsDisplayViewAttrName;
    private String reportDisplayFormAttrName;
    private String calendarDatePatternAttrName;
    private String calendarDatetimePatternAttrName;
    private EngineService engine;
    private RepositoryService repository;
    private MessageSource messages;
    private String hasInputControlsAttrName;
    private String reportReadOnlyAttrName;
    private String reportFolderReadOnlyAttrName;
    private String staticDatePattern;
    private CalendarFormatProvider calendarFormatProvider;
    private String inputNamePrefix;
    private String attributeInputControlsInformation;
    private String inputControlsAttrName;
    @jakarta.annotation.Resource(name = "${bean.reportLoadingService}")
    protected ReportLoadingService reportLoadingService;
    @jakarta.annotation.Resource
    protected InputControlsLogicService inputControlsLogicService;
    private String attributeReportOptionsURI;
    @jakarta.annotation.Resource
    protected ServletContextWrapper servletContextWrapper;
    @jakarta.annotation.Resource
    protected ObjectPermissionService objectPermissionService;

    /**
     * Initialize Jackson mapper
     */
    protected ObjectMapper jsonMapper = new ObjectMapper();


    /**
     * Checks if ReportUnit has controls to be displayed.
     * Sets rendering views, calendar patterns, message sources into flow scope.
     * @param context
     * @return Event
     */
    public Event checkForParams(RequestContext context) {
        ReportUnit reportUnit = getReportUnit(context);
        if (reportUnit == null) throw new JSException("jsexception.view.report.could.not.load");

        setReportUnitAttributes(context, reportUnit);

        MutableAttributeMap flowScope = context.getFlowScope();
        flowScope.put(getCalendarDatePatternAttrName(), getCalendarDatePattern());
        flowScope.put(getCalendarDatetimePatternAttrName(), getCalendarDatetimePattern());

        ExecutionContext exContext = getExecutionContext(context);
        MessageSource messageSource = MessageSourceLoader.loadMessageSource(exContext, reportUnit, repository);
        if (messageSource != null) {
            context.getExternalContext().getSessionMap().put(getMessageSourceObjectAttrName(), messageSource);
        } else {
            context.getExternalContext().getSessionMap().remove(getMessageSourceObjectAttrName());
        }

        flowScope.put(getHasInputControlsAttrName(), hasVisibleInputControls(context));
        flowScope.put(getReportReadOnlyAttrName(), isReportReadOnly(context));
        flowScope.put(getReportFolderReadOnlyAttrName(), isReportFolderReadOnly(context));

        return success();
    }

    /**
     * Convert all provided parameters from request into JSONObject,
     * where key is a string and value is always array of strings even if there is single value.
     * @param context RequestContext
     * @throws JSONException
     * @return JSONObject
     */
    protected String getRequestParametersAsJSON(RequestContext context) {
        Map<String, String[]> nativeRequestMap = ((ServletRequest) context.getExternalContext().getNativeRequest()).getParameterMap();
        return convertObjectToJSONString(nativeRequestMap);
    }

    protected String convertObjectToJSONString(Object o) {
        try {
            return jsonMapper.writeValueAsString(o);
        } catch (IOException e) {
            throw new JSException(e);
        }
    }

    /**
     * Looks for InputControls in the flow scope, if they are absent, loads them.
     * @param context
     * @return List<InputControl>
     */
    protected List<InputControl> getInputControls(RequestContext context) {
        List<InputControl> controls = (List<InputControl>) context.getFlowScope().get(getInputControlsAttrName());

        if (controls == null) {
            ReportUnit reportUnit = getReportUnit(context);
            ExecutionContext exContext = getExecutionContext(context);
            exContext = ExecutionContextImpl.getRuntimeExecutionContext(exContext);
            controls = getInputControls(exContext, reportUnit);
            context.getFlowScope().put(getInputControlsAttrName(), controls);
        }
        return controls;
    }

    /**
     * Gets InputControls from ReportUnit and DataView.
     * @param exContext
     * @param container
     * @return List<InputControl>
     */
    protected List<InputControl> getInputControls(ExecutionContext exContext, InputControlsContainer container) {
        /* Get control refs from ReportUnit and DataView */
        return reportLoadingService.getInputControls(exContext, container);
    }

    /**
     *
     * @param exContext
     * @param reportUnit
     * @return
     */
    protected List<ResourceReference> getRuntimeInputControls(ExecutionContext exContext, ReportUnit reportUnit) {
        return reportLoadingService.getInputControlReferences(exContext, reportUnit);
    }

    /**
     * Gets ReportUnit from flow scope.
     * If it's not present there, gets ReportUnit uri from flow scope of request parameter map.
     * If Uri is resolved, resolve ReportUnit from repository and put in into flow scope.
     *
     * @param context
     * @return ReportUnit
     */
    protected ReportUnit getReportUnit(RequestContext context) {
        /* Resolve ReportUnit from flow scope */
        ReportUnit reportUnit = (ReportUnit) context.getFlowScope().get(getReportUnitObjectAttrName());
        if (reportUnit != null) return reportUnit;

        /* Resolve ReportUnit uri from flow scope and request parameter map */
        String reportUnitUri = getReportURI(context);

        /* Resolve ReportUnit from repository */
        if (reportUnitUri == null || reportUnitUri.trim().length() == 0) {
            reportUnit = null;
        } else {
            reportUnit = (ReportUnit) repository.getResource(getExecutionContext(context), reportUnitUri);
            if (reportUnit != null) {
                context.getFlowScope().put(getReportUnitObjectAttrName(), reportUnit);
                context.getFlowScope().put(ESCAPED_REPORT_DESCRIPTION_KEY, StringEscapeUtils.escapeJavaScript(reportUnit.getDescription()));
            }
        }

        return reportUnit;
    }

    /**
     * Resolve ReportUnit uri from flow scope and request parameter map
     * @param context
     * @return String ReportUnit uri
     */
    public String getReportURI(RequestContext context) {
        String reportUnitUri = (String) context.getFlowScope().get(getReportUnitAttrName());
        if (reportUnitUri == null) {
            reportUnitUri = context.getRequestParameters().get(getReportUnitAttrName());
            if (reportUnitUri != null) {
                context.getFlowScope().put(getReportUnitAttrName(), reportUnitUri);
            }
        }
        return reportUnitUri;
    }

    /**
     * Looks whether ReportUnit has InputControls
     * @param context
     * @return
     */
    protected boolean hasInputControls(RequestContext context) {
        return !getInputControls(context).isEmpty();
    }

    /**
     * Looks whether ReportUnit has read-only access for authenticated user.
     * This method is candidate for ObjectPermissionService next to the ObjectPermissionService.isObjectAdministrable()
     * @param context
     * @return
     */
    private boolean isReportReadOnly(RequestContext context) {
        return isResourceReadOnly(context, getReportUnit(context));
    }

    /**
     * Looks whether the ReportUnit's folder has read-only access for authenticated user.
     * @param context
     * @return
     */
    private boolean isReportFolderReadOnly(RequestContext context) {
        ReportUnit reportUnit = getReportUnit(context);
        return isResourceReadOnly(context, repository.getFolder(getExecutionContext(context), reportUnit.getParentFolder()));
    }

    /**
     * This method is candidate for ObjectPermissionService next to the ObjectPermissionService.isObjectAdministrable()
     * @param context
     * @param resource
     * @return
     */
    private boolean isResourceReadOnly(RequestContext context, Object resource) {
        ExecutionContext exContext = getExecutionContext(context);
        return objectPermissionService.isObjectReadOnlyAccessible(exContext, resource);
    }

    protected List<Object> getCurrentUserRecipients() {
        Authentication authenticationToken = SecurityContextHolder.getContext().getAuthentication();
        List<Object> recipients = new ArrayList<Object>();
        if (authenticationToken.getPrincipal() instanceof User) {
            User user = (User) authenticationToken.getPrincipal();
            recipients.add(user);
            recipients.addAll(user.getRoles());

            return recipients;
        } else {
            return null;
        }
    }


    /**
     * Looks whether ReportUnit has visible InputControls
     * @param context
     * @return
     */
    protected boolean hasVisibleInputControls(RequestContext context) {
        List<InputControl> controls = getInputControls(context);
        for (InputControl control : controls) {
            if (control.isVisible()) return true;
        }
        return false;
    }

    /**
     * Returns set of parameter names which don't have default values.
     * @param context
     * @return
     */
    protected Set<String> getParametersWithoutDefaultValues(RequestContext context) {
        Set<String> parameters = new HashSet<String>();
        ReportInputControlsInformation infos = getControlsInformation(context);

        for (String controlName : infos.getControlNames()) {
            ReportInputControlInformation info = infos.getInputControlInformation(controlName);
            if (info.getDefaultValue() == null) {
                parameters.add(controlName);
            }
        }
        return parameters;
    }

    /**
     * Set rendering views for report and input controls.
     * @param context
     * @param reportUnit
     */
    protected void setReportUnitAttributes(RequestContext context, ReportUnit reportUnit) {
        MutableAttributeMap flowScope = context.getFlowScope();
        String controlsView = reportUnit.getInputControlRenderingView();
        if (controlsView != null && controlsView.endsWith(".jsp")) {
            String controlsFlowView = controlsView.substring(0, controlsView.length() - ".jsp".length());
            flowScope.put(getControlsDisplayViewAttrName(), controlsFlowView);
        }
        flowScope.put(getControlsDisplayFormAttrName(), controlsView);
        String reportRenderingView = reportUnit.getReportRenderingView();
        if(StringUtils.isNotEmpty(reportRenderingView) && reportRenderingView.charAt(0) != '/') {
            reportRenderingView = servletContextWrapper.getJspPathPrefix() + reportRenderingView;
        }
        flowScope.put(getReportDisplayFormAttrName(), reportRenderingView);
        flowScope.put(IS_IC_REORDERING_ENABLED, reportUnit.getInputControls() != null && reportUnit.getInputControls().size() > 0);
    }

    /**
     * create an ExecutionContext to use for repo access
     * If the viewAsDashboardFrame request param is true, then allow exec-only perms for resources
     * @param reqContext
     * @return
     */
    protected static ExecutionContext getExecutionContext(RequestContext reqContext) {
        return getExecutionContext(StaticExecutionContextProvider.getExecutionContext(), reqContext);
    }

    /**
     * create an ExecutionContext to use for repo access
     * If the viewAsDashboardFrame request param is true, then allow exec-only perms for resources
     * @param exContext
     * @param reqContext
     * @return
     */
    protected static ExecutionContext getExecutionContext(ExecutionContext exContext, RequestContext reqContext) {
        return EngineServiceImpl.getRuntimeExecutionContext(exContext);
    }

    protected static boolean isDashboard(RequestContext reqContext) {
        String dashFrameParam = reqContext.getRequestParameters().get(VIEW_AS_DASHBOARD_FRAME);
        Boolean dashFlowParam = (Boolean) reqContext.getFlowScope().get(IS_DASHBOARD);
        return Boolean.valueOf(dashFrameParam) || (dashFlowParam != null && dashFlowParam);
    }

    /**
     * Look for input control values in the request
     * @param context
     * @return Map<String, String[]> the formatted input control values
     */
    public Map<String, String[]> getFormattedInputControlValuesFromRequest(RequestContext context) {
        ReportInputControlsInformation infos = getControlsInformation(context);
        List<InputControl> controls = getInputControls(context);
        String reportOptionURI = getReportOptionURI(context);
        String inputControlsContainerURI = reportOptionURI == null ? getReportURI(context) : reportOptionURI;

        Map<String, String[]> requestParametersForControlLogic = getRequestParametersForControlLogic(context, controls);

        try {
            /* Resolve values for controls. */
            List<InputControlState> inputControlStates = inputControlsLogicService.getValuesForInputControls(inputControlsContainerURI, infos.getControlNames(), requestParametersForControlLogic, false);

            // Flag only those input controls for which there has been provided a request parameter
            ValidationErrorsImpl validationErrors = new ValidationErrorsImpl();
            for (InputControlState state: inputControlStates) {
                if (state.getError() != null && requestParametersForControlLogic.get(state.getId()) != null) {
                    validationErrors.add(new InputControlValidationError(state.getError(), null, state.getError(), state.getUri(), null));
                }
            }

            if (!validationErrors.getErrors().isEmpty()) {
                throw new InputControlsValidationException(validationErrors);
            }

            Map<String, String[]> inputControlFormattedValues = ReportParametersUtils.getValueMapFromInputControlStates(inputControlStates);

            return inputControlFormattedValues;
        } catch (CascadeResourceNotFoundException e) {
            throw new JSException(String.format("Resource not found URI: %s Type: %s", e.getResourceUri(), e.getResourceType()));
        } catch (InputControlsValidationException e) {
            throw new JSValidationException(e.getErrors());
        }
    }

    /**
     * Look for report parameter values in request. If request has value for parameter, parse this value and return it.
     *
     * @param context RequestContext
     * @throws CascadeResourceNotFoundException
     * @return Map<String, Object> parsed parameter values from request.
     */
    public Map<String, Object> getReportParameterValuesFromRequest(RequestContext context) {
        ReportInputControlsInformation infos = getControlsInformation(context);
        List<InputControl> controls = getInputControls(context);
        String reportOptionURI = getReportOptionURI(context);
        String inputControlsContainerURI = reportOptionURI == null ? getReportURI(context) : reportOptionURI;

        Map<String, String[]> requestParametersForControlLogic = getRequestParametersForControlLogic(context, controls);

        try {
            /* Resolve values for controls. */
            List<InputControlState> inputControlStates = inputControlsLogicService.getValuesForInputControls(inputControlsContainerURI, infos.getControlNames(), requestParametersForControlLogic, false);
            checkInputControlStateForValidationErrors(inputControlStates);

            Map<String, String[]> inputControlFormattedValues = ReportParametersUtils.getValueMapFromInputControlStates(inputControlStates);
            Map<String, Object> inputControlValues = inputControlsLogicService.getTypedParameters(inputControlsContainerURI, inputControlFormattedValues);

            addCustomParameters(context, inputControlValues);
            saveInputControlsState(context, inputControlFormattedValues);
            return inputControlValues;
        } catch (CascadeResourceNotFoundException e) {
            throw new JSException(String.format("Resource not found URI: %s Type: %s", e.getResourceUri(), e.getResourceType()));
        } catch (InputControlsValidationException e) {
            throw new JSValidationException(e.getErrors());
        }
    }

    public String getReportOptionURI(RequestContext context) {
        return  (String)context.getFlowScope().get(getAttributeReportOptionsURI());
    }

    //checks input control states for errors and throws InputControlsValidationException if any validation error found
    private void checkInputControlStateForValidationErrors(List<InputControlState> inputControlStates)
            throws InputControlsValidationException {

        ValidationErrorsImpl validationErrors = new ValidationErrorsImpl();
        for (InputControlState state: inputControlStates) {
            if (state.getError() != null) {
                validationErrors.add(new InputControlValidationError(state.getError(), null, state.getError(), state.getUri(), null));
            }
        }

        if (!validationErrors.getErrors().isEmpty()) {
            throw new InputControlsValidationException(validationErrors);
        }
    }

    protected void saveInputControlsState(RequestContext context, Map<String, String[]> requestValues) {
        /* Do nothing */
    }

    protected Map<String, String[]> formatReportParameterValues(String reportUri, Map<String, Object> reportParameters) {
        if (reportParameters == null) return  new HashMap<String, String[]>();
        try {
            return inputControlsLogicService.formatTypedParameters(reportUri, reportParameters);
        } catch (CascadeResourceNotFoundException e) {
            throw new JSException(String.format("Resource not found URI: %s Type: %s", e.getResourceUri(), e.getResourceType()));
        } catch (InputControlsValidationException e) {
            throw new JSValidationException(e.getErrors());
        }
    }

    protected Map<String, String[]> getRequestParametersForControlLogic(RequestContext context, List<InputControl> controls) {
        ParameterMap requestParameters = context.getRequestParameters();
        Map<String, String[]> requestParametersForControlLogic = new HashMap<String, String[]>();
        for (InputControl control : controls) {
            requestParametersForControlLogic.put(control.getName(), requestParameters.getArray(control.getName()));
        }
        if (context!=null&&context.getAttributes()!=null&&context.getAttributes().contains(IC_REFRESH_KEY)) {
        	String[] t = {"true"};
        	requestParametersForControlLogic.put(IC_REFRESH_KEY, t);
        }
        return requestParametersForControlLogic;
    }


    protected void addCustomParameters(RequestContext context, Map<String, Object> parameterValues) {
        //nothing
    }

    protected InputValueProvider initialValueProvider(RequestContext context) {
        return defaultValuesProvider(context);
    }


    public String getAttributeReportOptionsURI()
    {
        return attributeReportOptionsURI;
    }

    public void setAttributeReportOptionsURI(String attributeReportOptionsURI)
    {
        this.attributeReportOptionsURI = attributeReportOptionsURI;
    }

    protected static abstract class InputValueProvider {
        private final InputValueProvider parent;

        protected InputValueProvider() {
            this(null);
        }

        protected InputValueProvider(InputValueProvider parent) {
            this.parent = parent;
        }

        public Object getValue(String inputName) {
            Object value;
            if (hasOwnValue(inputName)) {
                value = getOwnValue(inputName);
            } else if (parent != null) {
                value = parent.getValue(inputName);
            } else {
                value = null;
            }
            return value;
        }

        protected abstract boolean hasOwnValue(String inputName);

        protected abstract Object getOwnValue(String inputName);
    }

    protected static class MapValueProvider extends InputValueProvider {
        private final Map values;

        public MapValueProvider(Map values) {
            this.values = values;
        }

        public MapValueProvider(Map values, InputValueProvider parent) {
            super(parent);
            this.values = values;
        }

        protected boolean hasOwnValue(String inputName) {
            return values.containsKey(inputName);
        }

        protected Object getOwnValue(String inputName) {
            return values.get(inputName);
        }
    }

    protected InputValueProvider defaultValuesProvider(RequestContext context) {
        ReportInputControlsInformation controlsInfo = getControlsInformation(context);
        Map defaults = new HashMap();
        for (Iterator it = controlsInfo.getControlNames().iterator(); it.hasNext();) {
            String name = (String) it.next();
            ReportInputControlInformation info = controlsInfo.getInputControlInformation(name);
            if (info != null) {
                defaults.put(name, info.getDefaultValue());
            }
        }
        return new MapValueProvider(defaults);
    }

    /**
     * Get input ReportInputControlsInformation from flow scope, if it isn't present there, load it.
     * @param context
     * @return
     */
    protected ReportInputControlsInformation getControlsInformation(RequestContext context) {
        ReportInputControlsInformation info =
                (ReportInputControlsInformation) context.getFlowScope().get(getAttributeInputControlsInformation());

        if (info == null) {
            ExecutionContext executionContext = ExecutionContextImpl.getRuntimeExecutionContext(getExecutionContext(context));
            info = getEngine().getReportInputControlsInformation(executionContext,
                    getReportURI(context), Collections.emptyMap());

            if (info != null) {
                context.getFlowScope().put(getAttributeInputControlsInformation(), info);
            }
        }

        return info;
    }

    protected void setupThreadRepositoryContext(ExecutionContext exContext) {
        if (RepositoryUtil.getThreadRepositoryContext() == null) {
            RepositoryContext rc = new RepositoryContext();
            rc.setRepository(repository);
            rc.setExecutionContext(exContext);
            RepositoryUtil.setThreadRepositoryContext(rc);
        }
    }

    protected DateFormat getDateFormat(boolean interactiveParameters) {
        DateFormat format;

        if (interactiveParameters) {
            format = getCalendarFormatProvider().getDateFormat();
        } else {
            format = new SimpleDateFormat(getStaticDatePattern());
        }
        return format;
    }

    protected DateFormat getDatetimeFormat(boolean interactiveParameters) {
        DateFormat format;
        if (interactiveParameters) {
            format = getCalendarFormatProvider().getDatetimeFormat();
        } else {
            format = new SimpleDateFormat(getStaticDatePattern());
        }
        return format;
    }

    protected String getCalendarDatePattern()
    {
        return getCalendarFormatProvider().getCalendarDatePattern();
    }


    protected String getCalendarDatetimePattern()
    {
        return getCalendarFormatProvider().getCalendarDatetimePattern();
    }

    /*
     * method to get the reposervice object arguments: none returns:
     * RepositoryService
     */
    public RepositoryService getRepository() {
        return repository;
    }

    /*
     * method to set the reposervice object arguments: RepositoryService
     * returns: void
     */
    public void setRepository(RepositoryService repository) {
        this.repository = repository;
    }


    public MessageSource getMessages()
    {
        return messages;
    }

    public void setMessages(MessageSource messages)
    {
        this.messages = messages;
    }

    public EngineService getEngine() {
        return engine;
    }

    public void setEngine(EngineService engine) {
        this.engine = engine;
    }

    public String getReportUnitAttrName() {
        return reportUnitAttrName;
    }

    public void setReportUnitAttrName(String reportUnitAttrName) {
        this.reportUnitAttrName = reportUnitAttrName;
    }

    public String getHasInputControlsAttrName() {
        return hasInputControlsAttrName;
    }

    public void setHasInputControlsAttrName(String hasInputControlsAttrName) {
        this.hasInputControlsAttrName = hasInputControlsAttrName;
    }

    public String getReportReadOnlyAttrName() {
        return reportReadOnlyAttrName;
    }

    public void setReportReadOnlyAttrName(String reportReadOnlyAttrName) {
        this.reportReadOnlyAttrName = reportReadOnlyAttrName;
    }

    public String getReportFolderReadOnlyAttrName() {
        return reportFolderReadOnlyAttrName;
    }

    public void setReportFolderReadOnlyAttrName(String reportFolderReadOnlyAttrName) {
        this.reportFolderReadOnlyAttrName = reportFolderReadOnlyAttrName;
    }

    public String getStaticDatePattern() {
        return staticDatePattern;
    }

    public void setStaticDatePattern(String staticDatePattern) {
        this.staticDatePattern = staticDatePattern;
    }

    /**
     * @return Returns the reportUnitObjectAttrName.
     */
    public String getReportUnitObjectAttrName() {
        return reportUnitObjectAttrName;
    }

    /**
     * @param reportUnitObjectAttrName The reportUnitObjectAttrName to set.
     */
    public void setReportUnitObjectAttrName(String reportUnitObjectAttrName) {
        this.reportUnitObjectAttrName = reportUnitObjectAttrName;
    }

    /**
     * @return Returns the controlsDisplayFormAttrName.
     */
    public String getControlsDisplayFormAttrName() {
        return controlsDisplayFormAttrName;
    }

    public String getMessageSourceObjectAttrName() {
        return messageSourceObjectAttrName;
    }

    public void setMessageSourceObjectAttrName(String messageSourceObjectAttrName) {
        this.messageSourceObjectAttrName = messageSourceObjectAttrName;
    }

    /**
     * @param controlsDisplayFormAttrName The controlsDisplayFormAttrName to set.
     */
    public void setControlsDisplayFormAttrName(String controlsDisplayFormAttrName) {
        this.controlsDisplayFormAttrName = controlsDisplayFormAttrName;
    }

    /**
     * @return Returns the reportDisplayFormAttrName.
     */
    public String getReportDisplayFormAttrName() {
        return reportDisplayFormAttrName;
    }

    /**
     * @param reportDisplayFormAttrName The reportDisplayFormAttrName to set.
     */
    public void setReportDisplayFormAttrName(String reportDisplayFormAttrName) {
        this.reportDisplayFormAttrName = reportDisplayFormAttrName;
    }

    /**
     * @return Returns the calendarDatePatternAttrName.
     */
    public String getCalendarDatePatternAttrName()
    {
        return calendarDatePatternAttrName;
    }

    /**
     * @param calendarDatePatternAttrName The calendarDatePatternAttrName to set.
     */
    public void setCalendarDatePatternAttrName(String calendarDatePatternAttrName)
    {
        this.calendarDatePatternAttrName = calendarDatePatternAttrName;
    }

    public CalendarFormatProvider getCalendarFormatProvider() {
        return calendarFormatProvider;
    }

    public void setCalendarFormatProvider(
            CalendarFormatProvider calendarFormatProvider) {
        this.calendarFormatProvider = calendarFormatProvider;
    }

    public String getCalendarDatetimePatternAttrName() {
        return calendarDatetimePatternAttrName;
    }

    public void setCalendarDatetimePatternAttrName(
            String calendarDatetimePatternAttrName) {
        this.calendarDatetimePatternAttrName = calendarDatetimePatternAttrName;
    }

    public String getInputControlsAttrName() {
        return inputControlsAttrName;
    }

    public void setInputControlsAttrName(String inputControlsAttrName) {
        this.inputControlsAttrName = inputControlsAttrName;
    }

    public String getInputNamePrefix() {
        return inputNamePrefix;
    }

    public void setInputNamePrefix(String inputNamePrefix) {
        this.inputNamePrefix = inputNamePrefix;
    }

    public String getControlsDisplayViewAttrName() {
        return controlsDisplayViewAttrName;
    }

    public void setControlsDisplayViewAttrName(String controlsDisplayViewAttrName) {
        this.controlsDisplayViewAttrName = controlsDisplayViewAttrName;
    }

    public String getAttributeInputControlsInformation() {
        return attributeInputControlsInformation;
    }

    public void setAttributeInputControlsInformation(
            String attributeInputControlsInformation) {
        this.attributeInputControlsInformation = attributeInputControlsInformation;
    }

    public void setReportLoadingService(ReportLoadingService reportLoadingService) {
        this.reportLoadingService = reportLoadingService;
    }

    public InputControlsLogicService getInputControlsLogicService() {
        return inputControlsLogicService;
    }

    public void setInputControlsLogicService(InputControlsLogicService inputControlsLogicService) {
        this.inputControlsLogicService = inputControlsLogicService;
    }

    public ObjectPermissionService getObjectPermissionService() {
        return objectPermissionService;
    }

    public void setObjectPermissionService(ObjectPermissionService objectPermissionService) {
        this.objectPermissionService = objectPermissionService;
    }
}

