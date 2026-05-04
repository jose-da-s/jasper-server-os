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

import com.jaspersoft.jasperserver.dto.adhoc.query.field.ClientQueryGroup;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.common.ValidationErrorDescriptorBuilder;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorCode.QUERY_GROUPBY_ALLGROUP_NOT_FIRST;

/**
 * @author Vasyl Spachynskyi
 * @version $Id$
 * @since 15.02.2017
 */
public class CheckAllGroupPositionValidator implements ConstraintValidator<CheckAllGroupPosition, List<ClientQueryGroup>>,
        ValidationErrorDescriptorBuilder {
    @Override
    public void initialize(CheckAllGroupPosition constraintAnnotation) {}

    @Override
    public boolean isValid(List<ClientQueryGroup> groups, ConstraintValidatorContext context) {
        if (groups.size() > 1) {
            if (isAllGroup(groups.get(0))) {
                return true;
            }

            for (ClientQueryGroup g : groups) {
                if (isAllGroup(g)) return false;
            }
        }
        return true;
    }

    @Override
    public ErrorDescriptor build(ConstraintViolation violation) {
        return QUERY_GROUPBY_ALLGROUP_NOT_FIRST.createDescriptor(violation.getPropertyPath().toString());
    }

    private boolean isAllGroup(ClientQueryGroup group) {
        return group instanceof ClientQueryGroup.ClientAllGroup;
    }
}
