<%@ page contentType="text/html; charset=utf-8" %>
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

<%--
Overview:
	This template used to enforce that each page must have a title, a body ID and a body class.

Usage:
	<tiles:insertTemplate template="/WEB-INF/jsp/templates/page.jsp">
	    <tiles:putAttribute name="pageTitle" value="[REQUIRED]"/>
	    <tiles:putAttribute name="moduleName" value="[OPTIONAL]"/>
	    <tiles:putAttribute name="bodyID" value="[REQUIRED]"/>
	    <tiles:putAttribute name="pageClass" value="[OPTIONAL]"/>
	    <tiles:putAttribute name="bodyClass" value="[REQUIRED]"/>
	    <tiles:putAttribute name="headerContent" >
			[OPTIONAL]
	    </tiles:putAttribute>
	    <tiles:putAttribute name="bodyContent" >
			[REQUIRED]
	    </tiles:putAttribute>
	</tiles:insertTemplate>
--%>

<%@ page import="com.jaspersoft.jasperserver.api.JSException" %>

<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib prefix="tx" uri="http://tiles.apache.org/tags-tiles-extras"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!--/WEB-INF/jsp/templates/page.jsp revision A-->
<tx:useAttribute id="pageTitle" name="pageTitle" classname="java.lang.String" ignore="false"/>
<tx:useAttribute id="bodyID" name="bodyID" classname="java.lang.String" ignore="false"/><%-- This should be renamed to 'pageID' but would require name change on all calling pages --%>
<tx:useAttribute id="pageClass" name="pageClass" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="bodyClass" name="bodyClass" classname="java.lang.String" ignore="false"/>
<tx:useAttribute id="moduleName" name="moduleName" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="decorate" name="decorate" classname="java.lang.String" ignore="true"/>

<%
    if (pageTitle.length() == 0) { throw new JSException("Attribute \"pageTitle\" can't be empty."); }
    if (bodyID.length() == 0) { throw new JSException("Attribute \"bodyID\" can't be empty."); }
    if (bodyClass.length() == 0) { throw new JSException("Attribute \"bodyClass\" can't be empty."); }
%>

<html>
<head>
    <title><tiles:getAsString name="pageTitle"/></title>
    <tiles:insertAttribute name="headerContent" ignore="true"/>
    <c:if test="${not empty moduleName}">
        <jsp:include page="../modules/commonScripts.jsp"/>
        <script language="JavaScript" src="${scriptsUri}/${moduleName}.js" defer></script>
    </c:if>
</head>
<body id="<tiles:getAsString name="bodyID"/>" class="<tiles:getAsString name="pageClass" ignore="true"/>">

<div class="<tiles:getAsString name="bodyClass"/>">
    <a name="maincontent" id="maincontent"></a>
    <tiles:insertAttribute name="bodyContent"/>
</div>
</body>
</html>
