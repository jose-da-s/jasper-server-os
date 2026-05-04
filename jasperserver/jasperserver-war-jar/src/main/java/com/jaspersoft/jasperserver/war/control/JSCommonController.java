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


import com.jaspersoft.jasperserver.api.common.configuration.LoginLockoutConfig;
import com.jaspersoft.jasperserver.api.common.util.TimeZonesList;
import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.client.MetadataUserDetails;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.UserAuthorityServiceImpl;
import com.jaspersoft.jasperserver.api.security.SecurityConfiguration;
import com.jaspersoft.jasperserver.api.security.externalAuth.ExternalAuthProperties;
import com.jaspersoft.jasperserver.war.common.HeartbeatBean;
import com.jaspersoft.jasperserver.war.common.HeartbeatClientInfo;
import com.jaspersoft.jasperserver.war.common.JasperServerConstImpl;
import com.jaspersoft.jasperserver.war.common.LocalesList;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Locale;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;


/**
 * @author aztec
 * @version $Id$
 */
public class JSCommonController extends JRBaseController {
	public static final String IS_DEVELOPMENT_ENVIRONMENT_TYPE = "isDevelopmentEnvironmentType";
	public static final String USERS_EXCEEDED = "usersExceeded";
	public static final String BAN_USER = "banUser";
	protected HeartbeatBean heartbeat;
	private ExternalAuthProperties externalAuthPropertiesBean;

	private LocalesList locales;
	private TimeZonesList timezones;
	private String allowUserPasswordChange;
	private String passwordExpirationInDays;
	/* On/Off auto completion for login form */
	private String autoCompleteLoginForm;

	private String externalAuthPropertiesBeanName = "externalAuthProperties";

	private static final Log log = LogFactory.getLog(JSCommonController.class);

	/*
	 * Overridden method for handling the requests
	 * @args HttpServletRequest, HttpServletResponse
	 * @returns ModelAndView - Home Page
	 */
	public ModelAndView homePage(HttpServletRequest req, HttpServletResponse res)
			throws ServletException {
		return new ModelAndView("modules/home");
	}
	@RequestMapping("/login.html")
	public ModelAndView login(HttpServletRequest req, HttpServletResponse res)
			throws ServletException {
		setupLoginPage(req);

		return new ModelAndView("modules/login/login");
	}
	@RequestMapping("/externallogin.html")
	public ModelAndView externalLogin(HttpServletRequest req, HttpServletResponse res)
			throws ServletException {
		req.setAttribute("externalAuthPropertiesBean", getExternalAuthPropertiesBean());
		return new ModelAndView("modules/login/externalLogin");
	}

