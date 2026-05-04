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

package com.jaspersoft.jasperserver.jaxrs.common;

import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Map a {@link ConcurrencyFailureException} exception to a {@link Response}.
 *
 * @author askorodumov
 * @version $Id$
 */
@Provider
@Component
public class ConcurrencyFailureExceptionMapper implements ExceptionMapper<ConcurrencyFailureException> {
    private static final String ERROR_CODE = "concurrent.modification";

    /**
     * {@inheritDoc}
     */
    @Override
    public Response toResponse(ConcurrencyFailureException exception) {
        return Response
                .status(Response.Status.CONFLICT)
                .entity(new ErrorDescriptor()
                        .setErrorCode(ERROR_CODE)
                        .setMessage("Concurrent modification request error"))
                .build();
    }
}
