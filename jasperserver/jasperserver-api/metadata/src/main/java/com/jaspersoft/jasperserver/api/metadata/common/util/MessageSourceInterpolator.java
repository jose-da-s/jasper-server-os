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
package com.jaspersoft.jasperserver.api.metadata.common.util;

import org.hibernate.validator.internal.engine.MessageInterpolatorContext;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import java.util.List;
import java.util.Locale;

import static com.jaspersoft.jasperserver.api.metadata.common.util.ConstraintValidatorContextDecorator.ARGUMENTS;

/**
 * <p></p>
 *
 * @author Volodya Sabadosh
 * @version $Id$
 */
@Component
public class MessageSourceInterpolator implements MessageInterpolator {
    @Resource
    private MessageSource messageSource;

    private MessageInterpolator baseInterpolator = getBaseInterpolator();

    @Override
    public String interpolate(String messageTemplate, Context context) {
        return interpolate(messageTemplate, context, Locale.getDefault());
    }

    @Override
    @SuppressWarnings("unchecked")
    public String interpolate(String messageTemplate, Context context, Locale locale) {
        String message = baseInterpolator.interpolate(messageTemplate, context, locale);
        if (context instanceof MessageInterpolatorContext) {
            MessageInterpolatorContext messageInterpolatorContext = (MessageInterpolatorContext) context;
            List<Object> paramList = null;
            if (messageInterpolatorContext.getMessageParameters() != null) {
                paramList = (List<Object>)(messageInterpolatorContext.getMessageParameters().get(ConstraintValidatorContextDecorator.ARGUMENTS));
            }
            Object[] args = null;
            if (paramList != null) {
                args = paramList.toArray(new Object[] {paramList.size()});
            }
            message = messageSource.getMessage(message, args, locale);
        }

        return message;
    }

    protected MessageInterpolator getBaseInterpolator() {
        return Validation.byDefaultProvider()
                .configure().getDefaultMessageInterpolator();
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
}

