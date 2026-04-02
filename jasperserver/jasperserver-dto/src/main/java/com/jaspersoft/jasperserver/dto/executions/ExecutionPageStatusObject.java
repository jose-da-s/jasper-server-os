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
package com.jaspersoft.jasperserver.dto.executions;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;

import jakarta.xml.bind.annotation.XmlRootElement;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Narcis Marcu (nmarcu@tibco.com)
 */
@XmlRootElement(name = "pageStatus")
public class ExecutionPageStatusObject implements DeepCloneable<ExecutionPageStatusObject> {

    private Boolean pageFinal;
    private Long pageTimestamp;
    private ExecutionStatus reportStatus;
    private ErrorDescriptor errorDescriptor;

    public ExecutionPageStatusObject(){}

    public ExecutionPageStatusObject(ExecutionPageStatusObject source){
        checkNotNull(source);

        pageFinal = source.getPageFinal();
        pageTimestamp = source.getPageTimestamp();
        reportStatus = source.getReportStatus();
        errorDescriptor = copyOf(source.getErrorDescriptor());
    }

    public Boolean getPageFinal() {
        return pageFinal;
    }

    public ExecutionPageStatusObject setPageFinal(Boolean pageFinal) {
        this.pageFinal = pageFinal;
        return this;
    }

    public Long getPageTimestamp() {
        return pageTimestamp;
    }

    public ExecutionPageStatusObject setPageTimestamp(Long pageTimestamp) {
        this.pageTimestamp = pageTimestamp;
        return this;
    }

    public ExecutionStatus getReportStatus() {
        return reportStatus;
    }

    public ExecutionPageStatusObject setReportStatus(ExecutionStatus reportStatus) {
        this.reportStatus = reportStatus;
        return this;
    }

    public ErrorDescriptor getErrorDescriptor() {
        return errorDescriptor;
    }

    public ExecutionPageStatusObject setErrorDescriptor(ErrorDescriptor errorDescriptor) {
        this.errorDescriptor = errorDescriptor;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExecutionPageStatusObject that = (ExecutionPageStatusObject) o;

        if (errorDescriptor != null ? !errorDescriptor.equals(that.errorDescriptor) : that.errorDescriptor != null) {
            return false;
        }
        if (pageFinal != that.pageFinal) {
            return false;
        }
        if (pageTimestamp != that.pageTimestamp) {
            return false;
        }
        if (reportStatus != that.reportStatus) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = reportStatus != null ? reportStatus.hashCode() : 0;
        result = 31 * result + (pageFinal != null ? pageFinal.hashCode() : 0);
        result = 31 * result + (pageTimestamp != null ? pageTimestamp.hashCode() : 0);
        result = 31 * result + (errorDescriptor != null ? errorDescriptor.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ReportExecutionPageStatusObject{" +
                "pageFinal=" + pageFinal +
                ", pageTimestamp=" + pageTimestamp +
                ", reportStatus=" + reportStatus +
                ", errorDescriptor=" + errorDescriptor +
                '}';
    }

    @Override
    public ExecutionPageStatusObject deepClone() {
        return new ExecutionPageStatusObject(this);
    }
}
