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
package com.jaspersoft.jasperserver.war.action.hyperlinks;

import com.jaspersoft.jasperserver.api.engine.export.HyperlinkProducerFlowFactory;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.BaseReportExecutionHyperlinkProducerFactory;
import com.jaspersoft.jasperserver.war.action.ReportParametersAction;
import net.sf.jasperreports.engine.JRPrintHyperlink;
import net.sf.jasperreports.engine.JRPrintHyperlinkParameter;
import net.sf.jasperreports.engine.JRPrintHyperlinkParameters;
import net.sf.jasperreports.engine.export.JRHyperlinkProducer;
import net.sf.jasperreports.engine.type.HyperlinkTargetEnum;
import net.sf.jasperreports.j2ee.web.WebReportContext;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class ReportExecutionHyperlinkProducerFactory extends BaseReportExecutionHyperlinkProducerFactory implements HyperlinkProducerFlowFactory<HttpServletRequest, HttpServletResponse> {
	
	private static final long serialVersionUID = 1L;
	
	private String attributeReportLocale;
	private String urlParameterReportLocale;
	
	public JRHyperlinkProducer getHyperlinkProducer(HttpServletRequest request, HttpServletResponse response) {
		HyperlinkProducer hyperlinkProducer = new HyperlinkProducer(request, response);
		return hyperlinkProducer;
	}
	
	
	public class HyperlinkProducer extends BaseHyperlinkProducer {
		
		private final HttpServletResponse response;
		private String contextPath;
		private String flowExecutionKey;
		private String reportUnit;
		private Locale reportLocale;
        private boolean viewAsDashboardFrame = false;

		public HyperlinkProducer(final HttpServletRequest request, HttpServletResponse response) {
			this.response = response;
			if(request != null) {
				flowExecutionKey = (String) request.getAttribute("flowExecutionKey");
				contextPath = request.getContextPath();
				reportUnit = (String) request.getAttribute(getUrlParameterReportUnit());
				reportLocale = (Locale) request.getAttribute(getAttributeReportLocale());
				viewAsDashboardFrame = (request.getAttribute(ReportParametersAction.VIEW_AS_DASHBOARD_FRAME) != null) &&
						request.getAttribute(ReportParametersAction.VIEW_AS_DASHBOARD_FRAME).toString().equalsIgnoreCase("true");
			}
		}
		
		public String getHyperlink(JRPrintHyperlink hyperlink) {
			String uri = super.getHyperlink(hyperlink);
			return response != null ? response.encodeURL(uri) : uri;
		}
		
		//FIXME is this still required?  we are no longer using ReportExecution hyperlinks for Jive
		protected boolean isJiveLink(JRPrintHyperlink hyperlink) {
			JRPrintHyperlinkParameters parameters = hyperlink.getHyperlinkParameters();
			if (parameters != null) {
				for (JRPrintHyperlinkParameter param : parameters.getParameters()) {
					if (WebReportContext.REQUEST_PARAMETER_REPORT_CONTEXT_ID.equals(param.getName())) {
						return true;
					}
				}
			}
			return false;
		}
		
		protected void appendSubflowParams(JRPrintHyperlink hyperlink, URLParameters urlParams) {
			if (flowExecutionKey != null) {
				String eventId = isJiveLink(hyperlink) ? "jiveRun" : "drillReport";
				urlParams.appendParameter("_eventId_" + eventId, "");
				urlParams.appendParameter("_flowExecutionKey", flowExecutionKey);
			}
		}

		@Override
		protected void appendAdditionalParameters(JRPrintHyperlink hyperlink, URLParameters urlParams) {
			if (hyperlink.getHyperlinkTarget() == HyperlinkTargetEnum.SELF) {
				appendSubflowParams(hyperlink, urlParams);
			}
			
			if (reportUnit != null && !urlParams.hasParameter(getUrlParameterReportUnit())) {
				urlParams.appendParameter(getUrlParameterReportUnit(), reportUnit);
			}
			
			if (reportLocale != null) {
				urlParams.appendParameter(getUrlParameterReportLocale(), reportLocale.toString());
			}
            if (viewAsDashboardFrame && !HyperlinkTargetEnum.BLANK.getName().equals(hyperlink.getLinkTarget())) {
                if (isJiveLink(hyperlink))
                	urlParams.appendParameter(ReportParametersAction.VIEW_AS_DASHBOARD_FRAME, "true");
                else
                	urlParams.appendParameter("decorate", "no");
            }
		}

		@Override
		protected void appendHyperlinkStart(JRPrintHyperlink hyperlink, StringBuffer sb) {			
			sb.append(contextPath);
		}

		protected Locale getLocale() {
			return LocaleContextHolder.getLocale();
		}
	}

	public String getAttributeReportLocale() {
		return attributeReportLocale;
	}

	public void setAttributeReportLocale(String attributeReportLocale) {
		this.attributeReportLocale = attributeReportLocale;
	}

	public String getUrlParameterReportLocale() {
		return urlParameterReportLocale;
	}

	public void setUrlParameterReportLocale(String urlParameterReportLocale) {
		this.urlParameterReportLocale = urlParameterReportLocale;
	}

}
