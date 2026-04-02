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
package com.jaspersoft.jasperserver.api.engine.scheduling.domain.jaxb;

import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobRuntimeInformation;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
public class ReportJobStateXmlAdapter extends XmlAdapter<String, Byte>{

     public enum State {
        UNKNOWN(ReportJobRuntimeInformation.STATE_UNKNOWN),
        NORMAL(ReportJobRuntimeInformation.STATE_NORMAL),
        EXECUTING(ReportJobRuntimeInformation.STATE_EXECUTING),
        PAUSED(ReportJobRuntimeInformation.STATE_PAUSED),
        COMPLETE(ReportJobRuntimeInformation.STATE_COMPLETE),
        ERROR(ReportJobRuntimeInformation.STATE_ERROR);

        private final Byte byteValue;

        private State(Byte byteValue) {
            this.byteValue = byteValue;
        }
    }

    @Override
    public Byte unmarshal(String v) throws Exception {
         Byte result = null;
        if (v != null && !"".equals(v))
            try {
                result = State.valueOf(v).byteValue;
            } catch (IllegalArgumentException e) {
                result = State.UNKNOWN.byteValue;
            }
        return result;
    }

    @Override
    public String marshal(Byte v) throws Exception {
         String result = null;
        for (State state : State.values()) {
            if (state.byteValue.equals(v)) {
                result = state.name();
                break;
            }
        }
        return result != null ? result : State.UNKNOWN.name();
    }
}
