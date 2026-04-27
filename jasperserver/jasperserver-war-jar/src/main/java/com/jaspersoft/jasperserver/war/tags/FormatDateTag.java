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

import com.jaspersoft.jasperserver.api.common.util.TimeZoneContextHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.support.RequestContextUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class FormatDateTag extends TagSupport {
	
	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(FormatDateTag.class); 
	
	public static final String DEFAULT_DATE_FORMAT_MESSAGE = "date.format";
	public static final String DEFAULT_DATETIME_FORMAT_MESSAGE = "datetime.format";

	private Date value;
	private boolean time = true;
	private String pattern;
	private String patternMessage;
	
	public FormatDateTag () {
	}

	public Date getValue() {
		return value;
	}

	public void setValue(Date value) {
		this.value = value;
	}

	public boolean isTime() {
		return time;
	}

	public void setTime(boolean time) {
		this.time = time;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getPatternMessage() {
		return patternMessage;
	}

	public void setPatternMessage(String patternMessage) {
		this.patternMessage = patternMessage;
	}
	

	public int doStartTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		WebApplicationContext applicationContext = RequestContextUtils.findWebApplicationContext(request);
		Locale locale = RequestContextUtils.getLocale(request);
		TimeZone timezone = TimeZoneContextHolder.getTimeZone();
		
		String formattingPattern = getFormattingPattern(applicationContext, locale);
		SimpleDateFormat format = new SimpleDateFormat(formattingPattern, locale);
		format.setTimeZone(timezone);

		String formattedValue = format.format(getValue());
		
		try {
			//TODO html escaping?
			pageContext.getOut().write(formattedValue);
		} catch (IOException e) {
			log.error(e);
			throw new JspException(e);
		}

		return SKIP_BODY;
	}

	protected String getFormattingPattern(WebApplicationContext applicationContext, Locale locale) {
		String formattingPattern = getPattern();
		
		if (formattingPattern == null && getPatternMessage() != null) {
			formattingPattern = applicationContext.getMessage(getPatternMessage(), null, locale);
		}
		
		if (formattingPattern == null) {
			String message = isTime() ? DEFAULT_DATETIME_FORMAT_MESSAGE : DEFAULT_DATE_FORMAT_MESSAGE; 
			formattingPattern = applicationContext.getMessage(message, null, locale);
		}
		
		return formattingPattern;
	}

	public void release() {
		value = null;
		time = true;
		pattern = null;
		patternMessage = null;
		
		super.release();
	}
	
	
}

