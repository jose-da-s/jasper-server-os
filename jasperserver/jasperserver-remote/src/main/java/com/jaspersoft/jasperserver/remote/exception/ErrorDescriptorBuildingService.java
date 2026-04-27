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
package com.jaspersoft.jasperserver.remote.exception;


import com.jaspersoft.jasperserver.api.common.error.handling.SecureExceptionHandler;
import com.jaspersoft.jasperserver.core.util.type.GenericTypeProcessorRegistry;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptorBuilder;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@Service
public class ErrorDescriptorBuildingService {
    @Resource
    private GenericTypeProcessorRegistry genericTypeProcessorRegistry;

    @Resource
    private SecureExceptionHandler secureExceptionHandler;

    public ErrorDescriptor buildErrorDescriptor(Throwable e){
        final ErrorDescriptorBuilder errorDescriptorBuilder = genericTypeProcessorRegistry
                .getTypeProcessor(e.getClass(), ErrorDescriptorBuilder.class, false);
        return errorDescriptorBuilder != null ? errorDescriptorBuilder.build(e) : secureExceptionHandler.handleException(e);
    }
}

