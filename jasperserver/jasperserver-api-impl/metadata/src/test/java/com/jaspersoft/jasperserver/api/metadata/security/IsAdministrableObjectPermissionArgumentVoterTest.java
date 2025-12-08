/*
 * Copyright (C) 2025 the Jasper Server OS Authors
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

import com.jaspersoft.jasperserver.api.metadata.user.domain.ObjectPermission;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.ObjectPermissionImpl;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.UserImpl;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.JasperServerSidRetrievalStrategyImpl;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.ResourceObjectIdentityRetrievalStrategyImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.acls.domain.AccessControlEntryImpl;
import org.springframework.security.acls.model.*;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * <p></p>
 *
 * @author Zakhar.Tomchenco
 * @version $Id$
 */
public class IsAdministrableObjectPermissionArgumentVoterTest {
    @Mock
    Authentication authentication;
    ObjectPermission permission = new ObjectPermissionImpl();
    User user = new UserImpl();
    Object object;
    Sid sid;
    List<Sid> sids;
    ObjectIdentityRetrievalStrategy oidStrategy;
    ObjectIdentity oid;

    @InjectMocks
    IsAdministrableObjectPermissionArgumentVoter voter = new IsAdministrableObjectPermissionArgumentVoter();
    @Mock
    private AclService provider;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        user.setUsername("name");
        user.setTenantId("tenant");

        User recipient = new UserImpl();
        recipient.setUsername(user.getUsername());
        recipient.setTenantId(user.getTenantId());
        sid = new JasperServerSidRetrievalStrategyImpl().getSid(recipient);
        sids = new JasperServerSidRetrievalStrategyImpl().getSids(authentication);
        sids.add(sid);
        oidStrategy = new ResourceObjectIdentityRetrievalStrategyImpl();

        permission.setPermissionRecipient(recipient);
        permission.setURI("repo:/");
        permission.setPermissionMask(1);
        oid = oidStrategy.getObjectIdentity(permission.getURI());

    }

    @Test
    public void testSupports() throws Exception {
        List entries = new ArrayList<AccessControlEntry>();
        Acl acl = new JasperServerAclImpl(oid,null);
        entries.add(new AccessControlEntryImpl(null,acl, sid,JasperServerPermission.ADMINISTRATION,true,false,false));
        acl = new JasperServerAclImpl(oid,entries);
        when(provider.readAclById(any(ObjectIdentity.class), any())).thenReturn(acl);
        when(authentication.getPrincipal()).thenReturn(user);

        assertTrue(voter.isPermitted(authentication, permission, object));
        assertTrue(true);
    }

    @Test
    public void testSupports_not() throws Exception {
        List entries = new ArrayList<AccessControlEntry>();
        Acl acl = new JasperServerAclImpl(oid,null);
        entries.add(new AccessControlEntryImpl(null,acl, sid,JasperServerPermission.READ,true,false,false));
        acl = new JasperServerAclImpl(oid,entries);

        when(provider.readAclById(any(ObjectIdentity.class), any())).thenReturn(acl);
        when(authentication.getPrincipal()).thenReturn(user);

        assertFalse(voter.isPermitted(authentication, permission, object));
    }

    @Test
    public void testSupports_notMany() throws Exception {
        List entries = new ArrayList<AccessControlEntry>();
        Acl acl = new JasperServerAclImpl(oid,null);
        entries.add(new AccessControlEntryImpl(null,acl, sid,JasperServerPermission.READ,true,false,false));
        entries.add(new AccessControlEntryImpl(null,acl, sid,JasperServerPermission.WRITE,true,false,false));
        acl = new JasperServerAclImpl(oid,entries);

        when(provider.readAclById(any(ObjectIdentity.class), any())).thenReturn(acl);
        when(authentication.getPrincipal()).thenReturn(user);

        assertFalse(voter.isPermitted(authentication, permission, object));
    }

    @Test
    public void testSupports_many() throws Exception {
        List entries = new ArrayList<AccessControlEntry>();
        Acl acl = new JasperServerAclImpl(oid,null);
        entries.add(new AccessControlEntryImpl(null,acl, sid,JasperServerPermission.READ,true,false,false));
        entries.add(new AccessControlEntryImpl(null,acl, sid,JasperServerPermission.ADMINISTRATION,true,false,false));
        acl = new JasperServerAclImpl(oid,entries);
        when(provider.readAclById(any(ObjectIdentity.class), any())).thenReturn(acl);
        when(authentication.getPrincipal()).thenReturn(user);

        assertFalse(voter.isPermitted(authentication, permission, object));
    }

    @Test
    public void testSupports_empty() throws Exception {
        Acl acl = new JasperServerAclImpl(oid,null);
        when(provider.readAclById(any(ObjectIdentity.class), any())).thenReturn(acl);
        assertFalse(voter.isPermitted(authentication, permission, object));
    }

    @Test
    public void testSupports_null() throws Exception {
        when(provider.readAclById(any(ObjectIdentity.class), any())).thenReturn(null);

        assertFalse(voter.isPermitted(authentication, permission, object));
    }


}
