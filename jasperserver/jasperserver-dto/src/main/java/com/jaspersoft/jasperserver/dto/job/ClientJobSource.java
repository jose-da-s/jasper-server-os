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
import com.jaspersoft.jasperserver.dto.job.adapters.ReportJobSourceParametersXmlAdapter;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Map;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.hashCodeOfMapWithArraysAsValues;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.isMapsWithArraysAsValuesEquals;

/**
 * <p/>
 * <p/>
 *
 * @author tetiana.iefimenko
 * @version $Id$
 * @see
 */
@XmlRootElement(name = "source")
public class ClientJobSource implements DeepCloneable<ClientJobSource> {

    private String reportUnitURI;
    private Map<String, String[]> parameters;
    private Integer referenceHeight, referenceWidth;

    public ClientJobSource() {
    }

    public ClientJobSource(ClientJobSource other) {
        checkNotNull(other);

        this.reportUnitURI = other.getReportUnitURI();
        this.referenceHeight = other.getReferenceHeight();
        this.referenceWidth = other.getReferenceWidth();
        this.parameters = copyOf(other.getParameters());
    }

    public String getReportUnitURI() {
        return reportUnitURI;
    }

    public ClientJobSource setReportUnitURI(String reportUnitURI) {
        this.reportUnitURI = reportUnitURI;
        return this;
    }

    public Integer getReferenceHeight() {
        return referenceHeight;
    }

    public ClientJobSource setReferenceHeight(Integer referenceHeight) {
        this.referenceHeight = referenceHeight;
        return this;
    }

    public Integer getReferenceWidth() {
        return referenceWidth;
    }

    public ClientJobSource setReferenceWidth(Integer referenceWidth) {
        this.referenceWidth = referenceWidth;
        return this;
    }

    @XmlJavaTypeAdapter(ReportJobSourceParametersXmlAdapter.class)
    public Map<String, String[]> getParameters() {
        return parameters;
    }

    public ClientJobSource setParameters(Map<String, String[]> parameters) {
        this.parameters = parameters;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientJobSource)) return false;

        ClientJobSource source = (ClientJobSource) o;

        if (!isMapsWithArraysAsValuesEquals(parameters, source.parameters))
            return false;
        if (reportUnitURI != null ? !reportUnitURI.equals(source.reportUnitURI) : source.reportUnitURI != null)
            return false;
        if (referenceHeight != null ? !referenceHeight.equals(source.referenceHeight) : source.referenceHeight != null)
            return false;
        if (referenceWidth != null ? !referenceWidth.equals(source.referenceWidth) : source.referenceWidth != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = reportUnitURI != null ? reportUnitURI.hashCode() : 0;
        result = 31 * result + hashCodeOfMapWithArraysAsValues(parameters);
        result = 31 * result + (referenceHeight != null ? referenceHeight.hashCode() : 0);
        result = 31 * result + (referenceWidth != null ? referenceWidth.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientJobSource{" +
                "reportUnitURI='" + reportUnitURI + '\'' +
                ", parameters=" + parameters +
                '}';
    }

    @Override
    public ClientJobSource deepClone() {
        return new ClientJobSource(this);
    }
}
