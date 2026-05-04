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
package com.jaspersoft.jasperserver.jaxrs.authority;

import com.jaspersoft.jasperserver.api.metadata.user.domain.Role;
import com.jaspersoft.jasperserver.dto.authority.ClientRole;
import com.jaspersoft.jasperserver.dto.authority.RolesListWrapper;
import com.jaspersoft.jasperserver.jaxrs.common.RestConstants;
import com.jaspersoft.jasperserver.remote.common.RoleSearchCriteria;
import com.jaspersoft.jasperserver.remote.exception.AccessDeniedException;
import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.remote.exception.ResourceAlreadyExistsException;
import com.jaspersoft.jasperserver.remote.exception.ResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.resources.converters.RoleConverter;
import com.jaspersoft.jasperserver.remote.services.UserAndRoleService;

import jakarta.annotation.Resource;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl.getRuntimeExecutionContext;

/**
 * @author: Zakhar.Tomchenco
 */

public class RolesJaxrsService {

    private UserAndRoleService service;
    @Resource(name = "roleConverter")
    private RoleConverter converter;

    @Resource(name = "rootTenantId")
    private String rootTenantId;

    public Response getRoles(int startIndex,
                             int maxRecords,
                             String tenantId,
                             Boolean includeSubOrgs,
                             String search,
                             Boolean hasAllUsers,
                             List<String> userNames) throws ErrorDescriptorException {

        RoleSearchCriteria criteria = new RoleSearchCriteria();
        criteria.setRoleName("".equals(search) ? null : search);
        criteria.setTenantId("".equals(tenantId) ? null : tenantId);
        criteria.setIncludeSubOrgs(includeSubOrgs);
        criteria.setHasAllUsers(hasAllUsers);
        criteria.setUsersNames(userNames);

        List<Role> roles = service.findRoles(criteria);
        int totalCount = roles.size();

        if (totalCount < startIndex) {
            roles.clear();
        } else {
            if (maxRecords != 0) {
                if (startIndex + maxRecords > totalCount) {
                    roles = roles.subList(startIndex, totalCount);
                } else {
                    roles = roles.subList(startIndex, startIndex + maxRecords);
                }
            } else {
                if (startIndex > 0){
                    roles = roles.subList(startIndex, totalCount);
                }
            }
        }

        List<ClientRole> clientRoles = new ArrayList<ClientRole>(roles.size());
        for (Role role:roles){
            clientRoles.add(converter.toClient(role, null));
        }

        Response response;
        if (roles.size() == 0) {
            response = Response.status(Response.Status.NO_CONTENT)
                    .header(RestConstants.HEADER_START_INDEX, startIndex)
                    .header(RestConstants.HEADER_RESULT_COUNT, roles.size())
                    .header(RestConstants.HEADER_TOTAL_COUNT, totalCount)
                    .build();
        } else {
            response = Response.ok()
                    .entity(new RolesListWrapper(clientRoles))
                    .header(RestConstants.HEADER_START_INDEX, startIndex)
                    .header(RestConstants.HEADER_RESULT_COUNT, roles.size())
                    .header(RestConstants.HEADER_TOTAL_COUNT, totalCount)
                    .build();
        }
        return response;

    }

    public Response getRoles(String name, String tenantId) throws ErrorDescriptorException {
        Role role = findRole(name, tenantId);
        if (role == null) {
            throw new ResourceNotFoundException(name);
        }

        return Response.ok(converter.toClient(role, null)).build();
    }

    public Response deleteRole(String name, String tenantId) throws ErrorDescriptorException {
        Role role = findRole(name, tenantId);
        if (role == null) {
            throw new ResourceNotFoundException(name);
        }
        try {
            service.deleteRole(role);
        } catch (org.springframework.security.access.AccessDeniedException ade) {
            throw new AccessDeniedException(ade.getMessage());
        }

        return Response.noContent().build();
    }

    public Response createRole(ClientRole clientRole) throws ErrorDescriptorException {
        Role role = converter.toServer(getRuntimeExecutionContext(), clientRole, null);

        if (findRole(role.getRoleName(), role.getTenantId()) != null){
            throw new ResourceAlreadyExistsException(role.getRoleName());
        }
        ClientRole resultRole = converter.toClient(service.putRole(role), null);

        return Response.status(Response.Status.CREATED).entity(resultRole).build();
    }

    public Response updateRole(ClientRole newRole, String name, String tenantId) throws ErrorDescriptorException {
        Role oldRole = findRole(name, tenantId);
        ClientRole resultRole;
        if (oldRole != null) {
            if (findRole(newRole.getName(), tenantId) != null) {
                throw new ResourceAlreadyExistsException(newRole.getName());
            }
            resultRole = converter.toClient(service.updateRoleName(oldRole, newRole.getName()), null);
        } else {
            newRole.setName(name);     // to prevent possible data collisions
            newRole.setTenantId(tenantId);

            return createRole(newRole);
        }

        return Response.ok(resultRole).build();
    }

    private boolean requestedRole(Role role, String name, String tenantId) {
        return (role.getRoleName().equals(name)) &&
            ((role.getTenantId() == null && tenantId == null) ||
             (role.getTenantId() == null && rootTenantId.equals(tenantId)) ||
             (role.getTenantId() != null && role.getTenantId().equals(tenantId)));
    }

    private Role findRole(String name, String tenantId) throws ErrorDescriptorException {
        RoleSearchCriteria crit = new RoleSearchCriteria();
        crit.setRoleName(name);
        crit.setTenantId(tenantId);

        List<Role> found = service.findRoles(crit);

        for (Role role : found) {
            if (requestedRole(role, name, tenantId)) {
                return role;
            }
        }
        return null;
    }

    public UserAndRoleService getService() {
        return service;
    }

    public void setService(UserAndRoleService service) {
        this.service = service;
    }
}

