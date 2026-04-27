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
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.AwsReportDataSource;
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
public class AwsDataSourceResourceValidator extends GenericResourceValidator<AwsReportDataSource> {
    @Resource
    private JdbcDriverService jdbcDriverService;
    @Resource
    private ProfileAttributesResolver profileAttributesResolver;
    @Resource
    private List<String> awsRegions;

    @Override
    protected void internalValidate(ExecutionContext ctx, AwsReportDataSource resource, List<Exception> errors, Map<String, String[]> additionalParameters) {
        if (empty(resource.getAWSAccessKey()) && !empty(resource.getAWSSecretKey())){
            errors.add(new MandatoryParameterNotFoundException("AccessKey"));
        }
        if (empty(resource.getAWSSecretKey()) && !empty(resource.getAWSAccessKey())) {
            errors.add(new MandatoryParameterNotFoundException("SecretKey"));
        }
        if (empty(resource.getDbName())) {
            errors.add(new MandatoryParameterNotFoundException("DBName"));
        }
        if (empty(resource.getConnectionUrl())) {
            errors.add(new MandatoryParameterNotFoundException("ConnectionUrl"));
        } else {
            String url = resource.getConnectionUrl();
            if (!profileAttributesResolver.containsAttribute(url) && !url.trim().startsWith("jdbc:")) {
                errors.add(new IllegalParameterValueException("The JDBC URI must start with 'jdbc:'", "ConnectionUrl", url));
            }
        }
        String driverClass = resource.getDriverClass();
        if (empty(driverClass)) {
            errors.add(new MandatoryParameterNotFoundException("DriverClass"));
        } else if (!profileAttributesResolver.containsAttribute(driverClass) && !jdbcDriverService.isRegistered(driverClass)) {
            errors.add(new IllegalParameterValueException("Specified driver class is not registered", "DriverClass", driverClass));
        }
        if (empty(resource.getUsername())) {
            errors.add(new MandatoryParameterNotFoundException("Username"));
        }
        if (empty(resource.getAWSRegion())) {
            errors.add(new MandatoryParameterNotFoundException("Region"));
        } else {
            String awsRegion = resource.getAWSRegion();
            if (!profileAttributesResolver.containsAttribute(awsRegion) && !awsRegions.contains(awsRegion)) {
                StringBuilder message = new StringBuilder("Invalid value for the field Region. Valid values:");
                for (String region : awsRegions) {
                    message.append("\n").append(region);
                }
                errors.add(new IllegalParameterValueException(message.toString(), "Region", awsRegion));
            }
        }
        if (!empty(resource.getTimezone()) && !resource.getTimezone().matches("^[a-zA-Z/_]+$")) {
            errors.add(new IllegalParameterValueException("The timezone value contains not permitted characters", "Timezone", resource.getTimezone()));
        }
    }
}

