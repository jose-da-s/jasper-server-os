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

import jakarta.xml.bind.annotation.XmlElement;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Vasyl Spachynskyi
 * @version $Id$
 * @since 19.01.2016
 */
public abstract class ClientQueryResultData<T extends ClientQueryResultData<T, DataSet, TotalCounts>, DataSet, TotalCounts> implements DeepCloneable<ClientQueryResultData<T, DataSet, TotalCounts>> {
    protected TotalCounts totalCounts;
    protected DataSet dataSet;
    protected ClientQueryParams queryParams;
    private boolean truncated = false;

    public ClientQueryResultData() {
    }

    public ClientQueryResultData(ClientQueryResultData<T, DataSet, TotalCounts> source) {
        checkNotNull(source);

        setTotalCounts(deepCopyOfTotalCounts(source.getTotalCounts()));
        setDataSet(deepCopyOfDataSet(source.getDataSet()));
        queryParams = copyOf(source.getQueryParams());
        truncated = source.getTruncated();
    }

    protected abstract TotalCounts deepCopyOfTotalCounts(TotalCounts totalCounts);

    protected abstract DataSet deepCopyOfDataSet(DataSet dataSet);

    public abstract DataSet getDataSet();

    protected abstract T setDataSet(DataSet dataSet);

    @XmlElement(name = "params")
    public ClientQueryParams getQueryParams() {
        return queryParams;
    }

    public T setQueryParams(ClientQueryParams queryParams) {
        this.queryParams = queryParams;
        return (T) this;
    }

    protected abstract TotalCounts getTotalCounts();

    protected abstract T setTotalCounts(TotalCounts totalCounts);

    /**
     * Indicates - If data set size reached the limit, and result contains no full data
     */
    public Boolean getTruncated() {
        return truncated;
    }

    @SuppressWarnings("unchecked")
    public T setTruncated(Boolean truncated) {
        this.truncated = truncated != null ? truncated : false;
        return (T) this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientQueryResultData that = (ClientQueryResultData) o;

        if (getDataSet() != null ? !getDataSet().equals(that.getDataSet()) : that.getDataSet() != null) return false;
        if (queryParams != null ? !queryParams.equals(that.queryParams) : that.queryParams != null) return false;
        if (getTotalCounts() != null ? !getTotalCounts().equals(that.getTotalCounts()) : that.getTotalCounts() != null)
            return false;
        if (truncated != that.truncated) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getDataSet() != null ? getDataSet().hashCode() : 0;
        result = 31 * result + (queryParams != null ? queryParams.hashCode() : 0);
        result = 31 * result + (getTotalCounts() != null ? getTotalCounts().hashCode() : 0);
        result = 31 * result + (truncated ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientQueryResultData{" +
                "totalCounts=" + totalCounts +
                ", dataSet=" + dataSet +
                ", queryParams=" + queryParams +
                ", truncated=" + truncated +
                '}';
    }
}
