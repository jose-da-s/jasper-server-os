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
@XmlRootElement(name = "mailNotification")
public class ClientJobMailNotification implements DeepCloneable<ClientJobMailNotification>{


    private List<String> bccAddresses;
    private List<String> ccAddresses;
    private List<String> toAddresses;
    private Integer version;
    private Long id;
    private Boolean includingStackTraceWhenJobFails;
    private String messageText;
    private ClientMailNotificationSendType resultSendType;
    private Boolean skipEmptyReports;
    private Boolean skipNotificationWhenJobFails;
    private String subject;
    private String messageTextWhenJobFails;

    public ClientJobMailNotification() {
    }

    public ClientJobMailNotification(ClientJobMailNotification other) {
        checkNotNull(other);

        this.bccAddresses = copyOf(other.getBccAddresses());
        this.ccAddresses = copyOf(other.getCcAddresses());
        this.toAddresses = copyOf(other.getToAddresses());
        this.id = other.getId();
        this.includingStackTraceWhenJobFails = other.isIncludingStackTraceWhenJobFails();
        this.messageText = other.getMessageText();
        this.messageTextWhenJobFails = other.getMessageTextWhenJobFails();
        this.resultSendType = other.getResultSendType();
        this.skipEmptyReports = other.isSkipEmptyReports();
        this.skipNotificationWhenJobFails = other.isSkipNotificationWhenJobFails();
        this.subject = other.getSubject();
        this.version = other.getVersion();
    }

    @XmlJavaTypeAdapter(AddressesXmlAdapter.class)
    public List<String> getCcAddresses() {
        return ccAddresses;
    }

    public ClientJobMailNotification setCcAddresses(List<String> ccAddresses) {
        this.ccAddresses = ccAddresses;
        return this;
    }

    @XmlJavaTypeAdapter(AddressesXmlAdapter.class)
    public List<String> getToAddresses() {
        return toAddresses;
    }

    public ClientJobMailNotification setToAddresses(List<String> toAddresses) {
        this.toAddresses = toAddresses;
        return this;
    }

    @XmlJavaTypeAdapter(AddressesXmlAdapter.class)
    public List<String> getBccAddresses() {
        return bccAddresses;
    }

    public ClientJobMailNotification setBccAddresses(List<String> bccAddresses) {
        this.bccAddresses = bccAddresses;
        return this;
    }

    public Integer getVersion() {
        return version;
    }

    public ClientJobMailNotification setVersion(Integer version) {
        this.version = version;
        return this;
    }

    public Long getId() {
        return id;
    }

    public ClientJobMailNotification setId(Long id) {
        this.id = id;
        return this;
    }

    public Boolean isIncludingStackTraceWhenJobFails() {
        return includingStackTraceWhenJobFails;
    }

    public ClientJobMailNotification setIncludingStackTraceWhenJobFails(Boolean includingStackTraceWhenJobFails) {
        this.includingStackTraceWhenJobFails = includingStackTraceWhenJobFails;
        return this;
    }

    public String getMessageText() {
        return messageText;
    }

    public ClientJobMailNotification setMessageText(String messageText) {
        this.messageText = messageText;
        return this;
    }

    public Boolean isSkipEmptyReports() {
        return skipEmptyReports;
    }

    public ClientJobMailNotification setSkipEmptyReports(Boolean skipEmptyReports) {
        this.skipEmptyReports = skipEmptyReports;
        return this;
    }

    public Boolean isSkipNotificationWhenJobFails() {
        return skipNotificationWhenJobFails;
    }

    public ClientJobMailNotification setSkipNotificationWhenJobFails(Boolean skipNotificationWhenJobFails) {
        this.skipNotificationWhenJobFails = skipNotificationWhenJobFails;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public ClientJobMailNotification setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String getMessageTextWhenJobFails() {
        return messageTextWhenJobFails;
    }

    public ClientJobMailNotification setMessageTextWhenJobFails(String messageTextWhenJobFails) {
        this.messageTextWhenJobFails = messageTextWhenJobFails;
        return this;
    }

    public ClientMailNotificationSendType getResultSendType() {
        return resultSendType;
    }

    public ClientJobMailNotification setResultSendType(ClientMailNotificationSendType resultSendType) {
        this.resultSendType = resultSendType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientJobMailNotification)) return false;

        ClientJobMailNotification that = (ClientJobMailNotification) o;

        if (bccAddresses != null ? !bccAddresses.equals(that.bccAddresses) : that.bccAddresses != null) return false;
        if (ccAddresses != null ? !ccAddresses.equals(that.ccAddresses) : that.ccAddresses != null) return false;
        if (toAddresses != null ? !toAddresses.equals(that.toAddresses) : that.toAddresses != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (includingStackTraceWhenJobFails != null ? !includingStackTraceWhenJobFails.equals(that.includingStackTraceWhenJobFails) : that.includingStackTraceWhenJobFails != null)
            return false;
        if (messageText != null ? !messageText.equals(that.messageText) : that.messageText != null) return false;
        if (resultSendType != that.resultSendType) return false;
        if (skipEmptyReports != null ? !skipEmptyReports.equals(that.skipEmptyReports) : that.skipEmptyReports != null)
            return false;
        if (skipNotificationWhenJobFails != null ? !skipNotificationWhenJobFails.equals(that.skipNotificationWhenJobFails) : that.skipNotificationWhenJobFails != null)
            return false;
        if (subject != null ? !subject.equals(that.subject) : that.subject != null) return false;
        return messageTextWhenJobFails != null ? messageTextWhenJobFails.equals(that.messageTextWhenJobFails) : that.messageTextWhenJobFails == null;
    }

    @Override
    public int hashCode() {
        int result = bccAddresses != null ? bccAddresses.hashCode() : 0;
        result = 31 * result + (ccAddresses != null ? ccAddresses.hashCode() : 0);
        result = 31 * result + (toAddresses != null ? toAddresses.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (includingStackTraceWhenJobFails != null ? includingStackTraceWhenJobFails.hashCode() : 0);
        result = 31 * result + (messageText != null ? messageText.hashCode() : 0);
        result = 31 * result + (resultSendType != null ? resultSendType.hashCode() : 0);
        result = 31 * result + (skipEmptyReports != null ? skipEmptyReports.hashCode() : 0);
        result = 31 * result + (skipNotificationWhenJobFails != null ? skipNotificationWhenJobFails.hashCode() : 0);
        result = 31 * result + (subject != null ? subject.hashCode() : 0);
        result = 31 * result + (messageTextWhenJobFails != null ? messageTextWhenJobFails.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientJobMailNotification{" +
                "bccAddresses=" + bccAddresses +
                ", ccAddresses=" + ccAddresses +
                ", toAddresses=" + toAddresses +
                ", version=" + version +
                ", id=" + id +
                ", includingStackTraceWhenJobFails=" + includingStackTraceWhenJobFails +
                ", messageText='" + messageText + '\'' +
                ", resultSendType=" + resultSendType +
                ", skipEmptyReports=" + skipEmptyReports +
                ", skipNotificationWhenJobFails=" + skipNotificationWhenJobFails +
                ", subject='" + subject + '\'' +
                ", messageTextWhenJobFails='" + messageTextWhenJobFails + '\'' +
                '}';
    }

    @Override
    public ClientJobMailNotification deepClone() {
        return new ClientJobMailNotification(this);
    }
}
