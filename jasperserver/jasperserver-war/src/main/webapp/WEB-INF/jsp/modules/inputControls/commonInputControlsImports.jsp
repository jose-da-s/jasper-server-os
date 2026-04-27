<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="js" uri="/WEB-INF/jasperserver.tld" %>

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

<%--Common IC constants--%>
<%@ page import="com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.InputControlHandler"%>

<script type="text/javascript">
    <js:xssNonce type="javascript"/>

    __jrsConfigs__.inputControlsConstants = {};
    __jrsConfigs__.inputControlsConstants.NULL_SUBSTITUTION_VALUE = "<%= InputControlHandler.NULL_SUBSTITUTION_VALUE %>";
    __jrsConfigs__.inputControlsConstants.NULL_SUBSTITUTION_LABEL = "<%= InputControlHandler.NULL_SUBSTITUTION_LABEL %>";
    __jrsConfigs__.inputControlsConstants.NOTHING_SUBSTITUTION_VALUE = "<%= InputControlHandler.NOTHING_SUBSTITUTION_VALUE %>";
</script>

<%--Common IC functionality--%>
<c:if test="${isPro}">
    <script type="text/javascript">
        __jrsConfigs__.inputControlsConstants.DEFAULT_OPTION_TEXT = '<spring:message code="report.options.dialog.default.name" javaScriptEscape="true"/>';
        __jrsConfigs__.inputControlsConstants.CONTROL_DEFAULT_OPTION_TEXT = '<spring:message code="report.options.select.empty.label" javaScriptEscape="true"/>';
        __jrsConfigs__.inputControlsConstants.CONTROL_OPTIONS_TEXT = '<spring:message code="report.options.select.label" javaScriptEscape="true"/>';
    </script>
</c:if>
