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
 * @author Zakhar.Tomchenco
 * @version $Id$
 */

@XmlRootElement(name = "organizations")
public class OrganizationsListWrapper implements DeepCloneable<OrganizationsListWrapper> {

    private List<ClientTenant> list;

    public OrganizationsListWrapper() {
    }

    public OrganizationsListWrapper(OrganizationsListWrapper other) {
        checkNotNull(other);

        this.list = copyOf(other.getList());
    }

    public OrganizationsListWrapper(List<ClientTenant> roles) {
        this.list = new ArrayList<ClientTenant>(roles);
    }

    @Override
    public OrganizationsListWrapper deepClone() {
        return new OrganizationsListWrapper(this);
    }

    @XmlElement(name = "organization")
    public List<ClientTenant> getList() {
        return list;
    }

    public OrganizationsListWrapper setList(List<ClientTenant> list) {
        this.list = list;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrganizationsListWrapper that = (OrganizationsListWrapper) o;

        return list != null ? list.equals(that.list) : that.list == null;
    }

    @Override
    public int hashCode() {
        return list != null ? list.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "OrganizationsListWrapper{" +
                "list=" + list +
                '}';
    }
}
