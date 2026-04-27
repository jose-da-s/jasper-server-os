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
package com.jaspersoft.jasperserver.api.logging.filter;

import com.jaspersoft.jasperserver.api.logging.context.LoggingContextProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author Sergey Prilukin
 * @version $Id$
 */
public abstract class BasicLoggingFilter implements Filter {

    private static final Log log = LogFactory.getLog(BasicLoggingFilter.class);

    private LoggingContextProvider loggingContextProvider;
    private Exception originalException = null;

    public void setLoggingContextProvider(LoggingContextProvider loggingContextProvider) {
        this.loggingContextProvider = loggingContextProvider;
    }

    protected abstract void beforeChain(ServletRequest request) throws ServletException;

    protected abstract void logException(ServletRequest request, Exception filterException) throws ServletException;

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            beforeChain(request);
            chain.doFilter(request, response);
        } catch (IOException e) {
            originalException = e;
            throw e;
        } catch (ServletException e) {
            originalException = e;
            throw e;
        } catch (RuntimeException e) {
            originalException = e;
            throw e;
        } finally {
            try {
                logException(request, originalException);
                originalException = null;
                loggingContextProvider.flushContext();
            } catch (Exception e) {
                log.error("Exception during flushing audit context", e);
            }
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void destroy() {
    }
}

