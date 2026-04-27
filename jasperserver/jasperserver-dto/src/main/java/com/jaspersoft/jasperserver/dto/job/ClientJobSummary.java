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

package com.jaspersoft.jasperserver.dto.job;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p/>
 * <p/>
 *
 * @author tetiana.iefimenko
 * @version $Id$
 * @see
 */
@XmlRootElement(name = "jobsummary")
public class ClientJobSummary implements DeepCloneable<ClientJobSummary>{

    private Long id;
    private Long version;
    private String label;
    private String reportUnitURI;
    private String reportLabel;
    private String description;
    private String username;
    private ClientJobState state;

    public ClientJobSummary() {
    }

    public ClientJobSummary(ClientJobSummary other) {
        checkNotNull(other);

        this.id = other.getId();
        this.version = other.getVersion();
        this.label = other.getLabel();
        this.reportUnitURI = other.getReportUnitURI();
        this.reportLabel = other.getReportLabel();
        this.description = other.getDescription();
        this.username = other.getUsername();
        this.state = copyOf(other.getState());
    }

    public Long getId() {
        return id;
    }

    public ClientJobSummary setId(Long id) {
        this.id = id;
        return this;
    }

    @XmlElement(name = "owner")
    public String getUsername() {
        return username;
    }

    public ClientJobSummary setUsername(String username) {
        this.username = username;
        return this;
    }
    public String getLabel() {
        return label;
    }

    public ClientJobSummary setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getReportUnitURI() {
        return reportUnitURI;
    }

    public ClientJobSummary setReportUnitURI(String reportUnitURI) {
        this.reportUnitURI = reportUnitURI;
        return this;
    }

    public ClientJobState getState() {
        return state;
    }

    public ClientJobSummary setState(ClientJobState state) {
        this.state = state;
        return this;
    }

    public Long getVersion() {
        return version;
    }

    public ClientJobSummary setVersion(Long version) {
        this.version = version;
        return this;
    }


    public String getDescription() {
        return description;
    }

    public ClientJobSummary setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getReportLabel() {
        return reportLabel;
    }

    public ClientJobSummary setReportLabel(String reportLabel) {
        this.reportLabel = reportLabel;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientJobSummary)) return false;

        ClientJobSummary that = (ClientJobSummary) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (reportUnitURI != null ? !reportUnitURI.equals(that.reportUnitURI) : that.reportUnitURI != null)
            return false;
        if (reportLabel != null ? !reportLabel.equals(that.reportLabel) : that.reportLabel != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;
        if (state != null ? !state.equals(that.state) : that.state != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (reportUnitURI != null ? reportUnitURI.hashCode() : 0);
        result = 31 * result + (reportLabel != null ? reportLabel.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientJobSummary{" +
                "id=" + id +
                ", version=" + version +
                ", label='" + label + '\'' +
                ", reportUnitURI='" + reportUnitURI + '\'' +
                ", reportLabel='" + reportLabel + '\'' +
                ", description='" + description + '\'' +
                ", username='" + username + '\'' +
                ", state=" + state +
                '}';
    }

    @Override
    public ClientJobSummary deepClone() {
        return new ClientJobSummary(this);
    }
}
