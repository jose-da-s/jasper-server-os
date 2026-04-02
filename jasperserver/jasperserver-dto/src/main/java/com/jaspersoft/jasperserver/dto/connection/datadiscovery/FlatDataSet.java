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
package com.jaspersoft.jasperserver.dto.connection.datadiscovery;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;
import com.jaspersoft.jasperserver.dto.resources.domain.ResourceGroupElement;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.hashCodeOfListOfArrays;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.isListsOfArraysEquals;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@XmlRootElement(name = "flatDataSet")
public class FlatDataSet implements DeepCloneable<FlatDataSet> {
    private List<String[]> data;
    private ResourceGroupElement metadata;

    public FlatDataSet(){}

    public FlatDataSet(FlatDataSet source){
        checkNotNull(source);

        data = copyOf(source.getData());
        metadata = copyOf(source.getMetadata());
    }

    @XmlElementWrapper(name = "rows")
    @XmlElement(name = "row")
    public List<String[]> getData() {
        return data;
    }

    public FlatDataSet setData(List<String[]> data) {
        this.data = data;
        return this;
    }

    public ResourceGroupElement getMetadata() {
        return metadata;
    }

    public FlatDataSet setMetadata(ResourceGroupElement metadata) {
        this.metadata = metadata;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FlatDataSet)) return false;

        FlatDataSet that = (FlatDataSet) o;

        if (data != null ? !isListsOfArraysEquals(data, that.data) : that.data != null) return false;
        return metadata != null ? metadata.equals(that.metadata) : that.metadata == null;

    }

    @Override
    public int hashCode() {
        int result = hashCodeOfListOfArrays(data);
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FlatDataSet{" +
                "data=" + data +
                ", metadata=" + metadata +
                '}';
    }

    /*
     * DeepCloneable
     */

    @Override
    public FlatDataSet deepClone() {
        return new FlatDataSet(this);
    }
}
