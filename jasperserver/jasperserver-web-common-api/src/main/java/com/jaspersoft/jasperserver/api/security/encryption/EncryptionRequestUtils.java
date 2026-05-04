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
package com.jaspersoft.jasperserver.api.security.encryption;

import com.jaspersoft.jasperserver.api.security.SecurityConfiguration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * When encryption is turned on, the decrypted value is placed in the request attribute
 * by the same name because the original value in the request parameter is ummutable.
 *
 * @author nmacaraeg
 */
public class EncryptionRequestUtils {
    public static final String LEGACY_SPRING_SECURITY_FORM_USERNAME_KEY = "j_username";
    public static final String LEGACY_SPRING_SECURITY_FORM_PASSWORD_KEY = "j_password";

    /**
     * If encryption is turned on, then the decrypted value should be in the attribute by the same name;
     * otherwise, use the original parameter value. Note that we are pulling a string list from the attribute
     * and, for now, the assumption is that the first index in the list is the decrypted value we want.
     *
     * @param request a HttpServletRequest.
     * @param  keyName the key name for the parameter or attribute.
     * @return Either the decrypted value or the original parameter value.
     */
    public static String getValue(ServletRequest request, String keyName) {
        String originalParameterValue = request.getParameter(keyName);
        if (SecurityConfiguration.isEncryptionOn())  {
            List<String> decryptedValues = (List<String>)request.getAttribute(EncryptionFilter.DECRYPTED_PREFIX + keyName);

            return (decryptedValues != null && decryptedValues.size() > 0) ? decryptedValues.get(0) : originalParameterValue;
        }

        return originalParameterValue;
    }

    public static String getValueWithLegacySupport(ServletRequest request, String keyName) {
        String value = getValue(request,keyName);
        if (value==null && keyName.equals(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY)) {
            value = getValue(request,LEGACY_SPRING_SECURITY_FORM_PASSWORD_KEY);
        }
        if (value==null && keyName.equals(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY)) {
            value = getValue(request,LEGACY_SPRING_SECURITY_FORM_USERNAME_KEY);
        }

        return value;
    }

}

