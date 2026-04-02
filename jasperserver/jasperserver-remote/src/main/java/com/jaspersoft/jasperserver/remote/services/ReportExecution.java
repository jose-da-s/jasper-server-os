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
package com.jaspersoft.jasperserver.remote.services;

import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.ReportUnitResult;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.executions.ExecutionStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id: ReportExecution.java 26599 2012-12-10 13:04:23Z ykovalchyk $
 */
@XmlRootElement
public class ReportExecution implements Serializable {
    private static final long serialVersionUID = 1L;
    private final static Log log = LogFactory.getLog(ReportExecution.class);
    private ExecutionStatus status;
    private Integer totalPages;
    private Integer currentPage;
    private ErrorDescriptor errorDescriptor;
    private final ExportsContainer exports = new ExportsContainer();
    private ReportUnitResult reportUnitResult;
    private Map<String, String[]> rawParameters;
    private String requestId;
    private String reportURI;
    private ReportExecutionOptions options;
    private final Lock lock;
    private final Condition resultExist;
    private volatile Map<String, Object> convertedParameters;
    private volatile ReportExecution relatedExecution;

    public ReportExecution() {
        lock = new ReentrantLock();
        resultExist = lock.newCondition();
    }

    public ReportExecution getRelatedExecution() {
        return relatedExecution;
    }

    /**
     * Setter of relatedExecution. It is required to get export with alternate pagination mode.
     * @param relatedExecution - related report execution
     */
    public void setRelatedExecution(ReportExecution relatedExecution) {
        this.relatedExecution = relatedExecution;
    }

    @XmlTransient
    @Schema(hidden = true)
    public Map<String, Object> getConvertedParameters() {
        return convertedParameters;
    }

    /**
     * Setter for converted parameters. It's derivation of input control logic processed raw parameters.
     * They are cached here to be reused if report rerun is required.
     * @param convertedParameters - converted parameters to cache
     */
    public void setConvertedParameters(Map<String, Object> convertedParameters) {
        this.convertedParameters = convertedParameters;
    }

    @XmlTransient
    @Schema(hidden = true)
    public Map<String, String[]> getRawParameters() {
        lock.lock();
        try {
            return rawParameters;
        } finally {
            lock.unlock();
        }
    }

    public void setRawParameters(Map<String, String[]> rawParameters) {
        lock.lock();
        try {
            // reset cached converted parameters if new raw parameters are here.
            this.convertedParameters = null;
            this.rawParameters = rawParameters;
        } finally {
            lock.unlock();
        }
    }

    @XmlTransient
    @Schema(hidden = true)
    public ReportExecutionOptions getOptions() {
        lock.lock();
        try {
            return options;
        } finally {
            lock.unlock();
        }
    }

    public void setOptions(ReportExecutionOptions options) {
        lock.lock();
        try {
            this.options = options;
        } finally {
            lock.unlock();
        }
    }

    @XmlTransient
    @Schema(hidden = true)
    public ReportUnitResult getFinalReportUnitResult() {
        lock.lock();
        try {
            while (reportUnitResult == null && status != ExecutionStatus.cancelled && status != ExecutionStatus.failed) {
                if(log.isDebugEnabled()){
                    log.debug("Waiting for report unit result of report execution '" + getRequestId() +"'");
                }
                resultExist.await();
            }
            ErrorDescriptor descriptor = null;
            switch (status){
                case failed:{
                    descriptor = errorDescriptor != null ? errorDescriptor :
                            new ErrorDescriptor().setErrorCode("report.execution.failed").setMessage("Report execution failed");
                }
                break;
                case cancelled:{
                    descriptor = new ErrorDescriptor().setErrorCode("report.execution.cancelled")
                            .setMessage("Report execution cancelled");
                }
                break;
                default:
            }
            if (descriptor != null) throw new ErrorDescriptorException(descriptor);
        } catch (InterruptedException e) {
            throw new JSException(e);
        } finally {
            lock.unlock();
        }
        if(log.isDebugEnabled()){
            log.debug("About to get report unit result of report execution '" + getRequestId() +"'");
        }
        return reportUnitResult;
    }

    @XmlTransient
    @Schema(hidden = true)
    public ReportUnitResult getReportUnitResult() {
        lock.lock();
        try {
            return reportUnitResult;
        } finally {
            lock.unlock();
        }
    }
    
    public void setReportUnitResult(ReportUnitResult reportUnitResult) {
        lock.lock();
        try {
            this.reportUnitResult = reportUnitResult;
            if(log.isDebugEnabled()){
                log.debug("Report unit result is provided for report execution '" + getRequestId() +"'");
            }
            resultExist.signalAll();
        } finally {
            lock.unlock();
        }
    }
    
