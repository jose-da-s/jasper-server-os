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

import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.user.domain.Role;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.RoleImpl;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.JasperServerSidRetrievalStrategyImpl;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author Volodya Sabadosh
 * @version $Id: $
 */
public class ContextSensitiveAttributeArgumentVoter extends AttributeArgumentVoter {
    public static final String DEFAULT_PERMS = "default";

    @Resource
    private String roleAdministrator;

    private Map<String, List<Permission>> requiredPermissionsMap;

    @Override
    protected List<Sid> generateSids(Authentication authentication, Object object) {
        List<Sid> sids = super.generateSids(authentication, object);

        if (isPrivilegedForExecuteOnly(object)) {
            Role role = new RoleImpl();
            role.setRoleName(roleAdministrator);

            if (sidStrategy instanceof JasperServerSidRetrievalStrategyImpl) {
                sids.add(((JasperServerSidRetrievalStrategyImpl) sidStrategy).getSid(role));
            }
        }

        return sids;
    }

    @Override
    protected List<Permission> generateRequiredPermissions(Object secureObject) {
        if (isPrivilegedForExecuteOnly(secureObject)) {
            return requiredPermissionsMap.get(ExecutionContextImpl.EXECUTE_OVERRIDE);
        } else {
            return requiredPermissionsMap.get(DEFAULT_PERMS);
        }
    }

    public void setRequiredPermissionsMap(Map<String, List<Permission>> requiredPermissionsMap) {
        this.requiredPermissionsMap = requiredPermissionsMap;
    }

}

