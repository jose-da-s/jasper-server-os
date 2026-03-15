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

/**
 * @author tkavanagh
 * @version $id $
 */

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.engine.scheduling.ReportSchedulingInternalService;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJob;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobMailNotification;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobRepositoryDestination;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobSimpleTrigger;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobSource;
import com.jaspersoft.jasperserver.api.engine.scheduling.service.ReportSchedulingService;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ContentResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.ContentResourceImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.FolderImpl;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.JndiJdbcReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.export.Parameters;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

public class ExportCoreObjectsTestTestNG extends BaseExportTestCaseTestNG {

	private static final Log m_logger = LogFactory.getLog(ExportCoreObjectsTestTestNG.class);
	
	private static final String REPORT_SCHEDULING_SERVICE_BEAN_NAME = "reportSchedulingService";
	private static final String REPORT_SCHEDULING_INTERNAL_SERVICE_BEAN_NAME = "reportSchedulingInternalService";

	static final String PATH_SEP = "/";
	static final String LABEL = "_label";
	static final String DESC = "_description";
	static final String DESC_TEXT = " for export-import testing";

	static final String FOLDER_NAME = "exportTest03";
	static final String IMAGE_NAME = "ExportTestImage03";


    private ReportSchedulingService m_sched;
    private ReportSchedulingInternalService m_reportSchedulerInternal;

	private ExecutionContext context;

	private long mJobId;

	private RepositoryService m_repo;

    public ExportCoreObjectsTestTestNG(){
        m_logger.info("ExportCoreObjectsTestTestNG => constructor() called");
    }

    @BeforeClass()
	public void onSetUp() throws Exception {
        m_logger.info("ExportCoreObjectsTestTestNG => onSetUp() called");
		super.onSetUp();

		m_repo = (RepositoryService) getBean("repositoryService");

	    m_sched = (ReportSchedulingService) getBean(REPORT_SCHEDULING_SERVICE_BEAN_NAME);
	    m_reportSchedulerInternal = (ReportSchedulingInternalService) getBean(REPORT_SCHEDULING_INTERNAL_SERVICE_BEAN_NAME);

	}

    @AfterClass()
	public void onTearDown() throws Exception {
        m_logger.info("ExportCoreObjectsTestTestNG => onTearDown() called");
		super.onTearDown();
	}

	/*
	 * doCoreObject_SetOneTest
	 * 		FileResource
	 * 		ContentResource
	 */
    @Test()
	public void doCoreObject_SetOneTest() {
        m_logger.info("ExportCoreObjectsTestTestNG => doCoreObject_SetOneTest() called");
		createResource();
		
		String exportDir = createExportDir();
		Parameters exportParams = createParameters()
			.addParameterValue(PARAM_EXPORT_DIR, exportDir)
			.addParameterValue(PARAM_EXPORT_URIS, PATH_SEP + FOLDER_NAME);
		performExport(exportParams);

		deleteResource();

		Parameters importParams = createParameters()
			.addParameterValue(PARAM_IMPORT_DIR, exportDir);
		performImport(importParams);
		
		deleteResource();
	}

	static final String FOLDER_NAME_SINGLE = "exportTestSingle_01";
	static final String CONTENT_NAME_PDF_SINGLE = "ContentResourcePdf_01";

	/*
	 * doContentResourcePdf_SingleTest
	 */
    @Test(dependsOnMethods = "doCoreObject_SetOneTest")
	public void doContentResourcePdf_SingleTest() {
        m_logger.info("ExportCoreObjectsTestTestNG => doContentResourcePdf_SingleTest() called");
		createResourceSingle();
		
		String exportDir = createExportDir();
		Parameters exportParams = createParameters()
			.addParameterValue(PARAM_EXPORT_DIR, exportDir)
			.addParameterValue(PARAM_EXPORT_URIS, PATH_SEP + FOLDER_NAME_SINGLE + PATH_SEP + CONTENT_NAME_PDF_SINGLE);
		performExport(exportParams);

		deleteResourceSingle();

		Parameters importParams = createParameters()
			.addParameterValue(PARAM_IMPORT_DIR, exportDir);
		performImport(importParams);
		
		deleteResourceSingle();
	}

