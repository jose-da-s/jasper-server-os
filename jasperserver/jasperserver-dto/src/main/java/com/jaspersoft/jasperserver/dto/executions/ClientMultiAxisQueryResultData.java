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

import com.jaspersoft.jasperserver.dto.adhoc.dataset.ClientMultiAxisDataset;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Vasyl Spachynskyi
 * @version $Id$
 * @since 11.02.2016
 */
@XmlRootElement(name = "resultData")
public class ClientMultiAxisQueryResultData extends ClientQueryResultData<ClientMultiAxisQueryResultData, ClientMultiAxisDataset, List<Integer>> {

    private ClientMultiAxisDataset dataSet;
    private List<Integer> totalCounts;

    public ClientMultiAxisQueryResultData() {
    }

    public ClientMultiAxisQueryResultData(ClientMultiAxisQueryResultData source) {
        super(source);
    }

    public ClientMultiAxisQueryResultData(ClientMultiAxisDataset dataSet) {
        setDataSet(dataSet);
    }

    @Override
    protected List<Integer> deepCopyOfTotalCounts(List<Integer> totalCounts) {
        return copyOf(totalCounts);
    }

    @Override
    protected ClientMultiAxisDataset deepCopyOfDataSet(ClientMultiAxisDataset dataSet) {
        return copyOf(dataSet);
    }

    @Override
    public ClientMultiAxisQueryResultData deepClone() {
        return new ClientMultiAxisQueryResultData(this);
    }

    @Override
    @XmlElement(name = "dataset")
    public ClientMultiAxisDataset getDataSet() {
        return dataSet;
    }

    @Override
    public ClientMultiAxisQueryResultData setDataSet(ClientMultiAxisDataset dataset) {
        this.dataSet = dataset;
        return this;
    }

    @Override
    @XmlElement(name = "totalCounts")
    public List<Integer> getTotalCounts() {
        return totalCounts;
    }

    @Override
    public ClientMultiAxisQueryResultData setTotalCounts(List<Integer> totalCounts) {
        this.totalCounts = totalCounts;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ClientMultiAxisQueryResultData that = (ClientMultiAxisQueryResultData) o;

        if (dataSet != null ? !dataSet.equals(that.dataSet) : that.dataSet != null) return false;
        return totalCounts != null ? totalCounts.equals(that.totalCounts) : that.totalCounts == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (dataSet != null ? dataSet.hashCode() : 0);
        result = 31 * result + (totalCounts != null ? totalCounts.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientMultiAxisQueryResultData{" +
                "dataSet=" + dataSet +
                ", totalCounts=" + totalCounts +
                "} " + super.toString();
    }
}
