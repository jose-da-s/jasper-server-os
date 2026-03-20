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
package com.jaspersoft.jasperserver.war.control;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.user.domain.Role;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.client.MetadataUserDetails;

/**
 * @author swood
 *
 */
public class LoginController extends JRBaseController {
	
	private static Log log = LogFactory.getLog(LoginController.class);

	/**
	 * Ordered list of home pages
	 */
	private List homePageByRole;
	private List homePageByRoleEntries;
	private String defaultHomePage = "modules/home";

	/*
	 * Overridden method for handling the requests
	 * @args HttpServletRequest, HttpServletResponse
	 * @returns ModelAndView - Home Page
	 */
	@RequestMapping("/home.html")
	public ModelAndView homePage(HttpServletRequest req, HttpServletResponse res)
			throws ServletException {

        if ((SecurityContextHolder.getContext() == null)
            || !(SecurityContextHolder.getContext() instanceof SecurityContext)
            || (((SecurityContext) SecurityContextHolder.getContext())
            .getAuthentication() == null)) {
        	// Should never get here!
        	return new ModelAndView("modules/loginError");
        }

        Authentication auth = SecurityContextHolder.getContext()
                                                   .getAuthentication();

        if (auth.getPrincipal() == null) {
        	// Should never get here!
        	return new ModelAndView("modules/home");
        }

        User user = (User) auth.getPrincipal();
		return new ModelAndView(getBestHomeURLForUser(user));
	}

	/**
	 * If successfully logged in, make sure we have a user in our database that corresponds
	 * to the UserDetails we have been given. This allows users that are managed and authenticated
	 * externally to us to become part of the environment.
	 * 
	 * Also forward on to the correct home page for the user, based on external configuration.
	 * 
	 * @param req
	 * @param res
	 * @return
	 * @throws ServletException
	 */
	@RequestMapping("/loginsuccess.html")
	public ModelAndView loginSuccess(HttpServletRequest req, HttpServletResponse res)
		throws ServletException {

        if ((SecurityContextHolder.getContext() == null)
            || !(SecurityContextHolder.getContext() instanceof SecurityContext)
            || (((SecurityContext) SecurityContextHolder.getContext())
            .getAuthentication() == null)) {
        	// Should never get here!
        	return new ModelAndView("modules/loginError");
        }

        Authentication auth = SecurityContextHolder.getContext()
                                                   .getAuthentication();

        if (auth.getPrincipal() == null) {
        	// Should never get here!
        	return new ModelAndView("modules/loginError");
        }

        User user;
        
        log.debug("Authentication class: " + auth.getClass() +
        		" Authentication principal class: " + auth.getPrincipal().getClass());
        
        if (!(auth.getPrincipal() instanceof MetadataUserDetails)) {

			Object principal = auth.getPrincipal();
			String username = null;

			if (principal instanceof UserDetails) {
				username = ((UserDetails) auth.getPrincipal()).getUsername();
			} else if (principal instanceof String) {
				username = (String) principal;
			}

			log.debug("External user: " + username);

			user = getUserAuthService().getUser(new ExecutionContextImpl(),
					username);
		} else {
			user = (User) auth.getPrincipal();
		}

       
        
        return new ModelAndView(getBestHomeURLForUser(user));
	}
	
	/**
	 * Given a user, find their home page based on their role
	 * 
	 * @param User
	 *            user
	 * @return Home page URL for user
	 */
	private String getBestHomeURLForUser(User user) {
		if (getHomePageByRole() == null || getHomePageByRole().size() == 0) {
			log.debug("Set home page for user: " + user.getUsername() + " to default: " + getDefaultHomePage());
			return getDefaultHomePage();
		}
		Iterator it = getHomePageByRoleEntries().iterator();
		while (it.hasNext()) {
			RoleHomePage entry = (RoleHomePage) it.next();
			
			if (hasRole(user, entry.getRoleName())) {
				log.debug("Set home page for user: " + user.getUsername() + 
								" based on role: " + entry.getRoleName() + 
								"  to: " + entry.getHomePageURL());
				return entry.getHomePageURL();
			}
			
		}
		log.debug("Set home page for user: " + user.getUsername() + " to default: " + getDefaultHomePage());
		return getDefaultHomePage();
	}
	
	/**
	 * Does this user have a role of this name?
	 * 
	 * @param User u
	 * @param String roleName
	 * @return true if user has a role with the given name
	 */
	private boolean hasRole(User u, String roleName) {
		if (u == null || u.getRoles().size() == 0) {
			log.debug("No roles on user: " + u + " to check for role: " + roleName);
			return false;
		}
		Iterator it = u.getRoles().iterator(); 
		while (it.hasNext()) {
			Role r = (Role) it.next();
			if (r.getRoleName().equalsIgnoreCase(roleName)) {
				log.debug("Found role "  + roleName + " on user: " + u);
				return true;
			}
		}
		log.debug("Found NO role "  + roleName + " on user: " + u);
		return false;
	}

	/**
	 * @return Returns the defaultHomePage.
	 */
	public String getDefaultHomePage() {
		return defaultHomePage;
	}

	/**
	 * @param defaultHomePage The defaultHomePage to set.
	 */
	public void setDefaultHomePage(String defaultHomePage) {
		this.defaultHomePage = defaultHomePage;
	}

	/**
	 * @return Returns the homePageByRole.
	 */
	public List getHomePageByRole() {
		return homePageByRole;
	}

	/**
	 * Converts basic Spring list to Map.Entry
	 * 
	 * @param homePageByRole The homePageByRole to set.
	 */
	public void setHomePageByRole(List homePageByRole) {
		this.homePageByRole = homePageByRole;
		
		if (homePageByRole == null) {
			setHomePageByRoleEntries(null);
			return;
		}
		
		List entriesList = new ArrayList(homePageByRole.size());
		
		Iterator it = homePageByRole.iterator();
		
		while (it.hasNext()) {
			String str = (String) it.next();
			
			int pos = str.indexOf('|');
			
			if (pos == -1) {
				throw new RuntimeException("Invalid home page entry (needs | to separate role and URL:" + str);
			}
			
			RoleHomePage entry = new RoleHomePage(str.substring(0, pos),str.substring(pos + 1));
			entriesList.add(entry);
		}
		
		setHomePageByRoleEntries(entriesList);
		
	}

	/**
	 * @return Returns the homePageByRoleEntries.
	 */
	public List getHomePageByRoleEntries() {
		return homePageByRoleEntries;
	}

	/**
	 * @param homePageByRoleEntries The homePageByRoleEntries to set.
	 */
	public void setHomePageByRoleEntries(List homePageByRoleEntries) {
		this.homePageByRoleEntries = homePageByRoleEntries;
	}
	
	private class RoleHomePage {
		private String roleName;
		private String homePageURL;
		
		public RoleHomePage(String roleName, String homePageURL) {
			this.roleName = roleName;
			this.homePageURL = homePageURL;
		}
		
		/**
		 * @return Returns the homePageURL.
		 */
		public String getHomePageURL() {
			return homePageURL;
		}
		/**
		 * @param homePageURL The homePageURL to set.
		 */
		public void setHomePageURL(String homePageURL) {
			this.homePageURL = homePageURL;
		}
		/**
		 * @return Returns the roleName.
		 */
		public String getRoleName() {
			return roleName;
		}
		/**
		 * @param roleName The roleName to set.
		 */
		public void setRoleName(String roleName) {
			this.roleName = roleName;
		}
	}

}

