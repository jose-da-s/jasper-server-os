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
package com.jaspersoft.jasperserver.api.security.externalAuth.preauth;

import com.jaspersoft.jasperserver.api.common.crypto.CipherI;
import com.jaspersoft.jasperserver.api.security.ResponseHeaderUpdater;
import com.jaspersoft.jasperserver.api.common.util.AuthFilterConstants;
import com.jaspersoft.jasperserver.api.security.externalAuth.ExternalDataSynchronizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owasp.csrfguard.CsrfGuard;
import org.owasp.csrfguard.util.RandomGenerator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.util.Assert;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * This filter class extends AbstractPreAuthenticatedProcessingFilter to
 * call externalDataSynchronizer on successful authentication.
 *
 * User: dlitvak
 * Date: 10/1/13
 */
public class BasePreAuthenticatedProcessingFilter extends AbstractPreAuthenticatedProcessingFilter implements InitializingBean {
	private static final String REQUEST_PREAUTH_TOKEN = "REQUEST_PREAUTH_TOKEN";
	private static final String AUTHENTICATED_PREAUTH_TOKEN = "AUTHENTICATED_PREAUTH_TOKEN";
	private Logger log = LogManager.getLogger(this.getClass());
	private ResponseHeaderUpdater responseHeadersUpdater;
	private ThreadLocal<FilterChain> filterThread = new ThreadLocal<FilterChain>();
	private String principalParameter = "pp";
	private Boolean tokenInRequestParam = null;
	public static final String XSS_NONCE_ATTRIB_NAME = "XSS_NONCE";

	private CipherI tokenDecryptor;

