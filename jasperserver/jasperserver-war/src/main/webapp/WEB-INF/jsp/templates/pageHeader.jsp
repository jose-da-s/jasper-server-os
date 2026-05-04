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
	This template used for a specific icon and text in the page body container title row

Usage:
    <t:insertTemplate template="/WEB-INF/jsp/templates/pageHeader.jsp">
        <t:putAttribute name="titleText">New Datasource</t:putAttribute>
        <t:putAttribute name="titleIconClass">datasource</t:putAttribute>
    </t:insertTemplate>

--%>

<%@ taglib prefix="tx" uri="http://tiles.apache.org/tags-tiles-extras"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<tx:useAttribute id="pageHeaderClass" name="pageHeaderClass" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="pageHeaderText" name="pageHeaderText" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="pageHeaderIconClass" name="pageHeaderIconClass" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="headerContent" name="headerContent" classname="java.lang.String" ignore="true"/>

<div class="pageHeader ${pageHeaderClass}">
    <div class="pageHeader-title">
        <div class="pageHeader-title-icon ">
            <span class="icon ${pageHeaderIconClass}"></span>
        </div>
        <div role="heading" aria-level="1" class="pageHeader-title-text">${pageHeaderText}</div>
        <c:if test="${isEmbeddedDesigner}">
            <div class="pageHeader-title-controls pageHeader-controls-right">
                <div id="closeDesigner" class="closeIcon" js-nonmodal-tabindex="undefined" tabindex="-1"></div>
            </div>
        </c:if>
    </div>
    <c:if test="${headerContent != null}">
        ${headerContent}
    </c:if>
</div>
