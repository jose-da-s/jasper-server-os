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
package com.jaspersoft.jasperserver.remote.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.api.JSValidationException;
import com.jaspersoft.jasperserver.api.common.domain.ValidationError;
import com.jaspersoft.jasperserver.api.common.domain.ValidationErrors;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.common.domain.impl.ValidationErrorImpl;
import com.jaspersoft.jasperserver.api.common.domain.impl.ValidationErrorsImpl;
import com.jaspersoft.jasperserver.api.common.error.handling.SecureExceptionHandler;
import com.jaspersoft.jasperserver.api.common.util.TimeZoneContextHolder;
import com.jaspersoft.jasperserver.api.engine.common.service.EngineService;
import com.jaspersoft.jasperserver.api.engine.common.service.ReportExecutionStatusInformation;
import com.jaspersoft.jasperserver.api.engine.common.service.SchedulerReportExecutionStatusSearchCriteria;
import com.jaspersoft.jasperserver.api.engine.common.service.VirtualizerFactory;
import com.jaspersoft.jasperserver.api.engine.jasperreports.common.JSReportExecutionRequestCancelledException;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.PaginationParameters;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.ReportUnitResult;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.DataCacheProvider;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.CopyDestinationExistsException;
import com.jaspersoft.jasperserver.api.metadata.common.service.JSResourceNotFoundException;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.ExecutionsPublishingEventService;
import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.Argument;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.executions.ExecutionPageStatusObject;
import com.jaspersoft.jasperserver.dto.executions.ExecutionStatus;
import com.jaspersoft.jasperserver.dto.reports.inputcontrols.ReportInputControl;
import com.jaspersoft.jasperserver.dto.resources.ClientDataType;
import com.jaspersoft.jasperserver.inputcontrols.cascade.CascadeResourceNotFoundException;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlUITypeMapper;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlsLogicService;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlsValidationException;
import com.jaspersoft.jasperserver.inputcontrols.util.ReportParametersUtils;
import com.jaspersoft.jasperserver.remote.ServiceException;
import com.jaspersoft.jasperserver.remote.exception.*;
import com.jaspersoft.jasperserver.remote.reports.HtmlExportStrategy;
import com.jaspersoft.jasperserver.remote.services.*;
import com.jaspersoft.jasperserver.remote.services.impl.reportinfo.ReportInfo;
import com.jaspersoft.jasperserver.remote.utils.AuditHelper;
import com.jaspersoft.jasperserver.war.action.JSController;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.GenericElementReportTransformer;
import net.sf.jasperreports.engine.export.type.ZoomTypeEnum;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.engine.util.PartsUtil;
import net.sf.jasperreports.export.SimpleExporterInputItem;
import net.sf.jasperreports.web.JRInteractiveException;
import net.sf.jasperreports.j2ee.web.WebReportContext;
import net.sf.jasperreports.interactivity.actions.AbstractAction;
import net.sf.jasperreports.interactivity.actions.Action;
import net.sf.jasperreports.interactivity.actions.MultiAction;
import net.sf.jasperreports.interactivity.actions.SaveZoomCommand;
import net.sf.jasperreports.web.servlets.AsyncJasperPrintAccessor;
import net.sf.jasperreports.web.servlets.JasperPrintAccessor;
import net.sf.jasperreports.web.servlets.ReportExecutionStatus;
import net.sf.jasperreports.web.servlets.ReportPageStatus;
import net.sf.jasperreports.jackson.util.JacksonUtil;
import net.sf.jasperreports.web.util.WebConstants;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.Resource;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.jaspersoft.jasperserver.api.logging.audit.domain.AuditEventType.EXPORT;
import static com.jaspersoft.jasperserver.api.metadata.common.domain.InputControl.TYPE_MULTI_SELECT_LIST_OF_VALUES;
import static com.jaspersoft.jasperserver.api.metadata.user.service.impl.UserManagerServiceImpl.getCurrentUserQualifiedName;
import static com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.InputControlHandler.NOTHING_SUBSTITUTION_VALUE;
import static com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.InputControlHandler.NULL_SUBSTITUTION_VALUE;
import static net.sf.jasperreports.web.servlets.ReportExecutionStatus.Status.CANCELED;
import static net.sf.jasperreports.web.servlets.ReportExecutionStatus.Status.ERROR;

/**
 * Run Report service
 * Run a report unit using the passing in parameters and options
 *
 * @author ykovalchyk
 * @version $Id$
 */
@SuppressWarnings("deprecation")
@Service("runReportService")
public class RunReportServiceImpl implements RunReportService, Serializable, DisposableBean {
    /**
    * Thanks, Eclipse!
    */
    private static final long serialVersionUID = 8292143346647445860L;
    private static final Logger log = LogManager.getLogger(RunReportServiceImpl.class);
    private final static Pattern FILE_NAME_PATTERN = Pattern.compile(".*/([^/]+)$");

    @Resource
    private AuditHelper auditHelper;
    @Resource(name = "concreteEngineService")
    private EngineService engine;
    @Resource(name = "${bean.engineServiceTarget}")
    private EngineService unsecuredEngine;
    @Resource(name="loggableExecutorService")
    private Executor asyncExecutor;
    @Resource
    private InputControlsLogicService inputControlsLogicService;
    @Resource
    private ReportExecutor reportExecutor;
    @Resource(name = "concreteRepository")
    private RepositoryService repositoryService;
    @Resource
    private ErrorDescriptorBuildingService errorDescriptorBuildingService;
    @Resource
    private Integer waitForFinalJasperPrintMs;
    @Resource
    private Map<String, HtmlExportStrategy> htmlExportStrategies;
    @Resource(name = "defaultHtmlExportStrategy")
    private HtmlExportStrategy defaultHtmlExportStrategy;
    @Resource(name = "concreteVirtualizerFactory")
    private VirtualizerFactory virtualizerFactory;
    @Resource
    private SecureExceptionHandler secureExceptionHandler;

    @Resource
    private RunReportServiceCacheFactoryBean cacheFactoryBean;

    @Resource(name = "engineServiceDataCacheProvider")
    private DataCacheProvider dataCacheProvider;

    @Resource(name = "messageSource")
    protected MessageSource messageSource;

    @Resource
    private ExecutionsPublishingEventService publishingEventService;

    @Autowired
    private ApplicationContext applicationContext;

    private Ehcache getExecutionsCache() {
        return cacheFactoryBean.getObject();
    }

    private ReportExecution getReportExecutionFromCache(final String requestId) {
        final Element element = getExecutionsCache().get(requestId);
        if (missesExecution(element)) return null;

        Pair<String, ReportExecution> value = (Pair<String, ReportExecution>) element.getObjectValue();
        if (!isSameUser(value.getKey())) return null;

        return value.getValue();
    }

    /**
     * Returns report execution without checking if it's related to same user or not.
     * Can only be used in cases when prior method is configured to use Spring Security.
     *
     * @param requestId a generated {@link ReportExecution#getRequestId()} parameter
     * @return a report execution that might belong to another user
     */
    private ReportExecution getReportExecutionFromCacheNoUserCheck(final String requestId) {
        final Element element = getExecutionsCache().get(requestId);
        return (!missesExecution(element)) ? ((Pair<String, ReportExecution>) element.getObjectValue()).getValue() : null;
    }

    private void putReportExecutionToCache(final String requestId, final ReportExecution reportExecution) {
        final Element element = new Element(requestId, Pair.of(
                getCurrentUserQualifiedName(), reportExecution
        ));
        getExecutionsCache().put(element);
    }

