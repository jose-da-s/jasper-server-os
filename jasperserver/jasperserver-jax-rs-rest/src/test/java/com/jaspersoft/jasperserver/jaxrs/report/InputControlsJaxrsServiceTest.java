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

package com.jaspersoft.jasperserver.jaxrs.report;

import com.jaspersoft.jasperserver.dto.reports.ReportParameters;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.junit.Before;
import org.junit.Test;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.PathSegment;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static com.jaspersoft.jasperserver.jaxrs.report.InputControlsJaxrsService.INCLUDE_TOTAL_COUNT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InputControlsJaxrsServiceTest {

    private InputControlsJaxrsService service = new InputControlsJaxrsService();
    private PathSegment segment;
    @Before
    public void setup() {
        segment = new PathSegment() {
            @Override
            public String getPath() {
                return "Cascading_state_multi_select";
            }

            @Override
            public MultivaluedMap<String, String> getMatrixParameters() {
                return new MultivaluedStringMap();
            }
        };
    }

    @Test
    public void getInputControlIdsFromPathSegment_withValidPath() {
        Set<String> actualValue = service.getInputControlIdsFromPathSegment(segment);
        assertTrue(actualValue.contains( "Cascading_state_multi_select"));
    }

    @Test
    public void getInputControlIdsFromPathSegment_withMarixParameters() {
        MultivaluedMap<String, String> map = new MultivaluedHashMap();
        map.put("Country_multi_select", new ArrayList<>());
        PathSegment segment_1 = new PathSegment() {
            @Override
            public String getPath() {
                return "Cascading_state_multi_select";
            }

            @Override
            public MultivaluedMap<String, String> getMatrixParameters() {
                return new MultivaluedStringMap(map);
            }
        };
        Set<String> actualValue = service.getInputControlIdsFromPathSegment(segment_1);
        assertEquals(actualValue.size(), 2);
    }

    @Test
    public void getMapWithTotalCount_checkTotalCountParameter() {
        Map<String, String[]> parameters = service.getParametersWithTotalCount(new ReportParameters(), true);
        String[] actualValue = parameters.get(INCLUDE_TOTAL_COUNT);
        assertEquals("true", actualValue[0]);
    }
}
