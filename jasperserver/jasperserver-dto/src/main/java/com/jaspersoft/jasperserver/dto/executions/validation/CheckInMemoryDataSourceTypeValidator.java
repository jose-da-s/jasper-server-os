/*
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

package com.jaspersoft.jasperserver.dto.executions.validation;

import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.common.ValidationErrorDescriptorBuilder;
import com.jaspersoft.jasperserver.dto.resources.ClientReference;
import com.jaspersoft.jasperserver.dto.resources.ClientReferenceable;
import com.jaspersoft.jasperserver.dto.resources.domain.ClientDomain;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Set;

import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorCode.QUERY_IN_MEMORY_DATASOURCE_TYPE_NOT_SUPPORTED;

/**
 * @author Volodya Sabadosh
 */
public class CheckInMemoryDataSourceTypeValidator
        implements ConstraintValidator<CheckInMemoryDataSourceType, ClientReferenceable>,
        ValidationErrorDescriptorBuilder {
    private final Set<Class<?>> supportedDatasourceClazzAndNameMap = new HashSet<Class<?>>() {
        {
            add(ClientReference.class);
            add(ClientDomain.class);
        }
    };

    @Override
    public void initialize(CheckInMemoryDataSourceType constraintAnnotation) {

    }

    @Override
    public boolean isValid(ClientReferenceable value, ConstraintValidatorContext context) {
        return value == null || supportedDatasourceClazzAndNameMap.contains(value.getClass());
    }

    @Override
    public ErrorDescriptor build(ConstraintViolation violation) {
        String dataSourceName = getClientResourceName(violation.getInvalidValue().getClass());
        return QUERY_IN_MEMORY_DATASOURCE_TYPE_NOT_SUPPORTED.createDescriptor(violation.getPropertyPath().toString(),
                dataSourceName);
    }

    private String getClientResourceName(Class<?> clazz) {
        XmlRootElement xmlRootElement = clazz.getAnnotation(XmlRootElement.class);
        return xmlRootElement.name();
    }

}
