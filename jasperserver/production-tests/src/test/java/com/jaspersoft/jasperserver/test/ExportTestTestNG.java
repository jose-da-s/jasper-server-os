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

/**
 * @author tkavanagh
 * @version $id $
 */

import java.io.InputStream;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.DataType;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Query;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.DataTypeImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.FolderImpl;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.JdbcReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.JndiJdbcReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.export.Parameters;
import com.jaspersoft.jasperserver.util.test.BaseServiceSetupTestNG;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

public class ExportTestTestNG extends BaseExportTestCaseTestNG {

    private static final Log m_logger = LogFactory.getLog(ExportTestTestNG.class);
    private RepositoryService m_repositoryService;
	private ExecutionContext m_context;

	static final String PATH_SEP = "/";
	static final String LABEL = "_label";
	static final String DESC = "_description";
	static final String DESC_TEXT = " for export-import testing";

	/*
	SessionFactory sf;
	HibernateTemplate template;
	*/
    public ExportTestTestNG(){
        m_logger.info("ExportTestTestNG => constructor() called");
    }

    @BeforeClass()
	public void onSetUp() throws Exception {
        m_logger.info("ExportTestTestNG => onSetUp() called");
		super.onSetUp();
        m_repositoryService = getRepositoryService();
        m_context = new ExecutionContextImpl();
	}

    @AfterClass()
	public void onTearDown() throws Exception {
        m_logger.info("ExportTestTestNG => onTearDown() called");
		super.onTearDown();
	}

