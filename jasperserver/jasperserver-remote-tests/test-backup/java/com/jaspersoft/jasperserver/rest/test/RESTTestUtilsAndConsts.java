package com.jaspersoft.jasperserver.rest.test;

import com.jaspersoft.jasperserver.api.metadata.user.service.TenantService;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;

public class RESTTestUtilsAndConsts 
{
	static final boolean SHOW_SPEC_MODE = false; 
	static final String ORG = "organization_1";
	static final String PIPE = "%7C";	
	
	// SERVER PARAMETERS
	static final String SCHEME = "http";  
	static final String HOST = "localhost";
	static final int PORT = 8080;
	
	//Server paths
	static final String BASE_REST_URL = "/jasperserver/rest";
	static final String BASE_REST_PRO_URL = "/jasperserver-pro/rest";

    //Server paths
    static final String JASPERSERVER_CE_URL = "/jasperserver";
    static final String JASPERSERVER_PRO_URL = "/jasperserver-pro";
	
	
	// USER PARAMETERS
	static final String ADMIN_USER_NAME_CE = "jasperadmin";
	static final String ADMIN_PASS_CE = "jasperadmin";
	
	// USER PARAMETERS
	static final String ADMIN_USER_NAME_PRO = "jasperadmin";
	static final String ADMIN_PASS_PRO = "jasperadmin";
	public static final String ROLE_BASE_URL = "/role";
	
	static final String SUPERUSER_USER_NAME = "superuser";
	static final String SUPERUSER_PASSWORD = "superuser";
	
	static final String JOEUSER_USER_NAME = "joeuser";
	static final String JOEUSER_PASSWORD = "joeuser";
	
	static final String DEMO_USER_NAME = "demo";
	static final String DEMO_PASSWORD = "demo";
	
	public static final String REPORT_BASE_URL = "/report";
	public static final String TAG_NAME_FILE = "file";
	public static final String TAG_VALUE_REPORT = "report";
	public static final String TAG_VALUE_JASPERPRINT = "jasperPrint";
	public static final String REST_RESULTS_FOLDER = "src/main/java/com/jaspersoft/jasperserver/rest/test/serverresources/report/outputFiles";
	public static final String RUN_OUTPUT_FORMAT = "RUN_OUTPUT_FORMAT";
	
	public static final String DEFAULT_ORGANIZATION = "organization_1";
	public static final String ORGANIZATIONS = TenantService.ORGANIZATIONS;
	public static final String TENANT_BASE_URL = "/organization";
	
	public static String ROLE_ADMINISTRATOR = "ROLE_ADMINISTRATOR";
	public static String ROLE_USER= "ROLE_USER";
	public static String USER_BASE_URL= "/user";
	// REST SERVICES
	
	/* SERVER ENTITIES */
	//community edition
	static final String SERVICE_LOGIN = "/login";
	static final String SERVICE_JOB = "/job";
	static final String SERVICE_JOB_SUMMARY = "/jobsummary";
	static final String SERVICE_USER = "/user";
	static final String SERVICE_ROLE = "/role";
	static final String SERVICE_ATTRIBUTE = "/attribute";
	static final String SERVICE_PERMISSION = "/permission";
	public static final String PERMISSION_BASE_URL = "/permission";
	
	// pro
	static final String SERVICE_ORGANIZATION = "/organization";
	
	
	/* SERVER RESOURCES*/
	// general
	static final String RESOURCE_LIST = "/resources";
	public static final String RESOURCE = "/resource";
	
	// RESOURCE DESCRIPTORS
	// constants
	public static final String LOCAL_RESOURCE_DESCRIPTOR_PATH = "src/main/java/com/jaspersoft/jasperserver/rest/test/resources";
	public static final String LOCAL_NEW_RESOURCE_DESCRIPTORS_LOCATION = "/descriptors/new";
	public static final String LOCAL_UPDATE_RESOURCE_DESCRIPTORS_LOCATION = "/descriptors/update";
	public static final String LOCAL_NEW_RDS = LOCAL_RESOURCE_DESCRIPTOR_PATH + LOCAL_NEW_RESOURCE_DESCRIPTORS_LOCATION;
	public static final String LOCAL_UPDATE_RDS = LOCAL_RESOURCE_DESCRIPTOR_PATH + LOCAL_UPDATE_RESOURCE_DESCRIPTORS_LOCATION;
	
