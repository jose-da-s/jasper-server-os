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
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="authz" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="js" uri="/WEB-INF/jasperserver.tld" %>

<js:out javaScriptEscape="true">
<script type="text/javascript">
    <js:xssNonce type="javascript"/>
    if (typeof localContext === "undefined") {
        localContext = {};
    }

    localContext.initOptions = {
        fileUploadInput : 'filePath',
        resourceInput : 'resourceUri',
        browseButton : 'browser_button',
        dialogTitle : '<spring:message code="resource.DataSource.Title" javaScriptEscape="true"/>',
        treeId: 'dsTreeRepoLocation',
        providerId: 'dsTreeDataProvider',
        selectLeavesOnly: true
    };

    if (typeof __jrsConfigs__.locateDataSource === "undefined") {
        __jrsConfigs__.locateDataSource = {};
    }

    __jrsConfigs__.locateDataSource.localContext = localContext;

</script>
</js:out>