    @Schema(
    		description = "The current page", 
    		type = "integer",
    		example = "3")
    public Integer getCurrentPage() {
        lock.lock();
        try {
            return currentPage;
        } finally {
            lock.unlock();
        }
    }
    
    public void setCurrentPage(Integer currentPage) {
        lock.lock();
        try {
            this.currentPage = currentPage;
        } finally {
            lock.unlock();
        }
    }
    
    @Schema(description = "The request UUID", format="uuid", example = "b487a05a-4989-8b53-b2b9-b54752f998c4")
    public String getRequestId() {
        lock.lock();
        try {
            return requestId;
        } finally {
            lock.unlock();
        }
    }
    
    public void setRequestId(String requestId) {
        lock.lock();
        try {
            this.requestId = requestId;
        } finally {
            lock.unlock();
        }
    }
    
    @XmlElement(name = "export")
    @XmlElementWrapper(name = "exports")
    @ArraySchema(schema = @Schema(description = "Set of `ExportExecution` objects",  implementation = ExportExecution.class))
    public Set<ExportExecution> getExportsSet() {
        return !exports.isEmpty() ? new HashSet<ExportExecution>(exports.values()) : null;
    }
    
    public void setExportsSet(Set<ExportExecution> exportsSet) {
        exports.clear();
        if (exportsSet != null) {
            exports.putAll(exportsSet);
        }
    }

    @XmlTransient
    @Schema(hidden = true)
    public ExportsContainer getExports() {
        return exports;
    }
    
    @Schema(hidden = true)
    public ErrorDescriptor getErrorDescriptor() {
        lock.lock();
        try {
            return errorDescriptor;
        } finally {
            lock.unlock();
        }
    }

    public void setErrorDescriptor(ErrorDescriptor errorDescriptor) {
        lock.lock();
        try {
            this.errorDescriptor = errorDescriptor;
            if (errorDescriptor != null) {
                this.status = ExecutionStatus.failed;
                if (log.isDebugEnabled()) {
                    log.debug("Status of report execution '" + getRequestId() + "' is changed to " + status + ". Error descriptor: " + errorDescriptor);
                }
                if (!exports.isEmpty()) {
                    for (ExportExecution exportExecution : exports.values()) {
                        exportExecution.setErrorDescriptor(errorDescriptor);
                    }
                }
            }
            // error happened. Let's signal to react.
            resultExist.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Schema(description = "The requested report URI", example = "public/Samples/Reports/AllAccounts")
    public String getReportURI() {
        lock.lock();
        try {
            return reportURI;
        } finally {
            lock.unlock();
        }
    }

    public void setReportURI(String reportURI) {
        lock.lock();
        try {
            this.reportURI = reportURI;
        } finally {
            lock.unlock();
        }
    }

    @Schema(description = "The report execution status", implementation = ExecutionStatus.class)
    public ExecutionStatus getStatus() {
        lock.lock();
        try {
            return status;
        } finally {
            lock.unlock();
        }
    }

    public void setStatus(ExecutionStatus status) {
        if (status != this.status) {
            log.debug("Setting report execution status to: " + status);
        }
        lock.lock();
        try {
            if (this.status != status) {
                this.status = status;
                if (log.isDebugEnabled()) {
                    log.debug("Status of report execution '" + getRequestId() + "' is changed to " + status);
                }
                if (status == ExecutionStatus.cancelled || status == ExecutionStatus.ready || status == ExecutionStatus.queued) {
                    if (status == ExecutionStatus.queued) {
                        setReportUnitResult(null);
                        setErrorDescriptor(null);
                    }
                    if (!exports.isEmpty()) {
                        for (ExportExecution exportExecution : exports.values()) {
                            if (status == ExecutionStatus.cancelled) {
                                exportExecution.setStatus(ExecutionStatus.cancelled);
                            }
                        }
                    }
                }
                // status is changed, let's signal to react
                resultExist.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    @Schema(
    		description = "The total number of pages", 
    		type = "integer",
    		example = "47")
    public Integer getTotalPages() {
        lock.lock();
        try {
            return totalPages;
        } finally {
            lock.unlock();
        }
    }

    public void setTotalPages(Integer totalPages) {
        lock.lock();
        try {
            this.totalPages = totalPages;
        } finally {
            lock.unlock();
        }
    }
    
     
    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
    }
     
    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
    }
     
}
