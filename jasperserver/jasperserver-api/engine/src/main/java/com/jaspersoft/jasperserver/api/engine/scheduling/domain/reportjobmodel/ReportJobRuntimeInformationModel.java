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
package com.jaspersoft.jasperserver.api.engine.scheduling.domain.reportjobmodel;

import com.jaspersoft.jasperserver.api.JasperServerAPI;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobRuntimeInformation;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.jaxb.ReportJobStateXmlAdapter;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Searching report jobs by runtime information
 * 
 * @author Ivan Chan (ichan@jaspersoft.com)
 * @version $Id$
 * @since 1.0
 * @see com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobSummary#getRuntimeInformation()
 */
@JasperServerAPI
@XmlRootElement(name = "stateModel")
public class ReportJobRuntimeInformationModel extends ReportJobRuntimeInformation {

    private boolean isNextFireTimeModified = false;
    private boolean isPreviousFireTimeModified = false;
    private boolean isStateModified = false;
	private boolean isPreviousFireTimeFromModified = false;
	private boolean isPreviousFireTimeToModified = false;
	private boolean isNextFireTimeFromModified = false;
	private boolean isNextFireTimeToModified = false;

	/**
	 * Creates an empty object.
	 */
	public ReportJobRuntimeInformationModel() {
	}


	/**
	 * Sets the job next fire time.
	 * 
	 * @param nextFireTime the next fire time for the job
	 */
	public void setNextFireTime(Date nextFireTime) {
        isNextFireTimeModified = true;
		super.setNextFireTime(nextFireTime);
	}

	/**
	 * Sets the job previous fire time.
	 * 
	 * @param previousFireTime the previous fire time of the job
	 */
	public void setPreviousFireTime(Date previousFireTime) {
        isPreviousFireTimeModified = true;
		super.setPreviousFireTime(previousFireTime);
	}


	/**
	 * Sets the execution state of the job trigger.
	 *
	 * @param state one of the <code>STATE_*</code> constants
	 */
	public void setStateCode(Byte state) {
        isStateModified = true;
		super.setStateCode(state);
	}

    /**
     * returns whether NextFireTime has been modified
     *
     * @return true if the attribute has been modified
     */
    public boolean isNextFireTimeModified() { return isNextFireTimeModified; }

    /**
     * returns whether PreviousFireTime has been modified
     *
     * @return true if the attribute has been modified
     */
    public boolean isPreviousFireTimeModified() { return isPreviousFireTimeModified; }

    /**
     * returns whether State has been modified
     *
     * @return true if the attribute has been modified
     */
    public boolean isStateModified() { return isStateModified; }

	public boolean isPreviousFireTimeFromModified() {
		return isPreviousFireTimeFromModified;
	}

	public void setPreviousFireTimeFrom(Date previousFireTimeFrom) {
		isPreviousFireTimeFromModified = true;
    	super.setPreviousFireTimeFrom(previousFireTimeFrom);
	}

	public boolean isPreviousFireTimeToModified() {
		return isPreviousFireTimeToModified;
	}

	public void setPreviousFireTimeTo(Date previousFireTimeTo) {
    	isPreviousFireTimeToModified = true;
		super.setPreviousFireTimeTo(previousFireTimeTo);
	}

	public boolean isNextFireTimeFromModified() {
		return isNextFireTimeFromModified;
	}

	public void setNextFireTimeFrom(Date nextFireTimeFrom) {
    	this.isNextFireTimeFromModified = true;
		super.setNextFireTimeFrom(nextFireTimeFrom);
	}

	public boolean isNextFireTimeToModified() {
		return isNextFireTimeToModified;
	}

	public void setNextFireTimeTo(Date nextFireTimeTo) {
    	isNextFireTimeToModified = true;
		super.setNextFireTimeTo(nextFireTimeTo);
	}

	public void setStateCodes(List<Byte> states) {
    	this.isStateModified = true;
		super.setStateCodes(states);
	}
}
