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

import jakarta.xml.bind.annotation.XmlRootElement;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;

/**
 * @author: Zakhar.Tomchenco
 */

@XmlRootElement(name = "report")
public class ClientReportDetails implements DeepCloneable<ClientReportDetails> {
    private String dataSourceUri;
    private String label;
    private String location;
    private String template;

    public ClientReportDetails() {
    }

    public ClientReportDetails(ClientReportDetails other) {
        checkNotNull(other);

        this.dataSourceUri = other.getDataSourceUri();
        this.label = other.getLabel();
        this.location = other.getLocation();
        this.template = other.getTemplate();
    }

    @Override
    public ClientReportDetails deepClone() {
        return new ClientReportDetails(this);
    }

    public String getDataSourceUri() {
        return dataSourceUri;
    }

    public ClientReportDetails setDataSourceUri(String dataSourceUri) {
        this.dataSourceUri = dataSourceUri;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public ClientReportDetails setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public ClientReportDetails setLocation(String location) {
        this.location = location;
        return this;
    }

    public String getTemplate() {
        return template;
    }

    public ClientReportDetails setTemplate(String template) {
        this.template = template;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientReportDetails that = (ClientReportDetails) o;

        if (dataSourceUri != null ? !dataSourceUri.equals(that.dataSourceUri) : that.dataSourceUri != null)
            return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (location != null ? !location.equals(that.location) : that.location != null) return false;
        return template != null ? template.equals(that.template) : that.template == null;
    }

    @Override
    public int hashCode() {
        int result = dataSourceUri != null ? dataSourceUri.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (template != null ? template.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientReportDetails{" +
                "dataSourceUri='" + dataSourceUri + '\'' +
                ", label='" + label + '\'' +
                ", location='" + location + '\'' +
                ", template='" + template + '\'' +
                '}';
    }
}
