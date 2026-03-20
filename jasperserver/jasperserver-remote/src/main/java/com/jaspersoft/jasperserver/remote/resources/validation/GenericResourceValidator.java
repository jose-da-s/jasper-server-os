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
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributesResolver;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.exception.MandatoryParameterNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jaspersoft.jasperserver.remote.resources.validation.ValidationHelper.empty;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
public abstract class GenericResourceValidator<ResourceType extends Resource> implements ResourceValidator<ResourceType> {
    @jakarta.annotation.Resource
    private ProfileAttributesResolver profileAttributesResolver;

    @Override
    public List<Exception> validate(ExecutionContext ctx, ResourceType resource, boolean skipRepoFieldsValidation, Map<String, String[]> additionalParameters) {
        final List<Exception> validationErrors = new ArrayList<Exception>();
        if(!skipRepoFieldsValidation) {
            genericValidate(resource, validationErrors);
        }
        internalValidate(ctx, resource, validationErrors, additionalParameters);
        return validationErrors;
    }

    @Override
    public List<Exception> validate(ExecutionContext ctx, ResourceType resource) {
        return validate(ctx, resource, false, new HashMap<String, String[]>());
    }

    private void genericValidate(Resource resource, List<Exception> errors) {
        if (empty(resource.getLabel())) {
            errors.add(new MandatoryParameterNotFoundException("label"));
        } else {
            if (resource.getLabel().length() > 100) {
                errors.add(new IllegalParameterValueException("The label must not be longer than 100 characters", "label", resource.getLabel()));
            }
            if (profileAttributesResolver.containsAttribute(resource.getLabel())) {
                errors.add(new IllegalParameterValueException("Attribute placeholder is not allowed", "label", resource.getLabel()));
            }
        }

        if (!empty(resource.getDescription())) {
            if (resource.getDescription().length() > 250) {
                errors.add(new IllegalParameterValueException("The description must not be longer than 250 characters", "description", resource.getDescription()));
            }
            if (profileAttributesResolver.containsAttribute(resource.getDescription())) {
                errors.add(new IllegalParameterValueException("Attribute placeholder is not allowed", "description", resource.getDescription()));
            }
        }

        if (!empty(resource.getName()) && profileAttributesResolver.containsAttribute(resource.getName())) {
            errors.add(new IllegalParameterValueException("Attribute placeholder is not allowed", "name", resource.getName()));
        }
    }

    protected abstract void internalValidate(ExecutionContext ctx, ResourceType resource, List<Exception> errors, Map<String, String[]> additionalParameters);
}