    public ReportExecution getReportExecution(String requestId) throws ResourceNotFoundException {
        final ReportExecution execution = getReportExecutionFromCache(requestId);
        if (execution == null) {
            throw new ResourceNotFoundException(requestId);
        }
        actualizeExecutionStatus(execution);
        return execution;
    }

    protected void actualizeExecutionStatus(ReportExecution execution) {
        final ReportExecutionStatus reportStatus = execution != null && execution.getReportUnitResult() != null
                && execution.getReportUnitResult().getJasperPrintAccessor() != null
                        ? execution.getReportUnitResult().getJasperPrintAccessor().getReportStatus() : null;
        ExecutionStatus executionStatus = ExecutionStatus.queued;
        if (reportStatus != null) {
            final ReportExecutionStatus.Status status = reportStatus.getStatus();
            switch (status) {
                case CANCELED:
                    executionStatus = ExecutionStatus.cancelled;
                    break;
                case ERROR: {
                    executionStatus = ExecutionStatus.failed;
                    execution.setErrorDescriptor(secureExceptionHandler.handleException(reportStatus.getError()));
                }
                break;
                case FINISHED: {
                    executionStatus = ExecutionStatus.ready;
                    execution.setTotalPages(reportStatus.getTotalPageCount());
                }
                break;
                case RUNNING: {
                    executionStatus = ExecutionStatus.execution;
                    execution.setCurrentPage(reportStatus.getCurrentPageCount());
                }
                break;
            }
        }
        ExecutionStatus currentStatus = execution.getStatus();
        // first if loop is just trying to minimize the thread lock
        // Execution Status: CANCELED, ERROR, FINISHED are final.  Can't be overwritten.
        if (currentStatus != executionStatus &&
                (currentStatus == ExecutionStatus.execution || currentStatus == ExecutionStatus.queued)) {
            // trying to change status, but first, try to lock the thread to avoid race condition
            synchronized (this) {
                currentStatus = execution.getStatus();
                if (currentStatus != executionStatus &&
                        (currentStatus == ExecutionStatus.execution || currentStatus == ExecutionStatus.queued)) {
                    if (reportStatus != null) {
                        // set new status from report status
                        log.debug("Changing report execution status for " + execution.getRequestId() + " from " + currentStatus + " to " + executionStatus);
                        execution.setStatus(executionStatus);
                    } else {
                        // if it is not in engineExecutions map, the report execution might not be fully initialized yet
                        if (engine.getReportExecutionStatusList().stream().noneMatch(info -> info.getRequestId().equals(execution.getRequestId()))) {
                            log.debug("Set report execution status for " + execution.getRequestId() + " to QUEUE since report status is not available and the execution is not in engine executions map.");
                            execution.setStatus(ExecutionStatus.queued);
                        } else {
                            // don't do anything
                            // report status is in engineExecutions map, it is ok to maintain its execution or queue status.
                            log.debug("Set report execution status for " + execution.getRequestId() + " remains unchanged since report status is not available, but the execution is in engine executions map.");
                        }
                    }
                } else {
                    log.debug("Report execution status for " + execution.getRequestId() + " is final: " + currentStatus);

                }
            }
        } else {
            log.debug("Report execution status for " + execution.getRequestId() + " is final: " + currentStatus);
        }
    }

    public void startReportExecution(ReportExecution reportExecution) {
        startReportExecution(reportExecution, null);
    }

    public void startReportExecution(ReportExecution reportExecution,
            ReportExecutionOptions reportExecutionOptions) {
        final ReportExecutionOptions options = reportExecutionOptions != null ? reportExecutionOptions
                : reportExecution.getOptions();
        ReportUnitResult reportUnitResult = reportExecution.getReportUnitResult();
        try {
            if (reportUnitResult!=null) {
                virtualizerFactory.disposeReport(reportUnitResult);
            }
        } catch (RuntimeException ex) {
            log.warn("Report execution cleanup failed: ", ex);
        }
        if (ExecutionStatus.execution == reportExecution.getStatus()) {
            cancelReportExecution(reportExecution.getRequestId());
            reportExecution.setStatus(ExecutionStatus.cancelled);
            reportExecution = createFreshReportExecutionCopy(reportExecution, options);
        }
        // if we are restarting reportExecution, then we need to remove link to relatedExecution. It will be ran again
        // if alternative pagination mode will be needed
        reportExecution.setRelatedExecution(null);
        reportExecution.setReportUnitResult(null);
        reportExecution.setErrorDescriptor(null);
        reportExecution.setStatus(ExecutionStatus.execution);
        final Locale locale = LocaleContextHolder.getLocale();
        final TimeZone timeZone = TimeZoneContextHolder.getTimeZone();
        final SecurityContext context = SecurityContextHolder.getContext();
        final ReportExecution finalReportExecution = reportExecution;
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        asyncExecutor.execute(new Runnable() {
            @Override
            public void run() {
                LocaleContextHolder.setLocale(locale);
                TimeZoneContextHolder.setTimeZone(timeZone);
                SecurityContextHolder.setContext(context);
                RequestContextHolder.setRequestAttributes(requestAttributes);
                ReportUnitResult reportUnitResult = null;
                ErrorDescriptor errorDescriptor = null;

                try {
                    final String reportUnitUri = finalReportExecution.getReportURI();
                    Map<String, Object> convertedParameters = finalReportExecution.getConvertedParameters();
                    if (convertedParameters == null) {
                        synchronized (finalReportExecution) {
                            convertedParameters = finalReportExecution.getConvertedParameters();
                            if (convertedParameters == null) {
                                // no cached converted parameters. Let's make them by running input controls logic
                                // on raw parameters
                                final Map<String, String[]> rawParameters = finalReportExecution.getRawParameters();
                                // convert parameters from raw strings to objects
                                try {
                                    convertedParameters = executeInputControlsCascadeWithRawInput(reportUnitUri, rawParameters);
                                } catch (CascadeResourceNotFoundException e) {
                                    throw new ResourceNotFoundException("URI:" + e.getResourceUri() + " Type:" + e.getResourceType());
                                } catch (InputControlsValidationException e) {
                                    // raw type is used in core class. Cast is safe.
                                    @SuppressWarnings("unchecked")
                                    List<ValidationError> errors = e.getErrors().getErrors();
                                    List<String> errorParameters = new ArrayList<String>();
                                    for (ValidationError error : errors) {
                                        errorParameters.add(error.toString());
                                    }
                                    throw new ErrorDescriptorException(new ErrorDescriptor().setErrorCode("input.controls.validation.error")
                                            .setMessage("Input controls validation failure").addParameters(errorParameters.toArray()));
                                }
                                // forward parameters without input control defined
                                for (String currentKey : rawParameters.keySet()) {
                                    String[] currentValue = rawParameters.get(currentKey);
                                    if (currentValue != null && !convertedParameters.containsKey(currentKey)) {
                                        Object valueToForward;
                                        if (currentValue.length == 1) {
                                            // forward as single value
                                            valueToForward = currentValue[0];
                                        } else {
                                            // forward as collection
                                            Collection<String> collection = new ArrayList<String>();
                                            collection.addAll(Arrays.asList(currentValue));
                                            valueToForward = collection;
                                        }
                                        convertedParameters.put(currentKey, valueToForward);
                                    }
                                }
                            }
                            // cache converted parameter for feather runes.
                            finalReportExecution.setConvertedParameters(convertedParameters);
                        }
                    }
                    finalReportExecution.setStatus(ExecutionStatus.execution);
                    reportUnitResult = reportExecutor.runReport(reportUnitUri, convertedParameters, options);
                    //If reportExecutionOptions.ignorePagination wasn't set, then only here we can know what was a value
                    // for ignorePagination in current report execution. Let's cache it to use for further executions if
                    // not specified explicitly.
                    options.setDefaultIgnorePagination(!reportUnitResult.isPaginated());

                } catch (ErrorDescriptorException e) {
                    errorDescriptor = e.getErrorDescriptor();
                } catch (JSReportExecutionRequestCancelledException e){
                    // do nothing in this thread. Correct status is set by the thread, cancelled the execution.
                } catch (Exception e) {
                    // if report execution is interrupted, then it's cancelled. Do nothing in this case.
                    if(!(e.getCause() instanceof InterruptedException)){
                        // if not the case, then let's build error descriptor
                        errorDescriptor = errorDescriptorBuildingService.buildErrorDescriptor(e);
                    }
                }
                if (errorDescriptor != null) {
                    finalReportExecution.setErrorDescriptor(errorDescriptor);
                }
                if (reportUnitResult != null) {
                    final String requestId = options.getRequestId();
                    if (!requestId.equals(reportUnitResult.getRequestId())) {
                        throw new IllegalStateException("Report unit request and report unit result should have the same ID. RequestId: "
                                + requestId + " ResultId: " + reportUnitResult.getRequestId());
                    }
                    finalReportExecution.setReportUnitResult(reportUnitResult);
                    // wait for report execution to complete for execution status actualization
                    reportUnitResult.getJasperPrint();
                    actualizeExecutionStatus(finalReportExecution);
                }
            }
        });
    }

