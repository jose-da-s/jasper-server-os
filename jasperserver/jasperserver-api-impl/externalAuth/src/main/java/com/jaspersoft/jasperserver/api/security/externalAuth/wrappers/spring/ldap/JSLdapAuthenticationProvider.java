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
package com.jaspersoft.jasperserver.api.security.externalAuth.wrappers.spring.ldap;

import com.jaspersoft.jasperserver.api.JasperServerAPI;
import com.jaspersoft.jasperserver.api.common.configuration.LoginLockoutConfig;
import com.jaspersoft.jasperserver.api.common.error.handling.JSEmptyCredentialsException;
import com.jaspersoft.jasperserver.api.metadata.tenant.service.TenantPersistenceResolver;
import com.jaspersoft.jasperserver.api.metadata.user.domain.TenantQualified;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.hibernate.RepoExternalUserLoginEvent;
import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.hibernate.RepoTenant;
import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.hibernate.RepoUser;
import com.jaspersoft.jasperserver.api.metadata.user.service.ExternalUserLoginEventService;
import com.jaspersoft.jasperserver.api.metadata.user.service.UserAuthorityService;
import com.jaspersoft.jasperserver.api.security.internalAuth.InternalDaoAuthenticationProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Wrapper class for org.springframework.security.ldap.authentication.LdapAuthenticationProvider
 * @author dlitvak
 * @version $Id$
 * @since 6.0
 */
@JasperServerAPI
public class JSLdapAuthenticationProvider extends LdapAuthenticationProvider {
    private static final Logger logger = LogManager.getLogger(JSLdapAuthenticationProvider.class);
    private ExternalUserLoginEventService externalUserLoginEventService;
    private UserDetailsService userDetailsService;
    private UserAuthorityService userAuthorityService;
    private String qualifiedNameSeparator;
    private TenantPersistenceResolver tenantPersistenceResolver;

    @Autowired
    MessageSource messageSource;

    public JSLdapAuthenticationProvider(LdapAuthenticator authenticator, LdapAuthoritiesPopulator authoritiesPopulator) {
		super(authenticator, authoritiesPopulator);
	}

