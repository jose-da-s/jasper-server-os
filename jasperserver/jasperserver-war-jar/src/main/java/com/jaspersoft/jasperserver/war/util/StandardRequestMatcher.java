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

package com.jaspersoft.jasperserver.war.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class StandardRequestMatcher implements ObjectMatcher<HttpServletRequest> {

	private static final Log log = LogFactory.getLog(StandardRequestMatcher.class);
	
	private String method;
	private String scheme;
	private Map<String, String> headers;
	
	// thread local (to avoid locking) compiled pattern cache
	private transient ThreadLocal<Map<String, Pattern>> compiledPatternCache = 
		new ThreadLocal<Map<String,Pattern>>() {
			@Override
			protected Map<String, Pattern> initialValue() {
				return new HashMap<String, Pattern>();
			}
	};
	
	public boolean matches(HttpServletRequest request) {
		if (log.isDebugEnabled()) {
			log.debug("Matching request " + request + " against " + this);
		}
		
		if (method != null && !matchesValue(request.getMethod(), method)) {
			if (log.isDebugEnabled()) {
				log.debug("Request method " + request.getMethod() 
						+ " does not match method " + method);
			}
			return false;
		}
		
		if (scheme != null && !matchesValue(request.getScheme(), scheme)) {
			if (log.isDebugEnabled()) {
				log.debug("Request scheme " + request.getScheme() 
						+ " does not match scheme " + scheme);
			}
			return false;
		}
		
		if (headers != null && !headers.isEmpty()) {
			for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
				String header = headerEntry.getKey();
				String headerPattern = headerEntry.getValue();
				String value = request.getHeader(header);
				if (!matchesValue(value, headerPattern)) {
					if (log.isDebugEnabled()) {
						log.debug("Request header " + header + ": " + value
								+ " does not match " + headerPattern);
					}
					return false;
				}
			}
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Request " + request + " matches " + this);
		}
		
		return true;
	}

	protected boolean matchesValue(String value, String pattern) {
		if (value == null) {
			//nulls are treated as empty strings so that they can be matched
			value = "";
		} else {
			//all patterns need to be lower case
			value = value.toLowerCase();
		}
		return compiledPattern(pattern).matcher(value).matches();
	}
	
	protected Pattern compiledPattern(String pattern) {
		Map<String, Pattern> compiledPatterns = compiledPatternCache.get();
		Pattern compiled  = compiledPatterns.get(pattern);
		if (compiled == null) {
			compiled = Pattern.compile(pattern);
			compiledPatterns.put(pattern, compiled);
		}
		return compiled;
	}
	
	public String toString() {
		return new ToStringBuilder(this)
			.append("method", method)
			.append("scheme", scheme)
			.append("headers", headers).toString();
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	
}

