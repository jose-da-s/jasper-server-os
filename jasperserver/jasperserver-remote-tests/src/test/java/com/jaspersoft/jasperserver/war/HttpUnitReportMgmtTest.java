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
 * Jasper Server OS Reports Management Page - checking the Jasper Server OS Report Details Page
 **/
public class HttpUnitReportMgmtTest
	extends HttpUnitBaseTestCase {
	private static String        reptMgmtPageUrl = JasperServerConstants.instance().BASE_URL +
	                                               "/jasperserver/reportmgmt.html";
	private static String        repMgmtLink      = "AllAccounts";
	private static String        repNameText      = "txRepName";
	private static String 		 repPageText	  = "SEARCH";
	private static String		 repDetFormName	  = "fmRptDtls";
	private static String		 repDetText1	  = "AllAccounts";
	private static String		 repDetText2      = "sub-JRXML-01 in Repository";
	private static String        crRepImgText     = "/jasperserver/images/create_button.gif";
	
	protected static WebResponse retResponse;

	/**
	 * Constructor
	 *
	 * @param s
	 **/
	public HttpUnitReportMgmtTest(String s) {
		super(s);
	}

	/**
	 * Checks through commonLoginFunction for loggin in at first time 
	 *
	 * @throws Exception if fails
	 **/
	public void setUp()
	  throws Exception {
			wResponse = commonLoginFunction(reptMgmtPageUrl);
		}

	
	//****--------------------------------------------------------------------------*****/
	//*							HttpUnit test cases		                                */
	//****--------------------------------------------------------------------------*****/	
	
	
	/**
	 * This test case method is for Report Management Page of JS application This checks if the
	 * wResponse for the page is Null, checks for the textField. and also checks for the
	 * "AllAccounts" link. On a proper wResponse - test is success else - failure
	 *
	 * @throws Exception if fails
	 **/
	public void testReptMgmtPage()
	  throws Exception {		
		WebResponse reportMgmt = this.getWebConversation().getCurrentPage();
		
		assertNotNull("Report Management response is Null", reportMgmt);
		
		//checking for text field on the page
		assertNotNull("Report Management element is Null",
				reportMgmt.getElementsWithName(repNameText));

		//checking for the link on the page
//		WebLink link = reportMgmt.getLinkWith(repMgmtLink);
//		assertNotNull("All Account report is Null", link);

		String page = reportMgmt.getText();

		if ((page == null) || (page.trim().length() == 0)) {
			fail("No text fond in response");
		}

		assertTrue(page.indexOf("Name:") != -1);

		WebImage image = reportMgmt.getImageWithSource(crRepImgText);
		assertNotNull("Image is present", image);

	}

	/**
	 * This test case method is for Report Detail Page of JS application This checks if the
	 * response for the previous link is Null, and checks for the "AllAccounts" link, than on
	 * click  goes to next page and checks for the text contents. 
	 * Again click on the return button it comes back to report management page.
	 *  On a proper response test-success, else - failure
	 *
	 * @throws Exception if fails
	 **/
	public void testReptDetlPage()
	  throws Exception {
		//this is the report management page
		WebResponse reportMgmt = this.getWebConversation().getCurrentPage();		
		assertNotNull("Report Detail response is Null", reportMgmt);

		WebLink link = reportMgmt.getLinkWith(repMgmtLink);
		link.click();
		
		//checking for the next page after clicking on the link
		WebResponse reportDetail = this.getWebConversation().getCurrentPage();
		
		String string = reportDetail.getText();

		if ((string == null) || (string.trim().length() == 0)) {
			fail("No text found in response");
		}

		assertTrue((string.indexOf(repDetText1) != -1) &&
		           (string.indexOf(repDetText2) != -1));

		WebForm repDetForm = reportDetail.getFormWithName(repDetFormName);
		repDetForm.getScriptableObject().setParameterValue("repdtlsaction", "torepmgmt");
		repDetForm.submit();

		
		WebResponse bkRepMgmt = this.getWebConversation().getCurrentPage();
		
		String str = bkRepMgmt.getText();

		if ((str == null) || (str.trim().length() == 0)) {
			fail("No text found in response");
		}

		assertTrue(str.indexOf(repPageText) != -1);
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
	 * this method is for adding which all test case/s method/s need to be
	 *
	 * @return Test
	 *
	 * @throws Exception if fails
	 **/
	public static Test suite()
	  throws Exception {
		TestSuite suite = new TestSuite();
		
		TestCase test1 = new HttpUnitReportMgmtTest("testReptMgmtPage");
		TestCase test2 = new HttpUnitReportMgmtTest("testReptDetlPage");

		suite.addTest(test1);
	//	suite.addTest(test2);
		return suite;
	}
}
