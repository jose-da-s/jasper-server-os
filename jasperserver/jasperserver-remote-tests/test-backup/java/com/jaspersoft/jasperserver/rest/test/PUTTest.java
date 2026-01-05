package com.jaspersoft.jasperserver.rest.test;

import static com.jaspersoft.jasperserver.rest.test.RESTTestUtilsAndConsts.*;


import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jaspersoft.jasperserver.rest.RESTUtils;
import com.jaspersoft.jasperserver.rest.utils.JAXBList;

import com.jaspersoft.jasperserver.api.metadata.user.domain.ObjectPermission;
import com.jaspersoft.jasperserver.api.metadata.user.domain.Tenant;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.ObjectPermissionImpl;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.RoleImpl;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.TenantImpl;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.UserImpl;
import com.jaspersoft.jasperserver.ws.authority.WSRole;
import com.jaspersoft.jasperserver.ws.authority.WSUser;

public class PUTTest extends RESTTest 
{
	
	private final Log log = LogFactory.getLog(getClass());	
	@Before
	public void setUp() {
    	super.setUp();
    	httpReqCE = new HttpPut();
    	httpReqPro = new HttpPut();
     }
	

	
//	  //creation of new file resource the RD is before the binaries

//    
//    //creation of new file resource the binaries is before the RD
//	@Test 
//    public void Put200CreateNewFile_V2() throws Exception 
//    {
////		String fileRd = LOCAL_NEW_RDS+SAMPLE_IMAGE_FILE_RD;
////		String fileBins = LOCAL_RESOURCE_DESCRIPTOR_PATH+SAMPLE_IMAGE_FILE_BIN;
////    	
////    	//building the body
////		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
////		
////		//appending the binaries to the request body
////		FileBody bin = new FileBody(new File(fileBins));
////		reqEntity.addPart(REQUEST_SAMPLE_IMAGE_PATH, bin );
////		
////		//appending the file descriptor from a file 
////		reqEntity.addPart(REQUEST_PARAMENTER_RD, new StringBody(RESTTestUtilsAndConsts.getResourceDescriptor((new File(fileRd)))));
////		
////		((HttpEntityEnclosingRequestBase)httpReqCE).setEntity(reqEntity);
////		
////		//executing the request
////		httpRes = sendRequestCE(httpReqCE, SAMPLE_IMAGE_FILE_SERVER_PATH);
////		Assert.assertTrue("basic response check did not pass", isValidResposnse());
////		
////		deleteResource(SAMPLE_IMAGE_FILE_SERVER_PATH);
//    }
//	
//	@Test 
//    public void Put200NewDataType() throws Exception 
//    {
////		String rdFileName = LOCAL_NEW_RDS+SAMPLE_DATA_TYPE_RD;
////		
////    	//building the body
////		BasicHttpEntity reqEntity = new BasicHttpEntity();
////		
////		//appending the file descriptor from a file 
////		reqEntity.setContent(new FileInputStream(new File(rdFileName)));
////		
////		((HttpEntityEnclosingRequestBase)httpReqCE).setEntity(reqEntity);
////		
////		//executing the request
////		httpRes = sendRequestCE(httpReqCE, SAMPLE_DATA_TYPE_SERVER_PATH);
////		Assert.assertTrue("basic response check did not pass", isValidResposnse());
////		
////		deleteResource(SAMPLE_DATA_TYPE_SERVER_PATH);
//    }
//
//	
//	@Test 
//    public void Put200InputControl() throws Exception 
//    {
////		String rdFileName = LOCAL_NEW_RDS+SAMPLE_INPUT_CONTROL_RD;
////		
////    	//building the body
////		BasicHttpEntity reqEntity = new BasicHttpEntity();
////		
////		//appending the file descriptor from a file 
////		reqEntity.setContent(new FileInputStream(new File(rdFileName)));
////		
////		((HttpEntityEnclosingRequestBase)httpReqCE).setEntity(reqEntity);
////		
////		
////		//executing the request
////		httpRes = sendRequestCE(httpReqCE, SAMPLE_INPUT_CONTROL_SERVER_PATH);
////		Assert.assertTrue("basic response check did not pass", isValidResposnse());
////		
////		deleteResource(SAMPLE_INPUT_CONTROL_SERVER_PATH);
//    }
//	
//	@Test 
//    public void Put200InputControl_WithReferenceResource() throws Exception 
//    {
////		String rdFileName = LOCAL_NEW_RDS+SAMPLE_INPUT_CONTROL_WITH_REFERENCE_RD;
////		
////    	//building the body
////		BasicHttpEntity reqEntity = new BasicHttpEntity();
////		
////		//appending the file descriptor from a file 
////		reqEntity.setContent(new FileInputStream(new File(rdFileName)));
////		
////		((HttpEntityEnclosingRequestBase)httpReqCE).setEntity(reqEntity);
////		
////		
////		//executing the request
////		httpRes = sendRequestCE(httpReqCE, SAMPLE_INPUT_CONTROL_WITH_REFERENCES_ERVER_PATH);
////		Assert.assertTrue("basic response check did not pass", isValidResposnse());
////		
////		deleteResource(SAMPLE_INPUT_CONTROL_WITH_REFERENCES_ERVER_PATH);
//    }
//	
//	@Test 
//    public void Put200InputControl_WithLocalResource() throws Exception 
//    {
////		String rdFileName = LOCAL_NEW_RDS+SAMPLE_INPUT_CONTROL_WITH_LOCAL_RESOURCE_RD;
////		
////    	//building the body
////		BasicHttpEntity reqEntity = new BasicHttpEntity();
////		
////		//appending the file descriptor from a file 
////		reqEntity.setContent(new FileInputStream(new File(rdFileName)));
////		
////		((HttpEntityEnclosingRequestBase)httpReqCE).setEntity(reqEntity);
////		
////		
////		//executing the request
////		httpRes = sendRequestCE(httpReqCE, SAMPLE_INPUT_CONTROL_WITH_LOCAL_RESOURCE_SERVER_PATH);
////		Assert.assertTrue("basic response check did not pass", isValidResposnse());
////		
////		deleteResource(SAMPLE_INPUT_CONTROL_WITH_LOCAL_RESOURCE_SERVER_PATH);
//    }
//	
//	@Test 
//    public void Put200JDBC() throws Exception 
//    {
////		String rdFileName = LOCAL_NEW_RDS+SAMPLE_JDBC_FILE_RD;
////		
////    	//building the body
////		BasicHttpEntity reqEntity = new BasicHttpEntity();
////		
////		//appending the file descriptor from a file 
////		reqEntity.setContent(new FileInputStream(new File(rdFileName)));
////		
////		((HttpEntityEnclosingRequestBase)httpReqCE).setEntity(reqEntity);
////		
////		
////		//executing the request
////		httpRes = sendRequestCE(httpReqCE, SAMPLE_JDBC_FILE_SERVER_PATH);
////		Assert.assertTrue("basic response check did not pass", isValidResposnse());
////		
////		deleteResource(SAMPLE_JDBC_FILE_SERVER_PATH);
//    }
//	

//	
//	@Test 
//    public void Put200ListOfValues() throws Exception 
//    {
////		String rdFileName = LOCAL_NEW_RDS+SAMPLE_LIST_OF_VALUES_RD;
////		
////    	//building the body
////		BasicHttpEntity reqEntity = new BasicHttpEntity();
////		
////		//appending the file descriptor from a file 
////		reqEntity.setContent(new FileInputStream(new File(rdFileName)));
////		
////		((HttpEntityEnclosingRequestBase)httpReqCE).setEntity(reqEntity);
////		
////		
////		//executing the request
////		httpRes = sendRequestCE(httpReqCE, SAMPLE_LIST_OF_VALUES_SERVER_PATH);
////		Assert.assertTrue("basic response check did not pass", isValidResposnse());
////		
////		deleteResource(SAMPLE_LIST_OF_VALUES_SERVER_PATH);
//    }
//	
////	@Test
////	public void put200Domain() throws Exception{
//		//inits
////		String boundleFileURL = LOCAL_RESOURCES_LOCATION+"/sales_ro.properties";
////		String schemaURL = LOCAL_RESOURCES_LOCATION+"/sales_ro.properties";
////		
////		
////		String fileRd = LOCAL_NEW_RDS+SAMPLE_COMPOLEX_REPORT_UNIT_RD;
////		
////		FileBody bin;
////		
////    	//building the body
////		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
////		
////		//appending the file descriptor from a file 
////		reqEntity.addPart(REQUEST_PARAMENTER_RD, new StringBody(RESTTestUtils.getResourceDescriptor(new File(fileRd))));
////		
////		//appending the binaries to the request body
////		bin = new FileBody(new File(SalesByMonthReportLocalPath));
////		reqEntity.addPart(SalesByMonthReportURI, bin );
////		
////		//appending the binaries to the request body
////		bin = new FileBody(new File(ScriptletLocalPath));
////		reqEntity.addPart(ScriptletURI, bin );
////		
////		//appending the binaries to the request body
////		bin = new FileBody(new File(LogoLocalPath));
////		reqEntity.addPart(LogoURI, bin );
////		
////		//appending the binaries to the request body
////		bin = new FileBody(new File(SalesByMonthDetailLocalPath));
////		reqEntity.addPart(SalesByMonthDetailURI, bin );
////		
////		//appending the binaries to the request body
////		bin = new FileBody(new File(sales_propertiesLocalPath));
////		reqEntity.addPart(sales_propertiesURI, bin );
////		
////		//appending the binaries to the request body
////		bin = new FileBody(new File(sales_ro_propertiesLocalPath));
////		reqEntity.addPart(sales_ro_propertiesURI, bin );
////		
////		((HttpEntityEnclosingRequestBase)httpReqCE).setEntity(reqEntity);
////		
////		//executing the request
////		httpRes = executeCall(httpReqCE, SAMPLE_COMPLEX_REPORT_SERVER_PATH);
////		Assert.assertTrue("basic response check did not pass", isValidResposnse());
////		
////		deleteResource(SAMPLE_COMPLEX_REPORT_SERVER_PATH);
//		
////	}
//	
	
