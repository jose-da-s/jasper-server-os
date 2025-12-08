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

package com.jaspersoft.jasperserver.jaxrs.job;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.FTPInfo;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJob;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobAlert;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobCalendarTrigger;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobMailNotification;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobRepositoryDestination;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobSimpleTrigger;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobSource;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobTrigger;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.ContentResourceImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.InputControlImpl;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.dto.common.ExportType;
import com.jaspersoft.jasperserver.dto.common.OutputFormat;
import com.jaspersoft.jasperserver.dto.connection.FtpConnection;
import com.jaspersoft.jasperserver.dto.job.ClientCalendarDaysType;
import com.jaspersoft.jasperserver.dto.job.ClientIntervalUnitType;
import com.jaspersoft.jasperserver.dto.job.ClientJobAlert;
import com.jaspersoft.jasperserver.dto.job.ClientJobAlertRecipient;
import com.jaspersoft.jasperserver.dto.job.ClientJobAlertState;
import com.jaspersoft.jasperserver.dto.job.ClientJobCalendarTrigger;
import com.jaspersoft.jasperserver.dto.job.ClientJobFtpInfo;
import com.jaspersoft.jasperserver.dto.job.ClientJobMailNotification;
import com.jaspersoft.jasperserver.dto.job.ClientJobRepositoryDestination;
import com.jaspersoft.jasperserver.dto.job.ClientJobSimpleTrigger;
import com.jaspersoft.jasperserver.dto.job.ClientJobSource;
import com.jaspersoft.jasperserver.dto.job.ClientJobTrigger;
import com.jaspersoft.jasperserver.dto.job.ClientMailNotificationSendType;
import com.jaspersoft.jasperserver.dto.job.ClientReportJob;
import com.jaspersoft.jasperserver.dto.job.ProtCommand;
import com.jaspersoft.jasperserver.inputcontrols.cascade.CascadeResourceNotFoundException;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlsLogicService;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlsValidationException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.types.date.FixedDate;
import net.sf.jasperreports.types.date.FixedTimestamp;
import net.sf.jasperreports.types.date.RelativeTimestampRange;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.i18n.LocaleContextHolder;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * <p>
 * <p>
 *
 * @author tetiana.iefimenko
 * @version $Id$
 * @see
 */
public class ReportJobConverterTest {
    private static final String AMERICA_NEW_YORK = "America/New_York";
    private static final String AMERICA_LOS_ANGELES = "America/Los_Angeles";
    @Mock
    private InputControlsLogicService inputControlsLogicService;
    @Mock
    private RepositoryService repositoryService;

    @InjectMocks
    private ReportJobConverter converter = spy(new ReportJobConverter());

    private ClientReportJob clientJobWithCalendarTrigger;
    private ReportJob serverJobWithCalendarTrigger;
    private ClientReportJob clientJobWithSimpleTrigger;
    private ReportJob serverJobWithSimpleTrigger;
    private Timestamp timestamp;
    private TreeSet<Byte> weekDays;
    private TreeSet<Byte> monthDays;
    private Date date;
    private LinkedHashMap<String, String[]> clientSourceParameters;
    LinkedHashMap<String, Object> serverSourceParameters;

    private TimeZone losAngelesTimezone = TimeZone.getTimeZone(AMERICA_LOS_ANGELES);
    private TimeZone newYorkTimezone = TimeZone.getTimeZone(AMERICA_NEW_YORK);

    private ExecutionContext ctx  = ExecutionContextImpl.getRuntimeExecutionContext();

