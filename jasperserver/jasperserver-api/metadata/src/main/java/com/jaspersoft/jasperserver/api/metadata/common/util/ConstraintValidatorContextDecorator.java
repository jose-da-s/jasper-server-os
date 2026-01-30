/*
 * Copyright (C) 2025 the Jasper Server OS Authors
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

package com.jaspersoft.jasperserver.api.metadata.common.util;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * <p></p>
 *
 * @author Vlad Zavadskii
 * @version $Id: ConstraintValidatorContextDecorator.java 58781 2016-04-20 14:24:39Z vzavadsk $
 */
public class ConstraintValidatorContextDecorator implements ConstraintValidatorContext {
    public static final String ARGUMENTS = "message.arguments";

    private HibernateConstraintValidatorContext context;

    public ConstraintValidatorContextDecorator(ConstraintValidatorContext context) {
        if (!(context instanceof HibernateConstraintValidatorContext)) {
            throw new IllegalArgumentException("\"" + ConstraintValidatorContextDecorator.class.getName() +
                    "\" currently supports only \"" + HibernateConstraintValidatorContext.class.getName() +
                    "\" implementation");
        }

        this.context = (HibernateConstraintValidatorContext) context;
    }

    // unchecked cast to List<Object> is assured by "setArguments" method's signature
    @SuppressWarnings("unchecked")
    public static List<Object> getArguments(ConstraintViolation violation) {
        List<Object> arguments = null;

        if (violation instanceof ConstraintViolationImpl) {
            Map<String, Object> variables = ((ConstraintViolationImpl<?>) violation).getExpressionVariables();
            if (variables != null) {
                arguments = (List<Object>) variables.get(ARGUMENTS);
            }
        }

        return arguments;
    }

    // unchecked cast to List<Object> is assured by "setArguments" method's signature
    @SuppressWarnings("unchecked")
    public static Object[] getArgumentsArray(ConstraintViolation violation) {
        List<Object> arguments = getArguments(violation);
        if (arguments != null) {
            return arguments.toArray();
        }
        return null;
    }

    public ConstraintValidatorContextDecorator setArguments(Object... arguments) {
        return setArguments(asList(arguments));
    }

    public ConstraintValidatorContextDecorator setArguments(List<Object> arguments) {
        context.addExpressionVariable(ARGUMENTS, arguments);
        return this;
    }

    @Override
    public void disableDefaultConstraintViolation() {
        context.disableDefaultConstraintViolation();
    }

    @Override
    public String getDefaultConstraintMessageTemplate() {
        return context.getDefaultConstraintMessageTemplate();
    }

    @Override
    public ConstraintViolationBuilder buildConstraintViolationWithTemplate(String messageTemplate) {
        return context.buildConstraintViolationWithTemplate(messageTemplate);
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        return context.unwrap(type);
    }

    @Override
    public javax.validation.ClockProvider getClockProvider() {
        return context.getClockProvider();
    }
}
