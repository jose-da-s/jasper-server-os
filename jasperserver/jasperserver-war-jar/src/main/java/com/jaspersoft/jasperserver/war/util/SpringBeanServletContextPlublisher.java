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

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class SpringBeanServletContextPlublisher implements ServletContextListener {

	private final static Log log = LogFactory.getLog(SpringBeanServletContextPlublisher.class); 
	
	private final static String ATTRIBUTE_BEAN_NAMES = "sessionPublishedBeans";

	public void contextInitialized(ServletContextEvent sce) {
		ServletContext servletContext = sce.getServletContext();
		ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		String beanNamesAttr = servletContext.getInitParameter(ATTRIBUTE_BEAN_NAMES);
		String[] beanNames = beanNamesAttr.split("\\,");
		for (int i = 0; i < beanNames.length; i++) {
			String beanName = beanNames[i];
			Object bean = applicationContext.getBean(beanName);
			if (bean == null) {
				log.warn("Bean \"" + beanName + "\" not found");
			} else {
				servletContext.setAttribute(beanName, bean);
				
				if (log.isDebugEnabled()) {
					log.debug("Bean \"" + beanName + "\" published in the application context");
				}
			}
		}
	}

	public void contextDestroyed(ServletContextEvent sce) {
		// NOOP
		
	}

}

