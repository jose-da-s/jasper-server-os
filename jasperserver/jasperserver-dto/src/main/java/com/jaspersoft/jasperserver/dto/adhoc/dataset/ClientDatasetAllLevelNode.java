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

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Volodya Sabadosh
 * @version $Id$
 */
public class ClientDatasetAllLevelNode extends AbstractClientDatasetLevelNode<ClientDatasetAllLevelNode, List<String>> {

    private List<String> data;

    public ClientDatasetAllLevelNode() {
        super();
    }

    public ClientDatasetAllLevelNode(ClientDatasetAllLevelNode other) {
        super(other);
        data = copyOf(other.getData());
    }

    @Override
    @XmlElementWrapper(name = "data")
    @XmlElement(name = "item")
    public List<String> getData() {
        return this.data;
    }

    @Override
    public ClientDatasetAllLevelNode setData(List<String> data) {
        this.data = data;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ClientDatasetAllLevelNode that = (ClientDatasetAllLevelNode) o;

        return data != null ? data.equals(that.data) : that.data == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientDatasetAllLevelNode{} " + super.toString();
    }

    @Override
    public ClientDatasetAllLevelNode deepClone() {
        return new ClientDatasetAllLevelNode(this);
    }
}
