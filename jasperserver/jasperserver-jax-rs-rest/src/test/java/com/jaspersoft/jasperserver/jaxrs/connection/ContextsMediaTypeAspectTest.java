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
import org.junit.Test;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import static jakarta.ws.rs.core.HttpHeaders.ACCEPT;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * <p></p>
 *
 * @author Volodya Sabadosh
 */
public class ContextsMediaTypeAspectTest {
    private ContextsMediaTypeAspect contextsMediaTypeAspect = new ContextsMediaTypeAspect();
    private JoinPoint joinPoint = mock(JoinPoint.class);

    @Test
    public void checkMediaType_someContentTypeIsXmlAndAcceptIsJson_success() {
        mockContentTypeAndAccept("application/repository.xmlConnection+xml", "application/repository.xmlConnection+json");
        try {
            contextsMediaTypeAspect.checkMediaType(joinPoint);
            fail();
        } catch (WebApplicationException ex) {
            assertEquals(Response.Status.UNSUPPORTED_MEDIA_TYPE, ex.getResponse().getStatusInfo());
        }
    }

    @Test
    public void checkMediaType_someContentTypeIsJsonAndAcceptIsXml_success() {
        mockContentTypeAndAccept("application/repository.xmlConnection+json", "application/repository.xmlConnection+xml");
        try {
            contextsMediaTypeAspect.checkMediaType(joinPoint);
            fail();
        } catch (WebApplicationException ex) {
            assertEquals(Response.Status.NOT_ACCEPTABLE, ex.getResponse().getStatusInfo());
        }
    }

    @Test
    public void checkMediaType_someContentTypeIsXmlAndAcceptAndCaseInsensitiveIsJson_success() {
        mockContentTypeAndAccept("application/repository.xmlConnection+XmL", "application/repository.xmlConnection+JsOn");
        try {
            contextsMediaTypeAspect.checkMediaType(joinPoint);
            fail();
        } catch (WebApplicationException ex) {
            assertEquals(Response.Status.UNSUPPORTED_MEDIA_TYPE, ex.getResponse().getStatusInfo());
        }
    }

    @Test
    public void checkMediaType_someContentTypeIsJsonAndAcceptIsXmlAndCaseInsensitive_success() {
        mockContentTypeAndAccept("application/repository.xmlConnection+JsoN", "application/repository.xmlConnection+XMl");
        try {
            contextsMediaTypeAspect.checkMediaType(joinPoint);
            fail();
        } catch (WebApplicationException ex) {
            assertEquals(Response.Status.NOT_ACCEPTABLE, ex.getResponse().getStatusInfo());
        }
    }

    @Test
    public void checkMediaType_bothAreJson_success() {
        mockContentTypeAndAccept("application/repository.xmlConnection+JsoN", "application/repository.xmlConnection+Json");
        contextsMediaTypeAspect.checkMediaType(joinPoint);
      }

    private void mockContentTypeAndAccept(final String contentType, final String accept) {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getHeader(CONTENT_TYPE)).thenReturn(contentType);
        when(httpServletRequest.getHeader(ACCEPT)).thenReturn(accept);

        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);

        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
    }

}

