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

import jakarta.xml.bind.annotation.XmlRootElement;

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
@XmlRootElement(name = "repositoryDestination")
public class ClientJobRepositoryDestination implements DeepCloneable<ClientJobRepositoryDestination> {

    private String folderURI;
    private Long id;
    private String outputDescription;
    private Boolean overwriteFiles;
    private Boolean sequentialFilenames;
    private Integer version;
    private String timestampPattern;
    private Boolean saveToRepository;
    private String defaultReportOutputFolderURI;
    private Boolean usingDefaultReportOutputFolderURI;
    private String outputLocalFolder;
    private ClientJobFtpInfo outputFTPInfo;

    public ClientJobRepositoryDestination() {
    }

    public ClientJobRepositoryDestination(ClientJobRepositoryDestination other) {
        checkNotNull(other);

        this.defaultReportOutputFolderURI = other.getDefaultReportOutputFolderURI();
        this.folderURI = other.getFolderURI();
        this.id = other.getId();
        this.outputDescription = other.getOutputDescription();
        this.outputFTPInfo = copyOf(other.getOutputFTPInfo());
        this.outputLocalFolder = other.getOutputLocalFolder();
        this.overwriteFiles = other.isOverwriteFiles();
        this.saveToRepository = other.isSaveToRepository();
        this.sequentialFilenames = other.isSequentialFilenames();
        this.timestampPattern = other.getTimestampPattern();
        this.usingDefaultReportOutputFolderURI = other.isUsingDefaultReportOutputFolderURI();
        this.version = other.getVersion();
    }

    public String getFolderURI() {
        return folderURI;
    }

    public ClientJobRepositoryDestination setFolderURI(String folderURI) {
        this.folderURI = folderURI;
        return this;
    }

    public Long getId() {
        return id;
    }

    public ClientJobRepositoryDestination setId(Long id) {
        this.id = id;
        return this;
    }

    public String getOutputDescription() {
        return outputDescription;
    }

    public ClientJobRepositoryDestination setOutputDescription(String outputDescription) {
        this.outputDescription = outputDescription;
        return this;
    }

    public Boolean isOverwriteFiles() {
        return overwriteFiles;
    }

    public ClientJobRepositoryDestination setOverwriteFiles(Boolean overwriteFiles) {
        this.overwriteFiles = overwriteFiles;
        return this;
    }

    public Boolean isSequentialFilenames() {
        return sequentialFilenames;
    }

    public ClientJobRepositoryDestination setSequentialFilenames(Boolean sequentialFilenames) {
        this.sequentialFilenames = sequentialFilenames;
        return this;
    }

    public Integer getVersion() {
        return version;
    }

    public ClientJobRepositoryDestination setVersion(Integer version) {
        this.version = version;
        return this;
    }

    public String getTimestampPattern() {
        return timestampPattern;
    }

    public ClientJobRepositoryDestination setTimestampPattern(String timestampPattern) {
        this.timestampPattern = timestampPattern;
        return this;
    }

    public Boolean isSaveToRepository() {
        return saveToRepository;
    }

    public ClientJobRepositoryDestination setSaveToRepository(Boolean saveToRepository) {
        this.saveToRepository = saveToRepository;
        return this;
    }

    public String getDefaultReportOutputFolderURI() {
        return defaultReportOutputFolderURI;
    }

    public ClientJobRepositoryDestination setDefaultReportOutputFolderURI(String defaultReportOutputFolderURI) {
        this.defaultReportOutputFolderURI = defaultReportOutputFolderURI;
        return this;
    }

    public Boolean isUsingDefaultReportOutputFolderURI() {
        return usingDefaultReportOutputFolderURI;
    }

    public ClientJobRepositoryDestination setUsingDefaultReportOutputFolderURI(Boolean usingDefaultReportOutputFolderURI) {
        this.usingDefaultReportOutputFolderURI = usingDefaultReportOutputFolderURI;
        return this;
    }

