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
package com.jaspersoft.jasperserver.dto.resources;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@XmlType(name = "resources")
public class ClientReportUnitResourceListWrapper implements DeepCloneable<ClientReportUnitResourceListWrapper> {
    private List<ClientReportUnitResource> files;

    public ClientReportUnitResourceListWrapper() {
    }

    public ClientReportUnitResourceListWrapper(List<ClientReportUnitResource> files) {
        this.files = files;
    }

    public ClientReportUnitResourceListWrapper(ClientReportUnitResourceListWrapper other) {
        checkNotNull(other);

        files = copyOf(other.getFiles());
    }

    @Override
    public ClientReportUnitResourceListWrapper deepClone() {
        return new ClientReportUnitResourceListWrapper(this);
    }

    @XmlElement(name = "resource")
    public List<ClientReportUnitResource> getFiles() {
        return files;
    }

    public ClientReportUnitResourceListWrapper setFiles(List<ClientReportUnitResource> files) {
        this.files = files;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientReportUnitResourceListWrapper that = (ClientReportUnitResourceListWrapper) o;

        return files != null ? files.equals(that.files) : that.files == null;
    }

    @Override
    public int hashCode() {
        return files != null ? files.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ClientReportUnitResourceListWrapper{" +
                "files=" + files +
                '}';
    }
}
