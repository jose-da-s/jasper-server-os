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
package com.jaspersoft.jasperserver.remote.services;

import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.OperationResult;
import com.jaspersoft.jasperserver.remote.ServiceException;
import net.sf.jasperreports.engine.JasperPrint;

import jakarta.activation.DataSource;
import java.util.Map;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id: LegacyRunReportService.java 26611 2012-12-10 14:17:08Z ykovalchyk $
 */
public interface LegacyRunReportService {
    /**
     * @return map of currently available input attachments
     */
    Map<String, DataSource> getInputAttachments();

    /**
     *
     * @return map of currently available ouput attachments
     */
    Map<String, DataSource> getReportAttachments(String reportName);


    /**
     * Return a response.
     * Generated files (one or more) are put in the output attachments map of this context
     *
     * @param reportUnitURI - target report to run URI
     * @param parameters - report parameters
     * @param arguments - report arguments
     * @param outputResourcesContainer - container to put report output resources
     * @return result of operation
     * @throws com.jaspersoft.jasperserver.remote.ServiceException - trown in case of unexpected errors
     */
    OperationResult runReport(String reportUnitURI, Map<String, Object> parameters, Map<String, String> arguments,
            Map<String, DataSource> outputResourcesContainer) throws ServiceException;

    /**
     * Export the report in a specific format using the specified arguments
     * Generated files (one or more) are put in the output attachments map of this context
     *
     * @param reportUnitURI - target report to run URI
     * @param jasperPrint - JasperPring object
     * @param arguments indicates the final file format, starting/ending pages, etc...
     * @param outputResourcesContainer - container to put report output resources
     * @return result of operation
     * @throws ServiceException - trown in case of unexpected errors
     */
    OperationResult exportReport(String reportUnitURI, JasperPrint jasperPrint, Map<String, String> arguments,
            Map<String, DataSource> outputResourcesContainer) throws ServiceException;

    /**
     * @return  map of currently available attributes
     */
    Map<String, Object> getAttributes();
}
