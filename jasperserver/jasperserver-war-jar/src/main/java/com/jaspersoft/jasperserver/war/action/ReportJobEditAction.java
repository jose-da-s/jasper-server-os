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
package com.jaspersoft.jasperserver.war.action;

import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.jaspersoft.jasperserver.api.JSValidationException;
import com.jaspersoft.jasperserver.api.common.domain.ValidationError;
import com.jaspersoft.jasperserver.api.common.util.StaticExecutionContextProvider;
import com.jaspersoft.jasperserver.api.common.util.TimeZoneContextHolder;
import com.jaspersoft.jasperserver.api.common.util.TimeZonesList;
import com.jaspersoft.jasperserver.api.engine.common.service.SecurityContextProvider;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.CalendarFormatProvider;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.*;
import com.jaspersoft.jasperserver.api.engine.scheduling.service.ReportJobNotFoundException;
import com.jaspersoft.jasperserver.api.engine.scheduling.service.ReportSchedulingService;
import com.jaspersoft.jasperserver.api.logging.audit.context.AuditContext;
import com.jaspersoft.jasperserver.api.logging.audit.domain.AuditEvent;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.RepositoryConfiguration;
import com.jaspersoft.jasperserver.war.common.LocalesList;
import com.jaspersoft.jasperserver.war.common.UserLocale;
import com.jaspersoft.jasperserver.war.dto.ByteEnum;
import com.jaspersoft.jasperserver.war.util.ValidationErrorsUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.DataBinder;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.beans.PropertyEditorSupport;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Pattern;

