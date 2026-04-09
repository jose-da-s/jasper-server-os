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

import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJob;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobRepositoryDestination;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobSimpleTrigger;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobSource;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobSummary;
import com.jaspersoft.jasperserver.api.metadata.common.domain.DataType;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Query;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.JndiJdbcReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.api.metadata.security.JasperServerPermission;
import com.jaspersoft.jasperserver.util.test.BaseServiceSetupTestNG;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;


/**
 * @author Lucian Chirita
 * @version $Id$
 */
public class RepositoryMoveAndCopyTestsTestNG extends BaseRepositoryTestTestNG {

    protected final Log m_logger = LogFactory.getLog(RepositoryMoveAndCopyTestsTestNG.class);

	public RepositoryMoveAndCopyTestsTestNG() {
	}

    @Test()
	public void doResourceMoveTest() throws Exception {
		m_logger.info("doResourceMoveTest() called");
		executeInTempFolder(new Callback() {
			public void execute() {
				JndiJdbcReportDataSource ds = (JndiJdbcReportDataSource) newResource(JndiJdbcReportDataSource.class, "ds");
				ds.setJndiName("ds-jndi");
				saveResource(ds);
				
				Query query = (Query) newResource(Query.class, "query");
				query.setLanguage("SQL");
				query.setSql("select");
				query.setDataSourceReference(ds.getURIString());
				saveResource(query);
				
				Folder folder = saveNewFolder("m");
				
				getUnsecureRepositoryService().moveResource(getExecutionContext(), ds.getURIString(), folder.getURIString());
				assertResourceInexistent(ds.getURIString());
				JndiJdbcReportDataSource movedDS = (JndiJdbcReportDataSource) 
						assertResourceExists(JndiJdbcReportDataSource.class, folder.getURIString(), ds.getName());
				assertEquals("ds-jndi", movedDS.getJndiName());
				Query updatedQuery = (Query) assertResourceExists(Query.class, query.getURIString());
				assertExternalReference(updatedQuery.getDataSource(), movedDS.getURIString());
			}
		});
	}

    @Test()
	public void doFolderMoveTest() throws Exception {
		m_logger.info("doFolderMoveTest() called");
		executeInTempFolder(new Callback() {
			public void execute() {
				Folder folder = saveNewFolder("m");
				
				JndiJdbcReportDataSource ds = (JndiJdbcReportDataSource) newResource(JndiJdbcReportDataSource.class, folder, "ds");
				ds.setJndiName("ds-jndi");
				saveResource(ds);
				
				Folder subFolder = saveNewFolder(folder, "s");
				
				Query query = (Query) newResource(Query.class, subFolder, "query");
				query.setLanguage("SQL");
				query.setSql("select");
				query.setDataSourceReference(ds.getURIString());
				saveResource(query);
				
				Folder destinationFolder = saveNewFolder("f");
				
				getUnsecureRepositoryService().moveFolder(getExecutionContext(), folder.getURIString(), destinationFolder.getURIString());
				assertFolderInexistent(folder.getURIString());
				Folder movedFolder = assertFolderExists(destinationFolder, folder.getName());
				Resource movedDS = assertResourceExists(JndiJdbcReportDataSource.class, movedFolder, ds.getName());
				Folder movedSubFolder = assertFolderExists(movedFolder, subFolder.getName());
				Query movedQuery = (Query) assertResourceExists(Query.class, movedSubFolder, query.getName());
				assertExternalReference(movedQuery.getDataSource(), movedDS.getURIString());
			}
		});
	}

    @Test()
	public void doResourceCopyTest() throws Exception {
		m_logger.info("doResourceCopyTest() called");
		executeInTempFolder(new Callback() {
			public void execute() {
				Query query = (Query) newResource(Query.class, "query");
				query.setLanguage("SQL");
				query.setSql("select");
				JndiJdbcReportDataSource ds = (JndiJdbcReportDataSource) newResource(JndiJdbcReportDataSource.class, "ds");
				ds.setJndiName("ds-jndi");
				query.setDataSource(ds);
				saveResource(query);
				
				String copyUri = makeUri("q2");
				getUnsecureRepositoryService().copyResource(getExecutionContext(), query.getURIString(), copyUri);
				assertResourceExists(Query.class, query.getURIString());
				Query queryCopy = (Query) assertResourceExists(Query.class, copyUri);
				assertEquals("SQL", queryCopy.getLanguage());
				assertEquals("select", queryCopy.getSql());
				JndiJdbcReportDataSource copyDS = (JndiJdbcReportDataSource) assertLocalReference(query.getDataSource(), JndiJdbcReportDataSource.class);
				assertEquals("ds-jndi", copyDS.getJndiName());
			}
		});
	}

