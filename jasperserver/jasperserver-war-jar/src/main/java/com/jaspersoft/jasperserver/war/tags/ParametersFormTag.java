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
package com.jaspersoft.jasperserver.war.tags;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Ionut Nedelcu (ionutned@users.sourceforge.net)
 * @version $Id$
 */
public class ParametersFormTag extends TagSupport
{
	private static final Log log = LogFactory.getLog(ParametersFormTag.class);
	
	public static final String DEFAULT_RENDER_JSP = "/WEB-INF/jsp/modules/inputControls/DefaultParametersForm.jsp";
/*
	public static final String REPORT_PARAMETERS_ATTR = "reportparams";
	public static final String WRAPPERS_ATTR = "wrappers";
	public static final String SUBMIT_PARAM = "_eventId_submit";
	public static final String CANCEL_PARAM = "_eventId_cancel";
*/

	public static final String ATTRIBUTE_ON_INPUT_CHANGE = "onInputChange";
	public static final String ATTRIBUTE_INPUT_NAME_PREFIX = "inputNamePrefix";
	public static final String ATTRIBUTE_READ_ONLY = "readOnlyForm";

	private String renderJsp;
	private String reportName;
	private String onInputChange;
	private String inputNamePrefix;
	private boolean readOnly;

	public int doStartTag() throws JspException
	{
		setAttributes();
		
        return Tag.SKIP_BODY;
	}


	protected void setAttributes()
	{
		ServletRequest request = pageContext.getRequest();
		
		if (onInputChange != null && onInputChange.length() > 0)
		{
			request.setAttribute(ATTRIBUTE_ON_INPUT_CHANGE, onInputChange);
		}
		
		request.setAttribute(ATTRIBUTE_INPUT_NAME_PREFIX, getInputNamePrefix());
		request.setAttribute(ATTRIBUTE_READ_ONLY, Boolean.valueOf(isReadOnly()));
	}


	/**
	 *
	 */
	public int doEndTag() throws JspException
	{
		try {
			pageContext.include(getRenderJsp());

		} catch (Exception e) {
			log.error(e, e);
			throw new JspException(e);
		}

		return EVAL_PAGE;
	}


	/**
	 *
	 */
	public String getRenderJsp() {
		if (renderJsp == null || renderJsp.trim().length() == 0) {
			return DEFAULT_RENDER_JSP;
		}
		return "/WEB-INF/jsp/" + renderJsp;
	}

	/**
	 *
	 */
	public void setRenderJsp(String renderJsp) {
		this.renderJsp = renderJsp;
	}

	public String getReportName()
	{
		return reportName;
	}

	public void setReportName(String reportName)
	{
		this.reportName = reportName;
	}

	public String getOnInputChange()
	{
		return onInputChange;
	}

	public void setOnInputChange(String onInputChange)
	{
		this.onInputChange = onInputChange;
	}

	public String getInputNamePrefix()
	{
		return inputNamePrefix == null ? "" : inputNamePrefix;
	}

	public void setInputNamePrefix(String inputNamePrefix)
	{
		this.inputNamePrefix = inputNamePrefix;
	}

	public boolean isReadOnly()
	{
		return readOnly;
	}

	public void setReadOnly(boolean readOnly)
	{
		this.readOnly = readOnly;
	}
}

