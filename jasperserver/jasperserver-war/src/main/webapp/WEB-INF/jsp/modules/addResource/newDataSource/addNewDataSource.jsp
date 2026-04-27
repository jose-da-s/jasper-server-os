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

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="isAuthorized" value="false"/>
<c:set var="disableAwsDefaults" value="${not isEc2Instance or suppressEc2CredentialsWarnings}" />

<!--
<c:choose>
    <c:when test="${isProVersion}">
        <authz:authorize access="hasRole('ROLE_SUPERUSER')">
            <c:set var="isAuthorized" value="true"/>
        </authz:authorize>
    </c:when>
    <c:otherwise>
        <authz:authorize access="hasRole('ROLE_ADMINISTRATOR')">
            <c:set var="isAuthorized" value="true"/>
        </authz:authorize>
    </c:otherwise>
</c:choose>
-->

<t:insertTemplate template="/WEB-INF/jsp/templates/page.jsp">
    <t:putAttribute name="pageTitle">
        <c:choose>
            <c:when test="${dataResource.editMode}"><spring:message code="resource.datasource.jdbc.page.title.edit"/></c:when>
            <c:otherwise><spring:message code="resource.datasource.jdbc.page.title.new"/></c:otherwise>
        </c:choose>
    </t:putAttribute>
    <t:putAttribute name="bodyClass" value="oneColumn flow wizard oneStep"/>
    <t:putAttribute name="bodyID" value="addNewDataSource"/>
    <t:putAttribute name="moduleName" value="dataSource/dataSourceMain"/>
    <t:putAttribute name="headerContent">
        <jsp:include page="addNewDataSourceState.jsp"/>
    </t:putAttribute>

    <t:putAttribute name="bodyContent">
    </t:putAttribute>
</t:insertTemplate>
