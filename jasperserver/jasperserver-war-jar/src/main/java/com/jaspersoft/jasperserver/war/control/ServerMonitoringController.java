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
package com.jaspersoft.jasperserver.war.control;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.jaspersoft.jasperserver.api.common.properties.Log4jSettingsService;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.ModelAndView;

import org.springframework.web.servlet.mvc.Controller;
import com.jaspersoft.jasperserver.api.engine.common.service.impl.NavigationActionModelSupport;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class ServerMonitoringController implements Controller {

    private NavigationActionModelSupport navigationActionModelSupport;

    public void setNavigationActionModelSupport(NavigationActionModelSupport navigationActionModelSupport) {
            this.navigationActionModelSupport = navigationActionModelSupport;
        }

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        ModelAndView mav = null;
        if (this.navigationActionModelSupport.isAvailableAdminConsole()) {
         mav = new ModelAndView("modules/serverMonitoring/adminCockpit");
         } else {
         mav = new ModelAndView("modules/system/404");
         }
        return mav;
    }

}


