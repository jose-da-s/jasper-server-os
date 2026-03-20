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

package com.jaspersoft.jasperserver.war.action.hyperlinks;

import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.engine.export.HyperlinkProducerFlowFactory;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.ReportUnitResult;
import com.jaspersoft.jasperserver.war.util.SessionObjectSerieAccessor;
import net.sf.jasperreports.engine.JRPrintAnchorIndex;
import net.sf.jasperreports.engine.JRPrintHyperlink;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRHyperlinkProducer;
import net.sf.jasperreports.web.servlets.JasperPrintAccessor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Map;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class LocalAnchorHyperlinkProducerFactory implements HyperlinkProducerFlowFactory<HttpServletRequest, HttpServletResponse>, Serializable {

	private static final long serialVersionUID = 1L;

	private SessionObjectSerieAccessor jasperPrintAccessor;
	private String jasperPrintNameRequestAttribute;
	private String flowControllerMapping;
	private String navigateEventID;
	private String pageIndexParameter;

	public JRHyperlinkProducer getHyperlinkProducer(HttpServletRequest request, HttpServletResponse response) {
		return new LocalAnchorHyperlinkProducer(request, response);
	}

	protected class LocalAnchorHyperlinkProducer implements JRHyperlinkProducer {
		private final HttpServletResponse response;
		private final String appContext;
		private final JasperPrintAccessor jasperPrintAccessor;
		private final String flowExecutionKey;
		
		public LocalAnchorHyperlinkProducer(HttpServletRequest request, HttpServletResponse response) {
			this.response = response;
			this.appContext = request.getContextPath();
			
			this.flowExecutionKey = (String) request.getAttribute("flowExecutionKey");

			String jasperPrintName = (String) request.getAttribute(getJasperPrintNameRequestAttribute());
			ReportUnitResult result = null;
			try {
				result = (ReportUnitResult) getJasperPrintAccessor().getObject(request, jasperPrintName);
			} catch (Exception e) {
				// not able to get report unit result from session. Do nothing.
			}
			if (result == null){
                result = (ReportUnitResult) request.getAttribute("reportResult");
            }
			this.jasperPrintAccessor = (result == null ? null : result.getJasperPrintAccessor());
			if (jasperPrintAccessor == null) {
				throw new JSException("jsexception.jasperprint.not.found", new Object[]{jasperPrintName});
			}
		}

		public String getHyperlink(JRPrintHyperlink hyperlink) {
			String ref = null;
			String anchor = hyperlink.getHyperlinkAnchor();
			if (anchor != null) {
				// get the JasperPrint object, waiting until it's final.
				// we need to get the final JasperPrint because otherwise we might not
				// locate an anchor which is on a page that has not yet been generated.
				JasperPrint jasperPrint = jasperPrintAccessor.getFinalJasperPrint();
				Map anchorIndexes = jasperPrint.getAnchorIndexes();
				JRPrintAnchorIndex anchorIndex = (JRPrintAnchorIndex) anchorIndexes.get(anchor);
				if (anchorIndex != null) {
					int page = anchorIndex.getPageIndex();
					ref = createAnchorURL(anchor, page);
				}
			} else if (hyperlink.getHyperlinkPage() != null) {
                ref = createAnchorURL(null, hyperlink.getHyperlinkPage().intValue() - 1);
            }
			return ref;
		}

		protected String createAnchorURL(String anchor, int page) {
			StringBuffer uri = new StringBuffer(200);
			uri.append(appContext);
			uri.append(getFlowControllerMapping());
			uri.append("?_eventId_");
			uri.append(getNavigateEventID());
			uri.append("=&");
			uri.append(getPageIndexParameter());
			uri.append("=");
			uri.append(page);
			uri.append("&_flowExecutionKey=");
			uri.append(flowExecutionKey);
            if (anchor != null) {
                uri.append("#");
                uri.append(anchor);
            }
			return response != null ? response.encodeURL(uri.toString()) : uri.toString();
		}
	}

	public String getJasperPrintNameRequestAttribute() {
		return jasperPrintNameRequestAttribute;
	}

	public void setJasperPrintNameRequestAttribute(String jasperPrintNameRequestAttribute) {
		this.jasperPrintNameRequestAttribute = jasperPrintNameRequestAttribute;
	}
	
	public String getFlowControllerMapping() {
		return flowControllerMapping;
	}

	public void setFlowControllerMapping(String flowControllerMapping) {
		this.flowControllerMapping = flowControllerMapping;
	}

	public String getNavigateEventID() {
		return navigateEventID;
	}

	public void setNavigateEventID(String navigateEventID) {
		this.navigateEventID = navigateEventID;
	}

	public String getPageIndexParameter() {
		return pageIndexParameter;
	}

	public void setPageIndexParameter(String pageIndexParameter) {
		this.pageIndexParameter = pageIndexParameter;
	}

	public SessionObjectSerieAccessor getJasperPrintAccessor() {
		return jasperPrintAccessor;
	}

	public void setJasperPrintAccessor(
			SessionObjectSerieAccessor jasperPrintAccessor) {
		this.jasperPrintAccessor = jasperPrintAccessor;
	}

}

