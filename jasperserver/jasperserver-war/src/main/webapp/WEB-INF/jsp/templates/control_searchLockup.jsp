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

    <t:insertTemplate template="/WEB-INF/jsp/templates/control_searchLockup.jsp">
        <t:putAttribute name="containerID" value="[OPTIONAL]"/>
        <t:putAttribute name="containerClass" value="[OPTIONAL]"/>
        <t:putAttribute name="inputID" value="[OPTIONAL]"/>
        <t:putAttribute name="inputTabindex" value="[OPTIONAL]"/>
    </t:insertTemplate>

--%>

<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="t" %>
<%@ taglib prefix="tx" uri="http://tiles.apache.org/tags-tiles-extras"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<tx:useAttribute id="containerID" name="containerID" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="containerClass" name="containerClass" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="containerAttr" name="containerAttr" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="inputID" name="inputID" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="accClass" name="accClass" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="label" name="label" classname="java.lang.String" ignore="true"/>

<%--/WEB-INF/jsp/templates/control_searchLockup.jsp revision A--%>
<form role="search" id="${containerID}" class="control searchLockup ${containerClass}" ${containerAttr} <c:if test="${label != null}">aria-label="${label}"</c:if>>
    <div class="wrap">
        <input type="text" class="${accClass}" id="${inputID}" aria-label="<spring:message code='button.search' javaScriptEscape='true'/>"/>
    </div>
    <b class="right">
        <button class="button searchClear" aria-label="<spring:message code='button.clear' javaScriptEscape='true'/>"></button>
    </b>
    <button class="button search up" aria-label="<spring:message code='button.search' javaScriptEscape='true'/>"></button>
</form>

