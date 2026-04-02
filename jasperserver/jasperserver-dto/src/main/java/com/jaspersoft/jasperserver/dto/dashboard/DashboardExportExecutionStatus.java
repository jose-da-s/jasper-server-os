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

package com.jaspersoft.jasperserver.dto.dashboard;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlRootElement;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;

/**
 * <p>DTO for dashboard execution status.</p>
 *
 * @author Zakhar Tomchenko
 * @version $Id: $
 */
@XmlRootElement(name = "dashboardExportExecutionStatus")
public class DashboardExportExecutionStatus implements DeepCloneable<DashboardExportExecutionStatus> {
    private String id;
    private int progress;
    private Status status;

    public DashboardExportExecutionStatus() {}

    public DashboardExportExecutionStatus(DashboardExportExecutionStatus other) {
        checkNotNull(other);

        id = other.id;
        progress = other.progress;
        status = other.status;
    }

    public String getId() {
        return id;
    }

    public DashboardExportExecutionStatus setId(String id) {
        this.id = id;
        return this;
    }

    public int getProgress() {
        return progress;
    }

    public DashboardExportExecutionStatus setProgress(int progress) {
        this.progress = progress;
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public DashboardExportExecutionStatus setStatus(Status status) {
        this.status = status;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DashboardExportExecutionStatus that = (DashboardExportExecutionStatus) o;

        if (progress != that.progress) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return status == that.status;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + progress;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DashboardExportExecutionStatus{" +
                "id='" + id + '\'' +
                ", progress=" + progress +
                ", status=" + status +
                '}';
    }

    public enum Status {
        execution, ready, failed, cancelled
    }

    /*
     *  DeepCloneable
     */

    @Override
    public DashboardExportExecutionStatus deepClone() {
        return new DashboardExportExecutionStatus(this);
    }
}
