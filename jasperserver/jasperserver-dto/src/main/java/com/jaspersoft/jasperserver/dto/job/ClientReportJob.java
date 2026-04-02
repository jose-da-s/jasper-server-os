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
import com.jaspersoft.jasperserver.dto.common.ExportType;
import com.jaspersoft.jasperserver.dto.common.OutputFormat;
import com.jaspersoft.jasperserver.dto.job.adapters.OutputFormatXmlAdapter;
import com.jaspersoft.jasperserver.dto.job.adapters.TimestampToStringXmlAdapter;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.sql.Timestamp;
import java.util.Set;

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
@XmlRootElement(name = "job")
public class ClientReportJob implements DeepCloneable<ClientReportJob>{

    private Long id;
    private Integer version;
    private String username;
    private String label;
    private String description;
    private Timestamp creationDate;
    private ClientJobTrigger trigger;
    private ClientJobSource source;
    private String baseOutputFilename;
    private ExportType exportType;
    private Set<OutputFormat> outputFormats;
    private String outputLocale;
    private String outputTimeZone;
    private ClientJobRepositoryDestination repositoryDestination;
    private ClientJobMailNotification mailNotification;
    private ClientJobAlert alert;

    public ClientReportJob() {
    }

    public ClientReportJob(ClientReportJob other) {
        checkNotNull(other);

        this.id = other.getId();
        this.version = other.getVersion();
        this.username = other.getUsername();
        this.label = other.getLabel();
        this.description = other.getDescription();
        this.creationDate = copyOf(other.getCreationDate());
        this.trigger = copyOf(other.getTrigger());
        this.source = copyOf(other.getSource());
        this.exportType = other.getExportType();
        this.baseOutputFilename = other.getBaseOutputFilename();
        this.outputFormats = copyOf(other.getOutputFormats());
        this.outputLocale = other.getOutputLocale();
        this.outputTimeZone = other.getOutputTimeZone();
        this.repositoryDestination = copyOf(other.getRepositoryDestination());
        this.mailNotification = copyOf(other.getMailNotification());
        this.alert = copyOf(other.getAlert());
    }

    public Long getId() {
        return id;
    }

    public ClientReportJob setId(Long id) {
        this.id = id;
        return this;
    }

    public Integer getVersion() {
        return version;
    }

    public ClientReportJob setVersion(Integer version) {
        this.version = version;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public ClientReportJob setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public ClientReportJob setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ClientReportJob setDescription(String description) {
        this.description = description;
        return this;
    }

    @XmlJavaTypeAdapter(TimestampToStringXmlAdapter.class)
    public Timestamp getCreationDate() {
        return creationDate;
    }

    public ClientReportJob setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public String getBaseOutputFilename() {
        return baseOutputFilename;
    }

    public ClientReportJob setBaseOutputFilename(String baseOutputFilename) {
        this.baseOutputFilename = baseOutputFilename;
        return this;
    }

    public String getOutputLocale() {
        return outputLocale;
    }

    public ClientReportJob setOutputLocale(String outputLocale) {
        this.outputLocale = outputLocale;
        return this;
    }

    @XmlElement(name = "repositoryDestination")
    public ClientJobRepositoryDestination getRepositoryDestination() {
        return repositoryDestination;
    }

    public ClientReportJob setRepositoryDestination(ClientJobRepositoryDestination repositoryDestination) {
        this.repositoryDestination = repositoryDestination;
        return this;
    }

    @XmlElement(name = "mailNotification")
    public ClientJobMailNotification getMailNotification() {
        return mailNotification;
    }

    public ClientReportJob setMailNotification(ClientJobMailNotification mailNotification) {
        this.mailNotification = mailNotification;
        return this;
    }

    @XmlElement(name = "source")
    public ClientJobSource getSource() {
        return source;
    }

    public ClientReportJob setSource(ClientJobSource source) {
        this.source = source;
        return this;
    }

    @XmlElement(name = "alert")
    public ClientJobAlert getAlert() {
        return alert;
    }

    public ClientReportJob setAlert(ClientJobAlert alert) {
        this.alert = alert;
        return this;
    }

    @XmlElements({
            @XmlElement(name = "simpleTrigger", type = ClientJobSimpleTrigger.class),
            @XmlElement(name = "calendarTrigger", type = ClientJobCalendarTrigger.class)})
    public ClientJobTrigger getTrigger() {
        return trigger;
    }

    public ClientReportJob setTrigger(ClientJobTrigger trigger) {
        this.trigger = trigger;
        return this;
    }

	public ExportType getExportType() {
		return exportType;
	}

	public ClientReportJob setExportType(ExportType exportType) {
		this.exportType = exportType;
        return this;
	}

    @XmlElement(name = "outputFormats")
    @XmlJavaTypeAdapter(OutputFormatXmlAdapter.class)
    public Set<OutputFormat> getOutputFormats() {
        return outputFormats;
    }

    public ClientReportJob setOutputFormats(Set<OutputFormat> outputFormats) {
        this.outputFormats = outputFormats;
        return this;
    }

    public String getOutputTimeZone() {
        return outputTimeZone;
    }

    public ClientReportJob setOutputTimeZone(String outputTimeZone) {
        this.outputTimeZone = outputTimeZone;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientReportJob)) return false;