	private ExternalDataSynchronizer externalDataSynchronizer;
    private String jsonRedirectUrl;
	private String welcomePage;

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		Assert.notNull(tokenDecryptor, "tokenDecryptor must be set.  It should not be DevelopmentPlainTextNonCipher in production.");
	}


	public void setWelcomePage(String welcomePage) {
		this.welcomePage = welcomePage;
	}

	public void setJsonRedirectUrl(String jsonRedirectUrl) {
        this.jsonRedirectUrl = jsonRedirectUrl;
    }

	/**
	 * Override to extract the principal information from the current request
	 */
	@Override
	protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
		String principal;

		if (logger.isDebugEnabled())
			logger.debug("Looking for token by principalParameter name '" + principalParameter + "'");

		if (tokenInRequestParam == null) {
			principal = request.getHeader(principalParameter);
			if (principal == null) {
				logger.debug("Token not found in the header, looking in the request parameters.");
				principal = request.getParameter(principalParameter);
			}
		}
		else if (tokenInRequestParam) {
			principal = request.getParameter(principalParameter);
		}
		else {
			principal = request.getHeader(principalParameter);
		}


		if (principal == null) {
			if (logger.isDebugEnabled())
				logger.debug(principalParameter + " was not found. Skipping pre-auth authentication.");
			return principal;
		}

		if (logger.isDebugEnabled())
			logger.debug("Found " + principalParameter + ". Starting pre-auth authentication.");
		return tokenDecryptor.decrypt(principal);
	}

	@Override
	protected boolean principalChanged(HttpServletRequest request, Authentication currentAuthentication) {
		Object principal = getPreAuthenticatedPrincipal(request);
		Authentication authToken = SecurityContextHolder.getContext().getAuthentication();

		if (principal == null && (authToken != null && authToken.isAuthenticated()))
			return false;

		//if the token has not changed, do not re-authenticate the user.
		Object sessToken = request.getSession().getAttribute(AUTHENTICATED_PREAUTH_TOKEN);
		if (principal != null && sessToken != null && principal.equals(sessToken))
			return false;

		return super.principalChanged(request, currentAuthentication);
	}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if(isJsonResponseRequested((HttpServletRequest) request)){
            // it's visualize authentication. Correct JSON success response is required here.
            // The only way to get correct redirect is to authenticate again. So, let's clear security context.
            SecurityContextHolder.clearContext();
        }

        request.setAttribute(REQUEST_PREAUTH_TOKEN, getPreAuthenticatedPrincipal((HttpServletRequest) request));
		filterThread.set(chain);
        super.doFilter(request, response, chain);
    }

    /**
	 * Override to extract the credentials (if applicable) from the current request. Some implementations
	 * may return a dummy value.
	 */
	@Override
	protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
		return "N/A";
	}

	/**
	 *
	 * @param principalParameter - parameter that contains pre-authenticated user details
	 */
	public void setPrincipalParameter(String principalParameter) {
		this.principalParameter = principalParameter;
	}

	/**
	 * Configuration determining whether principalParameter is read from header (default) or
	 * from request parameter.
	 *
	 * @param tokenInRequestParam - default is true
	 */
	public void setTokenInRequestParam(boolean tokenInRequestParam) {
		this.tokenInRequestParam = tokenInRequestParam;
	}

	public boolean isTokenInRequestParam() {
		return tokenInRequestParam;
	}
	/**
	 *
	 * @param externalDataSynchronizer - object that invokes post-auth Processors
	 */
	public void setExternalDataSynchronizer(ExternalDataSynchronizer externalDataSynchronizer) {
		this.externalDataSynchronizer = externalDataSynchronizer;
	}

	/**
	 * The decryptor applied to the text in principalParameter.
	 * Default: PlainTextSymmetricNonCypher - returns principalParameter unencrypted.
	 * @param tokenDecryptor
	 */
	public void setTokenDecryptor(CipherI tokenDecryptor) {
		this.tokenDecryptor = tokenDecryptor;
	}

	/**
	 * Puts the <code>Authentication</code> instance returned by the
	 * authentication manager into the secure context.
	 *
	 * Calls Synchronizer to create a user mirror image in Jasper database
	 */
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) throws IOException, ServletException {
		if (responseHeadersUpdater != null) {
			responseHeadersUpdater.changeHeaders(response, request);
		}
		HttpSession session = request.getSession(false);
		if (session != null && session.getAttribute(XSS_NONCE_ATTRIB_NAME)==null) {
			CsrfGuard csrfGuard = CsrfGuard.getInstance();
			String tokenValue = RandomGenerator.generateRandomId(csrfGuard.getPrng(), csrfGuard.getTokenLength());
			session.setAttribute(XSS_NONCE_ATTRIB_NAME, "nonce-" + tokenValue);
		}
		FilterChain chain = filterThread.get();
		if(request.getHeader(AuthFilterConstants.X_REMOTE_DOMAIN)!=null) {
			request.setAttribute(AuthFilterConstants.AUTH_FLOW_CONST, "true");
			chain.doFilter(request, response);
		}
		super.successfulAuthentication(request, response, authResult);
		externalDataSynchronizer.synchronize();
        final String contextPath = request.getContextPath();
        if (isJsonResponseRequested(request)) {
            // authentication JSON response is requested, let's redirect to configured URL
            // this is used by visualize.js authentication
            final String redirectUrl = contextPath + jsonRedirectUrl;
            try {
                response.sendRedirect(response.encodeRedirectURL(redirectUrl));
            } catch (IOException e) {
                log.error("Unable to send redirect to " + redirectUrl, e);
            }
        }

        HttpSession sess = request.getSession();
		sess.setAttribute(AUTHENTICATED_PREAUTH_TOKEN, request.getAttribute(REQUEST_PREAUTH_TOKEN));
	}

	public void setResponseHeadersUpdater(ResponseHeaderUpdater responseHeadersUpdater) {
		this.responseHeadersUpdater = responseHeadersUpdater;
	}

    protected boolean isJsonResponseRequested(HttpServletRequest request){
        final String accept = request.getHeader("Accept");
        final String requestURI = request.getRequestURI();
        final String contextPath = request.getContextPath();
        return accept != null && accept.toLowerCase().contains("application/json") && (requestURI.equals(contextPath) || requestURI.equals(contextPath + "/") || requestURI.equals(contextPath + welcomePage));

    }
}

