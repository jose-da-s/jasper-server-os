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
package com.jaspersoft.jasperserver.jaxrs.customdatasources;

import com.jaspersoft.jasperserver.dto.customdatasources.ClientCustomDataSourceDefinition;
import com.jaspersoft.jasperserver.dto.customdatasources.CustomDataSourceDefinitionsListWrapper;
import com.jaspersoft.jasperserver.remote.customdatasources.CustomDataSourcesRemoteService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@Service
@Path("/customDataSources")
@Scope("prototype")
public class CustomDataSourcesJaxrsService {
    @Resource
    private CustomDataSourcesRemoteService customDataSourcesRemoteService;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public CustomDataSourceDefinitionsListWrapper getCustomDataSources(){
        final List<String> customDataSourceDefinitions = customDataSourcesRemoteService.getCustomDataSourceDefinitions();
        if(customDataSourceDefinitions.isEmpty()){
            throw new WebApplicationException(Response.Status.NO_CONTENT);
        }
        return new CustomDataSourceDefinitionsListWrapper(customDataSourceDefinitions);
    }

    @GET
    @Path("/{name}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public ClientCustomDataSourceDefinition getCustomDataSourceDefinition(@PathParam("name")String name){
        return customDataSourcesRemoteService.getCustomDataSourceDefinition(name);
    }
}

