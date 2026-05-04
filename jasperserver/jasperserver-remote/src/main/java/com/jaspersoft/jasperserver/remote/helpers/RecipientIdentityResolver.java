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

package com.jaspersoft.jasperserver.remote.helpers;

import com.jaspersoft.jasperserver.api.metadata.user.domain.Role;
import com.jaspersoft.jasperserver.api.metadata.user.domain.Tenant;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.service.TenantService;
import com.jaspersoft.jasperserver.api.metadata.user.service.UserAuthorityService;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.exception.ResourceNotFoundException;

import jakarta.annotation.Resource;
import java.util.Map;
import java.util.Set;

/**
 * <p></p>
 *
 * @author Zakhar.Tomchenco
 * @author Volodya Sabadosh
 * @version $Id$
 */
public class RecipientIdentityResolver {
    //Available protocols map.
    protected Map<String, Class<?>> map;
    protected String identifierLabel;
    protected Set<String> requiredProtocols;

    @Resource(name = "concreteTenantService")
    protected TenantService tenantService;
    @Resource(name = "concreteUserAuthorityService")
    protected UserAuthorityService userAuthorityService;

    public Class<?> getClassForProtocol(String protocol) throws IllegalParameterValueException {
        if (protocol != null && !requiredProtocols.contains(protocol)) {
            throw new IllegalParameterValueException("type", protocol);
        }

        return map.get(protocol);
    }

    public String getProtocolForClass(Class<?> clazz) {
        for (String key : requiredProtocols){
            if (map.get(key).isAssignableFrom(clazz)) {
                return key;
            }
        }
        return null;
    }

    public RecipientIdentity toIdentity(String uri) throws IllegalParameterValueException {
        RecipientIdentity identity;
        if (uri == null) {
            throw new IllegalParameterValueException(identifierLabel, "null");
        }
        String[] protocolLevel = uri.split(":");
        if (protocolLevel.length != 2) {
            throw new IllegalParameterValueException(identifierLabel, uri);
        }
        String[] segments = protocolLevel[1].split("/");

        Class<?> clazz = getClassForProtocol(protocolLevel[0]);

        if (segments.length == 0 && Tenant.class.isAssignableFrom(clazz)) {
            return new RecipientIdentity(clazz, TenantService.ORGANIZATIONS);
        }

        if (!segments[0].equals("")) {
            throw new IllegalParameterValueException(identifierLabel, uri);
        }

        if (segments.length == 2) {
            identity = new RecipientIdentity(clazz, segments[1]);
        } else if (segments.length == 3) {
            identity = new RecipientIdentity(clazz, makeIdentityId(uri, segments[1], segments[2]));
        } else {
            throw new IllegalParameterValueException(identifierLabel, uri);
        }

        return identity;
    }

    protected String makeIdentityId(String uri, String segment1, String segment2) {
        throw new IllegalParameterValueException(identifierLabel, uri);
    }

    public RecipientIdentity toIdentity(Object recipient) throws IllegalParameterValueException {
        Class<?> identityClazz;
        String identityId;
        if (recipient instanceof Role) {
            identityClazz = Role.class;
            Role role = (Role) recipient;
            identityId = role.getRoleName();
        } else if (recipient instanceof User) {
            identityClazz = User.class;
            User user = (User) recipient;
            identityId = user.getUsername();
        } else if (recipient instanceof Tenant) {
            identityClazz = Tenant.class;
            Tenant tenant = (Tenant) recipient;
            identityId = !isRootTenant(tenant.getId()) ? tenant.getId() : TenantService.ORGANIZATIONS;
        } else {
            throw new IllegalArgumentException(identifierLabel);
        }

        return new RecipientIdentity(identityClazz, identityId);
    }


    public String toRecipientUri(Object recipient) throws IllegalParameterValueException {
        return toRecipientUri(toIdentity(recipient));
    }

    public String toRecipientUri(RecipientIdentity recipientIdentity) {
        String recipientProtocol = getProtocolForClass(recipientIdentity.getRecipientClass());

        return recipientProtocol.concat(":/").concat(getIdentityUri(recipientIdentity));
    }

    protected String getIdentityUri(RecipientIdentity recipientIdentity) {
        if (Tenant.class.isAssignableFrom(recipientIdentity.getRecipientClass())
                && recipientIdentity.getId().equals(TenantService.ORGANIZATIONS)) {
            return "";
        }

        return recipientIdentity.getId();
    }

    protected boolean isRootTenant(String tenantId) {
        return tenantId == null || tenantId.equals(TenantService.ORGANIZATIONS);
    }

    public Object resolveRecipientObject(RecipientIdentity recipientIdentity) {
        Class<?> clazz = recipientIdentity.getRecipientClass();
        String id = recipientIdentity.getId();

        Object res = null;
        if (Role.class.equals(clazz)) {
            res = userAuthorityService.getRole(null, id);
        }
        if (User.class.equals(clazz)) {
            res = userAuthorityService.getUser(null, id);
        }
        if (Tenant.class.equals(clazz)) {
            res = tenantService.getTenant(null, id);
        }
        if (res == null) {
            throw new ResourceNotFoundException(id);
        }

        return res;
    }

    public Object resolveRecipientObject(String uri) {
        RecipientIdentity recipientIdentity = toIdentity(uri);
        return resolveRecipientObject(recipientIdentity);
    }

    public void setMap(Map<String, Class<?>> map) {
        this.map = map;
    }

    public void setIdentifierLabel(String identifierLabel) {
        this.identifierLabel = identifierLabel;
    }

    public void setRequiredProtocols(Set<String> requiredProtocols) {
        this.requiredProtocols = requiredProtocols;
    }
}

