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
package com.jaspersoft.jasperserver.remote.services.impl;

import com.jaspersoft.jasperserver.api.metadata.common.domain.InternalURI;
import com.jaspersoft.jasperserver.api.metadata.common.domain.PermissionUriProtocol;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.util.RepositoryUtils;
import com.jaspersoft.jasperserver.api.metadata.security.JasperServerPermission;
import com.jaspersoft.jasperserver.api.metadata.user.domain.ObjectPermission;
import com.jaspersoft.jasperserver.api.metadata.user.domain.ProfileAttribute;
import com.jaspersoft.jasperserver.api.metadata.user.domain.Role;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.RoleImpl;
import com.jaspersoft.jasperserver.api.metadata.user.service.AttributesSearchCriteria;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeLevel;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeService;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.InternalURIDefinition;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.acls.model.Permission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Volodya Sabadosh
 * @version $Id$
 */
@Component("attributesPermissionService")
@Transactional(rollbackFor = Exception.class)
public class AttributesPermissionServiceImpI extends PermissionsServiceImpl {
    protected static final Map<Integer, Permission> ALLOWED_PERMISSION_MASKS;

    static {
        Map<Integer, Permission> map = new HashMap<Integer, Permission>();

        map.put(JasperServerPermission.ADMINISTRATION.getMask(), JasperServerPermission.ADMINISTRATION);
        map.put(JasperServerPermission.READ.getMask(), JasperServerPermission.READ);
        map.put(JasperServerPermission.EXECUTE.getMask(), JasperServerPermission.EXECUTE);
        map.put(JasperServerPermission.NOTHING.getMask(), JasperServerPermission.NOTHING);

        ALLOWED_PERMISSION_MASKS = Collections.unmodifiableMap(map);
    }

    @Resource
    protected String roleAdministrator;

    @Resource
    protected ProfileAttributeService profileAttributeService;

    public List<ObjectPermission> putPermissions(InternalURI internalURI, List<ObjectPermission> objectPermissions) throws ErrorDescriptorException {
        ProfileAttribute profileAttribute;
        if (!(internalURI instanceof ProfileAttribute)) {
            return super.putPermissions(internalURI, objectPermissions);
        } else {
            profileAttribute = (ProfileAttribute) internalURI;
        }

        for (ObjectPermission objectPermission : objectPermissions) {
            validatePermissionsOrder(objectPermission);
        }

        Permission effectivePermissionMask = getEffectivePermissionForRoleAdmin(profileAttribute);

        for (ObjectPermission permission : objectPermissions) {
            permission.setURI(profileAttribute.getURI());
        }

        //Indicates whether we should remove all child attributes relatively to target profileAttribute.
        boolean recursive = false;

        Permission newAdminPermissionMask = null;
        for (ObjectPermission objectPermission : objectPermissions) {
            if (isAdminRole(getPermissionRecipient(objectPermission.getPermissionRecipient()))) {
                newAdminPermissionMask = ALLOWED_PERMISSION_MASKS.get(objectPermission.getPermissionMask());
                if (effectivePermissionMask == JasperServerPermission.ADMINISTRATION && newAdminPermissionMask !=
                        JasperServerPermission.ADMINISTRATION) {
                    recursive = true;
                }
            }
        }

        List<ObjectPermission> result = doPutPermissions(profileAttribute, objectPermissions, false);

        AttributesSearchCriteria searchCriteria = new AttributesSearchCriteria.Builder().setNames(Collections.
                singleton(profileAttribute.getAttrName())).setRecursive(true).build();
        List<ProfileAttribute> childAttributes = profileAttributeService.
                getProfileAttributesForPrincipal(null, profileAttribute.getPrincipal(), searchCriteria).getList();

        for (ProfileAttribute attribute : childAttributes) {
            if (attribute.getLevel().equals(ProfileAttributeLevel.CHILD)) {
                if (recursive) {
                    // Removes child attributes(if we switch target attribute permission From Administrator to lower permission(Read, Execute, No Access)).
                    profileAttributeService.deleteProfileAttribute(null, attribute);
                } else {
                    // Safe the right order of permission mask. Higher level should always have more higher permissions.
                    Permission permissionMask = getEffectivePermissionForRoleAdmin(attribute);
                    if (newAdminPermissionMask != null && permissionComparator.compare(permissionMask, newAdminPermissionMask) > 0) {
                        List<ObjectPermission> existing = objectPermissionService.getObjectPermissionsForObject(makeExecutionContext(), attribute);
                        for (ObjectPermission permission : existing) {
                            this.deletePermission(permission);
                        }
                    }
                }
            }
        }

        return result;
    }

