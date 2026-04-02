/*
 * Copyright (C) 2025-2026 the Jasper Server OS Authors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * Copyright (C) 2005-2023. Cloud Software Group, Inc. All Rights Reserved. Confidential & Proprietary.
 * Licensed pursuant to commercial Cloud Software Group, Inc End User License Agreement.
 */
package com.jaspersoft.jasperserver.dto.dashboard;

import java.util.List;

import jakarta.xml.bind.annotation.XmlElementWrapper;

import com.jaspersoft.jasperserver.dto.reports.ReportParameter;

/**
*
* @author Lucian Chirita
*/
public class ComponentParameters implements RuntimeComponentAttribute {

	private List<ReportParameter> parameters;

	@XmlElementWrapper(name = "parameters")
	public List<ReportParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<ReportParameter> parameters) {
		this.parameters = parameters;
	}

}
