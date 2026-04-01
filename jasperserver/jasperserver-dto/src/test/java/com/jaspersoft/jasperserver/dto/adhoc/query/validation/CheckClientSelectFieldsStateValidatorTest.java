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

package com.jaspersoft.jasperserver.dto.adhoc.query.validation;

import com.jaspersoft.jasperserver.dto.adhoc.query.ClientQuery;
import com.jaspersoft.jasperserver.dto.adhoc.query.MultiLevelQueryBuilder;
import com.jaspersoft.jasperserver.dto.adhoc.query.field.ClientQueryField;
import com.jaspersoft.jasperserver.dto.adhoc.query.select.ClientSelect;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import org.junit.Test;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;

import static com.jaspersoft.jasperserver.dto.adhoc.query.MultiLevelQueryBuilder.select;
import static com.jaspersoft.jasperserver.dto.adhoc.query.MultiLevelQueryBuilder.selectDistinct;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorCode.Codes.QUERY_SELECT_ILLEGAL_FIELDS_STATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

import static java.util.Arrays.asList;

/**
 * <p></p>
 *
 * @author Yehor Bobyk
 * @version $Id$
 */

public class CheckClientSelectFieldsStateValidatorTest {

    private CheckClientSelectFieldsStateValidator validator = new CheckClientSelectFieldsStateValidator();
    private ConstraintValidatorContext validatorContext = mock(ConstraintValidatorContext.class);
    private ClientQueryField clientQueryField = new ClientQueryField();

    @Test
    public void initialize_success() {
        CheckClientSelect annotation = mock(CheckClientSelect.class);
        validator.initialize(annotation);
    }

    @Test
    public void isValid_emptyClientSelectObject_returnTrue() {
        ClientQuery clientQuery = new MultiLevelQueryBuilder().build();
        assertTrue(validator.isValid(clientQuery.getSelect(), validatorContext));
    }

    @Test
    public void isValid_clientSelectObjectWithNotEmptyFieldList_returnTrue() {
        ClientQuery clientQuery = select(clientQueryField)
                .build();

        assertTrue(validator.isValid(clientQuery.getSelect(), validatorContext));
    }

    @Test
    public void isValid_clientSelectObjectWithNotEmptyDistinctFieldList_returnTrue() {
        ClientQuery clientQuery = selectDistinct(clientQueryField)
                .build();

        assertTrue(validator.isValid(clientQuery.getSelect(), validatorContext));
    }

    @Test
    public void isNotValid_clientSelectObjectWithNotEmptyFieldListAndNotEmptyDistinctFieldList_returnFalse() {
        ClientSelect clientSelect = new ClientSelect(asList(clientQueryField));
        clientSelect.setDistinctFields(asList(clientQueryField));

        assertFalse(validator.isValid(clientSelect, validatorContext));
    }

    @Test
    public void build_success() {
        ConstraintViolation violation = mock(ConstraintViolation.class);
        ErrorDescriptor errorDescriptor = validator.build(violation);

        assertEquals(new ErrorDescriptor().setErrorCode(QUERY_SELECT_ILLEGAL_FIELDS_STATE), errorDescriptor);
    }

}
