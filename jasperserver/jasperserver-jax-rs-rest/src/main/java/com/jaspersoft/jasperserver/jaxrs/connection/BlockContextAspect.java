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

import com.jaspersoft.jasperserver.dto.resources.ResourceMediaType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

/**
 * @author ykovalch
 * @version $Id$
 */

@Component
@Aspect
public class BlockContextAspect {
    private static final List<String> mimeTypesToBlock = Arrays.asList(
            ResourceMediaType.REPORT_UNIT_JSON.toLowerCase(),
            ResourceMediaType.DOMAIN_TOPIC_JSON.toLowerCase());
    @Before("execution(public * ContextsJaxrsService.createContext(..))")
    public void checkMediaType(JoinPoint joinPoint) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String contentType = request.getHeader("Content-Type");
        if (contentType != null) {
            final String loverCaseContentType = contentType.toLowerCase();
            for (String mimeType : mimeTypesToBlock) {
                if(loverCaseContentType.contains(mimeType)){
                    throw new WebApplicationException(Response.Status.UNSUPPORTED_MEDIA_TYPE);
                }
            }
        }
    }

}

