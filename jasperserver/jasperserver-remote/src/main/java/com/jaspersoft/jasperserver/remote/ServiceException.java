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


package com.jaspersoft.jasperserver.remote;

import com.jaspersoft.jasperserver.api.JSException;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author gtoffoli
 */
public class ServiceException extends JSException {

    public static final int NO_ERROR = 0;
    public static final int GENERAL_ERROR = 1;
    public static final int GENERAL_ERROR2 = 2;
    public static final int EXPORT_ERROR = 4;
    public static final int FILL_ERROR = 5;
    public static final int GENERAL_REQUEST_ERROR = 6;
    private int errorCode = 0;

    public static final int RESOURCE_NOT_FOUND = HttpServletResponse.SC_NOT_FOUND;
    public static final int FORBIDDEN = HttpServletResponse.SC_FORBIDDEN;
    public static final int RESOURCE_BAD_REQUEST = HttpServletResponse.SC_BAD_REQUEST;
    public static final int INTERNAL_SERVER_ERROR = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;


    /** Creates a new instance of WSException */
    public ServiceException(int code, String message) {
        this(code, message, new Object[]{});
        
    }

    public ServiceException(String message) {
    	this(GENERAL_ERROR, message);
    }

    public ServiceException(Exception e) {
    	this(GENERAL_ERROR, e.getMessage());
    }

    public ServiceException(String message, Object[] args) {
	this(GENERAL_ERROR, message, args);
    }

    public ServiceException(int code, String message, Object[] args) {
	super(message, args);
        this.errorCode = code;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}

