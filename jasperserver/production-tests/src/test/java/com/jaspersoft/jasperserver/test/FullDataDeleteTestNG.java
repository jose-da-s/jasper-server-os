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
package com.jaspersoft.jasperserver.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;

import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.util.test.BaseServiceSetupTestNG;

/**
 * @author srosen
 *
 * Deletes the full production data for CE using the TestNG framework
 * 
 * 2013-08-14 tkavanagh
 *   Sample report deletion has been removed from this file for the reports found 
 *   in /reports/samples. As of 5.5 release, these reports have been converted to 
 *   test resources (not part of standard sample resources). 
 */
public class FullDataDeleteTestNG extends BaseServiceSetupTestNG {

    protected final Log m_logger = LogFactory.getLog(FullDataDeleteTestNG.class);

	protected void onSetUp() throws Exception {
		m_logger.info("onSetUp() called");
	}

	public void onTearDown() {
		m_logger.info("onTearDown() called");
	}

    /*
    * This method is the starting point for deleting resources that comprise the Full production
    * data for the Community Edition (CE) product.
    *   Full Data == Sample Data
    */
    @Test()
	public void deleteFullDataResources() throws Exception {
        m_logger.info("deleteFullDataResources() called");

        // delete a interactive folder and its resources
        deleteInteractiveReportResources();
        deleteAnalysisConnectionResources();
        deleteUserAuthorityServiceTestResources();
        deleteContentRepositoryTestResources();
        deleteSchedulerResources();
        deleteHibernateRepositoryReportResources();
        deleteHibernateRepositoryDataSourceResources();
        deleteDefaultDomainWhitelist();
        deleteDBProfileAttributes();
    }

    // move this to configuration file if people want to use
    // this for testing on a regular basis...
    private final boolean DO_PSEUDO_JAPANESE_FOODMART = false;

    private void deleteAnalysisConnectionResources() {
        m_logger.info("deleteAnalysisConnectionResources() called");

        // reports
        deleteEmployees();
        deleteEmployeeAccounts();
        deleteAnalysisReportsFolder();

        if (DO_PSEUDO_JAPANESE_FOODMART) {
            deleteFoodmartJaDataSourceResource();
        }

        deleteSugarFoodmartDataSourceResourceVirtual();

        deleteSugarCRMDataSourceResourceJNDI();
        deleteSugarCRMDataSourceResource();

        deleteFoodmartJNDIDataSourceResource();
        deleteFoodmartJDBCDataSourceResource();
    }

    private void deleteEmployees() {
        m_logger.info("deleteEmployees() => deleting /analysis/reports/Employees");
        getUnsecureRepositoryService().deleteResource(null, "/analysis/reports/Employees");
    }

    private void deleteEmployeeAccounts() {
        m_logger.info("deleteEmployeeAccounts() => deleting /analysis/reports/EmployeeAccounts");
        getUnsecureRepositoryService().deleteResource(null, "/analysis/reports/EmployeeAccounts");
    }

    protected void deleteAnalysisReportsFolder() {
        m_logger.info("deleteAnalysisReportsFolder() => deleting /analysis/reports");
        getUnsecureRepositoryService().deleteFolder(null, "/analysis/reports");
    }

    protected void deleteFoodmartJaDataSourceResource() {
        m_logger.info("deleteFoodmartJaDataSourceResource() => deleting /analysis/datasources/FoodmartJaDataSource");
        getUnsecureRepositoryService().deleteResource(null, "/analysis/datasources/FoodmartJaDataSource");
    }

    protected void deleteSugarCRMDataSourceResourceJNDI() {
        m_logger.info("deleteSugarCRMDataSourceResourceJNDI() => deleting /analysis/datasources/SugarCRMDataSourceJNDI");
        getUnsecureRepositoryService().deleteResource(null, "/analysis/datasources/SugarCRMDataSourceJNDI");
    }

    protected void deleteSugarCRMDataSourceResource() {
        m_logger.info("deleteSugarCRMDataSourceResource() => deleting /analysis/datasources/SugarCRMDataSource");
        getUnsecureRepositoryService().deleteResource(null, "/analysis/datasources/SugarCRMDataSource");
    }

    protected void deleteFoodmartJNDIDataSourceResource() {
        m_logger.info("deleteFoodmartJNDIDataSourceResource() => deleting /analysis/datasources/FoodmartDataSourceJNDI");
        getUnsecureRepositoryService().deleteResource(null, "/analysis/datasources/FoodmartDataSourceJNDI");
    }

    protected void deleteFoodmartJDBCDataSourceResource() {
        m_logger.info("deleteFoodmartJDBCDataSourceResource() => deleting /analysis/datasources/FoodmartDataSource");
        getUnsecureRepositoryService().deleteResource(null, "/analysis/datasources/FoodmartDataSource");
    }

    protected void deleteSugarFoodmartDataSourceResourceVirtual() {
        m_logger.info("deleteSugarCRMDataSourceResourceVirtual() => deleting /datasources/SugarCRMDataSourceVirtual");
        getUnsecureRepositoryService().deleteResource(null, "/datasources/SugarFoodmartVDS");
    }

    private void deleteUserAuthorityServiceTestResources() {
        m_logger.info("deleteUserAuthorityServiceTestResources() called");

        User theUser = getUser(BaseServiceSetupTestNG.USER_JOEUSER);
        removeRole(theUser, BaseServiceSetupTestNG.ROLE_USER);
        deleteUser(BaseServiceSetupTestNG.USER_JOEUSER);
    }

