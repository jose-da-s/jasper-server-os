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
import com.jaspersoft.jasperserver.api.metadata.common.domain.DataType;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConversionOptions;
import com.jaspersoft.jasperserver.dto.resources.ClientDataType;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlValidationException;
import com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.converters.DataConverterService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@Service
public class DataTypeResourceConverter extends ResourceConverterImpl<DataType, ClientDataType> {
    @Resource
    private DataConverterService dataConverterService;

    @Override
    protected DataType resourceSpecificFieldsToServer(ExecutionContext ctx, ClientDataType clientObject, DataType resultToUpdate, List<Exception> exceptions, ToServerConversionOptions options) throws IllegalParameterValueException {
        byte type;
        if (clientObject.getType() != null) {
            switch (clientObject.getType()) {
                case text:
                    resultToUpdate.setDataTypeType(DataType.TYPE_TEXT);
                    resultToUpdate.setRegularExpr(clientObject.getPattern());
                    resultToUpdate.setMaxLength(clientObject.getMaxLength());
                    break;
                case date:
                    resultToUpdate.setDataTypeType(DataType.TYPE_DATE);
                    setNumbers(clientObject, resultToUpdate);
                    break;
                case datetime:
                    resultToUpdate.setDataTypeType(DataType.TYPE_DATE_TIME);
                    setNumbers(clientObject, resultToUpdate);
                    break;
                case number:
                    resultToUpdate.setDataTypeType(DataType.TYPE_NUMBER);
                    setNumbers(clientObject, resultToUpdate);
                    break;
                case time:
                    resultToUpdate.setDataTypeType(DataType.TYPE_TIME);
                    setNumbers(clientObject, resultToUpdate);
                    break;
            }
        } else {
            // to get validation error later, because user did not specified type, which is mandatory
            resultToUpdate.setDataTypeType((byte) 0);
        }
        return resultToUpdate;
    }

    private void setNumbers(ClientDataType clientObject, DataType resultToUpdate) throws IllegalParameterValueException {
        try {
            resultToUpdate.setMaxValue((Comparable) dataConverterService.convertSingleValue(clientObject.getMaxValue(), resultToUpdate));
        } catch (InputControlValidationException e) {
            throw new IllegalParameterValueException("maxValue", clientObject.getMaxValue());
        }
        try {
            resultToUpdate.setMinValue((Comparable) dataConverterService.convertSingleValue(clientObject.getMinValue(), resultToUpdate));
        } catch (InputControlValidationException e) {
            throw new IllegalParameterValueException("minValue", clientObject.getMinValue());
        }
        resultToUpdate.setStrictMax(clientObject.isStrictMax());
        resultToUpdate.setStrictMin(clientObject.isStrictMin());
    }

    @Override
    protected ClientDataType resourceSpecificFieldsToClient(ClientDataType client, DataType serverObject, ToClientConversionOptions options) {
        ClientDataType.TypeOfDataType type;
        switch (serverObject.getDataTypeType()) {
            case DataType.TYPE_TEXT:
                type = ClientDataType.TypeOfDataType.text;
                break;
            case DataType.TYPE_DATE:
                type = ClientDataType.TypeOfDataType.date;
                break;
            case DataType.TYPE_DATE_TIME:
                type = ClientDataType.TypeOfDataType.datetime;
                break;
            case DataType.TYPE_NUMBER:
                type = ClientDataType.TypeOfDataType.number;
                break;
            case DataType.TYPE_TIME:
                type = ClientDataType.TypeOfDataType.time;
                break;
            default:
                throw new IllegalStateException("Unsupported dataType '" + serverObject.getDataTypeType() + "'");
        }
        client.setType(type);
        if (serverObject.getMaxValue() != null) {
            client.setMaxValue(dataConverterService.formatSingleValue(serverObject.getMaxValue(), serverObject, String.class));
        }
        if (serverObject.getMinValue() != null) {
            client.setMinValue(dataConverterService.formatSingleValue(serverObject.getMinValue(), serverObject, String.class));
        }
        // ignore empty string pattern
        client.setPattern("".equals(serverObject.getRegularExpr()) ? null : serverObject.getRegularExpr());
        client.setStrictMax(serverObject.isStrictMax());
        client.setStrictMin(serverObject.isStrictMin());
        client.setMaxLength(serverObject.getMaxLength());
        return client;
    }
}

