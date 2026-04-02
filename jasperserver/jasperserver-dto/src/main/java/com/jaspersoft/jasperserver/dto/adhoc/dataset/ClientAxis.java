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
import jakarta.xml.bind.annotation.XmlElements;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Vasyl Spachynskyi
 * @version $Id$
 * @since 05.04.2016
 */
public class ClientAxis implements DeepCloneable<ClientAxis>, Serializable {
    private ClientAxisNode axisNode;
    private List<ClientMultiAxisDatasetLevel> levels = new ArrayList<ClientMultiAxisDatasetLevel>();

    public ClientAxis() {
    }

    public ClientAxis(ClientAxis axis) {
        checkNotNull(axis);

        axisNode = copyOf(axis.getAxisNode());
        levels = copyOf(axis.getLevels());
    }

    public ClientAxisNode getAxisNode() {
        return axisNode;
    }

    public ClientAxis setAxisNode(ClientAxisNode axisNode) {
        this.axisNode = axisNode;
        return this;
    }

    @XmlElementWrapper(name = "levels")
    @XmlElements({
            @XmlElement(name = "level", type = ClientMultiAxisGroupLevel.class),
            @XmlElement(name = "aggregation", type = ClientMultiAxisAggregationLevel.class)})
    public List<ClientMultiAxisDatasetLevel> getLevels() {
        return levels;
    }

    public ClientAxis setLevels(List<ClientMultiAxisDatasetLevel> levels) {
        if (levels == null) {
            this.levels = new ArrayList<ClientMultiAxisDatasetLevel>();
        } else {
            this.levels = levels;
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientAxis that = (ClientAxis) o;

        if (axisNode != null ? !axisNode.equals(that.axisNode) : that.axisNode != null) return false;
        return levels.equals(that.levels);

    }

    @Override
    public int hashCode() {
        int result = axisNode != null ? axisNode.hashCode() : 0;
        result = 31 * result + (levels.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "ClientAxis{" +
                "axisNode=" + axisNode +
                ", levels=" + levels +
                '}';
    }

    @Override
    public ClientAxis deepClone() {
        return new ClientAxis(this);
    }
}