    @BeforeMethod
    public void setUp() throws InputControlsValidationException, CascadeResourceNotFoundException {
        initMocks(this);
        Mockito.doReturn(clientSourceParameters).when(converter).extractFormatParameters(any(ReportJobSource.class));
        Mockito.doReturn(clientSourceParameters).when(converter).extractFormatParameters(any(ReportJobSource.class));

        Mockito.doReturn(new InputControlImpl()).when(repositoryService).getResource(any(ExecutionContext.class), anyString());
        Mockito.doReturn(serverSourceParameters).when(inputControlsLogicService).getTypedParameters(anyString(), anyMap());

        date = Calendar.getInstance().getTime();
        timestamp = new Timestamp(date.getTime());
        weekDays = new TreeSet<Byte>() {{
            add((byte) 1);
            add((byte) 3);
            add((byte) 5);
        }};
        monthDays = new TreeSet<Byte>() {{
            add((byte) 11);
            add((byte) 31);
            add((byte) 15);
        }};
        clientSourceParameters = new LinkedHashMap<String, String[]>();
        clientSourceParameters.put("Country_multi_select", new String[]{"Mexico"});
        clientSourceParameters.put("Cascading_name_single_select", new String[]{"Chin-Lovell Engineering Associates"});
        clientSourceParameters.put("Cascading_state_multi_select", new String[]{"DF", "Jalisco", "Mexico"});
        serverSourceParameters = new LinkedHashMap<String, Object>();
        serverSourceParameters.put("Country_multi_select", new String[]{"Mexico"});
        serverSourceParameters.put("Cascading_name_single_select", new String[]{"Chin-Lovell Engineering Associates"});
        serverSourceParameters.put("Cascading_state_multi_select", new String[]{"DF", "Jalisco", "Mexico"});
        serverSourceParameters.put(JRParameter.REPORT_TIME_ZONE, TimeZone.getTimeZone("America/Los_Angeles"));
        clientJobWithCalendarTrigger = new ClientReportJob();
        clientJobWithCalendarTrigger.
                setId(10L).
                setVersion(10).
                setUsername("userName").
                setLabel("label").
                setDescription("description").
                setCreationDate(timestamp).
                setBaseOutputFilename("outputDirectory").
                setOutputLocale("en").
                setOutputTimeZone("America/Los_Angeles").
                setExportType(ExportType.DEFAULT);

        ClientJobSource clientJobSource = new ClientJobSource();
        clientJobSource.
                setReportUnitURI("reportUri").
                setParameters(clientSourceParameters);
        clientJobWithCalendarTrigger.setSource(clientJobSource);
        Set<OutputFormat> outputFormats = new HashSet<OutputFormat>() {{
            add(OutputFormat.PDF);
            add(OutputFormat.PNG);
            add(OutputFormat.JSON);
        }};
        clientJobWithCalendarTrigger.setOutputFormats(outputFormats);

        ClientJobRepositoryDestination clientJobRepositoryDestination = new ClientJobRepositoryDestination();
        clientJobRepositoryDestination.
                setFolderURI("folderURI").
                setId(10L).
                setOutputDescription("outputDescription").
                setOverwriteFiles(true).
                setSequentialFilenames(true).
                setVersion(10).
                setTimestampPattern("yyyyMMddHHmm").
                setSaveToRepository(false).
                setDefaultReportOutputFolderURI("defaultReportOutputFolderURI").
                setUsingDefaultReportOutputFolderURI(true).
                setOutputLocalFolder("outputLocalFolder");
        ClientJobFtpInfo clientJobFtpInfo = new ClientJobFtpInfo();
        clientJobFtpInfo.setUserName("userName").
                setPassword("password").
                setFolderPath("folderPath").
                setServerName("servername").
                setType(FtpConnection.FtpType.ftp).
                setProtocol("TLS").
                setPort(21).
                setImplicit(true).
                setPbsz(0L).
                setProt(ProtCommand.C.toString()).
                setPropertiesMap(new LinkedHashMap<String, String>()).
                setSshKey("/sshKeys/myOpenSSHKey").
                setSshPassphrase("mySecurePhrase");

        clientJobRepositoryDestination.setOutputFTPInfo(clientJobFtpInfo);
        clientJobWithCalendarTrigger.setRepositoryDestination(clientJobRepositoryDestination);

        ClientJobMailNotification clientJobMailNotification = new ClientJobMailNotification();
        clientJobMailNotification.
                setVersion(10).
                setId(10L).
                setBccAddresses(Collections.singletonList("someBccAddress@someDomain.com")).
                setCcAddresses(Collections.singletonList("someCcAddress@someDomain.com")).
                setToAddresses(Collections.singletonList("someToAddress@someDomain.com")).
                setIncludingStackTraceWhenJobFails(true).
                setMessageText("Sample report message").
                setResultSendType(ClientMailNotificationSendType.SEND_ATTACHMENT).
                setSkipEmptyReports(true).
                setSkipNotificationWhenJobFails(true).
                setSubject("Sample Report Subject").
                setMessageTextWhenJobFails("Sample fail message");
        clientJobWithCalendarTrigger.setMailNotification(clientJobMailNotification);
        ClientJobAlert clientJobAlert = new ClientJobAlert();
        clientJobAlert.
                setId(10L).
                setVersion(10).
                setRecipient(ClientJobAlertRecipient.NONE).
                setJobState(ClientJobAlertState.NONE).
                setMessageText("Sample success message").
                setMessageTextWhenJobFails("Sample failure message").
                setSubject("Sample Status Subject").
                setIncludingStackTrace(true).
                setIncludingReportJobInfo(true).
                setToAddresses(Collections.singletonList("someToAddress@someDomain.com"));
        clientJobWithCalendarTrigger.setAlert(clientJobAlert);

        clientJobWithSimpleTrigger = new ClientReportJob(clientJobWithCalendarTrigger);

        ClientJobTrigger clientJobCalendarTrigger = new ClientJobCalendarTrigger();
        clientJobCalendarTrigger.setId(10L).
                setVersion(10).
                setTimezone("en").
                setCalendarName("calendarName").
                setStartType(1).
                setStartDate(date).
                setEndDate(date).
                setMisfireInstruction(0);
        ((ClientJobCalendarTrigger) clientJobCalendarTrigger).
                setMinutes("1-10").
                setHours("8-16").
                setDaysType(ClientCalendarDaysType.WEEK).
                setWeekDays(weekDays).
                setMonthDays("1,3,5-22").
                setMonths(monthDays);
        clientJobWithCalendarTrigger.setTrigger(clientJobCalendarTrigger);

        ClientJobTrigger clientJobSimpleTrigger = new ClientJobSimpleTrigger();
        clientJobSimpleTrigger.setId(10L).
                setVersion(10).
                setTimezone("en").
                setCalendarName("calendarName").
                setStartType(1).
                setStartDate(date).
                setEndDate(date).
                setMisfireInstruction(0);
        ((ClientJobSimpleTrigger) clientJobSimpleTrigger).
                setOccurrenceCount(1).
                setRecurrenceInterval(2).
                setRecurrenceIntervalUnit(ClientIntervalUnitType.DAY);
        clientJobWithSimpleTrigger.setTrigger(clientJobSimpleTrigger);

        serverJobWithCalendarTrigger = new ReportJob();
        serverJobWithCalendarTrigger.setId(10L);
        serverJobWithCalendarTrigger.setVersion(10);
        serverJobWithCalendarTrigger.setUsername("userName");
        serverJobWithCalendarTrigger.setLabel("label");
        serverJobWithCalendarTrigger.setDescription("description");
        serverJobWithCalendarTrigger.setCreationDate(timestamp);
        serverJobWithCalendarTrigger.setBaseOutputFilename("outputDirectory");
        serverJobWithCalendarTrigger.setOutputLocale("en");
        serverJobWithCalendarTrigger.setOutputTimeZone("America/Los_Angeles");

        ReportJobSource serverJobSource = new ReportJobSource();
        serverJobSource.setReportUnitURI("reportUri");
        serverJobSource.setParameters(serverSourceParameters);
        serverJobWithCalendarTrigger.setSource(serverJobSource);
        Set<Byte> outputFormatsCodes = new HashSet<Byte>() {{
            add((byte) 1);
            add((byte) 16);
            add((byte) 15);
        }};
        serverJobWithCalendarTrigger.setOutputFormatsSet(outputFormatsCodes);

        ReportJobRepositoryDestination serverRepositoryDestination = new ReportJobRepositoryDestination();
        serverRepositoryDestination.setFolderURI("folderURI");
        serverRepositoryDestination.setId(10L);
        serverRepositoryDestination.setOutputDescription("outputDescription");
        serverRepositoryDestination.setOverwriteFiles(true);
        serverRepositoryDestination.setSequentialFilenames(true);
        serverRepositoryDestination.setVersion(10);
        serverRepositoryDestination.setTimestampPattern("yyyyMMddHHmm");
        serverRepositoryDestination.setSaveToRepository(false);
        serverRepositoryDestination.setDefaultReportOutputFolderURI("defaultReportOutputFolderURI");
        serverRepositoryDestination.setUsingDefaultReportOutputFolderURI(true);
        serverRepositoryDestination.setOutputLocalFolder("outputLocalFolder");

        FTPInfo serverFtpInfo = new FTPInfo();
        serverFtpInfo.setUserName("userName");
        serverFtpInfo.setPassword("password");
        serverFtpInfo.setFolderPath("folderPath");
        serverFtpInfo.setServerName("servername");
        serverFtpInfo.setType("TYPE_FTP");
        serverFtpInfo.setProtocol("TLS");
        serverFtpInfo.setPort(21);
        serverFtpInfo.setImplicit(true);
        serverFtpInfo.setPbsz(0L);
        serverFtpInfo.setProt(ProtCommand.C.toString());
        serverFtpInfo.setSshKey("/sshKeys/myOpenSSHKey");
        serverFtpInfo.setSshPassphrase("mySecurePhrase");

        serverRepositoryDestination.setOutputFTPInfo(serverFtpInfo);
        serverJobWithCalendarTrigger.setContentRepositoryDestination(serverRepositoryDestination);

        ReportJobMailNotification serverMailNotification = new ReportJobMailNotification();
        serverMailNotification.setVersion(10);
        serverMailNotification.setId(10L);
        serverMailNotification.setBccAddresses(Collections.singletonList("someBccAddress@someDomain.com"));
        serverMailNotification.setCcAddresses(Collections.singletonList("someCcAddress@someDomain.com"));
        serverMailNotification.setToAddresses(Collections.singletonList("someToAddress@someDomain.com"));
        serverMailNotification.setIncludingStackTraceWhenJobFails(true);
        serverMailNotification.setMessageText("Sample report message");
        serverMailNotification.setResultSendTypeCode((byte) 2);
        serverMailNotification.setSkipEmptyReports(true);
        serverMailNotification.setSkipNotificationWhenJobFails(true);
        serverMailNotification.setSubject("Sample Report Subject");
        serverMailNotification.setMessageTextWhenJobFails("Sample fail message");
        serverJobWithCalendarTrigger.setMailNotification(serverMailNotification);

        ReportJobAlert serverJobAlert = new ReportJobAlert();
        serverJobAlert.setId(10L);
        serverJobAlert.setVersion(10);
        serverJobAlert.setRecipient(ReportJobAlert.Recipient.NONE);
        serverJobAlert.setJobState(ReportJobAlert.JobState.NONE);
        serverJobAlert.setMessageText("Sample success message");
        serverJobAlert.setMessageTextWhenJobFails("Sample failure message");
        serverJobAlert.setSubject("Sample Status Subject");
        serverJobAlert.setIncludingStackTrace(true);
        serverJobAlert.setIncludingReportJobInfo(true);
        serverJobAlert.setToAddresses(Collections.singletonList("someToAddress@someDomain.com"));
        serverJobWithCalendarTrigger.setAlert(serverJobAlert);

        serverJobWithSimpleTrigger = new ReportJob(serverJobWithCalendarTrigger);

        serverJobWithSimpleTrigger.setId(serverJobWithCalendarTrigger.getId());
        serverJobWithSimpleTrigger.setVersion(serverJobWithCalendarTrigger.getVersion());
        serverJobWithSimpleTrigger.getContentRepositoryDestination().
                setVersion(serverJobWithCalendarTrigger.getContentRepositoryDestination().getVersion());
        serverJobWithSimpleTrigger.getMailNotification().
                setId(serverJobWithCalendarTrigger.getMailNotification().getId());
        serverJobWithSimpleTrigger.getMailNotification().
                setVersion(serverJobWithCalendarTrigger.getMailNotification().getVersion());
        serverJobWithSimpleTrigger.getMailNotification().
                setSkipNotificationWhenJobFails(serverJobWithCalendarTrigger.getMailNotification().isSkipNotificationWhenJobFails());
        serverJobWithSimpleTrigger.setAlert(serverJobAlert);

        ReportJobTrigger reportJobCalendarTrigger = new ReportJobCalendarTrigger();
        reportJobCalendarTrigger.setId(10L);
        reportJobCalendarTrigger.setVersion(10);
        reportJobCalendarTrigger.setTimezone("en");
        reportJobCalendarTrigger.setCalendarName("calendarName");
        reportJobCalendarTrigger.setStartType((byte) 1);
        reportJobCalendarTrigger.setStartDate(date);
        reportJobCalendarTrigger.setEndDate(date);
        reportJobCalendarTrigger.setMisfireInstruction(0);
        ((ReportJobCalendarTrigger) reportJobCalendarTrigger).setMinutes("1-10");
        ((ReportJobCalendarTrigger) reportJobCalendarTrigger).setHours("8-16");
        ((ReportJobCalendarTrigger) reportJobCalendarTrigger).setDaysTypeCode((byte) 2);
        ((ReportJobCalendarTrigger) reportJobCalendarTrigger).setWeekDays(weekDays);
        ((ReportJobCalendarTrigger) reportJobCalendarTrigger).setMonthDays("1,3,5-22");
        ((ReportJobCalendarTrigger) reportJobCalendarTrigger).setMonths(monthDays);
        serverJobWithCalendarTrigger.setTrigger(reportJobCalendarTrigger);


        ReportJobTrigger reportJobSimpleTrigger = new ReportJobSimpleTrigger();
        reportJobSimpleTrigger.setId(10L);
        reportJobSimpleTrigger.setVersion(10);
        reportJobSimpleTrigger.setTimezone("en");
        reportJobSimpleTrigger.setCalendarName("calendarName");
        reportJobSimpleTrigger.setStartType((byte) 1);
        reportJobSimpleTrigger.setStartDate(date);
        reportJobSimpleTrigger.setEndDate(date);
        reportJobSimpleTrigger.setMisfireInstruction(0);
        ((ReportJobSimpleTrigger) reportJobSimpleTrigger).setOccurrenceCount(1);
        ((ReportJobSimpleTrigger) reportJobSimpleTrigger).setRecurrenceInterval(2);
        ((ReportJobSimpleTrigger) reportJobSimpleTrigger).setRecurrenceIntervalUnit((byte) 3);
        serverJobWithSimpleTrigger.setTrigger(reportJobSimpleTrigger);
    }

