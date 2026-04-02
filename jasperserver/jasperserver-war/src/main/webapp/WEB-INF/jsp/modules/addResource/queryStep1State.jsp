<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.jaspersoft.jasperserver.war.common.JasperServerConst" %>
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
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="js" uri="/WEB-INF/jasperserver.tld" %>

<c:set var="MAX_LENGTH_NAME" value="<%=JasperServerConst.MAX_LENGTH_NAME%>" />
<c:set var="MAX_LENGTH_LABEL" value="<%=JasperServerConst.MAX_LENGTH_LABEL%>" />

<script type="text/javascript">
    <js:xssNonce type="javascript"/>
    if (typeof localContext === "undefined") {
        localContext = {};
    }

    localContext.initOptions = {
        <js:out javaScriptEscape="true">
        isEditMode: ${query.editMode},
        resourceIdNotSupportedSymbols: "${resourceIdNotSupportedSymbols}"
        </js:out>
    };

    if (typeof resource === "undefined") {
        resource = {};
    }

    if (typeof resource.messages === "undefined") {
        resource.messages = {};
    }

    resource.messages["labelToLong"] = '<spring:message code="QueryValidator.error.too.long.query.label" arguments="${MAX_LENGTH_LABEL}" javaScriptEscape="true"/>';
    resource.messages["labelIsEmpty"] = '<spring:message code="QueryValidator.error.not.empty.query.label" javaScriptEscape="true"/>';
    resource.messages["resourceIdToLong"] = '<spring:message code="QueryValidator.error.too.long.query.name" arguments="${MAX_LENGTH_NAME}" javaScriptEscape="true"/>';
    resource.messages["resourceIdIsEmpty"] = '<spring:message code="QueryValidator.error.not.empty.query.name" javaScriptEscape="true"/>';
    resource.messages["resourceIdInvalidChars"] = '<spring:message code="QueryValidator.error.invalid.chars.query.name" javaScriptEscape="true"/>';
    resource.messages["descriptionToLong"] = '<spring:message code="QueryValidator.error.too.long.query.description" javaScriptEscape="true"/>';
    resource.messages["resource.Query.Title"] = '<spring:message code="resource.Query.Title" javaScriptEscape="true"/>';

    if (typeof __jrsConfigs__.addQuery === "undefined") {
        __jrsConfigs__.addQuery = {};
    }

    __jrsConfigs__.addQuery.localContext = localContext;
    __jrsConfigs__.addQuery.resource = resource;
</script>