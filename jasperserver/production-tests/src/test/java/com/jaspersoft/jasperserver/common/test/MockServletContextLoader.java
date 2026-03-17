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
package com.jaspersoft.jasperserver.common.test;

import com.jaspersoft.jasperserver.test.ks.KeystoreUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.support.AbstractContextLoader;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import javax.servlet.ServletContext;

/**
 * User: dlitvak
 * Date: 9/14/12
 */
public class MockServletContextLoader extends AbstractContextLoader {

	protected static final Logger logger = LogManager.getLogger(MockServletContextLoader.class);

	public static final ServletContext SERVLET_CONTEXT = new MockServletContext();

	protected BeanDefinitionReader createBeanDefinitionReader(final GenericApplicationContext context) {
		return new XmlBeanDefinitionReader(context);
	}

	@Override
	public final ConfigurableApplicationContext loadContext(final String... locations) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Loading ApplicationContext for locations ["
					+ StringUtils.arrayToCommaDelimitedString(locations) + "].");
		}
		KeystoreUtils.createIfNotExists(this.getClass());

		final GenericWebApplicationContext webContext = new GenericWebApplicationContext();
		SERVLET_CONTEXT.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webContext);
		webContext.setServletContext(SERVLET_CONTEXT);
		createBeanDefinitionReader(webContext).loadBeanDefinitions(locations);
		AnnotationConfigUtils.registerAnnotationConfigProcessors(webContext);
		webContext.refresh();
		webContext.registerShutdownHook();
		return webContext;
	}


	@Override
	protected String getResourceSuffix() {
		return "-context.xml";
	}

	/**
	 * Loads a new {@link org.springframework.context.ApplicationContext context} based on the supplied
	 * {@link org.springframework.test.context.MergedContextConfiguration merged context configuration},
	 * configures the context, and finally returns the context in a fully
	 * <em>refreshed</em> state.
	 * <p>Concrete implementations should register annotation configuration
	 * processors with bean factories of
	 * {@link org.springframework.context.ApplicationContext application contexts} loaded by this
	 * {@code SmartContextLoader}. Beans will therefore automatically be
	 * candidates for annotation-based dependency injection using
	 * {@link org.springframework.beans.factory.annotation.Autowired @Autowired},
	 * {@link javax.annotation.Resource @Resource}, and
	 * {@link  //javax.inject.Inject @Inject}. In addition, concrete implementations
	 * should set the active bean definition profiles in the context's
	 * {@link org.springframework.core.env.Environment Environment}.
	 * <p>Any <code>ApplicationContext</code> loaded by a
	 * {@code SmartContextLoader} <strong>must</strong> register a JVM
	 * shutdown hook for itself. Unless the context gets closed early, all context
	 * instances will be automatically closed on JVM shutdown. This allows for
	 * freeing of external resources held by beans within the context (e.g.,
	 * temporary files).
	 *
	 * @param mergedConfig the merged context configuration to use to load the
	 *                     application context
	 * @return a new application context
	 * @throws Exception if context loading failed
	 * @see #processContextConfiguration(org.springframework.test.context.ContextConfigurationAttributes)
	 * @see org.springframework.context.annotation.AnnotationConfigUtils# registerAnnotationConfigProcessors()
	 * @see org.springframework.test.context.MergedContextConfiguration#getActiveProfiles()
	 * @see org.springframework.context.ConfigurableApplicationContext#getEnvironment()
	 */
	@Override
	public final ApplicationContext loadContext(final MergedContextConfiguration mergedConfig) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Loading ApplicationContext for merged context configuration [%s].",
					mergedConfig));
		}
		KeystoreUtils.createIfNotExists(this.getClass());

		final GenericWebApplicationContext webContext = new GenericWebApplicationContext();
		SERVLET_CONTEXT.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webContext);
		webContext.setServletContext(SERVLET_CONTEXT);

		webContext.getEnvironment().setActiveProfiles(mergedConfig.getActiveProfiles());
		prepareContext(webContext);
		customizeBeanFactory(webContext.getDefaultListableBeanFactory());
		loadBeanDefinitions(webContext, mergedConfig);
		AnnotationConfigUtils.registerAnnotationConfigProcessors(webContext);
		customizeContext(webContext);
		webContext.refresh();
		webContext.registerShutdownHook();
		return webContext;
	}


	/**
	 * Prepare the {@link org.springframework.context.support.GenericApplicationContext} created by this <code>ContextLoader</code>.
	 * Called <i>before</i> bean definitions are read.
	 * <p>The default implementation is empty. Can be overridden in subclasses to
	 * customize <code>GenericApplicationContext</code>'s standard settings.
	 * @param context the context that should be prepared
	 * @see #loadContext(org.springframework.test.context.MergedContextConfiguration)
	 * @see #loadContext(String...)
	 * @see org.springframework.context.support.GenericApplicationContext#setAllowBeanDefinitionOverriding
	 * @see org.springframework.context.support.GenericApplicationContext#setResourceLoader
	 * @see org.springframework.context.support.GenericApplicationContext#setId
	 * @since 2.5
	 */
	protected void prepareContext(GenericApplicationContext context) {
	}

	/**
	 * Customize the internal bean factory of the ApplicationContext created by
	 * this <code>ContextLoader</code>.
	 * <p>The default implementation is empty but can be overridden in subclasses
	 * to customize <code>DefaultListableBeanFactory</code>'s standard settings.
	 * @param beanFactory the bean factory created by this <code>ContextLoader</code>
	 * @see #loadContext(org.springframework.test.context.MergedContextConfiguration)
	 * @see #loadContext(String...)
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowBeanDefinitionOverriding
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowEagerClassLoading
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowCircularReferences
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowRawInjectionDespiteWrapping
	 * @since 2.5
	 */
	protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
	}

	/**
	 * Load bean definitions into the supplied {@link org.springframework.context.support.GenericApplicationContext context}
	 * from the configuration locations or classes in the supplied
	 * <code>MergedContextConfiguration</code>.</li>
	 * <p>The default implementation delegates to the {@link org.springframework.beans.factory.support.BeanDefinitionReader}
	 * returned by {@link # createBeanDefinitionReader()} to
	 * {@link org.springframework.beans.factory.support.BeanDefinitionReader#loadBeanDefinitions(String) load} the
	 * bean definitions.
	 * <p>Subclasses must provide an appropriate implementation of
	 * {@link # createBeanDefinitionReader()}. Alternatively subclasses may
	 * provide a <em>no-op</em> implementation of {@code createBeanDefinitionReader()}
	 * and override this method to provide a custom strategy for loading or
	 * registering bean definitions.
	 * @param context the context into which the bean definitions should be loaded
	 * @param mergedConfig the merged context configuration
	 * @see #loadContext(org.springframework.test.context.MergedContextConfiguration)
	 * @since 3.1
	 */
	protected void loadBeanDefinitions(GenericApplicationContext context, MergedContextConfiguration mergedConfig) {
		createBeanDefinitionReader(context).loadBeanDefinitions(mergedConfig.getLocations());
	}

	/**
	 * Customize the {@link org.springframework.context.support.GenericApplicationContext} created by this
	 * <code>ContextLoader</code> <i>after</i> bean definitions have been
	 * loaded into the context but <i>before</i> the context is refreshed.
	 * <p>The default implementation is empty but can be overridden in subclasses
	 * to customize the application context.
	 * @param context the newly created application context
	 * @see #loadContext(org.springframework.test.context.MergedContextConfiguration)
	 * @see #loadContext(String...)
	 * @since 2.5
	 */
	protected void customizeContext(GenericApplicationContext context) {
	}

}