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
    Standard Confirm. Provide user an opportunity to cancel an irrevocable action.

Usage:
    <t:insertTemplate template="/WEB-INF/jsp/templates/standardConfirm.jsp">
        <t:putAttribute name="containerTitle">[OPTIONAL]</t:putAttribute>
        <t:putAttribute name="bodyContent">
            
        </t:putAttribute>
        <t:putAttribute name="okLabel" value="[OPTIONAL]"/>
        <t:putAttribute name="cancelLabel" value="[OPTIONAL]"/>
    </t:insertTemplate>
--%>

<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="tx" uri="http://tiles.apache.org/tags-tiles-extras"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="js" uri="/WEB-INF/jasperserver.tld" %>

<tx:useAttribute name="okLabel" id="okLabel" ignore="true" classname="java.lang.String"/>
<tx:useAttribute name="cancelLabel" id="cancelLabel" ignore="true" classname="java.lang.String"/>
<tx:useAttribute id="containerClass" name="containerClass" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="leftButtonId" name="leftButtonId" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="rightButtonId" name="rightButtonId" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="bodyContent" name="bodyContent" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="containerTitle" name="containerTitle" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="containerElements" name="containerElements" classname="java.lang.String" ignore="true"/>

<t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
    <t:putAttribute name="containerClass">panel dialog overlay standardConfirm moveable centered_horz centered_vert ${containerClass}</t:putAttribute>
    <t:putAttribute name="containerID" value="standardConfirm"/>
    <t:putAttribute name="containerAttributes" value="role='dialog' aria-describedby='standardConfirm' aria-modal='true'"/>
    <t:putAttribute name="containerElements">${containerElements}</t:putAttribute>
    <t:putAttribute name="containerTitle">
        <c:choose>
            <c:when test="${containerTitle == null}">
                <spring:message code="DIALOG_CONFIRM_TITLE"/>
            </c:when>
            <c:otherwise>${containerTitle}</c:otherwise>
        </c:choose>
    </t:putAttribute>
    <t:putAttribute name="headerClass" value="mover"/>
    <t:putAttribute name="bodyContent">
        ${bodyContent}
    </t:putAttribute>   
    <t:putAttribute name="footerContent">    
        <button id="${leftButtonId}" class="button action primary up"><span class="wrap"><c:choose><c:when test="${not empty okLabel}">${okLabel}</c:when><c:otherwise><spring:message code="DIALOG_CONFIRM_BUTTON_LABEL_OK"/></c:otherwise></c:choose></span><span class="icon"></span></button>
        <button id="${rightButtonId}" class="button action up"><span class="wrap"><c:choose><c:when test="${not empty cancelLabel}">${cancelLabel}</c:when><c:otherwise><spring:message code="DIALOG_CONFIRM_BUTTON_LABEL_CANCEL"/></c:otherwise></c:choose></span><span class="icon"></span></button>
    </t:putAttribute>
</t:insertTemplate>

<script id="standardConfirmTemplate" type="text/mustache">
    <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
        <t:putAttribute name="containerClass">panel dialog overlay standardConfirm moveable centered_horz centered_vert</t:putAttribute>
        <t:putAttribute name="containerTitle">
            <spring:message code="DIALOG_CONFIRM_TITLE"/>
        </t:putAttribute>
        <t:putAttribute name="containerElements">${containerElements}</t:putAttribute>
        <t:putAttribute name="headerClass" value="mover"/>
        <p class="message"></p>
        <t:putAttribute name="footerContent">
            <js:xssNonce/>
            <button class="button action primary up ok">
                <span class="wrap"><spring:message code="DIALOG_CONFIRM_BUTTON_LABEL_OK"/></span>
                <span class="icon"></span>
            </button>
            <button class="button action up cancel">
                <span class="wrap"><spring:message code="DIALOG_CONFIRM_BUTTON_LABEL_CANCEL"/></span>
                <span class="icon"></span>
            </button>
        </t:putAttribute>
    </t:insertTemplate>
</script>