    public JSLdapAuthenticationProvider(LdapAuthenticator authenticator) {
        super(authenticator);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Authentication resultAuth;
        if (authentication.getName() == null || authentication.getName().trim().length() == 0) {
            getHttpSession().setAttribute(InternalDaoAuthenticationProvider.EMPTY_CREDENTIALS_SESSIONATTR, messageSource.getMessage(
                    "jsp.loginError.usernameIsBlank",null, LocaleContextHolder.getLocale()));
            throw new JSEmptyCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
        try {
            logger.info("Authenticating user using LDAP.");
            int allowedNumberOfLoginAttempts = Integer.parseInt(getUserLockAllowedLoginAttempts());
            resultAuth = (allowedNumberOfLoginAttempts <= 0) ?  super.authenticate(authentication)
                    : additionalAuthenticationChecks(authentication);
        }
        catch (IncorrectResultSizeDataAccessException irsdae) {
            logger.error(irsdae.getMessage());
            RepoExternalUserLoginEvent repoExternalUserLoginEvent = createOrUpdateRecordForExternalUserLoginEvent(authentication,false);
            setLoginPageAttributes(getHttpSession(),repoExternalUserLoginEvent);
            throw new BadCredentialsException("Found more than 1 user for the supplied credentials!", irsdae);
        }
        // This must precede AuthenticationException as it is a subsubclass of AuthenticationException
        catch (JSEmptyCredentialsException e) {
            logger.info("No username provided at login");
            throw e;
        }
        catch (AuthenticationException e) {
            logger.warn(e.getMessage());
            RepoExternalUserLoginEvent repoExternalUserLoginEvent = createOrUpdateRecordForExternalUserLoginEvent(authentication,false);
            setLoginPageAttributes(getHttpSession(),repoExternalUserLoginEvent);
/*
        considering the existing implementation for ProviderManager, a null returned here will allow the system
        to continue with the other configured authentication providers
*/
            //throw e;
            return null;
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            RepoExternalUserLoginEvent repoExternalUserLoginEvent = createOrUpdateRecordForExternalUserLoginEvent(authentication,false);
            setLoginPageAttributes(getHttpSession(),repoExternalUserLoginEvent);
            throw e;
        }
        // Assuming we reached this point, authentication must have succeeded, reset NOFA to 0
        //RepoExternalUserLoginEvent repoExternalUserLoginEvent = createOrUpdateRecordForExternalUserLoginEvent(resultAuth, true);
        //updateNofaOfInternalUsers(repoExternalUserLoginEvent);
        return resultAuth;
    }

    private Authentication additionalAuthenticationChecks(Authentication authentication) {
        logger.info("Performing additional authentication checks.");
        Authentication ldapAuthentication;
        RepoExternalUserLoginEvent repoExternalUserLoginEvent = findUserByUserNameAndTenantId(authentication);
        if(repoExternalUserLoginEvent != null){
            logger.debug("User record found in external login events.");
            //entry of this user exists in the external user login event table
            if(repoExternalUserLoginEvent.isEnabled()){
                logger.info("User status is enabled. Trying to authenticate.");
                ldapAuthentication = super.authenticate(authentication);
            }
            else{
                //User is disabled. Navigate back to login page with error messages.
                logger.debug("User "+getUsername(authentication)+" has been locked out.");
                setLoginPageAttributes(getHttpSession(),repoExternalUserLoginEvent);
                // Returning null because user is disabled/locked -- no authentication performed.
                return null;
            }
        }
        else{
            //might be a login attempt for the first time.
            logger.info("User record not found in external login events.");
            ldapAuthentication = super.authenticate(authentication);
        }
        RepoExternalUserLoginEvent repoExternalUserLoginEventAuthenticated = createOrUpdateRecordForExternalUserLoginEvent(authentication,true);
        updateNofaOfInternalUsers(repoExternalUserLoginEventAuthenticated,ldapAuthentication);
        return ldapAuthentication;
    }

    private RepoExternalUserLoginEvent findUserByUserNameAndTenantId(Authentication authentication) {
        String tenantId = getTenantId((UsernamePasswordAuthenticationToken) authentication);
        String username = getUsername(authentication);
        username = getTruncatedUsername(username);
        return (RepoExternalUserLoginEvent) this.externalUserLoginEventService.getExternalUserLoginEventByUsernameAndTenantId(username,tenantId);
    }

    private String getUsername(Authentication authentication) {
        String username = "";
        if(authentication.getPrincipal() instanceof LdapUserDetailsImpl){
            LdapUserDetailsImpl ldapUserDetails = (LdapUserDetailsImpl) authentication.getPrincipal();
            username = ldapUserDetails.getUsername();
        }
        else{
            username = (String) authentication.getPrincipal();
        }
        int sepIndex = username.lastIndexOf(getQualifiedNameSeparator());
        if (sepIndex < 0) {
            // if no separator, treat as no tenant
            return username;
        }
        return username.substring(0, sepIndex);
    }

    private String getTenantId(UsernamePasswordAuthenticationToken authentication) {
        String tId = "";
        Object details = authentication.getDetails();
        if (details instanceof TenantQualified) {
            tId = ((TenantQualified) details).getTenantId();
        }
        return tId;
    }

    private RepoExternalUserLoginEvent createOrUpdateRecordForExternalUserLoginEvent(Authentication authentication, boolean isAuthenticated) {
        RepoExternalUserLoginEvent repoExternalUserLoginEvent;
        HttpSession httpSession = getHttpSession();
        httpSession.setAttribute("userPrincipal",authentication.getPrincipal());
        String username = getTruncatedUsername(authentication.getName());
        repoExternalUserLoginEvent = (RepoExternalUserLoginEvent) this.externalUserLoginEventService.getExternalUserLoginEventByUsernameAndTenantId(username,
                getTenantId((UsernamePasswordAuthenticationToken) authentication));
        return repoExternalUserLoginEvent == null ? createRecordForExternalLoginEvent(authentication, isAuthenticated)
                : updateRecordForExternalLoginEvent(repoExternalUserLoginEvent, isAuthenticated, authentication);
    }

    private RepoExternalUserLoginEvent updateRecordForExternalLoginEvent(RepoExternalUserLoginEvent repoExternalUserLoginEvent, boolean isAuthenticated, Authentication authentication) {
        //record exists in the external user login events table
        logger.info("Updating the user record in external user login events");
        if (!isAuthenticated) {
            //login attempt failed for the user
            setNumberOfFailedLoginAttemptsAndUserEnabledStatus(repoExternalUserLoginEvent);
            setLoginPageAttributes(getHttpSession(),repoExternalUserLoginEvent);
        } else {
            //user is authenticated
            repoExternalUserLoginEvent.setNumberOfFailedLoginAttempts(0);
            repoExternalUserLoginEvent.setRecordLastUpdateDate(new Date());
        }
        setTenantForExternalUserLoginEvent(repoExternalUserLoginEvent, authentication);
        if(isAuthenticated){
            logger.debug("Authentication Successful. Setting NOFA to 0 for user "+repoExternalUserLoginEvent.getUsername());
            this.getExternalUserLoginEventService().updateExternalUserLoginEvent(repoExternalUserLoginEvent);
        }
        return repoExternalUserLoginEvent;
    }

    private void updateNofaOfInternalUsers(RepoExternalUserLoginEvent repoExternalUserLoginEvent, Authentication ldapAuthentication) {
        DetachedCriteria criteria = DetachedCriteria.forClass(RepoUser.class);
        criteria.add(Restrictions.eq("username", repoExternalUserLoginEvent.getUsername()));
        RepoTenant repoTenant = getTenantPersistenceResolver().getPersistentTenant(repoExternalUserLoginEvent.getTenantId(),true);
        criteria.add(Restrictions.eq("tenant",repoTenant));
        List<User> internalUsers = getUserAuthorityService().getUsersByCriteria(null,criteria);
        for(User user : internalUsers){
            user.setNumberOfFailedLoginAttempts(repoExternalUserLoginEvent.getNumberOfFailedLoginAttempts());
            logger.debug("Setting NOFA to 0 for internal user "+repoExternalUserLoginEvent.getUsername());
            ((UserAuthorityService)getUserDetailsService()).putUser(null,user);
        }
    }

    private RepoExternalUserLoginEvent createRecordForExternalLoginEvent(Authentication authentication, boolean isAuthenticated) {
        logger.info("Creating a new record in external user login events");
        RepoExternalUserLoginEvent newRecord = createNewExternalUserLoginEvent(authentication);
        newRecord.setNumberOfFailedLoginAttempts(isAuthenticated ? 0 : 1);
        setTenantForExternalUserLoginEvent(newRecord, authentication);
        //if the user is authenticated, the record will be created in LdapExternalTenantProcessor
        //if the user is not authenticated, the user credentials will be validated against DB and record creation will execute in InternalDaoAuthenticationProvider.
        return newRecord;
    }

    private void setTenantForExternalUserLoginEvent(RepoExternalUserLoginEvent newRecord,Authentication authentication) {
        newRecord.setTenantId(getTenantId((UsernamePasswordAuthenticationToken) authentication));
    }

    private void setLoginPageAttributes(HttpSession httpSession, RepoExternalUserLoginEvent repoExternalUserLoginEvent) {
        int allowedLoginAttempts = Integer.parseInt(getUserLockAllowedLoginAttempts());
        int numberOfLoginAttemptsRemaining = allowedLoginAttempts - repoExternalUserLoginEvent.getNumberOfFailedLoginAttempts();
        httpSession.setAttribute("userPrincipal",repoExternalUserLoginEvent.getUsername());
        if(repoExternalUserLoginEvent.isEnabled()){
            httpSession.setAttribute("numberOfLoginAttemptsRemaining",numberOfLoginAttemptsRemaining);
            httpSession.setAttribute("isUserLocked",false);
        }
        else{
            // since user is disabled/locked, set the isUserLocked property to true.
            httpSession.setAttribute("isUserLocked",true);
        }
    }

    private HttpSession getHttpSession() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        Assert.notNull(requestAttributes,"RequestAttributes is null. Unexpected Error");
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes)requestAttributes).getRequest();
        Assert.notNull(httpServletRequest,"HttpServletRequest is null. Unexpected Error");
        return httpServletRequest.getSession();
    }

    private RepoExternalUserLoginEvent createNewExternalUserLoginEvent(Authentication authentication) {
        RepoExternalUserLoginEvent repoExternalUserLoginEvent = new RepoExternalUserLoginEvent();
        String username = getUsername(authentication);
        username = getTruncatedUsername(username);
        repoExternalUserLoginEvent.setUsername(username);
        repoExternalUserLoginEvent.setRecordCreationDate(new Date());
        repoExternalUserLoginEvent.setEnabled(true);
        return repoExternalUserLoginEvent;
    }

    private void setNumberOfFailedLoginAttemptsAndUserEnabledStatus(RepoExternalUserLoginEvent repoExternalUserLoginEvent) {
        int numberOfFailedAttempts = repoExternalUserLoginEvent.getNumberOfFailedLoginAttempts();
        int numberOfAllowedLoginAttempts = Integer.parseInt(getUserLockAllowedLoginAttempts());
        numberOfFailedAttempts++;
        repoExternalUserLoginEvent.setNumberOfFailedLoginAttempts(numberOfFailedAttempts);
        if(numberOfFailedAttempts >= numberOfAllowedLoginAttempts){
            //NOFA exceeded
            repoExternalUserLoginEvent.setEnabled(false);
        }
        else{
            repoExternalUserLoginEvent.setNumberOfFailedLoginAttempts(numberOfFailedAttempts);
        }
        repoExternalUserLoginEvent.setRecordLastUpdateDate(new Date());
    }

    public ExternalUserLoginEventService getExternalUserLoginEventService() {
        return externalUserLoginEventService;
    }

    public void setExternalUserLoginEventService(ExternalUserLoginEventService externalUserLoginEventService) {
        this.externalUserLoginEventService = externalUserLoginEventService;
    }

    public String getUserLockAllowedLoginAttempts() {
        return LoginLockoutConfig.getNumberOfFailedLoginAttempts();
    }

    public UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public String getQualifiedNameSeparator() {
        return qualifiedNameSeparator;
    }

    public void setQualifiedNameSeparator(String qualifiedNameSeparator) {
        this.qualifiedNameSeparator = qualifiedNameSeparator;
    }

    private String getTruncatedUsername(String username){
        if(username.length() > 100){
            username = username.substring(0,100);
        }
        return username;
    }

    public UserAuthorityService getUserAuthorityService() {
        return userAuthorityService;
    }

    public void setUserAuthorityService(UserAuthorityService userAuthorityService) {
        this.userAuthorityService = userAuthorityService;
    }

    public TenantPersistenceResolver getTenantPersistenceResolver() {
        return tenantPersistenceResolver;
    }

    public void setTenantPersistenceResolver(TenantPersistenceResolver tenantPersistenceResolver) {
        this.tenantPersistenceResolver = tenantPersistenceResolver;
    }
}

