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

package com.jaspersoft.jasperserver.api;

import jakarta.validation.ConstraintViolation;
import java.util.HashSet;
import java.util.Set;

/**
 * <p></p>
 *
 * @author Volodya Sabadosh
 * @version $Id$
 */
public class JSConstraintViolationException extends RuntimeException {
    private final Set<ConstraintViolation<?>> constraintViolations;

    public JSConstraintViolationException(String message, Set<? extends ConstraintViolation<?>> constraintViolations) {
        super(buildErrorMessage(message, constraintViolations));
        this.constraintViolations = new HashSet<ConstraintViolation<?>>(constraintViolations);
    }

    private static String buildErrorMessage(String baseMessage, Set<? extends ConstraintViolation<?>> constraintViolations) {
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append(baseMessage).append(" \n");

        for (ConstraintViolation constraintViolation : constraintViolations) {
            errorMessage.append("Property ").
                    append(constraintViolation.getPropertyPath()).append(" is invalid. ").
                    append(constraintViolation.getMessage()).append(" \n");

        }

        return errorMessage.toString();
    }

    public Set<ConstraintViolation<?>> getConstraintViolations() {
        return constraintViolations;
    }


}
