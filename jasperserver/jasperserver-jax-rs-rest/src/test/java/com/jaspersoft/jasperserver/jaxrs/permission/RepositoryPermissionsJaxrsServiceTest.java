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

package com.jaspersoft.jasperserver.jaxrs.permission;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InternalURI;
import com.jaspersoft.jasperserver.api.metadata.common.domain.PermissionUriProtocol;
import com.jaspersoft.jasperserver.api.metadata.user.domain.ObjectPermission;
import com.jaspersoft.jasperserver.api.metadata.user.domain.Role;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.ObjectPermissionImpl;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.RoleImpl;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.UserImpl;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.InternalURIDefinition;
import com.jaspersoft.jasperserver.dto.permissions.RepositoryPermission;
import com.jaspersoft.jasperserver.dto.permissions.RepositoryPermissionListWrapper;
import com.jaspersoft.jasperserver.jaxrs.common.RestConstants;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.exception.ResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.helpers.RecipientIdentity;
import com.jaspersoft.jasperserver.remote.helpers.RecipientIdentityResolver;
import com.jaspersoft.jasperserver.remote.resources.converters.PermissionConverter;
import com.jaspersoft.jasperserver.remote.services.PermissionsService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * <p></p>
 *
 * @author Zakhar.Tomchenco
 * @version $Id$
 */
public class RepositoryPermissionsJaxrsServiceTest {
    @InjectMocks
    private RepositoryPermissionsJaxrsService service;

    @Mock private PermissionsService permissionsService;
    @Mock private RecipientIdentityResolver permissionRecipientIdentityResolver;
    @Mock private PermissionConverter converter;

    private final List<ObjectPermission> mockPermissons = new ArrayList<ObjectPermission>(10);

    private final String userUri = "user:/me";
    private final RecipientIdentity userIdentity = new RecipientIdentity(User.class, "me");

    private final String roleUri = "role:/ROLE_SITH";
    private final RecipientIdentity roleIdentity = new RecipientIdentity(Role.class, "ROLE_SITH");

    private final ObjectPermission serverPermission = new ObjectPermissionImpl();
    private final RepositoryPermission clientPermission = new RepositoryPermission();
    ExecutionContext ctx = ExecutionContextImpl.getRuntimeExecutionContext();

    @BeforeClass
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(permissionRecipientIdentityResolver.getClassForProtocol("user")).thenReturn((Class)User.class);
        when(permissionRecipientIdentityResolver.getClassForProtocol("role")).thenReturn((Class)Role.class);
        when(permissionRecipientIdentityResolver.toIdentity(userUri)).thenReturn(userIdentity);
        when(permissionRecipientIdentityResolver.toIdentity(roleUri)).thenReturn(roleIdentity);

        when(converter.toClient(serverPermission, null)).thenReturn(clientPermission);
        when(converter.toServer(ctx, clientPermission, null)).thenReturn(serverPermission);

        mockPermissons.add(createObjectPermissionForRole(1, "repo:/", "ROLE_SUPERUSER", null));
        mockPermissons.add(createObjectPermissionForRole(0, "repo:/", "ROLE_ANONYMOUS", null));
        mockPermissons.add(createObjectPermissionForRole(2, "repo:/", "ROLE_USER", null));
        mockPermissons.add(createObjectPermissionForRole(8, "repo:/public", "ROLE_USER", null));
        mockPermissons.add(createObjectPermissionForRole(2, "repo:/public", "ROLE_ANONYMOUS", null));

