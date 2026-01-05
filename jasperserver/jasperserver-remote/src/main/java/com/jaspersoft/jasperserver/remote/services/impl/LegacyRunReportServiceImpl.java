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

import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.ReportUnitResult;
import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.Argument;
import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.OperationResult;
import com.jaspersoft.jasperserver.dto.reports.inputcontrols.InputControlState;
import com.jaspersoft.jasperserver.remote.ServiceException;
import com.jaspersoft.jasperserver.remote.ServicesUtils;
import com.jaspersoft.jasperserver.remote.exporters.HtmlExporter;
import com.jaspersoft.jasperserver.remote.services.LegacyRunReportService;
import com.jaspersoft.jasperserver.remote.services.ReportExecutionOptions;
import com.jaspersoft.jasperserver.remote.services.ReportExecutor;
import com.jaspersoft.jasperserver.remote.utils.AuditHelper;
import com.jaspersoft.jasperserver.inputcontrols.util.ReportParametersUtils;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlsLogicService;
import com.jaspersoft.jasperserver.ws.xml.ByteArrayDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.export.GenericElementReportTransformer;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.engine.util.JRTypeSniffer;
import net.sf.jasperreports.export.SimpleExporterInputItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.activation.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import static com.jaspersoft.jasperserver.api.logging.audit.domain.AuditEventType.RUN_REPORT;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id: LegacyRunReportServiceImpl.java 26611 2012-12-10 14:17:08Z ykovalchyk $
 */
