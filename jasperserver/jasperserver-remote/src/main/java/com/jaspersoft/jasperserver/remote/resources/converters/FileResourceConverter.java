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
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConversionOptions;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.adapters.DomELCommonSimpleDateFormats;
import com.jaspersoft.jasperserver.dto.resources.ClientFile;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import jakarta.xml.bind.DatatypeConverter;
import java.text.ParseException;
import java.util.List;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 */
@Service
public class FileResourceConverter extends ResourceConverterImpl<FileResource, ClientFile>{
    @Autowired
    @Qualifier("concreteRepository")
    protected RepositoryService repositoryService;

    @Lazy
    @Autowired
    @Qualifier(PasswordCipherer.ID)
    private PasswordCipherer passwordCipherer;

    public PasswordCipherer getPasswordCipherer() {
        return passwordCipherer;
    }

    @Override
    protected FileResource resourceSpecificFieldsToServer(ExecutionContext ctx, ClientFile clientObject, FileResource resultToUpdate, List<Exception> exceptions, ToServerConversionOptions options) throws IllegalParameterValueException {
        if(resultToUpdate.getFileType() != null || resultToUpdate.isReference()){
            String type;
            if (resultToUpdate.isReference()){
                FileResource referenced = (FileResource)repositoryService.getResource(ctx, resultToUpdate.getReferenceURI());
                type = referenced.getFileType();
            } else {
                type = resultToUpdate.getFileType();
            }

            if (!type.equals(clientObject.getType().name())){
                // it's not allowed to change resource type
                throw new IllegalParameterValueException("type", clientObject.getType().name());
            }
        }

        if (!resultToUpdate.isReference()){
            resultToUpdate.setFileType(clientObject.getType().name());
        }

        if (clientObject.getContent() != null && !"".equals(clientObject.getContent())) {
            try {
                resultToUpdate.setData(getData(clientObject));
            } catch (IllegalArgumentException e) {
                throw new IllegalParameterValueException("content", "");
            }
            resultToUpdate.setReferenceURI(null);
            resultToUpdate.setFileType(clientObject.getType().name());
        }
            try {
                resultToUpdate.setCreationDate(clientObject.getCreationDate()!=null ? DomELCommonSimpleDateFormats.isoTimestampFormatNoMilliSeconds().parse(clientObject.getCreationDate()) : null);
                resultToUpdate.setUpdateDate(clientObject.getUpdateDate()!=null ? DomELCommonSimpleDateFormats.isoTimestampFormatNoMilliSeconds().parse(clientObject.getUpdateDate()) : null);
            } catch (ParseException e) {
                throw new IllegalParameterValueException("Exception occured while parsing date parameter", clientObject.getCreationDate(), clientObject.getUpdateDate());
            }
        return resultToUpdate;
    }

    @Override
    protected ClientFile resourceSpecificFieldsToClient(ClientFile client, FileResource serverObject, ToClientConversionOptions options) {
        String type;
        if (serverObject.isReference()){
            FileResource referenced = (FileResource)repositoryService.getResource(null, serverObject.getReferenceURI());
            type = referenced.getFileType();
        } else {
            type = serverObject.getFileType();
        }
        // Adding this condition to support existing mongo datasources with type ".config"
        type = type.equals(".config") ? "mongoDbSchema" : type;
        ClientFile.FileType clientFileType;
        try{
            clientFileType = ClientFile.FileType.valueOf(type);
        }catch (Exception e){
            // if no appropriate client type in an enum, then let it be unspecified
            clientFileType  = ClientFile.FileType.unspecified;
        }
        client.setType(clientFileType);
        return client;
    }

    private byte[] getData(ClientFile clientObject) {
        byte[] data = DatatypeConverter.parseBase64Binary(clientObject.getContent());
        if (clientObject.getType() == ClientFile.FileType.secureFile) {
            data = getPasswordCipherer().encodePassword(new String(data)).getBytes();
        }
        return data;
    }
}