    @AfterMethod
    public void after() {
    reset(repositoryService, inputControlsLogicService, converter);
    }

    @Test
    public void testConvertClientObjectWithCalendarTriggerToServer() throws InputControlsValidationException, CascadeResourceNotFoundException {
        Mockito.doReturn(new InputControlImpl()).when(repositoryService).getResource(any(ExecutionContext.class), anyString());
        Mockito.doReturn(serverSourceParameters).when(inputControlsLogicService).getTypedParameters(anyString(), anyMap());

        ReportJob reportJob = converter.toServer(ctx, clientJobWithCalendarTrigger, null);

        assertEquals(reportJob.getId(), serverJobWithCalendarTrigger.getId());
        assertEquals(reportJob.getVersion(), serverJobWithCalendarTrigger.getVersion());
        assertNotNull(reportJob.getUsername());
        assertEquals(reportJob.getUsername(), serverJobWithCalendarTrigger.getUsername());
        assertNotNull(reportJob.getLabel());
        assertEquals(reportJob.getLabel(), serverJobWithCalendarTrigger.getLabel());
        assertNotNull(reportJob.getDescription());
        assertEquals(reportJob.getDescription(), serverJobWithCalendarTrigger.getDescription());
        assertNotNull(reportJob.getCreationDate());
        assertEquals(reportJob.getCreationDate(), serverJobWithCalendarTrigger.getCreationDate());
        assertNotNull(reportJob.getBaseOutputFilename());
        assertEquals(reportJob.getBaseOutputFilename(), serverJobWithCalendarTrigger.getBaseOutputFilename());
        assertNotNull(reportJob.getOutputFormatsSet());
        assertEquals(reportJob.getOutputFormatsSet().size(), serverJobWithCalendarTrigger.getOutputFormatsSet().size());
        assertEquals(reportJob.getOutputFormatsSet(), serverJobWithCalendarTrigger.getOutputFormatsSet());
        assertNotNull(reportJob.getOutputLocale());
        assertEquals(reportJob.getOutputLocale(), serverJobWithCalendarTrigger.getOutputLocale());

        ReportJobCalendarTrigger resultTrigger = (ReportJobCalendarTrigger) reportJob.getTrigger();
        ReportJobCalendarTrigger expectedTrigger = (ReportJobCalendarTrigger) serverJobWithCalendarTrigger.getTrigger();
        assertNotNull(resultTrigger);
        assertEquals(resultTrigger.getId(), expectedTrigger.getId());
        assertNotNull(resultTrigger.getTimezone());
        assertEquals(resultTrigger.getTimezone(), expectedTrigger.getTimezone());
        assertNotNull(resultTrigger.getCalendarName());
        assertEquals(resultTrigger.getCalendarName(), expectedTrigger.getCalendarName());
        assertNotNull(resultTrigger.getStartType());
        assertEquals(resultTrigger.getStartType(), expectedTrigger.getStartType());
        assertNotNull(resultTrigger.getStartDate());
        assertEquals(resultTrigger.getStartDate().getTime(), expectedTrigger.getStartDate().getTime());
        assertNotNull(resultTrigger.getEndDate());
        assertEquals(resultTrigger.getEndDate(), expectedTrigger.getEndDate());
        assertEquals(resultTrigger.getMisfireInstruction(), expectedTrigger.getMisfireInstruction());
        assertNotNull(resultTrigger.getMinutes());
        assertEquals(resultTrigger.getMinutes(), expectedTrigger.getMinutes());
        assertNotNull(resultTrigger.getHours());
        assertEquals(resultTrigger.getHours(), expectedTrigger.getHours());
        assertNotNull(resultTrigger.getDaysTypeCode());
        assertEquals(resultTrigger.getDaysTypeCode(), expectedTrigger.getDaysTypeCode());
        assertNotNull(resultTrigger.getWeekDays());
        assertFalse(resultTrigger.getWeekDays().size() == 0);
        assertEquals(resultTrigger.getWeekDays(), expectedTrigger.getWeekDays());
        assertNotNull(resultTrigger.getMonthDays());
        assertEquals(resultTrigger.getMonthDays(), expectedTrigger.getMonthDays());
        assertNotNull(resultTrigger.getMonths());
        assertEquals(resultTrigger.getMonths(), expectedTrigger.getMonths());

        ReportJobSource resultSource = reportJob.getSource();
        ReportJobSource expectedSource = serverJobWithCalendarTrigger.getSource();
        assertNotNull(resultSource);
        assertNotNull(resultSource.getReportUnitURI());
        assertEquals(resultSource.getReportUnitURI(), expectedSource.getReportUnitURI());
        assertNotNull(resultSource.getParameters());
        assertEquals(resultSource.getParameters().size(), expectedSource.getParameters().size());
        assertEquals(resultSource.getParameters(), expectedSource.getParameters());

        ReportJobRepositoryDestination resultRepositoryDestination = reportJob.getContentRepositoryDestination();
        ReportJobRepositoryDestination expectedRepositoryDestination = serverJobWithCalendarTrigger.getContentRepositoryDestination();
        assertNotNull(resultRepositoryDestination);
        assertEquals(resultRepositoryDestination.getId(), expectedRepositoryDestination.getId());
        assertEquals(resultRepositoryDestination.getVersion(), expectedRepositoryDestination.getVersion());
        assertNotNull(resultRepositoryDestination.getFolderURI());
        assertEquals(resultRepositoryDestination.getFolderURI(), expectedRepositoryDestination.getFolderURI());
        assertEquals(resultRepositoryDestination.isSequentialFilenames(), expectedRepositoryDestination.isSequentialFilenames());
        assertEquals(resultRepositoryDestination.isOverwriteFiles(), expectedRepositoryDestination.isOverwriteFiles());
        assertEquals(resultRepositoryDestination.getTimestampPattern(), expectedRepositoryDestination.getTimestampPattern());
        assertEquals(resultRepositoryDestination.isSaveToRepository(), expectedRepositoryDestination.isSaveToRepository());
        assertEquals(resultRepositoryDestination.getDefaultReportOutputFolderURI(), expectedRepositoryDestination.getDefaultReportOutputFolderURI());
        assertEquals(resultRepositoryDestination.isUsingDefaultReportOutputFolderURI(), expectedRepositoryDestination.isUsingDefaultReportOutputFolderURI());
        assertEquals(resultRepositoryDestination.getOutputLocalFolder(), expectedRepositoryDestination.getOutputLocalFolder());

        FTPInfo resultOutputFTPInfo = resultRepositoryDestination.getOutputFTPInfo();
        FTPInfo expectedOutputFTPInfo = expectedRepositoryDestination.getOutputFTPInfo();
        assertNotNull(resultOutputFTPInfo);
        assertEquals(resultOutputFTPInfo.getUserName(), expectedOutputFTPInfo.getUserName());
        assertEquals(resultOutputFTPInfo.getPassword(), expectedOutputFTPInfo.getPassword());
        assertEquals(resultOutputFTPInfo.getFolderPath(), expectedOutputFTPInfo.getFolderPath());
        assertEquals(resultOutputFTPInfo.getServerName(), expectedOutputFTPInfo.getServerName());
        assertNotNull(resultOutputFTPInfo.getPropertiesMap());
        assertFalse(resultOutputFTPInfo.getPropertiesMap().size() == 0);
        assertTrue(resultOutputFTPInfo.getPropertiesMap().size() == 8);
        assertEquals(resultOutputFTPInfo.getType(), expectedOutputFTPInfo.getType());
        assertEquals(resultOutputFTPInfo.getPort(), expectedOutputFTPInfo.getPort());
        assertEquals(resultOutputFTPInfo.getProtocol(), expectedOutputFTPInfo.getProtocol());
        assertEquals(resultOutputFTPInfo.getPbsz(), expectedOutputFTPInfo.getPbsz());
        assertEquals(resultOutputFTPInfo.getSshKey(), expectedOutputFTPInfo.getSshKey());
        assertEquals(resultOutputFTPInfo.getSshPassphrase(), expectedOutputFTPInfo.getSshPassphrase());
        assertEquals(resultOutputFTPInfo.isImplicit(), expectedOutputFTPInfo.isImplicit());


        ReportJobMailNotification resultMailNotification = reportJob.getMailNotification();
        ReportJobMailNotification expectedMailNotification = serverJobWithCalendarTrigger.getMailNotification();
        assertNotNull(resultMailNotification);
        assertEquals(resultMailNotification.getId(), expectedMailNotification.getId());
        assertEquals(resultMailNotification.getVersion(), expectedMailNotification.getVersion());

        assertNotNull(resultMailNotification.getToAddresses());
        assertFalse(resultMailNotification.getToAddresses().size() == 0);
        assertEquals(resultMailNotification.getToAddresses(), expectedMailNotification.getToAddresses());

        assertNotNull(resultMailNotification.getCcAddresses());
        assertFalse(resultMailNotification.getCcAddresses().size() == 0);
        assertEquals(resultMailNotification.getCcAddresses(), expectedMailNotification.getCcAddresses());

        assertNotNull(resultMailNotification.getBccAddresses());
        assertFalse(resultMailNotification.getBccAddresses().size() == 0);
        assertEquals(resultMailNotification.getBccAddresses(), expectedMailNotification.getBccAddresses());

        assertEquals(resultMailNotification.getSubject(), expectedMailNotification.getSubject());
        assertEquals(resultMailNotification.getMessageText(), expectedMailNotification.getMessageText());
        assertEquals(resultMailNotification.getResultSendTypeCode(), expectedMailNotification.getResultSendTypeCode());
        assertEquals(resultMailNotification.isSkipEmptyReports(), expectedMailNotification.isSkipEmptyReports());
        assertEquals(resultMailNotification.getMessageTextWhenJobFails(), expectedMailNotification.getMessageTextWhenJobFails());
        assertEquals(resultMailNotification.isIncludingStackTraceWhenJobFails(), expectedMailNotification.isIncludingStackTraceWhenJobFails());
        assertEquals(resultMailNotification.isSkipNotificationWhenJobFails(), expectedMailNotification.isSkipNotificationWhenJobFails());

        ReportJobAlert resultAlert = reportJob.getAlert();
        ReportJobAlert expectedAlert = serverJobWithCalendarTrigger.getAlert();
        assertNotNull(resultAlert);
        assertEquals(resultAlert.getId(), expectedAlert.getId());
        assertEquals(resultAlert.getVersion(), expectedAlert.getVersion());
        assertEquals(resultAlert.getRecipient(), expectedAlert.getRecipient());
        assertNotNull(resultAlert.getToAddresses());
        assertFalse(resultAlert.getToAddresses().size() == 0);
        assertEquals(resultAlert.getToAddresses(), expectedAlert.getToAddresses());
        assertEquals(resultAlert.getJobState(), expectedAlert.getJobState());
        assertEquals(resultAlert.getMessageText(), expectedAlert.getMessageText());
        assertEquals(resultAlert.getMessageTextWhenJobFails(), expectedAlert.getMessageTextWhenJobFails());
        assertEquals(resultAlert.getSubject(), expectedAlert.getSubject());
        assertEquals(resultAlert.isIncludingStackTrace(), expectedAlert.isIncludingStackTrace());
        assertEquals(resultAlert.isIncludingReportJobInfo(), expectedAlert.isIncludingReportJobInfo());
    }

