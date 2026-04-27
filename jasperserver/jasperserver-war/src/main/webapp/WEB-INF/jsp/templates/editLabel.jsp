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
    Usage:

Usage:

    <t:insertTemplate template="/WEB-INF/jsp/templates/editLabel.jsp">
    </t:insertTemplate>

--%>

<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="tx" uri="http://tiles.apache.org/tags-tiles-extras"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<tx:useAttribute id="containerClass" name="containerClass" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="bodyContent" name="bodyContent" classname="java.lang.String" ignore="true"/>


<t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
    <t:putAttribute name="containerClass">panel dialog overlay editLabel moveable centered_horz centered_vert ${containerClass}</t:putAttribute>
    <t:putAttribute name="containerID" value="editLabel" />
    <t:putAttribute name="containerTitle">
        <spring:message code="ADH_1212_EDIT_LABEL_TITLE" javaScriptEscape="true"/>
    </t:putAttribute>
    <t:putAttribute name="headerClass" value="mover"/>
    <t:putAttribute name="bodyContent">
    		<span id="add_title" class="hidden"><spring:message code="ADH_031_MENU_ADD_LABEL" javaScriptEscape="true"/></span>
    		<span id="edit_title" class="hidden"><spring:message code="ADH_1212_EDIT_LABEL_TITLE" javaScriptEscape="true"/></span>
            <fieldset class="group">
                <legend class="offLeft"><span>Label</span></legend>
                <label class="control input text" for="addRoleName" title="<spring:message code="ADH_1212_EDIT_LABEL_SUB_TITLE" javaScriptEscape="true"/>">
                    <span class="wrap"><spring:message code="ADH_1212_EDIT_LABEL_SUB_TITLE" javaScriptEscape="true"/>:</span>
                    <input class="" id="editLabelInput" type="text" maxlength="100" value=""/>
                </label>
			</fieldset>
    </t:putAttribute>
    <t:putAttribute name="footerContent">
            <button id="editLabelBtn" class="button action primary up"><span class="wrap"><spring:message code="ADH_1212_EDIT_LABEL_SUBMIT" javaScriptEscape="true"/></span><span class="icon"></span></button>
            <button id="cancelEditBtn" class="button action up"><span class="wrap"><spring:message code="ADH_1212_EDIT_LABEL_CANCEL" javaScriptEscape="true"/><span class="icon"></span></button>
    </t:putAttribute>
</t:insertTemplate>