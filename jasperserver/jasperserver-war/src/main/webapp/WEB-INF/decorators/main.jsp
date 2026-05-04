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

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page" %>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="authz"%>

<c:choose>
    <c:when test="${param['_flowId'] == 'viewReportFlow'}">
        <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </c:when>
    <c:otherwise>
        <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
    </c:otherwise>
</c:choose>

<decorator:usePage id="thePage" />
<c:set var="pageProperties" value="${thePage.properties}"/>
<c:choose>
    <c:when test="${pageProperties['meta.formName']!=null}"><c:set var="formName" value="${pageProperties['meta.formName']}"/></c:when>
    <c:otherwise><c:set var="formName" value="mainForm"/></c:otherwise>
</c:choose>
<c:choose>
    <c:when test="${pageProperties['meta.formMethod']!=null}"><c:set var="formMethod" value="${pageProperties['meta.formMethod']}"/></c:when>
    <c:otherwise><c:set var="formMethod" value="GET"/></c:otherwise>
</c:choose>
<c:set var="pageType" value="${pageProperties['meta.pageType']}"/>

<%--main decorator inclusion--%>
<c:choose>
    <c:when test="${param['viewAsDashboardFrame'] == 'true' || param['decorate'] == 'no' || param['sessionDecorator'] == 'no' || sessionScope['sessionDecorator'] == 'no'}">
        <%@ include file="minimalDecorator.jsp" %>
    </c:when>
    <c:otherwise>
        <%@ include file="decorator.jsp" %>
    </c:otherwise>
</c:choose>