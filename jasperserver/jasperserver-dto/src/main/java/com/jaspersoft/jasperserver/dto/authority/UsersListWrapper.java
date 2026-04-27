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
package com.jaspersoft.jasperserver.dto.authority;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author: Zakhar.Tomchenco
 */

@XmlRootElement(name = "users")
public class UsersListWrapper implements DeepCloneable<UsersListWrapper> {
    private List<ClientUser> userList;

    public UsersListWrapper() {
    }

    public UsersListWrapper(List<ClientUser> users) {
        this.userList = new ArrayList<ClientUser>(users);
    }

    public UsersListWrapper(UsersListWrapper other) {
        checkNotNull(other);

        this.userList = copyOf(other.getUserList());
    }

    @Override
    public UsersListWrapper deepClone() {
        return new UsersListWrapper(this);
    }

    @XmlElement(name = "user")
    public List<ClientUser> getUserList() {
        return userList;
    }

    public UsersListWrapper setUserList(List<ClientUser> users) {
        this.userList = users;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UsersListWrapper that = (UsersListWrapper) o;

        return userList != null ? userList.equals(that.userList) : that.userList == null;
    }

    @Override
    public int hashCode() {
        return userList != null ? userList.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "UsersListWrapper{" +
                "userList=" + userList +
                '}';
    }
}
