/*
 * Copyright (C) 2025 the Jasper Server OS Authors
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

package com.jaspersoft.jasperserver.war.control;

import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.common.util.StaticExecutionContextProvider;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceLookup;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.api.metadata.user.service.ObjectPermissionService;
import com.jaspersoft.jasperserver.api.metadata.user.service.UserAuthorityService;
import com.jaspersoft.jasperserver.api.metadata.view.domain.FilterCriteria;
import com.jaspersoft.jasperserver.war.common.JasperServerConst;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.support.WebContentGenerator;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author aztec
 * @version $Id$
 */
@Controller
public abstract class JRBaseController extends WebContentGenerator {

	protected RepositoryService repository;
	protected UserAuthorityService userAuthService;
	protected ObjectPermissionService objPermService;

	public RepositoryService getRepository() {
		return repository;
	}

	public void setRepository(RepositoryService repository) {
		this.repository = repository;
	}

	public UserAuthorityService getUserAuthService() {
		return userAuthService;
	}

	public void setUserAuthService(UserAuthorityService userAuthService) {
		this.userAuthService = userAuthService;
	}

	public ObjectPermissionService getObjPermService() {
		return objPermService;
	}

	public void setObjPermService(ObjectPermissionService objPermService) {
		this.objPermService = objPermService;
	}

	protected final ResourceLookup[] getReportUnits()
	{
		
		List reportUnitsList = repository.loadResourcesList(
				StaticExecutionContextProvider.getExecutionContext(),
				FilterCriteria.createFilter(ReportUnit.class));
		
		ResourceLookup[] reportUnits = new ResourceLookup[0];
		
		if (reportUnitsList != null && !reportUnitsList.isEmpty()) {
			reportUnits = (ResourceLookup[]) reportUnitsList.toArray(reportUnits);
		}
		return reportUnits;
	}


	protected final ReportUnit getReportUnit(HttpServletRequest req)
	{
		String uri = req.getParameter(JasperServerConst.REPORT_REQUEST_PARAM);
		return getReportUnit(uri);
	}

	protected final ReportUnit getReportUnit(String uri)
	{
		ExecutionContextImpl executionContext = new ExecutionContextImpl();
		ReportUnit reportUnit = (ReportUnit) repository.getResource(executionContext, uri);
		return reportUnit;
	}

	protected final List getAllRoles()
	{
		ExecutionContextImpl context = new ExecutionContextImpl();
		List roles = userAuthService.getRoles(context, null);
		return roles;
	}

	protected final List getAllUsers()
	{
		ExecutionContextImpl context = new ExecutionContextImpl();
		List users = userAuthService.getUsers(context, null);
		return users;
	}

}