	@Test
    public void testVerify_cant_create_domain_with_demo_user() throws Exception {
//		loginToPROServer(DEMO_USER_NAME, DEMO_PASSWORD, ORG);
//        String testFolderPath = "/temp/JUNIT_NEW_FOLDER";
//        createFolder("JUNIT_NEW_FOLDER", "/temp");
//        String localPath = new File(".").getCanonicalPath();
//
//        String schemaBinFilePath = localPath + "/resource/rest/domain/schemas/Simple_Domain_schema.xml";
//
//        String fileRd = localPath + "/resource/rest/descriptors/domain/domainDataSource_Path.JUNIT_NEW_FOLDER.Simple_Domain.xml";
//        FileBody bin;
//
//        //building the body
//        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
//
//        //appending the file descriptor from a file
//        reqEntity.addPart(REQUEST_PARAMENTER_RD, new StringBody(RESTUtils.getResourceDescriptor(new File(fileRd))));
//
//        //appending the binaries to the request body
//        bin = new FileBody(new File(schemaBinFilePath));
//        reqEntity.addPart("/temp/JUNIT_NEW_FOLDER/Simple_Domain_files/Simple_Domain_schema", bin);
//
//        createResourceWithAttachment(reqEntity, HttpStatus.SC_FORBIDDEN);
    }
	
	@Test 
    public void Job_Put_200() throws Exception{
		putSampleJob_CE();
		int ceJobIndex = getJobIndexFromJobSummary(getSampleJobSummary_CE());
		sendAndAssert_CE(new HttpGet(), SERVICE_JOB+"/"+ceJobIndex);
		deleteJob(ceJobIndex, false);
		

		putSampleJob_PRO();
		int proJobIndex = getJobIndexFromJobSummary(getSampleJobSummary_PRO());
		sendAndAssert_PRO(new HttpGet(), SERVICE_JOB+"/"+proJobIndex);
		deleteJob(proJobIndex, true); 
    }
	
	@Test 
    public void Job_MultiOutput_Put_200() throws Exception{
		putSampleResource_CE(LOCAL_RESOURCE_DESCRIPTOR_PATH + SERVICE_JOB + SAMPLE_JOB_MULTI_OUTPUT_RD, SERVICE_JOB);
		
		int ceJobIndex = getJobIndexFromJobSummary(getSampleJobSummary_CE());
		sendAndAssert_CE(new HttpGet(), SERVICE_JOB+"/"+ceJobIndex);
		deleteJob(ceJobIndex, false);
		

		putSampleResource_PRO(LOCAL_RESOURCE_DESCRIPTOR_PATH + SERVICE_JOB + SAMPLE_JOB_MULTI_OUTPUT_RD, SERVICE_JOB);
		int proJobIndex = getJobIndexFromJobSummary(getSampleJobSummary_PRO());
		sendAndAssert_PRO(new HttpGet(), SERVICE_JOB+"/"+proJobIndex);
		deleteJob(proJobIndex, true); 
    }
	
	@Test 
    public void User_Put_200() throws Exception{
    	sendAndAssert_CE(httpReqCE, SERVICE_USER, LOCAL_NEW_RDS + SAMPLE_USER_CE_RD, HttpStatus.SC_CREATED);
    	sendAndAssert_PRO(httpReqPro, SERVICE_USER, LOCAL_NEW_RDS + SAMPLE_USER_PRO_RD, HttpStatus.SC_CREATED);
    	
    	sendAndAssert_CE(new HttpDelete(), "/user/JUNIT_USER");
    	sendAndAssert_PRO(new HttpDelete(), "/user/JUNIT_USER"+PIPE+ORG);
	}
	
