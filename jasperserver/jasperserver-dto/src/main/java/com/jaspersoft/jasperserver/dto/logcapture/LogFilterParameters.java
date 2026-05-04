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
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Yakiv Tymoshenko
 * @version $Id: Id $
 * @since 07.10.14
 */
@XmlRootElement
@XmlType(propOrder = {})
public class LogFilterParameters implements DeepCloneable<LogFilterParameters> {
    private String userId;
    private String sessionId;
    private ResourceAndSnapshotFilter resourceAndSnapshotFilter;

    public LogFilterParameters() {
        resourceAndSnapshotFilter = new ResourceAndSnapshotFilter();
    }

    public LogFilterParameters(LogFilterParameters other) {
        checkNotNull(other);

        this.userId = other.getUserId();
        this.sessionId = other.getSessionId();
        this.resourceAndSnapshotFilter = copyOf(other.getResourceAndSnapshotFilter());
    }

    @Override
    public LogFilterParameters deepClone() {
        return new LogFilterParameters(this);
    }

    public String getUserId() {
        return userId;
    }

    public LogFilterParameters setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getSessionId() {
        return sessionId;
    }

    public LogFilterParameters setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    @XmlElement(name = "resource")
    public ResourceAndSnapshotFilter getResourceAndSnapshotFilter() {
        return resourceAndSnapshotFilter;
    }

    public LogFilterParameters setResourceAndSnapshotFilter(ResourceAndSnapshotFilter resourceAndSnapshotFilter) {
        this.resourceAndSnapshotFilter = resourceAndSnapshotFilter;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LogFilterParameters)) return false;

        LogFilterParameters that = (LogFilterParameters) o;

        if (getUserId() != null ? !getUserId().equals(that.getUserId()) : that.getUserId() != null) return false;
        if (getSessionId() != null ? !getSessionId().equals(that.getSessionId()) : that.getSessionId() != null)
            return false;
        return !(getResourceAndSnapshotFilter() != null ? !getResourceAndSnapshotFilter().equals(that.getResourceAndSnapshotFilter()) : that.getResourceAndSnapshotFilter() != null);

    }

    @Override
    public int hashCode() {
        int result = getUserId() != null ? getUserId().hashCode() : 0;
        result = 31 * result + (getSessionId() != null ? getSessionId().hashCode() : 0);
        result = 31 * result + (getResourceAndSnapshotFilter() != null ? getResourceAndSnapshotFilter().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LogFilterParameters{" +
                "userId='" + userId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", resourceAndSnapshotFilter=" + resourceAndSnapshotFilter +
                '}';
    }
}