    @Override
    public ReportExecution getReportExecutionFromRawParameters(final String reportUnitURI, final Map<String, String[]> rawParameters,
            ReportExecutionOptions inputOptions, ExportExecutionOptions exportOptions,
            Map<String, Object> sessionParams) throws ErrorDescriptorException, JSValidationException {
        // basic synchronous validation:
        // check report URI for null
        if (reportUnitURI == null) {
            throw new MandatoryParameterNotFoundException("reportUnitUri");
        }
        // check report resource for existence
        final com.jaspersoft.jasperserver.api.metadata.common.domain.Resource resource = repositoryService
                .getResource(ExecutionContextImpl.getRuntimeExecutionContext(), reportUnitURI);
        if (resource == null) {
            throw new ResourceNotFoundException(reportUnitURI);
        } else if(!reportExecutor.isRunnableResource(resource)){
            throw new IllegalParameterValueException("reportURI", reportUnitURI);
        }

        final ReportExecutionOptions options = inputOptions != null ? inputOptions : new ReportExecutionOptions();

        if (sessionParams != null) {
            SimpleReportContext reportContext = new SimpleReportContext();
            for (Map.Entry<String, Object> entry: sessionParams.entrySet()) {
                reportContext.setParameterValue(entry.getKey(), entry.getValue());
            }
            options.setReportContext(reportContext);
        }

        String exportFormat = (exportOptions != null && exportOptions.getOutputFormat() != null
        		&& !exportOptions.getOutputFormat().isEmpty()) ? exportOptions.getOutputFormat().toUpperCase() : null;
        if (exportFormat != null) {
            PaginationParameters exportPagination = getExportPaginationParameters(reportUnitURI, null, exportFormat);
            options.setPaginationDefaults(exportPagination);
        }

        final ReportExecution execution = createReportExecution(reportUnitURI, rawParameters, options);
        final String requestId = execution.getRequestId();

        startReportExecution(execution);

        if(!options.isAsync()){
            // wait till report execution is complete
            try{
                execution.getFinalReportUnitResult().getJasperPrintAccessor().getFinalJasperPrint();
            } catch (ErrorDescriptorException e){
                throw e;
            } catch (Exception e){
                // if report fails in non async mode, then send error immediately
                throw new IllegalParameterValueException(secureExceptionHandler.handleException(e));
            }
        }
        if (exportFormat != null) {
            final ExportExecution exportExecution = executeExport(exportOptions, execution);
            if (!options.isAsync()) {
                // wait till export is complete
                try {
                    exportExecution.getFinalOutputResource();
                } catch (ExportExecutionRejectedException e) {
                    // if report fails in non async mode, then send error immediately
                    throw new IllegalParameterValueException(e.getErrorDescriptor());
                }
            }
        }
        return getReportExecution(requestId);
    }

    ReportExecution createReportExecution(String reportUnitURI, Map<String, String[]> rawParameters, ReportExecutionOptions options) {
        final ReportExecution execution = new ReportExecution();
        final String requestId;
        if (options.getRequestId() == null) {
            requestId = UUID.randomUUID().toString();
            options.setRequestId(requestId);
        } else {
            requestId = options.getRequestId();
        }
        execution.setOptions(options);
        execution.setRequestId(requestId);
        execution.setReportURI(reportUnitURI);
        execution.setRawParameters(rawParameters);

        publishingEventService.publishReportExecutions(requestId);
        putReportExecutionToCache(requestId, execution);

        return execution;
    }

    private ReportExecution createFreshReportExecutionCopy(ReportExecution copy, ReportExecutionOptions options) {
        String requestId = copy.getRequestId();
        Assert.notNull(requestId);

        options.setRequestId(requestId);

        return createReportExecution(copy.getReportURI(), copy.getRawParameters(), options);
    }

    public ExportExecution executeExport(final String executionId, final ExportExecutionOptions exportOptions)
            throws ErrorDescriptorException {
        final ReportExecution execution = getReportExecutionFromCache(executionId);
        if (execution == null)
            throw new ResourceNotFoundException(executionId);
        return executeExport(exportOptions, execution);
    }

    protected ExportExecution executeExport(ExportExecutionOptions exportOptions, final ReportExecution reportExecution) throws ErrorDescriptorException {
        final ExportExecution exportExecution = new ExportExecution();
        exportExecution.setSecureExceptionHandler(secureExceptionHandler);
        exportExecution.setStatus(ExecutionStatus.queued);
        exportExecution.setOptions(exportOptions);
        reportExecution.getExports().put(exportExecution);
        startExport(reportExecution, exportExecution);
        return exportExecution;
    }

    protected void startExport(final ReportExecution reportExecution, final ExportExecution exportExecution) {
        final SecurityContext context = SecurityContextHolder.getContext();
        final Locale locale = LocaleContextHolder.getLocale();
        asyncExecutor.execute(new Runnable() {
            @Override
            public void run() {
                SecurityContextHolder.setContext(context);
                LocaleContextHolder.setLocale(locale);
                try {
                    ReportUnitResult reportUnitResult;
                    Boolean ignoreCancelledReportExecution = exportExecution.getOptions().getIgnoreCancelledReportExecution();
                    if (ignoreCancelledReportExecution && ExecutionStatus.cancelled.equals(reportExecution.getStatus())) {
                        reportUnitResult = reportExecution.getReportUnitResult();
                    } else {
                        reportUnitResult = reportExecution.getFinalReportUnitResult();
                    }

                    if (reportUnitResult == null) {
                        exportExecution.setErrorDescriptor(new ErrorDescriptor()
                                .setErrorCode("export.failed")
                                .setMessage("Export can't be executed. Report execution status is '"
                                        + reportExecution.getStatus().toString() + "'."
                                        + (reportExecution.getErrorDescriptor() != null
                                        ? " " + reportExecution.getErrorDescriptor().toString() : "")));
                    } else {
                        executeExport(exportExecution, reportExecution);
                    }

                } catch (ErrorDescriptorException e) {
                    exportExecution.setErrorDescriptor(secureExceptionHandler.handleException(e, e.getErrorDescriptor()));
                } catch (Exception e) {
                    exportExecution.setErrorDescriptor(secureExceptionHandler.handleException(e));
                }
            }
        });
    }

