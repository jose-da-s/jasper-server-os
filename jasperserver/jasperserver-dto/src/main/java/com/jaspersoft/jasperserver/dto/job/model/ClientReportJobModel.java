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

package com.jaspersoft.jasperserver.dto.job.model;

/**
 * Definition model of a thumbnail execution job. Model is used in search/ update only.
 *
 * <p>
 * A thumbnail job definition specifies wich thumbnail inFolder execute and when,
 * what output inFolder generate and where inFolder send the output.
 * </p>
 *
 * @author Ivan Chan (ichan@jaspersoft.com)
 * @version $Id$
 * @since 4.7
 */

import com.jaspersoft.jasperserver.dto.common.OutputFormat;
import com.jaspersoft.jasperserver.dto.job.ClientJobAlert;
import com.jaspersoft.jasperserver.dto.job.ClientJobMailNotification;
import com.jaspersoft.jasperserver.dto.job.ClientJobRepositoryDestination;
import com.jaspersoft.jasperserver.dto.job.ClientJobSource;
import com.jaspersoft.jasperserver.dto.job.ClientJobTrigger;
import com.jaspersoft.jasperserver.dto.job.ClientReportJob;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;
import java.util.Set;

@XmlRootElement(name = "jobModel")
public class ClientReportJobModel extends ClientReportJob {

    private boolean isCreationDateModified = false;
    private boolean isSourceModified = false;
    private boolean isTriggerModified = false;
    private boolean isMailNotificationModified = false;
    private boolean isAlertModified = false;
    private boolean isContentRespositoryDestinationModified = false;
    private boolean isDescriptionModified = false;
    private boolean isLabelModified = false;
    private boolean isBaseOutputFileNameModified = false;
    private boolean isOutputFormatsModified = false;
    private boolean isUsernameModified = false;
    private boolean isOutputLocaleModified = false;

    public ClientReportJobModel() {
        super();
    }

    public ClientReportJobModel(ClientReportJobModel other) {
        super(other);
        this.isAlertModified = other.isAlertModified;
        this.isBaseOutputFileNameModified = other.isBaseOutputFileNameModified;
        this.isContentRespositoryDestinationModified = other.isContentRespositoryDestinationModified;
        this.isCreationDateModified = other.isCreationDateModified;
        this.isDescriptionModified = other.isDescriptionModified;
        this.isLabelModified = other.isLabelModified;
        this.isMailNotificationModified = other.isMailNotificationModified;
        this.isOutputFormatsModified = other.isOutputFormatsModified;
        this.isOutputLocaleModified = other.isOutputLocaleModified;
        this.isSourceModified = other.isSourceModified;
        this.isTriggerModified = other.isTriggerModified;
        this.isUsernameModified = other.isUsernameModified;
    }

    public ClientReportJobModel(ClientReportJob clientReportJob) {
        super(clientReportJob);
    }

    public boolean isAlertModified() {
        return isAlertModified;
    }

    public boolean isBaseOutputFileNameModified() {
        return isBaseOutputFileNameModified;
    }

    public boolean isContentRespositoryDestinationModified() {
        return isContentRespositoryDestinationModified;
    }

    public boolean isCreationDateModified() {
        return isCreationDateModified;
    }

    public boolean isDescriptionModified() {
        return isDescriptionModified;
    }

    public boolean isLabelModified() {
        return isLabelModified;
    }

    public boolean isMailNotificationModified() {
        return isMailNotificationModified;
    }

    public boolean isOutputFormatsModified() {
        return isOutputFormatsModified;
    }

    public boolean isOutputLocaleModified() {
        return isOutputLocaleModified;
    }

    public boolean isSourceModified() {
        return isSourceModified;
    }

    public boolean isTriggerModified() {
        return isTriggerModified;
    }

    public boolean isUsernameModified() {
        return isUsernameModified;
    }


    @XmlElement(name = "sourceModel")
    public ClientReportJobModel setSourceModel(ClientJobSourceModel source) {
        this.setSource(source);
        return this;
    }


    @XmlElements({
            @XmlElement(name = "simpleTriggerModel", type = ClientJobSimpleTriggerModel.class),
            @XmlElement(name = "calendarTriggerModel", type = ClientJobCalendarTriggerModel.class)})
    public ClientReportJobModel setTriggerModel(ClientJobTrigger trigger) {
        this.setTrigger(trigger);
        return this;
    }

    @XmlElement(name = "mailNotificationModel")
    public ClientReportJobModel setMailNotificationModel(ClientJobMailNotificationModel mailNotification) {
        this.setMailNotification(mailNotification);
        return this;
    }