    @Test()
	public void doFolderCopyTest() throws Exception {
		m_logger.info("doFolderCopyTest() called");
		executeInTempFolder(new Callback() {
			public void execute() {
				setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
				Folder folder = saveNewFolder("m");
				
				JndiJdbcReportDataSource ds = (JndiJdbcReportDataSource) 
						newResource(JndiJdbcReportDataSource.class, folder, "ds");
				ds.setJndiName("ds-jndi");
				saveResource(ds);
				
				Folder subFolder = saveNewFolder(folder, "s");
				
				Query query = (Query) newResource(Query.class, subFolder, "query");
				query.setLanguage("SQL");
				query.setSql("select");
				query.setDataSourceReference(ds.getURIString());
				saveResource(query);
				
				String copyUri = makeUri("m_copy");
				getUnsecureRepositoryService().copyFolder(getExecutionContext(), folder.getURIString(), copyUri);
				assertFolderExists(folder.getURIString());
				assertResourceExists(JndiJdbcReportDataSource.class, folder, ds.getName());
				assertFolderExists(folder, subFolder.getName());
				Query reQuery = (Query) assertResourceExists(Query.class, subFolder, query.getName());
				assertExternalReference(reQuery.getDataSource(), ds.getURIString());
				
				Folder folderCopy = assertFolderExists(copyUri);
				JndiJdbcReportDataSource dsCopy = (JndiJdbcReportDataSource) assertResourceExists(JndiJdbcReportDataSource.class, folderCopy, ds.getName());
				assertEquals("ds-jndi", dsCopy.getJndiName());
				Folder subFolderCopy = assertFolderExists(folderCopy, subFolder.getName());
				Query queryCopy = (Query) assertResourceExists(Query.class, subFolderCopy, query.getName());
				assertEquals("select", queryCopy.getSql());
				assertExternalReference(queryCopy.getDataSource(), dsCopy.getURIString());
			}
		});
	}

    @Test()
	public void doResourceOverwriteSameTypeTest() throws Exception {
		m_logger.info("doResourceOverwriteSameTypeTest() called");
		executeInTempFolder(new Callback() {
			public void execute() {
				JndiJdbcReportDataSource ds = (JndiJdbcReportDataSource) newResource(JndiJdbcReportDataSource.class, "ds");
				ds.setJndiName("ds-jndi");
				saveResource(ds);
				
				Folder folder = saveNewFolder("m");
				
				JndiJdbcReportDataSource ds2 = (JndiJdbcReportDataSource) newResource(JndiJdbcReportDataSource.class, 
						folder, "ds");
				ds2.setJndiName("ds2-jndi");
				saveResource(ds2);
				
				Resource copy = getUnsecureRepositoryService().copyResource(getExecutionContext(), ds.getURIString(), ds2.getURIString());
				assertNotNull(copy);
				assertEquals(folder.getURIString(), copy.getParentFolder());
				assertEquals("ds_1", copy.getName());
				assertTrue(copy instanceof JndiJdbcReportDataSource);
				assertEquals("ds-jndi", ((JndiJdbcReportDataSource) copy).getJndiName());
				
				assertResourceExists(JndiJdbcReportDataSource.class, ds.getURIString());
				JndiJdbcReportDataSource ds2Reloaded = (JndiJdbcReportDataSource) assertResourceExists(
						JndiJdbcReportDataSource.class, ds2.getURIString());
				assertEquals("ds2-jndi", ds2Reloaded.getJndiName());
				
				boolean exception = false;
				try {
					getUnsecureRepositoryService().moveResource(getExecutionContext(), ds.getURIString(), folder.getURIString());
				} catch (JSException e) {
					exception = true;
					assertEquals("jsexception.move.resource.path.already.exists", e.getMessage());
				}
				assertTrue(exception);
				
				JndiJdbcReportDataSource dsReloaded = (JndiJdbcReportDataSource) assertResourceExists(
						JndiJdbcReportDataSource.class, ds.getURIString());
				assertEquals("ds-jndi", dsReloaded.getJndiName());
				
				ds2Reloaded = (JndiJdbcReportDataSource) assertResourceExists(
						JndiJdbcReportDataSource.class, ds2.getURIString());
				assertEquals("ds2-jndi", ds2Reloaded.getJndiName());
			}
		});
	}

