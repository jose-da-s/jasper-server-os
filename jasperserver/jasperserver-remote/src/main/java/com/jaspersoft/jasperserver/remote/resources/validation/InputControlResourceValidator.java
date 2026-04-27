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

package com.jaspersoft.jasperserver.remote.resources.validation;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControl;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.exception.MandatoryParameterNotFoundException;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jaspersoft.jasperserver.remote.resources.validation.ValidationHelper.*;

/**
 * <p></p>
 *
 * @author Zakhar.Tomchenco
 * @version $Id$
 */
@Component
public class InputControlResourceValidator extends GenericResourceValidator<InputControl> {
    public static final String PROPERTY_REQUIRED = "usedFields";
    public static final String PROPERTY_REQUIRED_SEPARATOR = ";";

    @Resource
    private Map<String, Map<String, Object>> inputControlTypeConfiguration;
    private Map<String, PropertyDescriptor> properties = new HashMap<String, PropertyDescriptor>();


    @PostConstruct
    public void initialize() {
        try {
            PropertyDescriptor[] availableProperties = Introspector.getBeanInfo(InputControl.class).getPropertyDescriptors();
            for (PropertyDescriptor descriptor : availableProperties) {
                if (Object.class.isAssignableFrom(descriptor.getPropertyType()) && descriptor.getReadMethod() != null && descriptor.getWriteMethod() != null) {
                    properties.put(descriptor.getName(), descriptor);
                }
            }

            for (String key : inputControlTypeConfiguration.keySet()) {
                String required = (String) inputControlTypeConfiguration.get(key).get(PROPERTY_REQUIRED);
                if (required != null) {
                    for (String definedName : required.split(PROPERTY_REQUIRED_SEPARATOR)) {
                        if (!properties.containsKey(definedName)) {
                            StringBuilder message = new StringBuilder("The input control type ").append(key)
                                    .append(" contains unknown property '").append(definedName).append("'\n")
                                    .append("Valid values:\n");
                            for (String name : properties.keySet()) {
                                message.append("'").append(name).append("',\n");
                            }

                            throw new IllegalStateException(message.toString());
                        }
                    }
                }
            }
        } catch (IntrospectionException e) {
            throw new IllegalStateException("Introspection error", e);
        }
    }

    @Override
    protected void internalValidate(ExecutionContext ctx, InputControl resource, List<Exception> errors, Map<String, String[]> additionalParameters) {
        String type = Integer.toString(resource.getInputControlType());
        Map<String, Object> config = inputControlTypeConfiguration.get(type);

        for (String visibleColumn : resource.getQueryVisibleColumns()){
            if (visibleColumn.isEmpty()) {
                errors.add(new IllegalParameterValueException("'queryVisibleColumns' should not contain an empty value", "visibleColumns", Arrays.toString(resource.getQueryVisibleColumns())));
                break;
            }
        }

        if (config == null) {
            errors.add(new IllegalParameterValueException("The type " + type + " is invalid", "type", type));
        } else {
            List<String> required = config.containsKey(PROPERTY_REQUIRED) ? Arrays.asList(config.get(PROPERTY_REQUIRED).toString().split(PROPERTY_REQUIRED_SEPARATOR)) : Collections.EMPTY_LIST;
            for (String property : properties.keySet()) {
                boolean mustBe = required.contains(property);
                try {
                    Object value = properties.get(property).getReadMethod().invoke(resource);
                    if (empty(value) == mustBe) {
                        if (mustBe) {
                            errors.add(new MandatoryParameterNotFoundException(property));
                        } else {
                            errors.add(new IllegalParameterValueException("The property " + property + " should not be set for input control type " + type, property, ""));
                        }
                    }
                    if (value instanceof Collection) {
                        for (Object item : (Collection) value) {
                            if (empty(item) == mustBe) {
                                errors.add(new IllegalParameterValueException("An item should not be empty", property + ".item", item.toString()));
                            }
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                } catch (InvocationTargetException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

}

