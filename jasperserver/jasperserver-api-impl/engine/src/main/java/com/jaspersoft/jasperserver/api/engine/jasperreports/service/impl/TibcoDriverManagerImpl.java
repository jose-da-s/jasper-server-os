/*
 * Copyright (C) 2025-2026 the Jasper Server OS Authors
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

package com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl;

import com.jaspersoft.jasperserver.api.common.util.TibcoDriverManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

public class TibcoDriverManagerImpl implements TibcoDriverManager {

    private static final Log log = LogFactory.getLog(TibcoDriverManagerImpl.class);
    private static TibcoDriverManagerImpl instance;
    private static String[] SUPPORTED_TIBCO_DS = new String[]{"db2", "hive", "impala", "mongodb", "oracle", "redshift", "salesforce", "sqlserver","autorest", "googlebigquery"};
    public static final String GOOGLE_BIGQUERY_PROGRESS_DRIVER_CLASS = "tibcosoftware.jdbc.googlebigquery.GoogleBigQueryDriver";

    private TibcoDriverManagerImpl() {
        // call of jakarta.ws.rs.ext.RuntimeDelegate.setInstance() isn't needed here,
        // because it is already done in com.jaspersoft.jasperserver.jaxrs.RestSpringApplication
        preLoadDrivers();
    }

    public static TibcoDriverManager getInstance() {
        if (instance == null) instance = new TibcoDriverManagerImpl();
        return instance;
    }

    public Connection unlockConnection(Connection connection) throws java.sql.SQLException {
        return connection;
    }

    public Connection unlockConnection(DataSource dataSource) throws SQLException {
        return dataSource.getConnection();
    }

    private static void printLog(String str) {
        log.debug(str);
    //    System.out.println(str);
    }

    private static void printLog(String str, Throwable error) {
        log.debug(str, error);
    }
    
    public boolean unlockDSConnection(Connection originalConnection) throws SQLException {
        return true;
    }

    static public void preLoadDrivers() {
        registerDriver("tibcosoftware.jdbc.oracle.OracleDriver");
        registerDriver("tibcosoftware.jdbc.sqlserver.SQLServerDriver");
        registerDriver("tibcosoftware.jdbc.db2.DB2Driver");
        registerDriver("tibcosoftware.jdbc.hive.HiveDriver");
        registerDriver("tibcosoftware.jdbc.impala.ImpalaDriver");
        registerDriver("tibcosoftware.jdbc.mongodb.MongoDBDriver");
        registerDriver("tibcosoftware.jdbc.redshift.RedshiftDriver");
        registerDriver("tibcosoftware.jdbc.salesforce.SalesforceDriver");
        registerDriver("tibcosoftware.jdbc.sforce.SForceDriver");
        registerDriver("tibcosoftware.jdbc.autorest.AutoRESTDriver");
        registerDriver(GOOGLE_BIGQUERY_PROGRESS_DRIVER_CLASS);
        deregisterTibcosoftwareincDrivers();
    }

    static void registerDriver(String driverClassName) {
        if (isDriverExisted(driverClassName)) return;
        Class<?> tibcoDriver = null;
        Constructor<?> tdConstructor = null;
        Driver driver = null;
        if(driverClassName!=null){
            try {
                tibcoDriver = Class.forName(driverClassName);
                tdConstructor = tibcoDriver.getDeclaredConstructor();
                driver = (Driver) tdConstructor.newInstance((Object[])null);
                DriverManager.registerDriver(driver);
            } catch (Exception ex) {}
            if(tibcoDriver==null)  printLog("Couldn't Class.forName for " + driverClassName);
            else if(tdConstructor == null) printLog("Couldn't get constructor for " + driverClassName);
            else if(driver==null) printLog("Couldn't instantiate " + driverClassName);
        }
    }

    static boolean isDriverExisted(String driverClass) {
        Enumeration<Driver> e = DriverManager.getDrivers();
        while (e.hasMoreElements()) {
            Driver driver = (Driver)e.nextElement();
            String driverClassName = (String) driver.getClass().getName();
            if (driverClassName.equals(driverClass)) return true;
        }
        return false;
    }

    static void deregisterTibcosoftwareincDrivers() {
        Enumeration<Driver> e = DriverManager.getDrivers();
        while (e.hasMoreElements()) {
            Driver driver = (Driver)e.nextElement();
            String driverClassName = (String) driver.getClass().getName();
            try {
            if (driverClassName.startsWith("tibcosoftwareinc.")) DriverManager.deregisterDriver(driver);
            } catch (Exception ex) {
                printLog("Unable to deregister driver: " + driverClassName, ex);
            }
        }
    }

}

