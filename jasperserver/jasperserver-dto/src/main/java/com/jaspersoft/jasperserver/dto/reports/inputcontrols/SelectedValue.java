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

package com.jaspersoft.jasperserver.dto.reports.inputcontrols;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;

@XmlRootElement
public class SelectedValue implements Serializable, DeepCloneable<SelectedValue> {
    private final static long serialVersionUID = 1l;

    private String id;
    private List<InputControlOption> options;

    public SelectedValue(){
    }

    public SelectedValue(SelectedValue other) {
        checkNotNull(other);


        this.id = other.getId();
        this.options = other.getOptions();

    }

    @XmlAttribute(required = true)
    public String getId() {
        return id;
    }

    public SelectedValue setId(String id) {
        this.id = id;
        return this;
    }

    @XmlElement(name = "options")
    public List<InputControlOption> getOptions() {
        return options;
    }

    public SelectedValue setOptions(List<InputControlOption> options) {
        this.options = options;
        return this;
    }


    @Override
    public SelectedValue deepClone() {
        return new SelectedValue(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SelectedValue that = (SelectedValue) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(options, that.options);


    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (options != null ? options.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SelectedValue {" +
                "id=" + id +
                ", options=" + options +
                '}';
    }

}
