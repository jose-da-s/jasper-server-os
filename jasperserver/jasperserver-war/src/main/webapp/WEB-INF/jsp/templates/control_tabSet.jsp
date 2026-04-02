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

    <tiles:insertTemplate template="/WEB-INF/jsp/templates/control_tabSet.jsp">
        <tiles:putAttribute name="type" value="[REQUIRED (text|buttons)]"/>
        <tiles:putAttribute name="containerId" value="[OPTIONAL]"/>
        <tiles:putAttribute name="containerClass" value="[OPTIONAL]"/>
        <tiles:putListAttribute name="tabset">
            <tiles:addListAttribute>
                <tiles:addAttribute>[REQUIRED (tab ID)]</tiles:addAttribute>
                <tiles:addAttribute>[REQUIRED (tab Title)]</tiles:addAttribute>
                <tiles:addAttribute>[OPTIONAL (selected)]</tiles:addAttribute>
            </tiles:addListAttribute>
            <tiles:addListAttribute>
                <tiles:addAttribute>[REQUIRED (tab ID)]</tiles:addAttribute>
                <tiles:addAttribute>[REQUIRED (tab Title)]</tiles:addAttribute>
                <tiles:addAttribute>[OPTIONAL (selected)]</tiles:addAttribute>
            </tiles:addListAttribute>
        </tiles:putListAttribute>
    </tiles:insertTemplate>

--%>

<%@ page import="com.jaspersoft.jasperserver.api.JSException" %>
<%@ page import="java.util.Arrays" %>

<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib prefix="tx" uri="http://tiles.apache.org/tags-tiles-extras"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<tx:useAttribute id="type" name="type" classname="java.lang.String" ignore="false"/>
<tx:useAttribute id="containerId" name="containerId" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="containerClass" name="containerClass" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="tabset" name="tabset" classname="java.util.List" ignore="false"/>

<%
    String[] types = {"text", "buttons"};
    Arrays.sort(types);

    if (Arrays.binarySearch(types, type) < 0) {
        throw new JSException("Tubs of type \"" + type + "\" is not supported!");
    }
    if (tabset.size() == 0) { throw new JSException("Attribute \"tabset\" can't be empty list."); }
%>

<c:set var="isTabSelected" value="${false}"/>


<ul <c:if test="${not empty containerId}">id="<tiles:getAsString name="containerId"/>"</c:if> class="control tabSet ${type}${' '}${containerClass}${' '}${type == 'text' ? 'responsive' : ''}">
    <c:forEach var="tab" items="${tabset}" varStatus="status">
        <c:set var="extra" value=""/>
        <c:set var="selected" value=""/>

        <c:if test="${not isTabSelected and not empty tab.value[2] and tab.value[2] eq 'selected'}">
            <c:set var="isTabSelected" value="${true}"/>
            <c:set var="selected" value="selected"/>
        </c:if>

        <c:choose>
            <c:when test="${status.first}">
                <c:set var="extra" value="first"/>
            </c:when>
            <c:when test="${status.last}">
                <c:set var="extra" value="last"/>
            </c:when>
        </c:choose>

        <li class="tab ${extra}${' '}${selected}">
            <a id="<tiles:insertAttribute value="${tab.value[0]}"/>" class="button">
                <span class="wrap"><tiles:insertAttribute value="${tab.value[1]}"/></span>
            </a>
        </li><!--/.tab-->

    </c:forEach>
</ul><!--/.control-->
