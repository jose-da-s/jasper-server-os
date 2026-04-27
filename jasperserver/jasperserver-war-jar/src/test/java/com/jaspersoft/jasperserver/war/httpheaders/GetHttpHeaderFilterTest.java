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
public class GetHttpHeaderFilterTest extends BaseGetHttpHeaderFilterTest {

    @Test
    public void ensureOnlyFirstRuleApplied() throws ServletException, IOException {
        givenConfig(
                rule(GET, "/rest_v2/export",
                        header("Cache-Control", "max-age=86400,public"),
                        header("Pragma", ""),
                        jrsExpires(86400)
                ),
                rule(GET, "/rest_v2/export", header("Cache-Control", "private"))
        );

        whenFilter(GET, "/rest_v2", "/export");

        thenHeader("Cache-Control", "max-age=86400,public");
        thenHeader("Pragma", "");
        thenHeader("Expires", "Sun, 22 Nov 2015 02:41:20 GMT");
    }

    @Test
    public void ensureOnlyFirstRuleAppliedReverse() throws ServletException, IOException {
        givenConfig(
                rule(GET, "/rest_v2/export", header("Cache-Control", "private")),
                rule(GET, "/rest_v2/export",
                        header("Cache-Control", "max-age=86400,public"),
                        header("Pragma", ""),
                        jrsExpires(86400)
                )
        );

        whenFilter(GET, "/rest_v2", "/export");

        thenHeader("Cache-Control", "private");
    }

    @Test
    public void ensureHtmPageRuleApplied() throws ServletException, IOException {
        givenConfig(
                rule(GET, "/index\\.htm", header("Cache-Control", "private")),
                rule(GET, "/.*?\\.htm$",
                        header("Cache-Control", "max-age=86400,public"),
                        header("Pragma", ""),
                        jrsExpires(86400)
                )
        );

        whenFilter(GET, "", "/index.htm");

        thenHeader("Cache-Control", "private");
    }

    @Test
    public void ensureGenericJsOrHtmRuleAppliedForHtm() throws ServletException, IOException {
        givenConfig(
                rule(GET, "/index\\.htm", header("Cache-Control", "private")),
                rule(GET, "/.*?\\.([hH][tT][mM]|[jJ][sS]|[cC][sS][sS])$",
                        header("Cache-Control", "max-age=86400,public"),
                        header("Pragma", ""),
                        jrsExpires(86400)
                )
        );

        whenFilter(GET, "/scripts", "/template.htm");

        thenHeader("Cache-Control", "max-age=86400,public");
        thenHeader("Pragma", "");
        thenHeader("Expires", "Sun, 22 Nov 2015 02:41:20 GMT");
    }

    @Test
    public void ensureGenericJsOrHtmRuleAppliedForJs() throws ServletException, IOException {
        givenConfig(
                rule(GET, "/index\\.htm", header("Cache-Control", "private")),
                rule(GET, "/.*?\\.([hH][tT][mM]|[jJ][sS]|[cC][sS][sS])$",
                        header("Cache-Control", "max-age=86400,public"),
                        header("Pragma", ""),
                        jrsExpires(86400)
                )
        );

        whenFilter(GET, "/scripts", "/my.js");

        thenHeader("Cache-Control", "max-age=86400,public");
        thenHeader("Pragma", "");
        thenHeader("Expires", "Sun, 22 Nov 2015 02:41:20 GMT");
    }

    @Test
    public void ensureGenericJsOrHtmRuleAppliedForServletPath() throws ServletException, IOException {
        givenConfig(
                rule(GET, "/index\\.htm", header("Cache-Control", "private")),
                rule(GET, "/.*?\\.([hH][tT][mM]|[jJ][sS]|[cC][sS][sS])$",
                        header("Cache-Control", "max-age=86400,public"),
                        header("Pragma", ""),
                        jrsExpires(86400)
                )
        );

        whenFilter(GET, "/optimized-scripts/runtime_dependencies/jrs-ui/src/login/loginMain.js", "");

        thenHeader("Cache-Control", "max-age=86400,public");
        thenHeader("Pragma", "");
        thenHeader("Expires", "Sun, 22 Nov 2015 02:41:20 GMT");
    }

