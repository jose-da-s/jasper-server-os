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

package com.jaspersoft.jasperserver.jaxrs.job;

import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.api.JSValidationException;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.common.util.rd.DateRangeFactory;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.FTPInfo;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJob;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobAlert;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobCalendarTrigger;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobMailNotification;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobRepositoryDestination;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobSimpleTrigger;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobSource;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobTrigger;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.jaxb.FtpTypeAdapter;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.jaxb.OutputFormatConversionHelper;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.jaxb.ReportJobOutputFormatsWrapper;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.jaxb.ReportJobSendTypeXmlAdapter;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.jaxb.ReportJobTriggerCalendarDaysXmlAdapter;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.jaxb.ReportJobTriggerIntervalUnitXmlAdapter;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.reportjobmodel.FTPInfoModel;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.reportjobmodel.ReportJobAlertModel;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.reportjobmodel.ReportJobCalendarTriggerModel;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.reportjobmodel.ReportJobMailNotificationModel;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.reportjobmodel.ReportJobRepositoryDestinationModel;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.reportjobmodel.ReportJobSimpleTriggerModel;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.reportjobmodel.ReportJobSourceModel;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControlsContainer;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConverter;
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
import com.jaspersoft.jasperserver.dto.job.model.ClientJobAlertModel;
import com.jaspersoft.jasperserver.dto.job.model.ClientJobCalendarTriggerModel;
import com.jaspersoft.jasperserver.dto.job.model.ClientJobFTPInfoModel;
import com.jaspersoft.jasperserver.dto.job.model.ClientJobMailNotificationModel;
import com.jaspersoft.jasperserver.dto.job.model.ClientJobRepositoryDestinationModel;
import com.jaspersoft.jasperserver.dto.job.model.ClientJobSimpleTriggerModel;
import com.jaspersoft.jasperserver.dto.job.model.ClientJobSourceModel;
import com.jaspersoft.jasperserver.inputcontrols.cascade.CascadeResourceNotFoundException;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlsLogicService;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlsValidationException;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.exception.ResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.resources.converters.ToServerConverter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.types.date.RelativeTimestampRange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;

/**
 * <p>
 * <p>
 *
 * @author tetiana.iefimenko
 * @version $Id$
 * @see
 */
@Component
public class ReportJobConverter implements ToClientConverter<ReportJob, ClientReportJob, Object>, ToServerConverter<ClientReportJob, ReportJob, Object> {

    protected static final Log log = LogFactory.getLog(ReportJobConverter.class);

    private final List<String> propertiesNames = new ArrayList<String>() {{
        add("FTP_TYPE_PROPERTY");
        add("PORT_PROPERTY");
        add("PROTOCOL_PROPERTY");
        add("IS_IMPLICIT_PROPERTY");
        add("PHSZ_PROPERTY");
        add("PROT_PROPERTY");
        add("SSH_KEY_PROPERTY");
        add("SSH_PASSPHRASE_PROPERTY");
    }};

    @jakarta.annotation.Resource
    private InputControlsLogicService inputControlsLogicService;

    private FtpTypeAdapter ftpTypeAdapter = new FtpTypeAdapter();

    @jakarta.annotation.Resource(name = "concreteRepository")
    private RepositoryService repositoryService;


    @Override
    public final ClientReportJob toClient(ReportJob serverObject, Object options) {
        ClientReportJob clientReportJob = new ClientReportJob();
        clientReportJob.setId(serverObject.getId());
        clientReportJob.setVersion(serverObject.getVersion());
        clientReportJob.setUsername(serverObject.getUsername());
        clientReportJob.setLabel(serverObject.getLabel());
        clientReportJob.setDescription(serverObject.getDescription());
        Timestamp creationDate = serverObject.getCreationDate();
        clientReportJob.setCreationDate((creationDate != null) ? new Timestamp(creationDate.getTime()) : null);
        clientReportJob.setBaseOutputFilename(serverObject.getBaseOutputFilename());
        clientReportJob.setOutputLocale(serverObject.getOutputLocale());

        // convert trigger field
        clientReportJob.setTrigger(toClientJobTrigger(serverObject.getTrigger()));

        // convert source field
        try {
            clientReportJob.setSource(toClientJobSource(serverObject.getSource()));
        } catch (ClassCastException e) {
            log.error("Error interpreting parameter " + e.getMessage() +
                    " of job " + serverObject.getLabel() +
                    " of " + serverObject.getSource().getReportUnitURI(), e);
        }

        clientReportJob.setOutputTimeZone(serverObject.getOutputTimeZone());

        // convert destination field
        clientReportJob.setRepositoryDestination(toClientJobRepositoryDestination(serverObject.getContentRepositoryDestination()));

        //convert mail notification
        clientReportJob.setMailNotification(toClientJobMailNotification(serverObject.getMailNotification()));

        // convert alert
        clientReportJob.setAlert(toClientJobAlert(serverObject.getAlert()));

        // convert output formats
        ReportJobOutputFormatsWrapper outputFormatsWrapper = toOutputFormatsWrapper(serverObject.getOutputFormatsSet());
		clientReportJob.setExportType(toClientJobExportType(outputFormatsWrapper));
		clientReportJob.setOutputFormats(toClientJobOutputFormats(outputFormatsWrapper));

        return clientReportJob;
    }

