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

package com.jaspersoft.jasperserver.dto.dashboard;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;
import com.jaspersoft.jasperserver.dto.reports.ReportParameter;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p>DTO for dashboard execution parameters</p>
 *
 * @author Zakhar Tomchenko
 * @version $Id: $
 */
@XmlRootElement
public class DashboardParameters implements DeepCloneable<DashboardParameters> {

    private List<ReportParameter> dashboardParameters;

    public DashboardParameters() { }

    public DashboardParameters(List<ReportParameter> dashboardParameters) {
        this.dashboardParameters = dashboardParameters;
    }

    public DashboardParameters(DashboardParameters other) {
        checkNotNull(other);

        dashboardParameters = copyOf(other.getDashboardParameters());
    }

    @XmlElement(name = "dashboardParameter")
    public List<ReportParameter> getDashboardParameters() {
        return dashboardParameters;
    }

    public DashboardParameters setDashboardParameters(List<ReportParameter> dashboardParameters) {
        this.dashboardParameters = dashboardParameters;
        return this;
    }

    @XmlTransient
    public Map<String, String[]> getRawParameters(){
        Map<String, String[]> rawParameters = new HashMap<String, String[]>();
        if(dashboardParameters != null){
            for(ReportParameter currentParameter : dashboardParameters){
                rawParameters.put(
                        currentParameter.getName(),
                        currentParameter.getValues().toArray(new String[currentParameter.getValues().size()])
                );
            }
        }
        return rawParameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DashboardParameters that = (DashboardParameters) o;

        return dashboardParameters != null ? dashboardParameters.equals(that.dashboardParameters) : that.dashboardParameters == null;
    }

    @Override
    public int hashCode() {
        return dashboardParameters != null ? dashboardParameters.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "DashboardParameters{" +
                "dashboardParameters=" + dashboardParameters +
                '}';
    }

    /*
     * DeepCloneable
     */

    @Override
    public DashboardParameters deepClone() {
        return new DashboardParameters(this);
    }
}
