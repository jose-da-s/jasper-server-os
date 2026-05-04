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
package com.jaspersoft.jasperserver.dto.adhoc.query.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import java.io.Serializable;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * Measure sorting used for aggregated datasets.
 * Along with QueryAggregatedField meta-info it stores group sorting path,
 * for sorting measure just in some subgroup
 *
 * @author Andriy Godovanets
 */
public class ClientPathOrder implements ClientOrder, Serializable {
    @NotNull
    private Boolean isAscending;

    @NotNull
    @Size(min = 1)
    private List<String> path;

    public ClientPathOrder() {
        // no op
    }

    public ClientPathOrder(ClientPathOrder source) {
        checkNotNull(source);

        isAscending = source.isAscending();
        path = copyOf(source.getPath());
    }

    @Override
    public ClientPathOrder deepClone() {
        return new ClientPathOrder(this);
    }

    /**
     *
     * @return Path to the member to sort by
     */
    @XmlElementWrapper(name = "path")
    @XmlElement(name = "item")
    public List<String> getPath() {
        return path;
    }

    public ClientPathOrder setPath(List<String> path) {
        this.path = path;
        return this;
    }

    @Override
    public Boolean isAscending() {
        return isAscending;
    }

    public ClientPathOrder setAscending(Boolean ascending) {
        isAscending = ascending;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientPathOrder that = (ClientPathOrder) o;

        if (isAscending != null ? !isAscending.equals(that.isAscending) : that.isAscending != null) return false;
        return path != null ? path.equals(that.path) : that.path == null;
    }

    @Override
    public int hashCode() {
        int result = 31 * (isAscending != null ? isAscending.hashCode() : 0);
        result = 31 * result + (this.path != null ? this.path.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientPathOrder{" +
                "isAscending=" + isAscending() +
                ", path=" + path +
                "}";
    }
}
