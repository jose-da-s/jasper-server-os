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


package com.jaspersoft.jasperserver.war.action;

import org.junit.Test;
import org.springframework.validation.DataBinder;
import org.springframework.beans.MutablePropertyValues;

import static org.junit.Assert.*;

import com.jaspersoft.jasperserver.war.dto.ReportUnitWrapper;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.client.ReportUnitImpl;

public class ReportUnitActionBindingTest {

    @Test
    public void bindAllowedFields_withoutSuppressedFields() {
        ReportUnitWrapper wrapper = new ReportUnitWrapper();
        wrapper.setReportUnit(new ReportUnitImpl());

        DataBinder binder = new DataBinder(wrapper);
        new ReportUnitAction().initBinder(null, binder);

        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.add("reportUnit.label", "Label");
        pvs.add("reportUnit.name", "name_id");
        pvs.add("reportUnit.description", "Desc");
        pvs.add("reportUnit.controlsLayout", "1");
        pvs.add("reportUnit.alwaysPromptControls", "true");
        pvs.add("reportUnit.inputControlRenderingView", "/path/view.jsp");
        pvs.add("reportUnit.reportRenderingView", "/path/report.jsp");
        pvs.add("source", "FILE_SYSTEM");
        pvs.add("jrxmlData", "abc".getBytes());
        pvs.add("jrxmlUri", "/repo/path/main.jrxml");
        pvs.add("inputControlSource", "REPO");
        pvs.add("inputControlPath", "/repo/ic");

        binder.bind(pvs);

        assertEquals(0, binder.getBindingResult().getSuppressedFields().length);
        assertFalse(binder.getBindingResult().hasFieldErrors());

       
    }
}
