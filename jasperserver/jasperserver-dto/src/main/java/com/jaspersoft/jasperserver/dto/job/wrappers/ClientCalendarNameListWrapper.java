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
package com.jaspersoft.jasperserver.dto.job.wrappers;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 * @version $Id$
 */
@XmlRootElement(name = "calendarNameList")
public class ClientCalendarNameListWrapper implements DeepCloneable<ClientCalendarNameListWrapper> {
    private List<String> calendarNames;

    public ClientCalendarNameListWrapper() {
    }

    public ClientCalendarNameListWrapper(List<String> calendarNames) {
        if (calendarNames == null) {
            return;
        }
        this.calendarNames = new LinkedList<String>(calendarNames);
    }

    public ClientCalendarNameListWrapper(ClientCalendarNameListWrapper other) {
        checkNotNull(other);

        calendarNames = copyOf(other.getCalendarNames());
    }


    @XmlElement(name = "calendarName")
    public List<String> getCalendarNames() {
        return calendarNames;
    }

    public ClientCalendarNameListWrapper setCalendarNames(List<String> calendarNames) {
        this.calendarNames = calendarNames;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientCalendarNameListWrapper)) return false;

        ClientCalendarNameListWrapper that = (ClientCalendarNameListWrapper) o;

        return !(getCalendarNames() != null ? !getCalendarNames().equals(that.getCalendarNames()) : that.getCalendarNames() != null);

    }

    @Override
    public int hashCode() {
        return getCalendarNames() != null ? getCalendarNames().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ClientCalendarNameListWrapper{" +
                "calendarNames=" + calendarNames +
                '}';
    }

    @Override
    public ClientCalendarNameListWrapper deepClone() {
        return new ClientCalendarNameListWrapper(this);
    }
}
