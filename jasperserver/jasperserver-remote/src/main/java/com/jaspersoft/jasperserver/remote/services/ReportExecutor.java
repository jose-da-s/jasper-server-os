/*
 * Copyright (C) 2025 the Jasper Server OS Authors
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
package com.jaspersoft.jasperserver.remote.services;

import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.PaginationParameters;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.ReportUnitResult;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.remote.ServiceException;
import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.export.ExporterInputItem;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id: ReportExecutor.java 26599 2012-12-10 13:04:23Z ykovalchyk $
 */
public interface ReportExecutor {
    String getContentType(String outputFormat);
    boolean isRunnableResource(Resource resource);
    ReportUnitResult runReport(String reportUnitUri, Map<String, Object> parameters,
            ReportExecutionOptions reportExecutionOptions) throws ErrorDescriptorException;
    void exportReport(String reportUnitURI, List<ExporterInputItem> inputItems, String format, OutputStream output,
                                                      Map<?, ?> exportParameters) throws ServiceException;
    JasperReportsContext getJasperReportsContext(Boolean interactive);
	PaginationParameters getExportPaginationParameters(String reportUnitURI, JasperPrint jasperPrint, String outputFormat);
}
