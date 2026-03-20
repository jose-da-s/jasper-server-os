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
package com.jaspersoft.jasperserver.war;

import java.util.Locale;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang.LocaleUtils;
import org.springframework.web.servlet.LocaleResolver;

import com.jaspersoft.jasperserver.war.common.JasperServerConstImpl;

/**
 * @author Ionut Nedelcu (ionutned@users.sourceforge.net)
 * @version $Id
 */
public class JSSessionLocaleResolver implements LocaleResolver
{
	public Locale resolveLocale(HttpServletRequest request)
	{
		String sessionAttribute = JasperServerConstImpl.getUserLocaleSessionAttr();

        Locale locale =null;
        try {
            locale= (Locale)request.getSession().getAttribute(sessionAttribute);
        }
        catch(Exception e){}
        finally {


            if (locale == null) {
                //Try to get locale from cookies for login page, see bug #30500
                locale = getLocaleFromCookies(request);
            }

            if (locale == null) {
                locale = request.getLocale();
            }
        }
        return locale;
	}

    private Locale getLocaleFromCookies(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(JasperServerConstImpl.getUserLocaleSessionAttr())) {
                    if (cookie.getValue() != null && cookie.getValue().length() > 0) {
                        return LocaleUtils.toLocale(cookie.getValue());
                    }

                    break;
                }
            }
        }

        return null;
    }

	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale)
	{
		String sessionAttribute = JasperServerConstImpl.getUserLocaleSessionAttr();
		request.getSession().setAttribute(sessionAttribute, locale);
	}
}

