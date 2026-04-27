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

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@XmlRootElement
public class ReportOptionsSummaryList implements DeepCloneable<ReportOptionsSummaryList> {

    private List<ReportOptionsSummary> summaryList;

    public ReportOptionsSummaryList(){}

    public ReportOptionsSummaryList(List<ReportOptionsSummary> summaryList){
        this.summaryList = summaryList;
    }

    public ReportOptionsSummaryList(ReportOptionsSummaryList other) {
        checkNotNull(other);

        this.summaryList = copyOf(other.getSummaryList());
    }

    @XmlElement(name = "reportOptionsSummary")
    public List<ReportOptionsSummary> getSummaryList() {
        return summaryList;
    }

    public ReportOptionsSummaryList setSummaryList(List<ReportOptionsSummary> summaryList) {
        this.summaryList = summaryList;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReportOptionsSummaryList)) return false;

        ReportOptionsSummaryList that = (ReportOptionsSummaryList) o;

        if (summaryList != null ? !summaryList.equals(that.summaryList) : that.summaryList != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return summaryList != null ? summaryList.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ReportOptionsSummaryList{" +
                "summaryList=" + summaryList +
                '}';
    }

    /*
     * DeeCloneable
     */

    @Override
    public ReportOptionsSummaryList deepClone() {
        return new ReportOptionsSummaryList(this);
    }
}