    @XmlElement(name = "alertModel")
    public ClientReportJobModel setAlertModel(ClientJobAlertModel alert) {
        this.setAlert(alert);
        return this;
    }

    @XmlElement(name = "repositoryDestinationModel")
    public ClientReportJobModel setRepositoryDestinationModel(ClientJobRepositoryDestinationModel repositoryDestination) {
        this.setRepositoryDestination(repositoryDestination);
        return this;
    }

    @Override
    public ClientReportJobModel setSource(ClientJobSource source) {
        super.setSource(source);
        isSourceModified = true;
        return this;
    }

    @Override
    public ClientReportJobModel setTrigger(ClientJobTrigger trigger) {
        super.setTrigger(trigger);
        isTriggerModified = true;
        return this;
    }

    @Override
    public ClientReportJobModel setMailNotification(ClientJobMailNotification mailNotification) {
        super.setMailNotification(mailNotification);
        isMailNotificationModified = true;
        return this;
    }

    @Override
    public ClientReportJobModel setAlert(ClientJobAlert alert) {
        super.setAlert(alert);
        isAlertModified = true;
        return this;
    }

    @Override
    public ClientReportJobModel setRepositoryDestination(ClientJobRepositoryDestination repositoryDestination) {
        super.setRepositoryDestination(repositoryDestination);
        isContentRespositoryDestinationModified = true;
        return this;
    }

    @Override
    public ClientReportJobModel setDescription(String description) {
        super.setDescription(description);
        isDescriptionModified = true;
        return this;
    }

    @Override
    public ClientReportJobModel setCreationDate(Timestamp creationDate) {
        super.setCreationDate(creationDate);
        isCreationDateModified = true;
        return this;
    }

    @Override
    public ClientReportJobModel setLabel(String label) {
        super.setLabel(label);
        isLabelModified = true;
        return this;
    }

    @Override
    public ClientReportJobModel setBaseOutputFilename(String baseOutputFilename) {
        super.setBaseOutputFilename(baseOutputFilename);
        isBaseOutputFileNameModified = true;
        return this;
    }

    @Override
    public ClientReportJobModel setOutputFormats(Set<OutputFormat> outputFormats) {
        super.setOutputFormats(outputFormats);
        isOutputFormatsModified = true;
        return this;
    }

    @Override
    public ClientReportJobModel setUsername(String username) {
        super.setUsername(username);
        isUsernameModified = true;
        return this;
    }

    @Override
    public ClientReportJobModel setOutputLocale(String outputLocale) {
        super.setOutputLocale(outputLocale);
        isOutputLocaleModified = true;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientReportJobModel)) return false;
        if (!super.equals(o)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (isCreationDateModified ? 1 : 0);
        result = 31 * result + (isSourceModified ? 1 : 0);
        result = 31 * result + (isTriggerModified ? 1 : 0);
        result = 31 * result + (isMailNotificationModified ? 1 : 0);
        result = 31 * result + (isAlertModified ? 1 : 0);
        result = 31 * result + (isContentRespositoryDestinationModified ? 1 : 0);
        result = 31 * result + (isDescriptionModified ? 1 : 0);
        result = 31 * result + (isLabelModified ? 1 : 0);
        result = 31 * result + (isBaseOutputFileNameModified ? 1 : 0);
        result = 31 * result + (isOutputFormatsModified ? 1 : 0);
        result = 31 * result + (isUsernameModified ? 1 : 0);
        result = 31 * result + (isOutputLocaleModified ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientReportJobModel{" +
                "isAlertModified=" + isAlertModified +
                ", isCreationDateModified=" + isCreationDateModified +
                ", isSourceModified=" + isSourceModified +
                ", isTriggerModified=" + isTriggerModified +
                ", isMailNotificationModified=" + isMailNotificationModified +
                ", isContentRespositoryDestinationModified=" + isContentRespositoryDestinationModified +
                ", isDescriptionModified=" + isDescriptionModified +
                ", isLabelModified=" + isLabelModified +
                ", isBaseOutputFileNameModified=" + isBaseOutputFileNameModified +
                ", isOutputFormatsModified=" + isOutputFormatsModified +
                ", isUsernameModified=" + isUsernameModified +
                ", isOutputLocaleModified=" + isOutputLocaleModified +
                '}';
    }

    @Override
    public ClientReportJobModel deepClone() {
        return new ClientReportJobModel(this);
    }
}
