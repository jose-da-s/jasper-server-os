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

import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.common.crypto.PasswordCipherer;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.error.handling.SecureExceptionHandler;
import com.jaspersoft.jasperserver.api.common.util.FTPService;
import com.jaspersoft.jasperserver.api.engine.common.util.impl.FTPUtil;
import com.jaspersoft.jasperserver.api.metadata.common.service.JSResourceNotFoundException;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.dto.connection.FtpConnection;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.util.TrustManagerUtils;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@Service
public class FtpContextStrategy implements ContextManagementStrategy<FtpConnection, FtpConnection> {
    private final static Log log = LogFactory.getLog(FtpContextStrategy.class);
    private FTPService ftpService = new FTPUtil();

    @Resource
    private SecureExceptionHandler secureExceptionHandler;
    @Resource(name = "concreteRepository")
    private RepositoryService repository;
    @Resource
    private Map<String, FtpConnectionDescriptionProvider> connectionDescriptionProviders;

    @Override
    public FtpConnection createContext(ExecutionContext ctx, FtpConnection contextDescriptionOrg, Map<String, Object> data) throws IllegalParameterValueException {
        FTPService.FTPServiceClient client = null;
        // deep copy ftp connection, so the original ftp connection will remind unchange and we can reuse that in error message
        FtpConnection contextDescription = contextDescriptionOrg.deepClone();
        try {

            // Check secure fields for null values and update them if necessary:
            // resolve the original values
            FtpConnection storedFtpConnection = null;

            String holder = contextDescription.getHolder();
            FtpConnectionDescriptionProvider provider = null;
            if (holder != null) {
                String[] holderParts = holder.split(":");

                if (holderParts.length != 2)
                    throw new IllegalParameterValueException("holder", holder);

                provider = getConnectionDescriptionProviders().get(holderParts[0]);
                if (provider != null) {
                    storedFtpConnection = provider.getFtpConnectionDescription(holderParts[1]);
                } else {
                    throw new IllegalParameterValueException("holder", holder);
                }
            }
            // restore password
            if (contextDescription.getPassword() == null) {
                contextDescription.setPassword(storedFtpConnection != null ? storedFtpConnection.getPassword() : null);
            }
            // restore sshPassphrase
            if (contextDescription.getSshPassphrase() == null) {
                contextDescription.setSshPassphrase(storedFtpConnection != null ? storedFtpConnection.getSshPassphrase() : null);
            }


            if (contextDescription.getType() == FtpConnection.FtpType.ftp) {
                client = ftpService.connectFTP(contextDescription.getHost(), contextDescription.getPort(),
                        contextDescription.getUserName(), contextDescription.getPassword());
            } else if (contextDescription.getType() == FtpConnection.FtpType.ftps) {
                client = ftpService.connectFTPS(contextDescription.getHost(), contextDescription.getPort(),
                        contextDescription.getProtocol(), contextDescription.getImplicit(),
                        contextDescription.getPbsz(), contextDescription.getProt(),
                        contextDescription.getUserName(), contextDescription.getPassword(), false, TrustManagerUtils.getAcceptAllTrustManager());
            } else if (contextDescription.getType() == FtpConnection.FtpType.sftp) {

                // Read SSH Private key data from repo file resource
                String sshKeyPath = contextDescription.getSshKey();
                String sshKeyData = null;
                if (sshKeyPath != null) {
                    try {
                        // get file data
                        sshKeyData = new String(repository.getResourceData(null, sshKeyPath).getData());

                        // decode if encrypted
                        sshKeyData = PasswordCipherer.getInstance().decodePassword(sshKeyData);
                    } catch (JSResourceNotFoundException e) {
                        log.error("Failed to read the SSH Private Key from repository.");
                    } catch (DataAccessResourceFailureException e) {
                        log.warn("Failed to decrypt the SSH Private Key. Most likely reason is unencrypted data in db.");
                        // If not encrypted resources need to be supported then keep the resolved sshKeyData value
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                }

                client = ftpService.connectSFTP(contextDescription.getHost(), contextDescription.getPort(),
                        contextDescription.getUserName(), contextDescription.getPassword(), null,
                        sshKeyData, contextDescription.getSshPassphrase());
            } else {
                String message = "FtpConnection error: unknown FTP service type: " + contextDescription.getType();
                log.error(message);
                throw new JSException(message);
            }
            client.changeDirectory(contextDescription.getFolderPath());
        } catch (UnknownHostException e) {
            throw new ContextCreationFailedException(contextDescriptionOrg.getHost(), "host", null, e, secureExceptionHandler);
        } catch (Exception e) {
            throw new ContextCreationFailedException(contextDescriptionOrg, e, secureExceptionHandler);
        } finally {
            if (client != null) try {
                client.disconnect();
            } catch (Exception e) {
                log.error("Couldn't disconnect FTP connection", e);
            }
        }
        return contextDescription;
    }

    @Override
    public void deleteContext(FtpConnection contextDescription, Map<String, Object> data) {
        // do nothing
    }

    @Override
    public FtpConnection getContextForClient(FtpConnection contextDescription, Map<String, Object> data, Map<String, String[]> additionalProperties) {
        return new FtpConnection(contextDescription).setPassword(null).setSshPassphrase(null);
    }

    protected Map<String, FtpConnectionDescriptionProvider> getConnectionDescriptionProviders() {
        return connectionDescriptionProviders;
    }

    protected void setConnectionDescriptionProviders(Map<String, FtpConnectionDescriptionProvider> connectionDescriptionProviders) {
        this.connectionDescriptionProviders = connectionDescriptionProviders;
    }

    protected SecureExceptionHandler getSecureExceptionHandler() {
        return secureExceptionHandler;
    }

    protected void setSecureExceptionHandler(SecureExceptionHandler secureExceptionHandler) {
        this.secureExceptionHandler = secureExceptionHandler;
    }
}

