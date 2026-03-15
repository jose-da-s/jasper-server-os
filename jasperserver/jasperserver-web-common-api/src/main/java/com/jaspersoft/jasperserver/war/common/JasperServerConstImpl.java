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

import java.io.Serializable;

/**
 * @author aztec
 * @version $Id: JdbcDataSourceService.java 2331 2006-03-08 15:06:12Z lucian $
 */
public class JasperServerConstImpl implements JasperServerConst, Serializable {
    //getter methods for all constants
    //defined in the interface
    public static String getJSReposPath() {
        return JASPER_SERVER_REPOSITORY_PATH;
    }

    public static String getJSDataSrc() {
        return JASPER_SERVER_DATASOURCE;
    }

    public static String getJSConnector() {
        return JASPER_SERVER_DB_DRIVER;
    }

    public static String getJSUrl() {
        return JASPER_SERVER_DB_URL;
    }

    public static String getJasRepCont() {
        return JASPER_REPORT_CONTENT;
    }

    public static String getJSDbUser() {
        return JASPER_SERVER_DB_USER;
    }

    public static String getJSDbPasswd() {
        return JASPER_SERVER_DB_PASSWORD;
    }

    public static String getRepReqParam() {
        return REPORT_REQUEST_PARAM;
    }

	public static String getDataTypeReqParam() {
		return DATATYPE_REQUEST_PARAM;
	}

	public static String getInputControlReqParam() {
		return INPUTCONTROL_REQUEST_PARAM;
	}

	public static String getListOfValuesReqParam() {
		return LISTOFVALUES_REQUEST_PARAM;
	}

    public static String getJrxmlFileExtn() {
		return FILE_JRXML_EXTN;
	}

    public static String getJasperFileExtn() {
		return FILE_JASPER_EXTN;
	}

	public static int getJrxmlMaxSize() {
		return JRXML_THRESHOLD_SIZE;
	}

	public static String getFieldChoiceFile() {
		return FIELD_CHOICE_FILE_SYSTEM;
	}
	
	public static String getFieldChoiceRepo() {
		return FIELD_CHOICE_CONT_REPO;
	}
	public static String getFieldChoiceLocal() {
		return FIELD_CHOICE_LOCAL;
	}

	public static String getFieldChoiceNone() {
		return FIELD_CHOICE_NONE;
	}

	public static String getJNDIDatasourceType(){
		return TYPE_DATASRC_JNDI;
	}

	public static String getJDBCDatasourceType(){
		return TYPE_DATASRC_JDBC;
	}

    public static String getVirtualDatasourceType(){
		return TYPE_DATASRC_VIRTUAL;
	}

    public static String getAwsDatasourceType(){
		return TYPE_DATASRC_AWS;
	}

	public static String getBeanDatasourceType(){
		return TYPE_DATASRC_BEAN;
	}

	public static String getImageResourceType(){
		return TYPE_RSRC_IMAGE;
	}

	public static String getFontResourceType(){
		return TYPE_RSRC_FONT;
	}

	public static String getClassJarResourceType(){
		return TYPE_RSRC_CLASS_JAR;
	}

	public static String getQueryResourceType(){
		return TYPE_RSRC_QUERY;
	}

	public static String getSubReportResourceType(){
		return TYPE_RSRC_SUB_REPORT;
	}

	public static String getNoOption(){
		return OPTION_NO;
	}
	public static String getYesOption(){
		return OPTION_YES;
	}
	public static String getBooleanCtrlType(){
		return TYPE_BOOLEAN;
	}
    public static String getSingleValueCtrlType(){
    	return TYPE_SINGLE_VALUE;
    }
    public static String getSingleSelectLovCtrlType(){
    	return TYPE_SINGLE_SELECT_LIST_OF_VALUES;
    }
    public static String getSingleSelectQueryCtrlType(){
    	return TYPE_SINGLE_SELECT_QUERY;
    }
    public static String getMultiValueCtrlType(){
    	return TYPE_MULTI_VALUE;
    }
    public static String getMultiSelectLovCtrlType(){
    	return TYPE_MULTI_SELECT_LIST_OF_VALUES;
    }
    public static String getMultiSelectQueryCtrl(){
    	return TYPE_MULTI_SELECT_QUERY;
    }
    public static String getTypeText(){
    	return TYPE_TEXT;
    }
    public static String getTypeNumber(){
    	return TYPE_NUMBER;
    }
    public static String getTypeDate(){
    	return TYPE_DATE;
    }
    public static String getTypeDateTime(){
    	return TYPE_DATE_TIME;
    }
    public String getUploadedFileName(){
    	return UPLOADED_FILE_NAME;
    }
    public String getUploadedFileExt(){
    	return UPLOADED_FILE_EXT;
    }

	public static String getUserLocaleSessionAttr()
	{
		return USER_LOCALE_SESSION_ATTR;
	}

	public static String getUserTimezoneSessionAttr()
	{
		return USER_TIMEZONE_SESSION_ATTR;
	}
}