	private void createResourceSingle() {
        Folder folder = new FolderImpl();
		folder.setName(FOLDER_NAME_SINGLE);
		folder.setLabel(FOLDER_NAME_SINGLE + LABEL);
		folder.setDescription(FOLDER_NAME_SINGLE + DESC);
		folder.setParentFolder("/");
		m_repo.saveFolder(null, folder);

		ContentResource cont1 = new ContentResourceImpl();
		cont1.setName(CONTENT_NAME_PDF_SINGLE);
		cont1.setLabel(CONTENT_NAME_PDF_SINGLE);
		cont1.setParentFolder(folder);
		InputStream file  = getClass().getResourceAsStream("/FontsReport.pdf");
		cont1.readData(file);
		cont1.setFileType(ContentResource.TYPE_PDF);
		m_repo.saveResource(null, cont1);
	}

	private void deleteResourceSingle() {

		m_repo.deleteResource(null, PATH_SEP + FOLDER_NAME_SINGLE + PATH_SEP + CONTENT_NAME_PDF_SINGLE);
		m_repo.deleteFolder(null, PATH_SEP + FOLDER_NAME_SINGLE);
	}

	static final String FOLDER_NAME_HTML_SINGLE = "exportTestSingle_05";
	static final String CONTENT_NAME_HTML_SINGLE = "ContentResourceHtml_05";

	/*
	 * doContentResourceHtml_SingleTest
	 */
    @Test(dependsOnMethods = "doContentResourcePdf_SingleTest")
	public void doContentResourceHtml_SingleTest() {
        m_logger.info("ExportCoreObjectsTestTestNG => doContentResourceHtml_SingleTest() called");
		createResourceSingleHtml();

		String exportDir = createExportDir();
		Parameters exportParams = createParameters()
			.addParameterValue(PARAM_EXPORT_DIR, exportDir)
			.addParameterValue(PARAM_EXPORT_URIS, PATH_SEP + FOLDER_NAME_HTML_SINGLE + PATH_SEP + CONTENT_NAME_HTML_SINGLE);
		performExport(exportParams);

		deleteResourceSingleHtml();

		Parameters importParams = createParameters()
			.addParameterValue(PARAM_IMPORT_DIR, exportDir);
		performImport(importParams);

		ContentResource cr1 = (ContentResource) m_repo.getResource(null,
				PATH_SEP + FOLDER_NAME_HTML_SINGLE + PATH_SEP + CONTENT_NAME_HTML_SINGLE);

		assertNotNull(cr1);
		assertEquals(cr1.getName(), CONTENT_NAME_HTML_SINGLE);

		deleteResourceSingleHtml();
	}

	private void createResourceSingleHtml() {

		Folder folder = new FolderImpl();
		folder.setName(FOLDER_NAME_HTML_SINGLE);
		folder.setLabel(FOLDER_NAME_HTML_SINGLE + LABEL);
		folder.setDescription(FOLDER_NAME_HTML_SINGLE + DESC);
		folder.setParentFolder("/");
		m_repo.saveFolder(null, folder);

		ContentResource cont1 = new ContentResourceImpl();
		cont1.setName(CONTENT_NAME_HTML_SINGLE);
		cont1.setLabel(CONTENT_NAME_HTML_SINGLE);
		cont1.setParentFolder(folder);
		InputStream file  = getClass().getResourceAsStream("/FirstJasper.html");
		cont1.readData(file);
		cont1.setFileType(ContentResource.TYPE_HTML);

		/*
		 * Add images to the HTML ContentResource
		 */
		ArrayList imagesList = new ArrayList();

		ContentResource image = new ContentResourceImpl();
		image.setName("img_0_0_1");
		image.setLabel("img_0_0_1");
		file  = getClass().getResourceAsStream("/FirstJasper.html_files/img_0_0_1");
		image.readData(file);
		image.setFileType(ContentResource.TYPE_HTML);
		imagesList.add(image);

		image = new ContentResourceImpl();
		image.setName("img_0_7_125");
		image.setLabel("img_0_7_125");
		file  = getClass().getResourceAsStream("/FirstJasper.html_files/img_0_7_125");
		image.readData(file);
		image.setFileType(ContentResource.TYPE_HTML);
		imagesList.add(image);

		image = new ContentResourceImpl();
		image.setName("px");
		image.setLabel("px");
		file  = getClass().getResourceAsStream("/FirstJasper.html_files/px");
		image.readData(file);
		image.setFileType(ContentResource.TYPE_HTML);
		imagesList.add(image);

		cont1.setResources(imagesList);

		m_repo.saveResource(null, cont1);
	}