    @Test
    public void testConvertClientObjectWithoutParametersToServer_returnDefaultParameters() throws InputControlsValidationException, CascadeResourceNotFoundException {
        clientJobWithCalendarTrigger.getSource().setParameters(null);
        Mockito.doReturn(new InputControlImpl()).when(repositoryService).getResource(any(ExecutionContext.class), anyString());
        Mockito.doReturn(serverSourceParameters).when(inputControlsLogicService).getTypedParameters(anyString(), anyMap());

        ReportJob reportJob = converter.toServer(ctx, clientJobWithCalendarTrigger, null);

        ReportJobSource resultSource = reportJob.getSource();
        ReportJobSource expectedSource = serverJobWithCalendarTrigger.getSource();
        assertNotNull(resultSource);
        assertNotNull(resultSource.getReportUnitURI());
        assertEquals(resultSource.getReportUnitURI(), expectedSource.getReportUnitURI());
        assertNotNull(resultSource.getParameters());
        assertEquals(resultSource.getParameters().size(), expectedSource.getParameters().size());
        assertEquals(resultSource.getParameters(), expectedSource.getParameters());

        Mockito.verify(repositoryService).getResource(any(ExecutionContext.class), anyString());
        Mockito.verify(inputControlsLogicService).getTypedParameters(anyString(), anyMap());
    }

