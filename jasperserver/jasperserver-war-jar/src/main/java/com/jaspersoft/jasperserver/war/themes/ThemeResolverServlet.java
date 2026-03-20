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

package com.jaspersoft.jasperserver.war.themes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.NamedThreadLocal;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * The servlet serves HTTP agents and provides theme resource content.
 * The servlet context is configured in web.xml.
 * The servlet uses ThemeCache to get the content and last update date.
 * It leverages browser cache by setting "Last-Modified" and reading
 * "If-Modified-Since" cookies. If resource has not changed, the servlet sends
 * "304 Not Modified"
 * 
 * @author asokolnikov
 */
public class ThemeResolverServlet extends HttpServlet {

    public static final String DATE_FORMAT_PATTERN = "EEE, d MMM yyyy HH:mm:ss z";

    // All HTTP date/time stamps MUST be represented in Greenwich Mean Time (GMT), without exception.
    // See: http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3.1
    private static final TimeZone GMT_ZONE = TimeZone.getTimeZone("GMT");

    protected static final Log log = LogFactory.getLog(ThemeResolverServlet.class);

    private ThemeCache themeCache;
    private ServletContext servletContext;

    // keeps thread unsafe instances of DateFormat
    private static final ThreadLocal<Map<Locale, DateFormat>> lastDateFormat = new ThreadLocal<Map<Locale, DateFormat>>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // Check if we can find a resource in the current theme
        //String resPath = req.getRequestURI().substring((req.getContextPath() + req.getServletPath()).length());
        String resPath = req.getRequestURI().substring(req.getContextPath().length() + 1);
        ThemeResource themeResource = themeCache.getThemeResource(resPath);
        if (themeResource == null) {
            resp.sendError(404);
            return;
        }

        // Set contentType
        String filename = resPath;
        if (filename.indexOf("/") >= 0) {
            filename = filename.substring(filename.lastIndexOf("/") + 1);
        }
        String contentType = servletContext.getMimeType(filename);
        if (contentType == null) {
            log.error("Cannot detect a response content type for the file : " + filename);
            resp.sendError(404);
            return;
        }
        resp.setContentType(contentType);

        // Get Last Modified date
        Date lastModified = themeResource.getLastModified();
        // Get rid of ms
        lastModified.setTime(lastModified.getTime() / 1000 * 1000);        

        // Set cache controlling HTTP Response Headers
        DateFormat df = getFormat(Locale.ENGLISH);

        resp.setHeader("Last-Modified", df.format(lastModified));

        // Send 304 if resource has not been modified since last time requested
        String ifModSince = req.getHeader("If-Modified-Since");
        try {
            Date modDate = df.parse(ifModSince);
            if (!lastModified.after(modDate)) {
                resp.setStatus(304);
                return;
            }
        } catch (Exception e) {
        }

        // Send the full content
        resp.setContentLength(themeResource.getContent().length);
        ServletOutputStream os = resp.getOutputStream();
        os.write(themeResource.getContent());
        os.flush();
        os.close();
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

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        servletContext = config.getServletContext();

        WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        themeCache = (ThemeCache) ctx.getBean("themeCache");
    }
}

