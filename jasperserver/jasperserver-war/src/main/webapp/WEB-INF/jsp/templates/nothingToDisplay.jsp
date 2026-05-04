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
    Nothing To Display. Used to communicate to user that empty display is not an error.

Usage:
    <t:insertTemplate template="/WEB-INF/jsp/templates/nothingToDisplay.jsp">
        <t:putAttribute name="bodyContent"></t:putAttribute>
    </t:insertTemplate>
    
--%>

<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="tx" uri="http://tiles.apache.org/tags-tiles-extras"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ page import="com.jaspersoft.jasperserver.api.JSException" %>


<tx:useAttribute id="bodyContent" name="bodyContent" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="containerID" name="containerID" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="containerClass" name="containerClass" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="containerAttributes" name="containerAttributes" classname="java.lang.String" ignore="true"/>

<t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
	<t:putAttribute name="containerClass" value="panel info nothingToDisplay centered_vert centered_horz hidden ${containerClass}"/>

	<t:putAttribute name="containerAttributes" value="${not empty containerAttributes ? containerAttributes : 'style=\"position:absolute;top:33%;left:25%;bottom:33%;right:25%;min-width:100px;\"'}"/>
    <t:putAttribute name="containerID" value="${not empty containerID ? containerID : 'nothingToDisplay'}"/>
    <t:putAttribute name="bodyContent">
		${bodyContent}
    </t:putAttribute>
</t:insertTemplate>