    /**
     * If client wants to get export with another pagination mode, then we have to run report again with
     * alternate ignorePagination flag value. This method is intended to do it.
     * @param originalReportExecution - the original report execution to use as a template for related execution.
     * @param pagination - ignore pagination flag for report execution
     * @return related report execution instance
     */
    protected ReportExecution runRelatedExecution(ReportExecution originalReportExecution, PaginationParameters pagination){
        final ReportExecution relatedReportExecution = new ReportExecution();
        final String requestId = UUID.randomUUID().toString();
        relatedReportExecution.setRequestId(requestId);
        relatedReportExecution.setReportURI(originalReportExecution.getReportURI());
        relatedReportExecution.setRawParameters(originalReportExecution.getRawParameters());
        relatedReportExecution.setConvertedParameters(originalReportExecution.getConvertedParameters());
        relatedReportExecution.setOptions(new ReportExecutionOptions(originalReportExecution.getOptions()).setPaginationParameters(pagination));
        putReportExecutionToCache(requestId, relatedReportExecution);
        startReportExecution(relatedReportExecution);
        originalReportExecution.setRelatedExecution(relatedReportExecution);
        return relatedReportExecution;
    }

    protected void executeExport(ExportExecution exportExecution, ReportExecution reportExecution) {
        ReportUnitResult reportUnitResult;
        Boolean ignoreCancelledReportExecution = exportExecution.getOptions().getIgnoreCancelledReportExecution();
        if (ignoreCancelledReportExecution && ExecutionStatus.cancelled.equals(reportExecution.getStatus())) {
            reportUnitResult = reportExecution.getReportUnitResult();
        } else {
            reportUnitResult = reportExecution.getFinalReportUnitResult();
        }

        JasperPrintAccessor jasperPrintAccessor = reportUnitResult.getJasperPrintAccessor();

        String rawOutputFormat = exportExecution.getOptions().getOutputFormat();
        String outputFormat = rawOutputFormat != null ? rawOutputFormat.toUpperCase() : Argument.RUN_OUTPUT_FORMAT_PDF;
        PaginationParameters exportPagination = getExportPaginationParameters(reportUnitResult.getReportUnitURI(), jasperPrintAccessor,
        		outputFormat);

        final Boolean ignorePagination = exportExecution.getOptions().getIgnorePagination();
        if (ignorePagination != null) {
        	exportPagination = new PaginationParameters(exportPagination);
        	exportPagination.setPaginated(!ignorePagination);
        }

        if(!reportUnitResult.matchesPagination(exportPagination)){
    		// oops, we have to provide export with another pagination mode.
            ReportExecution relatedExecution = getRelatedExecution(reportExecution, exportPagination);
            // let's take a jasperPrintAccessor from related execution with appropriate ignorePagination flag value
            jasperPrintAccessor = relatedExecution.getFinalReportUnitResult().getJasperPrintAccessor();
        }
        try {
            ExportExecutionOptions exportOptions = exportExecution.getOptions();
            Integer exportEndPage = null;
            if (exportOptions.getPages() != null) {
                exportEndPage = exportOptions.getPages().getPage() != null ? exportOptions.getPages().getPage() : exportOptions.getPages().getEndPage();
            }
            JasperPrint jasperPrint;
            boolean isOutputFinal = true;
            long outputTimestamp = 0l;
            if (exportEndPage != null) {
                // convert page number to pageIndex (0-based)
                exportEndPage = exportEndPage - 1;
                if(waitForFinalJasperPrintMs > 0 && exportEndPage == 0
                        && jasperPrintAccessor instanceof AsyncJasperPrintAccessor){
                    ((AsyncJasperPrintAccessor) jasperPrintAccessor).waitForFinalJasperPrint(waitForFinalJasperPrintMs);
                }
                // in case if jasperPrintAccessor is AsyncJasperPrintAccessor and pageStatus() method is invoked,
                // then current thread waits till requested page is generated.
                final ReportPageStatus reportPageStatus = jasperPrintAccessor.pageStatus(exportEndPage, null);
                isOutputFinal = reportPageStatus.isPageFinal();
                outputTimestamp = reportPageStatus.getTimestamp();
                jasperPrint = jasperPrintAccessor.getJasperPrint();
            } else {
                jasperPrint = jasperPrintAccessor.getFinalJasperPrint();
                final String anchor = exportOptions.getAnchor();
                if(anchor != null && !anchor.isEmpty()){
                    final JRPrintAnchorIndex jrPrintAnchorIndex = jasperPrint.getAnchorIndexes().get(anchor);
                    if(jrPrintAnchorIndex != null){
                        exportOptions.setPages(new ReportOutputPages().setPage(jrPrintAnchorIndex.getPageIndex() + 1));
                    } else {
                        throw new IllegalParameterValueException("anchor", anchor);
                    }
                }
            }
            final ReportExecutionStatus.Status status = jasperPrintAccessor.getReportStatus().getStatus();
            if (ERROR.equals(status)) {
                exportExecution.setErrorDescriptor(errorDescriptorBuildingService
                        .buildErrorDescriptor(jasperPrintAccessor.getReportStatus().getError()));
            } else if (CANCELED.equals(status) && !ignoreCancelledReportExecution) {
                exportExecution.setStatus(ExecutionStatus.cancelled);
            } else {
                exportExecution.setStatus(ExecutionStatus.execution);
                if (Argument.RUN_OUTPUT_FORMAT_HTML.equalsIgnoreCase(outputFormat)) {
                    HtmlExportStrategy htmlExportStrategy = htmlExportStrategies.get(exportExecution.getOptions().getMarkupType()) != null
                            ? htmlExportStrategies.get(exportExecution.getOptions().getMarkupType()) : defaultHtmlExportStrategy;
                    htmlExportStrategy.export(reportExecution, exportExecution, jasperPrint);
                } else {
                    generateReportOutput(reportExecution, jasperPrint, outputFormat,
                            exportExecution, exportExecution.getOptions().getPages());
                }
                exportExecution.getOutputResource().setOutputFinal(isOutputFinal);
                exportExecution.getOutputResource().setOutputTimestamp(outputTimestamp);

                String zoomProperty = jasperPrint.getProperty(SaveZoomCommand.PROPERTY_VIEWER_ZOOM);
                String outputZoom = null;
                if (zoomProperty != null) {
                    ZoomTypeEnum zoomType = ZoomTypeEnum.getByName(zoomProperty);
                    outputZoom = zoomType != null ? zoomType.getHtmlValue() : zoomProperty;
                }
                exportExecution.getOutputResource().setOutputZoom(outputZoom);
                exportExecution.getOutputResource().setDataTimestampMessage(getLocalizedTimestampMessage(reportUnitResult.getDataTimestamp()));
                exportExecution.getOutputResource().setLastPartialPage(
                        jasperPrintAccessor.getReportStatus().getCurrentPageCount());

                ReportContext reportContext = reportUnitResult.getReportContext();
                DataCacheProvider.SnapshotSaveStatus snapshotSaveStatus =
                        dataCacheProvider.getSnapshotSaveStatus(reportContext);

                if (snapshotSaveStatus != null) {
                    exportExecution.getOutputResource().setSnapshotSaveStatus(snapshotSaveStatus.toString());
                }

                exportExecution.setStatus(ExecutionStatus.ready);
            }
        } catch (ErrorDescriptorException ex) {
            if("page.number.out.of.range".equals(ex.getErrorDescriptor().getErrorCode())) {
               exportExecution.setErrorDescriptor(ex.getErrorDescriptor());
            } else {
                exportExecution.setErrorDescriptor(
                    secureExceptionHandler.handleException(
                        ex.getCause() != null ? ex.getCause() : ex,
                        ex.getErrorDescriptor()));
            }
        } catch (Exception ex) {
            log.debug("Unexpected error occurs during export", ex);
            exportExecution.setErrorDescriptor(secureExceptionHandler.handleException(ex));
        }
    }

