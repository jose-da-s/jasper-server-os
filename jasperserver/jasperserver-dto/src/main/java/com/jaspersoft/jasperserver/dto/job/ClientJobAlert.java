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

package com.jaspersoft.jasperserver.dto.job;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;
import com.jaspersoft.jasperserver.dto.job.adapters.AddressesXmlAdapter;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p/>
 * <p/>
 *
 * @author tetiana.iefimenko
 * @version $Id$
 * @see
 */
@XmlRootElement(name = "reportJobAlert")
public class ClientJobAlert implements DeepCloneable<ClientJobAlert>{

    private Long id;
    private Integer version;
    private ClientJobAlertRecipient recipient;
    private ClientJobAlertState  jobState;
    private String messageText;
    private String messageTextWhenJobFails;
    private String subject;
    private Boolean includingStackTrace;
    private Boolean includingReportJobInfo;
    private List<String> toAddresses;

    public ClientJobAlert() {
    }

    public ClientJobAlert(ClientJobAlert other) {
        checkNotNull(other);

        this.id = other.id;
        this.includingReportJobInfo = other.includingReportJobInfo;
        this.includingStackTrace = other.includingStackTrace;
        this.jobState = other.jobState;
        this.messageText = other.messageText;
        this.messageTextWhenJobFails = other.messageTextWhenJobFails;
        this.recipient = other.recipient;
        this.subject = other.subject;
        this.toAddresses = copyOf(other.getToAddresses());
        this.version = other.version;

    }

    public List<String> getToAddresses() {
        return toAddresses;
    }

    @XmlJavaTypeAdapter(AddressesXmlAdapter.class)
    public ClientJobAlert setToAddresses(List<String> toAddresses) {
        this.toAddresses = toAddresses;
        return this;
    }

    public Long getId() {
        return id;
    }

    public ClientJobAlert setId(Long id) {
        this.id = id;
        return this;
    }

    public Integer getVersion() {
        return version;
    }

    public ClientJobAlert setVersion(Integer version) {
        this.version = version;
        return this;
    }

    public ClientJobAlertRecipient getRecipient() {
        return recipient;
    }

    public ClientJobAlert setRecipient(ClientJobAlertRecipient recipient) {
        this.recipient = recipient;
        return this;
    }

    public ClientJobAlertState getJobState() {
        return jobState;
    }

    public ClientJobAlert setJobState(ClientJobAlertState jobState) {
        this.jobState = jobState;
        return this;
    }

    public String getMessageText() {
        return messageText;
    }

    public ClientJobAlert setMessageText(String messageText) {
        this.messageText = messageText;
        return this;
    }

    public String getMessageTextWhenJobFails() {
        return messageTextWhenJobFails;
    }

    public ClientJobAlert setMessageTextWhenJobFails(String messageTextWhenJobFails) {
        this.messageTextWhenJobFails = messageTextWhenJobFails;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public ClientJobAlert setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public Boolean isIncludingStackTrace() {
        return includingStackTrace;
    }

    public ClientJobAlert setIncludingStackTrace(Boolean includingStackTrace) {
        this.includingStackTrace = includingStackTrace;
        return this;
    }

    public Boolean isIncludingReportJobInfo() {
        return includingReportJobInfo;
    }

    public ClientJobAlert setIncludingReportJobInfo(Boolean includingReportJobInfo) {
        this.includingReportJobInfo = includingReportJobInfo;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientJobAlert)) return false;

        ClientJobAlert jobAlert = (ClientJobAlert) o;

        if (id != null ? !id.equals(jobAlert.id) : jobAlert.id != null) return false;
        if (includingReportJobInfo != null ? !includingReportJobInfo.equals(jobAlert.includingReportJobInfo) : jobAlert.includingReportJobInfo != null)
            return false;
        if (includingStackTrace != null ? !includingStackTrace.equals(jobAlert.includingStackTrace) : jobAlert.includingStackTrace != null)
            return false;
        if (jobState != null ? !jobState.equals(jobAlert.jobState) : jobAlert.jobState != null) return false;
        if (messageText != null ? !messageText.equals(jobAlert.messageText) : jobAlert.messageText != null)
            return false;
        if (messageTextWhenJobFails != null ? !messageTextWhenJobFails.equals(jobAlert.messageTextWhenJobFails) : jobAlert.messageTextWhenJobFails != null)
            return false;
        if (recipient != null ? !recipient.equals(jobAlert.recipient) : jobAlert.recipient != null) return false;
        if (subject != null ? !subject.equals(jobAlert.subject) : jobAlert.subject != null) return false;
        if (toAddresses != null ? !toAddresses.equals(jobAlert.toAddresses) : jobAlert.toAddresses != null)
            return false;
        if (version != null ? !version.equals(jobAlert.version) : jobAlert.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (recipient != null ? recipient.hashCode() : 0);
        result = 31 * result + (jobState != null ? jobState.hashCode() : 0);
        result = 31 * result + (messageText != null ? messageText.hashCode() : 0);
        result = 31 * result + (messageTextWhenJobFails != null ? messageTextWhenJobFails.hashCode() : 0);
        result = 31 * result + (subject != null ? subject.hashCode() : 0);
        result = 31 * result + (includingStackTrace != null ? includingStackTrace.hashCode() : 0);
        result = 31 * result + (includingReportJobInfo != null ? includingReportJobInfo.hashCode() : 0);
        result = 31 * result + (toAddresses != null ? toAddresses.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientJobAlert{" +
                "id=" + id +
                ", version=" + version +
                ", recipient=" + recipient +
                ", jobState=" + jobState +
                ", messageText='" + messageText + '\'' +
                ", messageTextWhenJobFails='" + messageTextWhenJobFails + '\'' +
                ", subject='" + subject + '\'' +
                ", includingStackTrace=" + includingStackTrace +
                ", includingReportJobInfo=" + includingReportJobInfo +
                ", toAddresses=" + toAddresses +
                '}';
    }

    @Override
    public ClientJobAlert deepClone() {
        return new ClientJobAlert(this);
    }
}
