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
package com.jaspersoft.jasperserver.remote.connection.storage;

import com.jaspersoft.jasperserver.api.metadata.user.domain.client.UserImpl;
import com.jaspersoft.jasperserver.remote.exception.AccessDeniedException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.UUID;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
public class ContextsStorageSecurityAspectTest {
    private ContextsStorageSecurityAspect aspect = mock(ContextsStorageSecurityAspect.class);

    @BeforeMethod
    public void refresh(){
        Mockito.reset(aspect);
    }

    @Test
    public void saveOwnedContext_callSequence_success() throws Throwable {
        final ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        final ContextDataPair initialPair = new ContextDataPair(null, null);
        final Object[] args = {initialPair};
        when(joinPoint.getArgs()).thenReturn(args);
        final ContextsStorageSecurityAspect.OwnedContextDataPair ownedPair =
                new ContextsStorageSecurityAspect.OwnedContextDataPair(null, null, null);
        when(aspect.getOwnedDataPair(initialPair)).thenReturn(ownedPair);
        final Object expectedResult = new Object();
        when(joinPoint.proceed(args)).thenReturn(expectedResult);
        when(aspect.saveOwnedContext(joinPoint)).thenCallRealMethod();

        final Object result = aspect.saveOwnedContext(joinPoint);

        assertSame(result, expectedResult);
        assertSame(args[0], ownedPair);
    }

    @Test
    public void updateOwnedContext_callSequence_success() throws Throwable {
        final ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        final ContextDataPair initialPair = new ContextDataPair(null, null);
        final Object[] args = {null, initialPair};
        when(joinPoint.getArgs()).thenReturn(args);
        final ContextsStorageSecurityAspect.OwnedContextDataPair ownedPair =
                new ContextsStorageSecurityAspect.OwnedContextDataPair(null, null, null);
        when(aspect.getOwnedDataPair(initialPair)).thenReturn(ownedPair);
        doCallRealMethod().when(aspect).updateOwnedContext(joinPoint);

        aspect.updateOwnedContext(joinPoint);

        final ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(joinPoint).proceed(argsCaptor.capture());
        final Object[] effectiveArgs = argsCaptor.getValue();
        assertNotNull(effectiveArgs);
        assertSame(effectiveArgs[1], ownedPair);
    }

    @Test
    public void checkOwner_correctUser_accessGrantedNoException(){
        final JoinPoint joinPoint = mock(JoinPoint.class);
        final UUID uuid = UUID.randomUUID();
        when(joinPoint.getArgs()).thenReturn(new Object[]{uuid});
        final String expectedOwner = "someOwnerUser";
        when(aspect.getCurrentUserQualifiedName()).thenReturn(expectedOwner);
        when(aspect.getElement(uuid)).thenReturn(new ContextsStorageSecurityAspect.OwnedContextDataPair(new Object(), new HashMap(), expectedOwner));
        doCallRealMethod().when(aspect).checkOwner(joinPoint);
        Exception exception = null;

        try {
            aspect.checkOwner(joinPoint);
        } catch (Exception e){
            exception = e;
        }

        assertNull(exception);
    }

    @Test(expectedExceptions = AccessDeniedException.class)
    public void checkOwner_incorrectUser_accessDeniedException(){
        final JoinPoint joinPoint = mock(JoinPoint.class);
        final UUID uuid = UUID.randomUUID();
        when(joinPoint.getArgs()).thenReturn(new Object[]{uuid});
        when(aspect.getCurrentUserQualifiedName()).thenReturn("someAnotherUser");
        when(aspect.getElement(uuid)).thenReturn(new ContextsStorageSecurityAspect.OwnedContextDataPair(new Object(), new HashMap(), "someOwnerUser"));
        doCallRealMethod().when(aspect).checkOwner(joinPoint);

        aspect.checkOwner(joinPoint);
    }

    @Test
    public void checkOwner_noSuchEntry_noException(){
        final JoinPoint joinPoint = mock(JoinPoint.class);
        final UUID uuid = UUID.randomUUID();
        when(joinPoint.getArgs()).thenReturn(new Object[]{uuid});

        doCallRealMethod().when(aspect).checkOwner(joinPoint);
        Exception exception = null;

        try {
            aspect.checkOwner(joinPoint);
        } catch (Exception e){
            exception = e;
        }

        assertNull(exception);
    }

    @Test
    public void getOwnedDataPair(){
        final Object connection = new Object();
        final HashMap<String, Object> data = new HashMap<String, Object>();
        final ContextDataPair pair = new ContextDataPair(connection, data);
        when(aspect.getCurrentUserQualifiedName()).thenReturn("someOwnerUser");
        when(aspect.getOwnedDataPair(pair)).thenCallRealMethod();

        final ContextsStorageSecurityAspect.OwnedContextDataPair result = aspect.getOwnedDataPair(pair);

        assertNotNull(result);
        assertSame(result.getContext(), connection);
        assertSame(result.getData(), data);
        assertEquals(result.getOwner(), "someOwnerUser");
    }

    @Test
    public void getCurrentUserQualifiedName_withTenant_usernamePipeTenantId(){
        final SecurityContext initialContext = SecurityContextHolder.getContext();
        final SecurityContextImpl actualContext = new SecurityContextImpl();
        final Authentication authenticationMock = mock(Authentication.class);
        final UserImpl currentUser = new UserImpl();
        currentUser.setUsername("someUser");
        currentUser.setTenantId("someTenant");
        when(authenticationMock.getPrincipal()).thenReturn(currentUser);
        actualContext.setAuthentication(authenticationMock);
        SecurityContextHolder.setContext(actualContext);
        when(aspect.getCurrentUserQualifiedName()).thenCallRealMethod();

        final String result = aspect.getCurrentUserQualifiedName();
        assertEquals(result, "someUser|someTenant");
        SecurityContextHolder.setContext(initialContext);
    }

    @Test
    public void getCurrentUserQualifiedName_rootOrgUser_usernameOnly(){
        final SecurityContext initialContext = SecurityContextHolder.getContext();
        final SecurityContextImpl actualContext = new SecurityContextImpl();
        final Authentication authenticationMock = mock(Authentication.class);
        final UserImpl currentUser = new UserImpl();
        currentUser.setUsername("someRootOrgUser");
        when(authenticationMock.getPrincipal()).thenReturn(currentUser);
        actualContext.setAuthentication(authenticationMock);
        SecurityContextHolder.setContext(actualContext);
        when(aspect.getCurrentUserQualifiedName()).thenCallRealMethod();

        final String result = aspect.getCurrentUserQualifiedName();
        assertEquals(result, "someRootOrgUser");
        SecurityContextHolder.setContext(initialContext);
    }
}