    protected void changePermissionConsistencyCheck(ObjectPermission objectPermission) throws ErrorDescriptorException {
        if (StringUtils.isBlank(objectPermission.getURI())) {
            throw new IllegalParameterValueException("URI is blank", "uri", objectPermission.getURI());
        }

        if (!ALLOWED_PERMISSION_MASKS.keySet().contains(objectPermission.getPermissionMask())) {
            throw new IllegalParameterValueException("mask", Integer.toString(objectPermission.getPermissionMask()));
        }

        Object permissionRecipient = getPermissionRecipient(objectPermission.getPermissionRecipient());

        if (isAdminRole(permissionRecipient)) {
            objectPermission.setPermissionRecipient(permissionRecipient);
        } else {
            String attrName = RepositoryUtils.getName(objectPermission.getURI());

            throw new IllegalParameterValueException(new ErrorDescriptor()
                    .setErrorCode("attribute.invalid.permission.recipient")
                    .setMessage("The attribute permission recipient is invalid")
                    .setParameters(new String[]{attrName}));
        }
    }

    /**
     * Validates the right order of permission mask. Permissions for attribute, assigned at higher level should
     * have more higher permission mask than attribute permissions, defined at lower level.
     */
    protected void validatePermissionsOrder(ObjectPermission objectPermission) {
        InternalURIDefinition targetURI = new InternalURIDefinition(objectPermission.getURI());
        InternalURIDefinition parentURI = new InternalURIDefinition(targetURI.getParentPath(),
                PermissionUriProtocol.fromString(targetURI.getProtocol()));

        ObjectPermission effectiveObjectPermission;
        Object recipient = getPermissionRecipient(objectPermission.getPermissionRecipient());

        if (recipient instanceof Role) {
            effectiveObjectPermission = getEffectivePermission(parentURI, (Role) recipient);
        } else if (recipient instanceof User) {
            effectiveObjectPermission = getEffectivePermission(parentURI, (User) recipient);
        } else {
            throw new IllegalStateException("Unknown recipient class:" + recipient.getClass().getName());
        }

        Permission targetPermissionMask = ALLOWED_PERMISSION_MASKS.get(objectPermission.getPermissionMask());

        if (targetPermissionMask == null) {
            throw new IllegalParameterValueException("mask", Integer.toString(objectPermission.getPermissionMask()));
        }
        Permission inheritedPermissionMask = ALLOWED_PERMISSION_MASKS.get(effectiveObjectPermission.getPermissionMask());

        if (permissionComparator.compare(inheritedPermissionMask, targetPermissionMask) < 0) {
            String attrName = RepositoryUtils.getName(objectPermission.getURI());
            String[] errorParameters = new String[]{attrName,
                    Integer.toString(targetPermissionMask.getMask()), Integer.toString(inheritedPermissionMask.getMask())};

            throw new IllegalParameterValueException(new ErrorDescriptor()
                    .setErrorCode("attribute.invalid.permission.order").setMessage("The attribute permission order is invalid")
                    .setParameters(errorParameters));
        }
    }

    protected boolean isAdminRole(Object recipient) {
        return recipient instanceof Role && ((Role)recipient).getRoleName().equals(roleAdministrator)
                && ((Role)recipient).getTenantId() == null;
    }

    protected Permission getEffectivePermissionForRoleAdmin(InternalURI uri) {
        Role role = new RoleImpl();
        role.setRoleName(roleAdministrator);
        ObjectPermission effectivePermission = getEffectivePermission(uri, role);

        return ALLOWED_PERMISSION_MASKS.get(effectivePermission.getPermissionMask());
    }

}

