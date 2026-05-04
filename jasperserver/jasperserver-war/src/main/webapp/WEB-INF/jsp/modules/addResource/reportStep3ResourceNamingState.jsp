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
<%@ taglib prefix="authz" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="js" uri="/WEB-INF/jasperserver.tld" %>

<script type="text/javascript">
    <js:xssNonce type="javascript"/>
    if (typeof localContext === "undefined") {
        localContext = {};
    }

    localContext.initOptions = {
        <js:out javaScriptEscape="true">
        organizationId: "${organizationId}",
        publicFolderUri: "${publicFolderUri}",
        expectedType: "${expectedFileType}",
        folderURI: "${fileResource.fileResource.parentFolder}",
        isEditMode: ${fileResource.editMode},
        type: "folder",
        resourceIdNotSupportedSymbols: "${resourceIdNotSupportedSymbols}",
        isBrowseActive: ${fileResource.subflowMode && (fileResource.fileResource.fileType == 'olapMondrianSchema' || fileResource.fileResource.fileType == 'accessGrantSchema')}
        </js:out>
    };

    if (typeof resource === "undefined") {
        resource = {};
    }

    if (typeof resource.messages === "undefined") {
        resource.messages = {};
    }

    resource.messages['resource.Report.Title'] ='<spring:message code="resource.Report.Title" javaScriptEscape="true"/>';
    resource.messages["labelToLong"] = '<spring:message code="FileResourceValidator.error.too.long.fileResource.label" javaScriptEscape="true"/>';
    resource.messages["labelIsEmpty"] = '<spring:message code="FileResourceValidator.error.not.empty.fileResource.label" javaScriptEscape="true"/>';
    resource.messages["resourceIdToLong"] = '<spring:message code="FileResourceValidator.error.too.long.fileResource.name" javaScriptEscape="true"/>';
    resource.messages["resourceIdIsEmpty"] = '<spring:message code="FileResourceValidator.error.not.empty.fileResource.name" javaScriptEscape="true"/>';
    resource.messages["resourceIdInvalidChars"] = '<spring:message code="FileResourceValidator.error.invalid.chars.fileResource.name" javaScriptEscape="true"/>';
    resource.messages["descriptionToLong"] = '<spring:message code="FileResourceValidator.error.too.long.fileResource.description" javaScriptEscape="true"/>';

    if (typeof __jrsConfigs__.addJasperReport === "undefined") {
        __jrsConfigs__.addJasperReport = {};
    }

    __jrsConfigs__.addJasperReport.localContext = localContext;
    __jrsConfigs__.addJasperReport.resource = resource;

</script>