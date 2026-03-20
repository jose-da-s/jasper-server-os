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
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.support.RequestContextUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class CalendarInputTag extends BaseTagSupport {

	public static final String DEFAULT_DATE_FORMAT_MESSAGE = "calendar.date.format";
	public static final String DEFAULT_TIME_FORMAT_MESSAGE = "calendar.time.format";
	public static final String DEFAULT_DATETIME_SEPARATOR_MESSAGE = "calendar.datetime.separator";

	public static final String DEFAULT_IMAGE = "/images/cal.gif";

    private boolean date = true;
    private boolean time = true;
	private String formatPattern;
    private String timeFormatPattern;
    private String datetimeSeparator;
	private String name;
	private String value;
	private String timezoneOffset;
	private boolean readOnly = false;
	private String onchange;
    private String imageSrc;
    private String imageTipMessage;
    private String calendarInputJsp;
    private boolean showSecond = false;

	protected int doStartTagInternal() {
		return SKIP_BODY;
	}
	
	public int doEndTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		WebApplicationContext applicationContext = RequestContextUtils.findWebApplicationContext(request);
		Locale locale = RequestContextUtils.getLocale(request);

		Map attributes = new HashMap();
		attributes.put("name", name);
		attributes.put("value", value);
		attributes.put("onchange", onchange);
		attributes.put("readOnly", Boolean.valueOf(isReadOnly()));
		attributes.put("imageSrc", getImageSource(request));
		attributes.put("imageTooltip", getImageTooltip(applicationContext, locale));
		attributes.put("datePattern", getDatePattern(applicationContext, locale));
        attributes.put("timePattern", getTimePattern(applicationContext, locale));
        attributes.put("hasDate", Boolean.valueOf(date));
        attributes.put("hasTime", Boolean.valueOf(time));
		attributes.put("timezoneOffset", getOrCreateTimezoneOffset());
        attributes.put("showSecond", Boolean.valueOf(isShowSecond()));

		includeNested(getJsp(), attributes);
		return SKIP_BODY;
	}

    protected String getDatePattern(WebApplicationContext applicationContext, Locale locale) {
        return (this.formatPattern != null)? this.formatPattern : applicationContext.getMessage(DEFAULT_DATE_FORMAT_MESSAGE, null, locale);
    }

    protected String getTimePattern(WebApplicationContext applicationContext, Locale locale) {
        return (this.timeFormatPattern != null)? this.timeFormatPattern : applicationContext.getMessage(DEFAULT_TIME_FORMAT_MESSAGE, null, "hh:mm", locale);
    }

    protected String getDateTimeSeparator(WebApplicationContext applicationContext, Locale locale) {
        return (this.timeFormatPattern != null)? this.timeFormatPattern : applicationContext.getMessage(DEFAULT_DATETIME_SEPARATOR_MESSAGE, null, " ", locale);
    }

	protected String getImageSource(HttpServletRequest request) {
		String imageSource = imageSrc;
		if (imageSource == null) {
			imageSource = request.getContextPath() + DEFAULT_IMAGE;
		}
		return imageSource;
	}

	protected String getImageTooltip(WebApplicationContext applicationContext, Locale locale) {
		String message = null;
		if (imageTipMessage != null) {
			message = applicationContext.getMessage(imageTipMessage, null, locale);
		}
		return message;
	}

	protected String getOrCreateTimezoneOffset() {
		String tzOffset = timezoneOffset;
		if (tzOffset == null) {
			TimeZone timezone = TimeZoneContextHolder.getTimeZone();
			int offset = timezone.getOffset(System.currentTimeMillis());
			tzOffset = Integer.toString(offset);
		}
		return tzOffset;
	}

	protected String getJsp() {
		String jsp = calendarInputJsp;
		if (jsp == null) {
			jsp = getConfiguration().getCalendarInputJsp();
		}
		return jsp;
	}

	public void release() {
		date = true;
		time = true;
		formatPattern = null;
		name = null;
		value = null;
		readOnly = false;
		onchange = null;
		imageSrc = null;
		imageTipMessage = null;

		super.release();
	}

	public String getFormatPattern() {
		return formatPattern;
	}

	public void setFormatPattern(String formatPattern) {
		this.formatPattern = formatPattern;
	}

	public String getImageSrc() {
		return imageSrc;
	}

	public void setImageSrc(String imageSrc) {
		this.imageSrc = imageSrc;
	}

	public String getImageTipMessage() {
		return imageTipMessage;
	}

	public void setImageTipMessage(String imageTipMessage) {
		this.imageTipMessage = imageTipMessage;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOnchange() {
		return onchange;
	}

	public void setOnchange(String onChange) {
		this.onchange = onChange;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

    public boolean isDate() {
        return date;
    }

    public void setDate(boolean date) {
        this.date = date;
    }

    public boolean isTime() {
		return time;
	}

	public void setTime(boolean time) {
		this.time = time;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getTimezoneOffset() {
		return timezoneOffset;
	}

	public void setTimezoneOffset(String timezoneOffset) {
		this.timezoneOffset = timezoneOffset;
	}
	
	public String getCalendarInputJsp() {
		return calendarInputJsp;
	}

	public void setCalendarInputJsp(String calendarInputJsp) {
		this.calendarInputJsp = calendarInputJsp;
	}

    public String getTimeFormatPattern() {
        return timeFormatPattern;
    }

    public void setTimeFormatPattern(String tomeFormatPattern) {
        this.timeFormatPattern = tomeFormatPattern;
    }

    public String getDatetimeSeparator() {
        return datetimeSeparator;
    }

    public void setDatetimeSeparator(String datetimeSeparator) {
        this.datetimeSeparator = datetimeSeparator;
    }

    public boolean isShowSecond() {
        return showSecond;
    }

    public void setShowSecond(boolean showSecond) {
        this.showSecond = showSecond;
    }
}

