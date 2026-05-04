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
package com.jaspersoft.jasperserver.remote.handlers;

import com.jaspersoft.jasperserver.api.metadata.common.domain.DataType;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.ResourceDescriptor;
import com.jaspersoft.jasperserver.remote.ServiceException;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlValidationException;
import com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.converters.DataConverterService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author gtoffoli
 * @version $Id$
 */
@Service
public class DataTypeHandler extends RepositoryResourceHandler {

    @jakarta.annotation.Resource
    private DataConverterService dataConverterService;

    public Class getResourceType() {
        return DataType.class;
    }

    protected void doGet(Resource resource, ResourceDescriptor descriptor, Map options) throws ServiceException {
        DataType fileResource = (DataType) resource;
        descriptor.setWsType(ResourceDescriptor.TYPE_DATA_TYPE);
        descriptor.setHasData(false);
        descriptor.setIsReference(false);

        descriptor.setDataType(fileResource.getDataTypeType());
        descriptor.setPattern(fileResource.getRegularExpr());
        descriptor.setMaxValue(fileResource.getMaxValue() == null ? null : dataConverterService.formatSingleValue(
                fileResource.getMaxValue(), fileResource, fileResource.getMaxValue().getClass()));
        descriptor.setMinValue(fileResource.getMinValue() == null ? null : dataConverterService.formatSingleValue(
                fileResource.getMinValue(), fileResource, fileResource.getMinValue().getClass()));
        descriptor.setStrictMax(fileResource.isStrictMax());
        descriptor.setStrictMin(fileResource.isStrictMin());
    }

    @Override
    protected void updateResource(Resource resource, ResourceDescriptor descriptor, Map options)
    {
        super.updateResource(resource, descriptor, options);

        DataType dataType = (DataType) resource;
        // Validations should be done in the save method...
        dataType.setDataTypeType(descriptor.getDataType());
        dataType.setRegularExpr(descriptor.getPattern());
        dataType.setStrictMax(descriptor.isStrictMax());
        final String rawMaxValue = descriptor.getMaxValue();
        final String rawMinValue = descriptor.getMinValue();
        try {
            dataType.setMaxValue(rawMaxValue != null ? (Comparable) dataConverterService.convertSingleValue(rawMaxValue, dataType) : null);
            dataType.setMinValue(rawMinValue != null ? (Comparable) dataConverterService.convertSingleValue(rawMinValue, dataType) : null);
        } catch (InputControlValidationException e) {
            throw new RuntimeException(e.getValidationError() != null && e.getValidationError().getDefaultMessage() != null ?
                    e.getValidationError().getDefaultMessage() : e.getMessage(), e);
        }
        dataType.setStrictMin(descriptor.isStrictMin());
    }

    
}