	private void deleteResourceSingleHtml() {

		m_repo.deleteResource(null, PATH_SEP + FOLDER_NAME_HTML_SINGLE + PATH_SEP + CONTENT_NAME_HTML_SINGLE);
		m_repo.deleteFolder(null, PATH_SEP + FOLDER_NAME_HTML_SINGLE);
	}


	private void createResource() {
		Folder folder = new FolderImpl();
		folder.setName(FOLDER_NAME);
		folder.setLabel(FOLDER_NAME + LABEL);
		folder.setDescription(FOLDER_NAME + DESC);
		folder.setParentFolder("/");
		m_repo.saveFolder(null, folder);

		/*
		 * FileResource
		 */
		FileResource img1 = (FileResource) m_repo.newResource(null, FileResource.class);
		img1.setFileType(FileResource.TYPE_IMAGE);
		img1.readData(getClass().getResourceAsStream("/images/jasperreports.png"));
		img1.setName(IMAGE_NAME);
		img1.setLabel(IMAGE_NAME + LABEL);
		img1.setDescription(IMAGE_NAME + DESC);
		img1.setParentFolder(folder);
		m_repo.saveResource(null, img1);


		/*
		 * ContentResource
		 */
		ContentResource cont1 = new ContentResourceImpl();
		cont1.setName("PdfTestFile");
		cont1.setLabel("Pdf test file");
		cont1.setParentFolder(folder);
		InputStream file  = getClass().getResourceAsStream("/FontsReport.pdf");
		cont1.readData(file);
		cont1.setFileType(ContentResource.TYPE_PDF);
		m_repo.saveResource(null, cont1);

		ContentResource htmlFile = new ContentResourceImpl();
		htmlFile.setName("HtmlTestFile");
		htmlFile.setLabel("HTML test file with images");
		htmlFile.setParentFolder(folder);
		file  = getClass().getResourceAsStream("/FirstJasper.html");
		htmlFile.readData(file);
		htmlFile.setFileType(ContentResource.TYPE_HTML);

		m_repo.saveResource(null, htmlFile);
	}

	private void deleteResource() {

		m_repo.deleteFolder(null, PATH_SEP + FOLDER_NAME);
	}

	/*
	 * doContentResourceJobSchedule_SingleTest
	 */
    //@Test(dependsOnMethods = "doContentResourceHtml_SingleTest")
	public void doContentResourceJobSchedule_SingleTest() throws Exception {
        m_logger.info("ExportCoreObjectsTestTestNG => doContentResourceJobSchedule_SingleTest() called");
		deleteSingleJob();
		createSingleJob();

		String exportDir = createExportDir();
		Parameters exportParams = createParameters()
			.addParameterValue(PARAM_EXPORT_DIR, exportDir)
			.addParameterValue(PARAM_EXPORT_URIS, RU_FOLDER_PATH)
			.addParameterValue(PARAM_EXPORT_REPORT_JOB_URIS, RU_URI);
		performExport(exportParams);

		deleteSingleJob();

		Parameters importParams = createParameters()
			.addParameterValue(PARAM_IMPORT_DIR, exportDir);
		performImport(importParams);

		deleteSingleJob();
	}

	static final String RU_FOLDER = "exportTestJobSingle_BBB";
	static final String RU_NAME = "TestRUForJob";
	static final String RU_REPORT_NAME = "TestRUReport";
	static final String RU_IMAGE_NAME = "TestRUImage";
	static final String RU_JNDI_NAME = "TestJNDIDS";
	static final String RU_FOLDER_PATH = "/" + RU_FOLDER;
	static final String RU_URI = RU_FOLDER_PATH + PATH_SEP + RU_NAME;
	static final String RU_JNDI_URI =  RU_FOLDER_PATH + PATH_SEP + RU_JNDI_NAME;
	static final String DEST_FOLDER = "scheduled";

