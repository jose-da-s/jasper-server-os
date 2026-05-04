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
package com.jaspersoft.jasperserver.dto.adhoc.dataset;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.hashCodeOfListOfArrays;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.isListsOfArraysEquals;

/**
 * @author Volodya Sabadosh
 * @version $Id$
 */
@XmlRootElement(name = "flatDataset")
public class ClientFlatDataset implements DeepCloneable<ClientFlatDataset>, Serializable {
    private int counts;
    private List<ClientFlatDatasetFieldReference> fields = new ArrayList<ClientFlatDatasetFieldReference>();
    private List<String[]> rows;

    public ClientFlatDataset() {

    }

    public ClientFlatDataset(ClientFlatDataset dataset) {
        checkNotNull(dataset);

        this.counts = dataset.getCounts();
        this.fields = copyOf(dataset.getFields());
        this.rows = copyOf(dataset.getRows());
    }

    @XmlElement(name = "counts")
    public int getCounts() {
        return counts;
    }

    public ClientFlatDataset setCounts(int counts) {
        this.counts = counts;
        return this;
    }

    public List<ClientFlatDatasetFieldReference> getFields() {
        return fields;
    }

    public ClientFlatDataset setFields(List<ClientFlatDatasetFieldReference> fields) {
        if (fields == null) {
            this.fields = new ArrayList<ClientFlatDatasetFieldReference>();
        } else {
            this.fields = fields;
        }
        return this;
    }

    @XmlElementWrapper(name = "rows")
    @XmlElement(name = "row")
    public List<String[]> getRows() {
        return rows;
    }

    public ClientFlatDataset setRows(List<String[]> rows) {
        this.rows = rows;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientFlatDataset that = (ClientFlatDataset) o;

        if (counts != that.counts) return false;
        if (!fields.equals(that.fields)) return false;
        return isListsOfArraysEquals(rows, that.rows);
    }


    @Override
    public int hashCode() {
        int result = counts;
        result = 31 * result + fields.hashCode();
        result = 31 * result + hashCodeOfListOfArrays(rows);
        return result;
    }

    @Override
    public String toString() {
        return "ClientFlatDataset{" +
                "counts=" + counts +
                ", fields=" + fields +
                ", rows=" + rows +
                '}';
    }

    @Override
    public ClientFlatDataset deepClone() {
        return new ClientFlatDataset(this);
    }
}