    public String getOutputLocalFolder() {
        return outputLocalFolder;
    }

    public ClientJobRepositoryDestination setOutputLocalFolder(String outputLocalFolder) {
        this.outputLocalFolder = outputLocalFolder;
        return this;
    }

    public ClientJobFtpInfo getOutputFTPInfo() {
        return outputFTPInfo;
    }

    public ClientJobRepositoryDestination setOutputFTPInfo(ClientJobFtpInfo outputFTPInfo) {
        this.outputFTPInfo = outputFTPInfo;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientJobRepositoryDestination)) return false;

        ClientJobRepositoryDestination that = (ClientJobRepositoryDestination) o;

        if (defaultReportOutputFolderURI != null ? !defaultReportOutputFolderURI.equals(that.defaultReportOutputFolderURI) : that.defaultReportOutputFolderURI != null)
            return false;
        if (folderURI != null ? !folderURI.equals(that.folderURI) : that.folderURI != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (outputDescription != null ? !outputDescription.equals(that.outputDescription) : that.outputDescription != null)
            return false;
        if (outputFTPInfo != null ? !outputFTPInfo.equals(that.outputFTPInfo) : that.outputFTPInfo != null)
            return false;
        if (outputLocalFolder != null ? !outputLocalFolder.equals(that.outputLocalFolder) : that.outputLocalFolder != null)
            return false;
        if (overwriteFiles != null ? !overwriteFiles.equals(that.overwriteFiles) : that.overwriteFiles != null)
            return false;
        if (saveToRepository != null ? !saveToRepository.equals(that.saveToRepository) : that.saveToRepository != null)
            return false;
        if (sequentialFilenames != null ? !sequentialFilenames.equals(that.sequentialFilenames) : that.sequentialFilenames != null)
            return false;
        if (timestampPattern != null ? !timestampPattern.equals(that.timestampPattern) : that.timestampPattern != null)
            return false;
        if (usingDefaultReportOutputFolderURI != null ? !usingDefaultReportOutputFolderURI.equals(that.usingDefaultReportOutputFolderURI) : that.usingDefaultReportOutputFolderURI != null)
            return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = folderURI != null ? folderURI.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (outputDescription != null ? outputDescription.hashCode() : 0);
        result = 31 * result + (overwriteFiles != null ? overwriteFiles.hashCode() : 0);
        result = 31 * result + (sequentialFilenames != null ? sequentialFilenames.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (timestampPattern != null ? timestampPattern.hashCode() : 0);
        result = 31 * result + (saveToRepository != null ? saveToRepository.hashCode() : 0);
        result = 31 * result + (defaultReportOutputFolderURI != null ? defaultReportOutputFolderURI.hashCode() : 0);
        result = 31 * result + (usingDefaultReportOutputFolderURI != null ? usingDefaultReportOutputFolderURI.hashCode() : 0);
        result = 31 * result + (outputLocalFolder != null ? outputLocalFolder.hashCode() : 0);
        result = 31 * result + (outputFTPInfo != null ? outputFTPInfo.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientJobRepositoryDestination{" +
                "folderURI='" + folderURI + '\'' +
                ", id=" + id +
                ", outputDescription='" + outputDescription + '\'' +
                ", overwriteFiles=" + overwriteFiles +
                ", sequentialFilenames=" + sequentialFilenames +
                ", version=" + version +
                ", timestampPattern='" + timestampPattern + '\'' +
                ", saveToRepository=" + saveToRepository +
                ", defaultReportOutputFolderURI='" + defaultReportOutputFolderURI + '\'' +
                ", usingDefaultReportOutputFolderURI=" + usingDefaultReportOutputFolderURI +
                ", outputLocalFolder='" + outputLocalFolder + '\'' +
                ", outputFTPInfo=" + outputFTPInfo +
                '}';
    }

    @Override
    public ClientJobRepositoryDestination deepClone() {
        return new ClientJobRepositoryDestination(this);
    }
}
