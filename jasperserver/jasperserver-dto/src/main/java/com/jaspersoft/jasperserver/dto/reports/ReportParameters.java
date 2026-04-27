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
package com.jaspersoft.jasperserver.dto.reports;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@XmlRootElement
public class ReportParameters implements DeepCloneable<ReportParameters> {
    private List<ReportParameter> reportParameters;
    public static final String SELECT = "_select";
    public static final String LIMIT = "_limit";
    public static final String OFFSET = "_offset";
    public static final String CRITERIA = "_criteria";

    public ReportParameters() { }

    public ReportParameters(List<ReportParameter> reportParameters) {
        this.reportParameters = reportParameters;
    }

    public ReportParameters(ReportParameters other) {
        checkNotNull(other);

        reportParameters = copyOf(other.getReportParameters());
    }

    @XmlElement(name = "reportParameter")
    @ArraySchema(schema = @Schema(name = "reportParameter", description = "The list of report parameters.", implementation = ReportParameter.class))
    public List<ReportParameter> getReportParameters() {
        return reportParameters;
    }

    public ReportParameters setReportParameters(List<ReportParameter> reportParameters) {
        this.reportParameters = reportParameters;
        return this;
    }

    // TODO: should this be serializable?
    @Schema(
			description = "The raw parameters.", 
			hidden = true
			)
    public Map<String, String[]> getRawParameters(){

        Map<String, String[]> rawParameters = new HashMap<>();

        if (reportParameters != null) {
            for (ReportParameter currentParameter : reportParameters) {
                if (currentParameter.getValues().size() > 0) {
                    rawParameters.put(
                            currentParameter.getName(),
                            currentParameter.getValues().toArray(new String[currentParameter.getValues().size()])
                    );
                }
                if (currentParameter.getSelect() != null) {
                    rawParameters.put(
                            currentParameter.getName() + SELECT,
                            new String[]{currentParameter.getSelect()}
                    );
                }
                if (currentParameter.getLimit() != null) {
                    rawParameters.put(
                            currentParameter.getName() + LIMIT,
                            new String[]{currentParameter.getLimit()}
                    );
                }
                if (currentParameter.getOffset() != null) {
                    rawParameters.put(
                            currentParameter.getName() + OFFSET,
                            new String[]{currentParameter.getOffset()}
                    );
                }
                if (currentParameter.getCriteria() != null) {
                    rawParameters.put(
                            currentParameter.getName() + CRITERIA,
                            new String[]{currentParameter.getCriteria()}
                    );
                }
            }
        }
        return rawParameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReportParameters that = (ReportParameters) o;

        return reportParameters != null ? reportParameters.equals(that.reportParameters) : that.reportParameters == null;
    }

    @Override
    public int hashCode() {
        return reportParameters != null ? reportParameters.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ReportParameters{" +
                "reportParameters=" + reportParameters +
                '}';
    }

    /*
     * DeepCloneable
     */

    @Override
    public ReportParameters deepClone() {
        return new ReportParameters(this);
    }
}