	@Test 
    public void User_Put_Post_UPDATE_WITH_EMPTY_TENANT_400() throws Exception{
		sendAndAssert_PRO(httpReqPro, SERVICE_USER, LOCAL_NEW_RDS + SAMPLE_USER_PRO_RD, HttpStatus.SC_CREATED);
    	sendAndAssert_PRO(new HttpPost(), SERVICE_USER+"/JUNIT_USER", LOCAL_NEW_RDS + "/JUNIT_USER_PRO_NO_TENANT_ID_NEW_PASSWORD.xml", HttpStatus.SC_OK);
    	
    	sendAndAssert_PRO(new HttpDelete(), "/user/JUNIT_USER");
	}
	
	@Test 
    public void User_Put_200_LOGIN_AS_NEW_USER() throws Exception{
		sendAndAssert_CE(new HttpPut(), SERVICE_USER, LOCAL_NEW_RDS + SAMPLE_USER_CE_RD, HttpStatus.SC_CREATED);
    	sendAndAssert_PRO(new HttpPut(), SERVICE_USER, LOCAL_NEW_RDS + SAMPLE_USER_PRO_RD, HttpStatus.SC_CREATED);
    	
    	loginToServer("JUNIT_USER", "JUNIT_USER", "JUNIT_USER", "JUNIT_USER");
    	
    	sendAndAssert_CE(new HttpDelete(), "/user/JUNIT_USER", HttpStatus.SC_BAD_REQUEST);
    	sendAndAssert_PRO(new HttpDelete(), "/user/JUNIT_USER", HttpStatus.SC_BAD_REQUEST);
    	
    	loginToServer();
    	sendAndAssert_CE(new HttpDelete(), "/user/JUNIT_USER");
    	sendAndAssert_PRO(new HttpDelete(), "/user/JUNIT_USER");
	}
	
