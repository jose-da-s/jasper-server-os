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
public class ClientDatasetGroupLevel extends AbstractClientDatasetLevel {
    private String fieldRef;
    private String type;
    private List<String> members;

    public ClientDatasetGroupLevel() {}

    public ClientDatasetGroupLevel(ClientDatasetGroupLevel level) {
        super(level);
        fieldRef = level.getFieldRef();
        type = level.getType();
        members = copyOf(level.getMembers());
    }

    @XmlElement(name="reference")
    public String getFieldRef() {
        return fieldRef;
    }

    public ClientDatasetGroupLevel setFieldRef(String name) {
        this.fieldRef = name;
        return this;
    }

    @XmlElementWrapper(name="members")
    @XmlElement(name="member")
    public List<String> getMembers() {
        return members;
    }

    public ClientDatasetGroupLevel setMembers(List<String> members) {
        this.members = members;
        return this;
    }

    @Override
    @XmlElementWrapper(name = "aggregations")
    @XmlElement(name = "aggregation")
    public List<ClientDatasetFieldReference> getFieldRefs() {
        return super.getFieldRefs();
    }

    @XmlElement(name = "type")
    public String getType() {
        return type;
    }

    public ClientDatasetGroupLevel setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ClientDatasetGroupLevel that = (ClientDatasetGroupLevel) o;

        if (fieldRef != null ? !fieldRef.equals(that.fieldRef) : that.fieldRef != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        return !(members != null ? !members.equals(that.members) : that.members != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (fieldRef != null ? fieldRef.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (members != null ? members.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientDatasetGroupLevel{" +
                "fieldRef='" + fieldRef + '\'' +
                ", type='" + type + '\'' +
                ", members=" + members +
                '}';
    }

    @Override
    public ClientDatasetGroupLevel deepClone() {
        return new ClientDatasetGroupLevel(this);
    }
}
