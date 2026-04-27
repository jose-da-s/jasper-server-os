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
package com.jaspersoft.jasperserver.remote.services.impl;

import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.ReportUnitResult;
import com.jaspersoft.jasperserver.remote.exception.ResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.services.RunReportService;
import com.jaspersoft.jasperserver.api.engine.common.service.GlobalReportExecutionAccessor;
import net.sf.jasperreports.engine.JasperReportsContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@Service("reportExecutionAccessor")
public class ReportExecutionAccessorIml implements GlobalReportExecutionAccessor {
    private final static Log log = LogFactory.getLog(ReportExecutionAccessorIml.class);
    @Resource(name = "runReportService")
    private RunReportService runReportService;

    @Override
    public ReportUnitResult getReportResult(String executionId) {
        ReportUnitResult result = null;
        try {
            result = runReportService.getReportExecution(executionId).getFinalReportUnitResult();
        } catch (ResourceNotFoundException e) {
            // just log for now. No executions found.
            log.error(e);
        }
        return result;
    }

    @Override
    public JasperReportsContext getJasperReportsContext(String executionId) {
        try {
            return runReportService.getReportExecution(executionId).getOptions().getJasperReportsContext();
        } catch (ResourceNotFoundException e) {
            // just log for now. No executions found.
            log.error(e);
        }
        return null;
    }

    @Override
    public void refreshOutput(String executionId) {
        try {
            runReportService.startReportExecution(runReportService.getReportExecution(executionId));
        } catch (Exception e) {
            // just log for now. No executions found.
            log.error(e);
        }
    }

}

