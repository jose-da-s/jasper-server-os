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

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InternalURI;
import com.jaspersoft.jasperserver.api.metadata.common.domain.PermissionUriProtocol;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.common.service.ResourceFactory;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.PersistentObjectResolver;
import com.jaspersoft.jasperserver.api.metadata.security.JasperServerPermission;
import com.jaspersoft.jasperserver.api.metadata.user.domain.ObjectPermission;
import com.jaspersoft.jasperserver.api.metadata.user.domain.Role;
import com.jaspersoft.jasperserver.api.metadata.user.domain.TenantQualified;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.ObjectPermissionImpl;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.RoleImpl;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.UserImpl;
import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.client.MetadataUserDetails;
import com.jaspersoft.jasperserver.api.metadata.user.service.ObjectPermissionService;
import com.jaspersoft.jasperserver.api.metadata.user.service.UserAuthorityService;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.AclPermissionsSecurityChecker;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.InternalURIDefinition;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.JasperServerSidRetrievalStrategyImpl;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.ObjectPermissionServiceImpl;
import com.jaspersoft.jasperserver.api.security.JasperServerAclHelper;
import com.jaspersoft.jasperserver.remote.exception.AccessDeniedException;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.remote.exception.ResourceAlreadyExistsException;
import com.jaspersoft.jasperserver.remote.exception.ResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.helpers.RecipientIdentity;
import com.jaspersoft.jasperserver.remote.services.PermissionsService;
import com.jaspersoft.jasperserver.remote.services.ResourcesManagementRemoteService;
import com.jaspersoft.jasperserver.remote.utils.AuditHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.acls.model.Permission;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.jaspersoft.jasperserver.api.logging.audit.domain.AuditEventType.CREATE_PERMISSION;
import static com.jaspersoft.jasperserver.api.logging.audit.domain.AuditEventType.DELETE_PERMISSION;
import static com.jaspersoft.jasperserver.api.logging.audit.domain.AuditEventType.UPDATE_PERMISSION;

/**
 * @author Volodya Sabadosh (vsabadosh@jaspersoft.com)
 * @author Zakhar Tomchenco (ztomchenco@jaspersoft.com)
 * @version $Id$
 */
@Component("permissionsService")
@Transactional(rollbackFor = Exception.class)
public class PermissionsServiceImpl implements PermissionsService {
    protected static final Set<Integer> ALLOWED_MASKS;

    static {
        Set<Integer> set = new HashSet<Integer>();
        for (com.jaspersoft.jasperserver.search.model.permission.Permission perm :
                com.jaspersoft.jasperserver.search.model.permission.Permission.values()) {
            set.add(perm.getMask());
        }
        ALLOWED_MASKS = Collections.unmodifiableSet(set);
    }

    @jakarta.annotation.Resource(name = "objectPermissionService")
    protected ObjectPermissionService objectPermissionService;
    @jakarta.annotation.Resource(name = "externalAclService")
    protected AclService aclService;
    @jakarta.annotation.Resource(name = "aclSecurityChecker")
    protected AclPermissionsSecurityChecker aclPermissionsSecurityChecker;
    @jakarta.annotation.Resource(name = "repositoryOIRetrievalStrategy")
    protected ObjectIdentityRetrievalStrategy repositoryOIRetrievalStrategy;
    @jakarta.annotation.Resource(name = "objectPermissionService")
    protected PersistentObjectResolver persistentObjectResolver;
    @jakarta.annotation.Resource(name = "concreteRepository")
    protected RepositoryService repositoryService;
    @jakarta.annotation.Resource
    protected AuditHelper auditHelper;
    @jakarta.annotation.Resource
    private ResourcesManagementRemoteService resourcesManagementRemoteService;
    @jakarta.annotation.Resource(name = "mappingResourceFactory")
    private ResourceFactory resourceFactory;
    @jakarta.annotation.Resource(name = "concreteUserAuthorityService")
    protected UserAuthorityService userAuthorityService;