    @Test
    public void testConvertClientObjectNotInputContainerToServer() throws InputControlsValidationException, CascadeResourceNotFoundException {
        Mockito.doReturn(new ContentResourceImpl()).when(repositoryService).getResource(any(ExecutionContext.class), anyString());
        Mockito.doReturn(serverSourceParameters).when(inputControlsLogicService).getTypedParameters(anyString(), anyMap());

        ReportJob reportJob = converter.toServer(ctx, clientJobWithCalendarTrigger, null);

        ReportJobSource resultSource = reportJob.getSource();
        ReportJobSource expectedSource = serverJobWithCalendarTrigger.getSource();
        assertNotNull(resultSource);
        assertNotNull(resultSource.getReportUnitURI());
        assertEquals(resultSource.getReportUnitURI(), expectedSource.getReportUnitURI());
        assertNotNull(resultSource.getParameters());
        assertEquals(resultSource.getParameters().size(), expectedSource.getParameters().size());

        Mockito.verify(repositoryService).getResource(any(ExecutionContext.class), anyString());
        Mockito.verifyNoInteractions(inputControlsLogicService);
    }

    @Test
    public void testConvertClientObjectNotExistingSource() throws InputControlsValidationException, CascadeResourceNotFoundException {
        Mockito.doReturn(null).when(repositoryService).getResource(any(ExecutionContext.class), anyString());
        Mockito.doReturn(serverSourceParameters).when(inputControlsLogicService).getTypedParameters(anyString(), anyMap());

        ReportJob reportJob = converter.toServer(ctx, clientJobWithCalendarTrigger, null);

        ReportJobSource resultSource = reportJob.getSource();
        ReportJobSource expectedSource = serverJobWithCalendarTrigger.getSource();
        assertNotNull(resultSource);
        assertNotNull(resultSource.getReportUnitURI());
        assertEquals(resultSource.getReportUnitURI(), expectedSource.getReportUnitURI());
        assertNotNull(resultSource.getParameters());
        assertTrue(resultSource.getParameters().isEmpty());

        Mockito.verify(repositoryService).getResource(any(ExecutionContext.class), anyString());
        Mockito.verifyNoInteractions(inputControlsLogicService);
    }