    @Test()
	public void doResourceOverwriteOtherTypeTest() throws Exception {
		m_logger.info("doResourceOverwriteOtherTypeTest() called");
		executeInTempFolder(new Callback() {
			public void execute() {
				Folder folder = saveNewFolder("m");
				
				JndiJdbcReportDataSource ds = (JndiJdbcReportDataSource) newResource(
						JndiJdbcReportDataSource.class, folder, "ds");
				ds.setJndiName("ds-jndi");
				saveResource(ds);
				
				DataType dt = (DataType) newResource(DataType.class, "ds");
				dt.setDataTypeType(DataType.TYPE_TEXT);
				saveResource(dt);
				
				Resource copy = getUnsecureRepositoryService().copyResource(getExecutionContext(), ds.getURIString(), dt.getURIString());
				assertNotNull(copy);
				assertEquals(TEMP_FOLDER_URI, copy.getParentFolder());
				assertEquals("ds_1", copy.getName());
				assertTrue(copy instanceof JndiJdbcReportDataSource);
				assertEquals("ds-jndi", ((JndiJdbcReportDataSource) copy).getJndiName());
				
				Resource copy2 = getUnsecureRepositoryService().copyResource(getExecutionContext(), ds.getURIString(), dt.getURIString());
				assertNotNull(copy2);
				assertEquals(TEMP_FOLDER_URI, copy2.getParentFolder());
				assertEquals("ds_2", copy2.getName());
				assertTrue(copy2 instanceof JndiJdbcReportDataSource);
				assertEquals("ds-jndi", ((JndiJdbcReportDataSource) copy2).getJndiName());
				
				assertResourceExists(JndiJdbcReportDataSource.class, ds.getURIString());
				assertResourceExists(DataType.class, dt.getURIString());
				
				boolean exception = false;
				try {
					getUnsecureRepositoryService().moveResource(getExecutionContext(), ds.getURIString(), TEMP_FOLDER_URI);
				} catch (JSException e) {
					exception = true;
					assertEquals("jsexception.move.resource.path.already.exists", e.getMessage());
				}
				assertTrue(exception);
				
				JndiJdbcReportDataSource dsReloaded = (JndiJdbcReportDataSource) assertResourceExists(
						JndiJdbcReportDataSource.class, ds.getURIString());
				assertEquals("ds-jndi", dsReloaded.getJndiName());
				assertResourceExists(DataType.class, dt.getURIString());
			}
		});
	}

