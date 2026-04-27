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

import com.jaspersoft.jasperserver.api.common.error.handling.SecureExceptionHandler;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.executions.ExecutionStatus;
import com.jaspersoft.jasperserver.remote.exception.ExportExecutionRejectedException;
import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@XmlRootElement
public class ExportExecution {
    private final static Log log = LogFactory.getLog(ExportExecution.class);
    private String id = UUID.randomUUID().toString();
    private ExportExecutionOptions options;
    private ExecutionStatus status;
    private ErrorDescriptor errorDescriptor;
    private ReportOutputResource outputResource;
    private Map<String, ReportOutputResource> attachments = new ConcurrentHashMap<String, ReportOutputResource>();
    private Lock lock;
    private Condition condition;
    private SecureExceptionHandler secureExceptionHandler;

    public ExportExecution() {
        lock = new ReentrantLock();
        condition = lock.newCondition();
    }

    @XmlElement
    public String getId() {
        lock.lock();
        try {
            return id;
        } finally {
            lock.unlock();
        }
    }

    public void setId(String id) {
        lock.lock();
        try {
            this.id = id;
        } finally {
            lock.unlock();
        }
    }

    @XmlTransient
    public ExportExecutionOptions getOptions() {
        lock.lock();
        try {
            return options;
        } finally {
            lock.unlock();
        }
    }

    public void setOptions(ExportExecutionOptions options) {
        lock.lock();
        try {
            this.options = options;
        } finally {
            lock.unlock();
        }
    }

    public ExecutionStatus getStatus() {
        lock.lock();
        try {
            return status;
        } finally {
            lock.unlock();
        }
    }

    public void setStatus(ExecutionStatus status) {
        lock.lock();
        try {
            this.status = status;
            if (log.isDebugEnabled()) {
                log.debug("Status of export execution '" + getId() + "' is changed to " + status);
            }
            if (status != ExecutionStatus.queued && status != ExecutionStatus.execution) {
                condition.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

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
            this.status = ExecutionStatus.failed;
            this.errorDescriptor = errorDescriptor;
            if (log.isDebugEnabled()) {
                log.debug("Status of export execution '" + getId() + "' is changed to " + status + ". Error descriptor: " + errorDescriptor);
            }
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public ReportOutputResource getOutputResource() {
        lock.lock();
        try {
            return outputResource;
        } finally {
            lock.unlock();
        }
    }

    @XmlTransient
    public ReportOutputResource getFinalOutputResource() throws ErrorDescriptorException {
        lock.lock();
        ReportOutputResource result;
        try {
            while (status == ExecutionStatus.queued || status == ExecutionStatus.execution) {
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    throw new ErrorDescriptorException(secureExceptionHandler.handleException(e));
                }
            }
            final ExecutionStatus currentStatus = status;
            if (currentStatus == null) throw new IllegalStateException("Status shouldn't be null");
            ErrorDescriptor descriptor = null;
            result = outputResource;
            switch (currentStatus) {
                case failed: {
                    descriptor = errorDescriptor != null ? errorDescriptor :
                            new ErrorDescriptor().setErrorCode("export.failed").setMessage("Export failed");
                }
                break;
                case cancelled: {
                    descriptor = new ErrorDescriptor().setErrorCode("export.cancelled")
                            .setMessage("Export cancelled");
                }
                break;
            }
            if (descriptor != null) throw new ExportExecutionRejectedException(descriptor);
        } finally {
            lock.unlock();
        }
        return result;
    }

    public void setOutputResource(ReportOutputResource outputResource) {
        lock.lock();
        try {
            this.outputResource = outputResource;
        } finally {
            lock.unlock();
        }
    }

    @XmlTransient
    public Map<String, ReportOutputResource> getAttachments() {
        lock.lock();
        try {
            return attachments;
        } finally {
            lock.unlock();
        }
    }

    public void setAttachments(Map<String, ReportOutputResource> attachments) {
        lock.lock();
        try {
            this.attachments = attachments;
        } finally {
            lock.unlock();
        }
    }

    @XmlElementWrapper(name = "attachments")
    @XmlElement(name = "attachment")
    public Set<ReportOutputResource> getAttachmentsSet() {
        lock.lock();
        try {
            return attachments != null && !attachments.isEmpty() ? new HashSet<>(attachments.values()) : null;
        } finally {
            lock.unlock();
        }
    }

    public void setSecureExceptionHandler(SecureExceptionHandler secureExceptionHandler) {
        this.secureExceptionHandler = secureExceptionHandler;
    }
}
