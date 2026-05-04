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

package com.jaspersoft.jasperserver.jaxrs.bundle;

import static org.springframework.util.DigestUtils.md5DigestAsHex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Map;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Providers;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Igor.Nesterenko, Zahar.Tomchenko
 */
public abstract class AbstractBundleJaxrsService {

    @Context
    private Providers providers;

    public byte[] toJson(Map<String, String> messages) {
        MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;

        ByteArrayOutputStream entityStream = new ByteArrayOutputStream();
        MessageBodyWriter<Map> messageBodyWriter = providers.getMessageBodyWriter(Map.class, Map.class, new Annotation[0], mediaType);

        if (messageBodyWriter != null) {
            try {
                messageBodyWriter.writeTo(messages, Map.class, Map.class, new Annotation[0], mediaType, null, entityStream);
                return entityStream.toByteArray();
            } catch (IOException e) {  }
        }
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        for (String key : messages.keySet()) {
            messages.put(key, messages.get(key));
        }

        return json.toString().getBytes();
    }

    protected EntityTag generateETag(ObjectNode messagesJson) {
        if (messagesJson == null) {
            return null;
        } else {
            return generateETag(messagesJson.toString().getBytes());
        }
    }

    public EntityTag generateETag(byte[] bytes) {
        return new EntityTag(md5DigestAsHex(bytes));
    }
}
