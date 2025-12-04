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

package com.jaspersoft.jasperserver.test.remote;

import java.util.Properties;

/**
 * Contains the username and password used to log in
 * Also contains the initial url as base usl used by all the pages
 **/
public class JasperServerConstants {

    /* changed from an interface to a singleton class so it can be overridden
       for builds that layer on top of OS that want to reuse remote-tests */

    private static JasperServerConstants mConstants;
    private JasperServerConstants() {
        setupProperties();
        setDefaults();
    }

    public static JasperServerConstants instance() {
	if (mConstants == null) {
	    mConstants = new JasperServerConstants();
	}
	return mConstants;
    }

    protected void setupProperties() {
        Properties sys = System.getProperties();

        HOST = sys.getProperty(PROP_HOST_NAME);
        PORT = sys.getProperty(PROP_HOST_PORT);
        APP_CONTEXT_PATH = sys.getProperty(PROP_APP_CONTEXT_PATH);
        DB_TYPE     = sys.getProperty(PROP_DB_TYPE);
        DB_HOST     = sys.getProperty(PROP_DB_HOST);
        DB_PORT     = sys.getProperty(PROP_DB_PORT);
        DB_NAME     = sys.getProperty(PROP_DB_NAME);
        DB_USERNAME = sys.getProperty(PROP_DB_USERNAME);
        DB_PASSWORD = sys.getProperty(PROP_DB_PASSWORD);

    }

    protected void setDefaults() {

        USERNAME      = "jasperadmin";
        PASSWORD      = "jasperadmin";
        USER_USERNAME = "joeuser";
        USER_PASSWORD = "joeuser";
        BASE_URL      = "http://" + HOST + ":" + PORT;
        XMLA_URL      = BASE_URL + "/" + APP_CONTEXT_PATH + "/xmla";
        HOME_PAGE_URL = BASE_URL + "/" + APP_CONTEXT_PATH + "/home.html";
        WS_END_POINT_URL = BASE_URL + "/" + APP_CONTEXT_PATH + "/services/repository";
        WS_SCHEDULING_END_POINT_URL = BASE_URL + "/" + APP_CONTEXT_PATH + "/services/ReportScheduler";

        WS_PROTOCOL   = "http://";
        WS_BASE_URL = HOST + ":" + PORT + "/" + APP_CONTEXT_PATH + "/services/";
        WS_USER_AND_ROLE_MANAGEMENT_END_POINT_URL =
            WS_PROTOCOL + USERNAME + ":" + PASSWORD + "@" + WS_BASE_URL + "UserAndRoleManagementService";
        WS_USER_AND_ROLE_MANAGEMENT_END_POINT_URL_AS_USER =
            WS_PROTOCOL + USER_USERNAME + ":" + USER_PASSWORD + "@" + WS_BASE_URL + "UserAndRoleManagementService";

        WS_PERMISSIONS_MANAGEMENT_END_POINT_URL =
            WS_PROTOCOL + USERNAME + ":" + PASSWORD + "@" + WS_BASE_URL + "PermissionsManagementService";

        REST_BASE_URL = BASE_URL + "/" + APP_CONTEXT_PATH + "/rest_v2";
        REST_RESOURCES_URL = REST_BASE_URL + "/resources";
        REST_REPORTS_URL = REST_BASE_URL + "/reports";
        REST_JOBS_URL = REST_BASE_URL + "/jobs";
        REST_EXPORT_URL = REST_BASE_URL + "/export";
        REST_IMPORT_URL = REST_BASE_URL + "/import";

        USERNAME2     = "joeuser";
        PASSWORD2  = "joeuser";
        BAD_PASSWORD2  = "wrongPassword";
        IMPORT_EXPORT_KEY_ALIAS = "deprecatedImportExportEncSecret";
    }

    public String PROP_HOST_NAME        = "remote.test.host";
    public String PROP_HOST_PORT        = "remote.test.port";
    public String PROP_APP_CONTEXT_PATH = "remote.test.app-context-path";
    public String PROP_DB_TYPE          = "remote.test.dbType";
    public String PROP_DB_HOST          = "remote.test.dbHost";
    public String PROP_DB_PORT          = "remote.test.dbPort";

    public String PROP_DB_NAME          = "remote.test.dbName";
    public String PROP_DB_USERNAME      = "remote.test.dbUsername";
    public String PROP_DB_PASSWORD      = "remote.test.dbPassword";

    public String HOST;
    public String PORT;
    public String APP_CONTEXT_PATH;
    public String DB_TYPE;
    public String DB_HOST;
    public String DB_PORT;

    public String DB_NAME;
    public String DB_USERNAME;
    public String DB_PASSWORD;
    public String USERNAME;
    public String PASSWORD;
    public String USER_USERNAME;
    public String USER_PASSWORD;
    public String BASE_URL;
    public String XMLA_URL;
    public String HOME_PAGE_URL;
    public String WS_END_POINT_URL;
    public String WS_SCHEDULING_END_POINT_URL;

    public String WS_PROTOCOL;
    public String WS_BASE_URL;
    public String WS_USER_AND_ROLE_MANAGEMENT_END_POINT_URL;
    public String WS_USER_AND_ROLE_MANAGEMENT_END_POINT_URL_AS_USER;

    public String WS_PERMISSIONS_MANAGEMENT_END_POINT_URL;

    public String REST_BASE_URL;
    public String REST_RESOURCES_URL;
    public String REST_REPORTS_URL;
    public String REST_JOBS_URL;
    public String REST_EXPORT_URL;
    public String REST_IMPORT_URL;

    public String USERNAME2;
    public String PASSWORD2;
    public String BAD_PASSWORD2;
    public String IMPORT_EXPORT_KEY_ALIAS;
}
