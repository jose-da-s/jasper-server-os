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
package com.jaspersoft.jasperserver.remote.settings;


import java.util.HashMap;
import java.util.Map;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * <p></p>
 *
 * @author dhalder
 * @version $Id$
 */
@Service
public class AdhocSettingsProvider  implements SettingsProvider {
  // AdHoc Designer property's prefix
  private final static String DESIGNER_KEY = "adhoc.";
  /*@Resource
  private PropertiesManagementService propertiesManagementService;*/
  @Resource
  private Map<String, Object> adhocSettings;;
  @Override
  public Object getSettings() {
    final Map<String, Object> settings = new HashMap<>();
    settings.putAll(adhocSettings);
   /* settings.put("canViewQuery", propertiesManagementService.getProperty(DESIGNER_KEY+"canViewQuery"));



    settings.put("maxAvailableValues", propertiesManagementService.getProperty(DESIGNER_KEY+"maxAvailableValues"));
    settings.put("maxResultSetRows", propertiesManagementService.getProperty(DESIGNER_KEY+"maxResultSetRows"));
    settings.put("maxExecutionTimeSec", propertiesManagementService.getProperty(DESIGNER_KEY+"maxExecutionTimeSec"));
    settings.put("domainStrategyEnabled", propertiesManagementService.getProperty(DESIGNER_KEY+"domainDataStrategy"));
    settings.put("sqlStrategyEnabled", propertiesManagementService.getProperty(DESIGNER_KEY+"sqlQueryDataStrategy"));
    settings.put("displayNullAsZeroForAggregateValue", propertiesManagementService.getProperty(DESIGNER_KEY+"displayNullAsZeroForAggregateValue"));*/

    return settings;
  }
}

