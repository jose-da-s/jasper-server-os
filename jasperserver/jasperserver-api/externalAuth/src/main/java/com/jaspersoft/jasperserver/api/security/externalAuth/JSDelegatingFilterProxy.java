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
package com.jaspersoft.jasperserver.api.security.externalAuth;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import com.jaspersoft.jasperserver.api.security.ResponseHeaderUpdater;
import jakarta.servlet.Filter;
import jakarta.servlet.ServletException;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * External authentication config. support class.  This class creates 'external' proxies
 * for the key authentication filters.
 *
 * Using this class allows for a convenient drop-in external authentication configuration.
 * When an applicationContext-externalAuth-*.xml file is dropped into WEB-INF, JRS automatically
 * switches to an external authentication mode without any need to configure other application
 * context files.  If such a file is absent, JRS authenticates via an internal database.
 *
 * User: dlitvak
 * Date: 8/26/12
 */
public class JSDelegatingFilterProxy extends DelegatingFilterProxy implements InitializingBean {
	private Filter defaultFilter;

	public Filter getDefaultFilter() {
		return defaultFilter;
	}

	private ResponseHeaderUpdater responseHeadersUpdater;

	/**
	 *
	 * @param defaultFilter filter used if a filter with targetBeanName is not found in the application context
	 */
	public void setDefaultFilter(Filter defaultFilter) {
		this.defaultFilter = defaultFilter;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		if (responseHeadersUpdater != null) {
			responseHeadersUpdater.changeHeaders((HttpServletResponse)response, (HttpServletRequest) request);
		}
		super.doFilter(request, response, filterChain);
	}


	@Override
	public void afterPropertiesSet() throws ServletException {
		super.afterPropertiesSet();

		Assert.notNull(this.defaultFilter, "defaultFilter must be specified for " + this.getClass());
	}

	/**
	 * Initialize the proxy filter
	 * @param wac Web Application Context
	 * @return  proxy filter
	 * @throws ServletException
	 */
	@Override
	protected Filter initDelegate(WebApplicationContext wac) throws ServletException {
		try {
			Filter delegateFilter = super.initDelegate(wac);
			logger.warn("Filter proxy bean " + this.getTargetBeanName() + " discovered.");
			return delegateFilter;
		}
		catch (NoSuchBeanDefinitionException nsbde) {
			logger.warn(this.getTargetBeanName() + " bean was not \"dropped into\" application context.  Using " + this.defaultFilter.getClass() + " instead.");
			return this.defaultFilter;
		}
	}

	public void setResponseHeadersUpdater(ResponseHeaderUpdater responseHeadersUpdater) {
		this.responseHeadersUpdater = responseHeadersUpdater;
	}

}

