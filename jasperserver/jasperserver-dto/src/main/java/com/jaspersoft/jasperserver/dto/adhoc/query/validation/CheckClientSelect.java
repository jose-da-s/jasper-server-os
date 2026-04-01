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

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import java.lang.annotation.*;

import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorCode.Codes.QUERY_SELECT_ILLEGAL_FIELDS_STATE;

/**
 * <p></p>
 *
 * @author Yehor Bobyk
 * @version $Id$
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Constraint(validatedBy = {CheckClientSelectFieldsStateValidator.class})
@ReportAsSingleViolation
public @interface CheckClientSelect {

    String message() default QUERY_SELECT_ILLEGAL_FIELDS_STATE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
