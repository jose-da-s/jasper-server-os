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
package com.jaspersoft.jasperserver.api.security.externalAuth;

import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.client.MetadataUserDetails;
import com.jaspersoft.jasperserver.api.security.externalAuth.processors.ExternalUserProcessor;
import com.jaspersoft.jasperserver.api.security.externalAuth.processors.InternalUserService;
import com.jaspersoft.jasperserver.api.security.externalAuth.processors.ProcessorData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.inject.Inject;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

import static com.jaspersoft.jasperserver.api.security.externalAuth.processors.ProcessorData.Key.EXTERNAL_AUTHORITIES;
import static com.jaspersoft.jasperserver.api.security.externalAuth.processors.ProcessorData.Key.EXTERNAL_AUTH_DETAILS;
import static com.jaspersoft.jasperserver.api.security.externalAuth.processors.ProcessorData.Key.EXTERNAL_LOADED_DETAILS;

/**
 *
 *     
 * @author dlitvak
 *
 */
public class ExternalDataSynchronizerImpl implements ExternalDataSynchronizer, InitializingBean {

	private static final Logger logger = LogManager.getLogger(ExternalDataSynchronizerImpl.class);


	private ExternalUserDetailsService externalUserDetailsService = new EmptyExternalUserDetailsService();
    private List<ExternalUserProcessor> externalUserProcessors;

	@Inject
	private PlatformTransactionManager transactionManager;

	@Inject
	private InternalUserService internalUserService;
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(externalUserProcessors, "externalUserProcessors must not be null");
        Assert.notEmpty(externalUserProcessors, "externalUserProcessors must not be empty: at least external user setup processor must be present to enter th user into JRS DB.");
    }

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public void synchronize() {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (logger.isDebugEnabled())
			    logger.debug("Authentication token: " + (auth == null ? "none" : auth));

			if (auth != null && auth.getPrincipal() != null &&
					auth.isAuthenticated() &&
					!(auth instanceof AnonymousAuthenticationToken)) {

				//make sure this thread has fresh ProcessorData each time it invokes synchronize()
				ProcessorData.getInstance().clearData();

				loadExternalUserDetailsToProcessorData(auth);
				boolean continueExecution = true;
				if (internalUserService !=null) {
					User user = internalUserService.getUser();
					if (user == null) {
						synchronized (this) {
							user = internalUserService.getUser();
							if (user == null) {
								TransactionStatus transaction = null;
								try {
									transaction = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
									for (ExternalUserProcessor processor : externalUserProcessors)
										processor.process();
									transaction.flush();
									transactionManager.commit(transaction);
									continueExecution = false;
								}
								catch (Exception e) {
									logger.error("Exception occurred during synchronization in synchronized mode", e.getMessage());
									continueExecution = true;
								} finally {
									if (transaction != null && !transaction.isCompleted()) {
										transactionManager.rollback(transaction);
										logger.error("New transaction got rollback in synchronized mode. Will try again and proceed in non-synchronized mode");
									}
								}
							}
						}
					}
				}
				if (continueExecution) {
					for  (ExternalUserProcessor processor : externalUserProcessors)
						processor.process();
				}
				Authentication newAuth = SecurityContextHolder.getContext().getAuthentication();
				// The authentication can be null if there are no roles. This sets the anonymous user as the
				// logged-in user, if the anonymousUserFilter is in the chain
				if (newAuth != null && newAuth.getPrincipal() instanceof MetadataUserDetails) {
					MetadataUserDetails newPrincipal = (MetadataUserDetails) newAuth.getPrincipal();

					// Keep a hold of the original principal: it may be useful later
					newPrincipal.setOriginalAuthentication(auth);
				}
			}
		}
		catch(DisabledException disabledException){
			throw disabledException;
		}
		catch (Exception e) {
			logger.error("Error during synchronization", e);
			// Because exception happened during successful authentication processing,
			// we need to clear the session of the authentication token.
			ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
			HttpSession sess = attr.getRequest().getSession();
			if (sess != null)
				sess.removeAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
			throw new AuthenticationServiceException("Error during synchronization", e);
		}
	}

	/**
	 * This method merges external user data from Authentication and ExternalUserDetailsService and transfers them
	 * to ProcessorData.  ExternalUserProcessor's process ProcessorData into internal database entries.
	 *
	 * @param auth
	 */
	protected void loadExternalUserDetailsToProcessorData(final Authentication auth) {
		final Object principal = auth.getPrincipal();
		if (principal == null)
			throw new JSException("Principal is null.");

		final String username = principal instanceof UserDetails ? ((UserDetails)principal).getUsername() : principal.toString();
		final List<Map<String, Object>> userDetails = externalUserDetailsService.loadDetails(username);

		if (logger.isDebugEnabled())
			logger.debug("Loaded " + (userDetails != null ? userDetails.size() : "userDetails=null") + " userDetails for user " + username + ".");

		final ProcessorData processorData = ProcessorData.getInstance();

		if (userDetails != null && !userDetails.isEmpty())
			processorData.addData(EXTERNAL_LOADED_DETAILS, userDetails.get(0));

		processorData.addData(EXTERNAL_AUTH_DETAILS, auth.getPrincipal());
		processorData.addData(EXTERNAL_AUTHORITIES, auth.getAuthorities());
	}


	protected ExternalUserDetailsService getExternalUserDetailsService() {
		return externalUserDetailsService;
	}

	public void setExternalUserDetailsService(ExternalUserDetailsService externalUserDetailsService) {
		Assert.notNull(externalUserDetailsService, "Cannot set user details service to null.");
		this.externalUserDetailsService = externalUserDetailsService;
	}

	public void setExternalUserProcessors(List<ExternalUserProcessor> externalUserProcessors) {
		Assert.notNull(externalUserProcessors, "Cannot set empty processor list.");
		this.externalUserProcessors = externalUserProcessors;
	}

	protected List<ExternalUserProcessor> getExternalUserProcessors() {
		return externalUserProcessors;
	}
}