    @Test()
	public void doResourceOverwriteFolderTest() throws Exception {
		m_logger.info("doResourceOverwriteFolderTest() called");
		executeInTempFolder(new Callback() {
			public void execute() {
				JndiJdbcReportDataSource ds = (JndiJdbcReportDataSource) newResource(
						JndiJdbcReportDataSource.class, "ds");
				ds.setJndiName("ds-jndi");
				saveResource(ds);
				
				Folder folder = saveNewFolder("m");
				Folder subFolder = saveNewFolder(folder, "ds");
				
				Resource copy = getUnsecureRepositoryService().copyResource(getExecutionContext(), ds.getURIString(), subFolder.getURIString());
				assertNotNull(copy);
				assertEquals(folder.getURIString(), copy.getParentFolder());
				assertEquals("ds_1", copy.getName());
				assertTrue(copy instanceof JndiJdbcReportDataSource);
				assertEquals("ds-jndi", ((JndiJdbcReportDataSource) copy).getJndiName());
				
				Resource copy2 = getUnsecureRepositoryService().copyResource(getExecutionContext(), ds.getURIString(), subFolder.getURIString());
				assertNotNull(copy2);
				assertEquals(folder.getURIString(), copy2.getParentFolder());
				assertEquals("ds_2", copy2.getName());
				assertTrue(copy2 instanceof JndiJdbcReportDataSource);
				assertEquals("ds-jndi", ((JndiJdbcReportDataSource) copy2).getJndiName());
				
				assertResourceExists(JndiJdbcReportDataSource.class, ds.getURIString());
				assertResourceInexistent(folder.getURIString());
				assertFolderExists(folder.getURIString());
				
				boolean exception = false;
				try {
					getUnsecureRepositoryService().moveResource(getExecutionContext(), ds.getURIString(), folder.getURIString());
				} catch (JSException e) {
					exception = true;
					assertEquals("jsexception.move.resource.path.already.exists", e.getMessage());
				}
				assertTrue(exception);
				
				JndiJdbcReportDataSource dsReloaded = (JndiJdbcReportDataSource) assertResourceExists(
						JndiJdbcReportDataSource.class, ds.getURIString());
				assertEquals("ds-jndi", dsReloaded.getJndiName());
				
				assertFolderExists(subFolder.getURIString());
			}
		});
	}
	
    @Test()
	public void doResourcePermissionsTest() throws Exception {
		m_logger.info("doResourcePermissionsTest() called");
		executeInTempFolder(new Callback() {
			public void execute() {
				JndiJdbcReportDataSource ds = (JndiJdbcReportDataSource) newResource(
						JndiJdbcReportDataSource.class, "ds");
				ds.setJndiName("ds-jndi");
				saveResource(ds);
				setUserPermission(JasperServerPermission.READ.getMask(), ds.getURIString(), USER_JOE);
				
				Folder folder = saveNewFolder("m");
				
				String copyURI = makeUri("ds2");
				getUnsecureRepositoryService().copyResource(getExecutionContext(), ds.getURIString(), copyURI);
				assertResourceExists(JndiJdbcReportDataSource.class, ds.getURIString());
				JndiJdbcReportDataSource dsCopy = (JndiJdbcReportDataSource) assertResourceExists(
						JndiJdbcReportDataSource.class, copyURI);
				List copyPermissions = getPermissions(dsCopy);
				assertTrue(copyPermissions == null || copyPermissions.isEmpty());
				
				getUnsecureRepositoryService().moveResource(getExecutionContext(), ds.getURIString(), folder.getURIString());
				assertResourceInexistent(ds.getURIString());
				JndiJdbcReportDataSource movedDS = (JndiJdbcReportDataSource) assertResourceExists(
						JndiJdbcReportDataSource.class, folder, ds.getName());
				assertUserPermission(JasperServerPermission.READ.getMask(), movedDS, USER_JOE);
			}
		});
	}

