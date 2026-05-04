/*
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

import jakarta.validation.constraints.NotNull;
import java.util.Arrays;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Vasyl Spachynskyi
 * @version $Id$
 * @since 20.01.2016
 */
public class ClientQueryParams implements DeepCloneable<ClientQueryParams>{
    @NotNull
    private int[] offset;
    @NotNull
    private int[] pageSize;

    public ClientQueryParams() {
    }

    public ClientQueryParams(ClientQueryParams source) {
        checkNotNull(source);

        offset = copyOf(source.getOffset());
        pageSize = copyOf(source.getPageSize());
    }

    @Override
    public ClientQueryParams deepClone() {
        return new ClientQueryParams(this);
    }

    public int[] getOffset() {
        return offset;
    }

    public ClientQueryParams setOffset(int[] offset) {
        this.offset = offset;
        return this;
    }

    public int[] getPageSize() {
        return pageSize;
    }

    public ClientQueryParams setPageSize(int[] pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientQueryParams that = (ClientQueryParams) o;

        if (!Arrays.equals(offset, that.offset)) return false;
        return Arrays.equals(pageSize, that.pageSize);

    }

    @Override
    public int hashCode() {
        int result = offset != null ? Arrays.hashCode(offset) : 0;
        result = 31 * result + (pageSize != null ? Arrays.hashCode(pageSize) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientQueryParams{" +
                "offset=" + Arrays.toString(offset) +
                ", pageSize=" + Arrays.toString(pageSize) +
                '}';
    }
}
