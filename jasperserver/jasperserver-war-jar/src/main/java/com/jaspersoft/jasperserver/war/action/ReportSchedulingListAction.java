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

import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.error.handling.SecureExceptionHandler;
import com.jaspersoft.jasperserver.api.common.util.StaticExecutionContextProvider;
import com.jaspersoft.jasperserver.api.engine.scheduling.service.ReportSchedulingService;
import org.json.JSONObject;
import org.springframework.context.MessageSource;
import org.springframework.webflow.action.MultiAction;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class ReportSchedulingListAction extends MultiAction {

    public static final String AJAX_RESPONSE_MODEL = "ajaxResponseModel";

    private String reportUnitURIAttrName;
	private String jobListAttrName;
	private String selectedJobsParamName;
	private String attributeOwnerURI;
	
	private ReportSchedulingService schedulingService;

    @Resource(name="messageSource")
    protected MessageSource messages;

    @Resource
    protected SecureExceptionHandler secureExceptionHandler;

    public ReportSchedulingListAction() {
	}

	public ReportSchedulingService getSchedulingService() {
		return schedulingService;
	}

	public void setSchedulingService(ReportSchedulingService schedulingService) {
		this.schedulingService = schedulingService;
	}

	public String getJobListAttrName() {
		return jobListAttrName;
	}

	public void setJobListAttrName(String jobListAttrName) {
		this.jobListAttrName = jobListAttrName;
	}

	public String getReportUnitURIAttrName() {
		return reportUnitURIAttrName;
	}

	public void setReportUnitURIAttrName(String reportUnitURIAttrName) {
		this.reportUnitURIAttrName = reportUnitURIAttrName;
	}

	public String getSelectedJobsParamName() {
		return selectedJobsParamName;
	}

	public void setSelectedJobsParamName(String selectedJobsParamName) {
		this.selectedJobsParamName = selectedJobsParamName;
	}

    public Event listJobs(RequestContext context) {
		setRequestErrorMessage(context);

        List jobs = loadJobList(context);
		context.getRequestScope().put(getJobListAttrName(), jobs);
		
		context.getRequestScope().put(getAttributeOwnerURI(), getOwnerURI(context));
		
		return success();
	}

    /**
     * Format exception text if possible, otherwise just use message from exception.
     * Place result in requestScope.errorPopupMessage
     *
     * @param context
     * @return
     */
    public Event formatException(RequestContext context) {
        Exception e = (Exception)context.getFlashScope().get("rootCauseException");
        String formattedMsg;
        if(e instanceof JSException) {
            ExecutionContext ctx = getExecutionContext(context);
            formattedMsg = messages.getMessage(e.getMessage(), ((JSException)e).getArgs(), ctx.getLocale());
        } else {
            formattedMsg = secureExceptionHandler.handleException(e).getMessage();
        }
        context.getRequestScope().put("errorPopupMessage", formattedMsg);
        return success();
    }

	protected String getOwnerURI(RequestContext context) {
		return context.getFlowScope().getRequiredString(getReportUnitURIAttrName());
	}

	protected List loadJobList(RequestContext context) {
		String reportUnitURI = context.getFlowScope().getString(getReportUnitURIAttrName());
		if (reportUnitURI == null) {
			reportUnitURI = (String)context.getRequestScope().get("reportUnitURI");
		}
		List jobs = schedulingService.getScheduledJobSummaries(getExecutionContext(context), reportUnitURI);
		return jobs;
	}

	protected void setRequestErrorMessage(RequestContext context) {
		MutableAttributeMap flowScope = context.getFlowScope();
		if (flowScope.contains("errorMessage")) {
			String message = (String) flowScope.remove("errorMessage");
			Object args = flowScope.remove("errorArguments");
			
			MutableAttributeMap requestScope = context.getRequestScope();
			requestScope.put("errorMessage", message);
			requestScope.put("errorArguments", args);
		}
	}

	public Event deleteJobs(RequestContext context) throws Exception {
		String jobId = context.getRequestParameters().get(getSelectedJobsParamName());
		if (jobId != null && jobId.length() > 0) {
			long id = Long.parseLong(jobId);
			schedulingService.removeScheduledJob(getExecutionContext(context), id);
		}

        JSONObject json = new JSONObject();
        json.put("result",true);
        json.put("id",jobId);
        if (schedulingService.getScheduledJobSummaries(getExecutionContext(context)).size() == 0) {
            json.put("empty",true);
        }
        context.getRequestScope().put(AJAX_RESPONSE_MODEL, json.toString());

		return success();
	}
	
	protected ExecutionContext getExecutionContext(RequestContext context) {
		return StaticExecutionContextProvider.getExecutionContext();
	}

	public String getAttributeOwnerURI() {
		return attributeOwnerURI;
	}

	public void setAttributeOwnerURI(String attributeOwnerURI) {
		this.attributeOwnerURI = attributeOwnerURI;
	}

    public Event redirectAfterEdit(RequestContext context) {
        context.getExternalContext().requestFlowExecutionRedirect();
        return success();
    }
}

