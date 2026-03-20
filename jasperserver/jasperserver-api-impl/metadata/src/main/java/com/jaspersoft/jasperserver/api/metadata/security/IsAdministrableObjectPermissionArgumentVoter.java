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

import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.metadata.common.domain.PermissionUriProtocol;
import com.jaspersoft.jasperserver.api.metadata.user.domain.ObjectPermission;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.AttributePathTransformer;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.InternalURIDefinition;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.JasperServerSidRetrievalStrategyImpl;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.acls.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p></p>
 *
 * @author Zakhar.Tomchenco
 * @version $Id$
 */
@Component
public class IsAdministrableObjectPermissionArgumentVoter extends BasicObjectPermissionArgumentVoter {
    private static final String ATTRIBUTE = "CAN_ADMINISTER";
    // spring initializes interceptors twice in case if proxied bean is injected to one of it's interceptors.
    // let's use unsecure object permission service
    @Resource(name = "internalAclService")
    private AclService resourcesAclService;
    @Resource
    private AttributePathTransformer attributePathTransformer;

    @Override
    protected boolean isPermitted(Authentication authentication, ObjectPermission objectPermission, Object object) {
        SidRetrievalStrategy sidStrategy = new JasperServerSidRetrievalStrategyImpl();

        Acl acl;
        try {
            acl = resourcesAclService.readAclById(getSecureObject(authentication, objectPermission), sidStrategy.getSids(authentication));
        } catch (JSException e) {
            // in some cases we are trying to reach not reachable resource, this will throw error
            acl=null;
        }
        List<Permission> requiredPermissionsList = new ArrayList<Permission>();
        requiredPermissionsList.add(JasperServerPermission.ADMINISTRATION);
        if (acl!=null && acl.isGranted(requiredPermissionsList,sidStrategy.getSids(authentication),false)) {
            return true;
        }

        return false;
    }

    private InternalURIDefinition getSecureObject(Authentication authentication, ObjectPermission objectPermission) {
        String checkPermissionUri = PermissionUriProtocol.removePrefix(objectPermission.getURI());
        PermissionUriProtocol protocol = PermissionUriProtocol.getProtocol(objectPermission.getURI());
        if (protocol == PermissionUriProtocol.ATTRIBUTE) {
            checkPermissionUri = attributePathTransformer.transformPath(checkPermissionUri, authentication);
        }

        return new InternalURIDefinition(checkPermissionUri, protocol);
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return ATTRIBUTE.equals(attribute.getAttribute());
    }
}

