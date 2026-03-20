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
package com.jaspersoft.jasperserver.remote.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaspersoft.jasperserver.api.logging.audit.context.AuditContext;
import com.jaspersoft.jasperserver.api.logging.audit.domain.AuditEvent;
import com.jaspersoft.jasperserver.api.logging.audit.domain.AuditEventType;
import com.jaspersoft.jasperserver.remote.helpers.JacksonMapperProvider;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.jaspersoft.jasperserver.api.logging.audit.domain.AuditEventType.*;

/**
 *
 * @author gtoffoli
 * @version $Id$
 */
@Component
public class AuditHelper {
    @Resource(name = "concreteAuditContext")
    private AuditContext auditContext;

    public void setAuditContext(AuditContext auditContext) {
        this.auditContext = auditContext;
    }

    public void createAuditEvent(final String auditEventType) {
        auditContext.doInAuditContext(
                new AuditContext.AuditContextCallback() {
                    public void execute() {
                        auditContext.createAuditEvent(auditEventType);
                    }
                });
    }

    public void closeAuditEvent(String jobType) {
        auditContext.doInAuditContext(jobType, new AuditContext.AuditContextCallbackWithEvent() {
            public void execute(AuditEvent auditEvent) {
                auditContext.closeAuditEvent(auditEvent);
            }
        });
    }

    public void addPropertyToAuditEvent(final String auditEventType, final String propertyName, final Object property) {
        auditContext.doInAuditContext(auditEventType,
                new AuditContext.AuditContextCallbackWithEvent() {
                    public void execute(AuditEvent auditEvent) {
                        auditContext.addPropertyToAuditEvent(propertyName, property, auditEvent);
                    }
                });
    }

    public void addExceptionToAllAuditEvents(final Throwable exception) {
        auditContext.doInAuditContext(
                new String[]{SAVE_RESOURCE.toString(), UPDATE_RESOURCE.toString(), DELETE_RESOURCE.toString(), COPY_RESOURCE.toString(), MOVE_RESOURCE.toString(),SAVE_RESOURCE.toString(),
                        CREATE_FOLDER.toString(), UPDATE_FOLDER.toString(), DELETE_FOLDER.toString(), COPY_FOLDER.toString(), MOVE_FOLDER.toString(),
                        RUN_REPORT.toString()},
                new AuditContext.AuditContextCallbackWithEvent() {
                    public void execute(AuditEvent auditEvent) {
                        auditContext.addPropertyToAuditEvent("exception", exception, auditEvent);
                    }
                });
    }



    public void createAuditEvent(String operation, String wsType, boolean isNew) {
        if ("copy".equals(operation)) {
            if ("folder".equals(wsType)) {
                createAuditEvent(COPY_FOLDER.toString());
            } else {
                createAuditEvent(COPY_RESOURCE.toString());
            }
        } else if ("move".equals(operation)) {
            if ("folder".equals(wsType)) {
                createAuditEvent(MOVE_FOLDER.toString());
            } else {
                createAuditEvent(MOVE_RESOURCE.toString());
            }
        } else if ("delete".equals(operation)) {
            if ("folder".equals(wsType)) {
                createAuditEvent(DELETE_FOLDER.toString());
            } else {
                createAuditEvent(DELETE_RESOURCE.toString());
            }
        } else if ("put".equals(operation)) {
            if ("folder".equals(wsType)) {
                if (isNew) {
                    createAuditEvent(CREATE_FOLDER.toString());
                } else {
                    createAuditEvent(UPDATE_FOLDER.toString());
                }
            } else {
                if (isNew) {
                    createAuditEvent(SAVE_RESOURCE.toString());
                } else {
                    createAuditEvent(UPDATE_RESOURCE.toString());
                }
            }
        } else if ("get".equals(operation)) {
            if (!"folder".equals(wsType)) {
                createAuditEvent(ACCESS_RESOURCE.toString());
            }
        }
    }

    public static String convertObjectToJSON(Object obj) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectMapper mapper = JacksonMapperProvider.getObjectMapper();
        mapper.writeValue(stream, obj);
        return stream.toString(StandardCharsets.UTF_8.name());
    }
}

