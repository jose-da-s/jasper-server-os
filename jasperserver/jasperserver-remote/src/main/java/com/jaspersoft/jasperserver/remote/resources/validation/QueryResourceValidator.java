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
import com.jaspersoft.jasperserver.api.metadata.common.domain.Query;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.exception.MandatoryParameterNotFoundException;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

import static com.jaspersoft.jasperserver.remote.resources.validation.ValidationHelper.empty;

/**
 * <p></p>
 *
 * @author Zakhar.Tomchenko
 * @version $Id$
 */
@Component
public class QueryResourceValidator extends GenericResourceValidator<Query> {
    @Resource(name = "queryLanguages")
    private List<String> queryLanguages;

    @Override
    protected void internalValidate(ExecutionContext ctx, Query resource, List<Exception> errors, Map<String, String[]> additionalParameters) {
        if (empty(resource.getSql())){
            errors.add(new MandatoryParameterNotFoundException("QueryValue"));
        }
        if (empty(resource.getLanguage())){
            errors.add(new MandatoryParameterNotFoundException("Language"));
        } else {
            if (!queryLanguages.contains(resource.getLanguage())){
                String supported = queryLanguages.get(0);

                for (int i = 1; i < queryLanguages.size(); i++){
                    supported += ", " + queryLanguages.get(i);
                }
                errors.add(new IllegalParameterValueException("The language " + resource.getLanguage() + " isn't supported. Supported languages: " + supported, "Language", resource.getLanguage()));
            }
        }
    }
}

