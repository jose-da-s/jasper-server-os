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

import com.jaspersoft.jasperserver.api.metadata.user.domain.Role;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.client.MetadataUserDetails;
import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.client.TenantAwareGrantedAuthority;
import com.jaspersoft.jasperserver.api.metadata.user.service.UserAuthorityService;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * @author achan
 *
 */
public class JIPortletAuthenticationProcessingFilter  implements Filter, InitializingBean{

    //~ Static fields/initializers =============================================

    private static final Log logger = LogFactory.getLog(JIPortletAuthenticationProcessingFilter.class);

    //~ Instance fields ========================================================

    private List trustedIpAddress; 
    private UserAuthorityService userService;

    //~ Methods ================================================================

    public void afterPropertiesSet() throws Exception {
    }

    public void destroy() {}

    public void doFilter(ServletRequest request, ServletResponse response,
        FilterChain chain) throws IOException, ServletException {
   	
    	
        if (logger.isDebugEnabled()) {
            logger.debug("Trusted Host Authentication.");
        }
   	
    	String incomingIPAddress = request.getRemoteAddr(); 
        // if not from trusted host, skip this filter totally
    	if ((incomingIPAddress == null) || (!isFromTrustedHost(incomingIPAddress))) {
    		chain.doFilter(request, response);
    		return;
    	}
    	
        if (logger.isDebugEnabled()) {
            logger.debug("Requested from Trusted Host IP:" + incomingIPAddress);
        }
    	
        List roleList = new ArrayList();
    	
        if (!(request instanceof HttpServletRequest)) {
            throw new ServletException("Can only process HttpServletRequest");
        }

        if (!(response instanceof HttpServletResponse)) {
            throw new ServletException("Can only process HttpServletResponse");
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        Credentials credentials = getUserCredentials(httpRequest);
        String userName = credentials.getUserName();
        String password = credentials.getPassword();

        if ((userName == null) || (userName.trim().length() == 0)) {
        	chain.doFilter(request, response);
        	return;
        }
        
    	// skip any actions when the authentication object already exists.
    	Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
    	if ((existingAuth != null) && (existingAuth.getName().equals(userName)) && (existingAuth.isAuthenticated())) {
            // if already authenticated, set the trusted host flag so that 
    		// subsequent filters (such as the Basic auth filter) know it's a
    		// portlet authentication
            request.setAttribute("fromTrustedHost", "true");
            
    		chain.doFilter(request, response);
    		return;    		
    	}
        
        // starting the flow       
        // user exist
        if (doesUserExist(userName)) {    	
        	// is it an internal user
            if (isInternalUser(userName)) {
               	String oldPassword = getUserPaswordFromRepository(userName);
               	// update password if they are different
               	if (!haveSamePassword(oldPassword, password)) {
               		updatePassword(userName, password);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Updated Password for User:" + userName);
                    }
               	}
            }
            // get list of role
            roleList = getUserRoleList(userName);
        } else {        	
            if (logger.isDebugEnabled()) {
                logger.debug("Created New User:" + userName);
            }
        	// create an internal user
        	roleList.add("ROLE_USER");
        	roleList.add("ROLE_PORTLET");
            createUserWithRoles(userName, password, roleList, false);  		
        }
        SecurityContextHolder.getContext().setAuthentication(createAuthenticationObject(userName, password, roleList, request));    
        // it's authenticated thru trusted host
        request.setAttribute("fromTrustedHost", "true");
        
        if (logger.isDebugEnabled()) {
            logger.debug("Created Authentication Object within JIPortletAuthenticationProcessingFilter");
        }
        
        chain.doFilter(request, response);
    }

    public void init(FilterConfig arg0) throws ServletException {}

    protected Credentials getUserCredentials( HttpServletRequest httpRequest) {
        String header = httpRequest.getHeader("Authorization");
        String userName = null;
        String password = null;
        if ((header != null) && header.startsWith("Basic ")) {
            String base64Token = header.substring(6);
            String token = new String(Base64.decodeBase64(
                        base64Token.getBytes()));
            int delim = token.indexOf(":");

            if (delim != -1) {
            	userName = token.substring(0, delim);
            	password = token.substring(delim + 1);
            }
        }

        return new Credentials(userName, password);
    }
	
	private boolean isFromTrustedHost(String ipAddress) {
		
		if ((trustedIpAddress == null) || (ipAddress == null)) {
			return false;
		}
		if (trustedIpAddress.size() == 0) {
			return false;
		}
		for (int i=0; i<trustedIpAddress.size(); i++) {
			if (((String)(trustedIpAddress.get(i))).equals(ipAddress)) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean doesUserExist(String userName) {
		return userService.userExists(null, userName);
	}

    protected User getUser(String username) {
        return userService.getUser(null, username);
    }

	private boolean isInternalUser(String userName) {
		User user = getUser(userName);
		if (user.isExternallyDefined()) {
			return false;
		}
		return true;
	}
	
	protected void createUserWithRoles(String userName, String password, List roleList, boolean externalUser) {
		User user = userService.newUser(null);
		user.setExternallyDefined(externalUser);
		user.setUsername(userName);
		user.setPassword(password);
		user.setFullName(userName);
		user.setEnabled(true);
		user.setEmailAddress(userName);
		HashSet roleSet = new HashSet();
		for (int i=0; i<roleList.size(); i++) {
			Role role = userService.newRole(null);
			role.setRoleName((String)roleList.get(i));
			roleSet.add(role);
		}
		user.setRoles(roleSet);
		userService.putUser(null, user);	
	}
	
	protected Authentication createAuthenticationObject(String userName, String password, List roleList, ServletRequest request) {
        List<GrantedAuthority> rolesAuthrity = new ArrayList<GrantedAuthority>(roleList.size());
        for (int i=0; i<roleList.size(); i++) {
            rolesAuthrity.add(new TenantAwareGrantedAuthority((String)roleList.get(i)));
        }      
        User user = getUser(userName);
        MetadataUserDetails md = new MetadataUserDetails(user);
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(md, password, rolesAuthrity);
        authRequest.setDetails(new WebAuthenticationDetails((HttpServletRequest)request));

        return authRequest;        
	}
	
	private String getUserPaswordFromRepository(String userName) {
		User user = getUser(userName);
		return user.getPassword();
	}
	
	private boolean haveSamePassword(String oldPassword, String newPassword) {
		return (oldPassword.equals(newPassword));
	}
	
	private void updatePassword(String userName, String newPassword) {
		User user = getUser(userName);
		user.setPassword(newPassword);
		userService.putUser(null, user);
	}
	
	private List getUserRoleList(String userName) {
		List roles = new ArrayList();
		User user = getUser(userName);
		if (user != null) {
			Iterator iter = user.getRoles().iterator();
			while (iter.hasNext()) {
				roles.add(((Role)iter.next()).getRoleName());
			}
		}
		return roles;
	}


	public List getTrustedIpAddress() {
		return trustedIpAddress;
	}

	public void setTrustedIpAddress(List trustedIpAddress) {
		this.trustedIpAddress = trustedIpAddress;
	}

	public UserAuthorityService getUserService() {
		return userService;
	}

	public void setUserService(UserAuthorityService userService) {
		this.userService = userService;
	}

	protected class Credentials {
        public Credentials(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }

        private String userName;
        private String password;

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}

