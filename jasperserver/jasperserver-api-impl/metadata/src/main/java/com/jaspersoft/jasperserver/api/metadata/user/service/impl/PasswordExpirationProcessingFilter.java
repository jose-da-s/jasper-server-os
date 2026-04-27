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

package com.jaspersoft.jasperserver.api.metadata.user.service.impl;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.Authentication;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;


import com.jaspersoft.jasperserver.api.metadata.user.service.UserAuthorityService;

/**
 * @author achan
 *
 */
public class PasswordExpirationProcessingFilter implements Filter, InitializingBean {

	
    private UserAuthorityService userService;
    private String passwordExpirationInDays;


    protected boolean isPasswordExpired(ExecutionContext context, Authentication auth, int nDays) {
        return userService.isPasswordExpired(context, auth.getName(), nDays);
    }

	public void doFilter(ServletRequest request, ServletResponse response,
	        FilterChain chain) throws IOException, ServletException {
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();		
		if (auth != null) {
		   // skip password expiration check if from trusted host
		   String fromTrustedHost = (String)request.getAttribute("fromTrustedHost");
		   if ("true".equals(fromTrustedHost)) {
			   request.removeAttribute("fromTrustedHost");
			   chain.doFilter(request, response);
			   return;
		   }
			
		   
		   // get expiration date
		   int nDays = 0;
		   try {
			   nDays = Integer.parseInt(passwordExpirationInDays);
		   } catch (NumberFormatException e) {}		
           if (nDays > 0) {
		      if (isPasswordExpired(null, auth, nDays)) {
			     SecurityContextHolder.getContext().setAuthentication(null);
			     chain.doFilter(request, response);
			     return;
		      }
           }
		}
		request.removeAttribute("fromTrustedHost");
		chain.doFilter(request, response);
	}
	
    public void afterPropertiesSet() throws Exception {
    }
    
    public void destroy() {}
    
    public void init(FilterConfig arg0) throws ServletException {}

	public UserAuthorityService getUserService() {
		return userService;
	}

	public void setUserService(UserAuthorityService userService) {
		this.userService = userService;
	}

	public String getPasswordExpirationInDays() {
		return passwordExpirationInDays;
	}

	public void setPasswordExpirationInDays(String passwordExpirationInDays) {
		this.passwordExpirationInDays = passwordExpirationInDays;
	}
	
    
	
}

