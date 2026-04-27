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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="../jsp/modules/common/jsEdition.jsp" %>

<meta name = "viewport" content="initial-scale=1.0, width=device-width">
<meta name="apple-mobile-web-app-capable" content="yes"/>

<%-- JavaScript and CSS resources that are common to all or most pages, doesn't include decorations-related code --%>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/runtime/${not empty runtimeHash ? runtimeHash : jsOptimizationProperties.runtimeHash}/themes/reset.css" type="text/css" media="screen">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/<spring:theme code="themeMinimal.css"/>" type="text/css" media="screen,print"/>

    <!--[if IE 7.0]>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/<spring:theme code="overrides_ie7.css"/>" type="text/css" media="screen"/>
    <![endif]-->

    <!--[if IE 8.0]>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/<spring:theme code="overrides_ie8.css"/>" type="text/css" media="screen"/>
    <![endif]-->

    <!--[if IE]>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/<spring:theme code="overrides_ie.css"/>" type="text/css" media="screen"/>
    <![endif]-->

    <c:if test="${empty openInEditor or openInEditor == false}">
      <link rel="stylesheet" href="${pageContext.request.contextPath}/<spring:theme code="jasper-ui/jasper-ui.css"/>" type="text/css" media="screen,print"/>
    </c:if>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/<spring:theme code="jquery-ui/jquery-ui.css"/>" type="text/css" media="screen,print"/>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/<spring:theme code="panel.css"/>" type="text/css" media="screen,print"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/<spring:theme code="menu.css"/>" type="text/css" media="screen,print"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/<spring:theme code="simpleColorPicker.css"/>" type="text/css" media="screen,print"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/<spring:theme code="webPageView.css"/>" type="text/css" media="screen,print"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/<spring:theme code="notifications.css"/>" type="text/css" media="screen,print"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/<spring:theme code="attributes.css"/>" type="text/css" media="screen,print"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/<spring:theme code="manageTenants.css"/>" type="text/css" media="screen,print"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/<spring:theme code="pagination.css"/>" type="text/css" media="screen,print"/>
    <c:if test="${param.decorate == 'no'}">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/<spring:theme code="overrides_custom.css"/>" type="text/css" media="screen"/>
    </c:if>
<%@ include file="../jsp/modules/common/jrsConfigs.jsp" %>