    protected ReportJobOutputFormatsWrapper toOutputFormatsWrapper(Set<Byte> outputFormatsSet) {
        ReportJobOutputFormatsWrapper outputFormatsWrapper = null;
        if (outputFormatsSet != null) {
            try {
                outputFormatsWrapper = OutputFormatConversionHelper.toOutputFormats(outputFormatsSet);
            } catch (Exception e) {
                log.debug(e, e);
            }
        }
        return outputFormatsWrapper;
    }

    protected Set<OutputFormat> toClientJobOutputFormats(ReportJobOutputFormatsWrapper outputFormatsWrapper) {
        Set<OutputFormat> outputFormats = new HashSet<OutputFormat>();
        if (outputFormatsWrapper != null) {
            Set<String> outputFormatsStrings = outputFormatsWrapper.getFormats();
            for (String outputFormatString : outputFormatsStrings) {
                outputFormats.add(OutputFormat.valueOf(outputFormatString));
            }
        }
        return outputFormats;
    }

    protected ExportType toClientJobExportType(ReportJobOutputFormatsWrapper outputFormatsWrapper) {
        ExportType exportType = null;
        if (outputFormatsWrapper != null) {
            String exportTypeString = outputFormatsWrapper.getExportType();
            if (exportTypeString != null) {
                exportType = ExportType.valueOf(exportTypeString);
            }
        }
        return exportType;
    }

    protected ClientJobAlert toClientJobAlert(ReportJobAlert serverAlert) {
        ClientJobAlert clientJobAlert = null;
        if (serverAlert != null) {
            clientJobAlert = new ClientJobAlert();
            clientJobAlert.
                    setId(serverAlert.getId()).
                    setVersion(serverAlert.getVersion()).
                    setMessageText(serverAlert.getMessageText()).
                    setMessageTextWhenJobFails(serverAlert.getMessageTextWhenJobFails()).
                    setSubject(serverAlert.getSubject()).
                    setIncludingStackTrace(serverAlert.isIncludingStackTrace()).
                    setIncludingReportJobInfo(serverAlert.isIncludingReportJobInfo());

            List<String> toAddresses = serverAlert.getToAddresses();
            ArrayList<String> clientAlertToAddresses = new ArrayList<String>();
            if (toAddresses != null) {
                for (String serverAlertToAddress : toAddresses) {
                    clientAlertToAddresses.add(serverAlertToAddress);
                }
            }
            clientJobAlert.setToAddresses(clientAlertToAddresses);
            ReportJobAlert.Recipient recipient = serverAlert.getRecipient();

            if (recipient != null) {
                clientJobAlert.
                        setRecipient(ClientJobAlertRecipient.valueOf(ReportJobAlert.Recipient.fromCode(recipient.getCode()).name()));
            }
            ReportJobAlert.JobState jobState = serverAlert.getJobState();
            if (jobState != null) {
                clientJobAlert.setJobState(ClientJobAlertState.valueOf(ReportJobAlert.JobState.fromCode(jobState.getCode()).name()));
            }
        }
        return clientJobAlert;
    }

    protected ClientJobMailNotification toClientJobMailNotification(ReportJobMailNotification serverMailNotification) {
        ClientJobMailNotification clientJobMailNotification = null;
        if (serverMailNotification != null) {
            clientJobMailNotification = new ClientJobMailNotification();
            clientJobMailNotification.
                    setId(serverMailNotification.getId()).
                    setVersion(serverMailNotification.getVersion()).
                    setIncludingStackTraceWhenJobFails(serverMailNotification.isIncludingStackTraceWhenJobFails()).
                    setMessageText(serverMailNotification.getMessageText()).
                    setSkipEmptyReports(serverMailNotification.isSkipEmptyReports()).
                    setSkipNotificationWhenJobFails(serverMailNotification.isSkipNotificationWhenJobFails()).
                    setSubject(serverMailNotification.getSubject()).
                    setMessageTextWhenJobFails(serverMailNotification.getMessageTextWhenJobFails());

            List serverBccAddresses = serverMailNotification.getBccAddresses();
            ArrayList<String> clientBccAddresses = new ArrayList<String>();
            if (serverBccAddresses != null) {
                for (Object bccAddress : serverBccAddresses) {
                    clientBccAddresses.add(bccAddress.toString());
                }
            }
            clientJobMailNotification.setBccAddresses(clientBccAddresses);

            List serverCcAddresses = serverMailNotification.getCcAddresses();
            ArrayList<String> clientCcAddresses = new ArrayList<String>();
            if (serverCcAddresses != null) {
                for (Object ccAddress : serverCcAddresses) {
                    clientCcAddresses.add(ccAddress.toString());
                }
            }
            clientJobMailNotification.setCcAddresses(clientCcAddresses);

            List serverToAddresses = serverMailNotification.getToAddresses();
            ArrayList<String> clientToAddresses = new ArrayList<String>();
            if (serverToAddresses != null) {
                for (Object toAddress : serverToAddresses) {
                    clientToAddresses.add(toAddress.toString());
                }
            }
            clientJobMailNotification.setToAddresses(clientToAddresses);

            Byte resultSendTypeCode = serverMailNotification.getResultSendTypeCode();
            if (resultSendTypeCode != null) {
                switch (resultSendTypeCode) {
                    case 1:
                        clientJobMailNotification.setResultSendType(ClientMailNotificationSendType.SEND);
                        break;
                    case 2:
                        clientJobMailNotification.setResultSendType(ClientMailNotificationSendType.SEND_ATTACHMENT);
                        break;
                    case 3:
                        clientJobMailNotification.setResultSendType(ClientMailNotificationSendType.SEND_ATTACHMENT_NOZIP);
                        break;
                    case 4:
                        clientJobMailNotification.setResultSendType(ClientMailNotificationSendType.SEND_EMBED);
                        break;
                    case 5:
                        clientJobMailNotification.setResultSendType(ClientMailNotificationSendType.SEND_ATTACHMENT_ZIP_ALL);
                        break;
                    case 6:
                        clientJobMailNotification.setResultSendType(ClientMailNotificationSendType.SEND_EMBED_ZIP_ALL_OTHERS);
                        break;
                }
            }
        }
        return clientJobMailNotification;
    }

