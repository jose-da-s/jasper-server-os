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

package com.jaspersoft.jasperserver.dto.adhoc.dataset;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.hashCodeOfListOfArrays;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.isListsOfArraysEquals;

/**
* @author Vasyl Spachynskyi
* @version $Id$
* @since 05.04.2016
*/
@XmlRootElement
public class ClientGroupAxis implements DeepCloneable<ClientGroupAxis>, Serializable {
    private List<String[]> level = new ArrayList<String[]>();

    public ClientGroupAxis() {}

    public ClientGroupAxis(ClientGroupAxis groupAxis) {
        checkNotNull(groupAxis);

        level = copyOf(groupAxis.getLevel());
    }

    @XmlElement(name = "level")
    public List<String[]> getLevel() {
        return level;
    }

    public ClientGroupAxis setLevel(List<String[]> columns) {
        if (columns == null) {
            this.level = new ArrayList<String[]>();
        } else {
            this.level = columns;
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientGroupAxis that = (ClientGroupAxis) o;

        return isListsOfArraysEquals(level, that.level);
    }

    @Override
    public int hashCode() {
        return hashCodeOfListOfArrays(level);
    }

    @Override
    public String toString() {
        return "ClientGroupAxis{" +
                "columns=" + level +
                '}';
    }

    @Override
    public ClientGroupAxis deepClone() {
        return new ClientGroupAxis(this);
    }
}