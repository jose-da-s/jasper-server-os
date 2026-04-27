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
package com.jaspersoft.jasperserver.core.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpSession;

/**
 * This class is used to override HttpServeletRequest so that it can return TolerantHttpSession instead of HttpSession
 *
 * Created by nthapa on 7/12/13.
 */
public class TolerantRequest extends HttpServletRequestWrapper {

    public TolerantRequest(HttpServletRequest obj)
    {
        super(obj);
    }

    @Override
    public HttpSession getSession()
    {
       return getSession(true);

    }

    /*
    * Overrides the getSession method of HttpServletRequestWrapper
    *
    *return null if HttpSession is null else returns TolerantHttpSession
    *
     */
    @Override
    public HttpSession getSession(boolean create)
    {
        HttpSession session = super.getSession(create);
        if (session != null) {
            return new TolerantHttpSession(session);
        } else {
            return session;
        }

    }
}

