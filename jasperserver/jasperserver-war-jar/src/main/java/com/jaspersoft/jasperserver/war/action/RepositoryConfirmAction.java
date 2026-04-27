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
package com.jaspersoft.jasperserver.war.action;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.webflow.action.MultiAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
public class RepositoryConfirmAction extends MultiAction {

    @Resource
    private MessageSource messageSource;

    public Event acceptConfirmation(RequestContext context){
        final String[] resourceTypes = context.getRequestParameters().getArray("resourceType");
        final List<String> resourceTypesList = Arrays.asList(resourceTypes);
        Collections.sort(resourceTypesList);
        final StringBuilder resourceTypesSuffixBuilder = new StringBuilder("resource.saved.confirmation");
        for(String currentResourceType : resourceTypesList){
            resourceTypesSuffixBuilder.append(".").append(currentResourceType);
        }
        final Locale locale = LocaleContextHolder.getLocale();
        context.getExternalContext().getSessionMap().put("repositorySystemConfirm",
                messageSource.getMessage(resourceTypesSuffixBuilder.toString(), null, locale));
        return success();
    }
}

