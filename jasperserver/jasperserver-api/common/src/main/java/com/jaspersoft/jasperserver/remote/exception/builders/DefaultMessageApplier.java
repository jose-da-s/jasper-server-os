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
package com.jaspersoft.jasperserver.remote.exception.builders;

import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Locale;

import static com.jaspersoft.jasperserver.dto.common.CommonErrorCode.toPropertyValues;
import static com.jaspersoft.jasperserver.remote.exception.builders.LocalizedErrorDescriptorBuilder.BUNDLE_PREFIX;

/**
 * @author Volodya Sabadosh
 * @version $Id$
 */
@Component
public class DefaultMessageApplier {
    @Resource(name = "messageSource")
    private MessageSource messageSource;

    /**
     * Applies bundled message in english locale if error descriptor message is null.
     *
     * @param descriptor error descriptor
     * @return errorDescriptor with injected default message.
     */
    public ErrorDescriptor applyDefaultMessageIfNotSet(ErrorDescriptor descriptor) {
        return applyDefaultMessageIfNotSet(descriptor, false);
    }

    /**
     * Applies bundled message in english locale if error descriptor message is null.
     *
     * @param descriptor error descriptor
     * @param useBundlePrefix indicates if message bundles with bundle prefix "exception.remote."
     *                        should be taken into account
     * @return errorDescriptor with injected default message.
     */
    public ErrorDescriptor applyDefaultMessageIfNotSet(ErrorDescriptor descriptor, boolean useBundlePrefix) {
        if (descriptor.getMessage() == null) {
            descriptor.setMessage(getDefaultMessage(descriptor, useBundlePrefix));
        }
        return descriptor;
    }

    private String getDefaultMessage(ErrorDescriptor descriptor, boolean useBundlePrefix) {
        String message = null;
        String errorCode = descriptor.getErrorCode();
        Object[] args = generateMsgArgs(descriptor);
        if (useBundlePrefix) {
            message = getMessageByCode(BUNDLE_PREFIX.concat(descriptor.getErrorCode()), args);
        }
        if (message == null) {
            message = getMessageByCode(errorCode, args);
        }
        return message;
    }

    private Object[] generateMsgArgs(ErrorDescriptor errorDescriptor) {
        if (errorDescriptor.getProperties() != null) {
            return toPropertyValues(errorDescriptor.getProperties());
        }
        return errorDescriptor.getParameters();
    }

    private String getMessageByCode(String code, Object[] args) {
        try {
            String message = messageSource.getMessage(code, args, Locale.ENGLISH);
            if (message == null || message.equals(code)) {
                return null;
            }
            return message;
        } catch (NoSuchMessageException e) {
            return null;
        }
    }

}

