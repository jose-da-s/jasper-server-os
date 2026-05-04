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

package com.jaspersoft.jasperserver.api.metadata.user.service.impl;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.jaspersoft.jasperserver.dto.common.AttributeErrorCode.Codes.ATTRIBUTE_PATTERNS_INCLUDES_INVALID;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p></p>
 *
 * @author Volodya Sabadosh
 * @version $Id$
 */
@Documented
@Constraint(validatedBy = CheckAttributePatternsValidator.class)
@Target({ FIELD, ANNOTATION_TYPE, TYPE })
@Retention(RUNTIME)
public @interface CheckAttributePatterns {
    String message() default ATTRIBUTE_PATTERNS_INCLUDES_INVALID;

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
