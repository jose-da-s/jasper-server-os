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
package com.jaspersoft.jasperserver.api.security.validators;

import com.jaspersoft.jasperserver.api.security.params.LoginParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;

import jakarta.servlet.ServletRequest;

/**
 * DEPRECATED!
 * Validation for login parameters/values.
 */
public class LoginValidator// extends ValidatorImpl
{
    private static final Logger LOG = LogManager.getLogger(LoginValidator.class);
    private final String context = this.getClass().getName();

    /**
     * @inheritDoc
     */
    public boolean validate(ServletRequest request) throws ValidationException, IntrusionException
    {
        boolean status = true;
        String validParamName = null;
        String validParamValue = null;

        StringBuilder paramName = new StringBuilder();
        StringBuilder paramValue = new StringBuilder();
        for (LoginParams p : LoginParams.values())
        {
            paramName.append(p.getParamName());
            if (paramName.length() > 0)
            {
                String param = request.getParameter(paramName.toString());
                if (param != null && param.length() > 0 && !param.equalsIgnoreCase("null"))
                {
                    paramValue.append(param);
                    //status = validate(paramValue.toString(), p.getValueValidationKey(), p.getMaxLength(), p.getAllowNull(), context);
                    LOG.debug("SAFE = [" + paramName + "] [" + paramValue + "]");
                }
            }

            // reset
            paramName.setLength(0);
            paramValue.setLength(0);
        }

        return status;
    }

}

