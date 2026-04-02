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
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * InputControlState
 * @author akasych
 * @version $Id$
 * Simple class to transfer Input Control value.
 */

@XmlRootElement
public class InputControlState implements Serializable, DeepCloneable<InputControlState> {

    private final static long serialVersionUID = 1l;

    private String uri;
    private String id;
    private String value;
    private String error;
    private String totalCount;
    private List<InputControlOption> options;

    public InputControlState(){
    }

    public InputControlState(InputControlState other) {
        checkNotNull(other);

        this.uri = other.getUri();
        this.id = other.getId();
        this.value = other.getValue();
        this.error = other.getError();
        this.totalCount = other.getTotalCount();
        this.options = copyOf(other.getOptions());
    }

    public String getId() {
        return id;
    }

    public InputControlState setId(String id) {
        this.id = id;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public InputControlState setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public String getValue() {
        return value;
    }

    public InputControlState setValue(String value) {
        this.value = value;
        return this;
    }

    public String getError() {
        return error;
    }

    public InputControlState setError(String error) {
        this.error = error;
        return this;
    }

    public String getTotalCount() {
        return totalCount;
    }

    public InputControlState setTotalCount(String totalCount) {
        this.totalCount = totalCount;
        return this;
    }

    @XmlElementWrapper(name = "options")
    @XmlElement(name = "option")
    public List<InputControlOption> getOptions() {
        return options;
    }

    public InputControlState setOptions(List<InputControlOption> options) {
        this.options = options;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InputControlState)) return false;

        InputControlState that = (InputControlState) o;

        if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (error != null ? !error.equals(that.error) : that.error != null) return false;
        if (totalCount != null ? !totalCount.equals(that.totalCount) : that.totalCount != null) return false;
        return options != null ? options.equals(that.options) : that.options == null;
    }

    @Override
    public int hashCode() {
        int result = uri != null ? uri.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (error != null ? error.hashCode() : 0);
        result = 31 * result + (totalCount != null ? totalCount.hashCode() : 0);
        result = 31 * result + (options != null ? options.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "InputControlState{" +
                "uri='" + uri + '\'' +
                ", id='" + id + '\'' +
                ", value='" + value + '\'' +
                ", error='" + error + '\'' +
                ", totalCount='" + totalCount + '\'' +
                ", options=" + options +
                '}';
    }

    /*
     * DeepCloneable
     */

    @Override
    public InputControlState deepClone() {
        return new InputControlState(this);
    }
}