    @Test()
	public void doFolderExportTest() {
        m_logger.info("ExportTestTestNG => doFolderExportTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);

		createFolderResources();

		// asserts
		FileResource fr1 = (FileResource) m_repositoryService.getResource(null,
				PATH_SEP + FOLDER_FOLDER_NAME1 + PATH_SEP + FOLDER_IMAGE_NAME1);
		assertNotNull(fr1);
		ReportUnit ru1 = (ReportUnit) m_repositoryService.getResource(null,
				PATH_SEP + FOLDER_FOLDER_NAME1 + PATH_SEP + FOLDER_RU_NAME);
		assertNotNull(ru1);

        String exportDir = createExportDir();
		Parameters exportParams = createParameters()
			.addParameterValue(PARAM_EXPORT_DIR, exportDir)
			.addParameterValue(PARAM_EXPORT_URIS, PATH_SEP + FOLDER_FOLDER_NAME1);
		performExport(exportParams);

		deleteFolderResources();

		// asserts
		FileResource fr_null = (FileResource) m_repositoryService.getResource(null,
				PATH_SEP + FOLDER_FOLDER_NAME1 + PATH_SEP + FOLDER_IMAGE_NAME1);
		assertNull(fr_null);

		ReportUnit ru_null = (ReportUnit) m_repositoryService.getResource(null,
				PATH_SEP + FOLDER_FOLDER_NAME1 + PATH_SEP + FOLDER_RU_NAME);
		assertNull(ru_null);

        Parameters importParams = createParameters()
			.addParameterValue(PARAM_IMPORT_DIR, exportDir);
		performImport(importParams);

		// asserts
		FileResource fr2 = (FileResource) m_repositoryService.getResource(null,
				PATH_SEP + FOLDER_FOLDER_NAME1 + PATH_SEP + FOLDER_IMAGE_NAME1);
		assertNotNull(fr2);

		ReportUnit ru2 = (ReportUnit) m_repositoryService.getResource(null,
				PATH_SEP + FOLDER_FOLDER_NAME1 + PATH_SEP + FOLDER_RU_NAME);
		assertNotNull(ru2);

		FileResource fr3 = (FileResource) m_repositoryService.getResource(null,
				PATH_SEP + FOLDER_FOLDER_NAME1 + PATH_SEP + FOLDER_FOLDER_NAME3 + PATH_SEP + FOLDER_IMAGE_NAME3);
		assertNotNull(fr3);

		deleteFolderResources();
	}

	private void createFolderResources() {
		// make a folder hierarchy
		Folder folder = new FolderImpl();
		folder.setName(FOLDER_FOLDER_NAME1);
		folder.setLabel(FOLDER_FOLDER_NAME1 + LABEL);
		folder.setDescription(FOLDER_FOLDER_NAME1 + DESC);
		folder.setParentFolder("/");
		m_repositoryService.saveFolder(null, folder);

		Folder folder2 = new FolderImpl();
		folder2.setName(FOLDER_FOLDER_NAME2);
		folder2.setLabel(FOLDER_FOLDER_NAME2 + LABEL);
		folder2.setDescription(FOLDER_FOLDER_NAME2 + DESC);
		folder2.setParentFolder(folder);
		m_repositoryService.saveFolder(null, folder2);

		Folder folder3 = new FolderImpl();
		folder3.setName(FOLDER_FOLDER_NAME3);
		folder3.setLabel(FOLDER_FOLDER_NAME3 + LABEL);
		folder3.setDescription(FOLDER_FOLDER_NAME3 + DESC);
		folder3.setParentFolder(folder);
		m_repositoryService.saveFolder(null, folder3);

		Folder folder4 = new FolderImpl();
		folder4.setName(FOLDER_FOLDER_NAME4);
		folder4.setLabel(FOLDER_FOLDER_NAME4 + LABEL);
		folder4.setDescription(FOLDER_FOLDER_NAME4 + DESC);
		folder4.setParentFolder(folder3);
		m_repositoryService.saveFolder(null, folder4);

		// make stand alone image in folder1
		FileResource image1 = (FileResource) m_repositoryService.newResource(null, FileResource.class);
		image1.setFileType(FileResource.TYPE_IMAGE);
		image1.readData(getClass().getResourceAsStream("/images/jasperreports.png"));
		image1.setName(FOLDER_IMAGE_NAME1);
		image1.setLabel(FOLDER_IMAGE_NAME1 + LABEL);
		image1.setDescription(FOLDER_IMAGE_NAME1 + DESC);
		image1.setParentFolder(folder);
		m_repositoryService.saveResource(null, image1);

		// make stand alone image in folder3
		FileResource image3 = (FileResource) m_repositoryService.newResource(null, FileResource.class);
		image3.setFileType(FileResource.TYPE_IMAGE);
		image3.readData(getClass().getResourceAsStream("/images/jasperreports.png"));
		image3.setName(FOLDER_IMAGE_NAME3);
		image3.setLabel(FOLDER_IMAGE_NAME3 + LABEL);
		image3.setDescription(FOLDER_IMAGE_NAME3 + DESC);
		image3.setParentFolder(folder3);
		m_repositoryService.saveResource(null, image3);

		// make stand alone image in folder4
		FileResource image4 = (FileResource) m_repositoryService.newResource(null, FileResource.class);
		image4.setFileType(FileResource.TYPE_IMAGE);
		image4.readData(getClass().getResourceAsStream("/images/jasperreports.png"));
		image4.setName(FOLDER_IMAGE_NAME4);
		image4.setLabel(FOLDER_IMAGE_NAME4 + LABEL);
		image4.setDescription(FOLDER_IMAGE_NAME4 + DESC);
		image4.setParentFolder(folder4);
		m_repositoryService.saveResource(null, image4);

		// make ReportUnit, Report jrxml, and image
		ReportUnit unit = (ReportUnit) m_repositoryService.newResource(null, ReportUnit.class);
		unit.setName(FOLDER_RU_NAME);
		unit.setLabel(FOLDER_RU_NAME + LABEL);
		unit.setDescription(FOLDER_RU_NAME + DESC);
		unit.setParentFolder(folder);

		FileResource reportRes = (FileResource) m_repositoryService.newResource(null, FileResource.class);
		reportRes.setFileType(FileResource.TYPE_JRXML);
		reportRes.setName(FOLDER_JRXML_NAME);
		reportRes.setLabel(FOLDER_JRXML_NAME + LABEL);
		reportRes.setDescription(FOLDER_JRXML_NAME + DESC);
		reportRes.setParentFolder(folder);
		InputStream jrxml = getClass().getResourceAsStream(
				"/reports/jasper/TestReportExportImport01.jrxml");
		reportRes.readData(jrxml);
		unit.setMainReport(reportRes);

		unit.setDataSourceReference("/datasources/JServerJNDIDS");

		FileResource res1 = (FileResource) m_repositoryService.newResource(null, FileResource.class);
		res1.setFileType(FileResource.TYPE_IMAGE);
		res1.readData(getClass().getResourceAsStream("/images/jasperreports.png"));
		res1.setName(FOLDER_IMAGE_RU_NAME);
		res1.setLabel(FOLDER_IMAGE_RU_NAME + LABEL);
		res1.setDescription(FOLDER_IMAGE_RU_NAME + DESC);
		unit.addResource(res1);

		m_repositoryService.saveResource(null, unit);
	}

	private void deleteFolderResources() {
		m_repositoryService.deleteResource(null, PATH_SEP + FOLDER_FOLDER_NAME1 + PATH_SEP + FOLDER_IMAGE_NAME1);

		m_repositoryService.deleteResource(null, PATH_SEP + FOLDER_FOLDER_NAME1 + PATH_SEP
				+ FOLDER_FOLDER_NAME3 + PATH_SEP + FOLDER_IMAGE_NAME3);

		m_repositoryService.deleteResource(null, PATH_SEP + FOLDER_FOLDER_NAME1 + PATH_SEP
				+ FOLDER_FOLDER_NAME3 + PATH_SEP + FOLDER_FOLDER_NAME4 + PATH_SEP + FOLDER_IMAGE_NAME4);

		m_repositoryService.deleteResource(null, PATH_SEP + FOLDER_FOLDER_NAME1 + PATH_SEP + FOLDER_RU_NAME);

		m_repositoryService.deleteFolder(null, PATH_SEP + FOLDER_FOLDER_NAME1 + PATH_SEP
				+ FOLDER_FOLDER_NAME3 + PATH_SEP + FOLDER_FOLDER_NAME4);

		m_repositoryService.deleteFolder(null, PATH_SEP + FOLDER_FOLDER_NAME1 + PATH_SEP + FOLDER_FOLDER_NAME3);
		m_repositoryService.deleteFolder(null, PATH_SEP + FOLDER_FOLDER_NAME1 + PATH_SEP + FOLDER_FOLDER_NAME2);
		m_repositoryService.deleteFolder(null, PATH_SEP + FOLDER_FOLDER_NAME1);
	}

	public void skip_testReportUnitExport() throws Exception {

		createReportUnitResources();

		String exportDir = createExportDir();
		Parameters exportParams = createParameters()
			.addParameterValue(PARAM_EXPORT_DIR, exportDir)
			.addParameterValue(PARAM_EXPORT_URIS, RU_URI);
		performExport(exportParams);

		deleteReportUnitResources();

		Parameters importParams = createParameters()
			.addParameterValue(PARAM_IMPORT_DIR, exportDir);
		performImport(importParams);

		deleteReportUnitResources();
	}

	/*
	 * todo: ### pick one of the tests and have it leave a report
	 *       existing so that we can run it from the gui or
	 *       via httpunit and ensure that it works.
	 *       This would be the production cc test.
	 */

	public void skip_testLinkedResources() throws Exception {

		createLinkedResources();

		String exportDir = createExportDir();
		Parameters exportParams = createParameters()
			.addParameterValue(PARAM_EXPORT_DIR, exportDir)
			.addParameterValue(PARAM_EXPORT_URIS, PATH_SEP + LINK_FOLDER_NAME + PATH_SEP + LINK_RU_NAME);
		performExport(exportParams);

		ReportUnit ru1 = (ReportUnit) m_repositoryService.getResource(null,
				PATH_SEP + LINK_FOLDER_NAME + PATH_SEP + LINK_RU_NAME);
		assertNotNull(ru1);
		assertEquals(ru1.getName(), LINK_RU_NAME);

		deleteLinkedResources();

		ReportUnit ru1_null = (ReportUnit) m_repositoryService.getResource(null,
				PATH_SEP + LINK_FOLDER_NAME + PATH_SEP + LINK_RU_NAME);
		assertNull(ru1_null);

		Parameters importParams = createParameters()
			.addParameterValue(PARAM_IMPORT_DIR, exportDir);
		performImport(importParams);

		ReportUnit ru2 = (ReportUnit) m_repositoryService.getResource(null,
				PATH_SEP + LINK_FOLDER_NAME + PATH_SEP + LINK_RU_NAME);
		assertNotNull(ru2);
		assertEquals(ru2.getName(), LINK_RU_NAME);

		deleteLinkedResources();

		ReportUnit ru2_null = (ReportUnit) m_repositoryService.getResource(null,
				PATH_SEP + LINK_FOLDER_NAME + PATH_SEP + LINK_RU_NAME);
		assertNull(ru2_null);
	}

	/*
	 * Add the uri to a arbitrary resource and add a prepend path,
	 * then this test will export that resource to the new location.
	 * Good for testing the export-import of a complex ReportUnit
	 * (for instance).
	 *
	 * Note: If the same test is run twice it will fail the second
	 *       time because the target resource will already be there.
	 *
	 */
    @Test()
	public void doRandomPrependResourceTest() {
        m_logger.info("ExportTestTestNG => doRandomPrependResourceTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);

        /* Performing export. */
        String exportDir = createExportDir();
		Parameters exportParams = createParameters()
			.addParameterValue(PARAM_EXPORT_DIR, exportDir)
			.addParameterValue(PARAM_EXPORT_URIS, "/reports/samples/SalesByMonth");
		performExport(exportParams);
        /* No data is deleted after exporting. */

        /* Performing import. */
		Parameters importParams = createParameters()
			.addParameterValue(PARAM_IMPORT_DIR, exportDir)
			.addParameterValue(PARAM_IMPORT_PREPEND_PATH, "/newLocation_07");
		performImport(importParams);

        /* Asserting that data was really imported. */
        Folder importFolder = m_repositoryService.getFolder(null, "/newLocation_07");
        assertNotNull(importFolder);
        Folder mainSubfolder = m_repositoryService.getFolder(null, "/newLocation_07/reports/samples");
        assertNotNull(mainSubfolder);
        ReportUnit report = (ReportUnit) m_repositoryService.getResource(null,
            "/newLocation_07/reports/samples/SalesByMonth");
        assertNotNull(report);
        
        /* Deleting imported data. */
        m_repositoryService.deleteFolder(null, "/newLocation_07");

        /* Asserting that data was really deleted. */
        importFolder = m_repositoryService.getFolder(null, "/newLocation_07");
        assertNull(importFolder);
	}

	/*
	 * Create some folders and an image and export. Delete originals
	 * and import using a relative path (prepended path).
	 */
    @Test()
	public void doPrependPathSimpleTest() {
        m_logger.info("ExportTestTestNG => doPrependPathSimpleTest() called");

		try {
            createPrependResources();

			String exportDir = createExportDir();
			Parameters exportParams = createParameters()
				.addParameterValue(PARAM_EXPORT_DIR, exportDir)
				.addParameterValue(PARAM_EXPORT_URIS, PREPEND_IMAGE_URI);
			performExport(exportParams);

			// assert not null
			FileResource img1 = (FileResource) m_repositoryService.getResource(null, PREPEND_IMAGE_URI);
			assertNotNull(img1);

			deletePrependResources1();

			// assert null
			FileResource img_null = (FileResource) m_repositoryService.getResource(null, PREPEND_IMAGE_URI);
			assertNull(img_null);

            Parameters importParams = createParameters()
				.addParameterValue(PARAM_IMPORT_DIR, exportDir)
				.addParameterValue(PARAM_IMPORT_PREPEND_PATH, NEW_PREPEND_PATH);
			performImport(importParams);

            // assert not null
			FileResource img2 = (FileResource) m_repositoryService.getResource(null, NEW_PREPEND_PATH + PREPEND_IMAGE_URI);
			assertNotNull(img2);

			deletePrependResources2();	// delete resources under new prepend named dir
		} catch (Exception e) {
			System.out.println("caught exception, e " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void createPrependResources() {

		Folder imagesFolder = m_repositoryService.getFolder(null, "/images");

		if (imagesFolder == null) {
			imagesFolder = new FolderImpl();
			imagesFolder.setName("images");
			imagesFolder.setLabel("Test");
			imagesFolder.setParentFolder("/");
			m_repositoryService.saveFolder(null, imagesFolder);
		}

		Folder folder1 = new FolderImpl();
		folder1.setName(PREPEND_FOLDER1);
		folder1.setLabel("Test Images 1");
		folder1.setDescription("Folder" + DESC_TEXT);
		folder1.setParentFolder(imagesFolder);
		m_repositoryService.saveFolder(null, folder1);

		Folder folder2 = new FolderImpl();
		folder2.setName(PREPEND_FOLDER2);
		folder2.setLabel("Test Images 2");
		folder2.setDescription("Folder" + DESC_TEXT);
		folder2.setParentFolder(folder1);
		m_repositoryService.saveFolder(null, folder2);

		FileResource image = (FileResource) m_repositoryService.newResource(null, FileResource.class);
		image.setFileType(FileResource.TYPE_IMAGE);
		image.readData(getClass().getResourceAsStream("/images/jasperreports.png"));
		image.setName(PREPEND_IMAGE_NAME);
		image.setLabel(PREPEND_IMAGE_NAME + LABEL);
		image.setDescription("Image" + DESC_TEXT);
		image.setParentFolder(folder2);

		m_repositoryService.saveResource(null, image);
	}

	/*
	 * delete resources used for simple prepend test
	 * (before prepending new folder name).
	 */
	private void deletePrependResources1() {
		m_repositoryService.deleteResource(null, PREPEND_IMAGE_URI);
		m_repositoryService.deleteFolder(null, PREPEND_FOLDER2_PATH);
		m_repositoryService.deleteFolder(null, PREPEND_FOLDER1_PATH);
	}

	/*
	 * delete resources used for simple prepend test
	 * (after prepending new folder).
	 */
	private void deletePrependResources2() {
		m_repositoryService.deleteResource(null, NEW_PREPEND_PATH + PREPEND_IMAGE_URI);
		m_repositoryService.deleteFolder(null, NEW_PREPEND_PATH + PREPEND_FOLDER2_PATH);
		m_repositoryService.deleteFolder(null, NEW_PREPEND_PATH + PREPEND_FOLDER1_PATH);
		m_repositoryService.deleteFolder(null, NEW_PREPEND_PATH);
	}

    @Test()
	public void doSimpleFileResourceTest() {
        m_logger.info("ExportTestTestNG => doSimpleFileResourceTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);

        final String MY_IMAGE_NAME = "myTestImage01E12345678";
		final String EXTRA_DIR_NAME = "extra_45678";

		Folder imagesFolder = m_repositoryService.getFolder(null, "/myTestImages");

        /* Creating images folder if needed. */
		if (imagesFolder == null) {
			imagesFolder = new FolderImpl();
			imagesFolder.setName("myTestImages");
			imagesFolder.setLabel("Test");
			imagesFolder.setDescription("Test");
			imagesFolder.setParentFolder("/");
			m_repositoryService.saveFolder(null, imagesFolder);
		}

		FileResource image = (FileResource) m_repositoryService.newResource(null, FileResource.class);
		image.setFileType(FileResource.TYPE_IMAGE);
		image.readData(getClass().getResourceAsStream("/images/jasperreports.png"));
		image.setName(MY_IMAGE_NAME);
		image.setLabel(MY_IMAGE_NAME + LABEL);
		image.setDescription(MY_IMAGE_NAME + DESC_TEXT);
		image.setParentFolder(imagesFolder);

        /* Saving image. */
		m_repositoryService.saveResource(null, image);

        /* Performing export. */
		String exportDir = createExportDir();
		Parameters exportParams = createParameters()
			.addParameterValue(PARAM_EXPORT_DIR, exportDir)
			.addParameterValue(PARAM_EXPORT_URIS, "/myTestImages/" + MY_IMAGE_NAME);
		performExport(exportParams);

        /* Deleting resource/folder. */
		m_repositoryService.deleteResource(null, "/myTestImages/" + MY_IMAGE_NAME);
		m_repositoryService.deleteFolder(null, "/myTestImages");

        /* Assertions that resource/folder were really deleted. */
        image = (FileResource) m_repositoryService.getResource(null, "/myTestImages/" + MY_IMAGE_NAME);
        assertNull(image);
        imagesFolder = m_repositoryService.getFolder(null, "/myTestImages");
        assertNull(imagesFolder);

        /* Performing import. */
        Parameters importParams = createParameters()
            .addParameterValue(PARAM_IMPORT_DIR, exportDir)
            .addParameterValue(PARAM_IMPORT_PREPEND_PATH, EXTRA_DIR_NAME);
        performImport(importParams);

        /* Checking that image was really imported. */
		FileResource img = (FileResource) m_repositoryService.getResource(null, "/" + EXTRA_DIR_NAME + "/myTestImages/"
            + MY_IMAGE_NAME);
		assertNotNull(img);

        /* Deleting imported resource/folder. */
        m_repositoryService.deleteResource(null, "/" + EXTRA_DIR_NAME + "/myTestImages/" + MY_IMAGE_NAME);
        m_repositoryService.deleteFolder(null, "/" + EXTRA_DIR_NAME);

        /* Assertions that resource/folder were really deleted. */
        image = (FileResource) m_repositoryService.getResource(null, "/" + EXTRA_DIR_NAME + "/myTestImages/" +
            MY_IMAGE_NAME);
        assertNull(image);
        imagesFolder = m_repositoryService.getFolder(null, "/" + EXTRA_DIR_NAME);
        assertNull(imagesFolder);
	}

    @Test()
	public void doInputControlTest() {
        m_logger.info("ExportTestTestNG => doInputControlTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);

        /* Creating input control. */
		createInputControlResources();

        /* Performing export. */
		String exportDir = createExportDir();
		Parameters exportParams = createParameters()
			.addParameterValue(PARAM_EXPORT_DIR, exportDir)
			.addParameterValue(PARAM_EXPORT_URIS, INPUT_CONTROL_URI);
		performExport(exportParams);

        /* Deleting input control and its resources. */
		deleteInputControlResources();

		/* Asserting that input control was really deleted. */
		InputControl ic_null = (InputControl) m_repositoryService.getResource(null, INPUT_CONTROL_URI);
		assertNull(ic_null);

        /* Performing import. */
		Parameters importParams = createParameters()
			.addParameterValue(PARAM_IMPORT_DIR, exportDir);
		performImport(importParams);

		/* Asserting that input control should be found. */
		InputControl ic_chk = (InputControl) m_repositoryService.getResource(null, INPUT_CONTROL_URI);
		assertNotNull(ic_chk);
		assertEquals(INPUT_CONTROL_NAME, ic_chk.getName());

		/* Asserting that datatype control should be found. */
		DataType dt_chk = (DataType) ic_chk.getDataType().getLocalResource();
		assertNotNull(dt_chk);
		assertEquals(INPUT_CONTROL_DATATYPE_NAME, dt_chk.getName());

        /* Deleting input control and its resources. */
		deleteInputControlResources();

        /* Asserting that input control was really deleted. */
		ic_null = (InputControl) m_repositoryService.getResource(null, INPUT_CONTROL_URI);
		assertNull(ic_null);
	}

	private void createReportUnitResources() throws Exception {
		// create new folder
		Folder folder = new FolderImpl();
		folder.setName(RU_FOLDER);
		folder.setLabel(RU_FOLDER + LABEL);
		folder.setDescription("Folder" + DESC_TEXT);
		folder.setParentFolder("/reports");
		m_repositoryService.saveFolder(null, folder);

		// create report unit
		ReportUnit unit = (ReportUnit) m_repositoryService.newResource(null, ReportUnit.class);
		unit.setName(RU_NAME);
		unit.setLabel(RU_NAME + LABEL);
		unit.setDescription("Report Unit" + DESC_TEXT);
		unit.setParentFolder(folder);

		// setup the datasource
		JndiJdbcReportDataSource datasource;
		if (m_repositoryService.getResource(m_context, RU_JNDI_URI)== null) {
			datasource = (JndiJdbcReportDataSource) m_repositoryService.newResource(null, JndiJdbcReportDataSource.class);
			datasource.setName(RU_JNDI_NAME);
			datasource.setLabel(RU_JNDI_NAME + LABEL);
			datasource.setDescription("jndi data source" + DESC_TEXT);
			datasource.setJndiName(getJdbcProps().getProperty("test.jndi"));
			datasource.setParentFolder(folder);
			m_repositoryService.saveResource(null, datasource);
		} else {
			datasource = (JndiJdbcReportDataSource) m_repositoryService.getResource(m_context, RU_JNDI_URI);
		}
		unit.setDataSourceReference(RU_JNDI_URI);

		// setup the mainReport jrxml
		FileResource reportRes = (FileResource) m_repositoryService.newResource(null, FileResource.class);
		reportRes.setFileType(FileResource.TYPE_JRXML);
		reportRes.setName(RU_REPORT_NAME);
		reportRes.setLabel(RU_REPORT_NAME + LABEL);
		reportRes.setDescription("Report" + DESC_TEXT);
		InputStream jrxml = getClass().getResourceAsStream("/reports/jasper/TestReportExportImport01.jrxml");
		reportRes.readData(jrxml);
		unit.setMainReport(reportRes);

		// setup an image
		FileResource image1 = (FileResource) m_repositoryService.newResource(null, FileResource.class);
		image1.setFileType(FileResource.TYPE_IMAGE);
		image1.readData(getClass().getResourceAsStream("/images/jasperreports.png"));
		image1.setName(RU_IMAGE_NAME);
		image1.setLabel(RU_IMAGE_NAME + LABEL);
		image1.setDescription("Image" + DESC_TEXT);
		unit.addResource(image1);

		m_repositoryService.saveResource(null, unit);
	}

	private void deleteReportUnitResources() {
		m_repositoryService.deleteResource(null, RU_URI);
		m_repositoryService.deleteResource(null, RU_JNDI_URI);
		m_repositoryService.deleteFolder(null, "/reports/" + RU_FOLDER);
	}

	private void createInputControlResources() {
        InputControl ic = (InputControl) m_repositoryService.newResource(null, InputControl.class);

		ic.setName(INPUT_CONTROL_NAME);
		ic.setLabel(INPUT_CONTROL_NAME + "_label");
		ic.setDescription(INPUT_CONTROL_NAME + " Description");
		ic.setInputControlType(InputControl.TYPE_SINGLE_VALUE);
		ic.setMandatory(false);
		ic.setReadOnly(true);
		ic.setVisible(true);

		DataType dt = new DataTypeImpl();
		dt.setName(INPUT_CONTROL_DATATYPE_NAME);
		dt.setLabel(INPUT_CONTROL_DATATYPE_NAME + "_label");
		dt.setDescription(INPUT_CONTROL_DATATYPE_NAME + " Description");
		dt.setDataTypeType(DataType.TYPE_NUMBER);
		dt.setMaxLength(new Integer(10));
		dt.setMaxValue(new Integer(1000));
		dt.setStrictMax(true);
		ic.setDataType(dt);

		ic.setParentFolder("/datatypes");

		m_repositoryService.saveResource(m_context, ic);
	}

	private void deleteInputControlResources() {
        m_repositoryService.deleteResource(null, INPUT_CONTROL_URI);
	}

	private void createLinkedResources() throws Exception {
		/*
		 * link target resources (ie resources will point to these)
		 */

		Folder folder1 = new FolderImpl();
		folder1.setName(LINK_TARGET_FOLDER_NAME);
		folder1.setLabel(LINK_TARGET_FOLDER_NAME + LABEL);
		folder1.setDescription(LINK_TARGET_FOLDER_NAME + DESC_TEXT);
		folder1.setParentFolder("/");
		m_repositoryService.saveFolder(null, folder1);

		// target image
		FileResource image1 = (FileResource) m_repositoryService.newResource(null, FileResource.class);
		image1.setFileType(FileResource.TYPE_IMAGE);
		image1.readData(getClass().getResourceAsStream("/images/jasperreports.png"));
		image1.setName(LINK_TARGET_IMAGE_NAME);
		image1.setLabel(LINK_TARGET_IMAGE_NAME + LABEL);
		image1.setDescription(LINK_TARGET_IMAGE_NAME + DESC_TEXT);
		image1.setParentFolder(folder1);
		m_repositoryService.saveResource(null, image1);

		// target data source
		JndiJdbcReportDataSource datasource =
			(JndiJdbcReportDataSource) m_repositoryService.newResource(null, JndiJdbcReportDataSource.class);
		datasource.setName(LINK_TARGET_JNDI_NAME);
		datasource.setLabel(LINK_TARGET_JNDI_NAME + LABEL);
		datasource.setDescription(LINK_TARGET_JNDI_NAME + DESC_TEXT);
		datasource.setJndiName(getJdbcProps().getProperty("test.jndi"));
		datasource.setParentFolder(folder1);
		m_repositoryService.saveResource(null, datasource);

		// target jrxml
		FileResource reportRes1 = (FileResource) m_repositoryService.newResource(null, FileResource.class);
		reportRes1.setFileType(FileResource.TYPE_JRXML);
		reportRes1.setName(LINK_TARGET_JRXML_NAME);
		reportRes1.setLabel(LINK_TARGET_JRXML_NAME + LABEL);
		reportRes1.setDescription(LINK_TARGET_JRXML_NAME + DESC_TEXT);
		InputStream jrxml = getClass().getResourceAsStream("/reports/jasper/TestReportExportImport01.jrxml");
		reportRes1.readData(jrxml);
		reportRes1.setParentFolder(folder1);
		m_repositoryService.saveResource(null, reportRes1);

		/*
		 * create linked and other resources
		 */

		// folder
		Folder folder2 = new FolderImpl();
		folder2.setName(LINK_FOLDER_NAME);
		folder2.setLabel(LINK_FOLDER_NAME + LABEL);
		folder2.setDescription(LINK_FOLDER_NAME + DESC_TEXT);
		folder2.setParentFolder("/");
		m_repositoryService.saveFolder(null, folder2);

		// report unit
		ReportUnit unit = (ReportUnit) m_repositoryService.newResource(null, ReportUnit.class);
		unit.setName(LINK_RU_NAME);
		unit.setLabel(LINK_RU_NAME + LABEL);
		unit.setDescription(LINK_RU_NAME + DESC_TEXT);
		unit.setParentFolder(folder2);

		// mainReport jrxml (local)
		FileResource reportRes2 = (FileResource) m_repositoryService.newResource(null, FileResource.class);
		reportRes2.setFileType(FileResource.TYPE_JRXML);
		reportRes2.setName(LINK_JRXML_NAME);
		reportRes2.setLabel(LINK_JRXML_NAME + LABEL);
		reportRes2.setDescription(LINK_JRXML_NAME + DESC_TEXT);
		InputStream jrxml2 = getClass().getResourceAsStream("/reports/jasper/TestReportExportImport01.jrxml");
		reportRes2.readData(jrxml2);
		//unit.setMainReport(reportRes2);

		// mainReport jrxml (reference)
		unit.setMainReportReference(PATH_SEP + LINK_TARGET_FOLDER_NAME + PATH_SEP + LINK_TARGET_JRXML_NAME);

		// link to existing image
		FileResource imgLink = (FileResource) m_repositoryService.newResource(null, FileResource.class);
		imgLink.setFileType(FileResource.TYPE_IMAGE);
		imgLink.setName(LINK_IMAGE_NAME1);
		imgLink.setLabel(LINK_IMAGE_NAME1 + LABEL);
		imgLink.setDescription(LINK_IMAGE_NAME1 + DESC_TEXT);
		imgLink.setReferenceURI(PATH_SEP + LINK_TARGET_FOLDER_NAME + PATH_SEP + LINK_TARGET_IMAGE_NAME);
		unit.addResource(imgLink);

		// set datasource (local)
		JndiJdbcReportDataSource datasource2 =
			(JndiJdbcReportDataSource) m_repositoryService.newResource(null, JndiJdbcReportDataSource.class);
		datasource2.setName(LINK_RU_JNDI_NAME);
		datasource2.setLabel(LINK_RU_JNDI_NAME + LABEL);
		datasource2.setDescription(LINK_RU_JNDI_NAME + DESC_TEXT);
		datasource2.setJndiName(getJdbcProps().getProperty("test.jndi"));
		//unit.setDataSource(datasource2);

		// set datasource (reference)
		unit.setDataSourceReference(PATH_SEP + LINK_TARGET_FOLDER_NAME + PATH_SEP + LINK_TARGET_JNDI_NAME);

		m_repositoryService.saveResource(null, unit);
	}

	private void deleteLinkedResources() {
		m_repositoryService.deleteResource(null, PATH_SEP + LINK_FOLDER_NAME + PATH_SEP + LINK_RU_NAME);
		m_repositoryService.deleteFolder(null, PATH_SEP + LINK_FOLDER_NAME);

		m_repositoryService.deleteResource(null, PATH_SEP + LINK_TARGET_FOLDER_NAME + PATH_SEP + LINK_TARGET_IMAGE_NAME);
		m_repositoryService.deleteResource(null, PATH_SEP + LINK_TARGET_FOLDER_NAME + PATH_SEP + LINK_TARGET_JNDI_NAME);
		m_repositoryService.deleteResource(null, PATH_SEP + LINK_TARGET_FOLDER_NAME + PATH_SEP + LINK_TARGET_JRXML_NAME);
		m_repositoryService.deleteFolder(null, PATH_SEP + LINK_TARGET_FOLDER_NAME);
	}

    @Test()
	public void doQueryExportImportTest() {
        m_logger.info("ExportTestTestNG => doQueryExportImportTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);

		Folder folder = new FolderImpl();
		folder.setName("tmp_export");
		folder.setLabel("tmp_export");
		m_repositoryService.saveFolder(m_context, folder);

		try {
			Query query = (Query) m_repositoryService.newResource(m_context, Query.class);
			query.setName("query");
			query.setLabel("query");
			query.setParentFolder("/tmp_export");
			query.setLanguage("sql");
			query.setSql("select * from dual");

			JdbcReportDataSource ds = (JdbcReportDataSource) m_repositoryService.newResource(m_context, JdbcReportDataSource.class);
			ds.setName("ds");
			ds.setLabel("ds");
			ds.setConnectionUrl("jdbc:mysql://localhost:3306/jasperserver");
			ds.setDriverClass("com.mysql.jdbc.Driver");
			ds.setUsername("user");
			ds.setPassword("passwd");

			query.setDataSource(ds);

			m_repositoryService.saveResource(m_context, query);

			boolean queryDeleted = false;
			try {

				String exportDir = createExportDir();
				Parameters exportParams = createParameters()
					.addParameterValue(PARAM_EXPORT_DIR, exportDir)
					.addParameterValue(PARAM_EXPORT_URIS, "/tmp_export/query");
				performExport(exportParams);

				m_repositoryService.deleteResource(m_context, "/tmp_export/query");
				queryDeleted = true;

				Resource queryRes = m_repositoryService.getResource(m_context, "/tmp_export/query");
				assertNull("Query deleted", queryRes);

				queryDeleted = false;

				Parameters importParams = createParameters()
					.addParameterValue(PARAM_IMPORT_DIR, exportDir);
				performImport(importParams);

				queryRes = m_repositoryService.getResource(m_context, "/tmp_export/query");
				assertNotNull("Query imported", queryRes);
				assertTrue(queryRes instanceof Query);
				query = (Query) queryRes;

				assertEquals("sql", query.getLanguage());
				ResourceReference dsRef = query.getDataSource();
				assertNotNull("Query datasource", dsRef);
				assertTrue("Query datasource local", dsRef.isLocal());
				assertNotNull(dsRef.getLocalResource());
				assertTrue(dsRef.getLocalResource() instanceof JdbcReportDataSource);
				ds = (JdbcReportDataSource) dsRef.getLocalResource();
				assertEquals("ds", ds.getName());
				assertEquals("passwd", ds.getPassword());

				m_repositoryService.deleteResource(m_context, "/tmp_export/query");
				queryDeleted = true;
			} finally {
				if (!queryDeleted) {
					m_repositoryService.deleteResource(m_context, "/tmp_export/query");
				}
			}
		} finally {
			m_repositoryService.deleteFolder(null, "/tmp_export");
		}
	}

	// LINK TESTING
	static final String LINK_TARGET_FOLDER_NAME = "test_01E";
	static final String LINK_TARGET_IMAGE_NAME = "TestImage_01E";
	static final String LINK_TARGET_JNDI_NAME = "TestJNDI_01E";
	static final String LINK_TARGET_JDBC_NAME = "TestJDBC_01E";
	static final String LINK_TARGET_JRXML_NAME = "TestJRXML_01E";
	static final String LINK_FOLDER_NAME = "test_link_01E";
	static final String LINK_RU_NAME = "TestReportUnitWithLinks_01E";
	static final String LINK_RU_JNDI_NAME = "TestRUJNDI_01E";
	static final String LINK_JRXML_NAME = "TestLinkedJRXML_01E";
	static final String LINK_IMAGE_NAME1 = "TestLinkedImage_01E";

	// PREPEND PATH IMAGE TESTING
	static final String PREPEND_FOLDER1 = "imageDir1A_B12";
	static final String PREPEND_FOLDER2 = "imageDir2A_B12";
	static final String PREPEND_IMAGE_NAME = "TestImage03A_B12";
	static final String NEW_PREPEND_PATH = "/extra_01A_B12";
	static final String PREPEND_FOLDER1_PATH = "/images/" + PREPEND_FOLDER1;
	static final String PREPEND_FOLDER2_PATH = PREPEND_FOLDER1_PATH + PATH_SEP + PREPEND_FOLDER2;
	static final String PREPEND_IMAGE_URI = PREPEND_FOLDER2_PATH + PATH_SEP + PREPEND_IMAGE_NAME;

	// REPORT UNIT
	static final String RU_FOLDER = "reportDir1B";
	static final String RU_NAME = "TestRUExportImport1B";
	static final String RU_REPORT_NAME = "TestRUReportExportImport1B";
	static final String RU_IMAGE_NAME = "TestRUImage1B";
	static final String RU_JNDI_NAME = "TestJNDIDS1B";
	static final String RU_FOLDER_PATH = "/reports/" + RU_FOLDER;
	static final String RU_URI = RU_FOLDER_PATH + PATH_SEP + RU_NAME;
	static final String RU_JNDI_URI =  RU_FOLDER_PATH + PATH_SEP + RU_JNDI_NAME;

	// FOLDERS
	static final String FOLDER_FOLDER_NAME1 = "testFolder1C2";
	static final String FOLDER_FOLDER_NAME2 = "testFolder2C2";
	static final String FOLDER_FOLDER_NAME3 = "testFolder3C2";
	static final String FOLDER_FOLDER_NAME4 = "testFolder4C2";
	static final String FOLDER_IMAGE_NAME1 = "testImage1C2";
	static final String FOLDER_IMAGE_NAME3 = "testImage3C2";
	static final String FOLDER_IMAGE_NAME4 = "testImage4C2";
	static final String FOLDER_IMAGE_RU_NAME = "testRUImage1C2";
	static final String FOLDER_JRXML_NAME = "testJrxml1C2";
	static final String FOLDER_RU_NAME = "testReportUnit1C2";

	// INPUT CONTROL
	static final String INPUT_CONTROL_NAME = "numInputTest1D";
	static final String INPUT_CONTROL_DATATYPE_NAME = "numInputDataType1D";
	static final String INPUT_CONTROL_URI = "/datatypes/" + INPUT_CONTROL_NAME;

}
