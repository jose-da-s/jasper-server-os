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

<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="js" uri="/WEB-INF/jasperserver.tld" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/scripts/runtime_dependencies/jquery-ui/themes/redmond/jquery-ui-1.10.4-custom.css" type="text/css" media="screen">

<c:choose>
    <c:when test="${userLocale == null or empty userLocale}">
        <script type='text/javascript' src="${pageContext.request.contextPath}/scripts/runtime_dependencies/jquery-ui/ui/jquery.ui.datepicker-en.js"></script>
    </c:when>
    <c:when test="${userLocale == 'zh-CN' || userLocale == 'zh-TW' || userLocale == 'pt-BR'|| userLocale == 'pt_BR' || userLocale == 'zh_CN' || userLocale == 'zh_TW'}">
        <script type='text/javascript' src="${pageContext.request.contextPath}/scripts/runtime_dependencies/jquery-ui/ui/jquery.ui.datepicker-${fn:replace(userLocale, "_", "-")}.js"></script>
    </c:when>
    <c:otherwise>
        <script type='text/javascript' src="${pageContext.request.contextPath}/scripts/runtime_dependencies/jquery-ui/ui/jquery.ui.datepicker-${fn:substring(userLocale, 0,2)}.js"></script>
    </c:otherwise>
</c:choose>

<script type="text/javascript">
    <js:xssNonce type="javascript"/>

    jQuery.timepicker.setDefaults({
        timeText:'<spring:message code="CAL_time" javaScriptEscape="true"/>',
        hourText:'<spring:message code="CAL_hour" javaScriptEscape="true"/>',
        minuteText:'<spring:message code="CAL_min" javaScriptEscape="true"/>',
        secondText:'<spring:message code="CAL_second" javaScriptEscape="true"/>',
        currentText:'<spring:message code="CAL_now" javaScriptEscape="true"/>',
        closeText:'<spring:message code="CAL_close" javaScriptEscape="true"/>',
        timeFormat:'<spring:message code="calendar.time.format" javaScriptEscape="true"/>',
        dateFormat:'<spring:message code="calendar.date.format" javaScriptEscape="true"/>',
        separator:'<spring:message code="calendar.datetime.separator" javaScriptEscape="true"/>'
    });

    JRS.i18n["bundledCalendarFormat"] = '<spring:message code="calendar.date.format" javaScriptEscape="true"/>';
    JRS.i18n["bundledCalendarTimeFormat"] = '<spring:message code="calendar.time.format" javaScriptEscape="true"/>';
</script>

<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/runtime_dependencies/jquery-ui/ui/jquery.datepicker.extensions.js"> </script>