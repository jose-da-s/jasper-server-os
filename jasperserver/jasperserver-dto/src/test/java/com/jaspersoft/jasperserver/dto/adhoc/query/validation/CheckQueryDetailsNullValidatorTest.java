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

import com.jaspersoft.jasperserver.dto.adhoc.query.field.ClientQueryField;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import org.junit.Test;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import java.util.ArrayList;

import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorCode.Codes.QUERY_DETAILS_UNSUPPORTED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * <p></p>
 *
 * @author Volodya Sabadosh
 * @version $Id$
 */
public class CheckQueryDetailsNullValidatorTest {
    private CheckQueryDetailsNullValidator validator = new CheckQueryDetailsNullValidator();

    private ConstraintValidatorContext validatorContext = mock(ConstraintValidatorContext.class);

    @Test
    public void initialize_success() {
        CheckQueryDetailsNull annotation = mock(CheckQueryDetailsNull.class);
        validator.initialize(annotation);
    }

    @Test
    public void isValid_ValueNull_returnTrue() {
        ConstraintValidatorContext validatorContext = mock(ConstraintValidatorContext.class);

        assertTrue(validator.isValid(null, validatorContext));
    }

    @Test
    public void isValid_ValueNotNull_returnFalse() {
        ConstraintValidatorContext validatorContext = mock(ConstraintValidatorContext.class);

        assertFalse(validator.isValid(new ArrayList<ClientQueryField>(), validatorContext));
    }

    @Test
    public void build_success() {
        ConstraintViolation violation = mock(ConstraintViolation.class);
        ErrorDescriptor errorDescriptor = validator.build(violation);

        assertEquals(new ErrorDescriptor().setErrorCode(QUERY_DETAILS_UNSUPPORTED), errorDescriptor);
    }

}
