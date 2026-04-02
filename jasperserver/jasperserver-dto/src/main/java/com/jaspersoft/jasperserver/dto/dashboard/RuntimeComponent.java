/*
 * Copyright (C) 2025-2026 the Jasper Server OS Authors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * Copyright (C) 2005-2023. Cloud Software Group, Inc. All Rights Reserved. Confidential & Proprietary.
 * Licensed pursuant to commercial Cloud Software Group, Inc End User License Agreement.
 */
package com.jaspersoft.jasperserver.dto.dashboard;

import java.util.Arrays;
import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlElements;

/**
*
* @author Lucian Chirita
*/
public class RuntimeComponent {

	private String componentId;
	
	private List<RuntimeComponentAttribute> attributes;

	public RuntimeComponent() {
	}

	public RuntimeComponent(String componentId, RuntimeComponentAttribute...attributes) {
		this.componentId = componentId;
		this.attributes = Arrays.asList(attributes);
	}
	
	public String getComponentId() {
		return componentId;
	}

	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

    @XmlElementWrapper(name = "attributes")
    @XmlElements({
        @XmlElement(name = "componentReportExecution", type = ComponentReportExecution.class),
        @XmlElement(name = "componentParameters", type = ComponentParameters.class),
        @XmlElement(name = "componentReplace", type = ComponentReplace.class),
        @XmlElement(name = "adhocTypeChange", type = AdhocTypeChange.class)})
	public List<RuntimeComponentAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<RuntimeComponentAttribute> attributes) {
		this.attributes = attributes;
	}
	
}