    @Test
    public void testConvertClientObjectWithSimpleTriggerToServer() throws InputControlsValidationException, CascadeResourceNotFoundException {
        Mockito.doReturn(serverSourceParameters).when(inputControlsLogicService).getTypedParameters(anyString(), anyMap());

        ReportJob reportJob = converter.toServer(ctx, clientJobWithSimpleTrigger, null);

        assertEquals(reportJob.getId(), serverJobWithSimpleTrigger.getId());
        assertEquals(reportJob.getVersion(), serverJobWithSimpleTrigger.getVersion());
        assertNotNull(reportJob.getUsername());
        assertEquals(reportJob.getUsername(), serverJobWithSimpleTrigger.getUsername());
        assertNotNull(reportJob.getLabel());
        assertEquals(reportJob.getLabel(), serverJobWithSimpleTrigger.getLabel());
        assertNotNull(reportJob.getDescription());
        assertEquals(reportJob.getDescription(), serverJobWithSimpleTrigger.getDescription());
        assertNotNull(reportJob.getCreationDate());
        assertEquals(reportJob.getCreationDate(), serverJobWithSimpleTrigger.getCreationDate());
        assertNotNull(reportJob.getBaseOutputFilename());
        assertEquals(reportJob.getBaseOutputFilename(), serverJobWithSimpleTrigger.getBaseOutputFilename());
        assertNotNull(reportJob.getOutputFormatsSet());
        assertEquals(reportJob.getOutputFormatsSet().size(), serverJobWithSimpleTrigger.getOutputFormatsSet().size());
        assertEquals(reportJob.getOutputFormatsSet(), serverJobWithSimpleTrigger.getOutputFormatsSet());
        assertNotNull(reportJob.getOutputLocale());
        assertEquals(reportJob.getOutputLocale(), serverJobWithSimpleTrigger.getOutputLocale());

        ReportJobSimpleTrigger resultTrigger = (ReportJobSimpleTrigger) reportJob.getTrigger();
        ReportJobSimpleTrigger expectedTrigger = (ReportJobSimpleTrigger) serverJobWithSimpleTrigger.getTrigger();
        assertNotNull(resultTrigger);
        assertEquals(resultTrigger.getId(), expectedTrigger.getId());
        assertNotNull(resultTrigger.getTimezone());
        assertEquals(resultTrigger.getTimezone(), expectedTrigger.getTimezone());
        assertNotNull(resultTrigger.getCalendarName());
        assertEquals(resultTrigger.getCalendarName(), expectedTrigger.getCalendarName());
        assertNotNull(resultTrigger.getStartType());
        assertEquals(resultTrigger.getStartType(), expectedTrigger.getStartType());
        assertNotNull(resultTrigger.getStartDate());
        assertEquals(resultTrigger.getStartDate().getTime(), expectedTrigger.getStartDate().getTime());
        assertNotNull(resultTrigger.getEndDate());
        assertEquals(resultTrigger.getEndDate(), expectedTrigger.getEndDate());
        assertEquals(resultTrigger.getMisfireInstruction(), expectedTrigger.getMisfireInstruction());
        assertEquals(resultTrigger.getOccurrenceCount(), expectedTrigger.getOccurrenceCount());
        assertNotNull(resultTrigger.getRecurrenceInterval());
        assertEquals(resultTrigger.getRecurrenceInterval(), expectedTrigger.getRecurrenceInterval());
        assertNotNull(resultTrigger.getRecurrenceIntervalUnit());
        assertEquals(resultTrigger.getRecurrenceIntervalUnit(), expectedTrigger.getRecurrenceIntervalUnit());

        ReportJobSource resultSource = reportJob.getSource();
        ReportJobSource expectedSource = serverJobWithSimpleTrigger.getSource();
        assertNotNull(resultSource);
        assertNotNull(resultSource.getReportUnitURI());
        assertEquals(resultSource.getReportUnitURI(), expectedSource.getReportUnitURI());
        assertNotNull(resultSource.getParameters());
        assertEquals(resultSource.getParameters().size(), expectedSource.getParameters().size());
        assertEquals(resultSource.getParameters(), expectedSource.getParameters());

        ReportJobRepositoryDestination resultRepositoryDestination = reportJob.getContentRepositoryDestination();
        ReportJobRepositoryDestination expectedRepositoryDestination = serverJobWithSimpleTrigger.getContentRepositoryDestination();
        assertNotNull(resultRepositoryDestination);
        assertEquals(resultRepositoryDestination.getId(), expectedRepositoryDestination.getId());
        assertEquals(resultRepositoryDestination.getVersion(), expectedRepositoryDestination.getVersion());
        assertNotNull(resultRepositoryDestination.getFolderURI());
        assertEquals(resultRepositoryDestination.getFolderURI(), expectedRepositoryDestination.getFolderURI());
        assertEquals(resultRepositoryDestination.isSequentialFilenames(), expectedRepositoryDestination.isSequentialFilenames());
        assertEquals(resultRepositoryDestination.isOverwriteFiles(), expectedRepositoryDestination.isOverwriteFiles());
        assertEquals(resultRepositoryDestination.getTimestampPattern(), expectedRepositoryDestination.getTimestampPattern());
        assertEquals(resultRepositoryDestination.isSaveToRepository(), expectedRepositoryDestination.isSaveToRepository());
        assertEquals(resultRepositoryDestination.getDefaultReportOutputFolderURI(), expectedRepositoryDestination.getDefaultReportOutputFolderURI());
        assertEquals(resultRepositoryDestination.isUsingDefaultReportOutputFolderURI(), expectedRepositoryDestination.isUsingDefaultReportOutputFolderURI());
        assertEquals(resultRepositoryDestination.getOutputLocalFolder(), expectedRepositoryDestination.getOutputLocalFolder());

        FTPInfo resultOutputFTPInfo = resultRepositoryDestination.getOutputFTPInfo();
        FTPInfo expectedOutputFTPInfo = expectedRepositoryDestination.getOutputFTPInfo();
        assertNotNull(resultOutputFTPInfo);
        assertEquals(resultOutputFTPInfo.getUserName(), expectedOutputFTPInfo.getUserName());
        assertEquals(resultOutputFTPInfo.getPassword(), expectedOutputFTPInfo.getPassword());
        assertEquals(resultOutputFTPInfo.getFolderPath(), expectedOutputFTPInfo.getFolderPath());
        assertEquals(resultOutputFTPInfo.getServerName(), expectedOutputFTPInfo.getServerName());
        assertNotNull(resultOutputFTPInfo.getPropertiesMap());
        assertFalse(resultOutputFTPInfo.getPropertiesMap().size() == 0);
        assertTrue(resultOutputFTPInfo.getPropertiesMap().size() == 8);
        assertEquals(resultOutputFTPInfo.getType(), expectedOutputFTPInfo.getType());
        assertEquals(resultOutputFTPInfo.getPort(), expectedOutputFTPInfo.getPort());
        assertEquals(resultOutputFTPInfo.getProtocol(), expectedOutputFTPInfo.getProtocol());
        assertEquals(resultOutputFTPInfo.getPbsz(), expectedOutputFTPInfo.getPbsz());
        assertEquals(resultOutputFTPInfo.getSshKey(), expectedOutputFTPInfo.getSshKey());
        assertEquals(resultOutputFTPInfo.getSshPassphrase(), expectedOutputFTPInfo.getSshPassphrase());
        assertEquals(resultOutputFTPInfo.isImplicit(), expectedOutputFTPInfo.isImplicit());


        ReportJobMailNotification resultMailNotification = reportJob.getMailNotification();
        ReportJobMailNotification expectedMailNotification = serverJobWithSimpleTrigger.getMailNotification();
        assertNotNull(resultMailNotification);
        assertEquals(resultMailNotification.getId(), expectedMailNotification.getId());
        assertEquals(resultMailNotification.getVersion(), expectedMailNotification.getVersion());

        assertNotNull(resultMailNotification.getToAddresses());
        assertFalse(resultMailNotification.getToAddresses().size() == 0);
        assertEquals(resultMailNotification.getToAddresses(), expectedMailNotification.getToAddresses());

        assertNotNull(resultMailNotification.getCcAddresses());
        assertFalse(resultMailNotification.getCcAddresses().size() == 0);
        assertEquals(resultMailNotification.getCcAddresses(), expectedMailNotification.getCcAddresses());

        assertNotNull(resultMailNotification.getBccAddresses());
        assertFalse(resultMailNotification.getBccAddresses().size() == 0);
        assertEquals(resultMailNotification.getBccAddresses(), expectedMailNotification.getBccAddresses());

        assertEquals(resultMailNotification.getSubject(), expectedMailNotification.getSubject());
        assertEquals(resultMailNotification.getMessageText(), expectedMailNotification.getMessageText());
        assertEquals(resultMailNotification.getResultSendTypeCode(), expectedMailNotification.getResultSendTypeCode());
        assertEquals(resultMailNotification.isSkipEmptyReports(), expectedMailNotification.isSkipEmptyReports());
        assertEquals(resultMailNotification.getMessageTextWhenJobFails(), expectedMailNotification.getMessageTextWhenJobFails());
        assertEquals(resultMailNotification.isIncludingStackTraceWhenJobFails(), expectedMailNotification.isIncludingStackTraceWhenJobFails());
        assertEquals(resultMailNotification.isSkipNotificationWhenJobFails(), expectedMailNotification.isSkipNotificationWhenJobFails());

        ReportJobAlert resultAlert = reportJob.getAlert();
        ReportJobAlert expectedAlert = serverJobWithSimpleTrigger.getAlert();
        assertNotNull(resultAlert);
        assertEquals(resultAlert.getId(), expectedAlert.getId());
        assertEquals(resultAlert.getVersion(), expectedAlert.getVersion());
        assertEquals(resultAlert.getRecipient(), expectedAlert.getRecipient());
        assertNotNull(resultAlert.getToAddresses());
        assertFalse(resultAlert.getToAddresses().size() == 0);
        assertEquals(resultAlert.getToAddresses(), expectedAlert.getToAddresses());
        assertEquals(resultAlert.getJobState(), expectedAlert.getJobState());
        assertEquals(resultAlert.getMessageText(), expectedAlert.getMessageText());
        assertEquals(resultAlert.getMessageTextWhenJobFails(), expectedAlert.getMessageTextWhenJobFails());
        assertEquals(resultAlert.getSubject(), expectedAlert.getSubject());
        assertEquals(resultAlert.isIncludingStackTrace(), expectedAlert.isIncludingStackTrace());
        assertEquals(resultAlert.isIncludingReportJobInfo(), expectedAlert.isIncludingReportJobInfo());

        Mockito.verify(repositoryService).getResource(any(ExecutionContext.class), anyString());
        Mockito.verify(inputControlsLogicService).getTypedParameters(anyString(), anyMap());
    }

