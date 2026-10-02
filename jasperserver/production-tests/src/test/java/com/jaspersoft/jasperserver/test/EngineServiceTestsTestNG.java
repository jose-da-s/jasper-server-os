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
        // the v7 reports come before the v6 ones because they're actually more fundamental, with
        // the v6 reports requiring on-the-fly conversion and getting run as v7 reports in the end
        createV7TableReportTemplate();
        createV7TableReport();
        createV6TableReportTemplate();
        createV6TableReport();
        createV7BarbecueReportTemplate();
        createV7BarbecueReport();
        createV6BarbecueReportTemplate();
        createV6BarbecueReport();
        createV7Barcode4JReportTemplate();
        createV7Barcode4JReport();
        createV6Barcode4JReportTemplate();
        createV6Barcode4JReport();
        createV7ChartReportTemplate();
        createV7ChartReport();
        createV6ChartReportTemplate();
        createV6ChartReport();
        createV7SpiderChartReportTemplate();
        createV7SpiderChartReport();
        createV6SpiderChartReportTemplate();
        createV6SpiderChartReport();
        createV7ElementReportTemplate();
        createV7ElementReport();
        createV6ElementReportTemplate();
        createV6ElementReport();
        createV7ScriptletReportTemplate();
        createV7ScriptletReport();
        createV6ScriptletReportTemplate();
        createV6ScriptletReport();
        createV7FunctionLibraryReportTemplate();
        createV7FunctionLibraryReport();
        createV6FunctionLibraryReportTemplate();
        createV6FunctionLibraryReport();
    }

    @AfterClass()
    public void onTearDown() throws Exception {
        m_logger.info("EngineServiceTestsTestNG => onTearDown() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);

        // delete resources for these Engine Service tests
        // (as usual, we delete resources in the opposite order they were created)
        deleteV6FunctionLibraryReport();
        deleteV6FunctionLibraryReportTemplate();
        deleteV7FunctionLibraryReport();
        deleteV7FunctionLibraryReportTemplate();
        deleteV6ScriptletReport();
        deleteV6ScriptletReportTemplate();
        deleteV7ScriptletReport();
        deleteV7ScriptletReportTemplate();
        deleteV6ElementReport();
        deleteV6ElementReportTemplate();
        deleteV7ElementReport();
        deleteV7ElementReportTemplate();
        deleteV6SpiderChartReport();
        deleteV6SpiderChartReportTemplate();
        deleteV7SpiderChartReport();
        deleteV7SpiderChartReportTemplate();
        deleteV6ChartReport();
        deleteV6ChartReportTemplate();
        deleteV7ChartReport();
        deleteV7ChartReportTemplate();
        deleteV6Barcode4JReport();
        deleteV6Barcode4JReportTemplate();
        deleteV7Barcode4JReport();
        deleteV7Barcode4JReportTemplate();
        deleteV6BarbecueReport();
        deleteV6BarbecueReportTemplate();
        deleteV7BarbecueReport();
        deleteV7BarbecueReportTemplate();
        deleteV6TableReport();
        deleteV6TableReportTemplate();
        deleteV7TableReport();
        deleteV7TableReportTemplate();
        deleteTableModelDSReport();
        deleteCustomDSReport();
        deleteCustomDSReportTemplate();
        deleteTableModelDS();
        deleteBeanDS();
    }

    @Test()
	public void doExecuteTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);

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

    @Test()
	public void doGetResourcesTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doGetResourcesTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
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

		addJar(unit, "/jars/scriptlet.jar", "Scriptlet");

		result = getEngineService().validate(null, unit);
		assertNotNull(result);
		assertEquals(ValidationResult.STATE_VALID, result.getValidationState());
	}

    @Test()
	public void doTrialExecuteTest() {
        m_logger.info("EngineServiceTestsTestNG => doTrialExecuteTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnit unit = createUnit();
		addJar(unit, "/jars/scriptlet.jar", "Scriptlet");

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

    @Test()
	public void doGetMainJasperReportTest() {
        m_logger.info("EngineServiceTestsTestNG => doGetMainJasperReportTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		JasperReport jasperReport = getEngineService().getMainJasperReport(null, "/reports/samples/AllAccounts");
		assertNotNull(jasperReport);
		assertEquals("AllAccounts", jasperReport.getName());
	}

    @Test()
	public void doExecuteWithCustomDataSourceTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteWithCustomDataSourceTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/DataSourceReport", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);
	}

    @Test()
	public void doExecuteWithTableModelDataSourceTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteWithTableModelDataSourceTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/DataSourceTableModel", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);
	}

    @Test()
	public void doExecuteV7TableTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteV7TableTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/table7", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);
	}

    @Test()
	public void doExecuteV6TableTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteV6TableTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/table6", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);
	}

    @Test()
	public void doExecuteV7BarbecueTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteV6BarbecueTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/barbecue7", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);
	}

    @Test()
	public void doExecuteV6BarbecueTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteV6BarbecueTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/barbecue6", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);
	}

    @Test()
	public void doExecuteV7Barcode4JTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteV7Barcode4JTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/barcode4J7", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);
	}

    @Test()
	public void doExecuteV6Barcode4jTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteV6BarbecueTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/barcode4J6", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);
	}

    @Test()
	public void doExecuteV7ChartTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteV6ChartTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/chart7", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);
	}

    @Test()
	public void doExecuteV6ChartTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteV6ChartTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/chart6", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);
	}

    @Test()
	public void doExecuteV7SpiderChartTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteV7SpiderChartTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/spiderChart7", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);
	}

    @Test()
	public void doExecuteV6SpiderChartTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteV6SpiderChartTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/spiderChart6", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);
	}

    @Test()
	public void doExecuteV7ElementTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteV7ElementTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/element7", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);
	}

    @Test()
	public void doExecuteV6ElementTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteV6TableTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/element6", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);
	}

    @Test()
	public void doExecuteV7ScriptletTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteV7ScriptletTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/scriptlet7", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);
	}

    @Test()
	public void doExecuteV6ScriptletTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteV6ScriptletTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/scriptlet6", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);
	}

    @Test()
	public void doExecuteV7FunctionLibraryTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteV7FunctionLibraryTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/functionLibrary7", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);
	}

    @Test()
	public void doExecuteV6FunctionLibraryTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteV6FunctionLibraryTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/functionLibrary6", new HashMap()));
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

    private void addJar(ReportUnit unit, String classpathResource, String repoName) {
        FileResource jar = (FileResource) getRepositoryService().newResource(null,
                FileResource.class);
        jar.setFileType(FileResource.TYPE_JAR);
        jar.readData(getClass().getResourceAsStream(classpathResource));
        setCommon(jar, repoName);
        unit.addResource(jar);
    }

    private void addImage(ReportUnit unit, String classpathResource, String repoName) {
        FileResource img = (FileResource) getRepositoryService().newResource(null,
                FileResource.class);
        img.setFileType(FileResource.TYPE_IMAGE);
        img.readData(getClass().getResourceAsStream(classpathResource));
        setCommon(img, repoName);
        unit.addResource(img);
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

    private void createV7TableReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => createV7TableReportTemplate() is creating /reports/samples/table7ReportTemplate");

        FileResource reportRes = (FileResource) getUnsecureRepositoryService().newResource(null, FileResource.class);
        reportRes.setFileType(FileResource.TYPE_JRXML);
        reportRes.setName("table7ReportTemplate");
        reportRes.setLabel("Jasper 7 Table Report");
        reportRes.setDescription("Jasper 7 report with a table");
        reportRes.setParentFolder("/reports/samples");

        InputStream jrxml = getClass().getResourceAsStream("/reports/jasper67-new/table7.jrxml");
        reportRes.readData(jrxml);

        getUnsecureRepositoryService().saveResource(null, reportRes);
    }

    private void deleteV7TableReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => deleteV7TableReportTemplate() is deleting /reports/samples/table7ReportTemplate");
        deleteResource("/reports/samples/table7ReportTemplate");
    }

    private void createV7TableReport() {
        m_logger.info("EngineServiceTestsTestNG => createV7TableReport() is creating /reports/samples/table7");

        ReportUnit unit = (ReportUnit) getUnsecureRepositoryService().newResource(null, ReportUnit.class);
        unit.setName("table7");
        unit.setLabel("Jasper 7 Table Report");
        unit.setDescription("Jasper 7 report with a table");
        unit.setParentFolder("/reports/samples");

		unit.setDataSourceReference("/datasources/JServerJdbcDS");
        unit.setMainReportReference("/reports/samples/table7ReportTemplate");

        getUnsecureRepositoryService().saveResource(null, unit);
    }

    private void deleteV7TableReport() {
        m_logger.info("EngineServiceTestsTestNG => deleteV7TableReport() is deleting /reports/samples/table7");
        deleteResource("/reports/samples/table7");
    }

    private void createV6TableReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => createV6TableReportTemplate() is creating /reports/samples/table6ReportTemplate");

        FileResource reportRes = (FileResource) getUnsecureRepositoryService().newResource(null, FileResource.class);
        reportRes.setFileType(FileResource.TYPE_JRXML);
        reportRes.setName("table6ReportTemplate");
        reportRes.setLabel("Jasper 6 Table Report");
        reportRes.setDescription("Report showing on-the-fly conversion of a Jasper 6 report with a table");
        reportRes.setParentFolder("/reports/samples");

        InputStream jrxml = getClass().getResourceAsStream("/reports/jasper67-old/table6.jrxml");
        reportRes.readData(jrxml);

        getUnsecureRepositoryService().saveResource(null, reportRes);
    }

    private void deleteV6TableReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => deleteV6TableReportTemplate() is deleting /reports/samples/table6ReportTemplate");
        deleteResource("/reports/samples/table6ReportTemplate");
    }

    private void createV6TableReport() {
        m_logger.info("EngineServiceTestsTestNG => createV6TableReport() is creating /reports/samples/table6");

        ReportUnit unit = (ReportUnit) getUnsecureRepositoryService().newResource(null, ReportUnit.class);
        unit.setName("table6");
        unit.setLabel("Jasper 6 Table Report");
        unit.setDescription("Report showing on-the-fly conversion of a Jasper 6 report with a table");
        unit.setParentFolder("/reports/samples");

		unit.setDataSourceReference("/datasources/JServerJdbcDS");
        unit.setMainReportReference("/reports/samples/table6ReportTemplate");

        getUnsecureRepositoryService().saveResource(null, unit);
    }

    private void deleteV6TableReport() {
        m_logger.info("EngineServiceTestsTestNG => deleteV6TableReport() is deleting /reports/samples/table6");
        deleteResource("/reports/samples/table6");
    }

    private void createV7BarbecueReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => createV7BarbecueReportTemplate() is creating /reports/samples/barbecue7ReportTemplate");

        FileResource reportRes = (FileResource) getUnsecureRepositoryService().newResource(null, FileResource.class);
        reportRes.setFileType(FileResource.TYPE_JRXML);
        reportRes.setName("barbecue7ReportTemplate");
        reportRes.setLabel("Jasper 7 Barbecue Report");
        reportRes.setDescription("Jasper 7 report with Barbecue barcodes");
        reportRes.setParentFolder("/reports/samples");

        InputStream jrxml = getClass().getResourceAsStream("/reports/jasper67-new/barbecue7.jrxml");
        reportRes.readData(jrxml);

        getUnsecureRepositoryService().saveResource(null, reportRes);
    }

    private void deleteV7BarbecueReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => deleteV7BarbecueReportTemplate() is deleting /reports/samples/barbecue7ReportTemplate");
        deleteResource("/reports/samples/barbecue7ReportTemplate");
    }

    private void createV7BarbecueReport() {
        m_logger.info("EngineServiceTestsTestNG => createV7BarbecueReport() is creating /reports/samples/barbecue7");

        ReportUnit unit = (ReportUnit) getUnsecureRepositoryService().newResource(null, ReportUnit.class);
        unit.setName("barbecue7");
        unit.setLabel("Jasper 7 Barbecue Report");
        unit.setDescription("Jasper 7 report with Barbecue barcodes");
        unit.setParentFolder("/reports/samples");

        unit.setMainReportReference("/reports/samples/barbecue7ReportTemplate");

        getUnsecureRepositoryService().saveResource(null, unit);
    }

    private void deleteV7BarbecueReport() {
        m_logger.info("EngineServiceTestsTestNG => deleteV7BarbecueReport() is deleting /reports/samples/barbecue7");
        deleteResource("/reports/samples/barbecue7");
    }

    private void createV6BarbecueReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => createV6BarbecueReportTemplate() is creating /reports/samples/barbecue6ReportTemplate");

        FileResource reportRes = (FileResource) getUnsecureRepositoryService().newResource(null, FileResource.class);
        reportRes.setFileType(FileResource.TYPE_JRXML);
        reportRes.setName("barbecue6ReportTemplate");
        reportRes.setLabel("Jasper 6 Barbecue Report");
        reportRes.setDescription("Report showing on-the-fly conversion of a Jasper 6 report with Barbecue barcodes");
        reportRes.setParentFolder("/reports/samples");

        InputStream jrxml = getClass().getResourceAsStream("/reports/jasper67-old/barbecue6.jrxml");
        reportRes.readData(jrxml);

        getUnsecureRepositoryService().saveResource(null, reportRes);
    }

    private void deleteV6BarbecueReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => deleteV6BarbecueReportTemplate() is deleting /reports/samples/barbecue6ReportTemplate");
        deleteResource("/reports/samples/barbecue6ReportTemplate");
    }

    private void createV6BarbecueReport() {
        m_logger.info("EngineServiceTestsTestNG => createV6BarbecueReport() is creating /reports/samples/barbecue6");

        ReportUnit unit = (ReportUnit) getUnsecureRepositoryService().newResource(null, ReportUnit.class);
        unit.setName("barbecue6");
        unit.setLabel("Jasper 6 Barbecue Report");
        unit.setDescription("Report showing on-the-fly conversion of a Jasper 6 report with Barbecue barcodes");
        unit.setParentFolder("/reports/samples");

        unit.setMainReportReference("/reports/samples/barbecue6ReportTemplate");

        getUnsecureRepositoryService().saveResource(null, unit);
    }

    private void deleteV6BarbecueReport() {
        m_logger.info("EngineServiceTestsTestNG => deleteV6BarbecueReport() is deleting /reports/samples/barbecue6");
        deleteResource("/reports/samples/barbecue6");
    }

    private void createV7Barcode4JReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => createV7Barcode4JReportTemplate() is creating /reports/samples/barcode4J7ReportTemplate");

        FileResource reportRes = (FileResource) getUnsecureRepositoryService().newResource(null, FileResource.class);
        reportRes.setFileType(FileResource.TYPE_JRXML);
        reportRes.setName("barcode4J7ReportTemplate");
        reportRes.setLabel("Jasper 7 Barcode4J Report");
        reportRes.setDescription("Jasper 7 report with Barcode4J barcodes");
        reportRes.setParentFolder("/reports/samples");

        InputStream jrxml = getClass().getResourceAsStream("/reports/jasper67-new/barcode4j7.jrxml");
        reportRes.readData(jrxml);

        getUnsecureRepositoryService().saveResource(null, reportRes);
    }

    private void deleteV7Barcode4JReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => deleteV7Barcode4JReportTemplate() is deleting /reports/samples/barcode4J7ReportTemplate");
        deleteResource("/reports/samples/barcode4J7ReportTemplate");
    }

    private void createV7Barcode4JReport() {
        m_logger.info("EngineServiceTestsTestNG => createV7Barcode4JReport() is creating /reports/samples/barcode4J7");

        ReportUnit unit = (ReportUnit) getUnsecureRepositoryService().newResource(null, ReportUnit.class);
        unit.setName("barcode4J7");
        unit.setLabel("Jasper 7 Barcode4J Report");
        unit.setDescription("Jasper 7 report with Barcode4J barcodes");
        unit.setParentFolder("/reports/samples");

        unit.setMainReportReference("/reports/samples/barcode4J7ReportTemplate");

        getUnsecureRepositoryService().saveResource(null, unit);
    }

    private void deleteV7Barcode4JReport() {
        m_logger.info("EngineServiceTestsTestNG => deleteV7Barcode4JReport() is deleting /reports/samples/barcode4J7");
        deleteResource("/reports/samples/barcode4J7");
    }

    private void createV6Barcode4JReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => createV6Barcode4JReportTemplate() is creating /reports/samples/barcode4J6ReportTemplate");

        FileResource reportRes = (FileResource) getUnsecureRepositoryService().newResource(null, FileResource.class);
        reportRes.setFileType(FileResource.TYPE_JRXML);
        reportRes.setName("barcode4J6ReportTemplate");
        reportRes.setLabel("Jasper 6 Barcode4J Report");
        reportRes.setDescription("Report showing on-the-fly conversion of a Jasper 6 report with Barcode4J barcodes");
        reportRes.setParentFolder("/reports/samples");

        InputStream jrxml = getClass().getResourceAsStream("/reports/jasper67-old/barcode4j6.jrxml");
        reportRes.readData(jrxml);

        getUnsecureRepositoryService().saveResource(null, reportRes);
    }

    private void deleteV6Barcode4JReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => deleteV6Barcode4JReportTemplate() is deleting /reports/samples/barcode4J6ReportTemplate");
        deleteResource("/reports/samples/barcode4J6ReportTemplate");
    }

    private void createV6Barcode4JReport() {
        m_logger.info("EngineServiceTestsTestNG => createV6Barcode4JReport() is creating /reports/samples/barcode4J6");

        ReportUnit unit = (ReportUnit) getUnsecureRepositoryService().newResource(null, ReportUnit.class);
        unit.setName("barcode4J6");
        unit.setLabel("Jasper 6 Barcode4J Report");
        unit.setDescription("Report showing on-the-fly conversion of a Jasper 6 report with Barcode4J barcodes");
        unit.setParentFolder("/reports/samples");

        unit.setMainReportReference("/reports/samples/barcode4J6ReportTemplate");

        getUnsecureRepositoryService().saveResource(null, unit);
    }

    private void deleteV6Barcode4JReport() {
        m_logger.info("EngineServiceTestsTestNG => deleteV6Barcode4JReport() is deleting /reports/samples/barcode4J6");
        deleteResource("/reports/samples/barcode4J6");
    }

    private void createV7ChartReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => createV7ChartReportTemplate() is creating /reports/samples/chart7ReportTemplate");

        FileResource reportRes = (FileResource) getUnsecureRepositoryService().newResource(null, FileResource.class);
        reportRes.setFileType(FileResource.TYPE_JRXML);
        reportRes.setName("chart7ReportTemplate");
        reportRes.setLabel("Jasper 7 Chart Report");
        reportRes.setDescription("Jasper 7 report with a chart");
        reportRes.setParentFolder("/reports/samples");

        InputStream jrxml = getClass().getResourceAsStream("/reports/jasper67-new/chart7.jrxml");
        reportRes.readData(jrxml);

        getUnsecureRepositoryService().saveResource(null, reportRes);
    }

    private void deleteV7ChartReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => deleteV7ChartReportTemplate() is deleting /reports/samples/chart7ReportTemplate");
        deleteResource("/reports/samples/chart7ReportTemplate");
    }

    private void createV7ChartReport() {
        m_logger.info("EngineServiceTestsTestNG => createV7ChartReport() is creating /reports/samples/chart7");

        ReportUnit unit = (ReportUnit) getUnsecureRepositoryService().newResource(null, ReportUnit.class);
        unit.setName("chart7");
        unit.setLabel("Jasper 7 Chart Report");
        unit.setDescription("Jasper 7 report with a chart");
        unit.setParentFolder("/reports/samples");

		unit.setDataSourceReference("/datasources/JServerJdbcDS");
        unit.setMainReportReference("/reports/samples/chart7ReportTemplate");

        getUnsecureRepositoryService().saveResource(null, unit);
    }

    private void deleteV7ChartReport() {
        m_logger.info("EngineServiceTestsTestNG => deleteV7ChartReport() is deleting /reports/samples/chart7");
        deleteResource("/reports/samples/chart7");
    }

    private void createV6ChartReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => createV6ChartReportTemplate() is creating /reports/samples/chart6ReportTemplate");

        FileResource reportRes = (FileResource) getUnsecureRepositoryService().newResource(null, FileResource.class);
        reportRes.setFileType(FileResource.TYPE_JRXML);
        reportRes.setName("chart6ReportTemplate");
        reportRes.setLabel("Jasper 6 Chart Report");
        reportRes.setDescription("Report showing on-the-fly conversion of a Jasper 6 report with a chart");
        reportRes.setParentFolder("/reports/samples");

        InputStream jrxml = getClass().getResourceAsStream("/reports/jasper67-old/chart6.jrxml");
        reportRes.readData(jrxml);

        getUnsecureRepositoryService().saveResource(null, reportRes);
    }

    private void deleteV6ChartReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => deleteV6ChartReportTemplate() is deleting /reports/samples/chart6ReportTemplate");
        deleteResource("/reports/samples/chart6ReportTemplate");
    }

    private void createV6ChartReport() {
        m_logger.info("EngineServiceTestsTestNG => createV6ChartReport() is creating /reports/samples/chart6");

        ReportUnit unit = (ReportUnit) getUnsecureRepositoryService().newResource(null, ReportUnit.class);
        unit.setName("chart6");
        unit.setLabel("Jasper 6 Chart Report");
        unit.setDescription("Report showing on-the-fly conversion of a Jasper 6 report with a chart");
        unit.setParentFolder("/reports/samples");

		unit.setDataSourceReference("/datasources/JServerJdbcDS");
        unit.setMainReportReference("/reports/samples/chart6ReportTemplate");

        getUnsecureRepositoryService().saveResource(null, unit);
    }

    private void deleteV6ChartReport() {
        m_logger.info("EngineServiceTestsTestNG => deleteV6ChartReport() is deleting /reports/samples/chart6");
        deleteResource("/reports/samples/chart6");
    }

    private void createV7SpiderChartReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => createV7SpiderChartReportTemplate() is creating /reports/samples/spiderChart7ReportTemplate");

        FileResource reportRes = (FileResource) getUnsecureRepositoryService().newResource(null, FileResource.class);
        reportRes.setFileType(FileResource.TYPE_JRXML);
        reportRes.setName("spiderChart7ReportTemplate");
        reportRes.setLabel("Jasper 7 Spider Chart Report");
        reportRes.setDescription("Jasper 7 report with a spider chart");
        reportRes.setParentFolder("/reports/samples");

        InputStream jrxml = getClass().getResourceAsStream("/reports/jasper67-new/spiderchart7.jrxml");
        reportRes.readData(jrxml);

        getUnsecureRepositoryService().saveResource(null, reportRes);
    }

    private void deleteV7SpiderChartReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => deleteV7SpiderChartReportTemplate() is deleting /reports/samples/spiderChart7ReportTemplate");
        deleteResource("/reports/samples/spiderChart7ReportTemplate");
    }

    private void createV7SpiderChartReport() {
        m_logger.info("EngineServiceTestsTestNG => createV7ChartReport() is creating /reports/samples/spiderChart7");

        ReportUnit unit = (ReportUnit) getUnsecureRepositoryService().newResource(null, ReportUnit.class);
        unit.setName("spiderChart7");
        unit.setLabel("Jasper 7 Spider Chart Report");
        unit.setDescription("Jasper 7 report with a spider chart");
        unit.setParentFolder("/reports/samples");

		unit.setDataSourceReference("/datasources/JServerJdbcDS");
        unit.setMainReportReference("/reports/samples/spiderChart7ReportTemplate");

        getUnsecureRepositoryService().saveResource(null, unit);
    }

    private void deleteV7SpiderChartReport() {
        m_logger.info("EngineServiceTestsTestNG => deleteV7ChartReport() is deleting /reports/samples/spiderChart7");
        deleteResource("/reports/samples/spiderChart7");
    }

    private void createV6SpiderChartReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => createV6SpiderChartReportTemplate() is creating /reports/samples/spiderChart6ReportTemplate");

        FileResource reportRes = (FileResource) getUnsecureRepositoryService().newResource(null, FileResource.class);
        reportRes.setFileType(FileResource.TYPE_JRXML);
        reportRes.setName("spiderChart6ReportTemplate");
        reportRes.setLabel("Jasper 6 Spider Chart Report");
        reportRes.setDescription("Report showing on-the-fly conversion of a Jasper 6 report with a spider chart");
        reportRes.setParentFolder("/reports/samples");

        InputStream jrxml = getClass().getResourceAsStream("/reports/jasper67-old/spiderchart6.jrxml");
        reportRes.readData(jrxml);

        getUnsecureRepositoryService().saveResource(null, reportRes);
    }

    private void deleteV6SpiderChartReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => deleteV6SpiderChartReportTemplate() is deleting /reports/samples/spiderChart6ReportTemplate");
        deleteResource("/reports/samples/spiderChart6ReportTemplate");
    }

    private void createV6SpiderChartReport() {
        m_logger.info("EngineServiceTestsTestNG => createV6SpiderChartReport() is creating /reports/samples/spiderChart6");

        ReportUnit unit = (ReportUnit) getUnsecureRepositoryService().newResource(null, ReportUnit.class);
        unit.setName("spiderChart6");
        unit.setLabel("Jasper 6 Spider Chart Report");
        unit.setDescription("Report showing on-the-fly conversion of a Jasper 6 report with a spider chart");
        unit.setParentFolder("/reports/samples");

		unit.setDataSourceReference("/datasources/JServerJdbcDS");
        unit.setMainReportReference("/reports/samples/spiderChart6ReportTemplate");

        getUnsecureRepositoryService().saveResource(null, unit);
    }

    private void deleteV6SpiderChartReport() {
        m_logger.info("EngineServiceTestsTestNG => deleteV6SpiderChartReport() is deleting /reports/samples/spiderChart6");
        deleteResource("/reports/samples/spiderChart6");
    }

    private void createV7ElementReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => createV7ElementReportTemplate() is creating /reports/samples/element7ReportTemplate");

        FileResource reportRes = (FileResource) getUnsecureRepositoryService().newResource(null, FileResource.class);
        reportRes.setFileType(FileResource.TYPE_JRXML);
        reportRes.setName("element7ReportTemplate");
        reportRes.setLabel("Jasper 7 Element Report");
        reportRes.setDescription("Jasper 7 report with various elements");
        reportRes.setParentFolder("/reports/samples");

        InputStream jrxml = getClass().getResourceAsStream("/reports/jasper67-new/element7.jrxml");
        reportRes.readData(jrxml);

        getUnsecureRepositoryService().saveResource(null, reportRes);
    }

    private void deleteV7ElementReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => deleteV7ElementReportTemplate() is deleting /reports/samples/element7ReportTemplate");
        deleteResource("/reports/samples/element7ReportTemplate");
    }

    private void createV7ElementReport() {
        m_logger.info("EngineServiceTestsTestNG => createV7ElementReport() is creating /reports/samples/element7");

        ReportUnit unit = (ReportUnit) getUnsecureRepositoryService().newResource(null, ReportUnit.class);
        unit.setName("element7");
        unit.setLabel("Jasper 7 Element Report");
        unit.setDescription("Jasper 7 report with various elements");
        unit.setParentFolder("/reports/samples");

        unit.setMainReportReference("/reports/samples/element7ReportTemplate");

        addImage(unit, "/images/test67.jpg", "test.jpg");
        addImage(unit, "/images/test67.png", "test.png");
        addImage(unit, "/images/test67.svg", "test.svg");

        getUnsecureRepositoryService().saveResource(null, unit);
    }

    private void deleteV7ElementReport() {
        m_logger.info("EngineServiceTestsTestNG => deleteV7ElementReport() is deleting /reports/samples/element7");
        deleteResource("/reports/samples/element7");
    }

    private void createV6ElementReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => createV6ElementReportTemplate() is creating /reports/samples/element6ReportTemplate");

        FileResource reportRes = (FileResource) getUnsecureRepositoryService().newResource(null, FileResource.class);
        reportRes.setFileType(FileResource.TYPE_JRXML);
        reportRes.setName("element6ReportTemplate");
        reportRes.setLabel("Jasper 6 Element Report");
        reportRes.setDescription("Report showing on-the-fly conversion of a Jasper 6 report with various elements");
        reportRes.setParentFolder("/reports/samples");

        InputStream jrxml = getClass().getResourceAsStream("/reports/jasper67-old/element6.jrxml");
        reportRes.readData(jrxml);

        getUnsecureRepositoryService().saveResource(null, reportRes);
    }

    private void deleteV6ElementReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => deleteV6ElementReportTemplate() is deleting /reports/samples/element6ReportTemplate");
        deleteResource("/reports/samples/element6ReportTemplate");
    }

    private void createV6ElementReport() {
        m_logger.info("EngineServiceTestsTestNG => createV6ElementReport() is creating /reports/samples/element6");

        ReportUnit unit = (ReportUnit) getUnsecureRepositoryService().newResource(null, ReportUnit.class);
        unit.setName("element6");
        unit.setLabel("Jasper 6 Element Report");
        unit.setDescription("Report showing on-the-fly conversion of a Jasper 6 report with various elements");
        unit.setParentFolder("/reports/samples");

        unit.setMainReportReference("/reports/samples/element6ReportTemplate");

        addImage(unit, "/images/test67.jpg", "test.jpg");
        addImage(unit, "/images/test67.png", "test.png");
        addImage(unit, "/images/test67.svg", "test.svg");

        getUnsecureRepositoryService().saveResource(null, unit);
    }

    private void deleteV6ElementReport() {
        m_logger.info("EngineServiceTestsTestNG => deleteV6ElementReport() is deleting /reports/samples/element6");
        deleteResource("/reports/samples/element6");
    }

    private void createV7ScriptletReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => createV7ScriptletReportTemplate() is creating /reports/samples/scriptlet7ReportTemplate");

        FileResource reportRes = (FileResource) getUnsecureRepositoryService().newResource(null, FileResource.class);
        reportRes.setFileType(FileResource.TYPE_JRXML);
        reportRes.setName("scriptlet7ReportTemplate");
        reportRes.setLabel("Jasper 7 Scriptlet Report");
        reportRes.setDescription("Jasper 7 report with a scriptlet");
        reportRes.setParentFolder("/reports/samples");

        InputStream jrxml = getClass().getResourceAsStream("/reports/jasper67-new/scriptlet7.jrxml");
        reportRes.readData(jrxml);

        getUnsecureRepositoryService().saveResource(null, reportRes);
    }

    private void deleteV7ScriptletReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => deleteV7ScriptletReportTemplate() is deleting /reports/samples/scriptlet7ReportTemplate");
        deleteResource("/reports/samples/scriptlet7ReportTemplate");
    }

    private void createV7ScriptletReport() {
        m_logger.info("EngineServiceTestsTestNG => createV7ScriptletReport() is creating /reports/samples/scriptlet7");

        ReportUnit unit = (ReportUnit) getUnsecureRepositoryService().newResource(null, ReportUnit.class);
        unit.setName("scriptlet7");
        unit.setLabel("Jasper 7 Scriptlet Report");
        unit.setDescription("Jasper 7 report with a scriptlet");
        unit.setParentFolder("/reports/samples");

        unit.setMainReportReference("/reports/samples/scriptlet7ReportTemplate");

        addJar(unit, "/jars/scriptlet67.jar", "scriptlet67");

        getUnsecureRepositoryService().saveResource(null, unit);
    }

    private void deleteV7ScriptletReport() {
        m_logger.info("EngineServiceTestsTestNG => deleteV7ScriptletReport() is deleting /reports/samples/scriptlet7");
        deleteResource("/reports/samples/scriptlet7");
    }

    private void createV6ScriptletReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => createV6ScriptletReportTemplate() is creating /reports/samples/scriptlet6ReportTemplate");

        FileResource reportRes = (FileResource) getUnsecureRepositoryService().newResource(null, FileResource.class);
        reportRes.setFileType(FileResource.TYPE_JRXML);
        reportRes.setName("scriptlet6ReportTemplate");
        reportRes.setLabel("Jasper 6 Scriptlet Report");
        reportRes.setDescription("Report showing on-the-fly conversion of a Jasper 6 report with a scriptlet");
        reportRes.setParentFolder("/reports/samples");

        InputStream jrxml = getClass().getResourceAsStream("/reports/jasper67-old/scriptlet6.jrxml");
        reportRes.readData(jrxml);

        getUnsecureRepositoryService().saveResource(null, reportRes);
    }

    private void deleteV6ScriptletReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => deleteV6ScriptletReportTemplate() is deleting /reports/samples/scriptlet6ReportTemplate");
        deleteResource("/reports/samples/scriptlet6ReportTemplate");
    }

    private void createV6ScriptletReport() {
        m_logger.info("EngineServiceTestsTestNG => createV6ScriptletReport() is creating /reports/samples/scriptlet6");

        ReportUnit unit = (ReportUnit) getUnsecureRepositoryService().newResource(null, ReportUnit.class);
        unit.setName("scriptlet6");
        unit.setLabel("Jasper 6 Scriptlet Report");
        unit.setDescription("Report showing on-the-fly conversion of a Jasper 6 report with a scriptlet");
        unit.setParentFolder("/reports/samples");

        unit.setMainReportReference("/reports/samples/scriptlet6ReportTemplate");

        addJar(unit, "/jars/scriptlet67.jar", "scriptlet67");

        getUnsecureRepositoryService().saveResource(null, unit);
    }

    private void deleteV6ScriptletReport() {
        m_logger.info("EngineServiceTestsTestNG => deleteV6ScriptletReport() is deleting /reports/samples/scriptlet6");
        deleteResource("/reports/samples/scriptlet6");
    }

    private void createV7FunctionLibraryReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => createV7FunctionLibraryReportTemplate() is creating /reports/samples/functionLibrary7ReportTemplate");

        FileResource reportRes = (FileResource) getUnsecureRepositoryService().newResource(null, FileResource.class);
        reportRes.setFileType(FileResource.TYPE_JRXML);
        reportRes.setName("functionLibrary7ReportTemplate");
        reportRes.setLabel("Jasper 7 Function Library Report");
        reportRes.setDescription("Jasper 7 report with a function library");
        reportRes.setParentFolder("/reports/samples");

        InputStream jrxml = getClass().getResourceAsStream("/reports/jasper67-new/functionLibrary7.jrxml");
        reportRes.readData(jrxml);

        getUnsecureRepositoryService().saveResource(null, reportRes);
    }

    private void deleteV7FunctionLibraryReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => deleteV7FunctionLibraryReportTemplate() is deleting /reports/samples/functionLibrary7ReportTemplate");
        deleteResource("/reports/samples/functionLibrary7ReportTemplate");
    }

    private void createV7FunctionLibraryReport() {
        m_logger.info("EngineServiceTestsTestNG => createV7FunctionLibraryReport() is creating /reports/samples/functionLibrary7");

        ReportUnit unit = (ReportUnit) getUnsecureRepositoryService().newResource(null, ReportUnit.class);
        unit.setName("functionLibrary7");
        unit.setLabel("Jasper 7 Function Library Report");
        unit.setDescription("Jasper 7 report with a function library");
        unit.setParentFolder("/reports/samples");

        unit.setMainReportReference("/reports/samples/functionLibrary7ReportTemplate");

        addJar(unit, "/jars/functionLibrary67.jar", "functionLibrary67");

        getUnsecureRepositoryService().saveResource(null, unit);
    }

    private void deleteV7FunctionLibraryReport() {
        m_logger.info("EngineServiceTestsTestNG => deleteV7FunctionLibraryReport() is deleting /reports/samples/functionLibrary7");
        deleteResource("/reports/samples/functionLibrary7");
    }

    private void createV6FunctionLibraryReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => createV6FunctionLibraryReportTemplate() is creating /reports/samples/functionLibrary6ReportTemplate");

        FileResource reportRes = (FileResource) getUnsecureRepositoryService().newResource(null, FileResource.class);
        reportRes.setFileType(FileResource.TYPE_JRXML);
        reportRes.setName("functionLibrary6ReportTemplate");
        reportRes.setLabel("Jasper 6 Function Library Report");
        reportRes.setDescription("Report showing on-the-fly conversion of a Jasper 6 report with a function library");
        reportRes.setParentFolder("/reports/samples");

        InputStream jrxml = getClass().getResourceAsStream("/reports/jasper67-old/functionLibrary6.jrxml");
        reportRes.readData(jrxml);

        getUnsecureRepositoryService().saveResource(null, reportRes);
    }

    private void deleteV6FunctionLibraryReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => deleteV6FunctionLibraryReportTemplate() is deleting /reports/samples/functionLibrary6ReportTemplate");
        deleteResource("/reports/samples/functionLibrary6ReportTemplate");
    }

    private void createV6FunctionLibraryReport() {
        m_logger.info("EngineServiceTestsTestNG => createV6FunctionLibraryReport() is creating /reports/samples/functionLibrary6");

        ReportUnit unit = (ReportUnit) getUnsecureRepositoryService().newResource(null, ReportUnit.class);
        unit.setName("functionLibrary6");
        unit.setLabel("Jasper 6 Function Library Report");
        unit.setDescription("Report showing on-the-fly conversion of a Jasper 6 report with a function library");
        unit.setParentFolder("/reports/samples");

        unit.setMainReportReference("/reports/samples/functionLibrary6ReportTemplate");

        addJar(unit, "/jars/functionLibrary67.jar", "functionLibrary67");

        getUnsecureRepositoryService().saveResource(null, unit);
    }

    private void deleteV6FunctionLibraryReport() {
        m_logger.info("EngineServiceTestsTestNG => deleteV6FunctionLibraryReport() is deleting /reports/samples/functionLibrary6");
        deleteResource("/reports/samples/functionLibrary6");
    }

    private void deleteResource(String uri) {
        Resource result = getRepositoryService().getResource(null, uri);
        assertNotNull(result);
        getRepositoryService().deleteResource(null, uri);
    }

}
