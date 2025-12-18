package com.jaspersoft.jasperserver.rest.test;

import static com.jaspersoft.jasperserver.rest.test.RESTTestUtilsAndConsts.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class POSTTest extends RESTTest{
	
	@Before
	public void setUp() {
    	super.setUp();
    	httpReqCE = new HttpPost();
    	httpReqPro = new HttpPost();
     }
	
	/** POST TESTS **/
	
	@Test
	public void Job_Post_200() throws Exception
	{
		putSampleJob_CE();
		int ceJobIndex = getJobIndexFromJobSummary(getSampleJobSummary_CE());
		
		//building update request
		BasicHttpEntity reqEntity = new BasicHttpEntity();
		reqEntity.setContent(new FileInputStream(LOCAL_UPDATE_RDS + SAMPLE_JOB_RD));
		((HttpEntityEnclosingRequestBase)httpReqCE).setEntity(reqEntity);
		sendAndAssert_CE(httpReqCE, SERVICE_JOB+"/"+ceJobIndex);
		sendAndAssert_CE(new HttpDelete(), SERVICE_JOB+"/"+ceJobIndex);
		

		putSampleJob_PRO();
		int proJobIndex = getJobIndexFromJobSummary(getSampleJobSummary_PRO());
		reqEntity = new BasicHttpEntity();
		
		//appending the file descriptor from a file 
		reqEntity.setContent(new FileInputStream(LOCAL_UPDATE_RDS + SAMPLE_JOB_RD));
		
		((HttpEntityEnclosingRequestBase)httpReqPro).setEntity(reqEntity);
		sendAndAssert_PRO(httpReqPro, SERVICE_JOB+"/"+proJobIndex);
		sendAndAssert_PRO(new HttpDelete(), SERVICE_JOB+"/"+proJobIndex);
		
	}
	
	
	/* PRO OBJECTS*/
	@Test 
    public void Organization_Post_200() throws Exception{
		//it is covered by the organization put
    }
	
	@Test 
    public void Permission_Post_200() throws Exception{
		putSampleFolder();
		sendAndAssert_CE(httpReqCE, SERVICE_PERMISSION+"/JUNIT_NEW_FOLDER", LOCAL_RESOURCE_DESCRIPTOR_PATH + SAMPLE_PERMISSION_CE_RD, HttpStatus.SC_OK);
		sendAndAssert_PRO(httpReqPro, SERVICE_PERMISSION+"/JUNIT_NEW_FOLDER", LOCAL_RESOURCE_DESCRIPTOR_PATH + SAMPLE_PERMISSION_PRO_RD, HttpStatus.SC_OK);
		deleteSampleFolder();
    	
    }
	
	@Test 
    public void User_Post_200() throws Exception 
    {
		
		sendAndAssert_CE(new HttpPut(), SERVICE_USER, LOCAL_NEW_RDS + SAMPLE_USER_CE_RD, HttpStatus.SC_CREATED);
    	sendAndAssert_PRO(new HttpPut(), SERVICE_USER, LOCAL_NEW_RDS + SAMPLE_USER_PRO_RD, HttpStatus.SC_CREATED);
    	
    	sendAndAssert_CE(httpReqCE, SERVICE_USER+"/JUNIT_USER", LOCAL_UPDATE_RDS + SAMPLE_USER_CE_RD);
    	sendAndAssert_PRO(httpReqPro, SERVICE_USER+"/JUNIT_USER", LOCAL_UPDATE_RDS + SAMPLE_USER_PRO_RD);
    	
    	sendAndAssert_CE(new HttpDelete(), "/user/JUNIT_USER");
    	sendAndAssert_PRO(new HttpDelete(), "/user/JUNIT_USER"+PIPE+"organization_1");
    }
	
	@Test 
    public void Role_Post_200() throws Exception 
    {
//		sendAndAssert_CE(new HttpPut(), SERVICE_ROLE, LOCAL_NEW_RDS + SAMPLE_ROLE_RD);
//		sendAndAssert_PRO(new HttpPut(), SERVICE_ROLE, LOCAL_UPDATE_RDS + SAMPLE_ROLE_RD);
//		
//		sendAndAssert_CE(httpReqCE, SERVICE_ROLE+"/ROLE_JUNIT", LOCAL_UPDATE_RDS + SAMPLE_ROLE_RD);
//		sendAndAssert_PRO(httpReqPro, SERVICE_ROLE+"/ROLE_JUNIT", LOCAL_UPDATE_RDS + SAMPLE_ROLE_RD);
//		
//		// deleting the created roles
//		sendAndAssert_CE(new HttpDelete(), SERVICE_ROLE+"/ROLE_JUNIT_UPDATED");
//		sendAndAssert_PRO(new HttpDelete(), SERVICE_ROLE+"/ROLE_JUNIT_UPDATED");
    }
	
	
	
	  //creation of new file resource the RD is before the binaries
//	@Test 
//    public void Post200CreateNewFile_V1() throws Exception 
//    {
//		putSampleImageFileResource(SAMPLE_IMAGE_FILE_SERVER_PATH);
//
//		String fileRd = LOCAL_UPDATE_RDS+RD_IMAGE_FILE;
//		String fileBins = LOCAL_RESOURCE_DESCRIPTOR_PATH+SAMPLE_IMAGE_FILE_BIN;
//    	
//    	//building the body
//		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
//		
//		//appending the file descriptor from a file 
//		reqEntity.addPart(REQUEST_PARAMENTER_RD, new StringBody(RESTTestUtilsAndConsts.getResourceDescriptor(new File(fileRd))));
//		
//		//appending the binaries to the request body
//		FileBody bin = new FileBody(new File(fileBins));
//		reqEntity.addPart(REQUEST_SAMPLE_IMAGE_PATH, bin );
//		
//		((HttpEntityEnclosingRequestBase)httpReq).setEntity(reqEntity);
//		
//		//executing the request
//		httpRes = executeCall(httpReq, SAMPLE_IMAGE_FILE_SERVER_PATH);
//		Assert.assertTrue("basic response check did not pass", isValidResposnse());
//		
//		deleteResource(SAMPLE_IMAGE_FILE_SERVER_PATH);
//    }
//	
//	@Test 
//    public void Post200NewDataType() throws Exception 
//    {
//		putSampleResource(RD_DATA_TYPE, SAMPLE_DATA_TYPE_SERVER_PATH);
//		
//    	//building the body
//		BasicHttpEntity reqEntity = new BasicHttpEntity();
//		
//		//appending the file descriptor from a file 
//		reqEntity.setContent(new FileInputStream(new File(LOCAL_UPDATE_RDS+RD_DATA_TYPE)));
//		
//		((HttpEntityEnclosingRequestBase)httpReq).setEntity(reqEntity);
//		
//		//executing the request
//		httpRes = executeCall(httpReq, SAMPLE_DATA_TYPE_SERVER_PATH);
//		Assert.assertTrue("basic response check did not pass", isValidResposnse());
//		
//		deleteResource(SAMPLE_DATA_TYPE_SERVER_PATH);
//    }
//	
//	@Test //copies a file from /ContentFiles to /ContentFiles/JUNIT_NEW_FOLDER  
//    public void Post200CopyFile() throws Exception 
//    {
//		putSampleFolder();
//		putSampleResource(RD_DATA_TYPE, SAMPLE_DATA_TYPE_SERVER_PATH);
//		
//    	//executing the request
//		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
//    	qparams.add(new BasicNameValuePair(COPY_TO, SAMPLE_FOLDER_SERVER_ACTUAL_PATH+"/COPY_TO_TEST"));
//    	
//		httpRes = executeCall(httpReq, SAMPLE_DATA_TYPE_SERVER_PATH, qparams);
//		Assert.assertTrue("basic response check did not pass", isValidResposnse());
//		
//		deleteResource(SAMPLE_FOLDER_SERVER_PATH);
//		deleteResource(SAMPLE_DATA_TYPE_SERVER_PATH);
//    }
//	
//	//http://localhost:8080/jasperserver/rest/resources/reports/samples?copyTo=/reports/NEW_FOLDER
//	@Test //copies a file from /ContentFiles to /ContentFiles/JUNIT_NEW_FOLDER  
//    public void Post200CopyFolder() throws Exception 
//    {
//		//putSampleFolder();
//		//putSampleResource(SAMPLE_DATA_TYPE_RD, SAMPLE_DATA_TYPE_SERVER_PATH);
//		
//    	//executing the request
//		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
//    	qparams.add(new BasicNameValuePair(COPY_TO, "/datasources/JUNIT"));
//    	
//		httpRes = executeCall(httpReq, "/resource/reports/samples", qparams);
//		Assert.assertTrue("basic response check did not pass", isValidResposnse());
//		
//		//deleteResource(SAMPLE_FOLDER_SERVER_PATH);
//		//deleteResource(SAMPLE_DATA_TYPE_SERVER_PATH);
//    }
//		
//		
//	
//	@Test 
//    public void Post200MoveFile() throws Exception 
//    {
//		putSampleFolder();
//		putSampleResource(RD_DATA_TYPE, SAMPLE_DATA_TYPE_SERVER_PATH);
//		
//    	//executing the request
//		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
//    	qparams.add(new BasicNameValuePair(MOVE_TO, SAMPLE_FOLDER_SERVER_ACTUAL_PATH));
//    	
//		httpRes = executeCall(httpReq, SAMPLE_DATA_TYPE_SERVER_PATH, qparams);
//		Assert.assertTrue("basic response check did not pass", isValidResposnse());
//		
//		deleteResource(SAMPLE_FOLDER_SERVER_PATH);
//    }
//
//	
//	@Test 
//    public void Put200InputControl() throws Exception 
//    {
//		String rdFileName = LOCAL_RDS+SAMPLE_INPUT_CONTROL_RD;
//		
//    	//building the body
//		BasicHttpEntity reqEntity = new BasicHttpEntity();
//		
//		//appending the file descriptor from a file 
//		reqEntity.setContent(new FileInputStream(new File(rdFileName)));
//		
//		((HttpEntityEnclosingRequestBase)httpReq).setEntity(reqEntity);
//		
//		
//		//executing the request
//		httpRes = executeCall(httpReq, SAMPLE_INPUT_CONTROL_PATH);
//		Assert.assertTrue("basic response check did not pass", isValidResposnse());
//		
//		deleteResource(SAMPLE_INPUT_CONTROL_PATH);
//    }
//	
//	@Test 
//    public void Put200JDBC() throws Exception 
//    {
//		String rdFileName = LOCAL_RDS+SAMPLE_JDBC_FILE_RD;
//		
//    	//building the body
//		BasicHttpEntity reqEntity = new BasicHttpEntity();
//		
//		//appending the file descriptor from a file 
//		reqEntity.setContent(new FileInputStream(new File(rdFileName)));
//		
//		((HttpEntityEnclosingRequestBase)httpReq).setEntity(reqEntity);
//		
//		
//		//executing the request
//		httpRes = executeCall(httpReq, SAMPLE_JDBC_FILE_SERVER_PATH);
//		Assert.assertTrue("basic response check did not pass", isValidResposnse());
//		
//		deleteResource(SAMPLE_JDBC_FILE_SERVER_PATH);
//    }
//	
//	@Test 
//    public void Put200ListOfValues() throws Exception 
//    {
//		String rdFileName = LOCAL_RDS+SAMPLE_LIST_OF_VALUES_RD;
//		
//    	//building the body
//		BasicHttpEntity reqEntity = new BasicHttpEntity();
//		
//		//appending the file descriptor from a file 
//		reqEntity.setContent(new FileInputStream(new File(rdFileName)));
//		
//		((HttpEntityEnclosingRequestBase)httpReq).setEntity(reqEntity);
//		
//		
//		//executing the request
//		httpRes = executeCall(httpReq, SAMPLE_LIST_OF_VALUES_SERVER_PATH);
//		Assert.assertTrue("basic response check did not pass", isValidResposnse());
//		
//		deleteResource(SAMPLE_LIST_OF_VALUES_SERVER_PATH);
//    }
	
	/**/
	/* SERVER RESOURCES */
	@Test 
    public void resource_Folder_POST_200() throws Exception
    {
//		sendAndAssert_CE(new HttpPut(), RESOURCE+"/JUNIT_NEW_FOLDER", LOCAL_NEW_RDS + SAMPLE_FOLDER_RD, HttpStatus.SC_CREATED);
//		sendAndAssert_CE(httpReqCE, RESOURCE+"/JUNIT_NEW_FOLDER", LOCAL_UPDATE_RDS + SAMPLE_FOLDER_RD, HttpStatus.SC_CREATED);
//		sendAndAssert_CE(new HttpDelete(), RESOURCE+"/JUNIT_NEW_UPDATED_FOLDER");
		
		
//		sendAndAssert_PRO(new HttpPut(), RESOURCE+"/JUNIT_NEW_FOLDER", LOCAL_NEW_RDS + SAMPLE_FOLDER_RD, HttpStatus.SC_CREATED);
//		sendAndAssert_PRO(httpReqPro, RESOURCE+"/JUNIT_NEW_FOLDER", LOCAL_UPDATE_RDS + SAMPLE_FOLDER_RD);
//		sendAndAssert_PRO(new HttpDelete(), RESOURCE+"/JUNIT_NEW_UPDATED_FOLDER");
    }

	/**/
	/* SERVER RESOURCES */
	@Test 
    public void resource_Folder_MOVETO_POST_200() throws Exception
    {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		/* CE moving reports to JUNIT_NEW_FOLDER and back*/
		// create a destination folder
		sendAndAssert_CE(new HttpPut(), RESOURCE+"/JUNIT_NEW_FOLDER", LOCAL_NEW_RDS + SAMPLE_FOLDER_RD, HttpStatus.SC_CREATED);
		
		// to move to JUNIT_NEW_FOLDER
		qparams.add(new BasicNameValuePair(MOVE_TO, "/JUNIT_NEW_FOLDER"));
		httpRes = sendRequestCE(httpReqCE, RESOURCE+"/reports", qparams);
		Assert.assertTrue("moving reports to JUNIT NEW FOLDER", isValidResposnse());
		
		
		// sending the request to move to reports
		sendAndAssert_CE(new HttpPut(), RESOURCE+"/reports", LOCAL_NEW_RDS + SAMPLE_FOLDER_REPORTS_RD, HttpStatus.SC_CREATED);
		qparams = new ArrayList<NameValuePair>();
    	qparams.add(new BasicNameValuePair(MOVE_TO, "/reports"));
		httpRes = sendRequestCE(httpReqCE, RESOURCE+"/JUNIT_NEW_FOLDER/reports/samples", qparams);
		Assert.assertTrue("moving back to reports", isValidResposnse());
		
		sendAndAssert_CE(new HttpDelete(), RESOURCE+"/JUNIT_NEW_FOLDER");
		
		/* PRO moving public to JUNIT_NEW_FOLDER and back*/
		// create a destination folder
		sendAndAssert_PRO(new HttpPut(), RESOURCE+"/JUNIT_NEW_FOLDER", LOCAL_NEW_RDS + SAMPLE_FOLDER_RD, HttpStatus.SC_CREATED);
		
		qparams = new ArrayList<NameValuePair>();
    	qparams.add(new BasicNameValuePair(MOVE_TO, "/JUNIT_NEW_FOLDER"));
		httpRes = sendRequestPRO(httpReqPro, RESOURCE+"/public", qparams);
		Assert.assertTrue("moving reports to JUNIT NEW FOLDER", isValidResposnse());
		
		//moving it back to public
		sendAndAssert_PRO(new HttpPut(), RESOURCE+"/public", LOCAL_NEW_RDS + SAMPLE_FOLDER_PUBLIC_RD, HttpStatus.SC_CREATED);
		
		qparams = new ArrayList<NameValuePair>();
    	qparams.add(new BasicNameValuePair(MOVE_TO, "/public"));
		httpRes = sendRequestPRO(httpReqPro, RESOURCE+"/JUNIT_NEW_FOLDER/public/adhoc", qparams);
		Assert.assertTrue("moving back to reports", isValidResposnse());
		httpRes = sendRequestPRO(httpReqPro, RESOURCE+"/JUNIT_NEW_FOLDER/public/audit", qparams);
		Assert.assertTrue("moving back to reports", isValidResposnse());
		
		sendAndAssert_PRO(new HttpDelete(), RESOURCE+"/JUNIT_NEW_FOLDER");
    }
	
	/**/
	/* SERVER RESOURCES */
	@Test 
    public void resource_Folder_COPYTO_POST_200() throws Exception
    {
		//inits
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		
		/*CE*/
		/* copying reports to JUNIT_NEW_FOLDER */
		qparams.add(new BasicNameValuePair(COPY_TO, "/JUNIT_NEW_FOLDER"));
		httpRes = sendRequestCE(httpReqCE, RESOURCE+"/reports", qparams);
		Assert.assertTrue("copying reports to JUNIT NEW FOLDER", isValidResposnse());
		sendAndAssert_CE(new HttpDelete(), RESOURCE+"/JUNIT_NEW_FOLDER");
		
		/*PRO*/
		/* copying reports to JUNIT_NEW_FOLDER */
		qparams = new ArrayList<NameValuePair>();
    	qparams.add(new BasicNameValuePair(COPY_TO, "/JUNIT_NEW_FOLDER"));
		httpRes = sendRequestPRO(httpReqPro, RESOURCE+"/public", qparams);
		Assert.assertTrue("copy reports to JUNIT NEW FOLDER", isValidResposnse());
		sendAndAssert_PRO(new HttpDelete(), RESOURCE+"/JUNIT_NEW_FOLDER");
    }
	
	@Test 
	public void resource_IMG_POST_200() throws Exception{
		putSampleFolderAndFile();
		putSampleFileInSampleFolder(	new HttpPost(),
						LOCAL_NEW_RDS+SAMPLE_IMAGE_FILE_RD, 
						LOCAL_RESOURCE_DESCRIPTOR_PATH+SAMPLE_IMAGE_2_FILE_BIN, 
						"/JUNIT_IMAGE_FILE");
		deleteSampleFolder();
	}
	
	//changes the Logo picture in report
	@Test 
	public void Resource_LocalResource_IN_ReportUnit_POST_200() throws Exception 
	{
		putSampleReport();
		
		String fileRDPath = LOCAL_NEW_RDS+SAMPLE_IMAGE_LOGO_INSIDE_REPORT_FILE_RD; 
		String fileBinPath = LOCAL_RESOURCE_DESCRIPTOR_PATH+SAMPLE_IMAGE_2_FILE_BIN;
		
		tempHttpReq = new HttpPost();
    	MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		reqEntity.addPart(REQUEST_PARAMENTER_RD, new StringBody(RESTTestUtilsAndConsts.getResourceDescriptor(new File(fileRDPath))));
		FileBody bin = new FileBody(new File(fileBinPath));
		reqEntity.addPart("/JUNIT_NEW_FOLDER/JUNIT_REPORT_UNIT_SALES_BY_MONTH_files/Logo", bin );
		((HttpEntityEnclosingRequestBase)tempHttpReq).setEntity(reqEntity); 
		
		//executing the request
		httpRes = sendRequestCE(tempHttpReq, RESOURCE+"/JUNIT_NEW_FOLDER/JUNIT_REPORT_UNIT_SALES_BY_MONTH_files/Logo");
		Assert.assertTrue("basic response check did not pass", isValidResposnse(httpRes, HttpStatus.SC_CREATED)||httpRes.getStatusLine().getStatusCode()==HttpStatus.SC_OK);
		
		//executing the request
		httpRes = sendRequestPRO(tempHttpReq, RESOURCE+"/JUNIT_NEW_FOLDER/JUNIT_REPORT_UNIT_SALES_BY_MONTH_files/Logo");
		Assert.assertTrue("basic response check did not pass", isValidResposnse(HttpStatus.SC_CREATED)||httpRes.getStatusLine().getStatusCode()==HttpStatus.SC_OK);
		
		//validating the file size
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
    	qparams.add(new BasicNameValuePair("fileData", "true"));
    	httpRes = sendRequestCE(new HttpGet(), RESOURCE+"/JUNIT_NEW_FOLDER/JUNIT_REPORT_UNIT_SALES_BY_MONTH_files/Logo", qparams);
    	Assert.assertTrue("wrong file size: ", isValidResposnse()&& httpRes.getEntity().getContentLength() == -1);
    	
    	httpRes = sendRequestPRO(new HttpGet(), RESOURCE+"/JUNIT_NEW_FOLDER/JUNIT_REPORT_UNIT_SALES_BY_MONTH_files/Logo", qparams);
    	Assert.assertTrue("wrong file size: ", isValidResposnse()&& httpRes.getEntity().getContentLength() == -1);
    	
		deleteSampleFolder();
	}
	
	@Test  
	public void Resources_InputControl_POST_200() throws Exception{
		putSampleInputControlInSampleFolder();
		putSampleInputControlInSampleFolder(	new HttpPost(), LOCAL_UPDATE_RDS+SAMPLE_INPUT_CONTROL_WITH_LOCAL_RESOURCE_RD, "/JUNIT_INPUT_CONTROL_CONTAINS_LOCAL_RESOURCE", HttpStatus.SC_OK);
		    	
    	deleteSampleInputControl();
    	deleteSampleFolder();
	}
	
	@Test
	public void Resource_Query_POST_200() throws Exception{

		putSampleFolder();
		
		String fileRd = LOCAL_RESOURCE_DESCRIPTOR_PATH+"/query/CustomerCityQuery.xml";
		sendAndAssert_CE(new HttpPut(), SAMPLE_FOLDER_URL, fileRd, HttpStatus.SC_CREATED);
		sendAndAssert_PRO(new HttpPut(), SAMPLE_FOLDER_URL, fileRd, HttpStatus.SC_CREATED);
		
		fileRd = LOCAL_RESOURCE_DESCRIPTOR_PATH+"/query/CustomerCityQuery_update.xml";
		sendAndAssert_CE(new HttpPost(), SAMPLE_FOLDER_URL+"/CustomerCityQuery", fileRd, HttpStatus.SC_OK);
		sendAndAssert_PRO(new HttpPost(), SAMPLE_FOLDER_URL+"/CustomerCityQuery", fileRd, HttpStatus.SC_OK);
		
		deleteSampleFolder();
		
	}



//	http://robot:8080/jasperserver-pro/rest/resource/reports?moveTo=/public
//	http://robot:8080/jasperserver-pro/rest/resource/reports?copyTo=/public
//	/rest/resources/reports/samples?q=all


}
