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

import com.jaspersoft.jasperserver.api.security.externalAuth.ExternalAuthProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.HashMap;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@Service
public class AuthSettingsProvider implements SettingsProvider, InitializingBean {
    @Resource
    private ApplicationContext applicationContext;
    private String ticketParameterName;
    @Override
    public Object getSettings() {
        return new HashMap<String, String>(){{put("ticketParameterName", ticketParameterName);}};
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            ticketParameterName = applicationContext.getBean(ExternalAuthProperties.class).getTicketParameterName();
        }catch (Exception e){
            // Seems no SSO configured
            // let's use default ticket parameter name
            ticketParameterName = "ticket";
        }
    }
}

