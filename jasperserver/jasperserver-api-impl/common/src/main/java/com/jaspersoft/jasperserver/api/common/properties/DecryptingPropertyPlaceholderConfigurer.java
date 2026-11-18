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

package com.jaspersoft.jasperserver.api.common.properties;

import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.Resource;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

public class DecryptingPropertyPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer {

	private static final Map<String, String> springImportedProperties = new Hashtable<String, String>();

	public DecryptingPropertyPlaceholderConfigurer() {
		setLocalOverride(true);
	}

	@Override
	protected ConfigurablePropertyResolver createPropertyResolver(MutablePropertySources propertySources) {
		return new DecryptingPropertySourcesPropertyResolver(propertySources);
	}

	/**
	 * It seems this one is no longer (6.2.17) used in the context of BeanFactoryPostProcessor,
	 * but let's keep it around just in case. Its functionality is now covered by
	 * {@link setLocations(Resource... locations)}, {@link setLocation(Resource location)},
	 * {@link setPropertiesArray(Properties... propertiesArray)}, and {@link setProperties(Properties properties)}.
	 * hopefully.
	 */
	protected void convertProperties(Properties properties) {
		super.convertProperties(properties);

		//store spring props from properties files
		for (Object propKey : properties.keySet())
			springImportedProperties.put(propKey.toString(), properties.getProperty(propKey.toString()));

	}

	public static Map<String, String> getSpringImportedProperties() {
		return Collections.unmodifiableMap(springImportedProperties);
	}

	@Override
	public void setProperties(Properties properties) {
		super.setProperties(properties);
		for (Object propKey : properties.keySet()) {
			springImportedProperties.put(propKey.toString(), properties.getProperty(propKey.toString()));
		}
	}

	@Override
	public void setPropertiesArray(Properties... propertiesArray) {
		super.setPropertiesArray(propertiesArray);
		for (Properties properties : propertiesArray) {
			for (Object propKey : properties.keySet()) {
				springImportedProperties.put(propKey.toString(), properties.getProperty(propKey.toString()));
			}
		}
	}

	@Override
	public void setLocations(Resource... locations) {
		super.setLocations(locations);
		for (Resource location : locations) {
			try {
				Properties properties = new Properties();
				properties.load(location.getInputStream());
				for (Object propKey : properties.keySet()) {
					springImportedProperties.put(propKey.toString(), properties.getProperty(propKey.toString()));
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed to load properties from location: " + location, e);
			}
		}
	}

	@Override
	public void setLocation(Resource location) {
		super.setLocation(location);
		try {
			Properties properties = new Properties();
			properties.load(location.getInputStream());
			for (Object propKey : properties.keySet()) {
				springImportedProperties.put(propKey.toString(), properties.getProperty(propKey.toString()));
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to load properties from location: " + location, e);
		}
	}
}