    protected Comparator<Permission> permissionComparator = new Comparator<Permission>() {
        @Override
        public int compare(Permission o1, Permission o2) {
            // 1 is the highest mask value
            int mask1 = o1.equals(JasperServerPermission.ADMINISTRATION) ? Integer.MAX_VALUE : o1.getMask();
            int mask2 = o2.equals(JasperServerPermission.ADMINISTRATION) ? Integer.MAX_VALUE : o2.getMask();

            // 32 is a lowest except 0
            mask1 = mask1 == JasperServerPermission.EXECUTE.getMask() ? 1 : mask1;
            mask2 = mask2 == JasperServerPermission.EXECUTE.getMask() ? 1 : mask2;

            return mask1 - mask2;
        }
    };

    protected Comparator<AccessControlEntry> aclCompartor = new Comparator<AccessControlEntry>() {
        @Override
        public int compare(AccessControlEntry o1, AccessControlEntry o2) {
            int result = permissionComparator.compare(o1.getPermission(), o2.getPermission());

            if (result == 0) {
                //if masks are equal, closest(longer identity's uri) is bigger
                result = (o1.getAcl().getObjectIdentity().getIdentifier().toString().length()) -
                        (o2.getAcl().getObjectIdentity().getIdentifier().toString().length());
            }
            return result;
        }
    };

    public void setResourcesManagementRemoteService(ResourcesManagementRemoteService resourcesManagementRemoteService) {
        this.resourcesManagementRemoteService = resourcesManagementRemoteService;
    }

    public void setAuditHelper(AuditHelper auditHelper) {
        this.auditHelper = auditHelper;
    }

    public List<ObjectPermission> getPermissions(String resourceURI) throws ErrorDescriptorException {
        Resource resource = resourcesManagementRemoteService.locateResource(resourceURI);
        return objectPermissionService.getObjectPermissionsForObject(makeExecutionContext(), resource);
    }

    public List<ObjectPermission> getPermissions(String resourceURI, Class<?> recipientType, String recipientId, boolean effectivePermissions, boolean resolveAll) throws ErrorDescriptorException {
        List<ObjectPermission> result;

        PermissionUriProtocol uriProtocol = PermissionUriProtocol.RESOURCE;
        for (PermissionUriProtocol protocol : PermissionUriProtocol.values()) {
            String prefix = "/".concat(protocol.getProtocolPrefix());
            if (resourceURI.startsWith(prefix)) {
                resourceURI = resourceURI.substring(prefix.length());
                uriProtocol = protocol;
                break;
            } else if (resourceURI.startsWith(protocol.getProtocolPrefix())) {
                resourceURI = resourceURI.substring(protocol.getProtocolPrefix().length());
                uriProtocol = protocol;
                break;
            }
        }

        if (resolveAll) {
            result = resolveAll(resourceURI, recipientType, recipientId, effectivePermissions);
        } else {
            InternalURI resource = resolveResource(resourceURI, uriProtocol);
            if (effectivePermissions) {
                result = objectPermissionService.getEffectivePermissionsForObject(makeExecutionContext(), resource);
                //Normlize URI and Permission recipients, because getEffectivePermissionsForObject using ACL service to pickup permissions
                for(ObjectPermission op: result) {
                    normalizeObjectPermission(op);
                }
            } else {
                result = objectPermissionService.getObjectPermissionsForObject(makeExecutionContext(), resource);
            }
            if (recipientType != null) {
                result = filterByType(result, recipientType);
                if (recipientId != null) {
                    //just to make sure, that recipient exists
                    resolveRecipientObject(recipientType, recipientId);
                    result = filterById(result, recipientId);
                }
            }
        }
        return result;
    }

    private Object resolveRecipientFromSid(Sid sid) {
        if (sid instanceof PrincipalSid) {
            return resolveRecipientObject(User.class,((PrincipalSid) sid).getPrincipal());
        } else {
            return resolveRecipientObject(Role.class,((GrantedAuthoritySid)sid).getGrantedAuthority());
        }

    }
    public ObjectPermission getPermission(String resourceURI, Class<?> recipientType, String recipientId) throws ErrorDescriptorException {
        Object recipient = resolveRecipientObject(recipientType, recipientId);
        Resource object = resolveResource(resourceURI);
        List<ObjectPermission> permissions = objectPermissionService.getObjectPermissionsForObjectAndRecipient(makeExecutionContext(), object, recipient);
        return permissions.size() > 0 ? (ObjectPermission) permissions.get(0) : null;
    }

