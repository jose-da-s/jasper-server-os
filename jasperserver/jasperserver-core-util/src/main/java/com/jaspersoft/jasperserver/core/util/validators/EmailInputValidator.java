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
package com.jaspersoft.jasperserver.core.util.validators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

/**
 * If pattern specified validates using RegExp match.
 * Otherwise using {@see InternetAddress}.
 *
 * @author schubar
 */
public class EmailInputValidator<T> extends RegExpValidator<T> {

    private static final Log log = LogFactory.getLog(EmailInputValidator.class);

    @Override
    public boolean isValid(T email) {
        if (email == null) {
            return false;
        }

        if (this.getPattern() != null) {
            return super.isValid(email);

        } else {
            boolean result = true;
            try {
                InternetAddress emailAddress = new InternetAddress(email.toString());
                emailAddress.validate();
            } catch (AddressException ex) {
                result = false;
                if (log.isDebugEnabled()) { log.debug(String.format("Email address %s not valid.", email)); }
            }

            return result;
        }
    }
}
