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
import com.jaspersoft.jasperserver.api.metadata.user.domain.ProfileAttribute;
import com.jaspersoft.jasperserver.api.metadata.user.service.AttributesSearchResult;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.AttributePathTransformer;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.AttributesSearchResultImpl;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.InternalURIDefinition;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.JasperServerSidRetrievalStrategyImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.acls.model.SidRetrievalStrategy;
import org.springframework.security.core.Authentication;

import jakarta.annotation.Resource;
import java.util.Collection;
    import java.util.LinkedList;
import java.util.List;

/**
 * @author Volodya Sabadosh
 * @version $Id: $
 */
public class AttributeArgumentVoter extends BasicObjectArgumentVoter<ProfileAttribute> {
    private String configAttribute;

    @Resource(name = "internalAclService")
    private AclService resourcesAclService;

    private List<Permission> requiredPermissions;
    @Resource
    private AttributePathTransformer attributePathTransformer;

    protected SidRetrievalStrategy sidStrategy;

    //Default Constructor
    public AttributeArgumentVoter() {
        sidStrategy = new JasperServerSidRetrievalStrategyImpl();
    }

    @Override
    protected boolean isPermitted(Authentication authentication, ProfileAttribute filteredObject, Object object) {
        String checkPermissionUri = attributePathTransformer.transformPath(filteredObject.getPath(), authentication);
        List<Permission> requiredPermissions = generateRequiredPermissions(object);
        List<Sid> checkSids = generateSids(authentication, object);

        Acl acl;
        try {
            acl = resourcesAclService.readAclById(new InternalURIDefinition(checkPermissionUri,
                    PermissionUriProtocol.ATTRIBUTE), checkSids);
        } catch (JSException e) {
            // in some cases we are trying to reach not reachable resource, this will throw error
            acl = null;
        }
        return acl != null && acl.isGranted(requiredPermissions, checkSids, false);
    }

    public Object decide(Authentication authentication, Object object, Collection<ConfigAttribute> attributes,
                         Object returnedObject) throws AccessDeniedException {
        if (supports(object, attributes) && returnedObject != null && returnedObject instanceof AttributesSearchResult) {
            List<?> originalAttributes = ((AttributesSearchResult)returnedObject).getList();

            Collection<ProfileAttribute> filteredAttributes = getFilteredObjects(originalAttributes);
            List<ProfileAttribute>  resList = new LinkedList<ProfileAttribute>();

            for (ProfileAttribute filteredObject : filteredAttributes) {
                if (isPermitted(authentication, filteredObject, object)) {
                    resList.add(filteredObject);
                }
            }
            AttributesSearchResult<ProfileAttribute> searchResult = new AttributesSearchResultImpl<ProfileAttribute>();

            searchResult.setTotalCount(resList.size());
            searchResult.setList(resList);

            return searchResult;
        }  else {
            return super.decide(authentication, object, attributes, returnedObject);
        }
    }


    @Override
    public boolean supports(ConfigAttribute attribute) {
        return this.configAttribute.equals(attribute.getAttribute());
    }

    protected List<Sid> generateSids(Authentication authentication, Object secureObject) {
        return sidStrategy.getSids(authentication);
    }

    protected List<Permission> generateRequiredPermissions(Object secureObject) {
        return this.requiredPermissions;
    }

    public void setConfigAttribute(String configAttribute) {
        this.configAttribute = configAttribute;
    }

    public void setRequiredPermissions(List<Permission> requiredPermissions) {
        this.requiredPermissions = requiredPermissions;
    }

}

