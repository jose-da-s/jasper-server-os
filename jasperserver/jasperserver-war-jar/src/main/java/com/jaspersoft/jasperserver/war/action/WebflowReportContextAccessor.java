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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.ReportContext;
import net.sf.jasperreports.j2ee.web.WebReportContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.execution.RequestContext;

import com.jaspersoft.jasperserver.war.util.SessionObjectSerieAccessor;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class WebflowReportContextAccessor {

	private static final Log log = LogFactory.getLog(WebflowReportContextAccessor.class);
	
	private String parameterFlowReportContext = "rptCtx";//"jr.ctxid";
	private String requestParameterReportContextId = WebReportContext.REQUEST_PARAMETER_REPORT_CONTEXT_ID;

	private SessionObjectSerieAccessor sessionAccessor;
	
    public void initFlowScope(RequestContext requestContext) {
        Map flowValues = new HashMap();
        
        requestContext.getRequestParameters().asMap();
        flowValues.putAll(requestContext.getRequestParameters().asMap());
        
        // try to preserve REQUEST_PARAMETER_REPORT_CONTEXT_ID
        Map oldFlowMap = (Map)requestContext.getFlowScope().get(parameterFlowReportContext);
        if (oldFlowMap != null) {
        	flowValues.put(requestParameterReportContextId, oldFlowMap.get(requestParameterReportContextId));
        }
        
        requestContext.getFlowScope().put(parameterFlowReportContext, flowValues);//FIXMEJIVE
    }
	
	public WebflowReportContext getContext(RequestContext requestContext) {
		return getContext(requestContext, true);
	}

	public WebflowReportContext getContext(RequestContext requestContext, boolean create) {
        ExternalContext ec = requestContext.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) ec.getNativeRequest();

        WebflowReportContext webflowReportContext = null;

        Map flowMap = (Map)requestContext.getFlowScope().get(parameterFlowReportContext);
        String reportContextId = null;
        if (flowMap != null) {
            reportContextId = (String)flowMap.get(requestParameterReportContextId);
        }

		if (reportContextId != null) {
			webflowReportContext = (WebflowReportContext) sessionAccessor.getObject(request, reportContextId);
		}
		
		if (log.isDebugEnabled()) {
			log.debug("flow found " + webflowReportContext + " for id " + reportContextId);
		}

		if (webflowReportContext == null) {
			webflowReportContext = new WebflowReportContext();
            String sessionId = sessionAccessor.putObject(request, webflowReportContext);
            webflowReportContext.setId(sessionId);
            
            if (log.isDebugEnabled()) {
            	log.debug("flow created " + webflowReportContext + " with id " + sessionId);
            }
            
            if (flowMap != null) {	// FIXME check this! it prevents the context from being recreated each time getInstance() is called
            	flowMap.put(requestParameterReportContextId, webflowReportContext.getId());
            	requestContext.getFlowScope().put(parameterFlowReportContext, flowMap);
            }
        }

        if (webflowReportContext != null) {
            webflowReportContext.setRequestContext(requestContext);
            webflowReportContext.setFlowValues(flowMap);
            webflowReportContext.setParameterValue(JRParameter.REPORT_CONTEXT, webflowReportContext);
        }
		return webflowReportContext;
	}

    public WebflowReportContext getContextById(HttpServletRequest request, String reportContextId) {
        return (WebflowReportContext) sessionAccessor.getObject(request, reportContextId);
    }

	public WebflowReportContext getContext(HttpServletRequest request, Map flowMap) {
        WebflowReportContext webflowReportContext = null;

        String reportContextId = null;
        if (flowMap != null) {
            reportContextId = (String)flowMap.get(requestParameterReportContextId);
        }

		if (reportContextId != null) {
			webflowReportContext = (WebflowReportContext) sessionAccessor.getObject(request, reportContextId);
		}
		
		if (log.isDebugEnabled()) {
			log.debug("request found " + webflowReportContext + " for id " + reportContextId);
		}

        if (webflowReportContext == null) {
            webflowReportContext = new WebflowReportContext();
            webflowReportContext.setFlowValues(flowMap);
            webflowReportContext.setParameterValue(JRParameter.REPORT_CONTEXT, webflowReportContext);
            String sessionId = sessionAccessor.putObject(request, webflowReportContext);
            webflowReportContext.setId(sessionId);
            
            if (log.isDebugEnabled()) {
            	log.debug("request created " + webflowReportContext + " with id " + sessionId);
            }
        }

		return webflowReportContext;
	}

	public void removeContext(RequestContext requestContext) {
        ExternalContext ec = requestContext.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) ec.getNativeRequest();
        
        Map flowMap = (Map)requestContext.getFlowScope().get(parameterFlowReportContext);
        String reportContextId = null;
        if (flowMap != null) {
            reportContextId = (String)flowMap.get(requestParameterReportContextId);
        }
        
        if (log.isDebugEnabled()) {
        	log.debug("found report context id " + reportContextId + " for removal");
        }
        
        if (reportContextId != null) {
        	ReportContext reportContext = (ReportContext) getSessionAccessor().removeObject(request, reportContextId);
        	if (reportContext != null) {
        		//safety measure to reduce the risk of objects from the report context not getting garbage collected
        		reportContext.clearParameterValues();
        	}
        }
	}
	
	public SessionObjectSerieAccessor getSessionAccessor() {
		return sessionAccessor;
	}

	public void setSessionAccessor(SessionObjectSerieAccessor sessionAccessor) {
		this.sessionAccessor = sessionAccessor;
	}

	public String getParameterFlowReportContext() {
		return parameterFlowReportContext;
	}

	public void setParameterFlowReportContext(String parameterFlowReportContext) {
		this.parameterFlowReportContext = parameterFlowReportContext;
	}

	public String getRequestParameterReportContextId() {
		return requestParameterReportContextId;
	}

	public void setRequestParameterReportContextId(String requestParameterReportContextId) {
		this.requestParameterReportContextId = requestParameterReportContextId;
	}
	
}