    @Test
    public void testConvertServerObjectWithCalendarTriggerToClient() {
        ClientReportJob resultClientReportJob = converter.toClient(serverJobWithCalendarTrigger, null);
        assertEquals(resultClientReportJob, clientJobWithCalendarTrigger);
    }

    @Test
    public void testConvertServerObjectWithSimpleTriggerToClient() {
        ClientReportJob resultClientReportJob = converter.toClient(serverJobWithSimpleTrigger, null);
        assertEquals(resultClientReportJob, clientJobWithSimpleTrigger);
    }

    @Test
    public void testConvertServerObjectWithDefaultValuesToClient() {
        ClientReportJob resultClientReportJob = converter.toClient(new ReportJob(), null);
        assertNotNull(resultClientReportJob);
    }

    @Test
    public void testConvertClientObjectWithDefaultValuesToServer() {
        ReportJob resultReportJob = converter.toServer(ctx, new ClientReportJob(), null);
        assertNotNull(resultReportJob);
    }

    @Test
    public void testConvertClientObjectWithDefaultValuesToServerDashboard() {
        serverJobWithCalendarTrigger.getSource().setReferenceHeight(300);
        serverJobWithCalendarTrigger.getSource().setReferenceWidth(400);

        clientJobWithCalendarTrigger.getSource().setReferenceHeight(300);
        clientJobWithCalendarTrigger.getSource().setReferenceWidth(400);

        ReportJob resultReportJob = converter.toServer(ctx, new ClientReportJob(), null);
        assertNotNull(resultReportJob);
    }