    public ObjectPermission getEffectivePermission(InternalURI internalURI, Role role) {
        Authentication authentication = createAuthentication(role);
        ObjectPermission permission = getEffectivePermission(internalURI, authentication);
        permission.setPermissionRecipient(role);
        return permission;
    }

    public ObjectPermission getEffectivePermission(InternalURI internalURI, User user) {
        Authentication authentication = createAuthentication(user);
        return getEffectivePermission(internalURI, authentication);
    }

    public ObjectPermission getEffectivePermission(InternalURI internalURI, Authentication authentication) {
        ObjectPermission permission = new ObjectPermissionImpl();
        permission.setPermissionRecipient(authentication.getPrincipal());
        List<Sid> sids = new JasperServerSidRetrievalStrategyImpl().getSids(authentication);
        Acl effectiveAcl = aclService.readAclById(repositoryOIRetrievalStrategy.getObjectIdentity(internalURI), sids);

        if (effectiveAcl != null) {
            List<AccessControlEntry> permissions = new ArrayList<AccessControlEntry>();
            AccessControlEntry ace;
            for (Sid sid : sids) {
                ace = JasperServerAclHelper.locateAceForSid(effectiveAcl, sid);
                if (ace != null) {
                    permissions.add(ace);
                }

            }
            if (!permissions.isEmpty()) {
                AccessControlEntry entry = Collections.max(permissions, aclCompartor);
                permission.setPermissionMask(entry.getPermission().getMask());
                permission.setURI(this.extractUriFromAcl(entry.getAcl()));

            } else {
                permission.setPermissionMask(JasperServerPermission.NOTHING.getMask());
            }

        } else {
            permission.setPermissionMask(JasperServerPermission.NOTHING.getMask());
        }
        return permission;
    }

    public List<ObjectPermission> getPermissionsForObject(String targetURI) throws ErrorDescriptorException {
        if (!aclPermissionsSecurityChecker.isPermitted(JasperServerPermission.ADMINISTRATION, targetURI)) {
            throw new AccessDeniedException("Access is denied");
        }
        Resource res = repositoryService.getResource(makeExecutionContext(), targetURI);
        if (res == null) {
            res = repositoryService.getFolder(makeExecutionContext(), targetURI);

            if (res == null) {
                throw new ErrorDescriptorException("There is no resource or folder for target URI \"" + targetURI + "\"");
            }
        }

        return objectPermissionService.getObjectPermissionsForObject(makeExecutionContext(), res);
    }

    public ObjectPermission createPermission(ObjectPermission objectPermission) throws ErrorDescriptorException {
        return doPutPermission(objectPermission, false);
    }

    public ObjectPermission putPermission(ObjectPermission objectPermission) throws ErrorDescriptorException {
        return doPutPermission(objectPermission, true);
    }

    public List<ObjectPermission> putPermissions(InternalURI internalURI, List<ObjectPermission> objectPermissions) throws ErrorDescriptorException {
        PermissionUriProtocol protocol = PermissionUriProtocol.fromString(internalURI.getProtocol());
        String uri = protocol.addPrefix(internalURI.getPath());

        // make sure that permissions will be definitely assigned to this resource
        for (ObjectPermission permission : objectPermissions) {
            permission.setURI(uri);
        }
        return doPutPermissions(internalURI, objectPermissions, false);
    }

    public List<ObjectPermission> createPermissions(List<ObjectPermission> objectPermissions) throws ErrorDescriptorException {
        return doPutPermissions(null, objectPermissions, true);
    }

