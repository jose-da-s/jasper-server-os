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

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;

@XmlRootElement(name = "selectedValues")
public class SelectedValuesListWrapper  implements Serializable, DeepCloneable<SelectedValuesListWrapper> {

    private final static long serialVersionUID = 1l;
    private List<SelectedValue> selectedValues;

    public SelectedValuesListWrapper(){
    }

    public SelectedValuesListWrapper(List<SelectedValue> selectedValues){
        this.selectedValues = selectedValues;
    }

    public SelectedValuesListWrapper(SelectedValuesListWrapper other) {
        checkNotNull(other);

        this.selectedValues = other.getSelectedValues();

    }
    @XmlElement(name = "selectedValue")
    public List<SelectedValue> getSelectedValues() {
        return selectedValues;
    }

    public SelectedValuesListWrapper setSelectedValues(List<SelectedValue> selectedValues) {
        this.selectedValues = selectedValues;
        return this;
    }

    @Override
    public SelectedValuesListWrapper deepClone() {
        return new SelectedValuesListWrapper(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SelectedValuesListWrapper that = (SelectedValuesListWrapper) o;
        return Objects.equals(selectedValues, that.selectedValues);

    }

    @Override
    public int hashCode() {
        return selectedValues != null ? selectedValues.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "SelectedValuesListWrapper{" +
                 "selectedValues=" + selectedValues +
                '}';
    }

}