    @Test
    public void toServer_withRelativeTimestampRange_returnServerJob() throws InputControlsValidationException, CascadeResourceNotFoundException {
        final String expression = "DAY-7";

        LinkedHashMap<String, Object> serverParams = new LinkedHashMap<String, Object>();
        serverParams.put("relativeTS", new RelativeTimestampRange(expression, losAngelesTimezone, 0));

        Mockito.doReturn(serverParams).when(inputControlsLogicService).getTypedParameters(anyString(), anyMap());

        Map<String, String[]> parameters = new HashMap<String, String[]>(){{
            put("relativeTS", new String[]{expression});
        }};

        ClientJobSource source = new ClientJobSource()
                .setParameters(parameters)
                .setReportUnitURI("/test");

        ClientReportJob job = new ClientReportJob()
                .setSource(source)
                .setOutputTimeZone(AMERICA_NEW_YORK);

        ReportJob resultReportJob = converter.toServer(ctx, job, null);

        RelativeTimestampRange actual = (RelativeTimestampRange) resultReportJob.getSource().getParameters().get("relativeTS");
        RelativeTimestampRange expected = new RelativeTimestampRange(expression, newYorkTimezone, 0);

        assertEquals(actual.getStart(), expected.getStart());
    }


    @Test
    public void toServer_withFixedTimestamp_returnServerJob() throws InputControlsValidationException, CascadeResourceNotFoundException {
        final String expression = "2018-08-09 10:10:10";

        FixedTimestamp expected = new FixedTimestamp(expression, losAngelesTimezone, FixedTimestamp.TIMESTAMP_PATTERN);

        LinkedHashMap<String, Object> serverParams = new LinkedHashMap<String, Object>();
        serverParams.put("fixedTS", expected);

        Mockito.doReturn(serverParams).when(inputControlsLogicService).getTypedParameters(anyString(), anyMap());

        Map<String, String[]> parameters = new HashMap<String, String[]>(){{
            put("fixedTS", new String[]{expression});
        }};

        ClientJobSource source = new ClientJobSource()
                .setParameters(parameters)
                .setReportUnitURI("/test");

        ClientReportJob job = new ClientReportJob()
                .setSource(source)
                .setOutputTimeZone(AMERICA_NEW_YORK);

        ReportJob resultReportJob = converter.toServer(ctx, job, null);

        FixedTimestamp actual = (FixedTimestamp) resultReportJob.getSource().getParameters().get("fixedTS");

        assertEquals(actual.getStart(), expected.getStart());
    }

    @Test
    public void toServer_withFixedDate_returnServerJob() throws InputControlsValidationException, CascadeResourceNotFoundException {
        final String expression = "2018-08-09";

        FixedDate expected = new FixedDate(expression, losAngelesTimezone, FixedTimestamp.DATE_PATTERN);
        LinkedHashMap<String, Object> serverParams = new LinkedHashMap<String, Object>();
        serverParams.put("fixedDate", expected);

        Mockito.doReturn(serverParams).when(inputControlsLogicService).getTypedParameters(anyString(), anyMap());

        Map<String, String[]> parameters = new HashMap<String, String[]>(){{
            put("fixedDate", new String[]{expression});
        }};

        ClientJobSource source = new ClientJobSource()
                .setParameters(parameters)
                .setReportUnitURI("/test");

        ClientReportJob job = new ClientReportJob()
                .setSource(source)
                .setOutputTimeZone(AMERICA_NEW_YORK);

        ReportJob resultReportJob = converter.toServer(ctx, job, null);

        FixedDate actual = (FixedDate) resultReportJob.getSource().getParameters().get("fixedDate");

        assertEquals(actual, expected);
    }

    @Test
    public void toServer_withFixedTimestampAndWithoutOutputTimezone_returnServerJob() throws InputControlsValidationException, CascadeResourceNotFoundException {
        final String expression = "DAY-7";

        LinkedHashMap<String, Object> serverParams = new LinkedHashMap<String, Object>();
        serverParams.put("relativeTS", new RelativeTimestampRange(expression, losAngelesTimezone, 0));

        Mockito.doReturn(serverParams).when(inputControlsLogicService).getTypedParameters(anyString(), anyMap());

        Map<String, String[]> parameters = new HashMap<String, String[]>(){{
            put("relativeTS", new String[]{expression});
        }};

        ClientJobSource source = new ClientJobSource()
                .setParameters(parameters)
                .setReportUnitURI("/test");

        ClientReportJob job = new ClientReportJob()
                .setSource(source);

        ReportJob resultReportJob = converter.toServer(ctx, job, null);

        RelativeTimestampRange actual = (RelativeTimestampRange) resultReportJob.getSource().getParameters().get("relativeTS");
        RelativeTimestampRange expected = new RelativeTimestampRange(expression, null, 0);

        assertEquals(actual.getStart(), expected.getStart());
    }

    @Test
    public void toServer_defaultLocale_null() {
        ClientReportJob job = new ClientReportJob();

        ReportJob resultReportJob = converter.toServer(ctx, job, null);

        assertEquals(resultReportJob.getOutputLocale(), LocaleContextHolder.getLocale().toString());
    }

    @Test
    public void toServer_dashboard() {
        ClientJobSource source = new ClientJobSource()
                .setReportUnitURI("/test")
                .setReferenceHeight(400)
                .setReferenceWidth(300);

        ClientReportJob job = new ClientReportJob()
                .setSource(source);

        ReportJob resultReportJob = converter.toServer(ctx, job, null);

        assertEquals(resultReportJob.getSource().getReferenceHeight(), source.getReferenceHeight());
        assertEquals(resultReportJob.getSource().getReferenceWidth(), source.getReferenceWidth());
    }

}