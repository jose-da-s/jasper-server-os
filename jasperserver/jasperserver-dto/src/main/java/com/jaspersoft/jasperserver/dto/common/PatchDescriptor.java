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

package com.jaspersoft.jasperserver.dto.common;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author Zakhar.Tomchenco
 * @version $Id$
 */
@XmlRootElement(name = "patchItems")
public class PatchDescriptor implements DeepCloneable<PatchDescriptor> {
    private List<PatchItem> items;
    private int version;

    public PatchDescriptor() {
    }

    public PatchDescriptor(int version) {
        this.version = version;
    }

    public PatchDescriptor(List<PatchItem> items, int version) {
        this.items = new ArrayList<PatchItem>(items);
        this.version = version;
    }

    public PatchDescriptor(PatchDescriptor other) {
        checkNotNull(other);

        this.items = copyOf(other.getItems());
        this.version = other.getVersion();
    }

    @Override
    public PatchDescriptor deepClone() {
        return new PatchDescriptor(this);
    }

    @XmlElement(name = "version")
    public int getVersion() {
        return version;
    }

    public PatchDescriptor setVersion(int version) {
        this.version = version;
        return this;
    }

    @XmlElement(name = "patch")
    public List<PatchItem> getItems() {
        return items;
    }

    public PatchDescriptor setItems(List<PatchItem> items) {
        this.items = items;
        return this;
    }

    public PatchDescriptor field(String name, String value) {
        if (items == null) {
            items = new LinkedList<PatchItem>();
        }

        PatchItem item = new PatchItem();
        item.setField(name);
        item.setValue(value);
        items.add(item);

        return this;
    }

    public PatchDescriptor expression(String expression) {
        if (items == null) {
            items = new LinkedList<PatchItem>();
        }

        PatchItem item = new PatchItem();
        item.setExpression(expression);
        items.add(item);

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PatchDescriptor that = (PatchDescriptor) o;

        if (version != that.version) return false;
        return items != null ? items.equals(that.items) : that.items == null;
    }

    @Override
    public int hashCode() {
        int result = items != null ? items.hashCode() : 0;
        result = 31 * result + version;
        return result;
    }

    @Override
    public String toString() {
        return "PatchDescriptor{" +
                "items=" + items +
                ", version=" + version +
                '}';
    }
}
