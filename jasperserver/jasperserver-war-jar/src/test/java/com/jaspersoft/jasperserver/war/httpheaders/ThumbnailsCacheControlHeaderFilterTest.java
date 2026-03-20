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

package com.jaspersoft.jasperserver.war.httpheaders;

import org.junit.Test;

import jakarta.servlet.ServletException;
import java.io.IOException;

import static org.springframework.http.HttpMethod.GET;

/**
 * Created by schubar on 11/18/15.
 */
public class ThumbnailsCacheControlHeaderFilterTest extends BaseGetHttpHeaderFilterTest {

    @Test
    public void ensurePrivateIfReportHasBenExecuted() throws ServletException, IOException {
        givenConfig(
                rule(GET, "/rest_v2/thumbnails/.*",
                        header("Cache-Control", "private,no-cache,no-store,max-age=0,must-revalidate"),
                        header("Pragma", "no-cache")
                )
        );

        whenFilter(GET, "/rest_v2", "/thumbnails/public/Samples/Reports/01._Geographic_Results_by_Segment_Report");

        thenHeader("Cache-Control", "private,no-cache,no-store,max-age=0,must-revalidate");
        thenHeader("Pragma", "no-cache");
    }

    @Test
    public void ensurePrivateIfDefaultAllowedAndReportHasBenExecuted() throws ServletException, IOException {
        givenConfig(
                rule(GET, "/rest_v2/thumbnails/.+",
                        header("Cache-Control", "private,no-cache,no-store,max-age=0,must-revalidate"),
                        header("Pragma", "no-cache")
                )
        );

        whenFilter(GET, "/rest_v2", "/thumbnails/public/Samples/Reports/01._Geographic_Results_by_Segment_Report?defaultAllowed=true");

        thenHeader("Cache-Control", "private,no-cache,no-store,max-age=0,must-revalidate");
        thenHeader("Pragma", "no-cache");
    }

    @Test
    public void ensurePrivateIfDefaultNotAllowedAndReportHasBenExecuted() throws ServletException, IOException {
        givenConfig(
                rule(GET, "/rest_v2/thumbnails/.*\\?.*&?defaultAllowed=false",
                        header("Cache-Control", "private,no-cache,no-store,max-age=0,must-revalidate"),
                        header("Pragma", "no-cache")
                )
        );

        whenFilter(GET, "/rest_v2", "/thumbnails/public/Samples/Reports/01._Geographic_Results_by_Segment_Report?defaultAllowed=false");

        thenHeader("Cache-Control", "private,no-cache,no-store,max-age=0,must-revalidate");
        thenHeader("Pragma", "no-cache");
    }

    @Test
    public void ensurePrivateIfDefaultAllowedAndReportHasNotBenExecuted() throws ServletException, IOException {
        givenConfig(
                rule(GET, "/rest_v2/thumbnails/.*\\?.*&?defaultAllowed=true",
                        header("Cache-Control", "private,no-cache,no-store,max-age=0,must-revalidate"),
                        header("Pragma", "no-cache")
                )
        );

        whenFilter(GET, "/rest_v2", "/thumbnails/public/Samples/Reports/01._Geographic_Results_by_Segment_Report?defaultAllowed=true");

        thenHeader("Cache-Control", "private,no-cache,no-store,max-age=0,must-revalidate");
        thenHeader("Pragma", "no-cache");
    }

    @Test
    public void ensurePrivateIfDefaultNotAllowedAndReportHasNotBenExecuted() throws ServletException, IOException {
        // If no payload don't cache
        givenConfig(
                rule(GET, "/rest_v2/thumbnails/.*\\?.*&?defaultAllowed=false",
                        header("Cache-Control", "private,no-cache,no-store,max-age=0,must-revalidate"),
                        header("Pragma", "no-cache")
                )
        );

        whenFilter(GET, "/rest_v2", "/thumbnails/public/Samples/Reports/01._Geographic_Results_by_Segment_Report?defaultAllowed=false");

        thenHeader("Cache-Control", "private,no-cache,no-store,max-age=0,must-revalidate");
        thenHeader("Pragma", "no-cache");
    }

}

