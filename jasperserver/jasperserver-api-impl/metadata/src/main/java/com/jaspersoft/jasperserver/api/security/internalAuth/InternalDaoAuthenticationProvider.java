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
package com.jaspersoft.jasperserver.api.security.internalAuth;

import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.common.configuration.LoginLockoutConfig;
import com.jaspersoft.jasperserver.api.common.error.handling.JSEmptyCredentialsException;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.LogEventService;
import com.jaspersoft.jasperserver.api.metadata.user.domain.TenantQualified;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.client.MetadataUserDetails;
import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.hibernate.RepoExternalUserLoginEvent;
import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.hibernate.RepoTenant;
import com.jaspersoft.jasperserver.api.metadata.user.service.ExternalUserLoginEventService;
import com.jaspersoft.jasperserver.api.metadata.user.service.TenantService;
import com.jaspersoft.jasperserver.api.metadata.user.service.UserAuthorityService;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.UserIsNotInternalException;
import com.jaspersoft.jasperserver.api.security.SystemLoggedInUserStorage;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Date;

/**
 *  InternalDaoAuthenticationProvider overrides successful authentication token creation to distinguish
 *  our internal authentication against jaspersoft database versus external authentication against any other
 *  source.
 *
 * User: dlitvak
 * Date: 11/14/12
 */
public class InternalDaoAuthenticationProvider extends DaoAuthenticationProvider {
	public static final String NUMBER_OF_LOGIN_ATTEMPTS_REMAINING_SESSIONATTR = "numberOfLoginAttemptsRemaining";
	public static final String IS_USER_LOCKED_SESSIONATTR = "isUserLocked";
	public static final String USER_PRINCIPAL_SESSIONATTR = "userPrincipal";

    public static final String EMPTY_CREDENTIALS_SESSIONATTR = "emptyCredentials" ;

    SystemLoggedInUserStorage systemLoggedInUserStorage;
	LogEventService loggingService;
	ExternalUserLoginEventService externalUserLoginEventService;
	String qualifiedNameSeparator;
	private static final Logger logger = Logger.getLogger(InternalDaoAuthenticationProvider.class);

	@Autowired
	MessageSource messageSource;

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
												  UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
		if (!(userDetails instanceof MetadataUserDetails))
				throw new UserIsNotInternalException("User needs to be internal to be authenticated by " + this.getClass());
		if (((MetadataUserDetails)userDetails).isExternallyDefined()) {
			try {
				externalUserCheck(userDetails, authentication);
			} catch (UserIsNotInternalException e) {
				// Handled below, becomes BadCredentialsException
			}
		}

		int allowedLoginAttempts = Integer.parseInt(getUserLockAllowedLoginAttempts());

