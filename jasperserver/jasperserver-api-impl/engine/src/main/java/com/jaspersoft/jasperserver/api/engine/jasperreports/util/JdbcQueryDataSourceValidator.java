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

package com.jaspersoft.jasperserver.api.engine.jasperreports.util;


import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomReportDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.Errors;

import jakarta.annotation.Resource;
import java.io.File;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ichan
 * Date: 2/12/14
 * Time: 2:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class JdbcQueryDataSourceValidator implements CustomDataSourceValidator {

    private final static Log log = LogFactory.getLog(XlsDataSourceValidator.class);

    public void validatePropertyValues(CustomReportDataSource ds, Errors errors) {
        String query = null;
        String urlField = null;
        String driver = null;
        Map props = ds.getPropertyMap();

        if (props != null) {
            urlField = (String) ds.getPropertyMap().get("url");
            query = (String) ds.getPropertyMap().get("query");
            driver = (String) ds.getPropertyMap().get("driver");
        }

        if (urlField == null || urlField.length() == 0) {
            reject(errors, "url", "Please enter jdbc url");
        } else if (!urlField.toLowerCase().startsWith("jdbc:")) {
            reject(errors, "url", "Invalid jdbc url");
        }

        if (query == null || query.length() == 0) {
            reject(errors, "query", "Please enter query");
        } else if (!query.toLowerCase().startsWith("select")) {
            reject(errors, "query", "Invalid jdbc query");
        }

        if (driver == null || driver.length() == 0) {
            reject(errors, "driver", "Please enter jdbc driver");
        }
    }

    // first arg is the path of the property which has the error
    // for custom DS's this will always be in the form "reportDataSource.propertyMap[yourPropName]"
    protected void reject(Errors errors, String name, String reason) {
        if (errors != null) errors.rejectValue("jdbcQueryDataSource.propertyMap[" + name + "]",  reason);
        else log.debug("jdbcQueryDataSource.propertyMap[" + name + "] - " + reason);
    }


}

