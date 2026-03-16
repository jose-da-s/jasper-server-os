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

import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.remote.exception.AccessDeniedException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import javax.cache.Cache;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.UUID;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@Component
@Aspect
public class ContextsStorageSecurityAspect {
    @Resource(name = "contextsCache")
    private Cache<UUID, Object> cache;
    @Around("execution(* ContextDataStorage.save(com.jaspersoft.jasperserver.remote.connection.storage.ContextDataPair))")
    public Object saveOwnedContext(ProceedingJoinPoint joinPoint) throws Throwable {
        final Object[] args = joinPoint.getArgs();
        args[0] = getOwnedDataPair((ContextDataPair) args[0]);
        return joinPoint.proceed(args);
    }
    @Around("execution(* ContextDataStorage.update(java.util.UUID, com.jaspersoft.jasperserver.remote.connection.storage.ContextDataPair))")
    public void updateOwnedContext(ProceedingJoinPoint joinPoint) throws Throwable {
        final Object[] args = joinPoint.getArgs();
        args[1] = getOwnedDataPair((ContextDataPair) args[1]);
        joinPoint.proceed(args);
    }
    @Before("execution(* ContextDataStorage.update(java.util.UUID, com.jaspersoft.jasperserver.remote.connection.storage.ContextDataPair)) ||" +
            "execution(* ContextDataStorage.get(java.util.UUID)) ||" +
            "execution(* ContextDataStorage.delete(java.util.UUID))"
    )
    public void checkOwner(JoinPoint joinPoint){
        final UUID uuid = (UUID) joinPoint.getArgs()[0];
        final Object pair = getElement(uuid);
        if(pair != null){
            if(pair != null){
                if(!(pair instanceof OwnedContextDataPair &&
                        getCurrentUserQualifiedName().equals(((OwnedContextDataPair) pair).getOwner()))){
                    // throw exception if pair isn't null and it has another owner, than current user or doesn't
                    // have owner at all
                    throw new AccessDeniedException(uuid.toString());
                }
            }
        }
    }

    /**
     * This method is created to avoid mocking of cache (which is mostly final)
     * @param uuid the context UUID
     * @return element with context or null if doesn't exist
     */
    protected Object getElement(UUID uuid){
        return cache.get(uuid);
    }

    protected OwnedContextDataPair getOwnedDataPair(ContextDataPair source){
        final String userQualifiedName = getCurrentUserQualifiedName();
        return (OwnedContextDataPair) new OwnedContextDataPair(source.getContext(),
                source.getData(), userQualifiedName).setExternalContextClass(source.getExternalContextClass());
    }

    protected String getCurrentUserQualifiedName(){
        final User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        final String tenantId = user.getTenantId();
        return user.getUsername() + (tenantId != null ? "|" + tenantId : "");
    }

    protected static class OwnedContextDataPair extends ContextDataPair {
        private final String owner;

        public OwnedContextDataPair(Object connection, Map<String, Object> data, String owner) {
            super(connection, data);
            this.owner = owner;
        }

        public String getOwner() {
            return owner;
        }
    }

}
