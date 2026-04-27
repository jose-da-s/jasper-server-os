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

import com.jaspersoft.jasperserver.dto.adhoc.dataset.ClientFlatDataset;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Vasyl Spachynskyi
 * @version $Id$
 * @since 11.02.2016
 */
@XmlRootElement(name = "resultData")
public class ClientFlatQueryResultData extends ClientQueryResultData<ClientFlatQueryResultData, ClientFlatDataset,
        Integer> {

    public ClientFlatQueryResultData() {
    }

    public ClientFlatQueryResultData(ClientFlatDataset dataSet) {
        this.dataSet = dataSet;
    }

    public ClientFlatQueryResultData(ClientFlatQueryResultData source) {
        super(source);
    }

    @Override
    protected Integer deepCopyOfTotalCounts(Integer totalCounts) {
        return totalCounts;
    }

    @Override
    protected ClientFlatDataset deepCopyOfDataSet(ClientFlatDataset dataSet) {
        return copyOf(dataSet);
    }

    @Override
    public ClientFlatQueryResultData deepClone() {
        return new ClientFlatQueryResultData(this);
    }

    @Override
    @XmlElement(name = "totalCounts")
    public Integer getTotalCounts() {
        return totalCounts;
    }

    @Override
    public ClientFlatQueryResultData setTotalCounts(Integer totalCounts) {
        this.totalCounts = totalCounts;
        return this;
    }

    @Override
    @XmlElement(name = "dataset")
    public ClientFlatDataset getDataSet() {
        return dataSet;
    }

    @Override
    public ClientFlatQueryResultData setDataSet(ClientFlatDataset dataset) {
        this.dataSet = dataset;
        return this;
    }

    @Override
    public String toString() {
        return "ClientFlatQueryResultData{" +
                "dataSet=" + dataSet +
                ", totalCounts=" + totalCounts +
                "} " + super.toString();
    }
}