@Service("legacyRunReportService")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LegacyRunReportServiceImpl implements LegacyRunReportService, Serializable {
    private final static Log log = LogFactory.getLog(LegacyRunReportServiceImpl.class);
    private static final String KEY_JASPER_PRINT_RESOURCE = "jasperPrint";
    @javax.annotation.Resource
    private AuditHelper auditHelper;
    @javax.annotation.Resource
    private ReportExecutor reportExecutor;
    @Autowired
    private MessageSource messageSource;
    @javax.annotation.Resource
    private ServicesUtils servicesUtils;
    @javax.annotation.Resource
    private InputControlsLogicService inputControlsLogicService;

    private final Map<String, Object> attributes = new HashMap<String, Object>();
    private final Map<String, Map<String, DataSource>> outputAttachments = new HashMap<String, Map<String, DataSource>>();
    private final Map<String, DataSource> inputAttachments = new HashMap<String, DataSource>();

    public Map<String, DataSource> getInputAttachments() {
        return inputAttachments;
    }

    public synchronized Map<String, DataSource> getReportAttachments(String reportName) {
        if (outputAttachments.get(reportName) == null)
            outputAttachments.put(reportName, new HashMap<String, DataSource>());
        return outputAttachments.get(reportName);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Return a response.
     * Generated files (one or more) are put in the output attachments map of this context.
     * Used by v1 REST service
     *
     * @param reportUnitURI - target report to run URI
     * @param parameters    - report parameters
     * @param arguments     - report arguments
     * @return the run report operation result
     * @throws com.jaspersoft.jasperserver.remote.ServiceException
     */
    public OperationResult runReport(String reportUnitURI, Map<String, Object> parameters, Map<String, String> arguments, Map<String, DataSource> outputReportResources) throws ServiceException {

        long currentTime = System.currentTimeMillis();
        auditHelper.createAuditEvent(RUN_REPORT.toString());

        OperationResult or = servicesUtils.createOperationResult(OperationResult.SUCCESS, null);

        try {
            ReportExecutionOptions executionOptions = new ReportExecutionOptions();
            executionOptions.setFreshData(Boolean.parseBoolean(arguments.get(Argument.FRESH_DATA)));
            executionOptions.setSaveDataSnapshot(Boolean.parseBoolean(arguments.get(Argument.SAVE_DATA_SNAPSHOT)));
            executionOptions.setInteractive(Boolean.parseBoolean(arguments.get(Argument.PARAM_INTERACTIVE)));
            executionOptions.setIgnorePagination(Boolean.parseBoolean(arguments.get(Argument.IGNORE_PAGINATION)));

            // execute input controls cascade
            final Map<String, String[]> rawParameters = parameters == null || parameters.isEmpty() ?
                    new HashMap<String, String[]>() : inputControlsLogicService.formatTypedParameters(reportUnitURI, parameters);
            final List<InputControlState> valuesForInputControls = inputControlsLogicService.getValuesForInputControls(
                    reportUnitURI, null, rawParameters, executionOptions.isFreshData());
            final Map<String, String[]> inputControlFormattedValues = ReportParametersUtils
                    .getValueMapFromInputControlStates(valuesForInputControls);
            Map<String, Object> runtimeParameters = inputControlsLogicService.getTypedParameters(reportUnitURI, inputControlFormattedValues);
            // forward parameters without input control defined
            for (String currentKey : parameters.keySet()) {
                Object currentValue = parameters.get(currentKey);
                if (shouldRestoreValue(currentValue, runtimeParameters.get(currentKey))) {
                    // passed within request value is changed by input controls cascade
                    // and should be restored for backward compatibility reason. See bug #31803 for details.
                    runtimeParameters.put(currentKey, parameters.get(currentKey));
                }
            }

            // run the report
            ReportUnitResult result = reportExecutor.runReport(reportUnitURI, runtimeParameters, executionOptions);


            if (result == null) {

                throw new ServiceException(ServiceException.FILL_ERROR,
                        messageSource.getMessage("webservices.error.errorExecutingReportUnit",
                                new Object[]{reportUnitURI}, LocaleContextHolder.getLocale())
                );

            } else {

                JasperPrint jasperPrint = result.getJasperPrint();

                or = exportReport(reportUnitURI, jasperPrint, arguments, outputReportResources);

                // The jasperprint may have been transformed during export...
                this.getAttributes().put(KEY_JASPER_PRINT_RESOURCE, jasperPrint);
            }

            if (or.getReturnCode() != 0) {
                auditHelper.addExceptionToAllAuditEvents(new Exception(or.getMessage()));
            }

        } catch (ServiceException e) {
            log.error("caught exception: " + e.getMessage(), e);
            or.setReturnCode(e.getErrorCode());
            or.setMessage(e.getMessage());
            auditHelper.addExceptionToAllAuditEvents(e);

        } catch (Throwable e) {
            log.error("caught Throwable exception: " + e.getMessage(), e);
            e.printStackTrace(System.out);
            System.out.flush();
            or.setReturnCode(1);
            or.setMessage(e.getMessage());
            auditHelper.addExceptionToAllAuditEvents(e);
        }

        auditHelper.addPropertyToAuditEvent(RUN_REPORT.toString(), "reportExecutionStartTime", new Date(currentTime));
        auditHelper.addPropertyToAuditEvent(RUN_REPORT.toString(), "reportExecutionTime", System.currentTimeMillis() - currentTime);
        return or;
    }

    protected boolean shouldRestoreValue(Object originalValue, Object processedValue){
        boolean result;
        if(originalValue == null){
            // no original value, don't restore any.
            result = false;
        } else if(processedValue == null){
            // value have been removed. Restore it.
            result = true;
        } else if(originalValue.getClass() != processedValue.getClass()){
            // parameter value is typed by the input controls logic, don't restore it to raw state.
            result = false;
        } else if(!originalValue.equals(processedValue)){
            // values are of the same type, but not equals. Restore original value.
            result = true;
        } else {
            // don't restore anything by default
            result = false;
        }
        return result;
    }


    /**
     * Export the report in a specific format using the specified arguments
     * Generated files (one or more) are put in the output attachments map of this context
     *
     * @param jasperPrint JasperPrint
     * @param arguments   - indicates the final file format, starting/ending pages, etc...
     * @return OperationResult
     * @throws ServiceException
     */
    public OperationResult exportReport(String reportUnitURI, JasperPrint jasperPrint,
                                        Map<String, String> arguments,
                                        Map<String, DataSource> outputResourcesContainer)
            throws ServiceException {

        long currentTime = System.currentTimeMillis();
        auditHelper.createAuditEvent(RUN_REPORT.toString());

        OperationResult or = servicesUtils.createOperationResult(OperationResult.SUCCESS, null);

        try {
            String format = arguments.get(Argument.RUN_OUTPUT_FORMAT);
            if (format == null) format = Argument.RUN_OUTPUT_FORMAT_PDF;
            format = format.toUpperCase();

            String transformerKey = arguments.get(Argument.RUN_TRANSFORMER_KEY);

            // Export...
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ByteArrayDataSource bads;

            if (format.equals(Argument.RUN_OUTPUT_FORMAT_JRPRINT)) {
                if (log.isDebugEnabled()) {
                    log.debug("Returning JasperPrint");
                }
                if (transformerKey != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Transforming JasperPrint generic element for key " + transformerKey);
                    }
                    final JasperReportsContext jasperReportsContext = reportExecutor.getJasperReportsContext(Boolean.parseBoolean(arguments.get(Argument.PARAM_INTERACTIVE)));
                    GenericElementReportTransformer.transformGenericElements(jasperReportsContext, jasperPrint, transformerKey);
                }

                JRSaver.saveObject(jasperPrint, bos);
                bads = new ByteArrayDataSource(bos.toByteArray());
                outputResourcesContainer.put(KEY_JASPER_PRINT_RESOURCE, bads);
            } else {
                Map<String, Object> exportParameters = new HashMap<>();

                String value = arguments.get(Argument.RUN_OUTPUT_PAGE);
                if (value != null) {
                    exportParameters.put(Argument.RUN_OUTPUT_PAGE, value);
                }

                value = arguments.get(Argument.RUN_OUTPUT_IMAGES_URI);
                if (value != null) exportParameters.put(Argument.RUN_OUTPUT_IMAGES_URI, value);
                exportParameters.put(HtmlExporter.INTERACTIVE_PARAM_NAME, Boolean.valueOf(arguments.get(Argument.PARAM_INTERACTIVE)));

                try {
                    // Export to the output stream
                    reportExecutor.exportReport(
                            reportUnitURI,
                            Collections.singletonList(new SimpleExporterInputItem(jasperPrint)),
                            format,
                            bos,
                            exportParameters
                    );
                } catch (Exception e) {
                    log.error("Error exporting report", e);
                    throw new ServiceException(ServiceException.EXPORT_ERROR,
                            messageSource.getMessage("webservices.error.errorExportingReportUnit",
                                    new Object[]{e.getMessage()}, LocaleContextHolder.getLocale()));
                } finally {
                    if (bos != null) {
                        try {
                            bos.close();
                        } catch (IOException ex) {
                            log.error("caught exception: " + ex.getMessage(), ex);
                        }
                    }
                }

                bads = new ByteArrayDataSource(bos.toByteArray(), reportExecutor.getContentType(format));
                outputResourcesContainer.put("report", bads);
                addAdditionalAttachmentsForReport(format, exportParameters, outputResourcesContainer);
            }

            if (or.getReturnCode() != 0) {
                auditHelper.addExceptionToAllAuditEvents(new Exception(or.getMessage()));
            }

        } catch (ServiceException e) {
            log.error("caught exception: " + e.getMessage(), e);
            or.setReturnCode(e.getErrorCode());
            or.setMessage(e.getMessage());
            auditHelper.addExceptionToAllAuditEvents(e);

        } catch (Throwable e) {
            log.error("caught Throwable exception: " + e.getMessage(), e);
            e.printStackTrace(System.out);
            System.out.flush();
            or.setReturnCode(1);
            or.setMessage(e.getMessage());
            auditHelper.addExceptionToAllAuditEvents(e);
        }

        auditHelper.addPropertyToAuditEvent(RUN_REPORT.toString(), "reportExecutionStartTime", new Date(currentTime));
        auditHelper.addPropertyToAuditEvent(RUN_REPORT.toString(), "reportExecutionTime", System.currentTimeMillis() - currentTime);
        return or;
    }

    /**
     * Create additional Web Services attachments for the content. At this stage, HTML reports
     * have their images as attachments
     *
     * @param format                   - the format
     * @param exportParameters         - the export parameters
     * @param outputResourcesContainer - the container to put additional attachments
     * @throws ServiceException
     */
    private void addAdditionalAttachmentsForReport(String format, Map<String, Object> exportParameters,
                                                   Map<String, DataSource> outputResourcesContainer) throws ServiceException {
        if (log.isDebugEnabled()) {
            log.debug("Format requested: " + format + "  " + Argument.RUN_OUTPUT_FORMAT_HTML);
        }
        if (!format.equals(Argument.RUN_OUTPUT_FORMAT_HTML)) {
            return;
        }

        try {
            // imagesMap wird jetzt unter "IMAGES_MAP" als Standard-String-Key gespeichert
            @SuppressWarnings("unchecked")
            Map<String, byte[]> imagesMap = (Map<String, byte[]>) exportParameters.get("IMAGES_MAP");

            if (imagesMap == null || imagesMap.isEmpty()) {
                return;
            }

            if (log.isDebugEnabled()) {
                log.debug("imagesMap : " + imagesMap.keySet());
            }

            for (Map.Entry<String, byte[]> entry : imagesMap.entrySet()) {
                String name = entry.getKey();
                byte[] data = entry.getValue();

                String mimeType = JRTypeSniffer.getImageTypeValue(data).getMimeType();

                if (log.isDebugEnabled()) {
                    log.debug("Adding image for HTML: " + name + ", type: " + mimeType);
                }

                ByteArrayDataSource bads = new ByteArrayDataSource(data, mimeType);
                outputResourcesContainer.put(name, bads);
            }
        } catch (Throwable e) {
            log.error(e);
            throw new ServiceException(ServiceException.EXPORT_ERROR,
                    messageSource.getMessage("webservices.error.errorAddingImage",
                            new Object[]{e.getMessage()}, LocaleContextHolder.getLocale())
            );
        }
    }
}
