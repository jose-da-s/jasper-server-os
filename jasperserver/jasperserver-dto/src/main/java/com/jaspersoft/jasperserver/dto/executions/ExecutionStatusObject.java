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
 * @author Yaroslav.Kovalchyk
 * @version $Id: ReportExecutionStatusEntity.java 26599 2012-12-10 13:04:23Z ykovalchyk $
 */
@XmlRootElement(name = "status")
public class ExecutionStatusObject implements DeepCloneable<ExecutionStatusObject> {

    private ExecutionStatus value;
    private ErrorDescriptor errorDescriptor;

    public ExecutionStatusObject(){}

    public ExecutionStatusObject(ExecutionStatusObject source){
        checkNotNull(source);

        value = source.getValue();
        errorDescriptor = copyOf(source.getErrorDescriptor());
    }

    public ExecutionStatus getValue() {
        return value;
    }

    public ExecutionStatusObject setValue(ExecutionStatus value) {
        this.value = value;
        return this;
    }

    public ErrorDescriptor getErrorDescriptor() {
        return errorDescriptor;
    }

    public ExecutionStatusObject setErrorDescriptor(ErrorDescriptor errorDescriptor) {
        this.errorDescriptor = errorDescriptor;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExecutionStatusObject that = (ExecutionStatusObject) o;

        if (errorDescriptor != null ? !errorDescriptor.equals(that.errorDescriptor) : that.errorDescriptor != null)
            return false;
        if (value != that.value) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (errorDescriptor != null ? errorDescriptor.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ReportExecutionStatusObject{" +
                "value=" + value +
                ", errorDescriptor=" + errorDescriptor +
                '}';
    }

    @Override
    public ExecutionStatusObject deepClone() {
        return new ExecutionStatusObject(this);
    }
}
