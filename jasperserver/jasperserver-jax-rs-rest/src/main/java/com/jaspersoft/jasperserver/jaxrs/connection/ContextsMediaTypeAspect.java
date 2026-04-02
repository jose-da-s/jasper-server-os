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
package com.jaspersoft.jasperserver.jaxrs.connection;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import static jakarta.ws.rs.core.HttpHeaders.ACCEPT;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;

/**
 * @author serhii.blazhyievskyi
 * @version $Id$
 */
@Component
@Aspect
public class ContextsMediaTypeAspect {
    private final String XML = "xml";

    @Before("execution(public * ContextsJaxrsService.*(..))")
    public void checkMediaType(JoinPoint joinPoint) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String mediaType = request.getHeader(CONTENT_TYPE);
        String accept = request.getHeader(ACCEPT);

        failIfHeaderEndsWithXml(mediaType, Response.Status.UNSUPPORTED_MEDIA_TYPE);
        failIfHeaderEndsWithXml(accept, Response.Status.NOT_ACCEPTABLE);
    }

    private void failIfHeaderEndsWithXml(String header, Response.Status failedResponseStatus) {
        if (header != null && header.trim().toLowerCase().endsWith(XML)) {
            throw new WebApplicationException(failedResponseStatus);
        }
    }
}

