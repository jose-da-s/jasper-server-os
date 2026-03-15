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

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.ValidationDetail;
import com.jaspersoft.jasperserver.api.common.domain.ValidationResult;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.engine.common.domain.Result;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.ReportUnitRequest;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.ReportUnitResult;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.TrialReportUnitRequest;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.BeanReportDataSource;
import com.jaspersoft.jasperserver.util.test.BaseServiceSetupTestNG;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class EngineServiceTestsTestNG extends BaseServiceSetupTestNG  {
	private ExecutionContext m_context;
    protected static Log m_logger = LogFactory.getLog(EngineServiceTestsTestNG.class);

	public EngineServiceTestsTestNG(){
        m_logger.info("EngineServiceTestsTestNG => constructor() called");
    }

    @BeforeClass()
    public void onSetUp() throws Exception {
        m_logger.info("EngineServiceTestsTestNG => onSetUp() called");

        // create an execution context for these tests
        m_context = new ExecutionContextImpl();

        // create resources for these Engine Service tests
        createBeanDS();
        createTableModelDS();
        createCustomDSReportTemplate();
        createCustomDSReport();
        createTableModelDSReport();
    }

    @AfterClass()
    public void onTearDown() throws Exception {
        m_logger.info("EngineServiceTestsTestNG => onTearDown() called");

        // delete resources for these Engine Service tests
        // (as usual, we delete resources in the opposite order they were created)
        deleteTableModelDSReport();
        deleteCustomDSReport();
        deleteCustomDSReportTemplate();
        deleteTableModelDS();
        deleteBeanDS();
    }

    /**
     *  doExecuteTest
     */
    @Test()
	public void doExecuteTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteTest() called");

		// make a reportunit, execute it, and delete it
		FileResource reportRes = (FileResource) getRepositoryService().newResource(null, FileResource.class);
		reportRes.setFileType(FileResource.TYPE_JRXML);
		setCommon(reportRes, "EmployeesJRXML");
		InputStream jrxml = getClass().getResourceAsStream("/reports/jasper/Employees.jrxml");
		reportRes.readData(jrxml);
		ReportUnit unit = (ReportUnit) getRepositoryService().newResource(null, ReportUnit.class);
		unit.setName("Employees_JDBC");
		unit.setLabel("Employees_JDBC");
		unit.setParentFolder("/reports/samples");
		unit.setDataSourceReference("/datasources/JServerJdbcDS");
		unit.setMainReport(reportRes);

		getRepositoryService().saveResource(null, unit);

		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/Employees_JDBC", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);

		getRepositoryService().deleteResource(null,"/reports/samples/Employees_JDBC");
	}

    /**
     *  doGetResourcesTest
     */
    @Test()
	public void doGetResourcesTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doGetResourcesTest() called");
		ReportUnit reportUnit = (ReportUnit) getRepositoryService().getResource(m_context, "/reports/samples/AllAccounts");
		assertNotNull(reportUnit);
		ResourceReference reportRef = reportUnit.getMainReport();
		assertNotNull(reportRef);
		assertTrue(reportRef.isLocal());
		Resource report = reportRef.getLocalResource();
		assertNotNull(report);
		assertTrue(report instanceof FileResource);
		Resource[] resources = getEngineService().getResources(new ResourceReference(report));
		assertNotNull(resources);
		assertTrue(resources.length == 2);
	}

    /**
     *  doValidateTest
     */
    @Test()
	public void doValidateTest() {
        m_logger.info("EngineServiceTestsTestNG => doValidateTest() called");
		ReportUnit unit = createUnit();

		ValidationResult result = getEngineService().validate(null, unit);
		assertNotNull(result);
		assertEquals(ValidationResult.STATE_ERROR, result.getValidationState());
		List results = result.getResults();
		assertNotNull(results);
		assertTrue(results.size() >= 1);
		ValidationDetail detail = (ValidationDetail) results.get(0);
		assertNotNull(detail);
		assertEquals("SalesByMonthTrialReport", detail.getName());

		addJar(unit);

		result = getEngineService().validate(null, unit);
		assertNotNull(result);
		assertEquals(ValidationResult.STATE_VALID, result.getValidationState());
	}

    /**
     *  doTrialExecuteTest
     */
    @Test()
	public void doTrialExecuteTest() {
        m_logger.info("EngineServiceTestsTestNG => doTrialExecuteTest() called");
		ReportUnit unit = createUnit();
		addJar(unit);

		TrialReportUnitRequest request = new TrialReportUnitRequest(unit, null);
		Result result = getEngineService().execute(null, request);
		assertNotNull(result);
		assertTrue(result instanceof ReportUnitResult);
		ReportUnitResult ruRes = (ReportUnitResult) result;
		JasperPrint print = ruRes.getJasperPrint();
		assertNotNull(print);
		List pages = print.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 1);
	}

    /**
     *  doGetMainJasperReportTest
     */
    @Test()
	public void doGetMainJasperReportTest() {
        m_logger.info("EngineServiceTestsTestNG => doGetMainJasperReportTest() called");
		JasperReport jasperReport = getEngineService().getMainJasperReport(null, "/reports/samples/AllAccounts");
		assertNotNull(jasperReport);
		assertEquals("AllAccounts", jasperReport.getName());
	}

    /**
     *  doExecuteWithCustomDsTest
     */
    @Test()
	public void doExecuteWithCustomDataSourceTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteWithCustomDataSourceTest() called");
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/DataSourceReport", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);
	}

    /**
     *  doExecuteWithTableModelDataSourceTest
     */
    @Test()
	public void doExecuteWithTableModelDataSourceTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteWithTableModelDataSourceTest() called");
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/DataSourceTableModel", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);
	}

    private ReportUnit createUnit() {
        ReportUnit unit = (ReportUnit) getRepositoryService().newResource(null,
                ReportUnit.class);
        setCommon(unit, "SalesByMonthTrial");
        unit.setParentFolder("/reports");

        FileResource mainReport = (FileResource) getRepositoryService().newResource(null, FileResource.class);
        mainReport.setFileType(FileResource.TYPE_JRXML);
        setCommon(mainReport, "SalesByMonthTrialReport");
        mainReport.readData(getClass().getResourceAsStream("/reports/jasper/SalesByMonth.jrxml"));
        unit.setMainReport(mainReport);

        unit.setDataSourceReference("/datasources/JServerJdbcDS");

        FileResource img = (FileResource) getRepositoryService().newResource(null, FileResource.class);
        img.setFileType(FileResource.TYPE_IMAGE);
        img.readData(getClass().getResourceAsStream("/images/jasperreports.png"));
        setCommon(img, "Logo");
        unit.addResource(img);

        FileResource subrep = (FileResource) getRepositoryService().newResource(null, FileResource.class);
        subrep.setFileType(FileResource.TYPE_JRXML);
        subrep.readData(getClass().getResourceAsStream("/reports/jasper/SalesByMonthDetail.jrxml"));
        setCommon(subrep, "SalesByMonthDetail");
        unit.addResource(subrep);

        FileResource resBdl = (FileResource) getRepositoryService().newResource(null, FileResource.class);
        resBdl.setFileType(FileResource.TYPE_RESOURCE_BUNDLE);
        resBdl.readData(getClass().getResourceAsStream("/resource_bundles/sales.properties"));
        setCommon(resBdl, "sales.properties");
        unit.addResource(resBdl);

        return unit;
    }

    private void addJar(ReportUnit unit) {
        FileResource jar = (FileResource) getRepositoryService().newResource(null,
                FileResource.class);
        jar.setFileType(FileResource.TYPE_JAR);
        jar.readData(getClass().getResourceAsStream("/jars/scriptlet.jar"));
        setCommon(jar, "Scriptlet");
        unit.addResource(jar);
    }

    private void setCommon(Resource res, String id) {
        res.setName(id);
        res.setLabel(id + "_label");
        res.setDescription(id + " description");
    }

    private void createBeanDS() {
        m_logger.info("EngineServiceTestsTestNG => createBeanDS() is creating /datasources/CustomDSFromBean");

        BeanReportDataSource datasource = (BeanReportDataSource) getUnsecureRepositoryService().newResource(null, BeanReportDataSource.class);
        datasource.setName("CustomDSFromBean");
        datasource.setLabel("Custom data source from a bean");
        datasource.setDescription("A custom data source through a bean");
        datasource.setParentFolder("/datasources");

        datasource.setBeanName("customTestDataSourceService");

        getUnsecureRepositoryService().saveResource(null, datasource);
    }

    private void deleteBeanDS() {
        m_logger.info("EngineServiceTestsTestNG => deleteBeanDS() is deleting /datasources/CustomDSFromBean");
        deleteResource("/datasources/CustomDSFromBean");
    }

    private void createTableModelDS() {
        m_logger.info("EngineServiceTestsTestNG => createTableModelDS() is creating /datasources/CustomTableModelDS");

        BeanReportDataSource datasource = (BeanReportDataSource) getUnsecureRepositoryService().newResource(null, BeanReportDataSource.class);
        datasource.setName("CustomTableModelDS");
        datasource.setLabel("Custom data source from a table model");
        datasource.setDescription("A custom data source through a table model");
        datasource.setParentFolder("/datasources");

        datasource.setBeanName("customTestDataSourceServiceFactory");
        datasource.setBeanMethod("tableModelDataSource");

        getUnsecureRepositoryService().saveResource(null, datasource);
    }

    private void deleteTableModelDS() {
        m_logger.info("EngineServiceTestsTestNG => deleteTableModelDS() is deleting /datasources/CustomTableModelDS");
        deleteResource("/datasources/CustomTableModelDS");
    }

    private void createCustomDSReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => createCustomDSReportTemplate() is creating /reports/samples/DataSourceReportTemplate");

        FileResource reportRes = (FileResource) getUnsecureRepositoryService().newResource(null, FileResource.class);
        reportRes.setFileType(FileResource.TYPE_JRXML);
        reportRes.setName("DataSourceReportTemplate");
        reportRes.setLabel("Report showing Custom Data Source");
        reportRes.setDescription("Report showing use of Custom Data Source via a bean");
        reportRes.setParentFolder("/reports/samples");

        InputStream jrxml = getClass().getResourceAsStream("/reports/jasper/DataSourceReport.jrxml");
        reportRes.readData(jrxml);

        getUnsecureRepositoryService().saveResource(null, reportRes);
    }

    private void deleteCustomDSReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => deleteCustomDSReportTemplate() is deleting /reports/samples/DataSourceReportTemplate");
        deleteResource("/reports/samples/DataSourceReportTemplate");
    }

    private void createCustomDSReport() {
        m_logger.info("EngineServiceTestsTestNG => createCustomDSReport() is creating /reports/samples/DataSourceReport");

        ReportUnit unit = (ReportUnit) getUnsecureRepositoryService().newResource(null, ReportUnit.class);
        unit.setName("DataSourceReport");
        unit.setLabel("Report showing Custom Data Source");
        unit.setDescription("Report showing use of Custom Data Source via a bean");
        unit.setParentFolder("/reports/samples");

        unit.setMainReportReference("/reports/samples/DataSourceReportTemplate");
        unit.setDataSourceReference("/datasources/CustomDSFromBean");

        getUnsecureRepositoryService().saveResource(null, unit);
    }

    private void deleteCustomDSReport() {
        m_logger.info("EngineServiceTestsTestNG => deleteCustomDSReport() is deleting /reports/samples/DataSourceReport");
        deleteResource("/reports/samples/DataSourceReport");
    }

    private void createTableModelDSReport() {
        m_logger.info("EngineServiceTestsTestNG => createTableModelDSReport() is creating /reports/samples/DataSourceTableModel");

        ReportUnit unit = (ReportUnit) getUnsecureRepositoryService().newResource(null, ReportUnit.class);
        unit.setName("DataSourceTableModel");
        unit.setLabel("Table Model Data Source");
        unit.setDescription("Report showing use of Custom Data Source via table model");
        unit.setParentFolder("/reports/samples");

        unit.setMainReportReference("/reports/samples/DataSourceReportTemplate");
        unit.setDataSourceReference("/datasources/CustomTableModelDS");
        unit.setMainReportReference("/reports/samples/DataSourceReportTemplate");

        getUnsecureRepositoryService().saveResource(null, unit);
    }

    private void deleteTableModelDSReport() {
        m_logger.info("EngineServiceTestsTestNG => deleteTableModelDSReport() is deleting /reports/samples/DataSourceTableModel");
        deleteResource("/reports/samples/DataSourceTableModel");
    }

    private void deleteResource(String uri) {
        Resource result = getRepositoryService().getResource(null, uri);
        assertNotNull(result);
        getRepositoryService().deleteResource(null, uri);
    }

}