	// resource descriptors paths
	static final String SAMPLE_FOLDER_RD = "/folder_URI.newFolder.xml";
	static final String SAMPLE_FOLDER1_RD = "/folder_URI.newFolder1.xml";
	static final String SAMPLE_FOLDER_REPORTS_RD = "/folder_URI.reports.xml";
	static final String SAMPLE_FOLDER_PUBLIC_RD = "/folder_URI.public.xml";
	static final String SAMPLE_DATA_TYPE_RD = "/dataType_URI.datasources.DataType1.xml";
	static final String SAMPLE_IMAGE_FILE_RD = "/image_URI.JUNIT_NEW_FOLDER.JUNIT_IMAGE_FILE.xml";
	static final String SAMPLE_IMAGE_FILE_BIN = "/jasperSoftLogo.jpg";
	static final String SAMPLE_IMAGE_2_FILE_BIN = "/differentPic.jpg";
	static final String SAMPLE_IMAGE_LOGO2_INSIDE_REPORT_FILE_RD = "/image_URI.JUNIT_NEW_FOLDER.JUNIT_REPORT_UNIT_SALES_BY_MONTH_files.Logo2.xml";
	static final String SAMPLE_IMAGE_LOGO_INSIDE_REPORT_FILE_RD = "/image_URI.JUNIT_NEW_FOLDER.JUNIT_REPORT_UNIT_SALES_BY_MONTH_files.Logo.xml";
	static final String SAMPLE_PROPERTIES_BIN = "/JUNIT_PROPERTIES.properties";
	static final String SAMPLE_INPUT_CONTROL_RD = "/inputControl_URI.ContentFiles.isMyInputControl.xml";
	static final String SAMPLE_INPUT_CONTROL_WITH_REFERENCE_RD = "/inputControl_URI.ContentFiles.JUNIT_INPUT_CONTROL_CONTAINS_REFERENCE.xml";
	static final String SAMPLE_INPUT_CONTROL_WITH_LOCAL_RESOURCE_RD = "/inputControl_URI.JUNIT_NEW_FOLDER.JUNIT_INPUT_CONTROL_CONTAINS_LOCAL_RESOURCE.xml";
	static final String SAMPLE_JDBC_FILE_RD = "/jdbc_URI.ContentFiles.New_JDBC.xml";
	static final String SAMPLE_LIST_OF_VALUES_RD = "/listOfValues_URI.ContentFiles.JUNIT_LIST_OF_VALUES.xml";
	static final String SAMPLE_COMPOLEX_REPORT_UNIT_RD = "/reportUnit_URI.JUNIT_NEW_FOLDER.COMPLEX_REPORT_UNIT_TEST.xml";
	static final String SAMPLE_JOB_RD = "/reportJob_URI.reports.samples.allaccounts.xml";
	static final String SAMPLE_RUN_REPORT_RD = "/runAllAccountsReport.xml";
	static final String SAMPLE_PROPERITES_RD = "/properities_URI.JUNIT_NEW_FOLDER.JUNIT_PROPERTIES.xml";
	
	
	static final String SAMPLE_ROLE_CE_RD = "/role/role_junit_ce.xml";
	static final String SAMPLE_ROLE_PRO_RD = "/role/role_junit_pro.xml";
	
	static final String SAMPLE_JOB_REG_RD = "/reportJob_URI.reports.samples.allaccounts.xml";
	static final String SAMPLE_JOB_MULTI_OUTPUT_RD = "/multiOutputFormats_uri.reports.samples.allAccounts.xml";
	
	static final String SAMPLE_PERMISSION_CE_RD = "/permission/permission_ce_uri.reports.xml";
	static final String SAMPLE_PERMISSION_PRO_RD = "/permission/permission_pro_uri.reports.xml";
	
	static final String SAMPLE_USER_CE_RD = "/JUNIT_USER_CE.xml";
	static final String SAMPLE_USER_PRO_RD = "/JUNIT_USER_PRO.xml";
	
	static final String SAMPLE_ORGANIZATION_RD ="/organization_URI.organization_1.JOrg.xml";
	static final String SAMPLE_DOMAIN_RD ="/Domain_URI.Domains.Simple_Domain.xml";
	static final String SAMPLE_PERMISSIONS_RD = "/permissions.xml";
	static final String SAMPLE_ATTRIBUTE_XML = "/attributes.xml";
	
	//binaries
	static final String BIN_IMAGE_FILE = "/jasperSoftLogo.jpg";
	static final String BIN_SAMPLE_PROPERTIES = "/sales_ro.properties";
	static final String BIN_SAMPLE_PROPERTIES2 = "/sales_ro2.properties";
	
	
	// CALLS PARAMETERS
	// login
	static final String PARAMETER_USERNAME = "j_username";
	static final String PARAM_PASSWORD = "j_password";
	
	// resource 
	static final String MOVE_TO = "moveTo";
	static final String COPY_TO = "copyTo";
	
		
	// SERVER FOLDERS
	static final String SAMPLE_FOLDER_URL = RESOURCE+"/JUNIT_NEW_FOLDER";
	static final String SAMPLE_REPORT_URL = "/reports/samples/AllAccounts";
    
    public static final String CT_CONTENT_TYPE = "content-type";
    public static final String CT_JSON = "application/json";
    public static final String CT_XML = "text/xml";
	
	
	static final String LOG4J_PATH = "src/main/java/com/jaspersoft/jasperserver/rest/test/resources/log4j2.properties";
	
	public static String getResourceDescriptor(File f) throws Exception
    {
    	return IOUtils.toString(new FileInputStream(f));
    }

}
