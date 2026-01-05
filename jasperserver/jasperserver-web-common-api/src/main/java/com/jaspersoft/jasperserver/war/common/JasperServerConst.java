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
package com.jaspersoft.jasperserver.war.common;

/**
 * @author aztec
 * @version $Id: JdbcDataSourceService.java 2331 2006-03-08 15:06:12Z lucian $
 */
public interface JasperServerConst {
    String JASPER_SERVER_REPOSITORY_PATH = "C:\\reports\\";
    String JASPER_SERVER_DATASOURCE = "java:/comp/env/jdbc/jserver";
    String JASPER_SERVER_DB_DRIVER = "com.mysql.jdbc.Driver";
    String JASPER_SERVER_DB_URL = "jdbc:mysql://localhost:3306/jserver";
    String JASPER_REPORT_CONTENT = "jasRepContent";
    String JASPER_SERVER_DB_USER = "root";
    String JASPER_SERVER_DB_PASSWORD = "password";
    String REPORT_REQUEST_PARAM = "report";
	String DATATYPE_REQUEST_PARAM = "dataType";
	String INPUTCONTROL_REQUEST_PARAM = "inputControl";
	String LISTOFVALUES_REQUEST_PARAM = "listOfValues";
	String FILE_JRXML_EXTN = ".jrxml";
    String FILE_JASPER_EXTN = ".jasper";
    int JRXML_THRESHOLD_SIZE = 102400;
    String FIELD_CHOICE_FILE_SYSTEM ="FILE_SYSTEM";
    String FIELD_CHOICE_CONT_REPO ="CONTENT_REPOSITORY";
    String FIELD_CHOICE_LOCAL = "LOCAL";
	String FIELD_CHOICE_NONE = "NONE";
	String TYPE_DATASRC_JNDI 	=	"JNDI Data Source";
    String TYPE_DATASRC_JDBC 	=	"JDBC Data Source";
    String TYPE_DATASRC_VIRTUAL =	"Virtual Data Source";
    String TYPE_DATASRC_AWS =	"AWS Data Source";
    String TYPE_DATASRC_BEAN 	=	"Bean Data Source";
    String TYPE_DATASRC_CUSTOM	=	"Custom Data Source";
    String TYPE_RSRC_IMAGE 		=	"resourceTypes.image";
    String TYPE_RSRC_FONT 		=	"resourceTypes.font";
    String TYPE_RSRC_SUB_REPORT =	"resourceTypes.jrxml";
    String TYPE_RSRC_CLASS_JAR 	=	"resourceTypes.jar";
    String TYPE_RSRC_RESOURCE_BUNDLE 	=	"resourceTypes.resource.bundle";
    String TYPE_RSRC_XML_FILE=	"resourceTypes.xml.file";
    String TYPE_RSRC_CSS_FILE=	"resourceTypes.css.file";
    String TYPE_RSRC_CONTENT_RESOURCE=	"resource.com.jaspersoft.jasperserver.api.metadata.common.domain.ContentResource.label";
    String TYPE_RSRC_MONGODB_JDBC_CONFIG= "resourceTypes.mongodb.jdbc.config";
    String TYPE_RSRC_AZURE_CERTIFICATE= "resourceTypes.azure.certificate.file";
    String TYPE_RSRC_MONGODB_SCHEMA= "resourceTypes.mongodb.schema";
    String TYPE_RSRC_KEY= "resourceTypes.key";
    String TYPE_RSRC_PUB= "resourceTypes.pub";
    String TYPE_RSRC_PPK= "resourceTypes.ppk";
    String TYPE_SECURE_FILE = "resourceTypes.secure.file";
    String TYPE_RSRC_ACCESS_GRANT_SCHEMA=	"resourceTypes.access.grant.schema"; // pro-only
    String TYPE_RSRC_STYLE_TEMPLATE	=	"resourceTypes.style.template";
    String TYPE_RSRC_QUERY 		=	"resourceTypes.query";
    String OPTION_YES		=	"Yes";
    String OPTION_NO		=	"No";
    String TYPE_BOOLEAN = "Boolean";
	String TYPE_SINGLE_VALUE = "Single Value";
	String TYPE_SINGLE_SELECT_LIST_OF_VALUES = "Single Select List of Values";
	String TYPE_SINGLE_SELECT_QUERY = "Single Select Query";
	String TYPE_MULTI_VALUE = "Multi Value";
	String TYPE_MULTI_SELECT_LIST_OF_VALUES = "Multi Select List of Values";
	String TYPE_MULTI_SELECT_QUERY = "Multi Select Query";
	String TYPE_TEXT = "Text";
	String TYPE_NUMBER = "Number";
	String TYPE_DATE = "Date";
	String TYPE_DATE_TIME = "Date-Time";
	String UPLOADED_FILE_NAME="uploadedFileName";
	String UPLOADED_FILE_EXT="uploadedFileExt";
	String FIELD_FILE_UPLOAD="data";

	String REQUEST_PARAMETER_EDIT_RESOURCE_URI = "resource";
	
	//	Max field lengths for validation
	int MAX_LENGTH_NAME = 100;
	int MAX_LENGTH_LABEL = 100;
	int MAX_LENGTH_DESC = 250;

    int MAX_LENGTH_VARCHAR = 255;

	Integer MAX_LENGTH_NAME_W = new Integer(MAX_LENGTH_NAME);
	Integer MAX_LENGTH_LABEL_W = new Integer(MAX_LENGTH_LABEL);
	Integer MAX_LENGTH_DESC_W = new Integer(MAX_LENGTH_DESC);

    Integer MAX_LENGTH_VARCHAR_W = new Integer(MAX_LENGTH_VARCHAR);

	String USER_LOCALE_SESSION_ATTR = "userLocale";
	String USER_TIMEZONE_SESSION_ATTR = "userTimezone";

}
