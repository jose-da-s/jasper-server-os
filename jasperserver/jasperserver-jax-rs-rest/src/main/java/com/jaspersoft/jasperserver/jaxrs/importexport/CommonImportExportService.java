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
package com.jaspersoft.jasperserver.jaxrs.importexport;

import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.api.common.crypto.Hexer;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResourceData;
import com.jaspersoft.jasperserver.api.metadata.common.service.JSResourceNotFoundException;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.security.encryption.PlainCipher;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.importexport.ImportTask;
import com.jaspersoft.jasperserver.dto.importexport.State;
import com.jaspersoft.jasperserver.dto.resources.ClientProperty;
import com.jaspersoft.jasperserver.export.service.ImportExportService;
import com.jaspersoft.jasperserver.export.service.impl.ImportExportServiceImpl;
import com.jaspersoft.jasperserver.remote.services.async.ImportExportTask;
import com.jaspersoft.jasperserver.remote.services.async.ImportRunnable;
import com.jaspersoft.jasperserver.remote.services.async.Task;
import com.jaspersoft.jasperserver.remote.services.async.TasksManager;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import javax.management.openmbean.InvalidKeyException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl.getRuntimeExecutionContext;
import static com.jaspersoft.jasperserver.export.service.ImportExportService.SECRET_KEY;
import static com.jaspersoft.jasperserver.export.util.EncryptionParams.KEY_ALIAS_PARAMETER;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.empty;
import static org.apache.commons.lang.StringUtils.join;

/**
 * @author Zakhar.Tomchenco
 */

public abstract class CommonImportExportService {
    private static final Log log = LogFactory.getLog(CommonImportExportService.class);
    public static final String OMITTED = "<omitted>";

    @Resource(name = "concreteRepository")
    protected RepositoryService repository;

    @Resource(name = "passwordEncoder")
    protected PlainCipher passwordEncoder;

    @Resource(name = "importExportCipher")
    protected PlainCipher importExportCipher;

    Function<String, ErrorDescriptor> invalidKeyLength =  (String key) -> new ErrorDescriptor()
                    .setErrorCode("import.invalid.secretKey.length")
                    .addProperties(new ClientProperty("secretKey", OMITTED));

    public Optional<String> processSecretKey(String secretKey) throws InvalidEncryptionKey {
        if (secretKey == null) return empty();

        String sKey = secretKey.trim();
        if (sKey.isEmpty()) return empty();

        final byte[] key;
        try {
            key = Hexer.parse(sKey);// parsing just to validate
        } catch (Exception e) {
            throw new InvalidEncryptionKey(new ErrorDescriptor()
                    .setErrorCode("import.invalid.secretKey")
                    .addProperties(new ClientProperty("secretKey", OMITTED)));
        }

        if (isValid(key)) {
            return Optional.of(sKey);

        } else {
            throw new InvalidEncryptionKey(new ErrorDescriptor()
                    .setErrorCode("import.invalid.secretKey.length")
                    .addProperties(new ClientProperty("secretKey", OMITTED)));
        }

    }

    public Optional<String> processSecretUri(String secretUri ) throws InvalidEncryptionKey {
        if (secretUri == null) return empty();

        String sUri = secretUri.trim();
        if (sUri.isEmpty()) return empty();

        final byte[] key;

        final String content;
        try {
            FileResourceData resourceData = repository.getResourceData(getRuntimeExecutionContext(), sUri);
            content = IOUtils.toString(resourceData.getDataStream(), UTF_8.name());
        } catch (JSResourceNotFoundException | IOException e) {
            throw new InvalidEncryptionKey(new ErrorDescriptor()
                    .setErrorCode("import.invalid.secretUri").setException(e)
                    .addProperties(new ClientProperty("secretUri", sUri)), e);
        }

        final String data;
        try {
            data = passwordEncoder.decode(content.trim());
            key = Hexer.parse(data);
        } catch (Exception e) {
            throw new InvalidEncryptionKey(new ErrorDescriptor()
                    .setErrorCode("import.invalid.secretUri.secretFile").setException(e)
                    .addProperties(new ClientProperty("secretUri", sUri)), e);
        }

        if (isValid(key)) {
            return Optional.of(data);
        } else {
            throw new InvalidEncryptionKey(new ErrorDescriptor()
                    .setErrorCode("import.invalid.secretKey.length")
                    .addProperties(new ClientProperty("secretUri", sUri)));

        }

    }

    private boolean isValid(final byte[] key) {
        final String transformation = importExportCipher.getCipherTransformation();
        return
                (transformation.startsWith("AES") && (key.length == 16 || key.length == 24 || key.length == 32))
                        || (transformation.startsWith("DESede") && (key.length == 14 || key.length == 21));
    }

    static class InvalidEncryptionKey extends Exception {
        final ErrorDescriptor error;

        public InvalidEncryptionKey(ErrorDescriptor error) {
            super(error.getMessage());
            this.error = error;
        }

        public InvalidEncryptionKey(ErrorDescriptor error, Throwable cause) {
            super(cause);
            this.error = error;
        }
    }
}

