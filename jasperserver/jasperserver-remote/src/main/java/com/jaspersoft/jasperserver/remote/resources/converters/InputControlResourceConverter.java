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

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConversionOptions;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.adapters.DomELCommonSimpleDateFormats;
import com.jaspersoft.jasperserver.dto.resources.ClientInputControl;
import com.jaspersoft.jasperserver.dto.resources.ClientReferenceableDataType;
import com.jaspersoft.jasperserver.dto.resources.ClientReferenceableListOfValues;
import com.jaspersoft.jasperserver.dto.resources.ClientReferenceableQuery;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.exception.MandatoryParameterNotFoundException;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.text.ParseException;
import java.util.*;

/**
 * <p></p>
 *
 * @author Zakhar.Tomchenco
 * @version $Id$
 */
@Service
public class InputControlResourceConverter extends ResourceConverterImpl<InputControl, ClientInputControl> {
    @Resource(name = "resourceReferenceConverterProvider")
    private ResourceReferenceConverterProvider resourceConverterProvider;

    @Override
    protected InputControl resourceSpecificFieldsToServer(ExecutionContext ctx, ClientInputControl clientObject, InputControl resultToUpdate, List<Exception> exceptions, ToServerConversionOptions options) throws IllegalParameterValueException, MandatoryParameterNotFoundException {
        ResourceReferenceConverter<ClientReferenceableDataType> dataTypeConverter = resourceConverterProvider.getConverterForType(ClientReferenceableDataType.class);
        ResourceReferenceConverter<ClientReferenceableListOfValues> lovConverter = resourceConverterProvider.getConverterForType(ClientReferenceableListOfValues.class);
        ResourceReferenceConverter<ClientReferenceableQuery> queryConverter = resourceConverterProvider.getConverterForType(ClientReferenceableQuery.class);

        resultToUpdate.setDataType(dataTypeConverter.toServer(ctx, clientObject.getDataType(), resultToUpdate.getDataType(), options));
        resultToUpdate.setListOfValues(lovConverter.toServer(ctx, clientObject.getListOfValues(), resultToUpdate.getListOfValues(), options));
        resultToUpdate.setQuery(queryConverter.toServer(ctx, clientObject.getQuery(), resultToUpdate.getQuery(), options));

        resultToUpdate.setMandatory(clientObject.isMandatory());
        resultToUpdate.setReadOnly(clientObject.isReadOnly());
        resultToUpdate.setVisible(clientObject.isVisible());
        resultToUpdate.setInputControlType(clientObject.getType());
        resultToUpdate.setQueryValueColumn(clientObject.getValueColumn());
        try {
            resultToUpdate.setCreationDate(clientObject.getCreationDate()!=null ? DomELCommonSimpleDateFormats.isoTimestampFormatNoMilliSeconds().parse(clientObject.getCreationDate()) : null);
            resultToUpdate.setUpdateDate(clientObject.getUpdateDate()!=null ? DomELCommonSimpleDateFormats.isoTimestampFormatNoMilliSeconds().parse(clientObject.getUpdateDate()) : null);
        } catch (ParseException e) {
           throw new IllegalParameterValueException("Exception occured while parsing date parameter", clientObject.getCreationDate(), clientObject.getUpdateDate());
        }
        for( String column : resultToUpdate.getQueryVisibleColumns()){
            resultToUpdate.removeQueryVisibleColumn(column);
        }

        if (clientObject.getVisibleColumns() != null){
            for( String column : clientObject.getVisibleColumns()){
                resultToUpdate.addQueryVisibleColumn(column);
            }
        }

        return resultToUpdate;
    }

    @Override
    protected ClientInputControl resourceSpecificFieldsToClient(ClientInputControl client, InputControl serverObject, ToClientConversionOptions options) {
        ResourceReferenceConverter<ClientReferenceableDataType> dataTypeConverter = resourceConverterProvider.getConverterForType(ClientReferenceableDataType.class);
        ResourceReferenceConverter<ClientReferenceableListOfValues> lovConverter = resourceConverterProvider.getConverterForType(ClientReferenceableListOfValues.class);
        ResourceReferenceConverter<ClientReferenceableQuery> queryConverter = resourceConverterProvider.getConverterForType(ClientReferenceableQuery.class);

        client.setDataType(dataTypeConverter.toClient(serverObject.getDataType(), options));
        client.setListOfValues(lovConverter.toClient(serverObject.getListOfValues(), options));
        client.setQuery(queryConverter.toClient(serverObject.getQuery(), options));
        client.setMandatory(serverObject.isMandatory());
        client.setReadOnly(serverObject.isReadOnly());
        client.setVisible(serverObject.isVisible());
        client.setType(serverObject.getInputControlType());
        client.setValueColumn(serverObject.getQueryValueColumn());
        if (serverObject.getQueryVisibleColumns().length > 0){
            client.setVisibleColumns(new ArrayList<String>(Arrays.asList(serverObject.getQueryVisibleColumns())));
        }

        return client;
    }
}

