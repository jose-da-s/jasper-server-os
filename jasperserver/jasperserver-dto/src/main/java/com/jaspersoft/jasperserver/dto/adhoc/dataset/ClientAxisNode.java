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
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * Date: 12/7/13
 *
 * @author stas
 */
@XmlRootElement(name = "axisNode")
public class ClientAxisNode implements Serializable, DeepCloneable<ClientAxisNode> {
    private Integer memberIdx;
    private Integer dataIdx;
    private List<ClientAxisNode> children;
    private boolean isAll = false;

    public ClientAxisNode() {
    }

    public ClientAxisNode(final ClientAxisNode node) {
        checkNotNull(node);

        memberIdx = node.getMemberIdx();
        dataIdx = node.getDataIdx();
        children = copyOf(node.getChildren());
        isAll = node.isAll();
    }

    public ClientAxisNode setChildren(List<ClientAxisNode> children) {
        this.children = children;
        return this;
    }

    public Integer getDataIdx() {
        return dataIdx;
    }

    public ClientAxisNode setDataIdx(Integer dataIdx) {
        this.dataIdx = dataIdx;
        return this;
    }

    @XmlElement(name = "memberIdx")
    public Integer getMemberIdx() {
        return memberIdx;
    }

    public ClientAxisNode setMemberIdx(Integer memberIdx) {
        this.memberIdx = memberIdx;
        return this;
    }

    @XmlElementWrapper(name = "children")
    @XmlElement(nillable = false, required = false, name = "axisNode")
    public List<ClientAxisNode> getChildren() {
        return children;
    }

    @XmlElement(name = "all")
    public boolean isAll() {
        return isAll;
    }

    public ClientAxisNode setAll(boolean isAll) {
        this.isAll = isAll;
        return this;
    }

    @Override
    public String toString() {
        return "ClientAxisNode{" +
                "memberIdx=" + memberIdx +
                ", dataIdx=" + dataIdx +
                ", isAll=" + isAll +
                ", children=" + children +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientAxisNode axisNode = (ClientAxisNode) o;

        if (isAll != axisNode.isAll) return false;
        if (memberIdx != null ? !memberIdx.equals(axisNode.memberIdx) : axisNode.memberIdx != null) return false;
        if (dataIdx != null ? !dataIdx.equals(axisNode.dataIdx) : axisNode.dataIdx != null) return false;
        return !(children != null ? !children.equals(axisNode.children) : axisNode.children != null);

    }

    @Override
    public int hashCode() {
        int result = memberIdx != null ? memberIdx.hashCode() : 0;
        result = 31 * result + (dataIdx != null ? dataIdx.hashCode() : 0);
        result = 31 * result + (children != null ? children.hashCode() : 0);
        result = 31 * result + (isAll ? 1 : 0);
        return result;
    }

    @Override
    public ClientAxisNode deepClone() {
        return new ClientAxisNode(this);
    }
}