	protected void setupLoginPage(HttpServletRequest req) {
		Cookie[] cookies = req.getCookies();
		String locale = null;
		String preferredTz = null;
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				Cookie cookie = cookies[i];
				if (cookie.getName().equals(JasperServerConstImpl.getUserLocaleSessionAttr()))
					locale = cookie.getValue();
				if (cookie.getName().equals(JasperServerConstImpl.getUserTimezoneSessionAttr()))
					preferredTz = cookie.getValue();
			}
		}

		Locale displayLocale = req.getLocale();
		String preferredLocale;
		if (locale == null || locale.length() == 0) {
			preferredLocale = displayLocale.toString();
		} else {
			preferredLocale = locale;
		}

		if (preferredTz == null) {
			preferredTz = timezones.getDefaultTimeZoneID();
		}

		req.setAttribute("preferredLocale", preferredLocale);
		req.setAttribute("userLocales", locales.getUserLocales(displayLocale));
		req.setAttribute("preferredTimezone", preferredTz);
		req.setAttribute("userTimezones", timezones.getTimeZones(displayLocale));
		try {
			if (Integer.parseInt(passwordExpirationInDays) > 0) {
				allowUserPasswordChange = "true";
			}
		} catch (NumberFormatException e) {
			// if the value is NaN, then assume it's non postive.
			// not overwrite allowUserPasswordChange
		}
		req.setAttribute("allowUserPasswordChange", allowUserPasswordChange);
		req.setAttribute("passwordExpirationInDays", passwordExpirationInDays);
		req.setAttribute("passwordPattern", userAuthService.getAllowedPasswordPattern().replace("\\", "\\\\"));
		req.setAttribute("autoCompleteLoginForm", autoCompleteLoginForm);
		req.setAttribute(IS_DEVELOPMENT_ENVIRONMENT_TYPE, false);
		req.setAttribute(USERS_EXCEEDED, false);
		req.setAttribute(BAN_USER, false);
		req.setAttribute("isEncryptionOn", SecurityConfiguration.isEncryptionOn());
		removeSessionAttributesForLoginLockout(req);
		if(null != getUserLockAllowedLoginAttempts() &&
				StringUtils.isNotEmpty(getUserLockAllowedLoginAttempts()) && Integer.parseInt(getUserLockAllowedLoginAttempts()) > 0){
			req.setAttribute("isUserLockFeatureEnabled",true);
		}


	}
	@RequestMapping("/heartbeat.html")
	public ModelAndView heartbeat(HttpServletRequest req, HttpServletResponse res) throws ServletException {
		boolean isCallPermitted = false;

		String permit = req.getParameter("permit");
		if (permit != null) {
			isCallPermitted = Boolean.valueOf(permit);
		}

		heartbeat.permitCall(isCallPermitted);

		return new ModelAndView("ajax/ajaxresponse");
	}
	@RequestMapping("/heartbeatInfo.html")
	public ModelAndView heartbeatInfo(HttpServletRequest req, HttpServletResponse res) throws ServletException {
		final HeartbeatClientInfo info = new HeartbeatClientInfo();

		info.setNavigatorAppName(req.getParameter("navAppName"));
		info.setNavigatorAppVersion(req.getParameter("navAppVersion"));
		info.setNavigatorLocale(req.getLocale());
		info.setUserLocale(LocaleContextHolder.getLocale());

		String errorParam = null;
		try {
			info.setScreenWidth(getIntegerParameter(req, "scrWidth"));
		} catch (NumberFormatException ex) {
			errorParam = "scrWidth";
		}
		try {
			info.setScreenHeight(getIntegerParameter(req, "scrHeight"));
		} catch (NumberFormatException ex) {
			errorParam = "scrHeight";
		}
		try {
			info.setScreenColorDepth(getIntegerParameter(req, "scrColorDepth"));
		} catch (NumberFormatException ex) {
			errorParam = "scrColorDepth";
		}
		if (errorParam != null) {
			try {
				res.sendError(HttpStatus.BAD_REQUEST.BAD_REQUEST.value(), "Invalid value for " + errorParam + ": [" + req.getParameter(errorParam) + "]");
				return null;
			} catch (Exception ex2) {
				log.error("Failed to send error response", ex2);
			}
		}

		info.setUserAgent(req.getHeader("user-agent"));

		new Thread(
				new Runnable() {
					public void run() {
						heartbeat.updateClientInfo(info);
					}
				}
		).start();

		return new ModelAndView("ajax/ajaxresponse");
	}

	private Integer getIntegerParameter(HttpServletRequest req, String param) throws NumberFormatException {
		if (req.getParameter(param) != null) {
			return (new Integer(req.getParameter(param)));
		} else return null;
	}

	@RequestMapping(value = "/exituser.html", method = {POST, GET})
	public ModelAndView exitUser(HttpServletRequest req, HttpServletResponse res) {
		String redirectURL = "/logout.html";
		redirectURL = buildRedirectUrl(req, redirectURL);

		if (UserAuthorityServiceImpl.isUserSwitched()) {
			redirectURL = "/j_acegi_exit_user";
			req.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
		}

		return new ModelAndView("redirect:" + redirectURL);
	}


	@RequestMapping("/logout.html")
	public ModelAndView logout(HttpServletRequest req, HttpServletResponse res)
			throws ServletException {
		// invalidate session
		HttpSession session = req.getSession(false);
		if (session != null) {
			session.invalidate();
		}

		//Determine if the user logging out is internal.  If it is, logout to the internal JRS logout url.
		//If the user is external, logout to the url defined in ExternalAuthProperties bean (app context)
		boolean loggedinUserIsExternal = false;
		final Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
		Object principal = authObj.getPrincipal();
		if (principal instanceof MetadataUserDetails) {
			loggedinUserIsExternal = ((MetadataUserDetails) principal).isExternallyDefined();
		}

		// we aren't using RememberMe but this is how we'd log out if we did
		// Cookie terminate = new Cookie(TokenBasedRememberMeServices.ACEGI_SECURITY_HASHED_REMEMBER_ME_COOKIE_KEY, null);
		// terminate.setMaxAge(0);
		// res.addCookie(terminate);
		SecurityContextHolder.clearContext(); //invalidate authentication

		ExternalAuthProperties externalAuthProperties = getExternalAuthPropertiesBean();
		String externalLogoutUrl = (externalAuthProperties != null ? externalAuthProperties.getLogoutUrl() : null);
		if (loggedinUserIsExternal && externalLogoutUrl != null && externalLogoutUrl.length() > 0) {
			return new ModelAndView("redirect:" + externalLogoutUrl);
		}
		else {
			String redirectURL = "redirect:/login.html";
			redirectURL = buildRedirectUrl(req, redirectURL);

			return new ModelAndView(redirectURL);
		}
	}

	protected String buildRedirectUrl(HttpServletRequest req, String redirectURL) {
		if (req.getParameter("showPasswordChange") == null) {
			if (req.getParameter("weakPassword") != null) {
				redirectURL += "?weakPassword=" + req.getParameter("weakPassword");
			}
		} else {
			redirectURL += "?showPasswordChange=" + req.getParameter("showPasswordChange");
			if (req.getParameter("weakPassword") != null) {
				redirectURL += "&weakPassword=" + req.getParameter("weakPassword");
			}
		}
		return redirectURL;
	}

	protected ExternalAuthProperties getExternalAuthPropertiesBean() {
		try {
			if (externalAuthPropertiesBean == null)
				externalAuthPropertiesBean = (ExternalAuthProperties) getApplicationContext().getBean(externalAuthPropertiesBeanName);
		}
		catch (Exception e) {
			log.warn("Could not find " + ExternalAuthProperties.class + " bean in the context (using default).  Check that external " +
					"authentication context xml is in the path.");
		}
		return externalAuthPropertiesBean;
	}
	@RequestMapping("/loginerror.html")
	public ModelAndView loginError(HttpServletRequest req, HttpServletResponse res)
			throws ServletException {
		log.warn("There was a login error");
		return new ModelAndView("modules/loginError");
	}
	@RequestMapping("/error.html")
	public ModelAndView securityError(HttpServletRequest req, HttpServletResponse res)
			throws ServletException {
		log.warn("There was a security error");
		return new ModelAndView("modules/system/prepErrorPage");
	}

	/*
	 * @args req, res
	 * @returns ModelAndView - menutest.jsp
	 */
	@RequestMapping("/menutest.html")
	public ModelAndView menuTest(HttpServletRequest req, HttpServletResponse res)
			throws ServletException {
		return new ModelAndView("menutest");
	}

	public LocalesList getLocales()
	{
		return locales;
	}

	public void setLocales(LocalesList locales)
	{
		this.locales = locales;
	}

	public TimeZonesList getTimezones()
	{
		return timezones;
	}

	public void setTimezones(TimeZonesList timezones)
	{
		this.timezones = timezones;
	}

	public String getAllowUserPasswordChange()
	{
		return allowUserPasswordChange;
	}

	public void setAllowUserPasswordChange(String changePassword)
	{
		this.allowUserPasswordChange = changePassword;
	}

	public String getPasswordExpirationInDays() {
		return passwordExpirationInDays;
	}

	public void setPasswordExpirationInDays(String passwordExpirationInDays) {
		this.passwordExpirationInDays = passwordExpirationInDays;
	}

	public HeartbeatBean getHeartbeat() {
		return heartbeat;
	}

	public void setHeartbeat(HeartbeatBean heartbeat) {
		this.heartbeat = heartbeat;
	}

	public String getAutoCompleteLoginForm() {
		return autoCompleteLoginForm;
	}

	public void setAutoCompleteLoginForm(String autoCompleteLoginForm) {
		this.autoCompleteLoginForm = autoCompleteLoginForm;
	}

	public String getExternalAuthPropertiesBeanName() {
		return externalAuthPropertiesBeanName;
	}

	public void setExternalAuthPropertiesBeanName(String externalAuthPropertiesBeanName) {
		this.externalAuthPropertiesBeanName = externalAuthPropertiesBeanName;
	}

	/*
	 * Overridden method for handling the requests
	 * @args HttpServletRequest, HttpServletResponse
	 * @returns ModelAndView - Home Page
	 */
	@RequestMapping("/encrypt.html")
	public ModelAndView encryptionPage(HttpServletRequest req, HttpServletResponse res)
			throws ServletException {
		return new ModelAndView("modules/encrypt");
	}

	public String getUserLockAllowedLoginAttempts() {
		return LoginLockoutConfig.getNumberOfFailedLoginAttempts();
	}

	/**
	 * Removes the session attributes specific to NOFA (number of failed attempts). These values are added when the authentication attempt is
	 * unsuccessful. A user might reload the page after the unsuccessful login attempt. As these values are stored in
	 * the session, reloading of the page will still show the values on the login page. Removing these values will clear
	 * the error message on the login page and will hide the number of failed attempts/user enabled and disabled status
	 * allowing the user to attempt login again.
	 * @param httpServletRequest - A HttpServletRequest instance
	 */
	private void removeSessionAttributesForLoginLockout(HttpServletRequest httpServletRequest) {
		Map<String, String[]> pathParameters = httpServletRequest.getParameterMap();
        /*
        If the user login attempt is unsuccessful,the login-lockout related parameters are added in the session and are
        not removed until a successful logout happens. An unsuccessful login attempt results in the URL being populated
        with a path variable named error (typically error=1)
        If the user tries to access the login page of the application after an unsuccessful login attempt, these parameters
        have to be removed to avoid incorrect error messages to be displayed on the Login page
        */
		if(pathParameters != null && pathParameters.size() == 0){
			HttpSession httpSession = httpServletRequest.getSession();
			Assert.notNull(httpSession,"HttpSession is null. Unexpected Error");
			httpSession.removeAttribute("userPrincipal");
			httpSession.removeAttribute("numberOfLoginAttemptsRemaining");
			httpSession.removeAttribute("isUserLocked");
		}
	}
}

