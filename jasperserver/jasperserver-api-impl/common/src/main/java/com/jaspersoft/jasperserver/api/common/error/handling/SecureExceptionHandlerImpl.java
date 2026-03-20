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

package com.jaspersoft.jasperserver.api.common.error.handling;

import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.UUID;

import static com.jaspersoft.jasperserver.api.common.error.handling.ExceptionOutputManager.GENERIC_ERROR_MESSAGE_CODE;
import static java.lang.String.format;

/**
 * @author dlitvak
 * @version $Id$
 */
@Service
public final class SecureExceptionHandlerImpl implements SecureExceptionHandler {
    private static final Logger logger = LogManager.getLogger(SecureExceptionHandlerImpl.class);
    public static final String ERROR_UID = "error.uid.message";

    @Resource
    private ExceptionOutputManager exceptionOutputManager;

    @Resource
    private MessageSource messageSource;

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void setExceptionOutputManager(ExceptionOutputManager exceptionOutputManager) {
        this.exceptionOutputManager = exceptionOutputManager;
    }

    @Override
    public ErrorDescriptor handleException(Throwable t) {
        return handleException(t, new ErrorDescriptor(), null);
    }

    @Override
    public ErrorDescriptor handleException(Throwable t, ErrorDescriptor errorInfo) {
        return handleException(t, errorInfo.getErrorCode(), errorInfo.getMessage(), null);
    }

    @Override
    public ErrorDescriptor handleException(Throwable t, ErrorDescriptor errorInfo, Locale locale) {
        return handleException(t, errorInfo.getErrorCode(), errorInfo.getMessage(), locale);
    }

    private ErrorDescriptor handleException(Throwable t, String errorCode, String errorMessage, Locale locale) {
//        logger.error(t);
        final Locale loc = locale != null ? locale : LocaleContextHolder.getLocale();
        if (errorMessage == null && errorCode != null)
			errorMessage = extractErrorMessageForErrorCode(errorCode, null, loc);

        ErrorDescriptor errorDescriptor = new ErrorDescriptor();
        errorDescriptor.setErrorCode(errorCode != null ? errorCode : GENERIC_ERROR_MESSAGE_CODE);

		StringBuilder msgBuff = new StringBuilder();
        if (exceptionOutputManager.isExceptionMessageAllowed()) { //check if non-JRS message output is authorized
            if (errorMessage != null && !errorMessage.isEmpty()) {
                msgBuff.append(errorMessage);
            } else {
                msgBuff.append(t.getMessage());
            }
        }
        else {   //Put in GENERIC_ERROR_MESSAGE_CODE as the last resort
            final String msg = extractErrorMessageForErrorCode(GENERIC_ERROR_MESSAGE_CODE, null, loc);
            msgBuff.append(msg != null ? msg : GENERIC_ERROR_MESSAGE_CODE);
        }

        if (exceptionOutputManager.isUIDOutputOn()) {
            final String errorUid = UUID.randomUUID().toString();
            errorDescriptor.setErrorUid(errorUid);

            String errUidMsg = extractErrorMessageForErrorCode(ERROR_UID, null, loc);
            msgBuff.append(format(" (%s: %s)",errUidMsg != null ? errUidMsg : ERROR_UID, errorUid));
        }
        errorDescriptor.setMessage(msgBuff.toString());

        if (exceptionOutputManager.isStackTraceAllowed()) {
            final StringWriter stackTraceBuffer = new StringWriter();
            final PrintWriter stackTraceWriter = new PrintWriter(stackTraceBuffer);
            t.printStackTrace(stackTraceWriter);

            errorDescriptor.addParameters(stackTraceBuffer);
        }
		errorDescriptor.setException(t);
        if (logger != null) {
            final String message;
            if (t != null) {
                message = errorDescriptor.getMessage()  + (t.getCause() != null ? ". " + t.getCause().getMessage() : "");
            } else {
                message = errorDescriptor.getMessage();
            }
            logger.error(message, errorDescriptor.getException());
        }
        return errorDescriptor;
    }

    private String extractErrorMessageForErrorCode(String errCode, Object[] args, Locale locale) {
        try {
            return messageSource.getMessage(errCode, args, locale != null ? locale : LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException nme) {
            logger.debug("Tried and failed to obtain error message for error code: " + errCode);
            return null;
        }
    }
}

