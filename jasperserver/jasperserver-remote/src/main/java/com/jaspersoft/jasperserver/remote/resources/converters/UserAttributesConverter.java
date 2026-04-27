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
import com.jaspersoft.jasperserver.api.metadata.common.domain.InternalURI;
import com.jaspersoft.jasperserver.api.metadata.common.domain.PermissionUriProtocol;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConversionOptions;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConverter;
import com.jaspersoft.jasperserver.api.metadata.user.domain.ProfileAttribute;
import com.jaspersoft.jasperserver.api.metadata.user.domain.TenantQualified;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.ProfileAttributeImpl;
import com.jaspersoft.jasperserver.api.metadata.user.service.AttributesSearchCriteria;
import com.jaspersoft.jasperserver.api.metadata.user.service.AttributesSearchResult;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeLevel;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeService;
import com.jaspersoft.jasperserver.api.metadata.user.service.TenantService;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.AttributePathTransformer;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.InternalURIDefinition;
import com.jaspersoft.jasperserver.dto.authority.ClientAttribute;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.exception.MandatoryParameterNotFoundException;
import com.jaspersoft.jasperserver.remote.helpers.AttributesConfig;
import com.jaspersoft.jasperserver.remote.helpers.RecipientIdentityResolver;
import com.jaspersoft.jasperserver.remote.services.PermissionsService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.regex.Pattern;

/**
 * <p></p>
 *
 * @author Zakhar.Tomchenco
 * @author Volodya Sabadosh
 * @version $Id$
 */
