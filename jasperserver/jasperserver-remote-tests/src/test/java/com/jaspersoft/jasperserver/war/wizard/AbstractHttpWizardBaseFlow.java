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

package com.jaspersoft.jasperserver.war.wizard;

import java.io.File;

import com.jaspersoft.jasperserver.war.HttpUnitBaseTestCase;
import com.meterware.httpunit.SubmitButton;
import com.meterware.httpunit.UploadFileSpec;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebResponse;


/**
 * The test cases are for: -
 * Connecting the Jasper Server OS Startup Page - checking the Jasper Server OS Wizard
 **/
public abstract class AbstractHttpWizardBaseFlow
	extends HttpUnitBaseTestCase
	implements WizardScreenConstants {
	private static WebForm      repNamingForm;
	private static WebForm      loadPageForm;
	private static SubmitButton nextButton;
	private static SubmitButton backButton;
	private static WebForm      loadJRXMLForm;
	private static String       resourcePath;
	private static WebForm repositoryForm;
	private static SubmitButton addNewButton;
	private static WebForm pageForm;
	
	/**
	 * Constructor
	 *
	 * @param s
	 **/
	public AbstractHttpWizardBaseFlow(String s) {
		super(s);
		resourceMap.put("Jrxml1", "AllAccounts.jrxml");
		resourceMap.put("Jrxml2", "SalesByMonth.jrxml");
		resourceMap.put("Jrxml3", "SalesByMonthDetail.jrxml");
		resourceMap.put("Img1", "logo.jpg");
		resourceMap.put("Img2", "report.jpg");
		resourceMap.put("text", "test.txt");
		resourcePath = getClass().getClassLoader().getResource("SalesByMonth.jrxml").getPath();
	}

	/**
	 * Checks through commonLoginFunction for loggin in at first time
	 *
	 * @throws Exception if fails
	 **/
	public void setUp()
	  throws Exception {
		wResponse = commonLoginFunction(homePageUrl);
		
	}
	
	

	//****--------------------------------------------------------------------------*****/
	//*							HttpUnit test Common Workflow                           */
	//****--------------------------------------------------------------------------*****/	

	
	
	
	protected void runWizardFlow(TestAttribute attrb)
	  throws Exception {
		
		//		step 1:Getting the repository browser page
		checkRepositoryPage();
		//		Step 2:Get report naming page
		checkRepNamingPage(attrb);
		//		step 3:Get Load JRXML page
		checkLoadFilePage(attrb);
		//		step 4:Get Select Source page
		checkSelectSourcePage();
		//		step 5:Get Data Source page
		checkDataSourcepage(attrb);
		//		step 6:Get Service Property page
		checkDataDescriptionPage(attrb);
		//		step 7:Get External Resource page
		checkPublishPage();
	}

	
	//step 1:Getting the repository browser page
	private void checkRepositoryPage() throws Exception {
		//this is response for home page
		assertNotNull(" Home Page Response is Null ", wResponse);
		
		//checks for the Repository link on the home page
		WebLink link = wResponse.getLinkWith(repositoryLink);
		assertNotNull(repositoryLink +"Link is Null ", link);

		wResponse = link.click();
		
		//checking if the link exist on the page
		assertNotNull("Link element is not on the page",wResponse.getElementsWithName(rootLink));
		
		//checking for the text on the Repositery Browser page
		String opt = wResponse.getText();
		assertNotNull("page text is Null ", opt);
		
		if ((opt == null) || (opt.trim().length() == 0)) {
			fail("Text not found in response");
		}

		assertTrue((opt.indexOf(repositoryPageText1) != -1) && (opt.indexOf(repositoryPageText2) != -1));	
		
		//createing new report
		repositoryForm = wResponse.getFormWithName(repFormText);
		assertNotNull("Form text is Null ", repositoryForm);
		
		addNewButton = repositoryForm.getSubmitButton(addNewBtnText);
		assertNotNull("Button text is Null ", addNewButton);
		
		repositoryForm.setParameter("cmbResourceType",repositoryUnitValueText);
		
		wResponse = repositoryForm.submit(addNewButton);

		String str = wResponse.getText();
		assertNotNull("page text is Null ", str);
		
		if ((str == null) || (str.trim().length() == 0)) {
			fail("Text not found in response");
		}

		assertTrue((str.indexOf(createReportPageText) != -1));	
	}
	
	
	//step 2:Checks for the Report name & Report Label
	protected void checkRepNamingPage(TestAttribute attrb)
	  throws Exception {
		assertNotNull(" Home Page Response is Null ", wResponse);

		//checking if the textField exists
		assertNotNull("No text field on the page", wResponse.getElementsWithName(nameText));

		//checking for the text on the page
		String string = wResponse.getText();
		assertNotNull("page text is Null ", string);
		
		if ((string == null) || (string.trim().length() == 0)) {
			fail("There is no text in response");
		}

		assertTrue((string.indexOf(pageText) != -1));

		//checking for "cancel" button functionality
		repNamingForm = wResponse.getFormWithName(repNameFormText);
		assertNotNull("Form text is Null ", repNamingForm);
		
		SubmitButton cancelButton = repNamingForm.getSubmitButton(cancelBtnText);
		assertNotNull("Button text is Null ", cancelButton);
		
		wResponse = repNamingForm.submit(cancelButton);

		String report = wResponse.getText();		
		assertNotNull("page text is Null ", report);
		
		if ((report == null) || (report.trim().length() == 0)) {
			fail("There is no text in response");
		}
		
		assertTrue(report.indexOf(ViewReportPageText) != -1);

		repositoryForm = wResponse.getFormWithName(repFormText);
		assertNotNull("Form text is Null ", repositoryForm);
		
		addNewButton = repositoryForm.getSubmitButton(addNewBtnText);
		assertNotNull("Button text is Null ", addNewButton);
		
		repositoryForm.setParameter("cmbResourceType",repositoryUnitValueText);
		
		wResponse = repositoryForm.submit(addNewButton);		

		String wizard = wResponse.getText();
		assertNotNull("page text is Null ", wizard);
		
		if ((wizard == null) || (wizard.trim().length() == 0)) {
			fail("there is no text on response");
		}

		assertTrue(wizard.indexOf(pageText) != -1);
		
		checkNxtBtnForRepNamingPage(attrb);
	}

	
	// Next button functionality for report naming page
	private void checkNxtBtnForRepNamingPage(TestAttribute attrb)
	  throws Exception {
		assertNotNull(" Report Naming Page Response is Null ", wResponse);
		
		repNamingForm = wResponse.getFormWithName(repNameFormText);
		assertNotNull("Form text is Null ", repNamingForm);
		
		nextButton = repNamingForm.getSubmitButton(nextBtnText);
		assertNotNull("Button text is Null ", nextButton);
		
		//checking for the "next" button functionality with error message
		assertNotNull(" Report Naming Page Response is Null ", wResponse);
				
		assertNotNull(" FORM object is null ", repNamingForm);

		nextButton = repNamingForm.getSubmitButton(nextBtnText);
		assertNotNull(" Next button is null ", nextButton);

		wResponse = repNamingForm.submit(nextButton);

		String str = wResponse.getText();
		assertNotNull("page text is Null ", str);
		
		if ((str == null) || (str.trim().length() == 0)) {
			fail(" there is no text on response ");
		}

		assertTrue((str.indexOf(errorMsgRepPageText1) != -1) &&
		           (str.indexOf(errorMsgRepPageText2) != -1));

		//checking for the "next" button functionality with proper response	
		
		repNamingForm.setParameter("reportUnit.name", attrb.getReportName());
		repNamingForm.setParameter("reportUnit.label", attrb.getLabel());

		wResponse = repNamingForm.submit(nextButton);

		String nextPage = wResponse.getText();
		assertNotNull("page text is Null ", nextPage);
		
		if ((nextPage == null) || (nextPage.trim().length() == 0)) {
			fail(" there is no text on response ");
		}
		assertTrue(nextPage.indexOf(loadPageText1) != -1);
	}
	

	//step 3: Get Load JRXML page
	protected void checkLoadFilePage(TestAttribute attrb)
	  throws Exception {
		assertNotNull(" Report Naming Page Response is Null ", wResponse);

		//checking if the textField exists
		assertNotNull("No text field on the page", wResponse.getElementsWithName(fileFieldText));

		//checking for text
		String string = wResponse.getText();
		assertNotNull("page text is Null ", string);
		
		if ((string == null) || (string.trim().length() == 0)) {
			fail(" There is no text on response ");
		}

		assertTrue((string.indexOf(loadPageText1) != -1));

		//checking for the back button
		loadJRXMLForm = wResponse.getFormWithName(formText);
		backButton    = loadJRXMLForm.getSubmitButton(backBtnText);

		wResponse = loadJRXMLForm.submit(backButton);

		String str = wResponse.getText();
		assertNotNull("page text is Null ", str);
		
		if ((str == null) || (str.trim().length() == 0)) {
			fail("There is no text in response");
		}

		assertTrue(str.indexOf("Description") != -1);

		repNamingForm = wResponse.getFormWithName(repNameFormText);
		assertNotNull("Form text is Null ", repNamingForm);
		
		nextButton    = repNamingForm.getSubmitButton(nextBtnText);
		assertNotNull("Button text is Null ", nextButton);
		
		wResponse = repNamingForm.submit(nextButton);

		String str1 = wResponse.getText();
		assertNotNull("page text is Null ", str1);
		
		if ((str1 == null) || (str1.trim().length() == 0)) {
			fail("There is no text in response");
		}

		assertTrue(str1.indexOf(loadPageText1) != -1);

		checkNxtBtnForLoadFilePage(attrb);
	}
	

	//next button functionality for Load JRXML page
	private void checkNxtBtnForLoadFilePage(TestAttribute attrb)
	  throws Exception {
			
		assertNotNull(" Load File Page Response is Null ", wResponse);
		
		loadPageForm = wResponse.getFormWithName(formText);
		assertNotNull("Form text is Null ", loadPageForm);
		
		//next button with no uploaded file
		loadPageForm = wResponse.getFormWithName(formText);
		assertNotNull("Form text is Null ", loadPageForm);
		
		nextButton   = loadPageForm.getSubmitButton(nextBtnText);
		assertNotNull("Button text is Null ", nextButton);
		
		wResponse = loadPageForm.submit(nextButton);

		String error = wResponse.getText();
		assertNotNull("page text is Null ", error);
		
		if ((error == null) || (error.trim().length() == 0)) {
			fail("There is no text in response");
		}

		assertTrue(error.indexOf(errorMsgLoadPageText) != -1);		

		//next button with proper response		
		SubmitButton nextButton2 = loadPageForm.getSubmitButton(nextBtnText);
		File             jrxml      = new File(getJrxmlFileResourceURL(attrb.getJrxml()));
		UploadFileSpec[] uploadFile = new UploadFileSpec[] { new UploadFileSpec(jrxml) };
		loadPageForm.setParameter("jrxmlData", uploadFile);

		wResponse = loadPageForm.submit(nextButton2);

		String page = wResponse.getText();
		assertNotNull("page text is Null ", page);
		
		if ((page == null) || (page.trim().length() == 0)) { 
			fail("There is no text in response");
		}

		assertTrue(page.indexOf(reportListPageText) != -1);

		
		//For adding new Control through Add Control button
		
		//description for file through Add Control
		Description description = new Description();
		description.setFileName("newFile"+String.valueOf((int)(Math.random()*1000)));
		description.setFileLabel("newLink");
		description.setFileDescription("This is new file");
		
		wResponse = addControl(wResponse, description);
		
		String text1 = wResponse.getText();
		assertNotNull("page text is Null ", text1);
		
		if ((text1 == null) || (text1.trim().length() == 0)) { 
			fail("There is no text in response");
		}

		assertTrue(text1.indexOf(addControlPageText) != -1);
		
		
		//For adding new resources
		
		//description for file through Add Resources
		Description description2 = new Description();
		description2.setFileName("ResourceFile"+String.valueOf((int)(Math.random()*1000)));
		description2.setFileLabel("ResourceLink");
		description2.setFileDescription("This is new Resource File");
		
		wResponse = addResource(wResponse,description2, getFileResourceURLByType("Img2"));
		
		String text2 = wResponse.getText();
		assertNotNull("page text is Null ", text2);
		
		if ((text2 == null) || (text2.trim().length() == 0)) { 
			fail("There is no text in response");
		}

		assertTrue(text2.indexOf(resourceFileText) != -1);
		
		String str = wResponse.getText();
		assertNotNull("page text is Null ", str);
		
		//adding file details through Add Now link		
		if(str.indexOf("LogoLink") != -1){
		
		wResponse = addNowDetails(wResponse, getFileResourceURLByType("Img2"));
		
		String text3 = wResponse.getText();
		assertNotNull("page text is Null ", text3);
		
		if ((text3 == null) || (text3.trim().length() == 0)) { 
			fail("There is no text in response");
		}

		assertTrue(text3.indexOf(fileAddedText) != -1);
		
		
		wResponse = addNowDetails(wResponse, getFileResourceURLByType("Img1"));
		
		String text4 = wResponse.getText();
		assertNotNull("page text is Null ", text4);
		
		if ((text4 == null) || (text4.trim().length() == 0)) { 
			fail("There is no text in response");
		}

		assertTrue(text4.indexOf(fileAddedText) != -1);
		}else{
			wResponse = addNowDetails(wResponse, getFileResourceURLByType("Jrxml3"));
			
			String text5 = wResponse.getText();
			
			if ((text5 == null) || (text5.trim().length() == 0)) { 
				fail("There is no text in response");
			}

			assertTrue(text5.indexOf(fileAddedText) != -1);
			
			
			wResponse = addNowDetails(wResponse, getFileResourceURLByType("Img1"));
			
			String text6 = wResponse.getText();
			
			if ((text6 == null) || (text6.trim().length() == 0)) { 
				fail("There is no text in response");
			}

			assertTrue(text6.indexOf(fileAddedText) != -1);
		}
		
		//Going to next page through Next button
		pageForm = wResponse.getFormWithName(listPageFormText);
		assertNotNull("Form is Null ", pageForm);
		
		nextButton = pageForm.getSubmitButton(nextBtnText);
		assertNotNull("button is Null ", nextButton);
		
		wResponse = pageForm.submit(nextButton);
		
		String string = wResponse.getText();
		assertNotNull("page text is Null ", string);
		
		if ((string == null) || (string.trim().length() == 0)) {
			fail("There is no text on response");
		}

		assertTrue(string.indexOf(sourceSelectionPageText) != -1);
	}

	
	//step 4:Get Select Source page
	public void checkSelectSourcePage()
	  throws Exception {
		assertNotNull("Upload Success Page Response is Null ", wResponse);

		WebForm sourceSellectionForm = wResponse.getFormWithName(sourceSellFormText);
		assertNotNull("Form is Null ", sourceSellectionForm);
		
		//checking for back button
		SubmitButton backButton = sourceSellectionForm.getSubmitButton(backBtnText);
		assertNotNull("Form is Null ", backButton);
		
		wResponse = sourceSellectionForm.submit(backButton);

		loadPageForm = wResponse.getFormWithName(listPageFormText);
		nextButton   = loadPageForm.getSubmitButton(nextBtnText);

		wResponse = loadPageForm.submit(nextButton);

		String next = wResponse.getText();
		assertNotNull("page text is Null ", next);
		
		if ((next == null) || (next.trim().length() == 0)) {
			fail("There is no text in response");
		}

		assertTrue(next.indexOf(sourceSelectionPageText) != -1);

		//checking for next button
		WebForm sourceSelectionForm2 = wResponse.getFormWithName(sourceSellFormText);
		nextButton = sourceSelectionForm2.getSubmitButton(nextBtnText);

		sourceSelectionForm2.setParameter("source", "LOCAL");
		sourceSelectionForm2.setParameter("selectedUri", "/datasources/JServerJNDIDS");

		wResponse = sourceSelectionForm2.submit(nextButton);

		String str = wResponse.getText();
		assertNotNull("page text is Null ", str);
		
		if ((str == null) || (str.trim().length() == 0)) {
			fail("There is no text in response");
		}

		assertTrue(str.indexOf(dataSrcPageText1) != -1);
	}

	
	//step 5:Get Data Source page
	public void checkDataSourcepage(TestAttribute attrb)
	  throws Exception {
		assertNotNull(" Load File Page Response is Null ", wResponse);

		//checking for text on the page
		String str = wResponse.getText();
		assertNotNull("page text is Null ", str);
		
		if ((str == null) || (str.trim().length() == 0)) {
			fail("There is no text on response");
		}

		assertTrue((str.indexOf(dataSrcPageText1) != -1));

		WebForm dataSrcForm = wResponse.getFormWithName(dataSrcPageFormText);
		assertNotNull("Form is Null ", dataSrcForm);
		
		nextButton = dataSrcForm.getSubmitButton(nextBtnText);
		assertNotNull("Form is Null ", nextButton);
		
		dataSrcForm.setParameter("type", attrb.getDataSourceAttrb().getDataSourceType());
		wResponse = dataSrcForm.submit(nextButton);
	}

	
	//step 6:Get Data Description page
	public void checkDataDescriptionPage(TestAttribute attrb)
	  throws Exception {
		assertNotNull(" Data Source Page Response is Null ", wResponse);

		String string = wResponse.getText();
		assertNotNull("page text is Null ", string);
		
		if (string.indexOf("JDBC") != -1) {
			assertNotNull(" Data Source Page Response is Null ", wResponse);

			//next buttoon with error message
			WebForm propertyForm1 = wResponse.getFormWithName("fmCRValidConf");
			nextButton = propertyForm1.getSubmitButton(nextBtnText);

			wResponse = propertyForm1.submit(nextButton);

			String opt = wResponse.getText();

			if ((opt == null) || (opt.trim().length() == 0)) {
				fail("There is no text on the page");
			}

			assertTrue((opt.indexOf(errMsgText2) != -1) &&
			           (opt.indexOf(errorMsgText3) != -1));

			//now the next button with proper response		
			propertyForm1.setParameter("reportDataSource.name", attrb.getDataSourceAttrb().getName());
			propertyForm1.setParameter("reportDataSource.label", attrb.getDataSourceAttrb().getLabel());

			propertyForm1.setParameter("reportDataSource.driverClass", attrb.getDataSourceAttrb().getDriver());
			propertyForm1.setParameter("reportDataSource.connectionUrl", attrb.getDataSourceAttrb().getUrl());
			propertyForm1.setParameter("reportDataSource.username", attrb.getDataSourceAttrb().getUsername());
			propertyForm1.setParameter("reportDataSource.password", attrb.getDataSourceAttrb().getPassword());

			wResponse = propertyForm1.submit(nextButton);
		} else {
			assertNotNull(" Data Source Page Response is Null ", wResponse);

			//next buttoon with error message
			WebForm propertyForm2 = wResponse.getFormWithName(propertyPageFormText);
			nextButton = propertyForm2.getSubmitButton(nextBtnText);

			wResponse = propertyForm2.submit(nextButton);

			String str = wResponse.getText();

			if ((str == null) || (str.trim().length() == 0)) {
				fail("There is no text on the page");
			}

			assertTrue((str.indexOf(errMsgServiceNameText1) != -1) &&
			           (str.indexOf(errMsgText2) != -1));

			//now the next button with proper response
			propertyForm2.setParameter("reportDataSource.name", attrb.getDataSourceAttrb().getName());
			propertyForm2.setParameter("reportDataSource.label", attrb.getDataSourceAttrb().getLabel());
			propertyForm2.setParameter("reportDataSource.jndiName", attrb.getDataSourceAttrb().getServiceName());

			wResponse = propertyForm2.submit(nextButton);
		}

		String page = wResponse.getText();
		assertNotNull("page text is Null ", page);
		if ((page == null) || (page.trim().length() == 0)) {
			fail("There is no text on the page");
		}

		assertTrue(page.indexOf(validationPageText) != -1);
	}

	
	//step 7:Get Report Velidation page
	public void checkPublishPage()
	  throws Exception {
		assertNotNull(" Data Source Description Page Response is Null ", wResponse);

		WebForm extResourceForm = wResponse.getFormWithName(testFormText);
		assertNotNull("Form is Null ", extResourceForm);
		
		SubmitButton saveButton = extResourceForm.getSubmitButton(saveSelectedBtnText);
		assertNotNull("Form is Null ", saveButton);
		
		wResponse = extResourceForm.submit(saveButton);

		String string = wResponse.getText();
		assertNotNull("page text is Null ", string);
		
		if ((string == null) || (string.trim().length() == 0)) {
			fail("There is no text on the page");
		}

		assertTrue(string.indexOf(ViewReportPageText) != -1);
	}
	


	//****--------------------------------------------------------------------------*****/
	//*				HttpUnit private framework methods                                  */
	//****--------------------------------------------------------------------------*****/		

	
	
	/**
	 * @param type of file
	 *
	 * @return resourceMap 
	 **/
	protected String getFileResourceByType(String type) {
		return (String)resourceMap.get(type);
		
	}

	/**
	 * Shows which jrxml to be taken from resource
	 *
	 * @return file path
	 **/
	private String getJrxmlFileResourceURL(String jrxml) {
		//return resourcePath+File.separator+jrxml;
		return  getClass().getClassLoader().getResource(jrxml).getPath();
	}

	/**
	 * Shows the type of file to be uploaded and gives the URL for the path
	 *
	 * @param type of file
	 *
	 * @return URL from where file to be uploaded
	 **/
	private String getFileResourceURLByType(String type) {
		//return  resourcePath+File.separator+resourceMap.get(type);
		return  getClass().getClassLoader().getResource((String)resourceMap.get(type)).getPath();
	}

	

	//****--------------------------------------------------------------------------*****/
	//*				Abstract method defination                                          */
	//****--------------------------------------------------------------------------*****/	

	
	
	/**
	 * Add controls for File
	 * 
	 * @param webResponse page refrence
	 * @param description of parameters
	 *  
	 * @return webResponse
	 * 
	 * @throws Exception if fails
	 */
	public WebResponse addControl(WebResponse webResponse, 
									Description description)
	throws Exception {
		
		assertNotNull("List Report Detail page response is Null",webResponse);
		
		String str = webResponse.getText();
		
		if ((str == null) || (str.trim().length() == 0)) {
			fail("There is no text on response");
		}

		assertTrue(str.indexOf(reportListPageText) != -1);
		
		pageForm = webResponse.getFormWithName(listPageFormText);
		assertNotNull("Form is Null ", pageForm);
		
		SubmitButton addControlBtn = pageForm.getSubmitButton(addCtrlBtnText);
		assertNotNull("Button is Null ", addControlBtn);
		
		webResponse = pageForm.submit(addControlBtn);
		
		String text = webResponse.getText();
		assertNotNull("page text is Null ", text);
		
		if(text == null || text.trim().length()==0)
			fail("There is no text on page");
		
		assertTrue(text.indexOf(locateInputControlText)!= -1);
		
		WebForm locateInputForm = webResponse.getFormWithName(datadescFormText);
		assertNotNull("Form is Null ", locateInputForm);
		
		nextButton = locateInputForm.getSubmitButton(nextBtnText);
		assertNotNull("button is Null ", nextButton);
		
		locateInputForm.setParameter("inputControlSource","LOCAL");
		
		webResponse = locateInputForm.submit(nextButton);
		
		String string = webResponse.getText();
		assertNotNull("page text is Null ", string);
		
		if(string == null || string.trim().length()==0)
			fail("There is no text on page");
		
		assertTrue(string.indexOf(reportInputControlText)!= -1);
		
		
		WebForm descriptionForm = webResponse.getFormWithName(descriptionFormText);
		assertNotNull("Form is Null ", descriptionForm);
		
		nextButton = descriptionForm.getSubmitButton(nextBtnText);
		assertNotNull("button is Null ", nextButton);
		
		descriptionForm.setParameter("inputControl.name",description.getFileName());
		descriptionForm.setParameter("inputControl.label",description.getFileLabel());
		
		webResponse = descriptionForm.submit(nextButton);
		
		WebForm dataTypeForm = webResponse.getFormWithName(datadescFormText);
		assertNotNull("Form is Null ", dataTypeForm);
		
		nextButton = dataTypeForm.getSubmitButton(nextBtnText);
		assertNotNull("button is Null ", nextButton);
		
		dataTypeForm.setParameter("source","LOCAL");
				
		webResponse = dataTypeForm.submit(nextButton);
		
		String page = webResponse.getText();
		assertNotNull("page text is Null ", page);
		
		if(page == null || page.trim().length()==0)
			fail("There is no text on page");
		
		assertTrue(page.indexOf(dataTypePageText)!= -1);
		
		WebForm dataDescForm = webResponse.getFormWithName(dataTypeFormText);
		assertNotNull("Form is Null ", dataDescForm);
		
		SubmitButton saveButton = dataDescForm.getSubmitButton(savetBtnText);
		assertNotNull("button is Null ", saveButton);
		
		dataDescForm.setParameter("dataType.name",description.getFileName());
		dataDescForm.setParameter("dataType.label",description.getFileLabel());
		
		webResponse = dataDescForm.submit(saveButton);
		
		return webResponse;
	}	
	
	
	
	/**
	 * Add resource for File
	 * 
	 * @param webResponse page refrence
	 * @param description of parameters
	 * @param fileLocation locates the file location 
	 *  
	 * @return webResponse
	 * 
	 * @throws Exception if fails
	 */
	public WebResponse addResource(WebResponse webResponse, 
									Description description,
									String  fileLocation)
	throws Exception {
		
		assertNotNull("List Report Detail page response is Null",webResponse);
		
		pageForm = webResponse.getFormWithName(listPageFormText);
		
		SubmitButton addResourceBtn = pageForm.getSubmitButton(addResourceBtnText);
		
		webResponse = pageForm.submit(addResourceBtn);
		
		String string = webResponse.getText();
		
		if(string == null || string.trim().length()==0)
			fail("There is no text on page");
		
		assertTrue(string.indexOf(reportFileResPageText)!= -1);
		
		WebForm loadPageForm = webResponse.getFormWithName(formText);
		
		SubmitButton nextButton = loadPageForm.getSubmitButton(nextBtnText);
		
		File             file1        = new File(fileLocation);
		UploadFileSpec[] uploadFile = new UploadFileSpec[] { new UploadFileSpec(file1) };

		loadPageForm.setParameter("source", "FILE_SYSTEM");
		loadPageForm.setParameter("newData", uploadFile);
		
		webResponse = loadPageForm.submit(nextButton);
		
		String str = webResponse.getText();
		if(str==null || str.trim().length()==0)
			fail("There is no text on the page");
		
		assertTrue(str.indexOf(fileResourcePageText)!= -1);
		
		WebForm rescDescForm = webResponse.getFormWithName(resDescriptiomFormText);
		
		nextButton = rescDescForm.getSubmitButton(nextBtnText);
		
		rescDescForm.setParameter("fileResource.name",description.getFileName());
		rescDescForm.setParameter("fileResource.label",description.getFileLabel());
		rescDescForm.setParameter("fileResource.description",description.getFileDescription());
		
		webResponse = rescDescForm.submit(nextButton);		
		
		return webResponse;		
		
	}	
	
	
	/**
	 * Add Details for File
	 *
	 * @param wResponse page refrence
	 * @param fileLocation where file is located
	 * @param description of parameters
	 *
	 * @return wResponse
	 *
	 * @throws Exception if fails
	 **/
	public WebResponse addNowDetails(WebResponse wResponse,
	                                 String      fileLocation)
	  throws Exception {
		assertNotNull(" Load File Page Response is Null ", wResponse);

		String str = wResponse.getText();
		
		if ((str == null) || (str.trim().length() == 0)) {
			fail("There is no text on response");
		}

		assertTrue(str.indexOf(reportListPageText) != -1);

		pageForm = wResponse.getFormWithName(listPageFormText);
		
		WebLink addNowLink = wResponse.getLinkWith("Add Now");
				
		wResponse = addNowLink.click();
		
		String string = wResponse.getText();
		if ((string == null) || (string.trim().length() == 0)) {
			fail("There is no text on response");
		}

		assertTrue(string.indexOf(reportFileResPageText) != -1);
		
				
		WebForm form1 = wResponse.getFormWithName(formText);
		
		nextButton = form1.getSubmitButton(nextBtnText);

		File             file        = new File(fileLocation);
		UploadFileSpec[] uploadFile1 = new UploadFileSpec[] { new UploadFileSpec(file) };

		form1.setParameter("source", "FILE_SYSTEM");
		form1.setParameter("newData", uploadFile1);

		wResponse = form1.submit(nextButton);

		String string2 = wResponse.getText();

		if ((string2 == null) || (string2.trim().length() == 0)) {
			fail("There is no text on response");
		}

		assertTrue(string2.indexOf(fileResourcePageText) != -1);

		WebForm descriptionForm = wResponse.getFormWithName(resDescriptiomFormText);
		nextButton = descriptionForm.getSubmitButton(nextBtnText);
		wResponse = descriptionForm.submit(nextButton);

		return wResponse;
	}

	/**
	 * Form bean for Resource nameing
	 *
	 **/
	public class Description {
		private String fileName;
		private String fileLabel;
		private String fileDescription;
		private String fileType;

		public Description() {
			super();
		}

		public Description(String name,
		                   String label,
		                   String description,
		                   String type) {
			super();
			this.fileName        = name;
			this.fileLabel       = label;
			this.fileDescription = description;
			this.fileType        = type;
		}

		public String getFileDescription() {
			return fileDescription;
		}

		public void setFileDescription(String fileDescription) {
			this.fileDescription = fileDescription;
		}

		public String getFileLabel() {
			return fileLabel;
		}

		public void setFileLabel(String fileLabel) {
			this.fileLabel = fileLabel;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getFileType() {
			return fileType;
		}

		public void setFileType(String fileType) {
			this.fileType = fileType;
		}
	}		
	
}
