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
import com.jaspersoft.jasperserver.dto.resources.ClientReference;
import com.jaspersoft.jasperserver.dto.resources.ClientSemanticLayerDataSource;
import com.jaspersoft.jasperserver.dto.resources.domain.ClientDomain;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;

import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorCode.QUERY_IN_MEMORY_DATASOURCE_TYPE_NOT_SUPPORTED;
import static com.jaspersoft.jasperserver.dto.resources.ResourceMediaType.SEMANTIC_LAYER_DATA_SOURCE_CLIENT_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Volodya Sabadosh
 */
@RunWith(MockitoJUnitRunner.class)
public class CheckInMemoryDataSourceTypeValidatorTest {
    private CheckInMemoryDataSourceTypeValidator checkInMemoryDataSourceTypeValidator =
            new CheckInMemoryDataSourceTypeValidator();

    private ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
    @Before
    public void setUp() {
    }

    @Test
    public void isValid_DomainDataSource_true() {
        ClientDomain domain = new ClientDomain();
        assertTrue(checkInMemoryDataSourceTypeValidator.isValid(domain, context));
    }

    @Test
    public void isValid_Reference_true() {
        ClientReference domain = new ClientReference();
        assertTrue(checkInMemoryDataSourceTypeValidator.isValid(domain, context));
    }

    @Test
    public void isValid_nonSupportedDatasource_false() {
        ClientSemanticLayerDataSource domain = new ClientSemanticLayerDataSource();
        assertFalse(checkInMemoryDataSourceTypeValidator.isValid(domain, context));
    }

    @Test
    public void isValid_DataSourceIsNull_true() {
        ClientDomain domain = null;
        assertTrue(checkInMemoryDataSourceTypeValidator.isValid(domain, context));
    }

    @Test
    public void build_nonSupportedDatasource_correctErrorCode() {
        ConstraintViolation violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);
        when(violation.getInvalidValue()).thenReturn(new ClientSemanticLayerDataSource());
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        String propertyPathStr = "datasource";
        when(propertyPath.toString()).thenReturn(propertyPathStr);

        ErrorDescriptor errorDescriptor = checkInMemoryDataSourceTypeValidator.build(violation);

        assertEquals(QUERY_IN_MEMORY_DATASOURCE_TYPE_NOT_SUPPORTED.
                createDescriptor(propertyPathStr, SEMANTIC_LAYER_DATA_SOURCE_CLIENT_TYPE), errorDescriptor);
    }

    @Test
    public void initialize_success() {
        CheckInMemoryDataSourceType checkInMemoryDataSourceType = mock(CheckInMemoryDataSourceType.class);
        checkInMemoryDataSourceTypeValidator.initialize(checkInMemoryDataSourceType);
    }

}