    @Override
	public ReportExecution getRelatedExecution(ReportExecution reportExecution, PaginationParameters exportPagination) {
		// Let's check if we already have jasperPrint with this mode generated:
		ReportExecution relatedExecution = reportExecution.getRelatedExecution();
		if(relatedExecution == null
				|| !relatedExecution.getFinalReportUnitResult().matchesPagination(exportPagination)){
		    synchronized (reportExecution){
		        relatedExecution = reportExecution.getRelatedExecution();
		        if(relatedExecution == null
		        		|| !relatedExecution.getFinalReportUnitResult().matchesPagination(exportPagination)){
		            // No. We don't have generated jasperPrint for this pagination mode.
		            // Let's run another report execution to generate it.
		        	//TODO use a data snapshot to refill the report with a different pagination
		            relatedExecution = runRelatedExecution(reportExecution, exportPagination);
		            // link it to current report execution to track it's status and to be able to cancel it.
		            reportExecution.setRelatedExecution(relatedExecution);
		        	//TODO keep a map of ReportExecution per PaginationParameters instead of replacing the single related execution
		            //not changing now (6.4.0 hotfix) because it would change the ReportExecution JSON/XML serialization
		        }
		    }
		}
		return relatedExecution;
	}

    private String getLocalizedTimestampMessage(Date dataTimestamp) {
        if (dataTimestamp != null) {
            Locale locale = LocaleContextHolder.getLocale();
            TimeZone timeZone = TimeZoneContextHolder.getTimeZone();
            String dateFormat = messageSource.getMessage("date.format", null, locale);
            String timeFormat = messageSource.getMessage("time.format", null, locale);

            SimpleDateFormat dateFormatter =  new SimpleDateFormat(dateFormat);
            dateFormatter.setTimeZone(timeZone);
            String reportRefreshDate = dateFormatter.format(dataTimestamp);

            SimpleDateFormat timeFormatter =  new SimpleDateFormat(timeFormat);
            timeFormatter.setTimeZone(timeZone);
            String reportRefreshTime = timeFormatter.format(dataTimestamp);

            return messageSource.getMessage("jasper.report.view.data.snapshot.message", new Object[] { reportRefreshDate, reportRefreshTime }, locale);
        }

        return null;
    }

    protected PaginationParameters getExportPaginationParameters(String reportURI, JasperPrintAccessor jasperPrintAccessor,
    		String outputFormat) {
    	PaginationParameters pagination;
    	if (Argument.RUN_OUTPUT_FORMAT_HTML.equalsIgnoreCase(outputFormat)
    			|| Argument.RUN_OUTPUT_FORMAT_JRPRINT.equalsIgnoreCase(outputFormat)) {
    		//default params
    		pagination = new PaginationParameters();
    	} else {
    		pagination = reportExecutor.getExportPaginationParameters(reportURI,
    				jasperPrintAccessor == null ? null : jasperPrintAccessor.getJasperPrint(), outputFormat);
    	}
		return pagination;
	}

    public ReportOutputResource getOutputResource(String executionId, ExportExecutionOptions exportExecutionOptions) throws ErrorDescriptorException {
        final ReportExecution execution = getReportExecutionFromCache(executionId);
        if (execution == null) throw new ResourceNotFoundException(executionId);
        return executeExport(exportExecutionOptions, execution).getFinalOutputResource();
    }

    public ReportOutputResource getOutputResource(String executionId, String exportId) throws ErrorDescriptorException {
        final ReportExecution reportExecution = getReportExecution(executionId);
        final ExportExecution exportExecution = getExportExecution(executionId, exportId);
        if (exportExecution.getStatus() == ExecutionStatus.cancelled
                && !(reportExecution.getStatus() == ExecutionStatus.cancelled)) {
            // cancelled status means export reset is done. Need to rerun
            exportExecution.setStatus(ExecutionStatus.queued);
            startExport(reportExecution, exportExecution);
        }
        return exportExecution.getFinalOutputResource();
    }

    public ExportExecution getExportExecution(String executionId, String exportId) throws ResourceNotFoundException {
        final ReportExecution execution = getReportExecutionFromCache(executionId);
        if (execution == null) throw new ResourceNotFoundException(executionId);
        final ExportExecution exportExecution = execution.getExports().get(exportId);
        if (exportExecution == null) throw new ResourceNotFoundException(exportId);
        return exportExecution;
    }

    public ReportOutputResource getAttachment(String executionId, String exportId, String attachmentName) throws ResourceNotFoundException {
        ReportOutputResource outputResource = getExportExecution(executionId, exportId).getAttachments().get(attachmentName);
        if (outputResource == null) throw new ResourceNotFoundException(attachmentName);
        return outputResource;
    }

    @Override
    public Response getReportInfo(String reportExecutionId) {
        ReportExecution reportExecution = getReportExecutionFromCache(reportExecutionId);
        if (reportExecution == null) {
            throw new ResourceNotFoundException(reportExecutionId);
        }

        ReportUnitResult reportUnitResult = reportExecution.getReportUnitResult();
        JasperPrintAccessor jasperPrintAccessor = reportUnitResult != null ?
                reportUnitResult.getJasperPrintAccessor() : null;

        ReportInfo reportInfo = new ReportInfo();

        if (jasperPrintAccessor != null) {
            JasperPrint jasperPrint = jasperPrintAccessor.getFinalJasperPrint();

            List<PrintBookmark> bookmarks = jasperPrint.getBookmarks();
            if (bookmarks != null && bookmarks.size() > 0) {
                reportInfo.setBookmarks(bookmarks);
            }

            PrintParts parts = PartsUtil.instance(reportExecutor.getJasperReportsContext(
            		reportExecution.getOptions().isInteractive())).getVisibleParts(jasperPrint);
            if (parts != null && parts.hasParts()) {
                reportInfo.setParts(parts, jasperPrint.getName());
            }
        }

        return Response.ok(reportInfo).build();
    }

