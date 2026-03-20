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

import com.jaspersoft.jasperserver.api.engine.jasperreports.util.CustomDataSourceValidator;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomReportDataSource;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.Errors;

import jakarta.annotation.Resource;
import java.util.Map;

/**
 * 
 * @author Eric Diaz
 * 
 */
public class MongoDbDataSourceValidator implements CustomDataSourceValidator {
    @Resource(name= "messageSource")
    private MessageSource messageSource;

    final static String ERRORCODE_MONGOURI_REQUIRED = "MongoDbDataSource.mongoURI.required";

    @Override
    public void validatePropertyValues(CustomReportDataSource customReportDataSource, Errors errors) {
      Map<?, ?> propertyMap = customReportDataSource.getPropertyMap();
      String mongoURI = (String) propertyMap.get("mongoURI");
      String message = messageSource.getMessage(ERRORCODE_MONGOURI_REQUIRED, null, LocaleContextHolder.getLocale());
    if (mongoURI == null || mongoURI.length() == 0) {
      errors.rejectValue("reportDataSource.propertyMap[mongoURI]", ERRORCODE_MONGOURI_REQUIRED, message);
    }
  }
}

