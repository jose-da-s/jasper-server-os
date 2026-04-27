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

package com.jaspersoft.jasperserver.jaxrs.resources;


import com.google.common.base.Strings;
import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.api.metadata.common.service.JSResourceNotFoundException;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.FavoriteResourceService;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.dto.favorite.FavoriteResource;
import com.jaspersoft.jasperserver.dto.favorite.FavoriteResourceListWrapper;
import com.jaspersoft.jasperserver.remote.exception.MandatoryParameterNotFoundException;
import com.jaspersoft.jasperserver.remote.exception.ResourceNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Path("/favorites")
@Scope("prototype")
public class FavoritesJaxrsService {
    protected static final Log log = LogFactory.getLog(FavoritesJaxrsService.class);

    @Resource(name = "${bean.favoriteResourceService}")
    private FavoriteResourceService favoriteResourceService;
    private User user;

    @POST
    @Consumes("application/json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addFavorites(FavoriteResourceListWrapper data) throws ErrorDescriptorException {
        validateInput(data);
        List<String> uriList = data.getFavorites().stream().map(FavoriteResource::getUri).collect(Collectors.toList());
        try {
            favoriteResourceService.addFavorites(getUser(), uriList);
        } catch (JSResourceNotFoundException e) {
            throw new ResourceNotFoundException(e.getMessage());
        } catch (AccessDeniedException e) {
            throw new com.jaspersoft.jasperserver.remote.exception.AccessDeniedException("Access denied.", e.getMessage());
        }
        return Response.status(Response.Status.CREATED).entity(new FavoriteResourceListWrapper(data.getFavorites())).build();
    }

    @POST
    @Path("/delete")
    @Consumes("application/json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeFavorites(FavoriteResourceListWrapper data) throws ErrorDescriptorException {
        validateInput(data);
        List<String> uriList = data.getFavorites().stream().map(FavoriteResource::getUri).collect(Collectors.toList());
        try {
            favoriteResourceService.removeFavorites(getUser(), uriList);
        } catch (JSResourceNotFoundException e) {
            throw new ResourceNotFoundException(e.getMessage());
        } catch (AccessDeniedException e) {
            throw new com.jaspersoft.jasperserver.remote.exception.AccessDeniedException("Access denied.", e.getMessage());
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    private User getUser() {
        if (user == null)
            user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user;
    }

    private void validateInput(FavoriteResourceListWrapper data) {
        List<FavoriteResource> favoriteResources = Optional.ofNullable(data).map(FavoriteResourceListWrapper::getFavorites).orElse(Collections.EMPTY_LIST);
        if (data.getFavorites() == null) {
            throw new MandatoryParameterNotFoundException("favorites");
        }
        if (favoriteResources.stream().map(FavoriteResource::getUri).anyMatch(Strings::isNullOrEmpty)) {
            throw new MandatoryParameterNotFoundException("uri");
        }
    }
}

