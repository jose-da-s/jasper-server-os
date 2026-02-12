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
package com.jaspersoft.jasperserver.api.security;

import com.jaspersoft.jasperserver.api.metadata.common.service.ResourceFactory;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.cache.Cache;

import java.io.Serializable;

/**
 * System logged in user storage, that stores temporary user credentials to allow internal system do
 * successful authorization by userName and password. Originally it was created to allow PhantomJS driver, that is used
 * for dashboard job output generation, connect to the server with external userName and random generated password.
 *
 * @author Volodya Sabadosh
 * @version $Id$
 */
public class SystemLoggedInUserStorage {
    private ResourceFactory objectFactory;
    private Cache cache;
    private final String allowPasswordCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`!@#$%^&*()-_=+[{]}\\|;:\'\",<.>/?";

    /**
     * Stores user in temporary loggedIn user storage, to force security filters allow this credentials.
     *
     * @param userName user name. In tenant environment has pattern {userName}|{tenantId}.
     * @param password user password.
     *
     * @return user
     */
    public User storeUser(String userName, String password) {
        QueryCompositeKey key = new QueryCompositeKey(userName, password);

        User user = (User)objectFactory.newObject(User.class);
        user.setUsername(userName);
        user.setPassword(password);

        if (cache != null) {
            cache.put(key, user);
        }

        return user;
    }

    public User storeUserWithRandomPassword(String userName) {
        return storeUser(userName, RandomStringUtils.random(15, allowPasswordCharacters));
    }

    public User loadUserByNameAndPassword(String userName, String password) {
        QueryCompositeKey key = new QueryCompositeKey(userName, password);
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(key);
            if (wrapper != null) {
                return (User) wrapper.get();
            }
        }
        return null;
    }

    public void removeUser(String userName, String password) {
        QueryCompositeKey key = new QueryCompositeKey(userName, password);
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(key);
            if (wrapper != null) {
                cache.evict(key);
            }
        }
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public void setObjectFactory(ResourceFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    private class QueryCompositeKey implements Serializable {
        private String userName;
        private String password;

        public QueryCompositeKey(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            QueryCompositeKey that = (QueryCompositeKey) o;

            if (password != null ? !password.equals(that.password) : that.password != null) return false;
            if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = userName != null ? userName.hashCode() : 0;
            result = 31 * result + (password != null ? password.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "QueryCompositeKey{" +
                    "userName='" + userName + '\'' +
                    ", password='" + password + '\'' +
                    '}';
        }
    }

}
