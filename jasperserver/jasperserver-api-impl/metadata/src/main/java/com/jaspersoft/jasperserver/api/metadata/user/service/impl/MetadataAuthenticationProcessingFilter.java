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

import com.jaspersoft.jasperserver.api.common.util.spring.StaticApplicationContext;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.client.MetadataUserDetails;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;

/**
 * To be used as part of an Acegi FilterChainProxy.
 * 
 * An authentication can exist that is not based on our internal security mechanism, such
 * as using an external LDAP service. This filter will assume that the external authentication
 * is what is wanted, and will:
 * 
 * <ul>
 *     <li>create a user in the metadata if it does not exist, adding any default internal roles</li>
 *     <li>synchronize the external roles with the user profile, adding and removing external roles</li>
 * </ul>
 *     
 * @author swood
 *
 * @deprecated deprecated per emerald SSO work
 */
@Deprecated
public class MetadataAuthenticationProcessingFilter implements Filter, InitializingBean {

	private static final Log log = LogFactory.getLog(MetadataAuthenticationProcessingFilter.class);

	protected ExternalUserService externalUserService;

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(externalUserService, "externalUserService must be set");
    }

    /**
     * Does nothing - we reply on IoC lifecycle services instead.
     *
     * @param ignored not used
     *
     * @throws ServletException DOCUMENT ME!
     */
    public void init(FilterConfig ignored) throws ServletException {}

    /**
     * Does nothing - we reply on IoC lifecycle services instead.
     */
    public void destroy() {}

    public void doFilter(ServletRequest request, ServletResponse response,
        FilterChain chain) throws IOException, ServletException {

		//##########################################################################################################
		// This code was inserted here for legacy reasons.  In 5.1, there have been significant changes
		// to SSO, whereby this filter has been removed from the filter chain.  Upon numerous complaints
		// (Redhat), we decided to reinsert the filter back, but make its execution conditional on the absence
		// of proxyAuthenticationProcessingFilter bean.  The latter would mean that SSO is configured the
		// old way, and the filter will be executed as pre-5.1.
		//##########################################################################################################
		//[Bug 33285] skip JIAuthenticationSynchronizer , if external auth is configured the new way.
		boolean proxyAuthProcessingFilterBeanPresent =
				StaticApplicationContext.getApplicationContext().containsBean("proxyAuthenticationProcessingFilter");
		if (proxyAuthProcessingFilterBeanPresent) {
			log.info("Found proxyAuthenticationProcessingFilter bean. External authentication has been setup post-5.1 way");
			chain.doFilter(request, response);
			return;
		}
		//##########################################################################################################

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (log.isDebugEnabled()) {
        	if (auth == null) {
        		log.debug("No authentication token");
        	} else {
        		log.debug("Authentication token: '" + auth + "'");
        	}
        }
        // If we have authenticated, but not against an internal metadata store,
        // synch up with a possibly new user in our repository

        if (auth != null && auth.getPrincipal() != null &&
        		!(auth.getPrincipal() instanceof MetadataUserDetails) &&
                !(auth instanceof AnonymousAuthenticationToken)) {

            User principalUser = getExternalUserService().maintainInternalUser();
            getExternalUserService().makeUserLoggedIn(principalUser);

            if (log.isDebugEnabled()) {
                log.debug(
                    "Populated SecurityContextHolder with JS authentication: '"
                    + SecurityContextHolder.getContext().getAuthentication()
                    + "'");
            }

            Authentication newAuth = SecurityContextHolder.getContext().getAuthentication();
            
            // The authentication can be null if there are no roles. This sets the anonymous user as the
            // logged-in user, if the anonymousUserFilter is in the chain
            
            if (newAuth != null && newAuth.getPrincipal() instanceof MetadataUserDetails) {
            	MetadataUserDetails newPrincipal = (MetadataUserDetails) newAuth.getPrincipal();

            	// Keep a hold of the original principal: it may be useful
            	// later
            	newPrincipal.setOriginalAuthentication(auth);
            }
        }
        
        chain.doFilter(request, response);

        if (log.isDebugEnabled()) {
            log.debug(
                "After chain, JI metadata token is: '"
                + SecurityContextHolder.getContext().getAuthentication()
                + "'");
        }
    }


	public ExternalUserService getExternalUserService() {
		return externalUserService;
	}

	public void setExternalUserService(ExternalUserService externalUserService) {
		this.externalUserService = externalUserService;
	}

}