    @Override
    public Response getReportExecutionPageStatus(String reportExecutionId, String pages) {
        ReportOutputPages outputPages = ReportOutputPages.valueOf(pages);
        Response.ResponseBuilder response;
        ExecutionPageStatusObject pageStatusObject = new ExecutionPageStatusObject();

        if (outputPages == null || (outputPages.getPage() == null && outputPages.getStartPage() == null && outputPages.getEndPage() == null)) {
            response = Response.noContent();
        } else {
            if (outputPages.getPage() != null) {
                outputPages.setStartPage(outputPages.getPage()).setEndPage(outputPages.getPage());
            }

            Map<String, String> result = new HashMap<>();

            ReportExecution reportExecution = getReportExecutionFromCache(reportExecutionId);
            if (reportExecution == null) {
                throw new ResourceNotFoundException(reportExecutionId);
            }

            ReportUnitResult reportUnitResult = reportExecution.getReportUnitResult();
            JasperPrintAccessor jasperPrintAccessor = reportUnitResult != null ?
                    reportUnitResult.getJasperPrintAccessor() : null;
            if (jasperPrintAccessor == null) {
                response = Response.ok(result);
                return response.build();
            }

            ReportExecutionStatus reportStatus = jasperPrintAccessor.getReportStatus();

            if (reportStatus.getStatus() == ReportExecutionStatus.Status.ERROR) {
                pageStatusObject.setReportStatus(executionStatus(reportStatus));
                pageStatusObject.setErrorDescriptor(secureExceptionHandler.handleException(reportStatus.getError()));

                return Response.ok(pageStatusObject).build();
            }

            boolean exists = true;
            boolean pageFinal = true;
            long pageTimestamp = 0l;
            for (int idx = outputPages.getStartPage() - 1; idx <= outputPages.getEndPage() - 1; ++idx) {
                ReportPageStatus pageStatus = jasperPrintAccessor.pageStatus(idx, null);
                if (!pageStatus.pageExists()) {
                    exists = false;
                    break;
                }
                pageFinal = pageFinal && pageStatus.isPageFinal();
                pageTimestamp = Math.max(pageTimestamp, pageStatus.getTimestamp());
            }

            if (exists) {
                pageStatusObject.setPageFinal(pageFinal);
                pageStatusObject.setPageTimestamp(pageTimestamp);
                pageStatusObject.setReportStatus(executionStatus(reportStatus));

                response = Response.ok(pageStatusObject);
            } else {
                response = Response.status(Response.Status.NOT_FOUND);
            }
        }

        return response.build();
    }

    public Response getStatusForAsyncCancelledExecution(String reportExecutionId) {
        ReportExecution reportExecution = getReportExecutionFromCache(reportExecutionId);
        if (reportExecution == null) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        ReportUnitResult reportUnitResult = reportExecution.getReportUnitResult();
        JasperPrintAccessor printAccessor = reportUnitResult.getJasperPrintAccessor();
        try {
            // this will wait for the report to end
            printAccessor.getFinalJasperPrint();
        } catch (JRRuntimeException e) {
            // we don't need to handle the exception here, we're doing getReportStatus() below
        }

        Map<String, Object> result = new LinkedHashMap<>();

        ReportExecutionStatus reportStatus = printAccessor.getReportStatus();
        result.put("lastPartialPage", reportStatus.getCurrentPageCount() - 1);

        ExecutionStatus executionStatus;
        switch (reportStatus.getStatus()) {
            case FINISHED:
                executionStatus = ExecutionStatus.ready;
                Integer totalPageCount = reportStatus.getTotalPageCount();
                result.put("totalPages", totalPageCount);

                ReportContext reportContext = reportUnitResult.getReportContext();
                DataCacheProvider.SnapshotSaveStatus snapshotSaveStatus =
                        dataCacheProvider.getSnapshotSaveStatus(reportContext);

                if (snapshotSaveStatus != null) {
                    result.put("snapshotSaveStatus", snapshotSaveStatus.toString());
                }

                break;
            case ERROR:
                executionStatus = ExecutionStatus.failed;
                break;
            case CANCELED:
                executionStatus = ExecutionStatus.cancelled;
                break;
            case RUNNING:
            default:
                executionStatus = ExecutionStatus.queued;
                break;
        }

        result.put("reportStatus", executionStatus);

        return Response.ok(result).build();
    }

    private ExecutionStatus executionStatus(ReportExecutionStatus reportStatus) {
        ExecutionStatus executionStatus;
        switch(reportStatus.getStatus()) {
            case CANCELED:
                executionStatus = ExecutionStatus.cancelled;
                break;
            case ERROR: {
                executionStatus = ExecutionStatus.failed;
            }
            break;
            case FINISHED: {
                executionStatus = ExecutionStatus.ready;
            }
            break;
            case RUNNING: {
                executionStatus = ExecutionStatus.execution;
            }
            break;
            default: {
                executionStatus = ExecutionStatus.queued;
            }
        }

        return executionStatus;
    }

    @Override
    public Response runReportAction(String reportExecutionId, String actionData) {
        Map<String, Object> result = new LinkedHashMap<>();
        Response.Status resultStatus = Response.Status.OK;

        if (reportExecutionId != null && actionData != null) {
            ReportExecution reportExecution = getReportExecutionFromCache(reportExecutionId);
            if (reportExecution == null) {
                throw new ResourceNotFoundException(reportExecutionId);
            }
            ReportContext reportContext = reportExecution.getReportUnitResult().getReportContext();
            JasperReportsContext currentJasperReportsContext = reportExecutor.getJasperReportsContext(reportExecution.getOptions().isInteractive());

            reportContext.setParameterValue(WebConstants.REQUEST_PARAMETER_REPORT_URI, reportExecution.getReportURI());
            Action action = getAction(actionData, currentJasperReportsContext, reportContext);
            JSController controller = new JSController(currentJasperReportsContext);

            ReportUnitResult reportUnitResult = reportExecution.getReportUnitResult();
            if (reportUnitResult != null) {
                reportContext.setParameterValue(WebReportContext.REPORT_CONTEXT_PARAMETER_JASPER_PRINT_ACCESSOR, reportUnitResult.getJasperPrintAccessor());
            }

            try {
                // clear search stuff before performing an action
                if (action.requiresRefill()) {
                    reportContext.setParameterValue("net.sf.jasperreports.search.term.highlighter", null);
                }

                controller.runAction(reportContext, action);
                result.put("contextid", reportExecutionId);

                // FIXMEJIVE: actions shoud return their own ActionResult that would contribute with JSON object to the output
                JsonNode actionResult = (JsonNode) reportContext.getParameterValue("net.sf.jasperreports.web.actions.result.json");
                if (actionResult != null) {
                    result.put("actionResult", actionResult);
                    reportContext.setParameterValue("net.sf.jasperreports.web.actions.result.json", null);
                }

                if (action.requiresRefill()) {
                    startReportExecution(reportExecution);
                }

            } catch (JRInteractiveException e) {
                resultStatus = Response.Status.INTERNAL_SERVER_ERROR;
                result = new LinkedHashMap<>();
                result.put("msg", "The server encountered an error!");
                result.put("devmsg", e.getMessage());
            } catch (CopyDestinationExistsException e) {
                resultStatus = Response.Status.FORBIDDEN;
                result = new LinkedHashMap<>();
                result.put("msg", e.getMessage());
                result.put("code", "resource.already.exists");
            } catch (JSResourceNotFoundException e) {
                resultStatus = Response.Status.FORBIDDEN;
                result = new LinkedHashMap<>();
                result.put("msg", "Folder not found");
                result.put("code", "folder.not.found");
            } catch (AccessDeniedException e) {
                resultStatus = Response.Status.FORBIDDEN;
                result = new LinkedHashMap<>();
                result.put("msg", e.getMessage());
                result.put("code", "access.denied");
            }
        } else {
            resultStatus = Response.Status.BAD_REQUEST;
        }

        return Response.status(resultStatus).entity(Collections.singletonMap("result", result)).build();
    }