	private void createSingleJob() throws Exception {

        // create a folder
		Folder folder = new FolderImpl();
		folder.setName(RU_FOLDER);
		folder.setLabel(RU_FOLDER + LABEL);
		folder.setDescription(RU_FOLDER + DESC);
		folder.setParentFolder("/");
		m_repo.saveFolder(null, folder);

        // create the destination folder
		Folder destFolder = new FolderImpl();
		destFolder.setName(DEST_FOLDER);
		destFolder.setLabel(DEST_FOLDER + LABEL);
		destFolder.setDescription(DEST_FOLDER + DESC);
		destFolder.setParentFolder(folder);
		m_repo.saveFolder(null, destFolder);

        // create report unit
		ReportUnit unit = (ReportUnit) m_repo.newResource(null, ReportUnit.class);
		unit.setName(RU_NAME);
		unit.setLabel(RU_NAME + LABEL);
		unit.setDescription("Report Unit" + DESC_TEXT);
		unit.setParentFolder(folder);

		// setup the datasource
		JndiJdbcReportDataSource datasource;
		if (m_repo.getResource(context, RU_JNDI_URI)== null) {
			datasource = (JndiJdbcReportDataSource) m_repo.newResource(null, JndiJdbcReportDataSource.class);
			datasource.setName(RU_JNDI_NAME);
			datasource.setLabel(RU_JNDI_NAME + LABEL);
			datasource.setDescription("jndi data source" + DESC_TEXT);
			datasource.setJndiName(getJdbcProps().getProperty("test.jndi"));
			datasource.setParentFolder(folder);
			m_repo.saveResource(null, datasource);
		} else {
			datasource = (JndiJdbcReportDataSource) m_repo.getResource(context, RU_JNDI_URI);
		}
		unit.setDataSourceReference(RU_JNDI_URI);

		// setup the mainReport jrxml
		FileResource reportRes = (FileResource) m_repo.newResource(null, FileResource.class);
		reportRes.setFileType(FileResource.TYPE_JRXML);
		reportRes.setName(RU_REPORT_NAME);
		reportRes.setLabel(RU_REPORT_NAME + LABEL);
		reportRes.setDescription("Report" + DESC_TEXT);
		InputStream jrxml = getClass().getResourceAsStream("/reports/jasper/TestReportExportImport01.jrxml");
		reportRes.readData(jrxml);
		unit.setMainReport(reportRes);

		// setup an image
		FileResource image1 = (FileResource) m_repo.newResource(null, FileResource.class);
		image1.setFileType(FileResource.TYPE_IMAGE);
		image1.readData(getClass().getResourceAsStream("/images/jasperreports.png"));
		image1.setName(RU_IMAGE_NAME);
		image1.setLabel(RU_IMAGE_NAME + LABEL);
		image1.setDescription("Image" + DESC_TEXT);
		unit.addResource(image1);

		m_repo.saveResource(null, unit);

		// create Job
		ReportJobSource source = new ReportJobSource();
		source.setReportUnitURI(RU_URI);
		Map params = new HashMap();
		params.put("param1", new Integer(5));
		params.put("param2", "value2");
		source.setParametersMap(params);

		Date startDate = new Date();
		ReportJobSimpleTrigger trigger = new ReportJobSimpleTrigger();
		trigger.setStartDate(startDate);
		trigger.setOccurrenceCount(20);
		trigger.setRecurrenceInterval(10);
		trigger.setRecurrenceIntervalUnit(ReportJobSimpleTrigger.INTERVAL_DAY);

		ReportJobRepositoryDestination repositoryDestination = new ReportJobRepositoryDestination();
		repositoryDestination.setFolderURI(PATH_SEP + RU_FOLDER + PATH_SEP + DEST_FOLDER);

		ReportJobMailNotification mailNotification = new ReportJobMailNotification();
		mailNotification.addTo("john@smith.com");
		mailNotification.setSubject("Scheduled report");
		mailNotification.setMessageText("Executed report");

		ReportJob job = new ReportJob();
		job.setLabel("JobFor_" + RU_NAME + "_Label");
		job.setDescription("Description");
		job.setSource(source);
		job.setTrigger(trigger);
		job.setBaseOutputFilename("foo");
		job.addOutputFormat(ReportJob.OUTPUT_FORMAT_PDF);
		job.addOutputFormat(ReportJob.OUTPUT_FORMAT_RTF);
		job.setContentRepositoryDestination(repositoryDestination);
		job.setMailNotification(mailNotification);

		job = m_sched.scheduleJob(null, job);
		mJobId = job.getId();
	}

	private void deleteSingleJob() {

		Folder folder = m_repo.getFolder(null, PATH_SEP + RU_FOLDER);
		if (folder != null) {
			m_repo.deleteResource(null, RU_URI);
			m_repo.deleteFolder(null, PATH_SEP + RU_FOLDER);
	
			m_sched.removeScheduledJob(null, mJobId);
		}
	}

}
