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

<js:out javaScriptEscape="true">
<script type="text/javascript">
    <js:xssNonce type="javascript"/>
    if (typeof localContext === "undefined") {
        localContext = {};
    }

    localContext.initOptions = {
        editMode: "${connectionWrapper.editMode}",
        type: "${connectionWrapper.type}",
        ParentFolderUri: "${parentFolder}"
    };

    if (typeof resourceMondrianLocate === "undefined") {
        resourceMondrianLocate = {};
    }

    if (typeof resourceMondrianLocate.messages === "undefined") {
        resourceMondrianLocate.messages = {};
    }

    resourceMondrianLocate.messages["resource.AnalysisConnectionMmondrianLocate.Title"]  = '<spring:message code="resource.AnalysisConnectionMmondrianLocate.Title" javaScriptEscape="true"/>';

    if (typeof __jrsConfigs__.locateConnectionSource === "undefined") {
        __jrsConfigs__.locateConnectionSource = {};
    }

    __jrsConfigs__.locateConnectionSource.localContext = localContext;
    __jrsConfigs__.locateConnectionSource.resourceMondrianLocate = resourceMondrianLocate;

</script>
</js:out>