    private Action getAction(String jsonData, JasperReportsContext jasperReportsContext, ReportContext reportContext) {
        Action result = null;
        List<AbstractAction> actions = JacksonUtil.getInstance(jasperReportsContext).loadAsList(jsonData, AbstractAction.class);
        if (actions != null) {
            if (actions.size() == 1) {
                result = actions.get(0);
                AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
                beanFactory.autowireBeanProperties(result, AutowireCapableBeanFactory.AUTOWIRE_NO, false);
                beanFactory.initializeBean(result, result.getClass().getName());
            } else if (actions.size() > 1) {
                result = new MultiAction(actions);
            }

            ((AbstractAction) result).init(jasperReportsContext, reportContext);
        }
        return result;
    }

    @Override
    public ReportOutputResource getReportOutputFromRawParameters(String reportUnitURI, Map<String, String[]> rawParameters,
            ReportExecutionOptions executionOptions, ExportExecutionOptions exportOptions) throws ErrorDescriptorException {
        final ReportExecution execution = getReportExecutionFromRawParameters(reportUnitURI, rawParameters, executionOptions, exportOptions, null);
        return getOutputResource(execution.getRequestId(), exportOptions);
    }

    protected Map<String, Object> executeInputControlsCascadeWithRawInput(String reportUnitUri, Map<String, String[]> rawInputParameters) throws CascadeResourceNotFoundException, InputControlsValidationException {
        final List<ReportInputControl> inputControlsForReport = inputControlsLogicService.getInputControlsWithValues(reportUnitUri, null, rawInputParameters);
        final Map<String, String[]> inputControlFormattedValues = ReportParametersUtils.getValueMapFromInputControls(inputControlsForReport);

        // Fix for https://jira.tibco.com/browse/JS-30276
        // The behaviour, described in the bug caused by change of actual values, sent in rawInputParameters to some
        // other, resolved by input control logic in inputControlFormattedValues. So, it a user sends invalid values,
        // they just dropped and replaced with values, that will definitely allow report to run.
        // This behaviour might be useful for cascading IC, in particular, when a user sends values for controls in cascade, and sent child's value
        // does not present in the subset of possible child values filtered by parent's value.
        // But this works in this way for all controls, and according to the requirement in the bug, this case should throw an error.
        // This particular check determines case, when sent value for a non-cascading control was silently alternated and throw an error.
        // For cascading controls the behaviour is not changed.
        verifyCorrectParameterValuesForNonCascadingControls(inputControlsForReport, rawInputParameters, inputControlFormattedValues);

        return inputControlsLogicService.getTypedParameters(reportUnitUri, inputControlFormattedValues);
    }

    protected void verifyCorrectParameterValuesForNonCascadingControls(List<ReportInputControl> inputControlsForReport, Map<String, String[]> rawInputParameters, Map<String, String[]> inputControlFormattedValues) throws InputControlsValidationException {
        for (String key : rawInputParameters.keySet()){
            if (inputControlFormattedValues.get(key) != null){
                String[] oldValues, newValues;

                newValues = Arrays.stream(inputControlFormattedValues.get(key)).sorted().distinct()
                        .toArray(String[]::new);
                oldValues = Arrays.stream(rawInputParameters.get(key)).sorted().distinct()
                        .toArray(String[]::new);

                ClientDataType type = null;
                String selectionType = null;
                for (ReportInputControl it : inputControlsForReport){
                    if (it.getId().equals(key)) {
                        type = it.getDataType();
                        selectionType = it.getType();
                    }
                }

                if (newValues.length == 0 && oldValues.length == 1 && NOTHING_SUBSTITUTION_VALUE.equals(oldValues[0])) {
                    // correct case, do nothing
                } else if (newValues.length == 1 && oldValues.length == 1 && (NOTHING_SUBSTITUTION_VALUE.equals(newValues[0]) && NULL_SUBSTITUTION_VALUE.equals(oldValues[0]))) {
                    // correct case, do nothing
                } else if (newValues.length > 0 && InputControlUITypeMapper.getUiType(TYPE_MULTI_SELECT_LIST_OF_VALUES).equals(selectionType) && oldValues.length == 1 && (NOTHING_SUBSTITUTION_VALUE.equals(oldValues[0]))) {
                    // correct case, do nothing
                    // see bug 64856.  Importing adhoc report from 7.5.1
                    // and in this case oldValues is ~NOTHING~, and new value is ALL (everything)
                } else if (newValues.length == oldValues.length) {
                    for (int i = 0; i < oldValues.length; i++) {
                        if (!isSameValue(newValues[i], oldValues[i], type)) {
                            verifyCascade(inputControlsForReport, rawInputParameters.keySet(), key, oldValues[i]);
                        }
                    }
                } else {
                    verifyCascade(inputControlsForReport, rawInputParameters.keySet(), key, null);
                }
            } else {
                List<ReportInputControl> inputControls = inputControlsToVerify(inputControlsForReport, rawInputParameters);
                verifyCascade(inputControls, rawInputParameters.keySet(), key, null);
            }
        }
    }

    private List<ReportInputControl> inputControlsToVerify(List<ReportInputControl> inputControlsForReport, Map<String, String[]> rawInputParameters) {
        Predicate<ReportInputControl> containsKey = ic -> rawInputParameters.containsKey(ic.getId());
        Predicate<ReportInputControl> isArrayWithNothingValue = ic -> rawInputParameters.get(ic.getId()).length == 1
                        && NOTHING_SUBSTITUTION_VALUE.equals(rawInputParameters.get(ic.getId())[0]);

        return inputControlsForReport.stream()
                // Ignore input controls that have "~NOTHING~" as the only selected value
                .filter(containsKey.and(isArrayWithNothingValue.negate()))
                .collect(Collectors.toList());
    }

    private boolean isSameValue(String newValue, String oldValue, ClientDataType type){
        if (type != null){
            if (type.getType().equals(ClientDataType.TypeOfDataType.date) ||
                    type.getType().equals(ClientDataType.TypeOfDataType.datetime) ||
                    type.getType().equals(ClientDataType.TypeOfDataType.time)) {
                return true;
            }
        }

        return newValue != null && newValue.equals(oldValue) || newValue == null && oldValue== null;
    }

    private void verifyCascade(List<ReportInputControl> inputControls, Set<String> specifiedValues,  String offendedControl, String offendedValue) throws InputControlsValidationException{
        if (!cascadingControlChangeCheck(inputControls, specifiedValues, offendedControl)) {
            ValidationErrors errors = new ValidationErrorsImpl();
            errors.add(new ValidationErrorImpl(null, null, "Non acceptable value" + (offendedValue == null ? "." : ": " + offendedValue), offendedControl));
            throw new InputControlsValidationException(errors);
        }
    }

    private boolean cascadingControlChangeCheck(List<ReportInputControl> inputControls, Set<String> specifiedValues, String controlId){
        ReportInputControl control = null;
        boolean result = false;

        for (ReportInputControl it : inputControls){
            if (it.getId().equals(controlId)){
                control = it;
            }
        }

        if (control == null) {
            // do not do anything, keep existing behaviour
            result = true;
        } else {
            if (control.getMasterDependencies() != null) {
                for (String masterId : control.getMasterDependencies()) {
                    if (specifiedValues.contains(masterId)) {
                        result = true;
                    } else {
                        result |= cascadingControlChangeCheck(inputControls, specifiedValues, masterId);
                    }
                }
            }
        }

        return result;
    }

