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
package com.jaspersoft.jasperserver.jaxrs.report;

import com.jaspersoft.jasperserver.api.engine.common.service.ReportExecutionStatusInformation;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.EngineServiceImpl;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Set;

/**
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@XmlRootElement(name = "reportExecutions")
public class ReportExecutionsSetWrapper {

    public ReportExecutionsSetWrapper(){}

    public ReportExecutionsSetWrapper(Set<ReportExecutionStatusInformation> informations) {
        this.informations = informations;
    }

    private Set<ReportExecutionStatusInformation> informations;

    @XmlElement(name = "reportExecution", type = EngineServiceImpl.ReportExecutionStatus.class)
    public Set<ReportExecutionStatusInformation> getInformations() {
        return informations;
    }

    public void setInformations(Set<ReportExecutionStatusInformation> informations) {
        this.informations = informations;
    }
}
