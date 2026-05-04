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
<%@ page import="com.jaspersoft.jasperserver.war.common.HeartbeatBean" %>

<%@ page contentType="text/html; charset=utf-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="authz" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="js" uri="/WEB-INF/jasperserver.tld" %>

<js:out javaScriptEscape="true">
<script type="text/javascript">
    <js:xssNonce type="javascript"/>

    // Initialization of repository search init object.
    var heartbeatInitOptions = {
        baseUrl: "${pageContext.request.contextPath}",
        showDialog: false,
        sendClientInfo: false
    };

<%
    HeartbeatBean heartbeat = (HeartbeatBean)application.getAttribute("concreteHeartbeatBean");
%>
<authz:authorize access="hasRole('ROLE_ADMINISTRATOR')">
    <%
        if (heartbeat != null && heartbeat.haveToAskForPermissionNow()) {
    %>
    heartbeatInitOptions.showDialog = true;
    <%
        }
    %>
</authz:authorize>
<authz:authorize access="hasAnyRole('ROLE_USER','ROLE_ADMINISTRATOR')">
    <%
        if (heartbeat != null && heartbeat.isMakingCalls()
                && session != null && session.getAttribute("jsHeartbeatSentClientInfo") == null) {
            session.setAttribute("jsHeartbeatSentClientInfo", Boolean.TRUE);
    %>
    heartbeatInitOptions.sendClientInfo = true;
    <%
        }
    %>
</authz:authorize>
</script>
</js:out>