        ClientReportJob that = (ClientReportJob) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (creationDate != null ? !creationDate.equals(that.creationDate) : that.creationDate != null) return false;
        if (trigger != null ? !trigger.equals(that.trigger) : that.trigger != null) return false;
        if (source != null ? !source.equals(that.source) : that.source != null) return false;
        if (exportType != null ? !exportType.equals(that.exportType) : that.exportType != null) {
            return false;
        }
        if (baseOutputFilename != null ? !baseOutputFilename.equals(that.baseOutputFilename) : that.baseOutputFilename != null)
            return false;
        if (outputFormats != null ? !outputFormats.equals(that.outputFormats) : that.outputFormats != null)
            return false;
        if (outputLocale != null ? !outputLocale.equals(that.outputLocale) : that.outputLocale != null) return false;
        if (outputTimeZone != null ? !outputTimeZone.equals(that.outputTimeZone) : that.outputTimeZone != null)
            return false;
        if (repositoryDestination != null ? !repositoryDestination.equals(that.repositoryDestination) : that.repositoryDestination != null)
            return false;
        if (mailNotification != null ? !mailNotification.equals(that.mailNotification) : that.mailNotification != null)
            return false;
        if (alert != null ? !alert.equals(that.alert) : that.alert != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (trigger != null ? trigger.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (exportType != null ? exportType.hashCode() : 0);
        result = 31 * result + (baseOutputFilename != null ? baseOutputFilename.hashCode() : 0);
        result = 31 * result + (outputFormats != null ? outputFormats.hashCode() : 0);
        result = 31 * result + (outputLocale != null ? outputLocale.hashCode() : 0);
        result = 31 * result + (outputTimeZone != null ? outputTimeZone.hashCode() : 0);
        result = 31 * result + (repositoryDestination != null ? repositoryDestination.hashCode() : 0);
        result = 31 * result + (mailNotification != null ? mailNotification.hashCode() : 0);
        result = 31 * result + (alert != null ? alert.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientReportJob{" +
                "id=" + id +
                ", version=" + version +
                ", username='" + username + '\'' +
                ", label='" + label + '\'' +
                ", description='" + description + '\'' +
                ", creationDate=" + creationDate +
                ", trigger=" + trigger +
                ", source=" + source +
                ", baseOutputFilename='" + baseOutputFilename + '\'' +
                ", exportType=" + exportType +
                ", outputFormats=" + outputFormats +
                ", outputLocale='" + outputLocale + '\'' +
                ", outputTimeZone='" + outputTimeZone + '\'' +
                ", repositoryDestination=" + repositoryDestination +
                ", mailNotification=" + mailNotification +
                ", alert=" + alert +
                '}';
    }

    @Override
    public ClientReportJob deepClone() {
        return new ClientReportJob(this);
    }
}