		// in case of connection request from jconsole for jmx, httpsession won't be present
		// so authentication checks should happen without user login failed attempts checking
		if(allowedLoginAttempts <= 0 || RequestContextHolder.getRequestAttributes() == null){
			//super.additionalAuthenticationChecks(userDetails,authentication);
			/**
			 * looks like a bug in spring-security-core-5.7.2.jar {@link DaoAuthenticationProvider}:38,
			 * parameters are passed in reverse order than expected
			 * if call goes to super.additionalAuthenticationChecks(userDetails,authentication), it will result in ERROR
			 * for Illegal argument
			 *
			 * FIX: passed correct parameters in the implementation below
			 * It should be evaluated again in future spring security upgrade
 			 */
			if (authentication.getCredentials() == null) {
				logger.debug("Failed to authenticate since no credentials provided");
				throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
			} else {
				String presentedPassword = authentication.getCredentials().toString();
				if (!super.getPasswordEncoder().matches(userDetails.getPassword(), presentedPassword)) {
					logger.debug("Failed to authenticate since password does not match stored value");
					throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
				}
			}
		}
		else {
			additionalAuthenticationChecksForNumberOfFailedAttempts(authentication, userDetails,getHttpSession());
		}
	}

	private void additionalAuthenticationChecksForNumberOfFailedAttempts
			(UsernamePasswordAuthenticationToken authentication, UserDetails userDetails, HttpSession httpSession) {
		int allowedLoginAttempts = Integer.parseInt(getUserLockAllowedLoginAttempts());
		String presentedPassword = authentication.getCredentials().toString();

		int numberOfLoginAttemptsRemaining = 0;
		boolean isUserLocked = false;
		if (!super.getPasswordEncoder().matches(userDetails.getPassword(), presentedPassword)) {
			int numberOfFailedAttempts = ((MetadataUserDetails) userDetails).getNumberOfFailedLoginAttempts();
			numberOfFailedAttempts++;
			if(numberOfFailedAttempts >= allowedLoginAttempts){
				((MetadataUserDetails)userDetails).setNumberOfFailedLoginAttempts(numberOfFailedAttempts);
				((MetadataUserDetails)userDetails).setEnabled(false);
				isUserLocked = true;
				logger.debug("User "+userDetails.getUsername() +" has been locked out.");
				//createLogEvent(userDetails,authentication);
			}
			else{
				((MetadataUserDetails)userDetails).setNumberOfFailedLoginAttempts(numberOfFailedAttempts);
				numberOfLoginAttemptsRemaining = allowedLoginAttempts - numberOfFailedAttempts;
			}
			//((UserAuthorityServiceImpl)super.getUserDetailsService()).putUser(null,((MetadataUserDetails)user));
			httpSession.setAttribute(IS_USER_LOCKED_SESSIONATTR,isUserLocked);
			httpSession.setAttribute(NUMBER_OF_LOGIN_ATTEMPTS_REMAINING_SESSIONATTR,numberOfLoginAttemptsRemaining);
			httpSession.setAttribute(USER_PRINCIPAL_SESSIONATTR,authentication.getName());
			updateUser(userDetails,authentication);
			throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials","Bad credentials. Number of login attempts remaining: "+numberOfLoginAttemptsRemaining));
		}
		else{
			logger.debug("Authentication Successful for user: "+userDetails.getUsername());
			((MetadataUserDetails)userDetails).setNumberOfFailedLoginAttempts(0);
			updateUser(userDetails,authentication);
		}
	}

	private void updateUser(UserDetails userDetails, Authentication authentication) {
		try{
			//adding the exception handling here to handle issues that may arise when the user record is updated.
			((UserAuthorityService) getUserDetailsService()).putUser(null,((MetadataUserDetails)userDetails));
			//updating the external user login event, just in case the app has multiple authentication mechanisms.
			RepoTenant repoTenant = (RepoTenant) externalUserLoginEventService.getTenantForExternalUserLoginEvent(
					((MetadataUserDetails) userDetails).getTenantId(),userDetails.getUsername());
			if(repoTenant == null)
				repoTenant = (RepoTenant) externalUserLoginEventService.getTenantForExternalUserLoginEvent(TenantService.ORGANIZATIONS,userDetails.getUsername());

			RepoExternalUserLoginEvent repoExternalUserLoginEvent = (RepoExternalUserLoginEvent)
					externalUserLoginEventService.getExternalUserLoginEventByUsernameAndTenantId(userDetails.getUsername(),repoTenant.getTenantId());
			if(null != repoExternalUserLoginEvent){
				repoExternalUserLoginEvent.setEnabled(userDetails.isEnabled());
				repoExternalUserLoginEvent.setRecordLastUpdateDate(new Date());
				repoExternalUserLoginEvent.setNumberOfFailedLoginAttempts(((MetadataUserDetails) userDetails).getNumberOfFailedLoginAttempts());
				externalUserLoginEventService.updateExternalUserLoginEvent(repoExternalUserLoginEvent);
			}
		}
		catch(JSException jsException){
			throw jsException;
		}
		catch (Exception exception){
			logger.warn("Error while updating user record for "+userDetails.getUsername()+" | "
					+exception.getMessage(),exception);
		}
	}

	private HttpSession getHttpSession() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		Assert.notNull(requestAttributes,"RequestAttributes is null. Unexpected error...");
		HttpServletRequest httpServletRequest = ((ServletRequestAttributes)requestAttributes).getRequest();
		Assert.notNull(httpServletRequest,"HttpServletRequest is null. Unexpected error...");
		return httpServletRequest.getSession();
	}

	protected void externalUserCheck(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) {
		if (authentication instanceof InternalizableAuthentication
				&& ((InternalizableAuthentication) authentication).canInternalize()) {
			//authentication object indicates that it can be internalized, no further checks needed
			return;
		}

		User sysUser = loadUserFromSystemStorageByAuth(authentication);
		if (sysUser == null) {
			throw new UserIsNotInternalException("User needs to be internal to be authenticated by " + this.getClass());
		} else {
			((MetadataUserDetails) userDetails).setPassword(sysUser.getPassword());
		}
	}

	/**
	 * Create InternalAuthenticationToken after successful authentication against JRS database
	 *
	 * @param principal that should be the principal in the returned object (defined by the {@link
	 *        #isForcePrincipalAsString()} method)
	 * @param authentication that was presented to the provider for validation
	 * @param user that was loaded by the implementation
	 *
	 * @return the successful authentication token
	 */
	@Override
	protected Authentication createSuccessAuthentication(Object principal, Authentication authentication,
														 UserDetails user) {
		InternalAuthenticationTokenImpl result = new InternalAuthenticationTokenImpl(principal,
				authentication.getCredentials(), user.getAuthorities());
		result.setDetails(authentication.getDetails());

		return result;
	}

	private User loadUserFromSystemStorageByAuth(UsernamePasswordAuthenticationToken authentication) {
		User sysUser = null;
		if (authentication.getPrincipal() instanceof String && authentication.getCredentials() instanceof String) {
			String userName = (String)authentication.getPrincipal();
			String password = (String)authentication.getCredentials();
			sysUser = systemLoggedInUserStorage != null ? systemLoggedInUserStorage.loadUserByNameAndPassword(userName, password) : null;
		}
		return sysUser;
	}

	public void externalUserAuthenticationChecks(String username,UsernamePasswordAuthenticationToken authenticationToken){
		String tenantId = getTenantId(authenticationToken);
		username = getTruncatedUsername(username);
		RepoExternalUserLoginEvent repoExternalUserLoginEvent = (RepoExternalUserLoginEvent) this.getExternalUserLoginEventService().
				getExternalUserLoginEventByUsernameAndTenantId(username,tenantId);
		if(null == repoExternalUserLoginEvent)
			createExternalUserLoginEvent(username,tenantId);
		else
			updateExternalUserLoginEvent(repoExternalUserLoginEvent);
	}

	private void updateExternalUserLoginEvent(RepoExternalUserLoginEvent repoExternalUserLoginEvent) {
		int numberOfAllowedLoginAttempts = Integer.parseInt(getUserLockAllowedLoginAttempts());
		boolean isUserLocked = false;
		int numberOfFailedLoginAttempts = repoExternalUserLoginEvent.getNumberOfFailedLoginAttempts();
		int numberOfLoginAttemptsRemaining = 0;
		if(repoExternalUserLoginEvent.isEnabled()){
			numberOfFailedLoginAttempts++;
			if(numberOfFailedLoginAttempts >= numberOfAllowedLoginAttempts){
			//disable the user
				repoExternalUserLoginEvent.setNumberOfFailedLoginAttempts(numberOfAllowedLoginAttempts);
				repoExternalUserLoginEvent.setEnabled(false);
				isUserLocked = true;
				logger.debug("User "+repoExternalUserLoginEvent.getUsername() +" has been locked out");
				this.getExternalUserLoginEventService().updateExternalUserLoginEvent(repoExternalUserLoginEvent);
			}
			else{
			repoExternalUserLoginEvent.setNumberOfFailedLoginAttempts(numberOfFailedLoginAttempts);
			numberOfLoginAttemptsRemaining = numberOfAllowedLoginAttempts - numberOfFailedLoginAttempts;
			repoExternalUserLoginEvent.setRecordLastUpdateDate(new Date());
			this.getExternalUserLoginEventService().updateExternalUserLoginEvent(repoExternalUserLoginEvent);
			}
		}
		else{
			isUserLocked = true;
			logger.debug("User "+repoExternalUserLoginEvent.getUsername() +" has been locked out");
		}
		getHttpSession().setAttribute("isUserLocked",isUserLocked);
		getHttpSession().setAttribute("numberOfLoginAttemptsRemaining",numberOfLoginAttemptsRemaining);
		getHttpSession().setAttribute("userPrincipal",repoExternalUserLoginEvent.getUsername());
	}

	private void createExternalUserLoginEvent(String username, String tenantId) {
		RepoExternalUserLoginEvent repoExternalUserLoginEvent = new RepoExternalUserLoginEvent();
		repoExternalUserLoginEvent.setUsername(username);
		repoExternalUserLoginEvent.setTenantId(tenantId);
		repoExternalUserLoginEvent.setEnabled(true);
		repoExternalUserLoginEvent.setNumberOfFailedLoginAttempts(1);
		repoExternalUserLoginEvent.setRecordCreationDate(new Date());
		this.getExternalUserLoginEventService().addNewExternalUserLoginEvent(repoExternalUserLoginEvent);
		int numberOfLoginAttemptsRemaining = Integer.parseInt(getUserLockAllowedLoginAttempts()) - 1;
		getHttpSession().setAttribute("numberOfLoginAttemptsRemaining",numberOfLoginAttemptsRemaining);
		getHttpSession().setAttribute("isUserLocked",false);
		getHttpSession().setAttribute("userPrincipal",repoExternalUserLoginEvent.getUsername());
	}

	private String parseUserNameToGetTenantId(UsernamePasswordAuthenticationToken authentication) {
		String username = (String) authentication.getPrincipal();
		if(username == null || username.isEmpty()){
			getHttpSession().setAttribute(InternalDaoAuthenticationProvider.EMPTY_CREDENTIALS_SESSIONATTR, messageSource.getMessage(
					"jsp.loginError.usernameIsBlank",null, LocaleContextHolder.getLocale()));
			throw new JSEmptyCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
		}
		int sepIndex = username.lastIndexOf(getQualifiedNameSeparator());
		if (sepIndex < 0) {
			// if no separator, treat as no tenant
			return "";
		}
		else {
			return username.substring(sepIndex + getQualifiedNameSeparator().length());
		}
	}
	public void setSystemLoggedInUserStorage(SystemLoggedInUserStorage systemLoggedInUserStorage) {
		this.systemLoggedInUserStorage = systemLoggedInUserStorage;
	}

	public String getUserLockAllowedLoginAttempts() {
		return LoginLockoutConfig.getNumberOfFailedLoginAttempts();
	}

	public LogEventService getLoggingService() {
		return loggingService;
	}

	public void setLoggingService(LogEventService loggingService) {
		this.loggingService = loggingService;
	}

	public ExternalUserLoginEventService getExternalUserLoginEventService() {
		return externalUserLoginEventService;
	}

	public void setExternalUserLoginEventService(ExternalUserLoginEventService externalUserLoginEventService) {
		this.externalUserLoginEventService = externalUserLoginEventService;
	}

	public String getQualifiedNameSeparator() {
		return qualifiedNameSeparator;
	}

	public void setQualifiedNameSeparator(String qualifiedNameSeparator) {
		this.qualifiedNameSeparator = qualifiedNameSeparator;
	}

	private String getTenantId(UsernamePasswordAuthenticationToken authentication) {
		String tId = "";
		Object details = authentication.getDetails();
		if (details instanceof TenantQualified) {
			tId = ((TenantQualified) details).getTenantId() != null ? ((TenantQualified) details).getTenantId() : "";
		}
		if(tId.isEmpty()){
			tId = parseUserNameToGetTenantId(authentication);
		}
		return tId;
	}

	private String getTruncatedUsername(String username){
		if(username.length() > 100){
			username = username.substring(0,100);
		}
		return username;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	@Override
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
}