	// todo: bugs 11992, 11993 are about PostgreSQL unit-tests failing
	// todo: it's tricky setting up to avoid this test under postgres so commenting out temporarily
	public void SKIP_testReportUnit() throws Exception {
		m_logger.info("SKIP_testReportUnit() called");
		executeInTempFolder(new Callback() {
			public void execute() throws IOException {
				JndiJdbcReportDataSource ds = (JndiJdbcReportDataSource) newResource(JndiJdbcReportDataSource.class, "ds");
				ds.setJndiName("ds-jndi");
				saveResource(ds);
				
				ReportUnit report = (ReportUnit) newResource(ReportUnit.class, "report");
				report.setDataSourceReference(ds.getURIString());
				report.setMainReport(newFileResource("main", FileResource.TYPE_JRXML, "/reports/jasper/AllAccounts.jrxml"));
				report.addResource(newFileResource("img", FileResource.TYPE_IMAGE, "/images/jasperreports.png"));
				
				InputControl ic = (InputControl) newResource(InputControl.class, "ic");
				ic.setInputControlType(InputControl.TYPE_SINGLE_VALUE);
				ic.setMandatory(true);
				DataType dt = (DataType) newResource(DataType.class, "dt");
				dt.setDataTypeType(DataType.TYPE_TEXT);
				dt.setMaxLength(new Integer(20));
				ic.setDataType(dt);
				report.addInputControl(ic);
				
				saveResource(report);
				
				ReportJobSource jobSource = new ReportJobSource();
				jobSource.setReportUnitURI(report.getURIString());
				
				Calendar future = Calendar.getInstance();
				future.add(Calendar.YEAR, 10);
				ReportJobSimpleTrigger trigger = new ReportJobSimpleTrigger();
				trigger.setStartDate(future.getTime());
				trigger.setOccurrenceCount(1);
				
				ReportJobRepositoryDestination repositoryDestination = new ReportJobRepositoryDestination();
				repositoryDestination.setFolderURI(TEMP_FOLDER_URI);
				
				ReportJob job = new ReportJob();
				job.setLabel("foo");
				job.setSource(jobSource);
				job.setTrigger(trigger);
				job.setBaseOutputFilename("foo");
				job.addOutputFormat(ReportJob.OUTPUT_FORMAT_PDF);
				job.setContentRepositoryDestination(repositoryDestination);
				getReportSchedulingService().scheduleJob(getExecutionContext(), job);
				
				Folder folder = saveNewFolder("m");
				
				String copyURI = folder.getURIString() + Folder.SEPARATOR + "report2";
				getUnsecureRepositoryService().copyResource(getExecutionContext(), report.getURIString(), copyURI);
				assertResourceExists(ReportUnit.class, report.getURIString());
				ReportUnit reportCopy = (ReportUnit) assertResourceExists(ReportUnit.class, copyURI);
				assertReportUnit(reportCopy);
				List copyJobs = getReportSchedulingService().getScheduledJobSummaries(
                        getExecutionContext(), reportCopy.getURIString());
				assertTrue(copyJobs == null || copyJobs.isEmpty());
				
				getUnsecureRepositoryService().moveResource(getExecutionContext(), report.getURIString(), folder.getURIString());
				assertResourceInexistent(report.getURIString());
				assertResourceExists(JndiJdbcReportDataSource.class, ds.getURIString());
				
				ReportUnit movedReport = (ReportUnit) assertResourceExists(ReportUnit.class, folder, report.getName());
				assertReportUnit(movedReport);
				
				List movedJobs = getReportSchedulingService().getScheduledJobSummaries(
                        getExecutionContext(), movedReport.getURIString());
				assertNotNull(movedJobs);
				assertEquals(1, movedJobs.size());
				ReportJobSummary movedJob = (ReportJobSummary) movedJobs.get(0);
				assertEquals("foo", movedJob.getLabel());
				assertEquals(movedReport.getURIString(), movedJob.getReportUnitURI());
			}
			
			protected void assertReportUnit(ReportUnit report) throws IOException {
				assertExternalReference(report.getDataSource(), makeUri("ds"));
				FileResource main = (FileResource) assertLocalReference(report.getMainReport(), FileResource.class);
				assertEquals(FileResource.TYPE_JRXML, main.getFileType());
				assertFileData(main, "/reports/jasper/AllAccounts.jrxml");
				
				List resources = report.getResources();
				assertNotNull(resources);
				assertEquals(1, resources.size());
				FileResource img = (FileResource) assertLocalReference(
						(ResourceReference) resources.get(0), FileResource.class);
				assertEquals(FileResource.TYPE_IMAGE, img.getFileType());
				assertFileData(img, "/images/jasperreports.png");
				
				List inputControls = report.getInputControls();
				assertNotNull(inputControls);
				assertEquals(1, inputControls.size());
				InputControl movedIC = (InputControl) assertLocalReference(
						(ResourceReference) inputControls.get(0), InputControl.class);
				assertEquals(InputControl.TYPE_SINGLE_VALUE, movedIC.getInputControlType());
				assertTrue(movedIC.isMandatory());
				DataType movedDT = (DataType) assertLocalReference(movedIC.getDataType(), DataType.class);
				assertEquals(DataType.TYPE_TEXT, movedDT.getDataTypeType());
				assertEquals(new Integer(20), movedDT.getMaxLength());
			}
		});
	}

