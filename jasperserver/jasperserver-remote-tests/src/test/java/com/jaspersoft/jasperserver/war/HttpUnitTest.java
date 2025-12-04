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
import java.net.*;

/**
 * The test cases are for:
 * - Connecting the Jasper Server OS Startup Page
 * - checking the Jasper Server OS Home Page
 * - checking the Jasper Server OS List Reports Page
 * - checking the Jasper Server OS View Report Page
 */
public class HttpUnitTest extends TestCase {

    //private static String homePageUrl = JasperServerConstants.instance().BASE_URL+"/jasperserver/home.html";
    private static String homePageUrl = JasperServerConstants.instance().BASE_URL+"/jasperserver/flow.html?_flowId=listReportsFlow";  
    private static String homePgText = "The world's most popular open source reporting engine";
    private static String intRepLink = "REPORTS";
    private static String indRepLink = "AllAccounts";
    private static String pageText1 = "Accounts";
    private static String pageText2 = "Burnaby";
    
    private static WebResponse wResponse ;

    /**
     * Constructor
     * @param s
     */
    public HttpUnitTest(String s) {
        super(s);
    }

    /**
     * the main method for calling all the test cases whichever is being added
     * into the suite.
     * @param args
     */
    public static void main(String args[]) {
    	try {
    		junit.textui.TestRunner.run(suite());
    	} catch (Exception _ex) {
    		_ex.printStackTrace();
    	}
    }

    public void setUp() throws Exception{
    	if (wResponse == null){
    		wResponse = commonLoginFunction(homePageUrl);
    	}
    }

    /**
     * this method is for adding which all test case/s method/s need to be
     * @return Test
     */
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite();
        TestCase test1 = new HttpUnitTest("testStartURL");//verifies against Login Page
        TestCase test2 = new HttpUnitTest("testHomePage");//verifies against Home Page
        TestCase test3 = new HttpUnitTest("testLstRepLink");//verifies against List Report Link
        
        suite.addTest(test1);
        suite.addTest(test2);
        suite.addTest(test3);
        
        return suite;
    }

    /**
     * This test case method is for Login Page of JS application
     * This checks for the "url" and the "textfield" for UserName
     * On a proper response test is success else failure
     * @return - void
     */
    public void testStartURL() throws Exception {    	
        WebResponse response = gettingURLResponse(homePageUrl);
        assertNotNull(homePageUrl+" Response is NULL ", response);
        assertNotNull(" Login Page Element is Null ", response.getElementsWithName(homePgText));
    }

    /**
     * This test case method is for Home Page of JS application
     * This checks for the "url" and the "Recent Saved Reports" link
     * On a proper response - test is success else - failure
     * @return - void
     */
    public void testHomePage() throws Exception {
    	assertNotNull(homePageUrl+"Response is NULL ", wResponse);
    	assertNotNull(" Home Page Element is Null ", wResponse.getElementsWithName("j_username"));
    }

    /**
     * This test case is for checking for ListReports link in response 
     * to the previous link and checks if "Interactive" link exists
     * If link found - test success, else - failure 
     * @return - void
     */
    public void testLstRepLink() throws Exception {
		assertNotNull(" List Response is Null ", wResponse);
   		WebLink link = wResponse.getLinkWith(intRepLink);
   		assertNotNull(intRepLink +"Link is NULL ", link);
   		wResponse = link.click();
    }

    /**
     * This test case is validating for View Report page.
     * In response for the previous page link it checks for "AllAccounts" link,
     * and on click, it goes to next page and checks if the text content and the link(as button) exists.
     * Also checks if clicking on the button, it goes to next page.
     * Checks for DIV element for Report Content.
     * If these are found - test success, else - failure
     * @return - void
     */
    public void testIndRepLink() throws Exception {
    	boolean matchFound = false;
    	boolean valueFound = false;
	
		assertNotNull(" Interactive Reports - Response is NULL ", wResponse);
		
		WebLink link1 = wResponse.getLinkWith(indRepLink);
		assertNotNull(indRepLink+" Link is NULL ", link1);
		wResponse = link1.click();
		assertNotNull(indRepLink+" Response is NULL ", wResponse);
		
		String opt = wResponse.getText();
		assertNotNull(" Response is NULL ", opt);
		if(opt.indexOf(pageText1)!= -1 && opt.indexOf(pageText2) != -1 )
			matchFound = true;
		
		assertEquals(matchFound, true);
		
		WebLink[] linkArray = null;
		
		WebTable table = wResponse.getTableWithID("pageNumber");
		try{
			linkArray = table.getTableCell(0,5).getLinks();
			assertNotNull("Button Link is Null",linkArray);
			
			String pageString = table.getCellAsText(0,1);
			String lastPageNum = pageString.substring(9).trim();
			
			pageString = linkArray[0].click().getTableWithID("pageNumber").getCellAsText(0,1);
			if(pageString.indexOf(lastPageNum)!= pageString.lastIndexOf(lastPageNum))
				valueFound = true;
			assertEquals(valueFound,true);
		}catch (NullPointerException e) {
			e.printStackTrace();
		}
    }

    /**
     * This function is to perform the Common Login functionality
     * for each individual URL-s in the JS application
     * If link found - test success
     * else - failure
     * @return - void
     */
    private WebResponse commonLoginFunction(String url) throws Exception {
   		WebResponse response = null;
   		WebConversation wcon = new WebConversation();
   		WebRequest wreq = new GetMethodWebRequest(new URL(url), "");
   		response = wcon.getResponse(wreq);
   		WebForm form = response.getForms()[0];
   		assertEquals("Form Action", "j_acegi_security_check", form.getAction());
   		wreq = form.getRequest();
   		wreq.setParameter("j_username", JasperServerConstants.instance().USERNAME);
   		wreq.setParameter("j_password", JasperServerConstants.instance().PASSWORD);
   		response = wcon.getResponse(wreq); 		
   		return response;
	}

    /**
     * This method is for getting the response from the site to be tested
     * @return WebResponse
     */
    private WebResponse gettingURLResponse(String url) throws Exception {
    	WebResponse response = null;
        URL serverUrl = new URL(url);
        WebConversation conversation = new WebConversation();
        WebRequest request = new GetMethodWebRequest(serverUrl, "");
        response = conversation.getResponse(request);
        return response;
    }
}