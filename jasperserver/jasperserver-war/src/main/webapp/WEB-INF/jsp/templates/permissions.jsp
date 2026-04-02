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
    Usage:permit user to add a file to the repository

Usage:

    <t:insertTemplate template="/WEB-INF/jsp/templates/permissions.jsp">
    </t:insertTemplate>

--%>

<%@ page import="com.jaspersoft.jasperserver.api.JSException" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="tx" uri="http://tiles.apache.org/tags-tiles-extras"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<tx:useAttribute id="containerClass" name="containerClass" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="bodyContent" name="bodyContent" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="selectFileButtonId" name="selectFileButtonId" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="cancelButtonId" name="cancelButtonId" classname="java.lang.String" ignore="true"/>



<t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
    <t:putAttribute name="containerID" value="permissions"/>
    <t:putAttribute name="containerClass">panel dialog overlay moveable sizeable centered_horz centered_vert permissions ${containerClass}</t:putAttribute>
    <t:putAttribute name="containerElements"><div class="sizer diagonal"></div></t:putAttribute>
    <t:putAttribute name="headerClass" value="mover"/>
    <t:putAttribute name="containerTitle"><spring:message code="permissionsDialog.title" javaScriptEscape="true"/>:&nbsp;<span class="path">the/path/to/the/object/here</span></t:putAttribute>
    <t:putAttribute name="bodyContent">
        <ul id="viewMode" class="list tabSet text control horizontal responsive" tabindex="0" role="tablist" js-navtype="tablist" aria-labelledby="permissionDialogTitle permissionsViewByTabLabel">
            <li role="none" class="label first" id="permissionsViewByTabLabel"><p class="wrap"><spring:message code="permissionsDialog.viewBy" javaScriptEscape="true"/>:</p></li>
            <li role="tab" class="tab tablistleaf"><p class="wrap button" tabindex="-1"><spring:message code="permissionsDialog.byUser" javaScriptEscape="true"/></p></li>
            <li role="tab" class="tab tablistleaf last selected"><p class="wrap button" tabindex="-1"><spring:message code="permissionsDialog.byRole" javaScriptEscape="true"/></p></li>
        </ul>
        <t:insertTemplate template="/WEB-INF/jsp/templates/control_searchLockup.jsp">
            <t:putAttribute name="containerID" value="searchPermissionsBox"/>
            <t:putAttribute name="inputID" value="permissionsSearchInput"/>
        </t:insertTemplate>
        <p class="message hint" tabindex="0"><spring:message code="permissionsDialog.hint" javaScriptEscape="true"/></p>
        <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
            <t:putAttribute name="containerID" value="permissionsListContainer"/>
            <t:putAttribute name="containerClass" value="control groupBox fillParent"/>
            <t:putAttribute name="bodyContent">${bodyContent}</t:putAttribute>
        </t:insertTemplate>
        <p class="message warning" tabindex="0"><spring:message code="permissionsDialog.navWarning" javaScriptEscape="true"/></p>
    </t:putAttribute>
    <t:putAttribute name="footerContent">
        <button id="permissionsApply" class="button action primary up"><span class="wrap"><spring:message code="button.apply"/></span><span class="icon"></span></button>
        <button id="permissionsOk" class="button action up"><span class="wrap"><spring:message code="button.ok"/></span><span class="icon"></span></button>
        <button id="permissionsCancel" class="button action up"><span class="wrap"><spring:message code="button.cancel"/></span><span class="icon"></span></button>
    </t:putAttribute>
</t:insertTemplate>
