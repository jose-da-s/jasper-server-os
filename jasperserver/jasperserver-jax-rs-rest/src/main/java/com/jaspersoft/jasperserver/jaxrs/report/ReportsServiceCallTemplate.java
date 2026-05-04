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
package com.jaspersoft.jasperserver.jaxrs.report;

import com.jaspersoft.jasperserver.api.JSValidationException;
import com.jaspersoft.jasperserver.api.metadata.common.service.JSResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.common.RemoteServiceCallTemplate;
import com.jaspersoft.jasperserver.remote.common.RemoteServiceInTemplateCaller;
import com.jaspersoft.jasperserver.remote.exception.AccessDeniedException;
import com.jaspersoft.jasperserver.remote.exception.ResourceAlreadyExistsException;
import com.jaspersoft.jasperserver.remote.exception.ResourceNotFoundException;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;


/**
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
public class ReportsServiceCallTemplate<T> implements RemoteServiceCallTemplate<T> {
    private static final Log log = LogFactory.getLog(ReportsServiceCallTemplate.class);
    // in case of RunReportsJaxrsService ResponseType is always of type jakarta.ws.rs.core.Response
    @SuppressWarnings("unchecked")
    public <ResponseType> ResponseType callRemoteService(RemoteServiceInTemplateCaller<ResponseType, T> caller, T service) {
        Response response;
        try {
            response = (Response)caller.call(service);
        }catch (JSValidationException e){
            response = Response.status(Response.Status.BAD_REQUEST).entity(e.getErrors()).build();
        }catch (ResourceAlreadyExistsException e){
            response = Response.status(Response.Status.FORBIDDEN).entity(e.getErrorDescriptor()).build();
        }catch (JSResourceNotFoundException e){
            response = Response.status(Response.Status.NOT_FOUND).entity(
                    new ErrorDescriptor().setMessage("Resource not found")
                            .setErrorCode(ResourceNotFoundException.ERROR_CODE_RESOURCE_NOT_FOUND)
                            .addParameters(e.getArgs()))
                    .build();
        }catch (org.springframework.security.access.AccessDeniedException e){
            response = Response.status(Response.Status.FORBIDDEN).entity(new AccessDeniedException(e.getMessage()).getErrorDescriptor()).build();
        }catch (com.jaspersoft.jasperserver.remote.exception.AccessDeniedException e){
            response = Response.status(Response.Status.FORBIDDEN).entity(e.getErrorDescriptor()).build();
        }catch (WebApplicationException e){
            response = e.getResponse();
        }
        return (ResponseType)response;
    }
}
