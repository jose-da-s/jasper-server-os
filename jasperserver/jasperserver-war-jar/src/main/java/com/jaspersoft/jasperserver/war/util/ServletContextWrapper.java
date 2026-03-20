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

import java.net.MalformedURLException;
import java.net.URL;

import jakarta.servlet.ServletContext;

import org.springframework.web.context.ServletContextAware;

import com.jaspersoft.jasperserver.api.common.service.ServletContextInformation;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class ServletContextWrapper implements ServletContextAware, ServletContextInformation {

	private ServletContext servletContext;
	private String jspPathPrefix;

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public boolean jspExists(String path) {
		String finalPath = getJspPathPrefix() + path;
		try {
			URL resource = servletContext.getResource(finalPath);
			return resource != null;
		} catch (MalformedURLException e) {
			return false;
		}
	}

	public String getJspPathPrefix() {
		return jspPathPrefix;
	}

	public void setJspPathPrefix(String pathPrefix) {
		this.jspPathPrefix = pathPrefix;
	}
	
}

