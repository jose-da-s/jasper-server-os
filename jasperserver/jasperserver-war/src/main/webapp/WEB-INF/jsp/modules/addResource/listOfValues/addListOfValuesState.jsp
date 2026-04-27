<%@ page import="com.jaspersoft.jasperserver.war.common.JasperServerConst" %>
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
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="js" uri="/WEB-INF/jasperserver.tld" %>

<c:set var="MAX_LENGTH_NAME" value="<%=JasperServerConst.MAX_LENGTH_NAME%>" />
<c:set var="MAX_LENGTH_LABEL" value="<%=JasperServerConst.MAX_LENGTH_LABEL%>" />
<c:set var="MAX_LENGTH_DESC" value="<%=JasperServerConst.MAX_LENGTH_DESC%>" />

<script type="text/javascript">
    <js:xssNonce type="javascript"/>
    if (typeof localContext === "undefined") {
        localContext = {};
    }

    localContext.initOptions = {
        <js:out javaScriptEscape="true">
        isEditMode: ${listOfValuesDTO.editMode},
        resourceIdNotSupportedSymbols: "${resourceIdNotSupportedSymbols}"
        </js:out>
    };


    if (typeof resource === "undefined") {
        resource = {};
    }

    if (typeof resource.messages === "undefined") {
        resource.messages = {};
    }

    resource.messages["labelToLong"] = '<spring:message code="ListOfValuesValidator.error.too.long.listOfValues.label" arguments="${MAX_LENGTH_LABEL}" javaScriptEscape="true"/>';
    resource.messages["labelIsEmpty"] = '<spring:message code="ListOfValuesValidator.error.not.empty.listOfValues.label" javaScriptEscape="true"/>';
    resource.messages["resourceIdToLong"] = '<spring:message code="ListOfValuesValidator.error.too.long.listOfValues.name" arguments="${MAX_LENGTH_NAME}" javaScriptEscape="true"/>';
    resource.messages["resourceIdIsEmpty"] = '<spring:message code="ListOfValuesValidator.error.not.empty.listOfValues.name" javaScriptEscape="true"/>';
    resource.messages["resourceIdInvalidChars"] = '<spring:message code="ListOfValuesValidator.error.invalid.chars.listOfValues.name" javaScriptEscape="true"/>';
    resource.messages["descriptionToLong"] = '<spring:message code="ListOfValuesValidator.error.too.long.listOfValues.description" arguments="${MAX_LENGTH_DESC}" javaScriptEscape="true"/>';
    resource.messages["itemNameIsEmpty"] = '<spring:message code="ListOfValuesValidator.error.not.empty.newLabel" javaScriptEscape="true"/>';
    resource.messages["itemValueIsEmpty"] = '<spring:message code="ListOfValuesValidator.error.not.empty.newValue" javaScriptEscape="true"/>';

    if (typeof __jrsConfigs__.addListOfValues === "undefined") {
        __jrsConfigs__.addListOfValues = {};
    }

    __jrsConfigs__.addListOfValues.localContext = localContext;
    __jrsConfigs__.addListOfValues.resource = resource;
</script>