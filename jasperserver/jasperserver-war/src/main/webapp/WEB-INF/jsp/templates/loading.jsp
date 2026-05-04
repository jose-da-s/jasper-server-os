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
    <t:insertTemplate template="/WEB-INF/jsp/templates/loading.jsp">
        <t:putAttribute name="containerClass" value="cancellable">
    </t:insertTemplate>
    
    Use cancellable option only when load can be cancelled.
--%>

<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="tx" uri="http://tiles.apache.org/tags-tiles-extras"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<%@ page import="com.jaspersoft.jasperserver.api.JSException" %>


<tx:useAttribute id="containerClass" name="containerClass" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="containerID" name="containerID" classname="java.lang.String" ignore="true"/>

<div id="loading" class="jr-mDialog  jr-mDialogModal jr dialog loading movable centered_horz centered_vert ${containerClass}" style="position: absolute;">
    <div class="jr-mDialog-header jr mover">
        <h1 class="jr-mDialog-header-title jr"><spring:message code='jsp.loading'/></h1>
    </div>

    <div class="jr-mDialog-body jr-mDialog-bodyPadded jr">
        <div class="jr-mOverlay jr-mOverlayClear jr">
            <div class="jr-mSpinner jr-mSpinnerPlain jr"></div>
        </div>
    </div>

    <div class="jr-mDialog-footer jr">
        <button id="cancel" class="jr-mButton jr-mButtonText jr-mButtonPrimary jr-uWidth-100pc jr" aria-label="<spring:message code="button.cancel"/> ">
            <span class="jr-mButton-label jr"><spring:message code="button.cancel"/></span>
        </button>
    </div>
</div>

