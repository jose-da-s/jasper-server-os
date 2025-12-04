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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.meterware.httpunit.SubmitButton;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebResponse;
import org.xml.sax.SAXException;

/**
 * The test cases are for: -
 * Connecting the Jasper Server OS Startup Page - checking the Jasper Server OS Home Page - checking the
 * Jasper Server OS User Management Page
 **/
public class HttpUnitCreateUserTest
	extends HttpUnitBaseTestCase {
	private static String userMgmtPageUrl = JasperServerConstants.instance().BASE_URL +
											"/jasperserver/flow.html";
	private static String userMgmtPageString = "?_flowId=userListFlow";
	private static String objSecurityLink   = "Object Security for User";
	private static String usrSearchFormText = "frm";
	private static String editUserFormText  = "fmCreEdUsr";
	private static String newUsrButtonText  = "_eventId_add";
	private static String submitButtonText  = "_eventId_save";
	private static String cancelButtonText  = "_eventId_cancel";
	private static String userNameText = "user.username";
	private static String usersText = "Assigned Roles";
	private static String errMsg1Text = "Property user.username threw exception; nested exception is java.lang.RuntimeException: No user name";
	private static String errMsg2Text = "Full name cannot be empty";
	private static String usersSearchText = "Enabled";

	private static SubmitButton cancelButton;
	private static WebForm createUsrForm;
	private static SubmitButton submitButton;
	private static WebForm usrSearchForm;
	private static SubmitButton newUsrButton;

	/**
	 * Creates a new HttpUnitCreateUserTest object.
	 **/
	public HttpUnitCreateUserTest() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param s
	 **/
	public HttpUnitCreateUserTest(String s) {
		super(s);
	}

	/**
	 * Checks through commonLoginFunction for logging in in at first time
	 *
	 * @throws Exception if fails
	 **/
	public void setUp()
	  throws Exception {
		  commonLoginFunction(userMgmtPageUrl, userMgmtPageString);
	}


	//****--------------------------------------------------------------------------*****/
	//*						HttpUnit test cases		                                    */
	//****--------------------------------------------------------------------------*****/	


	/**
	 * This test case method is for Create/Edit User page of JS application.
	 * This page take User information as input parameter and on Submit goes to User Search Page
	 * on proper response test success else fail
	 * 
	 * @throws Exception if fails
	 **/
	public void testCreateUserPage()
	  throws Exception {
		User user = new User();
		user.setUserName("Test_HttpUnit");
		user.setFullName("Test_HttpUnit");
		user.setPassWord("123");
		user.setRePassWord("123");
		user.setEnable(true);

		this.createNewUser(user);
		WebResponse usrSearchPage = this.getWebConversation().getCurrentPage();

		String searchPage = usrSearchPage.getText();
		assertNotNull("User searsh page is null", searchPage);

		if ((searchPage == null) || (searchPage.trim().length() == 0)) {
			fail("No text found on response");
		}

		assertTrue(searchPage.indexOf(usersSearchText) != -1);
	}

	/**
	 * This test case method is for Create/Edit User page of JS application.
	 * This page take empty user info as parameter and on Submit remains on same Page with Error message
	 * On proper response test success else failure
	 * 
	 * @throws Exception if fails
	 **/
	public void testCreateNoUserPage()
	  throws Exception {
		 User user = new User(); //sending empty user info
		 this.createNewUser(user);

		WebResponse resultPage = this.getWebConversation().getCurrentPage();
		assertNotNull("Result page is null", resultPage);
		String createUserPage = resultPage.getText();

		if ((createUserPage == null) || (createUserPage.trim().length() == 0)) {
		fail("No text found on response");
		}

		assertTrue(createUserPage.indexOf(errMsg1Text) != -1 && createUserPage.indexOf(errMsg2Text) != -1);
	}

	/**
	 * This test case method is for Create/Edit User page of JS application.
	 * On click to Cancel Button it goes to User Search Page
	 * On proper response test success else failure
	 *
	 * @throws Exception if fails
	 **/
	public void testUserCancelPage()
	  throws Exception {

		WebResponse usrSearchPage = this.getWebConversation().getCurrentPage();
		assertNotNull("User searsh page is null", usrSearchPage);

		usrSearchForm = usrSearchPage.getFormWithName(usrSearchFormText);
		assertNotNull("Form is null", usrSearchForm);

		newUsrButton = usrSearchForm.getSubmitButton(newUsrButtonText);
		assertNotNull("Button is null", newUsrButton);

		usrSearchForm.submit(newUsrButton);

		WebResponse createUsrPage = this.getWebConversation().getCurrentPage();
		createUsrForm = createUsrPage.getFormWithName(editUserFormText);
		assertNotNull("Form is null", createUsrForm);

		cancelButton = createUsrForm.getSubmitButton(cancelButtonText);
		assertNotNull("Button is null", cancelButton);

		createUsrPage = createUsrForm.submit(cancelButton);

		//check if in response it come back to the User Search page
		WebResponse bckUsrSearchPage = this.getWebConversation().getCurrentPage();

		String str = bckUsrSearchPage.getText();
		assertNotNull("Text is null", str);
		if(str == null || str.trim().length()== 0)
			fail("There was no text in response");

		assertTrue(str.indexOf(usersSearchText)!= -1);
	}

	/**
	 * This method checks for the User Search Page and New User Button on the page
	 * also checks for the text field, text and link on the page.
	 *
	 * @param user to be created
	 *
	 * @throws Exception if fails
	 **/
	protected WebResponse createNewUser(User user) throws Exception
	{
		WebResponse userSearchPage = this.getWebConversation().getCurrentPage();
		usrSearchForm = userSearchPage.getFormWithName(usrSearchFormText);
		newUsrButton = usrSearchForm.getSubmitButton(newUsrButtonText);
		usrSearchForm.submit(newUsrButton);

		WebResponse createSearchPage = this.getWebConversation().getCurrentPage();
		assertNotNull("Text Field Element on Create/Edit Page is not present",
					  createSearchPage.getElementsWithName(userNameText));

		assertNotNull("Text Field Element on Create/Edit Page is not present",
					  createSearchPage.getElementsWithName(usersText));

		WebResponse createUserPage = this.getWebConversation().getCurrentPage();

		WebResponse resultPage = this.saveUser(createUserPage, user);

		return resultPage;

	}

	/**
	 * Saves a User .
	 *
	 * @param webResponse  page reference
	 * @param user  to be added 
	 *
	 * @return  Result response
	 *
	 * @throws Exception if fails
	 **/
	protected WebResponse saveUser(WebResponse webResponse,
								   User        user)
	  throws Exception {

			createUsrForm  = webResponse.getFormWithName(editUserFormText);
		submitButton = createUsrForm.getSubmitButton(submitButtonText);
		createUsrForm.setParameter("user.username", user.getUserName());
		createUsrForm.setParameter("user.password", user.getPassWord());
		createUsrForm.setParameter("user.fullName", user.getFullName());
		createUsrForm.setParameter("repassword", user.getRePassWord());
		createUsrForm.setCheckbox("user.enabled", user.isEnable());

		WebResponse wbResponse = createUsrForm.submit(submitButton);
		assertNotNull("The response is Null", wbResponse);

		return wbResponse;
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


	public class User {
		private String  userName;
		private String  passWord;
		private String  fullName;
		private String  rePassWord;
		private boolean enable;

		public User() {
			super();
		}

		public User(String userName,
					String passwd,
					String fullName,
					String rePassWord,
					boolean enable) {
			super();
			this.fullName = fullName;
			this.passWord = passwd;
			this.userName = userName;
			this.rePassWord = rePassWord;
			this.enable = enable;
		}

		public boolean isEnable() {
			return enable;
		}

		public void setEnable(boolean enable) {
			this.enable = enable;
		}

		public String getFullName() {
			return fullName;
		}

		public void setFullName(String fullName) {
			this.fullName = fullName;
		}

		public String getPassWord() {
			return passWord;
		}

		public void setPassWord(String passWord) {
			this.passWord = passWord;
		}

		public String getRePassWord() {
			return rePassWord;
		}

		public void setRePassWord(String rePassWord) {
			this.rePassWord = rePassWord;
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}
	}

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

		TestCase test1 = new HttpUnitCreateUserTest("testCreateUserPage");
		TestCase test2 = new HttpUnitCreateUserTest("testCreateNoUserPage");
		TestCase test3 = new HttpUnitCreateUserTest("testUserCancelPage");

		suite.addTest(test1);
		suite.addTest(test2);
		suite.addTest(test3);
		return suite;
	}

}
