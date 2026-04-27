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

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;


public class TrailingSlashFilter implements Filter {


@Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

      
        String requestUri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();

        if(requestUri.endsWith("/") && requestUri.length() > contextPath.length() + 1){
           
             String newUrl = requestUri.substring(0, requestUri.length() - 1);

            String queryString = httpRequest.getQueryString();
              if (StringUtils.hasText(queryString)) {
                newUrl += "?" + queryString;
              }
            httpResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            httpResponse.setHeader("Location", newUrl);
            return;
        }

        chain.doFilter(request, response);

    }

    @Override
    public void destroy() {

    }
}