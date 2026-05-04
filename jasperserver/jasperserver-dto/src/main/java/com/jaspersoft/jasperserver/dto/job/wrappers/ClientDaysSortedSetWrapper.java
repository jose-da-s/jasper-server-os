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
import java.util.SortedSet;
import java.util.TreeSet;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * This class is needed because of bug in JAXB.
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 * @XmlElementWrapper doesn't support @XmlJavaTypeAdapter
 */
@XmlRootElement
public class ClientDaysSortedSetWrapper implements DeepCloneable<ClientDaysSortedSetWrapper> {

    private SortedSet<String> days;

    public ClientDaysSortedSetWrapper() {
    }

    public ClientDaysSortedSetWrapper(SortedSet<String> days) {
        if (days == null) {
            return;
        }
        this.days = new TreeSet<String>(days);
    }

    public ClientDaysSortedSetWrapper(ClientDaysSortedSetWrapper other) {
        checkNotNull(other);

        days = copyOf(other.getDays());
    }

    @XmlElement(name = "day")
    public SortedSet<String> getDays() {
        return days;
    }

    public ClientDaysSortedSetWrapper setDays(SortedSet<String> days) {
        this.days = days;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientDaysSortedSetWrapper)) return false;

        ClientDaysSortedSetWrapper that = (ClientDaysSortedSetWrapper) o;

        return !(getDays() != null ? !getDays().equals(that.getDays()) : that.getDays() != null);

    }

    @Override
    public String toString() {
        return "ClientDaysSortedSetWrapper{" +
                "days=" + days +
                '}';
    }

    @Override
    public int hashCode() {
        return getDays() != null ? getDays().hashCode() : 0;
    }

    @Override
    public ClientDaysSortedSetWrapper deepClone() {
        return new ClientDaysSortedSetWrapper(this);
    }
}
