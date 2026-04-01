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
package com.jaspersoft.jasperserver.api.security.externalAuth.cas;

import com.jaspersoft.jasperserver.api.common.util.spring.UsernamePasswordAuthenticationParameterConfiguration;
import com.jaspersoft.jasperserver.api.common.util.AuthFilterConstants;
import com.jaspersoft.jasperserver.api.security.UsernamePasswordAuthenticationFilterWarningWrapper;
import com.jaspersoft.jasperserver.api.security.encryption.EncryptionRequestUtils;
import com.jaspersoft.jasperserver.api.security.externalAuth.ExternalDataSynchronizer;
import java.util.Map;
import org.apereo.cas.client.session.SessionMappingStorage;
import org.apereo.cas.client.session.SingleSignOutFilter;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.security.authentication.AuthenticationManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: dlitvak
 * Date: 10/20/12
 */
public class JSCasProcessingFilter extends CasAuthenticationFilter {
	private static final String CAS_TICKET_PARAM_NAME = "ticket";

	private String usernameParameter;
	private String passwordParameter;
	private ThreadLocal<FilterChain> filterThread = new ThreadLocal<FilterChain>();

	private UsernamePasswordAuthenticationParameterConfiguration parameterConfiguration;
	private static final String USERNAME_PARAM = "usernameParameter";
	private static final String PASSWORD_PARAM = "passwordParameter";

	@Override
    public Authentication attemptAuthentication(final HttpServletRequest request, final HttpServletResponse response)
			throws AuthenticationException, IOException {
            String password = obtainTicket(request);
		if (password != null && password.trim().length() > 0){
			final String username = "_cas_stateful_";
			UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
			authRequest.setDetails(authenticationDetailsSource.buildDetails(request));

			return this.getAuthenticationManager().authenticate(authRequest);
		}
		else {
			String username = null;
			if(usernameParameter != null && request.getParameter(usernameParameter) != null){
				username = request.getParameter(usernameParameter);
				password = request.getParameter(passwordParameter);
			}
			if(username == null){
				for(Map<String, String> authParam : parameterConfiguration.getAuthParameters()){
					username = EncryptionRequestUtils.getValue(request, authParam.get(USERNAME_PARAM));
					if(username != null){
						password = EncryptionRequestUtils.getValue(request, authParam.get(PASSWORD_PARAM));
						break;
					}
				}
			}
			UsernamePasswordAuthenticationToken authRequest =
					new UsernamePasswordAuthenticationToken(username != null ? username.trim() : "", password != null ? password : "");

			// Allow subclasses to set the "details" property
			setDetails(request, authRequest);

			Authentication authentication =  this.getAuthenticationManager().authenticate(authRequest);
			if( authentication != null && request.getHeader(AuthFilterConstants.X_REMOTE_DOMAIN)!=null)  {
				request.setAttribute(AuthFilterConstants.AUTH_FLOW_CONST, "true");
				FilterChain chain = filterThread.get();
				try {
					chain.doFilter(request, response);
				} catch (ServletException e) {
					throw new InternalAuthenticationServiceException(e.getMessage());
				}
			}
			return authentication;
		}
	}

	protected String obtainUsername(HttpServletRequest request) {
		return usernameParameter!=null ? request.getParameter(usernameParameter): UsernamePasswordAuthenticationFilterWarningWrapper.obtainUsernameWithLegacySupport(request);
	}

    protected String obtainTicket(HttpServletRequest request) {
        return request.getParameter(CAS_TICKET_PARAM_NAME);
    }

	protected String obtainPassword(HttpServletRequest request) {
		return passwordParameter!=null ? request.getParameter(passwordParameter) : UsernamePasswordAuthenticationFilterWarningWrapper.obtainPasswordWithLegacySupport(request);
	}

	String getUsernameParameter() {
		return usernameParameter;
	}

	/**
	 * Sets the parameter name which will be used to obtain the username from the login request.
	 *
	 * @param usernameParameter the parameter name. Defaults to "j_username".
	 */
	public void setUsernameParameter(String usernameParameter) {
		Assert.hasText(usernameParameter, "Username parameter must not be empty or null");
		this.usernameParameter = usernameParameter;
	}

	String getPasswordParameter() {
		return passwordParameter;
	}

	/**
	 * Sets the parameter name which will be used to obtain the password from the login request..
	 *
	 * @param passwordParameter the parameter name. Defaults to "j_password".
	 */
	public void setPasswordParameter(String passwordParameter) {
		Assert.hasText(passwordParameter, "Password parameter must not be empty or null");
		this.passwordParameter = passwordParameter;
	}

	/**
	 * Provided so that subclasses may configure what is put into the authentication request's details
	 * property.
	 *
	 * @param request that an authentication request is being created for
	 * @param authRequest the authentication request object that should have its details set
	 */
	protected void setDetails(HttpServletRequest request, UsernamePasswordAuthenticationToken authRequest) {
		authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
			ServletException {
		filterThread.set(chain);
		super.doFilter(req, res, chain);
	}
	public UsernamePasswordAuthenticationParameterConfiguration getParameterConfiguration() {
		return parameterConfiguration;
	}

	public void setParameterConfiguration(
			UsernamePasswordAuthenticationParameterConfiguration parameterConfiguration) {
		this.parameterConfiguration = parameterConfiguration;
	}


	@Override
	protected AuthenticationManager getAuthenticationManager() {
		return super.getAuthenticationManager();
	}}

