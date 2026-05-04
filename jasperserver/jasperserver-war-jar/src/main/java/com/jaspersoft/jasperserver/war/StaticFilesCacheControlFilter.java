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
package com.jaspersoft.jasperserver.war;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The filter adds the following HTTP Response headers to the response if requested resource matches configured type :
 *
 * Pragma: [empty]
 * Cache-Control: max-age=[configured seconds], public
 * Expires: [now + configured seconds]
 *
 * Expected init parameters :
 * name: urlEndsWith
 * value: space separated list of possible URL suffixes, for example : [.js .css .png .gif .jpg]
 *
 * name: expiresAfterAccessInSecs
 * value: expiration time in seconds, for example 86400 (24 hours)
 *
 * User: Andrew Sokolnikov
 * Date: 11/29/12
 */
@Deprecated
public class StaticFilesCacheControlFilter implements Filter {

    public static final String URL_ENDS_WITH = "urlEndsWith";
    public static final String URL_STARTS_WITH = "urlStartsWith";
	public static final String EXCLUDE_PAGE_FROM_CACHE_REGEX = "excludePageFromCacheRegex";
    public static final String EXPIRES_AFTER_ACCESS_IN_SECS = "expiresAfterAccessInSecs";
    public static final String DATE_FORMAT_PATTERN = "EEE, d MMM yyyy HH:mm:ss z";

    // keeps thread unsafe instances of DateFormat
    private static final ThreadLocal<Map<Locale, DateFormat>> lastDateFormat =
            new ThreadLocal<Map<Locale, DateFormat>>();

    // All HTTP date/time stamps MUST be represented in Greenwich Mean Time (GMT), without exception.
    // See: http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3.1
    private static final TimeZone GMT_ZONE = TimeZone.getTimeZone("GMT");

    private static final Log log = LogFactory.getLog(StaticFilesCacheControlFilter.class);

    private Set<String> urlSuffixes = new HashSet<String>();
    private Set<String> urlPrefixes = new HashSet<String>();
    private Set<String> exclusionRegexSet = new HashSet<String>();
    private int expiresInSecs = 0;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Enumeration<String> paramNames = filterConfig.getInitParameterNames();
        while (paramNames != null && paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
			String value = filterConfig.getInitParameter(paramName);
            if (URL_ENDS_WITH.equals(paramName)) {
                if (value != null && value.length() > 0) {
                    String[] types = value.split(" ");
                    for (int i = 0; i < types.length; i++) {
                        if (types[i].length() > 0) {
                            urlSuffixes.add(types[i].toLowerCase());
                            log.debug("URL suffix added : " + types[i]);
                        }
                    }
                }
            } else if (URL_STARTS_WITH.equals(paramName)) {
                if (value != null && value.length() > 0) {
                    String[] types = value.split(" ");
                    for (int i = 0; i < types.length; i++) {
                        if (types[i].length() > 0) {
                            urlPrefixes.add(types[i].toLowerCase());
                            log.debug("URL prefix added : " + types[i]);
                        }
                    }
                }
			} else if (EXCLUDE_PAGE_FROM_CACHE_REGEX.equals(paramName)) {
				if (value == null)
					continue;
				String[] regexStrs = value.split(" ");
				exclusionRegexSet.addAll(Arrays.asList(regexStrs));
				log.debug("Added " + exclusionRegexSet.size() + " cache exclusion regex.");
            } else if (EXPIRES_AFTER_ACCESS_IN_SECS.equals(paramName)) {
                try {
                    expiresInSecs = Integer.parseInt(value);
                    log.debug("Expires in seconds set : " + expiresInSecs);
                } catch (Exception ex) {
                    log.error(EXPIRES_AFTER_ACCESS_IN_SECS + " should be a non-negative integer", ex);
                }
            } else {
                log.warn("Unknown parameter, ignoring : " + paramName);
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            String path = httpServletRequest.getServletPath();
            if (path != null) {
                String resourcePatch = httpServletRequest.getPathInfo() == null ? path : path.concat(httpServletRequest.getPathInfo());

				boolean excludeFromCacheFlag = false;
				for (String exclRegex : exclusionRegexSet) {
					if (path.matches(exclRegex)) {
						log.debug("Excluding the path from browser cache by not setting the response header: " + path);
						excludeFromCacheFlag = true;
						break;
					}
				}

				if (!excludeFromCacheFlag) {
					boolean set = false;
					for (String suffix : urlSuffixes) {
						if (path.toLowerCase().endsWith(suffix)) {
							log.debug("Setting headers for " + path);
							setHeaders(httpServletRequest, httpServletResponse);
							set = true;
							break;
						}
					}
					if (!set){
						for (String prefix : urlPrefixes) {
							if (resourcePatch.toLowerCase().startsWith(prefix)) {
								log.debug("Setting headers for " + resourcePatch);
								setHeaders(httpServletRequest, httpServletResponse);
								break;
							}
						}
					}
				}
			}
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    private void setHeaders(HttpServletRequest request, HttpServletResponse response) {
        try {
            DateFormat df = getFormat(Locale.ENGLISH);

            response.setHeader("Cache-Control", "max-age=" + expiresInSecs + ", public");
            response.setHeader("Pragma", "");
            response.setHeader("Expires", df.format(new Date(new Date().getTime() + expiresInSecs * 1000)));

        } catch (Exception ex) {
            log.warn("Cannot set cache headers", ex);
        }
    }

    private DateFormat getFormat(Locale locale) {
        Map<Locale, DateFormat> map = lastDateFormat.get();
        if (map == null) {
            map = new HashMap<Locale, DateFormat>();
            lastDateFormat.set(map);
        }
        DateFormat dateFormat = map.get(locale);
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN, locale);
            dateFormat.setTimeZone(GMT_ZONE);

            map.put(locale, dateFormat);
        }
        return dateFormat;
    }
}