    private void deleteContentRepositoryTestResources() {
        m_logger.info("deleteContentRepositoryTestResources() called");

        m_logger.info("deleteContentRepositoryTestResources() => deleting /ContentFiles/xls");
        getUnsecureRepositoryService().deleteFolder(null, "/ContentFiles/xls");

        m_logger.info("deleteContentRepositoryTestResources() => deleting /ContentFiles/pdf");
        getUnsecureRepositoryService().deleteFolder(null, "/ContentFiles/pdf");

        m_logger.info("deleteContentRepositoryTestResources() => deleting /ContentFiles/html");
        getUnsecureRepositoryService().deleteFolder(null, "/ContentFiles/html");

        m_logger.info("deleteContentRepositoryTestResources() => deleting /ContentFiles");
        getUnsecureRepositoryService().deleteFolder(null, "/ContentFiles");
    }

    private void deleteHibernateRepositoryReportResources()  {
        m_logger.info("deleteHibernateRepositoryReportResources() called");

        // delete the analysis/datasources folder
        m_logger.info("deleteHibernateRepositoryReportResources() => deleting /analysis/datasources");
        getUnsecureRepositoryService().deleteFolder(null, "/analysis/datasources");

        // delete the main analysis folder
        m_logger.info("deleteHibernateRepositoryReportResources() => deleting /analysis");
        getUnsecureRepositoryService().deleteFolder(null, "/analysis");

        // 2013-08-14: delete of /reports/samples reports removed 

        // delete a sample image and images folder
        deleteImagesFolderAndSampleImage();

        // delete the reports folder
        m_logger.info("deleteHibernateRepositoryReportResources() => deleting /reports");
        getUnsecureRepositoryService().deleteFolder(null, "/reports");
    }

    private void deleteHibernateRepositoryDataSourceResources()  {
        m_logger.info("deleteHibernateRepositoryDataSourceResources() called");

        // delete the JdbcDS, the RepoDS, and the JndiDS
        // finally, delete the main datasources folder
        deleteJdbcDS();
        deleteRepoDS();
        deleteJndiDS();

        m_logger.info("deleteHibernateRepositoryDataSourceResources() => deleting /datasources");
        getUnsecureRepositoryService().deleteFolder(null, "/datasources");
    }

    private void deleteJndiDS() {
        getUnsecureRepositoryService().deleteResource(null, "/datasources/JServerJNDIDS");
    }

    private void deleteRepoDS() {
        getUnsecureRepositoryService().deleteResource(null, "/datasources/repositoryDS");
    }

    private void deleteJdbcDS() {
        getUnsecureRepositoryService().deleteResource(null, "/datasources/JServerJdbcDS");
    }


    private void deleteImagesFolderAndSampleImage() {
        m_logger.info("deleteImagesFolderAndSampleImage() => deleting /images/JRLogo");
        getUnsecureRepositoryService().deleteResource(null, "/images/JRLogo");

        m_logger.info("deleteImagesFolderAndSampleImage() => deleting /images");
        getUnsecureRepositoryService().deleteFolder(null, "/images");
    }

    private void deleteInteractiveReportResources() {
        deleteCustomersReport();
        deleteCustomersData();
        deleteCustomersDataAdapter();

        deleteTableReport();
        deleteCsvData();
        deleteCsvDataAdapter();

        deleteMapReport();

        m_logger.info("deleteInteractiveFolderAndReportResources() => deleting /reports/interactive");
        getUnsecureRepositoryService().deleteFolder(null, "/reports/interactive");
    }

    private void deleteCustomersReport() {
        m_logger.info("deleteCustomersReport() => deleting /reports/interactive/CustomersReport");
        getUnsecureRepositoryService().deleteResource(null, "/reports/interactive/CustomersReport");
    }

    private void deleteCustomersData() {
        m_logger.info("deleteCustomersData() => deleting /reports/interactive/CustomersData");
        getUnsecureRepositoryService().deleteResource(null, "/reports/interactive/CustomersData");
    }

    private void deleteCustomersDataAdapter() {
        m_logger.info("deleteCustomersDataAdapter() => deleting /reports/interactive/CustomersDataAdapter");
        getUnsecureRepositoryService().deleteResource(null, "/reports/interactive/CustomersDataAdapter");
    }

    private void deleteTableReport() {
        m_logger.info("deleteTableReport() => deleting /reports/interactive/TableReport");
        getUnsecureRepositoryService().deleteResource(null, "/reports/interactive/TableReport");
    }

    private void deleteCsvData() {
        m_logger.info("deleteCsvData() => deleting /reports/interactive/CsvData");
        getUnsecureRepositoryService().deleteResource(null, "/reports/interactive/CsvData");
    }

    private void deleteCsvDataAdapter() {
        m_logger.info("deleteCsvDataAdapter() => deleting /reports/interactive/CsvDataAdapter");
        getUnsecureRepositoryService().deleteResource(null, "/reports/interactive/CsvDataAdapter");
    }

    private void deleteMapReport() {
        m_logger.info("deleteMapReport() => deleting /reports/interactive/MapReport");
        getUnsecureRepositoryService().deleteResource(null, "/reports/interactive/MapReport");
    }

    private void deleteTableModelDSReport() {
        m_logger.info("deleteTableModelDSReport() => deleting /reports/samples/DataSourceTableModel");
        getUnsecureRepositoryService().deleteResource(null, "/reports/samples/DataSourceTableModel");
    }

    private void deleteSchedulerResources() {
        getReportScheduler().deleteCalendar(HOLIDAY_CALENDAR_NAME);
    }
}
