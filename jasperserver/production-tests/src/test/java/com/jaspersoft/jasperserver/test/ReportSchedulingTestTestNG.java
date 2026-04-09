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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.LogEvent;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.engine.common.service.LoggingService;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.FTPInfo;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJob;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobAlert;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobCalendarTrigger;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobIdHolder;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobMailNotification;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobRuntimeInformation;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobSimpleTrigger;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobSummary;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobSource;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobRepositoryDestination;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.reportjobmodel.FTPInfoModel;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.reportjobmodel.ReportJobModel;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.reportjobmodel.ReportJobCalendarTriggerModel;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.reportjobmodel.ReportJobMailNotificationModel;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.reportjobmodel.ReportJobSimpleTriggerModel;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.reportjobmodel.ReportJobRepositoryDestinationModel;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.reportjobmodel.ReportJobSourceModel;
import com.jaspersoft.jasperserver.api.engine.scheduling.quartz.ReportExecutionJob;
import com.jaspersoft.jasperserver.api.engine.scheduling.service.ReportJobNotFoundException;
import com.jaspersoft.jasperserver.api.engine.scheduling.service.ReportJobsPersistenceService;
import com.jaspersoft.jasperserver.api.engine.scheduling.service.ReportSchedulingService;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.FolderImpl;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.persistent.RepoFolder;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.util.test.BaseServiceSetupTestNG;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import jakarta.annotation.Resource;