    @Test()
	public void doFolderOverwriteResourceTest() throws Exception {
		m_logger.info("doFolderOverwriteResourceTest() called");
		executeInTempFolder(new Callback() {
			public void execute() {
				Folder folder = saveNewFolder("m");
				Folder destFolder = saveNewFolder("s");
				
				JndiJdbcReportDataSource ds = (JndiJdbcReportDataSource) newResource(
						JndiJdbcReportDataSource.class, destFolder, "m");
				ds.setJndiName("ds-jndi");
				saveResource(ds);

				Folder copy = getUnsecureRepositoryService().copyFolder(getExecutionContext(), folder.getURIString(), ds.getURIString());
				assertNotNull(copy);
				assertEquals(destFolder.getURIString(), copy.getParentFolder());
				assertEquals("m_1", copy.getName());

				assertFolderExists(folder.getURIString());
				assertResourceExists(JndiJdbcReportDataSource.class, destFolder, "m");

				boolean exception = false;
				try {
					getUnsecureRepositoryService().moveFolder(getExecutionContext(), folder.getURIString(), destFolder.getURIString());
				} catch (JSException e) {
					exception = true;
					assertEquals("jsexception.move.folder.path.already.exists", e.getMessage());
				}
				assertTrue(exception);
				
				assertFolderExists(folder.getURIString());
				assertResourceExists(JndiJdbcReportDataSource.class, destFolder, "m");
			}
		});
	}

    @Test()
	public void doFolderOverwriteTest() throws Exception {
		m_logger.info("doFolderOverwriteTest() called");
		executeInTempFolder(new Callback() {
			public void execute() {
				Folder folder = saveNewFolder("m");
				Folder destFolder = saveNewFolder("s");
				saveNewFolder(destFolder, "m");

				Folder copy = getUnsecureRepositoryService().copyFolder(getExecutionContext(), folder.getURIString(), destFolder.getURIString());
				assertNotNull(copy);
				assertEquals(TEMP_FOLDER_URI, copy.getParentFolder());
				assertEquals("s_1", copy.getName());

				Folder copy2 = getUnsecureRepositoryService().copyFolder(getExecutionContext(), folder.getURIString(), destFolder.getURIString());
				assertNotNull(copy2);
				assertEquals(TEMP_FOLDER_URI, copy2.getParentFolder());
				assertEquals("s_2", copy2.getName());

				assertFolderExists(folder.getURIString());
				assertFolderExists(destFolder, "m");

				boolean exception = false;
				try {
					getUnsecureRepositoryService().moveFolder(getExecutionContext(), folder.getURIString(), destFolder.getURIString());
				} catch (JSException e) {
					exception = true;
					assertEquals("jsexception.move.folder.path.already.exists", e.getMessage());
				}
				assertTrue(exception);
				
				assertFolderExists(folder.getURIString());
				assertFolderExists(destFolder, "m");
			}
		});
	}

    @Test()
	public void doFolderMoveAncestorTest() throws Exception {
		m_logger.info("doFolderMoveAncestorTest() called");
		executeInTempFolder(new Callback() {
			public void execute() {
				Folder folder = saveNewFolder("m");
				Folder destFolder = saveNewFolder(folder, "s");

				boolean exception = false;
				try {
					getUnsecureRepositoryService().moveFolder(getExecutionContext(), folder.getURIString(), destFolder.getURIString());
				} catch (JSException e) {
					exception = true;
					assertEquals("jsexception.move.folder.source.uri.ancestor.destination", e.getMessage());
				}
				assertTrue(exception);
				
				assertFolderExists(folder.getURIString());
				assertFolderExists(destFolder.getURIString());
			}
		});
	}

    @Test()
	public void doFolderMoveSameTest() throws Exception {
		m_logger.info("doFolderMoveSameTest() called");
		executeInTempFolder(new Callback() {
			public void execute() {
				Folder folder = saveNewFolder("m");

				boolean exception = false;
				try {
					getUnsecureRepositoryService().moveFolder(getExecutionContext(), folder.getURIString(), TEMP_FOLDER_URI);
				} catch (JSException e) {
					exception = true;
					assertEquals("jsexception.move.folder.to.same.folder", e.getMessage());
				}
				assertTrue(exception);
				
				assertFolderExists(folder.getURIString());
			}
		});
	}
	
