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
package com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.workflow.data;

import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.workflow.dto.UserWorkflow;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.*;

/**
 * @author Igor.Nesterenko
 * @version $Id$
 */

@Component
public class UserWorkflowStorage {

    @Resource(name="messageSource")
    protected MessageSource messageSource;

    @Resource(name="userWorkflows")
    private LinkedHashMap<String, UserWorkflow> workflows;

    public UserWorkflow findByName(String name){
        return localize(workflows.get(name));
    }

    public List<UserWorkflow> findAll(){
        return localize(workflows.values());
    }

    public List<UserWorkflow> findAllByParentName(String parentName){
        List<UserWorkflow> result = new ArrayList<UserWorkflow>();

        for (UserWorkflow userWorkflow : findAll()) {
            String flowParentName = String.valueOf(userWorkflow.getParentName());
            if (parentName.equals(flowParentName)){
                 result.add(userWorkflow);
             }
        }
        return result;
    }

    protected String getMessage(String key){
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    protected UserWorkflow localize(UserWorkflow workflow) {
        UserWorkflow result = new UserWorkflow(workflow);
        result.setLabel(getMessage(workflow.getLabel()));
        result.setDescription(getMessage(workflow.getDescription()));
        return result;
    }

    protected List<UserWorkflow> localize(Collection<UserWorkflow> workflows) {
        List<UserWorkflow> result = new ArrayList<UserWorkflow>();
        for (UserWorkflow userWorkflow : workflows) {
            result.add(localize(userWorkflow));
        }
        return result;
    }
}

