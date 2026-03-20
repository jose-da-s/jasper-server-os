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
package com.jaspersoft.jasperserver.remote.connection;

import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.FTPInfo;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJob;
import com.jaspersoft.jasperserver.api.engine.scheduling.service.ReportSchedulingService;
import com.jaspersoft.jasperserver.dto.connection.FtpConnection;

import jakarta.annotation.Resource;

/**
 * @author akasych
 * @version $Id:
 */
public class ReportJobFtpConnectionDescriptionProviderImpl implements FtpConnectionDescriptionProvider {

    @Resource(name = "concreteReportSchedulingService")
    private ReportSchedulingService scheduler;

    public FtpConnection getFtpConnectionDescription(String id) {
        FtpConnection ftpConnection = new FtpConnection();
        if (scheduler != null) {
            ReportJob job = scheduler.getScheduledJob(ExecutionContextImpl.getRuntimeExecutionContext(), new Long(id));
            if (job != null && job.getContentRepositoryDestination() != null && job.getContentRepositoryDestination().getOutputFTPInfo() != null) {
                FTPInfo ftpInfo = job.getContentRepositoryDestination().getOutputFTPInfo();
                ftpConnection.setHost(ftpInfo.getServerName());
                ftpConnection.setPort(ftpInfo.getPort());
                ftpConnection.setProtocol(ftpInfo.getProtocol());
                if (ftpInfo.getType().equals(FTPInfo.TYPE_FTP))
                    ftpConnection.setType(FtpConnection.FtpType.ftp);
                else if (ftpInfo.getType().equals(FTPInfo.TYPE_FTPS))
                    ftpConnection.setType(FtpConnection.FtpType.ftps);
                else if (ftpInfo.getType().equals(FTPInfo.TYPE_SFTP)) {
                    ftpConnection.setType(FtpConnection.FtpType.sftp);
                }
                ftpConnection.setUserName(ftpInfo.getUserName());
                ftpConnection.setPassword(ftpInfo.getPassword());
                ftpConnection.setProt(ftpInfo.getProt());
                ftpConnection.setPbsz(ftpInfo.getPbsz());
                ftpConnection.setSshKey(ftpInfo.getSshKey());
                ftpConnection.setSshPassphrase(ftpInfo.getSshPassphrase());
                ftpConnection.setFolderPath(ftpInfo.getFolderPath());
            }
        }
        return ftpConnection;
    }

}

