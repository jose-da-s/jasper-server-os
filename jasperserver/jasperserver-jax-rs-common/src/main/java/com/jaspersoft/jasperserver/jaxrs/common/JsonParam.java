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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.lang.reflect.ParameterizedType;

/**
 * This converter can be used in JAX-RS services for automatic conversion of JSON string parameters to objects.
 * Don't use this class as raw type!
 *
 * @param <T> - target object type, required. Don't use this class as raw type!
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
public class JsonParam<T> {

    private T object;

    public JsonParam(String json) {
        try {
            if (!(this.getClass().getGenericSuperclass() instanceof ParameterizedType))
                throw new IllegalAccessException(JsonParam.class.getName() + " must be parametrized");
            Class<?> targetClass = (Class) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];

            ObjectMapper mapper = new ObjectMapper();
            mapper.setAnnotationIntrospector(new JakartaXmlBindAnnotationIntrospector());

            object = (T) mapper.readValue(json, targetClass);
        } catch (Exception e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Couldn't parse JSON string: " + e.getMessage())
                    .build());
        }
    }


    public T getObject() {
        return object;
    }
}
