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
package com.jaspersoft.jasperserver.api.metadata.security;

import com.jaspersoft.jasperserver.api.metadata.common.domain.InternalURI;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.user.domain.ObjectPermission;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.InternalURIDefinition;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * <p>We don't allow add/update permissions for local folder or local resource. So, this voter deny access if object
 * permission has local folder or local resource URI. </p>
 *
 * @author Volodya Sabadosh
 */
@Service
public class NotLocalFolderAndResourceArgumentVoter extends BasicObjectPermissionArgumentVoter {
    private static final String ATTRIBUTE = "NOT_LOCAL_FOLDER_AND_RESOURCE";
    @Resource(name = "unsecureRepositoryService")
    private RepositoryService repositoryService;

    @Override
    protected boolean isPermitted(Authentication authentication, ObjectPermission objectPermission, Object object) {
        InternalURI internalURI = new InternalURIDefinition(objectPermission.getURI());
        return !(repositoryService.isLocalFolder(null, internalURI.getPath()) ||
                isLocalResource(internalURI));
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return ATTRIBUTE.equals(attribute.getAttribute());
    }

    private boolean isLocalResource(InternalURI targetUri) {
        return targetUri.getParentPath() != null &&
                repositoryService.isLocalFolder(null, targetUri.getParentPath());
    }


}

