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

package com.jaspersoft.jasperserver.remote.resources.validation;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.service.JdbcDriverService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.JdbcReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributesResolver;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.exception.MandatoryParameterNotFoundException;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

import java.util.List;
import java.util.Map;

import static com.jaspersoft.jasperserver.remote.resources.validation.ValidationHelper.empty;

/**
 * <p></p>
 *
 * @author Zakhar.Tomchenco
 * @version $Id$
 */
@Component
public class JdbcDataSourceResourceValidator extends GenericResourceValidator<JdbcReportDataSource> {
    @Resource
    private JdbcDriverService jdbcDriverService;
    @Resource
    private ProfileAttributesResolver profileAttributesResolver;

    @Override
    protected void internalValidate(ExecutionContext ctx, JdbcReportDataSource resource, List<Exception> errors, Map<String, String[]> additionalParameters) {
        if (empty(resource.getDriverClass())) {
            errors.add(new MandatoryParameterNotFoundException("DriverClass"));
        } else {
            String driverClass = resource.getDriverClass();
            if (!profileAttributesResolver.containsAttribute(driverClass) &&
                    !jdbcDriverService.isRegistered(driverClass)) {
                errors.add(new IllegalParameterValueException("Specified driver class is not registered", "DriverClass", resource.getDriverClass()));
            }
        }
        if (empty(resource.getConnectionUrl())) {
            errors.add(new MandatoryParameterNotFoundException("ConnectionUrl"));
        } else {
            String url = resource.getConnectionUrl();
            if (!profileAttributesResolver.containsAttribute(url) && !url.trim().contains("jdbc:")) {
                errors.add(new IllegalParameterValueException("The JDBC URI must start with 'jdbc:'", "ConnectionUrl", resource.getConnectionUrl()));
            }
        }
        if (!empty(resource.getTimezone()) && !resource.getTimezone().matches("^[a-zA-Z/_]+$")) {
            errors.add(new IllegalParameterValueException("The timezone value contains not permitted characters", "Timezone", resource.getTimezone()));
        }
    }
}

