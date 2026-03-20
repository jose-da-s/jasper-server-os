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
package com.jaspersoft.jasperserver.api.logging.service.impl;

import com.jaspersoft.jasperserver.api.logging.context.LoggableEvent;
import com.jaspersoft.jasperserver.api.logging.service.LoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

/**
 * @author Sergey Prilukin
 * @version $Id$
 */
public class LoggingFacade implements LoggingService {

    @Resource(name="${bean.routingMap}")
    private Map<Class<? extends LoggableEvent>, LoggingService> routingMap;
    private ExecutorService asyncExecutor;

    public void setAsyncExecutor(ExecutorService asyncExecutor) {
        this.asyncExecutor = asyncExecutor;
    }

    public void saveEvent(LoggableEvent loggableEvent) {
        for (Class<? extends LoggableEvent> clazz: routingMap.keySet()) {
            if (clazz.isAssignableFrom(loggableEvent.getClass())) {
                LoggingService loggingService = routingMap.get(clazz);
                if (loggingService != null) {
                    if (asyncExecutor != null) {
                        asyncExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                loggingService.saveEvent(loggableEvent);
                            }
                        });
                    } else {
                        loggingService.saveEvent(loggableEvent);
                    }
                }
            }
        }
    }

    private Map<Class<? extends LoggableEvent>, List<LoggableEvent>> geSplittedEventsMap(List<LoggableEvent> events) {
        Map<Class<? extends LoggableEvent>, List<LoggableEvent>> eventsByTypeMap =
                new HashMap<Class<? extends LoggableEvent>, List<LoggableEvent>>();

        for (LoggableEvent loggableEvent : events) {
            for (Class<? extends LoggableEvent> clazz: routingMap.keySet()) {
                if (clazz.isAssignableFrom(loggableEvent.getClass())) {
                    if (!eventsByTypeMap.containsKey(clazz)) {
                        eventsByTypeMap.put(clazz, new ArrayList<LoggableEvent>());
                    }

                    eventsByTypeMap.get(clazz).add(loggableEvent);
                }
            }
        }

        return eventsByTypeMap;
    }

    public void saveEvents(List<LoggableEvent> loggableEvents) {
        Map<Class<? extends LoggableEvent>, List<LoggableEvent>> eventsByTypeMap = geSplittedEventsMap(loggableEvents);
        for (Class<? extends LoggableEvent> clazz: eventsByTypeMap.keySet()) {
            LoggingService loggingService = routingMap.get(clazz);
            if (loggingService != null) {
                if (asyncExecutor != null) {
                    asyncExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            loggingService.saveEvents(eventsByTypeMap.get(clazz));
                        }
                    });
                } else {
                    loggingService.saveEvents(eventsByTypeMap.get(clazz));
                }
            }
        }
    }


}