    public void deletePermission(ObjectPermission objectPermission) throws ErrorDescriptorException {
        changePermissionConsistencyCheck(objectPermission);

        auditHelper.createAuditEvent(DELETE_PERMISSION.toString());
        objectPermissionService.deleteObjectPermission(makeExecutionContext(), objectPermission);
        auditHelper.closeAuditEvent(DELETE_PERMISSION.toString());
    }

    public ObjectPermission newObjectPermission() {
        return objectPermissionService.newObjectPermission(makeExecutionContext());
    }


    /**
     * Returns permission mask for resource with <strong>targetURI</strong></> for current user.
     *
     * @param targetURI resource URI.
     * @return
     * @throws ErrorDescriptorException
     */
    public int getAppliedPermissionMaskForObjectAndCurrentUser(String targetURI) throws ErrorDescriptorException {
        Resource resource = repositoryService.getResource(makeExecutionContext(), targetURI);
        if (resource == null) {
            resource = repositoryService.getFolder(makeExecutionContext(), targetURI);

            if (resource == null) {
                throw new ErrorDescriptorException("There is no resource or folder for target URI \"" + targetURI + "\"");
            }
        }

        Set<Integer> allUserPermissions = null;

        Authentication authenticationToken = SecurityContextHolder.getContext().getAuthentication();
        List<Sid> sids = new JasperServerSidRetrievalStrategyImpl().getSids(authenticationToken);
        Acl acl = aclService.readAclById(repositoryOIRetrievalStrategy.getObjectIdentity(resource), sids);
        CumulativePermission cPermission = new CumulativePermission();
        for (Sid sid : sids) {
            cPermission.set(JasperServerAclHelper.getPermissionForSid(acl, sid));
        }
        return cPermission.getMask();
    }


    protected void changePermissionConsistencyCheck(ObjectPermission objectPermission) throws ErrorDescriptorException {
        if (StringUtils.isBlank(objectPermission.getURI())) {
            throw new IllegalParameterValueException("URI is blank", "uri", objectPermission.getURI());
        }

        if (!ALLOWED_MASKS.contains(objectPermission.getPermissionMask())) {
            throw new IllegalParameterValueException("mask", Integer.toString(objectPermission.getPermissionMask()));
        }

        objectPermission.setPermissionRecipient(getPermissionRecipient(objectPermission.getPermissionRecipient()));

        // make sure, that resource exists
        resolveResource(objectPermission.getURI());
    }

    protected Object getPermissionRecipient(Object rawRecipient) {
        if (rawRecipient == null) {
            throw new IllegalParameterValueException("recipient", "null");
        } else if (rawRecipient instanceof RecipientIdentity) {
            RecipientIdentity identity = (RecipientIdentity) rawRecipient;
            return resolveRecipientObject(identity.getRecipientClass(), identity.getId());
        } else if (rawRecipient instanceof TenantQualified) {
            if (persistentObjectResolver.getPersistentObject(rawRecipient) == null) {
                if (rawRecipient instanceof User) {
                    User user = (User) rawRecipient;
                    throw new ResourceNotFoundException(user.getUsername());
                }
                if (rawRecipient instanceof Role) {
                    Role role = (Role) rawRecipient;
                    throw new ResourceNotFoundException(role.getRoleName());
                }
            }
        } else {
            throw new IllegalStateException("Unknown recipient class:" + rawRecipient.getClass().getName());
        }

        return rawRecipient;
    }

    public void setObjectPermissionService(ObjectPermissionService objectPermissionService) {
        this.objectPermissionService = objectPermissionService;
    }

    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public void setUserAuthorityService(UserAuthorityService userAuthorityService) {
        this.userAuthorityService = userAuthorityService;
    }

    protected ExecutionContext makeExecutionContext() {
        return ExecutionContextImpl.getRuntimeExecutionContext();
    }

