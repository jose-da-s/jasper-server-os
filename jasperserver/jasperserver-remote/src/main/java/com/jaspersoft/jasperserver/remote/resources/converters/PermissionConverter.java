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
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InternalURI;
import com.jaspersoft.jasperserver.api.metadata.common.domain.PermissionUriProtocol;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConversionOptions;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConverter;
import com.jaspersoft.jasperserver.api.metadata.user.domain.ObjectPermission;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.ObjectPermissionImpl;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.UserImpl;
import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.client.MetadataUserDetails;
import com.jaspersoft.jasperserver.dto.permissions.RepositoryPermission;
import com.jaspersoft.jasperserver.remote.exception.MandatoryParameterNotFoundException;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.helpers.RecipientIdentityResolver;
import com.jaspersoft.jasperserver.remote.services.PermissionsService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * <p></p>
 *
 * @author Zakhar.Tomchenco
 * @version $Id$
 */
@Component
public class PermissionConverter implements ToClientConverter<ObjectPermission, RepositoryPermission, ToClientConversionOptions>, ToServerConverter<RepositoryPermission, ObjectPermission, ToServerConversionOptions> {
    @Resource(name = "concretePermissionsRecipientIdentityResolver")
    private RecipientIdentityResolver resolver;

    public RepositoryPermission toClient(ObjectPermission serverObject, ToClientConversionOptions options) {
        RepositoryPermission client = new RepositoryPermission(stripRepo(serverObject.getURI()), null, serverObject.getPermissionMask());

        if (serverObject.getPermissionRecipient() instanceof InternalURI){
            client.setRecipient(((InternalURI) serverObject.getPermissionRecipient()).getURI());
        } else if (serverObject.getPermissionRecipient() instanceof MetadataUserDetails) {
            User internalUri = new UserImpl(), metaUser = (User)serverObject.getPermissionRecipient();
            internalUri.setUsername(metaUser.getUsername());
            internalUri.setTenantId(metaUser.getTenantId());
            client.setRecipient(((InternalURI) internalUri).getURI());
        }
        return client;
    }

    public String getClientResourceType() {
        return RepositoryPermission.class.getName();
    }

    public ObjectPermission toServer(ExecutionContext ctx, RepositoryPermission clientObject, ToServerConversionOptions options) throws IllegalParameterValueException, MandatoryParameterNotFoundException {
        return toServer(ctx, clientObject, new ObjectPermissionImpl(), null);
    }

    public ObjectPermission toServer(ExecutionContext ctx, RepositoryPermission clientObject, ObjectPermission resultToUpdate, ToServerConversionOptions options) throws IllegalParameterValueException, MandatoryParameterNotFoundException {
        if (clientObject.getMask() == null) {
             throw new MandatoryParameterNotFoundException("mask");
        }
        if (clientObject.getRecipient() == null) {
            throw new MandatoryParameterNotFoundException("recipient");
        }
        if (clientObject.getUri() == null) {
            throw new MandatoryParameterNotFoundException("uri");
        }

        resultToUpdate.setPermissionMask(clientObject.getMask());
        resultToUpdate.setPermissionRecipient(resolver.toIdentity(clientObject.getRecipient()));
        resultToUpdate.setURI(addRepo(clientObject.getUri()));

        return resultToUpdate;
    }

    public String getServerResourceType() {
        return ObjectPermission.class.getName();
    }

    private String stripRepo(String source){
        if (source != null && source.startsWith(PermissionsService.REPO_URI_PREFIX)){
            source = source.substring(PermissionsService.REPO_URI_PREFIX.length());
        }
        return source;
    }

    private String addRepo(String source){
        PermissionUriProtocol permissionUriProtocol = PermissionUriProtocol.RESOURCE;
        for (PermissionUriProtocol protocol : PermissionUriProtocol.values()) {
            if (source.startsWith(protocol.getProtocolPrefix())) {
                permissionUriProtocol = protocol;
            }
        }

        if (permissionUriProtocol == PermissionUriProtocol.RESOURCE && !source.startsWith(permissionUriProtocol.getProtocolPrefix())){
            source = permissionUriProtocol.getProtocolPrefix().concat(ensureRoot(source));
        }
        return source;

    }

    private String ensureRoot(String uri){
        if (!uri.startsWith(Folder.SEPARATOR)){
            uri = Folder.SEPARATOR.concat(uri);
        }
        return uri;
    }
}