import static com.jaspersoft.jasperserver.api.logging.audit.domain.AuditEventType.SCHEDULE_REPORT;
import static com.jaspersoft.jasperserver.api.logging.audit.domain.AuditEventType.UPDATE_REPORT_SCHEDULING;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class ReportJobEditAction extends FormAction {
	
	protected static final Log log = LogFactory.getLog(ReportJobEditAction.class);
	
	public static final byte RECURRENCE_TYPE_NONE = 1;
	public static final byte RECURRENCE_TYPE_SIMPLE = 2;
	public static final byte RECURRENCE_TYPE_CALENDAR = 3;
	
	public static final String ATTR_NAME_TRIGGER_RECURRENCE_TYPE = "triggerRecurrenceType";
	public static final String ATTR_NAME_TRIGGER_ALL_WEEK_DAYS = "allWeekDays";	
	public static final String ATTR_NAME_TRIGGER_ALL_MONTHS = "allMonths";

	public static final String ATTR_NAME_ORGANIZATION_ID = "organizationId";
	public static final String ATTR_NAME_PUBLIC_FOLDER_URI = "publicFolderUri";

	public static final String EVENT_DETAILS_ERROR = "detailsError";
	public static final String EVENT_TRIGGER_ERROR = "triggerError";
	public static final String EVENT_OUTPUT_ERROR = "outputError";
	
	public static final String[] VALIDATION_FIELDS_DETAILS = {"label", "description"};
	public static final String[] VALIDATION_FIELDS_TRIGGER = {"trigger"};
	public static final String[] VALIDATION_FIELDS_OUTPUT = {"baseOutputFilename", 
		"outputFormats", "contentRepositoryDestination", "mailNotification"};
	
	protected static final Map VALIDATION_FIELDS_MAPPINGS;
	static
	{
		VALIDATION_FIELDS_MAPPINGS = new LinkedHashMap();
		VALIDATION_FIELDS_MAPPINGS.put(EVENT_DETAILS_ERROR, VALIDATION_FIELDS_DETAILS);
		VALIDATION_FIELDS_MAPPINGS.put(EVENT_TRIGGER_ERROR, VALIDATION_FIELDS_TRIGGER);
		VALIDATION_FIELDS_MAPPINGS.put(EVENT_OUTPUT_ERROR, VALIDATION_FIELDS_OUTPUT);
	}
	
	private MessageSource messageSource;
	private ReportSchedulingService schedulingService;
    private SecurityContextProvider securityContextProvider;
	private String isNewModeAttrName;
	private String reportUnitURIAttrName;
	private String editJobIdParamName;
	private String contentFoldersAttrName;
	private String outputFormatsAttrName;
	private String intervalUnitsAttrName;
	
	private LocalesList localesList;
	private String localesAttrName;

	private TimeZonesList timeZonesList;
	private String timeZonesAttrName;
	
	private RepositoryConfiguration configuration;

    private AuditContext auditContext;

	private CalendarFormatProvider formatProvider;


	private ValidationErrorsUtils validationUtils = ValidationErrorsUtils.instance();

	private Pattern addressSeparatorPattern = Pattern.compile("[,;]");
	private String addressDefaultSeparator = ", ";
	private MailAddressesEditor mailAddressesEditor;
	private CustomNumberEditor customNumberEditor;
	private CustomCollectionEditor byteSetEditor;
	private CustomCollectionEditor byteSortedSetEditor;

	//set by config files
	private List recurrenceIntervalUnits;
	private List allOutputFormats;
	
	private final List weekDays;
	private final List months;

	public ReportJobEditAction() {
//		allOutputFormats = getOutputFormats();
		weekDays = getWeekDays();
		months = getMonths();
	}
	
	protected List getOutputFormats() {
//		List allOutputFormatsList = new ArrayList();
//		allOutputFormatsList.add(new ByteEnum(ReportJob.OUTPUT_FORMAT_PDF, "report.output.pdf.label"));
//		allOutputFormatsList.add(new ByteEnum(ReportJob.OUTPUT_FORMAT_HTML, "report.output.html.label"));
//		allOutputFormatsList.add(new ByteEnum(ReportJob.OUTPUT_FORMAT_XLS, "report.output.xls.label"));
//		allOutputFormatsList.add(new ByteEnum(ReportJob.OUTPUT_FORMAT_RTF, "report.output.rtf.label"));
//		allOutputFormatsList.add(new ByteEnum(ReportJob.OUTPUT_FORMAT_CSV, "report.output.csv.label"));
//		allOutputFormatsList.add(new ByteEnum(ReportJob.OUTPUT_FORMAT_ODT, "report.output.odt.label"));
//		return allOutputFormatsList;
		return allOutputFormats;
	}
	
	protected List getWeekDays() {
		List weekDaysList = new ArrayList();
		weekDaysList.add(new ByteEnum((byte) 2, "week.days.label.mon"));
		weekDaysList.add(new ByteEnum((byte) 3, "week.days.label.tue"));
		weekDaysList.add(new ByteEnum((byte) 4, "week.days.label.wed"));
		weekDaysList.add(new ByteEnum((byte) 5, "week.days.label.thu"));
		weekDaysList.add(new ByteEnum((byte) 6, "week.days.label.fri"));
		weekDaysList.add(new ByteEnum((byte) 7, "week.days.label.sat"));
		weekDaysList.add(new ByteEnum((byte) 1, "week.days.label.sun"));
		return weekDaysList;
	}
	
	protected List getMonths() {
		List monthsList = new ArrayList();
		monthsList.add(new ByteEnum((byte) 1, "monts.label.jan"));
		monthsList.add(new ByteEnum((byte) 2, "monts.label.feb"));
		monthsList.add(new ByteEnum((byte) 3, "monts.label.mar"));
		monthsList.add(new ByteEnum((byte) 4, "monts.label.apr"));
		monthsList.add(new ByteEnum((byte) 5, "monts.label.may"));
		monthsList.add(new ByteEnum((byte) 6, "monts.label.jun"));
		monthsList.add(new ByteEnum((byte) 7, "monts.label.jul"));
		monthsList.add(new ByteEnum((byte) 8, "monts.label.aug"));
		monthsList.add(new ByteEnum((byte) 9, "monts.label.sep"));
		monthsList.add(new ByteEnum((byte) 10, "monts.label.oct"));
		monthsList.add(new ByteEnum((byte) 11, "monts.label.nov"));
		monthsList.add(new ByteEnum((byte) 12, "monts.label.dec"));
		return monthsList;
	}
	
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		
		mailAddressesEditor = new MailAddressesEditor();
		customNumberEditor = new CustomNumberEditor(Integer.class, true);
		byteSetEditor = new ByteCollectionEditor(Set.class);
		byteSortedSetEditor = new ByteCollectionEditor(SortedSet.class);
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public ReportSchedulingService getSchedulingService() {
		return schedulingService;
	}

	public void setSchedulingService(ReportSchedulingService schedulingService) {
		this.schedulingService = schedulingService;
	}

    public void setSecurityContextProvider(SecurityContextProvider securityContextProvider) {
        this.securityContextProvider = securityContextProvider;
    }

    public String getEditJobIdParamName() {
		return editJobIdParamName;
	}

	public void setEditJobIdParamName(String editJobIdParamName) {
		this.editJobIdParamName = editJobIdParamName;
	}

	public String getIsNewModeAttrName() {
		return isNewModeAttrName;
	}

	public void setIsNewModeAttrName(String isNewModeAttrName) {
		this.isNewModeAttrName = isNewModeAttrName;
	}

	public String getReportUnitURIAttrName() {
		return reportUnitURIAttrName;
	}

	public void setReportUnitURIAttrName(String reportUnitURIAttrName) {
		this.reportUnitURIAttrName = reportUnitURIAttrName;
	}

	public String getOutputFormatsAttrName() {
		return outputFormatsAttrName;
	}

	public void setOutputFormatsAttrName(String outputFormatsAttrName) {
		this.outputFormatsAttrName = outputFormatsAttrName;
	}

	public String getIntervalUnitsAttrName() {
		return intervalUnitsAttrName;
	}

	public void setIntervalUnitsAttrName(String intervalUnitsAttrName) {
		this.intervalUnitsAttrName = intervalUnitsAttrName;
	}

	public String getLocalesAttrName() {
		return localesAttrName;
	}

	public void setLocalesAttrName(String localesAttrName) {
		this.localesAttrName = localesAttrName;
	}

	public LocalesList getLocalesList() {
		return localesList;
	}

	public void setLocalesList(LocalesList localesList) {
		this.localesList = localesList;
	}

	public String getTimeZonesAttrName() {
		return timeZonesAttrName;
	}

	public void setTimeZonesAttrName(String timeZonesAttrName) {
		this.timeZonesAttrName = timeZonesAttrName;
	}

	public TimeZonesList getTimeZonesList() {
		return timeZonesList;
	}

	public void setTimeZonesList(TimeZonesList timeZonesList) {
		this.timeZonesList = timeZonesList;
	}

	public ValidationErrorsUtils getValidationUtils() {
		return validationUtils;
	}

	public void setValidationUtils(ValidationErrorsUtils validationUtils) {
		this.validationUtils = validationUtils;
	}

    public AuditContext getAuditContext() {
        return auditContext;
    }

    public void setAuditContext(AuditContext auditContext) {
        this.auditContext = auditContext;
    }

	public CalendarFormatProvider getFormatProvider() {
		return formatProvider;
	}

	public void setFormatProvider(CalendarFormatProvider formatProvider) {
		this.formatProvider = formatProvider;
	}

	protected void initBinder(RequestContext context, DataBinder binder) {
		super.initBinder(context, binder);
		
		binder.registerCustomEditor(List.class, "mailNotification.toAddresses", mailAddressesEditor);
		binder.registerCustomEditor(List.class, "mailNotification.ccAddresses", mailAddressesEditor);
		binder.registerCustomEditor(List.class, "mailNotification.bccAddresses", mailAddressesEditor);
		
		// create a fresh date editor so that is uses the current locale
		CustomDateEditor customDateEditor = new CustomDateEditor(formatProvider.getDatetimeFormat(), true);
		binder.registerCustomEditor(Date.class, customDateEditor);
		
		binder.registerCustomEditor(Integer.class, customNumberEditor);
		binder.registerCustomEditor(Set.class, "outputFormats", byteSetEditor);
		binder.registerCustomEditor(SortedSet.class, "trigger.weekDays", byteSortedSetEditor);
		binder.registerCustomEditor(SortedSet.class, "trigger.months", byteSortedSetEditor);
		binder.registerCustomEditor(String.class, "contentRepositoryDestination.timestampPattern", new StringTrimmerEditor(true));
		binder.setAllowedFields(
				"label",
				"description",
				"baseOutputFilename",
				"outputFormats",
				"outputLocale",
				"trigger.startType",
				"trigger.startDate",
				"trigger.endDate",
				"trigger.timezone",
				"trigger.calendarName",
				"trigger.occurrenceCount",
				"trigger.recurrenceInterval",
				"trigger.recurrenceIntervalUnit",
				"trigger.minutes",
				"trigger.hours",
				"trigger.daysTypeCode",
				"trigger.monthDays",
				"trigger.months",
				"trigger.weekDays",
				"contentRepositoryDestination.folderURI",
				"contentRepositoryDestination.sequentialFilenames",
				"contentRepositoryDestination.overwriteFiles",
				"contentRepositoryDestination.outputDescription",
				"contentRepositoryDestination.timestampPattern",
				"contentRepositoryDestination.saveToRepository",
				"contentRepositoryDestination.defaultReportOutputFolderURI",
				"contentRepositoryDestination.usingDefaultReportOutputFolderURI",
				"contentRepositoryDestination.outputLocalFolder",
				"contentRepositoryDestination.outputFTPInfo.serverName",
				"contentRepositoryDestination.outputFTPInfo.port",
				"contentRepositoryDestination.outputFTPInfo.userName",
				"contentRepositoryDestination.outputFTPInfo.password",
				"contentRepositoryDestination.outputFTPInfo.protocol",
				"contentRepositoryDestination.outputFTPInfo.type",
				"contentRepositoryDestination.outputFTPInfo.prot",
				"contentRepositoryDestination.outputFTPInfo.pbsz",
				"contentRepositoryDestination.outputFTPInfo.sshKey",
				"contentRepositoryDestination.outputFTPInfo.sshPassphrase",
				"contentRepositoryDestination.outputFTPInfo.folderPath",
				"contentRepositoryDestination.outputFTPInfo.propertiesMap[*]",
				"mailNotification.toAddresses",
				"mailNotification.ccAddresses",
				"mailNotification.bccAddresses",
				"mailNotification.subject",
				"mailNotification.messageText",
				"mailNotification.resultSendType",
				"mailNotification.skipEmptyReports",
				"mailNotification.messageTextWhenJobFails",
				"mailNotification.includingStackTraceWhenJobFails",
				"mailNotification.skipNotificationWhenJobFails"
		);
	}

	public Event setupForm(RequestContext context) throws Exception {
        context.getFlowScope().put(ATTR_NAME_ORGANIZATION_ID, securityContextProvider.getContextUser().getTenantId());
        context.getFlowScope().put(ATTR_NAME_PUBLIC_FOLDER_URI, configuration.getPublicFolderUri());

		// transfer request parameter "isRunNowMode" to flowScope
		String isRunNowMode = context.getRequestParameters().get("isRunNowModeRequest");
		if (isRunNowMode != null) {
			Boolean isRunNow = null;
			if ("true".equalsIgnoreCase(isRunNowMode)) {
				isRunNow = new Boolean(true);
			} else if ("false".equalsIgnoreCase(isRunNowMode)) {
				isRunNow = new Boolean(false);
			}
			context.getFlowScope().put("isRunNowMode", isRunNow);
		}
		
		try {
			return super.setupForm(context);
		} catch (ReportJobNotFoundException e) {
			context.getFlowScope().put("errorMessage", "report.job.edit.not.found");
			context.getFlowScope().put("errorArguments", new Long(e.getJobId()));
			return new Event(this, "notFound");
		}
	}

	protected Object createFormObject(RequestContext context) {
		ReportJob job;
		if (isNewMode(context)) {
			job = createNewReportJob(context);
		} else {
			Long jobIdParam = context.getRequestParameters().getRequiredLong(getEditJobIdParamName());
			long jobId = jobIdParam.longValue();
			job = schedulingService.getScheduledJob(StaticExecutionContextProvider.getExecutionContext(), jobId);
			if (job == null) {
				throw new ReportJobNotFoundException(jobId);
			}
		}
		
		if (job.getMailNotification() == null) {
			job.setMailNotification(new ReportJobMailNotification());
		}
		
		return job;
	}

	protected ReportJob createNewReportJob(RequestContext context) {
		ReportJob job;
		job = new ReportJob();
		ReportJobSimpleTrigger trigger = new ReportJobSimpleTrigger();
		job.setTrigger(trigger);
		job.setSource(new ReportJobSource());
		
		String ownerURI = newJobOwnerURI(context);
		job.getSource().setReportUnitURI(ownerURI);
		
		String reportName;
		int lastSepIdx = ownerURI.lastIndexOf(Folder.SEPARATOR);
		if (lastSepIdx >= 0) {
			reportName = ownerURI.substring(lastSepIdx + Folder.SEPARATOR_LENGTH);
			job.setBaseOutputFilename(reportName);
		} else {
			String quotedURI = "\"" + ownerURI + "\"";
			throw new JSException("jsexception.no.values.to.enumerate", new Object[] {quotedURI});
		}
		
		trigger.setStartType(ReportJobTrigger.START_TYPE_NOW);
		trigger.setOccurrenceCount(1);
		
		ReportJobRepositoryDestination repositoryDestination = new ReportJobRepositoryDestination();
		job.setContentRepositoryDestination(repositoryDestination);
		
		job.addOutputFormat(ReportJob.OUTPUT_FORMAT_PDF);
		
		return job;
	}

	protected String newJobOwnerURI(RequestContext context) {
		String reportUnitURI = context.getFlowScope().getString(getReportUnitURIAttrName());
		// get report uri from request parameter
		if (reportUnitURI == null) {
			reportUnitURI = (String)context.getRequestParameters().get(getReportUnitURIAttrName()+"Request");
			context.getFlowScope().put(getReportUnitURIAttrName(), reportUnitURI);
		}
		return reportUnitURI;
	}
	
	protected ReportJob getReportJob(RequestContext context) throws Exception {
		return (ReportJob) getFormObject(context);
	}

	protected boolean isNewMode(RequestContext context) {
		Boolean isNewMode = context.getFlowScope().getBoolean(getIsNewModeAttrName());
		// get value from request parameter if exist
		String newModeFromRequest = (String)context.getRequestParameters().get(getIsNewModeAttrName()+"Request");
		if (newModeFromRequest != null) {
			if ("true".equalsIgnoreCase(newModeFromRequest)) {
				isNewMode = new Boolean(true);
			} else if ("false".equalsIgnoreCase(newModeFromRequest)) {
				isNewMode = new Boolean(false);
			}
			context.getFlowScope().put(getIsNewModeAttrName(), isNewMode);
		}
		return isNewMode != null && isNewMode.booleanValue();
	}

	public Event setNowModeDefaults(RequestContext context) throws Exception {
		ReportJob job = getReportJob(context);
		String jobLabel = messageSource.getMessage("report.scheduling.job.runNow.label", null, "Run once job", getUserLocale());
		job.setLabel(jobLabel);
		return success();
	}
	
	public Event setOutputReferenceData(RequestContext context) {
		context.getFlowScope().put(getOutputFormatsAttrName(), allOutputFormats);
		if (getLocalesList() != null) {
			UserLocale[] userLocales = getLocalesList().getUserLocales(getUserLocale());
			if (userLocales != null && userLocales.length > 0) {
				context.getRequestScope().put(getLocalesAttrName(), userLocales);
			}
		}

        setTimeZoneData(context);
        return success();
	}


	protected Locale getUserLocale() {
		return LocaleContextHolder.getLocale();
	}
	
	public Event setTriggerReferenceData(RequestContext context) throws Exception {
		context.getRequestScope().put(getIntervalUnitsAttrName(), recurrenceIntervalUnits);
		
		ReportJob reportJob = getReportJob(context);
		byte triggerRecurrenceType = getTriggerRecurrenceType(reportJob.getTrigger());
		context.getRequestScope().put(ATTR_NAME_TRIGGER_RECURRENCE_TYPE, new Byte(triggerRecurrenceType));
		
		if (triggerRecurrenceType == RECURRENCE_TYPE_CALENDAR) {
			context.getRequestScope().put(ATTR_NAME_TRIGGER_ALL_WEEK_DAYS, weekDays);
			context.getRequestScope().put(ATTR_NAME_TRIGGER_ALL_MONTHS, months);
		}

        setTimeZoneData(context);
		return success();
	}

	protected void setTimeZoneData(RequestContext context) {
        if (getTimeZonesList() != null) {
            List timeZones = getTimeZonesList().getTimeZones(getUserLocale());
            context.getRequestScope().put(getTimeZonesAttrName(), timeZones);

            TimeZone userTz = TimeZoneContextHolder.getTimeZone();
            context.getRequestScope().put("preferredTimezone", userTz.getID());
        }
    }

	protected byte getTriggerRecurrenceType(ReportJobTrigger trigger) {
		byte type;
		if (trigger instanceof ReportJobSimpleTrigger) {
			ReportJobSimpleTrigger simpleTrigger = (ReportJobSimpleTrigger) trigger;
			if (simpleTrigger.getOccurrenceCount() == 1) {
				type = RECURRENCE_TYPE_NONE;
			} else {
				type = RECURRENCE_TYPE_SIMPLE;
			}
		} else if (trigger instanceof ReportJobCalendarTrigger) {
			type = RECURRENCE_TYPE_CALENDAR;
		} else {
			String quotedTriggerType ="\"" + trigger.getClass().getName() + "\""; 
			throw new JSException("jsexception.job.unknown.trigger.type", new Object[] {quotedTriggerType});
		}
		return type;
	}

	public Event setTriggerRecurrenceNone(RequestContext context) throws Exception {
		ReportJob job = getReportJob(context);
		ReportJobSimpleTrigger trigger = new ReportJobSimpleTrigger();
		copyCommonTriggerAttributes(trigger, job.getTrigger());
		trigger.setOccurrenceCount(1);
		job.setTrigger(trigger);
		return success();
	}

	public Event setTriggerRecurrenceSimple(RequestContext context) throws Exception {
		ReportJob job = getReportJob(context);
		ReportJobSimpleTrigger trigger = new ReportJobSimpleTrigger();
		copyCommonTriggerAttributes(trigger, job.getTrigger());
		trigger.setOccurrenceCount(ReportJobSimpleTrigger.RECUR_INDEFINITELY);
		trigger.setRecurrenceInterval(1);
		trigger.setRecurrenceIntervalUnit(ReportJobSimpleTrigger.INTERVAL_DAY);
		job.setTrigger(trigger);
		return success();
	}

	public Event setTriggerRecurrenceCalendar(RequestContext context) throws Exception {
		ReportJob job = getReportJob(context);
		ReportJobCalendarTrigger trigger = new ReportJobCalendarTrigger();
		copyCommonTriggerAttributes(trigger, job.getTrigger());
		trigger.setMinutes("0");
		trigger.setHours("0");
		trigger.setDaysType(ReportJobCalendarTrigger.DAYS_TYPE_ALL);
		
		TreeSet selectedMonths = new TreeSet();
		for (Iterator it = months.iterator(); it.hasNext();) {
			ByteEnum month = (ByteEnum) it.next();
			selectedMonths.add(new Byte(month.getCode()));
		}
		trigger.setMonths(selectedMonths);
		
		job.setTrigger(trigger);
		return success();
	}

	protected void copyCommonTriggerAttributes(ReportJobTrigger newTrigger, ReportJobTrigger trigger) {
		newTrigger.setTimezone(trigger.getTimezone());
		newTrigger.setStartType(trigger.getStartType());
		newTrigger.setStartDate(trigger.getStartDate());
		newTrigger.setEndDate(trigger.getEndDate());
        newTrigger.setCalendarName(trigger.getCalendarName());
	}

    public void createAuditReportSchedulingEvent(final String jobType) {
        auditContext.doInAuditContext(new AuditContext.AuditContextCallback() {
            public void execute() {
                auditContext.createAuditEvent(jobType);
            }
        });
    }

    public void closeReportSchedulingAuditEvent(String jobType) {
        auditContext.doInAuditContext(jobType, new AuditContext.AuditContextCallbackWithEvent() {
            public void execute(AuditEvent auditEvent) {

                auditContext.closeAuditEvent(auditEvent);
            }
        });
    }

	public Event saveJob(RequestContext context) throws Exception {
		ReportJob job = getReportJob(context);

		if (job.getMailNotification().isEmpty()) {
			job.setMailNotification(null);
		}
		
		try {
			if (isNewMode(context)) {
                job.setCreationDate(new Timestamp(GregorianCalendar.getInstance().getTimeInMillis()));
                // by default, send alert to owner and admin if job fails
                job.setAlert(new ReportJobAlert());
                createAuditReportSchedulingEvent(SCHEDULE_REPORT.toString());
				schedulingService.scheduleJob(StaticExecutionContextProvider.getExecutionContext(), job);
                closeReportSchedulingAuditEvent(SCHEDULE_REPORT.toString());
			} else {
				try {
                    createAuditReportSchedulingEvent(UPDATE_REPORT_SCHEDULING.toString());
					schedulingService.updateScheduledJob(StaticExecutionContextProvider.getExecutionContext(), job);
                    closeReportSchedulingAuditEvent(UPDATE_REPORT_SCHEDULING.toString());
				} catch (ReportJobNotFoundException e) {
					context.getFlowScope().put("errorMessage", "report.job.save.not.found");
					context.getFlowScope().put("errorArguments", new Long(e.getJobId()));
					return result("notFound");
				}
			}
		} catch (JSValidationException e) {
			String errorEvent = resolveValidationErrorEvent(e);
			if (errorEvent != null) {
				validationUtils.setErrors(getFormErrors(context), e.getErrors(), null);
				return result(errorEvent);
			}

			throw e;
		} finally {
			if (job.getMailNotification() == null) {
				job.setMailNotification(new ReportJobMailNotification());
			}
		}

		return success();
	}

	protected String resolveValidationErrorEvent(JSValidationException e) {
		String event = null;
		errors:
		for (Iterator it = e.getErrors().getErrors().iterator(); it.hasNext();) {
			ValidationError error = (ValidationError) it.next();
			String field = error.getField();
			if (field != null) {
				for (Iterator mapIt = VALIDATION_FIELDS_MAPPINGS.entrySet().iterator(); 
						mapIt.hasNext();) {
					Map.Entry entry = (Map.Entry) mapIt.next();
					String[] prefixes = (String[]) entry.getValue();
					if (fieldMatches(field, prefixes)) {
						event = (String) entry.getKey();
						break errors;
					}
				}
			}
		}
		return event;
	}
	
	protected boolean fieldMatches(String field, String[] fieldPrefixes) {
		boolean matches = false;
		for (int i = 0; i < fieldPrefixes.length; i++) {
			if (field.startsWith(fieldPrefixes[i])) {
				matches = true;
				break;
			}
		}
		return matches;
	}

	protected class MailAddressesEditor extends PropertyEditorSupport {
		
		public String getAsText() {
			StringBuffer sb = new StringBuffer();
			List addresses = (List) getValue();
			if (addresses != null && !addresses.isEmpty()) {
				Iterator it = addresses.iterator();
				String address = (String) it.next();
				sb.append(address);
				while (it.hasNext()) {
					sb.append(getAddressDefaultSeparator());
					address = (String) it.next();
					sb.append(address);
				}
			}
			return sb.toString();
		}

		public void setAsText(String text) throws IllegalArgumentException {
			List addressList = new ArrayList();
			if (text != null && text.trim().length() > 0) {
				String[] addresses = addressSeparatorPattern.split(text.trim());
				for (int i = 0; i < addresses.length; i++) {
					String address = addresses[i].trim();
					if (address.length() > 0) {
						addressList.add(address);
					}
				}
			}
			setValue(addressList);
		}
		
	}
	
	protected static class ByteCollectionEditor extends CustomCollectionEditor {
		
		public ByteCollectionEditor(Class collectionClass) {
			super(collectionClass);
		}

		protected Object convertElement(Object val) {
			if (val == null || val instanceof Byte) {
				return val;
			}
			
			try {
				return Byte.valueOf(val.toString());
			} catch (NumberFormatException e) {
				log.error("error parsing byte value", e);
				throw new JSExceptionWrapper(e);
			}
		}

	}

	public Pattern getAddressSeparatorPattern() {
		return addressSeparatorPattern;
	}

	public void setAddressSeparatorPattern(Pattern addressSeparatorPattern) {
		this.addressSeparatorPattern = addressSeparatorPattern;
	}

	public String getAddressDefaultSeparator() {
		return addressDefaultSeparator;
	}

	public void setAddressDefaultSeparator(String addressDefaultSeparator) {
		this.addressDefaultSeparator = addressDefaultSeparator;
	}

	public List getRecurrenceIntervalUnits() {
		return recurrenceIntervalUnits;
	}

	public void setRecurrenceIntervalUnits(List recurrenceIntervalUnits) {
		this.recurrenceIntervalUnits = recurrenceIntervalUnits;
	}
	
	public RepositoryConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(RepositoryConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * @return Returns the allOutputFormats.
	 */
	public List getAllOutputFormats() {
		return allOutputFormats;
	}

	/**
	 * @param allOutputFormats The allOutputFormats to set.
	 */
	public void setAllOutputFormats(List allOutputFormats) {
		this.allOutputFormats = allOutputFormats;
	}
}