    protected ObjectPermission doPutPermission(ObjectPermission objectPermission, boolean allowUpdate) throws ErrorDescriptorException {
        changePermissionConsistencyCheck(objectPermission);

        ObjectPermission existingObjectPermission =
                objectPermissionService.getObjectPermission(makeExecutionContext(), objectPermission);
        String auditEventType;

        if (existingObjectPermission == null) {
            auditEventType = CREATE_PERMISSION.toString();
        } else {
            if (allowUpdate) {
                auditEventType = UPDATE_PERMISSION.toString();
            } else {
                throw new ResourceAlreadyExistsException(objectPermission.getURI() + ";" + ((InternalURI) objectPermission.getPermissionRecipient()).getURI());
            }
        }

        auditHelper.createAuditEvent(auditEventType);
        objectPermissionService.putObjectPermission(makeExecutionContext(), objectPermission);
        auditHelper.closeAuditEvent(auditEventType);

        return objectPermissionService.getObjectPermission(makeExecutionContext(), objectPermission);
    }

    protected List<ObjectPermission> doPutPermissions(InternalURI internalURI, List<ObjectPermission> objectPermissions, boolean addTo) throws ErrorDescriptorException {
        if (!addTo) {
            String uri = internalURI.getPath();
            PermissionUriProtocol protocol = PermissionUriProtocol.fromString(internalURI.getProtocol());
            if (protocol == PermissionUriProtocol.ATTRIBUTE) {
               uri = protocol.addPrefix(uri);
            }

            List<ObjectPermission> existing = this.getPermissions(uri, null, null, false, false);
            for (ObjectPermission permission : existing) {
                this.deletePermission(permission);
            }
        }

        List<ObjectPermission> result = new LinkedList<ObjectPermission>();
        for (ObjectPermission permission : objectPermissions) {
            result.add(this.createPermission(permission));
        }
        return result;
    }

    protected String extractUriFromAcl(Acl entry) {
        final ObjectIdentity objectIdentity = entry.getObjectIdentity();
        return objectIdentity instanceof ObjectPermissionServiceImpl.URIObjectIdentity
                ? ((ObjectPermissionServiceImpl.URIObjectIdentity) objectIdentity).getURI()
                : objectIdentity.getIdentifier().toString();
    }
    protected ObjectPermission normalizeObjectPermission(ObjectPermission op) {
        op.setURI(normalizeUri(op.getURI()));
        if (op.getPermissionRecipient() instanceof Sid) {
            op.setPermissionRecipient(resolveRecipientFromSid((Sid) op.getPermissionRecipient()));
        }
        return op;
    }

    protected String normalizeUri(String uri) {
        return uri;
    }

    protected Object resolveRecipientObject(Class<?> clazz, String id) throws ResourceNotFoundException {
        Object res = null;
        if (Role.class.equals(clazz)) {
            Role role = new RoleImpl();
            role.setRoleName(id);
            res = role;
        }
        if (User.class.equals(clazz)) {
            User user = new UserImpl();
            user.setUsername(id);
            res = user;
        }

        if (persistentObjectResolver.getPersistentObject(res) == null) {
            throw new ResourceNotFoundException(clazz.getSimpleName() + " " + id);
        }

        return res;
    }

    protected InternalURI resolveResource(String uri, PermissionUriProtocol protocol) throws ErrorDescriptorException {
        InternalURI resource;
        if (protocol == PermissionUriProtocol.RESOURCE) {
            resource = repositoryService.getResource(makeExecutionContext(), uri);
            if (resource == null) {
                resource = repositoryService.getFolder(makeExecutionContext(), uri);
            }
            if (resource == null) {
                throw new ResourceNotFoundException(uri);
            }

            if (!aclPermissionsSecurityChecker.isPermitted(JasperServerPermission.ADMINISTRATION, resource)) {
                throw new AccessDeniedException("Access denied", uri);
            }
        } else {
            resource = new InternalURIDefinition(uri, protocol);
        }

        return resource;
    }

    protected Resource resolveResource(String uri) throws ErrorDescriptorException {
        if (uri.startsWith(REPO_URI_PREFIX)) {
            uri = uri.substring(REPO_URI_PREFIX.length());
        }

        Resource resource = repositoryService.getResource(makeExecutionContext(), uri);
        if (resource == null) {
            resource = repositoryService.getFolder(makeExecutionContext(), uri);
        }
        if (resource == null) {
            throw new ResourceNotFoundException(uri);
        }

        if (!aclPermissionsSecurityChecker.isPermitted(JasperServerPermission.ADMINISTRATION, resource)) {
            throw new AccessDeniedException("Access denied", uri);
        }

        return resource;
    }