        mockPermissons.add(createObjectPermissionForRole(1, "repo:/", "Darth Tirannus", null));
        mockPermissons.add(createObjectPermissionForRole(0, "repo:/", "Darth Vader", null));
        mockPermissons.add(createObjectPermissionForRole(2, "repo:/", "Darth Revan", null));
        mockPermissons.add(createObjectPermissionForRole(2, "repo:/public", "Darth Malak", null));
        mockPermissons.add(createObjectPermissionForRole(0, "repo:/public", "Darth Sidius", null));
     }

    @BeforeMethod
    public void init() throws Exception {
        when(permissionsService.getPermissions(anyString(), (Class)eq(null), (String)eq(null), eq(false), eq(false))).thenReturn(new ArrayList<ObjectPermission>(mockPermissons));
        when(permissionsService.getPermissions(anyString(), eq(Role.class), (String)eq(null), eq(false), eq(false))).thenReturn(mockPermissons.subList(0, 5));
        when(permissionsService.getPermissions(anyString(), eq(User.class), (String)eq(null), eq(false), eq(false))).thenReturn(mockPermissons.subList(5, 10));
        when(permissionsService.getPermissions(anyString(), eq(Role.class), anyString(), eq(false), eq(false))).thenReturn(mockPermissons.subList(0, 1));
        when(permissionsService.getPermissions(anyString(), eq(User.class), anyString(), eq(false), eq(false))).thenReturn(mockPermissons.subList(5, 6));
    }

    @AfterMethod
    public void cleanUp() throws Exception {
        reset(permissionsService);
    }

    @Test(expectedExceptions = {IllegalParameterValueException.class})
    public void testGetPermissionsEntryPoint_malformed_number() throws Exception {
        UriInfo info = new UriInfoImpl("permissions/public?limit=asdfji");

        Response response = service.getPermissionsEntryPoint(info);
    }

    @Test(groups = {"getPermissions"})
    public void testGetPermissionsEntryPoint_getPermissions() throws Exception {
        UriInfo info = new UriInfoImpl("permissions/public");

        Response response = service.getPermissionsEntryPoint(info);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        RepositoryPermissionListWrapper entity = (RepositoryPermissionListWrapper)response.getEntity();
        assertEquals(entity.getPermissions().size(), mockPermissons.size());

        MultivaluedMap<String, ?> metadata = response.getMetadata();
        assertNotNull(metadata.get(RestConstants.HEADER_START_INDEX));
        assertNotNull(metadata.get(RestConstants.HEADER_RESULT_COUNT));
        assertNotNull(metadata.get(RestConstants.HEADER_TOTAL_COUNT));
    }

    @Test(groups = {"getPermissions"})
    public void testGetPermissionsEntryPoint_getPermissions_basicPagination() throws Exception {
        UriInfo info = new UriInfoImpl("permissions/public");

        Response response = service.getPermissionsEntryPoint(info);

        MultivaluedMap<String, ?> metadata =  response.getMetadata();
        assertEquals((int)((List<Integer>)metadata.get(RestConstants.HEADER_START_INDEX)).get(0), 0);
        assertEquals((int)((List<Integer>)metadata.get(RestConstants.HEADER_RESULT_COUNT)).get(0), mockPermissons.size());
        assertEquals((int)((List<Integer>)metadata.get(RestConstants.HEADER_TOTAL_COUNT)).get(0), mockPermissons.size());
    }

    @Test(groups = {"getPermissions"})
    public void testGetPermissionsEntryPoint_getPermissions_startIndex() throws Exception {
        int startIndex = 5;
        UriInfo info = new UriInfoImpl("permissions/public?offset="+startIndex);

        Response response = service.getPermissionsEntryPoint(info);

        MultivaluedMap<String, ?> metadata =  response.getMetadata();
        assertEquals((int)((List<Integer>)metadata.get(RestConstants.HEADER_START_INDEX)).get(0), startIndex);
        assertEquals((int)((List<Integer>)metadata.get(RestConstants.HEADER_RESULT_COUNT)).get(0), mockPermissons.size() - startIndex);
        assertEquals((int)((List<Integer>)metadata.get(RestConstants.HEADER_TOTAL_COUNT)).get(0), mockPermissons.size());
    }

    @Test(groups = {"getPermissions"})
    public void testGetPermissionsEntryPoint_getPermissions_startIndex_exclusive() throws Exception {
        int startIndex = mockPermissons.size();
        UriInfo info = new UriInfoImpl("permissions/public?offset="+startIndex);

        Response response = service.getPermissionsEntryPoint(info);

        assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());

        MultivaluedMap<String, ?> metadata =  response.getMetadata();
        assertEquals((int)((List<Integer>)metadata.get(RestConstants.HEADER_START_INDEX)).get(0), startIndex);
        assertEquals((int)((List<Integer>)metadata.get(RestConstants.HEADER_RESULT_COUNT)).get(0), mockPermissons.size() - startIndex);
        assertEquals((int)((List<Integer>)metadata.get(RestConstants.HEADER_TOTAL_COUNT)).get(0), mockPermissons.size());
    }

    @Test(groups = {"getPermissions"})
    public void testGetPermissionsEntryPoint_getPermissions_startIndex_big() throws Exception {
        int startIndex = 50;
        UriInfo info = new UriInfoImpl("permissions/public?offset="+startIndex);

        Response response = service.getPermissionsEntryPoint(info);

        assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());

        MultivaluedMap<String, ?> metadata =  response.getMetadata();
        assertEquals((int)((List<Integer>)metadata.get(RestConstants.HEADER_START_INDEX)).get(0), startIndex);
        assertEquals((int)((List<Integer>)metadata.get(RestConstants.HEADER_RESULT_COUNT)).get(0), 0);
        assertEquals((int)((List<Integer>)metadata.get(RestConstants.HEADER_TOTAL_COUNT)).get(0), mockPermissons.size());
    }

    @Test(groups = {"getPermissions"})
    public void testGetPermissionsEntryPoint_getPermissions_limit() throws Exception {
        int limit = 5;
        UriInfo info = new UriInfoImpl("permissions/public?limit="+limit);

        Response response = service.getPermissionsEntryPoint(info);

        RepositoryPermissionListWrapper entity = (RepositoryPermissionListWrapper)response.getEntity();
        assertEquals(entity.getPermissions().size(), limit);

        MultivaluedMap<String, ?> metadata =  response.getMetadata();
        assertEquals((int)((List<Integer>)metadata.get(RestConstants.HEADER_START_INDEX)).get(0), 0);
        assertEquals((int)((List<Integer>)metadata.get(RestConstants.HEADER_RESULT_COUNT)).get(0), limit);
        assertEquals((int)((List<Integer>)metadata.get(RestConstants.HEADER_TOTAL_COUNT)).get(0), mockPermissons.size());
    }

    @Test(groups = {"getPermissions"})
    public void testGetPermissionsEntryPoint_getPermissions_startIndex_limit() throws Exception {
        int limit = 2;
        int startIndex = 5;
        UriInfo info = new UriInfoImpl("permissions/public?limit="+limit+"&offset="+startIndex);

        Response response = service.getPermissionsEntryPoint(info);

        RepositoryPermissionListWrapper entity = (RepositoryPermissionListWrapper)response.getEntity();
        assertEquals(entity.getPermissions().size(), limit);

        MultivaluedMap<String, ?> metadata =  response.getMetadata();
        assertEquals((int)((List<Integer>)metadata.get(RestConstants.HEADER_START_INDEX)).get(0), startIndex);
        assertEquals((int)((List<Integer>)metadata.get(RestConstants.HEADER_RESULT_COUNT)).get(0), limit);
        assertEquals((int)((List<Integer>)metadata.get(RestConstants.HEADER_TOTAL_COUNT)).get(0), mockPermissons.size());
    }

    @Test(groups = {"getPermissions"})
    public void testGetPermissionsEntryPoint_getPermissions_startIndex_limit_less() throws Exception {
        int limit = 2;
        int startIndex = 5;
        UriInfo info = new UriInfoImpl("permissions/public?limit="+limit+"&offset="+startIndex);

        Response response = service.getPermissionsEntryPoint(info);

        RepositoryPermissionListWrapper entity = (RepositoryPermissionListWrapper)response.getEntity();
        assertEquals(entity.getPermissions().size(), limit);

        MultivaluedMap<String, ?> metadata =  response.getMetadata();
        assertEquals((int)((List<Integer>)metadata.get(RestConstants.HEADER_START_INDEX)).get(0), startIndex);
        assertEquals((int)((List<Integer>)metadata.get(RestConstants.HEADER_RESULT_COUNT)).get(0), limit);
        assertEquals((int)((List<Integer>)metadata.get(RestConstants.HEADER_TOTAL_COUNT)).get(0), mockPermissons.size());
    }

    @Test(groups = {"getPermissions"})
    public void testGetPermissionsEntryPoint_getPermissions_startIndex_limit_bigger() throws Exception {
        int limit = 12;
        int startIndex = 5;
        UriInfo info = new UriInfoImpl("permissions/public?limit="+limit+"&offset="+startIndex);

        Response response = service.getPermissionsEntryPoint(info);

        RepositoryPermissionListWrapper entity = (RepositoryPermissionListWrapper)response.getEntity();
        assertEquals(entity.getPermissions().size(), mockPermissons.size() - startIndex);

        MultivaluedMap<String, ?> metadata =  response.getMetadata();
        assertEquals((int)((List<Integer>)metadata.get(RestConstants.HEADER_START_INDEX)).get(0), startIndex);
        assertEquals((int)((List<Integer>)metadata.get(RestConstants.HEADER_RESULT_COUNT)).get(0), mockPermissons.size() - startIndex);
        assertEquals((int)((List<Integer>)metadata.get(RestConstants.HEADER_TOTAL_COUNT)).get(0), mockPermissons.size());
    }

    @Test(dependsOnGroups = {"getPermissions"})
    public void testGetPermissionsEntryPoint_getPermissions_wo_slash() throws Exception {
        UriInfo info = new UriInfoImpl("permissions/public");

        Response response = service.getPermissionsEntryPoint(info);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        RepositoryPermissionListWrapper entity = (RepositoryPermissionListWrapper)response.getEntity();
        assertEquals(entity.getPermissions().size(), mockPermissons.size());

        MultivaluedMap<String, ?> metadata = response.getMetadata();
        assertNotNull(metadata.get(RestConstants.HEADER_START_INDEX));
        assertNotNull(metadata.get(RestConstants.HEADER_RESULT_COUNT));
        assertNotNull(metadata.get(RestConstants.HEADER_TOTAL_COUNT));
    }

    @Test(dependsOnGroups = {"getPermissions"})
    public void testGetPermissionsEntryPoint_getPermissions_w_slash() throws Exception {
        UriInfo info = new UriInfoImpl("permissions/public/");

        Response response = service.getPermissionsEntryPoint(info);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        RepositoryPermissionListWrapper entity = (RepositoryPermissionListWrapper)response.getEntity();
        assertEquals(entity.getPermissions().size(), mockPermissons.size());

        MultivaluedMap<String, ?> metadata = response.getMetadata();
        assertNotNull(metadata.get(RestConstants.HEADER_START_INDEX));
        assertNotNull(metadata.get(RestConstants.HEADER_RESULT_COUNT));
        assertNotNull(metadata.get(RestConstants.HEADER_TOTAL_COUNT));
    }

    @Test(groups = {"getPermission"})
    public void testGetPermissionsEntryPoint_getPermission() throws Exception {
        when(permissionsService.getPermission("/public", userIdentity.getRecipientClass(), userIdentity.getId())).thenReturn(serverPermission);

        UriInfo info = new UriInfoImpl("permissions/public;recipient=user:/me");

        Response response = service.getPermissionsEntryPoint(info);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        RepositoryPermission entity = (RepositoryPermission)response.getEntity();
        assertEquals(entity, clientPermission);
    }

    @Test(groups = {"getPermission"}, expectedExceptions = ResourceNotFoundException.class)
    public void testGetPermissionsEntryPoint_getPermission_notFound() throws Exception {
        when(permissionsService.getPermission("/public", userIdentity.getRecipientClass(), userIdentity.getId())).thenReturn(serverPermission);

        UriInfo info = new UriInfoImpl("permissions/pubaflic;recipient=user:/me");

        Response response = service.getPermissionsEntryPoint(info);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        RepositoryPermission entity = (RepositoryPermission)response.getEntity();
        assertEquals(entity, clientPermission);
    }

    @Test(dependsOnGroups = {"getPermission"})
    public void testGetPermissionsEntryPointRoot_getPermission_wo_slash() throws Exception{
        when(permissionsService.getPermission("/", userIdentity.getRecipientClass(), userIdentity.getId())).thenReturn(serverPermission);

        UriInfo info = new UriInfoImpl("permissions;recipient=user:/me");

        Response response = service.getPermissionsEntryPointRoot(info);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        RepositoryPermission entity = (RepositoryPermission)response.getEntity();
        assertEquals(entity, clientPermission);
    }

    @Test(dependsOnGroups = {"getPermission"})
    public void testGetPermissionsEntryPointRoot_getPermission_w_slash() throws Exception{
        when(permissionsService.getPermission("/", userIdentity.getRecipientClass(), userIdentity.getId())).thenReturn(serverPermission);

        UriInfo info = new UriInfoImpl("permissions/;recipient=user:/me");

        Response response = service.getPermissionsEntryPointRoot(info);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        RepositoryPermission entity = (RepositoryPermission)response.getEntity();
        assertEquals(entity, clientPermission);
    }

    @Test(groups = {"createPermissions"})
    public void testCreatePermissions() throws Exception {
        List<RepositoryPermission> data = new ArrayList<RepositoryPermission>();
        data.add(clientPermission);
        RepositoryPermissionListWrapper input = new RepositoryPermissionListWrapper(data);
        String uri = Folder.SEPARATOR;
        String jerseyUri = "";

        Response response = service.createPermissions(input);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());

        RepositoryPermissionListWrapper entity = (RepositoryPermissionListWrapper)response.getEntity();
        assertEquals(entity.getPermissions().size(), input.getPermissions().size());
        assertEquals(entity.getPermissions().get(0), clientPermission);

        verify(permissionsService).createPermissions(any(List.class));
    }

    @Test(groups = {"createPermission"})
    public void testCreatePermission_create() throws Exception {
        when(permissionsService.getPermission("/public", userIdentity.getRecipientClass(), userIdentity.getId())).thenReturn(null);

        UriInfo info = new UriInfoImpl("permissions/public;recipient=user:/me");

        Response response = service.createPermission(clientPermission);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());

        RepositoryPermission entity = (RepositoryPermission)response.getEntity();
        assertEquals(entity, clientPermission);

        verify(permissionsService).createPermission(serverPermission);
    }

    @Test(groups = {"deletePermissions"}, dependsOnGroups = {"getPermissions"})
    public void testDeletePermissionsEntryPoint_deletePermissions() throws Exception {
        UriInfo info = new UriInfoImpl("permissions/public");

        Response response = service.deletePermissionsEntryPoint(info);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());

        for ( ObjectPermission permission : mockPermissons){
            verify(permissionsService).deletePermission(permission);
        }
    }

    @Test(groups = {"deletePermissions"}, dependsOnGroups = {"getPermissions"}, expectedExceptions = {ResourceNotFoundException.class})
    public void testDeletePermissionsEntryPoint_deletePermissions_no_resource() throws Exception {
        when(permissionsService.getPermissions(anyString(), (Class)eq(null), (String)eq(null), eq(false), eq(false))).thenThrow(ResourceNotFoundException.class);
        UriInfo info = new UriInfoImpl("permissions/puasf");

        Response response = service.deletePermissionsEntryPoint(info);
    }

    @Test(dependsOnGroups = {"deletePermissions"})
    public void testDeletePermissionsEntryPointRoot_deletePermissionsRoot_wo_slash() throws Exception {
        UriInfo info = new UriInfoImpl("permissions");

        Response response = service.deletePermissionsEntryPointRoot(info);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());

        for ( ObjectPermission permission : mockPermissons){
            verify(permissionsService).deletePermission(permission);
        }
    }

    @Test(dependsOnGroups = {"deletePermissions"})
    public void testDeletePermissionsEntryPointRoot_deletePermissionsRoot_w_slash() throws Exception {
        UriInfo info = new UriInfoImpl("permissions/");

        Response response = service.deletePermissionsEntryPointRoot(info);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());

        for ( ObjectPermission permission : mockPermissons){
            verify(permissionsService).deletePermission(permission);
        }
    }

    @Test(groups = {"deletePermission"})
    public void testDeletePermissionEntryPoint_deletePermission() throws Exception {
        when(permissionsService.getPermission("/public", userIdentity.getRecipientClass(), userIdentity.getId())).thenReturn(serverPermission);

        UriInfo info = new UriInfoImpl("permissions/public;recipient=user:/me");

        Response response = service.deletePermissionsEntryPoint(info);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());

        verify(permissionsService).deletePermission(serverPermission);
    }

    @Test(groups = {"deletePermission"}, expectedExceptions = {ResourceNotFoundException.class})
    public void testDeletePermissionEntryPoint_deletePermission_notFound() throws Exception {
        when(permissionsService.getPermission("/public", userIdentity.getRecipientClass(), userIdentity.getId())).thenReturn(null);

        UriInfo info = new UriInfoImpl("permissions/public;recipient=user:/me");

        Response response = service.deletePermissionsEntryPoint(info);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());

        verify(permissionsService).deletePermission(serverPermission);
    }

    @Test(groups = {"deletePermission"})
    public void testDeletePermissionsEntryPoint_deletePermission() throws Exception {
        when(permissionsService.getPermission("/public", userIdentity.getRecipientClass(), userIdentity.getId())).thenReturn(serverPermission);

        UriInfo info = new UriInfoImpl("permissions/public;recipient=user:/me");

        Response response = service.deletePermissionsEntryPoint(info);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());

        verify(permissionsService).deletePermission(serverPermission);
    }

    @Test(groups = {"deletePermission"}, expectedExceptions = {ResourceNotFoundException.class})
    public void testDeletePermissionsEntryPoint_deletePermission_notFound() throws Exception {
        when(permissionsService.getPermission("/public", userIdentity.getRecipientClass(), userIdentity.getId())).thenReturn(null);

        UriInfo info = new UriInfoImpl("permissions/public;recipient=user:/me");

        Response response = service.deletePermissionsEntryPoint(info);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());

        verify(permissionsService).deletePermission(serverPermission);
    }

    @Test(dependsOnGroups = {"deletePermission"})
    public void testDeletePermissionsEntryPointRoot_deletePermission_wo_slash() throws Exception {
        when(permissionsService.getPermission("/", userIdentity.getRecipientClass(), userIdentity.getId())).thenReturn(serverPermission);

        UriInfo info = new UriInfoImpl("permissions;recipient=user:/me");

        Response response = service.deletePermissionsEntryPointRoot(info);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());

        verify(permissionsService).deletePermission(serverPermission);
    }

    @Test(dependsOnGroups = {"deletePermission"})
    public void testDeletePermissionsEntryPointRoot_deletePermission_w_slash() throws Exception {
        when(permissionsService.getPermission("/", userIdentity.getRecipientClass(), userIdentity.getId())).thenReturn(serverPermission);

        UriInfo info = new UriInfoImpl("permissions/;recipient=user:/me");

        Response response = service.deletePermissionsEntryPointRoot(info);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());

        verify(permissionsService).deletePermission(serverPermission);
    }

    @Test(groups = {"updatePermissions"})
    public void testUpdatePermissions() throws Exception {
        List<RepositoryPermission> data = new ArrayList<RepositoryPermission>();
        data.add(clientPermission);
        RepositoryPermissionListWrapper input = new RepositoryPermissionListWrapper(data);
        InternalURI uri = new InternalURIDefinition("/public", PermissionUriProtocol.RESOURCE);

        String jerseyUri = "public";

        Response response = service.updatePermissions(input,jerseyUri);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        RepositoryPermissionListWrapper entity = (RepositoryPermissionListWrapper)response.getEntity();
        assertEquals(entity.getPermissions().size(), input.getPermissions().size());
        assertEquals(entity.getPermissions().get(0), clientPermission);

        verify(permissionsService).putPermissions(eq(uri), any(List.class));
    }

    @Test(dependsOnGroups = {"updatePermissions"})
    public void testUpdatePermissionsRoot() throws Exception {
        List<RepositoryPermission> data = new ArrayList<RepositoryPermission>();
        data.add(clientPermission);
        RepositoryPermissionListWrapper input = new RepositoryPermissionListWrapper(data);
        InternalURI uri = new InternalURIDefinition(Folder.SEPARATOR, PermissionUriProtocol.RESOURCE);

        Response response = service.updatePermissionsRoot(input);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        RepositoryPermissionListWrapper entity = (RepositoryPermissionListWrapper)response.getEntity();
        assertEquals(entity.getPermissions().size(), input.getPermissions().size());
        assertEquals(entity.getPermissions().get(0), clientPermission);

        verify(permissionsService).putPermissions(eq(uri), any(List.class));
    }

    @Test(groups = {"updatePermission"})
    public void testUpdatePermission() throws Exception {
        when(permissionsService.getPermission("/public", userIdentity.getRecipientClass(), userIdentity.getId())).thenReturn(serverPermission);

        UriInfo info = new UriInfoImpl("permissions/public;recipient=user:/me");

        Response response = service.updatePermission(clientPermission, info);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        RepositoryPermission entity = (RepositoryPermission)response.getEntity();
        assertEquals(entity, clientPermission);

        verify(permissionsService).putPermission(serverPermission);
    }

    @Test(groups = {"updatePermission"})
    public void testUpdatePermission_create() throws Exception {
        when(permissionsService.getPermission("/public", userIdentity.getRecipientClass(), userIdentity.getId())).thenReturn(null);

        UriInfo info = new UriInfoImpl("permissions/public;recipient=user:/me");

        Response response = service.updatePermission(clientPermission, info);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        RepositoryPermission entity = (RepositoryPermission)response.getEntity();
        assertEquals(entity, clientPermission);

        verify(permissionsService).putPermission(serverPermission);
    }

    @Test(dependsOnGroups = {"updatePermission"})
    public void testUpdatePermissionRoot_w_slash() throws Exception {
        when(permissionsService.getPermission("/", userIdentity.getRecipientClass(), userIdentity.getId())).thenReturn(serverPermission);

        UriInfo info = new UriInfoImpl("permissions/;recipient=user:/me");

        Response response = service.updatePermissionRoot(clientPermission, info);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        RepositoryPermission entity = (RepositoryPermission)response.getEntity();
        assertEquals(entity, clientPermission);

        verify(permissionsService).putPermission(serverPermission);
    }

    @Test(dependsOnGroups = {"updatePermission"})
    public void testUpdatePermissionRoot_wo_slash() throws Exception {
        when(permissionsService.getPermission("/", userIdentity.getRecipientClass(), userIdentity.getId())).thenReturn(serverPermission);

        UriInfo info = new UriInfoImpl("permissions;recipient=user:/me");

        Response response = service.updatePermissionRoot(clientPermission, info);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        RepositoryPermission entity = (RepositoryPermission)response.getEntity();
        assertEquals(entity, clientPermission);

        verify(permissionsService).putPermission(serverPermission);
    }

    private ObjectPermission createObjectPermissionForUser(int mask, String uri, String name, String tenantId){
        User user = new UserImpl();
        user.setUsername(name);
        user.setTenantId(tenantId);
        return createObjectPermission(mask, uri, user);
    }

    private ObjectPermission createObjectPermissionForRole(int mask, String uri, String name, String tenantId){
        Role role = new RoleImpl();
        role.setRoleName(name);
        role.setTenantId(tenantId);
        return createObjectPermission(mask, uri, role);
    }

    private ObjectPermission createObjectPermission(int mask, String uri, Object recipient){
        ObjectPermission permission = new ObjectPermissionImpl();
        permission.setPermissionMask(mask);
        permission.setURI(uri);
        permission.setPermissionRecipient(recipient);
        return permission;
    }

    private class PathSegmentImpl implements PathSegment {

        private String path;
        private MultivaluedMap<String, String> map;

        private PathSegmentImpl(String path, MultivaluedMap<String, String> map) {
            this.path = path;
            this.map = map;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public MultivaluedMap<String, String> getMatrixParameters() {
            return map == null ? new MultivaluedHashMap<String, String>() : map;
        }
    }

    private class UriInfoImpl implements UriInfo {

        private String path;

        private UriInfoImpl(String path) {
            this.path = path;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public String getPath(boolean decode) {
            return path;
        }

        @Override
        public List<PathSegment> getPathSegments() {
            List<PathSegment> result = new LinkedList<PathSegment>();

            String pathWithoutRecipient = path.split("\\?")[0].split(";")[0];
            String[] segments = pathWithoutRecipient.split("/");
            for (int i = 0; i< segments.length - 1; i++){
                result.add(new PathSegmentImpl(segments[i], null));
            }

            String[] withRecipient = path.split("\\?")[0].split(";");
            MultivaluedMap<String, String> multivaluedMap = null;
            if (withRecipient.length > 1){
                String[] recipient = withRecipient[1].split("=");
                multivaluedMap = new MultivaluedHashMap<String, String>();
                multivaluedMap.add(recipient[0], recipient[1]);
            }
            result.add(new PathSegmentImpl(segments[segments.length - 1], multivaluedMap));

            return result;
        }

        @Override
        public List<PathSegment> getPathSegments(boolean decode) {
            return getPathSegments();
        }

        @Override
        public URI getRequestUri() {
            return null;
        }

        @Override
        public UriBuilder getRequestUriBuilder() {
            return null;
        }

        @Override
        public URI getAbsolutePath() {
            return null;
        }

        @Override
        public UriBuilder getAbsolutePathBuilder() {
            return null;
        }

        @Override
        public URI getBaseUri() {
            return null;
        }

        @Override
        public UriBuilder getBaseUriBuilder() {
            return null;
        }

        @Override
        public MultivaluedMap<String, String> getPathParameters() {
            return null;
        }

        @Override
        public MultivaluedMap<String, String> getPathParameters(boolean decode) {
            return null;
        }

        @Override
        public MultivaluedMap<String, String> getQueryParameters() {
            MultivaluedMap<String, String> map  = null;
            String[] withParameters = path.split("\\?");
            if (withParameters.length > 1){
                String[] parameters = withParameters[1].split("&");
                map = new MultivaluedHashMap<String, String>();
                for (String parameter : parameters){
                    String[] pars = parameter.split("=");
                    map.put(pars[0], Arrays.asList(pars[1]));
                }
            }
            return map;
        }

        @Override
        public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
            return getQueryParameters();
        }

        @Override
        public List<String> getMatchedURIs() {
            return null;
        }

        @Override
        public List<String> getMatchedURIs(boolean decode) {
            return null;
        }

        @Override
        public List<Object> getMatchedResources() {
            return null;
        }

        @Override
        public URI resolve(URI uri) {
            return uri;
        }

        @Override
        public URI relativize(URI uri) {
            return uri;
        }

    }
}
