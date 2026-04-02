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

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 * @author Yaroslav.Kovalchyk
 * @version $Id: ReportOutputResource.java 26599 2012-12-10 13:04:23Z ykovalchyk $
 */
@XmlRootElement
public class ReportOutputResource {

    private String contentType;
    private byte[] data;
    private String fileName;
    private String pages;
    private Boolean outputFinal;
    private Long outputTimestamp;
    private String outputZoom;
    private String dataTimestampMessage;
    private Integer lastPartialPage;
    private String snapshotSaveStatus;

    public String getPages() {
        return pages;
    }

    public ReportOutputResource setPages(String pages) {
        this.pages = pages;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public ReportOutputResource setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public ReportOutputResource setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    @XmlTransient
    public byte[] getData() {
        return data;
    }

    public ReportOutputResource setData(byte[] data) {
        this.data = data;
        return this;
    }

    public Boolean getOutputFinal() {
        return outputFinal;
    }

    public ReportOutputResource setOutputFinal(Boolean outputFinal) {
        this.outputFinal = outputFinal;
        return this;
    }

    public Long getOutputTimestamp() {
        return outputTimestamp;
    }

    public ReportOutputResource setOutputTimestamp(Long outputTimestamp) {
        this.outputTimestamp = outputTimestamp;
        return this;
    }

    @XmlTransient
    public String getOutputZoom() {
        return outputZoom;
    }

    public ReportOutputResource setOutputZoom(String outputZoom) {
        this.outputZoom = outputZoom;
        return this;
    }

    @XmlTransient
    public String getDataTimestampMessage() {
        return dataTimestampMessage;
    }

    public ReportOutputResource setDataTimestampMessage(String dataTimestampMessage) {
        this.dataTimestampMessage = dataTimestampMessage;
        return this;
    }

    @XmlTransient
    public Integer getLastPartialPage() {
        return lastPartialPage;
    }

    public ReportOutputResource setLastPartialPage(Integer lastPartialPage) {
        this.lastPartialPage = lastPartialPage;
        return this;
    }

    @XmlTransient
    public String getSnapshotSaveStatus() {
        return snapshotSaveStatus;
    }

    public ReportOutputResource setSnapshotSaveStatus(String snapshotSaveStatus) {
        this.snapshotSaveStatus = snapshotSaveStatus;
        return this;
    }
}
