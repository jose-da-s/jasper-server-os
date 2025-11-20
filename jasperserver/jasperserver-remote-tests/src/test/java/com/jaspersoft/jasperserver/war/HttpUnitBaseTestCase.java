/*
 * Copyright (C) 2025 the Jasper Server OS Authors
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

package com.jaspersoft.jasperserver.war;

import com.meterware.httpunit.*;

import java.net.*;

import junit.framework.*;

/**
 * The test cases are for: -
 * Connecting the Jasper Server OS Startup Page - checking the Jasper Server OS Home Page - checking the
 * Jasper Server OS List Reports Page - checking the Jasper Server OS View Report Page
 **/
public abstract class HttpUnitBaseTestCase
	extends TestCase {

	private String               username = "j_username";
	private String               password = "j_password";
	protected static WebResponse wResponse;
	private WebConversation      webConversation;

	/**
	 * Creates a new HttpUnitBaseTestCase object.
	 **/
	public HttpUnitBaseTestCase() {
		super();
	}

	/**
	 * Creates a new HttpUnitBaseTestCase object.
	 *
	 * @param arg0 arg0
	 **/
	public HttpUnitBaseTestCase(String arg0) {
		super(arg0);
	}

	/**
	 * Common Login functionality for each individual URL-s in the
	 * JS application. Throws exception if login is  not successfull or  link is not found 
	 *
	 * @param url application url
	 *
	 * @return - void
	 *
	 * @throws Exception if fails
	 **/
	protected WebResponse commonLoginFunction(String url)
	  throws Exception {
		return commonLoginFunction(url, "");
	}

	protected WebResponse commonLoginFunction(String url, String urlString)
	  throws Exception {
		WebResponse     response = null;
		WebConversation wcon = new WebConversation();
		WebRequest      wreq = new GetMethodWebRequest(new URL(url), urlString);
		response = wcon.getResponse(wreq);

		WebForm form = response.getForms()[0];
		assertEquals("Form Action", "j_acegi_security_check", form.getAction());
		wreq = form.getRequest();
		wreq.setParameter(username, getloginCredentials()[0]);
		wreq.setParameter(password, getloginCredentials()[1]);
		response = wcon.getResponse(wreq);

		this.setWebConversation(wcon);
		return response;
	}
	/**
	 * This method is for getting the response from the site to be tested
	 * TODO : need to revisit this method implementation
	 *
	 * @param url Url to be navigated
	 *
	 * @return WebResponse
	 *
	 * @throws Exception if fails
	 **/
	protected WebResponse getURLResponse(String url)
	  throws Exception {
		WebResponse     response     = null;
		URL             serverUrl    = new URL(url);
		WebConversation conversation = new WebConversation();
		WebRequest      request      = new GetMethodWebRequest(serverUrl, "");
		response = conversation.getResponse(request);
		return response;
	}

	/**
	 * Subclass would provide implementation for this method
	 *
	 * @return username and password
	 **/
	protected abstract String[] getloginCredentials();

	/**
	 * Returns the associated webConversation object reference.
	 *
	 * @return the associated webConversation object reference.
	 **/
	public WebConversation getWebConversation() {
		return webConversation;
	}

	/**
	 * Sets the WebConversation object.
	 *
	 * @param webConversation  
	 **/
	public void setWebConversation(WebConversation webConversation) {
		this.webConversation = webConversation;
	}
}