    protected boolean isSameId(Object permissionRecipient, String recipientId) {
        return permissionRecipient instanceof User && ((User) permissionRecipient).getUsername().equals(recipientId) ||
                permissionRecipient instanceof Role && ((Role) permissionRecipient).getRoleName().equals(recipientId);
    }

    protected List<ObjectPermission> filterByType(List<ObjectPermission> data, Class<?> recipientType) {
        List<ObjectPermission> result = new LinkedList<ObjectPermission>();
        for (ObjectPermission permission : data) {
            if (recipientType.isInstance(permission.getPermissionRecipient())) {
                result.add(permission);
            }
        }
        return result;
    }

    protected List<ObjectPermission> filterById(List<ObjectPermission> data, String recipientId) {
        List<ObjectPermission> result = new LinkedList<ObjectPermission>();
        for (ObjectPermission permission : data) {
            if (isSameId(permission.getPermissionRecipient(), recipientId)) {
                result.add(permission);
            }
        }
        return result;
    }

    protected Authentication createAuthentication(Role role) {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(1);
        authorities.add(new SimpleGrantedAuthority(role.getRoleName()));
        MetadataUserDetails dummy = new MetadataUserDetails(new UserImpl());
        dummy.setUsername("dummyUserdummyUserdummyUserdummyUserdummyUser");

        return new UsernamePasswordAuthenticationToken(dummy, null, authorities);
    }

    protected Authentication createAuthentication(User user) {
        final Set<Role> roles = user.getRoles();
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(roles.size());

        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.getRoleName()));
        }

        return new UsernamePasswordAuthenticationToken(new MetadataUserDetails(user), null, authorities);
    }

    private List<ObjectPermission> resolveAll(String resourceURI, Class<?> recipientType, String recipientId, boolean effectivePermissions) throws ErrorDescriptorException {
        List<ObjectPermission> res;
        if (recipientType == null) {
            res = resolveAllUsers(resourceURI, recipientId, effectivePermissions);
            res.addAll(resolveAllRoles(resourceURI, recipientId));
        } else {
            if (recipientType.equals(User.class)) {
                res = resolveAllUsers(resourceURI, recipientId, effectivePermissions);
            } else {
                res = resolveAllRoles(resourceURI, recipientId);
            }
        }
        return res;
    }

    private List<ObjectPermission> resolveAllRoles(String resourceURI, String recipientId) throws ErrorDescriptorException {
        List<Role> roles = getRolesForResource(resourceURI, recipientId);
        Resource resource = resolveResource(resourceURI);

        List<ObjectPermission> res = new LinkedList<ObjectPermission>();
        for (Role role : roles) {
            res.add(getEffectivePermission(resource, role));
            }

        return res;
    }

    private List<ObjectPermission> resolveAllUsers(String resourceURI, String recipientId, boolean effectivePermissions) throws ErrorDescriptorException {
        List<User> users = getUsersForResource(resourceURI, recipientId);
        Resource resource = resolveResource(resourceURI);

        List<ObjectPermission> res = new LinkedList<ObjectPermission>();
        ObjectPermission permission;
        for (User user : users) {
            if (effectivePermissions) {
                permission = getEffectivePermission(resource, user);
            } else {
                //create authentication without roles, based on user, so result will be for user only
                user.setRoles(new HashSet<Role>());
                Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, new ArrayList<GrantedAuthority>());
                permission = getEffectivePermission(resource, authentication);
            }
            res.add(permission);
        }
        return res;
    }

    protected List<User> getUsersForResource(String resourceURI, String recipientId) {
        return userAuthorityService.getTenantUsers(null, null, null);
    }

    protected List getRolesForResource(String resourceURI, String recipientId) {
        return userAuthorityService.getTenantRoles(null, null, null);
    }
}