@Service
public class UserAttributesConverter implements ToServerConverter<ClientAttribute, ProfileAttribute, ToServerConversionOptions>,
        ToClientConverter<ProfileAttribute, ClientAttribute, ToClientConversionOptions> {
    @Resource(name = "attributesPermissionService")
    private PermissionsService attributesPermissionService;
    @Resource(name="concreteAttributesRecipientIdentityResolver")
    protected RecipientIdentityResolver recipientIdentityResolver;
    @Resource
    private AttributePathTransformer attributePathTransformer;
    @Resource
    private ProfileAttributeService profileAttributeService;
    @Resource
    private AttributesConfig attributesConfig;

    private Pattern empty = Pattern.compile("^\\s*$");

    protected static final Pattern PATTERN_RESOURCE_NAME_REPLACE = Pattern.compile("[/\\\\]");

    @Override
    public ClientAttribute toClient(ProfileAttribute serverObject, ToClientConversionOptions options) {
        ClientAttribute client = new ClientAttribute();
        client.setName(serverObject.getAttrName());
        if (serverObject.isSecure()) {
            client.setSecure(true);
        } else {
            client.setValue(serverObject.getAttrValue());
        }
        if (serverObject.getLevel() == ProfileAttributeLevel.PARENT) {
            client.setInherited(true);
        }
        if (isVisibleAttrHolder(serverObject)) {
            client.setHolder(recipientIdentityResolver.toRecipientUri(serverObject.getPrincipal()));
        }
        client.setDescription(serverObject.getDescription());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String checkSecurityForAttrPath = attributePathTransformer.transformPath(serverObject.getPath(), authentication);
        InternalURI internalURI = new InternalURIDefinition(checkSecurityForAttrPath, PermissionUriProtocol.ATTRIBUTE);
        client.setPermissionMask(attributesPermissionService.getEffectivePermission(internalURI, authentication).getPermissionMask());

       return client;
    }

    @Override
    public ProfileAttribute toServer(ExecutionContext ctx, ClientAttribute clientObject, ToServerConversionOptions options) throws IllegalParameterValueException, MandatoryParameterNotFoundException {
        return toServer(ctx, clientObject, new ProfileAttributeImpl(), null);
    }

    @Override
    public ProfileAttribute toServer(ExecutionContext ctx, ClientAttribute clientObject, ProfileAttribute resultToUpdate, ToServerConversionOptions options) throws IllegalParameterValueException, MandatoryParameterNotFoundException {
        if (isEmpty(clientObject.getName()) || clientObject.getName().length() > attributesConfig.getMaxLengthAttrName()) {
            throw new IllegalParameterValueException("name", clientObject.getName());
        }
        if (clientObject.getValue() == null && !(clientObject.isSecure() != null && clientObject.isSecure()) ||
                clientObject.getValue() != null && clientObject.getValue().length() > attributesConfig.getMaxLengthAttrValue()) {
            throw new IllegalParameterValueException("value", clientObject.getValue());
        }
        if (clientObject.getDescription() != null && clientObject.getDescription().length() > attributesConfig.getMaxLengthDescription()) {
            throw new IllegalParameterValueException("description", clientObject.getDescription());
        }

        if (isEmpty(clientObject.getHolder())) {
            throw new IllegalParameterValueException("principal", clientObject.getHolder());
        }

        Object attrHolder = recipientIdentityResolver.resolveRecipientObject(clientObject.getHolder());
        if (clientObject.getValue() == null && clientObject.isSecure()) {
            AttributesSearchCriteria searchCriteria =
                    new AttributesSearchCriteria.Builder().setNames(Collections.singleton(clientObject.getName())).build();
            AttributesSearchResult<ProfileAttribute> searchResult
                    = profileAttributeService.getProfileAttributesForPrincipal(null, attrHolder, searchCriteria);
            if (searchResult.getList().size() > 0) {
                clientObject.setValue(searchResult.getList().get(0).getAttrValue());
            } else {
                throw new IllegalParameterValueException("value", clientObject.getValue());
            }
        }

        resultToUpdate.setAttrName(makeAttributeName(clientObject.getName()));
        resultToUpdate.setAttrValue(clientObject.getValue());
        resultToUpdate.setSecure(clientObject.isSecure() != null && clientObject.isSecure());
        resultToUpdate.setDescription(clientObject.getDescription());

        resultToUpdate.setPrincipal(attrHolder);
        resultToUpdate.setUri(clientObject.getName(), profileAttributeService.generateAttributeHolderUri(resultToUpdate.getPrincipal()));

        return resultToUpdate;
    }

    protected String makeAttributeName(String attrName) {
        return PATTERN_RESOURCE_NAME_REPLACE.matcher(attrName).replaceAll("_");
    }

    protected String getAuthenticatedTenantId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            if (auth.getPrincipal() instanceof TenantQualified) {
                return  ((TenantQualified) auth.getPrincipal()).getTenantId();
            }
        }
        return null;
    }

    protected boolean isVisibleAttrHolder(ProfileAttribute serverObject) {
        String tenantId = getAuthenticatedTenantId();
        if (tenantId == null || tenantId.equals(TenantService.ORGANIZATIONS)) {
            //For root admin
            return true;
        } else {
            String orgTemplate = AttributePathTransformer.TENANT_MARKER.concat(tenantId);
            return serverObject.getPath().contains(orgTemplate);
        }
    }

    protected boolean isEmpty(String val) {
        return val == null || empty.matcher(val).matches();
    }

    public void setProfileAttributeService(ProfileAttributeService profileAttributeService) {
        this.profileAttributeService = profileAttributeService;
    }

    public void setAttributesPermissionService(PermissionsService attributesPermissionService) {
        this.attributesPermissionService = attributesPermissionService;
    }

    public void setRecipientIdentityResolver(RecipientIdentityResolver recipientIdentityResolver) {
        this.recipientIdentityResolver = recipientIdentityResolver;
    }

    public void setAttributePathTransformer(AttributePathTransformer attributePathTransformer) {
        this.attributePathTransformer = attributePathTransformer;
    }

    public void setAttributesConfig(AttributesConfig attributesConfig) {
        this.attributesConfig = attributesConfig;
    }

    @Override
    public String getServerResourceType() {
        return ProfileAttribute.class.getName();
    }

    @Override
    public String getClientResourceType() {
        return ClientAttribute.class.getName();
    }
}