    /**
     * @param reportExecution - the report execution instance. Holds all execution context related things.
     * @param jasperPrint     - filled with data jasper print object
     * @param outputFormat - output format in raw format
     * @param exportExecution - export execution model
     * @param pages           - what pages should be exported
     * @throws ErrorDescriptorException
     */
    protected void generateReportOutput(ReportExecution reportExecution, JasperPrint jasperPrint,
            String outputFormat, ExportExecution exportExecution, ReportOutputPages pages) throws ErrorDescriptorException {
        try {
            // Export...
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final ReportExecutionOptions reportExecutionOptions = reportExecution.getOptions();
            if (outputFormat.equals(Argument.RUN_OUTPUT_FORMAT_JRPRINT)) {
                if (log.isDebugEnabled())
                    log.debug("Returning JasperPrint");
                if (reportExecutionOptions.getTransformerKey() != null) {
                    if (log.isDebugEnabled())
                        log.debug("Transforming JasperPrint generic element for key " + reportExecutionOptions.getTransformerKey());
                    GenericElementReportTransformer.transformGenericElements(reportExecutor.getJasperReportsContext(reportExecutionOptions.isInteractive()), jasperPrint, reportExecutionOptions.getTransformerKey());
                }
                JRSaver.saveObject(jasperPrint, bos);
                exportExecution.setOutputResource(new ReportOutputResource().setContentType("application/octet-stream").setData(bos.toByteArray()));
            } else {
                if (outputFormat.equals(Argument.RUN_OUTPUT_FORMAT_XLS)) {
                    outputFormat = Argument.RUN_OUTPUT_FORMAT_XLSX;
                } else if (outputFormat.equals(Argument.RUN_OUTPUT_FORMAT_XLS_NOPAG)) {
                    outputFormat = Argument.RUN_OUTPUT_FORMAT_XLSX_NOPAG;
                }
                HashMap<String, Object> exportParameters = new HashMap<String, Object>(reportExecution.getRawParameters());
                if (pages != null) exportParameters.put(Argument.RUN_OUTPUT_PAGES, pages);
                final String reportURI = reportExecution.getReportURI();
                auditHelper.createAuditEvent(EXPORT.toString());
                auditHelper.addPropertyToAuditEvent(EXPORT.toString(), "uris", reportURI);
                try {
                    reportExecutor.exportReport(reportURI, Collections.singletonList(new SimpleExporterInputItem(jasperPrint)), outputFormat, bos, exportParameters);
                } catch (ErrorDescriptorException e) {
                    auditHelper.addExceptionToAllAuditEvents(e);
                    throw e;
                } catch (Exception e) {
                    log.debug("Error exporting report", e);
                    auditHelper.addExceptionToAllAuditEvents(e);
                    throw new ErrorDescriptorException(
                            new ErrorDescriptor()
                                    .setErrorCode("webservices.error.errorExportingReportUnit").setMessage(e.getMessage()), e
                    );
                } finally {
                    try {
                        bos.close();
                    } catch (IOException ex) {
                        log.error("caught exception: " + ex.getMessage(), ex);
                    }
                }
                final Matcher matcher = FILE_NAME_PATTERN.matcher(reportURI);
                final String fileExtension = outputFormat.equals(Argument.RUN_OUTPUT_FORMAT_DATA_CSV) || outputFormat.equals(Argument.RUN_OUTPUT_FORMAT_DATA_XLSX)
                        ? outputFormat.toLowerCase().replace("_",".")
                        : outputFormat.toLowerCase();
                exportExecution.setOutputResource(new ReportOutputResource()
                        .setContentType(reportExecutor.getContentType(outputFormat))
                        .setData(bos.toByteArray())
                        .setFileName((matcher.find() ? matcher.group(1) : "report") + "." + fileExtension)
                        .setPages(pages != null ? pages.toString() : null));
            }
        } catch (ErrorDescriptorException e) {
            throw e;
        } catch (ServiceException e) {
            log.error("caught exception: " + e.getMessage(), e);
        } catch (Throwable e) {
            log.error("caught Throwable exception: " + e.getMessage(), e);
        }
    }

    /**
     * @param searchCriteria - search criteria
     * @return set of currently running report's information
     */
    public Set<ReportExecutionStatusInformation> getCurrentlyRunningReports(SchedulerReportExecutionStatusSearchCriteria searchCriteria) {
        Set<ReportExecutionStatusInformation> result = null;
        List<ReportExecutionStatusInformation> reportExecutionStatusList =
                engine.getSchedulerReportExecutionStatusList(searchCriteria);
        if (reportExecutionStatusList != null && !reportExecutionStatusList.isEmpty()) {
            result = new HashSet<ReportExecutionStatusInformation>();
            result.addAll(reportExecutionStatusList);
        }
        return result;
    }

    public Boolean cancelReportExecution(String requestId) throws ErrorDescriptorException {
        return cancelReportExecution(requestId, engine);
    }

    protected Boolean cancelReportExecution(String requestId, EngineService effectiveEngine) throws ErrorDescriptorException {
        final boolean cancelled = effectiveEngine.cancelExecution(requestId);
        if (cancelled) {
            // Prior method call "effectiveEngine.cancelExecution()" uses Spring Security to check
            // if the user has access to this report execution. So this call is safe.
            final ReportExecution reportExecution = getReportExecutionFromCacheNoUserCheck(requestId);
            if (reportExecution != null) reportExecution.setStatus(ExecutionStatus.cancelled);
        }
        return cancelled;
    }

    public Boolean deleteReportExecution(String requestId) throws ErrorDescriptorException {
        try {
            ReportExecution execution = getReportExecutionFromCache(requestId);
            if (execution != null) {
                cancelReportExecution(requestId);
                if (execution.getStatus() == ExecutionStatus.ready) {
                    virtualizerFactory.disposeReport(execution.getFinalReportUnitResult());
                } else if(execution.getStatus() == ExecutionStatus.cancelled) {
                    if (execution.getReportUnitResult() != null) {
                        virtualizerFactory.disposeReport(execution.getReportUnitResult());
                    }
                }
                return getExecutionsCache().remove(requestId);
            }
            return false;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e1) {
            throw new ErrorDescriptorException(
                    new ErrorDescriptor().setErrorCode("Unexpected error while removing ReportExecution")
                            .setParameters(e1.getMessage()), e1);
        }
    }

    @Override
    public void destroy() {
        List<?> keys = getExecutionsCache().getKeys();
        keys.stream()
                .map(Object::toString)
                .forEach(requestId -> {
                    ReportExecution execution = getReportExecutionFromCache(requestId);
                    try {
                        cancelReportExecution(requestId, unsecuredEngine);
                        if (execution != null && execution.getStatus() == ExecutionStatus.ready) {
                            virtualizerFactory.disposeReport(execution.getFinalReportUnitResult());
                        }
                    } catch (RuntimeException ex) {
                        log.warn("Report execution cleanup failed: ", ex);
                    }
                });
        getExecutionsCache().removeAll(keys);
        getExecutionsCache().evictExpiredElements();
    }

    private boolean isSameUser(String userName) {
        String currentUserName = getCurrentUserQualifiedName();
        if (currentUserName == null) return false;

        return new EqualsBuilder()
                .append(userName, currentUserName)
                .isEquals();
    }

    private boolean missesExecution(Element element) {
        return element == null || element.getObjectValue() == null;
    }


}