    @Test()
	public void doFolderPermissionsTest() throws Exception {
		m_logger.info("doFolderPermissionsTest() called");
		executeInTempFolder(new Callback() {
			public void execute() {
				setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
				Folder folder = saveNewFolder("f");
				setUserPermission(JasperServerPermission.ADMINISTRATION.getMask(), folder.getURIString(), USER_JOE);
				
				JndiJdbcReportDataSource ds = (JndiJdbcReportDataSource) newResource(
						JndiJdbcReportDataSource.class, folder, "ds");
				ds.setJndiName("ds-jndi");
				saveResource(ds);
				setUserPermission(JasperServerPermission.READ_WRITE.getMask(), ds.getURIString(), USER_JOE);
				
				String copyURI = makeUri("f2");
				getUnsecureRepositoryService().copyFolder(getExecutionContext(), folder.getURIString(), copyURI);
				assertFolderExists(folder.getURIString());
				Folder folderCopy = assertFolderExists(copyURI);
				List folderCopyPermissions = getPermissions(folderCopy);
				assertTrue(folderCopyPermissions == null || folderCopyPermissions.isEmpty());
				JndiJdbcReportDataSource dsCopy = (JndiJdbcReportDataSource) assertResourceExists(
						JndiJdbcReportDataSource.class, folderCopy, ds.getName());
				List dsCopyPermissions = getPermissions(dsCopy);
				assertTrue(dsCopyPermissions == null || dsCopyPermissions.isEmpty());
				
				Folder destFolder = saveNewFolder("m");
				getUnsecureRepositoryService().moveFolder(getExecutionContext(), folder.getURIString(), destFolder.getURIString());
				assertFolderInexistent(folder.getURIString());
				Folder movedFolder = assertFolderExists(destFolder, folder.getName());
				assertUserPermission(JasperServerPermission.ADMINISTRATION.getMask(), movedFolder, USER_JOE);
				JndiJdbcReportDataSource movedDs = (JndiJdbcReportDataSource) assertResourceExists(
						JndiJdbcReportDataSource.class, movedFolder, ds.getName());
				assertUserPermission(JasperServerPermission.READ_WRITE.getMask(), movedDs, USER_JOE);
			}
		});
	}

    @Test()
	public void doFolderCopyAncestorTest() throws Exception {
		m_logger.info("doFolderCopyAncestorTest() called");
		executeInTempFolder(new Callback() {
			public void execute() {
				setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
				Folder folder = saveNewFolder("m");
				
				JndiJdbcReportDataSource ds = (JndiJdbcReportDataSource) 
						newResource(JndiJdbcReportDataSource.class, "ds");
				ds.setJndiName("ds-jndi");
				saveResource(ds);
				
				Folder subFolder = saveNewFolder(folder, "s");
				
				Query query = (Query) newResource(Query.class, subFolder, "query");
				query.setLanguage("SQL");
				query.setSql("select");
				query.setDataSourceReference(ds.getURIString());
				saveResource(query);
				
				String copyUri = subFolder.getURIString() + Folder.SEPARATOR + "m2";
				getUnsecureRepositoryService().copyFolder(getExecutionContext(), folder.getURIString(), copyUri);
				
				assertFolderExists(folder.getURIString());
				assertResourceExists(JndiJdbcReportDataSource.class, ds.getURIString());
				assertFolderExists(folder, subFolder.getName());
				Query reQuery = (Query) assertResourceExists(Query.class, subFolder, query.getName());
				assertExternalReference(reQuery.getDataSource(), ds.getURIString());
				
				Folder folderCopy = assertFolderExists(copyUri);
				assertFolderChildren(folderCopy, 1, 0);
				Folder subFolderCopy = assertFolderExists(folderCopy, subFolder.getName());
				assertFolderChildren(subFolderCopy, 0, 1);
				Query queryCopy = (Query) assertResourceExists(Query.class, subFolderCopy, query.getName());
				assertEquals("select", queryCopy.getSql());
				assertExternalReference(queryCopy.getDataSource(), ds.getURIString());
			}
		});
	}
	
}
