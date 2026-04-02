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
package com.jaspersoft.jasperserver.dto.connection;

import com.jaspersoft.jasperserver.dto.common.ResourceLocation;

import jakarta.xml.bind.annotation.XmlRootElement;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@XmlRootElement(name = "lfs")
public class LfsConnection implements ResourceLocation {
    private String path;

    public LfsConnection(){
    }

    public LfsConnection(LfsConnection source){
        checkNotNull(source);

        path = source.getPath();
    }

    public String getPath() {
        return path;
    }

    public LfsConnection setPath(String path) {
        this.path = path;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LfsConnection that = (LfsConnection) o;

        if (path != null ? !path.equals(that.path) : that.path != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "LfsConnection{" +
                "path='" + path + '\'' +
                "} " + super.toString();
    }

    /*
     * DeepCloneable
     */

    @Override
    public LfsConnection deepClone() {
        return new LfsConnection(this);
    }
}
