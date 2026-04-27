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
import com.jaspersoft.jasperserver.api.metadata.common.domain.ContentResource;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.RepositorySecurityChecker;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.core.util.validators.ValidationUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author serhii.blazhyievskyi
 * @version $Id$
 */
@Component
public class ContentResourceValidator extends GenericResourceValidator<ContentResource> {
    @Resource(name = "repositoryService")
    private RepositoryService repository;
    @Resource(name = "repositoryServiceSecurityChecker")
    private RepositorySecurityChecker repositoryServiceSecurityChecker;
    @Value("#{configurationBean.maxFileSize}")
    private Long maxFileSize;


    public RepositoryService getRepository()
    {
        return repository;
    }

    public void setRepository(RepositoryService repository)
    {
        this.repository = repository;
    }

    public void setRepositoryServiceSecurityChecker(RepositorySecurityChecker repositoryServiceSecurityChecker) {
        this.repositoryServiceSecurityChecker = repositoryServiceSecurityChecker;
    }

    public void setMaxFileSize(Long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    @Override
    protected void internalValidate(ExecutionContext ctx, ContentResource resource, List<Exception> errors, Map<String, String[]> additionalParameters) {
        validateNaming(resource, errors);
        validateUpload(resource, errors);
    }

    private void validateNaming(ContentResource resource, List<Exception> errors) {
        if (resource.getLabel() == null
                || resource.getLabel().trim().length() == 0) {
            errors.add(new IllegalParameterValueException("Enter a name for this resource", "label", ""));
        } else {
            if (resource.getLabel().length() > 100) {
                errors.add(new IllegalParameterValueException(
                        "The resource label is too long. The maximum length is 100 characters.", "label", ""));
            } else if (!ValidationUtil.regExValidateLabel(resource.getLabel())) {
                errors.add(new IllegalParameterValueException(
                        "The resource label has invalid characters. You might have mistyped it.", "label", ""));
            }
        }

        if (resource.getDescription() != null && resource.getDescription().length() > 250)
            errors.add(new IllegalParameterValueException(
                    "The description is too long. The maximum length is 250 characters.", "description", ""));
    }

    private void validateUpload(ContentResource resource, List<Exception> errors) {
        if (maxFileSize >= 0 && resource.getData() != null && resource.getData().length > maxFileSize){
            errors.add(new IllegalParameterValueException("The file must be smaller than " + maxFileSize + " bytes", "data", ""));
        }
    }
}

