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
	This special info panel is used only by the system to update the user when actions complete. There is only ever one of these at a time.

Usage:
    <tiles:insertTemplate template="/WEB-INF/jsp/templates/systemConfirm.jsp">
        <tiles:putAttribute name="containerID" value="[OPTIONAL]"/>
        <tiles:putAttribute name="containerClass" value="[OPTIONAL]"/>
        <tiles:putAttribute name="bodyID" value="[OPTIONAL]"/>
        <tiles:putAttribute name="bodyClass" value="[OPTIONAL]"/>
        <tiles:putAttribute name="messageContent">
            [OPTIONAL]
        </tiles:putAttribute>
    </tiles:insertTemplate>
--%>

<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib prefix="tx" uri="http://tiles.apache.org/tags-tiles-extras"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<tx:useAttribute id="containerID" name="containerID" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="containerClass" name="containerClass" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="bodyID" name="bodyID" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="bodyClass" name="bodyClass" classname="java.lang.String" ignore="true"/>

<tx:useAttribute id="messageContent" name="messageContent" classname="java.lang.String" ignore="true"/>


<tiles:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
    <tiles:putAttribute name="containerID">${containerID}</tiles:putAttribute>
    <tiles:putAttribute name="containerClass">panel info system ${containerClass}</tiles:putAttribute>
    <tiles:putAttribute name="bodyID">${bodyID}</tiles:putAttribute>
    <tiles:putAttribute name="bodyClass">${bodyClass}</tiles:putAttribute>
    <tiles:putAttribute name="bodyContent">
		<p class="message">
            ${messageContent}
		</p>
    </tiles:putAttribute>
</tiles:insertTemplate>