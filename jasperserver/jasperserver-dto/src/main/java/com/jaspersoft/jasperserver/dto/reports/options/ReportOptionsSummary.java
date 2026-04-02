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
package com.jaspersoft.jasperserver.dto.reports.options;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlRootElement;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;

/**
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@XmlRootElement
public class ReportOptionsSummary implements DeepCloneable<ReportOptionsSummary> {
    private String uri;
    private String id;
    private String label;

    public ReportOptionsSummary() { }

    public ReportOptionsSummary(ReportOptionsSummary other) {
        checkNotNull(other);

        this.uri = other.uri;
        this.id = other.id;
        this.label = other.label;
    }

    public String getUri() {
        return uri;
    }

    public ReportOptionsSummary setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public String getId() {
        return id;
    }

    public ReportOptionsSummary setId(String id) {
        this.id = id;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public ReportOptionsSummary setLabel(String label) {
        this.label = label;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReportOptionsSummary)) return false;

        ReportOptionsSummary that = (ReportOptionsSummary) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uri != null ? uri.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (label != null ? label.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ReportOptionsSummary{" +
                "uri='" + uri + '\'' +
                ", id='" + id + '\'' +
                ", label='" + label + '\'' +
                '}';
    }

    /*
     * DeepCloneable
     */

    @Override
    public ReportOptionsSummary deepClone() {
        return new ReportOptionsSummary(this);
    }
}
