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

import com.jaspersoft.jasperserver.dto.importexport.State;
import com.jaspersoft.jasperserver.remote.exception.NoSuchTaskException;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/*
*  @author inesterenko
*/

@Component
public class BasicTasksManager implements TasksManager {

    protected Map<String, Task> tasks = new ConcurrentHashMap<String, Task>();

    @Resource(name = "loggableExecutorService")
    private ExecutorService executor;

    public BasicTasksManager(Map<String, Task> tasks) {
        this.tasks = new ConcurrentHashMap<String, Task>(tasks);
    }

    public BasicTasksManager() {
        super();
    }

    @Override
    public ExecutorService getExecutor() {
        return executor;
    }

    protected  String generateUniqueId(){
        String uuid = UUID.randomUUID().toString();
        if (tasks.containsKey(uuid)) {
            uuid = generateUniqueId();
        }
        return uuid;
    }

    @Override
    public State startTask(Task task) {
        State state = task.getState();
        state.setPhase(Task.INPROGRESS);
        String uuid = generateUniqueId();
        task.setUniqueId(uuid);
        state.setId(uuid);
        tasks.put(uuid, task);
        task.start(executor);
        return state;
    }

    @Override
    public State restartTask(Task task) {
        State state = task.getState();
        String taskId = state.getId();
        if (!tasks.containsKey(taskId)) {
            throw new NoSuchTaskException(taskId);
        }
        state.setPhase(Task.INPROGRESS);
        state.setError(null);
        task.start(executor);
        return state;
    }

    @Override
    public Set<String> getTaskIds() {
        return Collections.unmodifiableSet(tasks.keySet());
    }

    @Override
    public Task getTask(String taskId) throws NoSuchTaskException {
        if (tasks.containsKey(taskId)) {
            return tasks.get(taskId);
        } else {
            throw new NoSuchTaskException(taskId);
        }
    }

    @Override
    public void finishTask(String taskId) throws NoSuchTaskException {
        getTask(taskId).stop();
        tasks.remove(taskId);
    }

    public State getTaskState(String taskId) throws NoSuchTaskException {
        return getTask(taskId).getState();
    }
}

