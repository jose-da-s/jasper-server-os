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
import java.util.Arrays;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * Created by schubar on 12/3/13.
 * @version $Id$
 */
@XmlRootElement
public class ClientMultiAxisDataset implements DeepCloneable<ClientMultiAxisDataset>, Serializable {
    private List<ClientAxis> axes;

    private List<String[]> data;
    private List<Integer> counts = new ArrayList<Integer>();

    public ClientMultiAxisDataset() {}

    public ClientMultiAxisDataset(final ClientMultiAxisDataset dataset) {
        checkNotNull(dataset);

        axes = copyOf(dataset.getAxes());
        data = copyOf(dataset.getData());
        counts = copyOf(dataset.getCounts());
    }

    @XmlElementWrapper(name = "axes")
    @XmlElement(name = "axis")
    public List<ClientAxis> getAxes() {
        return axes;
    }

    public ClientMultiAxisDataset setAxes(List<ClientAxis> axes) {
        this.axes = axes;
        return this;
    }

    @XmlElementWrapper(name = "data")
    @XmlElement(name = "column")
    public List<String[]> getData() {
        return data;
    }

    public ClientMultiAxisDataset setData(List<String[]> data) {
        this.data = data;
        return this;
    }

    public List<Integer> getCounts() {
        return counts;
    }

    public ClientMultiAxisDataset setCounts(List<Integer> counts) {
        if (counts == null) {
            this.counts = new ArrayList<Integer>();
        } else {
            this.counts = counts;
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientMultiAxisDataset that = (ClientMultiAxisDataset) o;

        if (axes != null ? !axes.equals(that.axes) : that.axes != null) return false;
        if (!equalsListOfArrays(data, that.data)) return false;

        return counts.equals(that.counts);
    }

    private <T> boolean equalsListOfArrays(List<T[]> first, List<T[]> second) {
        if (first == null && second == null) {
            return true;
        } else if (first == null) {
            return false;
        } else if (second == null) {
            return false;
        } else if (first.size() != second.size()) {
            return false;
        } else {
            for (int i = 0; i < first.size(); i++) {
                T[] firstItem = first.get(i);
                T[] secondItem = second.get(i);
                if (!Arrays.equals(firstItem, secondItem)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = axes != null ? axes.hashCode() : 0;
        if (data != null) {
            for (String[] datum : data) {
                result = 31 * result + Arrays.hashCode(datum);
            }
        }
        result = 31 * result + counts.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ClientMultiAxisDataset{" +
                "axes=" + axes +
                ", data=" + data +
                ", counts=" + counts +
                '}';
    }

    @Override
    public ClientMultiAxisDataset deepClone() {
        return new ClientMultiAxisDataset(this);
    }
}
