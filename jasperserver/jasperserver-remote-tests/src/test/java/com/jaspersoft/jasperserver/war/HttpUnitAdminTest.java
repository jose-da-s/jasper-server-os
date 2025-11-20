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

import junit.framework.*;

/**
 * This Class is used for writing Testcases for testing a Jasper site. The test cases are for: -
 * Connecting the Jasper Server OS Startup Page - checking the Jasper Server OS Home Page - checking the
 * Jasper Server OS Admin Reports Page
 **/
public class HttpUnitAdminTest
	extends HttpUnitBaseTestCase {
	//private static String homePageUrl    = JasperServerConstants.instance().HOME_PAGE_URL;
	
	private static String  admHomePageUrl = JasperServerConstants.instance().BASE_URL +
	    "/jasperserver/flow.html%3f_flowId=repoAdminFlow";

	private static String adminPgText1 = "Repository Management";
	//private static String adminPgText2  = "NOTIFICATIONS"; // not used
	
	/**
	 * Constructor
	 *
	 * @param s
	 **/
	public HttpUnitAdminTest(String s) {
		super(s);
	}

	/**
	 * Checks through commonLoginFunction for logging in in at first time 
	 *
	 * @throws Exception if fails
	 **/
	public void setUp()
	  throws Exception {
			wResponse = commonLoginFunction(admHomePageUrl);
		}
	
	
	//****--------------------------------------------------------------------------*****/
	//*				HttpUnit test cases		                                            */
	//****--------------------------------------------------------------------------*****/	

	
	
	/**
	 * This test case method is for Admin Home Page of JS application This Page checks if the
	 * wResponse for this page is Null, and also checks for the text contents. On a proper
	 * response - test is success else - failure
	 *
	 * @throws Exception if fails
	 **/
	public void testAdmHomePage()
	  throws Exception {
		WebResponse adminPage = this.getWebConversation().getCurrentPage();
		assertNotNull("Admin Page Response is Null", adminPage);

		String opt = adminPage.getText();
		
		if ((opt == null) || (opt.trim().length() == 0)) {
			fail("Text not found in response");
		}

		assertTrue(opt.indexOf(adminPgText1) != -1);
	}
	
	
	//****--------------------------------------------------------------------------*****/
	//*				Base class method implementaion                                     */
	//****--------------------------------------------------------------------------*****/	

	
	
	/* (non-Javadoc)
	 * @see com.jaspersoft.jasperserver.war.HttpUnitBaseTestCase#getloginCredentials()
	 */
	protected String[] getloginCredentials() {
		return new String[] { JasperServerConstants.instance().USERNAME, 
				      JasperServerConstants.instance().PASSWORD };
	}
	
	//****--------------------------------------------------------------------------*****/
	//*				HttpUnit framework methods                                          */
	//****--------------------------------------------------------------------------*****/		
	
	
	/**
	 * the main method for calling all the test cases whichever is being added into the suite.
	 *
	 * @param args
	 **/
	public static void main(String[] args) {
		try {
			junit.textui.TestRunner.run(suite());
		} catch (Exception _ex) {
			_ex.printStackTrace();
		}
	}
	
	/**
	 * this method is for adding which all test case/s method/s need to be tested
	 *
	 * @return Test
	 *
	 * @throws Exception if fails
	 **/
	public static Test suite()
	  throws Exception {
		TestSuite suite = new TestSuite();

		TestCase test1 = new HttpUnitAdminTest("testAdmHomePage");
		suite.addTest(test1);

		return suite;
	}

}
