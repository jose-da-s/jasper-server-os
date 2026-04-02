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
package com.jaspersoft.jasperserver.jaxrs.common;

import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.remote.exception.ErrorDescriptorBuildingService;

import jakarta.annotation.Resource;
import jakarta.ws.rs.core.Response;

/**
 * @author Vasyl Spachynskyi
 * @version $Id$
 * @since 25.03.2016
 */
public abstract class AbstractSerializationExceptionMapper {
    public static String ERROR_CODE = "serialization.error";

    @Resource
    private ErrorDescriptorBuildingService errorDescriptorBuildingService;

    public Response toResponse(Exception exception) {
        ErrorDescriptor errorDescriptor = buildErrorDescriptor(exception);
        return buildResponse(errorDescriptor);
    }

    protected Response buildResponse(ErrorDescriptor errorDescriptor) {
        return Response.status(Response.Status.BAD_REQUEST).entity(errorDescriptor).build();
    }

    protected ErrorDescriptor buildErrorDescriptor(Exception exception) {
        ErrorDescriptor errorDescriptor = errorDescriptorBuildingService.buildErrorDescriptor(exception);
        errorDescriptor.setErrorCode(ERROR_CODE);
        return errorDescriptor;
    }
}

