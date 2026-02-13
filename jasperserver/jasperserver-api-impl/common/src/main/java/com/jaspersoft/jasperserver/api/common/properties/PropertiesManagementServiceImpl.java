/*
 * Copyright (C) 2025 the Jasper Server OS Authors
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

import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.service.ResourceFactory;
import com.jaspersoft.jasperserver.api.metadata.user.domain.ProfileAttribute;
import com.jaspersoft.jasperserver.api.metadata.user.service.AttributesSearchCriteria;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeCategory;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeGroup;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * PropertiesManagementServiceImpl
 *
 * This service manages setting, retrieving and storing of
 * configurable properties.  by convention, the property keys
 * should be qualified with dots to avoid namespace conflict.
 *   for example:  report.scheduling.repository.content
 *
 * @author sbirney (sbirney@users.sourceforge.net)
 * @author udavidovich
 *
 * TODO: mulitple VMs concurrent updates
 * TODO: mulitple namespaces
 */
public class PropertiesManagementServiceImpl
implements PropertiesManagementService, ApplicationContextAware {

    private static final Log log = LogFactory.getLog(PropertiesManagementServiceImpl.class);
    private static final AttributesSearchCriteria searchCriteria = new AttributesSearchCriteria.Builder().build();

    public static final String CONTENT_FOLDER = "properties";
    public static final String RESOURCE_NAME = "GlobalPropertiesList";
    public static final String RESOURCE_FULL_NAME = Folder.SEPARATOR +
													   CONTENT_FOLDER +
													   Folder.SEPARATOR +
													   RESOURCE_NAME;

    protected ApplicationContext context;
	private ProfileAttributeService profileAttributeService;
	private ResourceFactory resourceFactory;

    public PropertiesManagementServiceImpl() {
    }

    /**
     * Use setProperty method to set and apply a configuration key to a value.
     * The key prefix must correspond to the changer being configured.
     * @param key should be fully qualified, must not be null
     * @param val the value as a string
     */
    public void setProperty(String key, String val) {
    	log.debug("Property " + key + " is going to be set to " + val);
        profileAttributeService.putProfileAttribute(null, makeProfileAttribute(key, val));
    }
    /**
     * Use getProperty to retrieve the state of a configuration property.
     * The value is queried from the relevant changer
     * @param key must not be null
     * @return associated value or null
     */
    public String getProperty(String key) {
        ProfileAttribute profileAttribute = profileAttributeService.getProfileAttribute(null,
                makeProfileAttribute(key, null));
        return (profileAttribute != null) ? profileAttribute.getAttrValue() : null;
    }

    public Map<String, String> getProperties() {
        @SuppressWarnings("unchecked")
        List<ProfileAttribute> profileAttributes = profileAttributeService.getProfileAttributesForPrincipal(null,
                ProfileAttributeCategory.SERVER.getPrincipal(resourceFactory), searchCriteria).getList();
        Map<String, String> properties = new HashMap<String, String>();

        for (ProfileAttribute profileAttribute : profileAttributes) {
            properties.put(profileAttribute.getAttrName(), profileAttribute.getAttrValue());
        }

        return properties;
    }

    // while we are using ProfileAttributeService this method is not needed anymore
    public void saveProperties() {
        throw new UnsupportedOperationException();
    }

    public int size() {
        @SuppressWarnings("unchecked")
        List<ProfileAttribute> profileAttributes = profileAttributeService.getProfileAttributesForPrincipal(null,
                ProfileAttributeCategory.SERVER.getPrincipal(resourceFactory), searchCriteria).getList();
        return profileAttributes.size();
    }

    public Set entrySet() {
        @SuppressWarnings("unchecked")
        List<ProfileAttribute> profileAttributes = profileAttributeService.getProfileAttributesForPrincipal(null,
                ProfileAttributeCategory.SERVER.getPrincipal(resourceFactory), searchCriteria).getList();
        Map<String, String> map = new HashMap<String, String>();

        for (ProfileAttribute profileAttribute : profileAttributes) {
            map.put(profileAttribute.getAttrName(), profileAttribute.getAttrValue());
        }

        return map.entrySet();
    }

    public String remove(String key) {
        ProfileAttribute profileAttribute = profileAttributeService.getProfileAttribute(null,
                makeProfileAttribute(key, null));
        profileAttributeService.deleteProfileAttribute(null, profileAttribute);
        return profileAttribute.getAttrValue();
    }

    public Map<String, String> removeByValue(String value) {
        @SuppressWarnings("unchecked")
        List<ProfileAttribute> profileAttributes = profileAttributeService.getProfileAttributesForPrincipal(null,
                ProfileAttributeCategory.SERVER.getPrincipal(resourceFactory),
                new AttributesSearchCriteria
                        .Builder()
                        .setGroups(Collections.singleton(ProfileAttributeGroup.CUSTOM_SERVER_SETTINGS.toString()))
                        .build()).getList();
        Map<String, String> removedKeys = new HashMap<String, String>();

        for (ProfileAttribute profileAttribute : profileAttributes) {
            if (profileAttribute.getAttrValue() != null && profileAttribute.getAttrValue().equals(value)) {
                profileAttributeService.deleteProfileAttribute(null, profileAttribute);
                removedKeys.put(profileAttribute.getAttrName(), profileAttribute.getAttrValue());
            }
        }

        return removedKeys;
    }

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		context = applicationContext;
	}

	@Override
	public void reloadProperties() {
        profileAttributeService.applyProfileAttributes();
	}

	public void setResourceFactory(ResourceFactory resourceFactory) {
		this.resourceFactory = resourceFactory;
	}

	public void setProfileAttributeService(ProfileAttributeService profileAttributeService) {
		this.profileAttributeService = profileAttributeService;
	}

	private ProfileAttribute makeProfileAttribute(String name, String value) {
		ProfileAttribute profileAttribute = profileAttributeService.newProfileAttribute(null);
        profileAttribute.setAttrName(name);
		profileAttribute.setAttrValue(value);
		profileAttribute.setGroup(profileAttributeService.getChangerName(name));
		profileAttribute.setPrincipal(ProfileAttributeCategory.SERVER.getPrincipal(resourceFactory));
		profileAttribute.setSecure(false);

		return profileAttribute;
	}
}
