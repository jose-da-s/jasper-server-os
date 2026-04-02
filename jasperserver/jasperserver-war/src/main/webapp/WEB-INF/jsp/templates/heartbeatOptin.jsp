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
    Heartbeat dialog.

Usage:
    <t:insertTemplate template="/WEB-INF/jsp/templates/heartbeatOptin.jsp">
        <t:putAttribute name="okLabel" value="[OPTIONAL]"/>
    </t:insertTemplate>
--%>

<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="tx" uri="http://tiles.apache.org/tags-tiles-extras"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>


<tx:useAttribute id="containerID" name="containerID" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="containerClass" name="containerClass" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="leftButtonId" name="leftButtonId" classname="java.lang.String" ignore="true"/>

<t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
    <t:putAttribute name="containerClass">panel dialog overlay heartbeatOptin moveable centered_horz centered_vert ${containerClass}</t:putAttribute>
    <t:putAttribute name="containerID">${containerID != null ? containerID : 'heartbeatOptin'}</t:putAttribute>
    <t:putAttribute name="containerTitle"><spring:message code="heartbeat.optin.title"/></t:putAttribute>
    <t:putAttribute name="headerClass" value="mover"/>
    <t:putAttribute name="bodyContent">
        <spring:message code="heartbeat.optin.message" htmlEscape="false"/>
        <div class="control checkBox">
            <label class="wrap" for="heartbeatCheck" title="<spring:message code="heartbeat.optin.tooltip"/>">
                <spring:message code="heartbeat.optin.label"/>
            </label>
            <input class="" id="heartbeatCheck" type="checkbox" checked="checked"/>
        </div>
    </t:putAttribute>   
    <t:putAttribute name="footerContent">    
        <button id="${leftButtonId}" class="button action primary up"><span class="wrap"><spring:message code="DIALOG_CONFIRM_BUTTON_LABEL_OK"/></span><span class="icon"></span></button>
    </t:putAttribute>
</t:insertTemplate>