    @Test
    public void ensureGenericJsOrHtmRuleIsNotApplied() throws ServletException, IOException {
        givenConfig(
                rule(GET, "/index\\.htm", header("Cache-Control", "private")),
                rule(GET, "/.*?\\.htm$",
                        header("Cache-Control", "max-age=86400,public"),
                        header("Pragma", ""),
                        jrsExpires(86400)
                )
        );

        whenFilter(GET, "", "login.html");

        thenNoHeader("Cache-Control", "private");
        thenNoHeader("Cache-Control", "max-age=86400,public");
        thenNoHeader("Pragma", "");
        thenNoHeader("Expires", "Sun, 22 Nov 2015 02:41:20 GMT");
    }

    @Test
    public void ensureRestCallCanBeConfigured() throws ServletException, IOException {
        givenConfig(
                rule(GET, "/rest_v2/export/[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}/state",
                        header("Cache-Control", "no-cache,no-store"),
                        header("Pragma", "no-cache"),
                        header("MustRevalidate", "no-cache")


                )
        );

        whenFilter(GET, "/rest_v2", "/export/53f3c1c4-ee3b-48ed-b83c-32dfab206b7d/state");

        thenHeader("Cache-Control", "no-cache,no-store");
        thenHeader("Pragma", "no-cache");
    }

    @Test
    public void ensurePatternIsSupportingDifferentLetterCaseFor_js() throws ServletException, IOException {
        givenConfig(
                rule(GET, "/.*?\\.([hH][tT][mM]|[jJ][sS]|[cC][sS][sS])$",
                        header("Cache-Control", "max-age=86400,public")
                )
        );

        whenFilter(GET, "/optimized-scripts/my.js", "");

        thenHeader("Cache-Control", "max-age=86400,public");
    }


    @Test
    public void ensurePatternIsSupportingDifferentLetterCaseFor_Js() throws ServletException, IOException {
        givenConfig(
                rule(GET, "/.*?\\.([hH][tT][mM]|[jJ][sS]|[cC][sS][sS])$",
                        header("Cache-Control", "max-age=86400,public")
                )
        );

        whenFilter(GET, "/optimized-scripts/my.js", "");

        thenHeader("Cache-Control", "max-age=86400,public");
    }


    @Test
    public void ensurePatternIsSupportingDifferentLetterCaseFor_jS() throws ServletException, IOException {
        givenConfig(
                rule(GET, "/.*?\\.([hH][tT][mM]|[jJ][sS]|[cC][sS][sS])$",
                        header("Cache-Control", "max-age=86400,public")
                )
        );

        whenFilter(GET, "/optimized-scripts/my.js", "");

        thenHeader("Cache-Control", "max-age=86400,public");
    }

    @Test
    public void ensurePatternIsSupportingDifferentLetterCaseFor_JS() throws ServletException, IOException {
        givenConfig(
                rule(GET, "/.*?\\.([hH][tT][mM]|[jJ][sS]|[cC][sS][sS])$",
                        header("Cache-Control", "max-age=86400,public")
                )
        );

        whenFilter(GET, "/optimized-scripts/my.js", "");

        thenHeader("Cache-Control", "max-age=86400,public");
    }

    @Test
    public void ensureHeaderIsNotAppliedIfExtensionJSSIsIncorrect() throws ServletException, IOException {
        givenConfig(
                rule(GET, "/.*?\\.([hH][tT][mM]|[jJ][sS]|[cC][sS][sS])$",
                        header("Cache-Control", "max-age=86400,public")
                )
        );

        whenFilter(GET, "/optimized-scripts/my.jss", "");

        thenNoHeader("Cache-Control", "max-age=86400,public");
    }

    @Test
    public void ensureHeaderIsNotAppliedIfExtensionJJSIsIncorrect() throws ServletException, IOException {
        givenConfig(
                rule(GET, "/.*?\\.([hH][tT][mM]|[jJ][sS]|[cC][sS][sS])$",
                        header("Cache-Control", "max-age=86400,public")
                )
        );

        whenFilter(GET, "/optimized-scripts/my.jjs", "");

        thenNoHeader("Cache-Control", "max-age=86400,public");
    }

    @Test
    public void ensureHeaderIsAppliedForBundels() throws ServletException, IOException {
        givenConfig(
                rule(GET, "/rest_v2/bundles(\\?.*&?expanded=true|/.+)",
                        header("Cache-Control", "no-transform,max-age=86400"),
                        header("Vary", "Accept-Language")
                )
        );

        whenFilter(GET, "/rest_v2/bundles/CommonBundle", "");

        thenHeader("Cache-Control", "no-transform,max-age=86400");
        thenHeader("Vary", "Accept-Language");
    }


}

