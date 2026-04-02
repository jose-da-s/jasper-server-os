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
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Volodya Sabadosh
 * @version $Id$
 */
public abstract class AbstractClientDatasetLevelNode<T extends AbstractClientDatasetLevelNode<T, Data>, Data> implements DeepCloneable, Serializable {
    private List<AbstractClientDatasetLevelNode> children;

    public AbstractClientDatasetLevelNode() {

    }

    public AbstractClientDatasetLevelNode(AbstractClientDatasetLevelNode other) {
        checkNotNull(other);

        children = copyOf(other.getChildren());
    }

    public abstract Data getData();

    public abstract T setData(Data data);

    @XmlElementWrapper(name = "children")
    @XmlElements({
            @XmlElement(name = "all", type = ClientDatasetAllLevelNode.class),
            @XmlElement(name = "group", type = ClientDatasetGroupLevelNode.class),
            @XmlElement(name = "detail", type = ClientDatasetDetailLevelNode.class)})
    public List<AbstractClientDatasetLevelNode> getChildren() {
        return children;
    }

    @SuppressWarnings("unchecked")
    public T setChildren(List<AbstractClientDatasetLevelNode> children) {
        this.children = children;
        return (T) this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractClientDatasetLevelNode<?, ?> levelNode = (AbstractClientDatasetLevelNode<?, ?>) o;

        return !(children != null ? !children.equals(levelNode.children) : levelNode.children != null);
    }

    @Override
    public int hashCode() {
        return children != null ? children.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "AbstractClientDatasetLevelNode{" +
                "data=" + getData() +
                ", children=" + children +
                '}';
    }
}