import static org.testng.AssertJUnit.*;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class ReportSchedulingTestTestNG extends BaseServiceSetupTestNG
{
    protected static Log m_logger = LogFactory.getLog(ReportSchedulingTestTestNG.class);

	private ReportJobsPersistenceService m_reportJobsPersistenceService;
  private ReportSchedulingService m_reportSchedulingService;
	private ExecutionContext m_executionContext;
    private LoggingService m_loggingService;

    long eventId = -1;

	public LoggingService getLoggingService() {
		return m_loggingService;
	}

    @jakarta.annotation.Resource(name = "loggingService")
	public void setLoggingService(LoggingService loggingService) {
        m_logger.info("setLoggingService() called");
		this.m_loggingService = loggingService;
	}

	public ReportJobsPersistenceService getReportJobsPersistenceService() {
		return m_reportJobsPersistenceService;
	}

    @jakarta.annotation.Resource(name = "securedReportJobsPersistenceService")
	public void setReportJobsPersistenceService(
		ReportJobsPersistenceService reportJobsPersistenceService) {
		m_logger.info("setReportJobsPersistenceService() called");
		this.m_reportJobsPersistenceService = reportJobsPersistenceService;
	}

  public ReportSchedulingService getReportSchedulingService() {
    return m_reportSchedulingService;
  }

  @Resource(name = "reportSchedulingService")
  public void setReportSchedulingService(
          ReportSchedulingService s) {
    m_logger.info("setReportSchedulingService() called");
    this.m_reportSchedulingService = s;
  }


	public ReportSchedulingTestTestNG(){
        m_logger.info("ReportSchedulingTestTestNG => constructor() called");
	}

    @BeforeClass()
	protected void onSetUp() throws Exception {
        m_logger.info("ReportSchedulingTestTestNG => onSetUp() called");
		m_executionContext = new ExecutionContextImpl();
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
	}

    @AfterClass()
	protected void onTearDown() {
        m_logger.info("ReportSchedulingTestTestNG => onTearDown() called");
        if (eventId >= 0) {
            m_loggingService.delete(null, new long[] { eventId });
        }
	}

    /**
     *  doPersistenceTest
     */
  @Test()
	public void doPersistenceTest() {
        m_logger.info("ReportSchedulingTestTestNG => doPersistenceTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);

        // REPORT JOB 1
 
		ReportJobSource source = new ReportJobSource();
		source.setReportUnitURI("/test/reportURI");
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
		repositoryDestination.setFolderURI("/test/scheduled");
		repositoryDestination.setOutputDescription("report output");
		repositoryDestination.setSequentialFilenames(true);
		repositoryDestination.setTimestampPattern("yyyyMMdd");
        repositoryDestination.setDefaultReportOutputFolderURI("/default/report_output/folder");
        repositoryDestination.setUsingDefaultReportOutputFolderURI(true);

		ReportJobMailNotification mailNotification = new ReportJobMailNotification();
		mailNotification.addTo("john@smith.com");
		mailNotification.setSubject("Scheduled report");
		mailNotification.setMessageText("Executed report");

		
		Folder folder = new FolderImpl();
		folder.setURIString("/test");
		folder.setName("test");
		folder.setLabel("test");
		folder.setDescription("test");
		
		getRepositoryService().saveFolder(m_executionContext, folder);
		
		
		ReportUnit report = (ReportUnit) getRepositoryService().newResource(m_executionContext, ReportUnit.class);
        report.setURIString("/test/reportURI");
        report.setLabel("reportURI");
        report.setName("reportURI");
        report.setDescription("reportURI");
        
        getRepositoryService().saveResource(m_executionContext, report);
		
		ReportJob job_01 = new ReportJob();
		job_01.setScheduledResource(new ResourceReference(report.getURIString()));
		job_01.setLabel("foo");
		job_01.setDescription("bar");
		job_01.setSource(source);
		job_01.setTrigger(trigger);
		job_01.setBaseOutputFilename("foo");
		job_01.addOutputFormat(ReportJob.OUTPUT_FORMAT_PDF);
		job_01.addOutputFormat(ReportJob.OUTPUT_FORMAT_RTF);
		job_01.setContentRepositoryDestination(repositoryDestination);
		job_01.setMailNotification(mailNotification);

		job_01 = m_reportJobsPersistenceService.saveJob(m_executionContext, job_01);
    
    m_logger.info("saved job_01 " + job_01.getId() + " has version=" + job_01.getVersion());

		assertNotNull(job_01);
		long jobId_01 = job_01.getId();
        String userName = job_01.getUsername();

        //  Report Job 02

        source = new ReportJobSource();
		source.setReportUnitURI("/test/A_ReportURI");
		params = new HashMap();
		params.put("param1", new Integer(5));
		params.put("param2", "value2");
		source.setParametersMap(params);

		startDate = new Date();
		ReportJobCalendarTrigger trigger2 = new ReportJobCalendarTrigger();
        trigger2.setMinutes("0");
        trigger2.setHours("0");
        trigger2.setDaysTypeCode(trigger2.DAYS_TYPE_ALL);
        TreeSet months = new TreeSet();
        months.add(new Byte((byte)1));
        months.add(new Byte((byte)2));
        months.add(new Byte((byte)3));
        trigger2.setMonthDays("");
        trigger2.setMonths(months);
        trigger2.setTimezone("America/Los_Angeles");
        trigger2.setStartType(trigger2.START_TYPE_NOW);

        repositoryDestination = new ReportJobRepositoryDestination();
		repositoryDestination.setFolderURI("/test/scheduled");
		repositoryDestination.setOutputDescription("report output");
		repositoryDestination.setSequentialFilenames(false);
		repositoryDestination.setTimestampPattern("yyyyMMdd");
        repositoryDestination.setSaveToRepository(false);
        repositoryDestination.setOutputLocalFolder("c:/tmp");
        FTPInfo ftpInfo = new FTPInfo();
        ftpInfo.setUserName("JohnSmith");
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("PORT", "27");
        ftpInfo.setPropertiesMap(map);
        repositoryDestination.setOutputFTPInfo(ftpInfo);

		mailNotification = new ReportJobMailNotification();
		mailNotification.addTo("john@smith.com");
        mailNotification.addTo("peter@pan.com");
		mailNotification.setSubject("Scheduled report");
		mailNotification.setMessageText("Executed report");
        mailNotification.setSkipNotificationWhenJobFails(true);

        ReportJobAlert alert = new ReportJobAlert();
        alert.setRecipient(ReportJobAlert.Recipient.ADMIN);
        alert.setMessageText("CUSTOMIZE MESSAGE");
        alert.setJobState(ReportJobAlert.JobState.FAIL_ONLY);
        ArrayList<String> to_Addresses = new ArrayList<String>();
        to_Addresses.add("peterpan@jaspersoft.com");
        to_Addresses.add("peter.pan@gmail.com");
        alert.setToAddresses(to_Addresses);

		ReportUnit report2 = (ReportUnit) getRepositoryService().newResource(m_executionContext, ReportUnit.class);
        report2.setURIString("/test/reportURI");

        ReportJob job_02 = new ReportJob();
		job_02.setScheduledResource(new ResourceReference(report2.getURIString()));
		job_02.setLabel("A_ReportJob_2");
		job_02.setDescription("bar");
		job_02.setSource(source);
		job_02.setTrigger(trigger2);
		job_02.setBaseOutputFilename("aReportJob_2_OUTPUT");
		job_02.addOutputFormat(ReportJob.OUTPUT_FORMAT_PDF);
		job_02.addOutputFormat(ReportJob.OUTPUT_FORMAT_RTF);
		job_02.setContentRepositoryDestination(repositoryDestination);
        job_02.setAlert(alert);
        boolean exceptionCaught = false;
        try {
		    job_02.setMailNotification(mailNotification);
        } catch (Exception ex) {
            exceptionCaught = true;
        }
        assertTrue(exceptionCaught);
        mailNotification.setResultSendTypeCode(mailNotification.RESULT_SEND_ATTACHMENT);
        job_02.setMailNotification(mailNotification);

		job_02 = m_reportJobsPersistenceService.saveJob(m_executionContext, job_02);
        assertEquals(false, job_02.getContentRepositoryDestination().isSaveToRepository());

		assertNotNull(job_02);
		long jobId_02 = job_02.getId();

        // test creation date
        assertTrue((System.currentTimeMillis() - job_01.getCreationDate().getTime()) < 60000);
        assertTrue((System.currentTimeMillis() - job_02.getCreationDate().getTime()) < 60000);

        // test default report output of user

        assertEquals("/default/report_output/folder", job_02.getContentRepositoryDestination().getDefaultReportOutputFolderURI());
        assertEquals("/default/report_output/folder", job_01.getContentRepositoryDestination().getDefaultReportOutputFolderURI());
        assertEquals(true, job_01.getContentRepositoryDestination().isUsingDefaultReportOutputFolderURI());
        assertEquals(false, job_02.getContentRepositoryDestination().isUsingDefaultReportOutputFolderURI());

        // test alert
        testAlert(job_02.getAlert());

        // test output destination
        assertNull(job_01.getContentRepositoryDestination().getOutputLocalFolder());
        assertTrue((job_01.getContentRepositoryDestination().getOutputFTPInfo() == null) ||
                (job_01.getContentRepositoryDestination().getOutputFTPInfo().getFolderPath() == null));
        assertEquals("c:/tmp", job_02.getContentRepositoryDestination().getOutputLocalFolder());
        assertEquals("JohnSmith", job_02.getContentRepositoryDestination().getOutputFTPInfo().getUserName());
        Map<String, String> ftpProperties = job_02.getContentRepositoryDestination().getOutputFTPInfo().getPropertiesMap();
        assertEquals("27", ftpProperties.get("PORT"));
        assertNotNull(job_02.getContentRepositoryDestination().getOutputFTPInfo().getPassword());  //password was encrypted.  Unencrypted pwd is never stored.

      // test mail notification
      assertTrue(!job_01.getMailNotification().isSkipNotificationWhenJobFails());
      assertTrue(job_02.getMailNotification().isSkipNotificationWhenJobFails());

		boolean deleted = true;
		try {
			job_01 = m_reportJobsPersistenceService.loadJob(m_executionContext, new ReportJobIdHolder(jobId_01));

      m_logger.info("retrieved job_01 "+job_01.getId()+" has version="+job_01.getVersion());

			assertNotNull(job_01);
			assertEquals("foo", job_01.getLabel());
			Set outputFormats = job_01.getOutputFormatsSet();
			assertNotNull(outputFormats);
			assertEquals(2, outputFormats.size());
			assertTrue(outputFormats.contains(new Byte(ReportJob.OUTPUT_FORMAT_PDF)));
			assertTrue(outputFormats.contains(new Byte(ReportJob.OUTPUT_FORMAT_RTF)));

			source = job_01.getSource();
			assertNotNull(source);
			assertEquals("/test/reportURI", source.getReportUnitURI());
			params = source.getParametersMap();
			assertNotNull(params);
			assertEquals(2, params.size());
			assertTrue(params.containsKey("param1"));
			assertEquals(new Integer(5), params.get("param1"));
			assertTrue(params.containsKey("param2"));
			assertEquals("value2", params.get("param2"));

			assertNotNull(job_01.getTrigger());
			assertTrue(job_01.getTrigger() instanceof ReportJobSimpleTrigger);
			trigger = (ReportJobSimpleTrigger) job_01.getTrigger();
			assertEquals(20, trigger.getOccurrenceCount());
			assertNotNull(trigger.getRecurrenceIntervalUnit());
			assertEquals(ReportJobSimpleTrigger.INTERVAL_DAY, trigger.getRecurrenceIntervalUnit().byteValue());

			repositoryDestination = job_01.getContentRepositoryDestination();
			assertNotNull(repositoryDestination);
			assertEquals("/test/scheduled", repositoryDestination.getFolderURI());
			assertEquals("report output", repositoryDestination.getOutputDescription());
			assertTrue(repositoryDestination.isSequentialFilenames());
			assertEquals("yyyyMMdd", repositoryDestination.getTimestampPattern());
			assertFalse(repositoryDestination.isOverwriteFiles());

			mailNotification = job_01.getMailNotification();
			assertNotNull(mailNotification);
			assertEquals("Scheduled report", mailNotification.getSubject());
			List toAddresses = mailNotification.getToAddresses();
			assertNotNull(toAddresses);
			assertEquals(1, toAddresses.size());
			assertEquals("john@smith.com", toAddresses.get(0));

			long origJobId = job_01.getId();
			int origJobVersion = job_01.getVersion();
			long origTriggerId = trigger.getId();
			int origTriggerVersion = trigger.getVersion();
			long origMailId = mailNotification.getId();
			int origMailVersion = mailNotification.getVersion();
			job_01.setDescription("updated");
			mailNotification.setSubject("updated subject");
			mailNotification.addTo("joan@smith.com");
			mailNotification.addCc("mary@smith.com");
			m_reportJobsPersistenceService.updateJob(m_executionContext, job_01);
			job_01 = m_reportJobsPersistenceService.loadJob(m_executionContext, new ReportJobIdHolder(jobId_01));
			assertNotNull(job_01);
			assertEquals("foo", job_01.getLabel());
			assertEquals("updated", job_01.getDescription());
			assertEquals(origJobId, job_01.getId());
			assertEquals(origJobVersion + 1, job_01.getVersion());
			assertNotNull(job_01.getTrigger());
			assertTrue(job_01.getTrigger() instanceof ReportJobSimpleTrigger);
			trigger = (ReportJobSimpleTrigger) job_01.getTrigger();
			assertEquals(origTriggerId, trigger.getId());
			assertEquals(origTriggerVersion, trigger.getVersion());
			mailNotification = job_01.getMailNotification();
			assertNotNull(mailNotification);
			assertEquals(origMailId, mailNotification.getId());
			assertEquals(origMailVersion + 1, mailNotification.getVersion());
			assertEquals("updated subject", mailNotification.getSubject());
			toAddresses = mailNotification.getToAddresses();
			assertEquals(2, toAddresses.size());
			assertEquals("john@smith.com", toAddresses.get(0));
			assertEquals("joan@smith.com", toAddresses.get(1));
			List ccAddresses = mailNotification.getCcAddresses();
			assertNotNull(ccAddresses);
			assertEquals(1, ccAddresses.size());
			assertEquals("mary@smith.com", ccAddresses.get(0));

			List jobs = m_reportJobsPersistenceService.listJobs(m_executionContext, "/test/reportURI");
			assertNotNull(jobs);
			assertTrue(1 <= jobs.size());
			boolean found = false;
			for (Iterator it = jobs.iterator(); it.hasNext();) {
				Object element = it.next();
				assertTrue(element instanceof ReportJobSummary);
				ReportJobSummary summary = (ReportJobSummary) element;
				if (summary.getId() == jobId_01) {
					found = true;
					assertEquals("foo", summary.getLabel());
					break;
				}
			}
			assertTrue(found);
            // test listJobs function by filtering Report Job Criteria model
            testListJobs(jobId_01, jobId_02, userName);
            // test sorting feature
            testSorting(jobId_01, jobId_02);
            testSortingDES(jobId_01, jobId_02);
            testSortingNONEDES(jobId_01, jobId_02);
            // test pagination feature
            testPagination(jobId_01, jobId_02);

            // test bulk update feature by using report job model
            testUpdateJobsByIDFAIL(jobId_01, jobId_02);

            // test bulk update feature by using report job model
            testUpdateJobsByIDFAILInvalidID(jobId_01, jobId_02);

            // test bulk update feature by using report job model
            testUpdateJobsByIDFAILInvalidPath(jobId_01, jobId_02);
    //        testUpdateJobsByID(jobId_01, jobId_02);
    //        testUpdateJobsByReference(job_01, job_02);

            m_reportJobsPersistenceService.deleteJob(m_executionContext, new ReportJobIdHolder(jobId_01));
            m_reportJobsPersistenceService.deleteJob(m_executionContext, new ReportJobIdHolder(jobId_02));
			deleted = true;
            try {
                job_01 = m_reportJobsPersistenceService.loadJob(m_executionContext, new ReportJobIdHolder(jobId_01));
                assertTrue(true);
            } catch (ReportJobNotFoundException ad) {}
            try {
                job_02 = m_reportJobsPersistenceService.loadJob(m_executionContext, new ReportJobIdHolder(jobId_02));
                assertTrue(true);
            } catch (ReportJobNotFoundException ad) {}

		} finally {
			if (!deleted) {
				m_reportJobsPersistenceService.deleteJob(m_executionContext, new ReportJobIdHolder(jobId_01));
                m_reportJobsPersistenceService.deleteJob(m_executionContext, new ReportJobIdHolder(jobId_02));
			}
			getRepositoryService().deleteFolder(m_executionContext, folder.getURIString());
			
		}
	}

    private void testAlert(ReportJobAlert alert) {
        assertEquals(ReportJobAlert.Recipient.ADMIN, alert.getRecipient());
        assertEquals("CUSTOMIZE MESSAGE", alert.getMessageText());
        assertEquals(ReportJobAlert.JobState.FAIL_ONLY, alert.getJobState());
        ArrayList<String> toAddresses = new ArrayList<String>();
        toAddresses.add("peterpan@jaspersoft.com");
        toAddresses.add("peter.pan@gmail.com");
        assertEquals(2, alert.getToAddresses().size());
        for (String address: toAddresses) assertEquals(true, alert.getToAddresses().contains(address));
    }



  /**
   *  Test that exercises getJobsByNextFireTime
   *
   *  For the test:
   *     0.  Define a time window that we will search within
   *     1.  Schedule 2 jobs, 1 will fire within our window and 1 will fire outside of our window
   *     2.  Query to find jobs within our window and verify that we fetch the expected one.
   *     3.  Query all jobs and verify that we get both back.
   *
   *
   *  We'll schedule 2 jobs
   */
  @Test()
  public void doBasicFindByNextFireTimePersistenceTest() {
    m_logger.info("\n\nReportSchedulingTestTestNG => doBasicFindByNextFireTimePersistenceTest() called");
    setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);


    // REPORT JOB 1  The Job that we want out query to find 

    Date now = new Date();
    long nowMillis = now.getTime();
    long windowIntervalMillis = 1000 * 60 * 6;  //  size of the query time window 6 minutes
    long queryWindowStart = nowMillis - 10;     //  wind clock back just to be sure
    long queryWindowEnd = nowMillis  + windowIntervalMillis;
    Date report01StartDate = new Date(queryWindowStart + (1000 * 60 * 4));   // start in 4 minutes
    Date report02StartDate = new Date(queryWindowStart + (1000 * 60 * 10));   // start in 10 minutes
    Date startDate = new Date(queryWindowStart);
    Date endDate = new Date(queryWindowEnd);

    
    ReportJobSource source = new ReportJobSource();
    // validator requires a REAL report
    source.setReportUnitURI("/reports/samples/AllAccounts");
    Map params = new HashMap();
    //params.put("param1", new Integer(5));
    //params.put("param2", "value2");
    source.setParametersMap(params);
    
    
    ReportJobSimpleTrigger trigger = new ReportJobSimpleTrigger();
    trigger.setStartDate(report01StartDate);
    trigger.setOccurrenceCount(5);
    trigger.setRecurrenceInterval(1);
    trigger.setRecurrenceIntervalUnit(ReportJobSimpleTrigger.INTERVAL_MINUTE);

    ReportJobRepositoryDestination repositoryDestination = new ReportJobRepositoryDestination();
    // validator requires a REAL folder
    repositoryDestination.setFolderURI("/reports/samples");
    repositoryDestination.setOutputDescription("report output");
    repositoryDestination.setSequentialFilenames(true);
    repositoryDestination.setTimestampPattern("yyyyMMdd");
    repositoryDestination.setDefaultReportOutputFolderURI("/default/report_output/folder");
    repositoryDestination.setUsingDefaultReportOutputFolderURI(true);

    ReportJobMailNotification mailNotification = new ReportJobMailNotification();
    mailNotification.addTo("john@smith.com");
    mailNotification.setSubject("Scheduled report");
    mailNotification.setMessageText("Executed report");

    ReportJob job_01 = new ReportJob();
    job_01.setLabel("foo");
    job_01.setDescription("bar");
    job_01.setSource(source);
    job_01.setTrigger(trigger);
    job_01.setBaseOutputFilename("foo_"+(new Date().getTime()));
    job_01.addOutputFormat(ReportJob.OUTPUT_FORMAT_PDF);
    job_01.addOutputFormat(ReportJob.OUTPUT_FORMAT_RTF);
    job_01.setContentRepositoryDestination(repositoryDestination);
    job_01.setMailNotification(mailNotification);

    job_01 = m_reportSchedulingService.scheduleJob(m_executionContext, job_01);
    m_logger.info("scheduled Job 01 id='"+job_01.getId()+"' label='"+job_01.getLabel()+" for "+report01StartDate);

    assertNotNull(job_01);
    long jobId_01 = job_01.getId();
    String userName = job_01.getUsername();


    //  Report Job 02      The Job that we DON'T want our query to find

    source = new ReportJobSource();

    // validator requires a REAL report
    source.setReportUnitURI("/reports/samples/AllAccounts");
    params = new HashMap();
    source.setParametersMap(params);

    ReportJobSimpleTrigger trigger2 = new ReportJobSimpleTrigger();
    trigger2.setStartDate(report02StartDate);
    trigger2.setOccurrenceCount(5);
    trigger2.setRecurrenceInterval(1);
    trigger2.setRecurrenceIntervalUnit(ReportJobSimpleTrigger.INTERVAL_MINUTE);


    repositoryDestination = new ReportJobRepositoryDestination();
    // validator requires a REAL folder
    repositoryDestination.setFolderURI("/reports/samples");
    repositoryDestination.setOutputDescription("report output");
    repositoryDestination.setSequentialFilenames(false);
    repositoryDestination.setTimestampPattern("yyyyMMdd");
    repositoryDestination.setSaveToRepository(false);

    mailNotification = new ReportJobMailNotification();
    mailNotification.addTo("john@smith.com");
    mailNotification.addTo("peter@pan.com");
    mailNotification.setSubject("Scheduled report");
    mailNotification.setMessageText("Executed report");

    ReportJob job_02 = new ReportJob();
    job_02.setLabel("A_ReportJob_2");
    job_02.setDescription("bar");
    job_02.setSource(source);
    job_02.setTrigger(trigger2);
    job_02.setBaseOutputFilename("aReportJob_2_OUTPUT_" + (new Date().getTime()));
    job_02.addOutputFormat(ReportJob.OUTPUT_FORMAT_PDF);
    job_02.addOutputFormat(ReportJob.OUTPUT_FORMAT_RTF);
    job_02.setContentRepositoryDestination(repositoryDestination);
    boolean exceptionCaught = false;
    try {
      job_02.setMailNotification(mailNotification);
    } catch (Exception ex) {
      exceptionCaught = true;
    }
    assertTrue(exceptionCaught);
    mailNotification.setResultSendTypeCode(mailNotification.RESULT_SEND_ATTACHMENT);
    job_02.setMailNotification(mailNotification);

    job_02 = m_reportSchedulingService.scheduleJob(m_executionContext, job_02);
    m_logger.info("scheduled Job 02 id='"+job_02.getId()+"' label='"+job_02.getLabel()+" for "+report02StartDate);

    assertEquals(false, job_02.getContentRepositoryDestination().isSaveToRepository());

    assertNotNull(job_02);
    long jobId_02 = job_02.getId();

    boolean deleted = false;
    try {
      List<ReportJobSummary> summaryList = 
              m_reportSchedulingService.getJobsByNextFireTime(m_executionContext,
                      null,
                      startDate,
                      endDate,
                      null);

      StringBuilder sb = new StringBuilder();
      int count = 0;
      for (ReportJobSummary js : summaryList) {
        ReportJobRuntimeInformation ri = js.getRuntimeInformation();
        Date nextFireTime = (ri == null ? null : ri.getNextFireTime());
        String time = (nextFireTime == null ? "NULL" : nextFireTime.toString());
        sb.append((count++) +" job "+js.getId()+" "+nextFireTime+" = "+time+"\n");
      }
      m_logger.info(sb.toString());

      m_logger.info("expecting query to return 1 report, got "+summaryList.size());
      int expectedNumberOfReports = 1;
      assertEquals("Error ! expected to get back 1 Report in window but instead we got "+summaryList.size(),expectedNumberOfReports, summaryList.size());

      ReportJobSummary rjs = summaryList.get(0);
      long id1 = rjs.getId();
      m_logger.info("Expected to get back jobId='"+jobId_01+"', got '"+id1+"'");
      assertEquals("Error ! expected to get back job01 id = '" + jobId_01 +
              "', but instead we got '" + id1 + ", note: job02 id = '" + jobId_02, id1, jobId_01);

      summaryList =
              m_reportSchedulingService.getJobsByNextFireTime(m_executionContext,
                      null,
                      null,
                      null,
                      null);
      sb.setLength(0);
      count = 0;
      for (ReportJobSummary js : summaryList) {
        ReportJobRuntimeInformation ri = js.getRuntimeInformation();
        Date nextFireTime = (ri == null ? null : ri.getNextFireTime());
        String time = (nextFireTime == null ? "NULL" : nextFireTime.toString());
        sb.append((count++) +" job "+js.getId()+" "+nextFireTime+" = "+time+"\n");
      }
      m_logger.info(sb.toString());

      m_logger.info("expecting query to return 2 report, got "+summaryList.size());
      expectedNumberOfReports = 2;
      assertEquals("Error ! expected to get back 2 Reports but instead we got "+summaryList.size(),expectedNumberOfReports, summaryList.size());

      try {
        m_reportJobsPersistenceService.deleteJob(m_executionContext, new ReportJobIdHolder(jobId_01));
        m_reportJobsPersistenceService.deleteJob(m_executionContext, new ReportJobIdHolder(jobId_02));
        deleted = true;
      } catch (Throwable th) {
        m_logger.info("Error !  Throwable while attempting to delete job '"+th.getMessage()+"'");
      }

      try {
          m_reportJobsPersistenceService.deleteJob(m_executionContext, new ReportJobIdHolder(jobId_02));
      } catch (Throwable th)  {}
      try {
          job_01 = m_reportJobsPersistenceService.loadJob(m_executionContext, new ReportJobIdHolder(jobId_01));
          assertTrue(true);
      } catch (ReportJobNotFoundException ad) {}
      try {
          job_02 = m_reportJobsPersistenceService.loadJob(m_executionContext, new ReportJobIdHolder(jobId_02));
          assertTrue(true);
      } catch (ReportJobNotFoundException ad) {}

    } finally {
      if (!deleted) {
        try {
        m_reportJobsPersistenceService.deleteJob(m_executionContext, new ReportJobIdHolder(jobId_01));
        } catch (Throwable th)  {}
        try {
        m_reportJobsPersistenceService.deleteJob(m_executionContext, new ReportJobIdHolder(jobId_02));
        } catch (Throwable th) {}
      }
    }
  }


  /**
   *  2013-03-12  thorick:
   *                        The call to Quartz to pause an individual job returns without any Exception
   *                        but when we query for the state of the trigger we don't get back
   *                        PAUSED.
   *                        Don't know right now if the trigger is actually paused or not.
   *                        disabling the UnitTest for now.
   *
   *  Test that exercises call down to Quartz pause/resume function.
   *
   *
   *  We'll schedule 2 jobs and test:
   *    pause/resume 1 job  by name
   *
   */
  @Test()
  public void doPauseResumeIndividualTest() {
    m_logger.info("\n\nReportSchedulingTestTestNG => doPauseResumeIndividualTest() called");
    setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);


    // REPORT JOB 1  The Job that we want out query to find

    Date now = new Date();
    long nowMillis = now.getTime();
    long windowIntervalMillis = 1000 * 60 * 15;  //  size of the query time window 15 minutes
    long queryWindowStart = nowMillis - 10;     //  wind clock back just to be sure
    long queryWindowEnd = nowMillis  + windowIntervalMillis;
    Date report01StartDate = new Date(queryWindowStart + (1000 * 60 * 10));   // start in 10 minutes
    Date report02StartDate = new Date(queryWindowStart + (1000 * 60 * 10));   // start in 10 minutes
    Date startDate = new Date(queryWindowStart);
    Date endDate = new Date(queryWindowEnd);



    ReportJobSource source = new ReportJobSource();
    // validator requires a REAL report
    source.setReportUnitURI("/reports/samples/AllAccounts");
    Map params = new HashMap();
    source.setParametersMap(params);

    ReportJobSimpleTrigger trigger = new ReportJobSimpleTrigger();
    trigger.setStartDate(report01StartDate);
    trigger.setOccurrenceCount(5);
    trigger.setRecurrenceInterval(1);
    trigger.setRecurrenceIntervalUnit(ReportJobSimpleTrigger.INTERVAL_MINUTE);

    ReportJobRepositoryDestination repositoryDestination = new ReportJobRepositoryDestination();
    // validator requires a REAL folder
    repositoryDestination.setFolderURI("/reports/samples");
    repositoryDestination.setOutputDescription("report output");
    repositoryDestination.setSequentialFilenames(true);
    repositoryDestination.setTimestampPattern("yyyyMMdd");
    repositoryDestination.setDefaultReportOutputFolderURI("/default/report_output/folder");
    repositoryDestination.setUsingDefaultReportOutputFolderURI(true);

    ReportJobMailNotification mailNotification = new ReportJobMailNotification();
    mailNotification.addTo("john@smith.com");
    mailNotification.setSubject("Scheduled report");
    mailNotification.setMessageText("Executed report");

    ReportJob job_01 = new ReportJob();
    job_01.setLabel("foo");
    job_01.setDescription("bar");
    job_01.setSource(source);
    job_01.setTrigger(trigger);
    job_01.setBaseOutputFilename("foo_"+(new Date().getTime()));
    job_01.addOutputFormat(ReportJob.OUTPUT_FORMAT_PDF);
    job_01.addOutputFormat(ReportJob.OUTPUT_FORMAT_RTF);
    job_01.setContentRepositoryDestination(repositoryDestination);
    job_01.setMailNotification(mailNotification);

    job_01 = m_reportSchedulingService.scheduleJob(m_executionContext, job_01);
    m_logger.info("scheduled Job 01 id='"+job_01.getId()+"' label='"+job_01.getLabel()+" for "+report01StartDate);

    assertNotNull(job_01);
    long jobId_01 = job_01.getId();
    String userName = job_01.getUsername();


    //  Report Job 02

    source = new ReportJobSource();

    // validator requires a REAL report
    source.setReportUnitURI("/reports/samples/AllAccounts");
    params = new HashMap();
    source.setParametersMap(params);

    ReportJobSimpleTrigger trigger2 = new ReportJobSimpleTrigger();
    trigger2.setStartDate(report02StartDate);
    trigger2.setOccurrenceCount(5);
    trigger2.setRecurrenceInterval(1);
    trigger2.setRecurrenceIntervalUnit(ReportJobSimpleTrigger.INTERVAL_MINUTE);


    repositoryDestination = new ReportJobRepositoryDestination();
    // validator requires a REAL folder
    repositoryDestination.setFolderURI("/reports/samples");
    repositoryDestination.setOutputDescription("report output");
    repositoryDestination.setSequentialFilenames(false);
    repositoryDestination.setTimestampPattern("yyyyMMdd");
    repositoryDestination.setSaveToRepository(false);

    mailNotification = new ReportJobMailNotification();
    mailNotification.addTo("john@smith.com");
    mailNotification.addTo("peter@pan.com");
    mailNotification.setSubject("Scheduled report");
    mailNotification.setMessageText("Executed report");

    ReportJob job_02 = new ReportJob();
    job_02.setLabel("A_ReportJob_2");
    job_02.setDescription("bar");
    job_02.setSource(source);
    job_02.setTrigger(trigger2);
    job_02.setBaseOutputFilename("aReportJob_2_OUTPUT_" + (new Date().getTime()));
    job_02.addOutputFormat(ReportJob.OUTPUT_FORMAT_PDF);
    job_02.addOutputFormat(ReportJob.OUTPUT_FORMAT_RTF);
    job_02.setContentRepositoryDestination(repositoryDestination);
    boolean exceptionCaught = false;
    try {
      job_02.setMailNotification(mailNotification);
    } catch (Exception ex) {
      exceptionCaught = true;
    }
    assertTrue(exceptionCaught);
    mailNotification.setResultSendTypeCode(mailNotification.RESULT_SEND_ATTACHMENT);
    job_02.setMailNotification(mailNotification);

    job_02 = m_reportSchedulingService.scheduleJob(m_executionContext, job_02);
    m_logger.info("scheduled Job 02 id='"+job_02.getId()+"' label='"+job_02.getLabel()+" for "+report02StartDate);

    assertEquals(false, job_02.getContentRepositoryDestination().isSaveToRepository());

    assertNotNull(job_02);
    long jobId_02 = job_02.getId();



    boolean deleted = false;
    final Byte paused = ReportJobRuntimeInformation.STATE_PAUSED;
    final Byte normal = ReportJobRuntimeInformation.STATE_NORMAL;
    ArrayList pausedL = new ArrayList();
    pausedL.add(paused);
    ArrayList normalL = new ArrayList();
    normalL.add(normal);

    try {
      m_logger.info("do pause of "+jobId_01);
      List<ReportJob> pauseList = new ArrayList<ReportJob>();
      pauseList.add(job_01);

      m_reportSchedulingService.pause(pauseList,
          false);

      List<ReportJobSummary> summaryList =
          m_reportSchedulingService.getJobsByNextFireTime(m_executionContext,
              null,
              startDate,
              endDate,
              pausedL);

      int expectedSize = 1;
      assertEquals("Error ! expected to get back "+expectedSize+" paused jobs, instead we got "+summaryList.size(),
          expectedSize, summaryList.size());

      boolean found1 = false;
      boolean found2 = false;
      for (ReportJobSummary rjs : summaryList) {
        m_logger.info(rjs.getId()+"  status = "+rjs.getRuntimeInformation().getStateCode()+", note 'PAUSED == 3");

        if (rjs.getId() == jobId_01)   found1 = true;
        if (rjs.getId() == jobId_02)   found2 = true;
      }
      assertTrue("Error, we expected to have paused "+job_01+" but we didn't !", found1);
      assertFalse("Error, we expected to have NOT paused "+job_02+" but we did !", found2);

      try {
        m_reportJobsPersistenceService.deleteJob(m_executionContext, new ReportJobIdHolder(jobId_01));
        m_reportJobsPersistenceService.deleteJob(m_executionContext, new ReportJobIdHolder(jobId_02));
        deleted = true;
      } catch (Throwable th) {
        m_logger.info("Error !  Throwable while attempting to delete job '"+th.getMessage()+"'");
      }
      try {
          m_reportJobsPersistenceService.deleteJob(m_executionContext, new ReportJobIdHolder(jobId_02));
          assertTrue(true);
      } catch (Throwable th)  {}

      try {
          job_01 = m_reportJobsPersistenceService.loadJob(m_executionContext, new ReportJobIdHolder(jobId_01));
          assertTrue(true);
      } catch (ReportJobNotFoundException ad) {}
      try {
          job_02 = m_reportJobsPersistenceService.loadJob(m_executionContext, new ReportJobIdHolder(jobId_02));
          assertTrue(true);
      } catch (ReportJobNotFoundException ad) {}

    } finally {
      if (!deleted) {
        try {
          m_reportJobsPersistenceService.deleteJob(m_executionContext, new ReportJobIdHolder(jobId_01));
        } catch (Throwable th)  {}
        try {
          m_reportJobsPersistenceService.deleteJob(m_executionContext, new ReportJobIdHolder(jobId_02));
        } catch (Throwable th) {}
      }
    }
  }





      /**
      *  Test that exercises call down to Quartz pause/resume function.
      *
      *  We'll schedule 2 jobs and test:
      *    pause/resume all jobs
      *
      */
  @Test()
  public void doPauseResumeAllTest() {
    m_logger.info("\n\nReportSchedulingTestTestNG => doPauseResumeAllTest() called");
    setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);


    // REPORT JOB 1  The Job that we want out query to find

    Date now = new Date();
    long nowMillis = now.getTime();
    long windowIntervalMillis = 1000 * 60 * 15;  //  size of the query time window 15 minutes
    long queryWindowStart = nowMillis - 10;     //  wind clock back just to be sure
    long queryWindowEnd = nowMillis  + windowIntervalMillis;
    Date report01StartDate = new Date(queryWindowStart + (1000 * 60 * 10));   // start in 10 minutes
    Date report02StartDate = new Date(queryWindowStart + (1000 * 60 * 10));   // start in 10 minutes
    Date startDate = new Date(queryWindowStart);
    Date endDate = new Date(queryWindowEnd);



    ReportJobSource source = new ReportJobSource();
    // validator requires a REAL report
    source.setReportUnitURI("/reports/samples/AllAccounts");
    Map params = new HashMap();
    source.setParametersMap(params);

    ReportJobSimpleTrigger trigger = new ReportJobSimpleTrigger();
    trigger.setStartDate(report01StartDate);
    trigger.setOccurrenceCount(5);
    trigger.setRecurrenceInterval(1);
    trigger.setRecurrenceIntervalUnit(ReportJobSimpleTrigger.INTERVAL_MINUTE);

    ReportJobRepositoryDestination repositoryDestination = new ReportJobRepositoryDestination();
    // validator requires a REAL folder
    repositoryDestination.setFolderURI("/reports/samples");
    repositoryDestination.setOutputDescription("report output");
    repositoryDestination.setSequentialFilenames(true);
    repositoryDestination.setTimestampPattern("yyyyMMdd");
    repositoryDestination.setDefaultReportOutputFolderURI("/default/report_output/folder");
    repositoryDestination.setUsingDefaultReportOutputFolderURI(true);

    ReportJobMailNotification mailNotification = new ReportJobMailNotification();
    mailNotification.addTo("john@smith.com");
    mailNotification.setSubject("Scheduled report");
    mailNotification.setMessageText("Executed report");

    ReportJob job_01 = new ReportJob();
    job_01.setLabel("foo");
    job_01.setDescription("bar");
    job_01.setSource(source);
    job_01.setTrigger(trigger);
    job_01.setBaseOutputFilename("foo_"+(new Date().getTime()));
    job_01.addOutputFormat(ReportJob.OUTPUT_FORMAT_PDF);
    job_01.addOutputFormat(ReportJob.OUTPUT_FORMAT_RTF);
    job_01.setContentRepositoryDestination(repositoryDestination);
    job_01.setMailNotification(mailNotification);

    job_01 = m_reportSchedulingService.scheduleJob(m_executionContext, job_01);
    m_logger.info("scheduled Job 01 id='"+job_01.getId()+"' label='"+job_01.getLabel()+" for "+report01StartDate);

    assertNotNull(job_01);
    long jobId_01 = job_01.getId();
    String userName = job_01.getUsername();


    //  Report Job 02

    source = new ReportJobSource();

    // validator requires a REAL report
    source.setReportUnitURI("/reports/samples/AllAccounts");
    params = new HashMap();
    source.setParametersMap(params);

    ReportJobSimpleTrigger trigger2 = new ReportJobSimpleTrigger();
    trigger2.setStartDate(report02StartDate);
    trigger2.setOccurrenceCount(5);
    trigger2.setRecurrenceInterval(1);
    trigger2.setRecurrenceIntervalUnit(ReportJobSimpleTrigger.INTERVAL_MINUTE);


    repositoryDestination = new ReportJobRepositoryDestination();
    // validator requires a REAL folder
    repositoryDestination.setFolderURI("/reports/samples");
    repositoryDestination.setOutputDescription("report output");
    repositoryDestination.setSequentialFilenames(false);
    repositoryDestination.setTimestampPattern("yyyyMMdd");
    repositoryDestination.setSaveToRepository(false);

    mailNotification = new ReportJobMailNotification();
    mailNotification.addTo("john@smith.com");
    mailNotification.addTo("peter@pan.com");
    mailNotification.setSubject("Scheduled report");
    mailNotification.setMessageText("Executed report");

    ReportJob job_02 = new ReportJob();
    job_02.setLabel("A_ReportJob_2");
    job_02.setDescription("bar");
    job_02.setSource(source);
    job_02.setTrigger(trigger2);
    job_02.setBaseOutputFilename("aReportJob_2_OUTPUT_" + (new Date().getTime()));
    job_02.addOutputFormat(ReportJob.OUTPUT_FORMAT_PDF);
    job_02.addOutputFormat(ReportJob.OUTPUT_FORMAT_RTF);
    job_02.setContentRepositoryDestination(repositoryDestination);
    boolean exceptionCaught = false;
    try {
      job_02.setMailNotification(mailNotification);
    } catch (Exception ex) {
      exceptionCaught = true;
    }
    assertTrue(exceptionCaught);
    mailNotification.setResultSendTypeCode(mailNotification.RESULT_SEND_ATTACHMENT);
    job_02.setMailNotification(mailNotification);

    job_02 = m_reportSchedulingService.scheduleJob(m_executionContext, job_02);
    m_logger.info("scheduled Job 02 id='"+job_02.getId()+"' label='"+job_02.getLabel()+" for "+report02StartDate);

    assertEquals(false, job_02.getContentRepositoryDestination().isSaveToRepository());

    assertNotNull(job_02);
    long jobId_02 = job_02.getId();



    boolean deleted = false;
    final Byte paused = ReportJobRuntimeInformation.STATE_PAUSED;
    final Byte normal = ReportJobRuntimeInformation.STATE_NORMAL;
    ArrayList pausedL = new ArrayList();
    pausedL.add(paused);
    ArrayList normalL = new ArrayList();
    normalL.add(normal);
    
    try {
      m_logger.info("now pause all");
      //List<ReportJob> pauseList1 = new ArrayList<ReportJob>();
      //pauseList1.add(job_01);
      m_reportSchedulingService.pause(null,
              true);

      List<ReportJobSummary> summaryList =
          m_reportSchedulingService.getJobsByNextFireTime(m_executionContext,
              null,
              startDate,
              endDate,
              pausedL);

      int expectedSize = 2;
      assertEquals("Error ! expected to get back "+expectedSize+" paused jobs, instead we got "+summaryList.size(),
          expectedSize, summaryList.size());

      boolean found1 = false;
      boolean found2 = false;
      for (ReportJobSummary rjs : summaryList) {
        m_logger.info(rjs.getId()+"  status = "+rjs.getRuntimeInformation().getStateCode()+", note 'PAUSED == 3");

        if (rjs.getId() == jobId_01)   found1 = true;
        if (rjs.getId() == jobId_02)   found2 = true;
      }
      assertTrue("Error, we expected to have paused "+job_01+" but we didn't !", found1);
      assertTrue("Error, we expected to have paused "+job_02+" but we didn't !", found2);



      m_logger.info("now do resume all");
      m_reportSchedulingService.resume(null, true);
      summaryList =
          m_reportSchedulingService.getJobsByNextFireTime(m_executionContext,
              null,
              startDate,
              endDate,
              normalL);

      expectedSize = 2;
      assertEquals("Error ! expected to get back "+expectedSize+" paused jobs, instead we got "+summaryList.size(),
          expectedSize, summaryList.size());

      found1 = false;
      found2 = false;
      for (ReportJobSummary rjs : summaryList) {
        m_logger.info(rjs.getId()+"  status = "+rjs.getRuntimeInformation().getStateCode()+", note 'NORMAL == 1");

        if (rjs.getId() == jobId_01)   found1 = true;
        if (rjs.getId() == jobId_02)   found2 = true;
      }
      assertTrue("Error, we expected to have resumed "+job_01+" but we didn't !", found1);
      assertTrue("Error, we expected to have resumed "+job_02+" but we didn't !", found2);


      try {
        m_reportJobsPersistenceService.deleteJob(m_executionContext, new ReportJobIdHolder(jobId_01));
        m_reportJobsPersistenceService.deleteJob(m_executionContext, new ReportJobIdHolder(jobId_02));
        deleted = true;
      } catch (Throwable th) {
        m_logger.info("Error !  Throwable while attempting to delete job '"+th.getMessage()+"'");
      }

      try {
        m_reportJobsPersistenceService.deleteJob(m_executionContext, new ReportJobIdHolder(jobId_02));
        assertTrue(true);
      } catch (Throwable th)  {}

      try {
          job_01 = m_reportJobsPersistenceService.loadJob(m_executionContext, new ReportJobIdHolder(jobId_01));
          assertTrue(true);
      } catch (ReportJobNotFoundException ad) {}
      try {
          job_02 = m_reportJobsPersistenceService.loadJob(m_executionContext, new ReportJobIdHolder(jobId_02));
          assertTrue(true);
      } catch (ReportJobNotFoundException ad) {}
    } finally {
      if (!deleted) {
        try {
          m_reportJobsPersistenceService.deleteJob(m_executionContext, new ReportJobIdHolder(jobId_01));
        } catch (Throwable th)  {}
        try {
          m_reportJobsPersistenceService.deleteJob(m_executionContext, new ReportJobIdHolder(jobId_02));
        } catch (Throwable th) {}
      }
    }
  }



  /**
   *  Test that exercises scheduleJobsOnceNow
   *
   *  For the test:
   *     0.  Define a time window that we will search within
   *     1.  Schedule 1 job, it will fire outside of our test window period
   *     2.  Retrieve the ReportJob from the REPO and use the 'scheduleJobsOnceNow' facility to schedule an immediate run
   *     3.  Query to find job within our window and verify that we fetch the expected one which is the runOnceNow job.
   */
  @Test()
  public void doScheduleJobsOnceNowTest() {
    m_logger.info("\n\nReportSchedulingTestTestNG => doScheduleJobsOnceNowTest() called");
    setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);


    // REPORT JOB 1  The Job that we want to execute 'onceNow' at a later time

    Date now = new Date();
    long nowMillis = now.getTime();
    long windowIntervalMillis = 1000 * 60 * 6;  //  size of the query time window 6 minutes
    long queryWindowStart = nowMillis - 10;     //  wind clock back just to be sure
    long queryWindowEnd = nowMillis  + windowIntervalMillis;
    Date report01StartDate = new Date(queryWindowStart + (1000 * 60 * 20));   // start in 20 minutes
    Date startDate = new Date(queryWindowStart);
    Date endDate = new Date(queryWindowEnd);

    m_logger.info("at test start, trigger query window startDate="+startDate+", endDate="+endDate);


    ReportJobSource source = new ReportJobSource();
    // validator requires a REAL report
    source.setReportUnitURI("/reports/samples/AllAccounts");
    Map params = new HashMap();
    source.setParametersMap(params);

    ReportJobSimpleTrigger trigger = new ReportJobSimpleTrigger();
    trigger.setStartDate(report01StartDate);
    trigger.setOccurrenceCount(5);
    trigger.setRecurrenceInterval(1);
    trigger.setRecurrenceIntervalUnit(ReportJobSimpleTrigger.INTERVAL_MINUTE);

    ReportJobRepositoryDestination repositoryDestination = new ReportJobRepositoryDestination();
    // validator requires a REAL folder
    repositoryDestination.setFolderURI("/reports/samples");
    repositoryDestination.setOutputDescription("report output");
    repositoryDestination.setSequentialFilenames(true);
    repositoryDestination.setTimestampPattern("yyyyMMdd");
    repositoryDestination.setDefaultReportOutputFolderURI("/default/report_output/folder");
    repositoryDestination.setUsingDefaultReportOutputFolderURI(true);

    ReportJobMailNotification mailNotification = new ReportJobMailNotification();
    mailNotification.addTo("john@smith.com");
    mailNotification.setSubject("Scheduled report");
    mailNotification.setMessageText("Executed report");

    ReportJob job_01 = new ReportJob();
    job_01.setLabel("hoo");
    job_01.setDescription("bar");
    job_01.setSource(source);
    job_01.setTrigger(trigger);
    job_01.setBaseOutputFilename("hoo_"+(new Date().getTime()));
    job_01.addOutputFormat(ReportJob.OUTPUT_FORMAT_PDF);
    job_01.addOutputFormat(ReportJob.OUTPUT_FORMAT_RTF);
    job_01.setContentRepositoryDestination(repositoryDestination);
    job_01.setMailNotification(mailNotification);

    job_01 = m_reportSchedulingService.scheduleJob(m_executionContext, job_01);
    m_logger.info("scheduled Job 01 id='"+job_01.getId()+"' label='"+job_01.getLabel()+" for "+report01StartDate);

    assertTrue(job_01 != null);
    long jobId_01 = job_01.getId();
    ReportJob readJob_01 = m_reportJobsPersistenceService.loadJob(m_executionContext, new ReportJobIdHolder(jobId_01));
    assertTrue("Fatal error !  we were unable to retrieve job '"+jobId_01+
        "' that we just scheduled !", readJob_01 != null);
    
    
    // now schedule that same report to run onceNow using the reportJob as the model to clone
    List<ReportJob> l = new ArrayList<ReportJob>();
    l.add(readJob_01);
    List<ReportJob> scheduledOnceList = m_reportSchedulingService.scheduleJobsOnceNow(m_executionContext, l);

    if (scheduledOnceList == null || scheduledOnceList.size() <= 0)
      assertEquals("Error our scheduleOnceList should contain 1 report, instead it is null or empty",  1 == 2 );

    assertEquals("Error our scheduleOnceList should contain 1 report, instead it contains "+scheduledOnceList.size(),
        scheduledOnceList.size(), 1);

    ReportJob scheduledOnceJob = scheduledOnceList.get(0);
    long scheduledOnceJobId = scheduledOnceJob.getId();

    // now verify that the trigger for the runOnce job got set by Quartz

    m_logger.info("get jobs with nextFIreTime between start="+startDate+" and end="+endDate);
    m_logger.info("verify that our scheduleOnce Job "+scheduledOnceJobId+" has a nextFireTime before "+endDate);

    boolean deleted = false;
    try {
      List<ReportJobSummary> summaryList =
              m_reportSchedulingService.getJobsByNextFireTime(m_executionContext,
                      null,
                      startDate,
                      endDate,
                      null);

      StringBuilder sb = new StringBuilder();
      int count = 0;
      ReportJobSummary theSummary = null;
      ReportJobRuntimeInformation theInfo = null;
      for (ReportJobSummary js : summaryList) {
        ReportJobRuntimeInformation ri = js.getRuntimeInformation();
        if (js.getId() == scheduledOnceJobId) {
          theInfo = ri;
          theSummary = js;
        }
        Date nextFireTime = (ri == null ? null : ri.getNextFireTime());
        String time = (nextFireTime == null ? "NULL" : nextFireTime.toString());
        sb.append((count++) +" job "+js.getId()+" "+nextFireTime+" = "+time+"\n");  
      }
      m_logger.info(sb.toString());

      int expectedNumberOfReports = 1;
      m_logger.info("expecting query to return "+expectedNumberOfReports+" reports, got "+summaryList.size());
      assertEquals("Error ! expected to get back "+expectedNumberOfReports+" Reports in window but instead we got "+summaryList.size(),expectedNumberOfReports, summaryList.size());

      Date nextFireTime = theInfo.getNextFireTime();
      assertTrue("Error !  Expected non-NULL Trigger.nextFireTime for runOnceNowJob "+scheduledOnceJobId, nextFireTime != null);
      assertTrue("Error !  Expected our runOnceNow Job to have a nextTriggerFireTime "+
          "before "+endDate+", but instead it is set for "+nextFireTime, nextFireTime.before(endDate));
      
      
      try {
        m_reportJobsPersistenceService.deleteJob(m_executionContext, new ReportJobIdHolder(jobId_01));
        m_reportJobsPersistenceService.deleteJob(m_executionContext, new ReportJobIdHolder(theSummary.getId()));
        deleted = true;
      } catch (Throwable th) {
        m_logger.info("Error !  Throwable while attempting to delete job '"+th.getMessage()+"'");
      }
    try {
        job_01 = m_reportJobsPersistenceService.loadJob(m_executionContext, new ReportJobIdHolder(jobId_01));
        assertTrue(true);
    } catch (ReportJobNotFoundException ad) {}
    try {
        ReportJob job_02 = m_reportJobsPersistenceService.loadJob(m_executionContext, new ReportJobIdHolder(theSummary.getId()));
        assertTrue(true);
    } catch (ReportJobNotFoundException ad) {}

    } finally {
      if (!deleted) {
        try {
          m_reportJobsPersistenceService.deleteJob(m_executionContext, new ReportJobIdHolder(jobId_01));
        } catch (Throwable th)  {}
        try {
          m_reportJobsPersistenceService.deleteJob(m_executionContext, new ReportJobIdHolder(scheduledOnceJobId));
        } catch (Throwable th) {}
      }
    }
  }



  /**
   *  Sanity Test that exercises getJobsByNextFireTime filtered by Trigger State
   *     choose the state that we can have set most reliably:  STATE_NORMAL
   *
   *  For the test:
   *     0.  Define a time window that we will search within
   *     1.  Schedule jobs that will fire outside of our window
   *     2.  Query to find jobs within our window and verify that we fetch the expected one.
   *
   */
  @Test()
  public void doBasicFindByNextFireTimeTriggerStatesPersistenceSanityTest() {
    m_logger.info("\n\nReportSchedulingTestTestNG => doBasicFindByNextFireTimeTriggerStatesPersistenceSanityTest() called");


    // REPORT JOB 1  The Job that we want out query to find

    Date now = new Date();
    long nowMillis = now.getTime();
    long windowIntervalMillis = 1000 * 60 * 6;  //  size of the query time window 6 minutes
    long queryWindowStart = nowMillis - 10;     //  wind clock back just to be sure
    long queryWindowEnd = nowMillis  + windowIntervalMillis;
    Date report01StartDate = new Date(queryWindowStart + (1000 * 60 * 4));   // start in 4 minutes
    Date report02StartDate = new Date(queryWindowStart + (1000 * 60 * 10));   // start in 10 minutes
    Date startDate = new Date(queryWindowStart);
    Date endDate = new Date(queryWindowEnd);


    ReportJobSource source = new ReportJobSource();
    // validator requires a REAL report
    source.setReportUnitURI("/reports/samples/AllAccounts");
    Map params = new HashMap();
    source.setParametersMap(params);

    ReportJobSimpleTrigger trigger = new ReportJobSimpleTrigger();
    trigger.setStartDate(report01StartDate);
    trigger.setOccurrenceCount(5);
    trigger.setRecurrenceInterval(1);
    trigger.setRecurrenceIntervalUnit(ReportJobSimpleTrigger.INTERVAL_MINUTE);

    ReportJobRepositoryDestination repositoryDestination = new ReportJobRepositoryDestination();
    // validator requires a REAL folder
    repositoryDestination.setFolderURI("/reports/samples");
    repositoryDestination.setOutputDescription("report output");
    repositoryDestination.setSequentialFilenames(true);
    repositoryDestination.setTimestampPattern("yyyyMMdd");
    repositoryDestination.setDefaultReportOutputFolderURI("/default/report_output/folder");
    repositoryDestination.setUsingDefaultReportOutputFolderURI(true);

    ReportJobMailNotification mailNotification = new ReportJobMailNotification();
    mailNotification.addTo("john@smith.com");
    mailNotification.setSubject("Scheduled report");
    mailNotification.setMessageText("Executed report");

    ReportJob job_01 = new ReportJob();
    job_01.setLabel("foo");
    job_01.setDescription("bar");
    job_01.setSource(source);
    job_01.setTrigger(trigger);
    job_01.setBaseOutputFilename("foo_"+(new Date().getTime()));
    job_01.addOutputFormat(ReportJob.OUTPUT_FORMAT_PDF);
    job_01.addOutputFormat(ReportJob.OUTPUT_FORMAT_RTF);
    job_01.setContentRepositoryDestination(repositoryDestination);
    job_01.setMailNotification(mailNotification);

    job_01 = m_reportSchedulingService.scheduleJob(m_executionContext, job_01);
    m_logger.info("scheduled Job 01 id='"+job_01.getId()+"' label='"+job_01.getLabel()+" for "+report01StartDate);

    assertNotNull(job_01);
    long jobId_01 = job_01.getId();

    boolean deleted = false;
    Byte triggerStateNormal = ReportJobRuntimeInformation.STATE_NORMAL;
    m_logger.info("search for Triggers in ReportJobRuntimeInformation.STATE_NORMAL="+
        ReportJobRuntimeInformation.STATE_NORMAL);
    List<Byte> triggerStateArray = new ArrayList<Byte>();
    triggerStateArray.add(triggerStateNormal);
    try {
      List<ReportJobSummary> summaryList =
          m_reportSchedulingService.getJobsByNextFireTime(m_executionContext,
              null,
              startDate,
              endDate,
              triggerStateArray);

      StringBuilder sb = new StringBuilder();
      int count = 0;
      for (ReportJobSummary js : summaryList) {
        ReportJobRuntimeInformation ri = js.getRuntimeInformation();
        Date nextFireTime = (ri == null ? null : ri.getNextFireTime());
        String time = (nextFireTime == null ? "NULL" : nextFireTime.toString());
        sb.append((count++) +" job "+js.getId()+" "+nextFireTime+" = "+time+"\n");
      }
      m_logger.info(sb.toString());

      m_logger.info("expecting query to return 1 report, got "+summaryList.size());
      int expectedNumberOfReports = 1;
      assertEquals("Error ! expected to get back 1 Report in window but instead we got "+summaryList.size(),expectedNumberOfReports, summaryList.size());

      ReportJobSummary rjs = summaryList.get(0);
      long id1 = rjs.getId();
      m_logger.info("Expected to get back jobId='"+jobId_01+"', got '"+id1+"'");
      assertEquals("Error ! expected to get back job01 id = '" + jobId_01 +
          "', but instead we got back "+id1, id1, jobId_01);

      try {
        m_reportJobsPersistenceService.deleteJob(m_executionContext, new ReportJobIdHolder(jobId_01));
        deleted = true;
      } catch (Throwable th) {
        m_logger.info("Error !  Throwable while attempting to delete job '"+th.getMessage()+"'");
      }

      try {
          job_01 = m_reportJobsPersistenceService.loadJob(m_executionContext, new ReportJobIdHolder(jobId_01));
          assertNull(job_01);
      } catch (ReportJobNotFoundException ad)  {}

    } finally {
      if (!deleted) {
        try {
          m_reportJobsPersistenceService.deleteJob(m_executionContext, new ReportJobIdHolder(jobId_01));
        } catch (Throwable th)  {}
      }
    }
  }



    private void testUpdateJobsByIDFAIL(long jobId_01, long jobId_02) {
        List<ReportJobIdHolder> jobIdHolders = new ArrayList<ReportJobIdHolder>();
        jobIdHolders.add(new ReportJobIdHolder(jobId_01));
        jobIdHolders.add(new ReportJobIdHolder(jobId_02));
        ReportJobModel jobModel= new ReportJobModel();
        ReportJobMailNotificationModel mailNotificationModel = new ReportJobMailNotificationModel();
        mailNotificationModel.setSubject("new subject");
        jobModel.setMailNotificationModel(mailNotificationModel);

        ReportJobSimpleTriggerModel trigger = new ReportJobSimpleTriggerModel();
		trigger.setStartDate(new Date());
		trigger.setOccurrenceCount(30);
		trigger.setRecurrenceInterval(15);
		trigger.setRecurrenceIntervalUnit(ReportJobSimpleTrigger.INTERVAL_HOUR);
        jobModel.setTriggerModel(trigger);
        boolean throwException = false;
        try {
            jobIdHolders = m_reportJobsPersistenceService.updateJobsByID(m_executionContext, jobIdHolders, jobModel, false);
        } catch (Exception ex) {
    //        System.out.println("EXPECTED EXCEPTION - " + ex.toString());
            throwException = true;
        }
        assertTrue(throwException);
        /**

        assertEquals(2, jobIdHolders.size());
        for (ReportJobIdHolder idHolder: jobIdHolders) {
           ReportJob reportJob = m_reportJobsPersistenceService.loadJob(m_executionContext, idHolder);
           assertEquals("new subject", reportJob.getMailNotification().getSubject());
           if (reportJob.getId() == jobId_01) {
                assertTrue(reportJob.getTrigger() instanceof ReportJobSimpleTrigger);
                assertEquals(ReportJobSimpleTrigger.INTERVAL_HOUR, ((ReportJobSimpleTrigger) reportJob.getTrigger()).getRecurrenceIntervalUnit().byteValue());
           } else {
              assertTrue(reportJob.getTrigger() instanceof ReportJobCalendarTrigger);
           }
        }
        **/
    }
        private void testUpdateJobsByIDFAILInvalidID(long jobId_01, long jobId_02) {
        List<ReportJobIdHolder> jobIdHolders = new ArrayList<ReportJobIdHolder>();
        jobIdHolders.add(new ReportJobIdHolder(jobId_01));
        jobIdHolders.add(new ReportJobIdHolder(jobId_02));
        jobIdHolders.add(new ReportJobIdHolder(111111111111l));
        ReportJobModel jobModel= new ReportJobModel();
        ReportJobMailNotificationModel mailNotificationModel = new ReportJobMailNotificationModel();
        mailNotificationModel.setSubject("new subject");
        jobModel.setMailNotificationModel(mailNotificationModel);

        boolean throwException = false;
        try {
            jobIdHolders = m_reportJobsPersistenceService.updateJobsByID(m_executionContext, jobIdHolders, jobModel, false);
        } catch (Exception ex) {
            throwException = true;
        }
        assertTrue(throwException);
    }

    private void testUpdateJobsByIDFAILInvalidPath(long jobId_01, long jobId_02) {
        List<ReportJobIdHolder> jobIdHolders = new ArrayList<ReportJobIdHolder>();
        jobIdHolders.add(new ReportJobIdHolder(jobId_01));
        jobIdHolders.add(new ReportJobIdHolder(jobId_02));
        ReportJobModel jobModel= new ReportJobModel();
        ReportJobRepositoryDestinationModel destinationModel = new ReportJobRepositoryDestinationModel();
        destinationModel.setFolderURI("SamplePath");
        destinationModel.setSaveToRepository(true);
        destinationModel.setUsingDefaultReportOutputFolderURI(false);
        jobModel.setContentRepositoryDestinationModel(destinationModel);
        jobModel.setBaseOutputFilename("ABC");
        boolean throwException = false;
        try {
            jobIdHolders = m_reportJobsPersistenceService.updateJobsByID(m_executionContext, jobIdHolders, jobModel, false);
        } catch (Exception ex) {
            throwException = true;
        }
        assertTrue(throwException);
    }

     private void testUpdateJobsByID(long jobId_01, long jobId_02) {
        List<ReportJobIdHolder> jobIdHolders = new ArrayList<ReportJobIdHolder>();
        jobIdHolders.add(new ReportJobIdHolder(jobId_01));
        jobIdHolders.add(new ReportJobIdHolder(jobId_02));
        ReportJobModel jobModel= new ReportJobModel();
        ReportJobMailNotificationModel mailNotificationModel = new ReportJobMailNotificationModel();
        mailNotificationModel.setSubject("new subject");
        jobModel.setMailNotificationModel(mailNotificationModel);
        jobIdHolders = m_reportJobsPersistenceService.updateJobsByID(m_executionContext, jobIdHolders, jobModel, false);
        assertEquals(2, jobIdHolders.size());
        for (ReportJobIdHolder idHolder: jobIdHolders) {
           ReportJob reportJob = m_reportJobsPersistenceService.loadJob(m_executionContext, idHolder);
           assertEquals("new subject", reportJob.getMailNotification().getSubject());
        }
    }



    private void testUpdateJobsByReference(ReportJob job_01, ReportJob job_02) {
        List<ReportJob> jobHolders = new ArrayList<ReportJob>();
        jobHolders.add(job_01);
        jobHolders.add(job_02);
        ReportJobModel jobModel= new ReportJobModel();
        ReportJobRepositoryDestinationModel destinationModel= new ReportJobRepositoryDestinationModel();
        destinationModel.setFolderURI("/test/report_scheduled");
        jobModel.setContentRepositoryDestinationModel(destinationModel);
        ReportJobMailNotificationModel mailNotificationModel = new ReportJobMailNotificationModel();
        mailNotificationModel.setSubject("new subject");
        jobModel.setMailNotificationModel(mailNotificationModel);

        ReportJobSimpleTriggerModel trigger = new ReportJobSimpleTriggerModel();
		trigger.setStartDate(new Date());
		trigger.setOccurrenceCount(50);
		trigger.setRecurrenceInterval(35);
		trigger.setRecurrenceIntervalUnit(ReportJobSimpleTrigger.INTERVAL_MINUTE);
        jobModel.setTrigger(trigger);

        jobHolders = m_reportJobsPersistenceService.updateJobs(m_executionContext, jobHolders, jobModel, true);
        assertEquals(2, jobHolders.size());
        for (ReportJob reportJob: jobHolders) {
           reportJob = m_reportJobsPersistenceService.loadJob(m_executionContext, new ReportJobIdHolder(reportJob.getId()));
           assertEquals("/test/report_scheduled", reportJob.getContentRepositoryDestination().getFolderURI());
           assertEquals("new subject", reportJob.getMailNotification().getSubject());
           assertTrue(reportJob.getTrigger() instanceof ReportJobSimpleTrigger);
           assertEquals(ReportJobSimpleTrigger.INTERVAL_MINUTE, ((ReportJobSimpleTrigger) reportJob.getTrigger()).getRecurrenceIntervalUnit().byteValue());
        }
    }

    private void testSorting(long jobId_01, long jobId_02) {
    //    System.out.println("\n Test Sorting \n");
        List jobs = m_reportJobsPersistenceService.listJobs(m_executionContext, null, 0, -1, ReportJobModel.ReportJobSortType.SORTBY_REPORTNAME, true);
        assertEquals(jobId_02, ((ReportJobSummary) jobs.get(0)).getId());
        assertEquals(jobId_01, ((ReportJobSummary) jobs.get(1)).getId());
    }

    private void testSortingDES(long jobId_01, long jobId_02) {
    //    System.out.println("\n Test Sorting \n");
        List jobs = m_reportJobsPersistenceService.listJobs(m_executionContext, null, 0, -1, ReportJobModel.ReportJobSortType.SORTBY_REPORTNAME, false);
        assertEquals(jobId_01, ((ReportJobSummary) jobs.get(0)).getId());
        assertEquals(jobId_02, ((ReportJobSummary) jobs.get(1)).getId());
    }

    private void testSortingNONEDES(long jobId_01, long jobId_02) {
    //    System.out.println("\n Test Sorting \n");
        List jobs = m_reportJobsPersistenceService.listJobs(m_executionContext, null, 0, -1, ReportJobModel.ReportJobSortType.NONE, true);
        List jobs2 = m_reportJobsPersistenceService.listJobs(m_executionContext, null, 0, -1, ReportJobModel.ReportJobSortType.NONE, false);
        assertEquals(((ReportJobSummary) jobs.get(0)).getId(), ((ReportJobSummary) jobs2.get(1)).getId());
        assertEquals(((ReportJobSummary) jobs.get(1)).getId(), ((ReportJobSummary) jobs2.get(0)).getId());
    }

    private void testPagination(long jobId_01, long jobId_02) {
    //    System.out.println("\n Test Pagination \n");
        List jobs = m_reportJobsPersistenceService.listJobs(m_executionContext, null, 1, 1, ReportJobModel.ReportJobSortType.SORTBY_JOBNAME, true);
        assertEquals(jobId_01, ((ReportJobSummary) jobs.get(0)).getId());
    }

    private void testListJobs(long jobId_01, long jobId_02, String userName) {
            // test Mail Notification
            ReportJobModel criteria = new ReportJobModel();
            ReportJobMailNotificationModel mailNotificationModel = new ReportJobMailNotificationModel();
            mailNotificationModel.setSubject("updated subject");
            criteria.setMailNotificationModel(mailNotificationModel);
            ArrayList<Long> expectedJob01 = new ArrayList<Long>();
            expectedJob01.add(jobId_01);
            compareResult(criteria, expectedJob01);
            // test base report job
            criteria = new ReportJobModel();
            criteria.setLabel("foo");
            compareResult(criteria, expectedJob01);
            // test output format
            criteria = new ReportJobModel();
            Set<Byte> outputFormats = new HashSet<Byte>();
            outputFormats.add(ReportJob.OUTPUT_FORMAT_PDF);
            criteria.setOutputFormatsSet(outputFormats);
            ArrayList<Long> expectedJobBOTH = new ArrayList<Long>();
            expectedJobBOTH.add(jobId_01);
            expectedJobBOTH.add(jobId_02);
            compareResult(criteria, expectedJobBOTH);
            // test user name
            criteria = new ReportJobModel();
            criteria.setUsername(userName);
            compareResult(criteria, expectedJobBOTH);

            // test trigger
            criteria = new ReportJobModel();
            ReportJobCalendarTriggerModel reportJobCalendarTriggerModel = new ReportJobCalendarTriggerModel();
            TreeSet months = new TreeSet();
            months.add(new Byte((byte)1));
            months.add(new Byte((byte)2));
            months.add(new Byte((byte)3));
            reportJobCalendarTriggerModel.setMonths(months);
            criteria.setTriggerModel(reportJobCalendarTriggerModel);
            ArrayList<Long> expectedJob02 = new ArrayList<Long>();
            expectedJob02.add(jobId_02);
            compareResult(criteria, expectedJob02);
            // test data source
            criteria = new ReportJobModel();
            ReportJobSourceModel reportJobSourceModel = new ReportJobSourceModel();
            reportJobSourceModel.setReportUnitURI("/test/reportURI");
            criteria.setSourceModel(reportJobSourceModel);
            compareResult(criteria, expectedJob01);
            // test repository destination
            criteria = new ReportJobModel();
            ReportJobRepositoryDestinationModel reportJobRepositoryDestinationModel = new ReportJobRepositoryDestinationModel();
            reportJobRepositoryDestinationModel.setSequentialFilenames(false);
            criteria.setContentRepositoryDestinationModel(reportJobRepositoryDestinationModel);
            compareResult(criteria, expectedJob02);
            // test ftp info
            criteria = new ReportJobModel();
            reportJobRepositoryDestinationModel = new ReportJobRepositoryDestinationModel();
            FTPInfoModel ftpInfoModel = new FTPInfoModel();
            ftpInfoModel.setUserName("JohnSmith");
            reportJobRepositoryDestinationModel.setOutputFTPInfoModel(ftpInfoModel);
            criteria.setContentRepositoryDestinationModel(reportJobRepositoryDestinationModel);
            compareResult(criteria, expectedJob02);
            // should return none
            criteria.setMailNotificationModel(mailNotificationModel);
            compareResult(criteria, new ArrayList<Long>());
            // text email
            criteria = new ReportJobModel();
            mailNotificationModel = new ReportJobMailNotificationModel();
            ArrayList<String> toAddress = new ArrayList<String>();
            toAddress.add("peter@pan.com");
            mailNotificationModel.setToAddresses(toAddress);
            criteria.setMailNotificationModel(mailNotificationModel);
            compareResult(criteria, expectedJob02);
    }

    private void compareResult(ReportJobModel criteria, List<Long> expectedJobIDs) {
        	List jobs = m_reportJobsPersistenceService.listJobs(m_executionContext, criteria, 0, -1, null, true);
            if ((jobs == null) || (jobs.size() == 0)) {
                assertTrue((expectedJobIDs == null) || (expectedJobIDs.size() == 0));
                return;
            }
			assertNotNull(jobs);
			assertEquals(expectedJobIDs.size(), jobs.size());
			boolean found = false;
			for (Iterator it = jobs.iterator(); it.hasNext();) {
				Object element = it.next();
				assertTrue(element instanceof ReportJobSummary);
				ReportJobSummary summary = (ReportJobSummary) element;
				if (expectedJobIDs.contains(summary.getId())) {
					found = true;
					break;
				}
			}
			assertTrue(found);
    }

    /**
     *  doLoggingTest
     */
    @Test(dependsOnMethods = "doPersistenceTest")
    public void doLoggingTest() {
        m_logger.info("ReportSchedulingTestTestNG => doLoggingTest() called");

        LogEvent event = m_loggingService.instantiateLogEvent();
        event.setComponent(ReportExecutionJob.LOGGING_COMPONENT);
        event.setType(LogEvent.TYPE_ERROR);
        event.setMessageCode("log.error.report.job.failed");

        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        printWriter.println("Quartz Job: testJob");
        printWriter.println("Quartz Trigger: testTrigger");

        printWriter.println("Exceptions:");

        try {
            int[] i = new int[2];
            int j = i[5];

        } catch (Exception e) {
            e.printStackTrace(printWriter);
        }

        printWriter.flush();
        event.setText(writer.toString());
        event.setState(LogEvent.STATE_UNREAD);

        m_loggingService.log(event);

        eventId = event.getId();
    }
}

