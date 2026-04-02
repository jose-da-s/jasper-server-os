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
package com.jaspersoft.jasperserver.remote.resources.converters;

import com.jaspersoft.jasperserver.api.common.crypto.PasswordCipherer;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ContentResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConversionOptions;
import com.jaspersoft.jasperserver.dto.resources.ClientFile;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import org.springframework.stereotype.Service;

import jakarta.xml.bind.DatatypeConverter;
import java.util.List;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@Service
public class ContentResourceConverter extends ResourceConverterImpl<ContentResource, ClientFile>{

        @Override
    protected ContentResource resourceSpecificFieldsToServer(ExecutionContext ctx, ClientFile clientObject, ContentResource resultToUpdate, List<Exception> exceptions, ToServerConversionOptions options) throws IllegalParameterValueException {
        if(resultToUpdate.getFileType() != null &&
                (!resultToUpdate.getFileType().equals(clientObject.getType().name()) &&
                        !(ContentResource.TYPE_UNSPECIFIED.equals(resultToUpdate.getFileType()) && clientObject.getType().equals(ClientFile.FileType.unspecified)))){
            // it's not allowed to change resource type
            throw new IllegalParameterValueException("type", clientObject.getType().name());
        }
        if (ClientFile.FileType.unspecified.equals(clientObject.getType())){
            resultToUpdate.setFileType(ContentResource.TYPE_UNSPECIFIED);
        } else {
            resultToUpdate.setFileType(clientObject.getType().name());
        }

        if (clientObject.getContent() != null && !"".equals(clientObject.getContent())) {
            try {
                byte[] data = DatatypeConverter.parseBase64Binary(clientObject.getContent());
                
                // If the file to create/update is of type secure file, we should encrypt the
                // content if it is not encrypted yet
                if (resultToUpdate.getFileType() != null
                    && resultToUpdate.getFileType().equals(ClientFile.FileType.secureFile.name())) {
                        data = PasswordCipherer.getInstance().encodePassword(new String(data)).getBytes();
                }
                
                resultToUpdate.setData(data);
                
            } catch (IllegalArgumentException e) {
                throw new IllegalParameterValueException("content", "");
            }
        }

        return resultToUpdate;
    }

    @Override
    protected ClientFile resourceSpecificFieldsToClient(ClientFile client, ContentResource serverObject, ToClientConversionOptions options) {
        if (ContentResource.TYPE_UNSPECIFIED.equals(serverObject.getFileType())){
            client.setType(ClientFile.FileType.unspecified);
        } else {
            client.setType(ClientFile.FileType.valueOf(serverObject.getFileType()));
        }
        return client;
    }
}
