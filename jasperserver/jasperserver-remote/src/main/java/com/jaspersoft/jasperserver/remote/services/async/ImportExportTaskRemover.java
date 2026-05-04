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
package com.jaspersoft.jasperserver.remote.services.async;

import com.jaspersoft.jasperserver.remote.exception.NoSuchTaskException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jakarta.annotation.Resource;
import java.util.Set;

/**
 * In most cases import export task will be removed after finishing. But in the case if finished task still exist more then
 * <param>expirationInterval</param> then scheduler job will remove it.
 *
 * @author Volodya Sabadosh
 * @version $Id$
 */
public class ImportExportTaskRemover {
    protected final static Log log = LogFactory.getLog(ImportExportTaskRemover.class);

    private long expirationInterval = 86400000;

    @Resource
    private TasksManager basicTaskManager;

    public synchronized void call() {
        Set<String> availableTaskIds = basicTaskManager.getTaskIds();
        for (String taskId : availableTaskIds) {
            try {
                Task task = basicTaskManager.getTask(taskId);
                if (!task.getState().getPhase().equals(Task.INPROGRESS)
                        && task.getTaskCompletionDate() != null
                        && System.currentTimeMillis() - task.getTaskCompletionDate().getTime() > expirationInterval) {
                    basicTaskManager.finishTask(taskId);
                }
            } catch (NoSuchTaskException ex) {
                log.error("Import export task removing exception.  " + ex.getMessage());
            }
        }
    }

    public void setExpirationInterval(long expirationInterval) {
        this.expirationInterval = expirationInterval;
    }
}

