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
    Tab control to be used to permit switching
Usage:

    <t:insertTemplate template="/WEB-INF/jsp/templates/userCountExceeded.jsp">
        <t:putAttribute name="containerID" value="[OPTIONAL]"/>
        <t:putAttribute name="containerClass" value="[OPTIONAL]"/>
        <t:putAttribute name="inputID" value="[OPTIONAL]"/>
    </t:insertTemplate>

--%>

<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="t" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="tx" uri="http://tiles.apache.org/tags-tiles-extras"%>

<tx:useAttribute id="containerID" name="containerID" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="containerClass" name="containerClass" classname="java.lang.String" ignore="true"/>

<tiles:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
    <tiles:putAttribute name="containerID">${containerID}</tiles:putAttribute>
    <tiles:putAttribute name="containerClass">panel info system ${containerClass}</tiles:putAttribute>
    <tiles:putAttribute name="bodyID">${bodyID}</tiles:putAttribute>
    <tiles:putAttribute name="bodyClass">${bodyClass}</tiles:putAttribute>
    <tiles:putAttribute name="bodyContent">
		<p class="message" style="color:red;">
            <spring:message code="LIC_017_license.user.count.exceeded.menu"/>
		</p>
    </tiles:putAttribute>
</tiles:insertTemplate>


