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
package com.jaspersoft.jasperserver.remote.resources.converters;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConversionOptions;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConverter;
import com.jaspersoft.jasperserver.api.metadata.user.domain.Role;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.UserImpl;
import com.jaspersoft.jasperserver.api.security.encryption.EncryptionManager;
import com.jaspersoft.jasperserver.dto.authority.ClientRole;
import com.jaspersoft.jasperserver.dto.authority.ClientUser;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.exception.MandatoryParameterNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import java.security.KeyPair;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p></p>
 *
 * @author Zakhar.Tomchenco
 * @version $Id$
 */
@Service
public class UserConverter implements ToServerConverter<ClientUser, User, ToServerConversionOptions>, ToClientConverter<User, ClientUser, ToClientConversionOptions> {

    @Resource
    private RoleConverter roleConverter;

    @Override
    public ClientUser toClient(User user, ToClientConversionOptions options) {
        ClientUser clientUser = new ClientUser();

        clientUser.setFullName(user.getFullName());
        clientUser.setEmailAddress(user.getEmailAddress());
        clientUser.setExternallyDefined(user.isExternallyDefined());
        clientUser.setEnabled(user.isEnabled());
        clientUser.setPreviousPasswordChangeTime(user.getPreviousPasswordChangeTime());
        clientUser.setTenantId(user.getTenantId());
        clientUser.setUsername(user.getUsername());
        clientUser.setNumberOfFailedLoginAttempts(user.getNumberOfFailedLoginAttempts());

        if (user.getRoles() != null){
            Set<ClientRole> roleSet = new HashSet<ClientRole>();
            // core class uses raw Set. Cast is safe
            @SuppressWarnings("unchecked")
            final Set<Role> roles = (Set<Role>) user.getRoles();
            for (Role role: roles){
                roleSet.add(roleConverter.toClient(role, null));
            }
            clientUser.setRoleSet(roleSet);
        }
        return clientUser;
    }

    @Override
    public User toServer(ExecutionContext ctx, ClientUser clientObject, User user, ToServerConversionOptions options) throws IllegalParameterValueException, MandatoryParameterNotFoundException {
        if (clientObject.getTenantId() != null) user.setTenantId(clientObject.getTenantId());
        if (clientObject.getUsername() != null) user.setUsername(clientObject.getUsername());
        if (clientObject.getEmailAddress() != null) user.setEmailAddress(clientObject.getEmailAddress());
        if (clientObject.getFullName() != null) user.setFullName(clientObject.getFullName());
        if (clientObject.isEnabled() != null) user.setEnabled(clientObject.isEnabled());
        if (clientObject.isExternallyDefined() != null) user.setExternallyDefined(clientObject.isExternallyDefined());
        if (clientObject.getNumberOfFailedLoginAttempts() != null) user.setNumberOfFailedLoginAttempts(clientObject.getNumberOfFailedLoginAttempts());
        if (clientObject.getPassword() != null) {
            if (user.getPassword()!= null && !clientObject.getPassword().equals(user.getPassword())){
                user.setPreviousPasswordChangeTime(new Date());
            }

            String pwd = tryDecryptPwdFromJCryption(clientObject.getPassword());
            user.setPassword(pwd);
        }
        if (clientObject.getRoleSet() != null) {
            Set<Role> newRoles = new HashSet<Role>();
            for (ClientRole role: clientObject.getRoleSet()){
                newRoles.add(roleConverter.toServer(ctx, role, null));
            }
            user.setRoles(newRoles);
        }

        return user;
    }

    private String tryDecryptPwdFromJCryption(String pwd) {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession sess = attr.getRequest().getSession();
        KeyPair keyPair = (KeyPair) sess.getAttribute(EncryptionManager.KEYPAIR_SESSION_KEY);

        if (keyPair != null) {
            EncryptionManager encMngr = new EncryptionManager();
            List<String> decList = encMngr.decrypt(keyPair.getPrivate(), pwd);
            if (decList.size() == 1)
                pwd = decList.get(0);
        }

        return pwd;
    }

    @Override
    public User toServer(ExecutionContext ctx, ClientUser clientObject, ToServerConversionOptions options) throws IllegalParameterValueException, MandatoryParameterNotFoundException {
        return toServer(ctx, clientObject, new UserImpl(), null);
    }

    @Override
    public String getServerResourceType() {
        return User.class.getName();
    }

    @Override
    public String getClientResourceType() {
        return ClientUser.class.getName();
    }

    public static  UserConverter getUserConverter() {
        UserConverter userConverter = new UserConverter();
        userConverter.roleConverter = new RoleConverter();
        return userConverter;
    }
}

