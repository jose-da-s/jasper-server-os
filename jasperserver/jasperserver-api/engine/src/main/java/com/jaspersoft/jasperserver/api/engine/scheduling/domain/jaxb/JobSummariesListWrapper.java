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
package com.jaspersoft.jasperserver.api.engine.scheduling.domain.jaxb;

import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobSummary;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * This class is needed because of bug in JAXB.
 * XmlElementWrapper annotation doesn't support @XmlJavaTypeAdapter
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@XmlRootElement(name = "jobs")
public class JobSummariesListWrapper {

    private List<ReportJobSummary> jobSummaries;

    public JobSummariesListWrapper(){}
    public JobSummariesListWrapper(List<ReportJobSummary> jobSummaries){
        this.jobSummaries = jobSummaries;
    }
    @XmlElement(name = "jobsummary")
    public List<ReportJobSummary> getJobSummaries() {
        return jobSummaries;
    }

    public void setJobSummaries(List<ReportJobSummary> jobSummaries) {
        this.jobSummaries = jobSummaries;
    }

}