	// tenant id is not specified in a multitenant environment.
	@Test 
    public void User_Put_400_NoTenantIdInAMultiTenantEnviorment() throws Exception{
		sendAndAssert_PRO(httpReqPro, SERVICE_ORGANIZATION, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/tenants" + "/1st_level_tenant.xml", HttpStatus.SC_CREATED);
    	sendAndAssert_PRO(httpReqPro, SERVICE_USER, LOCAL_NEW_RDS + "/JUNIT_USER_PRO_NO_TENANT_ID.xml", HttpStatus.SC_BAD_REQUEST);
    	
    	sendAndAssert_PRO(new HttpDelete(), SERVICE_ORGANIZATION+"/2");
    	
    	
	}
	
	@Test 
    public void Role_Put_200() throws Exception{
		/* tested as part of the GET role test*/
    }
	
	@Test 
    public void Role_Put_400_UNSUPPORTED_CHAR() throws Exception{
		sendAndAssert_CE(new HttpPut(), SERVICE_ROLE, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/role" + "/role_junit_ce_ILLEGAL_CHARS.xml", HttpStatus.SC_BAD_REQUEST);
    }
	
	@Test 
    public void Role_Put_400_PUT_ROOT_ROLE() throws Exception{
		loginToPROServer(SUPERUSER_USER_NAME, SUPERUSER_PASSWORD);
		sendAndAssert_PRO(new HttpPut(), SERVICE_ROLE, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/role" + "/role_junit_pro_NO_TENANT.xml", HttpStatus.SC_CREATED);
		sendAndAssert_PRO(new HttpDelete(), SERVICE_ROLE+"/ROLE_JUNIT");
    }
	
	@Test 
    public void Role_Put_400_PUT_ROOT_ROLE_ORGANIZATIONS() throws Exception{
		loginToPROServer(SUPERUSER_USER_NAME, SUPERUSER_PASSWORD);
		sendAndAssert_PRO(new HttpPut(), SERVICE_ROLE, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/role" + "/role_junit_pro_ROOT_ROLE.xml", HttpStatus.SC_CREATED);
		sendAndAssert_PRO(new HttpDelete(), SERVICE_ROLE+"/ROLE_JUNIT");
    }
	
	
	
	@Test 
    public void Permission_Put_200() throws Exception{
		putSampleFolder();
		
		sendAndAssert_CE(new HttpPut(), SERVICE_PERMISSION+"/JUNIT_NEW_FOLDER", LOCAL_RESOURCE_DESCRIPTOR_PATH + SAMPLE_PERMISSION_CE_RD, HttpStatus.SC_OK);
		sendAndAssert_PRO(new HttpPut(), SERVICE_PERMISSION+"/JUNIT_NEW_FOLDER", LOCAL_RESOURCE_DESCRIPTOR_PATH + SAMPLE_PERMISSION_PRO_RD, HttpStatus.SC_OK);
		
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
    	qparams.add(new BasicNameValuePair("roles", "ROLE_ANONYMOUS, ROLE_ETL_ADMIN, ROLE_USER"));
    	qparams.add(new BasicNameValuePair("users", "joeuser, etladmin"));
    	
    	HttpResponse res;
    	res =  sendRequestCE(new HttpDelete(), SERVICE_PERMISSION+"/JUNIT_NEW_FOLDER", qparams);
    	Assert.assertTrue("wrong return value: ", isValidResposnse(res, HttpStatus.SC_OK));
    	res = sendRequestPRO(new HttpDelete(), SERVICE_PERMISSION+"/JUNIT_NEW_FOLDER", qparams);
    	Assert.assertTrue("wrong return value: ", isValidResposnse(res, HttpStatus.SC_OK));
    	
		deleteSampleFolder();
    }
	
	@Test 
    public void Permission_Put_404_MALFORMED_URI() throws Exception{
		putSampleFolder();
		
		sendAndAssert_CE(new HttpPut(), SERVICE_PERMISSION+"/JUNIT_NEW_FOLDER", LOCAL_RESOURCE_DESCRIPTOR_PATH + "/permission/permission_ce_uri.reports_MALFORMED_URI_1.xml", HttpStatus.SC_NOT_FOUND);
		sendAndAssert_CE(new HttpPut(), SERVICE_PERMISSION+"/JUNIT_NEW_FOLDER", LOCAL_RESOURCE_DESCRIPTOR_PATH + "/permission/permission_ce_uri.reports_MALFORMED_URI_2.xml", HttpStatus.SC_NOT_FOUND);
		sendAndAssert_CE(new HttpPut(), SERVICE_PERMISSION+"/JUNIT_NEW_FOLDER", LOCAL_RESOURCE_DESCRIPTOR_PATH + "/permission/permission_ce_uri.reports_MALFORMED_URI_3.xml", HttpStatus.SC_NOT_FOUND);
		
		deleteSampleFolder();
    }
	
	@Test 
    public void Permission_Put_200_CHANGE_YOUR_OWN_PERMISSIONS() throws Exception{
		putSampleFolder();
		
		sendAndAssert_CE(new HttpPut(), SERVICE_PERMISSION+"/JUNIT_NEW_FOLDER", LOCAL_RESOURCE_DESCRIPTOR_PATH + "/permission/permission_ce_uri.reports_FOR_SUPERUSER_ANDMINISTRATOR.xml", HttpStatus.SC_FORBIDDEN);
		sendAndAssert_PRO(new HttpPut(), SERVICE_PERMISSION+"/JUNIT_NEW_FOLDER", LOCAL_RESOURCE_DESCRIPTOR_PATH + "/permission/permission_pro_uri.reports_FOR_SUPERUSER_ANDMINISTRATOR.xml", HttpStatus.SC_FORBIDDEN);
		
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
    	qparams.add(new BasicNameValuePair("roles", "ROLE_ADMINISTRATOR, ROLE_SUPERUSER"));
    	qparams.add(new BasicNameValuePair("users", "jasperadmin"));
    	
    	HttpResponse res;
    	res =  sendRequestCE(new HttpDelete(), SERVICE_PERMISSION+"/JUNIT_NEW_FOLDER", qparams);
    	Assert.assertTrue("wrong return value: ", isValidResposnse(res, HttpStatus.SC_FORBIDDEN));
    	res = sendRequestPRO(new HttpDelete(), SERVICE_PERMISSION+"/JUNIT_NEW_FOLDER", qparams);
    	Assert.assertTrue("wrong return value: ", isValidResposnse(res, HttpStatus.SC_FORBIDDEN));
    	
		deleteSampleFolder();
    }
	
	@Test 
    public void Attribute_Put_201() throws Exception{
		sendAndAssert_CE(httpReqCE, SERVICE_ATTRIBUTE+"/joeuser", LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/attributes" + "/attributes.xml", HttpStatus.SC_CREATED);
    	sendAndAssert_PRO(httpReqPro, SERVICE_ATTRIBUTE+"/joeuser"+ PIPE +"organization_1", LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/attributes" + "/attributes.xml", HttpStatus.SC_CREATED);
    }

    @Test
    public void Attribute_Put_201_FOR_NEW_USER() throws Exception{

        sendAndAssert_CE(httpReqCE, SERVICE_USER, LOCAL_NEW_RDS + SAMPLE_USER_CE_RD, HttpStatus.SC_CREATED);
    	sendAndAssert_CE(httpReqCE, SERVICE_ATTRIBUTE+"/JUNIT_USER", LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/attributes" + "/attributes.xml", HttpStatus.SC_CREATED);
        sendAndAssert_CE(new HttpDelete(), "/user/JUNIT_USER");


        sendAndAssert_PRO(httpReqPro, SERVICE_USER, LOCAL_NEW_RDS + SAMPLE_USER_PRO_RD, HttpStatus.SC_CREATED);
    	sendAndAssert_PRO(httpReqPro, SERVICE_ATTRIBUTE+"/JUNIT_USER"+PIPE+"organization_1", LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/attributes" + "/attributes.xml", HttpStatus.SC_CREATED);
        sendAndAssert_PRO(new HttpDelete(), "/user/JUNIT_USER"+PIPE+"organization_1");

    }
	
	@Test 
    public void Attribute_Put_404_ATTRIBUTES_TO_A_NONEXISTING_USER() throws Exception{
		sendAndAssert_CE(httpReqCE, SERVICE_ATTRIBUTE+"/none_existing_user", LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/attributes" + "/attributes.xml", HttpStatus.SC_BAD_REQUEST);
    	sendAndAssert_PRO(httpReqPro, SERVICE_ATTRIBUTE+"/none_existing_user"+ PIPE +"organization_1", LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/attributes" + "/attributes.xml", HttpStatus.SC_BAD_REQUEST);
    }
	
	
	
	
	
	/* PRO OBJECTS*/
	@Test 
    public void Organization_Put_201() throws Exception{
		sendAndAssert_PRO(httpReqPro, SERVICE_ORGANIZATION, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/tenants" + "/1st_level_tenant.xml", HttpStatus.SC_CREATED);
		sendAndAssert_PRO(httpReqPro, SERVICE_ORGANIZATION, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/tenants" + "/2nd_level_tenant.xml", HttpStatus.SC_CREATED);
		
		sendAndAssert_PRO(new HttpPost(), SERVICE_ORGANIZATION+"/2", LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/tenants" + "/1st_level_tenant_update.xml", HttpStatus.SC_OK);
		
		sendAndAssert_PRO(new HttpDelete(), SERVICE_ORGANIZATION+"/2_chiled");
		sendAndAssert_PRO(new HttpDelete(), SERVICE_ORGANIZATION+"/2");
    }
	
	@Test 
    public void Organization_Put_400_UPDATE_WITH_WRONG_ORG_NAME() throws Exception{
		sendAndAssert_PRO(httpReqPro, SERVICE_ORGANIZATION, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/tenants" + "/1st_level_tenant.xml", HttpStatus.SC_CREATED);
		sendAndAssert_PRO(new HttpPost(), SERVICE_ORGANIZATION+"/22", LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/tenants" + "/1st_level_tenant_update.xml", HttpStatus.SC_BAD_REQUEST);
		sendAndAssert_PRO(new HttpDelete(), SERVICE_ORGANIZATION+"/2");
    }
	
	@Test 
    public void Organization_Put_400_ORGANIZATION_WITH_SPECIAL_TAGS() throws Exception{
		sendAndAssert_PRO(httpReqPro, SERVICE_ORGANIZATION, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/tenants" + "/1st_level_tenant_ALIAS.xml", HttpStatus.SC_BAD_REQUEST);
		sendAndAssert_PRO(httpReqPro, SERVICE_ORGANIZATION, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/tenants" + "/1st_level_tenant_TENANT_NAME.xml", HttpStatus.SC_BAD_REQUEST);
		sendAndAssert_PRO(httpReqPro, SERVICE_ORGANIZATION, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/tenants" + "/1st_level_tenant_ID.xml", HttpStatus.SC_BAD_REQUEST);
    }
	
	@Test 
    public void Organization_Put_400_DELETE_OWN_ORGANIZATION() throws Exception{
		sendAndAssert_PRO(httpReqPro, SERVICE_ORGANIZATION, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/tenants" + "/1st_level_tenant.xml", HttpStatus.SC_CREATED);
		loginToPROServer(ADMIN_USER_NAME_PRO+PIPE+"2", ADMIN_PASS_PRO);
		sendAndAssert_PRO(new HttpDelete(), SERVICE_ORGANIZATION+"/2", HttpStatus.SC_BAD_REQUEST);
		loginToPROServer(SUPERUSER_USER_NAME, SUPERUSER_PASSWORD);
		sendAndAssert_PRO(new HttpDelete(), SERVICE_ORGANIZATION+"/2");
    }
	
	@Test 
    public void Organization_Put_403_PUTTING_AN_ALREADY_EXIST_ORGANIZATION() throws Exception{
		sendAndAssert_PRO(httpReqPro, SERVICE_ORGANIZATION, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/tenants" + "/1st_level_tenant.xml", HttpStatus.SC_CREATED);
		sendAndAssert_PRO(httpReqPro, SERVICE_ORGANIZATION, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/tenants" + "/1st_level_tenant.xml", HttpStatus.SC_FORBIDDEN);
				
		sendAndAssert_PRO(new HttpDelete(), SERVICE_ORGANIZATION+"/2");
    }
	
	@Test 
    public void Organization_Put_404_MISSING_PARAMETERS_IN_DESCRIPTOR() throws Exception{
		sendAndAssert_PRO(httpReqPro, SERVICE_ORGANIZATION, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/tenants" + "/1st_level_tenant_MISSING_PARAMETERS_1.xml", HttpStatus.SC_BAD_REQUEST);
		sendAndAssert_PRO(httpReqPro, SERVICE_ORGANIZATION, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/tenants" + "/1st_level_tenant_MISSING_PARAMETERS_2.xml", HttpStatus.SC_BAD_REQUEST);
		sendAndAssert_PRO(httpReqPro, SERVICE_ORGANIZATION, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/tenants" + "/1st_level_tenant_MISSING_PARAMETERS_3.xml", HttpStatus.SC_BAD_REQUEST);
    }
	
	
	
	@Test 
    public void Organization_Put_403_DELETING_SIBLING_ORGANIZATION() throws Exception{
		sendAndAssert_PRO(httpReqPro, SERVICE_ORGANIZATION, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/tenants" + "/org1.xml", HttpStatus.SC_CREATED);
		sendAndAssert_PRO(httpReqPro, SERVICE_ORGANIZATION, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/tenants" + "/org2.xml", HttpStatus.SC_CREATED);
		
		loginToPROServer(ADMIN_USER_NAME_PRO, ADMIN_PASS_PRO, "org1");
		sendAndAssert_PRO(new HttpDelete(), SERVICE_ORGANIZATION +"/org2", HttpStatus.SC_BAD_REQUEST);
		
		loginToServer();
		sendAndAssert_PRO(new HttpDelete(), SERVICE_ORGANIZATION +"/org1", HttpStatus.SC_OK);
		sendAndAssert_PRO(new HttpDelete(), SERVICE_ORGANIZATION +"/org2", HttpStatus.SC_OK);
	}
	
	@Test 
    public void Role_PUT_POST_200() throws Exception{
		sendAndAssert_CE(new HttpPut(), SERVICE_ROLE, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/role" + "/role_junit_ce_EMPTY_NAME.xml", HttpStatus.SC_BAD_REQUEST);
		sendAndAssert_CE(new HttpPut(), SERVICE_ROLE, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/role" + "/role_junit_ce_ILLEGAL_CHARS.xml", HttpStatus.SC_BAD_REQUEST);
        sendAndAssert_CE(new HttpPut(), SERVICE_ROLE, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/role" + "/role_junit_ce.xml", HttpStatus.SC_CREATED);
        sendAndAssert_CE(new HttpPost(), SERVICE_ROLE+"/ROLE_JUNIT", LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/role" + "/role_junit_ce_EMPTY_NAME.xml", HttpStatus.SC_BAD_REQUEST);
        sendAndAssert_CE(new HttpPut(), SERVICE_ROLE, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/role" + "/role_junit_ce.xml", HttpStatus.SC_BAD_REQUEST);
		sendAndAssert_CE(new HttpPost(), SERVICE_ROLE+"/ROLE_JUNIT", LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/role" + "/role_junit_ce_ILLEGAL_CHARS.xml", HttpStatus.SC_BAD_REQUEST);
        sendAndAssert_CE(new HttpDelete(), SERVICE_ROLE+"/ROLE_JUNIT");

        sendAndAssert_PRO(new HttpPut(), SERVICE_ROLE, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/role" + "/role_junit_pro_ROOT_ROLE.xml", HttpStatus.SC_CREATED);
        sendAndAssert_PRO(new HttpPut(), SERVICE_ROLE, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/role" + "/role_junit_pro_ILLEGAL_CHARS.xml", HttpStatus.SC_BAD_REQUEST);
        sendAndAssert_PRO(new HttpPut(), SERVICE_ROLE, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/role" + "/role_junit_pro_NONE_EXISTING_TENANT_NAME.xml", HttpStatus.SC_BAD_REQUEST);
		sendAndAssert_PRO(new HttpDelete(), SERVICE_ROLE+"/ROLE_JUNIT");

		sendAndAssert_PRO(new HttpPut(), SERVICE_ROLE, LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/role" + "/role_junit_pro.xml", HttpStatus.SC_CREATED);
		sendAndAssert_PRO(new HttpPost(), SERVICE_ROLE+"/ROLE_JUNIT", LOCAL_RESOURCE_DESCRIPTOR_PATH+ "/role" + "/role_junit_pro_NO_TENANT.xml", HttpStatus.SC_OK);


		sendAndAssert_PRO(new HttpDelete(), SERVICE_ROLE+"/ROLE_JUNIT"+PIPE+ORG);
    }
	
	/* SERVER RESOURCES */
	@Test 
    public void resource_Folder_PUT_201() throws Exception 
    {
		sendAndAssert_CE(httpReqCE, RESOURCE+"/JUNIT_NEW_FOLDER", LOCAL_NEW_RDS + SAMPLE_FOLDER_RD, HttpStatus.SC_CREATED);
		sendAndAssert_CE(new HttpDelete(), RESOURCE+"/JUNIT_NEW_FOLDER");
		
		sendAndAssert_PRO(httpReqPro, RESOURCE+"/JUNIT_NEW_FOLDER", LOCAL_NEW_RDS + SAMPLE_FOLDER_RD, HttpStatus.SC_CREATED);
		sendAndAssert_PRO(new HttpDelete(), RESOURCE+"/JUNIT_NEW_FOLDER");
    }
	
	@Test 
    public void Role_PUT_400() throws Exception{
		putSampleRole_CE();
		sendAndAssert_CE(httpReqCE, SERVICE_ROLE+"/ROLE_JUNIT", HttpStatus.SC_BAD_REQUEST);
		sendAndAssert_CE(new HttpDelete(), SERVICE_ROLE+"/ROLE_JUNIT");

		putSampleRole_PRO();
		sendAndAssert_PRO(httpReqPro, SERVICE_ROLE+"/ROLE_JUNIT"+PIPE+ORG, HttpStatus.SC_BAD_REQUEST);
    	sendAndAssert_PRO(new HttpDelete(), SERVICE_ROLE+"/ROLE_JUNIT"+PIPE+ORG);
    }
	
	@Test 
    public void Role_GET_SUPERUSER() throws Exception{
		loginToPROServer(SUPERUSER_USER_NAME, SUPERUSER_USER_NAME);
		sendAndAssert_PRO(new HttpGet(), SERVICE_ROLE+"/ROLE_USER");
	}
	
	@Test 
    public void Role_PUT_POST_400() throws Exception{
		putSampleRole_PRO();
		sendAndAssert_PRO(new HttpPost(), SERVICE_ROLE+"/ROLE_JUNIT", LOCAL_RESOURCE_DESCRIPTOR_PATH+"/role/role_junit_pro_UPDATED.xml");
		sendAndAssert_PRO(new HttpDelete(), SERVICE_ROLE+"/ROLE_JUNIT_UPDATED"+PIPE+ORG);
	}
	
	@Test 
	public void resource_IMG_PUT_200() throws Exception 
	{
		/* CE */
		sendAndAssert_CE(httpReqCE, RESOURCE+"/JUNIT_NEW_FOLDER", LOCAL_NEW_RDS + SAMPLE_FOLDER_RD, HttpStatus.SC_CREATED);
		
		String fileRd = LOCAL_NEW_RDS+SAMPLE_IMAGE_FILE_RD;
		String fileBins = LOCAL_RESOURCE_DESCRIPTOR_PATH+SAMPLE_IMAGE_FILE_BIN;
		
		
		//building the body
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		//appending the file descriptor from a file 
		reqEntity.addPart(REQUEST_PARAMENTER_RD, new StringBody(RESTTestUtilsAndConsts.getResourceDescriptor(new File(fileRd))));
		
		//appending the binaries to the request body
		FileBody bin = new FileBody(new File(fileBins));
		reqEntity.addPart("/JUNIT_NEW_FOLDER/JUNIT_IMAGE_FILE", bin );//reqEntity.addPart(REQUEST_SAMPLE_IMAGE_PATH, bin );
		
		((HttpEntityEnclosingRequestBase)httpReqCE).setEntity(reqEntity);
		
		//executing the request
		httpRes = sendRequestCE(httpReqCE, "/resource/JUNIT_NEW_FOLDER");
		Assert.assertTrue("basic response check did not pass", isValidResposnse(201));
		
		sendAndAssert_CE(new HttpDelete(), RESOURCE+"/JUNIT_NEW_FOLDER/JUNIT_IMAGE_FILE");
		sendAndAssert_CE(new HttpDelete(), RESOURCE+"/JUNIT_NEW_FOLDER");
		
		/* PRO */
		sendAndAssert_PRO(httpReqPro, RESOURCE+"/JUNIT_NEW_FOLDER", LOCAL_NEW_RDS + SAMPLE_FOLDER_RD, HttpStatus.SC_CREATED);
		((HttpEntityEnclosingRequestBase)httpReqPro).setEntity(reqEntity);
		
		//executing the request
		httpRes = sendRequestPRO(httpReqPro, "/resource/JUNIT_NEW_FOLDER");
		Assert.assertTrue("basic response check did not pass", isValidResposnse(201));
		
		sendAndAssert_PRO(new HttpDelete(), RESOURCE+"/JUNIT_NEW_FOLDER/JUNIT_IMAGE_FILE");
		sendAndAssert_PRO(new HttpDelete(), RESOURCE+"/JUNIT_NEW_FOLDER");
	}
	
	@Test
	public void Resource_ReportUnit_PUT_200() throws Exception
	{
		putSampleReport();
		deleteSampleReport();
		deleteSampleFolder();
	}
	
	//adds a logo_2 to the sample resource
	@Test 
	public void Resource_LocalResource_IMG_of_ReportUnit_PUT_200() throws Exception 
	{
		super.Resource_LocalResource_of_ReportUnit_PUT_200(	new HttpPut(), 
														LOCAL_NEW_RDS+SAMPLE_IMAGE_LOGO2_INSIDE_REPORT_FILE_RD, 
														LOCAL_RESOURCE_DESCRIPTOR_PATH+SAMPLE_IMAGE_FILE_BIN, 
														"/JUNIT_NEW_FOLDER/JUNIT_REPORT_UNIT_SALES_BY_MONTH_files/Logo_2", 
														"/JUNIT_NEW_FOLDER/JUNIT_REPORT_UNIT_SALES_BY_MONTH_files");
		
		sendAndAssert_CE(new HttpGet(), RESOURCE+"/JUNIT_NEW_FOLDER/JUNIT_REPORT_UNIT_SALES_BY_MONTH_files/Logo_2");
		sendAndAssert_PRO(new HttpGet(), RESOURCE+"/JUNIT_NEW_FOLDER/JUNIT_REPORT_UNIT_SALES_BY_MONTH_files/Logo_2");
		
		deleteSampleFolder();
	}
	
	@Test 
	public void Resource_LocalResource_PROP_of_ReportUnit_PUT_200() throws Exception 
	{
		super.Resource_LocalResource_of_ReportUnit_PUT_200(	new HttpPut(), 
														LOCAL_NEW_RDS+SAMPLE_PROPERITES_RD, 
														LOCAL_RESOURCE_DESCRIPTOR_PATH+BIN_SAMPLE_PROPERTIES, 
														"/JUNIT_NEW_FOLDER/JUNIT_REPORT_UNIT_SALES_BY_MONTH_files/JUNIT_PROPERTIES", 
														"/JUNIT_NEW_FOLDER/JUNIT_REPORT_UNIT_SALES_BY_MONTH");
		
		sendAndAssert_CE(new HttpGet(), RESOURCE+"/JUNIT_NEW_FOLDER/JUNIT_REPORT_UNIT_SALES_BY_MONTH_files/JUNIT_PROPERTIES");
		sendAndAssert_PRO(new HttpGet(), RESOURCE+"/JUNIT_NEW_FOLDER/JUNIT_REPORT_UNIT_SALES_BY_MONTH_files/JUNIT_PROPERTIES");
		
		sendAndAssert_CE(new HttpDelete(), RESOURCE+"/JUNIT_NEW_FOLDER/JUNIT_REPORT_UNIT_SALES_BY_MONTH_files/JUNIT_PROPERTIES");
		sendAndAssert_PRO(new HttpDelete(), RESOURCE+"/JUNIT_NEW_FOLDER/JUNIT_REPORT_UNIT_SALES_BY_MONTH_files/JUNIT_PROPERTIES");
		
		deleteSampleFolder();
	}
	
	@Test  
	public void Resource_DomainDataSource_PUT_201() throws Exception{

		putSampleFolder();
		
		String domainURI="/JUNIT_NEW_FOLDER/Simple_Domain";
		String schemaURI="/JUNIT_NEW_FOLDER/Simple_Domain_files/Simple_Domain_schema";
		String JNDIURI="/analysis/datasources/SugarCRMDataSourceJNDI";
		
		String schemaBinFilePath = LOCAL_RESOURCE_DESCRIPTOR_PATH+"/domain/Simple_Domain_schema.xml";
		 
		
		String fileRd = LOCAL_RESOURCE_DESCRIPTOR_PATH+"/domain/domainDataSource_Path.JUNIT_NEW_FOLDER.Simple_Domain.xml";
		FileBody bin;
		
    	//building the body
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		
		//appending the file descriptor from a file 
		reqEntity.addPart(REQUEST_PARAMENTER_RD, new StringBody(RESTTestUtilsAndConsts.getResourceDescriptor(new File(fileRd))));
		
		//appending the binaries to the request body
		bin = new FileBody(new File(schemaBinFilePath));
		reqEntity.addPart(schemaURI, bin );
		
		((HttpEntityEnclosingRequestBase)httpReqPro).setEntity(reqEntity);
		
		//executing the request
		httpRes = sendRequestPRO(httpReqPro, RESOURCE+"/JUNIT_NEW_FOLDER");
		Assert.assertTrue("basic response check did not pass", isValidResposnse(201));
		
		deleteSampleFolder();
		
	}
	
	@Test  
	public void Resource_DomainDataSource_PUT_403() throws Exception{

		putSampleFolder();
		
		loginToPROServer(JOEUSER_USER_NAME, JOEUSER_USER_NAME);
		
		String domainURI="/JUNIT_NEW_FOLDER/Simple_Domain";
		String schemaURI="/JUNIT_NEW_FOLDER/Simple_Domain_files/Simple_Domain_schema";
		String JNDIURI="/analysis/datasources/SugarCRMDataSourceJNDI";
		
		String schemaBinFilePath = LOCAL_RESOURCE_DESCRIPTOR_PATH+"/domain/Simple_Domain_schema.xml";
		 
		
		String fileRd = LOCAL_RESOURCE_DESCRIPTOR_PATH+"/domain/domainDataSource_Path.JUNIT_NEW_FOLDER.Simple_Domain.xml";
		FileBody bin;
		
    	//building the body
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		
		//appending the file descriptor from a file 
		reqEntity.addPart(REQUEST_PARAMENTER_RD, new StringBody(RESTTestUtilsAndConsts.getResourceDescriptor(new File(fileRd))));
		
		//appending the binaries to the request body
		bin = new FileBody(new File(schemaBinFilePath));
		reqEntity.addPart(schemaURI, bin );
		
		((HttpEntityEnclosingRequestBase)httpReqPro).setEntity(reqEntity);
		
		//executing the request
		httpRes = sendRequestPRO(httpReqPro, RESOURCE+"/JUNIT_NEW_FOLDER");
		Assert.assertTrue("basic response check did not pass", isValidResposnse(403));
		
		loginToServer();
		deleteSampleFolder();
		
	}
	
	@Test  
	public void PUT_200_Resource_DomainDataComplexSource() throws Exception{

		putSampleFolder();
		
		String domainURI="/JUNIT_NEW_FOLDER/supermartDomain";
		
		String schemaURI="/JUNIT_NEW_FOLDER/supermartDomain_files/supermartDomain_domain_schema";
		String propertiesURI="/JUNIT_NEW_FOLDER/supermartDomain_files/supermart_domain.properties";
		String propertiesEnURI="/JUNIT_NEW_FOLDER/supermartDomain_files/supermart_domain_en_US.properties";
		String securityFileURI="/JUNIT_NEW_FOLDER/supermartDomain_files/supermartDomain_domain_security";
		String JNDIURI="/analysis/datasources/SugarCRMDataSourceJNDI";
		
		String schemaBinFilePath = LOCAL_RESOURCE_DESCRIPTOR_PATH+"/domain/supermartDomain_domain_schema.xml";
		String propertiesBin = LOCAL_RESOURCE_DESCRIPTOR_PATH+"/domain/supermart_domain.properties";
		String propertiesEnBin = LOCAL_RESOURCE_DESCRIPTOR_PATH+"/domain/supermart_domain_en_US.properties";
		String securityFileBin = LOCAL_RESOURCE_DESCRIPTOR_PATH+"/domain/supermartDomain_domain_security.xml";
		 
		
		String fileRd = LOCAL_RESOURCE_DESCRIPTOR_PATH+"/domain/supermartDomain_DataSource.xml";
		FileBody bin;
		
    	//building the body
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		
		//appending the file descriptor from a file 
		reqEntity.addPart(REQUEST_PARAMENTER_RD, new StringBody(RESTTestUtilsAndConsts.getResourceDescriptor(new File(fileRd))));
		
		//appending the binaries to the request body
		bin = new FileBody(new File(schemaBinFilePath));
		reqEntity.addPart(schemaURI, bin );
		
		//appending the binaries to the request body
		bin = new FileBody(new File(propertiesBin));
		reqEntity.addPart(propertiesURI, bin );
		
		//appending the binaries to the request body
		bin = new FileBody(new File(propertiesEnBin));
		reqEntity.addPart(propertiesEnURI, bin );
		
		//appending the binaries to the request body
		bin = new FileBody(new File(securityFileBin));
		reqEntity.addPart(securityFileURI, bin );
		
		((HttpEntityEnclosingRequestBase)httpReqPro).setEntity(reqEntity);
		
		//executing the request
		httpRes = sendRequestPRO(httpReqPro, RESOURCE+"/JUNIT_NEW_FOLDER");
		Assert.assertTrue("basic response check did not pass", isValidResposnse(201));
		
		deleteSampleFolder();
		
	}
	
	@Test  
	public void Resource_Query_PUT_200() throws Exception{

		putSampleFolder();
		
		String fileRd = LOCAL_RESOURCE_DESCRIPTOR_PATH+"/query/CustomerCityQuery.xml";
		sendAndAssert_CE(new HttpPut(), SAMPLE_FOLDER_URL, fileRd, HttpStatus.SC_CREATED);
		sendAndAssert_PRO(new HttpPut(), SAMPLE_FOLDER_URL, fileRd, HttpStatus.SC_CREATED);
		
		sendAndAssert_CE(new HttpDelete(), SAMPLE_FOLDER_URL+"/CustomerCityQuery");
		sendAndAssert_PRO(new HttpDelete(), SAMPLE_FOLDER_URL+"/CustomerCityQuery");
		
		deleteSampleFolder();
	}
	
	@Test  
	public void Resource_DomainTopic_PUT_200() throws Exception{

		putSampleFolder();
		
		String cssURI="/JUNIT_NEW_FOLDER/Simple_Domain_Topic_files/css_test";
		String cssBin = LOCAL_RESOURCE_DESCRIPTOR_PATH+"/domainTopic/css_test";
		
		String fileRd = LOCAL_RESOURCE_DESCRIPTOR_PATH+"/domainTopic/Simple_Domain_Topic.xml";
		FileBody bin;
		
    	//building the body
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		
		//appending the file descriptor from a file 
		reqEntity.addPart(REQUEST_PARAMENTER_RD, new StringBody(RESTTestUtilsAndConsts.getResourceDescriptor(new File(fileRd))));
		
		//appending the binaries to the request body
		bin = new FileBody(new File(cssBin));
		reqEntity.addPart(cssURI, bin );
		
		((HttpEntityEnclosingRequestBase)httpReqPro).setEntity(reqEntity);
		
		//executing the request
		httpRes = sendRequestPRO(httpReqPro, RESOURCE+"/JUNIT_NEW_FOLDER");
		Assert.assertTrue("basic response check did not pass", isValidResposnse(201));
		
		deleteSampleFolder();
		
	}
	
	@Test  
	public void Resource_DomainTopic_POST_BY_USER_403() throws Exception
	{
		putSampleFolder();
		sendAndAssert_PRO(new HttpPut(), SERVICE_PERMISSION+"/JUNIT_NEW_FOLDER", LOCAL_RESOURCE_DESCRIPTOR_PATH + SAMPLE_PERMISSION_PRO_RD, HttpStatus.SC_OK);

		loginToPROServer(JOEUSER_USER_NAME, JOEUSER_PASSWORD);
		String cssURI="/JUNIT_NEW_FOLDER/Simple_Domain_Topic_files/css_test";
		String cssBin = LOCAL_RESOURCE_DESCRIPTOR_PATH+"/domainTopic/css_test";
		
		String fileRd = LOCAL_RESOURCE_DESCRIPTOR_PATH+"/domainTopic/Simple_Domain_Topic.xml";
		FileBody bin;
		
    	//building the body
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		
		//appending the file descriptor from a file 
		reqEntity.addPart(REQUEST_PARAMENTER_RD, new StringBody(RESTTestUtilsAndConsts.getResourceDescriptor(new File(fileRd))));
		
		//appending the binaries to the request body
		bin = new FileBody(new File(cssBin));
		reqEntity.addPart(cssURI, bin );
		
		((HttpEntityEnclosingRequestBase)httpReqPro).setEntity(reqEntity);
		
		//executing the request
		httpRes = sendRequestPRO(httpReqPro, RESOURCE+"/JUNIT_NEW_FOLDER");
		Assert.assertTrue("basic response check did not pass", isValidResposnse(403));
		
		deleteSampleFolder();
		
	}

	@Test 
	public void Resource_ReportOptions_PUT_201() throws Exception{
		putSampleResource_PRO(LOCAL_RESOURCE_DESCRIPTOR_PATH+"/reportOptions/reportOptions.xml", RESOURCE+"/reports/samples/");
		deleteResource_PRO(RESOURCE+"/reports/samples/Poland");
	}
	
	@Test 
	public void Report_PUT_201() throws Exception{
		putSampleResource_PRO(LOCAL_RESOURCE_DESCRIPTOR_PATH+"/reportOptions/reportOptions.xml", RESOURCE+"/reports/samples/");
		deleteResource_PRO(RESOURCE+"/reports/samples/Poland");
	}
	
	@Test 
	public void testVerify_new_user_of_new_tenant_has_roles() throws Exception {
		loginToPROServer(SUPERUSER_USER_NAME, SUPERUSER_PASSWORD);
		Tenant tenant = sampleOrganizationDescriptor();
		tenant.setParentId(null);
        createRESTOrganization(tenant, HttpStatus.SC_CREATED);

        loginToPROServer(ADMIN_USER_NAME_PRO, ADMIN_PASS_PRO, tenant.getId());

        WSRole[] wsRoles = getRESTRoles("");
        Assert.assertEquals(5, wsRoles.length);
        
        loginToPROServer(SUPERUSER_USER_NAME, SUPERUSER_PASSWORD);
        sendAndAssert_PRO(new HttpDelete(), TENANT_BASE_URL+"/"+tenant.getId());
    }
	
	
	@Test
	public void testVerify_cant_delete_himself() throws Exception {
		loginToServer();
		WSUser wsUser = sampleUserDescriptor();

        wsUser.setTenantId(DEFAULT_ORGANIZATION);
        WSRole[] roles = new WSRole[2];
        roles[0] = getRESTRoles(ROLE_ADMINISTRATOR)[0];
        roles[1] = getRESTRoles(ROLE_USER)[0];
        wsUser.setRoles(roles);

        createRESTUser(wsUser, HttpStatus.SC_CREATED);

        verifyUser(wsUser, true);

        loginToPROServer(wsUser.getUsername(), wsUser.getPassword(), wsUser.getTenantId());

        sendAndAssert_PRO(new HttpDelete(), "/user/" + wsUser.getUsername(), HttpStatus.SC_FORBIDDEN);
    	
        loginToServer();
        sendAndAssert_PRO(new HttpDelete(), "/user/"+wsUser.getUsername());
    }
	
	@Test
	public void testGet_Put_delete_permissions_for_role() throws Exception {
        loginToServer();

        JAXBList<ObjectPermission> permissionsList = getRESTPermissions("/reports", HttpStatus.SC_OK);
        WSRole wsRole = null;

        if (permissionsList.size() > 0) {
            permissionsList.get(0).setPermissionMask(1);
        } else {
            wsRole = sampleRoleDescriptor();
            createRESTRole(wsRole, HttpStatus.SC_CREATED);
            permissionsList.add(getSampleObjectPermission("repo:/reports", convertWSRole(wsRole), 1));
        }
        putRESTPermissions(permissionsList, HttpStatus.SC_OK);

        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("roles", "ROLE_USER"));
        qparams.add(new BasicNameValuePair("users", null)); //users and roles attributes should be specified anyway, even if there are no arguments.

        deleteRESTPermission("/reports", qparams, HttpStatus.SC_OK);

        if (wsRole != null) {
            deleteRESTRole(wsRole.getRoleName());
        }
    }
	
	public JAXBList<ObjectPermission> getRESTPermissions(String resource_url, int expectedStatsCode) throws Exception {

        httpReq = new HttpGet();
        httpRes = sendRequestPRO(httpReq, PERMISSION_BASE_URL + resource_url);
        assertValidResponseCode(expectedStatsCode, httpRes.getStatusLine().getStatusCode());

        if (expectedStatsCode != HttpStatus.SC_OK) {
            getResponseBody(httpRes.getEntity());
            return null;
        }

        InputStream getPermissionsResp = httpRes.getEntity().getContent();

        JAXBList<ObjectPermission> foundPermissions = (JAXBList<ObjectPermission>) RESTUtils.unmarshal(getPermissionsResp,JAXBList.class, 
        																								ObjectPermissionImpl.class, UserImpl.class, RoleImpl.class);
        return foundPermissions;
    }
	
	public WSRole sampleRoleDescriptor() {
        WSRole wsRole = new WSRole();
        wsRole.setRoleName("REST_JUNIT_ROLE");
        wsRole.setExternallyDefined(true);
        wsRole.setTenantId(DEFAULT_ORGANIZATION);

        return wsRole;
    }
	
	public void deleteRESTPermission(String resource_url, List<NameValuePair> params, int expectedStatsCode) throws Exception {
        httpReq = new HttpDelete();
        httpRes = sendRequestPRO(httpReq, PERMISSION_BASE_URL + resource_url, params);
        try {
            assertValidResponseCode(expectedStatsCode, httpRes.getStatusLine().getStatusCode());
        } finally {
            getResponseBody(httpRes.getEntity());
        }
    }
	
	 
	
	

}