    protected ClientJobRepositoryDestination toClientJobRepositoryDestination(ReportJobRepositoryDestination serverRepositoryDestination) {
        ClientJobRepositoryDestination clientJobRepositoryDestination = null;
        if (serverRepositoryDestination != null) {
            clientJobRepositoryDestination = new ClientJobRepositoryDestination();
            clientJobRepositoryDestination.
                    setFolderURI(serverRepositoryDestination.getFolderURI()).
                    setId(serverRepositoryDestination.getId()).
                    setOutputDescription(serverRepositoryDestination.getOutputDescription()).
                    setOverwriteFiles(serverRepositoryDestination.isOverwriteFiles()).
                    setSequentialFilenames(serverRepositoryDestination.isSequentialFilenames()).
                    setVersion(serverRepositoryDestination.getVersion()).
                    setTimestampPattern(serverRepositoryDestination.getTimestampPattern()).
                    setSaveToRepository(serverRepositoryDestination.isSaveToRepository()).
                    setDefaultReportOutputFolderURI(serverRepositoryDestination.getDefaultReportOutputFolderURI()).
                    setUsingDefaultReportOutputFolderURI(serverRepositoryDestination.isUsingDefaultReportOutputFolderURI()).
                    setOutputLocalFolder(serverRepositoryDestination.getOutputLocalFolder());

            ClientJobFtpInfo clientJobFtpInfo = new ClientJobFtpInfo();
            FTPInfo serverOutputFTPInfo = serverRepositoryDestination.getOutputFTPInfo();
            if (serverOutputFTPInfo != null) {

                clientJobFtpInfo.
                        setUserName(serverOutputFTPInfo.getUserName()).
                        setPassword(serverOutputFTPInfo.getPassword()).
                        setFolderPath(serverOutputFTPInfo.getFolderPath()).
                        setServerName(serverOutputFTPInfo.getServerName()).
                        setProtocol(serverOutputFTPInfo.getProtocol()).
                        setPort(serverOutputFTPInfo.getPort()).
                        setImplicit(serverOutputFTPInfo.isImplicit()).
                        setPbsz(serverOutputFTPInfo.getPbsz()).
                        setProt(serverOutputFTPInfo.getProt()).
                        setSshKey(serverOutputFTPInfo.getSshKey()).
                        setSshPassphrase(serverOutputFTPInfo.getSshPassphrase());
                try {
                    clientJobFtpInfo.setType(FtpConnection.FtpType.valueOf(ftpTypeAdapter.marshal(serverOutputFTPInfo.getType())));
                } catch (Exception e) {
                    log.error("Error interpreting parameter " + serverOutputFTPInfo.getType() + " of job FTP info ", e);
                }

                Map<String, String> propertiesMap = serverOutputFTPInfo.getPropertiesMap();
                if (propertiesMap != null) {
                    LinkedHashMap<String, String> clientProperties = new LinkedHashMap<String, String>();
                    for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
                        if (!propertiesNames.contains(entry.getKey())) {
                            clientProperties.put(entry.getKey(), entry.getValue());
                        }
                    }
                    clientJobFtpInfo.setPropertiesMap(clientProperties);
                }
            }
            clientJobRepositoryDestination.setOutputFTPInfo(clientJobFtpInfo);
        }
        return clientJobRepositoryDestination;
    }

    protected ClientJobSource toClientJobSource(ReportJobSource serverJobSource) {
        ClientJobSource clientJobSource = null;
        if (serverJobSource != null) {
            clientJobSource = new ClientJobSource();
            clientJobSource.setReportUnitURI(serverJobSource.getReportUnitURI());
            clientJobSource.setReferenceHeight(serverJobSource.getReferenceHeight());
            clientJobSource.setReferenceWidth(serverJobSource.getReferenceWidth());
            Map<String, Object> serverJobSourceParameters = serverJobSource.getParameters();

            if (serverJobSourceParameters != null && !serverJobSourceParameters.isEmpty()) {
                try {
                    final Map<String, String[]> formattedParameters = extractFormatParameters(serverJobSource);
                    clientJobSource.setParameters(formattedParameters);
                } catch (CascadeResourceNotFoundException e) {
                    throw new ResourceNotFoundException("URI:" + e.getResourceUri() + " Type:" + e.getResourceType());
                } catch (InputControlsValidationException e) {
                    throw new JSValidationException(e.getErrors());
                }
            }
        }
        return clientJobSource;
    }

    protected Map<String, String[]> extractFormatParameters(ReportJobSource jobSource) throws CascadeResourceNotFoundException, InputControlsValidationException {
        Map<String, String[]> formattedParams;
        if (repositoryService.getResource(ExecutionContextImpl.getRuntimeExecutionContext(), jobSource.getReportUnitURI())
                instanceof InputControlsContainer) {
            formattedParams = inputControlsLogicService
                    .formatTypedParameters(jobSource.getReportUnitURI(), jobSource.getParameters());
        } else {
            formattedParams = new HashMap<String, String[]>();

            for (String key : jobSource.getParameters().keySet()) {
                if (!JRParameter.REPORT_LOCALE.equals(key) ||
                        !JRParameter.REPORT_TIME_ZONE.equals(key) ||
                        !ReportJobSource.REFERENCE_HEIGHT_PARAMETER_NAME.equals(key) ||
                        !ReportJobSource.REFERENCE_WIDTH_PARAMETER_NAME.equals(key)) {
                    try {
                        Object paramValues = jobSource.getParameters().get(key);
                        if (paramValues instanceof Collection) {
                            List<String> castedParamVales = (List<String>) paramValues;
                            formattedParams.put(key, castedParamVales.toArray(new String[castedParamVales.size()]));
                        }
                    } catch (ClassCastException e) {
                        throw new ClassCastException(key);
                    }
                }
            }
        }

        return formattedParams;
    }


    protected ClientJobTrigger toClientJobTrigger(ReportJobTrigger serverJobTrigger) {
        ClientJobTrigger clientJobTrigger = null;
        if (serverJobTrigger != null) {
            if (serverJobTrigger instanceof ReportJobSimpleTrigger) {
                clientJobTrigger = new ClientJobSimpleTrigger();

                Byte recurrenceIntervalUnit = ((ReportJobSimpleTrigger) serverJobTrigger).getRecurrenceIntervalUnit();
                ClientIntervalUnitType clientIntervalUnitType = null;
                if (recurrenceIntervalUnit != null) {
                    switch (recurrenceIntervalUnit) {
                        case ReportJobSimpleTrigger.INTERVAL_MINUTE:
                            clientIntervalUnitType = ClientIntervalUnitType.MINUTE;
                            break;
                        case ReportJobSimpleTrigger.INTERVAL_HOUR:
                            clientIntervalUnitType = ClientIntervalUnitType.HOUR;
                            break;
                        case ReportJobSimpleTrigger.INTERVAL_DAY:
                            clientIntervalUnitType = ClientIntervalUnitType.DAY;
                            break;
                        case ReportJobSimpleTrigger.INTERVAL_WEEK:
                            clientIntervalUnitType = ClientIntervalUnitType.WEEK;
                            break;
                    }
                }

                ((ClientJobSimpleTrigger) clientJobTrigger).
                        setOccurrenceCount((((ReportJobSimpleTrigger) serverJobTrigger).getOccurrenceCount())).
                        setRecurrenceInterval(((ReportJobSimpleTrigger) serverJobTrigger).getRecurrenceInterval()).
                        setRecurrenceIntervalUnit(clientIntervalUnitType);
            }
            if (serverJobTrigger instanceof ReportJobCalendarTrigger) {
                clientJobTrigger = new ClientJobCalendarTrigger();
                Byte daysTypeCode = ((ReportJobCalendarTrigger) serverJobTrigger).getDaysTypeCode();
                ClientCalendarDaysType clientDaysType = null;
                if (daysTypeCode != null) {
                    switch (daysTypeCode) {
                        case 1:
                            clientDaysType = ClientCalendarDaysType.ALL;
                            break;
                        case 2:
                            clientDaysType = ClientCalendarDaysType.WEEK;
                            break;
                        case 3:
                            clientDaysType = ClientCalendarDaysType.MONTH;
                            break;
                    }
                }
                ((ClientJobCalendarTrigger) clientJobTrigger).
                        setMinutes(((ReportJobCalendarTrigger) serverJobTrigger).getMinutes()).
                        setHours(((ReportJobCalendarTrigger) serverJobTrigger).getHours()).
                        setDaysType(clientDaysType).
                        setWeekDays(((ReportJobCalendarTrigger) serverJobTrigger).getWeekDays()).
                        setMonthDays(((ReportJobCalendarTrigger) serverJobTrigger).getMonthDays()).
                        setMonths(((ReportJobCalendarTrigger) serverJobTrigger).getMonths());

            }

            if (clientJobTrigger != null) {
                clientJobTrigger.
                        setId(serverJobTrigger.getId()).
                        setVersion(serverJobTrigger.getVersion()).
                        setTimezone(serverJobTrigger.getTimezone()).
                        setCalendarName(serverJobTrigger.getCalendarName()).
                        setStartType(serverJobTrigger.getStartType()).
                        setStartDate((serverJobTrigger.getStartDate() != null) ?
                                new Date(serverJobTrigger.getStartDate().getTime()) : null).
                        setEndDate((serverJobTrigger.getEndDate() != null) ?
                                new Date(serverJobTrigger.getEndDate().getTime()) : null).
                        setMisfireInstruction(serverJobTrigger.getMisfireInstruction());
            }
        }
        return clientJobTrigger;
    }

    @Override
    public final String getClientResourceType() {
        return ClientReportJob.class.getName();
    }

    @Override
    public ReportJob toServer(ExecutionContext ctx, ClientReportJob clientObject, Object options) {
        return toServer(ctx, clientObject, new ReportJob(), null);
    }

    @Override
    public ReportJob toServer(ExecutionContext ctx, ClientReportJob clientObject, ReportJob resultToUpdate, Object options) {
        if ((clientObject.getId() != null)) {
            resultToUpdate.setId(clientObject.getId());
        }

        if ((clientObject.getVersion() != null)) {
            resultToUpdate.setVersion(clientObject.getVersion());
        }

        if (clientObject.getOutputLocale() == null){
            resultToUpdate.setOutputLocale(LocaleContextHolder.getLocale().toString());
        } else {
            resultToUpdate.setOutputLocale(clientObject.getOutputLocale());
        }

        resultToUpdate.setUsername(clientObject.getUsername());
        resultToUpdate.setLabel(clientObject.getLabel());
        resultToUpdate.setDescription(clientObject.getDescription());

        Timestamp creationDate = clientObject.getCreationDate();
        resultToUpdate.setCreationDate((creationDate != null) ? new Timestamp(creationDate.getTime()) : null);
        resultToUpdate.setBaseOutputFilename(clientObject.getBaseOutputFilename());

        resultToUpdate.setOutputTimeZone(clientObject.getOutputTimeZone());

        // convert trigger
        resultToUpdate.setTrigger(toServerJobTrigger(clientObject.getTrigger()));

        // convert source field
        resultToUpdate.setSource(toServerJobSource(clientObject.getSource(), clientObject.getOutputTimeZone()));

        // convert destination field
        resultToUpdate.setContentRepositoryDestination(toServerJobRepositoryDestination(clientObject.getRepositoryDestination()));

        //convert mail notification
        resultToUpdate.setMailNotification(toServerJobMailNotification(clientObject.getMailNotification()));

        // convert alert
        resultToUpdate.setAlert(toServerJobAlert(clientObject.getAlert()));

        // convert output formats

        resultToUpdate.setOutputFormatsSet(toServerOutputFormats(clientObject.getOutputFormats(), clientObject.getExportType()));

        return resultToUpdate;
    }

    protected ReportJobAlert toServerJobAlert(ClientJobAlert clientJobAlert) {
        ReportJobAlert serverAlert = null;
        if (clientJobAlert != null) {
            serverAlert = (clientJobAlert instanceof ClientJobAlertModel) ?
                    new ReportJobAlertModel() : new ReportJobAlert();
            if (clientJobAlert.getId() != null) {
                serverAlert.setId(clientJobAlert.getId());
            }
            if (clientJobAlert.getVersion() != null) {
                serverAlert.setVersion(clientJobAlert.getVersion());
            }
            if (clientJobAlert.getMessageText() != null) {
                serverAlert.setMessageText(clientJobAlert.getMessageText());
            }
            if (clientJobAlert.getMessageText() != null) {
                serverAlert.setMessageTextWhenJobFails(clientJobAlert.getMessageTextWhenJobFails());
            }
            if (clientJobAlert.getSubject() != null) {
                serverAlert.setSubject(clientJobAlert.getSubject());
            }
            if (clientJobAlert.isIncludingStackTrace() != null) {
                serverAlert.setIncludingStackTrace(clientJobAlert.isIncludingStackTrace());
            }
            if (clientJobAlert.isIncludingReportJobInfo() != null) {
                serverAlert.setIncludingReportJobInfo(clientJobAlert.isIncludingReportJobInfo());
            }

            List<String> toAddresses = clientJobAlert.getToAddresses();
            if (toAddresses != null) {
                ArrayList<String> serverAlertToAddresses = new ArrayList<String>();
                for (String clientAlertToAddress : toAddresses) {
                    serverAlertToAddresses.add(clientAlertToAddress);
                }
                serverAlert.setToAddresses(serverAlertToAddresses);
            }
            if (clientJobAlert.getRecipient() != null) {
                serverAlert.setRecipient(ReportJobAlert.Recipient.valueOf(clientJobAlert.getRecipient().name()));
            }
            if (clientJobAlert.getJobState() != null) {
                serverAlert.setJobState(ReportJobAlert.JobState.valueOf(clientJobAlert.getJobState().name()));
            }
        }
        return serverAlert;
    }

    protected ReportJobMailNotification toServerJobMailNotification(ClientJobMailNotification clientJobMailNotification) {
        ReportJobMailNotification serverMailNotification = null;
        if (clientJobMailNotification != null) {
            serverMailNotification = (clientJobMailNotification instanceof ClientJobMailNotificationModel) ?
                    new ReportJobMailNotificationModel() : new ReportJobMailNotification();
            if (clientJobMailNotification.getId() != null) {
                serverMailNotification.setId(clientJobMailNotification.getId());
            }
            if (clientJobMailNotification.getVersion() != null) {
                serverMailNotification.setVersion(clientJobMailNotification.getVersion());
            }
            if (clientJobMailNotification.isIncludingStackTraceWhenJobFails() != null) {
                serverMailNotification.setIncludingStackTraceWhenJobFails(clientJobMailNotification.isIncludingStackTraceWhenJobFails());
            }
            if (clientJobMailNotification.getMessageText() != null) {
                serverMailNotification.setMessageText(clientJobMailNotification.getMessageText());
            }
            if (clientJobMailNotification.isSkipEmptyReports() != null) {
                serverMailNotification.setSkipEmptyReports(clientJobMailNotification.isSkipEmptyReports());
            }
            if (clientJobMailNotification.isSkipNotificationWhenJobFails() != null) {
                serverMailNotification.setSkipNotificationWhenJobFails(clientJobMailNotification.isSkipNotificationWhenJobFails());
            }
            if (clientJobMailNotification.getSubject() != null) {
                serverMailNotification.setSubject(clientJobMailNotification.getSubject());
            }
            if (clientJobMailNotification.getMessageTextWhenJobFails() != null) {
                serverMailNotification.setMessageTextWhenJobFails(clientJobMailNotification.getMessageTextWhenJobFails());
            }

            List clientBccAddresses = clientJobMailNotification.getBccAddresses();
            if (clientBccAddresses != null) {
                ArrayList<String> serverBccAddresses = new ArrayList<String>();
                for (Object bccAddress : clientBccAddresses) {
                    serverBccAddresses.add(bccAddress.toString());
                }
                serverMailNotification.setBccAddresses(serverBccAddresses);
            }
            List clientCcAddresses = clientJobMailNotification.getCcAddresses();
            if (clientCcAddresses != null) {
                ArrayList<String> serverCcAddresses = new ArrayList<String>();
                for (Object ccAddress : clientCcAddresses) {
                    serverCcAddresses.add(ccAddress.toString());
                }
                serverMailNotification.setCcAddresses(serverCcAddresses);
            }
            List clientToAddresses = clientJobMailNotification.getToAddresses();
            if (clientToAddresses != null) {
                ArrayList<String> serverToAddresses = new ArrayList<String>();
                for (Object toAddress : clientToAddresses) {
                    serverToAddresses.add(toAddress.toString());
                }
                serverMailNotification.setToAddresses(serverToAddresses);
            }

            ClientMailNotificationSendType resultSendType = clientJobMailNotification.getResultSendType();
            if (resultSendType != null) {
                serverMailNotification.
                        setResultSendTypeCode(ReportJobSendTypeXmlAdapter.SendType.valueOf(resultSendType.name()).getProperty());
            }
        }
        return serverMailNotification;
    }

    protected ReportJobRepositoryDestination toServerJobRepositoryDestination(ClientJobRepositoryDestination clientJobRepositoryDestination) {
        ReportJobRepositoryDestination serverRepositoryDestination = null;
        if (clientJobRepositoryDestination != null) {
            serverRepositoryDestination = (clientJobRepositoryDestination instanceof ClientJobRepositoryDestinationModel)
                    ? new ReportJobRepositoryDestinationModel() : new ReportJobRepositoryDestination();
            if (clientJobRepositoryDestination.getFolderURI() != null) {
                serverRepositoryDestination.
                        setFolderURI(clientJobRepositoryDestination.getFolderURI());
            }
            if (clientJobRepositoryDestination.getId() != null) {
                serverRepositoryDestination.
                        setId(clientJobRepositoryDestination.getId());
            }
            if (clientJobRepositoryDestination.getOutputDescription() != null) {
                serverRepositoryDestination.
                        setOutputDescription(clientJobRepositoryDestination.getOutputDescription());
            }
            if (clientJobRepositoryDestination.isOverwriteFiles() != null) {
                serverRepositoryDestination.
                        setOverwriteFiles(clientJobRepositoryDestination.isOverwriteFiles());
            }
            if (clientJobRepositoryDestination.isSequentialFilenames() != null) {
                serverRepositoryDestination.
                        setSequentialFilenames(clientJobRepositoryDestination.isSequentialFilenames());
            }
            if (clientJobRepositoryDestination.getVersion() != null) {
                serverRepositoryDestination.
                        setVersion(clientJobRepositoryDestination.getVersion());
            }
            if (clientJobRepositoryDestination.getTimestampPattern() != null) {
                serverRepositoryDestination.
                        setTimestampPattern(clientJobRepositoryDestination.getTimestampPattern());
            }
            if (clientJobRepositoryDestination.isSaveToRepository() != null) {
                serverRepositoryDestination.
                        setSaveToRepository(clientJobRepositoryDestination.isSaveToRepository());
            }
            if (clientJobRepositoryDestination.getDefaultReportOutputFolderURI() != null) {
                serverRepositoryDestination.
                        setDefaultReportOutputFolderURI(clientJobRepositoryDestination.getDefaultReportOutputFolderURI());
            }
            if (clientJobRepositoryDestination.isUsingDefaultReportOutputFolderURI() != null) {
                serverRepositoryDestination.
                        setUsingDefaultReportOutputFolderURI(clientJobRepositoryDestination.isUsingDefaultReportOutputFolderURI());
            }
            if (clientJobRepositoryDestination.getOutputLocalFolder() != null) {
                serverRepositoryDestination.
                        setOutputLocalFolder(clientJobRepositoryDestination.getOutputLocalFolder());
            }

            ClientJobFtpInfo clientJobFtpInfo = clientJobRepositoryDestination.getOutputFTPInfo();
            FTPInfo serverOutputFTPInfo = null;
            if (clientJobFtpInfo != null) {
                serverOutputFTPInfo = (clientJobFtpInfo instanceof ClientJobFTPInfoModel) ?
                        new FTPInfoModel() : new FTPInfo();
                if (clientJobFtpInfo.getUserName() != null) {
                    serverOutputFTPInfo.setUserName(clientJobFtpInfo.getUserName());
                }
                if (clientJobFtpInfo.getPassword() != null) {
                    serverOutputFTPInfo.setPassword(clientJobFtpInfo.getPassword());
                }
                if (clientJobFtpInfo.getFolderPath() != null) {
                    serverOutputFTPInfo.setFolderPath(clientJobFtpInfo.getFolderPath());
                }
                if (clientJobFtpInfo.getServerName() != null) {
                    serverOutputFTPInfo.setServerName(clientJobFtpInfo.getServerName());
                }
                if (clientJobFtpInfo.getProtocol() != null) {
                    serverOutputFTPInfo.setProtocol(clientJobFtpInfo.getProtocol());
                }
                if (clientJobFtpInfo.getPort() != null) {
                    serverOutputFTPInfo.setPort(clientJobFtpInfo.getPort());
                }
                if (clientJobFtpInfo.getImplicit() != null) {
                    serverOutputFTPInfo.setImplicit(clientJobFtpInfo.getImplicit());
                }
                if (clientJobFtpInfo.getPbsz() != null) {
                    serverOutputFTPInfo.setPbsz(clientJobFtpInfo.getPbsz());
                }
                if (clientJobFtpInfo.getProt() != null) {
                    serverOutputFTPInfo.setProt(clientJobFtpInfo.getProt());
                }
                if (clientJobFtpInfo.getSshKey() != null) {
                    serverOutputFTPInfo.setSshKey(clientJobFtpInfo.getSshKey());
                }
                if (clientJobFtpInfo.getSshPassphrase() != null) {
                    serverOutputFTPInfo.setSshPassphrase(clientJobFtpInfo.getSshPassphrase());
                }

                if (clientJobFtpInfo.getType() != null) {
                    try {
                        serverOutputFTPInfo.setType(ftpTypeAdapter.unmarshal(clientJobFtpInfo.getType().toString()));
                    } catch (Exception e) {
                        log.error("Error interpreting parameter " + clientJobFtpInfo.getType() + " of job FTP info ", e);
                    }
                }
                 Map<String, String> clientPropertiesMap = clientJobFtpInfo.getPropertiesMap();
                if (clientPropertiesMap != null) {
                    for (Iterator<Map.Entry<String, String>> iterator = clientPropertiesMap.entrySet().iterator(); iterator.hasNext(); ) {
                        Map.Entry<String, String> entry = iterator.next();
                        //exclude params that set as fields
                        if (!propertiesNames.contains(entry.getKey())) {
                            serverOutputFTPInfo.getPropertiesMap().put(entry.getKey(), entry.getValue());
                        }
                    }
                }
            }
            serverRepositoryDestination.setOutputFTPInfo(serverOutputFTPInfo);
        }
        return serverRepositoryDestination;
    }

    protected ReportJobSource toServerJobSource(ClientJobSource clientJobSource, String timeZone) {
        ReportJobSource serverJobSource = null;
        if (clientJobSource != null) {
            serverJobSource = (clientJobSource instanceof ClientJobSourceModel) ?
                    new ReportJobSourceModel() : new ReportJobSource();

            if (clientJobSource.getReportUnitURI() != null) {
                serverJobSource.setReportUnitURI(clientJobSource.getReportUnitURI());
            }

            Map<String, String[]> clientParameters = (clientJobSource.getParameters() != null) ? clientJobSource.getParameters() : new HashMap<String, String[]>();

            serverJobSource.setReferenceHeight(clientJobSource.getReferenceHeight());
            serverJobSource.setReferenceWidth(clientJobSource.getReferenceWidth());

            // invocation of repository service involves security check
            final Resource resource = repositoryService.getResource(ExecutionContextImpl.getRuntimeExecutionContext(),
                    clientJobSource.getReportUnitURI());
            if (resource == null) {
                //if resource doesn't exist return empty object, error is caught in implementation of ReportJobValidator
                serverJobSource.setParameters(new HashMap<String, Object>());
                return serverJobSource;
            }

                if (resource instanceof InputControlsContainer) {
                    try {
                        Map<String, Object> typedParameters = inputControlsLogicService.getTypedParameters(clientJobSource.getReportUnitURI(), clientParameters);
                        serverJobSource.setParameters(typedParameters);

                        // RelativeTimestampRang have to be converted to server representation with report output timezone
                        // because the value of the RelativeTimestampRang depends in what timezone report will run
                        TimeZone tz = (timeZone != null) ? TimeZone.getTimeZone(timeZone) : null;
                        fixTimeZoneForRelativeTimestampRangeParams(typedParameters, tz);
                    } catch (ClassCastException e) {
                        log.error(e);
                        throw new IllegalParameterValueException("job.source.parameters", "Map with content of wrong type");
                    } catch (InputControlsValidationException e) {
                        throw new JSValidationException(e.getErrors());
                    } catch (CascadeResourceNotFoundException e) {
                        throw new ResourceNotFoundException("URI:" + e.getResourceUri() + " Type:" + e.getResourceType());
                    }
                } else {
                    // Dashboards aren't InputControlsContainers and receive all parameter values as List<String>
                    Map<String, Object> serverParameters = new HashMap<String, Object>();
                    for (String key : clientParameters.keySet()){
                        List<String> list = new ArrayList<String>();
                        for (String val : clientParameters.get(key)){
                            list.add(val);
                        }
                        serverParameters.put(key, list);
                    }
                    serverJobSource.setParameters(serverParameters);
                }

            // set timeZone
        }
        if (timeZone != null) {
            if (serverJobSource == null) serverJobSource = new ReportJobSource();
            if (serverJobSource.getParameters() == null) serverJobSource.setParameters(new HashMap<String, Object>());
            serverJobSource.getParameters().put(JRParameter.REPORT_TIME_ZONE, TimeZone.getTimeZone(timeZone));
        }
        return serverJobSource;
    }

    private void fixTimeZoneForRelativeTimestampRangeParams(Map<String, Object> typedParameters, TimeZone timeZone) {
        if (typedParameters == null) return;

        for (Map.Entry<String, Object> entry : typedParameters.entrySet()) {
            if (entry.getValue() instanceof RelativeTimestampRange) {
                RelativeTimestampRange range = (RelativeTimestampRange) entry.getValue();

                range = (RelativeTimestampRange) DateRangeFactory.getInstance(range.getExpression(), timeZone, Timestamp.class);
                entry.setValue(range);
            }
        }
    }

    protected ReportJobTrigger toServerJobTrigger(ClientJobTrigger clientJobTrigger) {
        ReportJobTrigger serverJobTrigger = null;
        if (clientJobTrigger != null) {
            if (clientJobTrigger instanceof ClientJobSimpleTrigger) {
                serverJobTrigger = (clientJobTrigger instanceof ClientJobSimpleTriggerModel) ?
                        new ReportJobSimpleTriggerModel() : new ReportJobSimpleTrigger();

                ClientIntervalUnitType recurrenceIntervalUnit = ((ClientJobSimpleTrigger) clientJobTrigger).getRecurrenceIntervalUnit();
                if (recurrenceIntervalUnit != null) {
                    ((ReportJobSimpleTrigger) serverJobTrigger).
                            setRecurrenceIntervalUnit(ReportJobTriggerIntervalUnitXmlAdapter.IntervalUnit.valueOf(recurrenceIntervalUnit.name()).getProperty());
                }

                Integer occurrenceCount = ((ClientJobSimpleTrigger) clientJobTrigger).getOccurrenceCount();
                if (occurrenceCount != null) {
                    ((ReportJobSimpleTrigger) serverJobTrigger).
                            setOccurrenceCount(occurrenceCount);
                }
                Integer recurrenceInterval = ((ClientJobSimpleTrigger) clientJobTrigger).getRecurrenceInterval();
                if (recurrenceInterval != null) {
                    ((ReportJobSimpleTrigger) serverJobTrigger).
                            setRecurrenceInterval(recurrenceInterval);
                }
            }
            if (clientJobTrigger instanceof ClientJobCalendarTrigger) {
                serverJobTrigger = (clientJobTrigger instanceof ClientJobCalendarTriggerModel) ?
                        new ReportJobCalendarTriggerModel() : new ReportJobCalendarTrigger();
                ClientCalendarDaysType daysType = ((ClientJobCalendarTrigger) clientJobTrigger).getDaysType();
                if (daysType != null) {
                    ((ReportJobCalendarTrigger) serverJobTrigger).setDaysTypeCode(ReportJobTriggerCalendarDaysXmlAdapter.DayTypes.valueOf(daysType.name()).getProperty());
                }
                String minutes = ((ClientJobCalendarTrigger) clientJobTrigger).getMinutes();
                if (minutes != null) {
                    ((ReportJobCalendarTrigger) serverJobTrigger).
                            setMinutes(minutes);
                }
                String hours = ((ClientJobCalendarTrigger) clientJobTrigger).getHours();
                if (hours != null) {
                    ((ReportJobCalendarTrigger) serverJobTrigger).
                            setHours(hours);
                }
                SortedSet<Byte> weekDays = ((ClientJobCalendarTrigger) clientJobTrigger).getWeekDays();
                if (weekDays != null) {
                    ((ReportJobCalendarTrigger) serverJobTrigger).
                            setWeekDays(weekDays);
                }
                String monthDays = ((ClientJobCalendarTrigger) clientJobTrigger).getMonthDays();
                if (monthDays != null) {
                    ((ReportJobCalendarTrigger) serverJobTrigger).
                            setMonthDays(monthDays);
                }
                SortedSet<Byte> months = ((ClientJobCalendarTrigger) clientJobTrigger).getMonths();
                if (months != null) {
                    ((ReportJobCalendarTrigger) serverJobTrigger).
                            setMonths(months);
                }

            }
            if (serverJobTrigger != null) {

                if (clientJobTrigger.getId() != null) serverJobTrigger.setId(clientJobTrigger.getId());
                if (clientJobTrigger.getVersion() != null) {
                    serverJobTrigger.
                            setVersion(clientJobTrigger.getVersion());
                }
                if (clientJobTrigger.getTimezone() != null) {
                    serverJobTrigger.
                            setTimezone(clientJobTrigger.getTimezone());
                }
                if (clientJobTrigger.getCalendarName() != null) {
                    serverJobTrigger.
                            setCalendarName(clientJobTrigger.getCalendarName());
                }
                if (clientJobTrigger.getStartType() != 0) {
                    serverJobTrigger.
                            setStartType((byte) clientJobTrigger.getStartType());
                }
                Date startDate = clientJobTrigger.getStartDate();
                if (startDate != null) {
                    serverJobTrigger.
                            setStartDate(new Date(startDate.getTime()));
                }
                Date endDate = clientJobTrigger.getEndDate();
                if (endDate != null) {
                    serverJobTrigger.
                            setEndDate(new Date(endDate.getTime()));
                }
                if (clientJobTrigger.getMisfireInstruction() != null) {
                    serverJobTrigger.
                            setMisfireInstruction(clientJobTrigger.getMisfireInstruction());
                }
            }
        }
        return serverJobTrigger;
    }

    protected Set<Byte> toServerOutputFormats(Set<OutputFormat> outputFormats, ExportType exportType) {
        Set<Byte> outputFormatsSet = new HashSet<Byte>();
        if (outputFormats != null) {
            Set<String> outputFormatsString = new LinkedHashSet<String>();
            for (OutputFormat outputFormat : outputFormats) {
                outputFormatsString.add(outputFormat.name());
            }
            try {
                outputFormatsSet = OutputFormatConversionHelper.toBytes(outputFormatsString, 
                        exportType == null ? null : exportType.name());
            } catch (ErrorDescriptorException e) {
            	throw e;
            } catch (Exception e) {
                return outputFormatsSet;
            }
        }
        return outputFormatsSet;
    }

    @Override
    public String getServerResourceType() {
        return ReportJob.class.getName();
    }
}

