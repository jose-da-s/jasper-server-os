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
package com.jaspersoft.jasperserver.dto.importexport;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.common.WarningDescriptor;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author: Zakhar.Tomchenco
 */
@XmlRootElement(name = "state")
public class State implements DeepCloneable<State> {

    String id;
    String message, phase;
    List<WarningDescriptor> warnings;
    ErrorDescriptor error;

    public State() {

    }

    public State(State other) {
        checkNotNull(other);

        id = other.getId();
        message = other.getMessage();
        phase = other.getPhase();
        warnings = copyOf(other.getWarnings());
        error = copyOf(other.getError());
    }

    @XmlElement(name = "id")
    public synchronized String getId() {
        return id;
    }

    public synchronized State setId(String id) {
        this.id = id;
        return this;
    }

    @XmlElement(name = "phase")
    public synchronized String getPhase() {
        return phase;
    }

    public synchronized State setPhase(String phase) {
        this.phase = phase;
        return this;
    }

    @XmlElement(name = "message")
    public synchronized String getMessage() {
        return message;
    }

    public synchronized State setMessage(String message) {
        this.message = message;
        return this;
    }

    public synchronized State setWarnings(List<WarningDescriptor> warnings) {
        this.warnings = warnings;
        return this;
    }

    @XmlElement(name = "warnings")
    public synchronized List<WarningDescriptor> getWarnings() {
        if (warnings != null && warnings.size() > 0) {
            return warnings;
        }
        return null;
    }

    @XmlElement(name = "error")
    public synchronized ErrorDescriptor getError() {
        if (error == null) {
            return null;
        } else {
            ErrorDescriptor errorDescriptor = new ErrorDescriptor(this.error);
            return errorDescriptor;
        }
    }

    public synchronized State setError(ErrorDescriptor error) {
        if (error == null) {
            this.error = null;
        } else {
            this.error = new ErrorDescriptor(error);
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;

        State state = (State) o;

        if (getId() != null ? !getId().equals(state.getId()) : state.getId() != null) return false;
        if (getMessage() != null ? !getMessage().equals(state.getMessage()) : state.getMessage() != null) return false;
        if (getPhase() != null ? !getPhase().equals(state.getPhase()) : state.getPhase() != null) return false;
        if (getWarnings() != null ? !getWarnings().equals(state.getWarnings()) : state.getWarnings() != null)
            return false;
        return !(getError() != null ? !getError().equals(state.getError()) : state.getError() != null);

    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getMessage() != null ? getMessage().hashCode() : 0);
        result = 31 * result + (getPhase() != null ? getPhase().hashCode() : 0);
        result = 31 * result + (getWarnings() != null ? getWarnings().hashCode() : 0);
        result = 31 * result + (getError() != null ? getError().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "State{" +
                "id='" + id + '\'' +
                ", message='" + message + '\'' +
                ", phase='" + phase + '\'' +
                ", warnings=" + warnings +
                ", error=" + error +
                '}';
    }

    /*
     * DeepCloneable
     */

    @Override
    public State deepClone() {
        return new State(this);
    }
}
