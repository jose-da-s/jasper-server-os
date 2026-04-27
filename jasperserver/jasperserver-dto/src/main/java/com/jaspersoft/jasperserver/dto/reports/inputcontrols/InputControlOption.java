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

import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;

/**
 * InputControlOption
 * @author akasych
 * @version $Id$
 * Simple class to transfer Input Control list option.
 */

@XmlRootElement(name = "inputControlOption")
public class InputControlOption implements Serializable, DeepCloneable<InputControlOption> {

    private final static long serialVersionUID = 1L;

    private Boolean selected;
    private String label;
    private String value;

    public InputControlOption(String value, String label, Boolean selected) {
        this.label = label;
        this.value = value;
        this.selected = selected;
    }

    public InputControlOption() {
        this(null, null, false);
    }

    public InputControlOption(String value, String label) {
        this(value, label, false);
    }

    public InputControlOption(InputControlOption other) {
        checkNotNull(other);

        this.selected = other.isSelected();
        this.label = other.getLabel();
        this.value = other.getValue();
    }

    public boolean hasSelected() {
        return selected != null && selected;
    }

    public Boolean isSelected() {
        return selected;
    }

    public InputControlOption setSelected(Boolean selected) {
        this.selected = selected;
        return this;
    }

    public String getLabel() {
        return label;
    }
    public InputControlOption setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getValue() {
        return value;
    }

    public InputControlOption setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InputControlOption)) return false;

        InputControlOption that = (InputControlOption) o;

        if (selected != that.selected) return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = selected != null ? selected.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "InputControlOption{" +
                "selected=" + selected +
                ", label='" + label + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    /*
     * DeepCloneable
     */

    @Override
    public InputControlOption deepClone() {
        return new InputControlOption(this);
    }
}
