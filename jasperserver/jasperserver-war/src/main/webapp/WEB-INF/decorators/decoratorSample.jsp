<%--
  ~ Copyright (C) 2025-2026 the Jasper Server OS Authors
  ~ SPDX-License-Identifier: AGPL-3.0-or-later
  ~ Copyright (C) 2005-2023. Cloud Software Group, Inc. All Rights Reserved.
  ~ http://www.jaspersoft.com.
  ~
  ~ Unless you have purchased a commercial license agreement from Jaspersoft,
  ~ the following license terms apply:
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program. If not, see <http://www.gnu.org/licenses/>.
  --%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page contentType="text/html; charset=utf-8" %>

<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="/WEB-INF/jasperserver.tld" prefix="js" %>



<script>
    var urlContext = "${pageContext.request.contextPath}";
</script>

<head>
    <js:xssNonce/>

    <meta http-equiv="Content-Type" content="text/html; charset=us-ascii" />

    <title>Quartz Layout Sample</title>

    <!-- General JasperServer Styles -->
    <link rel="stylesheet" href="themes/reset.css" type="text/css" media="screen">
    <link rel="stylesheet" href="themes/typography.css" type="text/css" media="screen">
    <!-- Theme -->
    <link rel="stylesheet" href="themes/pods/main.css" type="text/css" />
</head>

<body style="overflow:hidden" onresize="layoutManager.initialize()">

    <div id="context" class="container">
        <div class="header">
            <span id="logo"></span>
            <form id="globalSearch" class="searchWrapper">
                <input type="text" class="rndCorners-all"/>
                <button class="submit up"></button>
            </form>

        </div>
        <div id="frame" class="setMyHeight pod rndCorners-all">
            <div class="header rndCorners-top">
                <div class="navigation">
                    <ul class="sections" role="menubar">
                        <li tabindex="0" role="menuitem"><h4>Home</h4></li>
                        <li tabindex="-1" role="menuitem"><h4>View</h4></li>
                        <li tabindex="-1" role="menuitem"><h4>Manage</h4></li>
                        <li tabindex="-1" role="menuitem"><h4>Create</h4></li>
                    </ul>
                </div>
            </div>

            <div id="display" class="setMyHeight container">
<!--
        <decorator:body />
-->
                <div id="leftPod" class=" setMyHeight pod rndCorners-all">
                     <div class="header rndCorners-top">
                        <div class="title"><h4>Filters</h4></div>
                     </div>
                     <div id="searchFilters" class="content setMyHeight">
                        <ul>
                            <li>All results</li>
                            <li class="selected">Edited by me</li>
                            <li>Viewed by me</li>
                            <li>My Favorites</li>
                            <li>&nbsp;</li>
                        </ul>
                        <ul>
                            <li>Any time</li>
                            <li class="selected">Past week</li>
                            <li>Past month</li>
                            <li>&nbsp;</li>
                        </ul>
                        <ul>
                            <li>All types</li>
                            <li class="selected">Reports &amp; Dashboards</li>
                            <li>Scheduled</li>
                            <li>&nbsp;</li>
                        </ul>


                     </div>
                     <div class="footer"></div>
                </div>
                <div id="contentPod" class="setMyHeight setMyWidth pod rndCorners-all">
                     <div class="header setMyWidth rndCorners-top">
                        <div class="title"><h4>Search Results</h4></div>
                     </div>
                     <div class="toolbar setMyWidth"></div>
                     <div class="content setMyWidth setMyHeight">
                        <ul>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                            <li>item</li>
                        </ul>
                     </div>
                     <div class="footer setMyWidth"></div>
                </div>
                <!--div id="rightPod" class="hidden pod"></div-->

            </div><!-- display -->


        </div><!-- frame -->
        <div class="footer"></div>
    </div><!-- context -->

    <!-- JS Tool Scripts -->
    <script src="${pageContext.request.contextPath}/toolkit/utilities/external/prototype.js"></script>
    <script src="${pageContext.request.contextPath}/scripts/jasperserver.js"></script>
    <script src="${pageContext.request.contextPath}/scripts/ajax.js"></script>
    <script src="${pageContext.request.contextPath}/toolkit/utilities/common.js"></script>
    <script src="${pageContext.request.contextPath}/toolkit/tools/drag.js"></script>
    <script src="${pageContext.request.contextPath}/toolkit/parts/javascript/searchBox.js"></script>
    <script src="${pageContext.request.contextPath}/toolkit/tools/layoutManager.js"></script>

    <script>
        window.onResize = layoutManager.initialize;
    </script>


</body>
