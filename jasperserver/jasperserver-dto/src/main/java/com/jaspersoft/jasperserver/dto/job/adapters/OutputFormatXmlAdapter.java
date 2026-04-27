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

package com.jaspersoft.jasperserver.dto.job.adapters;

import com.jaspersoft.jasperserver.dto.common.OutputFormat;
import com.jaspersoft.jasperserver.dto.job.wrappers.ClientReportJobOutputFormatsWrapper;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashSet;
import java.util.Set;

/**
 * This adapter is used for ReportJob.outputFormats serialization.
 * ClientReportJobOutputFormatsWrapper is used for serialization because of no ability inFolder use @XmlElementWrapper together with @XmlJavaTypeAdapter.
 * See http://java.net/jira/browse/JAXB-787
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */

public class OutputFormatXmlAdapter extends XmlAdapter<ClientReportJobOutputFormatsWrapper, Set<OutputFormat>> {

    @Override
    public Set<OutputFormat> unmarshal(ClientReportJobOutputFormatsWrapper v) throws Exception {
        Set<OutputFormat> result = null;
        if (v != null && v.getFormats() != null && !v.getFormats().isEmpty()) {
            result = new HashSet<OutputFormat>();
            for (String currentValue : v.getFormats()) {
                OutputFormat outputFormat = OutputFormat.valueOf(currentValue);
                result.add(outputFormat);
            }
        }
        return result;
    }

    @Override
    public ClientReportJobOutputFormatsWrapper marshal(Set<OutputFormat> v) throws Exception {
        ClientReportJobOutputFormatsWrapper result = null;
        if (v != null && !v.isEmpty()) {
            Set<String> set = new HashSet<String>();
            for (OutputFormat currentValue : v) {
                String currentStringValue = currentValue.name();
                set.add(currentStringValue);
            }
            result = new ClientReportJobOutputFormatsWrapper();
            result.setFormats(set);
        }
        return result;
    }

}
