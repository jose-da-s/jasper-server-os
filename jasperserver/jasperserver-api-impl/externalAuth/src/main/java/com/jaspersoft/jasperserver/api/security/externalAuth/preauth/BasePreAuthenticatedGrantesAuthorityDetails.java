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
package com.jaspersoft.jasperserver.api.security.externalAuth.preauth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;

/**
 * Class used by authenticationDetailsSource in BasePreAuthenticatedProcessingFilter bean.
 * authenticationDetailsSource processes request parameters extracting them into the details object.
 *
 * User: dlitvak
 * Date: 10/1/13
 */
public class BasePreAuthenticatedGrantesAuthorityDetails extends PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails {

	public BasePreAuthenticatedGrantesAuthorityDetails(HttpServletRequest request) {
		super(request,new ArrayList<GrantedAuthority>());
	}
}

