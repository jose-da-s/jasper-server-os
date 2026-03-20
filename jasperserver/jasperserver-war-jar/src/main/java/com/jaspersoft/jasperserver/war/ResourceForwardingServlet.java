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

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.jaspersoft.jasperserver.war.common.JasperServerHttpConstants.FORWARDED_PARAMETERS;

/**
 * This servlet forwards requests from a path like
 * http://server:port/jrs/runtime/38479AD4/[scripts or optimized-scripts]/path/resource
 * to
 * http://server:port/jrs/[scripts or optimized-scripts]/path/resource
 * The "38479AD4" part is dynamically generated and changes on server start.
 *
 * The purpose for it is to help a proper cache management on client side.
 * We want resources to be cached by a browser for as long as we can, to reduce traffic between
 * the browser and the server related to static files (like javascript, etc.)
 * But we also want these resources to be updated when a client upgrades the server or makes modifications.
 *
 * @author asokolnikov
 */
public class ResourceForwardingServlet extends HttpServlet {
    private static Logger logger = LogManager.getLogger(ResourceForwardingServlet.class);

    public static final String FORWARD_WHITELIST_PARAM = "forwardWhitelist";
    private Set<String> forwardWhitelist = new HashSet<String>();

    // Max number of the directories in the whitelist entries
    private int maxTokens = 0;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        String resourcePath = req.getPathInfo() == null ? path : path.concat(req.getPathInfo());
        String newResourcePath = resourcePath.replaceFirst("^/[^/]+/[^/]*", "");

        String[] tokens = newResourcePath.split("/");
        StringBuilder buff = new StringBuilder();
        boolean allowForward = false;
        int pathTokenCount = 0;
        for (String t : tokens) {
            if (t.length() <= 0)
                continue;

            // Number of the path tokens is already at max number (found in the servlet config)
            // The path has not been found in the whitelist.  No need to continue searching.
            if (pathTokenCount >= this.maxTokens)
                break;

            buff.append("/");
            buff.append(t);
            pathTokenCount++;

            String pathSubstr = buff.toString().toUpperCase();
            if (forwardWhitelist.contains(pathSubstr)) {
                if (logger.isDebugEnabled())
                    logger.debug("Resource path " + newResourcePath + " was found in the whitelist due to " + pathSubstr + " entry.");
                allowForward = true;
                break;
            }
        }

        if (!allowForward) {
            logger.warn("Resource path " + newResourcePath + " forbidden, because it is not whitelisted.");
            resp.sendError(403);
            return;
        }

        if (logger.isDebugEnabled())
            logger.debug("Forwarding resource path " + resourcePath + " to " + newResourcePath);

        if (req.getParameterMap() != null) {
            req.setAttribute(FORWARDED_PARAMETERS, req.getParameterMap());
        }
        req.getRequestDispatcher(newResourcePath).forward(req, resp);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        String forwardWhitelistStr = config.getInitParameter(FORWARD_WHITELIST_PARAM);
        if (forwardWhitelistStr != null && !forwardWhitelistStr.trim().isEmpty()) {
            for (String res : forwardWhitelistStr.split(",")) {
                int tokens = StringUtils.countMatches(res, "/");
                if (tokens > this.maxTokens)
                    this.maxTokens = tokens;
                this.forwardWhitelist.add(res.trim().toUpperCase());
            }
        }
    }

}

