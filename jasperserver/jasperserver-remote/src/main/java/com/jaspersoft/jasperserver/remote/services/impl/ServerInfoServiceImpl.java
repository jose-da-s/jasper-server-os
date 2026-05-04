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

package com.jaspersoft.jasperserver.remote.services.impl;

import com.jaspersoft.jasperserver.api.engine.jasperreports.util.CalendarFormatProvider;
import com.jaspersoft.jasperserver.api.logging.diagnostic.domain.DiagnosticAttribute;
import com.jaspersoft.jasperserver.api.logging.diagnostic.helper.DiagnosticAttributeBuilder;
import com.jaspersoft.jasperserver.api.logging.diagnostic.service.Diagnostic;
import com.jaspersoft.jasperserver.api.logging.diagnostic.service.DiagnosticCallback;
import com.jaspersoft.jasperserver.dto.serverinfo.ServerInfo;
import com.jaspersoft.jasperserver.remote.services.ServerInfoService;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Locale;
import java.util.Map;

/**
 * @author Volodya Sabadosh (vsabadosh@jaspersoft.com)
 * @version $Id $
 */
@Component("serverInfoService")
public class ServerInfoServiceImpl implements ServerInfoService, Diagnostic {

    private final static String JS_VERSION="JS_VERSION"; // No I18N

    private final static String BUILD_DATE_STAMP="BUILD_DATE_STAMP"; // No I18N

    private final static String BUILD_TIME_STAMP="BUILD_TIME_STAMP"; // No I18N

    @Resource
    protected MessageSource messageSource; // Used to get various messages

    @Resource(name = "isoCalendarFormatProvider")
    private CalendarFormatProvider calendarFormatProvider;

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public ServerInfo getServerInfo() {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setVersion(getServerVersion());
        serverInfo.setBuild(getServerBuild());
        // this is not returning the edition name based on license
        // it returns only CE vs PRO, which is really the basic
        // distinction between community vs. commercial bits
        serverInfo.setEdition(getServerEdition());

        serverInfo.setDateFormatPattern(calendarFormatProvider.getDatePattern());
        serverInfo.setDatetimeFormatPattern(calendarFormatProvider.getDatetimePattern());

        return serverInfo;
    }

    protected ServerInfo.ServerEdition getServerEdition() {
        return ServerInfo.ServerEdition.CE;
    }

    protected String getMessageVersionId() {
        return JS_VERSION;
    }

    protected String getServerVersion() {
        return messageSource.getMessage(getMessageVersionId(), new Object[]{}, Locale.getDefault());
    }

    protected String getServerBuild() {
        return messageSource.getMessage(BUILD_DATE_STAMP, new Object[]{}, Locale.getDefault()) +
                "_" + messageSource.getMessage(BUILD_TIME_STAMP, new Object[]{}, Locale.getDefault());
    }

    @Override
    public Map<DiagnosticAttribute, DiagnosticCallback> getDiagnosticData() {
        return new DiagnosticAttributeBuilder()
                .addDiagnosticAttribute(DiagnosticAttributeBuilder.VERSION, new DiagnosticCallback<String>() {
                    @Override
                    public String getDiagnosticAttributeValue() {
                        return getServerInfo().getVersion();
                    }
                })
                .addDiagnosticAttribute(DiagnosticAttributeBuilder.SOURCE_EDITION, new DiagnosticCallback<String>() {
                    @Override
                    public String getDiagnosticAttributeValue() {
                        return getServerInfo().getEdition().toString();
                    }
                })
                .addDiagnosticAttribute(DiagnosticAttributeBuilder.BUILD, new DiagnosticCallback<String>() {
                    @Override
                    public String getDiagnosticAttributeValue() {
                        return getServerInfo().getBuild();
                }
            }).build();
    }
}

