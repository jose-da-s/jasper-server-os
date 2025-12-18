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

package com.jaspersoft.jasperserver.remote;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.engine.common.service.EngineService;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.PaginationParameters;

import net.sf.jasperreports.engine.JRPropertiesHolder;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.export.ExporterInputItem;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author sanda zaharia (shertage@users.sourceforge.net)
 * @@version $Id: WSExporter.java 19933 2010-12-11 15:27:37Z tmatyashovsky $
 */
public interface ReportExporter extends Serializable {
	
	void exportReport(
            JasperPrint jasperPrint,
            OutputStream output,
            EngineService engineService,
            Map<?, ?> exportParameters,
            ExecutionContext executionContext,
            String reportUnitURI
    ) throws Exception;
	
	default void exportReport(
            List<ExporterInputItem> inputItems,
            OutputStream output,
            EngineService engineService,
            Map<?, ?> exportParameters,
            ExecutionContext executionContext,
            String reportUnitURI
    ) throws Exception {
		if (inputItems.size() != 1) {
			throw new UnsupportedOperationException();
		}
		exportReport(inputItems.get(0).getJasperPrint(), output, engineService,
				exportParameters, executionContext, reportUnitURI);
	}

    String getContentType();
    
	PaginationParameters getPaginationParameters(JRPropertiesHolder propertiesHolder);

}
