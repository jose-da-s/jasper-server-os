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
package com.jaspersoft.jasperserver.dto.logcapture;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Yakiv Tymoshenko
 * @version $Id: Id $
 * @since 06.10.14
 */
@XmlRootElement
@SuppressWarnings("unused")
public class CollectorSettingsList implements DeepCloneable<CollectorSettingsList> {
    List<CollectorSettings> collectorSettingsList;

    public CollectorSettingsList() {
    }

    public CollectorSettingsList(CollectorSettingsList other) {
        checkNotNull(other);

        this.collectorSettingsList = copyOf(other.getCollectorSettingsList());
    }

    public CollectorSettingsList(List<CollectorSettings> collectorSettingsList) {
        this.collectorSettingsList = copyOf(collectorSettingsList);
    }

    @Override
    public CollectorSettingsList deepClone() {
        return new CollectorSettingsList(this);
    }

    @XmlElementWrapper(name = "CollectorSettingsList")
    @XmlElement(name = "CollectorSettings")
    public List<CollectorSettings> getCollectorSettingsList() {
        return collectorSettingsList;
    }

    public CollectorSettingsList setCollectorSettingsList(List<CollectorSettings> collectorSettingsList) {
        this.collectorSettingsList = collectorSettingsList;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CollectorSettingsList)) return false;

        CollectorSettingsList that = (CollectorSettingsList) o;

        return !(getCollectorSettingsList() != null ? !getCollectorSettingsList().equals(that.getCollectorSettingsList()) : that.getCollectorSettingsList() != null);

    }

    @Override
    public int hashCode() {
        return getCollectorSettingsList() != null ? getCollectorSettingsList().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "CollectorSettingsList{" +
                "collectorSettingsList=" + collectorSettingsList +
                '}';
    }
}
